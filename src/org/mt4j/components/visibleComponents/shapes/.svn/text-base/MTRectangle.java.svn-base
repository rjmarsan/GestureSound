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

import org.mt4j.MTApplication;
import org.mt4j.components.TransformSpace;
import org.mt4j.components.bounds.BoundsZPlaneRectangle;
import org.mt4j.components.bounds.IBoundingShape;
import org.mt4j.util.MT4jSettings;
import org.mt4j.util.math.Vector3D;
import org.mt4j.util.math.Vertex;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * A simple rectangular shape.
 * 
 * @author Christopher Ruff
 */
public class MTRectangle extends MTPolygon {
	private PositionAnchor currentAnchor;
	
	//FIXME if the rectangle is rotated, the boundsZPlaneRectangle wont work anymore!
	
	public enum PositionAnchor{
		LOWER_LEFT,
		LOWER_RIGHT,
		UPPER_LEFT,
		CENTER
	}
	
	/**
	 * Instantiates a new mT rectangle.
	 * 
	 * @param texture the texture
	 * @param applet the applet
	 */
	public MTRectangle(PImage texture, PApplet applet) {
		this(0 ,0 ,0, texture.width, texture.height, applet);
		
		//hm..this is for when we create textured rects in other threads
		//, because when we init gl texture in other thread it breaks..
		this.setUseDirectGL(false);
		
		//IF we are useing OpenGL, set useDirectGL to true 
		//(=>creates OpenGL texture, draws with pure OpenGL commands)
		//in our main thread.
		if (MT4jSettings.getInstance().isOpenGlMode() && applet instanceof MTApplication){
			MTApplication app = (MTApplication)applet;
			app.invokeLater(new Runnable() {
				public void run() {
					if (!isUseDirectGL())
						setUseDirectGL(true);
				}
			});
		}
		
		this.setTexture(texture);
		this.setTextureEnabled(true);
	}
	
	/**
	 * Instantiates a new mT rectangle.
	 * 
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param width the width
	 * @param height the height
	 * @param pApplet the applet
	 */
	public MTRectangle(float x, float y, float z, float width, float height, PApplet pApplet) {
		this(new Vertex(x,y,z,0,0),width,height,pApplet);
	}

	/**
	 * Instantiates a new mT rectangle.
	 * 
	 * @param x the x
	 * @param y the y
	 * @param width the width
	 * @param height the height
	 * @param pApplet the applet
	 */
	public MTRectangle(float x, float y, float width, float height, PApplet pApplet) {
		this(new Vertex(x,y,0,0,0),width,height,pApplet);
	}
	
	/**
	 * Instantiates a new mT rectangle.
	 * 
	 * @param upperLeft the upper left
	 * @param width the width
	 * @param height the height
	 * @param pApplet the applet
	 */
	public MTRectangle(Vertex upperLeft, float width, float height, PApplet pApplet) {
		super(new Vertex[]{
				new Vertex(upperLeft.x,			upperLeft.y, 		upperLeft.z, 0, 0), 
				new Vertex(upperLeft.x+width, 	upperLeft.y, 		upperLeft.z, 1, 0), 
				new Vertex(upperLeft.x+width, 	upperLeft.y+height, upperLeft.z, 1, 1), 
				new Vertex(upperLeft.x,			upperLeft.y+height,	upperLeft.z, 0, 1), 
				new Vertex(upperLeft.x,			upperLeft.y,		upperLeft.z, 0, 0)},
				pApplet);
		
		this.setName("unnamed rectangle");
		//
		this.setBoundsBehaviour(AbstractShape.BOUNDS_ONLY_CHECK);
		
		currentAnchor = PositionAnchor.CENTER;
	}

	
	/* (non-Javadoc)
	 * @see com.jMT.components.visibleComponents.shapes.MTPolygon#computeDefaultBounds()
	 */
	@Override
	protected IBoundingShape computeDefaultBounds(){
		return new BoundsZPlaneRectangle(this);
	}
	
