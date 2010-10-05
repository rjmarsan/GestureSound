/*
 *  NodeWatcher.java
 *  JCollider
 *
 *  Copyright (c) 2004-2009 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	as published by the Free Software Foundation; either
 *	version 2, june 1991 of the License, or (at your option) any later version.
 *
 *	This software is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *	General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public
 *	License (gpl.txt) along with this software; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de , or visit http://www.sciss.de/jcollider
 *
 *
 *	JCollider is closely modelled after SuperCollider Language,
 *	often exhibiting a direct translation from Smalltalk to Java.
 *	SCLang is a software originally developed by James McCartney,
 *	which has become an Open Source project.
 *	See http://supercollider.sourceforge.net/ for details.
 *
 *
 *  Changelog:
 *		02-Oct-05	created
 *		30-Jul-06	public constructor has been removed. Use newFrom instead!
 *		17-Sep-06	fixed sync bug in listener notification; implemented queryAllNodes
 */

package de.sciss.jcollider;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.Set;
import javax.swing.Timer;

import de.sciss.app.BasicEvent;
import de.sciss.app.EventManager;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCPacket;

/**
 *	A node status managing class which has a similar concept
 *	as SCLang's NodeWatcher class, however the implementation is
 *	different and the feature set is different.
 *	<p>
 *	When a notification message arrives, it is deferred to the
 *	event dispatching thread. Interested objects can register
 *	a listener to receive <code>NodeEvent</code>s. Right before
 *	the listener's <code>nodeAction</code> method is called, the
 *	client side <code>Node</code> status is updated, i.e. the node
 *	linked to or unlinked from neighbouring nodes, the running
 *	playing flags are set etc.
 *	<p>
 *	Since these updates occur in the event thread, it is safe to
 *	use the <code>Node</code>'s <code>TreeNode</code> interface.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.33, 19-Mar-08
 *
 *	@synchronization	all methods are thread safe unless explicitely noted
 *						; however you should avoid to register a node at more than
 *						one active node watcher at a time
 */
