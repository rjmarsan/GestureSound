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
package org.mt4j.util.math;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import org.mt4j.components.interfaces.IMTComponent3D;
import org.mt4j.components.visibleComponents.GeometryInfo;
import org.mt4j.components.visibleComponents.StyleInfo;
import org.mt4j.util.MT4jSettings;
import org.mt4j.util.camera.Icamera;
import org.mt4j.util.opengl.GLTexture;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PGraphics3D;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.opengl.PGraphicsOpenGL;


/**
 * Class containing mostly static convenience utility methods.
 * 
 * @author Christopher Ruff
 */
public class Tools3D {
	//Declared here and static so it wont have to be initialize at every call to unproject
	/** The fb. */
	private static FloatBuffer fb = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asFloatBuffer();
	
	/** The fb un. */
	private static FloatBuffer fbUn = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asFloatBuffer();
	
	/** The model. */
	private static DoubleBuffer model;
	
	/** The proj. */
	private static DoubleBuffer proj;
	
	/** The view. */
	private static IntBuffer view;
	
	/** The win pos. */
	private static DoubleBuffer winPos;
	
	static{
		model 	= DoubleBuffer.allocate(16);
		proj 	= DoubleBuffer.allocate(16);
		view 	= IntBuffer.allocate(4);
		winPos 	= DoubleBuffer.allocate(3);
	}
	
	
	/**
	 * Check for gl error.
	 * 
	 * @param gl the gl
	 */
	public static int getGLError(GL gl){
		int error = gl.glGetError();
		if (error != GL.GL_NO_ERROR){
			System.out.println("GL Error: " + error);
		}else{
//			System.out.println("No gl error.");
		}
		return error;
	}
	
	
	/**
	 * Unprojects screen coordinates from 2D into 3D world space and returns a point that
	 * can be used to construct a ray form the camera to that point and check
	 * for intersections with objects.
	 * <p><b>NOTE</b>: if using openGL mode, the openGL context has to be valid at the time of calling this method.
	 * 
	 * @param applet the applet
	 * @param camera the camera
	 * @param screenX the screen x
	 * @param screenY the screen y
	 * 
	 * @return the vector3d
	 */
	public static Vector3D unprojectScreenCoords(PApplet applet, Icamera camera, float screenX, float screenY ){
		Vector3D ret = null;
		applet.pushMatrix();
		camera.update();
		ret = Tools3D.unprojectScreenCoords(applet, screenX, screenY);
		applet.popMatrix();
		return ret;
	}
	
	/**
	 * Unprojects screen coordinates from 2D into 3D world space and returns a point that
	 * can be used to construct a ray form the camera to that point and check
	 * for intersections with objects.
	 * <p><b>NOTE</b>: if using openGL mode, the openGL context has to be valid at the time of calling this method.
	 * 
	 * @param applet processings PApplet object
	 * @param screenX x coordinate on the screen
	 * @param screenY y coordinate on the screen
	 * 
	 * @return a point that lies on the line from the screen coordinates
	 * to the 3d world coordinates
	 */
	public static Vector3D unprojectScreenCoords(PApplet applet, float screenX, float screenY ){ //FIXME MAKE PRIVATE AGAIN! 
		Vector3D returnVect = new Vector3D(-999,-999,-999); //null?
		
//		MT4jSettings.getInstance().setRendererMode(MT4jSettings.P3D_MODE);
		
		switch (MT4jSettings.getInstance().getRendererMode()) {
		case MT4jSettings.OPENGL_MODE:
			int viewport[] = new int[4];
			double[] proj  = new double[16];
			double[] model = new double[16];
			double[] mousePosArr = new double[4];
			
			try{
			PGraphicsOpenGL pgl = ((PGraphicsOpenGL)applet.g); 
			GL gl = pgl.beginGL();  
			GLU glu = pgl.glu;
			
				  gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
				  gl.glGetDoublev(GL.GL_PROJECTION_MATRIX, proj, 0);
				  gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, model, 0);
				  
				  /*
				  System.out.println("OpenGL ProjectionMatrix: ");
				  for (int i = 0; i < proj.length; i++) {
						double p = proj[i];
						System.out.print(p + ", ");
						//if (i%4 == 0 && i==3)
						if (i==3 || i== 7 || i== 11 || i==15) {
							System.out.println();
						}
					  }
				  */
				  
				  /*
				  System.out.println("OpenGL ModelviewMatrix: ");
				  for (int i = 0; i < model.length; i++) {
						double p = model[i];
						System.out.print(p + ", ");
						//if (i%4 == 0 && i==3)
						if (i==3 || i== 7 || i== 11 || i==15) {
							System.out.println();
						}
					  }
				  System.out.println();
				  System.out.println("\n");
				  */
				  
				  /*
				  fbUn.clear();
				  gl.glReadPixels((int)screenX, applet.height - (int)screenY, 1, 1, GL.GL_DEPTH_COMPONENT, GL.GL_FLOAT, fbUn);
				  fbUn.rewind();
				  glu.gluUnProject((double)screenX, applet.height - (double)screenY, (double)fbUn.get(0), model, 0, proj, 0, viewport, 0, mousePosArr, 0);
				  */
				  
				  //FIXME test not using glReadpixel to get the depth at the location
				  //instead we have to build a ray with the result, from the camera location going through the resulst and check for hits ourselves
				  glu.gluUnProject((double)screenX, applet.height - (double)screenY, 0, model, 0, proj, 0, viewport, 0, mousePosArr, 0);
			  pgl.endGL();
			  
			  returnVect = new Vector3D((float)mousePosArr[0], (float)mousePosArr[1], (float)mousePosArr[2]);
			}catch(Exception e){
				e.printStackTrace();
				//System.out.println("Use method getWorldForScreenCoords only when drawing with openGL! And dont put negative screen values in!");
			}
			break;
		case MT4jSettings.P3D_MODE:
//			/*!
			try{
				Vector3D testpoint = new Vector3D(screenX, screenY, 1); //TEST! is 1 as winZ correct? -> or read from depth buffer at that pixel!
				
//				PMatrix modelView = new PMatrix(applet.g.getmodelview);
//				PMatrix projectionM = new PMatrix(applet.g.projection);
				
				//projectionsmatrix mit modelview multiplizieren 
//				projectionM.apply(modelView);
				
				//Ergebnis invertieren
//				PMatrix inv = projectionM.invert();
				
				PMatrix3D modelView 	= new PMatrix3D(applet.g.getMatrix());
				PMatrix3D projectionM 	= new PMatrix3D(((PGraphics3D)applet.g).projection);
				
				projectionM.apply(modelView);
				projectionM.invert();
				
				float[] result = new float[4];
				float[] factor = new float[]{  ((2 * testpoint.getX())  / applet.width)  -1,
											   ((2 * testpoint.getY())  / applet.height) -1, //screenH - y?
												(2 * testpoint.getZ()) -1 ,
												 1,};
				//Matrix mit Vector multiplizieren
				projectionM.mult(factor, result);
				
				//System.out.println("\nResult2: ");
				for (int i = 0; i < result.length; i++) {
					//W auf 1 machen!?
					result[i] /= result[result.length-1]; //normalize so w(4th coord) is 1
					//System.out.print(result[i] + " ");
				}
				
				//aus Result Vector3D machen
				returnVect = new Vector3D(result[0],result[1],result[2]);
			}catch(Exception e){
				e.printStackTrace();
			}
			break;
//			*/
		default:
			break;
		} 
//		System.out.println(returnVect);
		return returnVect;
	} 
	
	
	
	/**
	 * Constructs a picking ray from the components viewing camera position
	 * through the specified screen coordinates.
	 * The viewing camera of the object may not be null!
	 * <p><b>NOTE</b>: the openGL context has to be valid at the time of calling this method.
	 * 
	 * @param applet the applet
	 * @param component the component
	 * @param screenX the screen x
	 * @param screenY the screen y
	 * 
	 * @return the pick ray
	 */
	public static Ray getCameraPickRay(PApplet applet, IMTComponent3D component, float screenX, float screenY ){
//		Vector3D rayStartPoint 		= component.getResonsibleCamera().getPosition();
//		Vector3D newPointInRayDir 	=  Tools3D.unprojectScreenCoords(applet, component.getResonsibleCamera(), screenX, screenY);
//		return new Ray(rayStartPoint, newPointInRayDir);
		return Tools3D.getCameraPickRay(applet, component.getViewingCamera(), screenX, screenY);
	}
	
	/**
	 * Constructs a picking ray from the components viewing camera position
	 * through the specified screen coordinates.
	 * <p><b>NOTE</b>: the openGL context has to be valid at the time of calling this method.
	 * 
	 * @param applet the applet
	 * @param screenX the screen x
	 * @param screenY the screen y
	 * @param camera the camera
	 * 
	 * @return the pick ray
	 */
	public static Ray getCameraPickRay(PApplet applet, Icamera camera, float screenX, float screenY ){
		Vector3D rayStartPoint 		= camera.getPosition();
		Vector3D newPointInRayDir 	=  Tools3D.unprojectScreenCoords(applet, camera, screenX, screenY);
		return new Ray(rayStartPoint, newPointInRayDir);
	}
	
	
	
	/**
	 * Projects the given point to screenspace.
	 * <br>Shows where on the screen the point in 3d-Space will appear according
	 * to the current viewport, model and projection matrices.
	 * <p><b>NOTE</b>: the openGL context has to be valid at the time of calling this method.
	 * 
	 * @param gl the gl
	 * @param glu the glu
	 * @param point the point to project to the screen
	 * 
	 * @return the vector3 d
	 */
	public static Vector3D projectGL(GL gl, GLU glu, Vector3D point){
		return projectGL(gl, glu, point, null);
	}
	
	/**
	 * Projects the given point to screenspace.
	 * <br>Shows where on the screen the point in 3d-Space will appear according
	 * to the current viewport, model and projection matrices.
	 * <br><strong>Note</strong>: this method has to be called between a call to <code>processingApplet.beginGL()</code>
	 * and <code>processingApplet.endGL()</code>
	 * <p><b>NOTE</b>: the openGL context has to be valid at the time of calling this method.
	 * 
	 * @param gl the gl
	 * @param glu the glu
	 * @param point the point
	 * @param store the store - vector to store the result in or null to get a new vector
	 * 
	 * @return the vector3 d
	 */
	public static Vector3D projectGL(GL gl, GLU glu, Vector3D point, Vector3D store){
		if (store == null){
			store = new Vector3D();
		}
		
		model.clear();
		gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, model);
		
		proj.clear();
		gl.glGetDoublev(GL.GL_PROJECTION_MATRIX, proj);
		
		view.clear();
		gl.glGetIntegerv(GL.GL_VIEWPORT, view);
		float viewPortHeight = (float)view.get(3);
		
		winPos.clear();
		glu.gluProject(point.x, point.y, point.z, model, proj, view, winPos);
		
		winPos.rewind();
		float x = (float) winPos.get();
		float y = (float) winPos.get();
		y = viewPortHeight - y;			// Subtract The Current Y Coordinate From The Screen Height.
		
		store.setXYZ(x, y, 0);
		return store;