	public PositionAnchor getAnchor(){
		return this.currentAnchor;
	}
	
	public void setAnchor(PositionAnchor anchor){
		this.currentAnchor = anchor;
	}
	
	/* (non-Javadoc)
	 * @see mTouch.components.visibleComponents.shapes.AbstractShape#setPositionGlobal(util.math.Vector3D)
	 */
	@Override
	public void setPositionGlobal(Vector3D position) {
		switch (this.getAnchor()) {
		case CENTER:
			super.setPositionGlobal(position);
			break;
		case LOWER_LEFT:{
			Vertex[] vertices = this.getVerticesGlobal();
			Vertex lowerLeft = new Vertex(vertices[3]);
			this.translateGlobal(position.getSubtracted(lowerLeft));
		}break;
		case LOWER_RIGHT:{
			Vertex[] vertices = this.getVerticesGlobal();
			Vertex v = new Vertex(vertices[2]);
			this.translateGlobal(position.getSubtracted(v));
		}break;
		case UPPER_LEFT:{
			Vertex[] vertices = this.getVerticesGlobal();
			Vertex upperLeft = new Vertex(vertices[0]);
			this.translateGlobal(position.getSubtracted(upperLeft));
		}break;
		default:
			break;
		}
	}
	
	@Override
	public void setPositionRelativeToParent(Vector3D position) {
		switch (this.getAnchor()) {
		case CENTER:
			super.setPositionRelativeToParent(position);
			break;
		case LOWER_LEFT:{
			Vertex[] vertices = this.getVerticesLocal();
			Vertex lowerLeft = new Vertex(vertices[3]);
			lowerLeft.transform(this.getLocalMatrix());
			this.translate(position.getSubtracted(lowerLeft), TransformSpace.RELATIVE_TO_PARENT);
		}break;
		case LOWER_RIGHT:{
			Vertex[] vertices = this.getVerticesLocal();
			Vertex v = new Vertex(vertices[2]);
			v.transform(this.getLocalMatrix());
			this.translate(position.getSubtracted(v), TransformSpace.RELATIVE_TO_PARENT);
		}break;
		case UPPER_LEFT:{
			Vertex[] vertices = this.getVerticesLocal();
			Vertex v = new Vertex(vertices[0]);
			v.transform(this.getLocalMatrix());
			this.translate(position.getSubtracted(v), TransformSpace.RELATIVE_TO_PARENT);
		}break;
		default:
			break;
		}
	}

	
	public Vector3D getPosition(TransformSpace transformSpace){
		Vector3D v;
		switch (transformSpace) {
		case LOCAL:
			switch (this.getAnchor()) {
			case CENTER:
				return this.getCenterPointLocal();
			case LOWER_LEFT:
				return new Vector3D(this.getVerticesLocal()[3]);
			case LOWER_RIGHT:
				return new Vector3D(this.getVerticesLocal()[2]);
			case UPPER_LEFT:
				return new Vector3D(this.getVerticesLocal()[0]);
			default:
				break;
			}
			break;
		case RELATIVE_TO_PARENT:
			switch (this.getAnchor()) {
			case CENTER:
				return this.getCenterPointRelativeToParent();
			case LOWER_LEFT:
				v = new Vector3D(this.getVerticesLocal()[3]);
				v.transform(this.getLocalMatrix());
				return v;
			case LOWER_RIGHT:
				v = new Vector3D(this.getVerticesLocal()[2]);
				v.transform(this.getLocalMatrix());
				return v;
			case UPPER_LEFT:
				v = new Vector3D(this.getVerticesLocal()[0]);
				v.transform(this.getLocalMatrix());
				return v;
			default:
				break;
			}
			break;
		case GLOBAL:
			switch (this.getAnchor()) {
			case CENTER:
				return this.getCenterPointGlobal();
			case LOWER_LEFT:
				v = new Vector3D(this.getVerticesLocal()[3]);
				v.transform(this.getGlobalMatrix());
				return v;
			case LOWER_RIGHT:
				v = new Vector3D(this.getVerticesLocal()[2]);
				v.transform(this.getGlobalMatrix());
				return v;
			case UPPER_LEFT:
				v = new Vector3D(this.getVerticesLocal()[0]);
				v.transform(this.getGlobalMatrix());
				return v;
			default:
				break;
			}
			break;
		default:
			break;
		}
		return null;
	}
	

