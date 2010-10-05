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

package org.mt4j;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.FileInputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Properties;

import javax.media.opengl.GL;
import javax.swing.ImageIcon;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.mt4j.input.InputManager;
import org.mt4j.input.inputData.AbstractCursorInputEvt;
import org.mt4j.input.inputData.ActiveCursorPool;
import org.mt4j.input.inputData.InputCursor;
import org.mt4j.input.inputProcessors.globalProcessors.AbstractGlobalInputProcessor;
import org.mt4j.input.inputSources.AbstractInputSource;
import org.mt4j.sceneManagement.ISceneChangeListener;
import org.mt4j.sceneManagement.Iscene;
import org.mt4j.sceneManagement.SceneChangeEvent;
import org.mt4j.sceneManagement.transition.ITransition;
import org.mt4j.util.MT4jSettings;
import org.mt4j.util.animation.AnimationManager;
import org.mt4j.util.math.Tools3D;
import org.mt4j.util.opengl.GLFBO;

import processing.core.PApplet;



/**
 * Use this class to create a new multitouch application.
 * <br>The best way to create your application would be to extend this class and
 * put the <code>main</code> method into that class.
 * In the <code>main</code> method call the <code>initialize()</code> method.
 * Then override the <code>startUp()</code> method which is called
 * automatically after the initialize method. The <code>startUp()</code> method can be used to
 * create your scenes (extend the <code>AbstractScene</code> class) and add them to
 * the application by calling <code>addScene</code> method.
 * 
 * <p>Internally, the main method of processings PApplet class is called with the class name
 * of the extended PApplet class as an argument. The PApplet class then instantiates the given
 * class and calls its setup() and then repeatedly its run() method.
 * 
 * @author Christopher Ruff
 */
public class MTApplication extends PApplet {
	
	/** The Constant logger. */
	private static final Logger logger = Logger.getLogger(MTApplication.class.getName());
	static{
//		logger.setLevel(Level.ERROR);
//		logger.setLevel(Level.WARN);
//		logger.setLevel(Level.DEBUG);
		logger.setLevel(Level.INFO);
		SimpleLayout l = new SimpleLayout();
		ConsoleAppender ca = new ConsoleAppender(l);
		logger.addAppender(ca);
	}
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The scene change locked. */
	private boolean sceneChangeLocked;

//	private static MTApplication mtApp = null;
	
	/** The scene list. */
	private List<Iscene> sceneList;
	
	/** The current scene. */
	private Iscene currentScene;
	
	/** The animation mgr. */
	private AnimationManager animMgr;
	
	/** The time last frame. */
	private long timeLastFrame ;
	
	/** The already run. */
	private boolean alreadyRun;
	
	/** The v sync. */
	private boolean vSync = false;
	
	/** The input manager. */
	private InputManager inputManager;
	
	/** The scene changed listeners. */
	private List<ISceneChangeListener> sceneChangedListeners;
	
	/** The invoke later actions. */
	private Deque<Runnable> invokeLaterActions;
	
	/** The scene stack. */
	private ArrayDeque<Iscene> sceneStack;
	
	
//	private static boolean fullscreen;
	/*
	public static void main(String[] args){
//		MTApplication app  = new MTApplication();
		
		PApplet.main(new String[] {
//				   "--present", 
//				   "--exclusive",
				   "--bgcolor=#000000", 
				   "--hide-stop",
				   "org.mt4j.MTApplication"
				   }
				   ); 
	}
	@Override
	public void setup(){
		size(800,600, OPENGL); //TODO REMOVE
		logger.debug("Setup");
		System.out.println("Setup called");
		
		smooth();
		hint(ENABLE_OPENGL_2X_SMOOTH );
		smooth();
		noSmooth();
		
		background(0);
		
		GL gl = Tools3D.getGL(this);
//		 gl.glEnable(GL.GL_MULTISAMPLE);
//	     gl.glEnable(GL.GL_MULTISAMPLE_EXT);
	}
	@Override
	public void draw(){
//		background(255);
		
		fill(250,0,0,255);
		stroke(250,0,0,255);
		line(0,10, 280,20);
		
		GL gl = Tools3D.beginGL(this);
//		GL gl =  ((PGraphicsOpenGL)this.g).beginGL();
//		gl.glEnable(GL.GL_LINE_SMOOTH );  
		gl.glDisable(GL.GL_LINE_SMOOTH );  
//		gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_NICEST);  
		// Enable Blending 
		gl.glEnable(GL.GL_BLEND);  
		// Specifies pixel arithmetic  
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA); 
		gl.glLineWidth(1);
		gl.glColor4d(0.0, 0.0, 0.0, 1);
		
		gl.glBegin(GL.GL_LINE_STRIP);
		gl.glVertex3d(0, 20, 0);
		gl.glVertex3d(280, 30, 0);
		gl.glEnd();
		
		gl.glBegin(GL.GL_LINE_STRIP);
		gl.glVertex3d(0, 20, 0);
		gl.glVertex3d(711, 230, 0);
		gl.glVertex3d(200, 300, 0);
		gl.glVertex3d(100, 330, 0);
		gl.glEnd();
//		((PGraphicsOpenGL)this.g).endGL();
		
		Tools3D.endGL(this);
		
		if (this.mousePressed){
			fill(150);
			rect(mouseX, mouseY, 10,10);
		}
	}
	*/
	
	

