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
package org.mt4j.input.inputProcessors.componentProcessors;

import java.util.ArrayList;
import java.util.List;

import org.mt4j.input.inputData.InputCursor;
import org.mt4j.input.inputData.MTFingerInputEvt;
import org.mt4j.input.inputData.MTInputEvent;
import org.mt4j.input.inputProcessors.IInputProcessor;

public abstract class AbstractCursorProcessor extends AbstractComponentProcessor{
	private List<InputCursor> activeCursors;
	
	
	/** The lock priority. */
	private int lockPriority;
	
	
	public AbstractCursorProcessor(){
		activeCursors = new ArrayList<InputCursor>();
		this.lockPriority = 1;
	}

	@Override
	public boolean isInterestedIn(MTInputEvent inputEvt) {
//		return inputEvt instanceof AbstractCursorInputEvt;
		return inputEvt instanceof MTFingerInputEvt 
			&& inputEvt.hasTarget();
	}


	@Override
	protected void processInputEvtImpl(MTInputEvent inputEvent) {
//		AbstractCursorInputEvt posEvt = (AbstractCursorInputEvt)inputEvent;
		MTFingerInputEvt posEvt = (MTFingerInputEvt)inputEvent;
		InputCursor m = posEvt.getCursor();
		switch (posEvt.getId()) {
		case MTFingerInputEvt.INPUT_DETECTED:
			activeCursors.add(m);
			m.registerGeneralInterest(this);
			cursorStarted(m, posEvt);
			break;
		case MTFingerInputEvt.INPUT_UPDATED:
			cursorUpdated(m, posEvt);
			break;
		case MTFingerInputEvt.INPUT_ENDED:
			activeCursors.remove(m);
			cursorEnded(m, posEvt);
			m.unregisterGeneralInterest(this);
			break;
		default:
			break;
		}
	}
	
	
	/**
	 * Gets the active cursors which started on this component.
	 * 
	 * @return the active component cursors
	 */
	public List<InputCursor> getActiveComponentCursors(){
		return this.activeCursors;
	}
	
//	public List<InputCursor> getLockableCursors(){
//		for (Iterator iterator = activeCursors.iterator(); iterator.hasNext();) {
//			InputCursor cursor = (InputCursor) iterator.next();
//			if ()
//		}
//	}
	
	
	/**
	 * Gets the input cursor locking priority.
	 * 
	 * @return the input cursor locking priority
	 */
	public int getLockPriority() {
		return lockPriority;
	}


	/**
	 * Sets the  input cursor locking priority.
	 * 
	 * @param gesturePriority the new input cursor locking priority
	 */
	public void setLockPriority(int gesturePriority) {
		this.lockPriority = gesturePriority;
	}
	
	
	/**
	 * Checks if this input processor would have the 
	 * sufficient priority to lock the specified input cursors.
	 * 
	 * @param cursors the cursors
	 * 
	 * @return true, if successful
	 */
	protected boolean canLock(InputCursor... cursors){
		int locked = 0;
		for (int i = 0; i < cursors.length; i++) {
			InputCursor m = cursors[i];
			if (m.canLock(this)){
				locked++;
			}
		}
		return locked == cursors.length;
	}
	
	
	
	/**
	 * Locks the cursor with this processor if the processors lock priority
	 * is higher or equal than the current lock priority of this cursor.
	 * 
	 * @param cursors the cursors
	 * 
	 * @return true, if all specified cursors could get locked
	 */
	protected boolean getLock(InputCursor... cursors){
		int locked = 0;
		for (int i = 0; i < cursors.length; i++) {
			InputCursor m = cursors[i];
			if (m.getLock(this)){
				locked++;
			}
		}
		return locked == cursors.length;
	}
	
	
	
	/**
	 * Unlocks the specified cursors if they are not longer used by this processor.
	 * If the priority by which the cursors are locked changes by that, 
	 * the <code>cursorUnlocked</code> method is invoked on processors 
	 * with a lower priority who by that get a chance to lock this cursor again.
	 * 
	 * @param cursors the cursors
	 */
	protected void unLock(InputCursor... cursors){
		for (int i = 0; i < cursors.length; i++) {
			InputCursor inputCursor = cursors[i];
			inputCursor.unlock(this);
		}
	}
	
	
	@Override
	public int compareTo(AbstractComponentProcessor o) {
		if (o instanceof AbstractCursorProcessor) {
			AbstractCursorProcessor o2 = (AbstractCursorProcessor) o;
			
			if (this.getLockPriority() < o2.getLockPriority()){
				return -1;
			}else if (this.getLockPriority() > o2.getLockPriority()){
				return 1;
			}else{
				if (!this.equals(o2)
					&& this.getLockPriority() == o2.getLockPriority()
				){
					return -1;
				}
				return 0;
			}
		}else{
			return 1;
		}
	}	
	
	/**
	 * This method is called if a input processor with a higher locking-priority than this one sucessfully
	 * locked the specified cursor. If this cursor was used in this input processor, we have to stop using it until it
	 * is unlocked by the other processor!
	 * 
	 * @param cursor the cursor
	 * @param lockingprocessor the locking processor
	 */
	abstract public void cursorLocked(InputCursor cursor, IInputProcessor lockingprocessor);
	
	/**
	 * This method is called if a input processor with a higher locking-priority than this one removes his lock on the specified
	 * cursor (i.e. because the conditions for continuing the gesture aren't met anymore). This gives this input processor the chance to
	 * see if it can use the cursor and try to lock it again.
	 * 
	 * @param cursor the cursor
	 */
	abstract public void cursorUnlocked(InputCursor cursor);
	
	
	abstract public void cursorStarted(InputCursor inputCursor, MTFingerInputEvt currentEvent);
	
	abstract public void cursorUpdated(InputCursor inputCursor, MTFingerInputEvt currentEvent);
	
	abstract public void cursorEnded(InputCursor inputCursor, MTFingerInputEvt currentEvent);
	

}
