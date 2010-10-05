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
package org.mt4j.input.inputProcessors.componentProcessors.panProcessor;

import java.util.ArrayList;
import java.util.List;

import org.mt4j.components.interfaces.IMTComponent3D;
import org.mt4j.input.inputData.InputCursor;
import org.mt4j.input.inputData.MTFingerInputEvt;
import org.mt4j.input.inputProcessors.IInputProcessor;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.AbstractComponentProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.AbstractCursorProcessor;
import org.mt4j.util.math.Tools3D;
import org.mt4j.util.math.ToolsIntersection;
import org.mt4j.util.math.Vector3D;

import processing.core.PApplet;

/**
 * The Class PanProcessorTwoFingers. Multitouch gesture processor for panning the
 * canvas by moving the scene's camera. Should only be registered with MTCanvas components.
 * Fires PanEvent gesture events.
 * <br><strong>NOTE:</strong> Should be only used in combination with a MTCanvas component. 
 * @author Christopher Ruff
 */
public class PanProcessorTwoFingers extends AbstractCursorProcessor {
	
	/** The detect radius. */
	private float detectRadius;
	
	/** The applet. */
	private PApplet applet;
	
	/** The un used cursors. */
	private List<InputCursor> unUsedCursors;
	
	/** The locked cursors. */
	private List<InputCursor> lockedCursors;
	
	/** The point in plane. */
	private Vector3D pointInPlane;
	
	/** The plane normal. */
	private Vector3D planeNormal;
	
	
	/**
	 * Instantiates a new pan processor two fingers.
	 * 
	 * @param app the app
	 */
	public PanProcessorTwoFingers(PApplet app) {
		this(app, app.width/2);
	}
	
	/**
	 * Instantiates a new pan processor two fingers.
	 * 
	 * @param applet the applet
	 * @param panDetectRadius the pan detect radius
	 */
	public PanProcessorTwoFingers(PApplet applet, float panDetectRadius){
		this.applet = applet;
		this.detectRadius = panDetectRadius;
		this.unUsedCursors 	= new ArrayList<InputCursor>();
		this.lockedCursors 	= new ArrayList<InputCursor>();
		this.pointInPlane = new Vector3D(0,0,0); 
		this.planeNormal = new Vector3D(0,0,1); 
		this.setLockPriority(2);
	}
	

	@Override
	public void cursorStarted(InputCursor m, MTFingerInputEvt positionEvent) {
		if (lockedCursors.size() >= 2){ //gesture with 2 fingers already in progress
			unUsedCursors.add(m);
			logger.debug(this.getName() + " has already enough cursors for this gesture - adding to unused ID:" + m.getId());
		}else{ //no gesture in progress yet
			if (unUsedCursors.size() == 1){
				logger.debug(this.getName() + " has already has 1 unused cursor - we can try start gesture! used with ID:" + unUsedCursors.get(0).getId() + " and new cursor ID:" + m.getId());
				InputCursor otherCursor = unUsedCursors.get(0);
				
				//See if we can obtain a lock on both cursors
				if (this.canLock(otherCursor, m)){
					float newDistance = Vector3D.distance(
							new Vector3D(otherCursor.getCurrentEvent().getPosX(), otherCursor.getCurrentEvent().getPosY(),0),
							new Vector3D(m.getCurrentEvent().getPosX(), m.getCurrentEvent().getPosY(),0));
					if (newDistance < detectRadius) {
						this.getLock(otherCursor, m);
						logger.debug(this.getName() + " we could lock both cursors! And fingers in distance! - " + newDistance);
						unUsedCursors.remove(otherCursor);
						lockedCursors.add(otherCursor);
						lockedCursors.add(m);
						this.fireGestureEvent(new PanTwoFingerEvent(this, MTGestureEvent.GESTURE_DETECTED, positionEvent.getTargetComponent(), otherCursor, m, new Vector3D(0,0,0), positionEvent.getTargetComponent().getViewingCamera()));
					}else{
						logger.debug(this.getName() + " Cursors not close enough to start gesture. Distance: " + newDistance);
					}
				}else{
					logger.debug(this.getName() + " we could NOT lock both cursors!");
					unUsedCursors.add(m);	
				}
			}else{
				logger.debug(this.getName() + " we didnt have a unused cursor previously to start gesture now");
				unUsedCursors.add(m);
			}
		}
	}

