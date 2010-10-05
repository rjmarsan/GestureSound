/*
 *  GraphElemArray.java
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
 *		10-Sep-05	created
 */

package de.sciss.jcollider;

/**
 *	This interface does the dirty
 *	job of putting all kinds of different
 *	elements under one brand  which
 *	can be used in UGen graph generation.
 *	The <code>GraphElemArray</code> is
 *	used in automatic multichannel expansion.
 *
 *	@see	UGen#array( GraphElem, GraphElem )
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.2, 11-Sep-05 (some countries are still at war)
 */
public class GraphElemArray
implements GraphElem
{
	private final GraphElem[] elements;

	public GraphElemArray( GraphElem[] elements )
	{
		this.elements = elements;
	}
	
	public static GraphElemArray asArray( GraphElem g )
	{
		if( g instanceof GraphElemArray ) {
			return (GraphElemArray) g;
		} else {
			return new GraphElemArray( new GraphElem[] { g });
		}
	}
	
	public GraphElem getElement( int idx )
	{
		return elements[ idx ];
	}

	public int getNumElements()
	{
		return elements.length;
	}

// ----------- GraphElem interface -----------

	public int getNumOutputs()
	{
		return elements.length;
	}
	
	public GraphElem getOutput( int idx )
	{
		return elements[ idx ];
	}
	
	public UGenInput[] asUGenInputs()
	{
		switch( getNumOutputs() ) {
		case 0:
			return new UGenInput[0];
			
		case 1:
			return getOutput( 0 ).asUGenInputs();
			
		default:
			final UGenInput[][] result = new UGenInput[ getNumOutputs() ][];
		
			for( int i = 0; i < getNumOutputs(); i++ ) {
				result[ i ] = getOutput( i ).asUGenInputs();
			}
			return flatten( result );
		}
	}

	public static UGenInput[] flatten( UGenInput[][] ins )
	{
		int numCh = 0;
		for( int i = 0; i < ins.length; i++ ) numCh += ins[ i ].length;
		
		final UGenInput[] flat = new UGenInput[ numCh ];
		
		for( int i = 0, j = 0; i < ins.length; i++ ) {
			System.arraycopy( ins[ i ], 0, flat, j, ins[ i ].length );
			j += ins[ i ].length;
		}
		
		return flat;
	}
}