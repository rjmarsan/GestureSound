/*
 * Copyright (c) 2003-2009 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.mt4j.components.bounds;

import org.mt4j.components.MTComponent;
import org.mt4j.components.TransformSpace;
import org.mt4j.components.visibleComponents.shapes.AbstractShape;
import org.mt4j.components.visibleComponents.shapes.mesh.MTTriangleMesh;
import org.mt4j.components.visibleComponents.shapes.mesh.Triangle;
import org.mt4j.util.camera.IFrustum;
import org.mt4j.util.math.FastMath;
import org.mt4j.util.math.Matrix;
import org.mt4j.util.math.Ray;
import org.mt4j.util.math.Vector3D;

import processing.core.PGraphics;


/**
 * The Class BoundingBox.
 * Based on the bounding box class from jMonkeyEngine.
 */
public class OrientedBoundingBox implements IBoundingShape {
	
	/** The peer component. */
	private MTComponent peerComponent;
	
	/** The correct corners. */
	boolean correctCorners = false;
	
	/** The Constant _compVect1. */
	protected static final transient Vector3D _compVect1 = new Vector3D();
	
	/** The Constant _compVect2. */
	protected static final transient Vector3D _compVect2 = new Vector3D();
	
	/** The Constant _compVect3. */
	protected static final transient Vector3D _compVect3 = new Vector3D();

	 /** Extents of the box along the x,y,z axis. */
    public final Vector3D extent = new Vector3D(0, 0, 0); //Extent from the center of the bbox?

    /** Center point of the bounding box    *. */
    protected Vector3D center = new Vector3D();
    
    /** X axis of the Oriented Box. */
    public final Vector3D xAxis = new Vector3D(1, 0, 0);

    /** Y axis of the Oriented Box. */
    public final Vector3D yAxis = new Vector3D(0, 1, 0);

    /** Z axis of the Oriented Box. */
    public final Vector3D zAxis = new Vector3D(0, 0, 1);

    /** Vector array used to store the array of 8 corners the box has. */
    public final Vector3D[] vectorStore = new Vector3D[8];

    
    /** The min x. */
    private float minX;
    
    /** The max x. */
    private float maxX;
    
    /** The min y. */
    private float minY;
    
    /** The max y. */
    private float maxY;
    
    /** The min z. */
    private float minZ;
    
    /** The max z. */
    private float maxZ;
    
    
//    private Vector3D centerPointObjSpace;
	
	private Vector3D[] worldVecs;
	private boolean worldVecsDirty;
	private Vector3D centerPointWorld;
	private boolean centerWorldDirty;
	
    /**
     * The Constructor.
     * 
     * @param peerComponent the peer component
     */
    public OrientedBoundingBox(AbstractShape peerComponent){
    	this(peerComponent, peerComponent.getGeometryInfo().getVertices());
    }
    
    
    /**
     * The Constructor.
     * 
     * @param peerComponent the peer component
     * @param vectors the vectors
     */
    public OrientedBoundingBox(MTComponent peerComponent, Vector3D[] vectors){
    	this.peerComponent = peerComponent;
    	this.init();
    	 
    	this.computeFromVertices(vectors);
    	this.computeCorners();
    	
    	this.worldVecsDirty 	= true;
		this.centerWorldDirty 	= true;
		this.worldVecs 			= this.getVectorsGlobal();
		this.centerPointWorld 	= this.getCenterPointGlobal();
    }
    
    

    
    /**
     * The Constructor.
     * 
     * @param mesh the mesh
     */
    public OrientedBoundingBox(MTTriangleMesh mesh){
    	this.peerComponent = mesh;
    	this.init();
    	
    	this.computeFromTris(mesh.getTriangles(), 0, mesh.getTriangleCount());
    	this.computeCorners();
    	
    	this.worldVecsDirty 	= true;
		this.centerWorldDirty 	= true;
		this.worldVecs 			= this.getVectorsGlobal();
		this.centerPointWorld 	= this.getCenterPointGlobal();
    }
    
    
    /**
     * Inits the bbox.
     */
    private void init(){
    	for (int x = 0; x < vectorStore.length; x++){
    		vectorStore[x] = new Vector3D();
    	}
    }

//	public BoundingVolume transform(Matrix matrix) {
//		if (store == null || store.getType() != Type.OBB) {
//			store = new OrientedBoundingBox();
//		}
//		OrientedBoundingBox toReturn = (OrientedBoundingBox) store;
//		toReturn.extent.set(FastMath.abs(extent.x * scale.x), 
//				FastMath.abs(extent.y * scale.y), 
//				FastMath.abs(extent.z * scale.z));
//		rotate.mult(xAxis, toReturn.xAxis);
//		rotate.mult(yAxis, toReturn.yAxis);
//		rotate.mult(zAxis, toReturn.zAxis);
//		center.mult(scale, toReturn.center);
//		rotate.mult(toReturn.center, toReturn.center);
//		toReturn.center.addLocal(translate);
//		toReturn.correctCorners = false;
//		return toReturn;
//	}
	
//    /*
    