//		return new Vector3D(x, y, 0);
	}
	
	
	/**
	 * Projects the given 3D point to screenspace.
	 * <br>Shows where on the screen the point in 3d-Space will appear according
	 * to the supplied camera, viewport, and projection matrices.
	 * The modelview is temporarily changed to match the supplied camera matrix.
	 * 
	 * @param applet the applet
	 * @param cam the cam
	 * @param point the point
	 * 
	 * @return the vector3 d
	 */
	public static Vector3D project(PApplet applet, Icamera cam, Vector3D point){
		Vector3D ret;
		applet.pushMatrix();
		cam.update();
		ret = Tools3D.project(applet, point);	
		applet.popMatrix();
		return ret;
	}
	
	
	/**
	 * Projects the given point to screenspace. Uses the current modelview, and projection matrices - so update
	 * them accordingly before calling!
	 * <br>Shows where on the screen the point in 3d-Space will appear according
	 * to the current viewport, model and projection matrices.
	 * <p><b>NOTE</b>: if using openGL mode, the openGL context has to be valid at the time of calling this method.
	 * 
	 * @param applet the applet
	 * @param point the point
	 * 
	 * @return a new projected vector3d
	 */
	public static Vector3D project(PApplet applet, Vector3D point){
		switch (MT4jSettings.getInstance().getRendererMode()) {
		case MT4jSettings.OPENGL_MODE:
			try{ 
				PGraphicsOpenGL pgl = ((PGraphicsOpenGL)applet.g); 
				GL gl 	= pgl.beginGL();  
				GLU glu = pgl.glu;
				Vector3D returnVect = projectGL(gl, glu, point);
				pgl.endGL();
				return returnVect;
			}catch(Exception e){
				e.printStackTrace();
				//System.out.println("Use method getWorldForScreenCoords only when drawing with openGL! And dont put negative screen values in!");
			}
			break;
		case MT4jSettings.P3D_MODE:
//			/*!
			try{
				float x = applet.screenX(point.x, point.y, point.z);
				float y = applet.screenY(point.x, point.y, point.z);
				float z = applet.screenZ(point.x, point.y, point.z);
				return new Vector3D(x,y,z);
			}catch(Exception e){
				e.printStackTrace();
			}
			break;
//			*/
		default:
			return new Vector3D(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
		} 
		return new Vector3D(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
	}
	
	/**
	 * Start drawing in on TOP of everything drawn previously,
	 * also resets the camera, so that something drawn at 0,0,0
	 * will be drawn at the top left corner of the screen regardless
	 * of the camera used
	 * 
	 * You could say this allows you to draw directly on the screen, and
	 * on top of everything else. (at the near clipping plane?)
	 * 
	 * NOTE: you have to CALL endDrawOnTopStayOnScreen() if finished!
	 * 
	 * @param pa the pa
	 */
	public static void beginDrawOnTopStayOnScreen(PApplet pa){
		switch (MT4jSettings.getInstance().getRendererMode()) {
		case MT4jSettings.OPENGL_MODE:
			GL gl = ((PGraphicsOpenGL)pa.g).gl; 
			gl.glDepthFunc(javax.media.opengl.GL.GL_ALWAYS); //turn off Z buffering
			//reset to the default camera
			pa.camera(); 
			break;
		case MT4jSettings.P3D_MODE:
			for(int i=0;i<((PGraphics3D)pa.g).zbuffer.length;i++){
			  ((PGraphics3D)pa.g).zbuffer[i]=Float.MAX_VALUE;
			}
			pa.camera();
			break;
		default:
			break;
		}
	}
	
	/**
	 * Stop drawing in 2D after calling begin2D().
	 * 
	 * @param pa the pa
	 * @param camera the camera
	 */
	public static void endDrawOnTopStayOnScreen(PApplet pa, Icamera camera){
		switch (MT4jSettings.getInstance().getRendererMode()) {
		case MT4jSettings.OPENGL_MODE:
			GL gl = ((PGraphicsOpenGL)pa.g).gl; 
			gl.glDepthFunc(GL.GL_LEQUAL); //This is used by standart processing..
			//Change camera back to current 3d camera
			camera.update();
			break;
		case MT4jSettings.P3D_MODE:
			camera.update();
			break;
		default:
			break;
		}
	}
	
	/**
	 * Allows to draw ontop of everything, regardless of the values in the z-buffer.
	 * To stop doing that, call <code>endDrawOnTop(PApplet pa)</code> .
	 * 
	 * @param g the g
	 */
	public static void disableDepthBuffer(PGraphics g){ 
		switch (MT4jSettings.getInstance().getRendererMode()) {
		case MT4jSettings.OPENGL_MODE:
//			GL gl = ((PGraphicsOpenGL)pa.g).gl;
			GL gl = ((PGraphicsOpenGL)g).gl;
			gl.glPushAttrib(GL.GL_DEPTH_BUFFER_BIT);//FIXME TEST
			gl.glDepthFunc(javax.media.opengl.GL.GL_ALWAYS); //turn off Z buffering
			break;
		case MT4jSettings.P3D_MODE:
//			/*
//			for(int i=0;i<((PGraphics3D)pa.g).zbuffer.length;i++){
//			  ((PGraphics3D)pa.g).zbuffer[i]=Float.MAX_VALUE;
//			}
			for(int i=0;i<((PGraphics3D)g).zbuffer.length;i++){
				  ((PGraphics3D)g).zbuffer[i]=Float.MAX_VALUE;
				}
//			*/ 
			break;
		default:
			break;
		}
	}
	
	/**
	 * End draw on top.
	 * 
	 * @param g the g
	 */
	public static void restoreDepthBuffer(PGraphics g){ 
		switch (MT4jSettings.getInstance().getRendererMode()) {
		case MT4jSettings.OPENGL_MODE:
			GL gl = ((PGraphicsOpenGL)g).gl;
//			gl.glDepthFunc(GL.GL_LEQUAL); //This is used by standart processing..
			//FIXME TEST
			gl.glPopAttrib(); 
			break;
		case MT4jSettings.P3D_MODE:
			break;
		default:
			break;
		}
	}
	
	
	/**
	 * Goes through a list of vector arrays and gets the minimum and maximum
	 * values of all together.
	 * 
	 * @param Vector3DLists the vector3 d lists
	 * 
	 * @return a float[4] {minX, minY, maxX, maxY};
	 */
	public static float[] getMinXYMaxXY(ArrayList<Vertex[]> Vector3DLists) {
		float minX = Float.MAX_VALUE;
		float minY = Float.MAX_VALUE;
		float maxX = Float.MIN_VALUE;
		float maxY = Float.MIN_VALUE;
		for (int j = 0; j < Vector3DLists.size(); j++) {
			Vector3D[] vertices = Vector3DLists.get(j);
			for (int i = 0; i < vertices.length; i++) {
				Vector3D Vector3D = vertices[i];
				if (Vector3D.getX() < minX)
					minX = Vector3D.getX();
				if (Vector3D.getX() > maxX)
					maxX = Vector3D.getX();
				if (Vector3D.getY() < minY)
					minY = Vector3D.getY();
				if (Vector3D.getY() > maxY)
					maxY = Vector3D.getY();
			}
		}
		return new float[]{minX, minY, maxX, maxY};
	}
	
	
	/**
	 * goes through a list of vector arrays and
	 * gets the minimum and maximum values.
	 * 
	 * @param Vector3DList the vector3 d list
	 * 
	 * @return a float[4] {minX, minY, maxX, maxY};
	 */
	public static float[] getMinXYMaxXY(Vector3D[] Vector3DList) {
		float minX = Float.POSITIVE_INFINITY;
		float minY = Float.POSITIVE_INFINITY;
		float maxX = Float.NEGATIVE_INFINITY;
		float maxY = Float.NEGATIVE_INFINITY;
		for (int i = 0; i < Vector3DList.length; i++) {
			Vector3D Vector3D = Vector3DList[i];
			if (Vector3D.getX() < minX)
				minX = Vector3D.getX();
			if (Vector3D.getX() > maxX)
				maxX = Vector3D.getX();
			if (Vector3D.getY() < minY)
				minY = Vector3D.getY();
			if (Vector3D.getY() > maxY)
				maxY = Vector3D.getY();
		}
		return new float[]{minX, minY, maxX, maxY};
	}
	
	
	
	/**
	 * Calculates a random number ranging in between the floor and the ceiling value.
	 * 
	 * @param floor the floor
	 * @param ceiling the ceiling
	 * 
	 * @return the random
	 * 
	 * the generated random number
	 */
	public static float getRandom(float floor, float ceiling){
		return (floor + (float)Math.random()*(ceiling-floor));
	}
	
	
	
	/**
	 * Prints some available openGL extensions to the console.
	 * <p><b>NOTE</b>: the openGL context has to be valid at the time of calling this method.
	 * 
	 * @param pa the pa
	 */
	public static void printGLExtensions(PApplet pa){
		if (!MT4jSettings.getInstance().isOpenGlMode())
			return;
		GL gl =((PGraphicsOpenGL)pa.g).beginGL();
		String ext = gl.glGetString(GL.GL_EXTENSIONS);
		StringTokenizer tok = new StringTokenizer( ext, " " );
		while (tok.hasMoreTokens()) {
			System.out.println(tok.nextToken());
		}
		 int[] redBits 		= new int[1];
         int[] greenBits 	= new int[1];
         int[] blueBits 	= new int[1];
         int[] alphaBits 	= new int[1];
         int[] stencilBits 	= new int[1];
         int[] depthBits 	= new int[1];
         gl.glGetIntegerv(GL.GL_RED_BITS, redBits,0);
         gl.glGetIntegerv(GL.GL_GREEN_BITS, greenBits,0);
         gl.glGetIntegerv(GL.GL_BLUE_BITS, blueBits,0);
         gl.glGetIntegerv(GL.GL_ALPHA_BITS, alphaBits,0);
         gl.glGetIntegerv(GL.GL_STENCIL_BITS, stencilBits,0);
         gl.glGetIntegerv(GL.GL_DEPTH_BITS, depthBits,0);
		System.out.println("Red bits: " + redBits[0]);
		System.out.println("Green bits: " + greenBits[0]);
		System.out.println("Blue bits: " + blueBits[0]);
		System.out.println("Alpha bits: " + blueBits[0]);
		System.out.println("Depth Buffer bits: " + depthBits[0]);
		System.out.println("Stencil Buffer bits: " + stencilBits[0]);
		((PGraphicsOpenGL)pa.g).endGL();
	}
	
	//////////////////////////////////////////////////////////
	//  OPENGL STUFF										//
	//////////////////////////////////////////////////////////
	
	/**
	 * Checks whether the given extension is supported by the current opengl context.
	 * <p><b>NOTE</b>: the openGL context has to be valid at the time of calling this method.
	 * 
	 * @param pa the pa
	 * @param extensionName the extension name
	 * 
	 * @return true, if checks if is gl extension supported
	 */
	public static boolean isGLExtensionSupported(PApplet pa, String extensionName){
		if (!MT4jSettings.getInstance().isOpenGlMode())
			return false;
		
		GL gl =((PGraphicsOpenGL)pa.g).gl;
		boolean avail = gl.isExtensionAvailable(extensionName);
		/*
		String ext = gl.glGetString(GL.GL_EXTENSIONS);
		*/
		return(avail);
	}
	
	/**
	 * Checks whether non power of two texture dimensions are natively supported
	 * by the gfx hardware.
	 * 
	 * @param pa the pa
	 * 
	 * @return true, if supports non power of two texture
	 */
	public static boolean supportsNonPowerOfTwoTexture(PApplet pa){
		boolean supports = false;
		if (	Tools3D.isGLExtensionSupported(pa, "GL_TEXTURE_RECTANGLE_ARB")
			|| 	Tools3D.isGLExtensionSupported(pa, "GL_ARB_texture_non_power_of_two")
			|| 	Tools3D.isGLExtensionSupported(pa, "GL_ARB_texture_rectangle")
			|| 	Tools3D.isGLExtensionSupported(pa, "GL_NV_texture_rectangle")
			|| 	Tools3D.isGLExtensionSupported(pa, "GL_TEXTURE_RECTANGLE_EXT")
			|| 	Tools3D.isGLExtensionSupported(pa, "GL_EXT_texture_rectangle")
		){
			supports = true;
		}
			return supports;
	}
	
	/**
	 * Gen stencil display list gradient.
	 * <p><b>NOTE</b>: the openGL context has to be valid at the time of calling this method.
	 * 
	 * @param pa the pa
	 * @param vertBuff the vert buff
	 * @param tbuff the tbuff
	 * @param colorBuff the color buff
	 * @param strokeColBuff the stroke col buff
	 * @param indexBuff the index buff
	 * @param drawSmooth the draw smooth
	 * @param strokeWeight the stroke weight
	 * @param vertexArr the vertex arr
	 * @param outLines the out lines
	 * @param x1R the x1 r
	 * @param x1G the x1 g
	 * @param x1B the x1 b
	 * @param x1A the x1 a
	 * @param x2R the x2 r
	 * @param x2G the x2 g
	 * @param x2B the x2 b
	 * @param x2A the x2 a
	 * @param x3R the x3 r
	 * @param x3G the x3 g
	 * @param x3B the x3 b
	 * @param x3A the x3 a
	 * @param x4R the x4 r
	 * @param x4G the x4 g
	 * @param x4B the x4 b
	 * @param x4A the x4 a
	 * @param useGradient the use gradient
	 * 
	 * @return the int[]
	 */
	public static int[] genStencilDisplayListGradient(PApplet pa, FloatBuffer vertBuff, FloatBuffer tbuff, 
			FloatBuffer colorBuff, FloatBuffer strokeColBuff, IntBuffer indexBuff, 
			boolean drawSmooth, float strokeWeight, Vertex[] vertexArr, List<Vertex[]> outLines,
			float x1R, float x1G, float x1B, float x1A, float x2R, float x2G, float x2B, float x2A,
			float x3R, float x3G, float x3B, float x3A, float x4R, float x4G, float x4B, float x4A,
			boolean useGradient
		)
	{
		GL gl=((PGraphicsOpenGL)pa.g).beginGL();
		
		//Unbind any VBOs first
		gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
		gl.glBindBufferARB(GL.GL_ELEMENT_ARRAY_BUFFER_ARB, 0);
		
		//Generate new list IDs
		int[] returnVal = new int[2];
		int listIDFill = gl.glGenLists(1);
		if (listIDFill == 0){
			System.err.println("Failed to create display list");
			returnVal[0] = -1;
			returnVal[1] = -1;
			return returnVal;
		}
		int listIDOutline = gl.glGenLists(1);
		if (listIDOutline == 0){
			System.err.println("Failed to create display list");
			returnVal[0] = -1;
			returnVal[1] = -1;
			return returnVal;
		}
		
	    float[] minMax = Tools3D.getMinXYMaxXY(vertexArr);
	    float minX = minMax[0]-10;
	    float minY = minMax[1]-10;
	    float maxX = minMax[2]+10;
	    float maxY = minMax[3]+10;
	    
	    gl.glColor4d (0.0, 0.0, 0.0, 1.0);
	    
	    gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL.GL_COLOR_ARRAY);
		
		gl.glVertexPointer(3, GL.GL_FLOAT, 0, vertBuff);
		gl.glColorPointer(4, GL.GL_FLOAT, 0, colorBuff);
		
		//Using the strokecolor buffer strokecolor AND fill!
		
		//Generate List
		gl.glNewList(listIDFill, GL.GL_COMPILE);
			/////////////////////////////////////
			// Clear stencil and disable color //
		    // Draw with STENCIL			   //
		    /////////////////////////////////////
//			/*
			gl.glClearStencil(0);
			gl.glColorMask(false,false,false,false);
			gl.glDisable(GL.GL_BLEND);
			
			gl.glDepthMask(false);//remove..?
			
			//FIXME do this for non-zero rule?
//			gl.glColorMask(true,true,true,true);
//			gl.glEnable (GL.GL_BLEND);
//			gl.glDepthMask(true);//remove..?
			
			//Enable stencilbuffer
			gl.glEnable(GL.GL_STENCIL_TEST);
//		    gl.glStencilMask (0x01);
		    gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_INVERT);
		    gl.glStencilFunc (GL.GL_ALWAYS, 0, ~0);
		    
		    //Stecilfunc bestimmt ob in den stencil geschrieben wird oder nicht
		    //1.param: die vergleichsart der werte, 
		    //2.param: reference value, wird bei op reingeschrieben bei replace(?)
		    //3.prama: mask
		    //ref is & anded with mask and the result with the value in the stencil buffer
		    //mask is & with ref, mask is & stencil => vergleich
//		    gl.glStencilFunc(GL.GL_ALWAYS, 0x1, 0x1);
//		    gl.glStencilOp(GL.GL_KEEP, GL.GL_INVERT, GL.GL_INVERT);
		    
		    //TODO notice, "stencilOP" zum wert in stencilbuffer reinschreiben
		    //"stencilfunc" vergleicht framebuffer mit stencilbuffer und macht stencilOP wenn bedingung stimmt
		    
		    gl.glColor4d (colorBuff.get(0), colorBuff.get(1), colorBuff.get(2), colorBuff.get(3));
		    
			//DRAW //FIXME why does this not work?
			if (indexBuff == null){
				gl.glDrawArrays(GL.GL_TRIANGLE_FAN, 0, vertBuff.capacity()/3);
			}else{
				gl.glDrawElements(GL.GL_TRIANGLE_FAN, indexBuff.capacity(), GL.GL_UNSIGNED_INT, indexBuff);
			}
			
//		    gl.glBegin (GL.GL_TRIANGLE_FAN);
//		    for (int i = 0; i < vertexArr.length; i++) {
//				Vertex vertex = vertexArr[i];
//				gl.glVertex3f (vertex.getX(), vertex.getY(),  vertex.getZ());
//			}
//	    	gl.glEnd();
		    
//		    gl.glDrawArrays(GL.GL_TRIANGLE_FAN, 0, vertBuff.capacity()/3); 
//			*/
			//////////////////////////////////////
			gl.glDepthMask(true);
			
		    gl.glEnable (GL.GL_BLEND);
		    gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			//////////////////////
			// Draw fill		//
		    ////////////////////// 
//		    /*
			gl.glColorMask(true, true, true, true);
			gl.glEnable (GL.GL_BLEND);
			
		    gl.glStencilOp (GL.GL_ZERO, GL.GL_ZERO, GL.GL_ZERO); //org
		    gl.glStencilFunc(GL.GL_EQUAL, 0x01, 0x01);
			
//		    gl.glStencilOp (GL.GL_KEEP, GL.GL_REPLACE, GL.GL_ZERO);
//		    gl.glStencilFunc(GL.GL_EQUAL, 0x01, 0x01);
		    
		    if (useGradient){
			    gl.glBegin (GL.GL_QUADS);
				    gl.glColor4f(x1R, x1G, x1B, x1A);
				    gl.glVertex3d (minX, minY, 0.0);
				    gl.glColor4f(x2R, x2G, x2B, x2A);
				    gl.glVertex3d (maxX, minY, 0.0); 
				    gl.glColor4f(x3R, x3G, x3B, x3A);
				    gl.glVertex3d (maxX, maxY, 0.0); 
				    gl.glColor4f(x4R, x4G, x4B, x4A);
				    gl.glVertex3d (minX, maxY, 0.0); 
			    gl.glEnd ();
		    }else{
			    gl.glBegin (GL.GL_QUADS);
				    gl.glVertex3d (minX, minY, 0.0); 
				    gl.glVertex3d (maxX, minY, 0.0); 
				    gl.glVertex3d (maxX, maxY, 0.0); 
				    gl.glVertex3d (minX, maxY, 0.0); 
			    gl.glEnd ();
		    }
//		    */
		    ////////////////////////////////////
//		    gl.glDepthMask(true); //Disabled to avoid too many state switches, 
		    gl.glDisable (GL.GL_STENCIL_TEST);	 //Disabled to avoid too many state switches
		gl.glEndList();
		returnVal[0] = listIDFill;
		    
		//////////////////////////////
		// Draw aliased outline		//
		//////////////////////////////
		gl.glColorPointer(4, GL.GL_FLOAT, 0, strokeColBuff);
		
		gl.glNewList(listIDOutline, GL.GL_COMPILE);
//		  	gl.glEnable(GL.GL_STENCIL_TEST); 
		  	
		  	
//			gl.glColorMask(true, true, true, true);
//			gl.glDepthMask(false); //FIXME enable? disable?
		  	
//		    // Draw aliased off-pixels to real
//		    gl.glEnable (GL.GL_BLEND);
//		    gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			
//		    /*
//		    gl.glStencilOp (GL.GL_KEEP, GL.GL_KEEP, GL.GL_KEEP);
//		    gl.glStencilFunc (GL.GL_EQUAL, 0x00, 0x01); //THIS IS THE ORIGINAL!
		   
//		  	gl.glStencilOp (GL.GL_KEEP, GL.GL_KEEP, GL.GL_KEEP);
//		    gl.glStencilFunc (GL.GL_EQUAL, 0x00, ~1); 
		    
//		    gl.glEnable(GL.GL_LINE_SMOOTH);
	    	//FIXME TEST
			Tools3D.setLineSmoothEnabled(gl, true);
			
		    gl.glLineWidth(strokeWeight);
		    
		    //DRAW 
//			gl.glDrawElements(GL.GL_LINE_STRIP, indexBuff.capacity(), GL.GL_UNSIGNED_INT, indexBuff);
//			gl.glDrawArrays(GL.GL_LINE_STRIP, 0, vertexArr.length);
		    
		    /////TEST/// //TODO make vertex pointer arrays?
		    gl.glColor4d (strokeColBuff.get(0), strokeColBuff.get(1), strokeColBuff.get(2), strokeColBuff.get(3));
		    for (Vertex[] outline : outLines){
				 gl.glBegin (GL.GL_LINE_STRIP);
				 	for (Vertex vertex : outline){
				 		gl.glVertex3f (vertex.getX(), vertex.getY(), vertex.getZ());
				 	}
			    gl.glEnd();
			}
			////
//			gl.glDisable (GL.GL_LINE_SMOOTH);
	    	//FIXME TEST
			Tools3D.setLineSmoothEnabled(gl, false);
			//////////////////////////////////
//		*/
//			gl.glDisable (GL.GL_STENCIL_TEST);	
			
//		    gl.glDepthMask(true);
		gl.glEndList();
		
		returnVal[1] = listIDOutline;
		
		//Disable client states
		gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL.GL_COLOR_ARRAY);
		
		((PGraphicsOpenGL)pa.g).endGL();
		////////////////
		
		return returnVal;
	}
	
	/**
	 * Generate stencil display list.
	 * 
	 * @param pa the pa
	 * @param vertBuff the vert buff
	 * @param tbuff the tbuff
	 * @param colorBuff the color buff
	 * @param strokeColBuff the stroke col buff
	 * @param indexBuff the index buff
	 * @param drawSmooth the draw smooth
	 * @param strokeWeight the stroke weight
	 * @param vertexArr the vertex arr
	 * @param outLines the out lines
	 * 
	 * @return the int[]
	 */
	public static int[] generateStencilDisplayList(PApplet pa, FloatBuffer vertBuff, FloatBuffer tbuff, 
								FloatBuffer colorBuff, FloatBuffer strokeColBuff, IntBuffer indexBuff, 
								boolean drawSmooth, float strokeWeight, Vertex[] vertexArr, List<Vertex[]> outLines)
	{
		return Tools3D.genStencilDisplayListGradient(pa, vertBuff, tbuff, colorBuff, strokeColBuff, indexBuff, drawSmooth, strokeWeight, vertexArr, outLines, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,  1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,  1.0f, 1.0f, 1.0f, 1.0f, false);
	}
	
	//////////////////////////////////////////////////////
	// Generate Display Lists and get their IDs			//
	//////////////////////////////////////////////////////
	/**
	 * Creates 2 displaylists for drawing static geometry very fast.
	 * Returns the IDs (names) of the display lists generated with the given info.
	 * 
	 * @param pa the pa
	 * @param geometryInfo the geometry info
	 * @param useTexture the use texture
	 * @param texture the texture
	 * @param styleInfo the style info
	 * 
	 * @return the int[]
	 * 
	 * Returns the IDs (names) of the display lists generated with the given info.
	 */
	public static int[] generateDisplayLists(PApplet pa, GeometryInfo geometryInfo, boolean useTexture, PImage texture, StyleInfo styleInfo){
		return generateDisplayLists(pa, styleInfo.getFillDrawMode(), geometryInfo, useTexture, texture, styleInfo.isDrawSmooth(), styleInfo.getStrokeWeight());
	}
	
	
	/**
	 * Returns the IDs (names) of the display lists generated with the given info.
	 * 
	 * @param pa the pa
	 * @param fillDrawMode the fill draw mode
	 * @param geometryInfo the geometry info
	 * @param useTexture the use texture
	 * @param texture the texture
	 * @param drawSmooth the draw smooth
	 * @param strokeWeight the stroke weight
	 * 
	 * @return int[2] array where [0] is the list of the fill
	 * and [1] the list of the outline drawing list
	 */
	public static int[] generateDisplayLists(PApplet pa, int fillDrawMode, GeometryInfo geometryInfo,
									boolean useTexture, PImage texture, boolean drawSmooth, float strokeWeight
	){
		FloatBuffer tbuff 			= geometryInfo.getTexBuff();
		FloatBuffer vertBuff 		= geometryInfo.getVertBuff();
		FloatBuffer colorBuff 		= geometryInfo.getColorBuff();
		FloatBuffer strokeColBuff 	= geometryInfo.getStrokeColBuff();
		IntBuffer indexBuff 		= geometryInfo.getIndexBuff(); //null if not indexed
		
		GL gl;
//		gl =((PGraphicsOpenGL)pa.g).beginGL();
		gl =((PGraphicsOpenGL)pa.g).gl;
		//Unbind any VBOs first
		gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
		gl.glBindBufferARB(GL.GL_ELEMENT_ARRAY_BUFFER_ARB, 0);
		
		//Generate new list IDs
		int[] returnVal = new int[2];
		int listIDFill = gl.glGenLists(1);
		if (listIDFill == 0){
			System.err.println("Failed to create fill display list");
			returnVal[0] = -1;
			returnVal[1] = -1;
			return returnVal;
		}
		int listIDOutline = gl.glGenLists(1);
		if (listIDOutline == 0){
			System.err.println("Failed to create stroke display list");
			returnVal[0] = -1;
			returnVal[1] = -1;
			return returnVal;
		}
		
		gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL.GL_COLOR_ARRAY);
		
		gl.glVertexPointer(3, GL.GL_FLOAT, 0, vertBuff);
		gl.glColorPointer(4, GL.GL_FLOAT, 0, colorBuff);
		
