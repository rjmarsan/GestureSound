/*
 *  NodeTreeManager.java
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
 *		02-Oct-05	created
 */

package de.sciss.jcollider.gui;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import de.sciss.jcollider.Group;
import de.sciss.jcollider.Node;
import de.sciss.jcollider.NodeEvent;
import de.sciss.jcollider.NodeListener;
import de.sciss.jcollider.NodeWatcher;

/**
 *  A class that helps to set up a <code>JTree</code> with a view of
 *	a node graph. Since <code>Node</code> already implements the
 *	<code>TreeNode</code> interface, what we do here is to install
 *	a <code>NodeListener</code> on a provided <code>NodeWatcher</code>
 *	and keep a <code>TreeModel</code> up-to-date. In many cases
 *	you will not need to set up a <code>JTree</code> yourself,
 *	but simply create an instance of <code>NodeTreePanel</code>.
 *	<p>
 *	Because of the asynchronous nature of node manipulation,
 *	we have decided to not use the <code>MutableTreeNode</code> interface
 *	which cannot account for method failures, for example. The proposed
 *	implementation therefore is, to call the node's methods like
 *	<code>free</code> or <code>run</code> directly, and the tree will be
 *	updated as soon as the <code>&quot;/n_go&quot;</code>, <code>&quot;/n_off&quot;</code> etc.
 *	messages arrive.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.31, 08-Oct-07
 *
 *  @see		de.sciss.jcollider.gui.NodeTreePanel
 *  @see		de.sciss.jcollider.NodeWatcher
 *
 *	@todo		seems to miss /n_end when fired very quickly after /n_go ...
 */
