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
package org.mt4j.components.bounds;

import org.mt4j.components.MTComponent;
import org.mt4j.components.TransformSpace;
import org.mt4j.components.visibleComponents.shapes.AbstractShape;
import org.mt4j.util.camera.IFrustum;
import org.mt4j.util.math.Ray;
import org.mt4j.util.math.Tools3D;
import org.mt4j.util.math.ToolsIntersection;
import org.mt4j.util.math.Vector3D;

import processing.core.PGraphics;


/**
 * Bounding rectangle for 2D shapes that are parallel to the Z=0 plane.
 * 
 * @author Christopher Ruff
 */
public class BoundsZPlaneRectangle implements IBoundingShape {
	
//	/** The peer component. */
	private MTComponent peerComponent;
	
	/** The bounding points local. */
	private Vector3D[] boundingPointsLocal;
	
	/** The I n_ plan e_ tolerance. */
	public static float IN_PLANE_TOLERANCE = 0.015f;
	
	private Vector3D centerPointObjSpace;
	
	private Vector3D[] worldVecs;
	private boolean worldVecsDirty;
	private Vector3D centerPointWorld;
	private boolean centerWorldDirty;
	
	
	
	//TODO checken ob punkte wirklich in ebene liegen
	//-> point in plane test
	/**
	 * Instantiates a new bounds z plane rectangle.
	 * 
	 * @param peerComponent the peer component
	 */
	public BoundsZPlaneRectangle(AbstractShape peerComponent) {
		this(peerComponent, peerComponent.getVerticesLocal());
	}
	
	
	/**
	 * Instantiates a new bounds z plane rectangle.
	 * 
	 * @param peerComponent the peer component
	 * @param vertices the vertices
	 */
	public BoundsZPlaneRectangle(MTComponent peerComponent, Vector3D[] vertices) {
		super();
		this.peerComponent = peerComponent;
		
		this.boundingPointsLocal = this.getBoundingRectVertices(vertices);
		this.centerPointObjSpace = this.calcCenterPointObjSpace();
		this.worldVecsDirty 	= true;
		this.centerWorldDirty 	= true;
		this.worldVecs 			= this.getVectorsGlobal();
		this.centerPointWorld 	= this.getCenterPointGlobal();
	}


	
	public void setGlobalBoundsChanged(){
			this.worldVecsDirty = true;
			this.centerWorldDirty = true;
	}
	
	
	/**
	 * Calculates the bounding rectangles vertices and returns them.
	 * 
	 * @param vertices the vertices
	 * 
	 * @return a Vector3D array containing the vertices of the bounding rectangle
	 */
	private Vector3D[] getBoundingRectVertices(Vector3D[] vertices){
		float[] minMax = Tools3D.getMinXYMaxXY(vertices);
		float minX = minMax[0];
		float minY = minMax[1];
		float maxX = minMax[2];
		float maxY = minMax[3];
		float z = vertices[0].z;
		if (peerComponent != null && peerComponent instanceof AbstractShape){
			z = ((AbstractShape)peerComponent).getCenterPointLocal().z;
		}
		return (new Vector3D[]{
				new Vector3D(minX, minY,z),
				new Vector3D(maxX, minY,z),
				new Vector3D(maxX, maxY,z),
				new Vector3D(minX, maxY,z),
//				new Vector3D(minX, minY,z)
				}); 
	}
	
	
	public void drawBounds(final PGraphics g){
		g.pushMatrix();
		g.pushStyle();
		g.fill(150,180);
		
		g.beginShape();
		g.vertex(this.boundingPointsLocal[0].x, this.boundingPointsLocal[0].y, this.boundingPointsLocal[0].z);
		g.vertex(this.boundingPointsLocal[1].x, this.boundingPointsLocal[1].y, this.boundingPointsLocal[1].z);
		g.vertex(this.boundingPointsLocal[2].x, this.boundingPointsLocal[2].y, this.boundingPointsLocal[2].z);
		g.vertex(this.boundingPointsLocal[3].x, this.boundingPointsLocal[3].y, this.boundingPointsLocal[3].z);
		g.endShape();
		
		g.popStyle();
		g.popMatrix();
		
		
//		g.pushMatrix();
//		g.pushStyle();
//		g.fill(150,180);
//		
//		Vector3D[] v = this.getVectorsGlobal();
//		float[] minMax = Tools3D.getMinXYMaxXY(v);
//		
//		g.beginShape();
//		g.vertex(minMax[0], minMax[1], 0);
//		g.vertex(minMax[2], minMax[1], 0);
//		g.vertex(minMax[2], minMax[3], 0);
//		g.vertex(minMax[0], minMax[3], 0);
//		g.endShape();
////		
//		g.popStyle();
//		g.popMatrix();
	}


	
	/* (non-Javadoc)
	 * @see mTouch.components.bounds.IBoundingShape#containsPoint(util.math.Vector3D)
	 */
	public boolean containsPointLocal(Vector3D testPoint) {
		if (		testPoint.x >= this.boundingPointsLocal[0].x
			    && 	testPoint.x <= this.boundingPointsLocal[1].x
			    
			    && 	testPoint.y >= this.boundingPointsLocal[0].y
			    && 	testPoint.y <= this.boundingPointsLocal[2].y
			    
			    &&  Math.abs(testPoint.z - this.boundingPointsLocal[0].z) < IN_PLANE_TOLERANCE //Check if point roughly in same z plane as the rect with tolerance
			){
				return true;
			}else{
				return false;
			}
	}
	
	
	/** The rect normal. */
	private Vector3D rectNormal = new Vector3D(0,0,1);
	
