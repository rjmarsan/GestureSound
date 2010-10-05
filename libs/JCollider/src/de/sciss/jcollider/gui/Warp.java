/*
 *  Warp.java
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
 *		29-Jul-06   created
 */
 
package de.sciss.jcollider.gui;

/**
 *	Helper classes for warping a control value.
 *	This is a quite direct translation from the SClang classes.
 *
 *	@version	0.28, 29-Jul-06
 *	@author		Hanns Holger Rutz
 *
 *	@see		ControlSpec
 */
public abstract class Warp
{
	public static Warp lin	= new LinearWarp();
	public static Warp exp	= new ExponentialWarp();
	public static Warp sin	= new SineWarp();
	public static Warp cos	= new CosineWarp();
//	public static Warp amp	= new FaderWarp();
//	public static Warp db	= new DbFaderWarp();

	public static Warp curve( double curve )
	{
		if( curve == 0.0 ) {
			return lin;
		} else {
			return new CurveWarp( curve );
		}
	}

	public abstract double map( double value, ControlSpec spec );
	public abstract double unmap( double value, ControlSpec spec );
	
	private static class LinearWarp
	extends Warp
	{
		protected LinearWarp() { /* empty */ }
		
		public double map( double value, ControlSpec spec )
		{
			return( value * spec.getRange() + spec.getMinVal() );
		}

		public double unmap( double value, ControlSpec spec )
		{
			final double range = spec.getRange();
			
			if( range == 0.0 ) {
				return 0.0;
			} else {
				return(( value - spec.getMinVal() ) / range );
			}
		}
	}

	// minval and maxval must both be non zero and have the same sign.
	private static class ExponentialWarp
	extends Warp
	{
		protected ExponentialWarp() { /* empty */ }

		public double map( double value, ControlSpec spec )
		{
			return( Math.pow( spec.getRatio(), value ) * spec.getMinVal() );
		}
		
		public double unmap( double value, ControlSpec spec )
		{
			return( Math.log( value / spec.getMinVal() ) / Math.log( spec.getRatio() ));
		}
	}

	private static class SineWarp
	extends Warp
	{
		private static final double PIH = Math.PI * 0.5;
	
		protected SineWarp() { /* empty */ }

		public double map( double value, ControlSpec spec )
		{
			return( Math.sin( PIH * value ) * spec.getRange() + spec.getMinVal() );
		}

		public double unmap( double value, ControlSpec spec )
		{
			final double range = spec.getRange();
			
			if( range == 0.0 ) {
				return 0.0;
			} else {
				return( Math.asin(( value - spec.getMinVal() ) / range ) / PIH );
			}
		}
	}

	private static class CosineWarp
	extends Warp
	{
		private static final double PIH = Math.PI * 0.5;
	
		protected CosineWarp() { /* empty */ }

		public double map( double value, ControlSpec spec )
		{
			return( (0.5 - Math.cos( Math.PI * value ) * 0.5) * spec.getRange() + spec.getMinVal() );
		}

		public double unmap( double value, ControlSpec spec )
		{
			final double range = spec.getRange();
			
			if( range == 0.0 ) {
				return 0.0;
			} else {
				return( Math.acos( 1.0 - (( value - spec.getMinVal() ) / range ) * 2.0 ) / PIH );
			}
		}
	}

	private static class CurveWarp
	extends Warp
	{
		private final double curve;
		private final double grow;
		private final double oneByOneMGrow;
	
		protected CurveWarp( double curve )
		{
			this.curve		= curve;
			grow			= Math.exp( curve );
			oneByOneMGrow	= 1.0 / (1.0 - grow);
		}
		
		public double map( double value, ControlSpec spec )
		{
			final double a = spec.getRange() * oneByOneMGrow;
			final double b = spec.getMinVal() + a;

			return( b - a * Math.pow( grow, value ));
		}
		
		public double unmap( double value, ControlSpec spec )
		{
			final double a = spec.getRange() * oneByOneMGrow;
			final double b = spec.getMinVal() + a;

			return( Math.log(( b - value ) / a ) / curve );
		}
	}
}