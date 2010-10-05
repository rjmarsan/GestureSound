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
package org.mt4j.components.visibleComponents.shapes;

import java.util.ArrayList;

import javax.media.opengl.GL;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.mt4j.components.MTComponent;
import org.mt4j.components.TransformSpace;
import org.mt4j.components.bounds.IBoundingShape;
import org.mt4j.components.bounds.OrientedBoundingBox;
import org.mt4j.components.visibleComponents.AbstractVisibleComponent;
import org.mt4j.components.visibleComponents.GeometryInfo;
import org.mt4j.input.gestureAction.DefaultDragAction;
import org.mt4j.input.gestureAction.DefaultRotateAction;
import org.mt4j.input.gestureAction.DefaultScaleAction;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.rotateProcessor.RotateProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.scaleProcessor.ScaleProcessor;
import org.mt4j.util.MT4jSettings;
import org.mt4j.util.MTColor;
import org.mt4j.util.animation.Animation;
import org.mt4j.util.animation.AnimationEvent;
import org.mt4j.util.animation.AnimationManager;
import org.mt4j.util.animation.IAnimationListener;
import org.mt4j.util.animation.MultiPurposeInterpolator;
import org.mt4j.util.camera.IFrustum;
import org.mt4j.util.math.ConvexQuickHull2D;
import org.mt4j.util.math.Matrix;
import org.mt4j.util.math.Ray;
import org.mt4j.util.math.Tools3D;
import org.mt4j.util.math.Vector3D;
import org.mt4j.util.math.Vertex;
import org.mt4j.util.opengl.GLConstants;
import org.mt4j.util.opengl.GLTexture;
import org.mt4j.util.opengl.GLTextureParameters;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

/**
 * Abstract superclass for all kinds of shapes defined by vertices.
 * 
 * @author Christopher Ruff
 */
public abstract class AbstractShape extends AbstractVisibleComponent {
	private static final Logger logger = Logger.getLogger(AbstractShape.class.getName());
	static{
		logger.setLevel(Level.ERROR);
		SimpleLayout l = new SimpleLayout();
		ConsoleAppender ca = new ConsoleAppender(l);
		logger.addAppender(ca);
	}
	
	/** Default gesture actions. */
	private static final IGestureEventListener defaultScaleAction 	= new DefaultScaleAction();
	
	/** Default gesture actions*. */
	private static final IGestureEventListener defaultRotateAction = new DefaultRotateAction();
	
	/** Default gesture actions*. */
	private static final IGestureEventListener defaultDragAction	= new DefaultDragAction();
	
	
	//Texture Stuff
	/** The texture enabled. */
	private boolean textureEnabled; 
	
	/** The texture mode. */
	private int textureMode; // set defaults!
	
	/** The texture image. */
	private PImage textureImage;

	/** The draw direct gl. */
	private boolean drawDirectGL;
	
	/** The use vb os. */
	private boolean useVBOs;
	
	/** The use display list. */
	private boolean useDisplayList;
	
	/** The geometry of this shape. */
	private GeometryInfo geometryInfo;
	
	/** The bounds global vertices dirty. */
	private boolean boundsGlobalVerticesDirty;
	
	
	/** The vertices global. */
	private Vertex[] verticesGlobal;
	
	/** global vertices dirty. */
	private boolean globalVerticesDirty;
	
	
	/**
	 * Creates a new shape with the vertices provided.
	 * 
	 * @param vertices the vertices
	 * @param pApplet the applet
	 */
	public AbstractShape(Vertex[] vertices, PApplet pApplet) {
		this(new GeometryInfo(pApplet, vertices), pApplet);
	}
	
	/**
	 * Creates a new geometry with the geometryInfo provided.
	 * 
	 * @param geometryInfo the geometry info
	 * @param pApplet the applet
	 */
	public AbstractShape(GeometryInfo geometryInfo, PApplet pApplet) {
		super(pApplet,"unnamed  AbstractShape", /*null,*/ null);
		
		//Initialize fields 
		if (MT4jSettings.getInstance().isOpenGlMode()){
			this.drawDirectGL = true;
		}else{
			this.drawDirectGL = false;
		}
		
		this.useVBOs 			= false;
		this.useDisplayList 	= false;
		this.textureMode = PConstants.NORMALIZED;
		this.setFillDrawMode(GL.GL_TRIANGLE_FAN);
		this.boundsGlobalVerticesDirty = true;
		this.boundsAutoCompute = true;
		
		this.setGeometryInfo(geometryInfo);
		
		//Default
		this.boundsBehaviour = BOUNDS_CHECK_THEN_GEOMETRY_CHECK;
//		this.bounds = new Vector3D[0];
		
		this.setDefaultGestureActions();
		
		globalVerticesDirty = true;//
	}
	
	/*
	//FIXME TODO switch drawBounds! put draw() into IBoundingShape!
	@Override
	public void postDraw(PGraphics g) {
		super.postDraw(g);
		
		if (this.getBoundingShape() instanceof OrientedBoundingBox){
			OrientedBoundingBox b = (OrientedBoundingBox)this.getBoundingShape();
			b.drawBounds(g);
		}
		else if (this.getBoundingShape() instanceof BoundsZPlaneRectangle){
			BoundsZPlaneRectangle b = (BoundsZPlaneRectangle)this.getBoundingShape();
			b.drawBounds(g);
		}
		else if (this.getBoundingShape() instanceof BoundingSphere){
			BoundingSphere b = (BoundingSphere)this.getBoundingShape();
			b.drawBounds(g);
		}
		
	}
	*/
	
	
	/*
	//Test for drawing bounding shape aligned to coordinate axis
	@Override
	public void postDrawChildren(PGraphics g) {
		super.postDrawChildren(g);
		
		if (this.getBoundingShape() instanceof BoundsZPlaneRectangle){
			BoundsZPlaneRectangle b = (BoundsZPlaneRectangle)this.getBoundingShape();
//			b.drawBounds(g);
			g.pushMatrix();
			g.pushStyle();
			g.fill(250,150,150,180);
			
			Vector3D[] v = b.getVectorsGlobal();
			float[] minMax = Tools3D.getMinXYMaxXY(v);
			
			g.beginShape();
			g.vertex(minMax[0], minMax[1], 0);
			g.vertex(minMax[2], minMax[1], 0);
			g.vertex(minMax[2], minMax[3], 0);
			g.vertex(minMax[0], minMax[3], 0);
			g.endShape();
//			
			g.popStyle();
			g.popMatrix();
		}
	}
	*/
	
