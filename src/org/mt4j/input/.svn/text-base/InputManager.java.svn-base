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
package org.mt4j.input;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.mt4j.MTApplication;
import org.mt4j.input.inputProcessors.globalProcessors.AbstractGlobalInputProcessor;
import org.mt4j.input.inputSources.AbstractInputSource;
import org.mt4j.input.inputSources.KeyboardInputSource;
import org.mt4j.input.inputSources.MouseInputSource;
import org.mt4j.input.inputSources.MultipleMiceInputSource;
import org.mt4j.input.inputSources.TuioInputSource;
import org.mt4j.input.inputSources.Win7NativeTouchSource;
import org.mt4j.sceneManagement.Iscene;



/**
 * Manages the InputSources and Inputprocessors for each scene.
 * Starts up the default input sources.
 * 
 * @author Christopher Ruff
 */
public class InputManager {
	/** The Constant logger. */
	private static final Logger logger = Logger.getLogger(InputManager.class.getName());
	static{
//		logger.setLevel(Level.ERROR);
//		logger.setLevel(Level.DEBUG);
		logger.setLevel(Level.INFO);
		SimpleLayout l = new SimpleLayout();
		ConsoleAppender ca = new ConsoleAppender(l);
		logger.addAppender(ca);
	}
	
	/** The registered input sources. */
	private List<AbstractInputSource> registeredInputSources;
	
	/** The In processor to scene. */
	private Map<AbstractGlobalInputProcessor, Iscene> inputProcessorsToScene;
	
	/** The pa. */
	private MTApplication app;
	
	
	/**
	 * Instantiates a new input manager.
	 * 
	 * @param pa the processing context
	 */
	public InputManager(MTApplication pa) {
		super();
		this.registeredInputSources	= new ArrayList<AbstractInputSource>();

//		this.inputProcessorsToScene = new WeakHashMap<AbstractGlobalInputProcessor, Iscene>();
		this.inputProcessorsToScene = new HashMap<AbstractGlobalInputProcessor, Iscene>();
		
		this.app = pa;
		
		this.registerDefaultInputSources();
	}
	
	
	
