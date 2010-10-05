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
package org.mt4j.input.inputProcessors.componentProcessors.tapAndHoldProcessor;

import java.util.ArrayList;

import org.mt4j.components.MTCanvas;
import org.mt4j.components.interfaces.IMTComponent3D;
import org.mt4j.input.inputData.InputCursor;
import org.mt4j.input.inputData.MTFingerInputEvt;
import org.mt4j.input.inputProcessors.IInputProcessor;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.AbstractComponentProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.AbstractCursorProcessor;
import org.mt4j.util.math.Tools3D;
import org.mt4j.util.math.Vector3D;

import processing.core.PApplet;

/**
 * The Class TapAndHoldProcessor. Multi-Touch gesture which is triggered
 * after touching and resting the finger on the same spot for some time.
 * Fires TapAndHoldEvent gesture events.
 * 
 * @author Christopher Ruff
 */
public class TapAndHoldProcessor extends AbstractCursorProcessor {
	
	/** The applet. */
	private PApplet applet;
	
	/** The max finger up dist. */
	private float maxFingerUpDist;
	
	/** The un used cursors. */
	private ArrayList<InputCursor> unUsedCursors;
	
	/** The locked cursors. */
	private ArrayList<InputCursor> lockedCursors;
	
	/** The button down screen pos. */
	private Vector3D buttonDownScreenPos;

	/** The tap start time. */
	private long tapStartTime;
	
	/** The tap time. */
	private int holdTime;
	
	//TODO atm this only allows 1 tap on 1 component
	//if we want more we have to do different (save each cursor to each start time etc, dont relock other cursors)
	
	
	/**
	 * Instantiates a new tap processor.
	 * @param pa the pa
	 */
	public TapAndHoldProcessor(PApplet pa) {
		this(pa, 1800);
	}

	/**
	 * Instantiates a new tap and hold processor.
	 * @param pa the pa
	 * @param duration the duration
	 */
	public TapAndHoldProcessor(PApplet pa, int duration) {
		super();
		this.applet = pa;
		
		this.maxFingerUpDist = 17.0f;
		this.holdTime = duration;
		
		this.unUsedCursors 	= new ArrayList<InputCursor>();
		this.lockedCursors 	= new ArrayList<InputCursor>();
		this.setLockPriority(1);
		this.setDebug(false);
//		logger.setLevel(Level.DEBUG);
	}
	

	/* (non-Javadoc)
	 * @see org.mt4j.input.inputProcessors.componentProcessors.AbstractCursorProcessor#cursorStarted(org.mt4j.input.inputData.InputCursor, org.mt4j.input.inputData.AbstractCursorInputEvt)
	 */
	@Override
	public void cursorStarted(InputCursor c, MTFingerInputEvt positionEvent) {
		if (lockedCursors.size() >= 1){ //We assume that gesture is already in progress and add this new cursor to the unUsedList 
			unUsedCursors.add(c); 
		}else{
			if (unUsedCursors.size() == 0){ //Only start drag if no other finger on the component yet
				if (this.canLock(c)){//See if we can obtain a lock on this cursor (depends on the priority)
					this.getLock(c);
					logger.debug(this.getName() + " successfully locked cursor (id:" + c.getId() + ")");
					lockedCursors.add(c);
					buttonDownScreenPos = new Vector3D(c.getCurrentEvent().getPosX(), c.getCurrentEvent().getPosY(), 0);
					tapStartTime = System.currentTimeMillis();
					
					this.fireGestureEvent(new TapAndHoldEvent(this, MTGestureEvent.GESTURE_DETECTED, positionEvent.getTargetComponent(), c, false, new Vector3D(c.getCurrentEvent().getPosX(), c.getCurrentEvent().getPosY(), 0), this.holdTime, 0, 0));
					
					try {
						applet.registerPre(this);
					} catch (Exception e) {
						System.err.println(e.getMessage());
					}
				}else{
					unUsedCursors.add(c);
				}
			}else{
				unUsedCursors.add(c);
			}
		}
	}

	//problem: mouse does only send update evt if mouse is actually dragged
	//idea: registerPre and check if we have  alocked cursor and the times is up
	//then do the checking
	