	/**
	 * Dont instiatiate this class directly!
	 * It gets instantiated by the PApplet class via
	 * java reflection.
	 */
	public MTApplication(){
		sceneList 		= new ArrayList<Iscene>();
		currentScene 	= null;
		animMgr 		= AnimationManager.getInstance();
		alreadyRun 		= false;
		
		sceneChangedListeners = new ArrayList<ISceneChangeListener>();
		invokeLaterActions = new ArrayDeque<Runnable>();
		sceneStack = new ArrayDeque<Iscene>();
		
		pContext = this;
		
		sceneChangeLocked = false;
	}
	
	
	
	/**
	 * Initializes the processings settings.
	 * Call this method in your main method prior to anything else!
	 */
	public static void initialize(){
		 logger.debug(new CurrentClassGetter().getClassName() + " is the class instatiated by PApplet class.");
		 
		 boolean fullscreenExclusiveMode = true;
		 int display = 1;
		 
			//Load some properties from Settings.txt file
			Properties properties = new Properties();
		    try {
		        properties.load(new FileInputStream(MT4jSettings.getInstance().getDefaultSettingsPath() + "Settings.txt"));
			    MT4jSettings.fullscreen = Boolean.parseBoolean(properties.getProperty("Fullscreen", "false"));
			    //Use java's fullscreen exclusive mode (real fullscreen) or just use an undecorated window at fullscreen size 
			    fullscreenExclusiveMode = Boolean.parseBoolean(properties.getProperty("FullscreenExclusive", "true"));
			    //Which display to use for fullscreen
			    display = Integer.parseInt(properties.getProperty("Display", "1"));
		    } catch (Exception e) {
		    	logger.error("Error while loading Settings.txt file. Using defaults.");
		    }
		    
		    // Launch processing PApplet main() function
		    if (MT4jSettings.getInstance().isFullscreen()){
		    	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		    	MT4jSettings.getInstance().setScreenWidth(screenSize.width);
		    	MT4jSettings.getInstance().setScreenHeight(screenSize.height);
		    	if (fullscreenExclusiveMode){
		    		PApplet.main(new String[] {
							   "--display=" + display,
							   "--present", 
							   "--exclusive", 
							   "--bgcolor=#000000", 
							   "--hide-stop",
							   new CurrentClassGetter().getClassName()}
							   ); 
		    	}else{
		    		PApplet.main(new String[] {
							   "--display=" + display,
							   "--present", 
							   "--bgcolor=#000000", 
							   "--hide-stop",
							   new CurrentClassGetter().getClassName()}
							   ); 
		    	}
		    }else{
		    	PApplet.main(new String[] { 
		    			 "--display=" + display,
						   new CurrentClassGetter().getClassName() }); 
		    }
	}
	
	
	/** The p context. */
	private PApplet pContext;

	private ImageIcon mt4jIcon;

	//FIXME REMOVE??
	/**
	 * Sets the p5 context.
	 * 
	 * @param pa the new p5 context
	 */
	public void setP5Context(PApplet pa){
		this.pContext = pa;
	}
	