	@Override
	public void cursorUpdated(InputCursor m, MTFingerInputEvt positionEvent) {
		if (lockedCursors.size() == 2 && lockedCursors.contains(m)){
			InputCursor firstCursor = lockedCursors.get(0);
			InputCursor secondCursor = lockedCursors.get(1);
			Vector3D distance = (m.equals(firstCursor))? getNewTranslation(positionEvent.getTargetComponent(), firstCursor, secondCursor) : getNewTranslation(positionEvent.getTargetComponent(), secondCursor, firstCursor);
//			//logger.debug("DIST: " + distance);
			this.fireGestureEvent(new PanTwoFingerEvent(this, MTGestureEvent.GESTURE_UPDATED, positionEvent.getTargetComponent(), firstCursor, secondCursor, new Vector3D(distance.getX(),distance.getY(),0), positionEvent.getTargetComponent().getViewingCamera()));
		}
	}
	
	
	@Override
	public void cursorEnded(InputCursor m, MTFingerInputEvt positionEvent) {
		IMTComponent3D comp = positionEvent.getTargetComponent();
		logger.debug(this.getName() + " INPUT_ENDED RECIEVED - MOTION: " + m.getId());
		
		if (lockedCursors.size() == 2 && lockedCursors.contains(m)){
			InputCursor leftOverCursor = (lockedCursors.get(0).equals(m))? lockedCursors.get(1) : lockedCursors.get(0);
			
			lockedCursors.remove(m);
			if (unUsedCursors.size() > 0){ //Check if there are other cursors we could use for scaling if one was removed
				InputCursor futureCursor = unUsedCursors.get(0);
				if (this.canLock(futureCursor)){ //check if we have priority to lock another cursor and use it
					float newDistance = Vector3D.distance(
							new Vector3D(leftOverCursor.getCurrentEvent().getPosX(), leftOverCursor.getCurrentEvent().getPosY(),0),
							new Vector3D(futureCursor.getCurrentEvent().getPosX(), futureCursor.getCurrentEvent().getPosY(),0));
					if (newDistance < detectRadius) {//Check if other cursor is in distance 
						this.getLock(futureCursor);
						unUsedCursors.remove(futureCursor);
						lockedCursors.add(futureCursor);
						logger.debug(this.getName() + " we could lock another cursor! (ID:" + futureCursor.getId() +")");
						logger.debug(this.getName() + " continue with different cursors (ID: " + futureCursor.getId() + ")" + " " + "(ID: " + leftOverCursor.getId() + ")");
						//TODO fire start evt?
					}else{
						this.endGesture(m, leftOverCursor, comp);
					}
				}else{ //we dont have permission to use other cursor  - End scale
					this.endGesture(m, leftOverCursor, comp);
				}
			}else{ //no more unused cursors on comp - End scale
				this.endGesture(m, leftOverCursor, comp);
			}
			this.unLock(m); //FIXME TEST
		}else{ //cursor was not a scaling involved cursor
			if (unUsedCursors.contains(m)){
				unUsedCursors.remove(m);
			}
		}
	}
	
	
	/**
	 * End gesture.
	 * 
	 * @param inputEndedCursor the input ended cursor
	 * @param leftOverCursor the left over cursor
	 * @param comp the comp
	 */
	private void endGesture(InputCursor inputEndedCursor, InputCursor leftOverCursor, IMTComponent3D comp){
		lockedCursors.clear();
		unUsedCursors.add(leftOverCursor);
		this.unLock(leftOverCursor);
		this.fireGestureEvent(new PanTwoFingerEvent(this, MTGestureEvent.GESTURE_ENDED, comp, inputEndedCursor, leftOverCursor, new Vector3D(0,0,0), comp.getViewingCamera()));
	}
	
	

	/* (non-Javadoc)
	 * @see org.mt4j.input.inputAnalyzers.IInputAnalyzer#cursorLocked(org.mt4j.input.inputData.InputCursor, org.mt4j.input.inputAnalyzers.IInputAnalyzer)
	 */
	@Override
	public void cursorLocked(InputCursor m, IInputProcessor lockingAnalyzer) {
		if (lockingAnalyzer instanceof AbstractComponentProcessor){
			logger.debug(this.getName() + " Recieved MOTION LOCKED by (" + ((AbstractComponentProcessor)lockingAnalyzer).getName()  + ") - cursor ID: " + m.getId());
		}else{
			logger.debug(this.getName() + " Recieved MOTION LOCKED by higher priority signal - cursor ID: " + m.getId());
		}
		
		if (lockedCursors.contains(m)){ //cursors was used here! -> we have to stop the gesture
			//put all used cursors in the unused cursor list and clear the usedcursorlist
			unUsedCursors.addAll(lockedCursors); 
			lockedCursors.clear();
			//TODO fire ended evt?
			logger.debug(this.getName() + " cursor:" + m.getId() + " MOTION LOCKED. Was an active cursor in this gesture!");
		}
	}

	
	
