/***********************************************************************
 * mt4j Copyright (c) 2008 - 2010 Christopher Ruff, Fraunhofer-Gesellschaft All rights reserved.
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
package org.mt4j.sceneManagement.transition;

import org.mt4j.MTApplication;
import org.mt4j.components.TransformSpace;
import org.mt4j.components.visibleComponents.shapes.MTRectangle;
import org.mt4j.components.visibleComponents.widgets.MTSceneTexture;
import org.mt4j.sceneManagement.Iscene;
import org.mt4j.util.MTColor;
import org.mt4j.util.animation.Animation;
import org.mt4j.util.animation.AnimationEvent;
import org.mt4j.util.animation.IAnimationListener;
import org.mt4j.util.animation.MultiPurposeInterpolator;

/**
 * The Class FlipTransition.
 * 
 * @author Christopher Ruff
 */
public class FlipTransition extends AbstractTransition {
	
	/** The app. */
	private MTApplication app;
	
	/** The finished. */
	private boolean finished;
	
	/** The last scene. */
	private Iscene lastScene;
	
	/** The next scene. */
	private Iscene nextScene;
	
	/** The last scene window. */
	private MTSceneTexture lastSceneWindow;
	
	/** The next scene window. */
	private MTSceneTexture nextSceneWindow;
	
	/** The anim2. */
	private Animation anim2;
	
	/** The anim. */
	private Animation anim;
	
	/** The duration. */
	private long duration;
	
	/** The last scene rectangle. */
	private MTRectangle lastSceneRectangle;
	
	/** The next scene rectangle. */
	private MTRectangle nextSceneRectangle;
	
	
	/**
	 * Instantiates a new flip transition.
	 * 
	 * @param mtApplication the mt application
	 */
	public FlipTransition(MTApplication mtApplication) {
		this(mtApplication, 2000);
	}
	
	
	/**
	 * Instantiates a new flip transition.
	 * 
	 * @param mtApplication the mt application
	 * @param duration the duration
	 */
	public FlipTransition(MTApplication mtApplication, long duration) {
		super(mtApplication, "Flip Transition");
		this.app = mtApplication;
		this.duration = duration;
		this.finished = true;
		
		anim2 = new Animation("Flip animation 2", new MultiPurposeInterpolator(0,90, this.duration/2f, 0, 0.5f, 1) , this).addAnimationListener(new IAnimationListener(){
			//@Override
			public void processAnimationEvent(AnimationEvent ae) {
				switch (ae.getId()) {
				case AnimationEvent.ANIMATION_STARTED:
				case AnimationEvent.ANIMATION_UPDATED:
//					nextSceneWindow.rotateYGlobal(lastSceneWindow.getCenterPointGlobal(), ae.getAnimation().getInterpolator().getCurrentStepDelta());
					nextSceneRectangle.rotateYGlobal(lastSceneWindow.getCenterPointGlobal(), ae.getAnimation().getInterpolator().getCurrentStepDelta());
					break;
				case AnimationEvent.ANIMATION_ENDED:
					nextSceneRectangle.rotateYGlobal(lastSceneWindow.getCenterPointGlobal(), ae.getAnimation().getInterpolator().getCurrentStepDelta());
					finished = true;
					break;
				default:
					break;
				}
			}});
		anim2.setResetOnFinish(true);
		
        anim = new Animation("Flip animation 1", new MultiPurposeInterpolator(0,90, this.duration/2f, 0.5f, 1, 1) , this).addAnimationListener(new IAnimationListener(){
        	//@Override
        	public void processAnimationEvent(AnimationEvent ae) {
        		switch (ae.getId()) {
				case AnimationEvent.ANIMATION_STARTED:
				case AnimationEvent.ANIMATION_UPDATED:
//					lastSceneWindow.rotateYGlobal(lastSceneWindow.getCenterPointGlobal(), ae.getAnimation().getInterpolator().getCurrentStepDelta());
					lastSceneRectangle.rotateYGlobal(lastSceneWindow.getCenterPointGlobal(), ae.getAnimation().getInterpolator().getCurrentStepDelta());
					break;
				case AnimationEvent.ANIMATION_ENDED:
					lastSceneRectangle.rotateYGlobal(lastSceneWindow.getCenterPointGlobal(), ae.getAnimation().getInterpolator().getCurrentStepDelta());
//					nextSceneWindow.setVisible(true);
//					lastSceneWindow.setVisible(false);
					lastSceneRectangle.setVisible(false);
					nextSceneRectangle.setVisible(true);
					anim2.start();
					break;
				default:
					break;
				}
        	}});
       anim.setResetOnFinish(true);
		
	}


	/* (non-Javadoc)
	 * @see org.mt4j.sceneManagement.transition.ITransition#isFinished()
	 */
	public boolean isFinished() {
		return finished;
	}

	
	
	/* (non-Javadoc)
	 * @see org.mt4j.sceneManagement.transition.ITransition#setup(org.mt4j.sceneManagement.Iscene, org.mt4j.sceneManagement.Iscene)
	 */
	public void setup(Iscene lastScenee, Iscene nextScenee) {
		this.lastScene = lastScenee;
		this.nextScene = nextScenee;
		finished = false;
		
		//Disable the scene's global input processors. We will be redirecting the input
		//from the current scene to the window scene
		app.getInputManager().disableGlobalInputProcessors(lastScene);
		app.getInputManager().disableGlobalInputProcessors(nextScene);
		
		app.invokeLater(new Runnable() {
			public void run() {
				lastSceneWindow = new MTSceneTexture(app,0, 0, lastScene);
				nextSceneWindow = new MTSceneTexture(app,0, 0, nextScene);

				lastSceneRectangle = new MTRectangle(0,0, app.width, app.height, app);
				lastSceneRectangle.setGeometryInfo(lastSceneWindow.getGeometryInfo());
				lastSceneRectangle.setTexture(lastSceneWindow.getTexture());
				lastSceneRectangle.setStrokeColor(new MTColor(0,0,0,255));

				nextSceneRectangle = new MTRectangle(0,0, app.width, app.height, app);
				nextSceneRectangle.setGeometryInfo(nextSceneWindow.getGeometryInfo());
				nextSceneRectangle.setTexture(nextSceneWindow.getTexture());
				nextSceneRectangle.setStrokeColor(new MTColor(0,0,0,255));

				getCanvas().addChild(lastSceneRectangle);
				getCanvas().addChild(nextSceneRectangle);

				nextSceneRectangle.rotateY(nextSceneRectangle.getCenterPointGlobal(), 270, TransformSpace.GLOBAL);
				nextSceneRectangle.setVisible(false);

				//Draw scenes into texture once!
				lastSceneWindow.drawComponent(app.g);
				nextSceneWindow.drawComponent(app.g);
			}
		});

//		this.getCanvas().addChild(this.lastSceneWindow);
//		this.getCanvas().addChild(this.nextSceneWindow);
//		this.nextSceneWindow.rotateY(this.nextSceneWindow.getCenterPointGlobal(), 270, TransformSpace.GLOBAL);
//		this.nextSceneWindow.setVisible(false);
		anim.start();
		
		//TODO wihtout FBO copyPixels
	}
	
	
	/* (non-Javadoc)
	 * @see org.mt4j.sceneManagement.AbstractScene#shutDown()
	 */
	@Override
	public void shutDown() {
		finished = true;
		this.lastScene = null;
		this.nextScene = null;
		
		this.lastSceneWindow.destroy();
		this.nextSceneWindow.destroy();
		lastSceneRectangle.destroy();
		nextSceneRectangle.destroy();
	}
	
	
}