public class NodeTreeManager
implements NodeListener
{
	/**
	 *	Set this to <code>true</code> for debugging
	 *	the incoming node notifications and tree model updates
	 */
	public boolean					VERBOSE			= false;

	private final DefaultTreeModel	model;
	private final NodeWatcher		nw;
	private final Map				mapNodeBackups	= new HashMap();
	
	/**
	 *	Creates a new <code>NodeTreeManager</code> for a given
	 *	<code>NodeWatcher</code> instance and root <code>Node</code>.
	 *
	 *	@param	nw			the nodewatcher to listen to for node updates.
	 *						the tree manager copies the current list of registered
	 *						nodes from the watcher and continuously updates the tree
	 *						when node notfication messages arrive.
	 *	@param	rootNode	the supposed root element in the tree display.
	 *						can be something like <code>server.getDefaultGroup()</code>.
	 */
	public NodeTreeManager( NodeWatcher nw, Node rootNode )
	{
		this.nw	= nw;
		model	= new DefaultTreeModel( rootNode, true );
		nw.addListener( this );
		
		final List	collNodes = nw.getAllNodes();
		Node		node;
		
		for( int i = 0; i < collNodes.size(); i++ ) {
			node	= (Node) collNodes.get( i );
			mapNodeBackups.put( new Integer( node.getNodeID() ), node );
		}
	}

	public NodeTreeManager( NodeWatcher nw )
	{
		this( nw, null );
	}
	
	public void setRoot( Node rootNode )
	{
		model.setRoot( rootNode );
	}
	
	/**
	 *	Disposes all resources associated with the tree
	 *	manager. This clears the node backup list and
	 *	stops listening to the node watcher. Call this
	 *	method before calling <code>dispose</code> on
	 *	the node watcher.
	 */
	public void dispose()
	{
		mapNodeBackups.clear();
		setRoot( null );
		nw.removeListener( this );
	}

	/**
	 *	Returns a <code>TreeModel</code> suitable to
	 *	creating a <code>JTree</code> gadget.
	 *
	 *	@return	a tree model that reflects the node graph
	 *			monitored by this manager
	 */
	public TreeModel getModel()
	{
		return model;
	}
	
	/**
	 *	Dumps a list of nodes to the given stream.
	 *	Starts at the provided root node and traverses
	 *	all child elements. Usefull for debugging.
	 *
	 *	@param	stream		a stream to print on, such as <code>System.err</code>
	 *	@param	rootNode	the top element of the tree, or <code>null</code> (no action)
	 */
	public static void dumpTree( PrintStream stream, TreeNode rootNode )
	{
		if( rootNode == null ) {
			stream.println( "Empty tree" );
		} else {
			dumpTree( stream, rootNode, 0 );
		}
	}
	
	private static void dumpTree( PrintStream stream, TreeNode node, int nestCount )
	{
		for( int i = 0; i < nestCount; i++ ) stream.print( "  " );
		if( node.isLeaf() ) {
			stream.println( "- " + node.toString() );
		} else {
			stream.println( node.toString() );
//			stream.println( node.toString() + " : " + node.getChildCount() + " leafs" );
			if( ++nestCount > 100 ) {
				stream.println( "\nNest count exceeds 100, probably closed loop. Terminating" );
			}
			int childCount = 0;
			for( Enumeration children = node.children(); children.hasMoreElements() && (childCount < 300); childCount++ ) {
				dumpTree( stream, (TreeNode) children.nextElement(), nestCount );
			}
			if( childCount == 300 ) {
				stream.println( "\nChild count exceeds 300, probably closed loop. Terminating" );
			}
		}
	}
	
// ---------- NodeListener interface ----------
	
	/**
	 *	This method is part of the <code>NodeListener</code> interface.
	 *	Do not call this method.
	 */
	public void nodeAction( NodeEvent e )
	{
		final Node		node	= e.getNode();
		if( node == null ) return;	// only if we've got a client representation

		final Integer	key		= new Integer( e.getNodeID() );
		final Group		group;
		final Group		groupBak;
		final Node		predBak;
		final int		idx;
	
		switch( e.getID() ) {
		case NodeEvent.GO:
			group = node.getGroup();
			if( group != null ) {
				idx = group.getIndex( node );
				if( VERBOSE ) System.err.println( "nodesWereInserted( "+group+", { "+idx+" }, { "+node+" })" );
				model.nodesWereInserted( group, new int[] { idx });
			}
			mapNodeBackups.put( key, node );
			break;

		case NodeEvent.END:
			groupBak	= (Group) mapNodeBackups.get( new Integer( e.getOldParentGroupID() ));
			predBak		= (Node)  mapNodeBackups.get( new Integer( e.getOldPredNodeID() ));
			if( groupBak != null ) {
				idx	= predBak == null ? 0 : groupBak.getIndex( predBak ) + 1;
				if( VERBOSE ) System.err.println( "nodesWereRemoved( "+groupBak+", { "+idx+" }, { "+node+" })" );
				model.nodesWereRemoved( groupBak, new int[] { idx }, new Object[] { node });
			}
			mapNodeBackups.remove( key );
			break;

		case NodeEvent.ON:
		case NodeEvent.OFF:
			if( VERBOSE ) System.err.println( "nodeChanged( "+node+")" );
			model.nodeChanged( node );
			break;

		case NodeEvent.MOVE:
			groupBak	= (Group) mapNodeBackups.get( new Integer( e.getOldParentGroupID() ));
			group		= node.getGroup();
			
			if( (group != null) && (groupBak != null) ) {
				final TreeNode[]	oldPath			= model.getPathToRoot( groupBak );
				final TreeNode[]	newPath			= model.getPathToRoot( group );
				TreeNode			commonParent	= (TreeNode) model.getRoot();
				int					nodeID;
				
				// now determine the nearest parent of both groups ; XXX does is make sense?
commonLp:		for( int i = 0; i < oldPath.length; i++ ) {
					nodeID = ((Node) oldPath[ i ]).getNodeID();
					for( int j = 0; j < newPath.length; j++ ) {
						if( nodeID == ((Node) newPath[ j ]).getNodeID() ) {
							commonParent = oldPath[ i ];
							break commonLp;
						}
					}
				}
				
				if( commonParent != null ) {
					if( VERBOSE ) System.err.println( "nodeStructureChanged( "+commonParent+")" );
					model.nodeStructureChanged( commonParent );
				} else {
					model.nodeStructureChanged( (TreeNode) model.getRoot() );
				}
			}
			break;
		
		case NodeEvent.INFO:
			group = node.getGroup();
			if( group != null ) {
				if( VERBOSE ) System.err.println( "nodeStructureChanged( "+group+")" );
				model.nodeStructureChanged( group );
			}
			mapNodeBackups.put( key, node );
			break;
			
		default:
			break;
		}
	}
}