	/**
	 * Assigns the default gesture to this component, drag, rotate, scale.
	 * <br>Gets called in the constructor.
	 * Can be overridden in subclasses to allow other/more default gestures.
	 */
	protected void setDefaultGestureActions(){
		this.registerInputProcessor(new RotateProcessor(this.getRenderer()));
		this.setGestureAllowance(RotateProcessor.class, true);
//		this.addGestureListener(RotateProcessor.class, defaultRotateAction);
		this.addGestureListener(RotateProcessor.class, new DefaultRotateAction());
		
		this.registerInputProcessor(new ScaleProcessor(this.getRenderer()));
		this.setGestureAllowance(ScaleProcessor.class, true);
//		this.addGestureListener(ScaleProcessor.class, defaultScaleAction);
		this.addGestureListener(ScaleProcessor.class, new DefaultScaleAction());
		
		this.registerInputProcessor(new DragProcessor(this.getRenderer()));
		this.setGestureAllowance(DragProcessor.class, true);
//		this.addGestureListener(DragProcessor.class, defaultDragAction);
		this.addGestureListener(DragProcessor.class, new DefaultDragAction());
	}
	
//////////////// BOUNDING STUFF ///////////////////////////////
	/** The bounding shape. */
	private IBoundingShape boundingShape;
	
	/** The Constant BOUNDS_ONLY_CHECK. */
	public static final int BOUNDS_ONLY_CHECK 					= 1;
	
	/** The Constant BOUNDS_CHECK_THEN_GEOMETRY_CHECK. */
	public static final int BOUNDS_CHECK_THEN_GEOMETRY_CHECK 	= 2;
	
	/** The Constant BOUNDS_DONT_USE. */
	public static final int BOUNDS_DONT_USE						= 3;
	
	//FIXME RENAME, KEEP OLD ONES BUT AS DPERECATED
//	BOUNDSBEHAVIOUR_USE_GEOMETRY
//	BOUNDSBEHAVIOUR_USE_BOUNDS_AND_GEOMETRY
//	BOUNDSBEHAVIOUR_USE_BOUNDS
	
//	/** The Constant BOUNDS_ONLY_CHECK. */
//	public static final int BOUNDS_ONLY_CHECK 					= 1;
//	
//	/** The Constant BOUNDS_CHECK_THEN_GEOMETRY_CHECK. */
//	public static final int BOUNDS_CHECK_THEN_GEOMETRY_CHECK 	= 2;
//	
//	/** The Constant BOUNDS_DONT_USE. */
//	public static final int BOUNDS_DONT_USE						= 3;
	
	/** The bounds picking behaviour. */
	private int boundsBehaviour;
	
	/** The bounds auto compute. */
	private boolean boundsAutoCompute;
	
	//TODO!!
//	@Override
	//INterface geometryNode mit getCenter getBounds etc? für Abstractshape und ShapeContainer?
//	public void addChild(AbstractShape b){  
//		
//	}
	
	/**
	 * Sets the bounds behaviour. The behaviour influences 
	 * calculations in methods like <code>getIntersectionLocal</code> (used in picking) and
	 * <code>getComponentContainsPointLocal</code>.
	 * Allowed values are:
	 * <ul>
	 * <li><code>AbstractShape.BOUNDS_ONLY_CHECK</code>  <br>
	 * -> Uses the shape's bounding shape for the calculations <br>
	 * => faster, more inaccurate
	 * <li><code>AbstractShape.BOUNDS_DONT_USE</code>  <br>
	 *  -> Uses the shape's geometry for the calculations <br>
	 *  => slower, more accurate
	 * <li><code>AbstractShape.BOUNDS_CHECK_THEN_GEOMETRY_CHECK</code>   <br>
	 * -> Uses the shape's bounding shape first, and then also checks the geometry at picking. (Default)<br>
	 * => compromise between the other two 
	 * </ul>
	 * 
	 * @param boundsBehaviour the new bounds behaviour
	 */
	public void setBoundsBehaviour(int boundsBehaviour){
		this.boundsBehaviour = boundsBehaviour;
	}
	
	/**
	 * Gets the bounds behaviour.
	 * 
	 * @return the bounds behaviour constant
	 */
	private int getBoundsBehaviour(){
		return this.boundsBehaviour;
	}
	
	
	//FIXME REMOVE THESE METHODS IN THE NEXT VERSION!
	/**
	 * Sets the bounds picking behaviour.
	 * @param boundsPickingBehaviour the new bounds picking behaviour
	 * 
	 * @deprecated
	 * Method was renamed! Use setBoundsBehaviour()!
	 */
	public void setBoundsPickingBehaviour(int boundsPickingBehaviour){
		this.boundsBehaviour = boundsPickingBehaviour;
	}
	//FIXME REMOVE THESE METHODS IN THE NEXT VERSION!
	/**
	 * Gets the bounds picking behaviour.
	 * 
	 * @return the bounds picking behaviour
	 * @deprecated
	 * Method was renamed! Use getBoundsBehaviour()!
	 */
	private int getBoundsPickingBehaviour(){
		return this.boundsBehaviour;
	}
	
	/**
	 * Sets the bounding shape.
	 * 
	 * @param boundingShape the new bounding shape
	 */
	public void setBoundingShape(IBoundingShape boundingShape){
		this.boundingShape = boundingShape;
		this.setBoundsGlobalDirty(true);
	}	
	
	/**
	 * Gets the bounding shape.
	 * 
	 * @return the bounding shape
	 */
	public IBoundingShape getBoundingShape(){
		return this.boundingShape;
	}
	
	/**
	 * Checks if is bounding shape set.
	 * @return true, if is bounding shape set
	 */
	public boolean isBoundingShapeSet(){
		return this.boundingShape != null;
	}
	
	/**
	 * Computes a default bounding box for the shape.
	 * This gets called after setting creating a shape and its setGeometryInfo method is called.
	 */
	protected IBoundingShape computeDefaultBounds(){
		return new OrientedBoundingBox(this);
	}
	
	/**
	 * Sets the bounds auto compute.
	 * 
	 * @param autoCompute the new bounds auto compute
	 */
	public void setBoundsAutoCompute(boolean autoCompute){
		this.boundsAutoCompute = autoCompute;
	}
	