public class NodeWatcher
implements EventManager.Processor, OSCResponderNode.Action, Constants, Runnable
{
	/**
	 *	Set this to <code>true</code> for debugging
	 *	all relevant actions of the watcher, such as starting,
	 *	stopping, registering nodes, and updating node status.
	 */
	public boolean						VERBOSE			= false;

	private EventManager				em				= null;		// lazy

	// no getter method now because we might
	// allow to use more than one server
	// in a future version
	protected final Server				server;
	
	private int							dumpMode		= kDumpOff;

	private boolean						watching		= false;
	private boolean						fireAllNodes	= false;
	private boolean						autoRegister	= false;
	
	private final Map					mapNodes		= new HashMap();	// maps Integer( nodeID ) to Node ; synchronized through 'sync'
	
	private final OSCResponderNode[]	resps;
	
	private final Object				sync			= new Object();
	private final List					collQueue		= new ArrayList();	// element = (OSCMessage) ; synchronized through 'sync'
	
	private static final Map			allInstances	= new HashMap();	// (String) Server.name to (NodeWatcher) instance

	private NodeWatcher( Server s )
	{
		this.server	= s;
		
		// create responders for all known node notification messages.
		final List collValidCmds = NodeEvent.getValidOSCCommands();
		resps = new OSCResponderNode[ collValidCmds.size() ];
		for( int i = 0; i < collValidCmds.size(); i++ ) {
			resps[ i ] = new OSCResponderNode( server, (String) collValidCmds.get( i ), this );
		}
	}
	
	/**
	 *	Returns a <code>NodeWatcher</code> to
	 *	monitor a given server. Note that the client must
	 *	receive notifications, which is true by default when
	 *	booting a server. By default, events are only fired
	 *	for registered nodes. To change this behaviour, call
	 *	<code>setFireAllNodes( true )</code>
	 *
	 *	@param	s	the server to which the nodes to be watched belong
	 *
	 *	@see	Server#notify( boolean )
	 */
	public static NodeWatcher newFrom( Server s )
	throws IOException
	{
		synchronized( allInstances ) {
			NodeWatcher nw;
			
			nw = (NodeWatcher) allInstances.get( s.getName() );
			if( nw == null ) {
				nw = new NodeWatcher( s );
				nw.start();
				allInstances.put( s.getName(), nw ); 
			}
			return nw;
		}
	}
	
	/**
	 *	Starts the OSC responders that trace incoming
	 *	node notification events.
	 */
	public void start()
	throws IOException
	{
		if( server.isRunning() && !server.isNotified() ) {
			Server.getPrintStream().println( "NodeWatcher warning: server does not receive notifications" );
		}
	
		synchronized( sync ) {
			try {
				for( int i = 0; i < resps.length; i++ ) {
					resps[ i ].add();
				}
				watching = true;
				if( VERBOSE ) System.err.println( "NodeWatcher.start()" );
			}
			catch( IOException e1 ) {
				stop();
				throw e1;
			}
		}
	}
	
	/**
	 *	Stops the OSC responders that trace incoming
	 *	node notification events.
	 */
	public void stop()
	throws IOException
	{
		synchronized( sync ) {
			watching = false;
			if( VERBOSE ) System.err.println( "NodeWatcher.stop()" );
			for( int i = 0; i < resps.length; i++ ) {
				resps[ i ].remove();
			}
		}
	}

	/**
	 *	Queries the watching state
	 *
	 *	@return	<code>true</code> if we are watching for node changes
	 *			(i.e. after calling <code>start</code>), <code>false</code> otherwise
	 *			(i.e. after creating the watcher or after calling <code>stop</code>).
	 */
	public boolean isWatching()
	{
		synchronized( sync ) {
			return watching;
		}
	}


//*unregister { arg node;
//	var watcher;
//	watcher = this.newFrom(node.server);
//	watcher.unregister(node);
//}

/**
	 *	Adds a node to the list of known nodes.
	 *	The node will be automatically removed, when
	 *	a corresponding <code>&quot;/n_end&quot;</code>
	 *	message arrives. Note that there is a little chance
	 *	that <code>&quot;/n_go&quot;</code> messages are
	 *	missed if you register a node <strong>after</strong> it's new-message
	 *	has been sent to the server. A safe way to register
	 *	nodes is to call the basic-new-commands and send the
	 *	new-message after the registration. for example:
	 *	<PRE>
	 *	Synth mySynth = Synth.basicNew( "mySynthDef", myServer );
	 *	myNodeWatcher.register( mySynth );
	 *	myServer.sendMsg( mySynth.newMsg( myTarget, myArgNames, myArgValues ));
	 *	</PRE>
	 *
	 *	@param	node	the node to register
	 *
	 *	@see	#setFireAllNodes( boolean )
	 */
	public void register( Node node )
	{
		register( node, false );
	}
	
	public void register( Node node, boolean assumePlaying )
	{
		synchronized( sync ) {
			if( watching ) {
				final Object key = new Integer( node.getNodeID() );
				if( assumePlaying && mapNodes.containsKey( key )) {
					node.setPlaying( true );
				}
				mapNodes.put( key, node );
				if( VERBOSE ) System.err.println( "NodeWatcher.register( " + node + " )" );
			}
		}
	}

	/**
	 *	Unregister a node, that is remove it from the list of
	 *	known nodes. Note that you usually need not call this
	 *	because when a node is automatically unregistered
	 *	when a <code>&quot;/n_end&quot;</code> for that node arrives.
	 *
	 *	@param	node	the node to unregister
	 */
	public void unregister( Node node )
	{
		synchronized( sync ) {
			mapNodes.remove( new Integer( node.getNodeID() ));
			if( VERBOSE ) System.err.println( "NodeWatcher.unregister( " + node + " )" );
		}
	}
	
	/**
	 *	Queries a list of all registered nodes.
	 *
	 *	@return	a list whose elements are of class <code>Node</code>. the list
	 *			itself is a copy and maybe modified. It will not be affected by
	 *			successive calls to <code>register</code> or <code>unregister</code>
	 */
	public List getAllNodes()
	{
		synchronized( sync ) {
			return new ArrayList( mapNodes.values() );
		}
	}

	/**
	 *	Registers a listener to be informed about
	 *	node status changes. Status changes occur
	 *	as of nodes being created, destroyed, paused, resumed,
	 *	moved, or as a result of sending a <code>/n_query</code>
	 *	message to the server. The <code>fireAllNodes</code>
	 *	flag determines whether all status changes are
	 *	forwarded to listeners, or only those for previously
	 *	registered nodes.
	 *
	 *	@param	l		listener to be added
	 *
	 *	@see	#setFireAllNodes( boolean )
	 */
	public synchronized void addListener( NodeListener l )
	{
		if( em == null ) em = new EventManager( this );
		em.addListener( l );
	}

	/**
	 *	Unregisters a listener from being informed about
	 *	node status changes.
	 *
	 *	@param	l		listener to be removed
	 */
	public void removeListener( NodeListener l )
	{
		if( em != null ) em.removeListener( l );
	}
	
	/**
	 *	@param	timeout		maximum time to wait for node info replies
	 *	@param	doneAction	to be executed when the tree has been queried (can be <code>null</code>)
	 */
	public void queryAllNodes( float timeout, ActionListener doneAction )
	{
		setFireAllNodes( true );
		setAutoRegister( true );
	
		final Set			setNodes		= new HashSet();
		final NodeListener	nl;
		final Timer			stopQueryTimer;

		stopQueryTimer = new Timer( (int) (timeout * 1000), null );
		
		nl = new NodeListener() {
			public void nodeAction( NodeEvent e )
			{
				if( e.getID() != NodeEvent.INFO ) return;

				final Node		n			= e.getNode();
				final List		nextNodes;
				
				if( setNodes.add( n )) {
					register( n );
					nextNodes = new ArrayList( 2 );
					if( e.getHeadNodeID() != -1 ) nextNodes.add( new Integer( e.getHeadNodeID() ));
					if( e.getSuccNodeID() != -1 ) nextNodes.add( new Integer( e.getSuccNodeID() ));
					if( !nextNodes.isEmpty() ) {
						try {
							server.sendMsg( new OSCMessage( "/n_query", nextNodes.toArray() ));
						}
						catch( IOException e1 ) {
							e1.printStackTrace();
						}
					}
				}

				stopQueryTimer.restart();
			}
		};

		stopQueryTimer.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e )
			{
				removeListener( nl );
			}
		});
		if( doneAction != null ) stopQueryTimer.addActionListener( doneAction );
		stopQueryTimer.setRepeats( false );
		stopQueryTimer.restart();
		
		addListener( nl );
		try {
			server.sendMsg( server.getDefaultGroup().queryMsg() );
		}
		catch( IOException e1 ) {
			e1.printStackTrace();
		}
	}

	/**
	 *	Removes all nodes from the list of known nodes.
	 */
	public void clear()
	{
		synchronized( sync ) {
			mapNodes.clear();
			if( VERBOSE ) System.err.println( "NodeWatcher.clear()" );
		}
	}
	
	/**
	 *	Disposes any resources
	 *	allocated by this representation.
	 *	This shuts down OSC communication
	 *	and server event dispatching.
	 *	It clear the list of registered nodes.
	 *	Do not use this object any more
	 *	after calling this method.
	 */
	public void dispose()
	{
		synchronized( allInstances ) {
			if( isWatching() ) {
				try {
					stop();
				}
				catch( IOException e1 ) {
					System.err.println( "NodeWatcher.dispose : " +
						e1.getClass().getName() + " : " + e1.getLocalizedMessage() );
				}
			}
			if( em != null ) em.dispose();
			clear();
			
			allInstances.remove( server.getName() );

			if( VERBOSE ) System.err.println( "NodeWatcher.dispose()" );
		}
	}

	/**
	 *	Changes the way incoming messages are dumped
	 *	to the console. By default incoming messages are not
	 *	dumped. The server's print stream is used to do
	 *	the dumping
	 *
	 *	@param	dumpMode	only <code>kDumpNone</code> and <code>kDumpText</code>
	 *						are supported at the moment.
	 *
	 *	@see	Server#dumpOSC( int )
	 */
	public void dumpIncomingOSC( int dumpMode )
	{
		this.dumpMode = dumpMode;
	}
	
	/**
	 *	Decides whether node changes for any node
	 *	or only for registered nodes are delivered to
	 *	event listeners.
	 *
	 *	@param	allNodes	<code>true</code> to deliver events
	 *						for all incoming node messages;
	 *						<code>false</code> to deliver events only
	 *						if the corresponding node was registered
	 */
	public void setFireAllNodes( boolean allNodes )
	{
		fireAllNodes	= allNodes;
	}

	/**
	 *	Queries the event dispatching mode. See <code>setFireAllNodes</code>
	 *	for details
	 *
	 *	@see	#setFireAllNodes( boolean )
	 */
	public boolean getFireAllNodes()
	{
		return fireAllNodes;
	}

	/**
	 *	Decides whether unknown nodes should be automatically
	 *	added to the node watcher (usefull for debugging).
	 *
	 *	@param	onOff	<code>true</code> to have nodes automatically created and
	 *					registered upon incoming notification events. <code>false</code>
	 *					to stop automatic registration.
	 */
	public void setAutoRegister( boolean onOff )
	{
		autoRegister	= onOff;
	}

	/**
	 *	Queries whether automatic node registration is enabled.
	 *
	 *	@see	#setAutoRegister( boolean )
	 */
	public boolean setAutoRegister()
	{
		return autoRegister;
	}

	// @synchronization	has to be called with sync on sync
	private void nodeGo( Node node, NodeEvent e )
	{
		final Group group	= (Group) mapNodes.get( new Integer( e.getParentGroupID() ));
		final Node	pred	= (Node) mapNodes.get( new Integer( e.getPredNodeID() ));
		final Node	succ	= (Node) mapNodes.get( new Integer( e.getSuccNodeID() ));
		
		node.setGroup( group );
		node.setPredNode( pred );
		node.setSuccNode( succ );
		if( pred != null ) pred.setSuccNode( node );
		if( succ != null ) succ.setPredNode( node );
		
		if( group != null ) {
			if( e.getPredNodeID() == -1 ) {
				group.setHeadNode( node );
			}
			if( e.getSuccNodeID() == -1 ) {
				group.setTailNode( node );
			}
		}

		node.setRunning( true );
		node.setPlaying( true );

		if( VERBOSE ) System.err.println( "NodeWatcher.nodeGo( " + node + " )" );
	}

	// @synchronization	has to be called with sync on mapNodes
	private void nodeEnd( Node node, NodeEvent e )
	{
		final Group group	= node.getGroup();
		final Node	pred	= node.getPredNode();
		final Node	succ	= node.getSuccNode();
//System.err.println( "Removing "+node );
//System.err.println( " ... pred "+pred );
//System.err.println( " ... succ "+succ );
	
		node.setGroup( null );
		node.setPredNode( null );
		node.setSuccNode( null );
		if( pred != null ) pred.setSuccNode( succ );
		if( succ != null ) succ.setPredNode( pred );
		
		if( group != null ) {
			if( (group.getHeadNode() != null) && (group.getHeadNode().getNodeID() == node.getNodeID()) ) {
				group.setHeadNode( succ );
			}
			if( (group.getTailNode() != null) && (group.getTailNode().getNodeID() == node.getNodeID()) ) {
				group.setTailNode( pred );
			}
		}

		node.setPlaying( false );
		node.setRunning( false );
		mapNodes.remove( new Integer( node.getNodeID() ));

		if( VERBOSE ) System.err.println( "NodeWatcher.nodeEnd( " + node + " )" );
	}

	// @synchronization	has to be called with sync on sync
	private void nodeMove( Node node, NodeEvent e )
	{
		final Group oldGroup	= node.getGroup();
		final Node	oldPred		= node.getPredNode();
		final Node	oldSucc		= node.getSuccNode();
	
		final Group newGroup	= (Group) mapNodes.get( new Integer( e.getParentGroupID() ));
		final Node	newPred		= (Node) mapNodes.get( new Integer( e.getPredNodeID() ));
		final Node	newSucc		= (Node) mapNodes.get( new Integer( e.getSuccNodeID() ));

		node.setGroup( newGroup );
		node.setPredNode( newPred );
		node.setSuccNode( newSucc );
		// needs to be done before setting new pred/succ
		if( oldPred != null ) oldPred.setSuccNode( oldSucc );
		if( oldSucc != null ) oldSucc.setPredNode( oldPred );
		if( newPred != null ) newPred.setSuccNode( node );
		if( newSucc != null ) newSucc.setPredNode( node );

		// needs to be done before setting new group
		if( oldGroup != null ) {
			if( (oldGroup.getHeadNode() != null) && (oldGroup.getHeadNode().getNodeID() == node.getNodeID()) ) {
				oldGroup.setHeadNode( oldSucc );
			}
			if( (oldGroup.getTailNode() != null) && (oldGroup.getTailNode().getNodeID() == node.getNodeID()) ) {
				oldGroup.setTailNode( oldPred );
			}
		}

		if( newGroup != null ) {
			if( e.getPredNodeID() == -1 ) {
				newGroup.setHeadNode( node );
			}
			if( e.getSuccNodeID() == -1 ) {
				newGroup.setTailNode( node );
			}
		}

		if( VERBOSE ) System.err.println( "NodeWatcher.nodeMove( " + node + " )" );
	}

