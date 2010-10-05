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
package org.mt4j.components.visibleComponents.widgets;

import java.util.HashMap;

import javax.media.opengl.GL;

import org.mt4j.MTApplication;
import org.mt4j.components.visibleComponents.shapes.MTRectangle;
import org.mt4j.input.inputData.AbstractCursorInputEvt;
import org.mt4j.input.inputData.InputCursor;
import org.mt4j.input.inputData.MTInputEvent;
import org.mt4j.input.inputProcessors.globalProcessors.AbstractGlobalInputProcessor;
import org.mt4j.input.inputProcessors.globalProcessors.CursorTracer;
import org.mt4j.sceneManagement.Iscene;
import org.mt4j.util.math.Plane;
import org.mt4j.util.math.Tools3D;
import org.mt4j.util.math.Vector3D;
import org.mt4j.util.math.Vertex;
import org.mt4j.util.opengl.FboStack;
import org.mt4j.util.opengl.GLFBO;
import org.mt4j.util.opengl.GLTexture;

import processing.core.PConstants;
import processing.core.PGraphics;
import processing.opengl.PGraphicsOpenGL;

/**
 * The Class MTSceneTexture. This class allows to display a scene from within another scene.
 * The scene will be displayed as a textured rectangle. This is only supported using the OpenGL renderer
 * and a graphics card supporting the frame buffer object extension.
 * 
 * @author Christopher Ruff
 */
public class MTSceneTexture extends MTRectangle {

	private GLFBO fbo;
	private Iscene scene;
	private MTApplication app;
	
	private Plane p;
	private HashMap<InputCursor, InputCursor> oldCursorToNewCursor;
	
	//TODO kind of a hack
	private long lastUpdateTime;
	
	private boolean maximized;
	
	private MTSceneMenu sceneMenu;
	
	public MTSceneTexture(MTApplication pa, float x,	float y, Iscene theScene){
		this(pa, x, y, Math.round(pa.width * 0.6f), Math.round(pa.height * 0.6f), theScene);
	}

	public MTSceneTexture(MTApplication pa, float x,	float y, int fboWidth, int fboHeight, Iscene theScene){
		super(x, y, 0, pa.width, pa.height, pa);
		
		this.scene = theScene;
		this.app = pa;
		this.maximized = false;
		
		//Disable the scene's global input processors. We will be redirecting the input
		//from the current scene to the window scene
		pa.getInputManager().disableGlobalInputProcessors(scene);
		
		//Create FBO //FIXME TODO MAKE SIZE SETTABLE, also constructor
//		this.fbo = new GLFBO(pa, pa.width, pa.height);
//		this.fbo = new GLFBO(pa, Math.round(pa.width/2f), Math.round(pa.height/2f)); //FIXME TEST
//		this.fbo = new GLFBO(pa, Math.round(pa.width/1.5f), Math.round(pa.height/1.5f)); //FIXME TEST
//		this.fbo = new GLFBO(pa, Math.round(pa.width * 0.675f), Math.round(pa.height * 0.675f)); //FIXME TEST
		
		this.fbo = new GLFBO(pa, fboWidth, fboHeight); //FIXME TEST
		
		//Attach texture to FBO to draw into
		GLTexture tex = fbo.addNewTexture();
		
		//Invert y texture coord (FBO texture is flipped)
		Vertex[] v = this.getVerticesLocal();
		for (int i = 0; i < v.length; i++) {
			Vertex vertex = v[i];
			if (vertex.getTexCoordV() == 1.0f){
				vertex.setTexCoordV(0.0f);
			}else if (vertex.getTexCoordV() == 0.0f){
				vertex.setTexCoordV(1.0f);
			}
		}
		this.setVertices(v);
		
		//Apply the texture to this component
		this.setTexture(tex);
		
		//Scale texture coords if using TEXTURE_RECTANGLE_ARB extension
		if (!Tools3D.isPowerOfTwoDimension(tex)){
			Tools3D.scaleTextureCoordsForRectModeFromNormalized(tex, this.getGeometryInfo().getVertices());
			this.setTextureMode(PConstants.IMAGE);
			//Update the texture buffer!
			this.getGeometryInfo().updateTextureBuffer(this.isUseVBOs());
		}
		
		//REMOVE?
		this.setUseDirectGL(true);
//		this.setBoundsPickingBehaviour(BOUNDS_CHECK_THEN_GEOMETRY_CHECK);
		
		//Plane to check intersections with
		this.p = new Plane(new Vector3D(x,y,0) , new Vector3D(0,0,1));
		//Mapping of this scenes inputCursors to the newly created cursors of the window scene
		this.oldCursorToNewCursor = new HashMap<InputCursor, InputCursor>();
		
		this.lastUpdateTime = 0;
		
		//Draw this component and its children above 
		//everything previously drawn and avoid z-fighting
//		this.setDepthBufferDisabled(true); //FIXME this wont work well when the scene is 3D!
		
//		scene.getCanvas().setDepthBufferDisabled(false);
		
		getFbo().clear(true, 0, 0, 0, 0, true);
		
		pa.invokeLater(new Runnable() { 
			//Do the next frame because if MTSceneTexture is created in the first scene,
			//addScene() will make the scene texture scene the current scene which we dont want!
			public void run() {
				//Add it to the scene if it isnt already -> we cant destroy the scene later if it isnt in the app
				app.addScene(scene);		
			}
		});
		 
	}
	
	

