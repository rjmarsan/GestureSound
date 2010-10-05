/*
 *  Constant.java
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
 *		30-Jun-05	created
 */

package de.sciss.jcollider;

/**
 *	A <code>Constant</code> is a wrapper
 *	for a constant in a UGen graph. It
 *	implements the <code>UGenInput</code>
 *	interface and therefore also the
 *	<code>GraphElem</code> interface which
 *	describes heterogeneous elements of
 *	a UGen graph.
 *
 *	@see	UGen#ir( float )
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.31, 08-Oct-07
 */
public class Constant
implements UGenInput, Constants
{
	private final float value;

	public Constant( float value )
	{
		this.value	= value;
	}
	
	public float getValue()
	{
		return value;
	}

// --------- UGenInput interface ---------

	public Object getRate()
	{
		return kScalarRate;
	}

	public String dumpName()
	{
		return( String.valueOf( value ));
	}

	public UGenInput[] asUGenInputs()
	{
		return new UGenInput[] { this };
	}
	
	public int getNumOutputs()
	{
		return 1;
	}
	
	public GraphElem getOutput( int idx )
	{
		return this;
	}
}
