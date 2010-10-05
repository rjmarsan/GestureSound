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

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;

import org.mt4j.components.TransformSpace;
import org.mt4j.components.bounds.BoundingSphere;
import org.mt4j.components.bounds.IBoundingShape;
import org.mt4j.components.bounds.OrientedBoundingBox;
import org.mt4j.components.visibleComponents.GeometryInfo;
import org.mt4j.util.MT4jSettings;
import org.mt4j.util.MTColor;
import org.mt4j.util.math.BezierVertex;
import org.mt4j.util.math.Ray;
import org.mt4j.util.math.Tools3D;
import org.mt4j.util.math.ToolsIntersection;
import org.mt4j.util.math.Vector3D;
import org.mt4j.util.math.Vertex;
import org.mt4j.util.opengl.GLTexture;

import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * This class represents a planar, convex polygon. The user of this class
 * is responsible for the polygon being planar and convex.
 * Methods like picking and others depend on these facts.
 * If <code>setNoFill(true)</code> is used and the polygon isnt closed, the
 * class can also be used to display a poly-line.
 * 
 * @author Christopher Ruff
 */
public class MTPolygon extends AbstractShape {
	
	//FIXME TRIAL REMOVE LATER
//	boolean useLocalObjectSpace;
	
	private Vector3D normal;
	private boolean normalDirty;
	
	/** save the first vertex color to check if color is uniform throught the polygon **/
	private float[] firstVertexColor;
	
	/**
	 * Instantiates a new mT polygon.
	 * 
	 * @param pApplet the applet
	 * @param vertices the vertices
	 */
	public MTPolygon(PApplet pApplet, Vertex[] vertices) { //Added for consitency
		this(vertices, pApplet);
	}
	
	/**
	 * Instantiates a new mT polygon.
	 * 
	 * @param vertices the vertices
	 * @param pApplet the applet
	 */
	public MTPolygon(Vertex[] vertices, PApplet pApplet) {
		super(vertices, pApplet);
		
		this.normalDirty = true;
//		this.normal = this.getNormal();
		
		this.setTextureEnabled(false);
		this.setTextureMode(PApplet.NORMALIZED);
		
		this.setEnabled(true);
		this.setVisible(true);
		
//		this.setDraggable(true);
//		this.setRotatable(true);
//		this.setSelectable(true);
		
		this.setDrawSmooth(true);
		this.setNoStroke(false);
		this.setNoFill(false);
		this.setName("Polygon");
		
//		useLocalObjectSpace = false; 
//		useLocalObjectSpace = true;
		
		this.setBoundsBehaviour(AbstractShape.BOUNDS_DONT_USE);
//		this.setBoundsPickingBehaviour(AbstractShape.BOUNDS_ONLY_CHECK);
	}
	
	/* (non-Javadoc)
	 * @see com.jMT.components.visibleComponents.shapes.AbstractShape#computeDefaultBounds()
	 */
	@Override
	protected IBoundingShape computeDefaultBounds(){
//		this.setBoundingShape(new BoundsArbitraryPlanarPolygon(this, this.getVerticesObjSpace())); //Works inly in z=0
		return new BoundingSphere(this);
//		return new BoundingSphere(new OrientedBoundingBox(this);
	}
	
	
	@Override
	public void setGeometryInfo(GeometryInfo geometryInfo) {
		super.setGeometryInfo(geometryInfo);
		this.normalDirty = true;
	}

	@Override
	public void setVertices(Vertex[] vertices) {
		super.setVertices(vertices);
		this.normalDirty = true;
	}

