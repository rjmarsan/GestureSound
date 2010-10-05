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

import org.mt4j.components.bounds.BoundsZPlaneRectangle;
import org.mt4j.components.bounds.IBoundingShape;
import org.mt4j.util.MTColor;
import org.mt4j.util.math.Vector3D;
import org.mt4j.util.math.Vertex;

import processing.core.PApplet;

/**
 * A simple ellipse shape.
 * 
 * @author Christopher Ruff
 */
public class MTEllipse extends MTPolygon {
	
	/** The radius x. */
	private float radiusX;
	
	/** The radius y. */
	private float radiusY;
	
	/** The center point. */
	private Vector3D centerPoint;
	
	/** The theta. */
	private float theta;
	
	/** The degrees. */
	private float degrees;

	private int segments;
	
	/**
	 * Instantiates a new mT ellipse.
	 * 
	 * @param pApplet the applet
	 * @param centerPoint the center point
	 * @param radiusX the radius x
	 * @param radiusY the radius y
	 */
	public MTEllipse(PApplet pApplet, Vector3D centerPoint, float radiusX, float radiusY) {
		this(pApplet, centerPoint, radiusX, radiusY, 45);
	}
	
	/**
	 * Instantiates a new mT ellipse.
	 * 
	 * @param pApplet the applet
	 * @param centerPoint the center point
	 * @param radiusX the radius x
	 * @param radiusY the radius y
	 * @param segments the segments
	 */
	public MTEllipse(PApplet pApplet, Vector3D centerPoint, float radiusX, float radiusY, int segments) {
		super(new Vertex[0], pApplet);
		this.radiusX 		= radiusX;
		this.radiusY 		= radiusY;
		this.centerPoint 	= centerPoint;
		this.segments = segments;
		theta = 0.0f;
		degrees = (float)Math.toRadians(360);
		
		this.setStrokeWeight(1);
		this.setNoFill(false);
		this.setNoStroke(false);
		
		this.create();
		
		this.setBoundsBehaviour(AbstractShape.BOUNDS_CHECK_THEN_GEOMETRY_CHECK);
		
		this.setName("unnamed MTEllipse");
	}

	/**
	 * Sets the degrees for the ellipse. 360 draws a full circle/ellipse
	 * while smaller values only draw part of the circle/ellipse.
	 * To take effect, <code>recreate()</code> has to be called.
	 * 
	 * @param degrees the new degrees
	 */
	public void setDegrees(float degrees){
		this.degrees = (float)Math.toRadians(degrees);
	}
	
	
	/**
	 * Gets the degrees for the ellipse. 360 draws a full circle/ellipse
	 * while smaller values only draw part of the circle/ellipse.
	 * 
	 * @return the degrees
	 */
	public float getDegrees(){
		return (float)Math.toDegrees(this.degrees);
	}
	
	
	/**
	 * (Re-)creates the ellipse using its current settings.
	 */
	public void create(){
		this.setVertices(this.getVertices(segments));
	}
	
	
	@Override
	protected IBoundingShape computeDefaultBounds() {
//		super.computeDefaultBounds();
		 //FIXME if the ellipse is rotatet X or Y, the bounding shape doesent work anymore.. to be safe we
		//would have to use boundingshpere or box..but slower..
		return new BoundsZPlaneRectangle(this);
	}

	/**
	 * Gets the vertices.
	 * 
	 * @param resolution the resolution
	 * 
	 * @return the vertices
	 */
	protected Vertex[] getVertices(int resolution){
		Vertex[] verts = new Vertex[resolution+1];
		
		float t;
		float inc = degrees / (float)resolution;
		
		double cosTheta = Math.cos(theta);
		double sinTheta = Math.sin(theta);
		
		MTColor fillColor = this.getFillColor();
		
		for (int i = 0; i < resolution; i++){
			t = 0 + (i * inc);
//			float x = (float) (centerPoint.x + (radiusX * Math.cos(t) * cosTheta) //TODO remove theta stuff? oder enablen als parameter?
//						- (radiusY * Math.sin(t) * sinTheta) );
//			float y = (float) (centerPoint.y + (radiusX * Math.cos(t) * sinTheta)
//						+ (radiusY * Math.sin(t) * cosTheta) );
			float x = (float) (centerPoint.x - (radiusX * Math.cos(t) * cosTheta)
					+ (radiusY * Math.sin(t) * sinTheta) );
			float y = (float) (centerPoint.y - (radiusX * Math.cos(t) * sinTheta)
					- (radiusY * Math.sin(t) * cosTheta) );
			
			verts[i] = new Vertex(x, y, centerPoint.z, fillColor.getR(), fillColor.getG(), fillColor.getB(), fillColor.getAlpha());
		}
		verts[verts.length-1] = verts[0];
//		System.out.println("Points: " + verts.length);
		
		//Create tex coords
		float width = radiusX*2;
		float height = radiusY*2;
		float upperLeftX = centerPoint.x-radiusX;
		float upperLeftY = centerPoint.y-radiusY;
		for (int i = 0; i < verts.length; i++) {
			Vertex vertex = verts[i];
			vertex.setTexCoordU((vertex.x-upperLeftX)/width);
			vertex.setTexCoordV((vertex.y-upperLeftY)/height);
			//System.out.println("TexU:" + vertex.getTexCoordU() + " TexV:" + vertex.getTexCoordV());
		}
		
		return verts;
	}
	
//	public List<Vector3D> getVerticesAbsolute(int resolution){
//		List<Vector3D> returnPts = new ArrayList<Vector3D>();
//
//		float t;
//
//		float inc = (float)Math.toRadians(rangeAng) / (float)resolution;
//		for (int i = 0; i < resolution; i++)
//		{
//			t = minAng + (i * inc);
//
//			float x = (float) (centerPt.x + (a * Math.cos(t) * Math.cos(theta))
//						- (b * Math.sin(t) * Math.sin(theta)) );
//			float y = (float) (centerPt.y + (a * Math.cos(t) * Math.sin(theta))
//						+ (b * Math.sin(t) * Math.cos(theta)) );
//		}
//
//		return returnPts;
//	}
	
	//
	//
	//
//		@Override
//		public Vector3D getGeometryIntersection(Ray ray) {
//			// TODO Auto-generated method stub
//			return null;
//		}
	//
	//
	//
//		@Override
//		public boolean isGeometryContainsPoint(Vector3D testPoint) {
//			// TODO Auto-generated method stub
//			return false;
//		}
	
	/* (non-Javadoc)
 * @see com.jMT.components.visibleComponents.shapes.MTPolygon#getCenterPointObjectSpace()
 */
@Override
	public Vector3D getCenterPointLocal() {
		Vector3D center = new Vector3D(this.centerPoint);
		return center;
	}
	

	//FIXME doesent work, seems to only get points in the shape of a outer rectangle
	/*
	public boolean isInsideEllipse(float ex,float ey,float w,float h,float px,float py) {
	    //Determine and normalize quadrant.
		float dx = Math.abs(ex-px);
		float dy = Math.abs(ey-py);
		double l;
	 
	    //Shortcut
	    if( dx > w/2 || dy > h/2 ) {
	      return false;
	    }
	 
	    //Calculate the semi-latus rectum of the ellipse at the given point
	    l = Math.sqrt( (double)((1-((dx*dx)/(w*w))) * (h*h)) ); 
	 
	    return dy < l;
	}  
	*/


	
}
