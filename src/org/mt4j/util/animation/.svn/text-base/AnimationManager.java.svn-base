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
import java.util.Iterator;

/**
 * The Class AnimationManager.
 * @author Christopher Ruff
 */
public class AnimationManager {
	
	/** The animations. */
	private ArrayList<Animation> animations;
	
	/** The instance. */
	private static AnimationManager instance = new AnimationManager();
	
	/** The animation mgr listener. */
	private ArrayList<IAnimationManagerListener> animationMgrListener;
	
	/**
	 * Instantiates a new animation manager.
	 */
	private AnimationManager(){
		animations = new ArrayList<Animation>();
		animationMgrListener = new ArrayList<IAnimationManagerListener>();
		
		animUpdateEvt = new AnimationUpdateEvent(this, 0);
	}
	
	/**
	 * Gets the single instance of AnimationManager.
	 * 
	 * @return single instance of AnimationManager
	 */
	static public AnimationManager getInstance(){
//		if (instance == null){
//			instance = new AnimationManager();
//			return instance;
//		}
//		else
			return instance;
	}
	
	
	/** The anim update evt. */
	private AnimationUpdateEvent animUpdateEvt;
	
	/**
	 * Update.
	 * 
	 * @param timeDelta the time delta
	 */
	public void update(long timeDelta){
//		AnimationUpdateEvent ev = new AnimationUpdateEvent(this, timeDelta);
		
		//INFO: animUpdatEvt is recycled everytime, so that no new object must be
		//allocated each frame! => the creation timestampt is wrong
		animUpdateEvt.setDeltaTime(timeDelta);
		fireAnimationUpdateEvent(animUpdateEvt);
		
		/*
		for (int i = 0; i < animations.size(); i++) {
			Animation a = animations.get(i);
			
			Iinterpolator interpolator = a.getInterpolator();
			//System.out.println("animating " + a.getName());
			
			if (a.isEnabled() && !interpolator.isFinished()){
				// Do the next interpolation iteration
				interpolator.interpolate(timeDelta);
				
				if (!interpolator.isFinished()){
					if (a.getStartedTime() == 0){ //Animation hasnt begun yet
						if (a.getTriggerTime() > 0){ //Check for trigger time up
							
						}else{
							a.setStartedTime(System.currentTimeMillis());
							a.postEvent(new AnimationEvent(this, AnimationEvent.ANIMATION_STARTED, a, a.getTargetObject()));
						}
					}else{
//						System.out.println("Animation UPDATED: " + a.getName());
						a.postEvent(new AnimationEvent(this, AnimationEvent.ANIMATION_UPDATED, a, a.getTargetObject()));
					}
				}else{
					a.postEvent(new AnimationEvent(this, AnimationEvent.ANIMATION_ENDED, a, a.getTargetObject()));
					this.removeAnimation(a);
					//TODO rather call smth like a.endAntionmation()
				}
			}
			
		}
		*/
	}
	
	
	/**
	 * Adds the animation.
	 * 
	 * @param a the a
	 */
	public void addAnimation(Animation a){
		if (!this.contains(a))
			animations.add(a);
	}

	/**
	 * Removes the animation.
	 * 
	 * @param a the a
	 */
	public void removeAnimation(Animation a){
		if (animations.contains(a))
			animations.remove(a);
	}

	/**
	 * Clear.
	 */
	public void clear() {
		Iterator<Animation> i = animations.iterator();
		while (i.hasNext()) {
			Animation a = (Animation)i.next();
//			a.stop();
			removeAnimationManagerListener(a);
		}
		animations.clear();
	}
	
	/**
	 * Gets the animations for target.
	 * 
	 * @param target the target
	 * 
	 * @return the animations for target
	 */
	public Animation[] getAnimationsForTarget(Object target){
		Iterator<Animation> i = animations.iterator();
		ArrayList<Animation> animations = new ArrayList<Animation>();
		while (i.hasNext()) {
			Animation a = (Animation)i.next();
			if (a.getTargetObject().equals(target)){
				animations.add(a);
			}
		}
		return (animations.toArray(new Animation[animations.size()]));
	}

	/**
	 * Contains.
	 * 
	 * @param arg0 the arg0
	 * 
	 * @return true, if successful
	 */
	public boolean contains(Animation arg0) {
		return animations.contains(arg0);
	}

	/**
	 * Size.
	 * 
	 * @return the int
	 */
	public int size() {
		return animations.size();
	}
	
	
	/**
	 * Fire animation update event.
	 * 
	 * @param up the up
	 */
	private void fireAnimationUpdateEvent(AnimationUpdateEvent up) {
//		synchronized(animationMgrListener) {
			for (int i = 0; i < animationMgrListener.size(); i++) {
				IAnimationManagerListener listener = (IAnimationManagerListener)animationMgrListener.get(i);
				listener.updateAnimation(up);
			}
//		}
	}
	

	/**
	 * Adds the animation manager listener.
	 * 
	 * @param listener the listener
	 */
	public synchronized void addAnimationManagerListener(IAnimationManagerListener listener){
		if (!animationMgrListener.contains(listener)){
			animationMgrListener.add(listener);
		}
		
	}
	
	/**
	 * Removes the animation manager listener.
	 * 
	 * @param listener the listener
	 */
	public synchronized void removeAnimationManagerListener(IAnimationManagerListener listener){
		if (animationMgrListener.contains(listener)){
			animationMgrListener.remove(listener);
		}
	}
	
	/**
	 * Removes the all animation listeners.
	 */
	public synchronized void removeAllAnimationListeners(){
		animationMgrListener.clear();
	}
	
	/**
	 * Gets the animation manager listeners.
	 * 
	 * @return the animation manager listeners
	 */
	public synchronized IAnimationManagerListener[] getAnimationManagerListeners(){
		return (IAnimationManagerListener[])animationMgrListener.toArray(new IAnimationManagerListener[this.animationMgrListener.size()]);
	}
	
}