	/**
	 * Initialize default input sources.
	 */
	protected void registerDefaultInputSources(){
		boolean enableMultiMouse = false;
		Properties properties = new Properties();
	    try {
	        properties.load(new FileInputStream(System.getProperty("user.dir") + File.separator + "Settings.txt"));
	        enableMultiMouse = Boolean.parseBoolean(properties.getProperty("MultiMiceEnabled", "false"));
	    } catch (Exception e) {
	    	logger.error("Error while loading Settings.txt file. Using defaults.");
	    }

	    if (enableMultiMouse){
	    	try {
	    		//Register single or multiple mice input source
	    		int connectedMice = MultipleMiceInputSource.getConnectedMouseCount();
//	    		/*
	    		logger.info("Found mice: " + connectedMice);
	    		if (connectedMice >= 2){ //FIXME should be > 1, but manymouse often detects more that arent there!?
	    			logger.info("-> Multiple Mice detected!");
	    			MultipleMiceInputSource multipleMice = new MultipleMiceInputSource(app);
	    			multipleMice.setMTApp(app);
	    			registeredInputSources.add(multipleMice);

	    			this.hideCursorInFrame();
	    		}else{
//	    			*/
	    			MouseInputSource mouseInput = new MouseInputSource(app);
	    			registeredInputSources.add(mouseInput);
	    		}
//	    		*/
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    		//Use default mouse input source
	    		MouseInputSource mouseInput = new MouseInputSource(app);
	    		registeredInputSources.add(mouseInput);
	    	}
	    }
	    else{
//	    	*/
	    	MouseInputSource mouseInput = new MouseInputSource(app);
	    	registeredInputSources.add(mouseInput);
	    }
//	    */
	    
	    //TODO TEST WIN7 WM_TOUCH input 
	  //AT THE MOMENT WE ONLY HAVE 32 BIT DLLs!
	    String platform = System.getProperty("os.name");
	    String bit = System.getProperty("sun.arch.data.model");
	    logger.info("Platform: \"" + platform + "\" -> JVM Bit: \"" + bit + "\"");
	    if (platform.toLowerCase().contains("windows 7")
	    	&& (bit.contains("32") 
	    	||	bit.contains("unknown"))
	    ) {
	    	Win7NativeTouchSource win7NativeInput = new Win7NativeTouchSource(app);
	    	if (win7NativeInput.isSuccessfullySetup()){
	    		registeredInputSources.add(win7NativeInput);	
	    	}
	    }

	    KeyboardInputSource keyInput= new KeyboardInputSource(app);
		TuioInputSource tuioInput 	= new TuioInputSource(app);
//		MuitoInputSource muitoInput = new MuitoInputSource(pa, "localhost", 6666);
		
		registeredInputSources.add(keyInput);
		registeredInputSources.add(tuioInput);
//		registeredInputSources.add(muitoInput);
	}
	
	
	/**
	 * Registers a new inputsource.
	 * 
	 * @param newInputSource the new input source
	 */
	public void registerInputSource(AbstractInputSource newInputSource){
		if (!registeredInputSources.contains(newInputSource)){
			registeredInputSources.add(newInputSource);
			//Add all processors to the new input source
			Set<AbstractGlobalInputProcessor> set = inputProcessorsToScene.keySet(); 
			for (Iterator<AbstractGlobalInputProcessor> iter = set.iterator(); iter.hasNext();) {
				AbstractGlobalInputProcessor processor = (AbstractGlobalInputProcessor) iter.next();
				//newInputSource.addInputListener(processor);
				this.saveAddInputListenerToSource(newInputSource, processor);
			}
		}else{
			logger.error("input source already registered! - " + newInputSource);
		}
	}
	
	
	/**
	 * Hides the mousecursor in multiple mice mode.
	 */
	private void hideCursorInFrame(){
		int[] pixels = new int[16 * 16];
		Image image = Toolkit.getDefaultToolkit().createImage(
		        new MemoryImageSource(16, 16, pixels, 0, 16));
		Cursor transparentCursor =
		        Toolkit.getDefaultToolkit().createCustomCursor
		             (image, new Point(0, 0), "invisibleCursor");
		app.frame.setCursor(transparentCursor);
	}
	
	
	/**
	 * Unregisters a input source.
	 * 
	 * @param is the input source
	 */
	public void unregisterInputSource(AbstractInputSource is){
		synchronized (registeredInputSources) {
			if (registeredInputSources.contains(is)){
				registeredInputSources.remove(is);
			}
		}
	}
	
	/**
	 * Gets the registered input sources.
	 * 
	 * @return the registered input sources
	 */
	public Collection<AbstractInputSource> getRegisteredInputSources(){
		return this.registeredInputSources;
	}
	
	
	/**
	 * Registers a new inputprocessor and adds it to the inputsources as listeners.
	 * 
	 * @param scene the scene
	 * @param inputprocessor the input processor
	 */
	public void registerGlobalInputProcessor(Iscene scene, AbstractGlobalInputProcessor inputprocessor){
		//By default disable the registered global input processor, so it doesent accidently
		//send events to a not even visible scene
		//-Only enable it if the scene is the currently active scene
		//-If a scene becomes active the processors will also be enabled
//		if (app.getCurrentScene() != null && app.getCurrentScene().equals(scene)){
		if (scene.equals(app.getCurrentScene())){
			inputprocessor.setDisabled(false);
		}else{
			inputprocessor.setDisabled(true);
		}
		
		inputProcessorsToScene.put(inputprocessor, scene);
		//Register the processor with all registered inputsources
		for (AbstractInputSource source: registeredInputSources){
			this.saveAddInputListenerToSource(source, inputprocessor);
		}
	}
	
	
	private void saveAddInputListenerToSource(AbstractInputSource source, AbstractGlobalInputProcessor inputprocessor){
		//Only add input processor to input sources 
		//that fire the event type that the processor is interested in
//		if (source.firesEventType(inputprocessor.getListenEventType())){
			source.addInputListener(inputprocessor);
//		}
	}
	
