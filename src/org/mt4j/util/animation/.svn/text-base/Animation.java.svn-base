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
package org.mt4j.util.animation;

import java.util.ArrayList;


/**
 * The Class Animation.
 * @author Christopher Ruff
 */
public class Animation implements IAnimationManagerListener{
	
	/** The name. */
	private String name;
	
	/** The interpolator. */
	private Iinterpolator interpolator;
	
	/** The target object. */
	private Object targetObject; 
	
	/** The animation listeners. */
	private ArrayList<IAnimationListener> animationListeners; 
	
	/** The reset on finish. */
	private boolean resetOnFinish;
	
	/** The trigger time. */
	private long triggerTime;
	
	/** The trigger count down. */
	private long triggerCountDown;
	
	/** The has started. */
	private boolean hasStarted;
	
	
	/**
	 * Instantiates a new animation.
	 * 
	 * @param name the name
	 * @param interpolator the interpolator
	 * @param targetObject the target object
	 */
	public Animation(String name, Iinterpolator interpolator, Object targetObject) {
		this(name, interpolator, targetObject, 0);
	}
	
	/**
	 * creates a new Animation object with the given interpolator.
	 * <br> if animating a concrete object, the targetObject should
	 * be passed as a parameter. If the Animation has no concrete target
	 * "null" can be passed
	 * 
	 * @param name the name
	 * @param interpolator the interpolator
	 * @param targetObject the target object
	 * @param triggerTime the trigger time
	 */
	public Animation(String name, Iinterpolator interpolator, Object targetObject, int triggerTime) {
		super();
		this.resetOnFinish = true; //Default
		this.name = name;
		
		this.interpolator = interpolator;
		this.targetObject = targetObject;
		
		animationListeners = new ArrayList<IAnimationListener>();
		
		this.triggerTime = triggerTime;
		this.triggerCountDown = triggerTime;
		this.hasStarted = false;
	}
	
	
//	/**
//	 * makes this animation object fire an animation event,
//	 * should ony get called by the AnimationManager class
//	 * @param e
//	 */
//	public void postEvent(AnimationEvent e){
//		fireAnimationEvent(e);
//		
//		if (e.getId() == AnimationEvent.ANIMATION_ENDED){
//			this.setEnabled(false);
//			if (this.isResetOnFinish())
//				this.getInterpolator().resetInterpolator();
////			System.out.println("Animation FINISHED: " + this.getName());
//		}
//	}
	
	/**
 * Start.
 */
public void start(){
		if (this.getInterpolator().isFinished()){
			System.err.println("Animation: " + this.getName() + " has finished! To start it again, call restart() or set Animation.setResetOnFinish(true)");
			return;
		}
		
		AnimationManager.getInstance().addAnimation(this);
		AnimationManager.getInstance().addAnimationManagerListener(this);
		
//		System.out.println("Animation STARTED: " + this.getName());
//		fireAnimationEvent(new AnimationEvent(this, AnimationEvent.ANIMATION_STARTED, this, this.getTargetObject()));
	}
	
	/**
	 * Restart.
	 */
	public void restart(){
		this.getInterpolator().resetInterpolator();
		
		this.triggerCountDown = this.getTriggerTime();
		this.hasStarted = false;
		AnimationManager.getInstance().addAnimation(this);
		AnimationManager.getInstance().addAnimationManagerListener(this);
		
//		System.out.println("Animation RESTARTED: " + this.getName());
//		fireAnimationEvent(new AnimationEvent(this, AnimationEvent.ANIMATION_STARTED, this, this.getTargetObject()));
	}
	
	
	/**
	 * Stop.
	 */
	public void stop(){
		AnimationManager.getInstance().removeAnimation(this);
		AnimationManager.getInstance().removeAnimationManagerListener(this);
		
//		System.out.println("Animation FINISHED: " + this.getName());
		//TODO fire?
//		fireAnimationEvent(new AnimationEvent(this, AnimationEvent.ANIMATION_ENDED, this, this.getTargetObject()));
	}
	
