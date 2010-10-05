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
package org.mt4j.input.inputProcessors.componentProcessors.dragProcessor;

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
 * The Class DragProcessor. For multi touch drag behaviour on components.
 * Fires DragEvent gesture events.
 * @author Christopher Ruff
 */
public class DragProcessor extends AbstractCursorProcessor {
	
	/** The applet. */
	private PApplet applet;
	
	/** The dc. */
	private DragContext dc;
	
	/** The un used cursorss. */
	private List<InputCursor> unUsedCursors;
	
	/** The locked cursorss. */
	private List<InputCursor> lockedCursors;
	

	/**
	 * Instantiates a new drag processor.
	 * 
	 * @param graphicsContext the graphics context
	 */
	public DragProcessor(PApplet graphicsContext){
		this.applet = graphicsContext;
		this.unUsedCursors = new ArrayList<InputCursor>();
		this.lockedCursors = new ArrayList<InputCursor>();
		this.setLockPriority(1);
		this.setDebug(false);
	}
	

	@Override
	public void cursorStarted(InputCursor cursor, MTFingerInputEvt positionEvent) {
		IMTComponent3D comp = positionEvent.getTargetComponent();
		if (lockedCursors.size() >= 1){ //We assume that drag is already in progress and add this new cursors to the unUsedList 
			unUsedCursors.add(cursor); 
		}else{
//			if (unUsedCursors.size() == 0){ //Only start drag if no other finger on the component yet -> use 1st finger //FIXME REALLY!??
				if (this.canLock(cursor)){//See if we can obtain a lock on this cursors (depends on the priority)
					dc = new DragContext(cursor, comp); 
					if (!dc.isGestureAborted()){ //See if the drag couldnt start (i.e. cursor doesent hit component anymore etc)
						this.getLock(cursor);//Lock the cursors for this processor
						logger.debug(this.getName() + " successfully locked cursor (id:" + cursor.getId() + ")");
						//TODO put the following in own method because needed often!
						lockedCursors.add(cursor);
						//Fire the "drag started" event
						this.fireGestureEvent(new DragEvent(this,MTGestureEvent.GESTURE_DETECTED, comp, cursor, dc.getLastPosition(), dc.getNewPosition()));
					}else{
						logger.debug(this.getName() + " gesture aborted, probably finger not on component!");
						dc = null;
						unUsedCursors.add(cursor);
					}
				}else{
					logger.debug(this.getName() + " we could NOT lock the cursor " + cursor);
					unUsedCursors.add(cursor);
				}
//			}else{
//				logger.debug(this.getName() + " already using a cursor, dont need cursor " + cursor);
//				unUsedCursors.add(cursor);
//			}
		}
	}

	@Override
	public void cursorUpdated(InputCursor m, MTFingerInputEvt positionEvent) {
		IMTComponent3D comp = positionEvent.getTargetComponent();
		if (lockedCursors.contains(m)){
			dc.updateDragPosition();
			this.fireGestureEvent(new DragEvent(this, MTGestureEvent.GESTURE_UPDATED, comp, m, dc.getLastPosition(), dc.getNewPosition()));
		}
	}

	
	@Override
	public void cursorEnded(InputCursor m, MTFingerInputEvt positionEvent) {
		IMTComponent3D comp = positionEvent.getTargetComponent();
		logger.debug(this.getName() + " INPUT_ENDED RECIEVED - MOTION: " + m.getId());
		if (lockedCursors.contains(m)){ //cursors was a actual gesture cursors
			dc.updateDragPosition();
			lockedCursors.remove(m);
			if (unUsedCursors.size() > 0){ //check if there are other cursorss on the component, we could use for drag
				InputCursor otherMotion = unUsedCursors.get(0); //TODO cycle through all available unUsedCursors and try to claim one, maybe the first one is claimed but another isnt!
				if (this.canLock(otherMotion)){ //Check if we have the priority to use this cursors
					DragContext newContext = new DragContext(otherMotion, comp); 
					if (!newContext.isGestureAborted()){
						dc = newContext;
						this.getLock(otherMotion);
						unUsedCursors.remove(otherMotion);
						lockedCursors.add(otherMotion);
						//TODO fire started? maybe not.. do we have to?
					}else{
						this.fireGestureEvent(new DragEvent(this, MTGestureEvent.GESTURE_ENDED, comp, m, dc.getLastPosition(), dc.getNewPosition()));
					}
				}else{
					this.fireGestureEvent(new DragEvent(this, MTGestureEvent.GESTURE_ENDED, comp, m, dc.getLastPosition(), dc.getNewPosition()));
				}
			}else{
				this.fireGestureEvent(new DragEvent(this, MTGestureEvent.GESTURE_ENDED, comp, m, dc.getLastPosition(), dc.getNewPosition()));
			}
			this.unLock(m); //FIXME TEST
		}else{ //cursors was not used for dragging
			if (unUsedCursors.contains(m)){
				unUsedCursors.remove(m);
			}
		}
	}


	
//FIXME wenn 1 cursors drag dann 2 cursors drag (wenn nur drag erlaubt auf obj) und dann 1.weg und 2. abort weil nicht auf obj, kann kein neuer finger auf obj starten!?
	