	/* (non-Javadoc)
	 * @see com.jMT.components.visibleComponents.AbstractVisibleComponent#drawComponent()
	 */
	@Override
	public void drawComponent(PGraphics g) {
//		super.drawComponent(g);
		
		PApplet renderer = this.getRenderer();
				
		//Draw the shape
		if (MT4jSettings.getInstance().isOpenGlMode()   
		   && this.isUseDirectGL()){
			GL gl = Tools3D.beginGL(renderer);
			
			//Draw with PURE opengl
			if (this.isUseDisplayList() /*&& this.getDisplayListIDs() != null && this.getDisplayListIDs()[0] != -1 && this.getDisplayListIDs()[1] != -1*/){
				int[] displayLists = this.getGeometryInfo().getDisplayListIDs();
				//Use Display Lists
				if (!this.isNoFill()) 
					gl.glCallList(displayLists[0]); //Draw fill
				if (!this.isNoStroke())
					gl.glCallList(displayLists[1]); //Draw outline
			}else{
				//Use Vertex Arrays or VBOs
				this.drawPureGl(gl);
			}
			Tools3D.endGL(renderer);
			
		}else{ //Draw with pure proccessing commands...
			MTColor fillColor = this.getFillColor();
			MTColor strokeColor = this.getStrokeColor();
			g.fill(fillColor.getR(), fillColor.getG(), fillColor.getB(), fillColor.getAlpha());
			g.stroke(strokeColor.getR(), strokeColor.getG(), strokeColor.getB(), strokeColor.getAlpha());
			g.strokeWeight(this.getStrokeWeight());

			if (MT4jSettings.getInstance().isOpenGlMode())
				if (this.isDrawSmooth()) 
					g.smooth();
				else 			
					g.noSmooth();

			//NOTE: if noFill() and noStroke()->absolutely nothing will be drawn-even when texture is set
			if (this.isNoFill())	
				g.noFill();
			if (this.isNoStroke()) 	
				g.noStroke();

			//Set the tint values 
			g.tint(fillColor.getR(), fillColor.getG(), fillColor.getB(), fillColor.getAlpha());

			//handles the drawing of the vertices with the texture coordinates
			//try doing a smoothed poly outline with opengl
			if (MT4jSettings.getInstance().isOpenGlMode()  
				&& this.isDrawSmooth()
				&& !this.isNoStroke()
				&& !this.isUseDirectGL()
			){
				g.noStroke();
				g.noSmooth();
				//draw insided of polygon, without smooth or stroke
				drawWithProcessing(g); 

				g.smooth();
				g.noFill(); 
				g.stroke(strokeColor.getR(), strokeColor.getG(), strokeColor.getB(), strokeColor.getAlpha());

				// DRAW SMOOTHED THE OUTLINE SHAPE OF THE POLYGON WIHTOUT FILL OR TEXTURE
				drawWithProcessing(g); 

				g.noSmooth();
				//restore fill color
				g.fill(fillColor.getR(), fillColor.getG(), fillColor.getB(), fillColor.getAlpha());
			}else{
				drawWithProcessing(g);
			}//end if gl and smooth

			//reSet the tint values to defaults 
			g.tint(255, 255, 255, 255);

			if (MT4jSettings.getInstance().isOpenGlMode() && this.isDrawSmooth())
				g.noSmooth(); //because of tesselation bug
		}
		
	}
	
	
	/**
	 * loops through all the vertices of the polygon
	 * and uses processings "vertex()" command to set their position
	 * and texture.
	 * @param g PGraphics
	 */
	private void drawWithProcessing(PGraphics g){
		g.beginShape(PApplet.POLYGON); //TODO make setbeginshape() behavior settable
		if (this.getTexture() != null && this.isTextureEnabled()){
			g.texture(this.getTexture());
			g.textureMode(this.getTextureMode());
		}
		Vertex[] vertices = this.getVerticesLocal();
		
		if (firstVertexColor == null){
			firstVertexColor = new float[4];
		}
		
		for (int i = 0; i < vertices.length; i++) {
			Vertex v = vertices[i];
			
			//Check if we have uniform color or have to use different colors for different vertices
			if (i == 0){
				firstVertexColor[0] = v.getR();
				firstVertexColor[1] = v.getG();
				firstVertexColor[2] = v.getB();
				firstVertexColor[3] = v.getA();
				g.fill(firstVertexColor[0], firstVertexColor[1], firstVertexColor[2], firstVertexColor[3]);
			}else{
				if (       firstVertexColor[0] != v.getR()
						|| firstVertexColor[1] != v.getG()
						|| firstVertexColor[2] != v.getB()
						|| firstVertexColor[3] != v.getA()
				){
					g.fill(v.getR(), v.getG(), v.getB(), v.getA()); //takes vertex colors into account	
				}
			}
			
			if (this.isTextureEnabled())
				g.vertex(v.x, v.y, v.z, v.getTexCoordU(), v.getTexCoordV());
			else{
				if (v.getType() == Vector3D.BEZIERVERTEX){
					BezierVertex b = (BezierVertex)v;
					g.bezierVertex(
							b.getFirstCtrlPoint().x,  b.getFirstCtrlPoint().y,  b.getFirstCtrlPoint().z, 
							b.getSecondCtrlPoint().x, b.getSecondCtrlPoint().y, b.getSecondCtrlPoint().z, 
							b.x, b.y, b.z  );
				}
				else
					g.vertex(v.x, v.y, v.z);
			}
		}//for end
		g.endShape();
	}
	
	
	/*
	 * To do multi-texture:
	 * 
	 * glClientActiveTexture(GL_TEXTURE1);
	 * glEnableClientState(GL_TEXTURE_COORD_ARRAY);
	 * glTexCoordPointer(2, GL_FLOAT, sizeof(myVertex), &myQuad[0].s1);
	 *
	 */

