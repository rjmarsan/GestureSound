/*
 *  LagControl.java
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
 *		15-Oct-05	created
 */

package de.sciss.jcollider;

/**
 *	Class for the LagControl UGen.
 *	This class is recognized by SynthDef
 *	in the building process.
 *	<p>
 *	For details, refer to the <code>Control</code> class.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.25, 15-Oct-05
 *
 *	@see		Control
 */
public class LagControl
extends Control
{
	private LagControl( String[] names, Object rate, float[] values, Constant[] lags )
	{
		super( "LagControl", rate, names.length, lags, 0 );
	
		if( (names.length != values.length) || (values.length != lags.length) ) {
			throw new IllegalArgumentException( "LagControl: # of names / values / lags must be equal" );
		}
	
		for( int i = 0; i < names.length; i++ ) {
			descs[ i ] = new ControlDesc( names[ i ], rate, values[ i ], lags[ i ].getValue() );
		}
	}
	
	private static Constant[] createLagInputs( float[] lags )
	{
		final Constant[] ins = new Constant[ lags.length ];
		
		for( int i = 0; i < ins.length; i++ ) {
			ins[ i ] = new Constant( lags[ i ]);
		}
		
		return ins;
	}
	
//	public static GraphElem kr( String name )
//	{
//		return kr( name, 0.0f );
//	}

	public static Control kr( String name, float defaultValue, float lag )
	{
		return new LagControl( new String[] { name }, kControlRate,
							   new float[] { defaultValue }, new Constant[] { new Constant( lag )});
	}

	public static Control kr( String[] names, float[] values, float[] lags )
	{
		return new LagControl( names, kControlRate, values, createLagInputs( lags ));
	}
}