	/* (non-Javadoc)
	 * @see com.jMT.components.visibleComponents.shapes.MTPolygon#get2DPolygonArea()
	 */
	@Override
	public double get2DPolygonArea() {
		return (getHeightXY(TransformSpace.RELATIVE_TO_PARENT)*getWidthXY(TransformSpace.RELATIVE_TO_PARENT));
	}

	/* (non-Javadoc)
	 * @see com.jMT.components.visibleComponents.shapes.MTPolygon#getCenterOfMassObjSpace()
	 */
	@Override
	public Vector3D getCenterOfMass2DLocal() {
		Vertex[] pickVertsUntrans = this.getVerticesLocal();
		Vector3D center = new Vector3D(
				pickVertsUntrans[0].getX() + ((pickVertsUntrans[1].getX() - pickVertsUntrans[0].getX())/2), 
				pickVertsUntrans[1].getY() + ((pickVertsUntrans[2].getY() - pickVertsUntrans[1].getY())/2), 
				pickVertsUntrans[0].getZ());
//		center.transform(this.getAbsoluteLocalToWorldMatrix());
		return center;
	}
	
	/* (non-Javadoc)
	 * @see com.jMT.components.visibleComponents.shapes.MTPolygon#getCenterPointObjectSpace()
	 */
	@Override
	public Vector3D getCenterPointLocal(){
		return this.getCenterOfMass2DLocal();
	}

