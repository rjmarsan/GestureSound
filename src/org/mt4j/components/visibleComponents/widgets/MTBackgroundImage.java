/***********************************************************************
 * mt4j Copyright (c) 2008 - 2009 C.Ruff, Fraunhofer-Gesellschaft All rights reserved.
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

import org.mt4j.MTApplication;
import org.mt4j.components.TransformSpace;
import org.mt4j.components.bounds.BoundsZPlaneRectangle;
import org.mt4j.components.bounds.IBoundingShape;
import org.mt4j.components.visibleComponents.shapes.MTPolygon;
import org.mt4j.util.MT4jSettings;
import org.mt4j.util.math.Vertex;
import org.mt4j.util.opengl.GLTexture;
import org.mt4j.util.opengl.GLTextureParameters;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * The Class MTBackgroundImage. Will display a pixel or svg image with the dimensions of the
 * screen. When using opengl and a pixel image the image can also be used tiled.
 * @author Christopher Ruff
 */
public class MTBackgroundImage extends MTPolygon {
	
	/** The svg image. */
	private MTSvg svgImage;

	/**
	 * Instantiates a new mT background image. 
	 * (Tiling works only with opengl)
	 * 
	 * @param mtApp the mt app
	 * @param bgImage the bg image
	 * @param tiled the tiled
	 */
	public MTBackgroundImage(MTApplication mtApp, PImage bgImage, boolean tiled) {
		super(new Vertex[]{
				new Vertex(0,0,0 , 0,0),
				new Vertex(mtApp.width,0,0, 1,0),
				new Vertex(mtApp.width,mtApp.height,0, 1,1),
				new Vertex(0,mtApp.height,0, 0,1)}, mtApp);
		
		if (MT4jSettings.getInstance().isOpenGlMode()){
			GLTextureParameters tp = new GLTextureParameters();
			tp.minFilter = GLTextureParameters.LINEAR; //"LINEAR" disables mip-mapping in opengl
	//		tp.target = GLConstants.RECTANGULAR;
			GLTexture tex = new GLTexture(mtApp, mtApp.width, mtApp.height, tp);
			tex.putImage(bgImage);
			this.setTexture(tex);
		}else{
			this.setTexture(bgImage);
		}
		
		if (tiled){
			//Generate texture coordinates to repeat the texture over the whole background (works only with OpenGL)
			float u = (float)mtApp.width/(float)bgImage.width;
			float v = (float)mtApp.height/(float)bgImage.height;
			
			Vertex[] backgroundVertices = this.getVerticesLocal();
			backgroundVertices[0].setTexCoordU(0);
			backgroundVertices[0].setTexCoordV(0);
			backgroundVertices[1].setTexCoordU(u);
			backgroundVertices[1].setTexCoordV(0);
			backgroundVertices[2].setTexCoordU(u);
			backgroundVertices[2].setTexCoordV(v);
			backgroundVertices[3].setTexCoordU(0);
			backgroundVertices[3].setTexCoordV(v);
			
			//Update changed texture coordinates for opengl buffer drawing
			if (MT4jSettings.getInstance().isOpenGlMode())
				this.getGeometryInfo().updateTextureBuffer(this.isUseVBOs());
		}
		this.setNoStroke(true);
		this.setPickable(false);
	}
	
	
	
	/**
	 * Instantiates a new MT background image.
	 * 
	 * @param pApplet the applet
	 * @param svgImage the svg image
	 * @param stretchToFit the stretch to fit
	 */
	public MTBackgroundImage(PApplet pApplet, MTSvg svgImage, boolean stretchToFitWidth, boolean stretchToFitHeight) {
		super(new Vertex[]{new Vertex(0,0,0 , 0,0),new Vertex(pApplet.width,0,0, 1,0),new Vertex(pApplet.width,pApplet.height,0, 1,1),new Vertex(0,pApplet.height,0, 0,1)}, pApplet);
		this.svgImage = svgImage;
		this.setPickable(false);
		//Actually dont draw this polygon - only its children (this.setVisible(false) would not draw the children)
		this.setNoFill(true);
		this.setNoStroke(true);
		//Because this is used in 2D on the z=0 plane probably. 
		this.setBoundingShape(new BoundsZPlaneRectangle(this));
		this.addChild(svgImage);
		
		if (stretchToFitWidth && stretchToFitHeight){
			svgImage.setSizeXYRelativeToParent(this.getWidthXY(TransformSpace.LOCAL), this.getHeightXY(TransformSpace.LOCAL));
		}else if (stretchToFitWidth){
			svgImage.setWidthXYRelativeToParent(this.getWidthXY(TransformSpace.LOCAL));
		}else if (stretchToFitHeight){
			svgImage.setHeightXYRelativeToParent(this.getHeightXY(TransformSpace.LOCAL));
		}
		svgImage.setPositionRelativeToParent(this.getCenterPointLocal());//Center the svg on the center of this polygon
		svgImage.setPickable(false);
	}
	
	
	@Override
	protected IBoundingShape computeDefaultBounds() {
		return	new BoundsZPlaneRectangle(this);
	}
	
	public MTSvg getSVGImage(){
		return this.svgImage;
	}

	
}
