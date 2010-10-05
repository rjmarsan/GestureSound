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

import javax.media.opengl.GL;

import org.mt4j.components.TransformSpace;
import org.mt4j.components.visibleComponents.StyleInfo;
import org.mt4j.util.MT4jSettings;
import org.mt4j.util.MTColor;
import org.mt4j.util.math.Matrix;
import org.mt4j.util.math.Ray;
import org.mt4j.util.math.Tools3D;
import org.mt4j.util.math.Vector3D;
import org.mt4j.util.math.Vertex;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.opengl.PGraphicsOpenGL;

/**
 * A class for drawing a simple line segment.
 * 
 * @author Christopher Ruff
 */
public class MTLine extends AbstractShape {
	
	/** The p context. */
	private PApplet pContext;
	
//	/** The use display list. */
//	private boolean  useDisplayList;
//	
//	/** The display list i ds. */
//	private int[] displayListIDs;

	/**
	 * Instantiates a new mT line.
	 * 
	 * @param pApplet the applet
	 * @param x1 the x1
	 * @param y1 the y1
	 * @param z1 the z1
	 * @param x2 the x2
	 * @param y2 the y2
	 * @param z2 the z2
	 */
	public MTLine(PApplet pApplet, float x1, float y1, float z1, float x2, float y2, float z2) {
		this(pApplet, new Vertex(x1,y1,z1), new Vertex(x2,y2,z2));
	}
	
	/**
	 * Instantiates a new mT line.
	 * 
	 * @param pApplet the applet
	 * @param x1 the x1
	 * @param y1 the y1
	 * @param x2 the x2
	 * @param y2 the y2
	 */
	public MTLine(PApplet pApplet, float x1, float y1, float x2, float y2) {
		this(pApplet, x1,y1,0,x2,y2,0);
	}
	
	/**
	 * Instantiates a new mT line.
	 * 
	 * @param pApplet the applet
	 * @param startPoint the start point
	 * @param endPoint the end point
	 */
	public MTLine(PApplet pApplet, Vertex startPoint, Vertex endPoint) {
		super(new Vertex[]{startPoint, endPoint},pApplet);
		this.pContext = pApplet;
		
//		useDisplayList = false;
//		displayListIDs = new int[1];
		
		this.setNoFill(true);
		this.setPickable(true);
		
		if (MT4jSettings.getInstance().isOpenGlMode()){
			this.getGeometryInfo().generateOrUpdateBuffersLocal(new StyleInfo(new MTColor(255,255,255,255), new MTColor(startPoint.getR(), startPoint.getG(), startPoint.getB(), startPoint.getA()), this.isDrawSmooth(), this.isNoStroke(), this.isNoFill(), this.getStrokeWeight(), this.getFillDrawMode(), this.getLineStipple()));
		}
		this.setBoundsBehaviour(AbstractShape.BOUNDS_DONT_USE);
	}

	//TODO getNormal() will crash ..
	//TODO override vobs?
	
	@Override
	public void generateDisplayLists(){
		if (MT4jSettings.getInstance().isOpenGlMode() && this.isUseDirectGL()){
			this.getGeometryInfo().deleteDisplayLists();
			this.getGeometryInfo().setDisplayListIDs(new int[]{
					Tools3D.generateOutLineDisplayList(
					pContext,
					this.getGeometryInfo().getVertBuff(),
					this.getGeometryInfo().getStrokeColBuff(),
					this.getGeometryInfo().getIndexBuff(),
					this.isDrawSmooth(),
					this.getStrokeWeight(),
					this.getLineStipple()), -1});
			}
	}
	
	
	/* (non-Javadoc)
	 * @see com.jMT.components.visibleComponents.shapes.AbstractShape#setUseVBOs(boolean)
	 */
	@Override
	public void setUseVBOs(boolean useVBOs) {
		System.err.println("MT Line doesent support vbos.");
	}
	