	public void drawBounds(PGraphics g){
		g.pushMatrix();
		g.fill(150,180);
		Vector3D l = this.getCenterPointLocal();
		g.translate(l.x, l.y, l.z);
		float w = getWidthXYVectLocal().length();
		g.box(w);
		g.popMatrix();
	}
    
	/* (non-Javadoc)
	 * @see mTouch.components.bounds.IBoundingShape#containsPoint(util.math.Vector3D)
	 */
	public boolean containsPointLocal(Vector3D point) {
		//TODO punkt erst in local space transformieren!?
		
		_compVect1.setValues(point);
		_compVect1.subtractLocal(center);
		
		float coeff = _compVect1.dot(xAxis);
		if (FastMath.abs(coeff) > extent.x) return false;

		coeff = _compVect1.dot(yAxis);
		if (FastMath.abs(coeff) > extent.y) return false;

		coeff = _compVect1.dot(zAxis);
		if (FastMath.abs(coeff) > extent.z) return false;
		return true;
	}

	
	
    /**
     * Sets the vectorStore information to the 8 corners of the box.
     */
    public void computeCorners() {
//  	Vector3D akEAxis0 = xAxis.mult(extent.x, _compVect1);
//  	Vector3D akEAxis1 = yAxis.mult(extent.y, _compVect2);
//  	Vector3D akEAxis2 = zAxis.mult(extent.z, _compVect3);
    	_compVect1.setValues(xAxis);
    	_compVect1.scaleLocal(extent.x);
    	Vector3D akEAxis0 =  _compVect1;

    	_compVect2.setValues(yAxis);
    	_compVect2.scaleLocal(extent.y);
    	Vector3D akEAxis1 =  _compVect2;

    	_compVect3.setValues(zAxis);
    	_compVect3.scaleLocal(extent.z);
    	Vector3D akEAxis2 =  _compVect3;

    	vectorStore[0].setValues(center);
    	vectorStore[0].subtractLocal(akEAxis0);
    	vectorStore[0].subtractLocal(akEAxis1);
    	vectorStore[0].subtractLocal(akEAxis2);
    	
    	vectorStore[1].setValues(center);
    	vectorStore[1].addLocal(akEAxis0);
    	vectorStore[1].subtractLocal(akEAxis1);
    	vectorStore[1].subtractLocal(akEAxis2);
    	
    	vectorStore[2].setValues(center);
    	vectorStore[2].addLocal(akEAxis0);
    	vectorStore[2].addLocal(akEAxis1);
    	vectorStore[2].subtractLocal(akEAxis2);
    	
    	vectorStore[3].setValues(center);
    	vectorStore[3].subtractLocal(akEAxis0);
    	vectorStore[3].addLocal(akEAxis1);
    	vectorStore[3].subtractLocal(akEAxis2);
    	
    	vectorStore[4].setValues(center);
    	vectorStore[4].subtractLocal(akEAxis0);
    	vectorStore[4].subtractLocal(akEAxis1);
    	vectorStore[4].addLocal(akEAxis2);
    	
    	vectorStore[5].setValues(center);
    	vectorStore[5].addLocal(akEAxis0);
    	vectorStore[5].subtractLocal(akEAxis1);
    	vectorStore[5].addLocal(akEAxis2);

    	vectorStore[6].setValues(center);
    	vectorStore[6].addLocal(akEAxis0);
    	vectorStore[6].addLocal(akEAxis1);
    	vectorStore[6].addLocal(akEAxis2);
    	
    	vectorStore[7].setValues(center);
    	vectorStore[7].subtractLocal(akEAxis0);
    	vectorStore[7].addLocal(akEAxis1);
    	vectorStore[7].addLocal(akEAxis2);

//    	vectorStore[0].set(center).subtractLocal(akEAxis0).subtractLocal(akEAxis1).subtractLocal(akEAxis2);
//    	vectorStore[1].set(center).addLocal(akEAxis0).subtractLocal(akEAxis1).subtractLocal(akEAxis2);
//    	vectorStore[2].set(center).addLocal(akEAxis0).addLocal(akEAxis1).subtractLocal(akEAxis2);
//    	vectorStore[3].set(center).subtractLocal(akEAxis0).addLocal(akEAxis1).subtractLocal(akEAxis2);
//    	vectorStore[4].set(center).subtractLocal(akEAxis0).subtractLocal(akEAxis1).addLocal(akEAxis2);
//    	vectorStore[5].set(center).addLocal(akEAxis0).subtractLocal(akEAxis1).addLocal(akEAxis2);
//    	vectorStore[6].set(center).addLocal(akEAxis0).addLocal(akEAxis1).addLocal(akEAxis2);
//    	vectorStore[7].set(center).subtractLocal(akEAxis0).addLocal(akEAxis1).addLocal(akEAxis2);
    	correctCorners = true;
    }


    
    /* (non-Javadoc)
     * @see util.BoundingShape#getIntersectionPoint(util.math.Ray)
     */
    public Vector3D getIntersectionLocal(Ray ray) {
//        Vector3D diff = _compVect1.set(ray.origin).subtractLocal(center);
    	 _compVect1.setValues(ray.getRayStartPoint());
    	 _compVect1.subtractLocal(center);
    	 Vector3D diff = _compVect1;
    	
    	 
        // convert ray to box coordinates
//        Vector3D direction = _compVect2.set(ray.direction.x, ray.direction.y, ray.direction.z);
    	 Vector3D rayDirection = ray.getDirection();
    	 _compVect2.setValues(rayDirection);
    	 Vector3D direction = _compVect2;
        
        float[] t = { 0f, Float.POSITIVE_INFINITY };
        
        float saveT0 = t[0], saveT1 = t[1];
        boolean notEntirelyClipped = 
        			this.clip(+direction.x, -diff.x - extent.x, t)
                && 	this.clip(-direction.x, +diff.x - extent.x, t)
                && 	this.clip(+direction.y, -diff.y - extent.y, t)
                && 	this.clip(-direction.y, +diff.y - extent.y, t)
                && 	this.clip(+direction.z, -diff.z - extent.z, t)
                && 	this.clip(-direction.z, +diff.z - extent.z, t);
        
        if (notEntirelyClipped && (t[0] != saveT0 || t[1] != saveT1)) {
            if (t[1] > t[0]) {
                float[] distances = t;
                
                Vector3D point1 = new Vector3D(rayDirection);
                point1.scaleLocal(distances[0]);
                point1.addLocal(ray.getRayStartPoint());
                
                Vector3D point2 = new Vector3D(rayDirection);
                point2.scaleLocal(distances[1]);
                point2.addLocal(ray.getRayStartPoint());
                
//                Vector3D[] points = new Vector3D[] { 
//                		point1,
//                		point2
////                        new Vector3D(ray.direction).multLocal(distances[0]).addLocal(ray.origin),
////                        new Vector3D(ray.direction).multLocal(distances[1]).addLocal(ray.origin)
//                };
                
//                IntersectionRecord record = new IntersectionRecord(distances, points);
//                return record;
                //return the closest point to the rayorigin!
               
//                System.out.println("2 points, 1:" + point1 + " 2:" + point2);
                
                
                _compVect3.setValues(point1);
                _compVect3.subtractLocal(ray.getRayStartPoint());
                float dist1Length = _compVect3.length();
                
                _compVect3.setValues(point2);
                _compVect3.subtractLocal(ray.getRayStartPoint());
                float dist2Length = _compVect3.length();
                
//                System.out.println("Intersection closest to ray origin: " +  (dist1Length > dist2Length? point2 : point1)  );
                return (dist1Length > dist2Length? point2 : point1); //point 1 or 2!
            }
                
            float[] distances = new float[] { t[0] };
            Vector3D point3 = new Vector3D(rayDirection);
            point3.scaleLocal(distances[0]);
            point3.addLocal(ray.getRayStartPoint());
            
            
//            Vector3D[] points = new Vector3D[] { 
//            		point3
////                    new Vector3D(ray.direction).multLocal(distances[0]).addLocal(ray.origin),
//            };
            
//            IntersectionRecord record = new IntersectionRecord(distances, points);
//            return record;  
//            System.out.println("point :" + point3 );
            return point3;
        } 
            
//        return new IntersectionRecord();  
//        System.out.println("no point.");
        return null;
    }

