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
package org.mt4j.input.inputProcessors.componentProcessors.scaleProcessor;

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
 * The Class ScaleProcessor. 2-Finger Scale multi-touch gesture.
 * Fires ScaleEvent gesture events.
 * @author Christopher Ruff
 */
public class ScaleProcessor extends AbstractCursorProcessor {
	
	/** The applet. */
	private PApplet applet;
	
	/** The unused cursors. */
	private List<InputCursor> unUsedCursors;
	
	/** The locked cursors. */
	private List<InputCursor> lockedCursors;
	
	/** The scale context. */
	private ScaleContext sc;
	
	
	
	/**
	 * Instantiates a new scale processor.
	 * 
	 * @param graphicsContext the graphics context
	 */
	public ScaleProcessor(PApplet graphicsContext){
		this.applet = graphicsContext;
		this.unUsedCursors 	= new ArrayList<InputCursor>();
		this.lockedCursors 	= new ArrayList<InputCursor>();
		this.setLockPriority(2);
	}
	
	

	@Override
	public void cursorStarted(InputCursor m, MTFingerInputEvt fEvt) {
		IMTComponent3D comp = fEvt.getTargetComponent();
		if (lockedCursors.size() >= 2){ //scale with 2 fingers already in progress
			unUsedCursors.add(m);
			logger.debug(this.getName() + " has already enough cursors for this gesture - adding to unused ID:" + m.getId());
		}else{ //no scale in progress yet
			if (unUsedCursors.size() == 1){
				logger.debug(this.getName() + " has already has 1 unused cursor - we can try start gesture! used with ID:" + unUsedCursors.get(0).getId() + " and new cursor ID:" + m.getId());
				InputCursor otherCursor = unUsedCursors.get(0);
				//See if we can obtain a lock on both cursors
				if (this.canLock(otherCursor, m)){
					ScaleContext newContext = new ScaleContext(otherCursor, m, comp);
					if (!newContext.isGestureAborted()){
						sc = newContext;
						//we can - now lock them
						this.getLock(otherCursor, m);
						unUsedCursors.remove(otherCursor);
						lockedCursors.add(otherCursor);
						lockedCursors.add(m);
						logger.debug(this.getName() + " we could lock both cursors!");
						this.fireGestureEvent(new ScaleEvent(this, MTGestureEvent.GESTURE_DETECTED, comp, otherCursor, m, 1, 1, 1, sc.getSecondFingerNewPos()));
					}else{
						sc = null;
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
	public void cursorUpdated(InputCursor m, MTFingerInputEvt fEvt) {
		IMTComponent3D comp = fEvt.getTargetComponent();
		if (lockedCursors.size() == 2 && lockedCursors.contains(m)){
			float newFactor = sc.getUpdatedScaleFactor(m);
			//Use the other cursor as the scaling point
			if (m.equals(sc.getFirstFingerCursor())){
				this.fireGestureEvent(new ScaleEvent(this, MTGestureEvent.GESTURE_UPDATED, comp, sc.getFirstFingerCursor(), sc.getSecondFingerCursor(), newFactor, newFactor, 1, sc.getSecondFingerNewPos()));
			}else{
				this.fireGestureEvent(new ScaleEvent(this, MTGestureEvent.GESTURE_UPDATED, comp, sc.getFirstFingerCursor(), sc.getSecondFingerCursor(), newFactor, newFactor, 1, sc.getFirstFingerNewPos()));
			}
		}
	}

	
	
	@Override
	public void cursorEnded(InputCursor m, MTFingerInputEvt fEvt) {
		IMTComponent3D comp = fEvt.getTargetComponent();
		
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
					ScaleContext newContext = new ScaleContext(futureCursor, leftOverCursor, comp);
					if (!newContext.isGestureAborted()){
						sc = newContext;
						this.getLock(futureCursor);
						unUsedCursors.remove(futureCursor);
						lockedCursors.add(futureCursor);
						logger.debug(this.getName() + " continue with different cursors (ID: " + futureCursor.getId() + ")" + " " + "(ID: " + leftOverCursor.getId() + ")");
						//TODO fire start evt?
					}else{
						endGesture(leftOverCursor, comp, firstCursor, secondCursor);
					}
				}else{ //we dont have permission to use other cursor  - End scale
					endGesture(leftOverCursor, comp, firstCursor, secondCursor);
				}
			}else{ //no more unused cursors on comp - End scale
				endGesture(leftOverCursor, comp, firstCursor, secondCursor);
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
		this.fireGestureEvent(new ScaleEvent(this, MTGestureEvent.GESTURE_ENDED, component, firstCursor, secondCursor, 1, 1, 1, sc.getFirstFingerNewPos()));
	}
	

	//TODO evtl bei finger_UP doch unlock() auf alle used cursors damit geringere cursors nochmal übernehmen können und dann wenn sie
	//den fingerUP evt kriegen auch wirklich gestureENDED senden können, sonst wirds evtl nie gesended wenn nicht in usedCursors bei
	//finger_UP 
	//TODO oder eben doch bei locked steal ended senden und bei resume wieder start!? oder gilt oberes trotzdem?
	
	//suppose theres a priority 3 gesture
	
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

				if (this.canLock(firstCursor, secondCursor)){
					IMTComponent3D comp = firstCursor.getFirstEvent().getTargetComponent();
					ScaleContext newContext = new ScaleContext(firstCursor, secondCursor, comp);
					if (!newContext.isGestureAborted()){
						sc = newContext;
						this.getLock(firstCursor, secondCursor);
						unUsedCursors.remove(firstCursor);
						unUsedCursors.remove(secondCursor);
						lockedCursors.add(firstCursor);
						lockedCursors.add(secondCursor);
						logger.debug(this.getName() + " we could lock cursors: " + firstCursor.getId() +", " + secondCursor.getId());
						//TODO fire started evt?
//						this.fireGestureEvent(new ScaleEvent(this, MTGestureEvent.GESTURE_DETECTED, comp, firstCursor, secondCursor, 1, 1, 1, sc.getSecondFingerNewPos()));
					}else{
						sc = null;
						logger.debug(this.getName() + " we could NOT resume gesture - cursors not on component: " + firstCursor.getId() +", " + secondCursor.getId());
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
	 * The Class ScaleContext.
	 */
	private class ScaleContext {
		
		/** The first finger cursor. */
		private InputCursor firstFingerCursor; 
		
		/** The second finger cursor. */
		private InputCursor secondFingerCursor; 
		
		/** The object. */
		private IMTComponent3D object;
		
		/** The first finger new pos. */
		private Vector3D firstFingerNewPos;
		
		/** The second finger new pos. */
		private Vector3D secondFingerNewPos;
		
		/** The second finger start pos. */
		private Vector3D secondFingerStartPos;
		
		/** The last scale distance. */
		private float lastScaleDistance;
		
		/** The scale plane normal. */
		private Vector3D scalePlaneNormal;
		
		/** The new finger middle pos. */
		private Vector3D newFingerMiddlePos;
		
		/** The old finger middle pos. */
		private Vector3D oldFingerMiddlePos;
		
		/** The first finger start pos. */
		private Vector3D firstFingerStartPos;

		private boolean gestureAborted;
		
		
		/**
		 * Instantiates a new scale context.
		 * 
		 * @param firstFingerCursor the first finger cursor
		 * @param secondFingerCursor the second finger cursor
		 * @param object the object
		 */
		public ScaleContext(InputCursor firstFingerCursor, InputCursor secondFingerCursor, IMTComponent3D object) {
			super();
			this.firstFingerCursor = firstFingerCursor;
			this.secondFingerCursor = secondFingerCursor;
			this.object = object;
			
			//irgendwo vorher checken ob der 1. finger überhaupt noch über dem obj ist? ist nur sicher der fall wenn mit 1 finger gedraggt wird..
			Vector3D interPoint = object.getIntersectionGlobal(
					Tools3D.getCameraPickRay(applet, object, firstFingerCursor.getCurrentEvent().getPosX(), firstFingerCursor.getCurrentEvent().getPosY()));
			if (interPoint !=null)
				firstFingerNewPos = interPoint;
			else{
				logger.warn(getName() + " firstFingerNewPos NEW = NULL");
				this.firstFingerNewPos	= new Vector3D();
				gestureAborted = true;
			}
			
			Vector3D scndInterPoint = object.getIntersectionGlobal(
					Tools3D.getCameraPickRay(applet, object, secondFingerCursor.getCurrentEvent().getPosX(), secondFingerCursor.getCurrentEvent().getPosY()));
			if (scndInterPoint !=null)
				secondFingerNewPos = scndInterPoint;
			else{
				logger.warn(getName() + " secondFingerNewPos NEW = NULL");
				secondFingerNewPos = new Vector3D();
				gestureAborted = true;
			}
			
			firstFingerStartPos = firstFingerNewPos.getCopy();
			secondFingerStartPos = secondFingerNewPos.getCopy();
			
			this.lastScaleDistance = Vector3D.distance(firstFingerNewPos, secondFingerNewPos);
			 //Prevent scaling to 0 if both fingers are on the same position at scalstart
			if (lastScaleDistance == 0.0)
				lastScaleDistance = 1.0f;
			
			//TODO settable? get from objects? camera orthogonal?
			scalePlaneNormal = new Vector3D(0,0,1);
			/*
			newFingerMiddlePos = getMiddlePointBetweenFingers();
			oldFingerMiddlePos = newFingerMiddlePos.getCopy();
			*/
		}
		
		public boolean isGestureAborted() {
			return gestureAborted;
		}

		/**
		 * Gets the second finger cursor.
		 * 
		 * @return the second finger cursor
		 */
		public InputCursor getSecondFingerCursor() {
			return this.secondFingerCursor;
		}

		/**
		 * Gets the first finger cursor.
		 * 
		 * @return the first finger cursor
		 */
		public InputCursor getFirstFingerCursor() {
			return this.firstFingerCursor;
		}

		/**
		 * Gets the object.
		 * 
		 * @return the object
		 */
		public IMTComponent3D getObject() {
			return this.object;
		}

		/**
		 * Gets the updated scale values.
		 * 
		 * @param m the m
		 * 
		 * @return the updated scale values
		 */
		public float[] getUpdatedScaleValues(InputCursor m){
			//TODO make it possible to scale x only, or y only? -> resize
			return new float[3];
		}
		
		/**
		 * Gets the updated scale factor.
		 * 
		 * @param m the m
		 * 
		 * @return the updated scale factor
		 */
		public float getUpdatedScaleFactor(InputCursor m){
			if (object == null || object.getViewingCamera() == null){ //IF component was destroyed while gesture still active
				this.gestureAborted = true;
				return 1;
			}
			
			//FIXME REMOVE!!
//			scalePlaneNormal = ((MTPolygon)object).getNormal();
//			logger.debug("scalePlaneNormal: " + scalePlaneNormal);
//			/*
			if (m.equals(firstFingerCursor)){ ///FIRST FINGER MOVED!
				Vector3D newFirstFingerPos = ToolsIntersection.getRayPlaneIntersection(
						Tools3D.getCameraPickRay(applet, object, firstFingerCursor.getCurrentEvent().getPosX(), firstFingerCursor.getCurrentEvent().getPosY()), 
						scalePlaneNormal, 
						firstFingerStartPos.getCopy());
				
				//Update the field
				if (newFirstFingerPos != null)
					this.firstFingerNewPos = newFirstFingerPos;
			
			}else if (m.equals(secondFingerCursor)){ ///SECOND FINGER MOVED!
				Vector3D newSecondFingerPos = ToolsIntersection.getRayPlaneIntersection(
						Tools3D.getCameraPickRay(applet, object, secondFingerCursor.getCurrentEvent().getPosX(), secondFingerCursor.getCurrentEvent().getPosY()), 
						scalePlaneNormal, 
						secondFingerStartPos.getCopy());
				
//				//TODO dragplane aus den beiden fingern ableiten -> wenn obj schräg im raum, dragplane entsprechend
//				Vector3D newSecondFingerPos = ToolsIntersection.getRayPlaneIntersection(new Ray(rayStartPoint, newPointInRayDir), scalePlaneNormal, secondFingerStartPos.getCopy());
				
				//Update the field
				if (newSecondFingerPos != null)
					this.secondFingerNewPos = newSecondFingerPos;
			}
			
			//IF THE FINGERS ARE ON THE SAME POSITION RETURN 1 SO THAT NOT SCALING IS DONE
			//ELSE THE OBJECTS WILL DISAPPEAR, scaled to 0
			if (firstFingerNewPos.equalsVector(secondFingerNewPos))
				return 1.0f;
			
			float newScaleDistance = Vector3D.distance(firstFingerNewPos, secondFingerNewPos);
			
			float newScaleFactor = newScaleDistance/lastScaleDistance;
			
			lastScaleDistance = newScaleDistance;
			
			return newScaleFactor;
//			*/
//			return 1;
		}
		
		/**
		 * Gets the updated middle finger pos delta.
		 * 
		 * @return the updated middle finger pos delta
		 */
		public Vector3D getUpdatedMiddleFingerPosDelta(){ //TODO REMOVE?
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
			Vector3D bla = secondFingerNewPos.getSubtracted(firstFingerNewPos); //= Richtungsvektor vom 1. zum 2. finger
			bla.scaleLocal(0.5f); //take the half
			return (new Vector3D(firstFingerNewPos.getX() + bla.getX(), firstFingerNewPos.getY() + bla.getY(), firstFingerNewPos.getZ() + bla.getZ()));
		}

		/**
		 * Gets the first finger new pos.
		 * 
		 * @return the first finger new pos
		 */
		public Vector3D getFirstFingerNewPos() {
			return firstFingerNewPos;
		}

		/**
		 * Gets the second finger new pos.
		 * 
		 * @return the second finger new pos
		 */
		public Vector3D getSecondFingerNewPos() {
			return secondFingerNewPos;
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.mt4j.input.inputAnalyzers.componentAnalyzers.AbstractComponentInputAnalyzer#getName()
	 */
	@Override
	public String getName() {
		return "Scale Processor";
	}



}
