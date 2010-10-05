/*
 *  ContiguousBlockAllocator.java
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
 *		24-Jul-06	created
 */

package de.sciss.jcollider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *	Quite a 1:1 translation from the SClang class written by James Harkins, this
 *	class is a robust allocator for busses in a dynamic environment.
 *	It does not exhibit the fragmentation problem of the PowerOfTwoAllocator.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.32, 25-Feb-08
 *
 *	@todo		freed should be a sorted Map !! then findAvailable could be faster
 *
 *	@warning	this class has not been thoroughly debugged
 */
public class ContiguousBlockAllocator
implements BlockAllocator
{
	private final int		size;
	private final Block[]	array;
	private	final Map		freed;
	private final int		pos;
	private int				top;

	public ContiguousBlockAllocator( int size )
	{
		this( size, 0 );
	}
	
	public ContiguousBlockAllocator( int size, int pos )
	{
		this.size	= size;
		this.pos	= pos;
		
		array		= new Block[ size ];
		array[ pos ]= new Block( pos, size - pos );
		freed		= new HashMap();
		top			= pos;
	}
	
	public int alloc()
	{
		return alloc( 1 );
	}
	
	public int alloc( int n )
	{
		final Block b = findAvailable( n );
		if( b == null ) return -1;
		
		return reserve( b.start, n, b, null ).start;
	}
	
	public Block reserve( int address )
	{
		return reserve( address, 1 );
	}
	
	public Block reserve( int address, int size )
	{
		Block b;
		
		if( array[ address ] != null ) {
			b	= array[ address ];
		} else {
			b	= findNext( address );
		}
		
		if( (b != null) && b.used && (address + size > b.start) ) {
			throw new IllegalStateException( "The block at (" + address + ", " + size + ") is already in use and cannot be reserved." );
		}
		
		if( b.start == address ) {
			return reserve( address, size, b, null );
		}
	
		b = findPrevious( address );
		if( (b != null) && b.used && (b.start + b.size > address) ) {
			throw new IllegalStateException( "The block at (" + address + ", " + size + ") is already in use and cannot be reserved." );
		}
		
		return reserve( address, size, null, b );
	}

	public void free( int address )
	{
		Block b, prev, next, temp;
		
		b = array[ address ];
		if( (b != null) && b.used ) {
			b.used	= false;
			addToFreed( b );
		
			prev = findPrevious( address );
			if( (prev != null) && !prev.used ) {
				temp = prev.join( b );
				if( temp != null ) {	// if block is the last one, reduce the top
					if( b.start == top ) {
						top = temp.start;
					}
					array[ temp.start ] = temp;
					array[ b.start ]	= null;
					removeFromFreed( prev );
					removeFromFreed( b );
					if( top > temp.start ) {
						addToFreed( temp );
					}
					b = temp;
				}
			}
			next = findNext( b.start );
			if( (next != null) && !next.used ) {
				temp = next.join( b );
				if( temp != null ) {	// if next is the last one, reduce the top
					if( next.start == top ) {
						top = temp.start;
					}
					array[ temp.start ] = temp;
					array[ next.start ]	= null;
					removeFromFreed( next );
					removeFromFreed( b );
				}
				if( top > temp.start ) {
					addToFreed( temp );
				}
			}
		}
	}
	
	public List getAllocatedBlocks()
	{
		final List	result = new ArrayList();
		Block		b;
		
		for( int i = 0; i < array.length; i++ ) {
			b = array[ i ];
			if( (b != null) && b.used ) {
				result.add( b );
			}
		}
		return result;
	}
	
	private Block findAvailable( int n )
	{
		Set			set;
		Map.Entry	entry;
		
		set = (Set) freed.get( new Integer( n ));
	
		if( set != null ) return (Block) set.iterator().next();
		
		for( Iterator iter = freed.entrySet().iterator(); iter.hasNext(); ) {
			entry = (Map.Entry) iter.next();
			if( ((Integer) entry.getKey()).intValue() >= n ) {
				set = (Set) entry.getValue();
				if( set != null ) return (Block) set.iterator().next();
			}
		}
		
		if( (top + n > size) || array[ top ].used ) return null;
		
		return array[ top ];
	}
	
	private void addToFreed( Block b )
	{
		final Object	key = new Integer( b.size );
		Set				set;
	
		set = (Set) freed.get( key );
		if( set == null ) {
			set = new HashSet();
			freed.put( key, set );
		}
		set.add( b );
	}
	
	private void removeFromFreed( Block b )
	{
		final Object	key = new Integer( b.size );
		final Set		set	= (Set) freed.get( key );
		
		if( set != null ) {
			set.remove( b );
			if( set.isEmpty() ) {
				freed.remove( key );
			}
		}
	}

	private Block findPrevious( int address )
	{
		for( int i = address - 1; i >= pos; i-- ) {
			if( array[ i ] != null ) return array[ i ];
		}
		return null;
	}

	private Block findNext( int address )
	{
		final Block temp = array[ address ];
		
		if( temp != null ) return array[ temp.start + temp.size ];

		for( int i = address + 1; i <= top; i++ ) {
			if( array[ i ] != null ) return array[ i ];
		}
		
		return null;
	}

	private Block reserve( int address, int size, Block availBlock, Block prevBlock )
	{
		if( availBlock == null ) {
			availBlock = prevBlock == null ? findPrevious( address ) : prevBlock;
		}
		if( availBlock.start < address ) {
			availBlock = split( availBlock, address - availBlock.start, false )[ 1 ];
		}
		return split( availBlock, size, true )[ 0 ];
	}

	private Block[] split( Block availBlock, int n, boolean used )
	{
		final Block[]	result		= availBlock.split( n );
		final Block		newB		= result[ 0 ];
		final Block		leftOver	= result[ 1 ];

		newB.used	= used;
		removeFromFreed( availBlock );
		if( !used ) addToFreed( newB );
		array[ newB.start ] = newB;
		if( leftOver != null ) {
			array[ leftOver.start ] = leftOver;
			if( top > leftOver.start ) {
				addToFreed( leftOver );
			} else {
				top = leftOver.start;
			}
		}
		return result;
	}
	
	public void debug()
	{
		Map.Entry entry;
	
		System.err.println( this.getClass().getName() + ":\n\nArray:" );
		for( int i = 0; i < array.length; i++ ) {
			if( array[ i ] != null ) {
				System.err.println( String.valueOf( i ) + ": " + array[ i ]);
			}
			System.err.println( "\nFree sets:" );
			for( Iterator iter = freed.entrySet().iterator(); iter.hasNext(); ) {
				entry = (Map.Entry) iter.next();
				System.err.print( entry.getKey().toString() + ": [ " );
				for( Iterator iter2 = ((Set) entry.getValue()).iterator(); iter2.hasNext(); ) {
					System.err.print( iter2.next().toString() + ", " );
				}
				System.err.println( "]" );
			}
		}
	}

	public static class Factory
	implements BlockAllocator.Factory
	{
		public BlockAllocator create( int size )
		{
			return new ContiguousBlockAllocator( size );
		}
		
		public BlockAllocator create( int size, int pos )
		{
			return new ContiguousBlockAllocator( size, pos );
		}
	}

	private static class Block
	implements BlockAllocator.Block
	{
		protected final int	start;
		protected final int	size;
		protected boolean	used	= false;  // assume free; owner must say otherwise

		public Block( int start, int size )
		{
			this.start	= start;
			this.size	= size;
		}
		
		public int getAddress()
		{
			return start;
		}
		
		public int getSize()
		{
			return size;
		}

		public boolean adjoins( Block b )
		{
			return( ((start < b.start) && (start + size >= b.start)) || ((start > b.start) && (b.start + b.size >= start)) );
		}
		
		public Block join( Block b )
		{
			final int newStart;
			final int newSize;
			
			if( adjoins( b )) {
				newStart	= Math.min( start, b.start );
				newSize		= Math.max( start + size, b.start + b.size ) - newStart;
				
				return new Block( newStart, newSize );
			} else {
				return null;
			}
		}
		
		public Block[] split( int len )
		{
			final Block[] result = new Block[ 2 ];
		
			if( len < size ) {
				result[ 0 ] = new Block( start, len );
				result[ 1 ] = new Block( start + len, size - len );
			} else if( len == size ) {
				result[ 0 ] = this;
			}
			
			return result;
		}
		
		public String toString()
		{
			return( "Block( start = " + start + "; size = " + size + "; used = " + used + " )" );
		}
	}
}