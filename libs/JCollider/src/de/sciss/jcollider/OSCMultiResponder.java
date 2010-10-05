/*
 *  OSCresponderNode.java
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
 *	See http://www.audiosynth.com/ for details.
 *
 *
 *  Changelog:
 *		04-Aug-05	created
 *		26-Aug-05	removed potential null pointer exception in removeNode()
 *		30-Sep-06	modified to comply with new NetUtil version
 *		08-Apr-08	fixes potential locking problem in messageReceived
 */

package de.sciss.jcollider;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.sciss.net.OSCClient;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCListener;

/**
 *	Despite the name, the <code>OSCMultiResponder</code>
 *	mimics the SClang counter part only superficially.
 *	It absorbs the whole <code>OSCResponder</code> class
 *	and is based on the <code>NetUtil</code> OSC library.
 *	<p>
 *	While the super class <code>OSCReceiver</code> allows
 *	only a coarse message filtering, using the simple
 *	<code>OSCListener</code> interface, the <code>OSCMultiResponder</code>
 *	maintains a map of OSC command names and listeners
 *	(<code>OSCResponderNode</code>s) who wish to be
 *	informed about only this particular type of messages.
 *	<p>
 *	When a new node is added using the <code>addNode</code>
 *	method, the static list of all multi responders is searched
 *	for the given server address. If it exists, the corresponding
 *	multi responder is used, otherwise a new multi responder is
 *	created. Likewise, when <code>removeNode</code> is called,
 *	the multi responder checks if all nodes have been removed,
 *	and if so will terminate the OSC receiver.
 *	<p>
 *	To keep the responder permanently active, the server creates
 *	a multi responder for its address upon instantiation.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.33, 08-Apr-08
 */