	/**
	 * ***********************************************************
	 * Processings setup. this is called once when the applet is started
	 * Used to define some initial settings
	 * **********************************************************.
	 */
	@Override
	public void setup(){
		//TOGGLES ALWAYS ON TOP MODE
		//this.frame.setAlwaysOnTop(true);
	
		logger.debug("-> setup called");
		
		//Load some properties from Settings.txt file
		Properties properties = new Properties();
	    try {
	        properties.load(new FileInputStream(MT4jSettings.getInstance().getDefaultSettingsPath() + "Settings.txt"));
	        //FIXME at fullscreen really use the screen dimension? -> we need to set the native resoultion ourselves!
	        //so we can have a lower fullscreen resolution than the screen dimensions
	        if (!MT4jSettings.getInstance().isFullscreen()){
		        MT4jSettings.getInstance().setScreenWidth(Integer.parseInt(properties.getProperty("DisplayWidth", "1024")));
			    MT4jSettings.getInstance().setScreenHeight(Integer.parseInt(properties.getProperty("DisplayHeight", "768")));
	        }
		    MT4jSettings.getInstance().setMaxFrameRate(Integer.parseInt(properties.getProperty("MaximumFrameRate", "60")));
		    MT4jSettings.getInstance().setRendererMode(Integer.parseInt(properties.getProperty("Renderer", new Integer(MT4jSettings.P3D_MODE).toString())));
		    MT4jSettings.getInstance().setNumSamples((Integer.parseInt(properties.getProperty("OpenGLAntialiasing", new Integer(0).toString()))));
		    
		    vSync = Boolean.parseBoolean(properties.getProperty("Vertical_sync", "false"));
		    //Set frametitle
		    String frameTitle = properties.getProperty("Frametitle", "MT-Application");
		    MT4jSettings.getInstance().setFrameTitle(frameTitle);
	    } catch (Exception e) {
	    	logger.error("Error while loading Settings.txt file. Using defaults. (" + e.getMessage() + ")");
	    }
		
		// Applet size - size() must be the first command in setup() method
		if (MT4jSettings.getInstance().getRendererMode() == MT4jSettings.OPENGL_MODE)
			this.size(MT4jSettings.getInstance().getScreenWidth(),MT4jSettings.getInstance().getScreenHeight(), PApplet.OPENGL); 
		else if (MT4jSettings.getInstance().getRendererMode() == MT4jSettings.P3D_MODE)
			this.size(MT4jSettings.getInstance().getScreenWidth(),MT4jSettings.getInstance().getScreenHeight(), PApplet.P3D);
	    
//	    pContext.hint( PApplet.ENABLE_OPENGL_4X_SMOOTH );  // ENABLES OPENGL EXTRA SMOOTHING -> DOESENT GET CONSISTENT RESULTS ON ALL MACHINES! DISABLE WHEN PROBLEMS OCCUR!
		//hint(ENABLE_DEPTH_SORT); // Enable primitive z-sorting of triangles and lines in P3D and OPENGL. This can slow performance considerably, and the algorithm is not yet perfect.
		//hint(DISABLE_ERROR_REPORT); // Speeds up the OPENGL renderer setting by not checking for errors while running.
		//hint(ENABLE_ACCURATE_TEXTURES); //Enables better texture accuracy for the P3D renderer. This option will do a better job of dealing with textures in perspective.  
		
//		hint(ENABLE_OPENGL_4X_SMOOTH);
		
		//Set icon image
		try {
			this.mt4jIcon = new ImageIcon(MT4jSettings.getInstance().getDefaultImagesPath() + 
					"MT4j.gif");
			this.frame.setIconImage(mt4jIcon.getImage());  
		}catch (Exception e){
			e.printStackTrace();
		}
		
		logger.info("MT4j window dimensions: \"" + MT4jSettings.getInstance().getScreenWidth() + " X " +  MT4jSettings.getInstance().getScreenHeight() + "\"");
		
//		//Set background color
//	    pContext.background(MT4jSettings.getInstance().getBackgroundClearColor());
		pContext.background(150);
		
		//Set the framerate
	    pContext.frameRate(MT4jSettings.getInstance().getMaxFrameRate());
	    logger.info("Maximum framerate: \"" + MT4jSettings.getInstance().getMaxFrameRate() + "\"");
		
		MT4jSettings.getInstance().setProgramStartTime(System.currentTimeMillis());
		
		//Set the window frame's title
		frame.setTitle(MT4jSettings.getInstance().getFrameTitle()); 
		
		//Apply some opengl settings like V-Syncing or multi-Sampling
		this.applyOpenGLStartSettings();
		
		//Create a new inputsourcePool
		this.setInputManager(new InputManager(this));
		
		//Call startup at the end of setup(). Should be overridden in extending classes
		this.startUp();

		/*
		* Resizable Window test
		* Problems:
		* - all textures, shaders etc get destroyed because a new gl context is created
		* - cursor coordiantes are calculated wrong? we prolly have to update Papplet width/height 
		frame.setResizable(true);
		frame.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				if(e.getSource() == frame) { 
					frame.setSize(frame.getWidth(), minHeight); 
				}
			}
		} );
		*/ 
	}
	