	/* (non-Javadoc)
	 * @see com.jMT.components.visibleComponents.AbstractVisibleComponent#drawComponent()
	 */
	@Override
	public void drawComponent(PGraphics g) {
		PApplet renderer = this.getRenderer();
		if (MT4jSettings.getInstance().isOpenGlMode()   
		    && this.isUseDirectGL()){
				GL gl=((PGraphicsOpenGL)renderer.g).beginGL();
			
				//Draw with PURE opengl
				if (this.isUseDisplayList()){
					//Use Display Lists
					if (!this.isNoStroke())
						gl.glCallList(this.getGeometryInfo().getDisplayListIDs()[0]); //Draw line
				}else{
					//Use Vertex Arrays or VBOs
					this.drawPureGl(gl);
				}
				((PGraphicsOpenGL)renderer.g).endGL();
		}else{
			//Draw with processing
			MTColor strokeColor = this.getStrokeColor();
			pContext.stroke(strokeColor.getR(), strokeColor.getG(), strokeColor.getB(), strokeColor.getAlpha());
			pContext.strokeWeight(this.getStrokeWeight());
			
			if (this.isDrawSmooth()) 
				pContext.smooth();
			else 	
				pContext.noSmooth();
			
			//Do the line
			Vertex[] verts = this.getVerticesLocal();
			pContext.line(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z);
		}
	}
	
	
	/**
	 * Draw pure gl.
	 * 
	 * @param gl the gl
	 */
	private void drawPureGl(GL gl){
		FloatBuffer strokeColBuff 	= this.getGeometryInfo().getStrokeColBuff();
		FloatBuffer vertBuff 		= this.getGeometryInfo().getVertBuff();
		//Enable Pointers, set vertex array pointer
		gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL.GL_COLOR_ARRAY);
		
		if (this.isUseVBOs()){
			gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, this.getGeometryInfo().getVBOVerticesName());
			gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);
		}else{
			gl.glVertexPointer(3, GL.GL_FLOAT, 0, vertBuff);
		}
		
		if (this.isUseVBOs()){
			gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, this.getGeometryInfo().getVBOStrokeColorName());
			gl.glColorPointer(4, GL.GL_FLOAT, 0, 0);
		}else{
			gl.glColorPointer(4, GL.GL_FLOAT, 0, strokeColBuff);
		}
		
//		//Turn on smooth outlines
//		if (this.isDrawSmooth())
//			gl.glEnable(GL.GL_LINE_SMOOTH);
		//FIXME TEST
		Tools3D.setLineSmoothEnabled(gl, true);
		
		//SET LINE STIPPLE
		short lineStipple = this.getLineStipple();
		if (lineStipple != 0){
			gl.glLineStipple(1, lineStipple);
			gl.glEnable(GL.GL_LINE_STIPPLE);
		}
		
		if (this.getStrokeWeight() > 0)
			gl.glLineWidth(this.getStrokeWeight());
		
		gl.glDrawArrays(GL.GL_LINE_STRIP, 0, vertBuff.capacity()/3);
		//RESET LINE STIPPLE
		if (lineStipple != 0){
			gl.glDisable(GL.GL_LINE_STIPPLE); 
		}
		
