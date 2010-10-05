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
package org.mt4j.util;

import java.io.File;

/**
 * A class with some configurations to read the current settings from.
 * 
 * @author Christopher Ruff
 */
public class MT4jSettings {
	
	/** The const and settings. */
	private static MT4jSettings constAndSettings = null;
	
	/** Screen Size X. */
	private int screenWidth = 1024;
	
	/** Screen Size Y. */
	private int screenHeight = 768;
	
	//Draw Modes
	/** The Constant OPENGL_MODE. */
	public static final int OPENGL_MODE = 1;
	
	/** The Constant P3D_MODE. */
	public static final int P3D_MODE    = 2;
	
	/** Current DrawMode. */
	private int renderer = P3D_MODE;
	
	/** The num samples. */
	private int numSamples = 0;
	
	/** Frame Title. */
	private String frameTitle = "MT-Application";
	
	/** Maximum FrameRate. */
	private int maxFrameRate = 60;
	
	/** Start time of the app. */
	private long programStartTime = 0;
	
	/** The fullscreen. */
	public static boolean fullscreen = false;
	
	/** The DEFAUL t_ fon t_ path. */
	public static String DEFAULT_SETTINGS_PATH = new String(System.getProperty("user.dir") + File.separator);

	public static String DEFAULT_DATA_FOLDER_PATH = new String(System.getProperty("user.dir") + File.separator + "data" + File.separator);
	
	/** The DEFAUL t_ fon t_ path. */
	public static String DEFAULT_FONT_PATH = DEFAULT_DATA_FOLDER_PATH;
	
	/** The DEFAUL t_ image s_ path. */
	public static String DEFAULT_IMAGES_PATH = new String(System.getProperty("user.dir") + File.separator + "data" + File.separator + "images"  +  File.separator);
	
//	public static String DEFAULT_VIDEOS_PATH = new String(System.getProperty("user.dir") + File.separator + "data" /*+ File.separator + "videos"  */ +  File.separator);
	//Since gsvideo looks into the ./data directory by itself
	/** The DEFAUL t_ video s_ path. */
	public static String DEFAULT_VIDEOS_PATH = new String("");
	
	/** The DEFAUL t_ sv g_ path. */
	public static String DEFAULT_SVG_PATH = new String(System.getProperty("user.dir") + File.separator + "data" + File.separator + "svg"  +  File.separator);
	
	/** The DEFAUL t_3 d_ mode l_ path. */
	public static String DEFAULT_3D_MODEL_PATH = new String(System.getProperty("user.dir") + File.separator + "data" + File.separator + "models"  +  File.separator);
	

	/**
	 * Gets the path to the /data folder.
	 * 
	 * @return the default data path
	 */
	public String getDataFolderPath() {
		return DEFAULT_DATA_FOLDER_PATH;
	}
	
	
	/**
	 * Gets the default settings path.
	 * 
	 * @return the default settings path
	 */
	public String getDefaultSettingsPath() {
		return DEFAULT_SETTINGS_PATH;
	}

	/**
	 * Gets the default font path.
	 * 
	 * @return the default font path
	 */
	public String getDefaultFontPath(){
		return DEFAULT_FONT_PATH;
	}
	
	/**
	 * Gets the default images path.
	 * 
	 * @return the default images path
	 */
	public String getDefaultImagesPath(){
		return DEFAULT_IMAGES_PATH;
	}
	
	/**
	 * Gets the default videos path.
	 * 
	 * @return the default videos path
	 */
	public String getDefaultVideosPath(){
		return DEFAULT_VIDEOS_PATH;
	}
	
	/**
	 * Gets the default svg path.
	 * 
	 * @return the default svg path
	 */
	public String getDefaultSVGPath(){
		return DEFAULT_SVG_PATH;
	}
	
	/**
	 * Gets the default3 d model path.
	 * 
	 * @return the default3 d model path
	 */
	public String getDefault3DModelPath(){
		return DEFAULT_3D_MODEL_PATH;
	}
	
	
	/**
	 * Checks if is fullscreen.
	 * 
	 * @return true, if is fullscreen
	 */
	public boolean isFullscreen(){
		return fullscreen;
	}
	
	
	/**
	 * Gets the num samples.
	 * 
	 * @return the num samples
	 */
	public int getNumSamples() {
		return numSamples;
	}