	/**
	 * Apply open gl start settings.
	 */
	private void applyOpenGLStartSettings(){
		//TODO pa.smooth() / pa.noSmooth() verändert auch line_smooth!
		//für test ob multisampling lines ohne Line_smooth okay rendered müssen
		//sicherheitshalber auch die pa.smoot() etc abgefangen werden und line_smooth immer disabled sein!
		
		//TODO check line drawing and abstractvisible at stencil in this context (line_smooth)
		
	    //TODO 
		// - if multisampling enabled dont do line smoothing at all
		// - OR: disable multisampling each time before doing line_smoothing! (better but expensive?) 
		//   -> info: disabling multisampling isnt possible at runtime..

	    // - or disable mutisample before drawing with line_smooth!
		//TOOD dont use lines to smooth some objects then (fonts, etc)
	    if (MT4jSettings.getInstance().isOpenGlMode() ){
	    	GL gl = Tools3D.getGL(this);
	    	
	    	logger.info("OpenGL Version: \"" + gl.glGetString(GL.GL_VERSION) + "\"");
	    	logger.info("Non power of two texture sizes allowed: \"" + Tools3D.supportsNonPowerOfTwoTexture(this) + "\"");
	    	logger.info("OpenGL Framebuffer Object Extension available: \"" + GLFBO.isSupported(this) + "\"");
	    	
			//Set VSyncing on -> to avoid tearing 
			//-> check if gfx card settings allow apps to set it!
			//-> Use with caution! only use with fps rate == monitor Hz!
			//and fps never drop below Hz! -> else choppy!
			//-> only works with opengl!
	    	Tools3D.setVSyncing(this, vSync);
			logger.info("Vertical Sync enabled: \"" + vSync + "\"");
	    	
	    	if ( MT4jSettings.getInstance().isMultiSampling()){
	    		gl.glEnable(GL.GL_MULTISAMPLE);
//	    		gl.glDisable(GL.GL_MULTISAMPLE);
	    		logger.info("OpenGL multi-sampling enabled.");
	    	}
	    	gl.glEnable(GL.GL_LINE_SMOOTH);
//	    	gl.glDisable(GL.GL_LINE_SMOOTH);
	    }
	}
	
	/**
	 * ********************************************************************************************
	 * Processings draw() gets called repeatedly by processings PApplet Class - unless noloop() is called
	 * ********************************************************************************************.
	 */
	@Override
	public void draw(){
		this.runApplication();
	}
	
	
	/**
	 * Is called at the end of the setup() method.
	 * <br>Override this method in your extended MTApplication class!
	 */
	public void startUp(){};
	
	
	