	/* (non-Javadoc)
	 * @see mTouch.components.bounds.IBoundingShape#getIntersectionPoint(util.math.Ray)
	 */
	public Vector3D getIntersectionLocal(Ray ray) {
		Vector3D[] verts = this.boundingPointsLocal;
//		rectNormal= this.getNormalObjSpace();
		//Normal should actually always be (0,0,1)!
		Vector3D testPoint = ToolsIntersection.getRayPlaneIntersection(ray, rectNormal, verts[0]);
		
		if (testPoint == null){
			return null;
		}
		return (this.containsPointLocal(testPoint) ? testPoint : null);
	}
	
	
	/**
	 * Gets the normal obj space.
	 * 
	 * @return the normal obj space
	 */
	private Vector3D getNormalObjSpace() {
		return Tools3D.getNormal(this.boundingPointsLocal[0], this.boundingPointsLocal[1], this.boundingPointsLocal[2], true);
	}
	
	private Vector3D calcCenterPointObjSpace(){
		Vector3D tmp0 = this.boundingPointsLocal[0].getCopy();
		Vector3D tmp1 = this.boundingPointsLocal[1].getSubtracted(this.boundingPointsLocal[0]);
		tmp1.scaleLocal(0.5f);
		
		Vector3D tmp2 = this.boundingPointsLocal[3].getSubtracted(this.boundingPointsLocal[0]);
		tmp2.scaleLocal(0.5f);
		
		tmp0.addLocal(tmp1);
		tmp0.addLocal(tmp2);
		return tmp0;
	}

	/* (non-Javadoc)
	 * @see mTouch.components.bounds.IBoundingShape#getCenterPointLocal()
	 */
	public Vector3D getCenterPointLocal() {
//		Vector3D tmp0 = this.boundingPointsLocal[0].getCopy();
//		Vector3D tmp1 = this.boundingPointsLocal[1].getSubtracted(this.boundingPointsLocal[0]);
//		tmp1.scaleLocal(0.5f);
//		
//		Vector3D tmp2 = this.boundingPointsLocal[3].getSubtracted(this.boundingPointsLocal[0]);
//		tmp2.scaleLocal(0.5f);
//		
//		tmp0.addLocal(tmp1);
//		tmp0.addLocal(tmp2);
//		return tmp0;
		return this.centerPointObjSpace.getCopy();
	}
	
	
	/* (non-Javadoc)
	 * @see mTouch.components.bounds.IBoundingShape#getCenterPointWorld()
	 */
	public Vector3D getCenterPointGlobal() {
		if (centerWorldDirty){
			Vector3D tmp = this.getCenterPointLocal();
			tmp.transform(this.peerComponent.getGlobalMatrix());
//			tmp = peerComponent.localToGlobal(tmp);
			this.centerPointWorld = tmp;
			this.centerWorldDirty = false;
			return this.centerPointWorld;
		}
		else{
			return this.centerPointWorld;
		}
//		Vector3D tmp = this.getCenterPointObjSpace();
//		tmp.transform(this.peerComponent.getAbsoluteLocalToWorldMatrix());
//		return tmp;
	}
	
	
	/* (non-Javadoc)
	 * @see mTouch.components.bounds.IBoundingShape#getVectorsObjSpace()
	 */
	public Vector3D[] getVectorsLocal() {
		return this.boundingPointsLocal;
	}

	
	//FIXME also cache?
	public Vector3D[] getVectorsRelativeToParent(){
		Vector3D[] vecs = Vector3D.getDeepVertexArrayCopy(this.boundingPointsLocal);
		Vector3D.transFormArrayLocal(this.peerComponent.getLocalMatrix(), vecs);
		return vecs;
	}
	