	/**
	 * Checks if is bounds auto compute.
	 * 
	 * @return true, if is bounds auto compute
	 */
	public boolean isBoundsAutoCompute(){
		return this.boundsAutoCompute;
	}
////////////////BOUNDING STUFF ///////////////////////////////
	
	
	/**
	 * Sets a new geometryInfo with new vertices for this shape.
	 * <br>If running in OpenGL mode, this also creates new vertex buffers 
	 * for openGL use and eventually new Vertex Buffer Objects or 
	 * Displaylists depending on the objects settings! 
	 * So DONT create them (buffers or vbos) on the geometryinfo yourself manually, 
	 * prior to setting it here!
	 * <br>Also calls computeDefaultBounds() if setAutoComputeBounds() is true (default)
	 * to recreate the bounding shape.
	 * <br><strong>NOTE:</strong> Be aware, that an old geometryinfo of this shape may have 
	 * created VBOs or displaylists on the gfx card which we should delete if not needed
	 * anywhere else!
	 * 
	 * @param geometryInfo the geometry info
	 */
	public void setGeometryInfo(GeometryInfo geometryInfo){
		if (this.isUseDirectGL()){
			if (geometryInfo.getVertBuff() == null 	|| geometryInfo.getStrokeColBuff() == null){ 
				//new geometryinfo has no drawbuffers created yet -> create them!
				geometryInfo.generateOrUpdateBuffersLocal(this.getStyleInfo());
			}else if (this.geometryInfo != null && geometryInfo.equals(this.geometryInfo)){
				// old geometryinfo is the same than the new one -> assumimg change -> create new buffers!
				geometryInfo.generateOrUpdateBuffersLocal(this.getStyleInfo());
			}else{
				//the new geometryinfo already has opengl draw buffers and 
				//the old geometryinfo is null or not the same as the new one 
				//-> just use the new geometry's buffers without recreating!
				
				//TODO do the same check with displaylists and vbos!??!
				//
			}
			
			if (this.isUseVBOs()){
				geometryInfo.generateOrUpdateAllVBOs();
			}
			
			if (this.isUseDisplayList()){
				geometryInfo.generateDisplayLists(
						this.isTextureEnabled(),
						this.getTexture(),
						this.getFillDrawMode(), 
						this.isDrawSmooth(), 
						this.getStrokeWeight());
			}
		}
		
		this.geometryInfo = geometryInfo;
		
		if (this.isBoundsAutoCompute()){
			if (geometryInfo.getVertices().length >= 3){
				this.setBoundingShape(this.computeDefaultBounds());
			}else{
//				logger.error("Warning: could not compute bounds because too few vertices were supplied: " + this.getName() + " in " + this + " -> Setting boundingShape to null.");
				this.setBoundingShape(null);
			}
		}else{
			this.setBoundingShape(null);
		}

		//Sets the base matrix dirty, so that when inquiring info about
		//vertices, they get updated first
//		this.setLocalBaseMatrixDirty(true);
		this.globalVerticesDirty = true;
	}
	
	/**
	 * Gets the geometry info. The geometryinfo contains the 
	 * geometric information of this shape by managing the shapes
	 * vertices, OpenGL vertex buffer objects and OpenGL display list.  
	 * 
	 * @return the geometry info
	 * 
	 * the geometry information object of that shape
	 */
	public GeometryInfo getGeometryInfo() {
		return this.geometryInfo;
	}

	
	/**
	 * Sets new vertices for that shape.
	 * and generates new vertex arrays for opengl mode.
	 * <li>Re-computes and sets the shapes default bounding shape.
	 * 
	 * @param vertices the vertices
	 */
	public void setVertices(Vertex[] vertices){
		this.getGeometryInfo().reconstruct(
				vertices,
				this.getGeometryInfo().getNormals(), 
				this.getGeometryInfo().getIndices(), 
				this.isUseDirectGL(), 
				this.isUseVBOs(), 
				this.getStyleInfo());
		
		if (this.isBoundsAutoCompute()){
			if (geometryInfo.getVertices().length >= 3){
				this.setBoundingShape(this.computeDefaultBounds());
			}else{
//				logger.error("Warning: could not compute bounds because too few vertices were supplied: " + this.getName() + " in " + this + " -> Setting boundingShape to null.");
				this.setBoundingShape(null);
			}
		}else{
			this.setBoundingShape(null);
		}
		//Sets the base matrix dirty, so that when inquiring info about
		//vertices, they get updated first
//		this.setLocalBaseMatrixDirty(true);
		this.globalVerticesDirty = true;
	}
	
	
	/**
	 * Returns the vertices of this shape without any transformations applied
	 * <br> <b>Caution:</b> If you alter them in anyway, changes will only
	 * be consistent by calling the <code>setVertices(Vertex[])</code> method with the changes vertices
	 * as an argument!.
	 * 
	 * @return the untransformed vertices
	 */
	public Vertex[] getVerticesLocal(){
		return this.getGeometryInfo().getVertices();
	}
	
	/**
	 * Returns the vertices of this shape in real world (global) coordinates
	 * <br> <b>Caution:</b> If you alter them in anyway, changes will only
	 * be consistent if you call the setVertices() method of the shape.
	 * <br><b>Caution:</b>This operation is not cheap since all vertices are 
	 * first copied and then transformed!
	 * <br><b>Note:</b> if a shape as a lot of vertices this will increase memory usage considerably
	 * because a complete copy of the shapes vertices is made and kept!
	 * @return the vertices in global coordinate space
	 */
	public Vertex[] getVerticesGlobal(){
		this.updateVerticesGlobal();
		return this.verticesGlobal;
	}

	/**
	 * Updates the verticesglobal array of the shape by
	 * multipying them with the current shape's global matrix.<br>
	 * <br>This calculates the real world space coordinates and saves it
	 * in the verticesglobal array. These vertices can be used to test at picking
	 * or just to know the real world global coordinates of the vertices.
	 */
	private void updateVerticesGlobal(){
		if (this.globalVerticesDirty){ 
			Vertex[] unTransformedCopy = Vertex.getDeepVertexArrayCopy(this.getGeometryInfo().getVertices());
			//transform the copied vertices and save them in the vertices array
			this.verticesGlobal = Vertex.transFormArray(this.getGlobalMatrix(), unTransformedCopy);
			this.globalVerticesDirty = false;
		}
	}
	
	
	//TODO REMOVE?
	/**
	 * Sets the bounds global vertices dirty.
	 * 
	 * @param boundsWorldVerticesDirty the new bounds world vertices dirty
	 */
	private void setBoundsGlobalDirty(boolean boundsWorldVerticesDirty) {
		this.boundsGlobalVerticesDirty = boundsWorldVerticesDirty;
		IBoundingShape bounds = this.getBoundingShape();
		if (bounds != null){
			bounds.setGlobalBoundsChanged();
		}
	}

	
	/* (non-Javadoc)
	 * @see com.jMT.components.MTBaseComponent#setMatricesDirty(boolean)
	 */
	@Override
	public void setMatricesDirty(boolean baseMatrixDirty) {
		/* 
		 * Overridden, so the component is also informed of the need to update
		 * the bounds vertices
		 */
		if (baseMatrixDirty){
			this.setBoundsGlobalDirty(true);
			
			this.globalVerticesDirty	= true;
		}
//		System.out.println("Set pickmat dirty on obj: " + this.getName());
		super.setMatricesDirty(baseMatrixDirty);
	}


	
	/**
	 * Gets the vertex count.
	 * 
	 * @return the vertex count
	 * 
	 * the number of vertices for that shape
	 */
	public int getVertexCount(){
		return this.getGeometryInfo().getVertexCount();
	}