	/**
	 * Interface method of IAnimationManagerListener
	 * <br>used to update the anmation (interpolate) with a given timedelta.
	 * 
	 * @param ev the ev
	 */
	public void updateAnimation(AnimationUpdateEvent ev) {
		//System.out.println("animating " + a.getName());
		
			if (triggerTime != 0){//If trigger is set
				triggerCountDown -= ev.getDeltaTime(); //if !<0?
				
				if (triggerCountDown <= 0){ //if trigger abgelaufen
					//Interpoliere mit neuerm zeitdelta auf neuen wert
					interpolator.interpolate(ev.getDeltaTime());
					
					if (!interpolator.isFinished()){
						if (!hasStarted){
							hasStarted = true;
							this.fireAnimationEvent(new AnimationEvent(this, AnimationEvent.ANIMATION_STARTED, this, this.getTargetObject()));
						}else{
							this.fireAnimationEvent(new AnimationEvent(this, AnimationEvent.ANIMATION_UPDATED, this, this.getTargetObject()));
						}
					}else{
						//FIXME wenn gefinished, sollte der interpolator bei lastStepdelta und 0 zurückgeben, oder??
						this.fireAnimationEvent(new AnimationEvent(this, AnimationEvent.ANIMATION_ENDED, this, this.getTargetObject()));
						AnimationManager.getInstance().removeAnimation(this);
						AnimationManager.getInstance().removeAnimationManagerListener(this);
						this.triggerCountDown = this.getTriggerTime();
						
						if (this.isResetOnFinish()){
							this.getInterpolator().resetInterpolator();
							this.triggerCountDown = this.triggerTime;
							this.hasStarted = false;
						}
					} 
				}//if triggetcount not up, do nothing
			}else{//If no trigger is set
				interpolator.interpolate(ev.getDeltaTime());
				
				if (!this.interpolator.isFinished()){
					if (!this.hasStarted){ //Animation hasnt begun yet
						this.hasStarted = true;
						this.fireAnimationEvent(new AnimationEvent(this, AnimationEvent.ANIMATION_STARTED, this, this.getTargetObject()));
					}else{
						this.fireAnimationEvent(new AnimationEvent(this, AnimationEvent.ANIMATION_UPDATED, this, this.getTargetObject()));
					}
				}else{
					this.fireAnimationEvent(new AnimationEvent(this, AnimationEvent.ANIMATION_ENDED, this, this.getTargetObject()));
					AnimationManager.getInstance().removeAnimation(this);
					AnimationManager.getInstance().removeAnimationManagerListener(this);
					
					if (this.isResetOnFinish()){
						this.getInterpolator().resetInterpolator();
						this.triggerCountDown = this.triggerTime; //Reset triggercountdown
						this.hasStarted = false;
					}
				}//end else interpol !finished
			}//end else trigger not set
			
		
		
		/*
		if (this.isEnabled() && !this.interpolator.isFinished()){
			
			
			triggerCountDown -= ev.getDeltaTime();
			if (triggerCountDown <= 0){
				if (!this.hasStarted){ 
					this.hasStarted = true;
					interpolator.interpolate(ev.getDeltaTime());
					this.fireAnimationEvent(new AnimationEvent(this, AnimationEvent.ANIMATION_STARTED, this, this.getTargetObject()));
				}else{
					interpolator.interpolate(ev.getDeltaTime());
					this.fireAnimationEvent(new AnimationEvent(this, AnimationEvent.ANIMATION_STARTED, this, this.getTargetObject()));
					
					
				}
			}
			
			// Do the next interpolation iteration
			interpolator.interpolate(ev.getDeltaTime());
			
			if (!this.interpolator.isFinished()){
				if (!this.hasStarted){ //Animation hasnt begun yet
					
					triggerCountDown -= ev.getDeltaTime();
					if (triggerCountDown <= 0){
						this.hasStarted = true;
						this.fireAnimationEvent(new AnimationEvent(this, AnimationEvent.ANIMATION_STARTED, this, this.getTargetObject()));
					}
					
//					if (this.getTriggerTime() > 0){
//						this.setStartedTime(System.currentTimeMillis());
//						if (System.currentTimeMillis() - this.getStartedTime() >= this.getTriggerTime()){
//							
//						}
//					}else{
//						hasStarted = true;
//						this.fireAnimationEvent(new AnimationEvent(this, AnimationEvent.ANIMATION_STARTED, this, this.getTargetObject()));
//					}
					
				}else{
					//System.out.println("Animation UPDATED: " + a.getName());
					this.fireAnimationEvent(new AnimationEvent(this, AnimationEvent.ANIMATION_UPDATED, this, this.getTargetObject()));
				}
			}else{ //If interpolation has reached target
				this.fireAnimationEvent(new AnimationEvent(this, AnimationEvent.ANIMATION_ENDED, this, this.getTargetObject()));
				AnimationManager.getInstance().removeAnimation(this);
				AnimationManager.getInstance().removeAnimationManagerListener(this);
				this.triggerCountDown = this.getTriggerTime();
				
				this.setEnabled(false);
				
				if (this.isResetOnFinish()){
					this.getInterpolator().resetInterpolator();
					this.triggerCountDown = this.getTriggerTime();
					this.hasStarted = false;
				}
			}
		}
		*/
		
	}
	