//	    if (this.isDrawSmooth())
//			gl.glDisable(GL.GL_LINE_SMOOTH);
		//FIXME TEST
		Tools3D.setLineSmoothEnabled(gl, false);
		
		gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL.GL_COLOR_ARRAY);
		if (this.isUseVBOs()){
			gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
			gl.glBindBufferARB(GL.GL_ELEMENT_ARRAY_BUFFER_ARB, 0);
		}
	}


	/* (non-Javadoc)
	 * @see com.jMT.components.visibleComponents.shapes.AbstractShape#isGeometryContainsPoint(util.math.Vector3D)
	 */
	@Override
	public boolean isGeometryContainsPointLocal(Vector3D testPoint) {
		//TODO implement
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.jMT.components.visibleComponents.shapes.AbstractShape#getGeometryIntersection(util.math.Ray)
	 */
	@Override
	public Vector3D getGeometryIntersectionLocal(Ray ray){
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.jMT.components.visibleComponents.shapes.AbstractShape#getCenterPointObjectSpace()
	 */
	@Override
	public Vector3D getCenterPointLocal() {
		Vertex[] v = this.getVerticesLocal();
		Vertex lengthVect = (Vertex)v[1].getSubtracted(v[0]);
		lengthVect.scaleLocal(0.5f);
		return v[0].getAdded(lengthVect);
	}
	
	/**
	 * Gets the length.
	 * 
	 * @return the length
	 */
	public float getLength() {
		Vertex[] v = this.getVerticesGlobal();
		Vertex lengthVect = (Vertex)v[1].getSubtracted(v[0]);
		return lengthVect.length();
	}

	/* (non-Javadoc)
	 * @see com.jMT.components.visibleComponents.shapes.AbstractShape#destroyComponent()
	 */
	@Override
	protected void destroyComponent() {
		
	}

	
	/* (non-Javadoc)
	 * @see mTouch.components.visibleComponents.shapes.AbstractShape#getHeightXY(mTouch.components.TransformSpace)
	 */
	@Override
	public float getHeightXY(TransformSpace transformSpace) {
//		switch (transformSpace) {
//		case RELATIVE_TO_SELF:
//			return this.getHeightXYObjSpace();
//		case RELATIVE_TO_PARENT:
//			return this.getHeightXYRelativeToParent();
//		case RELATIVE_TO_WORLD:
//			return this.getHeightXYGlobal();
//		default:
//			return -1;
//		}
		return 0;
	}
	
//	private float getHeightXYObjSpace() {
//		return this.getHeightXYVectObjSpace().length();
//	}
//	
//	
//	/**
//	 * Calculates the Height of this shape, by using its
//	 * bounding rectangle.
//	 * <br><strong>NOTE: </strong> This method will only work, if the polygon is parallel 
//	 * to the x/y plane in space AND has a boundingShape rectangle thats meets the same crieria
//	 * because the calculations are done with the bounding rectangle. (bounds instance of <code>BoundsZPlaneRectangle</code>)
//	 * @return the height
//	 */
//	private float getHeightXYRelativeToParent() {
//		Vector3D p = this.getHeightXYVectObjSpace();
//		Matrix m = new Matrix(this.getLocalBasisMatrix());
//		m.removeTranslationFromMatrix();
//		p.transform(m);
//		return p.length();
//	}
//	
//	
//	/**
//	 * <br><strong>NOTE: </strong> This method will only work, if the polygon is parallel 
//	 * to the x/y plane in space AND has a boundingShape rectangle thats meets the same crieria
//	 * because the calculations are done with the bounding rectangle. (bounds instance of <code>BoundsZPlaneRectangle</code>)
//	 * 
//	 * @return
//	 */
//	private float getHeightXYGlobal() {
//		Vector3D p = this.getHeightXYVectObjSpace();
//		Matrix m = new Matrix(this.getAbsoluteLocalToWorldMatrix());
//		m.removeTranslationFromMatrix();
//		p.transform(m);
//		return p.length();
//	}
//
//	
//	public Vector3D getHeightXYVectObjSpace() {
//		Vector3D[] boundRectVertsLocal = this.getGeometryInfo().getVertices();
//		Vector3D height = boundRectVertsLocal[2].getSubtracted(boundRectVertsLocal[1]);
//		return height;
//	}
	
	
	
	

	/* (non-Javadoc)
 * @see com.jMT.components.visibleComponents.shapes.AbstractShape#getWidthXY(com.jMT.components.TransformSpace)
 */
@Override
	public float getWidthXY(TransformSpace transformSpace) {
		switch (transformSpace) {
		case LOCAL:
			return this.getWidthXYObjSpace();
		case RELATIVE_TO_PARENT:
			return this.getWidthXYRealtiveToParent();
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
	private float getWidthXYObjSpace() {
		return this.getWidthXYVectObjSpace().length();
	}
	
	
	/**
	 * Calculates the width of this shape, by using its
	 * bounding rectangle.
	 * Uses the objects local transform. So the width will be
	 * relative to the parent only - not the whole world
	 * <br><strong>NOTE: </strong> This method will only work, if the polygon is parallel
	 * to the x/y plane in space AND has a boundingShape rectangle thats meets the same crieria
	 * because the calculations are done with the bounding rectangle. (bounds instance of <code>BoundsZPlaneRectangle</code>)
	 * 
	 * @return the height
	 */
	private float getWidthXYRealtiveToParent() {
		Vector3D p = this.getWidthXYVectObjSpace();
		Matrix m = new Matrix(this.getLocalMatrix());
		m.removeTranslationFromMatrix();
		p.transform(m);
		return p.length();
	}
	
	/**
	 * The length of the line in world coordinates.
	 * 
	 * @return the width xy global
	 */
	private float getWidthXYGlobal() {
		Vector3D p = this.getWidthXYVectObjSpace();
		Matrix m = new Matrix(this.getGlobalMatrix());
		m.removeTranslationFromMatrix();
		p.transform(m);
		return p.length();
	}

	/**
	 * Gets the width xy vect obj space.
	 * 
	 * @return the width xy vect obj space
	 */
	public Vector3D getWidthXYVectObjSpace() {
		Vector3D[] vertsLocal = this.getGeometryInfo().getVertices();
		Vector3D width = vertsLocal[1].getSubtracted(vertsLocal[0]);
//		System.out.println("Width of " + this.getName()+ " :" + width);
		return width;
	}


}
