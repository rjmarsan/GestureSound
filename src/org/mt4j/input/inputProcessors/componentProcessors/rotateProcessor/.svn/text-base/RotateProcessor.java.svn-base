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
package org.mt4j.input.inputProcessors.componentProcessors.rotateProcessor;

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
 * The Class RotateProcessor. Rotation multitouch gesture.
 * Fires RotateEvent gesture events.
 * @author Christopher Ruff
 */
public class RotateProcessor extends AbstractCursorProcessor {

	/** The applet. */
	private PApplet applet;

	/** The un used cursors. */
	private List<InputCursor> unUsedCursors;
	
	/** The locked cursors. */
	private List<InputCursor> lockedCursors;
	
	/** The rc. */
	private RotationContext rc;
	
	/** The drag plane normal. */
	private Vector3D dragPlaneNormal;
	
	
	/**
	 * Instantiates a new rotate processor.
	 * 
	 * @param graphicsContext the graphics context
	 */
	public RotateProcessor(PApplet graphicsContext){
		this.applet = graphicsContext;
		this.unUsedCursors 	= new ArrayList<InputCursor>();
		this.lockedCursors 	= new ArrayList<InputCursor>();
		this.dragPlaneNormal = new Vector3D(0,0,1);
		this.setLockPriority(2);
	}

	
	
