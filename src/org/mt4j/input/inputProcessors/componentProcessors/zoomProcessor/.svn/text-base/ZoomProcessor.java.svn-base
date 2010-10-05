/***********************************************************************
 * mt4j Copyright (c) 2008 - 2009 C.Ruff, Fraunhofer-Gesellschaft All rights reserved.
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
package org.mt4j.input.inputProcessors.componentProcessors.zoomProcessor;

import java.util.ArrayList;
import java.util.List;

import org.mt4j.components.interfaces.IMTComponent3D;
import org.mt4j.input.inputData.InputCursor;
import org.mt4j.input.inputData.MTFingerInputEvt;
import org.mt4j.input.inputProcessors.IInputProcessor;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.AbstractComponentProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.AbstractCursorProcessor;
import org.mt4j.util.math.Vector3D;

import processing.core.PApplet;

/**
 * The Class ZoomProcessor.
 * Multitouch background zoom gesture. This will change the scene's CAMERA.
 * Fires ZoomEvent gesture events.
 * <br><strong>NOTE:</strong> Should be only used in combination with a MTCanvas component. 
 * @author Christopher Ruff
 */
public class ZoomProcessor extends AbstractCursorProcessor {
	
	/** The zoom detect radius. */
	private float zoomDetectRadius;
	
	/** The old distance. */
	private float oldDistance;
	
	/** The applet. */
	private PApplet applet;

	/** The un used motions. */
	private List<InputCursor> unUsedMotions;
	
	/** The locked motions. */
	private List<InputCursor> lockedMotions;
	
	/**
	 * Instantiates a new zoom processor.
	 * 
	 * @param graphicsContext the graphics context
	 */
	public ZoomProcessor(PApplet graphicsContext){
		this(graphicsContext, graphicsContext.width/2);
	}
	
	/**
	 * Instantiates a new zoom processor.
	 * 
	 * @param graphicsContext the graphics context
	 * @param zoomDetectRadius the zoom detect radius
	 */
	public ZoomProcessor(PApplet graphicsContext, float zoomDetectRadius){
		this.applet = graphicsContext;
		this.unUsedMotions 	= new ArrayList<InputCursor>();
		this.lockedMotions 	= new ArrayList<InputCursor>();
		this.zoomDetectRadius = zoomDetectRadius;
		this.setLockPriority(2);
	}

	
	@Override
	public void cursorStarted(InputCursor m, MTFingerInputEvt positionEvent) {
		IMTComponent3D comp = positionEvent.getTargetComponent();
		if (lockedMotions.size() >= 2){ //scale with 2 fingers already in progress
			unUsedMotions.add(m);
			logger.debug(this.getName() + " has already enough motions for this gesture - adding to unused ID:" + m.getId());
		}else{ //no scale in progress yet
			if (unUsedMotions.size() == 1){
				logger.debug(this.getName() + " has already has 1 unused motion - we can try start gesture! used with ID:" + unUsedMotions.get(0).getId() + " and new motion ID:" + m.getId());
				InputCursor otherMotion = unUsedMotions.get(0);
				
				//See if we can obtain a lock on both motions
				if (this.canLock(otherMotion, m)){
					float newDistance = Vector3D.distance(
							new Vector3D(otherMotion.getCurrentEvent().getPosX(), otherMotion.getCurrentEvent().getPosY(),0),
							new Vector3D(m.getCurrentEvent().getPosX(), m.getCurrentEvent().getPosY(),0));
					if (newDistance < zoomDetectRadius) {
						this.oldDistance = newDistance;
						this.getLock(otherMotion, m);
						lockedMotions.add(otherMotion);
						lockedMotions.add(m);
						unUsedMotions.remove(otherMotion);
						logger.debug(this.getName() + " we could lock both motions! And fingers in zoom distance!");
						this.fireGestureEvent(new ZoomEvent(this, MTGestureEvent.GESTURE_DETECTED, comp, m, otherMotion, 0f, comp.getViewingCamera() ));
					}else{
						logger.debug(this.getName() + " Motions not close enough to start gesture. Distance: " + newDistance);
					}
				}else{
					logger.debug(this.getName() + " we could NOT lock both motions!");
					unUsedMotions.add(m);	
				}
			}else{
				logger.debug(this.getName() + " we didnt have a unused motion previously to start gesture now");
				unUsedMotions.add(m);
			}
		}
	}