	/**
	 * Unregisters a inputprocessor from _all_ the registered inputsources.
	 * 
	 * @param inputprocessor the input processor
	 */
	public void unregisterGlobalInputProcessor(AbstractGlobalInputProcessor inputprocessor){
		/*
		Set set = InprocessorToScene.keySet(); 
		for (Iterator iter = set.iterator(); iter.hasNext();) {
			AbstractInputprocessor processor = (AbstractInputprocessor) iter.next();
			
			//Check if the processor is registered here with a scene
			if (processor.equals(inputprocessor)){
				if (InprocessorToScene.get(processor).equals(scene)){
					for (AbstractInputSource source: registeredInputSources){
						source.removeInputListener(inputprocessor);
					}
				}
			}
		}
		*/
		
		//Remove the input processor from the processor->scene map
		if (inputProcessorsToScene.containsKey(inputprocessor)){
			inputProcessorsToScene.remove(inputprocessor);	
		}
		
		for (AbstractInputSource source: registeredInputSources){
			source.removeInputListener(inputprocessor);
		}
	}
	
	
	/**
	 * Gets the global inputprocessors associated with the specified scene.
	 * 
	 * @param scene the scene
	 * 
	 * @return the scene inputprocessors
	 */
	public AbstractGlobalInputProcessor[] getGlobalInputProcessors(Iscene scene){
		List<AbstractGlobalInputProcessor> processors = new ArrayList<AbstractGlobalInputProcessor>();
		
		Set<AbstractGlobalInputProcessor> set = inputProcessorsToScene.keySet(); 
		for (Iterator<AbstractGlobalInputProcessor> iter = set.iterator(); iter.hasNext();) {
			AbstractGlobalInputProcessor processor = (AbstractGlobalInputProcessor) iter.next();
			
			if (inputProcessorsToScene.get(processor).equals(scene)){
				processors.add(processor);	
			}
		}
		return processors.toArray(new AbstractGlobalInputProcessor[processors.size()]);
	}
	
	/**
	 * Enables the global inputprocessors that are associated with the given scene.
	 * 
	 * @param scene the scene
	 */
	public void enableGlobalInputProcessors(Iscene scene){
		Set<AbstractGlobalInputProcessor> set = inputProcessorsToScene.keySet(); 
		for (Iterator<AbstractGlobalInputProcessor> iter = set.iterator(); iter.hasNext();) {
			AbstractGlobalInputProcessor processor = (AbstractGlobalInputProcessor) iter.next();
			if (inputProcessorsToScene.get(processor).equals(scene)){
				processor.setDisabled(false);
			}
		}
	}
	
	/**
	 * Disables the global inputprocessors that are associated with the given scene.
	 * 
	 * @param scene the scene
	 */
	public void disableGlobalInputProcessors(Iscene scene){
		Set<AbstractGlobalInputProcessor> set = inputProcessorsToScene.keySet(); 
		for (Iterator<AbstractGlobalInputProcessor> iter = set.iterator(); iter.hasNext();) {
			AbstractGlobalInputProcessor processor = (AbstractGlobalInputProcessor) iter.next();
			if (inputProcessorsToScene.get(processor).equals(scene)){
				processor.setDisabled(true);
			}
		}
	}
	
	
	/**
	 * Removes input processors of the specified scene from listening to the registered input sources.
	 * 
	 * @param scene the scene
	 */
	public void removeGlobalInputProcessors(Iscene scene){
		AbstractGlobalInputProcessor[] sceneProcessors = this.getGlobalInputProcessors(scene);
		for (int i = 0; i < sceneProcessors.length; i++) {
			AbstractGlobalInputProcessor abstractGlobalInputProcessor = sceneProcessors[i];
			this.unregisterGlobalInputProcessor(abstractGlobalInputProcessor);
		}
	}


	
}