	@Override
	public void cursorStarted(InputCursor m,MTFingerInputEvt positionEvent) {
		IMTComponent3D comp = positionEvent.getTargetComponent();
		if (lockedCursors.size() >= 2){ //gesture with 2 fingers already in progress
			unUsedCursors.add(m);
			logger.debug(this.getName() + " has already enough cursors for this gesture - adding to unused ID:" + m.getId());
		}else{ //no gesture in progress yet
			if (unUsedCursors.size() == 1){
				logger.debug(this.getName() + " has already has 1 unused cursor - we can try start gesture! used with ID:" + unUsedCursors.get(0).getId() + " and new cursor ID:" + m.getId());
				InputCursor otherCursor = unUsedCursors.get(0);
				
				if (this.canLock(otherCursor, m)){
					rc = new RotationContext(otherCursor, m, comp);
					if (!rc.isGestureAborted()){
						this.getLock(otherCursor, m);
						unUsedCursors.remove(otherCursor);
						lockedCursors.add(otherCursor);
						lockedCursors.add(m);
						logger.debug(this.getName() + " we could lock both cursors!");
						this.fireGestureEvent(new RotateEvent(this, MTGestureEvent.GESTURE_DETECTED, comp, otherCursor, m, Vector3D.ZERO_VECTOR, rc.getRotationPoint(), 0f));
					}else{
						logger.debug(this.getName() + " gesture aborted, probably at least 1 finger not on component!");
						rc = null;
						unUsedCursors.add(m);	
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
		IMTComponent3D comp = positionEvent.getTargetComponent();
		if (lockedCursors.size() == 2 && lockedCursors.contains(m)){
			float rotationAngleDegrees = rc.updateAndGetRotationAngle(m);
			this.fireGestureEvent(new RotateEvent(this, MTGestureEvent.GESTURE_UPDATED, comp, rc.getPinFingerCursor(), rc.getRotateFingerCursor(), Vector3D.ZERO_VECTOR, rc.getRotationPoint(), rotationAngleDegrees));
		}
	}


	@Override
	public void cursorEnded(InputCursor m, MTFingerInputEvt positionEvent) {
		IMTComponent3D comp = positionEvent.getTargetComponent();
		logger.debug(this.getName() + " INPUT_ENDED RECIEVED - MOTION: " + m.getId());
		
		if (lockedCursors.size() == 2 && lockedCursors.contains(m)){
			InputCursor firstCursor;
			InputCursor secondCursor;
			if (lockedCursors.get(0).equals(m)){
				firstCursor = m;
				secondCursor = lockedCursors.get(1);
			}else{
				firstCursor = lockedCursors.get(0);
				secondCursor = m;
			}
			
			lockedCursors.remove(m);
			InputCursor leftOverCursor = lockedCursors.get(0);
			
			if (unUsedCursors.size() > 0){ //Check if there are other cursors we could use for scaling if one was removed
				InputCursor futureCursor = unUsedCursors.get(0);
				if (this.canLock(futureCursor)){ //check if we have priority to claim another cursor and use it
					RotationContext newContext = new RotationContext(futureCursor, leftOverCursor, comp);
					if (!newContext.isGestureAborted()){
						rc = newContext;
						this.getLock(futureCursor);
						unUsedCursors.remove(futureCursor);
						lockedCursors.add(futureCursor);
						logger.debug(this.getName() + " continue with different cursors (ID: " + futureCursor.getId() + ")" + " " + "(ID: " + leftOverCursor.getId() + ")");
						//TODO fire start evt?
					}else{ //couldnt start gesture - cursor's not on component 
						this.endGesture(leftOverCursor, comp, firstCursor, secondCursor);
					}
				}else{ //we dont have permission to use other cursor  - End gesture
					this.endGesture(leftOverCursor, comp, firstCursor, secondCursor);
				}
			}else{ //no more unused cursors on comp - End gesture
				this.endGesture(leftOverCursor, comp, firstCursor, secondCursor);
			}
			this.unLock(m); //FIXME TEST
		}else{ //cursor was not a scaling involved cursor
			if (unUsedCursors.contains(m)){
				unUsedCursors.remove(m);
			}
		}
	}
	
	private void endGesture(InputCursor leftOverCursor, IMTComponent3D component, InputCursor firstCursor, InputCursor secondCursor){
		lockedCursors.clear();
		unUsedCursors.add(leftOverCursor);
		this.unLock(leftOverCursor);
		this.fireGestureEvent(new RotateEvent(this, MTGestureEvent.GESTURE_ENDED, component, firstCursor, secondCursor, Vector3D.ZERO_VECTOR, rc.getRotationPoint(), 0));
	}
	

	//TODO evtl bei finger_UP doch unlock() auf alle used cursors damit geringere cursors nochmal übernehmen können und dann wenn sie
	//den fingerUP evt kriegen auch wirklich gestureENDED senden können, sonst wirds evtl nie gesended wenn nicht in usedCursors bei
	//finger_UP 
	//TODO oder eben doch bei locked steal ended senden und bei resume wieder start!? oder gilt oberes trotzdem?
	
	//suppose theres a priority 3 gesture
	
	//TODO at resuming scale check if cursor still on object! else we get problems and a wrong startPoint etc
	
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
		
		if (lockedCursors.contains(m)){ 
			//cursors was used here! -> we have to stop the gesture
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
					IMTComponent3D comp = firstCursor.getFirstEvent().getTargetComponent();
					RotationContext newContext = new RotationContext(firstCursor, secondCursor, comp);
					if (!newContext.isGestureAborted()){ //Check if we could start gesture (ie. if fingers on component)
						rc = newContext;
						this.getLock(firstCursor, secondCursor);
						lockedCursors.add(firstCursor);
						lockedCursors.add(secondCursor);
						logger.debug(this.getName() + " we could lock cursors: " + firstCursor.getId() +", " + secondCursor.getId());
						unUsedCursors.remove(firstCursor);
						unUsedCursors.remove(secondCursor);
					}else{
						rc = null;
						logger.debug(this.getName() + " we could NOT resume gesture - cursors not on component: " + firstCursor.getId() +", " + secondCursor.getId());
					}
					//TODO fire started evt?
				}else{
					logger.debug(this.getName() + " we could NOT lock cursors: " + firstCursor.getId() +", " + secondCursor.getId());
				}
			}
		}else{
			logger.error(this.getName() + "hmmm - investigate why is cursor not in unusedList?");
		}
	}
	
	
	
	
	
	/**
	 * The Class RotationContext.
	 */
	private class RotationContext {

		/** The pin finger start. */
		private Vector3D pinFingerStart;

		/** The pin finger last. */
		private Vector3D pinFingerLast;

		/** The pin finger new. */
		private Vector3D pinFingerNew;

		/** The rotate finger start. */
		private Vector3D rotateFingerStart;

		/** The rotate finger last. */
		private Vector3D rotateFingerLast;

		/** The rotate finger new. */
		private Vector3D rotateFingerNew;

		/** The last rotation vect. */
		private Vector3D lastRotationVect;

		/** The object. */
		private IMTComponent3D object;

		/** The rotation point. */
		private Vector3D rotationPoint;

		/** The pin finger cursor. */
		private InputCursor pinFingerCursor; 

		/** The rotate finger cursor. */
		private InputCursor rotateFingerCursor; 

		/** The new finger middle pos. */
		private Vector3D newFingerMiddlePos;

		/** The old finger middle pos. */
		private Vector3D oldFingerMiddlePos;

		/** The pin finger translation vect. */
		private Vector3D pinFingerTranslationVect;

		private boolean gestureAborted;

		/**
		 * Instantiates a new rotation context.
		 * 
		 * @param pinFingerCursor the pin finger cursor
		 * @param rotateFingerCursor the rotate finger cursor
		 * @param object the object
		 */
		public RotationContext(InputCursor pinFingerCursor, InputCursor rotateFingerCursor, IMTComponent3D object){
			this.pinFingerCursor = pinFingerCursor;
			this.rotateFingerCursor = rotateFingerCursor;

			Vector3D interPoint = object.getIntersectionGlobal(
					Tools3D.getCameraPickRay(applet, object, pinFingerCursor.getCurrentEvent().getPosX(), pinFingerCursor.getCurrentEvent().getPosY()));

			if (interPoint !=null)
				pinFingerNew = interPoint;
			else{
				logger.warn(getName() + " Pinfinger NEW = NULL");
				pinFingerNew = new Vector3D();
				//TODO ABORT THE Rotation HERE!
				gestureAborted = true;
			}

			//Use lastEvent when resuming with another cursor that started long ago
			Vector3D interPointRot = object.getIntersectionGlobal(
					Tools3D.getCameraPickRay(applet, object, rotateFingerCursor.getCurrentEvent().getPosX(), rotateFingerCursor.getCurrentEvent().getPosY()));

			if (interPointRot !=null)
				rotateFingerStart = interPointRot;
			else{
				logger.warn(getName() + " rotateFingerStart = NULL");
				rotateFingerStart = new Vector3D();
				//TODO ABORT THE Rotation HERE!
				gestureAborted = true;
			}

			this.pinFingerStart = pinFingerNew.getCopy(); 

			this.pinFingerLast	= pinFingerStart.getCopy(); 

			this.rotateFingerLast	= rotateFingerStart.getCopy();
			this.rotateFingerNew	= rotateFingerStart.getCopy();	

			this.object = object;

			this.rotationPoint = pinFingerNew.getCopy();

			//Get the rotation vector for reference for the next rotation
			this.lastRotationVect = rotateFingerStart.getSubtracted(pinFingerNew);

			newFingerMiddlePos = getMiddlePointBetweenFingers();
			oldFingerMiddlePos = newFingerMiddlePos.getCopy();

			pinFingerTranslationVect = new Vector3D(0,0,0);

			//FIXME REMOVE!
//			dragPlaneNormal = ((MTPolygon)object).getNormal();
//			logger.debug("DragNormal: " + dragPlaneNormal);
		}

		/**
		 * Update and get rotation angle.
		 * 
		 * @param moveCursor the move cursor
		 * 
		 * @return the float
		 */
		public float updateAndGetRotationAngle(InputCursor moveCursor) {
//			/*
			float newAngleRad;
			float newAngleDegrees;

			//save the current pinfinger location as the old one
			this.pinFingerLast = this.pinFingerNew;

			//save the current pinfinger location as the old one
			this.rotateFingerLast = this.rotateFingerNew;

			//Check which finger moved and has to be updated
			if (moveCursor.equals(pinFingerCursor)){
				updatePinFinger();
			}
			else if (moveCursor.equals(rotateFingerCursor)){
				updateRotateFinger();
			}

			//FIXME REMOVE!
//			dragPlaneNormal = ((MTPolygon)object).getNormal();
//			logger.debug("DragNormal: " + dragPlaneNormal);

			//TODO drop Z values after that?
			//calculate the vector between the rotation finger vectors
//			Vector3D currentRotationVect = rotateFingerNew.getSubtracted(pinFingerNew);
			Vector3D currentRotationVect = rotateFingerNew.getSubtracted(pinFingerNew).normalizeLocal(); //FIXME TEST normalize rotation vector

			//calculate the angle between the rotaion finger vectors
			newAngleRad 	= Vector3D.angleBetween(lastRotationVect, currentRotationVect);
			newAngleDegrees = (float)Math.toDegrees(newAngleRad); 

			//FIXME EXPERIMENTAL BECAUSE ANGLEBETWEEN GIVES ROTATIONS SOMETIMES WHEN BOTH VECTORS ARE EQUAL!?
			if (rotateFingerLast.equalsVector(rotateFingerNew) && pinFingerLast.equalsVector(pinFingerNew)){
				//logger.debug("Angle gleich lassen");
				newAngleDegrees = 0.0f;
			}else{
				//logger.debug("Neuer Angle: " + newAngleDegrees);
			}

//			logger.debug("lastRotVect: " + lastRotationVect + " currentROtationVect: " + currentRotationVect + " Deg: " + newAngleDegrees);

			Vector3D cross = lastRotationVect.getCross(currentRotationVect);

			//Get the direction of rotation
			if (cross.getZ() < 0){
				newAngleDegrees*=-1;
			}

			//Check if the current and last rotation vectors are equal or not 
			if (!Float.isNaN(newAngleDegrees)/*!String.valueOf(newAngleDegrees).equalsIgnoreCase("NaN")*/){
				//if (newAngleDegrees != Float.NaN){ //if the vectors are equal rotationangle is NAN?
				lastRotationVect = currentRotationVect;

				return newAngleDegrees;
			}else{
				//lastRotationVect = currentRotationVect;
				return 0;
			}
//			*/
//			return 0;
		}


		/**
		 * Update rotate finger.
		 */
		private void updateRotateFinger(){
			if (object == null || object.getViewingCamera() == null){ //IF component was destroyed while gesture still active
				this.gestureAborted = true;
				return ;
			}
			
			//TODO save last position and use that one if new one is null.. everywhere!
			Vector3D newRotateFingerPos = ToolsIntersection.getRayPlaneIntersection(
					Tools3D.getCameraPickRay(applet, object,rotateFingerCursor.getCurrentEvent().getPosX(), rotateFingerCursor.getCurrentEvent().getPosY()), 
					dragPlaneNormal, 
					rotateFingerStart.getCopy());
			//Update the field
			if (newRotateFingerPos != null){
				this.rotateFingerNew = newRotateFingerPos;

				//Reset pinfinger tranlation vector
				this.pinFingerTranslationVect = new Vector3D(0,0,0);

				this.rotationPoint = pinFingerNew; 
			}else{
				logger.error(getName() + " new newRotateFinger Pos = null at update");
			}
		}


		/**
		 * Update pin finger.
		 */
		private void updatePinFinger(){  
			if (object == null){ //IF component was destroyed while gesture still active
				this.gestureAborted = true;
				return;
			}
			
			Vector3D newPinFingerPos = ToolsIntersection.getRayPlaneIntersection(
					Tools3D.getCameraPickRay(applet, object, pinFingerCursor.getCurrentEvent().getPosX(), pinFingerCursor.getCurrentEvent().getPosY()), 
					dragPlaneNormal, 
					pinFingerStart.getCopy()); 
			if (newPinFingerPos != null){
				// Update pinfinger with new position
				this.pinFingerNew = newPinFingerPos;
				this.pinFingerTranslationVect = pinFingerNew.getSubtracted(pinFingerLast); //FIXME REMOVE?
				//Set the Rotation finger as the rotation point because the pinfinger was moved
				this.rotationPoint = rotateFingerNew; //FIXME REMOVE!!? made because of scale
			}else{
				logger.error(getName() + " new PinFinger Pos = null at update");
			}
		}

		//if obj is drag enabled, und not scalable! send middlepoint delta für translate, 
		/**
		 * Gets the updated middle finger pos delta.
		 * 
		 * @return the updated middle finger pos delta
		 */
		public Vector3D getUpdatedMiddleFingerPosDelta(){
			newFingerMiddlePos = getMiddlePointBetweenFingers();
			Vector3D returnVect = newFingerMiddlePos.getSubtracted(oldFingerMiddlePos);

			this.oldFingerMiddlePos = newFingerMiddlePos;
			return returnVect;
		}

		/**
		 * Gets the middle point between fingers.
		 * 
		 * @return the middle point between fingers
		 */
		public Vector3D getMiddlePointBetweenFingers(){
			Vector3D bla = rotateFingerNew.getSubtracted(pinFingerNew); //= Richtungsvektor vom 1. zum 2. finger
			bla.scaleLocal(0.5f); //take the half
			return (new Vector3D(pinFingerNew.getX() + bla.getX(), pinFingerNew.getY() + bla.getY(), pinFingerNew.getZ() + bla.getZ()));
		}


		/**
		 * Gets the pin finger translation vect.
		 * 
		 * @return the pin finger translation vect
		 */
		public Vector3D getPinFingerTranslationVect() {
			return pinFingerTranslationVect;
		}

		/**
		 * Gets the pin finger start.
		 * 
		 * @return the pin finger start
		 */
		public Vector3D getPinFingerStart() {
			return pinFingerStart;
		}

		/**
		 * Gets the rotate finger start.
		 * 
		 * @return the rotate finger start
		 */
		public Vector3D getRotateFingerStart() {
			return rotateFingerStart;
		}

		/**
		 * Gets the rotation point.
		 * 
		 * @return the rotation point
		 */
		public Vector3D getRotationPoint() {
			return rotationPoint;
		}

		/**
		 * Gets the pin finger cursor.
		 * 
		 * @return the pin finger cursor
		 */
		public InputCursor getPinFingerCursor() {
			return pinFingerCursor;
		}

		/**
		 * Gets the rotate finger cursor.
		 * 
		 * @return the rotate finger cursor
		 */
		public InputCursor getRotateFingerCursor() {
			return rotateFingerCursor;
		}

		public boolean isGestureAborted() {
			return gestureAborted;
		}

	}

	
	
	/* (non-Javadoc)
	 * @see org.mt4j.input.inputAnalyzers.componentAnalyzers.AbstractComponentInputAnalyzer#getName()
	 */
	@Override
	public String getName() {
		return "Rotate Processor";
	}






	

}
