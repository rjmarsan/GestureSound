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
package org.mt4j.input.gestureAction;

import org.mt4j.components.MTComponent;
import org.mt4j.components.interfaces.IMTComponent3D;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.scaleProcessor.ScaleEvent;



/**
 * The Class DefaultScaleAction.
 * 
 * @author Christopher Ruff
 */
public class DefaultScaleAction implements IGestureEventListener {
	
	/** The target. */
	private IMTComponent3D target;
	
	/** The use custom target. */
	private boolean useCustomTarget;
	
	/**
	 * Instantiates a new default scale action.
	 */
	public DefaultScaleAction(){
		this.useCustomTarget = false;
	}
	
	/**
	 * Instantiates a new default scale action.
	 * 
	 * @param customTarget the custom target
	 */
	public DefaultScaleAction(IMTComponent3D customTarget){
		this.target = customTarget;
		this.useCustomTarget = true;
	}

	/* (non-Javadoc)
	 * @see com.jMT.input.gestureAction.IGestureAction#processGesture(com.jMT.input.inputAnalyzers.GestureEvent)
	 */
	public boolean processGestureEvent(MTGestureEvent g) {
		if (g instanceof ScaleEvent){
			ScaleEvent scaleEvent = (ScaleEvent)g;
			
			if (!useCustomTarget)
				target = scaleEvent.getTargetComponent(); 
			
			switch (scaleEvent.getId()) {
			case MTGestureEvent.GESTURE_DETECTED:
				if (target instanceof MTComponent){
					((MTComponent)target).sendToFront();
					/*
					Animation[] animations = AnimationManager.getInstance().getAnimationsForTarget(target);
					for (int i = 0; i < animations.length; i++) {
						Animation animation = animations[i];
						animation.stop();
					}
					*/
				}
				break;
			case MTGestureEvent.GESTURE_UPDATED:
				target.scaleGlobal(
						scaleEvent.getScaleFactorX(), 
						scaleEvent.getScaleFactorY(), 
						scaleEvent.getScaleFactorZ(), 
						scaleEvent.getScalingPoint());
//				target.scaleGlobal(
//						scaleEvent.getScaleFactorX(), 
//						scaleEvent.getScaleFactorY(), 
//						scaleEvent.getScaleFactorX(), 
//						scaleEvent.getScalingPoint());
				break;
			case MTGestureEvent.GESTURE_ENDED:
				break;
			default:
				break;
			}
		}
		return false;
	}

}