	/* (non-Javadoc)
	 * @see org.mt4j.input.inputAnalyzers.IInputAnalyzer#cursorsLocked(org.mt4j.input.inputData.InputMotion, org.mt4j.input.inputAnalyzers.IInputAnalyzer)
	 */
	@Override
	public void cursorLocked(InputCursor m, IInputProcessor lockingAnalyzer) {
		if (lockingAnalyzer instanceof AbstractComponentProcessor){
			logger.debug(this.getName() + " Recieved MOTION LOCKED by (" + ((AbstractComponentProcessor)lockingAnalyzer).getName()  + ") - cursors ID: " + m.getId());
		}else{
			logger.debug(this.getName() + " Recieved MOTION LOCKED by higher priority signal - cursors ID: " + m.getId());
		}

		if (lockedCursors.contains(m)){ //cursors was a actual gesture cursors
			lockedCursors.remove(m);
			//TODO fire ended evt?
			unUsedCursors.add(m);
			logger.debug(this.getName() + " cursors:" + m.getId() + " MOTION LOCKED. Was an active cursor in this gesture!");
		}else{ //TODO remove "else", it is pretty useless
			if (unUsedCursors.contains(m)){
				logger.debug(this.getName() + " MOTION LOCKED. But it was NOT an active cursors in this gesture!");
			}
		}
	}



	@Override
	public void cursorUnlocked(InputCursor m) {
		logger.debug(this.getName() + " Recieved UNLOCKED signal for cursors ID: " + m.getId());
		if (lockedCursors.size() >= 1){ //we dont need the unlocked cursors, gesture still in progress
			return;
		}
		
		if (unUsedCursors.contains(m)){
			if (this.canLock(m)){
				DragContext newContext = new DragContext(m, m.getCurrentEvent().getTargetComponent());
				if (!newContext.isGestureAborted()){
					dc = newContext;
					this.getLock(m);
					unUsedCursors.remove(m);
					lockedCursors.add(m);
					//TODO fire started? maybe not.. do we have to?
					logger.debug(this.getName() + " can resume its gesture with cursors: " + m.getId());
				}else{
					dc = null;
					logger.debug(this.getName() + " we could NOT start gesture - cursors not on component: " + m.getId());
				}
			}else{
				logger.debug(this.getName() + " still in progress - we dont need the unlocked cursors" );
			}
		}
	}

	
	
	
	
	/**
	 * The Class DragContext.
	 */
	private class DragContext {
				
				/** The start position. */
				private Vector3D startPosition;
				
				/** The last position. */
				private Vector3D lastPosition;
				
				/** The new position. */
				private Vector3D newPosition;
				
				/** The drag object. */
				private IMTComponent3D dragObject;
				
				/** The m. */
				private InputCursor m; 
				
				/** The gesture aborted. */
				private boolean gestureAborted;
				
				/** The drag plane normal. */
				private Vector3D dragPlaneNormal;

				
				/**
				 * Instantiates a new drag context.
				 * 
				 * @param m the m
				 * @param dragObject the drag object
				 */
				public DragContext(InputCursor m, IMTComponent3D dragObject){	
					this.dragObject = dragObject;
					this.m = m;
					gestureAborted = false;
					
					//Calculate the normal of the plane we will be dragging at (useful if camera isnt default)
					this.dragPlaneNormal =  dragObject.getViewingCamera().getPosition().getSubtracted(dragObject.getViewingCamera().getViewCenterPos()).normalizeLocal();
					
					//Set the Drag Startposition
					Vector3D interSectP = dragObject.getIntersectionGlobal(
							Tools3D.getCameraPickRay(applet, dragObject, m.getCurrentEvent().getPosX(), m.getCurrentEvent().getPosY()));
					
					if (interSectP != null)
						this.startPosition = interSectP;
					else{
						logger.warn(getName() + " Drag StartPoint Null -> aborting drag");
						gestureAborted = true; 
						this.startPosition = new Vector3D(0,0,0); //TODO ABORT GESTURE!
						//abortGesture(m); //TODO in anderen analyzern auch machen
					}
					
					this.newPosition = startPosition.getCopy();
					this.updateDragPosition();
					
					//Set the Drags lastPostition (the last one before the new one)
					this.lastPosition	= startPosition.getCopy();
				}
				
				/**
				 * Update drag position.
				 */
				public void updateDragPosition(){
					if (dragObject == null || dragObject.getViewingCamera() == null){ //IF component was destroyed while gesture still active
						this.gestureAborted = true;
						return ;
					}
					
					Vector3D newPos = ToolsIntersection.getRayPlaneIntersection(
							Tools3D.getCameraPickRay(applet, dragObject, m.getCurrentEvent().getPosX(), m.getCurrentEvent().getPosY()), 
							dragPlaneNormal, 
							startPosition);
					if (newPos != null){
						lastPosition = newPosition;
						newPosition = newPos;
					}
				}


				/**
				 * Gets the last position.
				 * 
				 * @return the last position
				 */
				public Vector3D getLastPosition() {
					return lastPosition;
				}

				/**
				 * Gets the new position.
				 * 
				 * @return the new position
				 */
				public Vector3D getNewPosition() {
					return newPosition;
				}

				/**
				 * Checks if is gesture aborted.
				 * 
				 * @return true, if is gesture aborted
				 */
				public boolean isGestureAborted() {
					return gestureAborted;
				}
			}
	
	
	
	/* (non-Javadoc)
	 * @see org.mt4j.input.inputAnalyzers.componentAnalyzers.AbstractComponentInputAnalyzer#getName()
	 */
	@Override
	public String getName() {
		return "Drag Processor";
	}

}
