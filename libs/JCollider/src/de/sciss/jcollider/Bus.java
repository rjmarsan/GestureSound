/*
 *  Bus.java
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
 */

package de.sciss.jcollider;

import java.io.IOException;
import java.io.PrintStream;

import de.sciss.net.OSCMessage;

/**
 *	Mimics SCLang's Bus class,
 *	that is, it's a client side
 *	representation of an audio or control bus
 *
 *	@warning	this is a quick direct translation from SClang
 *				which is largely untested. before all methods have been
 *				thoroughly verified, excepted some of them to be wrong
 *				or behave different than expected. what certainly works
 *				is instantiation
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.31, 08-Oct-07
 *
 *	@todo		missing methods (set, setn, fill, get, getn ...)
 */
public class Bus
implements Constants
{
	private final Server	server;

	private Object			rate;
	private int				index;
	private int				numChannels;

	/**
	 *	Creates an mono audio bus on the server at index 0.
	 *	This does not use the server's allocators.
	 *
	 *	@param	server	the <code>Server</code> on which the bus resides
	 */
	public Bus( Server server )
	{
		this( server, kAudioRate );
	}

	public Bus( Server server, Object rate )
	{
		this( server, rate, 0 );
	}

	public Bus( Server server, Object rate, int index )
	{
		this( server, rate, index, 1 );
	}

	public Bus( Server server, Object rate, int index, int numChannels )
	{
		this.rate			= rate;
		this.index			= index;
		this.numChannels	= numChannels;
		this.server			= server;
	}

	public static Bus control( Server server )
	{
		return Bus.control( server, 1 );
	}

	public static Bus control( Server server, int numChannels )
	{
		final int alloc	= server.getControlBusAllocator().alloc( numChannels );
		
		if( alloc == -1 ) {
			Server.getPrintStream().println(
				"Bus.control: failed to get a control bus allocated. " +
				"numChannels: " + numChannels + "; server: " + server.getName() );
			return null;
		} else {
			return new Bus( server, kControlRate, alloc, numChannels );
		}
	}

	public static Bus audio( Server server )
	{
		return Bus.audio( server, 1 );
	}
	
	public static Bus audio( Server server, int numChannels )
	{
		final int alloc	= server.getAudioBusAllocator().alloc( numChannels );
		
		if( alloc == -1 ) {
			Server.getPrintStream().println(
				"Bus.audio: failed to get a audio bus allocated. " +
				"numChannels: " + numChannels + "; server: " + server.getName() );
			return null;
		} else {
			return new Bus( server, kAudioRate, alloc, numChannels );
		}
	}

	public static Bus alloc( Server server, Object rate )
	{
		return Bus.alloc( server, rate, 1 );
	}

	public static Bus alloc( Server server, Object rate, int numChannels )
	{
		if( rate == kAudioRate ) {
			return Bus.audio( server, numChannels );
		} else if( rate == kControlRate ) {
			return Bus.control( server, numChannels );
		} else {
			throw new IllegalArgumentException( rate.toString() );
		}
	}
	
	public String toString()
	{
		return( "Bus(" + server.getName() + ", " + getRate() + ", " + getIndex() + ", " + getNumChannels() + ")" );
	}

	public Object getRate()
	{
		return rate;
	}
	
	public int getNumChannels()
	{
		return numChannels;
	}
	
	public int getIndex()
	{
		return index;
	}
	
	public Server getServer()
	{
		return server;
	}
	
	private void setRate( Object rate )
	{
		this.rate	= rate;
	}
	
	private void setNumChannels( int numChannels )
	{
		this.numChannels = numChannels;
	}
	
	private void setIndex( int index )
	{
		this.index	= index;
	}
	
	// for mono
	public void set( float value )
	throws IOException
	{
		getServer().sendMsg( setMsg( value ));
	}

	public void set( int[] offsets, float[] values )
	throws IOException
	{
		getServer().sendMsg( setMsg( offsets, values ));
	}

	/**
	 * 	Set the value of a monophonic bus.
	 * 	For multichannel busses, use
	 * 	setnMsg instead.
	 */
	public OSCMessage setMsg( float value )
	{
		return new OSCMessage( "/c_set", new Object[] {
			new Integer( getIndex() ), new Float( value )});
	}

	/**
	 * 	@warning	has not been tested
	 */
	public OSCMessage setMsg( int[] offsets, float[] values )
	{
		final int numEntries = offsets.length;
		if( numEntries != values.length ) {
			throw new IllegalArgumentException( "Number of offsets / values must be the same" );
		}
		final Object[] args = new Object[ numEntries << 1 ];
		final int idx = getIndex();
		for( int i = 0, j = 0; i < numEntries; i++ ) {
			args[ j++ ] = new Integer( idx + offsets[ i ]);
			args[ j++ ] = new Float( values[ i ]);
		}
		return new OSCMessage( "/c_set", args );
	}

	public void setn( float[] values )
	throws IOException
	{
		getServer().sendMsg( setnMsg( values ));
	}

	public void setn( int[] offsets, float[][] values )
	throws IOException
	{
		getServer().sendMsg( setnMsg( offsets, values ));
	}

	/**
	 * 	@warning	has not been tested
	 */
	public OSCMessage setnMsg( float[] values )
	{
		final int numValues = values.length;
		final Object[] args = new Object[ numValues + 2 ];
		args[ 0 ] = new Integer( getIndex() );
		args[ 1 ] = new Integer( numValues );
		for( int i = 0, j = 2; i < numValues; i++, j++ ) {
			args[ j ] = new Float( values[ i ]);
		}
		return new OSCMessage( "/c_setn", args );
	}

	/**
	 * 	@warning	has not been tested
	 */
	public OSCMessage setnMsg( int[] offsets, float[][] values )
	{
		final int numEntries = offsets.length;
		if( numEntries != values.length ) {
			throw new IllegalArgumentException( "Number of offsets / values must be the same" );
		}
		int numValues = 0;
		for( int i = 0; i < numEntries; i++ ) numValues += values[ i ].length;
		
		final int idx = getIndex();
		final Object[] args = new Object[ (numEntries << 1) + numValues ];
		for( int i = 0, j = 0; i < numEntries; i++ ) {
			args[ j++ ] = new Integer( idx + offsets[ i ]);
			final float[] vals = values[ i ];
			final int numVals = vals.length;
			args[ j++ ] = new Integer( numVals );
			for( int k = 0; k < numVals; k++, j++ ) {
				args[ j ] = new Float( vals[ k ]);
			}
		}
		return new OSCMessage( "/c_setn", args );
	}
	
	public void fill( float value )
	throws IOException
	{
		getServer().sendMsg( fillMsg( value ));
	}
	
	public void fill( int offset, int numChans, float value )
	throws IOException
	{
		getServer().sendMsg( fillMsg( offset, numChans, value ));
	}
	
	public void fill( int[] numChans, float[] values )
	throws IOException
	{
		getServer().sendMsg( fillMsg( numChans, values ));
	}
	
	public void fill( int[] offsets, int[] numChans, float[] values )
	throws IOException
	{
		getServer().sendMsg( fillMsg( offsets, numChans, values ));
	}
	
	public OSCMessage fillMsg( float value )
	{
		return fillMsg( 0, getNumChannels(), value );
	}
	
	public OSCMessage fillMsg( int offset, int numChans, float value )
	{
		return new OSCMessage( "/c_fill", new Object[] {
			new Integer( getIndex() + offset ), new Integer( numChans ), new Float( value )});
	}
	
	public OSCMessage fillMsg( int[] numChans, float[] values )
	{
		final int numEntries = numChans.length;
		final int[] offsets = new int[ numEntries ];
		for( int i = 0, j = 0; i < numEntries; i++ ) {
			offsets[ i ] = j;
			j += numChans[ i ];
		}
		return fillMsg( offsets, numChans, values );
	}
	
	/**
	 * 	@warning	has not been tested
	 */
	public OSCMessage fillMsg( int[] offsets, int[] numChans, float[] values )
	{
		final int numEntries = offsets.length;
		if( (numEntries != numChans.length) || (numEntries != values.length) ) {
			throw new IllegalArgumentException( "Number of offsets / numChans / values must be the same" );
		}
		
		final Object[] args = new Object[ numEntries * 3 ];
		final int idx = getIndex();
		for( int i = 0, j = 0; i < numEntries; i++ ) {
			args[ j++ ] = new Integer( idx + offsets[ i ]);
			args[ j++ ] = new Integer( numChans[ i ]);
			args[ j++ ] = new Integer( idx + offsets[ i ]);
		}
		
		return new OSCMessage( "/c_fill", args );
	}
	
	public void get( GetCompletionAction action )
	throws IOException
	{
		get( 0, action );
	}
	
	public void get( int offset, GetCompletionAction action )
	throws IOException
	{
		get( new int[] { offset }, action );
	}
	
	/**
	 * 	@warning	has not been tested
	 */
	public void get( final int[] offsets, final GetCompletionAction action )
	throws IOException
	{
		final OSCMessage m = getMsg( offsets );
		final int idx = getIndex();
		final OSCResponderNode resp = new OSCResponderNode( getServer(), "/c_set", new OSCResponderNode.Action() {
			public void respond( OSCResponderNode r, OSCMessage msg, long time )
			{
				final int numVals = msg.getArgCount() >> 1;
				if( numVals != offsets.length ) return;
				for( int i = 0, j = 0; i < numVals; i++, j += 2 ) {
					if( ((Number) msg.getArg( j )).intValue() != idx + offsets[ i ]) return;
				}
				final float[] vals = new float[ numVals ];
				for( int i = 0, j = 1; i < numVals; i++, j += 2 ) {
					vals[ i ] = ((Number) msg.getArg( j )).floatValue();
				}
				r.remove();
				action.completion( Bus.this, vals );
			}
		});
		resp.add();
		getServer().sendMsg( m );
	}
	
	/**
	 * 	@warning	has not been tested
	 */
	public void getn( final int[] offsets, final int[] numChans, final GetCompletionAction action )
	throws IOException
	{
		final int numEntries = offsets.length;
		if( numEntries != numChans.length ) {
			throw new IllegalArgumentException( "Number of offsets / numChans must be the same" );
		}

		final OSCMessage m = getnMsg( offsets, numChans );
		final int idx = getIndex();
		final OSCResponderNode resp = new OSCResponderNode( getServer(), "/c_setn", new OSCResponderNode.Action() {
			public void respond( OSCResponderNode r, OSCMessage msg, long time )
			{
				final int numArgs = msg.getArgCount();
				int numVals = 0;
				for( int i = 0, j = 0; j < numArgs; i++ ) {
					if( i >= numEntries ) return;
					final int nc = numChans[ i ];
					if( ((Number) msg.getArg( j++ )).intValue() != idx + offsets[ i ]) return;
					if( ((Number) msg.getArg( j++ )).intValue() != nc ) return;
					numVals += nc;
					j += nc;
				}
				
				final float[] vals = new float[ numVals ];
				for( int i = 0, j = 2, k = 0; i < numEntries; i++, j += 2 ) {
					for( int m = 0; m < numChans[ i ]; m++ ) {
						vals[ k++ ] = ((Number) msg.getArg( j++ )).floatValue();
					}
				}
				r.remove();
				action.completion( Bus.this, vals );
			}
		});
		resp.add();
		getServer().sendMsg( m );
	}
	
	public OSCMessage getMsg()
	{
		return new OSCMessage( "/c_get", new Object[] { new Integer( getIndex() )});
	}

	/**
	 * 	@warning	has not been tested
	 */
	public OSCMessage getMsg( int[] offsets )
	{
		final int numOffsets = offsets.length;
		final Object[] args = new Object[ numOffsets ];
		final int idx = getIndex();
		for( int i = 0; i < numOffsets; i++ ) {
			args[ i ] = new Integer( idx + offsets[ i ]);
		}
		return new OSCMessage( "/c_get", args );
	}
	
	public OSCMessage getnMsg()
	{
		return getnMsg( 0, getNumChannels() );
	}
	
	public OSCMessage getnMsg( int numChans )
	{
		return getnMsg( 0, numChans );
	}
	
	public OSCMessage getnMsg( int offset, int numChans )
	{
		return new OSCMessage( "/c_getn", new Object[] {
			new Integer( getIndex() + offset ), new Integer( numChans )});
	}
	
	/**
	 * 	@warning	has not been tested
	 */
	public OSCMessage getnMsg( int[] offsets, int[] numChans )
	{
		final int numEntries = offsets.length;
		if( numEntries != numChans.length ) {
			throw new IllegalArgumentException( "Number of offsets / numChans must be the same" );
		}
		final int idx = getIndex();
		final Object[] args = new Object[ numEntries << 1 ];
		for( int i = 0, j = 0; i < numEntries; i++ ) {
			args[ j++ ] = new Integer( idx + offsets[ i ]);
			args[ j++ ] = new Integer( numChans[ i ]);
		}
		return new OSCMessage( "/c_getn", args );
	}
	
	public void free()
	{
		final int idx = getIndex();
		if( idx == -1 ) {
			printOn( Server.getPrintStream() );
			Server.getPrintStream().println( " has already been freed" );
			return;
		}

		if( getRate() == kAudioRate ) {
			getServer().getAudioBusAllocator().free( idx );
		} else if( getRate() == kControlRate ) {
			getServer().getControlBusAllocator().free( idx );
		} else {
			throw new IllegalStateException( getRate().toString() );
		}

		setIndex( -1 );
		setNumChannels( -1 );
	}
	
	// allow reallocation
	public void alloc()
	{
		if( getRate() == kAudioRate ) {
			setIndex( getServer().getAudioBusAllocator().alloc( getNumChannels() ));
		} else if( getRate() == kControlRate ) {
			setIndex( getServer().getControlBusAllocator().alloc( getNumChannels() ));
		} else {
			throw new IllegalStateException( getRate().toString() );
		}
	}
	
	public void realloc()
	{
		if( getIndex() == -1 ) return;
	
		final Object	oldRate	= getRate();
		final int		oldCh	= getNumChannels();
		
		free();
		setRate( oldRate );
		setNumChannels( oldCh );
		alloc();
	}

//	// alternate syntaxes
//	setAll { arg value;
//		this.fill(value,numChannels);
//	}
//	
//	value_ { arg value;
//		this.fill(value,numChannels);
//	}
	
	public void printOn( PrintStream stream )
	{ 
		stream.print( this.getClass().getName() + "(" + getServer().getName() + "," + getRate() + "," +
			getIndex() + "," + getNumChannels() + ")" );
	}
	
	public boolean equals( Object o )
	{
		if( o instanceof Bus ) {
			final Bus aBus = (Bus) o;
			return( this.getIndex()			== aBus.getIndex() &&
					this.getNumChannels()	== aBus.getNumChannels() &&
					this.getRate()			== aBus.getRate() &&
					this.getServer()		== aBus.getServer() );
		} else {
			return false;
		}
	}
	
	public int hashCode()
	{
		return( getIndex() ^ -getNumChannels() ^ getRate().hashCode() ^ getServer().hashCode() );
	}
	
	/**
	 *	Queries whether this bus is playing audio
	 *	onto the hardware audio interface channels.
	 *
	 *	@return	<code>true</code> if this bus plays audio on audible interface channels,
	 *			<code>false</code> otherwise
	 */
	public boolean isAudioOut()
	{
		return( (rate == kAudioRate) && (getIndex() < getServer().getOptions().getFirstPrivateBus()) );
	}
	
//	ar {
//		if(rate == \audio,{
//			^In.ar(index,numChannels)
//		},{
//			//"Bus converting control to audio rate".inform;
//			^K2A.ar( In.kr(index,numChannels) )
//		})
//	}
//	
//	kr {
//		if(rate == \audio,{
//			^A2K.kr(index,numChannels)
//		},{
//			^In.kr(index,numChannels)
//		})
//	}

//	play { arg target=0, outbus, fadeTime, addAction=\addToTail;
//		if(this.isAudioOut.not,{ // returns a Synth
//			^{ this.ar }.play(target, outbus, fadeTime, addAction);
//		});
//	}

	// ---------- internal classes and interfaces ----------

	/**
	 *	Interface describing an action to take place after
	 *	an asynchronous bus query is completed.
	 */
	public static interface GetCompletionAction
	{
	   /**
		*	Executes the completion action.
		*
		*	@param	bus		the bus whose asynchronous action is completed.
		*/
		public void completion( Bus bus, float[] values );
	}
}