	/**
	 * Draws with pure opengl commands using vertex arrays, or vbos for speed.
	 * It is assumed that PGraphicsOpenGL's beginGL() method has already been called
	 * before calling this method!
	 * 
	 * @param gl the gl
	 */
	protected void drawPureGl(GL gl){
//		/*
		//Get display array/buffer pointers
		FloatBuffer tbuff 			= this.getGeometryInfo().getTexBuff();
		FloatBuffer vertBuff 		= this.getGeometryInfo().getVertBuff();
		FloatBuffer colorBuff 		= this.getGeometryInfo().getColorBuff();
		FloatBuffer strokeColBuff 	= this.getGeometryInfo().getStrokeColBuff();
		IntBuffer indexBuff 		= this.getGeometryInfo().getIndexBuff();
		
		//Enable Pointers, set vertex array pointer
		gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL.GL_COLOR_ARRAY);
		if (this.isUseVBOs()){//Vertices
			gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, this.getGeometryInfo().getVBOVerticesName());
			gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);
		}else{
			gl.glVertexPointer(3, GL.GL_FLOAT, 0, vertBuff);
		}
		
		//Default texture target
		int textureTarget = GL.GL_TEXTURE_2D;
		
		/////// DRAW SHAPE ///////
		if (!this.isNoFill()){ 
			boolean textureDrawn = false;
			if (this.isTextureEnabled()
				&& this.getTexture() != null 
				&& this.getTexture() instanceof GLTexture) //Bad for performance?
			{
				GLTexture tex = (GLTexture)this.getTexture();
				textureTarget = tex.getTextureTarget();
				
				//tells opengl which texture to reference in following calls from now on!
				//the first parameter is eigher GL.GL_TEXTURE_2D or ..1D
				gl.glEnable(textureTarget);
				gl.glBindTexture(textureTarget, tex.getTextureID());
				
				gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
				
				if (this.isUseVBOs()){//Texture
					gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, this.getGeometryInfo().getVBOTextureName());
					gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, 0);
				}else
					gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, tbuff);
				
				textureDrawn = true;
			}
			
			if (this.isUseVBOs()){//Color
				gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, this.getGeometryInfo().getVBOColorName());
				gl.glColorPointer(4, GL.GL_FLOAT, 0, 0);
			}else{
				gl.glColorPointer(4, GL.GL_FLOAT, 0, colorBuff);
			}
			
			//Normals
			if (this.getGeometryInfo().isContainsNormals()){
				gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
				if (this.isUseVBOs()){
					gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, this.getGeometryInfo().getVBONormalsName());
					gl.glNormalPointer(GL.GL_FLOAT, 0, 0); 
				}else{
					gl.glNormalPointer(GL.GL_FLOAT, 0, this.getGeometryInfo().getNormalsBuff());
				}
			}
			
			//DRAW //Draw with drawElements if geometry is indexed, else draw with drawArrays!
			if (this.getGeometryInfo().isIndexed()){
				gl.glDrawElements(this.getFillDrawMode(), indexBuff.limit(), GL.GL_UNSIGNED_INT, indexBuff);
//				gl.glDrawElements(this.getFillDrawMode(), indexBuff.capacity(), GL.GL_UNSIGNED_INT, indexBuff);
			}else{
				gl.glDrawArrays(this.getFillDrawMode(), 0, vertBuff.capacity()/3);
			}
			
			if (this.getGeometryInfo().isContainsNormals()){
				gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
			}
			
			if (textureDrawn){
				gl.glBindTexture(textureTarget, 0);//Unbind texture
				gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
				gl.glDisable(textureTarget); //weiter nach unten?
			}
		}
		
		////////// DRAW OUTLINE ////////
		if (!this.isNoStroke()){ 
			if (this.isUseVBOs()){
				gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, this.getGeometryInfo().getVBOStrokeColorName());
				gl.glColorPointer(4, GL.GL_FLOAT, 0, 0);
			}else{
				gl.glColorPointer(4, GL.GL_FLOAT, 0, strokeColBuff);
			}
			
			