//		gl.glDisable(GL.GL_POLYGON_SMOOTH);
		
		//Default target
		int textureTarget = GL.GL_TEXTURE_2D;
		
		/////// DO FILL LIST/////////////////////////////////
		
		/////////
		boolean textureDrawn = false;
		int usedTextureID = -1;
		if (useTexture
			&& texture != null 
			&& texture instanceof GLTexture) //Bad for performance?
		{
			GLTexture tex = (GLTexture)texture;
			textureTarget = tex.getTextureTarget();
			
			//tells opengl which texture to reference in following calls from now on!
			//the first parameter is eigher GL.GL_TEXTURE_2D or ..1D
			gl.glEnable(textureTarget);
			usedTextureID = tex.getTextureID();
			gl.glBindTexture(textureTarget, tex.getTextureID());
			
			gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
			
			if (false){//Texture //TODO kann man vbos aufrufen in displaylist?
				gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0); //FIXME get vbo id
				gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, 0);
			}else{
				gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, tbuff);
			}
			textureDrawn = true;
		}
		
		// Normals
		if (geometryInfo.isContainsNormals()){
			gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
			if (false){
				gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
				gl.glNormalPointer(GL.GL_FLOAT, 0, 0); 
			}else{
				gl.glNormalPointer(GL.GL_FLOAT, 0, geometryInfo.getNormalsBuff());
			}
		}
		
		if (false){//Color
			gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
			gl.glColorPointer(4, GL.GL_FLOAT, 0, 0);
		}else{
			gl.glColorPointer(4, GL.GL_FLOAT, 0, colorBuff);
		}
		
		// START recording display list and DRAW////////////////////
		gl.glNewList(listIDFill, GL.GL_COMPILE);
			if (textureDrawn){
				gl.glEnable(textureTarget); //muss texture in der liste gebinded werden? anscheinend JA!
				gl.glBindTexture(textureTarget, usedTextureID);
			}
			
			//DRAW with drawElements if geometry is indexed, else draw with drawArrays!
			if (geometryInfo.isIndexed()){
				gl.glDrawElements(fillDrawMode, indexBuff.capacity(), GL.GL_UNSIGNED_INT, indexBuff); //limit() oder capacity()??
			}else{
				gl.glDrawArrays(fillDrawMode, 0, vertBuff.capacity()/3);
			}
			
			if (textureDrawn){
				gl.glDisable(textureTarget); 
				gl.glBindTexture(textureTarget, 0);
			}
		gl.glEndList();
		//// STOP recording display list and DRAW////////////////////
		
		if (geometryInfo.isContainsNormals()){
			gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
		}

		if (textureDrawn){
			gl.glBindTexture(textureTarget, 0);//Unbind texture
			gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
			gl.glDisable(textureTarget); //weiter nach unten?
		}
		returnVal[0] = listIDFill;
		
		//////
		/*
		if (useTexture
			&& texture != null 
			&& texture instanceof GLTexture)
		{
			GLTexture tex = (GLTexture)texture;
			textureTarget = tex.getTextureTarget();
			
			//tells opengl which texture to reference in following calls from now on!
			//the first parameter is eigher GL.GL_TEXTURE_2D or ..1D
			gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
			gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, tbuff);
			
			//Start recording display list
			gl.glNewList(listIDFill, GL.GL_COMPILE);
				gl.glEnable(textureTarget);
				gl.glBindTexture(tex.getTextureTarget(), tex.getTextureID());
//				gl.glDisable(GL.GL_POLYGON_SMOOTH);
				//DRAW
				if (indexBuff == null){
					gl.glDrawArrays(fillDrawMode, 0, vertBuff.capacity()/3);
				}else{
					gl.glDrawElements(fillDrawMode, indexBuff.capacity(), GL.GL_UNSIGNED_INT, indexBuff);
				}
				
				gl.glBindTexture(tex.getTextureTarget(), 0);
				gl.glDisable(textureTarget); //weiter nach unten?
			gl.glEndList();

			gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
			gl.glBindTexture(textureTarget,0);//Unbind texture
			gl.glDisable(textureTarget); //weiter nach unten?
		}else{
			gl.glNewList(listIDFill, GL.GL_COMPILE);
				//DRAW
				if (indexBuff == null){
					gl.glDrawArrays(fillDrawMode, 0, vertBuff.capacity()/3);
				}else{
					gl.glDrawElements(fillDrawMode, indexBuff.capacity(), GL.GL_UNSIGNED_INT, indexBuff);
				}
//				gl.glDrawArrays(GL.GL_POLYGON, 0, vertBuff.capacity()/3);
			gl.glEndList();
		}
		
		returnVal[0] = listIDFill;
		*/
		//////////////////////////////////////////////////
		
		/////// DO OUTLINE LIST////////////////////////////
		gl.glColorPointer(4, GL.GL_FLOAT, 0, strokeColBuff);
		//Start recording display list
		gl.glNewList(listIDOutline, GL.GL_COMPILE);
		
