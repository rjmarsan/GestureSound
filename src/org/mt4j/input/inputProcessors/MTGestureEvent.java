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
package org.mt4j.input.inputProcessors;

import org.mt4j.components.interfaces.IMTComponent3D;
import org.mt4j.input.inputData.MTInputEvent;



/**
 * The Class GestureEvent.
 * @author Christopher Ruff
 */
public abstract class MTGestureEvent extends MTInputEvent {
	
	/** The id. */
	private int id;
	
	/** The target component. */
//	private IMTComponent3D targetComponent;
//	private InputMotion[] motions;
	
	/** The Constant GESTURE_DETECTED. */
	public static final int GESTURE_DETECTED = 0;
	
	/** The Constant GESTURE_UPDATED. */
	public static final int GESTURE_UPDATED = 1;
	
	/** The Constant GESTURE_ENDED. */
	public static final int GESTURE_ENDED 	= 2;
	
	/**
	 * Instantiates a new gesture event.
	 * 
	 * @param source the source
	 * @param id the id
	 * @param targetComponent the target component
	 */
	public MTGestureEvent(IInputProcessor source, int id, IMTComponent3D targetComponent) {
		super(source, targetComponent);
		this.id = id;
//		this.targetComponent = targetComponent;
	}
	
	
	

	@Override
	public IInputProcessor getSource() {
		return (IInputProcessor)super.getSource();
	}




	/**
	 * Gets the id.
	 * <br>Can be a value of:
	 * <ul>
	 * <li>GESTURE_DETECTED
	 * <li>GESTURE_UPDATED
	 * <li>GESTURE_ENDED
	 * </ul>
	 * 
	 * @return the id
	 */
	public int getId() {
		return id;
	}

//	/**
//	 * Gets the target component.
//	 * 
//	 * @return the target component
//	 */
//	public IMTComponent3D getTargetComponent() {
//		return targetComponent;
//	}
//	
//	/**
//	 * Sets the target component.
//	 * 
//	 * @param targetComponent the new target component
//	 */
//	public void setTargetComponent(IMTComponent3D targetComponent) {
//		this.targetComponent = targetComponent;
//	}

	/**
	 * Sets the id.
	 * 
	 * @param id the new id
	 */
	public void setId(int id) {
		this.id = id;
	}


	
	
	
//	public InputMotion[] getMotions() {
//		return this.motions;
//	}
}