	/**
	 * Checks if is use direct gl.
	 * 
	 * @return true, if checks if is use direct gl
	 * 
	 * true, if the shape tries to draw itself with OpenGL commands
	 * rather than processing commands
	 */
	public boolean isUseDirectGL() {
		return this.drawDirectGL;
	}

	/**
	 * If set to true - which is the default if using the OpenGL render mode - 
	 * this shape will bypass processings rendering pipeline
	 * and use the OpenGL context directly for performance increases.<br>
	 * Setting this to false forces the use of the processing renderer.
	 * <p>
	 * If this is set to true, and additionally, setUseVBOs() is set to true, 
	 * the shape is drawn by using vertex buffer objects (VBO). <br>
	 * By calling setUseDisplayList(true) it is drawn using display lists.
	 *  
	 * @param drawPureGL the draw pure gl
	 */
	public void setUseDirectGL(boolean drawPureGL){
		if (MT4jSettings.getInstance().isOpenGlMode()){
			if (!this.isUseDirectGL()  
				&& drawPureGL //FIXME WHY WAS THIS MISSING? is there a reason not to put this condition?
				&& this.getGeometryInfo().getVertices() != null 
				&& this.getGeometryInfo().getVertexCount() > 0){
				//Generate buffers for opengl array use
				this.getGeometryInfo().generateOrUpdateBuffersLocal(this.getStyleInfo());
			}
			
			this.drawDirectGL = drawPureGL; 
			
			//Wrap the current texture into a gl texture object for openGl use
			if 	(this.drawDirectGL
				&& this.getTexture() != null 
				&& !(this.getTexture() instanceof GLTexture)
			){
				this.setTexture(this.getTexture());
			}
		}else{
			logger.error(this.getName() + " - Cant use direct GL mode if not in opengl mode! Object: " + this.getName());
			this.drawDirectGL = false;
		}
	}
	
	/**
	 * Checks if this shape is drawn using VBOs.
	 * 
	 * @return true, if checks if is use vbos
	 * 
	 * true, if the shape tries to draw itself with OpenGL Vertex Buffer Objects
	 */
	public boolean isUseVBOs() {
		return this.useVBOs;
	}
	
	/**
	 * <br>Tries to use Vertex Buffer Objects for displaying this shape.<br>
	 * You have to be in OpenGL mode and set <code>setDrawDirectGL(true)</code>first.
	 * 
	 * @param useVBOs the use vb os
	 */
	public void setUseVBOs(boolean useVBOs) {
		if (MT4jSettings.getInstance().isOpenGlMode() && this.isUseDirectGL()){ 
			if (!this.isUseVBOs()){
				this.getGeometryInfo().generateOrUpdateAllVBOs();
			}
			this.useVBOs = useVBOs;
		}else{
			logger.error(this.getName() + " - Cant use VBOs if not in opengl mode and setDrawDirectGL has to be set to true! Object: " + this.getName());
			this.useVBOs = false;
		}
	}
	

	/**
	 * Checks if is use display list.
	 * 
	 * @return true, if checks if is use display list
	 * 
	 * true, if the shape tries to draw itself with OpenGL display lists
	 */
	public boolean isUseDisplayList() {
		return this.useDisplayList;
	}
	
	/**
	 * Tries to use a opengl display list for rendering this shape.<br>
	 * You have to be in OpenGL mode and <code>setDrawDirectGL()</code> has to
	 * be set to "true" first!
	 * <br><strong>NOTE: </strong> the display list has to be created first
	 * to use it! This can be done by calling <code>generateDisplayLists</code>.
	 * Instead of these 2 steps we can also just call <code>generateAndUseDisplayLists()</code>
	 * <br><strong>NOTE: </strong> if the shape was using a display list before we should delete it before setting
	 * a new one!
	 * 
	 * @param useDisplayList the use display list
	 */
	public void setUseDisplayList(boolean useDisplayList) {
			if (MT4jSettings.getInstance().isOpenGlMode() && this.isUseDirectGL()){
				this.useDisplayList = useDisplayList;
				if (this.getGeometryInfo().getDisplayListIDs()[0] == -1 
					&& this.getGeometryInfo().getDisplayListIDs()[1] == -1	){
					logger.warn(this.getName() + " - Warning, no displaylists created yet on component: " + this.getName());
				}
			}else{
				logger.error(this.getName() + " - Cant set display lists if not in opengl mode and setDrawDirectGL has to be set to true! Object: " + this.getName());
				this.useDisplayList = false;
			}
	}
	
	
	/**
	 * Generates 2 openGL display lists for drawing this shape.
	 * <br>One for the interior (with textures etc.) and
	 * one for drawing the outline.
	 * <br><code>setUseDirectGL</code> has to be set to true first!
	 * <br>To use the display lists for drawing, call <code>setUseDisplayList()</code>
	 * This method only generates them!
	 * <br><strong>NOTE: </strong> if the shape was using a display list before we should delete it before setting
	 * a new one!
	 */
	public void generateDisplayLists(){
		if (MT4jSettings.getInstance().isOpenGlMode() && this.isUseDirectGL()){
			this.getGeometryInfo().generateDisplayLists(
					this.isTextureEnabled(), 
					this.getTexture(), 
					this.getFillDrawMode(), 
					this.isDrawSmooth(), 
					this.getStrokeWeight());
		}else{
			logger.error(this.getName() + " - Cannot create displaylist if not in openGL mode or if setUseDirectGL() hasnt been set to true!");
		}
	}

	/**
	 * Generates and uses openGL display lists for drawing this
	 * shape.
	 */
	public void generateAndUseDisplayLists(){
		this.generateDisplayLists();
		this.setUseDisplayList(true);
	}
	