	/*
	public float getHeight() {
		Vertex[] v1 = this.getPickVerticesTrans();
		Vertex bla = (Vertex)v1[2].minus(v1[1]);
		return  bla.magnitude();
	}
	
	public float getWidth() {
		Vertex[] v = this.getPickVerticesTrans();
		Vertex bla = (Vertex)v[1].minus(v[0]);
		return bla.magnitude();
	}
	
	*/
	
	
	//TODO wie mit local und world umgehen?
	//setSize etc als "world" machen,
	//evtl zusätzliche methoden für setSizeLocal?
	
//	//FIXME this actually is setSizeLOCAL!!!
//	/**
//	 * Sets the size of the rectangle.
//	 * Changes the vertices themself, not the transform, to allow for hassle-free non-uniform scaling.
//	 * <p>Overridden because shearing will occur if the component was rotated and then scaled non-uniformly!
//	 * <br>This method preserves the orientation
//	 * 
//	 * @param width the width
//	 * @param height the height
//	 * 
//	 * @return true, if sets the size xy relative to parent
//	 */
//	@Override
//	public boolean setSizeXYRelativeToParent(float width, float height){
//		if (width > 0 && height > 0){
//			Vertex[] v = this.getVerticesLocal();
//			this.setVertices(new Vertex[]{
//					new Vertex(v[0].x,			v[0].y, 		v[0].z, v[0].getTexCoordU(), v[0].getTexCoordV(), v[0].getR(), v[0].getG(), v[0].getB(), v[0].getA()), 
//					new Vertex(v[0].x+width, 	v[1].y, 		v[1].z, v[1].getTexCoordU(), v[1].getTexCoordV(), v[1].getR(), v[1].getG(), v[1].getB(), v[1].getA()), 
//					new Vertex(v[0].x+width, 	v[1].y+height, 	v[2].z, v[2].getTexCoordU(), v[2].getTexCoordV(), v[2].getR(), v[2].getG(), v[2].getB(), v[2].getA()), 
//					new Vertex(v[3].x,			v[0].y+height,	v[3].z, v[3].getTexCoordU(), v[3].getTexCoordV(), v[3].getR(), v[3].getG(), v[3].getB(), v[3].getA()), 
//					new Vertex(v[4].x,			v[4].y,			v[4].z, v[4].getTexCoordU(), v[4].getTexCoordV(), v[4].getR(), v[4].getG(), v[4].getB(), v[4].getA()), 
//			});
//			return true;
//		}else
//			return false;
//	}
	
	
	public void setSizeLocal(float width, float height){
		if (width > 0 && height > 0){
			Vertex[] v = this.getVerticesLocal();
			this.setVertices(new Vertex[]{
					new Vertex(v[0].x,			v[0].y, 		v[0].z, v[0].getTexCoordU(), v[0].getTexCoordV(), v[0].getR(), v[0].getG(), v[0].getB(), v[0].getA()), 
					new Vertex(v[0].x+width, 	v[1].y, 		v[1].z, v[1].getTexCoordU(), v[1].getTexCoordV(), v[1].getR(), v[1].getG(), v[1].getB(), v[1].getA()), 
					new Vertex(v[0].x+width, 	v[1].y+height, 	v[2].z, v[2].getTexCoordU(), v[2].getTexCoordV(), v[2].getR(), v[2].getG(), v[2].getB(), v[2].getA()), 
					new Vertex(v[3].x,			v[0].y+height,	v[3].z, v[3].getTexCoordU(), v[3].getTexCoordV(), v[3].getR(), v[3].getG(), v[3].getB(), v[3].getA()), 
					new Vertex(v[4].x,			v[4].y,			v[4].z, v[4].getTexCoordU(), v[4].getTexCoordV(), v[4].getR(), v[4].getG(), v[4].getB(), v[4].getA()), 
			});
		}
	}
	
//	/* (non-Javadoc)
//	 * @see com.jMT.components.visibleComponents.shapes.MTPolygon#setHeightXYRelativeToParent(float)
//	 */
//	@Override
//	public boolean setHeightXYRelativeToParent(float height){
//		if (height > 0){
//			Vertex[] v = this.getVerticesLocal();
//			this.setVertices(new Vertex[]{
//					new Vertex(v[0].x,	v[0].y, 		v[0].z, v[0].getTexCoordU(), v[0].getTexCoordV(), v[0].getR(), v[0].getG(), v[0].getB(), v[0].getA()), 
//					new Vertex(v[1].x, 	v[1].y, 		v[1].z, v[1].getTexCoordU(), v[1].getTexCoordV(), v[1].getR(), v[1].getG(), v[1].getB(), v[1].getA()), 
//					new Vertex(v[2].x, 	v[1].y+height, 	v[2].z, v[2].getTexCoordU(), v[2].getTexCoordV(), v[2].getR(), v[2].getG(), v[2].getB(), v[2].getA()), 
//					new Vertex(v[3].x,	v[1].y+height,	v[3].z, v[3].getTexCoordU(), v[3].getTexCoordV(), v[3].getR(), v[3].getG(), v[3].getB(), v[3].getA()), 
//					new Vertex(v[4].x,	v[4].y,			v[4].z, v[4].getTexCoordU(), v[4].getTexCoordV(), v[4].getR(), v[4].getG(), v[4].getB(), v[4].getA()), 
//			});
//			return true;
//		}else
//			return false;
//	}
	