//			if (drawSmooth)
//				gl.glEnable(GL.GL_LINE_SMOOTH);
			//FIXME TEST
			Tools3D.setLineSmoothEnabled(gl, true);
			
			if (strokeWeight > 0)
				gl.glLineWidth(strokeWeight);
			
			//DRAW
			if (geometryInfo.isIndexed()){
				gl.glDrawElements(GL.GL_LINE_STRIP, indexBuff.capacity(), GL.GL_UNSIGNED_INT, indexBuff); ////indices.limit()?
			}else{
				gl.glDrawArrays(GL.GL_LINE_STRIP, 0, vertBuff.capacity()/3);
			}
			
//			if (drawSmooth)
//				gl.glDisable(GL.GL_LINE_SMOOTH);
			//FIXME TEST
			Tools3D.setLineSmoothEnabled(gl, false);
			
		gl.glEndList();
		returnVal[1] = listIDOutline;
		////////////////////////////////////////////////////
		
		//Disable client states
		gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL.GL_COLOR_ARRAY);

//		((PGraphicsOpenGL)pa.g).endGL();
		return returnVal;
	}
	
	
	//TODO make only 1 function gendisplaylist mit boolean generate outline/fill
	/**
	 * Returns the ID (name) of the display list
	 * If you dont want to use a line stipple pattern, use '0' for the parameter.
	 * 
	 * @param pa the pa
	 * @param vertBuff the vert buff
	 * @param strokeColBuff the stroke col buff
	 * @param indexBuff the index buff
	 * @param drawSmooth the draw smooth
	 * @param strokeWeight the stroke weight
	 * @param lineStipple the line stipple
	 * 
	 * @return int id of outline drawing list
	 */
	public static int generateOutLineDisplayList(PApplet pa, FloatBuffer vertBuff, FloatBuffer strokeColBuff, IntBuffer indexBuff, 
												boolean drawSmooth, float strokeWeight, short lineStipple){
		GL gl;
		gl =((PGraphicsOpenGL)pa.g).beginGL();
//		 WGL.ChoosePixelFormat(arg0, arg1) //TODO check into
		
		//Unbind any VBOs first
		gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
		gl.glBindBufferARB(GL.GL_ELEMENT_ARRAY_BUFFER_ARB, 0);
		//Generate new list IDs
		int returnVal = -1;
		int listIDOutline = gl.glGenLists(1);
		if (listIDOutline == 0){
			System.err.println("Failed to create display list");
			return returnVal;
		}
		gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL.GL_COLOR_ARRAY);
		gl.glVertexPointer(3, GL.GL_FLOAT, 0, vertBuff);
		gl.glColorPointer(4, GL.GL_FLOAT, 0, strokeColBuff);
		
		//Start recording display list
		gl.glNewList(listIDOutline, GL.GL_COMPILE);