    /**
     * <code>clip</code> determines if a line segment intersects the current
     * test plane.
     * 
     * @param denom the denominator of the line segment.
     * @param numer the numerator of the line segment.
     * @param t test values of the plane.
     * 
     * @return true if the line segment intersects the plane, false otherwise.
     */
    private boolean clip(float denom, float numer, float[] t) {
        // Return value is 'true' if line segment intersects the current test
        // plane. Otherwise 'false' is returned in which case the line segment
        // is entirely clipped.
        if (denom > 0.0f) {
            if (numer > denom * t[1])
                return false;
            if (numer > denom * t[0])
                t[0] = numer / denom;
            return true;
        } else if (denom < 0.0f) {
            if (numer > denom * t[0])
                return false;
            if (numer > denom * t[1])
                t[1] = numer / denom;
            return true;
        } else {
            return numer <= 0.0;
        }
    }

    
    /**
     * Compute from vertices.
     * 
     * @param vertices the vertices
     */
    public void computeFromVertices(Vector3D[] vertices){
    	if(vertices.length <= 0){
    		System.err.println("No vertices to compute Bounding box by!");
    		return;
    	}
    	
    	minX = vertices[0].x;
    	maxX = vertices[0].x; 
    	
    	minY = vertices[0].y; 
        maxY = vertices[0].y; 
        
        minZ = vertices[0].z;
        maxZ = vertices[0].z;
    	
        Vector3D v;
    	for(int i = 0; i < vertices.length; i++){
    		v = vertices[i];

    		 if (v.x < minX)
                 minX = v.x;
             else if (v.x > maxX)
                 maxX = v.x;

             if (v.y < minY)
                 minY = v.y;
             else if (v.y > maxY)
                 maxY = v.y;

             if (v.z < minZ)
                 minZ = v.z;
             else if (v.z > maxZ)
                 maxZ = v.z;

    	}
    	
    	this.center.setXYZ(minX + maxX, minY + maxY, minZ + maxZ);
    	this.center.scaleLocal(0.5f);
    	
//    	Vector3D center = new Vector3D(minX, minY, minZ);
//		center.addLocal(new Vector3D(maxX, maxY, maxZ));
//		center.scale(0.5f);
//		this.center = center;
//		center.set(min.addLocal(max));
//		center.multLocal(0.5f);
    	
//		System.out.println("Center: " + this.center);

		extent.setXYZ(maxX - center.x, maxY - center.y, maxZ - center.z);
//		extent.set(max.x - center.x, max.y - center.y, max.z - center.z);

//		System.out.println("Extent: x:" + this.extent.x + " y:" + this.extent.y + " z:" + this.extent.z + " Extent length:" + this.extent.length());
		
		/*
    	width  = (float)Math.sqrt((x2 - x1) * (x2 - x1)); 
    	height = (float)Math.sqrt((y2 - y1) * (y2 - y1));
    	depth  = (float)Math.sqrt((z2 - z1) * (z2 - z1));
		 */
		
		xAxis.setXYZ(1, 0, 0);
		yAxis.setXYZ(0, 1, 0);
		zAxis.setXYZ(0, 0, 1);

		correctCorners = false;
    }
    