	/* (non-Javadoc)
	 * @see mTouch.components.bounds.IBoundingShape#getVectorsWorld(util.math.Matrix)
	 */
	public Vector3D[] getVectorsGlobal() {
		if (this.worldVecsDirty){
			Vector3D[] vecs = Vector3D.getDeepVertexArrayCopy(this.boundingPointsLocal);
			Vector3D.transFormArrayLocal(this.peerComponent.getGlobalMatrix(), vecs);
			this.worldVecs = vecs;
			this.worldVecsDirty = false;
			return this.worldVecs;
		}else{
			return this.worldVecs;
		}
//		Vector3D[] vecs = Vector3D.getDeepVertexArrayCopy(this.boundingPointsLocal);
//		Vector3D.transFormArrayLocal(this.peerComponent.getAbsoluteLocalToWorldMatrix(), vecs);
//		return vecs;
	}
	
	
	
	
	/* (non-Javadoc)
	 * @see com.jMT.components.bounds.IBoundingShape#getHeightXY(com.jMT.components.TransformSpace)
	 */
	//@Override
	public float getHeightXY(TransformSpace transformSpace) {
		switch (transformSpace) {
		case LOCAL:
			return this.getHeightXYObjSpace();
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
	private float getHeightXYObjSpace() {
		return this.getHeightXYVectLocal().length();
	}
	
	/**
	 * Gets the height xy relative to parent.
	 * 
	 * @return the height xy relative to parent
	 */
	private float getHeightXYRelativeToParent() {
//		Vector3D p = this.getHeightXYVectLocal();
//		Matrix m = new Matrix(this.peerComponent.getLocalMatrix());
//		m.removeTranslationFromMatrix();
//		p.transform(m);
//		return p.length();
		
		Vector3D[] v = this.getVectorsRelativeToParent();
		float[] minMax = Tools3D.getMinXYMaxXY(v);
		return minMax[3] - minMax[1];
	}
	
	
	/**
	 * Gets the height xy global.
	 * 
	 * @return the height xy global
	 */
	private float getHeightXYGlobal() {
//		Vector3D p = this.getHeightXYVectLocal();
//		Matrix m = new Matrix(this.peerComponent.getGlobalMatrix());
//		m.removeTranslationFromMatrix();
//		p.transform(m);
//		return p.length();
		
		Vector3D[] v = this.getVectorsGlobal();
		float[] minMax = Tools3D.getMinXYMaxXY(v);
		return minMax[3] - minMax[1];
	}
	
	
	/**
	 * Gets the "height vector". The vector is calculated from the bounds vectors,
	 * representing a vector with the height as its length in object space.
	 * 
	 * @return the height xy vect obj space
	 * 
	 * vector representing the height of the boundingshape of the shape
	 */
	//@Override
	public Vector3D getHeightXYVectLocal() {
		Vector3D[] boundRectVertsLocal = this.getVectorsLocal();
		Vector3D height = boundRectVertsLocal[2].getSubtracted(boundRectVertsLocal[1]);
		return height;
	}

	
	/* (non-Javadoc)
	 * @see com.jMT.components.bounds.IBoundingShape#getWidthXY(com.jMT.components.TransformSpace)
	 */
	//@Override
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
		return this.getWidthXYVectLocal().length();
	}
	
	
	/**
	 * Gets the width xy realtive to parent.
	 * 
	 * @return the width xy realtive to parent
	 */
	private float getWidthXYRealtiveToParent() {
//		Vector3D p = this.getWidthXYVectLocal();
//		Matrix m = new Matrix(this.peerComponent.getLocalMatrix());
//		m.removeTranslationFromMatrix();
//		p.transform(m);
//		return p.length();
		
		Vector3D[] v = this.getVectorsRelativeToParent();
		float[] minMax = Tools3D.getMinXYMaxXY(v);
		return minMax[2] - minMax[0];
	}
	
	/**
	 * Gets the width xy global.
	 * 
	 * @return the width xy global
	 */
	private float getWidthXYGlobal() {
//		Vector3D p = this.getWidthXYVectLocal();
//		Matrix m = new Matrix(this.peerComponent.getGlobalMatrix());
//		m.removeTranslationFromMatrix();
//		p.transform(m);
//		return p.length();
		
		Vector3D[] v = this.getVectorsGlobal();
		float[] minMax = Tools3D.getMinXYMaxXY(v);
		return minMax[2] - minMax[0];
	}
	
	/**
	 * Gets the "Width vector". The vector is calculated from the bounds vectors,
	 * representing a vector with the Width as its length in object space.
	 * 
	 * @return the width xy vect obj space
	 * 
	 * vector representing the Width of the boundingshape of the shape
	 */
	//@Override
	public Vector3D getWidthXYVectLocal() {
		Vector3D[] boundRectVertsLocal = this.getVectorsLocal();
		Vector3D width = boundRectVertsLocal[1].getSubtracted(boundRectVertsLocal[0]);
//		System.out.println("Width of " + this.getName()+ " :" + width);
		return width;
	}


	//@Override
	public boolean isContainedInFrustum(IFrustum frustum) {
		Vector3D[] points = this.getVectorsGlobal();
		for (int i = 0; i < points.length; i++) {
			Vector3D vector3D = points[i];
			int test = frustum.isPointInFrustum(vector3D); 
			if (   test == IFrustum.INSIDE
				|| test == IFrustum.INTERSECT
			){
				return true;
			}
		}
		//Also check if center point is in frustum
		Vector3D center = this.getCenterPointGlobal();
		int test = frustum.isPointInFrustum(center); 
		if (   test == IFrustum.INSIDE
			|| test == IFrustum.INTERSECT
		){
			return true;
		}
		return false;
	}

	
}
