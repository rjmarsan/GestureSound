/*
 *  PowerOfTwoAllocator.java
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
 *		24-Jul-06	implements BlockAllocator
 */

package de.sciss.jcollider;

import java.util.ArrayList;
import java.util.List;

/**
 *	Quite a 1:1 translation from SClang, this
 *	is used as the default bus allocator by the server.
 *	
 *	@todo	unlike the node allocator, this cannot
 *			handle different client IDs. have to find
 *			out how different clients can peacefully
 *			coexist on the same server.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.32, 25-Feb-08
 */
public class PowerOfTwoAllocator
implements BlockAllocator
{
	private final int	size;
	private int			pos;
	
	private final Block[]	allocatedBlocks;
	private final Block[]	freeBlocks;

	public PowerOfTwoAllocator( int size )
	{
		this( size, 0 );
	}

	public PowerOfTwoAllocator( int size, int pos )
	{
		this.size		= size;
		this.pos		= pos;
		allocatedBlocks	= new Block[ size ];
		freeBlocks		= new Block[ 32 ];
	}

	public int alloc( final int n )
	{
		final int			result;
		int					np2, sizeClass;
		Block				node;
		
		for( np2 = 1, sizeClass = 0; np2 < n; np2 <<= 1, sizeClass++ ) ;	// next power of two
		
		node	= freeBlocks[ sizeClass ];
		if( node != null ) {
			freeBlocks[ sizeClass ]	= node.next;
			result					= node.address;

		} else if( pos + np2 <= size ) {
			allocatedBlocks[ pos ]	= new Block( pos, np2, sizeClass );
			result					= pos;
			pos					   += np2;
		
		} else {
			result = -1;
		}
		
		return result;
	}

	public void free( int address )
	{
		final Block node = allocatedBlocks[ address ];
		if( node != null ) {
			node.next						= freeBlocks[ node.sizeClass ];
			freeBlocks[ node.sizeClass ]	= node;
		}
	}

	public List getAllocatedBlocks()
	{
		final List result = new ArrayList();
		
		for( int i = 0; i < allocatedBlocks.length; i++ ) {
			if( allocatedBlocks[ i ] != null ) result.add( allocatedBlocks[ i ]);
		}
		
		return result;
	}
	
	public static class Factory
	implements BlockAllocator.Factory
	{
		public BlockAllocator create( int size )
		{
			return new PowerOfTwoAllocator( size );
		}
		
		public BlockAllocator create( int size, int pos )
		{
			return new PowerOfTwoAllocator( size, pos );
		}
	}

	private static class Block
	implements BlockAllocator.Block
	{
		protected final int	address;
		private final int	size;
		protected final int	sizeClass;

		protected Block	next	= null;
		
		protected Block( int address, int size, int sizeClass )
		{
			this.address	= address;
			this.size		= size;
			this.sizeClass	= sizeClass;
		}

		public int getAddress()
		{
			return address;
		}
		
		public int getSize()
		{
			return size;
		}
	}
}