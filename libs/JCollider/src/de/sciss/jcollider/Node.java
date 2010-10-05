/*
 *  Node.java
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
 *		04-Aug-05	created
 *		03-Oct-05	removed all setGroup statements. to have the group
 *					set correctly, use a NodeWatcher instead
 *					; implements TreeNode ; has predecessor / successor fields
 *					; class is abstract now (removed basicNew()
 *					; added traceMsg() ; changed printOn() ; nodes can be named
 */

package de.sciss.jcollider;

import java.io.IOException;
import java.io.PrintStream;
import javax.swing.tree.TreeNode;

import de.sciss.net.OSCMessage;

/**
 *	This is a client side representation of a node in the synthesis graph.
 *	It is the superclass of <code>Synth</code> and <code>Group</code> and
 *	as such abstract. The <code>javax.swing.tree.TreeNode</code> interface
 *	is implemented since it's a natural match and it allows classes such
 *	as <code>NodeTreeManager</code> to wrap scsynth's node graph on the
 *	client side into a usefull GUI representation.
 *	<p>
 *	Unlike SCLang, the status fields are not updated by the methods in this
 *	class. SCLang does this partly: It will update the node's parent group,
 *	while changing the running flag only when the node is freed. We decided to
 *	not copy this heterogenous behaviour. Parent group, running and playing
 *	flags as well as the newly introduced predecessor and successor fields
 *	must be updated by monitoring classes, typically a <code>NodeWatcher</code>.
 *
 *	@warning	not all methods are thorougly tested. before all methods have been
 *				thoroughly verified, excepted some of them to be wrong
 *				or behave different than expected. what certainly works
 *				is run-, set-, trace- and free-messages
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.36, 11-Oct-09
 *
 *	@see		de.sciss.jcollider.gui.NodeTreeManager
 *	@see		NodeWatcher
 */