	/**
	 * Sets the num samples.
	 * 
	 * @param numSamples the new num samples
	 */
	public void setNumSamples(int numSamples) {
		this.numSamples = numSamples;
	}
	
	/**
	 * Checks if is multi sampling.
	 * 
	 * @return true, if is multi sampling
	 */
	public boolean isMultiSampling(){
		return getNumSamples() > 0;
	}


	/**
	 * Instantiates a new constants and settings.
	 */
	private MT4jSettings(){
	}

	/**
	 * Returns the GlobalConstants and Settings Object.
	 * Implements the singleton pattern.
	 * 
	 * @return ConstantsAndHelpers object
	 */
	public static MT4jSettings getInstance(){
		if (constAndSettings == null){
			constAndSettings = new MT4jSettings();
			return constAndSettings;
		}else{
			return constAndSettings;
		}
	}
	
	
	/**
	 * Gets the screen height.
	 * 
	 * @return the screen height
	 */
	public int getScreenHeight() {
		return screenHeight;
	}

	/**
	 * Gets the screen width.
	 * 
	 * @return the screen width
	 */
	public int getScreenWidth() {
		return screenWidth;
	}

	/**
	 * Gets the screen center.
	 * 
	 * @return the screen center
	 */
	public float[] getScreenCenter(){
		return new float[]{getScreenWidth()/2, getScreenHeight()/2 , 0};
	}
	
	/**
	 * Gets the renderer mode.
	 * 
	 * @return the renderer mode
	 */
	public int getRendererMode() {
		return renderer;
	}

	/**
	 * Sets the renderer mode.
	 * 
	 * @param drawMode the new renderer mode
	 */
	public void setRendererMode(int drawMode) {
		this.renderer = drawMode;
	}
	
//	synchronized public long generateNewID(){
//		return ConstantsAndHelpers.currentID++;
//	}

	
	/**
	 * Sets the title of the application frame.
	 * Only takes effect if called right at the start.
	 * This should be called by internally only!
	 * 
	 * @param frameTitle the frame title
	 */
	public void setFrameTitle(String frameTitle) {
		this.frameTitle = frameTitle;
	}


	/**
	 * Gets the frame title.
	 * 
	 * @return the frame title
	 */
	public String getFrameTitle() {
		return frameTitle;
	}

	/**
	 * Sets the max frame rate.
	 * 
	 * @param frameRate the new max frame rate
	 */
	public void setMaxFrameRate(int frameRate) {
		this.maxFrameRate = frameRate;
	}
	
	/**
	 * Gets the max frame rate.
	 * 
	 * @return the max frame rate
	 */
	public int getMaxFrameRate() {
		return maxFrameRate;
	}

	/**
	 * Gets the program start time.
	 * 
	 * @return the program start time
	 */
	public long getProgramStartTime() {
		return programStartTime;
	}

	/**
	 * Sets the program start time.
	 * 
	 * @param programStartTime the new program start time
	 */
	public void setProgramStartTime(long programStartTime) {
		this.programStartTime = programStartTime;
	}

	/**
	 * NOTE: DONT SET THIS AFTER size() FROM PAPPLET HAS BEEN CALLED!.
	 * 
	 * @param screenHeight the screen height
	 */
	public void setScreenHeight(int screenHeight) {
		this.screenHeight = screenHeight;
	}

	/**
	 * NOTE: DONT SET THIS AFTER size() FROM PAPPLET HAS BEEN CALLED!.
	 * 
	 * @param screenWidth the screen width
	 */
	public void setScreenWidth(int screenWidth) {
		this.screenWidth = screenWidth;
	}


	/**
	 * Checks if is open gl mode.
	 * 
	 * @return true, if is open gl mode
	 */
	public boolean isOpenGlMode(){
		return this.getRendererMode() == MT4jSettings.OPENGL_MODE;
	}
	
	/**
	 * Checks if is p3d mode.
	 * 
	 * @return true, if is p3d mode
	 */
	public boolean isP3DMode(){
		return this.getRendererMode() == MT4jSettings.P3D_MODE;
	}
	

}