	/**
	 * Deletes the displaylists of the object and sets
	 * setUseDisplayList() to false.
	 */
	public void disableAndDeleteDisplayLists(){
		this.getGeometryInfo().deleteDisplayLists();
		this.setUseDisplayList(false);
	}
	
	
	//FIXME move to TOols3D!?
	/**
	 * Calculates the 2D XY convex hull for this shape.
	 * 
	 * @return the convex hull xy global
	 */
	public Vector3D[] getConvexHullXYGlobal(){
		ArrayList<Vector3D> vers = new ArrayList<Vector3D>();
		Vertex[] transVerts = this.getVerticesGlobal();
		for (int i = 0; i < transVerts.length; i++) {
			Vertex vertex = transVerts[i];
			vers.add(vertex);
		}
		ArrayList<Vector3D> edgeList = ConvexQuickHull2D.getConvexHull2D(vers);
		return (edgeList.toArray(new Vertex[edgeList.size()]));
	}

	
	@Override
	public void setFillColor(MTColor color) {
		super.setFillColor(color);
		this.getGeometryInfo().setVerticesColorAll(color.getR(), color.getG(), color.getB(), color.getAlpha());
	}

	
	@Override
	public void setStrokeColor(MTColor strokeColor) {
		super.setStrokeColor(strokeColor);
		if (MT4jSettings.getInstance().isOpenGlMode() && this.isUseDirectGL())  
			this.getGeometryInfo().setStrokeColorAll(strokeColor.getR(), strokeColor.getG(), strokeColor.getB(), strokeColor.getAlpha());
	}
	
	
	/**
	 * Tells the shape to use its texture.
	 * A texture has to be set previously!
	 * 
	 * @param texture the texture
	 */
	public void setTextureEnabled(boolean texture){
		this.textureEnabled = texture;
	}
	
	/**
	 * Checks if is texture enabled.
	 * 
	 * @return true, if checks if is texture enabled
	 * 
	 * true, if the shape is to use a texture
	 */
	public boolean isTextureEnabled(){
		return this.textureEnabled;
	}
	
	
	//TODO alte GL-Texture löschen wenn vorhanden!!?
	//TODO there is a problem when we set a pimage texture and have > 1.0 texcoords for tiling 
	/**
	 * Sets a texture for this shape.
	 * <br>Uses the texture coordinates in the provided vertices for drawing.
	 * <br>If openGL mode is used, it also creates a GLTexture object.
	 * <br>For best compatibility, power of two texture dimensions should be provided.
	 * If the provided texture is non power of two and you are in opengl mode, we try
	 * to use the RECTANGULAR texture extension.
	 * <br>If textures were disabled for this component, they are being enabled again.
	 * 
	 * @param newTexImage the new tex image
	 */
	public void setTexture(PImage newTexImage){
		if (newTexImage == null){
			this.textureImage = null;
//			System.out.println("Set texture to null");
			return;
		}
		
		//Enable textures
		if (!this.isTextureEnabled())
			this.setTextureEnabled(true);
		
		boolean isPowerOfTwo = Tools3D.isPowerOfTwoDimension(newTexImage);
		
		if (this.textureImage != null){ //Shape already has a texture
			
			boolean hasSameDimensions = (this.textureImage.width  == newTexImage.width 
									  && this.textureImage.height == newTexImage.height);
			
			if (this.isUseDirectGL()){
				if (this.textureImage instanceof GLTexture){
					//Old texture is instance of GLTexture object
					GLTexture oldGLTex = (GLTexture)this.textureImage;
					
//					if (hasSameDimensions){
//						//Old GLTexture obj has same dimension -> just put new pimage and pixels into texture obj
//						oldGLTex.putImage(newTexImage);
//						//TODO delete old texture?
//					}else{
						//Old gl texture doesnt have same dimensions -> make new Texture 
						/*
						GLTextureParameters p = new GLTextureParameters();
			        	p.format 	= oldGLTex.getTextureParams().format;
			        	p.magFilter = oldGLTex.getTextureParams().magFilter;
			        	p.minFilter = oldGLTex.getTextureParams().minFilter;
			        	p.target 	= oldGLTex.getTextureParams().target;
			        	GLTexture newTex = new GLTexture(this.getRenderer(), newTexImage.width, newTexImage.height, p);
						newTex.putImage(textureImage);
						
						this.textureImage = newTex;
						*/
					
						//TODO delete old glTexture object?
						//generateDefault a GLTexture object for use with pure openGL
						GLTextureParameters tParams = new GLTextureParameters();
						if (!isPowerOfTwo){
							tParams.target = GLConstants.RECTANGULAR;
							//We have to scale the texture coordinates from 0..1 to 0..width
//							
							//If old gltexture was rectangular we assume that the textcoords were scaled to the images dimensions (instead of normalzied)
							//so we rescale accordingly
							if (oldGLTex.getTextureParams().target == GLTexture.RECTANGULAR){
								this.scaleTextureCoordsForRectModeFromRectMode(this.textureImage, newTexImage, this.getGeometryInfo().getVertices());
							}else{
								Tools3D.scaleTextureCoordsForRectModeFromNormalized(newTexImage, this.getGeometryInfo().getVertices());
							}
							
							this.setTextureMode(PConstants.IMAGE);
							
							//Update the texture buffer!
							this.getGeometryInfo().updateTextureBuffer(this.isUseVBOs());
//							System.out.println("Non power of two texture detected in object: " + this.getName());
						}else{
//							this.setTextureMode(PConstants.NORMALIZED);
							//System.out.println("Power of two texture in: " + this.getName());
						}
						
						//Dont use mipmaps by default
						tParams.minFilter = GLTextureParameters.LINEAR;
						tParams.magFilter = GLTextureParameters.LINEAR;
						
						//Initialize emtpy texture
						GLTexture newGLTexture = new GLTexture(this.getRenderer(), newTexImage.width, newTexImage.height, tParams);
						//Fill the texture with pixels
						newGLTexture.putImage(newTexImage);
						newGLTexture.setFlippedY(true); //?
						newGLTexture.format = newTexImage.format;  
						this.textureImage = newGLTexture;
//					}
				}else{
					//Old texture isnt gltexture but this obj is in directGL mode ->create new gltexture
					GLTextureParameters tParams = new GLTextureParameters();
					if (!isPowerOfTwo){
						tParams.target = GLConstants.RECTANGULAR;
						//We have to scale the texture coordinates from 0..1 to 0..width
						Tools3D.scaleTextureCoordsForRectModeFromNormalized(newTexImage, this.getGeometryInfo().getVertices());
						this.setTextureMode(PConstants.IMAGE);
						
						//Update the texture buffer!
						this.getGeometryInfo().updateTextureBuffer(this.isUseVBOs());
//						System.out.println("Non power of two texture detected in object: " + this.getName());
					}else{
//						this.setTextureMode(PConstants.NORMALIZED);
						//System.out.println("Power of two texture in: " + this.getName());
					}
					
					//Dont use mipmaps by default
					tParams.minFilter = GLTextureParameters.LINEAR;
					tParams.magFilter = GLTextureParameters.LINEAR;
					
					//Initialize emtpy texture
					GLTexture newGLTexture = new GLTexture(this.getRenderer(), newTexImage.width, newTexImage.height, tParams);
					//Fill the texture with pixels
					newGLTexture.putImage(newTexImage);
					newGLTexture.setFlippedY(true); //?
					newGLTexture.format = newTexImage.format;  
					this.textureImage = newGLTexture;
				}
			}else{
				//Had old texture but this shape isnt set to use direct gl mode ->just set new pimage
				this.textureImage = newTexImage;
			}
		}else{
			//Didnt have previously set texture -> check if were in direct gl mode
//			System.out.println(this.getName() + "didnt have previous texture");
			
			if (this.isUseDirectGL()){
//				System.out.println(this.getName() + "hast direct gl mode set");
				
				//Shape is set to use direct gl mode -> check if the new texture is already a gltexture object instance
				if (newTexImage instanceof GLTexture){
					//New texture is already gltexture -> just set it
					this.textureImage = newTexImage;
				}else{
					//New texture isnt of GLTexture but we should use direct gl mode -> create one
					GLTextureParameters tParams = new GLTextureParameters();
					if (!isPowerOfTwo){
						tParams.target = GLConstants.RECTANGULAR;
						//We have to scale the texture coordinates from 0..1 to 0..width
						Tools3D.scaleTextureCoordsForRectModeFromNormalized(newTexImage, this.getGeometryInfo().getVertices());
						this.setTextureMode(PConstants.IMAGE);
						
						//Update the texture buffer!
						this.getGeometryInfo().updateTextureBuffer(this.isUseVBOs());
//						System.out.println("Non power of two texture detected in object: " + this.getName());
					}else{
//						this.setTextureMode(PConstants.NORMALIZED);
						//System.out.println("Power of two texture in: " + this.getName());
					}
					
					//Dont use mipmaps by default
					tParams.minFilter = GLTextureParameters.LINEAR;
					tParams.magFilter = GLTextureParameters.LINEAR;
					
					//Initialize emtpy texture
					GLTexture newGLTexture = new GLTexture(this.getRenderer(), newTexImage.width, newTexImage.height, tParams);
					//Fill the texture with pixels
					newGLTexture.putImage(newTexImage);
					newGLTexture.setFlippedY(true); //?
					newGLTexture.format = newTexImage.format;  
					this.textureImage = newGLTexture;
				}
			}else{
				//Didnt have old texture and this shape isnt set to use direct gl mode -> just set new pimage
				this.textureImage = newTexImage;
			}
		}
	}
	
	
	/**
	 * Rescale tex coords form old texture in rectangular mode to new.
	 * 
	 * @param oldTexture the old texture
	 * @param newTexture the new texture
	 * @param verts the verts
	 */
	private void scaleTextureCoordsForRectModeFromRectMode(PImage oldTexture, PImage newTexture, Vertex[] verts){
			for (int i = 0; i < verts.length; i++) {
				Vertex vertex = verts[i];
					vertex.setTexCoordU( (vertex.getTexCoordU()/oldTexture.width) * newTexture.width );
					vertex.setTexCoordV( (vertex.getTexCoordV()/oldTexture.height) *  newTexture.height);
			}
	}
	