	/* (non-Javadoc)
	 * @see org.mt4j.input.inputAnalyzers.IInputAnalyzer#cursorUnlocked(org.mt4j.input.inputData.InputCursor)
	 */
	@Override
	public void cursorUnlocked(InputCursor m) {
		logger.debug(this.getName() + " Recieved UNLOCKED signal for cursor ID: " + m.getId());
		
		if (lockedCursors.size() >= 2){ //we dont need the unlocked cursor, gesture still in progress
			return;
		}
		
		if (unUsedCursors.contains(m)){ //should always be true here!?
			if (unUsedCursors.size() >= 2){ //we can try to resume the gesture
				InputCursor firstCursor = unUsedCursors.get(0);
				InputCursor secondCursor = unUsedCursors.get(1);

				//See if we can obtain a lock on both cursors
				if (this.canLock(firstCursor, secondCursor)){
					float newDistance = Vector3D.distance(
							new Vector3D(firstCursor.getCurrentEvent().getPosX(), firstCursor.getCurrentEvent().getPosY(),0),
							new Vector3D(secondCursor.getCurrentEvent().getPosX(), secondCursor.getCurrentEvent().getPosY(),0));
					if (newDistance < detectRadius) {//Check if other cursor is in distance 
						this.getLock(firstCursor, secondCursor);
						unUsedCursors.remove(firstCursor);
						unUsedCursors.remove(secondCursor);
						lockedCursors.add(firstCursor);
						lockedCursors.add(secondCursor);
						logger.debug(this.getName() + " we could lock cursors: " + firstCursor.getId() +", " + secondCursor.getId());
						logger.debug(this.getName() + " continue with different cursors (ID: " + firstCursor.getId() + ")" + " " + "(ID: " + secondCursor.getId() + ")");
						//TODO fire start evt?
					}else{
						logger.debug(this.getName() + " distance was too great between cursors: " + firstCursor.getId() +", " + secondCursor.getId() + " distance: " + newDistance);
					}
				}else{
					logger.debug(this.getName() + " we could NOT lock cursors: " + firstCursor.getId() +", " + secondCursor.getId());
				}
			}
		}else{
			logger.error(this.getName() + "hmmm - investigate why is cursor not in unusedList?");
		}
	}
	
	
	
	
	/**
	 * Gets the new translation.
	 * 
	 * @param comp the comp
	 * @param movingCursor the moving cursor
	 * @param otherCursor the other cursor
	 * 
	 * @return the new translation
	 */
	private Vector3D getNewTranslation(IMTComponent3D comp, InputCursor movingCursor, InputCursor otherCursor){
		Vector3D fromFirstFinger = ToolsIntersection.getRayPlaneIntersection(
				Tools3D.getCameraPickRay(applet, comp.getViewingCamera(), movingCursor.getPreviousEvent().getPosX(), movingCursor.getPreviousEvent().getPosY()), 
				planeNormal, 
				pointInPlane);
		
		Vector3D fromSecondFinger = ToolsIntersection.getRayPlaneIntersection(
				Tools3D.getCameraPickRay(applet, comp.getViewingCamera(), otherCursor.getCurrentEvent().getPosX(), otherCursor.getCurrentEvent().getPosY()), 
				planeNormal, 
				pointInPlane);
		
		Vector3D oldMiddlePoint = getMiddlePointBetweenFingers(fromSecondFinger, fromFirstFinger);
		
		Vector3D toFirstFinger = ToolsIntersection.getRayPlaneIntersection(
				Tools3D.getCameraPickRay(applet, comp.getViewingCamera(), movingCursor.getCurrentEvent().getPosX(), movingCursor.getCurrentEvent().getPosY()), 
				planeNormal, 
				pointInPlane);
		
		Vector3D newMiddlePoint = getMiddlePointBetweenFingers(toFirstFinger ,  fromSecondFinger);
		Vector3D distance = newMiddlePoint.getSubtracted(oldMiddlePoint);
		return distance;
	}
	
	
	/**
	 * Gets the middle point between fingers.
	 * 
	 * @param firstFinger the first finger
	 * @param secondFinger the second finger
	 * 
	 * @return the middle point between fingers
	 */
	private Vector3D getMiddlePointBetweenFingers(Vector3D firstFinger, Vector3D secondFinger){
		Vector3D bla = secondFinger.getSubtracted(firstFinger); //= Richtungsvektor vom 1. zum 2. finger
		bla.scaleLocal(0.5f); //take the half
		return (new Vector3D(firstFinger.getX() + bla.getX(), firstFinger.getY() + bla.getY(), firstFinger.getZ() + bla.getZ()));
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.mt4j.input.inputAnalyzers.componentAnalyzers.AbstractComponentInputAnalyzer#getName()
	 */
	@Override
	public String getName() {
		return "two finger pan detector";
	}


	

}
