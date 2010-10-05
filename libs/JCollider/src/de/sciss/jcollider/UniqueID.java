/*
 *  UniqueID.java
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
 *	Utility class for
 *	outputting unique incremental
 *	numbers.
 *
 *	@todo	this should be simply put in
 *			the JCollider class
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.2, 11-Sep-05 (some countries are still at war)
 *
 *	@synchronization	this class is thread safe
 */
public abstract class UniqueID
{
	private static int id	= 1000;
	
	/**
	 *	Returns the next unique ID
	 */
	public static synchronized int next()
	{
		return id++;
	}
}
