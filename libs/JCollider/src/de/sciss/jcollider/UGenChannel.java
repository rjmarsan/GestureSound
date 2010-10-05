/*
 *  UGenChannel.java
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
 *		27-Aug-05	created
 */

package de.sciss.jcollider;

/**
 *	Represents one output channel of a (potentially
 *	multi-ouput) UGen.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.31, 08-Oct-07
 */
public class UGenChannel
implements UGenInput
{
	private final UGen	ugen;
	private final int	channel;

	/**
	 *	You do not directly create <code>UGenChannel</code>s
	 *	but retrieve them from a <code>UGen</code> by
	 *	calling its <code>getChannel</code> method
	 *
	 *	@see	UGen#getChannel( int )
	 */
	protected UGenChannel( UGen ugen, int channel )
	{
		this.ugen		= ugen;
		this.channel	= channel;
	}
	
	/**
	 *	Returns the <code>UGen</code> whose
	 *	output this object represents
	 */
	public UGen getUGen()
	{
		return ugen;
	}
	
	/**
	 *	Returns the index in the array of
	 *	outputs of the corresponding <code>UGen</code>
	 *	(beginning at zero).
	 */
	public int getChannel()
	{
		return channel;
	}

// -------- UGenInput interface --------

	public Object getRate()
	{
		return ugen.getOutputRate( channel );
	}

	public String dumpName()
	{
		if( ugen.getNumOutputs() <= 1 ) {
			return ugen.dumpName();
		} else {
			return( ugen.dumpName() + "[ch:" + channel + ']' );
		}
	
//		return( ugen.getSynthIndex() + "_" + ugen.getName() +
//			(ugen.getNumOutputs() > 1 ? String.valueOf( channel ) : "") );
	}
	
	/**
	 *	Returns <code>this</code> as an array
	 */
	public UGenInput[] asUGenInputs()
	{
		return new UGenInput[] { this };
	}
	
	/**
	 *	Returns <code>1</code> naturally
	 */
	public int getNumOutputs()
	{
		return 1;
	}
	
	/**
	 *	Returns <code>this</code> naturally
	 */
	public GraphElem getOutput( int idx )
	{
		return this;
	}
}