// ----------- OSCResponderNode.Action interface -----------

	/**
	 *	This method is part of the implementation of the 
	 *	OSCResponderNode.Action interface. Do not call this method.
	 */
	public void respond( OSCResponderNode r, OSCMessage msg, long time )
	{
		if( dumpMode == kDumpText ) {
			OSCPacket.printTextOn( Server.getPrintStream(), msg );
		}

		if( autoRegister ) {
			synchronized( sync ) {
				final boolean invoke = collQueue.isEmpty();
				collQueue.add( msg );
				if( invoke ) EventQueue.invokeLater( this );
			}
			return;
		}
	
		final Integer nodeIDObj = (Integer) msg.getArg( 0 );
		
		synchronized( sync ) {
			if( mapNodes.containsKey( nodeIDObj )) {
				final boolean invoke = collQueue.isEmpty();
				collQueue.add( msg );
				if( invoke ) EventQueue.invokeLater( this );
			}
		}
	}

// ----------- Runnable interface -----------

	/**
	 *	Part of internal message queueing.
	 *	Never call this method.
	 */
	public void run()
	{
		final long			when;
		Integer				nodeIDObj;
		OSCMessage			msg;
		NodeEvent			nde;
		NodeListener		listener;
		Node				node;
		
		synchronized( sync ) {
			if( !watching ) return;

			when = System.currentTimeMillis();

			for( Iterator iter = collQueue.iterator(); iter.hasNext(); ) {
				msg			= (OSCMessage) iter.next();
				nodeIDObj	= (Integer) msg.getArg( 0 );
				node		= (Node) mapNodes.get( nodeIDObj );
				if( node == null ) {
					if( autoRegister ) {
						node = ((Number) msg.getArg( 4 )).intValue() == NodeEvent.GROUP ? (Node) Group.basicNew( server, nodeIDObj.intValue() ) : (Node) Synth.basicNew( null, server, nodeIDObj.intValue() );
						register( node );
					} else if( !fireAllNodes ) return;
				}
				nde = NodeEvent.fromOSCMessage( msg, this, when, node );

				if( node != null ) {	// update the node's fields
					switch( nde.getID() ) {
					case NodeEvent.GO:
						nodeGo( node, nde );
						break;

					case NodeEvent.END:
						nodeEnd( node, nde );
						break;

					case NodeEvent.ON:
						node.setPlaying( true );
						break;

					case NodeEvent.OFF:
						node.setPlaying( false );
						break;

					case NodeEvent.MOVE:
						nodeMove( node, nde );
						break;

					case NodeEvent.INFO:
						nodeMove( node, nde );
						break;
					
					default:
						assert false : nde.getID();
					}
				}
				
				if( em != null ) {
					// we are already in the event thread, so let's just call the listeners directly
					for( int i = 0; i < em.countListeners(); i++ ) {
						listener = (NodeListener) em.getListener( i );
						try {
							listener.nodeAction( nde );
						}
						catch( Exception e1 ) {
							e1.printStackTrace();
						}
					}
				}
			} // for iter
			
			collQueue.clear();
			
		} // sync
	} // run
	