    /**
     * Compute from tris.
     * 
     * @param tris the tris
     * @param start the start
     * @param end the end
     */
	public void computeFromTris(Triangle[] tris, int start, int end) {
		if (end - start <= 0) {
			return;
		}

		Vector3D _compVect1 = new Vector3D();
		_compVect1.setXYZ(tris[start].v0.x, tris[start].v0.y , tris[start].v0.z);
		Vector3D min = _compVect1;

		Vector3D _compVect2 = min.getCopy();
		Vector3D max = _compVect2;

		Vector3D point;
		for (int i = start; i < end; i++) {

			point = tris[i].v0;
			if (point.x < min.x)
				min.x = point.x;
			else if (point.x > max.x)
				max.x = point.x;
			if (point.y < min.y)
				min.y = point.y;
			else if (point.y > max.y)
				max.y = point.y;
			if (point.z < min.z)
				min.z = point.z;
			else if (point.z > max.z)
				max.z = point.z;

			point = tris[i].v1;
			if (point.x < min.x)
				min.x = point.x;
			else if (point.x > max.x)
				max.x = point.x;
			if (point.y < min.y)
				min.y = point.y;
			else if (point.y > max.y)
				max.y = point.y;
			if (point.z < min.z)
				min.z = point.z;
			else if (point.z > max.z)
				max.z = point.z;


			point = tris[i].v2;
			if (point.x < min.x)
				min.x = point.x;
			else if (point.x > max.x)
				max.x = point.x;

			if (point.y < min.y)
				min.y = point.y;
			else if (point.y > max.y)
				max.y = point.y;

			if (point.z < min.z)
				min.z = point.z;
			else if (point.z > max.z)
				max.z = point.z;
		}

//		Vector3D center = min.getCopy();
//		center.addLocal(max);
//		center.scale(0.5f);
//		this.center = center;
		
		this.center.setXYZ(min.x+max.x, min.y+max.y, min.z+max.z);
		this.center.scaleLocal(0.5f);
		
		this.minX = min.x;
		this.minY = min.y;
		this.minZ = min.z;
		
		this.maxX = max.x;
		this.maxY = max.y;
		this.maxZ = max.z;
		
//		center.set(min.addLocal(max));
//		center.multLocal(0.5f);
		
//		System.out.println("Center: " + this.center);

		extent.setXYZ(max.x - center.x, max.y - center.y, max.z - center.z);
//		extent.set(max.x - center.x, max.y - center.y, max.z - center.z);

//		System.out.println("Extent: x:" + this.extent.x + " y:" + this.extent.y + " z:" + this.extent.z + " Extent length:" + this.extent.length());
		
//		xAxis = new Vector3D(1,0,0);
//		yAxis = new Vector3D(0,1,0);
//		zAxis = new Vector3D(0,0,1);
		
		xAxis.setXYZ(1, 0, 0);
		yAxis.setXYZ(0, 1, 0);
		zAxis.setXYZ(0, 0, 1);

		correctCorners = false;
	}