	/**
	 * Main run loop.
	 * <li>Updates the time passed since the last time drawn.
	 * <li>Updates any animations with the new time delta.
	 * <li>Updates and draws the current scene.
	 * <li>Updates and draws the current scene transitions.
	 */
	private void runApplication(){ 
//		/*
		//Use nanoTime
		if (!alreadyRun){
			alreadyRun = true;
			timeLastFrame = System.nanoTime();
		}
		long nanos = System.nanoTime();
		long timeDelta = (nanos - timeLastFrame) / 1000000L;
		timeLastFrame = nanos;
//		*/
		
		/*
		//Use currentTimeMillis
		if (!alreadyRun){
			alreadyRun = true;
			timeLastFrame = System.currentTimeMillis();
		}
		long millis = System.currentTimeMillis();
		long timeDelta = millis - timeLastFrame;
		timeLastFrame = millis;
		*/
		
//		System.out.println("TimeDelta: " + timeDelta);
		
		//Run invoke later actions
		synchronized (invokeLaterActions) {
			while (!invokeLaterActions.isEmpty()){
				invokeLaterActions.pollFirst().run();
			}
		}
		
		//Update animation manager
		animMgr.update(timeDelta);
		
//		/*
		//Handle scene transitions
		if (this.pendingTransition != null){
			//Run the transition
			this.pendingTransition.transition.drawAndUpdate(this.g, timeDelta);
			
			if (this.pendingTransition.transition.isFinished()){
				this.pendingTransition.transition.shutDown();
				this.doSceneChange(this.getCurrentScene(), this.pendingTransition.nextScene);
				this.pendingTransition = null;
			}
		}else{
			//Draw the current scene
			this.getCurrentScene().drawAndUpdate(this.g, timeDelta);
		}
//		 */
		
		/*
		//Update scene
		sceneMgr.updateCurrentScene(timeDelta);
		//Draw scene
		sceneMgr.drawCurrentScene();
		 */
	}

	
	
	/**
	 * Invokes the specified runnable at the beginning the next rendering loop in the rendering thread.
	 * This is especially useful for executing opengl commands from another thread - which would lead to errors
	 * if not synchronized with the rendering thread.
	 * 
	 * @param runnable the runnable
	 */
	public void invokeLater(Runnable runnable){
		synchronized (invokeLaterActions) {
			invokeLaterActions.addLast(runnable);	
		}
	}
	
	
	/**
	 * Checks which scene is on top of the scene stack at the moment.
	 * If no scene has been pushed on the stack, null is returned.
	 * 
	 * @return the iscene
	 */
	public Iscene peekScene(){
		return sceneStack.peek();
	}
	
	public int getSceneStackCount(){
		return sceneStack.size();
	}
	
	/**
	 * Pushes the current scene on the scene stack.
	 */
	public void pushScene(){
		if (getCurrentScene() == null){
			logger.debug("Scene stack is empty! No scene to put on the stack!");
		}else{
			logger.debug("Putting scene: " + getCurrentScene().getName() +  " on the stack.");
			sceneStack.offerFirst(getCurrentScene());
		}
	}
	
	
	/**
	 * Pops the scene thats currently ontop of the scene stack and changes back to it. 
	 * If the stack is empty no error is thrown and no scene change will happen.
	 */
	public boolean popScene(){
//		Iscene stackScene = sceneStack.pollFirst();
		
		Iscene stackScene = sceneStack.peek();
		if (stackScene != null){
			logger.debug("Popping scene: " + stackScene.getName() +  " back from the stack.");
			boolean changed = this.changeScene(stackScene);
			if (changed){
				sceneStack.pollFirst();
				return true;
			}else{
				return false;
			}
		}else{
			logger.warn("Scene stack is empty! No scene to pop from the stack!");
			return false;
		}
	}
	
	
	
	private boolean inDoSceneChange = false;
	private TransitionInfo pendingTransition;
	
