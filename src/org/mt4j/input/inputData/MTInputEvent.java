/***********************************************************************
 * mt4j Copyright (c) 2008 - 2009 Christopher Ruff, Fraunhofer-Gesellschaft All rights reserved.
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
package org.mt4j.input.inputData;

import org.mt4j.components.interfaces.IMTComponent3D;
import org.mt4j.input.MTEvent;

/**
 * The Class MTInputEvent. The base class for all input events.
 * 
 * @author Christopher Ruff
 */
public class MTInputEvent extends MTEvent {
	
	/** The target component. */
	private IMTComponent3D targetComponent;
	

	/**
	 * Instantiates a new mT input event.
	 * 
	 * @param source the source
	 */
	public MTInputEvent(Object source) {
		super(source);
		
	}
	
	/**
	 * Instantiates a new mT input event.
	 * 
	 * @param source the source
	 * @param targetComponent the target component
	 */
	public MTInputEvent(Object source, IMTComponent3D targetComponent) {
		super(source);
		this.targetComponent = targetComponent;
	}

//	/**
//	 * Gets the source.
//	 * 
//	 * @return the source
//	*/
//	@Override
//	public AbstractInputSource getSource() {
//	return (AbstractInputSource)super.getSource();
//	}


	/**
	 * Gets the target of this input event.
	 * <br><strong>NOTE:</strong> Not every event has a target component! To check this
	 * we can call <code>event.hasTarget()</code>.
	 * 
	 * @return the target component
	 */
	public IMTComponent3D getTargetComponent() {
		return targetComponent;
	}

	/**
	 * Sets the target component of this input event. 
	 * <br>NOTE: This is supposed to be called internally by
	 * MT4j and not by users.
	 * 
	 * @param targetComponent the new target component
	 */
	public void setTargetComponent(IMTComponent3D targetComponent) {
		this.targetComponent = targetComponent;
	}
	
	/**
	 * Checks if this input event has a target component.
	 * 
	 * @return true, if successful
	 */
	public boolean hasTarget(){
		return this.targetComponent != null;
	}
	
	/**
	 * This method is invoked right before the event is fired.
	 * This can be used to do event specific actions if needed before firing.
	 * <br>NOTE: this is called internally and should not be called by users!
	 */
	public void preFire(){
	}
	
}
