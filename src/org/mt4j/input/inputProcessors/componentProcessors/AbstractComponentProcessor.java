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
package org.mt4j.input.inputProcessors.componentProcessors;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.mt4j.input.IMTInputEventListener;
import org.mt4j.input.inputData.MTInputEvent;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.IInputProcessor;
import org.mt4j.input.inputProcessors.MTGestureEvent;

/**
 * The Class AbstractComponentProcessor.
 * @author Christopher Ruff
 */
public abstract class AbstractComponentProcessor implements IMTInputEventListener, IInputProcessor,  Comparable<AbstractComponentProcessor> {
	protected static final Logger logger = Logger.getLogger(AbstractComponentProcessor.class.getName());
	static{
//		logger.setLevel(Level.ERROR);
		logger.setLevel(Level.WARN);
//		logger.setLevel(Level.DEBUG);
		SimpleLayout l = new SimpleLayout();
		ConsoleAppender ca = new ConsoleAppender(l);
		logger.addAppender(ca);
	}

	
	/** if disabled. */
	private boolean disabled;

	/** The input listeners. */
	private List<IGestureEventListener> inputListeners;

	/** The debug. */
	private boolean debug;
	
	/**
	 * Instantiates a new abstract component input processor.
	 */
	public AbstractComponentProcessor() {
		super();
		this.inputListeners = new ArrayList<IGestureEventListener>();
		
		debug = false;
	}


	/* (non-Javadoc)
	 * @see org.mt4j.input.IMTInputEventListener#processInputEvent(org.mt4j.input.inputData.MTInputEvent)
	 */
	//@Override
	public boolean processInputEvent(MTInputEvent inEvt){
//	public void processInputEvent(MTInputEvent inEvt){
		if(!this.isDisabled() && inEvt.hasTarget()){ //Allow component processors to recieve inputevts only if they have a target (Canvas is target if null is picked..)
			this.processInputEvtImpl(inEvt);
			return true;
		}else{
			return false;
		}
	}
	
	
	/**
	 * Process input evt implementation.
	 * 
	 * @param inputEvent the input event
	 */
	abstract protected void processInputEvtImpl(MTInputEvent inputEvent);
	
	
	/**
	 * Checks if this input processor is interested in the specified
	 * MTInputEvent instance.
	 * If we want to create custom input processors we override this method
	 * and return true only for the kind of events we want to recieve.
	 * 
	 * @param inputEvt the input evt
	 * 
	 * @return true, if is interested in
	 */
	abstract public boolean isInterestedIn(MTInputEvent inputEvt);
	
	
	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	abstract public String getName();
	
	
	/**
	 * Checks if is disabled.
	 * 
	 * @return true, if is disabled
	 */
	public boolean isDisabled() {
		return disabled;
	}

	/**
	 * Sets the disabled.
	 * 
	 * @param disabled the new disabled
	 */
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}


	
	/**
	 * Adds the processor listener.
	 * 
	 * @param listener the listener
	 */
	public synchronized void addGestureListener(IGestureEventListener listener){
		if (!inputListeners.contains(listener)){
			inputListeners.add(listener);
		}
		
	}
	
	/**
	 * Removes the processor listener.
	 * 
	 * @param listener the listener
	 */
	public synchronized void removeGestureListener(IGestureEventListener listener){
		if (inputListeners.contains(listener)){
			inputListeners.remove(listener);
		}
	}
	
	/**
	 * Gets the processor listeners.
	 * 
	 * @return the processor listeners
	 */
	public synchronized IGestureEventListener[] getGestureListeners(){
		return (IGestureEventListener[])inputListeners.toArray(new IGestureEventListener[this.inputListeners.size()]);
	}
	
	/**
	 * Fire gesture event.
	 * 
	 * @param ge the ge
	 */
	protected void fireGestureEvent(MTGestureEvent ge) {
//		/*
		if (debug){
			switch (ge.getId()) {
			case MTGestureEvent.GESTURE_DETECTED:
				System.out.println(((AbstractComponentProcessor)ge.getSource()).getName() +  " fired GESTURE_DETECTED");
				break;
			case MTGestureEvent.GESTURE_UPDATED:
				System.out.println(((AbstractComponentProcessor)ge.getSource()).getName() +  " fired GESTURE_UPDATED");
				break;
			case MTGestureEvent.GESTURE_ENDED:
				System.out.println(((AbstractComponentProcessor)ge.getSource()).getName() +  " fired GESTURE_ENDED");
				break;
			default:
				break;
			}
		}
//		 */
		
		for (IGestureEventListener listener : inputListeners){
			listener.processGestureEvent(ge);
		}
	}


	/**
	 * Sets the debug.
	 * 
	 * @param debug the new debug
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	
	
	
	public int compareTo(AbstractComponentProcessor o) {
		return -1;
	}
	
	
	

}