	/**
	 * The Class TransitionInfo. Holding info about a scene change transition.
	 * @author Christopher Ruff
	 */
	private class TransitionInfo{
		ITransition transition;
		Iscene lastScene;
		Iscene nextScene;
		boolean destroyLastSceneAfterTransition = false; 
		public TransitionInfo(ITransition transition, Iscene lastScene, Iscene nextScene){
			this.transition = transition;
			this.lastScene = lastScene;
			this.nextScene = nextScene;
		}
	}
	
	
	/**
	 * Initiates the scene change. Checks if the old scene has a transition
	 * and sets it to be used in the main loop.
	 * 
	 * @param oldScene the old scene
	 * @param newScene the new scene
	 */
	private boolean initiateSceneChange(Iscene oldScene, Iscene newScene){
		//FIXME TEST!
		if (oldScene.equals(newScene)){
			logger.error("Trying to change from and to the same scene.");
			return false;
		}
		
		//Lock scene changes to only 1 at a time. At sending the bridge events during the 
		//scene change, it could occur that a scene change could be triggered again which we prevent
		if (!sceneChangeLocked){
			sceneChangeLocked = true;
			
			Iscene lastScene = this.getCurrentScene();
			
			//Remove pending animations // 
			//FIXME problemes, if new animations are defined in a scenes constructor, they get removed here..
			//AnimationManager.getInstance().clear();
			
			//Flush events so that enqueued input ended get sent to the last scene
			//(Problem: they have been removed from active cursor pool already so they dont
			//appear there and no ended and started evts are sent to the scenes!
			//IF input started or updated should be flushed with this they should appear in active
			//cursor list after that and be sended the right events
			//- maybe only flush input_ended?
			Collection<AbstractInputSource> sources = getInputManager().getRegisteredInputSources();
			for (AbstractInputSource abstractInputSource : sources) {
				abstractInputSource.flushEvents();
			}
			
			//Check which cursors are still active and clone their last evt as INPUT_ENDED
			//so the scene can complete its state (i.e. buttons are be released etc)
			this.sendEndedEvents(lastScene); 

			//Disable the last scene's global input processors
			this.getInputManager().disableGlobalInputProcessors(lastScene);
			
//			/*
			if (lastScene.getTransition() != null){
				ITransition t = lastScene.getTransition();
				this.pendingTransition = new TransitionInfo(t, lastScene, newScene);
				t.init();
				t.setup(lastScene, newScene);
				return true;
			}else{
				return this.doSceneChange(lastScene, newScene);
			}
//			 */
			//doSceneChange(oldScene, newScene);
		}else{
			logger.debug("Couldnt change scene -> Change is locked from another scene change.");
			return false;
		}
	}
	
	
	/**
	 * Does the scene change after the transition (if existing) is completed.
	 * @param oldScene the old scene
	 * @param newScene the new scene
	 */
	private boolean doSceneChange(Iscene oldScene, Iscene newScene){
		if (sceneChangeLocked && !inDoSceneChange){
			inDoSceneChange = true;
			
			//Maybe show loading progress for newScenne.Init first?
			oldScene.shutDown();
			
			//Initialize new Scene
			newScene.init();

			//Enable input Processors previously registered with that scene
			this.getInputManager().enableGlobalInputProcessors(newScene);

			//Check which cursors are active and clone their last evt as INPUT_DETECTED
			//so the scene doesent get INPUT_UPDATED without the start events
			this.sendStartedEvents(newScene); 

			//Set new current scene
			this.currentScene = newScene;
			
			//FIXME TEST -> Make it possible to destroy scenes after a transition
			//(During a transition the old scene cant be removed or destroyed because
			//its still the current scene!)
			if (pendingTransition != null){
				if (pendingTransition.destroyLastSceneAfterTransition){
					logger.debug("Destroying scene: " + pendingTransition.lastScene.getName() + " after the transition.");
					pendingTransition.lastScene.destroy();
				}
			}

			if (!this.sceneChangedListeners.isEmpty()){
				this.fireSceneChangeEvent(new SceneChangeEvent(this, oldScene, newScene));
			}
			logger.debug("Scene changed from: '" + oldScene + "' to: '" + newScene + "'");
			sceneChangeLocked = false;
			
			inDoSceneChange = false;
			return true;
		}else{
			return false;
		}
	}

	
	
