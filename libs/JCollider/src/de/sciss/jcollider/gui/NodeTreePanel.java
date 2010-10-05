/*
 *  NodeTreePanel.java
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
 *		03-Oct-05	created
 */

package de.sciss.jcollider.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import de.sciss.jcollider.Group;
import de.sciss.jcollider.Node;
import de.sciss.jcollider.NodeWatcher;
import de.sciss.jcollider.Server;
import de.sciss.jcollider.Synth;
import de.sciss.net.OSCBundle;
import de.sciss.net.OSCMessage;

/**
 *	A panel that contains a tree view of the nodes as
 *	monitored by a <code>NodeWatcher</code>.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.31, 08-Oct-07
 *
 *  @see		de.sciss.jcollider.gui.NodeTreeManager
 */
public class NodeTreePanel
extends JPanel
implements TreeSelectionListener, TreeModelListener
{
	/**
	 *	Flag for the constructor: create a button
	 *	bar to message the nodes.
	 */
	public static final int	BUTTONS	= 0x01;

	private final NodeTreeManager	ntm;
	private final JTree				ggTree;
	private final Node				rootNode;
	
	protected boolean				selectionContainsSynths		= false;
	protected boolean				selectionContainsGroups		= false;
	protected boolean				selectionContainsPlaying	= false;
	protected boolean				selectionContainsPausing	= false;
	protected final List			collSelectedNodes			= new ArrayList();
	
	private ActionPauseResume	actionPauseResume			= null;
	private ActionFree			actionFree					= null;
	private ActionFreeAll		actionFreeAll				= null;
	private ActionDeepFree		actionDeepFree				= null;
	private ActionTrace		actionTrace					= null;

	/**
	 *	Creates a new <code>NodeTreePanel</code> for a given node watcher
	 *	and root element. See the <code>NodeTreeManager</code> constructor
	 *	for details. Don't forget to call the <code>dispose</code> method
	 *	when the component is not needed any more.
	 *
	 *	@param	nw			the node watcher to use for monitoring
	 *	@param	rootNode	the root element to display in the tree
	 *	@param	flags		flags that control what kind of gadgets are created
	 *						(e.g. <code>BUTTONS</code>).
	 *
	 *	@see	NodeTreeManager#NodeTreeManager( NodeWatcher, Node )
	 */
	public NodeTreePanel( NodeWatcher nw, Node rootNode, int flags )
	{
		super( new BorderLayout() );
		
		this.rootNode	= rootNode;
		ntm				= new NodeTreeManager( nw, rootNode );
		ggTree			= new JTree( ntm.getModel() );
		ggTree.setShowsRootHandles( true );
		ggTree.setCellRenderer( new TreeNodeRenderer() );
//		ggTree.getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
		ggTree.addTreeSelectionListener( this );
		ntm.getModel().addTreeModelListener( this );
						
		final JScrollPane ggScroll = new JScrollPane( ggTree,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );

		add( ggScroll, BorderLayout.CENTER );
		if( (flags & BUTTONS) != 0 ) {
			add( createButtons( flags ), BorderLayout.SOUTH );
		}
	}
	
	/**
	 *	Creates a panel with default gadgets.
	 */
	public NodeTreePanel( NodeWatcher nw, Node rootNode )
	{
		this( nw, rootNode, BUTTONS );
	}
	
	public NodeTreeManager getManager()
	{
		return ntm;
	}

	private JComponent createButtons( int flags )
	{
		final JToolBar	tb	= new JToolBar();
		AbstractButton	but;
	
		tb.setFloatable( false );
		actionPauseResume	= new ActionPauseResume();
		but					= new JButton( actionPauseResume );
//		but.setFont( fntGUI );
		tb.add( but );
		actionFree			= new ActionFree();
		but					= new JButton( actionFree );
//		but.setFont( fntGUI );
		tb.add( but );
		actionFreeAll		= new ActionFreeAll();
		but					= new JButton( actionFreeAll );
//		but.setFont( fntGUI );
		tb.add( but );
		actionDeepFree		= new ActionDeepFree();
		but					= new JButton( actionDeepFree );
//		but.setFont( fntGUI );
		tb.add( but );
		actionTrace			= new ActionTrace();
		but					= new JButton( actionTrace );
//		but.setFont( fntGUI );
		tb.add( but );

		return tb;
	}

	/**
	 *	Frees resources when the component is
	 *	not used any more. This will dispose
	 *	the internal <code>NodeTreeManager</code>.
	 */
	public void dispose()
	{
		ggTree.removeTreeSelectionListener( this );
		ntm.getModel().removeTreeModelListener( this );
		ntm.dispose();
	}

	/**
	 *	Creates a window containing this panel.
	 *	Do not try to create more than one window
	 *	for one panel or to attach the panel to
	 *	more than one container. The returned
	 *	frame will be visible and brought to the
	 *	front. The frame's default close operation
	 *	is <code>JFrame.DO_NOTHING_ON_CLOSE</code>,
	 *	so you will have to install a <code>WindowListener</code>
	 *	if the user shall be able to close the window.
	 */
	public JFrame makeWindow()
	{
		final JFrame		f	= new JFrame( "[" + rootNode.getServer().getName() + "] tree for " +
											  rootNode.toString() );
		final Container		cp	= f.getContentPane();
		
		f.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
		f.addWindowListener( new WindowAdapter() {
			public void windowClosed( WindowEvent e )
			{
				dispose();
			}
		});
		cp.setLayout( new BorderLayout() );
		cp.add( this, BorderLayout.CENTER );
		f.pack();
		f.setVisible( true );
		f.toFront();
		
		return f;
	}

	private void updateActions()
	{
		final TreePath[] sel = ggTree.getSelectionPaths();
	
		selectionContainsSynths		= false;
		selectionContainsGroups		= false;
		selectionContainsPlaying	= false;
		selectionContainsPausing	= false;
		collSelectedNodes.clear();
		
		if( sel != null ) {
			Node node;
			for( int i = 0; i < sel.length; i++ ) {
				node = (Node) sel[ i ].getLastPathComponent();
				collSelectedNodes.add( node );
				if( !selectionContainsSynths  && (node instanceof Synth) ) selectionContainsSynths  = true;
				if( !selectionContainsGroups  && (node instanceof Group) ) selectionContainsGroups  = true;
				if( !selectionContainsPlaying && node.isPlaying() )		   selectionContainsPlaying = true;
				if( !selectionContainsPausing && !node.isPlaying() )	   selectionContainsPausing = true;
			}
		}
		if( actionPauseResume != null )	actionPauseResume.update();
		if( actionFree != null )		actionFree.update();
		if( actionFreeAll != null )		actionFreeAll.update();
		if( actionDeepFree != null )	actionDeepFree.update();
		if( actionTrace != null )		actionTrace.update();
	}

// --------------- TreeSelectionListener interface ---------------

	/**
	 *	This method is part of the <code>TreeSelectionListener</code> interface.
	 *	Do not call this method.
	 */
	public void valueChanged( TreeSelectionEvent e )
	{
		updateActions();
	}
	
// --------------- TreeModelListener interface ---------------

	/**
	 *	This method is part of the <code>TreeModelListener</code> interface.
	 *	Do not call this method.
	 */
	public void treeNodesChanged( TreeModelEvent e )
	{
		updateActions();	// some actions change behaviour when nodes are paused/ resumed
	}

	/**
	 *	This method is part of the <code>TreeModelListener</code> interface.
	 *	Do not call this method.
	 */
	public void treeNodesInserted( TreeModelEvent e )
	{
		// updateActions();	// not necessary i think XXX
	}

	/**
	 *	This method is part of the <code>TreeModelListener</code> interface.
	 *	Do not call this method.
	 */
	public void treeNodesRemoved(TreeModelEvent e)
	{
		updateActions();	// because corresponding deselections are not fired
	}

	/**
	 *	This method is part of the <code>TreeModelListener</code> interface.
	 *	Do not call this method.
	 */
	public void treeStructureChanged(TreeModelEvent e)
	{
		updateActions();	// necessary? XXX
	}
	
// --------------- internal classes ---------------

	private abstract class NodeAction
	extends AbstractAction
	{
		protected NodeAction( String name )
		{
			super( name );
		}
	
		// bisschen aufwendig, aber evtl. wird es
		// NodeWatcher geben, die mehrere Server abhoeren
		public void actionPerformed( ActionEvent e )
		{
			final HashMap	mapServersToBundles	= new HashMap();
			OSCBundle		bndl;
			OSCMessage		msg;
			Server			server;
			Node			node;
		
			for( int i = 0; i < collSelectedNodes.size(); i++ ) {
				node		= (Node) collSelectedNodes.get( i );
				msg			= createMessage( node );
				if( msg == null ) continue;
				server		= node.getServer();
				bndl		= (OSCBundle) mapServersToBundles.get( server );
				if( bndl == null ) {
					bndl	= new OSCBundle();
					mapServersToBundles.put( server, bndl );
				}
				bndl.addPacket( msg );
			}
			
			for( Iterator iter = mapServersToBundles.keySet().iterator(); iter.hasNext(); ) {
				server = (Server) iter.next();
				try {
					server.sendBundle( (OSCBundle) mapServersToBundles.get( server ));
				}
				catch( IOException e1 ) {
					System.err.println( e1.getClass().getName() + " : " + e1.getLocalizedMessage() );
				}
			}
		}
		
		protected abstract OSCMessage createMessage( Node node );
		
		protected abstract void update();
	}

	private class ActionPauseResume
	extends NodeAction
	{
		private static final String	NAME_PAUSE	= "Pause";
		private static final String	NAME_RESUME	= "Resume";
	
		private boolean runFlag	= false;
	
		protected ActionPauseResume()
		{
			super( NAME_PAUSE );
			setEnabled( false );
		}
		
		protected OSCMessage createMessage( Node node )
		{
			return node.runMsg( runFlag );
		}
		
		protected void update()
		{
			runFlag	= selectionContainsPausing;
			setEnabled( (selectionContainsSynths  || selectionContainsGroups) &&
						(selectionContainsPlaying != selectionContainsPausing) );
			putValue( NAME, runFlag ? NAME_RESUME : NAME_PAUSE );
		}
	}

	private class ActionFree
	extends NodeAction
	{
		protected ActionFree()
		{
			super( "Free" );
			setEnabled( false );
		}
		
		protected OSCMessage createMessage( Node node )
		{
			return node.freeMsg();
		}
		
		protected void update()
		{
			setEnabled( selectionContainsSynths || selectionContainsGroups );
		}
	}

	private class ActionFreeAll
	extends NodeAction
	{
		protected ActionFreeAll()
		{
			super( "Free All" );
			setEnabled( false );
		}
		
		protected OSCMessage createMessage( Node node )
		{
			if( node instanceof Group ) {
				return ((Group) node).freeAllMsg();
			} else {
				return null;
			}
		}
		
		protected void update()
		{
			setEnabled( !selectionContainsSynths && selectionContainsGroups );
		}
	}

	private class ActionDeepFree
	extends NodeAction
	{
		protected ActionDeepFree()
		{
			super( "Deep Free" );
			setEnabled( false );
		}
		
		protected OSCMessage createMessage( Node node )
		{
			if( node instanceof Group ) {
				return ((Group) node).deepFreeMsg();
			} else {
				return null;
			}
		}
		
		protected void update()
		{
			setEnabled( !selectionContainsSynths && selectionContainsGroups );
		}
	}

	private class ActionTrace
	extends NodeAction
	{
		protected ActionTrace()
		{
			super( "Trace" );
			setEnabled( false );
		}
		
		protected OSCMessage createMessage( Node node )
		{
			return node.traceMsg();
		}
		
		protected void update()
		{
			setEnabled( selectionContainsSynths || selectionContainsGroups );
		}
	}

	private static class TreeNodeRenderer
	extends DefaultTreeCellRenderer
	{
// doch 'n bisschen krass
//		private static final Color	colrPlaying	= new Color( 0x00, 0x50, 0x30 );
		private static final Color	colrPlaying	= Color.black;
		private static final Color	colrPausing	= new Color( 0x90, 0x90, 0x90 );
		private static final Color	colrDied	= new Color( 0x90, 0x00, 0x30 );
	
		protected TreeNodeRenderer()
		{
			super();
		}

		public Component getTreeCellRendererComponent( JTree tree, Object value, boolean sel, boolean expanded,
													   boolean leaf, int row, boolean hasFocus )
		{
			// DefaultTreeCellRenderer will set up the JLabel properties
			super.getTreeCellRendererComponent( tree, value, sel, expanded, leaf, row, hasFocus );

			final Node node = (Node) value;
			
			if( node.isRunning() ) {
				if( node.isPlaying() ) {
					this.setForeground( colrPlaying );
				} else {
					this.setForeground( colrPausing );
				}
			} else {
				this.setForeground( colrDied );
			}

			return this;
		}
	}
}