	/**
	 * Gets the trigger time.
	 * 
	 * @return the trigger time
	 */
	public long getTriggerTime() {
		return triggerTime;
	}

	/**
	 * Sets the trigger time.
	 * 
	 * @param triggerTime the new trigger time
	 */
	public void setTriggerTime(long triggerTime) {
		this.triggerTime = triggerTime;
		this.triggerCountDown = triggerTime;
	}
	
	
	/**
	 * Fire animation event.
	 * 
	 * @param anev the anev
	 */
	private void fireAnimationEvent(AnimationEvent anev) {
		synchronized(animationListeners) {
			for (int i = 0; i < animationListeners.size(); i++) {
				IAnimationListener listener = (IAnimationListener)animationListeners.get(i);
				listener.processAnimationEvent(anev);
			}
		}
	}

	
	
	/**
	 * Checks if is reset on finish.
	 * 
	 * @return true, if is reset on finish
	 */
	public boolean isResetOnFinish() {
		return resetOnFinish;
	}

	/**
	 * Sets the reset on finish.
	 * 
	 * @param resetOnFinish the new reset on finish
	 */
	public void setResetOnFinish(boolean resetOnFinish) {
		this.resetOnFinish = resetOnFinish;
	}

	/**
	 * Adds the animation listener.
	 * 
	 * @param listener the listener
	 */
	public synchronized Animation addAnimationListener(IAnimationListener listener){
		if (!animationListeners.contains(listener)){
			animationListeners.add(listener);
		}
		return this;
	}
	
	/**
	 * Removes the animation listener.
	 * 
	 * @param listener the listener
	 */
	public synchronized void removeAnimationListener(IAnimationListener listener){
		if (animationListeners.contains(listener)){
			animationListeners.remove(listener);
		}
	}
	
	/**
	 * Removes the all animation listeners.
	 */
	public synchronized void removeAllAnimationListeners(){
		animationListeners.clear();
	}
	
	/**
	 * Gets the animation listeners.
	 * 
	 * @return the animation listeners
	 */
	public synchronized IAnimationListener[] getAnimationListeners(){
		return (IAnimationListener[])animationListeners.toArray(new IAnimationListener[this.animationListeners.size()]);
	}
	
	

	/**
	 * Gets the target object.
	 * 
	 * @return the target object
	 */
	public Object getTargetObject() {
		return targetObject;
	}


	/**
	 * Sets the target object.
	 * 
	 * @param targetObject the new target object
	 */
	public void setTargetObject(Object targetObject) {
		this.targetObject = targetObject;
	}

	

	/**
	 * Gets the interpolator.
	 * 
	 * @return the interpolator
	 */
	public Iinterpolator getInterpolator() {
		return interpolator;
	}


	/**
	 * Sets the interpolator.
	 * 
	 * @param interpolator the new interpolator
	 */
	public void setInterpolator(Iinterpolator interpolator) {
		this.interpolator = interpolator;
	}


	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}


	/**
	 * Sets the name.
	 * 
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	
	
	

}