	/*//rectangular works even if dimensions is power of two so..not neccesary
	private void scaleTextureCoordsForNormalizedModeFromRect(PImage texture, Vertex[] verts){
		//TODO
	}
	*/
	
	/**
	 * Gets the texture.
	 * 
	 * @return the texture
	 * 
	 * the texture object associated with this shape (either a PImage or GLTexture obj)
	 */
	public PImage getTexture() {
		return this.textureImage;
	}

	/**
	 * Sets the way texture coordinates are handled in processing. This setting
	 * is not considered if using OpenGL mode!
	 * Allowed values are: <code>PApplet.NORMALIZED</code> and <code>PApplet.IMAGE</code>
	 * <br>Default is <code>PApplet.NORMALIZED</code>.
	 * Which indicates that the texture coordinates should be in normalized
	 * range from 0.0 to 1.0!
	 * In image mode they have to range from 0..imageDimensions.
	 * 
	 * @param textureMode the texture mode
	 */
	public void setTextureMode(int textureMode){
		this.textureMode = textureMode;
	}
	
	/**
	 * Gets the processing texture mode.
	 * 
	 * @return the texture mode
	 */
	public int getTextureMode(){
		return this.textureMode;
	}
	
	
	/**
	 * Sets the global position of the component. (In global coordinates)
	 * 
	 * @param pos the pos
	 */
	public void setPositionGlobal(Vector3D pos){
		this.translateGlobal(pos.getSubtracted(this.getCenterPointGlobal()));
	}
	
	/**
	 * Sets the position of the component, relative to its parent coordinate frame.
	 * 
	 * @param pos the pos
	 */
	public void setPositionRelativeToParent(Vector3D pos){
		this.translate(pos.getSubtracted(this.getCenterPointRelativeToParent()), TransformSpace.RELATIVE_TO_PARENT);
	}
	
