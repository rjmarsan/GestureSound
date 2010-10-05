/***********************************************************************
 * mt4j Copyright (c) 2008 - 2009, C.Ruff, Fraunhofer-Gesellschaft All rights reserved.
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
package org.mt4j.sceneManagement;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import javax.media.opengl.GL;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.mt4j.MTApplication;
import org.mt4j.components.MTCanvas;
import org.mt4j.input.inputProcessors.globalProcessors.AbstractGlobalInputProcessor;
import org.mt4j.input.inputProcessors.globalProcessors.InputRetargeter;
import org.mt4j.sceneManagement.transition.ITransition;
import org.mt4j.util.MT4jSettings;
import org.mt4j.util.MTColor;
import org.mt4j.util.camera.Icamera;
import org.mt4j.util.camera.MTCamera;
import org.mt4j.util.math.Tools3D;

import processing.core.PGraphics;

/**
 * A class representing a scene in a program or game.
 * It has its own main canvas and global input processors.
 * 
 * @author Christopher Ruff
 */
public abstract class AbstractScene implements Iscene {
	/** The Constant logger. */
	private static final Logger logger = Logger.getLogger(AbstractScene.class.getName());
	static{
//		logger.setLevel(Level.ERROR);
//		logger.setLevel(Level.WARN);
//		logger.setLevel(Level.DEBUG);
		logger.setLevel(Level.INFO);
		SimpleLayout l = new SimpleLayout();
		ConsoleAppender ca = new ConsoleAppender(l);
		logger.addAppender(ca);
	}
	
	/** The scene cam. */
	private Icamera sceneCam;
	
	/** The main canvas. */
	private MTCanvas mainCanvas;
	
	/** The mt application. */
	private MTApplication mtApplication;
	
	/** The name. */
	private String name;

	/** The pre draw actions. */
	private Deque<IPreDrawAction> preDrawActions;
	
	/** The clear color. */
	private MTColor clearColor;
	
	/** The gl clear color. */
	private MTColor glClearColor;
	
	/** The clear before draw. */
	private boolean clearBeforeDraw;
	
	/** The transition. */
	private ITransition transition;

	/**
	 * The Constructor.
	 * 
	 * @param mtApplication the mt application
	 * @param name the name
	 */
	public AbstractScene(MTApplication mtApplication, String name) {
		super();
		this.name = name;
		this.mtApplication = mtApplication;
		this.sceneCam = new MTCamera(mtApplication);
		this.sceneCam.update();
		this.sceneCam.setZoomMinDistance(60);
		this.mainCanvas = new MTCanvas(mtApplication, name + " - Main Canvas", sceneCam);
		
//		preDrawActions = new LinkedList<IPreDrawAction>();
		preDrawActions = new ArrayDeque<IPreDrawAction>();
		
		this.registerDefaultGlobalInputProcessors();
		
		this.clearBeforeDraw = true;
		this.setClearColor(new MTColor(0,0,0, 255));
	}
	
	
	/**
	 * Register default global input processors. Can be overridden for custom behaviour. 
	 */
	protected void registerDefaultGlobalInputProcessors(){
		InputRetargeter inputRetargeter = new InputRetargeter(this.getCanvas());
		inputRetargeter.addProcessorListener(this.getCanvas());
		this.registerGlobalInputProcessor(inputRetargeter);
		
//		this.registerGlobalInputProcessor(new InputRetargeter(this.getCanvas()));
	}

	
	/**
	 * Is invoked on a scene just before it is set to be the currently active scene.
	 */
	abstract public void init() ;
	
	/*
	public void update(long timeDelta) {
		this.getMainCanvas().update(timeDelta);
	}
	*/
	
	/*//Replaces by drawAndUpdate
	public void draw() {
//		this.getMainCanvas().draw();
		this.getMainCanvas().drawAndUpdateCanvas(0);
	}
	*/
	
	
	/* (non-Javadoc)
	 * @see mTouch.sceneManagement.Iscene#drawAndUpdate(long)
	 */
	public void drawAndUpdate(PGraphics graphics, long timeDelta){
		//Process preDrawActions
		synchronized (preDrawActions) {
			for (Iterator<IPreDrawAction> iter = preDrawActions.iterator(); iter.hasNext();) {
				IPreDrawAction action = (IPreDrawAction) iter.next();
				action.processAction();
				if (!action.isLoop()){
					iter.remove();
				}
			}
		}
		
		//Clear the background
		if (this.clearBeforeDraw){ 
			this.clear(graphics);
		}
		
		//Draw and update canvas
		this.getCanvas().drawAndUpdateCanvas(graphics, timeDelta);
	}
	
	

	protected void clear(PGraphics graphics){
		if (MT4jSettings.getInstance().isOpenGlMode()){
			GL gl = Tools3D.getGL(mtApplication);
			gl.glClearColor(this.glClearColor.getR(), this.glClearColor.getG(), this.glClearColor.getB(), this.glClearColor.getAlpha());
			gl.glClear(
					GL.GL_COLOR_BUFFER_BIT 
					| 
					GL.GL_DEPTH_BUFFER_BIT
					);
//			gl.glDepthMask(false);
//			gl.glDisable(GL.GL_DEPTH_TEST);
		}else{
			graphics.background(this.clearColor.getR(), this.clearColor.getG(), this.clearColor.getB(), this.clearColor.getAlpha());				
		}
	}
	
