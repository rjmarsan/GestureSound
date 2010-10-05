/*
 *  NodeIDAllocator.java
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

/**
 *	Quite a 1:1 translation from SClang, this
 *	is used as the default node allocator by the server.
 *	
 *	@todo	allocPerm should be more generally available
 *			from other classes such as Synth and Group,
 *			because it can be vital to sound installation
 *			programming where the non-perm node count
 *			might well end up at the maximum value(?)
 *
 *	@todo	should check if user-max should be one less
 *			because java ints are signed?!
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.31, 08-Oct-07
 */
public class NodeIDAllocator
{
	private final int	user;
	private int			mask;
	private int			temp;
	private int			perm;
	
	private static final int MASK_SHIFT	= 26;
	private static final int USER_MAX	= (1 << (32 - MASK_SHIFT)) - 1;		// := 31
	private static final int TEMP_MIN	= 1000;
	private static final int TEMP_MAX	= (1 << 26) - 1;
	private static final int PERM_MIN	= 2;
	private static final int PERM_MAX	= TEMP_MIN - 1;

	public NodeIDAllocator()
	{
		this( 0 );
	}

	public NodeIDAllocator( int user )
	{
		if( user > USER_MAX ) throw new IllegalArgumentException( "NodeIDAllocator user ID > " + USER_MAX );
		
		this.user	= user;
		reset();
	}
	
	public void reset()
	{
		synchronized( this ) {
			mask = user << MASK_SHIFT;
			temp = TEMP_MIN;
			perm = PERM_MIN;
		}
	}

	public int alloc()
	{
		synchronized( this ) {
			final int result = temp | mask;
			
			if( ++temp > TEMP_MAX ) {
				temp = TEMP_MIN;
				System.err.println( "Warning: NodeIDAllocator exceeded maximum node ID!" );
			}
			return result;
		}
	}
	
	public int allocPerm()
	{
		synchronized( this ) {
			final int result = perm | mask;
			
			if( ++perm > PERM_MAX ) {
				perm = PERM_MAX;
				System.err.println( "Warning: NodeIDAllocator exceeded maximum permanent node ID!\n" +
									"Assigning temporary ID!" );
				return alloc();
			}
			return result;
		}
	}
	
	public static int getUserMax()
	{
		return USER_MAX;
	}
}

