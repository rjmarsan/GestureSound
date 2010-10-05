/***********************************************************************
 * mt4j Copyright (c) 2008 - 2009, C.Ruff, Fraunhofer-Gesellschaft All rights reserved.
 *  
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 ***********************************************************************/
package org.mt4j.input.inputProcessors.componentProcessors.tapProcessor;

import org.mt4j.components.interfaces.IMTComponent3D;
import org.mt4j.input.inputData.InputCursor;
import org.mt4j.input.inputProcessors.IInputProcessor;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.util.math.Vector3D;


/**
 * The Class ClickEvent.
 * @author Christopher Ruff
 */
public class TapEvent extends MTGestureEvent {
	
	/** The cursor. */
	private InputCursor cursor;
	
	/** The click point. */
	private Vector3D clickPoint;
	
	/** The click id. */
	private int clickID;
	
	/** The Constant BUTTON_DOWN. */
	public static final int BUTTON_DOWN = 3;
	
	/** The Constant BUTTON_UP. */
	public static final int BUTTON_UP	= 4;
	
	/** The Constant BUTTON_CLICKED. */
	public static final int BUTTON_CLICKED = 5;
	

	/**
	 * Instantiates a new click event.
	 * 
	 * @param source the source
	 * @param id the id
	 * @param targetComponent the target component
	 * @param cursor the cursor
	 * @param clickPoint the click point
	 * @param clickID the click id
	 */
	public TapEvent(IInputProcessor source, int id, IMTComponent3D targetComponent, InputCursor cursor, Vector3D clickPoint, int clickID) {
		super(source, id, targetComponent);
		this.cursor = cursor;
		this.clickPoint = clickPoint;
		this.clickID = clickID;
	}

	/**
	 * Gets the click point.
	 * 
	 * @return the click point
	 */
	public Vector3D getLocationOnScreen() {
		return clickPoint;
	}

	/**
	 * Gets the cursor.
	 * 
	 * @return the cursor
	 */
	public InputCursor getCursor() {
		return cursor;
	}

	/**
	 * Gets the click id.
	 * 
	 * @return the click id
	 */
	public int getTapID() {
		return clickID;
	}
	
	/**
	 * Checks if is tapped.
	 * 
	 * @return true, if is tapped
	 */
	public boolean isTapped(){
		return this.getTapID() == BUTTON_CLICKED;
	}
	
	/**
	 * Checks if is tap down.
	 * 
	 * @return true, if is tap down
	 */
	public boolean isTapDown(){
		return this.getTapID() == BUTTON_DOWN;
	}
	
	/**
	 * Checks if is tap canceled.
	 * 
	 * @return true, if is tap canceled
	 */
	public boolean isTapCanceled(){
		return this.getTapID() == BUTTON_UP;
	}
	

}