	/* (non-Javadoc)
	 * @see com.jMT.components.bounds.IBoundingShape#getCenterPointObjSpace()
	 */
	//@Override
	public Vector3D getCenterPointLocal() {
		return this.center.getCopy();
	}

	/* (non-Javadoc)
	 * @see com.jMT.components.bounds.IBoundingShape#getCenterPointWorld()
	 */
	//@Override
	public Vector3D getCenterPointGlobal() {
		if (centerWorldDirty){
			Vector3D tmp = this.getCenterPointLocal();
			tmp.transform(this.peerComponent.getGlobalMatrix());
			this.centerPointWorld = tmp;
			this.centerWorldDirty = false;
			return this.centerPointWorld;
		}else{
			return this.centerPointWorld;
		}
//		Vector3D worldCenter = new Vector3D(this.center);
//		worldCenter.transform(this.peerComponent.getAbsoluteLocalToWorldMatrix());
//		return worldCenter;
	}
	
	/**
	 * Gets the max x.
	 * 
	 * @return the max x
	 */
	public float getMaxX() {
		return this.maxX;
	}

	/**
	 * Gets the max y.
	 * 
	 * @return the max y
	 */
	public float getMaxY() {
		return this.maxY;
	}

	/**
	 * Gets the max z.
	 * 
	 * @return the max z
	 */
	public float getMaxZ() {
		return this.maxZ;
	}

	/**
	 * Gets the min x.
	 * 
	 * @return the min x
	 */
	public float getMinX() {
		return this.minX;
	}

	/**
	 * Gets the min y.
	 * 
	 * @return the min y
	 */
	public float getMinY() {
		return this.minY;
	}

	/**
	 * Gets the min z.
	 * 
	 * @return the min z
	 */
	public float getMinZ() {
		return minZ;
	}

	/* (non-Javadoc)
	 * @see com.jMT.components.bounds.IBoundingShape#getVectorsObjSpace()
	 */
	//@Override
	public Vector3D[] getVectorsLocal() {
		if (!this.correctCorners){
			this.computeCorners();
		}
		return this.vectorStore;
	}
	