	/**
	 * Sets the position of this component, relative to the other specified component.
	 * 
	 * @param otherComp the other comp
	 * @param pos the pos
	 */
	public void setPositionRelativeToOther(MTComponent otherComp, Vector3D pos){
		Matrix m0 = MTComponent.getTransformToDestinationLocalSpace(otherComp, this);
		pos.transform(m0);
		
		Vector3D centerpointGlobal = this.getCenterPointGlobal();
		centerpointGlobal.transform(this.getGlobalInverseMatrix()); //to localobj space
		centerpointGlobal.transform(this.getLocalMatrix()); //to parent relative space
		
		Vector3D diff = pos.getSubtracted(centerpointGlobal);
		
		this.translate(diff, TransformSpace.RELATIVE_TO_PARENT);
		
//		Vector3D globalCenter = this.getCenterPointGlobal();
//		
//		Vector3D localObjCenter = this.getCenterPointGlobal();
//		localObjCenter.transform(this.getAbsoluteWorldToLocalMatrix()); //to localobj space
////		localObjCenter.transform(this.getLocalBasisMatrix()); //to parent relative space
//		
//		
////		pos = MTBaseComponent.getWorldVecToParentRelativeSpace(otherComp, pos);
////		pos.transform(otherComp.getLocalBasisMatrix());
////		pos = MTBaseComponent.getWorldVecToLocalRelativeSpace(otherComp, pos);
//		
////		pos.transform(otherComp.getAbsoluteLocalToWorldMatrix());
//		
//		Matrix m = MTBaseComponent.getTransformToDestinationLocalSpace(this, otherComp);
////		m.multLocal(otherComp.getLocalBasisMatrix());
//		Matrix m2 = MTBaseComponent.getTransformToDestinationParentSpace(this, otherComp);
////		pos.transform(m);
//		
//		/*
//		
////		localObjCenter.transform(m2);
////		System.out.println("Localobjcenter transformed to other: " + localObjCenter);
//		
//		Vector3D diff = pos.minus(localObjCenter);
//		diff.transformDirectionVector(m2);
//		this.translateGlobal(diff);
//		*/
//		
//		
////		pos.transform(otherComp.getAbsoluteLocalToWorldMatrix());
////		localObjCenter.transform(this.getAbsoluteLocalToWorldMatrix());
////		localObjCenter.transform(otherComp.getAbsoluteWorldToLocalMatrix());
////		Vector3D diff = pos.minus(localObjCenter);
//		
//		Vector3D centerPoint = this.getBoundingShape().getCenterPointLocal();
////		centerPoint.transform(this.getLocalBasisMatrix());
//		
//		//Muss punkt nach svgComp space transformieren, da diese comp gescaled wird
//		centerPoint.transform(m);
//		pos.transform(otherComp.getAbsoluteLocalToWorldMatrix());
//		Vector3D diff = pos.minus(centerPoint);
//		
//		System.out.println("Point from where to start: " + localObjCenter);
//		System.out.println("Destination pos: " + pos);
//		System.out.println("Translation vector: " + diff);
////		Vector3D diff = pos.minus(localObjCenter);
////		diff.transformDirectionVector(m);
//		
//		this.translate(diff, TransformSpace.RELATIVE_TO_PARENT);
	}
	
	
	/* (non-Javadoc)
	 * @see com.jMT.components.visibleComponents.AbstractVisibleComponent#getComponentIntersectionPoint(util.math.Ray)
	 */
	@Override
	public Vector3D getIntersectionLocal(Ray ray) {
		switch (this.getBoundsBehaviour()) {
		case AbstractShape.BOUNDS_DONT_USE:
//			System.out.println("\"" + this.getName() + "\": -> GEOMETRY only check");
			return this.getGeometryIntersectionLocal(ray);
		case AbstractShape.BOUNDS_ONLY_CHECK:
			if (this.isBoundingShapeSet()){
//				System.out.println("\"" + this.getName() + "\": -> BOUNDS only check");
				return this.getBoundingShape().getIntersectionLocal(ray);
			}else{
//				System.out.println("\"" + this.getName() + "\": -> GEOMETRY only check");
				return this.getGeometryIntersectionLocal(ray);
			}
		case AbstractShape.BOUNDS_CHECK_THEN_GEOMETRY_CHECK:
			if (this.isBoundingShapeSet()){
//				System.out.println("\"" + this.getName() + "\": -> BOUNDS check then GEOMETRY check");
				Vector3D boundsIntersection = this.getBoundingShape().getIntersectionLocal(ray);
				if (boundsIntersection != null){
					return this.getGeometryIntersectionLocal(ray);
				}else{
					return null;
				}
			}else{
				return this.getGeometryIntersectionLocal(ray);
			}
		default:
			break;
		}
		return null;
	}
	
	
	/* (non-Javadoc)
	 * @see com.jMT.components.visibleComponents.AbstractVisibleComponent#componentContainsPoint(util.math.Vector3D)
	 */
	@Override
	protected boolean componentContainsPointLocal(Vector3D testPoint) {
		switch (this.getBoundsBehaviour()) {
		case AbstractShape.BOUNDS_DONT_USE:
//			System.out.println("\"" + this.getName() + "\": -> GEOMETRY only check");
			return this.isGeometryContainsPointLocal(testPoint);
		case AbstractShape.BOUNDS_ONLY_CHECK:
			if (this.isBoundingShapeSet()){
//				System.out.println("\"" + this.getName() + "\": -> BOUNDS only check");
				return this.getBoundingShape().containsPointLocal(testPoint);
			}else{
//				System.out.println("\"" + this.getName() + "\": -> GEOMETRY only check");
				return this.isGeometryContainsPointLocal(testPoint);
			}
		case AbstractShape.BOUNDS_CHECK_THEN_GEOMETRY_CHECK:
			if (this.isBoundingShapeSet()){
//				System.out.println("\"" + this.getName() + "\": -> BOUNDS check then GEOMETRY check");
				if (this.getBoundingShape().containsPointLocal(testPoint)){
					return this.isGeometryContainsPointLocal(testPoint);
				}else{
					return false;
				}
			}else{
				return this.isGeometryContainsPointLocal(testPoint);
			}
		default:
			break;
		}
		return false;
	}
	
	
	/* (non-Javadoc)
	 * @see org.mt4j.components.MTComponent#isContainedIn(util.camera.IFrustum)
	 */
	@Override
	public boolean isContainedIn(IFrustum frustum){
		//Check if bounds are contained in the frustum
		//if shape has no boundingshape return true by default
		if (this.getBoundingShape() != null){
			return this.getBoundingShape().isContainedInFrustum(frustum);
		}else{
			return true;
		}
	}
	
	/**
	 * Tests if the ray intersects the shape and where.
	 * The ray is assumed to be transformed to local space already!
	 * 
	 * @param ray the ray
	 * 
	 * @return the geometry intersection
	 * 
	 * the intersection point or null if no intersection occured
	 */
	abstract public Vector3D getGeometryIntersectionLocal(Ray ray);
	
	/**
	 * Tests is the geometry of the shape contains the given point.
	 * The testpoint is assumed to be transformed to local space already!
	 * 
	 * @param testPoint the test point
	 * 
	 * @return true, if checks if is geometry contains point
	 */
	abstract public boolean isGeometryContainsPointLocal(Vector3D testPoint);
	
	/**
	 * Gets the center point global.
	 * First it gets the local center and then transforms it to the global frame.
	 * 
	 * @return the center point global
	 * 
	 * the center of this shape in global coordinates
	 */
	public final Vector3D getCenterPointGlobal(){
		Vector3D center = this.getCenterPointLocal();
		center.transform(this.getGlobalMatrix());
		return center;
	}
	
	/**
	 * Gets the center point relative to parent.
	 * First it gets the local center and then transforms it to the parent frame.
	 * @return the center of this shape in coordinates relative to the shapes parent coordiante frame.
	 */
	public final Vector3D getCenterPointRelativeToParent(){
		Vector3D center = this.getCenterPointLocal();
		center.transform(this.getLocalMatrix());
		return center;
	}
	
	/**
	 * Gets the center point in local object space.
	 * This should always return a COPY of the centerpoint of the implementing shape
	 * since the point may get transformed afterwards.
	 * @return  the center point of this shape in untransformed local object coordinates.
	 */
	abstract public Vector3D getCenterPointLocal();

	
	
