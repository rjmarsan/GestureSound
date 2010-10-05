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
package org.mt4j.components.visibleComponents.font;

import java.util.List;

import org.mt4j.components.bounds.IBoundingShape;
import org.mt4j.components.visibleComponents.GeometryInfo;
import org.mt4j.components.visibleComponents.shapes.mesh.MTTriangleMesh;
import org.mt4j.util.math.Tools3D;
import org.mt4j.util.math.Vertex;
import org.mt4j.util.opengl.GluTrianglulator;

import processing.core.PApplet;

/**
 * A class representing the character of a vector font.
 * 
 * @author Christopher Ruff
 */
public class VectorFontCharacter extends 
		MTTriangleMesh 
//		MTComplexPolygon
		implements IFontCharacter {
	
	/** The unicode. */
	private String unicode;
	
	/** The horizontal dist. */
	private int horizontalDist;
	
	
	//TODO make constructor with leftoffset, unicode, horzindalAdv
	
	/**
	 * A vector font character class.
	 * The specified contour vertices are assumed to lie in the z=0 plane.
	 * 
	 * @param contours the contours
	 * @param pApplet the applet
	 */
	public VectorFontCharacter(/*Vertex[] innerVertices,*/ List<Vertex[]> contours, PApplet pApplet) {
//		super(innerVertices, outlines, pApplet);
//		/*
		 //Create dummy vertices, will be replaced later in the constructor
		super(pApplet, new GeometryInfo(pApplet, new Vertex[]{}), false);
		
		//Caluculate vertices from bezierinformation
		int segments = 10; 
		List<Vertex[]> bezierContours = Tools3D.createVertexArrFromBezierVertexArrays(contours, segments);
		
		//Triangulate bezier contours
		GluTrianglulator triangulator = new GluTrianglulator(pApplet);
		List<Vertex> tris = triangulator.tesselate(bezierContours);
		//Set new geometry info with triangulated vertices
		super.setGeometryInfo(new GeometryInfo(pApplet, tris.toArray(new Vertex[tris.size()])));
		//Set Mesh outlines
		this.setOutlineContours(bezierContours);
		//Delete triangulator (C++ object)
		triangulator.deleteTess(); 
//		*/
		
		this.setPickable(false);
	}

	/**
	 * Gets the contours.
	 * 
	 * @return the contours
	 */
	public List<Vertex[]> getContours(){
		return this.getOutlineContours();
	}
	
	
	@Override
	protected void setDefaultGestureActions() {
		//no gestures
	}
	
	/* (non-Javadoc)
	 * @see com.jMT.components.visibleComponents.shapes.AbstractShape#computeDefaultBounds()
	 */
	@Override
	protected IBoundingShape computeDefaultBounds(){
//		return new BoundsZPlaneRectangle(this);
		//We assume that font characters never get picked or anything 
		//and hope the creation speeds up through not calculating a bounding shape
		return null;
	}

	/* (non-Javadoc)
	 * @see com.jMT.components.visibleComponents.font.IFontCharacter#getUnicode()
	 */
	public String getUnicode() {
		return unicode;
	}
	
	/**
	 * Sets the unicode.
	 * 
	 * @param unicode the new unicode
	 */
	public void setUnicode(String unicode) {
		this.unicode = unicode;
	}

	/**
	 * The horizontal advancement distance specifies, how many units
	 * to the right, after this character the following character may be placed.
	 * 
	 * @return the horizontal dist
	 */
	public int getHorizontalDist() {
		return horizontalDist;
	}

	/**
	 * This shouldnt be set manually, except by the font parser/creator.
	 * 
	 * @param horizontalDist the horizontal dist
	 */
	public void setHorizontalDist(int horizontalDist) {
		this.horizontalDist = horizontalDist;
	}
	
	
	@Override
	protected void destroyDisplayLists() {
		super.destroyDisplayLists();
		
		//this should actually only be called explicitly since a fontchar is
		//usually not child of a component
		//So we destroy the list if we explicitly destroy a font
	}


}
