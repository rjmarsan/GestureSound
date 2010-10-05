/*
 *  Synth.java
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
 *		02-Oct-05	removed all setGroup statements. to have the group
 *					set correctly, use a NodeWatcher instead
 *		12-Jul-08	added get(n)(Msg) and grain constructors
 */

package de.sciss.jcollider;

import java.io.IOException;
import java.util.Enumeration;
import javax.swing.tree.TreeNode;

import de.sciss.net.OSCBundle;
import de.sciss.net.OSCMessage;

/**
 *	Mimics SCLang's Synth class,
 *	that is, it's a client side
 *	representation of a synth in the synthesis graph
 *
 *	@warning	this is a quick direct translation from SClang
 *				which is largely untested. before all methods have been
 *				thoroughly verified, excepted some of them to be wrong
 *				or behave different than expected. what certainly works
 *				is instantiation and new-messages
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.33, 12-Jul-08
 */
public class Synth
extends Node
{
	private final String defName;

	// immediately sends
	public Synth( String defName, Node target )
	throws IOException
	{
		this( defName, null, null, target, kAddToHead );
	}

	// immediately sends
	public Synth( String defName, String[] argNames, float[] argValues, Node target )
	throws IOException
	{
		this( defName, argNames, argValues, target, kAddToHead );
	}

	// immediately sends
	public Synth( String defName, String[] argNames, float[] argValues, Node target, int addAction )
	throws IOException
	{
		this( defName, target.getServer(), target.getServer().nextNodeID() );
		
		getServer().sendMsg( newMsg( target, argNames, argValues, addAction ));
	}
	
	// doesn't send
	private Synth( String defName, Server server, int nodeID )
	{
		super( server, nodeID );
		
		this.defName	= defName;
	}
	
	public static void grain( String defName, Node target )
	throws IOException
	{
		grain( defName, null, null, target, kAddToHead );
	}

	public static void grain( String defName, String[] argNames, float[] argValues, Node target )
	throws IOException
	{
		grain( defName, argNames, argValues, target, kAddToHead );
	}

	public static void grain( String defName, String[] argNames, float[] argValues, Node target, int addAction )
	throws IOException
	{
		final Synth s = new Synth( defName, target.getServer(), -1 );

		s.getServer().sendMsg( s.newMsg( target, argNames, argValues, addAction ));
	}
	
	public String getDefName()
	{
		return defName;
	}

	public OSCMessage newMsg()
	{
		return newMsg( getServer().asTarget() );
	}

	public OSCMessage newMsg( Node target )
	{
		return newMsg( target, null, null );
	}

	public OSCMessage newMsg( Node target, String[] argNames, float[] argValues )
	{
		return newMsg( target, argNames, argValues, kAddToHead );
	}

	public OSCMessage newMsg( Node target, String[] argNames, float[] argValues, int addAction )
	{
		if( target == null ) target = getServer().getDefaultGroup();
	
// removed 02-oct-05
//		this.setGroup( addAction == kAddToHead || addAction == kAddToTail ?
//			(Group) target : target.getGroup() );
			
		final int		argNum	= argNames == null ? 0 : argNames.length;
		final Object[]	allArgs	= new Object[ argNum * 2 + 4 ];
		
		allArgs[ 0 ]			= getDefName();
		allArgs[ 1 ]			= new Integer( getNodeID() );
		allArgs[ 2 ]			= new Integer( addAction );
		allArgs[ 3 ]			= new Integer( target.getNodeID() );
		
		for( int i = 0, j = 4; i < argNum; i++ ) {
			allArgs[ j++ ]		= argNames[ i ];
			allArgs[ j++ ]		= new Float( argValues[ i ]);
		}
			
		return new OSCMessage( "/s_new", allArgs );
	}

	public static Synth newPaused( String defName, String[] argNames, float[] argValues, Node target )
	throws IOException
	{
		return Synth.newPaused( defName, argNames, argValues, target, kAddToHead );
	}

	public static Synth newPaused( String defName, String[] argNames, float[] argValues, Node target, int addAction )
	throws IOException
	{
		final Synth		synth	= Synth.basicNew( defName, target.getServer() );
		final OSCBundle	bndl	= new OSCBundle( 0.0 );
		
		bndl.addPacket( synth.newMsg( target, argNames, argValues, addAction ));
		bndl.addPacket( synth.runMsg( false ));

		synth.getServer().sendBundle( bndl );
		return synth;
	}

	// does not send	(used for bundling)
	public static Synth basicNew( String defName, Server server )
	{
		return Synth.basicNew( defName, server, server.nextNodeID() );
	}

	public static Synth basicNew( String defName, Server server, int nodeID )
	{
		return new Synth( defName, server, nodeID );
	}

	public static Synth after( Node aNode, String defName )
	throws IOException
	{
		return Synth.after( aNode, defName, null, null );
	}

	public static Synth after( Node aNode, String defName, String[] argNames, float[] argValues )
	throws IOException
	{
		return new Synth( defName, argNames, argValues, aNode, kAddAfter );
	}

	public static Synth before( Node aNode, String defName )
	throws IOException
	{
		return Synth.before( aNode, defName, null, null );
	}

	public static Synth before( Node aNode, String defName, String[] argNames, float[] argValues )
	throws IOException
	{
		return new Synth( defName, argNames, argValues, aNode, kAddBefore );
	}

	public static Synth head( Group aGroup, String defName )
	throws IOException
	{
		return Synth.head( aGroup, defName, null, null );
	}

	public static Synth head( Node aGroup, String defName, String[] argNames, float[] argValues )
	throws IOException
	{
		return new Synth( defName, argNames, argValues, aGroup, kAddToHead );
	}

	public static Synth tail( Group aGroup, String defName )
	throws IOException
	{
		return Synth.tail( aGroup, defName, null, null );
	}

	public static Synth tail( Node aGroup, String defName, String[] argNames, float[] argValues )
	throws IOException
	{
		return new Synth( defName, argNames, argValues, aGroup, kAddToTail );
	}

	public static Synth replace( Node nodeToReplace, String defName )
	throws IOException
	{
		return Synth.replace( nodeToReplace, defName, null, null );
	}

	public static Synth replace( Node nodeToReplace, String defName, String[] argNames, float[] argValues )
	throws IOException
	{
		return new Synth( defName, argNames, argValues, nodeToReplace, kAddReplace );
	}

	public OSCMessage addToHeadMsg( Group aGroup, String[] argNames, float[] argValues )
	{
		return newMsg( aGroup, argNames, argValues, kAddToHead );
	}

	public OSCMessage addToTailMsg( Group aGroup, String[] argNames, float[] argValues )
	{
		return newMsg( aGroup, argNames, argValues, kAddToTail );
	}
	
	public OSCMessage addAfterMsg( Node aNode, String[] argNames, float[] argValues )
	{
		return newMsg( aNode, argNames, argValues, kAddAfter );
	}

	public OSCMessage addBeforeMsg( Node aNode, String[] argNames, float[] argValues )
	{
		return newMsg( aNode, argNames, argValues, kAddBefore );
	}
	
	public OSCMessage addReplaceMsg( Node aNode, String[] argNames, float[] argValues )
	{
		return newMsg( aNode, argNames, argValues, kAddReplace );
	}

	/**
	 * 	Queries the current value of a synth control.
	 * 
	 *	@param	index	the index of the control to query
	 *	@return	the curresponding control value
	 *	@throws IOException	when an error occurs sending the message, or when
	 *			a timeout or failure occurs with scsynth processing the message
	 */
	public float get( int index )
	throws IOException
	{
		final OSCMessage getMsg = getMsg( index );
		final OSCMessage replyMsg = getServer().sendMsgSync( getMsg, "/n_set", "/fail",
		    new int[] { 0, 1 }, new Object[] { new Integer( getNodeID() ), new Integer( index )},
		    new int[] { 0 }, new Object[] { getMsg.getName() }, 4f );
		
		if( (replyMsg != null) && replyMsg.getName().equals( "/n_set" )) {
			return ((Number) replyMsg.getArg( 2 )).floatValue();
		} else {
			throw new IOException( replyMsg == null ? "s_get timeout" : "s_get failed" );
		}
	}
	
	/**
	 * 	Queries different current values of a synth control.
	 * 
	 *	@param	indices	the indices of the controls to query
	 *	@return	the curresponding control values, or null if
	 *			a timeout or failure occurs with scsynth processing the message
	 *	@throws IOException	when an error occurs sending the message
	 */
	public float[] get( int[] indices )
	throws IOException
	{
		final OSCMessage	getMsg		= getMsg( indices );
		final int[]			doneIndices	= new int[ indices.length + 1 ];
		final Object[]		doneMatches	= new Object[ indices.length + 1 ];
		doneIndices[ 0 ] = 0;
		doneMatches[ 0 ] = new Integer( getNodeID() );
		for( int i = 1, j = 0, k = 1; j < indices.length; i++, j++, k += 2 ) {
			doneIndices[ i ] = k;
			doneMatches[ i ] = new Integer( indices[ j ]);
		}
		final OSCMessage replyMsg = getServer().sendMsgSync( getMsg, "/n_set", "/fail",
		    doneIndices, doneMatches,
		    new int[] { 0 }, new Object[] { getMsg.getName() }, 4f );
		
		if( (replyMsg != null) && replyMsg.getName().equals( "/n_set" )) {
			final float[] values = new float[ indices.length ];
			for( int i = 2, j = 0; j < indices.length; i += 2, j++ ) {
				 values[ j ] = ((Number) replyMsg.getArg( i )).floatValue();
			}
			return values;
		} else {
			return null;
		}
	}
	
	/**
	 * 	Queries the current value of a synth control.
	 * 
	 *	@param	name	the name of the control to query
	 *	@return	the curresponding control value
	 *	@throws IOException	when an error occurs sending the message, or when
	 *			a timeout or failure occurs with scsynth processing the message
	 */
	public float get( String name )
	throws IOException
	{
		final OSCMessage getMsg = getMsg( name );
		final OSCMessage replyMsg = getServer().sendMsgSync( getMsg, "/n_set", "/fail",
		    new int[] { 0, 1 }, new Object[] { new Integer( getNodeID() ), name },
		    new int[] { 0 }, new Object[] { getMsg.getName() }, 4f );
		
		if( (replyMsg != null) && replyMsg.getName().equals( "/n_set" )) {
			return ((Number) replyMsg.getArg( 2 )).floatValue();
		} else {
			throw new IOException( replyMsg == null ? "s_get timeout" : "s_get failed" );
		}
	}
	
	/**
	 * 	Queries different current values of a synth control.
	 * 
	 *	@param	names	the names of the controls to query
	 *	@return	the curresponding control values, or null if
	 *			a timeout or failure occurs with scsynth processing the message
	 *	@throws IOException	when an error occurs sending the message
	 */
	public float[] get( String[] names )
	throws IOException
	{
		final OSCMessage	getMsg		= getMsg( names );
		final int[]			doneIndices	= new int[ names.length + 1 ];
		final Object[]		doneMatches = new Object[ names.length + 1 ];
		doneIndices[ 0 ] = 0;
		doneMatches[ 0 ] = new Integer( getNodeID() );
		for( int i = 1, j = 0, k = 1; j < names.length; i++, j++, k += 2 ) {
			doneIndices[ i ] = k;
			doneMatches[ i ] = names[ j ];
		}
		final OSCMessage replyMsg = getServer().sendMsgSync( getMsg, "/n_set", "/fail",
		    doneIndices, doneMatches,
		    new int[] { 0 }, new Object[] { getMsg.getName() }, 4f );
		
		if( (replyMsg != null) && replyMsg.getName().equals( "/n_set" )) {
			final float[] values = new float[ names.length ];
			for( int i = 2, j = 0; j < names.length; i += 2, j++ ) {
				 values[ j ] = ((Number) replyMsg.getArg( i )).floatValue();
			}
			return values;
		} else {
			return null;
		}
	}
	
	public OSCMessage getMsg( int index )
	{
		return new OSCMessage( "/s_get", new Object[] { new Integer( getNodeID() ), new Integer( index )});
	}

	public OSCMessage getMsg( int[] indices )
	{
		final Object[] args = new Object[ indices.length + 1 ];
		args[ 0 ] = new Integer( getNodeID() );
		for( int i = 1, j = 0; j < indices.length; i++, j++ ) {
			args[ i ] = new Integer( indices[ j ]);
		}
		return new OSCMessage( "/s_get", args );
	}

	public OSCMessage getMsg( String name )
	{
		return new OSCMessage( "/s_get", new Object[] { new Integer( getNodeID() ), name });
	}

	public OSCMessage getMsg( String[] names )
	{
		final Object[] args = new Object[ names.length + 1 ];
		args[ 0 ] = new Integer( getNodeID() );
		System.arraycopy(  names, 0, args, 1, names.length );
		return new OSCMessage( "/s_get", args );
	}

	/**
	 * 	Queries a range of current values of the synth's controls.
	 * 
	 *	@param	index	the start index of the controls to query
	 *	@param	count	the number of successive controls to query
	 *	@return	the curresponding control values or null, when
	 *			a timeout or failure occurs with scsynth processing the message
	 *	@throws IOException	when an error occurs sending the message
	 */
	public float[] getn( int index, int count )
	throws IOException
	{
		final OSCMessage getnMsg = getnMsg( index, count );
		final OSCMessage replyMsg = getServer().sendMsgSync( getnMsg, "/n_setn", "/fail",
		    new int[] { 0, 1, 2 }, new Object[] { new Integer( getNodeID() ), new Integer( index ), new Integer( count )},
		    new int[] { 0 }, new Object[] { getnMsg.getName() }, 4f );
		
		if( (replyMsg != null) && replyMsg.getName().equals( "/n_setn" )) {
			final float[] values = new float[ count ];
			for( int i = 3, j = 0; j < count; j++ ) {
				values[ j ] = ((Number) replyMsg.getArg( i )).floatValue();
			}
			return values;
		} else {
			return null;
		}
	}
	
	/**
	 * 	Queries a range of current values of the synth's controls.
	 * 
	 *	@param	name	the name of the first control to query
	 *	@param	count	the number of successive controls to query
	 *	@return	the curresponding control values or null, when
	 *			a timeout or failure occurs with scsynth processing the message
	 *	@throws IOException	when an error occurs sending the message
	 */
	public float[] getn( String name, int count )
	throws IOException
	{
		final OSCMessage getnMsg = getnMsg( name, count );
		final OSCMessage replyMsg = getServer().sendMsgSync( getnMsg, "/n_setn", "/fail",
		    new int[] { 0, 1, 2 }, new Object[] { new Integer( getNodeID() ), new Integer( name ), new Integer( count )},
		    new int[] { 0 }, new Object[] { getnMsg.getName() }, 4f );
		
		if( (replyMsg != null) && replyMsg.getName().equals( "/n_setn" )) {
			final float[] values = new float[ count ];
			for( int i = 3, j = 0; j < count; j++ ) {
				values[ j ] = ((Number) replyMsg.getArg( i )).floatValue();
			}
			return values;
		} else {
			return null;
		}
	}

	/**
	 * 	Queries different ranges of current values of the synth's controls.
	 * 
	 *	@param	indices	the start indices of the controls to query
	 *	@param	counts	for each start index, the number of successive controls to query
	 *	@return	the curresponding control values, or null if
	 *			a timeout or failure occurs with scsynth processing the message
	 *	@throws IOException	when an error occurs sending the message
	 */
	public float[] getn( int[] indices, int[] counts )
	throws IOException
	{
		// getnMsg() checks indices.length versus counts.length already
		final OSCMessage	getnMsg		= getnMsg( indices, counts );
		final int[]			doneIndices	= new int[ (indices.length << 1) + 1 ];
		final Object[]		doneMatches	= new Object[ (indices.length << 1) + 1 ];
		int numValues    = 0;
		doneIndices[ 0 ] = 0;
		doneMatches[ 0 ] = new Integer( getNodeID() );
		for( int i = 1, j = 0, k = 1; j < indices.length; j++ ) {
			doneIndices[ i ] = k;
			doneMatches[ i ] = new Integer( indices[ j ]);
			i++;
			k++;
			doneIndices[ i ] = k;
			doneMatches[ i ] = new Integer( counts[ j ]);
			i++;
			k += counts[ j ] + 1;
			numValues += counts[ j ]; 
		}
		final OSCMessage replyMsg = getServer().sendMsgSync( getnMsg, "/n_setn", "/fail",
		    doneIndices, doneMatches,
		    new int[] { 0 }, new Object[] { getnMsg.getName() }, 4f );
		
		if( (replyMsg != null) && replyMsg.getName().equals( "/n_setn" )) {
			final float[] values = new float[ numValues ];
			for( int i = 3, j = 0, k = 0; j < indices.length; i += 2, j++ ) {
				for( int m = 0; m < counts[ j ]; i++, k++, m++ ) {
					values[ k ] = ((Number) replyMsg.getArg( i )).floatValue();
				}
			}
			return values;
		} else {
			return null;
		}
	}

	/**
	 * 	Queries different ranges of current values of the synth's controls.
	 * 
	 *	@param	names	the start names of the controls to query
	 *	@param	counts	for each start name, the number of successive controls to query
	 *	@return	the curresponding control values, or null if
	 *			a timeout or failure occurs with scsynth processing the message
	 *	@throws IOException	when an error occurs sending the message
	 */
	public float[] getn( String[] names, int[] counts )
	throws IOException
	{
		// getnMsg() checks indices.length versus counts.length already
		final OSCMessage	getnMsg		= getnMsg( names, counts );
		final int[]			doneIndices	= new int[ (names.length << 1) + 1 ];
		final Object[]		doneMatches	= new Object[ (names.length << 1) + 1 ];
		int numValues    = 0;
		doneIndices[ 0 ] = 0;
		doneMatches[ 0 ] = new Integer( getNodeID() );
		for( int i = 1, j = 0, k = 1; j < names.length; j++ ) {
			doneIndices[ i ] = k;
			doneMatches[ i ] = new Integer( names[ j ]);
			i++;
			k++;
			doneIndices[ i ] = k;
			doneMatches[ i ] = new Integer( counts[ j ]);
			i++;
			k += counts[ j ] + 1;
			numValues += counts[ j ]; 
		}
		final OSCMessage replyMsg = getServer().sendMsgSync( getnMsg, "/n_setn", "/fail",
		    doneIndices, doneMatches,
		    new int[] { 0 }, new Object[] { getnMsg.getName() }, 4f );
		
		if( (replyMsg != null) && replyMsg.getName().equals( "/n_setn" )) {
			final float[] values = new float[ numValues ];
			for( int i = 3, j = 0, k = 0; j < names.length; i += 2, j++ ) {
				for( int m = 0; m < counts[ j ]; i++, k++, m++ ) {
					values[ k ] = ((Number) replyMsg.getArg( i )).floatValue();
				}
			}
			return values;
		} else {
			return null;
		}
	}

	public OSCMessage getnMsg( int index, int count )
	{
		return new OSCMessage( "/s_getn", new Object[] { new Integer( getNodeID() ),
		 					   new Integer( index ), new Integer( count )});
	}

	public OSCMessage getnMsg( int[] indices, int[] counts )
	{
		if( indices.length != counts.length ) throw new IllegalArgumentException( "# of indices must match # of counts" );
		
		final Object[] args = new Object[ (indices.length << 1) + 1 ];
		args[ 0 ] = new Integer( getNodeID() );
		for( int i = 1, j = 0; j < indices.length; j++ ) {
			args[ i++ ] = new Integer( indices[ j ]);
			args[ i++ ] = new Integer( counts[ j ]);
		}
		return new OSCMessage( "/s_getn", args );
	}

	public OSCMessage getnMsg( String name, int count )
	{
		return new OSCMessage( "/s_getn", new Object[] { new Integer( getNodeID() ),
							   name, new Integer( count )});
	}

	public OSCMessage getnMsg( String[] names, int[] counts )
	{
		if( names.length != counts.length ) throw new IllegalArgumentException( "# of names must match # of counts" );
		
		final Object[] args = new Object[ (names.length << 1) + 1 ];
		args[ 0 ] = new Integer( getNodeID() );
		for( int i = 1, j = 0; j < names.length; j++ ) {
			args[ i++ ] = names[ j ];
			args[ i++ ] = new Integer( counts[ j ]);
		}
		return new OSCMessage( "/s_getn", args );
	}
	
	public String toString()
	{
		if( getName() == null ) {
			return( "Synth(" + getNodeID() + ",\"" + defName + "\")" );
		} else {
			return( "Synth::" + getName() + "(" + getNodeID() + ",\"" + defName + "\")" );
		}
	}

// -------------- TreeNode interface --------------

	public TreeNode getChildAt( int childIndex )
	{
		return null;
	}

	public int getChildCount()
	{
		return 0;
	}
	
	public int getIndex( TreeNode node )
	{
		return -1;
	}
	
	public boolean getAllowsChildren()
	{
		return false;
	}
	
	public boolean isLeaf()
	{
		return true;
	}
	
	public Enumeration children()
	{
		return null;	// XXX allowed?
	}
}