/*
 *  ServerEvent.java
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

import de.sciss.app.BasicEvent;

/**
 *	These kind of events get delivered by a 
 *	server represenation to inform listeners about
 *	server status changes
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.33, 19-Mar-08
 */
public class ServerEvent
extends BasicEvent
{
// --- ID values ---
	/**
	 *  returned by getID() : the server started running
	 */
	public static final int RUNNING		= 0;

	/**
	 *  returned by getID() : the server stopped running
	 */
	public static final int STOPPED		= 1;

	/**
	 *  returned by getID() : the server status has been updated
	 */
	public static final int COUNTS		= 2;

	private final Server	server;

	/**
	 *	@param	source	who fired the event
	 *	@param	ID		the type of status change, e.g. <code>RUNNING</code>
	 *	@param	when	timestamp of the event (e.g. <code>System.currentTimeMillis()</code>)
	 *	@param	server	the representation of the server whose status changed
	 */
	protected ServerEvent( Object source, int ID, long when, Server server )
	{
		super( source, ID, when );
	
		this.server		= server;
	}
	
	/**
	 *	@return	the representation of the server whose status changed
	 */
	public Server getServer()
	{
		return server;
	}

	/**
	 *	Used by the <code>EventManager</code> to
	 *	fuse successive events together when they queue.
	 *	Do not call this method.
	 */
	public boolean incorporate( BasicEvent oldEvent )
	{
		if( (oldEvent instanceof ServerEvent) &&
			(this.getSource() == oldEvent.getSource()) &&
			(this.getID() == oldEvent.getID()) ) {
			
			// XXX beware, when the actionID and actionObj
			// are used, we have to deal with them here
			
			return true;

		} else return false;
	}
}