	public void setGlobalBoundsChanged(){
		this.worldVecsDirty = true;
		this.centerWorldDirty = true;
}
	
	
	/* (non-Javadoc)
	 * @see com.jMT.components.bounds.IBoundingShape#getVectorsWorld(util.math.Matrix)
	 */
	//@Override
	public Vector3D[] getVectorsGlobal() {
		if (this.worldVecsDirty){
			Vector3D[] vecs = Vector3D.getDeepVertexArrayCopy(this.getVectorsLocal());
			Vector3D.transFormArrayLocal(this.peerComponent.getGlobalMatrix(), vecs);
			this.worldVecs = vecs;
			this.worldVecsDirty = false;
			return this.worldVecs;
		}else{
			return this.worldVecs;
		}
//		if (!this.correctCorners){
//			this.computeCorners();
//		}
//		Vector3D[] vecs = Vector3D.getDeepVertexArrayCopy(this.vectorStore);
//		Vector3D.transFormArrayLocal(this.peerComponent.getAbsoluteLocalToWorldMatrix(), vecs);
//		return vecs;
	}


	
	
	
	
	/* (non-Javadoc)
	 * @see com.jMT.components.bounds.IBoundingShape#getHeightXY(com.jMT.components.TransformSpace)
	 */
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
	 * Gets the "height vector" and transforms it to parent relative space, then calculates
	 * its length.
	 * 
	 * @return the height xy relative to parent
	 * 
	 * the height relative to its peer components parent frame of reference
	 */
	private float getHeightXYRelativeToParent() {
		Vector3D p = this.getHeightXYVectLocal();
		Matrix m = new Matrix(this.peerComponent.getLocalMatrix());
		m.removeTranslationFromMatrix();
		p.transform(m);
		return p.length();
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
		Vector3D p = this.getHeightXYVectLocal();
		Matrix m = new Matrix(this.peerComponent.getGlobalMatrix());
		m.removeTranslationFromMatrix();
		p.transform(m);
		return p.length();
	}

	/**
	 * Gets the "height vector". The vector is calculated from the bounds vectors,
	 * representing a vector with the height as its length in object space.
	 * 
	 * @return the height xy vect obj space
	 * 
	 * vector representing the height of the boundingshape of the shape
	 */
	public Vector3D getHeightXYVectLocal() {
		Vector3D[] boundRectVertsLocal = this.getVectorsLocal();
		Vector3D height = boundRectVertsLocal[2].getSubtracted(boundRectVertsLocal[1]);
		return height;
	}

	
	/* (non-Javadoc)
	 * @see com.jMT.components.bounds.IBoundingShape#getWidthXY(com.jMT.components.TransformSpace)
	 */
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
	 * Calculates the width of this shape, by using the
	 * bounding shapes vectors.
	 * Uses the objects local transform. So the width will be
	 * relative to the parent only - not the whole world
	 * 
	 * @return the width xy realtive to parent
	 * 
	 * the width
	 */
	private float getWidthXYRealtiveToParent() {
		Vector3D p = this.getWidthXYVectLocal();
		Matrix m = new Matrix(this.peerComponent.getLocalMatrix());
		m.removeTranslationFromMatrix();
		p.transform(m);
		return p.length();
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
		/*
		Vector3D[] boundRectVertsGlobal = this.getVectorsGlobal();
		
		float[] minMax = Tools3D.getMinXYMaxXY(boundRectVertsGlobal);
		float width = minMax[2] - minMax[0];
		if (true)
			return width;
		
		Vector3D d = boundRectVertsGlobal[1].getSubtracted(boundRectVertsGlobal[0]);
		
		Vector3D a = new Vector3D(boundRectVertsGlobal[1].x - boundRectVertsGlobal[0].x,0,0);
		Matrix mm = new Matrix(this.peerComponent.getGlobalMatrix());
		mm.removeTranslationFromMatrix();
		a.transform(mm);
		if (true)
			return a.length();
		*/
		
		Vector3D p = this.getWidthXYVectLocal();
		Matrix m = new Matrix(this.peerComponent.getGlobalMatrix());
		m.removeTranslationFromMatrix();
		p.transform(m);
		return p.length();
	}

	
	/**
	 * Gets the "Width vector". The vector is calculated from the bounds vectors,
	 * representing a vector with the Width as its length in object space.
	 * 
	 * @return the width xy vect obj space
	 * 
	 * vector representing the Width of the boundingshape of the shape
	 */
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
		return false;
	}



	//TODO getDepth()
	
}