	public void setHeightLocal(float height){
		Vertex[] v = this.getVerticesLocal();
		this.setVertices(new Vertex[]{
				new Vertex(v[0].x,	v[0].y, 		v[0].z, v[0].getTexCoordU(), v[0].getTexCoordV(), v[0].getR(), v[0].getG(), v[0].getB(), v[0].getA()), 
				new Vertex(v[1].x, 	v[1].y, 		v[1].z, v[1].getTexCoordU(), v[1].getTexCoordV(), v[1].getR(), v[1].getG(), v[1].getB(), v[1].getA()), 
				new Vertex(v[2].x, 	v[1].y+height, 	v[2].z, v[2].getTexCoordU(), v[2].getTexCoordV(), v[2].getR(), v[2].getG(), v[2].getB(), v[2].getA()), 
				new Vertex(v[3].x,	v[1].y+height,	v[3].z, v[3].getTexCoordU(), v[3].getTexCoordV(), v[3].getR(), v[3].getG(), v[3].getB(), v[3].getA()), 
				new Vertex(v[4].x,	v[4].y,			v[4].z, v[4].getTexCoordU(), v[4].getTexCoordV(), v[4].getR(), v[4].getG(), v[4].getB(), v[4].getA()), 
		});
	}
	
//	/**
//	 * Scales the shape to the given width.
//	 * Uses the bounding rectangle for calculation!
//	 * Aspect ratio is preserved!
//	 * 
//	 * @param width the width
//	 * 
//	 * @return true, if the width isnt negative
//	 */
//	@Override
//	public boolean setWidthXYRelativeToParent(float width){
//		if (width > 0){
//			Vertex[] v = this.getVerticesLocal();
//			this.setVertices(new Vertex[]{
//					new Vertex(v[0].x,			v[0].y, v[0].z, v[0].getTexCoordU(), v[0].getTexCoordV(), v[0].getR(), v[0].getG(), v[0].getB(), v[0].getA()), 
//					new Vertex(v[0].x+width, 	v[1].y, v[1].z, v[1].getTexCoordU(), v[1].getTexCoordV(), v[1].getR(), v[1].getG(), v[1].getB(), v[1].getA()), 
//					new Vertex(v[0].x+width, 	v[2].y, v[2].z, v[2].getTexCoordU(), v[2].getTexCoordV(), v[2].getR(), v[2].getG(), v[2].getB(), v[2].getA()), 
//					new Vertex(v[3].x,			v[3].y,	v[3].z, v[3].getTexCoordU(), v[3].getTexCoordV(), v[3].getR(), v[3].getG(), v[3].getB(), v[3].getA()), 
//					new Vertex(v[4].x,			v[4].y,	v[4].z, v[4].getTexCoordU(), v[4].getTexCoordV(), v[4].getR(), v[4].getG(), v[4].getB(), v[4].getA()), 
//			});
//			return true;
//		}else
//			return false;
//	}
	
	public void setWidthLocal(float width){
		if (width > 0){
			Vertex[] v = this.getVerticesLocal();
			this.setVertices(new Vertex[]{
					new Vertex(v[0].x,			v[0].y, v[0].z, v[0].getTexCoordU(), v[0].getTexCoordV(), v[0].getR(), v[0].getG(), v[0].getB(), v[0].getA()), 
					new Vertex(v[0].x+width, 	v[1].y, v[1].z, v[1].getTexCoordU(), v[1].getTexCoordV(), v[1].getR(), v[1].getG(), v[1].getB(), v[1].getA()), 
					new Vertex(v[0].x+width, 	v[2].y, v[2].z, v[2].getTexCoordU(), v[2].getTexCoordV(), v[2].getR(), v[2].getG(), v[2].getB(), v[2].getA()), 
					new Vertex(v[3].x,			v[3].y,	v[3].z, v[3].getTexCoordU(), v[3].getTexCoordV(), v[3].getR(), v[3].getG(), v[3].getB(), v[3].getA()), 
					new Vertex(v[4].x,			v[4].y,	v[4].z, v[4].getTexCoordU(), v[4].getTexCoordV(), v[4].getR(), v[4].getG(), v[4].getB(), v[4].getA()), 
			});
		}
	}
	
	//TODO also overRide setSizeGlobal()!!
	//TODO setSize setzt obj space size nicht relative bis jetzt! einfach width vector transformen und length() holen!
	

}