	/**
	 * Gets called on the current scene just before the current scene gets changed to another scene.
	 */
	abstract public void shutDown();
	
	
	/**
	 * Sets the clear color to use when the screen is cleared each frame
	 * before drawing.
	 * 
	 * @param clearColor the new clear color
	 */
	public void setClearColor(MTColor clearColor){
		this.clearColor = clearColor;
		this.glClearColor = new MTColor(this.clearColor.getR()/255f, this.clearColor.getG()/255f, this.clearColor.getB()/255f, this.clearColor.getAlpha()/255f);
	}
	
	
	/**
	 * Gets the clear color.
	 * 
	 * @return the clear color
	 */
	public MTColor getClearColor(){
		return this.clearColor;
	}
	
	
	/**
	 * Sets the scene to be cleared each frame or not.
	 * 
	 * @param clearScreen the new clear
	 */
	public void setClear(boolean clearScreen){
		this.clearBeforeDraw = clearScreen;
	}
	
	/**
	 * Checks if the scene is being cleared each frame.
	 * 
	 * @return true, if is clear
	 */
	public boolean isClear(){
		return this.clearBeforeDraw;
	}
	

	/* (non-Javadoc)
	 * @see mTouch.sceneManagement.Iscene#getMainCanvas()
	 */
	public MTCanvas getCanvas() {
		return mainCanvas;
	}

	
	/* (non-Javadoc)
	 * @see mTouch.sceneManagement.Iscene#getSceneCam()
	 */
	public Icamera getSceneCam() {
		return sceneCam;
	}

	/**
	 * Sets the scene cam. This is the camera which gets attached
	 * to the scene's canvas.
	 * 
	 * @param sceneCam the scene cam
	 */
	public void setSceneCam(Icamera sceneCam) {
		this.sceneCam = sceneCam; 
		this.getCanvas().attachCamera(sceneCam);
	}

	/**
	 * Gets the MT application instance.
	 * 
	 * @return the mT application
	 */
	public MTApplication getMTApplication(){
		return this.mtApplication;
	}

	/* (non-Javadoc)
	 * @see mTouch.sceneManagement.Iscene#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name the name
	 */
	public void setName(String name) {
		this.name = name;
	}

	
//	 * By default, the scene's canvas will be listening to the events of the registered
//	 * global input processor so the events will be delivered to the canvas object first.
	/**
	 * Registers a global input processor with the current scene.
	 * The global input processor will then recieve input events from all input sources 
	 * as long as this scene is active. 
	 * We can then add our own listeners to the global input processor.
	 * 
	 * @param processor the processor
	 */
	public void registerGlobalInputProcessor(AbstractGlobalInputProcessor processor){
		//Let the inputprocessor listen to the inputsources
		mtApplication.getInputManager().registerGlobalInputProcessor(this, processor);
		//Set this scenes main canvas to listen to the inputprocessor
//		processor.addProcessorListener(this.getCanvas()); //FIXME TESTWISE DISABLED
	}
	
	
	/**
	 * Unregisters a global input processor.
	 * 
	 * @param processor the processor
	 */
	public void unregisterGlobalInputProcessor(AbstractGlobalInputProcessor processor){
		mtApplication.getInputManager().unregisterGlobalInputProcessor(processor);
		processor.removeProcessorListener(this.getCanvas()); //FIXME can be removed now..?
	}
	
	/**
	 * Gets the global input processors.
	 * 
	 * @return the global input processors
	 */
	public AbstractGlobalInputProcessor[] getGlobalInputProcessors(){
		return mtApplication.getInputManager().getGlobalInputProcessors(this);
	}
	
	
	/**
	 * Registers an action to be processed before the next frame
	 * in the main drawing thread.
	 * 
	 * @param action the action
	 */
	public void registerPreDrawAction(IPreDrawAction action){
		synchronized (preDrawActions) {
			this.preDrawActions.addLast(action);
		}
	}

	
	/**
	 * Unregisters an PreDrawAction.
	 * 
	 * @param action the action
	 */
	public void unregisterPreDrawAction(IPreDrawAction action){
		synchronized (preDrawActions) {
			this.preDrawActions.remove(action);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.mt4j.sceneManagement.Iscene#getTransition()
	 */
	public ITransition getTransition(){
		return this.transition;
	}
	
	/**
	 * Sets the transition effect to use when a scene change takes
	 * place from this scene to another scene.
	 * 
	 * @param transition the new transition
	 */
	public void setTransition(ITransition transition){
		this.transition = transition;
	}
	
	
	/**
	 * Destroys the scene. Call this if the scene definitely isnt going to be used anymore.
	 * The scene can only be destroyed if it was added to the application and it isnt the currently
	 * active scene.
	 * - Destroys the scene's canvas
	 * - removes the global input listeners from the input sources
	 * 
	 * @return true, if successful
	 */
	public boolean destroy(){
		//If not already done, remove the scene from the mt application (only if not current scene)
		if (this.mtApplication.removeScene(this)){
			this.mtApplication.invokeLater(new Runnable() {
				public void run() {
					//Remove all global input processors of this scene from listening the the input sources
					AbstractGlobalInputProcessor[] inputProcessors = getGlobalInputProcessors();
					for (int i = 0; i < inputProcessors.length; i++) {
						AbstractGlobalInputProcessor abstractGlobalInputProcessor = inputProcessors[i];
						unregisterGlobalInputProcessor(abstractGlobalInputProcessor);
					}
				}
			});
			
			//Destroy the scene's canvas
			this.getCanvas().destroy();	
			
			preDrawActions.clear();
			
			logger.info("Destroyed scene: " + this.getName());
			return true;
		}else{
			//Try to destroy if removal of the scene failed because of a pending transition.
			this.mtApplication.destroySceneAfterTransition(this);
			logger.warn("Cant destroy currently active scene! (" + this.getName() + ") -> If scene in transition, trying to destroy afterwards.");
			return false;
		}
	}
}