	/**
	 * <li>Removes this component from its parent.
	 * <li>Calls <code>destroyComponent</code> on this component which
	 * can be used to free resources that the component used.
	 * <li>Recursively calls destroy() on its children
	 * <br>
	 * <p>
	 * By default, the openGl texture object and the VBOs associated with this shape will be deleted.
	 * Be careful when you share textures or VBOs across more than one object!
	 * Destroying of displaylists isnt done atm! Use disableAndDeleteDisplaylists() instead.
	 */
	@Override
	public void destroy(){
//		System.out.println(this + " -> DESTROY() -> (AbstractShape)");
		
		//FIXME if vbos or display lists are shared, they shouldnt be deleted!
		//right now, destroying of displaylists isnt done. Use disableAndDeleteDisplaylists() instead.
		
		if (this.geometryInfo != null){
			//Delete VBOs
			this.getGeometryInfo().deleteAllVBOs();
		}
		
		this.destroyDisplayLists();
		
		/*
		//Delete displaylist
		this.disableAndDeleteDisplayLists();
		*/
		
//		this.geometryInfo = null; //FIXME TEST
//		this.boundingShape = null;
		this.setBoundingShape(null);
		
		//Delete openGL texture object
		if (this.getTexture() instanceof GLTexture){
			GLTexture tex = (GLTexture) this.getTexture();
			//Delete texture
			tex.deleteTextureGL();
			this.setTexture(null);
			this.setTextureEnabled(false);
		} 
		super.destroy();
	}
	
	
	/**
	 * This is called during the shape's destroy() method.
	 * Override this and leave it empty if you dont want the 
	 * display list destroyed in your component 
	 * (makes sense with shared geometry infos/display lists)
	 */
	protected void destroyDisplayLists(){
//		/*
		//Delete displaylist
		this.disableAndDeleteDisplayLists();
//		*/
	}
	
	/* (non-Javadoc)
	 * @see com.jMT.components.MTBaseComponent#destroyComponent()
	 */
	@Override
	abstract protected void destroyComponent();
	
	/**
	 * Get the width of the shape in the XY-Plane. Uses the x and y coordinate
	 * values for calculation. Usually the calculation is delegated to the shapes
	 * bounding shape.
	 * 
	 * @param transformSpace the space the width is calculated in, can be global space, parent relative- or object space
	 * 
	 * @return the width xy
	 * 
	 * the width
	 */
	abstract public float getWidthXY(TransformSpace transformSpace);
	
	/**
	 * Get the height of the shape in the XY-Plane. Uses the x and y coordinate
	 * values for calculation. Usually the calculation is delegated to the shapes
	 * bounding shape.
	 * 
	 * @param transformSpace the space the width is calculated in, can be world space, parent relative- or object space
	 * 
	 * @return the height xy
	 * 
	 * the height
	 */
	abstract public float getHeightXY(TransformSpace transformSpace);
	
	
	
	/**
	 * Moves this shape to the specified global position using an animation specified 
	 * by the last three parameters
	 * 
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param interpolationDuration the interpolation duration
	 * @param accelerationEndTime the acceleration end time
	 * @param decelerationStartTime the deceleration start time
	 */
	public void tweenTranslateTo(float x, float y, float z, float interpolationDuration, float accelerationEndTime, float decelerationStartTime){
		Vector3D from 			= this.getCenterPointGlobal();
		Vector3D targetPoint 	= new Vector3D(x, y, z);
		Vector3D directionVect 	= targetPoint.getSubtracted(from);
		
		//GO through all animations for this shape
		Animation[] animations = AnimationManager.getInstance().getAnimationsForTarget(this);
		for (int i = 0; i < animations.length; i++) {
			Animation animation = animations[i];
			
			//Go through all listeners of these animations
			IAnimationListener[] animationListeners = animation.getAnimationListeners();
			for (int j = 0; j < animationListeners.length; j++) {
				IAnimationListener listener = animationListeners[j];
				//IF a listener is a TranslationAnimationListener the animations is a translationTween 
				//and should be stopped before doing this new animation
				if (listener instanceof TranslationAnimationListener)
					animation.stop();
			}
		}
		this.tweenTranslate(directionVect, interpolationDuration, accelerationEndTime, decelerationStartTime);
	}
	
	/**
	 * Moves this shape in the specified direction with an animation specified by the other parameters.
	 * 
	 * @param directionVect the direction vect
	 * @param interpolationDuration the interpolation duration
	 * @param accelerationEndTime the acceleration end time
	 * @param decelerationStartTime the deceleration start time
	 */
	public void tweenTranslate(Vector3D directionVect, float interpolationDuration, float accelerationEndTime, float decelerationStartTime){
		this.tweenTranslate(directionVect, interpolationDuration, accelerationEndTime, decelerationStartTime, 0);
	}
	
	/**
	 * Tween translate.
	 * 
	 * @param directionVect the direction vect
	 * @param interpolationDuration the interpolation duration
	 * @param accelerationEndTime the acceleration end time
	 * @param decelerationStartTime the deceleration start time
	 * @param triggerDelay the trigger delay
	 */
	public void tweenTranslate(Vector3D directionVect, float interpolationDuration, float accelerationEndTime, float decelerationStartTime, int triggerDelay){
		float distance = directionVect.length();
		MultiPurposeInterpolator interpolator = new MultiPurposeInterpolator(0, distance, interpolationDuration , accelerationEndTime, decelerationStartTime , 1);
		Animation animation = new Animation("Tween translate of " + this.getName(), interpolator, this, triggerDelay);
		animation.addAnimationListener(new TranslationAnimationListener(this, directionVect));
		animation.setResetOnFinish(false);
		animation.start();
	}
	
	/**
	 * This private class acts as an AnimationListener for translation animations.
	 * 
	 * @author C.Ruff
	 */
	private class TranslationAnimationListener implements IAnimationListener{
		/** The direction vector. */
		private Vector3D directionVector;
		
		/** The normalized dir vect. */
		private Vector3D normalizedDirVect;
		
		/** The shape. */
		private AbstractShape shape;

		/**
		 * Instantiates a new translation animation listener.
		 * 
		 * @param shape the shape
		 * @param directionVector the direction vector
		 */
		public TranslationAnimationListener(AbstractShape shape, Vector3D directionVector){
			this.directionVector = directionVector;
			this.normalizedDirVect = this.directionVector.getCopy();
			this.normalizedDirVect.normalizeLocal();
			this.shape = shape;
		}
		
		/* (non-Javadoc)
		 * @see util.animation.IAnimationListener#processAnimationEvent(util.animation.AnimationEvent)
		 */
		public void processAnimationEvent(AnimationEvent ae) {
			Object target = ae.getTargetObject();
			if (target != null && target.equals(this.shape)){
				AbstractShape shape = (AbstractShape)target;
				float amount = ae.getAnimation().getInterpolator().getCurrentStepDelta();
				
				Vector3D newTranslationVect = this.normalizedDirVect.getCopy();
				newTranslationVect.scaleLocal(amount);
				//Move shape
//				shape.translateGlobal(newTranslationVect);
				shape.translate(newTranslationVect);
			}
		}
	}
	
}