	@Override
	public void updateComponent(long timeDelta) {
		super.updateComponent(timeDelta);
		this.lastUpdateTime = timeDelta;
	}
	
	

	@Override
	public void drawComponent(PGraphics g){
		PGraphicsOpenGL pgl = (PGraphicsOpenGL)g; 
		GL gl = pgl.gl;
		
		fbo.startRenderToTexture();
			//Change blending mode to avoid artifacts from alpha blending at antialiasing for example
//			gl.glBlendFuncSeparate(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA, GL.GL_ZERO, GL.GL_ONE);
			gl.glBlendFuncSeparate(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA, GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);
			
//			gl.glEnable(gl.GL_ALPHA_TEST);
//			gl.glAlphaFunc(gl.GL_GREATER, 0.0f);
//			gl.glDisable(gl.GL_ALPHA_TEST);
			//Draw scene to texture
			scene.drawAndUpdate(pgl, this.lastUpdateTime);
		fbo.stopRenderToTexture();

		if (FboStack.getInstance(gl).peekFBO() == 0)
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA); //Restore default blend mode //FIXME TEST -> neccessary?
		
		//FIXME NOT NEEDED!? sufficient to call glGenerateMipmapEXT at texture creation!? 
		//I actually think its necessary to call each time after rendering to the texture! But lets see!
		/*
		GLTexture tex = (GLTexture) this.getTexture();
		gl.glBindTexture(tex.getTextureTarget(), tex.getTextureID());
		gl.glGenerateMipmapEXT(tex.getTextureTarget());
		gl.glBindTexture(tex.getTextureTarget(), 0);
		*/
		super.drawComponent(g);
	}
	
	/*TODO
	 * 
	 * - evtl scene kleiner zeichnen und kleineren fbo machen für kleines windows? ->performance
	 * 
	 * - make fullscreen ability! maximize/restore! 
	 * 
	 * - wenn scaling - scale viewport of scene to fit scale? ->pmt schaun
	 * 
	 * - fehler wenn gedreht x,y, axis und dann scale - wegen shearing?
	 * - fehler scheint von rotation zu kommen nachdem rotiert x,y,
	 * 
	 * - progressbar sizes wrong bei 512,512
	 * 
	 * - FBO: mehrere texture targets wenn supported ermöglichen 
	 * 
	 * - FBO: multisampling fbo optional wenn supported machen siehe glgraphicsoffscreen
	 * 
	 * - FBO: bei RECTANGLE dimensions keine mipmaps möglich?? 
	 * 
	 * - FBO: hardware fbo mit glCopyTex2D
	 * 
	 * - nur sceneDrawAndUpdate() wenn sich was verändert hat - sonst kann man einfach alte textur lassen! setSceneNeedsUpdate()?
	 * 
	 * (- add stencil buffer to frambufferobject - evtl checken if available/supported)
	 * (- wenn camera changed richtig picken in scene)
	 * (- neue posEvt schicken mit neuer cursor? eigenen inputretargeter verwenden?)
	 * 
	 * */
	

	@Override
	public boolean processInputEvent(MTInputEvent inEvt) {
		//We have to retarget inputevents for this component to the windowed scene
		if (inEvt instanceof AbstractCursorInputEvt){
			AbstractCursorInputEvt posEvt = (AbstractCursorInputEvt)inEvt;
			float x = posEvt.getPosX();
			float y = posEvt.getPosY();
			
			float newX = 0;
			float newY = 0;
			//Check intersection with infinite plane, this rect lies in
			Vector3D interSP = p.getIntersectionLocal(this.globalToLocal(Tools3D.getCameraPickRay(app, this, x, y)));
			if (interSP != null){
				//System.out.println(interSP);
				newX = interSP.x;
				newY = interSP.y;
			}
			
			AbstractCursorInputEvt newEvt = null;
			switch (posEvt.getId()) 
			{
			case AbstractCursorInputEvt.INPUT_DETECTED:{
				InputCursor newCursor = new InputCursor();
				try {
					newEvt = (AbstractCursorInputEvt) posEvt.clone();
					newEvt.setPositionX(newX);
					newEvt.setPositionY(newY);
//					newCursor.addEvent(newEvt);
					newEvt.setCursor(newCursor);
					newEvt.preFire();
					//Note: We dont set a target for the event! this can be
					//handled newly in the wondowed scenes InputRetargeter processor
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
				this.oldCursorToNewCursor.put(posEvt.getCursor(), newCursor);
				//TODO checken ob cursor bereits events enthält - überhaupt möglich?..
				//ELSE -> CLONE AND ADD ALL OLD EVENTS TO THE NEW CURSOR!
			}break;
			case AbstractCursorInputEvt.INPUT_UPDATED:{
				InputCursor newCursor = this.oldCursorToNewCursor.get(posEvt.getCursor());
				if (newCursor != null){
					try {
						newEvt = (AbstractCursorInputEvt) posEvt.clone();
						newEvt.setPositionX(newX);
						newEvt.setPositionY(newY);
//						newCursor.addEvent(newEvt);
						newEvt.setCursor(newCursor);
						newEvt.preFire();
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
					}
				}else{
					System.err.println("Couldnt find new cursor!");
				}
			}break;
			case AbstractCursorInputEvt.INPUT_ENDED:{
//				InputCursor newCursor = this.oldCursorToNewCursor.get(posEvt.getCursor());
				InputCursor newCursor = this.oldCursorToNewCursor.remove(posEvt.getCursor());
				if (newCursor != null){
					try {
						newEvt = (AbstractCursorInputEvt) posEvt.clone();
						newEvt.setPositionX(newX);
						newEvt.setPositionY(newY);
//						newCursor.addEvent(newEvt);
						newEvt.setCursor(newCursor);
						newEvt.preFire();
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
					}
//					this.oldCursorToNewCursor.remove(posEvt.getCursor());
				}else{
					System.err.println("Couldnt find new cursor!");
				}
			}break;
			default:
				break;
			}
			
			AbstractCursorInputEvt evtToFire = (newEvt != null) ? 
					newEvt : 
					posEvt; 
			
			//Send similar event to the windowed scenes global input processors
			AbstractGlobalInputProcessor[] globalAnalyzer = app.getInputManager().getGlobalInputProcessors(scene);
			for (int i = 0; i < globalAnalyzer.length; i++) {
				AbstractGlobalInputProcessor a = globalAnalyzer[i];
				if (!(a instanceof CursorTracer)){
					//Hack because actually processors are disabled so they dont recieve
					//input directly, so we dont call processInputEvt()!
					a.processInputEvtImpl(evtToFire);	
				}
				
			}
			return false;
		}else{
			return super.processInputEvent(inEvt);
		}
//		return super.processInputEvent(inEvt);
//		scene.getCanvas().processInputEvent(inEvt);
//		return true;
	}
	
	
//	@Override
//	public boolean processGestureEvent(MTGestureEvent gestureEvent) {
//		return super.processGestureEvent(gestureEvent);
//	}

	
	public Iscene getScene(){
		return this.scene;
	}


	public GLFBO getFbo() {
		return fbo;
	}

	
	public void addSceneMenu(){
		if (sceneMenu == null){
			float menuWidth = 64;
			float menuHeight = 64;
//			this.sceneMenu = new MTSceneMenu(this, app.width-menuWidth/2f, 0-menuHeight/2f, menuWidth, menuHeight, app);
//			this.sceneMenu = new MTSceneMenu(this, app.width-menuWidth, 0, menuWidth, menuHeight, app);
			this.sceneMenu = new MTSceneMenu(this, app.width-menuWidth, app.height-menuHeight, menuWidth, menuHeight, app);
			this.sceneMenu.setVisible(false);
		}
		
		this.sceneMenu.addToScene();
		if (maximized){
			this.sceneMenu.setVisible(true);
		}
	}
	
	public void destroySceneMenu(){
		this.sceneMenu.removeFromScene();
		this.sceneMenu.destroy();
		this.sceneMenu = null;
	}
	

	/**
	 * Maximize.
	 */
	public void maximize() {
		app.pushScene();
		app.addScene(scene);
		if (app.changeScene(scene)){
			maximized = true;
			
			this.addSceneMenu();
		}
	}
	
	/**
	 * Restore.
	 * 
	 * @return true, if successful
	 */
	public boolean restore(){
		if(app.popScene() /*&& app.removeScene(scene)*/){
			maximized = false;
			
			//FIXME TEST remove the in-scene window menu
			if (sceneMenu != null){
				this.sceneMenu.setVisible(false);
				this.sceneMenu.removeFromScene();
			}
			return true;
		}else{
			return false;
		}
	}
	

	@Override
	protected void destroyComponent() {
		super.destroyComponent();
		
		this.getFbo().destroy();
		
		//Restore to pop to another scene before destroying this
		if (maximized){ 
			restore(); 
		}
		
//		//FIXME Destroy and remove scene -> can we safely do this? if the scene is used elsewhere -> problem
//		//Problem if sceneTexture is used in transitions and destroyed after -> scene is destroyed!!
		//But if we dont destroy it the scene could linger around forever if used elsewhere!
//		scene.destroy();
		
		//Destroy scene Menu 
		if (sceneMenu != null){
			this.sceneMenu.removeFromScene();
			sceneMenu.destroy();
		}
	}
	
	
	
	//TODO if windowed
	//- maximize
	//- close
	
	//TODO 
	//IF CLOSE:
	//popScene()
	//destroy() scene window
	//remove scene from MTApp
	//scene.destroy()
	
	//IF RESTORE:
	//popScene();
	//remove window menu from scene's canvas
	

	
	
	
	
}