public class OSCMultiResponder
// extends OSCReceiver
implements OSCListener
{
//	private static final Map			mapServerToMulti	= new HashMap();
	private final List					allNodes			= new ArrayList();
//	private final Map					mapAddrToCmds		= new HashMap();
	private final Map					mapCmdToNodes		= new HashMap();
//	private final SocketAddress			addr;

	private static final boolean		debug				= false; 
//	private final boolean				alwaysListening;
//	private final Runnable				startOrStop;
	
	private final OSCClient				c;
	
	private OSCResponderNode[]			resps				= new OSCResponderNode[ 2 ];
	private final Object				sync				= new Object();
  
	/**
	 *	Creates a new responder for the given
	 *	<code>OSCClient</code>. This is done by the server to
	 *	create a permanent listener. Users should
	 *	use the <code>OSCResponderNode</code> class
	 *	instead.
	 *
	 *	@param	c		the client to who's receiver we should listen
	 *
	 *	@see	OSCResponderNode
	 */
//	protected OSCMultiResponder( InetSocketAddress addr, boolean alwaysListening )
	protected OSCMultiResponder( final OSCClient c )
	throws IOException
	{
//		super( DatagramChannel.open(), addr );
	
		this.c					= c;
//		this.addr				= addr;
//		this.alwaysListening	= alwaysListening;
		
//		mapServerToMulti.put( s, this );

//		if( alwaysListening ) {
//if( debug ) System.err.println( "OSCMultiResponder( addr = " + this.addr +"; hash = "+hashCode() + " ): startListening" );
//			c.start();
			c.addOSCListener( this );
//			startOrStop	= null;
//		} else {
//			final OSCMultiResponder enc_this = this;
//			startOrStop = new Runnable() {
//				public void run()
//				{
//					synchronized( allNodes ) {
//						if( allNodes.isEmpty() ) {
//if( debug ) System.err.println( "OSCMultiResponder( addr = " + enc_this.addr +"; hash = "+enc_this.hashCode() + " ): startListening" );
//							c.start();
//							c.addOSCListener( enc_this );
//						} else {
//							c.removeOSCListener( enc_this );
//if( debug ) System.err.println( "OSCMultiResponder( addr = " + enc_this.addr +"; hash = " + enc_this.hashCode() + " ): stopListening" );
//							try {
//								c.stop();
//							}
//							catch( IOException e1 ) {
//								e1.printStackTrace( System.err );
//							}
//						}
//					}
//				}
//			};
//		}
	}

//private static DatagramChannel createChannel()
//throws IOException
//{
//	final DatagramChannel dch = DatagramChannel.open();
////	dch.socket().bind( new InetSocketAddress( "127.0.0.1", 0 ));
//	dch.socket().bind( new InetSocketAddress( InetAddress.getLocalHost(), 0 ));
//	return dch;
//}

//	private static DatagramChannel createChannel( SocketAddress addr )
//	throws IOException
//	{
//		DatagramChannel dch = DatagramChannel.open();
////		dch.connect( addr );
//		dch.socket().bind( null );
//		return dch;
//	}

//	protected static OSCMultiResponder addNode( Server s, OSCResponderNode node )
//	throws IOException
//	{
//		OSCMultiResponder	resp;
//
//		synchronized( mapServerToMulti ) {
//			resp = (OSCMultiResponder) mapServerToMulti.get( s );
////			if( resp == null ) {
////				resp = new OSCMultiResponder( s );
////			}
//		}
//		resp.addNode( node );
//		return resp;
//	}
	
	protected Object getSync()
	{
		return sync;
	}

	protected void addNode( OSCResponderNode node )
	throws IOException
	{
		List specialNodes;
	
		synchronized( sync ) {
//			if( allNodes.isEmpty() && !alwaysListening ) {
//				if( EventQueue.isDispatchThread() ) {
//					startOrStop.run();
//				} else {
//					EventQueue.invokeLater( startOrStop );
//				}
//			}
			allNodes.add( node );
			specialNodes = (List) mapCmdToNodes.get( node.getCommandName() );
			if( specialNodes == null ) {
				specialNodes = new ArrayList( 4 );
				mapCmdToNodes.put( node.getCommandName(), specialNodes );
			}
			specialNodes.add( node );
		}
	}

	protected void removeNode( OSCResponderNode node )
//	throws IOException
	{
		final List specialNodes;

		synchronized( sync ) {
			specialNodes = (List) mapCmdToNodes.get( node.getCommandName() );
			if( specialNodes != null ) {
				specialNodes.remove( node );
				allNodes.remove( node );
//				for( int i = 0; i < resps.length; i++ ) {
//					resps[ i ] = null;	// clear references
//				}
				if( allNodes.isEmpty() ) {
					mapCmdToNodes.clear();
//					if( !alwaysListening ) {
//						if( EventQueue.isDispatchThread() ) {
//							startOrStop.run();
//						} else {
//							EventQueue.invokeLater( startOrStop );
//						}
//					}
				}
			}
		}
	}
	
	protected void dispose()
	{
		synchronized( sync ) {
			c.removeOSCListener( this );
			//		mapAddrsToMultis.remove( this );
			allNodes.clear();
			mapCmdToNodes.clear();
//			if( resps.length > 0 ) resps = new OSCResponderNode[ 0 ];
//			resps = null;
			if( debug ) System.err.println( "OSCMultiResponder( client = " + c +"; hash = " + hashCode() + " ): dispose" );			
			c.dispose();
		}

//		try {
//			stopListening();
//			getChannel().close();
//		}
//		catch( IOException e1 ) {
//			e1.printStackTrace( System.err );
//		}
	}

//	// true if o is an OSCResponder
//	// with the same address and command name
//	public boolean equals( Object o )
//	{
//		if( o instanceof OSCResponder ) {
//			final OSCResponder resp = (OSCResponder) o;
//			return( this.cmdName.equals( o.cmdName ) && this.addr.equals( o.addr ));
//		} else {
//			return false;
//		}
//	}
//
//	// [...] so as to maintain the general contract for the hashCode method,
//	// which states that equal objects must have equal hash codes.
//	public int hashCode()
//	{
//		return( addr.hashCode() ^ cmdName.hashCode() );
//	}

// ------------ OSCListener interface ------------

	public void messageReceived( OSCMessage msg, SocketAddress sender, long time )
	{
		final				List	specialNodes;
		final				int		numResps;
	
		synchronized( sync ) {
			specialNodes = (List) mapCmdToNodes.get( msg.getName() );
			if( specialNodes == null ) return;
			numResps = specialNodes.size();
			resps = (OSCResponderNode[]) specialNodes.toArray( resps );
		}

		for( int i = 0; i < numResps; i++ ) {
			try {
				resps[ i ].messageReceived( msg, sender, time );
			}
			catch( Exception e ) {
				e.printStackTrace( Server.getPrintStream() );
			}
			resps[ i ] = null;
		}
	}
}