//			if (drawSmooth)
//				gl.glEnable(GL.GL_LINE_SMOOTH);
			//FIXME TEST for multisample
			Tools3D.setLineSmoothEnabled(gl, true);
		
			if (strokeWeight > 0)
				gl.glLineWidth(strokeWeight);
			if (lineStipple != 0){
				gl.glLineStipple(1, lineStipple);
				gl.glEnable(GL.GL_LINE_STIPPLE);
			}
			
			if (indexBuff == null){
				gl.glDrawArrays(GL.GL_LINE_STRIP, 0, vertBuff.capacity()/3);
			}else{
				gl.glDrawElements(GL.GL_LINE_STRIP, indexBuff.capacity(), GL.GL_UNSIGNED_INT, indexBuff); ////indices.limit()?
			}
			
			//RESET LINE STIPPLE
			if (lineStipple != 0)
				gl.glDisable(GL.GL_LINE_STIPPLE); 
			
//			if (drawSmooth)
//				gl.glDisable(GL.GL_LINE_SMOOTH);
			//FIXME TEST for multisample
			Tools3D.setLineSmoothEnabled(gl, false);
			
		gl.glEndList();
		returnVal = listIDOutline;
		
		//Disable client states
		gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL.GL_COLOR_ARRAY);
		((PGraphicsOpenGL)pa.g).endGL();
		return returnVal;
	}
	
	/**
	 * Draws a filled 2d bezier shape in immediate mode with help of
	 * the stencil buffer to allow concave geometry.
	 * Beziervertices are allowerd in the vertex array.
	 * 
	 * @param pa the pa
	 * @param vertexArr the vertex arr
	 */
	public static void drawFilledBezierShape(PApplet pa, Vertex[] vertexArr){
		GL gl=((PGraphicsOpenGL)pa.g).beginGL();
		float[] minMax = Tools3D.getMinXYMaxXY(vertexArr);
//		/*
		 // Draw to stencil
		    gl.glDisable (GL.GL_BLEND);
		    gl.glEnable (GL.GL_STENCIL_TEST);
//		    gl.glStencilMask (0x01);
		    gl.glStencilOp (GL.GL_KEEP, GL.GL_KEEP, GL.GL_INVERT);
		    gl.glStencilFunc (GL.GL_ALWAYS, 0, ~0);
		    gl.glColorMask (false, false, false, false);
//		*/
		    //Change beziervertices to normal vertices - THIS IS EXPENSIVE!
		    Vertex[] allVertsBezierResolved = Tools3D.createVertexArrFromBezierArr(vertexArr, 15);
		           
		   //DRAW RAW FILL 
		   gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		   //gl.glBegin (GL.GL_LINE_STRIP);
		     gl.glBegin (GL.GL_TRIANGLE_FAN);
			     for (int i = 0; i < allVertsBezierResolved.length; i++) {
					Vertex vertex = allVertsBezierResolved[i];
					gl.glVertex3f(vertex.x, vertex.y, vertex.z);
				}
			 gl.glEnd ();

			 //Draw aliased off-pixels to real
		    gl.glColorMask (true, true, true, true);
		    gl.glEnable (GL.GL_BLEND);
		    gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

//			/*		    
		    gl.glStencilFunc (GL.GL_EQUAL, 0x00, 0x01);
		    gl.glStencilOp (GL.GL_KEEP, GL.GL_KEEP, GL.GL_KEEP);
		    
		    //DRAW OUTLINE
		   gl.glEnable(GL.GL_LINE_SMOOTH);
		   gl.glLineWidth(1.0f);
		   gl.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
		    gl.glBegin (GL.GL_LINES);
		     for (int i = 0; i < allVertsBezierResolved.length; i++) {
					Vertex vertex = allVertsBezierResolved[i];
					gl.glVertex3f(vertex.x, vertex.y, vertex.z);
		     }
		    gl.glEnd ();
		    gl.glDisable (GL.GL_LINE_SMOOTH);
//		*/
//		/*
		    // Draw FILL
		    gl.glStencilFunc (GL.GL_EQUAL, 0x01, 0x01);
		    gl.glStencilOp (GL.GL_ZERO, GL.GL_ZERO, GL.GL_ZERO);
		   
		    gl.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
		    gl.glBegin (GL.GL_QUADS);
		      gl.glVertex3f (minMax[0], minMax[1], 0.0f); 
		      gl.glVertex3f (minMax[2], minMax[1], 0.0f); 
		      gl.glVertex3f (minMax[2], minMax[3], 0.0f); 
		      gl.glVertex3f (minMax[0],  minMax[3], 0.0f); 
		    gl.glEnd ();

		    gl.glDisable(GL.GL_STENCIL_TEST);
//		*/
		    ((PGraphicsOpenGL)pa.g).endGL();
	}
	
	
	/**
	 * Changes the BezierVertex' in the Vertex array into regular vertices,
	 * and approximates the bezier curve this way.
	 * 
	 * @param vertexArr the vertex arr
	 * @param resolution the resolution
	 * 
	 * @return the vertex[]
	 */
	public static Vertex[] createVertexArrFromBezierArr(Vertex[] vertexArr, int resolution){
		ArrayList<Vertex> allVerticesWithCurves = new ArrayList<Vertex>();
		//Convert BezierVertices to regular Vertices
		float x0 = 100000;
		float x1 = -100000;
		float y0 = 100000;
		float y1 = -100000;
		//RESOTULTION FACTOR! more = better quality, less performance
		int segments = resolution; 
		
		//Replace the beziervertices with many calculated regular vertices
		    for (int i = 0; i < vertexArr.length; i++) {
				Vertex vertex = vertexArr[i];
				if (vertex instanceof BezierVertex){
					BezierVertex b = (BezierVertex)vertex;
					Vertex[] curve = Tools3D.getCubicBezierVertices(
							vertexArr[i-1].getX(), 
							vertexArr[i-1].getY(),
							b.getFirstCtrlPoint().getX(), 
							b.getFirstCtrlPoint().getY(), 
							b.getSecondCtrlPoint().getX(),
							b.getSecondCtrlPoint().getY(),
							b.getX(),
							b.getY(), 
							segments,
							x0,
							x1,
							y0,
							y1
					);	
					//Add all the curve vertices
					for (int j = 0; j < curve.length; j++) {
						Vertex curveVertex = curve[j];
						allVerticesWithCurves.add(new Vertex(curveVertex.getX(), curveVertex.getY(), 0, vertex.getR(),vertex.getG(),vertex.getB(),vertex.getA()));
					}
				}else{
					//Add the normal vertices
					allVerticesWithCurves.add(new Vertex(vertex.getX(), vertex.getY(), 0, vertex.getR(),vertex.getG(),vertex.getB(),vertex.getA()));
				}//else
			}//For
		return (Vertex[] )allVerticesWithCurves.toArray(new Vertex[allVerticesWithCurves.size()]);
	}
	
	/**
	 * Changes the BezierVertex' in the Vertex array lists into regular vertices,
	 * and approximates the bezier curve this way.
	 * 
	 * @param vertexArrays the vertex arrays
	 * @param resolution the resolution
	 * 
	 * @return the list< vertex[]>
	 */
	public static List<Vertex[]> createVertexArrFromBezierVertexArrays(List<Vertex[]> vertexArrays, int resolution){
		ArrayList<Vertex[]> partialPathsListCurves = new ArrayList<Vertex[]>() ;
		for (int i = 0; i < vertexArrays.size(); i++) {
			Vertex[] partArray = vertexArrays.get(i);
			partArray = Tools3D.createVertexArrFromBezierArr(partArray, resolution);
			partialPathsListCurves.add(partArray);
		}
		return partialPathsListCurves;
	}
	
	
	
	/**
	 * Calculates the vertices of a bezier curve defined by the startpoint p,
	 * the controlpoints b,b2 and the end point p2.
	 * The segments parameter defines the resolution of the curve
	 * 
	 * @param px0 the px0
	 * @param py0 the py0
	 * @param bx1 the bx1
	 * @param by1 the by1
	 * @param b2x2 the b2x2
	 * @param b2y2 the b2y2
	 * @param p2x3 the p2x3
	 * @param p2y3 the p2y3
	 * @param segments the segments
	 * @param xx0 the xx0
	 * @param yy0 the yy0
	 * @param xx1 the xx1
	 * @param yy1 the yy1
	 * 
	 * @return the cubic bezier vertices
	 */
	public static Vertex[] getCubicBezierVertices (
			float px0, 
			float py0, 
			float bx1, 
			float by1,
			float b2x2, 
			float b2y2, 
			float p2x3,
			float p2y3, 
			int segments, 
			float xx0, 
			float yy0,
			float xx1, 
			float yy1
	)
	{
//		Vertex[] returnArray = new Vertex[segments*2]; //org
		Vertex[] returnArray = new Vertex[segments];
		
		float lvl = 0.0f;
		float x;
		float y;

		/*
		x = (float)((px0 * (1.0 - lvl) * (1.0 - lvl) * (1.0 - lvl)) +
				(bx1 * 3.0 * lvl * (1.0 - lvl) * (1.0 - lvl)) +
				(b2x2 * 3.0 * lvl * lvl * (1.0 - lvl)) +
				(p2x3 * lvl * lvl * lvl));

		y = (float)((py0 * (1.0 - lvl) * (1.0 - lvl) * (1.0 - lvl)) +
				(by1 * 3.0 * lvl * (1.0 - lvl) * (1.0 - lvl)) +
				(b2y2 * 3.0 * lvl * lvl * (1.0 - lvl)) +
				(p2y3 * lvl * lvl * lvl));

		 */
		for (int i = 0; i < segments; i++) {
			/*
			xx0 = Math.min(xx0, x);
			xx1 = Math.max(xx1, x);
			yy0 = Math.min(yy0, y);
			yy1 = Math.max(yy1, y);
			*/

//			returnArray[i*2] = new Vertex(x,y,0); //org

			lvl = ((i + 1) / (float) segments);

			x = (float)((px0 * (1.0 - lvl) * (1.0 - lvl) * (1.0 - lvl)) +
					(bx1 * 3.0 * lvl * (1.0 - lvl) * (1.0 - lvl)) +
					(b2x2 * 3.0 * lvl * lvl * (1.0 - lvl)) +
					(p2x3 * lvl * lvl * lvl));

			y = (float)((py0 * (1.0 - lvl) * (1.0 - lvl) * (1.0 - lvl)) +
					(by1 * 3.0 * lvl * (1.0 - lvl) * (1.0 - lvl)) +
					(b2y2 * 3.0 * lvl * lvl * (1.0 - lvl)) +
					(p2y3 * lvl * lvl * lvl));

			/*
			xx0 = Math.min(xx0, x);
			xx1 = Math.max(xx1, x);
			yy0 = Math.min(yy0, y);
			yy1 = Math.max(yy1, y);
			*/
			
			returnArray[i] = new Vertex(x,y,0);

//			returnArray[i*2+1] = new Vertex(x,y,0); //org
			//System.out.println(x + " " + y);
		}
		return returnArray;
	}
	 
	
	/**
	 * Converts the values of a quadric bezier curve into a cubic.
	 * <br>Returns a BezierVertex with the values of the cubic curve equivalent to
	 * the quadric curve. Creates 2 controlpoints out of one.
	 * 
	 * @param bezierStart the bezier start
	 * @param firstQuadControlP the first quad control p
	 * @param quadEndPoint the quad end point
	 * 
	 * @return the cubic from quadratic curve
	 */
	public static BezierVertex getCubicFromQuadraticCurve(Vertex bezierStart, Vertex firstQuadControlP, Vertex quadEndPoint){
		Vertex bezStartCopy = (Vertex)bezierStart.getCopy();
		Vertex firstQuadControlPCopy = (Vertex)firstQuadControlP.getCopy();
		Vertex quadEndPCopy = (Vertex)quadEndPoint.getCopy();
		
		Vertex tmp1 = (Vertex)firstQuadControlPCopy.getSubtracted(bezStartCopy);
		tmp1.scaleLocal(2/3);
		Vertex cp1 = (Vertex)bezStartCopy.getAdded(tmp1);
		
		Vertex tmp2 = (Vertex)quadEndPCopy.getSubtracted(firstQuadControlP);
		tmp2.scaleLocal(1/3);
		Vertex cp2 = (Vertex)firstQuadControlP.getAdded(tmp1);
		return new BezierVertex(cp1.getX(), cp1.getY(),0, cp2.getX(), cp2.getY(),0, quadEndPCopy.getX(), quadEndPCopy.getY(), 0);
	}
	
	/**
	 * Calculates the vertices of a quadric bezier curve defined by the
	 * startpoint curveStartP, the controlpoint curveControlP and the end point curveEndP.<br>
	 * The segments parameter defines the resolution of the curve.
	 * <br>Note: This method uses only the X and Y Coordinates and generates a 2D curve!
	 * 
	 * @param curveStartP the curve start p
	 * @param curveControlP the curve control p
	 * @param curveEndP the curve end p
	 * @param segmentDetail the segment detail
	 * 
	 * @return the quad bezier vertices
	 */
	public static Vertex[] getQuadBezierVertices(Vertex curveStartP, Vertex curveControlP, Vertex curveEndP, int segmentDetail){
		//Change detail here
		double segments = (double)segmentDetail;
		double count = 0;  //used as our counter
		double detailBias; //how many points should we put on our curve.

		float x,y; //used as accumulators to make our code easier to read

		Vertex[] vertices = new Vertex[(int)segments];
		
		detailBias = 1.0 / segments; //we'll put 51 points on out curve (0.02 detail bias)
		
		int loopCount = 0;
		do
		{
			double b1 =  count*count;
			double b2 =	(2*count * (1-count)); 
			double b3 = ((1-count) * (1-count));
			
			x = (float)(curveStartP.getX()*b1 + curveControlP.getX()*b2 + curveEndP.getX()*b3);
			y = (float)(curveStartP.getY()*b1 + curveControlP.getY()*b2 + curveEndP.getY()*b3);

			vertices[loopCount] = new Vertex(x,y,0);
			
			count += detailBias;
			loopCount++;
		}while( count <= 1);
		return vertices;
	}
	
	/**
	 * Checks whether the supplied vertex array contains BezierVertex instances.
	 * 
	 * @param originalPointsArray the original points array
	 * 
	 * @return true, if contains bezier vertices
	 */
	public static boolean containsBezierVertices(Vertex[] originalPointsArray) {
		for (int i = 0; i < originalPointsArray.length; i++) {
			Vertex vertex = originalPointsArray[i];
			if (vertex instanceof BezierVertex){
				return true;
			}
		}
		return false;
	}
	
	

	/**
	 * Returns an arraylist of vertices that form the arc, built from the
	 * supplied parameters.
	 * 
	 * @param fromX1 curve startpointX
	 * @param fromX2 curve startpointY
	 * @param rx the x radius
	 * @param ry the y radius
	 * @param phi the phi
	 * @param large_arc the large_arc
	 * @param sweep the sweep
	 * @param x the x
	 * @param y the y
	 * @param segments the resolution of the curve, more segments -> smoother curve -> less performance
	 * 
	 * @return the list< vertex>
	 */
	public static List<Vertex> arcTo(float fromX1, float fromX2, float rx, float ry, float phi, boolean large_arc, boolean sweep, float x , float y, int segments){
		ArrayList<Vertex> vertexList = new ArrayList<Vertex>();
		int circle_points = segments;
		//current point
		float x1 = fromX1; 
	    float y1 = fromX2; 
	    float x2 = x;
	    float y2 = y;
	    float cp = (float) Math.cos(Math.toRadians(phi));
	    float sp = (float) Math.sin(Math.toRadians(phi));
//	    float cp = (float) Math.cos(phi);
//	    float sp = (float) Math.sin(phi);
	    
	    float dx = .5f * (x1 - x2);
	    float dy = .5f * (y1 - y2);
	    
	    float x_ = cp * dx + sp * dy;
	    float y_ = -sp * dx + cp * dy;
	    
	    float zaehler = (((rx*rx) * (ry*ry)) - ((rx*rx) * (y_*y_)) - ((ry*ry) * (x_*x_)));
	    if (zaehler < 0){
	    	zaehler *=-1;
	    }
//	    float r = (float)Math.sqrt( ( -1*(((rx*rx) * (ry*ry)) - ((rx*rx) * (y_*y_)) - ((ry*ry) * (x_*x_))) ) /
//	                      (((rx*rx) * (y_*y_)) + ((ry*ry) * (x_*x_))));
	    float r = (float)Math.sqrt( (zaehler ) /
                (((rx*rx) * (y_*y_)) + ((ry*ry) * (x_*x_))));
	    
	    /*
	    System.out.println(((rx*rx) * (ry*ry)) - ((rx*rx) * (y_*y_)) - ((ry*ry) * (x_*x_)) );
	    System.out.println(r);   
	    */
	    
	    //FIXME why does this often help? but not in all cases
//	    if (phi>=0){
//	    	large_arc = !large_arc;
//	    }
	    
	    //Orgininal
//	    if (large_arc != sweep){
//	    	r =-r;
//	    }   
	    
	    if (large_arc != sweep){
//	    	r = Math.abs(r);
	    	r =+r;
	    }else if (large_arc == sweep){
	    		r = -r;
	    }
	    
	        
	    float cx_ = (r * rx * y_) / ry;
	    float cy_ = (-r * ry * x_) / rx;
	    float cx = cp * cx_ - sp * cy_ + .5f * (x1 + x2);
	    float cy = sp * cx_ + cp * cy_ + .5f * (y1 + y2);
	    
	    float psi = angle(new float[]{1,0}, new float[]{(x_ - cx_)/rx, (y_ - cy_)/ry});
	   
	    float delta = angle( new float[]{(x_ - cx_)/rx , (y_ - cy_)/ry} , new float[]{(-x_ - cx_)/rx, (-y_ - cy_)/ry});
	                      
	    if (sweep && delta < 0){
			   delta += Math.PI * 2;
		}
	    if (!sweep && delta > 0){
	    	delta -= Math.PI * 2;
	    }
	    
//	    float n_points = max( int( abs(circle_points * delta / (2 * Math.PI))), 1);
	    float n_points = Math.max((int)( Math.abs(circle_points * delta / (2 * Math.PI))), 1);
	    
	    //Add curve startpoint
//	    vertexList.add(new Vertex(x1,y2,0));
	    //Add the rest
	    for (int i = 0; i < n_points+1; i++) {
	    	float theta = psi + i * delta / n_points;
            float ct = (float) Math.cos(theta);
            float st = (float) Math.sin(theta);
            
            float newX = cp * rx * ct - sp * ry * st + cx;
            float newY = sp * rx * ct + cp * ry * st + cy;
            float newZ = 0;
            
            /*
            //This prevents adding the same vertex as startpoint (fromx, fromY)
            if ((!vertexList.isEmpty() && (vertexList.get(vertexList.size()-1).x == newX && vertexList.get(vertexList.size()-1).y == newY))
            ){
            	//System.out.println("Same vertex, not using it.");
            }else if(vertexList.isEmpty() && (fromX1 == newX && fromX2 == newY)  ){
            	//System.out.println("Same vertex as (fromX, fromY), and list empty, not using it.");
            }else{
            	 vertexList.add(new Vertex(newX, newY , newZ));
            }
            */
            
//            /*
            vertexList.add(new Vertex(newX, newY , newZ));
//            /*
            
//            vertexList.add(new Vertex(cp * rx * ct - sp * ry * st + cx, sp * rx * ct + cp * ry * st + cy , 0));
//            System.out.println(vertexList.get(vertexList.size()-1));
		}
	    return vertexList;
	}
	
	/**
	 * Method is used by the method to draw an arc (arcTo()).
	 * 
	 * @param u the u
	 * @param v the v
	 * 
	 * @return the float
	 */
	private static float angle(float[] u, float[] v){
            float a = (float)Math.acos( (u[0]*v[0] + u[1]*v[1]) / (float)Math.sqrt((u[0]*u[0] + u[1]*u[1]) * (v[0]*v[0] + v[1]*v[1]))) ;
            float sgn = (u[0]*v[1] > u[1]*v[0])? 1 : -1;
            return sgn * a;
	}
	
	
	/**
	 * Checks if is power of two.
	 * 
	 * @param i the i
	 * 
	 * @return true, if is power of two
	 */
	public static boolean isPowerOfTwo(int i){
		return ((i&(i-1)) 		== 0);
	}
	
	/**
	 * Checks whether the given image is of power of 2 dimensions.
	 * 
	 * @param image the image
	 * 
	 * @return true, if checks if is power of two dimension
	 */
	public static boolean isPowerOfTwoDimension(PImage image){
		int iWidth 	= image.width;
		int iHeight = image.height;
		boolean bWidthIsPOT = 	((iWidth&(iWidth-1)) 		== 0);
		boolean bHeightIsPOT = 	((iHeight&(iHeight-1)) 		== 0);
		/* test width for power-of-two */
//		for (int i = 1; i <= 32; i++)
//			if ((unsigned long) iWidth - (1L << i) == 0){
//				bWidthIsPOT = true;
//				break;
//			}
//
//		/* test height for power-of-two */
//		for (int i = 1; i <= 32; i++)
//			if ((unsigned long) iHeight - (1L << i) == 0){
//				bHeightIsPOT = true;
//				break;
//			}
		return (bWidthIsPOT && bHeightIsPOT);
	}
	
	/**
	 * Returns the power of two number nearest to the given number.
	 * 
	 * @param value the value
	 * 
	 * @return the int
	 */
	public static int nearestPower(int value) {
		int i = 1;
		if (value == 0) {
			return (-1);
		}

		for (;;) {
			if (value == 1) {
				return (i);
			} else if (value == 3) {
				return (i * 4);
			}

			value = value >> 1;
	    i *= 2;
		}
	}



	/**
	 * Calculates and returns the normal vector of the plane, the 3 given points are lying in.
	 * Also normalizes the normal if <code>normalize</code> is set to true.
	 * 
	 * @param v0 the v0
	 * @param v1 the v1
	 * @param v2 the v2
	 * @param normalize the normalize
	 * 
	 * @return the normal
	 * 
	 * the normal vector
	 */
	public static Vector3D getNormal(Vector3D v0, Vector3D v1, Vector3D v2, boolean normalize){
		float ax,ay,az, bx,by,bz;
		ax = v1.x - v0.x; //aX
		ay = v1.y - v0.y; //aY
		az = v1.z - v0.z; //aZ
		
		bx = v2.x - v0.x; //bX
		by = v2.y - v0.y; //by
		bz = v2.z - v0.z; //bz
		
		float crossX = ay * bz - by * az;
        float crossY = az * bx - bz * ax;
        float crossZ = ax * by - bx * ay;
        
        Vector3D normal = new Vector3D(crossX, crossY, crossZ);
        
        if (normalize)
        	normal.normalizeLocal();
        
		return normal;
	}

	
	
	/**
	 * Projects the polygon and and the point to check into 2D and then checks if the given point
	 * is inside the shape.
	 * <br><strong>NOTE:</strong> The polygon to test has to be planar, meaning that all points must lie
	 * int the same plane in 3d space.
	 * 
	 * @param testPoint the test point
	 * @param polyNormal the poly normal
	 * @param polygonVertices the polygon vertices
	 * 
	 * @return true, if checks if is point in poly
	 * 
	 * whether the testpoint is inside the planar polygon or not
	 */
	public static boolean isPoint3DInPlanarPolygon(Vector3D[] polygonVertices, Vector3D testPoint, Vector3D polyNormal){
		if (testPoint == null) 
			return false;
		
		//Check parts of the normal to determine in which axis the poly is most contained in
		float absAX = PApplet.abs(polyNormal.x);
		float absBY = PApplet.abs(polyNormal.y);
		float absCZ = PApplet.abs(polyNormal.z);

		if (absAX > absBY){
			if ( absAX > absCZ){ //X biggest, project into y,z drop x
				return (isPoint2DInPolygon(new Vector3D(testPoint), polygonVertices, PolygonTestPlane.YZ));
			}else{ //Z biggest //project into x,y drop z
				return (isPoint2DInPolygon(new Vector3D(testPoint), polygonVertices, PolygonTestPlane.XY));
			}
		}else if (absBY > absAX){
			if (absBY > absCZ){ //Y biggest //project into x,z drop y
				return (isPoint2DInPolygon(new Vector3D(testPoint), polygonVertices, PolygonTestPlane.XZ));
			}else{ //Z biggest //project into x,y drop Z
				return (isPoint2DInPolygon(new Vector3D(testPoint), polygonVertices, PolygonTestPlane.XY));
			}
		}else if (absCZ > absAX){
			if (absCZ > absBY){ //Z biggest //project into x,y
				return (isPoint2DInPolygon(new Vector3D(testPoint), polygonVertices, PolygonTestPlane.XY));
			}else{ //Y biggest // project into x,z
				return (isPoint2DInPolygon(new Vector3D(testPoint), polygonVertices, PolygonTestPlane.XZ));
			}
		}else{
			return false;
		}
	}
	
	
	private enum PolygonTestPlane{
		XY,
		XZ,
		YZ;
	}
	
	private static boolean isPoint2DInPolygon(Vector3D testpoint, Vector3D[] thePolygon, PolygonTestPlane whichPlane) {
		float x;
		float y;
		int c = 0;
		switch (whichPlane) {
		case XY:	
			//System.out.println("Projected to X-Y");
			x = testpoint.x;
			y = testpoint.y;
			for (int i = 0, j = thePolygon.length - 1; i < thePolygon.length; j = i++) {
				if ((((thePolygon[i].y <= y) && (y < thePolygon[j].y)) ||
						((thePolygon[j].y <= y) && (y < thePolygon[i].y))) &&
						(x < (thePolygon[j].x - thePolygon[i].x) * (y - thePolygon[i].y) /
								(thePolygon[j].y - thePolygon[i].y) + thePolygon[i].x)) {
					c = (c + 1) % 2;
				}
			}
			break;
		case XZ:	
			//System.out.println("Projected to X-Z");
			x = testpoint.x;
			y = testpoint.z;
			for (int i = 0, j = thePolygon.length - 1; i < thePolygon.length; j = i++) {
				if ((((thePolygon[i].z <= y) && (y < thePolygon[j].z)) ||
						((thePolygon[j].z <= y) && (y < thePolygon[i].z))) &&
						(x < (thePolygon[j].x - thePolygon[i].x) * (y - thePolygon[i].z) /
								(thePolygon[j].z - thePolygon[i].z) + thePolygon[i].x)) {
					c = (c + 1) % 2;
				}
			}
			break;
		case YZ:	
			//System.out.println("Projected to Y-Z");
			x = testpoint.y;
			y = testpoint.z;
			for (int i = 0, j = thePolygon.length - 1; i < thePolygon.length; j = i++) {
				if ((((thePolygon[i].z <= y) && (y < thePolygon[j].z)) ||
						((thePolygon[j].z <= y) && (y < thePolygon[i].z))) &&
						(x < (thePolygon[j].y - thePolygon[i].y) * (y - thePolygon[i].z) /
								(thePolygon[j].z - thePolygon[i].z) + thePolygon[i].y)) {
					c = (c + 1) % 2;
				}
			}
			break;
		default:
			break;
		}
		return c == 1;
	}

	
	public static boolean isPoint2DInPolygon(float x, float y, Vector3D[] thePolygon) {
		int c = 0;
		for (int i = 0, j = thePolygon.length - 1; i < thePolygon.length; j = i++) {
			if ((((thePolygon[i].y <= y) && (y < thePolygon[j].y)) ||
					((thePolygon[j].y <= y) && (y < thePolygon[i].y))) &&
					(x < (thePolygon[j].x - thePolygon[i].x) * (y - thePolygon[i].y) /
							(thePolygon[j].y - thePolygon[i].y) + thePolygon[i].x)) {
				c = (c + 1) % 2;
			}
		}
		return c == 1;
	}

	
	
