/*
 *  ControlDesc.java
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
 *		28-Jun-05	created
 */

package de.sciss.jcollider;

import java.io.PrintStream;

/**
 *	A descriptor class for a control
 *	UGen, similar to SClang's ControlName class.
 *	Note that the <code>lag</code> parameter
 *	is currently unused.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.31, 08-Oct-07
 */
public class ControlDesc
{
	private final String	name;
	private final Object	rate;
	private final float		defaultValue;
	private final float		lag;

//	public ControlDesc( String name, int index, Object rate, float defaultValue, float lag )
	public ControlDesc( String name, Object rate, float defaultValue, float lag )
	{
		this.name			= name;
//		this.index			= index;
		this.rate			= rate;
		this.defaultValue	= defaultValue;
		this.lag			= lag;
	}

//	public ControlDesc( String name, int index, Object rate, float defaultValue )
	public ControlDesc( String name, Object rate, float defaultValue )
	{
//		this( name, index, rate, defaultValue, 0.0f );
		this( name, rate, defaultValue, 0.0f );
	}

	public Object getRate()
	{
		return rate;
	}
	
	public String getName()
	{
		return name;
	}

	public float getDefaultValue()
	{
		return defaultValue;
	}
	
//	public int getIndex()
//	{
//		return index;
//	}
	
	public float getLag()
	{
		return lag;
	}
	
	public void printOn( PrintStream out )
	{
//		out.print( "ControlDesc  P " + index );
//		out.print( "idx " + index );
		out.print( "\"" + (name == null ? "??? " : name) + "\" @ " + rate );
		out.println( ", default = " + defaultValue );
	}	
}