//			//Turn on smooth outlines
//			if (this.isDrawSmooth()){
//				gl.glEnable(GL.GL_LINE_SMOOTH);
//			}
			//FIXME TEST
			Tools3D.setLineSmoothEnabled(gl, true);
			
//			/*
			//SET LINE STIPPLE
				short lineStipple = this.getLineStipple();
				if (lineStipple != 0){
					gl.glLineStipple(1, lineStipple);
					gl.glEnable(GL.GL_LINE_STIPPLE);
				}
//			*/
			
			if (this.getStrokeWeight() > 0)
				gl.glLineWidth(this.getStrokeWeight());
			
			//DRAW Polygon outline
			//Draw with drawElements if geometry is indexed, else draw with drawArrays!
			if (this.getGeometryInfo().isIndexed()){
				gl.glDrawElements(GL.GL_LINE_STRIP, indexBuff.limit(), GL.GL_UNSIGNED_INT, indexBuff);
//				gl.glDrawElements(this.getFillDrawMode(), indexBuff.capacity(), GL.GL_UNSIGNED_INT, indexBuff);
			}else{
				gl.glDrawArrays(GL.GL_LINE_STRIP, 0, vertBuff.capacity()/3); 
			}
			
			//RESET LINE STIPPLE
			if (lineStipple != 0){
				gl.glDisable(GL.GL_LINE_STIPPLE);
			}
			
			//FIXME TEST 
			Tools3D.setLineSmoothEnabled(gl, false);
			/*
			if (this.isDrawSmooth())
				gl.glDisable(GL.GL_LINE_SMOOTH);
			 */
		}
		
		gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL.GL_COLOR_ARRAY);
		
		//TEST
		if (this.isUseVBOs()){
			gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
			gl.glBindBufferARB(GL.GL_ELEMENT_ARRAY_BUFFER_ARB, 0);
		}