//This is the old method- should work tho.	
//	/**
//	 * Use this to check whether a points lies in the polygon described by the vertices
//	 * <br><strong>NOTE:</strong> Use this in 2D, this only uses the x, y coordinates of the vectors!.
//	 * 
//	 * @param x the x
//	 * @param y the y
//	 * @param vertices the vertices
//	 * 
//	 * @return true, if checks if is polygon contains2 d
//	 */
//	public static boolean isPoint2DInPolygon(float x, float y, Vector3D[] vertices){ 
//		float xnew,ynew;
//		float xold,yold;
//		float x1,y1;
//		float x2,y2;
//		int i;
//		int npoints = vertices.length;
//		boolean inside = false;
//
//		if (npoints < 3) {
//			return(false);
//		}
//		
//		//why cast to int here?!
//		xold = vertices[npoints-1].x;
//		yold = vertices[npoints-1].y;
//		
//		for (i=0 ; i < npoints ; i++) {
////			xnew=(int)vertices[i].x; //why cast to int here?!
////			ynew=(int)vertices[i].y;
//			xnew = vertices[i].x;
//			ynew = vertices[i].y;
//				          
//			if (xnew > xold) {
//				x1=xold;
//				x2=xnew;
//				y1=yold;
//				y2=ynew;
//			}else {
//				x1=xnew;
//				x2=xold;
//				y1=ynew;
//				y2=yold;
//			}
//				          
////			if ((xnew < x) == (x <= xold)          /* edge "open" at one end */
////				&& ((long)y-(long)y1)*(long)(x2-x1) < ((long)y2-(long)y1)*(long)(x-x1))
////			{
//			if ((xnew < x) == (x <= xold)          /* edge "open" at one end */
//					&& (y-y1)*(x2-x1) < (y2-y1)*(x-x1))
//			{
//				inside = !inside;
//			}
//			xold = xnew;
//			yold = ynew;
//		}
//		return(inside);
//	}
	
	
	
	/**
	 * Returns the center of mass of the planar polygon vertices in the x,y plane.
	 * <br><strong>NOTE:</strong> Use this in 2D, this only uses the x, y coordinates of the vectors!.
	 * 
	 * @param vertices the vertices
	 * 
	 * @return the polygon center of mass2 d
	 */
	public static Vector3D getPolygonCenterOfMass2D(Vector3D[] vertices){
		float cx=0,cy=0;
		float area=(float)getPolygonArea2D(vertices);
		int i,j;
		int N = vertices.length;

		float factor=0;
		for (i=0;i<N;i++) {
			j = (i + 1) % N;
			factor = (vertices[i].x * vertices[j].y - vertices[j].x * vertices[i].y);
			cx+= (vertices[i].x + vertices[j].x) * factor;
			cy+= (vertices[i].y + vertices[j].y) * factor;
		}
		area*=6.0f;
		factor=1/area;
		cx*=factor;
		cy*=factor;
		
		//TODO how to get this in 3D? Project all vertex to the z plane and get the centerof mass ?
		//this is a test, calculating the Z coordinate by integrating over
		//all z values
		float zValues = 0;
		for (int k = 0; k < vertices.length-1; k++) {
			Vector3D vector3D = vertices[k];
			zValues += vector3D.z;
		}
		
		Vector3D center = new Vector3D(cx, cy , zValues/vertices.length);
//		System.out.println("Center: " + center);
		return center;
	}
	
	
	/**
	 * Calculates the distance between 2 points in 2D (only x,y considered)
	 * 
	 * @param v1 the v1
	 * @param v2 the v2
	 * 
	 * @return the float
	 */
	public static float distance2D(Vector3D v1, Vector3D v2){
		float dx = v1.getX() - v2.getX();
    	float dy = v1.getY() - v2.getY();
    	return (float) Math.sqrt(dx*dx + dy*dy);
	}
	
	/**
	 * Calculates the area of a 2D polygon using its transformed world coordinates
	 * <br><strong>NOTE:</strong> Use this in 2D, this only uses the x, y coordinates of the vectors!
	 * <br>NOTE: works only if the last vertex is equal to the first (polygon is closed correctly). (not confirmed..)
	 * <br>Polygon vertices have to be declared in counter-clockwise order! (or cw..?)
	 * @param vertices the vertices
	 * 
	 * @return the area as double
	 */
	public static double getPolygonArea2D(Vector3D[] vertices){
//		/*
		int i;
		int N = vertices.length;
		double area = 0;

		for (i=0;i<N-1;i++) {
			area = area + vertices[i].x * vertices[i+1].y - vertices[i+1].x * vertices[i].y;
		}
		area /= 2.0;
//		System.out.println("Area: " + (area < 0 ? -area : area));
//		*/
		
//		double area = getPolygonAreaSigned2D(vertices);
//		System.out.println("Area: " + area);
		
		return (area < 0 ? -area : area);
	}
	
	/*
	public static double getPolygonAreaSigned2D(Vector3D[] vertices){
		int i;
		int N = vertices.length;
		double area = 0;

		for (i=0;i<N-1;i++) {
			area = area + vertices[i].x * vertices[i+1].y - vertices[i+1].x * vertices[i].y;
		}
		area /= 2.0;
		return area;
	}
	*/
	
	/**
	 * Checks if the planar polygon vertices contain the point.
	 * 
	 * @param polygonPoints the polygon points
	 * @param testPoint the test point
	 * 
	 * @return true, if checks if is polygon contains point
	 */
	public static boolean isPolygonContainsPoint(Vector3D[] polygonPoints, Vector3D testPoint){
		Vector3D polyNormal = getNormal(polygonPoints[0],polygonPoints[1], polygonPoints[2], true);
		//Check if point is in plane of polygon
		Vector3D tmp = testPoint.getSubtracted(polygonPoints[0]);
		float dotProdukt = tmp.dot(polyNormal);
		
		//Remove the second condition if you want exact matches, this allows a small tolerance
		if (dotProdukt == 0 || Math.abs(dotProdukt) < 0.015) {
			return Tools3D.isPoint3DInPlanarPolygon(polygonPoints, testPoint, polyNormal);
		}
		else{	
			return false;
		}
	}


	public static GL beginGL(PApplet pa){
		return ((PGraphicsOpenGL)pa.g).beginGL();
	}
	
	public static void endGL(PApplet pa){
		((PGraphicsOpenGL)pa.g).endGL();
	}
	
	/**
	 * Gets the openGL context.
	 * <br>NOTE: If you want to invoke any opengl drawing commands (or other commands influencing or depending on the current modelview matrix)
	 * you have to call GL <code>Tools3D.beginGL(PApplet pa)</code> instead!
	 * <br>NOTE: the openGL context is only valid and current when the rendering thread is the current thread.
	 * <br>
	 * This only gets the opengl context if started in opengl mode using the opengl renderer.
	 * 
	 * @param pa the pa
	 * 
	 * @return the gL
	 */
	public static GL getGL(PApplet pa){
		//TODO experiemtn with 2 async gl contexts!
//		 ((PGraphicsOpenGL)pa.g).getContext().makeCurrent();
//		((PGraphicsOpenGL)pa.g).getContext().getGLDrawable().createContext(arg0);
//		boolean current =((PGraphicsOpenGL)pa.g).getContext().makeCurrent() == GLContext.CONTEXT_NOT_CURRENT;
		return ((PGraphicsOpenGL)pa.g).gl;
	}


	/**
	 * Sets the opengl vertical syncing on or off.
	 * 
	 * @param pa the pa
	 * @param on the on
	 */
	public static void setVSyncing(PApplet pa, boolean on){
		if (MT4jSettings.getInstance().getRendererMode() == MT4jSettings.OPENGL_MODE){
			GL gl = getGL(pa); 
			if (on){
				gl.setSwapInterval(1);
			}else{
				gl.setSwapInterval(0);
			}
		}
	}


	/**
	 * Clamps the specified value to the specified limits.
	 * If the value is exceeds the lower or upper limit, the value is set to that limit. Else
	 * the value stays the same.
	 * 
	 * @param x the x
	 * @param low the low
	 * @param high the high
	 * 
	 * @return the float
	 */
	public static final float clamp(float x, float low, float high) {
	     return (x < low) ? low : ((x > high) ? high : x);
	 }

	
    /**
     * Convenience function to map a variable from one coordinate space
     * to another.
     * 
     * @param theValue the the value
     * @param theInStart the the in start
     * @param theInEnd the the in end
     * @param theOutStart the the out start
     * @param theOutEnd the the out end
     * 
     * @return the float
     */
    public static final float map(float theValue, float theInStart, float theInEnd, float theOutStart, float theOutEnd) {
        return theOutStart + (theOutEnd - theOutStart) * ((theValue - theInStart) / (theInEnd - theInStart));
    }


    /**
     * For non power of two textures, the texture coordinates
     * have to be in the range from 0..texture_width instead of from 0.0 to 1.0.
     * <br>So we try to scale the texture coords to the width/height of the texture
     * 
     * @param texture the texture
     * @param verts the verts
     */
    public static void scaleTextureCoordsForRectModeFromNormalized(PImage texture, Vertex[] verts){
    	for (int i = 0; i < verts.length; i++) {
    		Vertex vertex = verts[i];
    		if (vertex.getTexCoordU() <= 1.0f && vertex.getTexCoordU() >= 0.0f){
    			vertex.setTexCoordU(vertex.getTexCoordU() *  texture.width);
    		}
    		if (vertex.getTexCoordV() <= 1.0f && vertex.getTexCoordV() >= 0.0f){
    			vertex.setTexCoordV(vertex.getTexCoordV() *  texture.height);
    		}
    	}
    }
    
    

    
    public static void setLineSmoothEnabled(GL gl, boolean enable){
//    	/*
    	//DO this if we use multisampling and enable line_smooth from the beginning 
    	//and use multisampling -> we turn off multisampling then before using line_smooth for best restult
    	if (enable){
    		if (MT4jSettings.getInstance().isMultiSampling()){
				gl.glDisable(GL.GL_MULTISAMPLE);
			}
    		//TODO Eventually even dont do that since enabled form the beginning!
    		gl.glEnable(GL.GL_LINE_SMOOTH); 
    	}else{
    		if (MT4jSettings.getInstance().isMultiSampling()){
				gl.glEnable(GL.GL_MULTISAMPLE);
			}
//    		gl.glDisable(GL.GL_LINE_SMOOTH); //Actually never disable line smooth
    	}
//    	*/
    	
    	//DO nothing if we use Multisampling but disable line_smooth from the beginning
    	// -> do all anti aliasing only through multisampling!
    	//
    	/*
    	if (enable){
    		if (MT4jSettings.getInstance().isMultiSampling()){
				gl.glDisable(GL.GL_MULTISAMPLE);
			}
    		//TODO Eventually even dont do that since enabled form the beginning!
    		gl.glEnable(GL.GL_LINE_SMOOTH); 
    	}else{
    		if (MT4jSettings.getInstance().isMultiSampling()){
				gl.glEnable(GL.GL_MULTISAMPLE);
			}
//    		gl.glDisable(GL.GL_LINE_SMOOTH); //Actually never disable line smooth
    	}
    	*/
    }
    
    
    /*
    public static void startRenderToTexture(){
    	//save viewport and set up new one
    	int viewport[4];
    	glGetIntegerv(GL_VIEWPORT,(int*)viewport);
    	glViewport(0,0,xSize,ySize);

    	//draw a misc scene
    	DrawScene();

    	//save data to texture using glCopyTexImage2D
    	glBindTexture(GL_TEXTURE_2D,texture);

    	glCopyTexImage2D(GL_TEXTURE_2D, 0, GL_RGB,
    	   0,0, xSize, ySize, 0);

    	//restore viewport
    	glViewport(viewport[0],viewport[1],viewport[2],viewport[3]);
    }
    */

}