	/**
	 * Changes the scene to the specified scene.
	 * <p>NOTE: This is not threadsafe while using OpenGL mode. If in openGL mode make,
	 * sure to call this only from the same thread. If running in a different thread,
	 * execute the scene change using the <code>invokeLater(Runnable runnable)</code> method 
	 * of the MTApplication instance!
	 * <p>NOTE: If the scene is not already added to the application by invoking <code>addScene()</code>, the scene
	 * is automatically added to the mtapplication.
	 * 
	 * @param newScene the new scene
	 */
	public synchronized boolean changeScene(Iscene newScene){
		if (!this.sceneList.contains(newScene)){
			this.addScene(newScene);
		}
		return this.initiateSceneChange(this.getCurrentScene(), newScene);
	}

	
	/**
	 * Checks which cursors are active during the scene change and
	 * sends input_ended events of the active cursors to last scene's global input processors 
	 * so actions in the last scene can be completed correctly.
	 * This means that one cursor can have more than one input_ended and input_started event
	 * in its event list!
	 * 
	 * @param lastScene the last scene
	 * @param newScene the new scene
	 */
	private void sendEndedEvents(Iscene lastScene){
		logger.debug("Sending INPUT_ENDED events to the last scene, Active motions: " + ActiveCursorPool.getInstance().getActiveCursorCount());
		InputCursor[] activeCursors = ActiveCursorPool.getInstance().getActiveCursors();
		for (int i = 0; i < activeCursors.length; i++) {
			InputCursor inputCursor = activeCursors[i];
			if (inputCursor.getCurrentEvent() != null){
				AbstractCursorInputEvt lastEvt = inputCursor.getCurrentEvent();
				if (lastEvt.getId() != AbstractCursorInputEvt.INPUT_ENDED){
					try {
						AbstractCursorInputEvt endedEvt = (AbstractCursorInputEvt) lastEvt.clone();
						endedEvt.setId(AbstractCursorInputEvt.INPUT_ENDED);
						endedEvt.preFire();
						
						this.sendEvtToSceneProcessors(lastScene, endedEvt);
						logger.debug("Sending INPUT_ENDED evt to scene: " + lastScene.getName() + " Cursor: " + endedEvt.getCursor());
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	
	/**
	 * Checks which cursors are active during the scene change and
	 * sends input_started to the new scene's global input processors so actions in the
	 * last scene can be completed correctly.
	 * This means that one cursor can have more than one input_ended and input_started event
	 * in its event list!
	 * 
	 * @param lastScene the last scene
	 * @param newScene the new scene
	 */
	private void sendStartedEvents(Iscene newScene){
		logger.debug("Sending INPUT_DETECTED events to the new scene, Active motions: " + ActiveCursorPool.getInstance().getActiveCursorCount());
		InputCursor[] activeCursors = ActiveCursorPool.getInstance().getActiveCursors();
		for (int i = 0; i < activeCursors.length; i++) {
			InputCursor inputCursor = activeCursors[i];
			if (inputCursor.getCurrentEvent() != null){
				//PROBLEM: if in lastscene last event in cursor was input_started enqueued
				//but not added to cursor yet,
				//shall we send it again in new scene? -> will input_started be sent twice?
				//- what if input started was enqueued during transition and not sent to any scene 
				AbstractCursorInputEvt lastEvt = inputCursor.getCurrentEvent();
				/*
				if (//lastEvt.getId() != AbstractCursorInputEvt.INPUT_DETECTED
						true
					){
				*/
					try {
						AbstractCursorInputEvt startedEvt = (AbstractCursorInputEvt) lastEvt.clone();
						startedEvt.setId(AbstractCursorInputEvt.INPUT_DETECTED);
						startedEvt.preFire();
						
						this.sendEvtToSceneProcessors(newScene, startedEvt);
						logger.debug("Sending INPUT_DETECTED evt to scene: " + newScene.getName() + " Cursor: " + startedEvt.getCursor());
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
					}
//				}
			}
		}
	}
	
	
	/**
	 * Send evt to scene processors.
	 * 
	 * @param scene the scene
	 * @param evtToFire the evt to fire
	 */
	private void sendEvtToSceneProcessors(Iscene scene, AbstractCursorInputEvt evtToFire){
		AbstractGlobalInputProcessor[] sceneInputProcessors = this.getInputManager().getGlobalInputProcessors(scene);
		for (int i = 0; i < sceneInputProcessors.length; i++) {
			AbstractGlobalInputProcessor a = sceneInputProcessors[i];
			//Hack, because processInputEvt() is disabled at this moment! -> not anymore..
//			a.processInputEvtImpl(evtToFire);
			a.processInputEvent(evtToFire);
		}
	}
	
	/**
	 * Gets the currently active scene.
	 * 
	 * @return the current scene
	 */
	public Iscene getCurrentScene(){
		return currentScene;
	}
	
	/*
	public void drawCurrentScene(){
		getCurrentScene().draw();
	}
	public void updateCurrentScene(long timeDelta){
		getCurrentScene().update(timeDelta);
	}
	*/

	/**
	 * Adds the scene to the list of scenes. 
	 * Also changes to that scene if it is the first one to be added.
	 * 
	 * @param scene the scene
	 */
	public void addScene(Iscene scene){
		if (this.getSceneCount() == 0){
			scene.init();
			this.currentScene = scene;
			this.getInputManager().enableGlobalInputProcessors(scene);
			this.fireSceneChangeEvent(new SceneChangeEvent(this, this.currentScene, this.currentScene));
		}
		if (!sceneList.contains(scene))
			sceneList.add(scene);
	}
	
	
	/**
	 * Adds all scenes.
	 * 
	 * @param scenes the scenes
	 */
	public void addAll(Iscene[] scenes){
//		if (this.getSceneCount() == 0 && scenes[0] != null){
//			this.currentScene = scenes[0];
//		}
		for (int i = 0; i < scenes.length; i++) {
			Iscene scene = scenes[i];
//			sceneList.add(scene);
			this.addScene(scene);
		}
	}
	
	/**
	 * Removes the scene from the list of scenes. Fails if the scene is the currently active scene.
	 * If the scene isnt going to be used anymore, calling the scene's destroy() method is the better choice
	 * than the removeScene method alone.
	 * 
	 * @param scene the scene
	 */
	public boolean removeScene(Iscene scene){
		if (sceneList.contains(scene)){
			if (scene.equals(this.currentScene)){
				logger.warn("Cant remove the scene if it is the currently active scene! (" + scene + ")");
				return false;
			}else{
				sceneList.remove(scene);
				return true;
			}
		}
		else{
			return false;	
		}
		
//		return true;
	}
	
	/**
	 * Destroy scene after transition. Workaround so that if a scene's destroy() method is called
	 * but the scene is in a transition (cant be removed then) we call destroy on the scene after
	 * the transition.
	 * Only has an impact if there is a pending transition with the specified scene as the last scene.
	 * 
	 * @param scene the scene
	 */
	public void destroySceneAfterTransition(Iscene scene){
		if (pendingTransition != null && pendingTransition.lastScene.equals(scene)){
			pendingTransition.destroyLastSceneAfterTransition = true;
		}
	}
	
	/**
	 * Gets the registered scenes.
	 * 
	 * @return the scenes
	 */
	public Iscene[] getScenes(){
		return ((Iscene[])sceneList.toArray(new Iscene[sceneList.size()]) );
	}
	
	/**
	 * Gets the scene by name.
	 * 
	 * @param name the name
	 * 
	 * @return the scene
	 */
	public Iscene getScene(String name){
		Iscene returnScene = null;
		for(Iscene scene : sceneList){
			if (scene.getName().equals(name))
				returnScene = scene; 
		}
		return returnScene;
	}
	
	
	/**
	 * Gets the scene count.
	 * 
	 * @return the scene count
	 */
	public int getSceneCount(){
		return sceneList.size();
	}
	

	/**
	 * Gets the input manager.
	 * 
	 * @return the input manager
	 */
	public InputManager getInputManager() {
		return inputManager;
	}

	/**
	 * Sets the input manager.
	 * 
	 * @param inputManager the new input manager
	 */
	public void setInputManager(InputManager inputManager) {
		this.inputManager = inputManager;
	}
	

/////////////////////////	
	/**
	 * Fire scene change event.
	 * 
	 * @param sc the sc
	 */
	protected void fireSceneChangeEvent(SceneChangeEvent sc) {
		for (ISceneChangeListener listener : sceneChangedListeners){
			listener.processSceneChangeEvent(sc);
		}
	}

	/**
	 * Adds a scene change listener.
	 * 
	 * @param listener the listener
	 */
	public synchronized void addSceneChangeListener(ISceneChangeListener listener){
		if (!this.sceneChangedListeners.contains(listener)){
			sceneChangedListeners.add(listener);
		}
		
	}
	
	/**
	 * Removes the scene change listener.
	 * 
	 * @param listener the listener
	 */
	public synchronized void removeSceneChangeListener(ISceneChangeListener listener){
		if (sceneChangedListeners.contains(listener)){
			sceneChangedListeners.remove(listener);
		}
	}
	
	/**
	 * Gets the scene change listeners.
	 * 
	 * @return the scene change listeners
	 */
	public synchronized ISceneChangeListener[] getSceneChangeListener(){
		return (ISceneChangeListener[])sceneChangedListeners.toArray(new ISceneChangeListener[this.sceneChangedListeners.size()]);
	}
/////////////////////////////////	


	/**
	 * Gets the class name.
	 * 
	 * @author C.Ruff
	 */
	public static class CurrentClassGetter extends SecurityManager {
		/**
		 * Gets the class name.
		 * 
		 * @return the class name
		 */
		public String getClassName() {
			return getClassContext()[2].getName(); //FIXME is this reliable to always work?
		}
	}


}
