/*
 *  NodeEvent.java
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
 *		17-Sep-06	fixed bug in incorporate (now always returns false for simplicity)
 */

package de.sciss.jcollider;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

import de.sciss.app.BasicEvent;
import de.sciss.net.OSCMessage;

/**
 *	These kind of events get delivered by a 
 *	node watcher to inform listeners about
 *	node status changes.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.33, 19-Mar-08
 */
public class NodeEvent
extends BasicEvent
{
// --- ID values ---
	/**
	 *  returned by getID() : the node was created
	 */
	public static final int GO			= 0;

	/**
	 *  returned by getID() : the node was destroyed
	 */
	public static final int END			= 1;

	/**
	 *  returned by getID() : the node was resumed
	 */
	public static final int ON			= 2;

	/**
	 *  returned by getID() : the node was paused
	 */
	public static final int OFF			= 3;

	/**
	 *  returned by getID() : the node has moved
	 */
	public static final int MOVE		= 4;

	/**
	 *  returned by getID() : the event was created by a /n_query command
	 */
	public static final int INFO		= 5;

	/**
	 *  returned by getType() : the node is a synth
	 */
	public static final int SYNTH		= 0;

	/**
	 *  returned by getType() : the node is a group
	 */
	public static final int GROUP		= 1;

	/**
	 *  returned by getType() : the node type is unknown
	 */
	public static final int UNKNOWN		= -1;

	private final Node	node;
	private final int	nodeID;
	private final int	nodeType;

	private final int	parentID;
	private final int	predID;
	private final int	succID;
	private final int	headID;
	private final int	tailID;

	private final int	oldParentID;
	private final int	oldPredID;
	private final int	oldSuccID;
//	private final int	oldHeadID;
//	private final int	oldTailID;

	private static final List	collValidCmds;
	
	static {
		final List coll = new ArrayList( 6 );
		coll.add( "/n_go" );
		coll.add( "/n_end" );
		coll.add( "/n_on" );
		coll.add( "/n_off" );
		coll.add( "/n_move" );
		coll.add( "/n_info" );
		collValidCmds	= Collections.unmodifiableList( coll );
	}

//	/**
//	 *	Constructs a <code>NodeEvent</code> from a valid node.
//	 *	All list fields of the node must be valid, i.e. parent group,
//	 *	predecessor, successor etc.
//	 *
//	 *	@param	source	who fired the event
//	 *	@param	ID		the type of status change, e.g. <code>GO</code>
//	 *	@param	when	timestamp of the event (e.g. <code>System.currentTimeMillis()</code>)
//	 *	@param	node	the representation of the node whose status changed
//	 */
//	protected NodeEvent( Object source, int ID, long when, Node node )
//	{
//		super( source, ID, when );
//	
//		this.node		= node;
//		nodeID			= node.getNodeID();
//		parentID		= node.getGroup().getNodeID();
//		predID			= node.getPredNode() == null ? -1 : node.getPredNode().getNodeID();
//		succID			= node.getSuccNode() == null ? -1 : node.getSuccNode().getNodeID();
//		if( node instanceof Synth ) {
//			nodeType	= SYNTH;
//		} else if( node instanceof Group ) {
//			nodeType	= GROUP;
//		} else {
//			nodeType	= UNKNOWN;
//		}
//		if( nodeType == GROUP ) {
//			final Group g = (Group) node;
//			headID		= g.getHeadNode() == null ? -1 : g.getHeadNode().getNodeID();
//			tailID		= g.getTailNode() == null ? -1 : g.getTailNode().getNodeID();
//		} else {
//			headID		= -1;
//			tailID		= -1;
//		}
//	}

	/**
	 *	Constructs a <code>NodeEvent</code> from a detailed description.
	 *	Note that the provided node (which may be <code>null</code>) is considered
	 *	not to be up-to-date, it is examined to fill in the old-fields (oldParentID,
	 *	oldPredID) etc. It will be returned by
	 *	<code>getNode</code>. The caller is responsible for updating the node's
	 *	fields accordingly, after processing the event.
	 *
	 *	@param	source		who fired the event
	 *	@param	ID			the type of status change, e.g. <code>GO</code>
	 *	@param	when		timestamp of the event (e.g. <code>System.currentTimeMillis()</code>)
	 *	@param	node		the representation of the node whose status changed
	 *	@param	nodeID		the ID of the node whose status changed
	 *	@param	parentID	the ID of the node's parent group
	 *	@param	predID		the node's predecessor or -1
	 *	@param	succID		the node's successor or -1
	 *	@param	nodeType	either of <code>GROUP</code> or <code>SYNTH</code>
	 *	@param	headID		(for groups) ID of the group's head (first) node
	 *	@param	tailID		(for groups) ID of the group's tail (last) node
	 */
	protected NodeEvent( Object source, int ID, long when, Node node, int nodeID, int parentID,
						 int predID, int succID, int nodeType,
						 int headID, int tailID )
	{
		super( source, ID, when );
	
		this.node				= node;
		this.nodeID				= nodeID;
		this.nodeType			= nodeType;
	
		this.parentID			= parentID;
		this.predID				= predID;
		this.succID				= succID;
		this.headID				= headID;
		this.tailID				= tailID;
		
		if( node == null ) {
			this.oldParentID	= -1;
			this.oldPredID		= -1;
			this.oldSuccID		= -1;
//			this.oldHeadID		= -1;
//			this.oldTailID		= -1;
		} else {
			this.oldParentID	= node.getGroup()    == null ? -1 : node.getGroup().getNodeID();
			this.oldPredID		= node.getPredNode() == null ? -1 : node.getPredNode().getNodeID();
			this.oldSuccID		= node.getSuccNode() == null ? -1 : node.getSuccNode().getNodeID();
//			if( nodeType == GROUP ) {
//				final Group	g	= (Group) node;
//				this.oldHeadID	= g.getHeadNode()    == null ? -1 : g.getHeadNode().getNodeID();
//				this.oldTailID	= g.getTailNode()    == null ? -1 : g.getTailNode().getNodeID();
//			} else {
//				this.oldHeadID	= -1;
//				this.oldTailID	= -1;
//			}
		}
	}
	
	/**
	 *	Constructs a <code>NodeEvent</code> from a valid node
	 *	notification OSC message. The provided node object is simply
	 *	stored for future reference through <code>getNode</code> and must
	 *	be updated by the caller according to the returned event.
	 *
	 *	@param	msg							OSC message such as <code>/n_go</code>
	 *	@param	source						who shall be known as the source of the generated event
	 *	@param	when						what is proposed time of the event generation
	 *	@param	node						a client side representation node to use for the event,
	 *										or <code>null</code> if no representation is known. The caller is
	 *										responsible for updating the node's status from the returned
	 *										event.
	 *
	 *	@throws	IllegalArgumentException	if the message doesn't contain a valid node message; you
	 *										can use <code>getIDFromOSCMessage</code> to determine if the
	 *										message is valid.
	 */
	public static NodeEvent fromOSCMessage( OSCMessage msg, Object source, long when, Node node )
	{
		final int eventID	= collValidCmds.indexOf( msg.getName() );
		if( eventID == -1 ) throw new IllegalArgumentException( "Not a valid node notification message : " + msg.getName() );
		
		final int nodeID	= ((Number) msg.getArg( 0 )).intValue();
		final int parentID	= ((Number) msg.getArg( 1 )).intValue();
		final int predID	= ((Number) msg.getArg( 2 )).intValue();
		final int succID	= ((Number) msg.getArg( 3 )).intValue();
		final int nodeType	= ((Number) msg.getArg( 4 )).intValue();
		final int headID	= nodeType == GROUP ? ((Number) msg.getArg( 5 )).intValue() : -1;
		final int tailID	= nodeType == GROUP ? ((Number) msg.getArg( 6 )).intValue() : -1;
		
// let's trust the programmer for the sake of speed
//		if( node != null ) {
//			if( node.getNodeID() != nodeID ) throw new IllegalArgumentException( "Message and Node have different nodeIDs" );
//			if( nodeType == SYNTH ) {
//				if( !(node instanceof Synth) ) throw new IllegalArgumentException( "Message and Node have different nodeTypes" );
//			} else if( nodeType == GROUP ) {
//				if( !(node instanceof Group) ) throw new IllegalArgumentException( "Message and Node have different nodeTypes" );
//			}
//		}
		
		return new NodeEvent( source, eventID, when, node, nodeID, parentID, predID, succID, nodeType, headID, tailID );
	}

	public static NodeEvent fromOSCMessage( OSCMessage msg, Object source, long when )
	{
		return fromOSCMessage( msg, source, when, null );
	}
	
	/**
	 *	Queries the event ID which would be used if the event was
	 *	generated from a provided OSC message.
	 *	
	 *	@param	msg	the message to parse
	 *	@return	the corresponding event ID or <code>-1</code> if the message command
	 *			is not in the list of valid notification commands
	 */
	public static int getIDFromOSCMessage( OSCMessage msg )
	{
		return collValidCmds.indexOf( msg.getName() );
	}
	
	/**
	 *	@return	the representation of the node whose status changed. this may return <code>null</code>
	 *			if the client side object is not known. in this case, use <code>getNodeID</code>
	 *			to query the node's identifier.
	 */
	public Node getNode()
	{
		return node;
	}

	/**
	 *	@return	the ID of the node whose status changed
	 */
	public int getNodeID()
	{
		return nodeID;
	}

	/**
	 *	@return	the ID of the group in which this node sits. Note that if <code>getNode</code>
	 *			returns a valid node, you can also use <code>getParentGroup</code> on the
	 *			returned node.
	 */
	public int getParentGroupID()
	{
		return parentID;
	}

	/**
	 *	@return	the ID of the group in which this node was sitting before the modification
	 *			occurred, or <code>-1</code>, if the node was not placed in a group.
	 */
	public int getOldParentGroupID()
	{
		return oldParentID;
	}

	/**
	 *	@return	the ID of the node sitting just before the modified node in the graph,
	 *			or <code>-1</code> if there is no predecessor. Note that if <code>getNode</code>
	 *			returns a valid node, you can also use <code>getPredNode</code> on the
	 *			returned node.
	 */
	public int getPredNodeID()
	{
		return predID;
	}

	/**
	 *	@return	the ID of the node which was sitting just before the modified node before the
	 *			modification occured, or <code>-1</code> if there was no predecessor.
	 */
	public int getOldPredNodeID()
	{
		return oldPredID;
	}

	/**
	 *	@return	the ID of the node which was sitting just after the modified node before the
	 *			modification occured, or <code>-1</code> if there was no successor.
	 */
	public int getOldSuccNodeID()
	{
		return oldSuccID;
	}

	/**
	 *	@return	the ID of the node sitting just after the modified node in the graph,
	 *			or <code>-1</code> if there is no successor. Note that if <code>getNode</code>
	 *			returns a valid node, you can also use <code>getSuccNode</code> on the
	 *			returned node.
	 */
	public int getSuccNodeID()
	{
		return succID;
	}

	/**
	 *	@return	the type node that was modified, one of <code>SYNTH</code> or <code>GROUP</code>.
	 *			other values might be returned if a new version of supercollider introduces
	 *			other node classes.
	 */
	public int getNodeType()
	{
		return nodeType;
	}

	/**
	 *	@return	if the modified node is a group, returns the ID of the node being the group's
	 *			head element. otherwise (or when the group is empty) returns
	 *			<code>-1</code>. Note that if <code>getNode</code>
	 *			returns a valid group, you can also use <code>getHeadNode</code> on the
	 *			returned group.
	 */
	public int getHeadNodeID()
	{
		return headID;
	}

	/**
	 *	@return	if the modified node is a group, returns the ID of the node being the group's
	 *			tail element. otherwise (or when the group is empty) returns
	 *			<code>-1</code>. Note that if <code>getNode</code>
	 *			returns a valid group, you can also use <code>getTailNode</code> on the
	 *			returned group.
	 */
	public int getTailNodeID()
	{
		return tailID;
	}
	
	/**
	 *	Returns a list of strings describing
	 *	all known OSC command names that form
	 *	valid node notification messages, that is
	 *	<code>&quot;/n_go&quot;</code>, <code>&quot;/n_end&quot;</code> etc.
	 *	The returned list is immutable.
	 *
	 *	@return	a list whose elements are of class <code>String</code>, each element
	 *			corresponding to a valid OSC command. Note that the element index
	 *			reflects the event IDs, so <code>returnedList.get( GO ) == &quot;/n_go&quot;</code> etc.
	 */
	public static java.util.List getValidOSCCommands()
	{
		return collValidCmds;
	}

	/**
	 *	Used by the <code>EventManager</code> to
	 *	fuse successive events together when they queue.
	 *	Do not call this method.
	 */
	public boolean incorporate( BasicEvent oldEvent )
	{
//		if( (oldEvent instanceof NodeEvent) &&
//			(this.getSource() == oldEvent.getSource()) &&
//			(this.getID() == oldEvent.getID()) ) {
//			
//			return ((NodeEvent) oldEvent).nodeID == this.nodeID;
//
//		} else
		return false;
	}
}