//		*/
	}
	
	/* (non-Javadoc)
	 * @see com.jMT.components.visibleComponents.shapes.AbstractShape#isGeometryContainsPoint(util.math.Vector3D)
	 */
	@Override
	public boolean isGeometryContainsPointLocal(Vector3D testPoint) { 
		return Tools3D.isPolygonContainsPoint(this.getVerticesLocal(), testPoint);
	}


	/* (non-Javadoc)
	 * @see com.jMT.components.visibleComponents.shapes.AbstractShape#getGeometryIntersection(util.math.Ray)
	 */
	@Override
	public Vector3D getGeometryIntersectionLocal(Ray ray){
		Vector3D[] vertices;
		vertices = this.getVerticesLocal();
		Vector3D polyNormal 	= this.getNormal();
		
		//Possible intersection point in plane of polygon
		Vector3D interSectPoint = ToolsIntersection.getRayPlaneIntersection(ray, polyNormal, vertices[0]);
		
		if (interSectPoint == null)
			return null;
		
		return (Tools3D.isPoint3DInPlanarPolygon(vertices, interSectPoint, polyNormal) ? interSectPoint : null);
	}


	
	/**
	 * calculates an intersection point from a ray and the plane the polygon lies in, this doesent check whether
	 * the intersectionpoint is inside the polygon or not!<br>
	 * <br><b>NOTE:</b> The polygon has to have at least 3 vertices, the Polygon should be coplanar and convex
	 * <br><b>NOTE:</b> The calculation is done in local object space, so the ray has to be transformed into this objects
	 * space aswell!.
	 * 
	 * @param polygonNormal the polygon normal
	 * @param ray the ray
	 * 
	 * @return the ray poly plane intersection point
	 * 
	 * a possible intersectionPoint or null if there is none
	 */
	private Vector3D getRayPolyPlaneIntersectionPoint(Ray ray, Vector3D polygonNormal){
		//get the polygons normal vector
		Vertex[] vertices;
//		if (useLocalObjectSpace)
//			vertices = this.getVerticesPickingLocal();
//		else
//			vertices = this.getVerticesPickingWorld();
		vertices = this.getVerticesLocal();
		return ToolsIntersection.getRayPlaneIntersection(ray, polygonNormal, vertices[0]);
	}
	
	
	
	/**
	 * Returns a normalized vector, perpendicular to the polygon (the normal)<br>
	 * <br>The normal vector is calculated in local object space! To transform it into
	 * world space use <code>normal.transformNormal(Matrix worldMatrix);</code>
	 * <br><b>NOTE:</b> The polygon has to have at least 3 vertices, the Polygon has to be coplanar!
	 * <br><b>NOTE:</b> Uses the three first vertices for computation, so make sure there arent duplicates!
	 * 
	 * @return the normal vector
	 */
	public Vector3D getNormal(){
		try {
			if (normalDirty){
				Vertex[] vertices;
//				if (useLocalObjectSpace)
					vertices = this.getVerticesLocal();
//				else
//					vertices = this.getVerticesWorld();
					
				if (vertices[0].equalsVector(vertices[1])
					|| vertices[0].equalsVector(vertices[2])
				){
					System.err.println("Warning: in component " + this.getName() + ", 2 vectors for normal computation are equal -> bad results! -" + this);
				}
				this.normal = Tools3D.getNormal(vertices[0], vertices[1], vertices[2], true);
				this.normalDirty = false;
				return this.normal; //FIXME return copy or original?
			}else{
				return this.normal;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Vector3D(0,0,1);
		}
	}
	

	
	/**
	 * Calculates the area of a 2D polygon using its transformed world coordinates
	 * <br>NOTE: works only if the last vertex is equal to the first (polygon is closed correctly).
	 * 
	 * @return the area as double
	 */
	public double get2DPolygonArea(){
		return Tools3D.getPolygonArea2D(this.getVerticesGlobal());
	}
	
	
	/**
	 * Calculates the center of mass of the polygon.
	 * The coordinates are transformed to world space first. THIS IS NOT CHEAP!
	 * NOTE: works only if the last vertex is equal to the first (polygon is closed correctly)
	 * NOTE: polygon needs to be coplanar and in the X,Y plane!
	 * 
	 * @return the center or mass as a Vector3D
	 */
	public Vector3D getCenterOfMass2DLocal(){
		//FIXME doesent work if polygon not in x,y plane!?
		return Tools3D.getPolygonCenterOfMass2D(this.getVerticesLocal());
	}
	
	
	/* (non-Javadoc)
	 * @see com.jMT.components.visibleComponents.shapes.AbstractShape#getCenterPointObjectSpace()
	 */
	@Override
	public Vector3D getCenterPointLocal(){
		if (this.isBoundingShapeSet()){
			return this.getBoundingShape().getCenterPointLocal();
		}else{
			//TODO this fails if the polygon isnt in the X,Y,0 Plane!
//			return this.getCenterOfMas2DLocal();
			return new OrientedBoundingBox(this).getCenterPointLocal();
		}
	}
	
	
	/* (non-Javadoc)
	 * @see com.jMT.components.visibleComponents.shapes.AbstractShape#getHeightXY(com.jMT.components.TransformSpace)
	 */
	public float getHeightXY(TransformSpace transformSpace) {
		switch (transformSpace) {
		case LOCAL:
			return this.getHeightXYLocal();
		case RELATIVE_TO_PARENT:
			return this.getHeightXYRelativeToParent();
		case GLOBAL:
			return this.getHeightXYGlobal();
		default:
			return -1;
		}
	}
	
	
	/**
	 * Gets the height xy obj space.
	 * 
	 * @return the height xy obj space
	 */
	private float getHeightXYLocal() {
		return this.getHeightXYVectLocal().length();
	}
	
	/**
	 * Gets the "height vector" and transforms it to parent relative space, then calculates
	 * its length.
	 * 
	 * @return the height xy relative to parent
	 * 
	 * the height relative to its parent space frame
	 */
	private float getHeightXYRelativeToParent() {
//		Vector3D p = this.getHeightXYVectLocal();
//		Matrix m = new Matrix(this.getLocalMatrix());
//		m.removeTranslationFromMatrix();
//		p.transform(m);
//		return p.length();
		if (this.isBoundingShapeSet()){
			return this.getBoundingShape().getHeightXY(TransformSpace.RELATIVE_TO_PARENT);
		}else{
			OrientedBoundingBox tempBounds = new OrientedBoundingBox(this);
			return tempBounds.getHeightXY(TransformSpace.RELATIVE_TO_PARENT);
		}
	}
	
	
	/**
	 * Gets the "height vector" and transforms it to world space, then calculates
	 * its length.
	 * 
	 * @return the height xy global
	 * 
	 * the height relative to the world space
	 */
	private float getHeightXYGlobal() {
//		Vector3D p = this.getHeightXYVectLocal();
//		Matrix m = new Matrix(this.getGlobalMatrix());
//		m.removeTranslationFromMatrix();
//		p.transform(m);
//		return p.length();
		if (this.isBoundingShapeSet()){
			return this.getBoundingShape().getHeightXY(TransformSpace.GLOBAL);
		}else{
			OrientedBoundingBox tempBounds = new OrientedBoundingBox(this);
			return tempBounds.getHeightXY(TransformSpace.GLOBAL);
		}
	}
	
	
	/**
	 * Gets the "height vector" from its boundingshape. If no boundingshape is set,
	 * a temporary bounding rectangle in the xy-plane is calculated and its height
	 * is calculated as a vector with the height as its length in object space.
	 * 
	 * @return the height xy vect obj space
	 * 
	 * vector representing the height of the boundingshape of the shape
	 */
	public Vector3D getHeightXYVectLocal() {
		if (this.isBoundingShapeSet()){
			return this.getBoundingShape().getHeightXYVectLocal();
		}else{
			OrientedBoundingBox tempBounds = new OrientedBoundingBox(this);
			return tempBounds.getHeightXYVectLocal();
		}
	}

	
	/* (non-Javadoc)
	 * @see com.jMT.components.visibleComponents.shapes.AbstractShape#getWidthXY(com.jMT.components.TransformSpace)
	 */
	public float getWidthXY(TransformSpace transformSpace) {
		switch (transformSpace) {
		case LOCAL:
			return this.getWidthXYLocal();
		case RELATIVE_TO_PARENT:
			return this.getWidthXYRelativeToParent();
		case GLOBAL:
			return this.getWidthXYGlobal();
		default:
			return -1;
		}
	}
	
	
	/**
	 * Gets the width xy obj space.
	 * 
	 * @return the width xy obj space
	 */
	private float getWidthXYLocal() {
		return this.getWidthXYVectLocal().length();
	}
	
	
	/**
	 * Calculates the width of this shape, by using its
	 * bounding shape.
	 * Uses the objects local transform. So the width will be
	 * relative to the parent only - not the whole world
	 * 
	 * @return the width xy relative to parent
	 * 
	 * the width
	 */
	private float getWidthXYRelativeToParent() {
//		Vector3D p = this.getWidthXYVectLocal();
//		Matrix m = new Matrix(this.getLocalMatrix());
//		m.removeTranslationFromMatrix();
//		p.transform(m);
//		return p.length();
		if (this.isBoundingShapeSet()){
			return this.getBoundingShape().getWidthXY(TransformSpace.RELATIVE_TO_PARENT);
		}else{
			OrientedBoundingBox tempBounds = new OrientedBoundingBox(this);
			return tempBounds.getWidthXY(TransformSpace.RELATIVE_TO_PARENT);
		}
	}
	
	/**
	 * Gets the "Width vector" and transforms it to world space, then calculates
	 * its length.
	 * 
	 * @return the width xy global
	 * 
	 * the Width relative to the world space
	 */
	private float getWidthXYGlobal() {
//		Vector3D p = this.getWidthXYVectLocal();
//		Matrix m = new Matrix(this.getGlobalMatrix());
//		m.removeTranslationFromMatrix();
//		p.transform(m);
//		return p.length();
		if (this.isBoundingShapeSet()){
			return this.getBoundingShape().getWidthXY(TransformSpace.GLOBAL);
		}else{
			OrientedBoundingBox tempBounds = new OrientedBoundingBox(this);
			return tempBounds.getWidthXY(TransformSpace.GLOBAL);
		}
	}
	
	/**
	 * Gets the "width vector" from its boundingshape. If no boundingshape is set,
	 * a temporary bounding rectangle in the xy-plane is calculated and its width
	 * is calculated as a vector with the width as its length in object space.
	 * 
	 * @return the width xy vect obj space
	 * 
	 * vector representing the width of the boundingshape of the shape
	 */
	public Vector3D getWidthXYVectLocal() {
		if (	this.isBoundingShapeSet()){
			return this.getBoundingShape().getWidthXYVectLocal();
		}else{
			OrientedBoundingBox tempBounds = new OrientedBoundingBox(this);
			return tempBounds.getWidthXYVectLocal();
		}
	}
	
	
	/**
	 * Scales this shape to the given width and height. Relative to its parent frame of reference.
	 * <br>Uses the shapes bounding shape for calculation.
	 * 
	 * @param width the width
	 * @param height the height
	 * 
	 * @return true, if sets the size xy relative to parent
	 * 
	 * returns false if negative values are put in
	 */
	public boolean setSizeXYRelativeToParent(float width, float height){
		if (width > 0 && height > 0){
			Vector3D centerPoint = this.getCenterPointRelativeToParent();
			this.scale(1/this.getWidthXYRelativeToParent(), 1/this.getHeightXYRelativeToParent(), 1, centerPoint);
			this.scale(width, height, 1, centerPoint);
			return true;
		}else
			return false;
	}
	
	/**
	 * Scales this shape to the given width and height in the XY-Plane. Relative to world space.
	 * <br>Uses the shapes bounding shape for calculation.
	 * 
	 * @param width the width
	 * @param height the height
	 * 
	 * @return true, if sets the size xy global
	 */
	public boolean setSizeXYGlobal(float width, float height){
		if (width > 0 && height > 0){
			Vector3D centerPoint = this.getCenterPointGlobal();
			this.scaleGlobal(1/this.getWidthXYGlobal(), 1/this.getHeightXYGlobal(), 1, centerPoint);
			this.scaleGlobal(width, height, 1, centerPoint); 
			return true;
		}else
			return false;
	}
	
	
	/**
	 * Scales the shape to the given height relative to parent space.
	 * Aspect ratio is preserved! The scaling is done Axis aligned, so
	 * shearing might occour if rotated!
	 * <br>Uses the shapes bounding shape for calculation.
	 * 
	 * @param height the height
	 * 
	 * @return true, if the height isnt negative
	 */
	public boolean setHeightXYRelativeToParent(float height){
		if (height > 0){
			Vector3D centerPoint = this.getCenterPointGlobal();
			this.scale(1/this.getHeightXYRelativeToParent(), 1/this.getHeightXYRelativeToParent(), 1, centerPoint);
			this.scale(height, height, 1, centerPoint);
			return true;
		}else
			return false;
	}
	
	
	/**
	 * Scales the shape to the given height relative to world space.
	 * Aspect ratio is preserved! The scaling is done Axis aligned, so
	 * shearing might occour if rotated!
	 * <br>Uses the shapes bounding shape for calculation.
	 * 
	 * @param height the height
	 * 
	 * @return true, if sets the height xy global
	 */
	public boolean setHeightXYGlobal(float height){
		if (height > 0){
			Vector3D centerPoint = this.getCenterPointGlobal();
			this.scaleGlobal(1/this.getHeightXYGlobal(), 1/this.getHeightXYGlobal(), 1, centerPoint);
			this.scaleGlobal(height, height, 1, centerPoint);
			return true;
		}else
			return false;
	}
	
	/**
	 * Scales the shape to the given width relative to parent space.
	 * Aspect ratio is preserved! 
	 * <br>NOTE: The scaling is done Axis aligned, so
	 * shearing might occour if rotated before!
	 * <br>Uses the shapes bounding shape for calculation.
	 * 
	 * @param width the width
	 * 
	 * @return true, if the width isnt negative
	 */
	public boolean setWidthXYRelativeToParent(float width){
		if (width > 0){
			Vector3D centerPoint = this.getCenterPointGlobal();
			this.scale(1/this.getWidthXYRelativeToParent(), 1/this.getWidthXYRelativeToParent(), 1, centerPoint);
			this.scale(width, width, 1, centerPoint);
			return true;
		}else
			return false;
	}
	
	
	/**
	 * Scales the shape to the given width relative to world space.
	 * Aspect ratio is preserved! The scaling is done Axis aligned, so
	 * shearing might occour if rotated!
	 * <br>Uses the shapes bounding shape for calculation.
	 * 
	 * @param width the width
	 * 
	 * @return true, if sets the width xy global
	 */
	public boolean setWidthXYGlobal(float width){
		if (width > 0){
			Vector3D centerPoint = this.getCenterPointGlobal();
			this.scaleGlobal(1/this.getWidthXYGlobal(), 1/this.getWidthXYGlobal(), 1, centerPoint);
			this.scaleGlobal(width, width, 1, centerPoint);
			return true;
		}else
			return false;
	}

	
	/* (non-Javadoc)
	 * @see com.jMT.components.visibleComponents.shapes.AbstractShape#destroyComponent()
	 */
	@Override
	protected void destroyComponent() {
		
	}


}
