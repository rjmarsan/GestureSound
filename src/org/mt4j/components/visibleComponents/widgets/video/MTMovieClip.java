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
package org.mt4j.components.visibleComponents.widgets.video;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.mt4j.components.TransformSpace;
import org.mt4j.components.bounds.BoundsZPlaneRectangle;
import org.mt4j.components.bounds.IBoundingShape;
import org.mt4j.components.visibleComponents.shapes.MTRoundRectangle;
import org.mt4j.components.visibleComponents.widgets.MTSlider;
import org.mt4j.components.visibleComponents.widgets.buttons.MTSvgButton;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragEvent;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.lassoProcessor.IdragClusterable;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent;
import org.mt4j.util.MT4jSettings;
import org.mt4j.util.MTColor;
import org.mt4j.util.math.Vector3D;
import org.mt4j.util.math.Vertex;
import org.mt4j.util.opengl.GLConstants;
import org.mt4j.util.opengl.GLTexture;

import processing.core.PApplet;
import processing.core.PImage;
import codeanticode.gsvideo.GSMovie;

/**
 * The Class MTMovieClip. 
 * A widget which can be used as a video player.
 * <br>NOTE: Needs to have the GStreamer framework to be installed on the system.
 * 
 * @author Christopher Ruff
 */
public class MTMovieClip extends 
//MTRectangle 
MTRoundRectangle
implements IdragClusterable {

	/** The movie. */
	private GSMovie movie;
	
	/** The first time read. */
	private boolean firstTimeRead;
	
	/** The selected. */
	private boolean selected;
	
	/** The play button. */
	MTSvgButton playButton;

	/**
	 * Instantiates a new MT movie clip.
	 * 
	 * @param movieFile the movie file
	 * @param upperLeft the upper left
	 * @param pApplet the applet
	 */
	public MTMovieClip(String movieFile, Vertex upperLeft, PApplet pApplet) {
		this(movieFile, upperLeft, 30, pApplet);
	}
	
	/**
	 * Instantiates a new MT movie clip.
	 * 
	 * @param movieFile the movie file - located in the ./data directory
	 * @param upperLeft the upper left movie position
	 * @param ifps the ifps the frames per second
	 * @param pApplet the applet
	 */
	public MTMovieClip(String movieFile, Vertex upperLeft, int ifps,  PApplet pApplet) {
//		super(upperLeft, 150, 100, pApplet);
		super(upperLeft.x,upperLeft.y,upperLeft.z, 105,127, 15,15, pApplet);
		
		try {
			movie = new GSMovie(pApplet, movieFile, ifps, this);
			
			this.setName("unnamed movieclip");		
			
			playButton = new MTSvgButton(MT4jSettings.getInstance().getDefaultSVGPath() 
					+ "play.svg" , pApplet);
			playButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
//					movie.play();
					switch (arg0.getID()) {
					case TapEvent.BUTTON_CLICKED:
						movie.loop();
						slider.setVisible(true);
						break;
					default:
						break;
					}
				}
			});
			playButton.scale(0.5f, 0.5f, 1, new Vector3D(0,0,0));
			playButton.translate(upperLeft);
			this.addChild(playButton);

			MTSvgButton stopButton = new MTSvgButton(MT4jSettings.getInstance().getDefaultSVGPath() 
					+ "stop.svg" , pApplet);
			stopButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					switch (arg0.getID()) {
					case TapEvent.BUTTON_CLICKED:
						movie.stop();
						movie.goToBeginning();
						slider.setVisible(false);
						break;
					default:
						break;
					}
				}
			});
			//TODO müsste eigentlich grösste comp aus svg holen, dann center an die stelle positionieren
			this.addChild(stopButton);
			stopButton.scale(0.5f, 0.5f, 1, new Vector3D(0,0,0));
			stopButton.translate(new Vector3D(upperLeft.x + 30 , upperLeft.y, upperLeft.z));
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		firstTimeRead = true;
		
		
		if (MT4jSettings.getInstance().isOpenGlMode())
			this.setUseDirectGL(true);
		
		try{
			PImage movieImg = pApplet.loadImage(MT4jSettings.getInstance().getDefaultImagesPath() + "Crystal_Clear_mimetype_video_cr.png");
			this.setTexture(movieImg);
			this.setTextureEnabled(true);
//			this.setSizeXYRelativeToParent(movieImg.width, movieImg.height);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//Slider
		this.duration = 0.0f;
		sliderXOffset = 10;
		sliderHeight = 10;
		slider = new MTSlider(upperLeft.x + sliderXOffset, upperLeft.y + this.getHeightXY(TransformSpace.LOCAL) - 30, this.getWidthXY(TransformSpace.LOCAL) - sliderXOffset*2, sliderHeight, 0, 10, pApplet);
		slider.getOuterShape().setFillColor(new MTColor(0, 0, 0, 80));
		slider.getOuterShape().setStrokeColor(new MTColor(0, 0, 0, 80));
		slider.getKnob().setFillColor(new MTColor(100, 100, 100, 80));
		slider.getOuterShape().setStrokeColor(new MTColor(100, 100, 100, 80));
		slider.getKnob().addGestureListener(DragProcessor.class, new IGestureEventListener() {
			//@Override
			public boolean processGestureEvent(MTGestureEvent ge) {
				DragEvent de = (DragEvent)ge;
				switch (de.getId()) {
				case MTGestureEvent.GESTURE_DETECTED:
					dragging = true;
					break;
				case MTGestureEvent.GESTURE_UPDATED:
					break;
				case MTGestureEvent.GESTURE_ENDED:
					if (m.isPlaying()){
						float currValue = slider.getValue();
						jump(currValue);
					}
					dragging = false;
					break;
				default:
					break;
				}
				return false;
			}
		});
		this.addChild(slider);
		slider.setVisible(false);
		dragging = false;
	}
	
	
	/* (non-Javadoc)
	 * @see org.mt4j.components.visibleComponents.shapes.MTRoundRectangle#computeDefaultBounds()
	 */
	@Override
	protected IBoundingShape computeDefaultBounds() {
		return new BoundsZPlaneRectangle(this);
	}
	
	/** The slider. */
	private MTSlider slider;
	
	/** The dragging. */
	private boolean dragging;
	
	/** The slider x offset. */
	private int sliderXOffset;
	
	/** The slider height. */
	private int sliderHeight;
	
	/** The m. */
	private GSMovie m;
	
	/**
	 * Movie event.
	 * 
	 * @param myMovie the my movie
	 * 
	 * @throws InterruptedException the interrupted exception
	 */
	public void movieEvent(GSMovie myMovie) throws InterruptedException {
		m = myMovie;
		
		if (!dragging){
			slider.setValue(myMovie.time()); //ONLY DO THIS WHEN NOT DRAGGING THE SLIDER
		}

		if (firstTimeRead 
			&& myMovie.available()
		){
			myMovie.read();
			System.out.println("Movie img format: " + m.format);
			
			//FIXME TEST - dont do every frame! Duration is only valid if playing..
			slider.setValueRange(0, myMovie.duration());
			
			this.setSizeLocal(m.width, m.height);
//			/*
			slider.setSizeXYRelativeToParent(m.width - 2*sliderXOffset, sliderHeight);
			Vector3D movieClipCenterLocal = this.getCenterPointLocal();
			slider.setPositionRelativeToParent(new Vector3D(movieClipCenterLocal.x, movieClipCenterLocal.y + this.getHeightXY(TransformSpace.LOCAL)*0.5f - slider.getHeightXY(TransformSpace.RELATIVE_TO_PARENT)*0.5f - 5,0 ));
//			*/
			
//			this.setUseDirectGL(false);
			this.setTexture(null); //TO force to rescale of new texture coordianates to RECTANGLE (0..width)
			this.setTexture(m);
			this.setTextureEnabled(true);
//			this.setUseDirectGL(true);
			firstTimeRead = false;
		}
	}
	
	
	/* (non-Javadoc)
	 * @see com.jMT.components.MTBaseComponent#updateComponent(long)
	 */
	@Override
	public void updateComponent(long timeDelta){
		super.updateComponent(timeDelta);
//		/*
		if (   m != null 
			&& m.isPlaying()
			&& m.available() //if unread frame available
			){
			if (this.getTexture() instanceof GLTexture){
				if (this.isUseDirectGL() && MT4jSettings.getInstance().isOpenGlMode()){
					//Directly put the new frame buffer into the texture only if in openGL mode 
					//without filling the PImage array of this objects texture and also not of the GSMovie PImage =>performance
					((GLTexture)this.getTexture()).putBuffer(m.getMoviePixelsBuffer(),  GLConstants.TEX4, GLConstants.TEX_UBYTE);
				}else{
					//Fill the PImage with the new movieframe
					//dont fill the openGL texture
					m.read();
					((GLTexture)this.getTexture()).putImageOnly(m);	
				}
			}else{
				//Usually all textures should be GLTextures instances, but just to be sure..
				m.read();
				this.setTexture(m); //SLOW!
			}
		}
//		*/
	}
	
	//FIXME TEST
	/** The duration. */
	float duration;
	
	/**
	 * Gets the duration.
	 * 
	 * @return the duration
	 */
	public float getDuration(){//duration only valid if video is playing
		if (movie.duration() == 0.0){
			return duration;
		}else{
			duration = movie.duration();
			return duration;
		}
	}
	

	/**
	 * Jump.
	 * 
	 * @param where the where
	 */
	public void jump(float where) {
		movie.jump(where);
	}


	/**
	 * Loop the movie.
	 */
	public void loop() {
		movie.loop();
	}


	/**
	 * No looping.
	 */
	public void noLoop() {
		movie.noLoop();
	}


	/**
	 * Pause.
	 */
	public void pause() {
		movie.pause();
	}


	/**
	 * Play.
	 */
	public void play() {
		movie.play();
	}


	/**
	 * Stop.
	 */
	public void stop() {
		movie.stop();
	}


	/**
	 * Time.
	 * 
	 * @return the time the movie plays in float
	 */
	public float getTime() {
		return movie.time();
	}

	/* (non-Javadoc)
	 * @see com.jMT.input.inputAnalyzers.clusterInputAnalyzer.IdragClusterable#isSelected()
	 */
	public boolean isSelected() {
		return selected;
	}

	/* (non-Javadoc)
	 * @see com.jMT.input.inputAnalyzers.clusterInputAnalyzer.IdragClusterable#setSelected(boolean)
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	

}