public abstract class Node
implements Constants, TreeNode
{
	private final Server	server;
	private final int		nodeID;
	private String			name		= null;
	private Group			group		= null;
	private Node			predNode	= null;
	private Node			succNode	= null;
	private boolean			isPlaying	= false;
	private boolean			isRunning	= false;

	/**
	 *	Creates a Node representation for a given
	 *	node ID. This method does not send any
	 *	messages to the server.
	 *
	 *	@param	server	the <code>Server</code> at which the <code>Node</code> resides
	 *	@param	nodeID	the identifier of the <code>Node</code>
	 */
	protected Node( Server server, int nodeID )
	{
		this.server	= server;
		this.nodeID	= nodeID;
	}

	/**
	 *	Creates a Node representation. This method does not send any
	 *	messages to the server. The node ID is automatically
	 *	assigned from the server's allocator.
	 *
	 *	@param	server	the <code>Server</code> at which the <code>Node</code> resides
	 */
	protected Node( Server server )
	{
		this( server, server.nextNodeID() );
	}

	/**
	 *	Set a custom name for the node.
	 *	This name is used in print out
	 *	or GUI representation
	 *
	 *	@param	name	a name for the node, or <code>null</node>
	 */
	public void setName( String name )
	{
		this.name	= name;
	}
	
	/**
	 *	Queries the custom name of the node.
	 *	By default, a node has not an assigned name.
	 *
	 *	@return		the node's name or <code>null</code> if no name has been given
	 */
	public String getName()
	{
		return name;
	}

	/**
	 *	Queries the group in which the node resides.
	 *
	 *	@return	the parent <code>Group</code> of the <code>Node</code> or <code>null</code>
	 *			if no group has been assigned.
	 */
	public Group getGroup()
	{
		return group;
	}

	/**
	 *	Queries the node's predecessor in the
	 *	graph tree. This will only return a valid
	 *	node, if the node was created on the server
	 *	and is monitored by a node watcher. If the
	 *	node is the head node of a group, this will
	 *	return <code>null</code>.
	 *
	 *	@return		the node's predecessor or <code>null</code>
	 */
	public Node getPredNode()
	{
		return predNode;
	}

	/**
	 *	Queries the node's successor in the
	 *	graph tree. This will only return a valid
	 *	node, if the node was created on the server
	 *	and is monitored by a node watcher. If the
	 *	node is the tail node of a group, this will
	 *	return <code>null</code>.
	 *
	 *	@return		the node's successor or <code>null</code>
	 */
	public Node getSuccNode()
	{
		return succNode;
	}
	
	/**
	 *	Queries the server at which the node resides
	 *
	 *	@return	the node's <code>Server</code>
	 */
	public Server getServer()
	{
		return server;
	}
	
	/**
	 *	Sets the node's group. This method
	 *	does not send any messages to the server.
	 *	It merely stores the group object. This method
	 *	may be called by a <code>NodeWatcher</code> for example
	 *	when it receives a <code>/n_info</code> message.
	 *
	 *	@param	group	the node's new <code>Group</code> or <code>null</code> (if the node is invalidated)
	 */
	protected void setGroup( Group group )
	{
//		if( group == null ) {
//			if( this.group != null ) {
//			
//			}
//		} else {
//			if( this.group == null ) {
//				this.group	= group;
//				if( group.getHeadNode() == null ) 
//			} else if( this.group.getNodeID() != group.getNodeID() ) {
//				if( (group.getHeadNode() != null) && (group.getHeadNode().getNodeID() == this.getNodeID()) ) {
//					group.setHeadNode( this.getSuccNode() );
//				}
//				if( (group.getTailNode() != null) && (group.getTailNode().getNodeID() == this.getNodeID()) ) {
//					group.setTailNode( this.getPredNode() );
//				}
				this.group	= group;
//			}
//		}
	}
	
	/**
	 *	Sets the node's predecessor node. This method
	 *	does not send any messages to the server.
	 *	It merely stores the passed in object. This method
	 *	may be called by a <code>NodeWatcher</code> for example
	 *	when it receives a <code>/n_info</code> message.
	 *
	 *	@param	predNode	the node's new predecessor <code>Node</code> or <code>null</code>
	 */
	protected void setPredNode( Node predNode )
	{
		this.predNode			= predNode;
//		if( predNode != null ) {
//			predNode.succNode	= this;
//		}
	}

	/**
	 *	Sets the node's successor node. This method
	 *	does not send any messages to the server.
	 *	It merely stores the passed in object. This method
	 *	may be called by a <code>NodeWatcher</code> for example
	 *	when it receives a <code>/n_info</code> message.
	 *
	 *	@param	succNode	the node's new successor <code>Node</code> or <code>null</code>
	 */
	protected void setSuccNode( Node succNode )
	{
		this.succNode			= succNode;
//		if( succNode != null ) {
//			succNode.predNode	= this;
//		}
	}
	
	/**
	 *	Queries the node's identifier.
	 *
	 *	@return	the identifier by which the node is known on the server side
	 */
	public int getNodeID()
	{
		return nodeID;
	}
	
	/**
	 *	Queries whether the node exists on the server and is
	 *	playing (not paused).
	 *	This information is only valid when the node was
	 *	tracked by a <code>NodeWatcher</code>.
	 *
	 *	@return	<code>true</code> if the node exists on the server and is playing (not paused),
	 *			<code>false</code> otherwise
	 */
	public boolean isPlaying()
	{
		return isPlaying;
	}

	/**
	 *	Specifies whether the node is playing (not paused).
	 *	This method is supposed to be called by a <code>NodeWatcher</code>.
	 *	Do not call it yourself.
	 *
	 *	@param		isPlaying	<code>true</code> if the node is playing
	 *							<code>false</code> if the node is paused
	 */
	protected void setPlaying( boolean isPlaying )
	{
		this.isPlaying = isPlaying;
	}
	
	/**
	 *	Queries whether the node exists on the server.
	 *	This information is only valid when the node was
	 *	tracked by a <code>NodeWatcher</code>.
	 *
	 *	@return	<code>true</code> if the node exists on the server,
	 *			<code>false</code> otherwise
	 */
	public boolean isRunning()
	{
		return isRunning;
	}

	/**
	 *	Specifies whether the node exists on the server.
	 *	This method is supposed to be called by a <code>NodeWatcher</code>.
	 *	Do not call it yourself.
	 *
	 *	@param		isRunning	<code>true</code> if the node exists on the server,
	 *							<code>false</code> otherwise
	 */
	protected void setRunning( boolean isRunning )
	{
		this.isRunning = isRunning;
	}
	
	/**
	 *	Two nodes are equal if they
	 *	reside on the same server and
	 *	have the same node ID. Note
	 *	that in this implementation,
	 *	object reference identity for the
	 *	servers is assumed. You should
	 *	not create several instances for
	 *	the same server.
	 */
	public boolean equals( Object o )
	{
		if( o instanceof Node ) {
			final Node node = (Node) o;
			return( (node.getNodeID() == this.getNodeID()) && (node.getServer() == this.getServer()) );
		} else {
			return false;
		}
	}

	/**
	 *	@see	#equals( Object )
	 */
	public int hashCode()
	{
		return( getServer().hashCode() ^ getNodeID() );
	}
	
	/**
	 *	Frees the node on the server.
	 *	This sends a <code>/n_free</code> message to the server.
	 *
	 *	@see	#freeMsg()
	 *	@throws	IOException	if an error occurs while sending the OSC message
	 */
	public void free()
	throws IOException
	{
//		free( true );
		getServer().sendMsg( freeMsg() );
	}

//	public void free( boolean sendFlag )
//	throws IOException
//	{
//		if( sendFlag ) {
//			getServer().sendMsg( freeMsg() );
//		}
//// removed 02-oct-05
////		setGroup( null );
////		setPlaying( false );
////		setRunning( false );
//	}

	/**
	 *	Creates an OSC <code>/n_free</code> message for the node.
	 *
	 *	@return	an <code>OSCMessage</code> which can be sent to the server
	 *
	 *	@see	#free()
	 */
	public OSCMessage freeMsg()
	{
		return new OSCMessage( "/n_free", new Object[] { new Integer( getNodeID() )});
	}

	/**
	 *	Resumes the node if it was paused.
	 *	This sends a <code>[ /n_run, &lt;nodeID&gt;, 1 ]</code> message to the server.
	 *
	 *	@see	#runMsg()
	 *	@throws	IOException	if an error occurs while sending the OSC message
	 */
	public void run()
	throws IOException
	{
		run( true );
	}

	/**
	 *	Pauses or resumes the node.
	 *	This sends a <code>/n_run</code> message to the server.
	 *
	 *	@param	flag	<code>false</code> to pause the node, <code>true</code> to resume the node.
	 *
	 *	@see	#runMsg( boolean )
	 *	@throws	IOException	if an error occurs while sending the OSC message
	 */
	public void run( boolean flag )
	throws IOException
	{
		getServer().sendMsg( runMsg( flag ));
	}
	
	/**
	 *	Creates an OSC <code>[ /n_run, &lt;nodeID&gt;, 1 ]</code> message for the node.
	 *
	 *	@return	an <code>OSCMessage</code> which can be sent to the server
	 *
	 *	@see	#run()
	 */
	public OSCMessage runMsg()
	{
		return runMsg( true );
	}

	/**
	 *	Creates an OSC <code>/n_run</code> message for the node.
	 *
	 *	@param	flag	<code>false</code> to pause the node, <code>true</code> to resume the node.
	 *	@return	an <code>OSCMessage</code> which can be sent to the server
	 *
	 *	@see	#run( boolean )
	 */
	public OSCMessage runMsg( boolean flag )
	{
		return new OSCMessage( "/n_run", new Object[] { new Integer( getNodeID() ), new Integer( flag ? 1 : 0 )});
	}

	/**
	 *	Sets a node's control parameter to a new value.
	 *	This sends a <code>/n_set</code> message to the server.
	 *	If the node is a <code>Synth</code>, it adjusts the synth's control value.
	 *	If the node is a <code>Group</code>, it adjusts the control values of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlName	the name of the control (<code>SynthDef</code> argument)
	 *	@param	value		the new value of the control
	 *
	 *	@see	#setMsg( String, float )
	 *	@throws	IOException	if an error occurs while sending the OSC message
	 */
	public void set( String ctrlName, float value )
	throws IOException
	{
		getServer().sendMsg( setMsg( ctrlName, value ));
	}

	/**
	 *	Creates an OSC <code>/n_set</code> message to change a node's control parameter to a new value.
	 *	Sending this message to a <code>Synth</code> adjusts the synth's control value.
	 *	Sending this message to a <code>Group</code> adjusts the control values of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlName	the name of the control (<code>SynthDef</code> argument)
	 *	@param	value		the new value of the control
	 *
	 *	@see	#set( String, float )
	 */
	public OSCMessage setMsg( String ctrlName, float value )
	{
		return new OSCMessage( "/n_set", new Object[] { new Integer( getNodeID() ), ctrlName, new Float( value )});
	}

	/**
	 *	Sets a node's control parameter to a new value.
	 *	This sends a <code>/n_set</code> message to the server.
	 *	If the node is a <code>Synth</code>, it adjusts the synth's control value.
	 *	If the node is a <code>Group</code>, it adjusts the control values of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlIdx		the index of the control (<code>SynthDef</code> argument)
	 *	@param	value		the new value of the control
	 *
	 *	@see	#setMsg( int, float )
	 *	@throws	IOException	if an error occurs while sending the OSC message
	 */
	public void set( int ctrlIdx, float value )
	throws IOException
	{
		getServer().sendMsg( setMsg( ctrlIdx, value ));
	}

	/**
	 *	Creates an OSC <code>/n_set</code> message to change a node's control parameter to a new value.
	 *	Sending this message to a <code>Synth</code> adjusts the synth's control value.
	 *	Sending this message to a <code>Group</code> adjusts the control values of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlIdx		the index of the control (<code>SynthDef</code> argument)
	 *	@param	value		the new value of the control
	 *
	 *	@see	#set( int, float )
	 */
	public OSCMessage setMsg( int ctrlIdx, float value )
	{
		return new OSCMessage( "/n_set", new Object[] { new Integer( getNodeID() ), new Integer( ctrlIdx ), new Float( value )});
	}

	/**
	 *	Sets a list of the node's control parameters to new values.
	 *	This sends a <code>/n_set</code> message to the server.
	 *	If the node is a <code>Synth</code>, it adjusts the synth's control values.
	 *	If the node is a <code>Group</code>, it adjusts the control values of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlNames	an array of the names of the controls (<code>SynthDef</code> arguments)
	 *	@param	values		an array of the new values of the controls. Each array element corresponds
	 *						to the control name in <code>ctrlNames</code> at the same array index. the array sizes of
	 *						<code>ctrlNames</code> and <code>values</code> must be equal.
	 *
	 *	@see	#setMsg( String[], float[] )
	 *	@throws	IOException	if an error occurs while sending the OSC message
	 */
	public void set( String[] ctrlNames, float[] values )
	throws IOException
	{
		getServer().sendMsg( setMsg( ctrlNames, values ));
	}

	/**
	 *	Creates an OSC <code>/n_set</code> message to adjust a list of the node's control parameters to new values.
	 *	Sending this message to a <code>Synth</code> adjusts the synth's control values.
	 *	Sending this message to a <code>Group</code> adjusts the control values of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlNames	an array of the names of the controls (<code>SynthDef</code> arguments)
	 *	@param	values		an array of the new values of the controls. Each array element corresponds
	 *						to the control name in <code>ctrlNames</code> at the same array index. the array sizes of
	 *						<code>ctrlNames</code> and <code>values</code> must be equal.
	 *
	 *	@see	#set( String[], float[] )
	 */
	public OSCMessage setMsg( String[] ctrlNames, float[] values )
	{
		final Object[] args = new Object[ (ctrlNames.length << 1) + 1 ];
		args[ 0 ]			= new Integer( getNodeID() );
		
		for( int i = 0, j = 1; i < ctrlNames.length; i++ ) {
			args[ j++ ] = ctrlNames[ i ];
			args[ j++ ] = new Float( values[ i ]);
		}
	
		return( new OSCMessage( "/n_set", args ));
	}
	
	/**
	 *	Sets a list of the node's control parameters to new values.
	 *	This sends a <code>/n_set</code> message to the server.
	 *	If the node is a <code>Synth</code>, it adjusts the synth's control values.
	 *	If the node is a <code>Group</code>, it adjusts the control values of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlIndices	an array of the indices of the controls (<code>SynthDef</code> arguments)
	 *	@param	values		an array of the new values of the controls. Each array element corresponds
	 *						to the control index in <code>ctrlIndices</code> at the same array index. the array sizes of
	 *						<code>ctrlIndices</code> and <code>values</code> must be equal.
	 *
	 *	@see	#setMsg( int[], float[] )
	 *	@throws	IOException	if an error occurs while sending the OSC message
	 */
	public void set( int[] ctrlIndices, float[] values )
	throws IOException
	{
		getServer().sendMsg( setMsg( ctrlIndices, values ));
	}

	/**
	 *	Creates an OSC <code>/n_set</code> message to adjust a list of the node's control parameters to new values.
	 *	Sending this message to a <code>Synth</code> adjusts the synth's control values.
	 *	Sending this message to a <code>Group</code> adjusts the control values of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlIndices	an array of the indices of the controls (<code>SynthDef</code> arguments)
	 *	@param	values		an array of the new values of the controls. Each array element corresponds
	 *						to the control index in <code>ctrlIndices</code> at the same array index. the array sizes of
	 *						<code>ctrlIndices</code> and <code>values</code> must be equal.
	 *
	 *	@see	#set( int[], float[] )
	 */
	public OSCMessage setMsg( int[] ctrlIndices, float[] values )
	{
		final Object[] args = new Object[ (ctrlIndices.length << 1) + 1 ];
		args[ 0 ]			= new Integer( getNodeID() );
		
		for( int i = 0, j = 1; i < ctrlIndices.length; i++ ) {
			args[ j++ ] = new Integer( ctrlIndices[ i ]);
			args[ j++ ] = new Float( values[ i ]);
		}
	
		return( new OSCMessage( "/n_set", args ));
	}
	
	/**
	 *	Sets ranges of the node's control parameters to new values.
	 *	This sends a <code>/n_fill</code> message to the server.
	 *	If the node is a <code>Synth</code>, it adjusts the synth's control values.
	 *	If the node is a <code>Group</code>, it adjusts the control values of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlNames	the names of the first control to change
	 *	@param	numControls	the numbers of successive controls to change
	 *	@param	values		an array of the new values of the controls. the sizes
	 *						of <code>ctrlNames</code>, <code>numControls</code> and <code>values</code>
	 *						must be equal.
	 *
	 *	@see	#fillMsg( String[], int[], float[] )
	 *	@throws	IOException	if an error occurs while sending the OSC message
	 */
	public void fill( String[] ctrlNames, int[] numControls, float[] values )
	throws IOException
	{
		getServer().sendMsg( fillMsg( ctrlNames, numControls, values ));
	}
	
	/**
	 *	Creates an OSC <code>/n_fill</code> message to adjust ranges of the node's control parameters to new values.
	 *	Sending this message to a <code>Synth</code> adjusts the synth's control values.
	 *	Sending this message to a <code>Group</code> adjusts the control values of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlNames	the names of the first control to change
	 *	@param	numControls	the numbers of successive controls to change
	 *	@param	values		an array of the new values of the controls. the sizes
	 *						of <code>ctrlNames</code>, <code>numControls</code> and <code>values</code>
	 *						must be equal.
	 *
	 *	@see	#fill( String[], int[], float[] )
	 */
	public OSCMessage fillMsg( String[] ctrlNames, int[] numControls, float[] values )
	{
		final Object[] args = new Object[ ctrlNames.length * 3 + 1 ];
		args[ 0 ]			= new Integer( getNodeID() );
		
		for( int i = 0, j = 1; i < ctrlNames.length; i++ ) {
			args[ j++ ]	= ctrlNames[ i ];
			args[ j++ ]	= new Integer( numControls[ i ]);
			args[ j++ ] = new Float( values[ i ]);
		}
	
		return( new OSCMessage( "/n_fill", args ));
	}

	/**
	 *	Sets ranges of the node's control parameters to new values.
	 *	This sends a <code>/n_fill</code> message to the server.
	 *	If the node is a <code>Synth</code>, it adjusts the synth's control values.
	 *	If the node is a <code>Group</code>, it adjusts the control values of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlIndices	the indices of the first control to change
	 *	@param	numControls	the numbers of successive controls to change
	 *	@param	values		an array of the new values of the controls. the sizes
	 *						of <code>ctrlIndices</code>, <code>numControls</code> and <code>values</code>
	 *						must be equal.
	 *
	 *	@see	#fillMsg( int[], int[], float[] )
	 *	@throws	IOException	if an error occurs while sending the OSC message
	 */
	public void fill( int[] ctrlIndices, int[] numControls, float[] values )
	throws IOException
	{
		getServer().sendMsg( fillMsg( ctrlIndices, numControls, values ));
	}
	
	/**
	 *	Creates an OSC <code>/n_fill</code> message to adjust ranges of the node's control parameters to new values.
	 *	Sending this message to a <code>Synth</code> adjusts the synth's control values.
	 *	Sending this message to a <code>Group</code> adjusts the control values of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlIndices	the indices of the first control to change
	 *	@param	numControls	the numbers of successive controls to change
	 *	@param	values		an array of the new values of the controls. the sizes
	 *						of <code>ctrlIndices</code>, <code>numControls</code> and <code>values</code>
	 *						must be equal.
	 *
	 *	@see	#fill( int[], int[], float[] )
	 */
	public OSCMessage fillMsg( int[] ctrlIndices, int[] numControls, float[] values )
	{
		final Object[] args = new Object[ ctrlIndices.length * 3 + 1 ];
		args[ 0 ]			= new Integer( getNodeID() );
		
		for( int i = 0, j = 1; i < ctrlIndices.length; i++ ) {
			args[ j++ ]	= new Integer( ctrlIndices[ i ]);
			args[ j++ ]	= new Integer( numControls[ i ]);
			args[ j++ ] = new Float( values[ i ]);
		}
	
		return( new OSCMessage( "/n_fill", args ));
	}

	/**
	 *	Sets ranges of the node's control parameters to new values.
	 *	This sends a <code>/n_setn</code> message to the server.
	 *	If the node is a <code>Synth</code>, it adjusts the synth's control values.
	 *	If the node is a <code>Group</code>, it adjusts the control values of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlNames	the names of the first control to change
	 *	@param	values		an array of arrays of the new values of the controls. each outer
	 *						array corresponds to one element in <code>ctrlNames</code>, hence
	 *						<code>ctrlNames.length</code> and <code>values.length</code>
	 *						must be equal.
	 *
	 *	@see	#setnMsg( String[], float[][] )
	 *	@throws	IOException	if an error occurs while sending the OSC message
	 */
	public void setn( String[] ctrlNames, float[][] values )
	throws IOException
	{
		getServer().sendMsg( setnMsg( ctrlNames, values ));
	}
	
	/**
	 *	Creates an OSC <code>/n_setn</code> message to adjust ranges of the node's control parameters to new values.
	 *	Sending this message to a <code>Synth</code> adjusts the synth's control values.
	 *	Sending this message to a <code>Group</code> adjusts the control values of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlNames	the names of the first control to change
	 *	@param	values		an array of arrays of the new values of the controls. each outer
	 *						array corresponds to one element in <code>ctrlNames</code>, hence
	 *						<code>ctrlNames.length</code> and <code>values.length</code>
	 *						must be equal.
	 *
	 *	@see	#setn( String[], float[][] )
	 */
	public OSCMessage setnMsg( String[] ctrlNames, float[][] values )
	{
		final Object[]	args;
		float[]			subV;
		int				numArgs	= 1;
		
		for( int i = 0; i < ctrlNames.length; i++ ) {
			numArgs += values[ i ].length + 2;
		}
		 
		args		= new Object[ numArgs ];
		args[ 0 ]	= new Integer( getNodeID() );
		
		for( int i = 0, j = 1; i < ctrlNames.length; i++ ) {
			args[ j++ ]	= ctrlNames[ i ];
			subV		= values[ i ];
			args[ j++ ] = new Integer( subV.length );
			for( int k = 0; k < subV.length; k++ ) {
				args[ j++ ] = new Float( subV[ k ]);
			}
		}
	
		return( new OSCMessage( "/n_setn", args ));
	}

	/**
	 *	Sets ranges of the node's control parameters to new values.
	 *	This sends a <code>/n_setn</code> message to the server.
	 *	If the node is a <code>Synth</code>, it adjusts the synth's control values.
	 *	If the node is a <code>Group</code>, it adjusts the control values of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlIndices	the indices of the first control to change
	 *	@param	values		an array of arrays of the new values of the controls. each outer
	 *						array corresponds to one element in <code>ctrlNames</code>, hence
	 *						<code>ctrlNames.length</code> and <code>values.length</code>
	 *						must be equal.
	 *
	 *	@see	#setnMsg( int[], float[][] )
	 *	@throws	IOException	if an error occurs while sending the OSC message
	 */
	public void setn( int[] ctrlIndices, float[][] values )
	throws IOException
	{
		getServer().sendMsg( setnMsg( ctrlIndices, values ));
	}
	
	/**
	 *	Creates an OSC <code>/n_setn</code> message to adjust ranges of the node's control parameters to new values.
	 *	Sending this message to a <code>Synth</code> adjusts the synth's control values.
	 *	Sending this message to a <code>Group</code> adjusts the control values of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlIndices	the indices of the first control to change
	 *	@param	values		an array of arrays of the new values of the controls. each outer
	 *						array corresponds to one element in <code>ctrlNames</code>, hence
	 *						<code>ctrlNames.length</code> and <code>values.length</code>
	 *						must be equal.
	 *
	 *	@see	#setn( String[], float[][] )
	 */
	public OSCMessage setnMsg( int[] ctrlIndices, float[][] values )
	{
		final Object[]	args;
		float[]			subV;
		int				numArgs	= 1;
		
		for( int i = 0; i < ctrlIndices.length; i++ ) {
			numArgs += values[ i ].length + 2;
		}
		 
		args		= new Object[ numArgs ];
		args[ 0 ]	= new Integer( getNodeID() );
		
		for( int i = 0, j = 1; i < ctrlIndices.length; i++ ) {
			args[ j++ ]	= new Integer( ctrlIndices[ i ]);
			subV		= values[ i ];
			args[ j++ ] = new Integer( subV.length );
			for( int k = 0; k < subV.length; k++ ) {
				args[ j++ ] = new Float( subV[ k ]);
			}
		}
	
		return( new OSCMessage( "/n_setn", args ));
	}

	/**
	 *	Maps a list of the node's control parameters to be automatically read from global control busses.
	 *	This sends a <code>/n_map</code> message to the server.
	 *	If the node is a <code>Synth</code>, it maps the synth's controls.
	 *	If the node is a <code>Group</code>, it maps the controls of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlIndices	an array of the indices of the controls (<code>SynthDef</code> arguments)
	 *	@param	busIndices	an array of the indices of the control busses. Each array element corresponds
	 *						to the control index in <code>ctrlIndices</code> at the same array index. the array sizes of
	 *						<code>ctrlIndices</code> and <code>values</code> must be equal. whenever a bus index is
	 *						<code>-1</code> the mapping is undone. Mapping is also undone by successive <code>/n_set</code>,
	 *						<code>/n_setn</code> or  <code>/n_fill</code> commands.
	 *
	 *	@see	#mapMsg( int[], int[] )
	 *	@throws	IOException	if an error occurs while sending the OSC message
	 */
	public void map( int[] ctrlIndices, int[] busIndices )
	throws IOException
	{
		getServer().sendMsg( mapMsg( ctrlIndices, busIndices ));
	}

	/**
	 *	Creates an OSC <code>/n_map</code> message to map a list of the node's control parameters to
	 *	be automatically read from global control busses.
	 *	Sending this message to a <code>Synth</code> maps the synth's controls.
	 *	Sending this message to a <code>Group</code> maps the controls of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlIndices	an array of the indices of the controls (<code>SynthDef</code> arguments)
	 *	@param	busIndices	an array of the indices of the control busses. Each array element corresponds
	 *						to the control index in <code>ctrlIndices</code> at the same array index. the array sizes of
	 *						<code>ctrlIndices</code> and <code>values</code> must be equal. whenever a bus index is
	 *						<code>-1</code> the mapping is undone. Mapping is also undone by successive <code>/n_set</code>,
	 *						<code>/n_setn</code> or  <code>/n_fill</code> commands.
	 *
	 *	@see	#map( int[], int[] )
	 */
	public OSCMessage mapMsg( int[] ctrlIndices, int[] busIndices )
	{
		final Object[] args = new Object[ (ctrlIndices.length << 1) + 1 ];
		args[ 0 ]			= new Integer( getNodeID() );
		
		for( int i = 0, j = 1; i < ctrlIndices.length; i++ ) {
			args[ j++ ] = new Integer( ctrlIndices[ i ]);
			args[ j++ ] = new Integer( busIndices[ i ]);
		}
	
		return( new OSCMessage( "/n_map", args ));
	}

	/**
	 *	Maps a list of the node's control parameters to be automatically read from global control busses.
	 *	This sends a <code>/n_map</code> message to the server.
	 *	If the node is a <code>Synth</code>, it maps the synth's controls.
	 *	If the node is a <code>Group</code>, it maps the controls of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlNames	an array of the names of the controls (<code>SynthDef</code> arguments)
	 *	@param	busIndices	an array of the names of the control busses. Each array element corresponds
	 *						to the control name in <code>ctrlNames</code> at the same array index. the array sizes of
	 *						<code>ctrlNames</code> and <code>values</code> must be equal. whenever a bus index is
	 *						<code>-1</code> the mapping is undone. Mapping is also undone by successive <code>/n_set</code>,
	 *						<code>/n_setn</code> or  <code>/n_fill</code> commands.
	 *
	 *	@see	#mapMsg( String[], int[] )
	 *	@throws	IOException	if an error occurs while sending the OSC message
	 */
	public void map( String[] ctrlNames, int[] busIndices )
	throws IOException
	{
		getServer().sendMsg( mapMsg( ctrlNames, busIndices ));
	}

	/**
	 *	Creates an OSC <code>/n_map</code> message to map a list of the node's control parameters to
	 *	be automatically read from global control busses.
	 *	Sending this message to a <code>Synth</code> maps the synth's controls.
	 *	Sending this message to a <code>Group</code> maps the controls of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlNames	an array of the names of the controls (<code>SynthDef</code> arguments)
	 *	@param	busIndices	an array of the names of the control busses. Each array element corresponds
	 *						to the control name in <code>ctrlNames</code> at the same array index. the array sizes of
	 *						<code>ctrlNames</code> and <code>values</code> must be equal. whenever a bus index is
	 *						<code>-1</code> the mapping is undone. Mapping is also undone by successive <code>/n_set</code>,
	 *						<code>/n_setn</code> or  <code>/n_fill</code> commands.
	 *
	 *	@see	#map( String[], int[] )
	 */
	public OSCMessage mapMsg( String[] ctrlNames, int[] busIndices )
	{
		final Object[] args = new Object[ (ctrlNames.length << 1) + 1 ];
		args[ 0 ]			= new Integer( getNodeID() );
		
		for( int i = 0, j = 1; i < ctrlNames.length; i++ ) {
			args[ j++ ] = ctrlNames[ i ];
			args[ j++ ] = new Integer( busIndices[ i ]);
		}
	
		return( new OSCMessage( "/n_map", args ));
	}
	
	/**
	 *	Maps a list of the node's control parameters to be automatically read from global control busses.
	 *	This sends a <code>/n_map</code> message to the server.
	 *	If the node is a <code>Synth</code>, it maps the synth's controls.
	 *	If the node is a <code>Group</code>, it maps the controls of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlIndices	an array of the indices of the controls (<code>SynthDef</code> arguments)
	 *	@param	busses		an array of control busses. Each array element corresponds
	 *						to the control index in <code>ctrlIndices</code> at the same array index. the array sizes of
	 *						<code>ctrlIndices</code> and <code>busses</code> must be equal. whenever a bus is
	 *						<code>null</code> the mapping is undone. Mapping is also undone by successive <code>/n_set</code>,
	 *						<code>/n_setn</code> or  <code>/n_fill</code> commands.
	 *
	 *	@see	#mapMsg( int[], Bus[] )
	 *	@throws	IOException	if an error occurs while sending the OSC message
	 */
	public void map( int[] ctrlIndices, Bus[] busses )
	throws IOException
	{
		getServer().sendMsg( mapMsg( ctrlIndices, busses ));
	}

	/**
	 *	Creates an OSC <code>/n_map</code> message to map a list of the node's control parameters to
	 *	be automatically read from global control busses.
	 *	Sending this message to a <code>Synth</code> maps the synth's controls.
	 *	Sending this message to a <code>Group</code> maps the controls of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlIndices	an array of the indices of the controls (<code>SynthDef</code> arguments)
	 *	@param	busses		an array of control busses. Each array element corresponds
	 *						to the control index in <code>ctrlIndices</code> at the same array index. the array sizes of
	 *						<code>ctrlIndices</code> and <code>busses</code> must be equal. whenever a bus is
	 *						<code>null</code> the mapping is undone. Mapping is also undone by successive <code>/n_set</code>,
	 *						<code>/n_setn</code> or  <code>/n_fill</code> commands.
	 *
	 *	@see	#map( int[], Bus[] )
	 */
	public OSCMessage mapMsg( int[] ctrlIndices, Bus[] busses )
	{
		final Object[] args = new Object[ (ctrlIndices.length << 1) + 1 ];
		args[ 0 ]			= new Integer( getNodeID() );
		
		for( int i = 0, j = 1; i < ctrlIndices.length; i++ ) {
			args[ j++ ] = new Integer( ctrlIndices[ i ]);
			args[ j++ ] = new Integer( busses[ i ] == null ? -1 : busses[ i ].getIndex() );
		}
	
		return( new OSCMessage( "/n_map", args ));
	}

	/**
	 *	Maps a list of the node's control parameters to be automatically read from global control busses.
	 *	This sends a <code>/n_map</code> message to the server.
	 *	If the node is a <code>Synth</code>, it maps the synth's controls.
	 *	If the node is a <code>Group</code>, it maps the controls of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlNames	an array of the names of the controls (<code>SynthDef</code> arguments)
	 *	@param	busses		an array of control busses. Each array element corresponds
	 *						to the control name in <code>ctrlNames</code> at the same array index. the array sizes of
	 *						<code>ctrlNames</code> and <code>busses</code> must be equal. whenever a bus is
	 *						<code>null</code> the mapping is undone. Mapping is also undone by successive <code>/n_set</code>,
	 *						<code>/n_setn</code> or  <code>/n_fill</code> commands.
	 *
	 *	@see	#mapMsg( String[], Bus[] )
	 *	@throws	IOException	if an error occurs while sending the OSC message
	 */
	public void map( String[] ctrlNames, Bus[] busses )
	throws IOException
	{
		getServer().sendMsg( mapMsg( ctrlNames, busses ));
	}

	/**
	 *	Creates an OSC <code>/n_map</code> message to map a list of the node's control parameters to
	 *	be automatically read from global control busses.
	 *	Sending this message to a <code>Synth</code> maps the synth's controls.
	 *	Sending this message to a <code>Group</code> maps the controls of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlNames	an array of the names of the controls (<code>SynthDef</code> arguments)
	 *	@param	busses		an array of control busses. Each array element corresponds
	 *						to the control name in <code>ctrlNames</code> at the same array index. the array sizes of
	 *						<code>ctrlNames</code> and <code>busses</code> must be equal. whenever a bus is
	 *						<code>null</code> the mapping is undone. Mapping is also undone by successive <code>/n_set</code>,
	 *						<code>/n_setn</code> or  <code>/n_fill</code> commands.
	 *
	 *	@see	#map( String[], Bus[] )
	 */
	public OSCMessage mapMsg( String[] ctrlNames, Bus[] busses )
	{
		final Object[] args = new Object[ (ctrlNames.length << 1) + 1 ];
		args[ 0 ]			= new Integer( getNodeID() );
		
		for( int i = 0, j = 1; i < ctrlNames.length; i++ ) {
			args[ j++ ] = ctrlNames[ i ];
			args[ j++ ] = new Integer( busses[ i ] == null ? -1 : busses[ i ].getIndex() );
		}
	
		return( new OSCMessage( "/n_map", args ));
	}

	/**
	 *	Convenience method for a single mapping.
	 */
	public void map( String ctrlName, Bus bus )
	throws IOException
	{
		getServer().sendMsg( mapMsg( ctrlName, bus ));
	}
	
	/**
	 *	Convenience method for a single mapping.
	 */
	public OSCMessage mapMsg( String ctrlName, Bus bus )
	{
		return( new OSCMessage( "/n_map", new Object[] {
			new Integer( getNodeID() ), ctrlName,
			new Integer( bus == null ? -1 : bus.getIndex() )}));
	}
	
	/**
	 *	Maps a list of the node's control parameters to be automatically read from global control busses.
	 *	This sends a <code>/n_map</code> message to the server.
	 *	If the node is a <code>Synth</code>, it maps the synth's controls.
	 *	If the node is a <code>Group</code>, it maps the controls of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlIndices	an array of the indices of the controls (<code>SynthDef</code> arguments)
	 *	@param	busIndices	an array of the indices of the control busses. Each array element corresponds
	 *						to the control index in <code>ctrlIndices</code> at the same array index. the array sizes of
	 *						<code>ctrlIndices</code> and <code>values</code> must be equal. whenever a bus index is
	 *						<code>-1</code> the mapping is undone. Mapping is also undone by successive <code>/n_set</code>,
	 *						<code>/n_setn</code> or  <code>/n_fill</code> commands.
	 *	@param	numControls	an array of the number of successive controls to map. this array must have the same size
	 *						as <code>ctrlIndices</code>.
	 *
	 *	@see	#mapnMsg( int[], int[], int[] )
	 *	@throws	IOException	if an error occurs while sending the OSC message
	 */
	public void mapn( int[] ctrlIndices, int[] busIndices, int[] numControls )
	throws IOException
	{
		getServer().sendMsg( mapnMsg( ctrlIndices, busIndices, numControls ));
	}

	/**
	 *	Creates an OSC <code>/n_map</code> message to map a list of the node's control parameters to
	 *	be automatically read from global control busses.
	 *	Sending this message to a <code>Synth</code> maps the synth's controls.
	 *	Sending this message to a <code>Group</code> maps the controls of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlIndices	an array of the indices of the controls (<code>SynthDef</code> arguments)
	 *	@param	busIndices	an array of the indices of the control busses. Each array element corresponds
	 *						to the control index in <code>ctrlIndices</code> at the same array index. the array sizes of
	 *						<code>ctrlIndices</code> and <code>values</code> must be equal. whenever a bus index is
	 *						<code>-1</code> the mapping is undone. Mapping is also undone by successive <code>/n_set</code>,
	 *						<code>/n_setn</code> or  <code>/n_fill</code> commands.
	 *	@param	numControls	an array of the number of successive controls to map. this array must have the same size
	 *						as <code>ctrlIndices</code>.
	 *
	 *	@see	#mapn( int[], int[], int[] )
	 */
	public OSCMessage mapnMsg( int[] ctrlIndices, int[] busIndices, int[] numControls )
	{
		final Object[] args = new Object[ ctrlIndices.length * 3 + 1 ];
		args[ 0 ]			= new Integer( getNodeID() );
		
		for( int i = 0, j = 1; i < ctrlIndices.length; i++ ) {
			args[ j++ ] = new Integer( ctrlIndices[ i ]);
			args[ j++ ] = new Integer( busIndices[ i ]);
			args[ j++ ] = new Integer( numControls[ i ]);
		}
	
		return( new OSCMessage( "/n_mapn", args ));
	}

	/**
	 *	Maps a list of the node's control parameters to be automatically read from global control busses.
	 *	This sends a <code>/n_mapn</code> message to the server.
	 *	If the node is a <code>Synth</code>, it maps the synth's controls.
	 *	If the node is a <code>Group</code>, it maps the controls of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlNames	an array of the names of the controls (<code>SynthDef</code> arguments)
	 *	@param	busIndices	an array of the names of the control busses. Each array element corresponds
	 *						to the control name in <code>ctrlNames</code> at the same array index. the array sizes of
	 *						<code>ctrlNames</code> and <code>values</code> must be equal. whenever a bus index is
	 *						<code>-1</code> the mapping is undone. Mapping is also undone by successive <code>/n_set</code>,
	 *						<code>/n_setn</code> or  <code>/n_fill</code> commands.
	 *	@param	numControls	an array of the number of successive controls to map. this array must have the same size
	 *						as <code>ctrlNames</code>.
	 *
	 *	@see	#mapnMsg( String[], int[], int[] )
	 *	@throws	IOException	if an error occurs while sending the OSC message
	 */
	public void mapn( String[] ctrlNames, int[] busIndices, int[] numControls )
	throws IOException
	{
		getServer().sendMsg( mapnMsg( ctrlNames, busIndices, numControls ));
	}

	/**
	 *	Creates an OSC <code>/n_mapn</code> message to map a list of the node's control parameters to
	 *	be automatically read from global control busses.
	 *	Sending this message to a <code>Synth</code> maps the synth's controls.
	 *	Sending this message to a <code>Group</code> maps the controls of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlNames	an array of the names of the controls (<code>SynthDef</code> arguments)
	 *	@param	busIndices	an array of the names of the control busses. Each array element corresponds
	 *						to the control name in <code>ctrlNames</code> at the same array index. the array sizes of
	 *						<code>ctrlNames</code> and <code>values</code> must be equal. whenever a bus index is
	 *						<code>-1</code> the mapping is undone. Mapping is also undone by successive <code>/n_set</code>,
	 *						<code>/n_setn</code> or  <code>/n_fill</code> commands.
	 *	@param	numControls	an array of the number of successive controls to map. this array must have the same size
	 *						as <code>ctrlNames</code>.
	 *
	 *	@see	#mapn( String[], int[], int[] )
	 */
	public OSCMessage mapnMsg( String[] ctrlNames, int[] busIndices, int[] numControls )
	{
		final Object[] args = new Object[ ctrlNames.length * 3 + 1 ];
		args[ 0 ]			= new Integer( getNodeID() );
		
		for( int i = 0, j = 1; i < ctrlNames.length; i++ ) {
			args[ j++ ] = ctrlNames[ i ];
			args[ j++ ] = new Integer( busIndices[ i ]);
			args[ j++ ] = new Integer( numControls[ i ]);
		}
	
		return( new OSCMessage( "/n_mapn", args ));
	}
	
	/**
	 *	Maps a list of the node's control parameters to be automatically read from global control busses.
	 *	This sends a <code>/n_mapn</code> message to the server.
	 *	If the node is a <code>Synth</code>, it maps the synth's controls.
	 *	If the node is a <code>Group</code>, it maps the controls of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlIndices	an array of the indices of the controls (<code>SynthDef</code> arguments)
	 *	@param	busses		an array of control busses. Each array element corresponds
	 *						to the control name in <code>ctrlNames</code> at the same array index. the array sizes of
	 *						<code>ctrlNames</code> and <code>busses</code> must be equal. The number of successive
	 *						controls is determined by each bus'es numChannels! Mapping is undone by successive <code>/n_set</code>,
	 *						<code>/n_setn</code> or  <code>/n_fill</code> commands.
	 *
	 *	@see	#mapnMsg( int[], Bus[] )
	 *	@throws	IOException	if an error occurs while sending the OSC message
	 */
	public void mapn( int[] ctrlIndices, Bus[] busses )
	throws IOException
	{
		getServer().sendMsg( mapnMsg( ctrlIndices, busses ));
	}

	/**
	 *	Creates an OSC <code>/n_mapn</code> message to map a list of the node's control parameters to
	 *	be automatically read from global control busses.
	 *	Sending this message to a <code>Synth</code> maps the synth's controls.
	 *	Sending this message to a <code>Group</code> maps the controls of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlIndices	an array of the indices of the controls (<code>SynthDef</code> arguments)
	 *	@param	busses		an array of control busses. Each array element corresponds
	 *						to the control name in <code>ctrlNames</code> at the same array index. the array sizes of
	 *						<code>ctrlNames</code> and <code>busses</code> must be equal. The number of successive
	 *						controls is determined by each bus'es numChannels! Mapping is undone by successive <code>/n_set</code>,
	 *						<code>/n_setn</code> or  <code>/n_fill</code> commands.
	 *
	 *	@see	#mapn( int[], Bus[] )
	 */
	public OSCMessage mapnMsg( int[] ctrlIndices, Bus[] busses )
	{
		final Object[] args = new Object[ ctrlIndices.length * 3 + 1 ];
		args[ 0 ]			= new Integer( getNodeID() );
		
		for( int i = 0, j = 1; i < ctrlIndices.length; i++ ) {
			args[ j++ ] = new Integer( ctrlIndices[ i ]);
			args[ j++ ] = new Integer( busses[ i ].getIndex() );
			args[ j++ ] = new Integer( busses[ i ].getNumChannels() );
		}
	
		return( new OSCMessage( "/n_mapn", args ));
	}

	/**
	 *	Maps a list of the node's control parameters to be automatically read from global control busses.
	 *	This sends a <code>/n_mapn</code> message to the server.
	 *	If the node is a <code>Synth</code>, it maps the synth's controls.
	 *	If the node is a <code>Group</code>, it maps the controls of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlNames	an array of the names of the controls (<code>SynthDef</code> arguments)
	 *	@param	busses		an array of control busses. Each array element corresponds
	 *						to the control name in <code>ctrlNames</code> at the same array index. the array sizes of
	 *						<code>ctrlNames</code> and <code>busses</code> must be equal. The number of successive
	 *						controls is determined by each bus'es numChannels! Mapping is undone by successive <code>/n_set</code>,
	 *						<code>/n_setn</code> or  <code>/n_fill</code> commands.
	 *
	 *	@see	#mapnMsg( String[], Bus[] )
	 *	@throws	IOException	if an error occurs while sending the OSC message
	 */
	public void mapn( String[] ctrlNames, Bus[] busses )
	throws IOException
	{
		getServer().sendMsg( mapnMsg( ctrlNames, busses ));
	}

	/**
	 *	Creates an OSC <code>/n_mapn</code> message to map a list of the node's control parameters to
	 *	be automatically read from global control busses.
	 *	Sending this message to a <code>Synth</code> maps the synth's controls.
	 *	Sending this message to a <code>Group</code> maps the controls of
	 *	all synths in this group and subgroups of this group.
	 *
	 *	@param	ctrlNames	an array of the names of the controls (<code>SynthDef</code> arguments)
	 *	@param	busses		an array of control busses. Each array element corresponds
	 *						to the control name in <code>ctrlNames</code> at the same array index. the array sizes of
	 *						<code>ctrlNames</code> and <code>busses</code> must be equal. The number of successive
	 *						controls is determined by each bus'es numChannels! Mapping is undone by successive <code>/n_set</code>,
	 *						<code>/n_setn</code> or  <code>/n_fill</code> commands.
	 *
	 *	@see	#mapn( String[], Bus[] )
	 */
	public OSCMessage mapnMsg( String[] ctrlNames, Bus[] busses )
	{
		final Object[] args = new Object[ ctrlNames.length * 3 + 1 ];
		args[ 0 ]			= new Integer( getNodeID() );
		
		for( int i = 0, j = 1; i < ctrlNames.length; i++ ) {
			args[ j++ ] = ctrlNames[ i ];
			args[ j++ ] = new Integer( busses[ i ].getIndex() );
			args[ j++ ] = new Integer( busses[ i ].getNumChannels() );
		}
	
		return( new OSCMessage( "/n_mapn", args ));
	}

	/**
	 *	Releases a node with default release time.
	 *
	 *	@see	#release( float )
	 */
	public void release()
	throws IOException
	{
		getServer().sendMsg( releaseMsg() );
	}

	/**
	 *	Releases a node. This assumes that that the <code>Synth</code>
	 *	represented by the node or the synths in the <code>Group</code> represented
	 *	by the node have specified a control named &quot;gate&quot; (usually used
	 *	by an <code>EnvGen</code> envelope generator UGen). This sends a <code>/n_set</code>
	 *	message for the &quot;gate&quot; control to the server.
	 *
	 *	@param	releaseTime	the time in seconds for the envelope to be released, or <code>0.0f</code>
	 *						to use the envelope's default release time
	 *
	 *	@see	#releaseMsg( float )
	 *	@throws	IOException	if an error occurs while sending the OSC message
	 */
	public void release( float releaseTime )
	throws IOException
	{
		getServer().sendMsg( releaseMsg( releaseTime ));
	}
	
	/**
	 *	Creates an OSC <code>/n_set</code> message to release a node with default release time.
	 *
	 *	@see	#releaseMsg( float )
	 */
	public OSCMessage releaseMsg()
	{
		return releaseMsg( 0.0f );
	}

	/**
	 *	Creates an OSC <code>/n_set</code> message to release a node. This assumes that that the <code>Synth</code>
	 *	represented by the node or the synths in the <code>Group</code> represented
	 *	by the node have specified a control named &quot;gate&quot; (usually used
	 *	by an <code>EnvGen</code> envelope generator UGen).
	 *
	 *	@param	releaseTime	the time in seconds for the envelope to be released, or <code>0.0f</code>
	 *						to use the envelope's default release time
	 *
	 *	@see	#release( float )
	 */
	public OSCMessage releaseMsg( float releaseTime )
	{
		return new OSCMessage( "/n_set", new Object[] { new Integer( getNodeID() ), "gate",
			new Float( releaseTime == 0.0f ? releaseTime : -1.0f - releaseTime )});	// so 1 sec. becomes -2 ??? XXX
	}
	
	/**
	 *	Sends an OSC <code>/n_trace</code> message to the server.
	 *	The server will print debugging information about the node into its terminal window.
	 *	If the node is a synth, this information includes the current input and output values for UGens in the synth.
	 *	If the node is a group, this information includes the node IDs inside the group.
	 *
	 *	@see	#traceMsg()
	 *	@throws	IOException	if an error occurs while sending the OSC message
	 */
	public void trace()
	throws IOException
	{
		getServer().sendMsg( traceMsg() );
	}

	/**
	 *	Creates an OSC <code>/n_trace</code> message for the node.
	 *	When this message is sent to the server, it will print debugging information about the node into its terminal window.
	 *	If the node is a synth, this information includes the current input and output values for UGens in the synth.
	 *	If the node is a group, this information includes the node IDs inside the group.
	 *
	 *	@see	#trace()
	 */
	public OSCMessage traceMsg()
	{
		return new OSCMessage( "/n_trace", new Object[] { new Integer( getNodeID() )});
	}

	/**
	 *	A debugging method that prints the results
	 *	of an OSC <code>/n_query</code> message into the server's default
	 *	print stream.
	 *
	 *	@see	#query( PrintStream )
	 *	@see	Server#getPrintStream()
	 *	@throws	IOException	if an error occurs while sending the OSC message
	 */
	public void query()
	throws IOException
	{
		query( Server.getPrintStream() );
	}

	/**
	 *	A debugging method that prints the results
	 *	of an OSC <code>/n_query</code> message into a given print stream.
	 *
	 *	@param	out	the stream to print out, e.g. <code>System.err</code>
	 *
	 *	@see	#query()
	 *	@throws	IOException	if an error occurs while sending the OSC message
	 */
	public void query( PrintStream out )
	throws IOException
	{
		final Object		nodeIDArg = new Integer( getNodeID() );
		final OSCMessage	reply;
		final Object		parent, prev, next, head, tail;
		final boolean		isGroup;
		
//		try {
			reply = getServer().sendMsgSync( queryMsg(), "/n_info", null, 0, nodeIDArg, 4f );
			if( reply == null ) {
				out.println( "[ \"/n_query\", " + nodeIDArg + " ] -> timeout" );
				return;
			}
			
			parent	= reply.getArg( 1 );
			prev	= reply.getArg( 2 );
			next	= reply.getArg( 3 );
			isGroup	= ((Number) reply.getArg( 4 )).intValue() == 1;
			
			out.println( (isGroup ? "Group   : " : "Synth   : ") + nodeIDArg + "\n  parent: " + parent + "\n  prev  : " + prev + "\n  next  : " + next );
			if( isGroup ) {
				head	= reply.getArg( 5 );
				tail	= reply.getArg( 6 );
				out.println( "  head  : " + head + "\n  tail  : " + tail );
			}			
//		}
//		catch( IOException e1 ) {
//			out.println( e1.toString() );
//		}
	}

	public OSCMessage queryMsg()
	{
		return new OSCMessage( "/n_query", new Object[] { new Integer( getNodeID() )});
	}

	public void register()
	throws IOException
	{
		register( false );
	}
	
	public void register( boolean assumePlaying )
	throws IOException
	{
		final NodeWatcher watcher = NodeWatcher.newFrom( getServer() );
		watcher.register( this, assumePlaying );
	}

	/**
	 *	Moves the node before another node in the server graph.
	 *
	 *	@param	aNode	the node before which this node is moved
	 *
	 *	@throws	IOException	if the OSC message could not be sent
	 *
	 *	@warning	this does not set the group field of the node.
	 *				the group field is only updated by a <code>NodeWatcher</code>
	 *
	 *	@see	#moveBeforeMsg( Node )
	 */
	public void moveBefore( Node aNode )
	throws IOException
	{
// NO
//	setGroup() is called by moveBeforeMsg()
//		this.setGroup( aNode.getGroup() );
		getServer().sendMsg( moveBeforeMsg( aNode ));
	}
	
	/**
	 *	Creates an OSC <code>/n_before</code> message. When the message is
	 *	sent to the server, this node is moved before another node in the server graph.
	 *
	 *	@param	aNode	the node before which this node is moved
	 *
	 *	@warning	this does not set the group field of the node.
	 *				the group field is only updated by a <code>NodeWatcher</code>
	 *
	 *	@see	#moveBefore( Node )
	 */
	public OSCMessage moveBeforeMsg( Node aNode )
	{
// removed 02-oct-05
//		this.setGroup( aNode.getGroup() );
		return( new OSCMessage( "/n_before", new Object[] {
			new Integer( this.getNodeID() ), new Integer( aNode.getNodeID() )}));
	}

	/**
	 *	Moves the node after another node in the server graph.
	 *
	 *	@param	aNode	the node after which this node is moved
	 *
	 *	@throws	IOException	if the OSC message could not be sent
	 *
	 *	@warning	this does not set the group field of the node.
	 *				the group field is only updated by a <code>NodeWatcher</code>
	 *
	 *	@see	#moveAfterMsg( Node )
	 */
	public void moveAfter( Node aNode )
	throws IOException
	{
// NO
//	setGroup() is called by moveAfterMsg()
//		this.setGroup( aNode.getGroup() );
		getServer().sendMsg( moveAfterMsg( aNode ));
	}
	
	/**
	 *	Creates an OSC <code>/n_after</code> message. When the message is
	 *	sent to the server, this node is moved before another node in the server graph.
	 *
	 *	@param	aNode	the node before which this node is moved
	 *
	 *	@warning	this does not set the group field of the node.
	 *				the group field is only updated by a <code>NodeWatcher</code>
	 *
	 *	@see	#moveAfter( Node )
	 */
	public OSCMessage moveAfterMsg( Node aNode )
	{
// removed 02-oct-05
//		this.setGroup( aNode.getGroup() );
		return( new OSCMessage( "/n_after", new Object[] {
			new Integer( this.getNodeID() ), new Integer( aNode.getNodeID() )}));
	}

	/**
	 *	Moves the node to the head of a group in the server graph.
	 *
	 *	@param	aGroup	the group to whose head this node is moved.
	 *					if <code>null</code> the server's default group is used
	 *
	 *	@throws	IOException	if the OSC message could not be sent
	 *
	 *	@warning	this does not set the group field of the node.
	 *				the group field is only updated by a <code>NodeWatcher</code>
	 *
	 *	@see	#moveToHeadMsg( Group )
	 */
	public void moveToHead( Group aGroup )
	throws IOException
	{
		(aGroup != null ? aGroup : getServer().getDefaultGroup()).moveNodeToHead( this );
	}

	/**
	 *	Creates an OSC <code>/g_head</code> message. When the message is
	 *	sent to the server, this node is moved to the head of a group in the server graph.
	 *
	 *	@param	aGroup	the group to whose head this node is moved.
	 *					if <code>null</code> the server's default group is used
	 *
	 *	@warning	this does not set the group field of the node.
	 *				the group field is only updated by a <code>NodeWatcher</code>
	 *
	 *	@see	#moveToHead( Group )
	 */
	public OSCMessage moveToHeadMsg( Group aGroup )
	{
		return (aGroup != null ? aGroup : getServer().getDefaultGroup()).moveNodeToHeadMsg( this );
	}

	/**
	 *	Moves the node to the tail of a group in the server graph.
	 *
	 *	@param	aGroup	the group to whose tail this node is moved.
	 *					if <code>null</code> the server's default group is used
	 *
	 *	@throws	IOException	if the OSC message could not be sent
	 *
	 *	@warning	this does not set the group field of the node.
	 *				the group field is only updated by a <code>NodeWatcher</code>
	 *
	 *	@see	#moveToTailMsg( Group )
	 */
	public void moveToTail( Group aGroup )
	throws IOException
	{
		(aGroup != null ? aGroup : getServer().getDefaultGroup()).moveNodeToTail( this );
	}

	/**
	 *	Creates an OSC <code>/g_tail</code> message. When the message is
	 *	sent to the server, this node is moved to the tail of a group in the server graph.
	 *
	 *	@param	aGroup	the group to whose tail this node is moved.
	 *					if <code>null</code> the server's default group is used
	 *
	 *	@warning	this does not set the group field of the node.
	 *				the group field is only updated by a <code>NodeWatcher</code>
	 *
	 *	@see	#moveToTail( Group )
	 */
	public OSCMessage moveToTailMsg( Group aGroup )
	{
		return (aGroup != null ? aGroup : getServer().getDefaultGroup()).moveNodeToTailMsg( this );
	}

	public void printOn( PrintStream stream )
	{
		stream.print( this.toString() );
	}
	
// -------------- TreeNode interface (subclasses need to complete it) --------------

	public TreeNode getParent()
	{
		return getGroup();
	}
}