// ----------- EventManager.Processor interface -----------

	/**
	 *	This is used to dispatch
	 *	node events. Do not call this method.
	 */
	public void processEvent( BasicEvent e )
	{
//		NodeListener		listener;
//		final NodeEvent		nde			= (NodeEvent) e;
//		final Node			node		= nde.getNode();
//
//		synchronized( sync ) {
//			if( !watching ) return;
//
//			if( node != null ) {	// update the node's fields
//				switch( e.getID() ) {
//				case NodeEvent.GO:
//					nodeGo( node, nde );
//					break;
//
//				case NodeEvent.END:
//					nodeEnd( node, nde );
//					break;
//
//				case NodeEvent.ON:
//					node.setPlaying( true );
//					break;
//
//				case NodeEvent.OFF:
//					node.setPlaying( false );
//					break;
//
//				case NodeEvent.MOVE:
//					nodeMove( node, nde );
//					break;
//
//				case NodeEvent.INFO:
//					nodeMove( node, nde );
//					break;
//				
//				default:
//					break;
//				}
//			}
//		
//			for( int i = 0; i < em.countListeners(); i++ ) {
//				listener = (NodeListener) em.getListener( i );
//System.err.println(" --> "+listener );
//				listener.nodeAction( nde );
//			}
//		} // synchronized( sync )
	}
}