	/**
	 * Pre.
	 */
	public void pre(){
		if (lockedCursors.size() == 1){
			IMTComponent3D comp = lockedCursors.get(0).getCurrentEvent().getTargetComponent();
			InputCursor c = lockedCursors.get(0).getCurrentEvent().getCursor();
			long nowTime = System.currentTimeMillis();
			long elapsedTime = nowTime - this.tapStartTime;
			Vector3D screenPos = new Vector3D(c.getCurrentEvent().getPosX(), c.getCurrentEvent().getPosY(), 0);
			float normalized = (float)elapsedTime / (float)this.holdTime;
			
			if (elapsedTime >= holdTime){
				normalized = 1;
				logger.debug("TIME PASSED!");
				Vector3D intersection = comp.getIntersectionGlobal(Tools3D.getCameraPickRay(applet, comp, c.getCurrentEvent().getPosX(), c.getCurrentEvent().getPosY()));
				//logger.debug("Distance between buttondownScreenPos: " + buttonDownScreenPos + " and upScrPos: " + buttonUpScreenPos +  " is: " + Vector3D.distance(buttonDownScreenPos, buttonUpScreenPos));
				if ( (intersection != null || comp instanceof MTCanvas) //FIXME hack - at canvas no intersection..
						&& 
					Vector3D.distance2D(buttonDownScreenPos, screenPos) <= this.maxFingerUpDist
				){
					this.fireGestureEvent(new TapAndHoldEvent(this, MTGestureEvent.GESTURE_ENDED, comp, c, true, screenPos, this.holdTime, elapsedTime, normalized));
				}else{
					logger.debug("DISTANCE TOO FAR OR NO INTERSECTION");
					this.fireGestureEvent(new TapAndHoldEvent(this, MTGestureEvent.GESTURE_ENDED, comp, c, false, screenPos, this.holdTime, elapsedTime, normalized));
				}
				lockedCursors.remove(c);
				this.unLock(c); 
				try {
					applet.unregisterPre(this);
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			}else{
				this.fireGestureEvent(new TapAndHoldEvent(this, MTGestureEvent.GESTURE_UPDATED, comp, c, false, screenPos, this.holdTime, elapsedTime, normalized));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.mt4j.input.inputProcessors.componentProcessors.AbstractCursorProcessor#cursorUpdated(org.mt4j.input.inputData.InputCursor, org.mt4j.input.inputData.AbstractCursorInputEvt)
	 */
	@Override
	public void cursorUpdated(InputCursor c, MTFingerInputEvt positionEvent) {
		if (lockedCursors.contains(c)){ //cursor is a actual used cursor
			IMTComponent3D comp = lockedCursors.get(0).getCurrentEvent().getTargetComponent();
			long nowTime = System.currentTimeMillis();
			long elapsedTime = nowTime - this.tapStartTime;
			Vector3D screenPos = new Vector3D(c.getCurrentEvent().getPosX(), c.getCurrentEvent().getPosY(), 0);
			float normalized = (float)elapsedTime / (float)this.holdTime;

			//logger.debug("Distance between buttondownScreenPos: " + buttonDownScreenPos + " and upScrPos: " + buttonUpScreenPos +  " is: " + Vector3D.distance(buttonDownScreenPos, buttonUpScreenPos));
			if (Vector3D.distance2D(buttonDownScreenPos, screenPos) > this.maxFingerUpDist){
				logger.debug("DISTANCE TOO FAR OR NO INTERSECTION");
				lockedCursors.remove(c);
				this.unLock(c); 
				try {
					applet.unregisterPre(this);
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
				this.fireGestureEvent(new TapAndHoldEvent(this, MTGestureEvent.GESTURE_ENDED, comp, c, false, screenPos, this.holdTime, elapsedTime, normalized));
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.mt4j.input.inputProcessors.componentProcessors.AbstractCursorProcessor#cursorEnded(org.mt4j.input.inputData.InputCursor, org.mt4j.input.inputData.AbstractCursorInputEvt)
	 */
	@Override
	public void cursorEnded(InputCursor c, MTFingerInputEvt positionEvent) {
		logger.debug(this.getName() + " INPUT_ENDED RECIEVED - MOTION: " + c.getId());
		if (lockedCursors.contains(c)){ //cursor was a actual used cursor
			lockedCursors.remove(c);
			
			long nowTime = System.currentTimeMillis();
			long elapsedTime = nowTime - this.tapStartTime;
			float normalized = (float)elapsedTime / (float)this.holdTime;
			
			if (unUsedCursors.size() > 0){ 			//check if there are other cursors on the component, we could use 
				InputCursor otherCursor = unUsedCursors.get(0); 
				if (this.canLock(otherCursor) 
						&& 
					Vector3D.distance2D(buttonDownScreenPos, new Vector3D(otherCursor.getCurrentEvent().getPosX(), otherCursor.getCurrentEvent().getPosY(), 0)) <= this.maxFingerUpDist)
				{ 	//Check if we have the priority to use this other cursor and if cursor is in range
					this.getLock(otherCursor);
					unUsedCursors.remove(otherCursor);
					lockedCursors.add(otherCursor);
					buttonDownScreenPos = new Vector3D(otherCursor.getCurrentEvent().getPosX(), otherCursor.getCurrentEvent().getPosY(), 0);
				}else{
					//Other cursor has higher prior -> end this gesture
					this.fireGestureEvent(new TapAndHoldEvent(this, MTGestureEvent.GESTURE_ENDED, c.getCurrentEvent().getTargetComponent(), c, false,  new Vector3D(c.getCurrentEvent().getPosX(), c.getCurrentEvent().getPosY(), 0), this.holdTime, elapsedTime, normalized));
					try {
						applet.unregisterPre(this);
					} catch (Exception e) {
						System.err.println(e.getMessage());
					}
				}
			}else{
				//We have no other cursor to continure gesture -> end
				this.fireGestureEvent(new TapAndHoldEvent(this, MTGestureEvent.GESTURE_ENDED, c.getCurrentEvent().getTargetComponent(), c, false,  new Vector3D(c.getCurrentEvent().getPosX(), c.getCurrentEvent().getPosY(), 0), this.holdTime, elapsedTime, normalized));
				try {
					applet.unregisterPre(this);
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			}
			this.unLock(c); 
		}else{ //cursor was not used here
			if (unUsedCursors.contains(c)){
				unUsedCursors.remove(c);
			}
		}
	}



	/* (non-Javadoc)
	 * @see org.mt4j.input.inputAnalyzers.IInputAnalyzer#cursorLocked(org.mt4j.input.inputData.InputCursor, org.mt4j.input.inputAnalyzers.IInputAnalyzer)
	 */
	@Override
	public void cursorLocked(InputCursor c, IInputProcessor lockingAnalyzer) {
		if (lockingAnalyzer instanceof AbstractComponentProcessor){
			logger.debug(this.getName() + " Recieved MOTION LOCKED by (" + ((AbstractComponentProcessor)lockingAnalyzer).getName()  + ") - cursor ID: " + c.getId());
		}else{
			logger.debug(this.getName() + " Recieved MOTION LOCKED by higher priority signal - cursor ID: " + c.getId());
		}
		if (lockedCursors.contains(c)){ //cursor was in use here
			lockedCursors.remove(c);
			
			long nowTime = System.currentTimeMillis();
			long elapsedTime = nowTime - this.tapStartTime;
			float normalized = (float)elapsedTime / (float)this.holdTime;
			
			this.fireGestureEvent(new TapAndHoldEvent(this, MTGestureEvent.GESTURE_ENDED, c.getCurrentEvent().getTargetComponent(), c, false, new Vector3D(c.getCurrentEvent().getPosX(), c.getCurrentEvent().getPosY(), 0), this.holdTime, elapsedTime, normalized));
			
			try {
				applet.unregisterPre(this);
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
			
			unUsedCursors.add(c);
			logger.debug(this.getName() + " cursor:" + c.getId() + " MOTION LOCKED. Was an active cursor in this gesture!");
		}else{ //TODO remove else, it is pretty useless
			if (unUsedCursors.contains(c)){
				logger.debug(this.getName() + " MOTION LOCKED. But it was NOT an active cursor in this gesture!");
			}
		}
	}



	/* (non-Javadoc)
	 * @see org.mt4j.input.inputAnalyzers.IInputAnalyzer#cursorUnlocked(org.mt4j.input.inputData.InputCursor)
	 */
	@Override
	public void cursorUnlocked(InputCursor c) {
		//TAP AND HOLD IS NOT RESUMABLE 
		
		
		/*
		logger.debug(this.getName() + " Recieved UNLOCKED signal for cursor ID: " + m.getId());

		if (lockedCursors.size() >= 1){ //we dont need the unlocked cursor, gesture still in progress
			logger.debug(this.getName() + " still in progress - we dont need the unlocked cursor" );
			return;
		}

		if (unUsedCursors.contains(m)){
			if (this.canLock(m)){
				this.getLock(m);
				unUsedCursors.remove(m);
				lockedCursors.add(m);
				//TODO fire started? maybe not.. do we have to?
				logger.debug(this.getName() + " can resume its gesture with cursor: " + m.getId());
			}
		}
		*/
	}

	
	
	/**
	 * Gets the max finger up dist.
	 * 
	 * @return the max finger up dist
	 */
	public float getMaxFingerUpDist() {
		return maxFingerUpDist;
	}


	/**
	 * Sets the maximum allowed distance of the position
	 * of the finger_down event and the finger_up event
	 * that fires a click event
	 * <br>This ensures that a click event is only raised
	 * if the finger didnt move that far during the click.
	 * 
	 * @param maxFingerUpDist the max finger up dist
	 */
	public void setMaxFingerUpDist(float maxFingerUpDist) {
		this.maxFingerUpDist = maxFingerUpDist;
	}
	
	
	
	
	/**
	 * Gets the time (in ms.) needed to hold to successfully tap&hold.
	 * 
	 * @return the Hold time
	 */
	public long getHoldTime() {
		return this.holdTime;
	}



	/**
	 * Sets the holding time for the gesture.
	 * 
	 * @param HoldTime the new tHoldap time
	 */
	public void setHoldTime(int tapTime) {
		this.holdTime = tapTime;
	}



	/* (non-Javadoc)
	 * @see org.mt4j.input.inputProcessors.componentProcessors.AbstractComponentProcessor#getName()
	 */
	@Override
	public String getName() {
		return "tap and hold processor";
	}

}