	@Override
	public void cursorUpdated(InputCursor m, MTFingerInputEvt positionEvent) {
		IMTComponent3D comp = positionEvent.getTargetComponent();
		if (lockedMotions.size() == 2 && lockedMotions.contains(m)){
			InputCursor firstMotion = lockedMotions.get(0);
			InputCursor secondMotion = lockedMotions.get(1);
			float fingerDistance = Vector3D.distance(
					new Vector3D(firstMotion.getCurrentEvent().getPosX(), firstMotion.getCurrentEvent().getPosY(), 0),
					new Vector3D(secondMotion.getCurrentEvent().getPosX(), secondMotion.getCurrentEvent().getPosY(), 0));
			float camZoomAmount = fingerDistance - oldDistance;
			oldDistance = fingerDistance;
			if (m.equals(firstMotion)){
				this.fireGestureEvent(new ZoomEvent(this, MTGestureEvent.GESTURE_UPDATED, comp, firstMotion, secondMotion, camZoomAmount, comp.getViewingCamera()));
			}else{
				this.fireGestureEvent(new ZoomEvent(this, MTGestureEvent.GESTURE_UPDATED, comp, firstMotion, secondMotion, camZoomAmount, comp.getViewingCamera()));
			}
		}
	}

	@Override
	public void cursorEnded(InputCursor m,	MTFingerInputEvt positionEvent) {
		IMTComponent3D comp = positionEvent.getTargetComponent();
		logger.debug(this.getName() + " INPUT_ENDED RECIEVED - MOTION: " + m.getId());
		
		if (lockedMotions.size() == 2 && lockedMotions.contains(m)){
			InputCursor leftOverMotion = (lockedMotions.get(0).equals(m))? lockedMotions.get(1) : lockedMotions.get(0);
			
			lockedMotions.remove(m);
			if (unUsedMotions.size() > 0){ //Check if there are other motions we could use for scaling if one was removed
				InputCursor futureMotion = unUsedMotions.get(0);
				if (this.canLock(futureMotion)){ //check if we have priority to lock another motion and use it
					float newDistance = Vector3D.distance(
							new Vector3D(leftOverMotion.getCurrentEvent().getPosX(), leftOverMotion.getCurrentEvent().getPosY(),0),
							new Vector3D(futureMotion.getCurrentEvent().getPosX(), futureMotion.getCurrentEvent().getPosY(),0));
					if (newDistance < zoomDetectRadius) {//Check if other motion is in distance 
						this.oldDistance = newDistance;
						this.getLock(futureMotion);
						unUsedMotions.remove(futureMotion);
						lockedMotions.add(futureMotion);
						logger.debug(this.getName() + " we could lock another motion! (ID:" + futureMotion.getId() +")");
						logger.debug(this.getName() + " continue with different motions (ID: " + futureMotion.getId() + ")" + " " + "(ID: " + leftOverMotion.getId() + ")");
						//TODO fire start evt?
					}else{
						this.endGesture(m, leftOverMotion, comp);
					}
				}else{ //we dont have permission to use other motion  - End scale
					this.endGesture(m, leftOverMotion, comp);
				}
			}else{ //no more unused motions on comp - End scale
				this.endGesture(m, leftOverMotion, comp);
			}
			this.unLock(m); //FIXME TEST
		}else{ //motion was not a scaling involved motion
			if (unUsedMotions.contains(m)){
				unUsedMotions.remove(m);
			}
		}
	}
	
	
	/**
	 * End gesture.
	 * 
	 * @param inputEndedMotion the input ended motion
	 * @param leftOverMotion the left over motion
	 * @param comp the comp
	 */
	private void endGesture(InputCursor inputEndedMotion, InputCursor leftOverMotion, IMTComponent3D comp){
		lockedMotions.clear();
		unUsedMotions.add(leftOverMotion);
		this.unLock(leftOverMotion);
		this.fireGestureEvent(new ZoomEvent(this, MTGestureEvent.GESTURE_ENDED, comp, inputEndedMotion, leftOverMotion, 0f, comp.getViewingCamera()));
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.mt4j.input.inputAnalyzers.IInputAnalyzer#motionLocked(org.mt4j.input.inputData.InputMotion, org.mt4j.input.inputAnalyzers.IInputAnalyzer)
	 */
	@Override
	public void cursorLocked(InputCursor m, IInputProcessor lockingAnalyzer) {
		if (lockingAnalyzer instanceof AbstractComponentProcessor){
			logger.debug(this.getName() + " Recieved MOTION LOCKED by (" + ((AbstractComponentProcessor)lockingAnalyzer).getName()  + ") - motion ID: " + m.getId());
		}else{
			logger.debug(this.getName() + " Recieved MOTION LOCKED by higher priority signal - motion ID: " + m.getId());
		}
		
		if (lockedMotions.contains(m)){ //motions was used here! -> we have to stop the gesture
			//put all used motions in the unused motion list and clear the usedmotionlist
			unUsedMotions.addAll(lockedMotions); 
			lockedMotions.clear();
			//TODO fire ended evt?
			logger.debug(this.getName() + " motion:" + m.getId() + " MOTION LOCKED. Was an active motion in this gesture - we therefor have to stop this gesture!");
		}
	}

	
	
	/* (non-Javadoc)
	 * @see org.mt4j.input.inputAnalyzers.IInputAnalyzer#motionUnlocked(org.mt4j.input.inputData.InputMotion)
	 */
	@Override
	public void cursorUnlocked(InputCursor m) {
		logger.debug(this.getName() + " Recieved UNLOCKED signal for motion ID: " + m.getId());
		
		if (lockedMotions.size() >= 2){ //we dont need the unlocked motion, gesture still in progress
			return;
		}
		
		if (unUsedMotions.contains(m)){ //should always be true here!?
			if (unUsedMotions.size() >= 2){ //we can try to resume the gesture
				InputCursor firstMotion = unUsedMotions.get(0);
				InputCursor secondMotion = unUsedMotions.get(1);

				//See if we can obtain a lock on both motions
				if (this.canLock(firstMotion, secondMotion)){
					float newDistance = Vector3D.distance(
							new Vector3D(firstMotion.getCurrentEvent().getPosX(), firstMotion.getCurrentEvent().getPosY(),0),
							new Vector3D(secondMotion.getCurrentEvent().getPosX(), secondMotion.getCurrentEvent().getPosY(),0));
					if (newDistance < zoomDetectRadius) {//Check if other motion is in distance 
						this.oldDistance = newDistance;
						this.getLock(firstMotion, secondMotion);

						unUsedMotions.remove(firstMotion);
						unUsedMotions.remove(secondMotion);
						lockedMotions.add(firstMotion);
						lockedMotions.add(secondMotion);
						logger.debug(this.getName() + " we could lock motions: " + firstMotion.getId() +", " + secondMotion.getId());
						logger.debug(this.getName() + " continue with different motions (ID: " + firstMotion.getId() + ")" + " " + "(ID: " + secondMotion.getId() + ")");
						//TODO fire started evt?
					}else{
						logger.debug(this.getName() + " distance was too great between motions: " + firstMotion.getId() +", " + secondMotion.getId() + " distance: " + newDistance);
					}
				}else{
					logger.debug(this.getName() + " we could NOT lock motions: " + firstMotion.getId() +", " + secondMotion.getId());
				}
			}
		}else{
			logger.error(this.getName() + "hmmm - investigate why is motion not in unusedList?");
		}
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.mt4j.input.inputAnalyzers.componentAnalyzers.AbstractComponentInputAnalyzer#getName()
	 */
	@Override
	public String getName() {
		return "Zoom Processor";
	}


	

	

}
