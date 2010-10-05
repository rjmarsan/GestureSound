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
 
package org.mt4j.util.opengl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.opengl.PGraphicsOpenGL;

import com.sun.opengl.util.BufferUtil;

/**
 * This class adds an opengl texture to a PImage object. The texture is handled in a similar way to the
 * pixels property: image data can be copied to and from the texture using loadTexture and updateTexture methods.
 * However, bringing the texture down to image or pixels data can slow down the application considerably (since
 * involves copying texture data from GPU to CPU), especially when handling large textures. So it is recommended
 * to do all the texture handling without calling updateTexture, and doing so only at the end if the texture
 * is needed as a regular image.
 * Original file Copyright (c) by Andrés Colubri
 */
public class GLTexture extends PImage implements PConstants , GLConstants{
	//FIXME TEST
    /** The initialized. */
	private boolean glTextureInitialized = false;
    
	/**
	 * Checks if is initialized.
	 * 
	 * @return true, if is initialized
	 */
	public boolean isGLTexObjectInitialized() {
		return glTextureInitialized;
	}

	/**
	 * Creates an instance of GLTexture with size 1x1. The texture is not initialized.
	 * 
	 * @param parent PApplet
	 */
    public GLTexture(PApplet parent){
    	this(parent, 1, 1, new GLTextureParameters(), false);
    }  

    /**
     * Creates an instance of GLTexture with size width x height. The texture is initialized (empty) to that size.
     * 
     * @param parent PApplet
     * @param width int
     * @param height int
     */	 
    public GLTexture(PApplet parent, int width, int height){
    	this(parent, width, height, new GLTextureParameters(), true);
    }
    
    
    /**
     * Creates an instance of GLTexture with size width x height and with the specified parameters.
     * The texture is initialized (empty) to that size.
     * 
     * @param parent PApplet
     * @param width int
     * @param height int
     * @param params GLTextureParameters
     */	 
    public GLTexture(PApplet parent, int width, int height, GLTextureParameters params){
    	this(parent, width, height, params, true);
    }	
    
    
//    FIXME ENABLE
    /**
     * Creates an instance of GLTexture with size width x height and with the specified parameters.
     * The texture is initialized (empty) to that size.
     * 
     * @param parent PApplet
     * @param width int
     * @param height int
     * @param params GLTextureParameters
     * @param initGLTextureObject indicates whether to also create a openGL texture object (needed if using opengl)
     */	 
    public GLTexture(PApplet parent, int width, int height, GLTextureParameters params, boolean initGLTextureObject){
        super(width, height, ARGB);  
//    	super(width, height, RGB);
        this.parent = parent;
       
        pgl = (PGraphicsOpenGL)parent.g;
        gl = pgl.gl;
		setTextureParams(params);
       
		if (initGLTextureObject){
			initTexture(width, height);
		}
    }	
    
    /**
     * Creates an instance of GLTexture with size width x height and with the specified parameters.
     * The texture is initialized (empty) to that size.
     * The last paramter is a dummy parameter, that isnt used.
     * This constructor initialized the textures internal PImage object (used by processing to texture stuff)
     * with 1,1, dimensions. This is useful if we really only intend to use the texture for opengl dawing.
     * 
     * @param parent PApplet
     * @param width int
     * @param height int
     * @param params GLTextureParameters
     * @param initGLTextureObject indicates whether to also create a openGL texture object (needed if using opengl)
     * @param dummy the dummy
     */	 
    public GLTexture(PApplet parent, int width, int height, GLTextureParameters params, boolean initGLTextureObject, int dummy){
    	//FIXME TEST DO DIFFERENT!
    	//this texture for opengl!
//    	super(width, height, ARGB);  
        super(1, 1, ARGB);
        this.width = width;
        this.height = height;
        
        this.parent = parent;
       
        pgl = (PGraphicsOpenGL)parent.g;
        gl = pgl.gl;
		setTextureParams(params);
       
		if (initGLTextureObject){
			initTexture(width, height);
		}
    }	
    
	
    
    /** Creates an instance of GLTexture using image file filename as source.
     * 
     * @param parent PApplet
     * @param filename String
     */	
    public GLTexture(PApplet parent, String filename){
    	this(parent, filename, new GLTextureParameters());
    }
    
    
    /**
     * Creates an instance of GLTexture with power-of-two width and height that such that width * height is the closest to size, and with the specified parameters.
     * The texture is initialized (empty) to that size.
     * 
     * @param parent PApplet
     * @param params GLTextureParameters
     * @param fileName the file name
     */	 
    public GLTexture(PApplet parent, String fileName, GLTextureParameters params){
        super(1, 1, ARGB);  
        this.parent = parent;
       
        pgl = (PGraphicsOpenGL)parent.g;
        gl = pgl.gl;
        
       setTextureParams(params); //TODO change params to use texture_recangle_arb if image is non power of two?
       loadTexture(fileName);
//        init(width, height, params); //INIT OR NOT? -> initialized in loadtexture already?
    }
    
    
    /**
     * Creates an instance of GLTexture with power-of-two width and height that such that width * height is the closest to size.
     * The texture is initialized (empty) to that size.
     * 
     * @param parent PApplet
     * @param size int
     */	
    public GLTexture(PApplet parent, int size){
        super(1, 1, ARGB);  
        this.parent = parent;
       
        pgl = (PGraphicsOpenGL)parent.g;
        gl = pgl.gl;
        
        calculateWidthHeight(size);
        
        init(width, height);
    }

    /**
     * Sets the size of the image and texture to width x height. If the texture is already initialized,
     * it first destroys the current opengl texture object and then creates a new one with the specified
     * size.
     * 
     * @param width int
     * @param height int
     */
    public void init(int width, int height){
	    init(width, height, new GLTextureParameters());
    }

    /**
     * Sets the size of the image and texture to width x height, and the parameters of the texture to params .
     * If the texture is already  initialized, it first destroys the current opengl texture object and then creates
     * a new one with the specified size.
     * 
     * @param width int
     * @param height int
     * @param params GLTextureParameters
     */
    public void init(int width, int height, GLTextureParameters params){
        super.init(width, height, ARGB);
		setTextureParams(params);
        initTexture(width, height);
    }	
	
    /**
     * Returns true if the texture has been initialized.
     * 
     * @return boolean
     */  
    public boolean available()
    {
        return 0 < tex[0];
    }
    
    /**
     * Provides the ID of the opegl texture object.
     * 
     * @return int
     */	
    public int getTextureID()
    {
        return tex[0];
    }

    /**
     * Returns the texture target.
     * 
     * @return int
     */	
    public int getTextureTarget()
    {
        return texTarget;
    }    

    /**
     * Returns the texture internal format.
     * 
     * @return int
     */	
    public int getTextureInternalFormat()
    {
        return texInternalFormat;
    }

    /**
     * Returns the texture minimization filter.
     * 
     * @return int
     */	
    public int getTextureMinFilter()
    {
        return minFilter;
    }

    /**
     * Returns the texture magnification filter.
     * 
     * @return int
     */	
    public int getTextureMagFilter()
    {
        return magFilter;
    }
	
    /**
     * Returns true or false whether or not the texture is using mipmaps.
     * 
     * @return boolean
     */	
    public boolean usingMipmaps()
    {
        return usingMipmaps;
    }
	
    /**
     * Returns the maximum possible value for the texture coordinate S.
     * 
     * @return float
     */	
    public float getMaxTextureCoordS()
    {
        return maxTexCoordS;
    }
	
    /**
     * Returns the maximum possible value for the texture coordinate T.
     * 
     * @return float
     */	
    public float getMaxTextureCoordT()
    {
        return maxTexCoordT;
    }
	
    /**
     * Returns true if the texture is flipped along the horizontal direction.
     * 
     * @return boolean;
     */	
    public boolean isFlippedX()
    {
        return flippedX;
    }

    /**
     * Sets the texture as flipped or not flipped on the horizontal direction.
     * 
     * @param v boolean;
     */	
    public void setFlippedX(boolean v)
    {
        flippedX = v;
    }	
	
    /**
     * Returns true if the texture is flipped along the vertical direction.
     * 
     * @return boolean;
     */	
    public boolean isFlippedY()
    {
        return flippedY;
    }

    /**
     * Sets the texture as flipped or not flipped on the vertical direction.
     * 
     * @param v boolean;
     */	
    public void setFlippedY(boolean v)
    {
        flippedY = v;
    }
	
    /**
     * Puts img into texture, pixels and image.
     * 
     * @param img PImage
     */
    public void putImage(PImage img){
//    	System.out.println("putting image in glTexture, img format: " + img.format);
        img.loadPixels();
        
        if ((img.width != width) || (img.height != height))
        {
        	//System.out.println("put pixels into texture..dimensions are different from former texture!");
        	GLTextureParameters p = new GLTextureParameters();
        	p.format = this.textureParams.format;
        	p.magFilter = this.textureParams.magFilter;
        	p.minFilter = this.textureParams.minFilter;
        	p.target = this.textureParams.target;
        	p.wrap_s = this.textureParams.wrap_s;
        	p.wrap_t = this.textureParams.wrap_t;
        	
            init(img.width, img.height, p);
        }

        // Putting img into pixels...
        PApplet.arraycopy(img.pixels, pixels);
   
        // ...into texture...
        loadTexture();
        
        // ...and into image.
        updatePixels();
    }
    
    /**
     * Puts img into texture, pixels and image.
     * 
     * @param img PImage
     */
    public void putImageOnly(PImage img){
//    	/*
        img.loadPixels();
        
        if ((img.width != width) || (img.height != height))
        {
        	System.out.println("put pixels into texture..dimensions are different from former texture!");
        	GLTextureParameters p = new GLTextureParameters();
        	p.format = this.textureParams.format;
        	p.magFilter = this.textureParams.magFilter;
        	p.minFilter = this.textureParams.minFilter;
        	p.target = this.textureParams.target;
        	p.wrap_s = this.textureParams.wrap_s;
        	p.wrap_t = this.textureParams.wrap_t;
        	
            init(img.width, img.height, p);
        }

        // Putting img into pixels...
        PApplet.arraycopy(img.pixels, pixels);
   
        // ...into texture...
//        loadTexture();
        
        // ...and into image.
        updatePixels();
//        */
    }

    
    /**
     * Puts pixels of img into openGL texture object only.
     * 
     * @param img PImage
     */
    public void putPixelsIntoTexture(PImage img){
        if ((img.width != width) || (img.height != height)){
        	//FIXME THIS WAS ADDED
        	System.out.println("put pixels into texture..dimensions are different from former texture!");
        	GLTextureParameters p = new GLTextureParameters();
        	p.format = this.textureParams.format;
        	p.magFilter = this.textureParams.magFilter;
        	p.minFilter = this.textureParams.minFilter;
        	p.target = this.textureParams.target;
        	p.wrap_s = this.textureParams.wrap_s;
        	p.wrap_t = this.textureParams.wrap_t;
        	
        	init(img.width, img.height, p);
//            init(img.width, img.height, new GLTextureParameters());
        }
   
        // Putting into texture.
		putBuffer(img.pixels);
    }
	
    /**
     * Puts the pixels of img inside the rectangle (x, y, x+w, y+h) into texture only.
     * 
     * @param img PImage
     * @param x int
     * @param y int
     * @param w int
     * @param h int
     */	
    public void putPixelsIntoTexture(PImage img, int x, int y, int w, int h){	
	    x = PApplet.constrain(x, 0, img.width);
		y = PApplet.constrain(y, 0, img.height);
		
	    w = PApplet.constrain(w, 0, img.width - x);
		h = PApplet.constrain(h, 0, img.height - y);
		
        if ((w != width) || (h != height))
        {
            init(w, h, new GLTextureParameters());
        }
		
		int p0;
		int dest[] = new int[w * h];
        for (int j = 0; j < h; j++)
		{
		    p0 = y * img.width + x + (img.width - w) * j;
            PApplet.arraycopy(img.pixels, p0 + w * j, dest, w * j, w);
		}
   
        // Putting into texture.
		putBuffer(dest);
    }
	
    /*
    public PImage getPImage(){
    	int w = width;
        int h = height;
    	PImage img = new PImage(width, height);
    	
    	int size = w * h;
    	IntBuffer buffer = BufferUtil.newIntBuffer(size);
        gl.glBindTexture(texTarget, tex[0]);
        gl.glGetTexImage(texTarget, 0, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE, buffer);
        gl.glBindTexture(texTarget, 0);       
       
        buffer.get(img.pixels);
        img.updatePixels();
        return img;
    }
    */
    
    /**
     * Copies texture to img.
     * 
     * @param img PImage
     */	
    public void getImage(PImage img){
        int w = width;
        int h = height;
        
        if ((img.width != w) || (img.height != h))
        {
            img.init(w, h, ARGB);
        }
     
        int size = w * h;
        IntBuffer buffer = BufferUtil.newIntBuffer(size);
        gl.glBindTexture(texTarget, tex[0]);
        gl.glGetTexImage(texTarget, 0, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE, buffer);
        gl.glBindTexture(texTarget, 0);       
       
        buffer.get(img.pixels);
        if (flippedX) 
        	flipArrayOnX(img.pixels, 1);
        if (flippedY) 
        	flipArrayOnY(img.pixels, 1);
        img.updatePixels();       
    }

    /**
     * Load texture, pixels and image from file.
     * 
     * @param filename String
     */
    public void loadTexture(String filename){
        PImage img = parent.loadImage(filename);
        putImage(img);
    }

    /**
     * Copy pixels to openGL texture (loadPixels should have been called beforehand).
     */	
    public void loadTexture(){
	    putBuffer(pixels);
    }
	
    /**
     * Copy texture to pixels (doesn't call updatePixels).
     */		
    public void updateTexture(){
        int size = width * height;
        IntBuffer buffer = BufferUtil.newIntBuffer(size);
		
        gl.glBindTexture(texTarget, tex[0]);
        gl.glGetTexImage(texTarget, 0, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE, buffer);
        gl.glBindTexture(texTarget, 0);
		
        buffer.get(pixels);

        if (flippedX) flipArrayOnX(pixels, 1);
        if (flippedY) flipArrayOnY(pixels, 1);
    }

//    /**
//     * Applies filter texFilter using this texture as source and destTex as destination.
//     * @param texFilter GLTextureFilter
//     * @param destTex GLTexture		
//     */
//    public void filter(GLTextureFilter texFilter, GLTexture destTex)
//    {
//        texFilter.apply(new GLTexture[] { this }, new GLTexture[] { destTex });
//    }
//
//    /**
//     * Applies filter texFilter using this texture as source and destTex as multiple destinations.
//     * @param texFilter GLTextureFilter
//     * @param destTexArray GLTexture[]			
//     */
//    public void filter(GLTextureFilter texFilter, GLTexture[] destTexArray)
//    {
//        texFilter.apply(new GLTexture[] { this }, destTexArray);
//    }
//
//    /**
//     * Applies filter texFilter using this texture as source, destTex as destination and params as the
//     * parameters for the filter.
//     * @param texFilter GLTextureFilter
//     * @param destTex GLTexture				
//     * @param params GLTextureFilterParameters			     
//     */	
//    public void filter(GLTextureFilter texFilter, GLTexture destTex, GLTextureFilterParameters params)
//    {
//        texFilter.apply(new GLTexture[] { this }, new GLTexture[] { destTex }, params);
//    }
//
//	/**
//     * Applies filter texFilter using this texture as source, destTex as multiple destinations and params as the
//     * parameters for the filter.
//     * @param texFilter GLTextureFilter
//     * @param destTexArray GLTexture[]	
//     * @param params GLTextureFilterParameters			
//     */	
//    public void filter(GLTextureFilter texFilter, GLTexture[] destTexArray, GLTextureFilterParameters params)
//    {
//        texFilter.apply(new GLTexture[] { this }, destTexArray, params);
//    }
//
//    /**
//     * Applies filter texFilter using this texture as source, destTex as destination and fadeConst as the
//     * fading constant for the filter.
//     * @param texFilter GLTextureFilter
//     * @param destTex GLTexture				
//     * @param fadeConst float			
//     */	
//    public void filter(GLTextureFilter texFilter, GLTexture destTex, float fadeConst)
//    {
//        texFilter.apply(new GLTexture[] { this }, new GLTexture[] { destTex }, fadeConst);
//    }
//
//	/**
//     * Applies filter texFilter using this texture as source, destTex as multiple destinations  and fadeConst as the
//     * fading constant for the filter.
//     * @param texFilter GLTextureFilter
//     * @param destTexArray GLTexture[]	
//     * @param fadeConst float			
//     */	
//    public void filter(GLTextureFilter texFilter, GLTexture[] destTexArray, float fadeConst)
//    {
//        texFilter.apply(new GLTexture[] { this }, destTexArray, fadeConst);
//    }
//	
//    /**
//     * Applies filter texFilter using this texture as source, destTex as destination, fadeConst as the
//     * fading constant for the filter  and params as the parameters for the filter.
//     * @param texFilter GLTextureFilter
//     * @param destTex GLTexture				
//     * @param fadeConst float			
//     */	
//    public void filter(GLTextureFilter texFilter, GLTexture destTex, float fadeConst, GLTextureFilterParameters params)
//    {
//        texFilter.apply(new GLTexture[] { this }, new GLTexture[] { destTex }, fadeConst, params);
//    }
//
//	/**
//     * Applies filter texFilter using this texture as source, destTex as multiple destinations, fadeConst as the
//     * fading constant for the filter and params as the parameters for the filter.
//     * @param texFilter GLTextureFilter
//     * @param destTexArray GLTexture[]	
//     * @param fadeConst float			
//     */	
//    public void filter(GLTextureFilter texFilter, GLTexture[] destTexArray, float fadeConst, GLTextureFilterParameters params)
//    {
//        texFilter.apply(new GLTexture[] { this }, destTexArray, fadeConst, params);
//    }	
//	

    
    /**
     * Draws the texture using the opengl commands, inside a rectangle of width w and height h
     * located at (x,y).
     * 
     * @param x float
     * @param y float
     * @param w float
     * @param h float
     */	

    /**
     * Copies intArray into the texture, assuming that the array contains 4 color components and pixels are unsigned bytes.
     * 
     * @param intArray int[]
     */	
    public void putBuffer(int[] intArray){
        putBuffer(intArray, TEX4, TEX_UBYTE);
    }

    /**
     * Copies intArray into the texture, using the specified format and assuming that the pixels are unsigned bytes.
     * 
     * @param intArray int[]
     * @param format int
     */	
    public void putBuffer(int[] intArray, int format){
        putBuffer(intArray, format, TEX_UBYTE);
    }

    /**
     * Copies intArray into the texture, using the specified format and assuming that the pixels are unsigned bytes.
     * 
     * @param intArray int[]
     * @param format int
     */
    public void putByteBuffer(int[] intArray, int format){
        putBuffer(intArray, format, TEX_UBYTE);
    }

    /**
     * Copies intArray into the texture, using the specified format and assuming that the pixels are integers.
     * 
     * @param intArray int[]
     * @param format int
     */
    public void putIntBuffer(int[] intArray, int format){
        putBuffer(intArray, format, TEX_INT);
    }

    /**
     * Copies intArray into the texture, using the format and type specified.
     * 
     * @param intArray int[]
     * @param format int
     * @param type int
     */	
    public void putBuffer(int[] intArray, int format, int type){
    	this.putBuffer(IntBuffer.wrap(intArray), format, type);
    	
//    	
//        if (tex[0] == 0){
//            initTexture(width, height);
//        }      
//  
//        int glFormat;
//        if (format == TEX1) 
//        	glFormat = GL.GL_LUMINANCE;
//        else if (format == TEX3) 
//        	glFormat = GL.GL_RGB;
//        else 
//        	glFormat = GL.GL_BGRA;
////        	glFormat = GL.GL_RGB;
//
//        int glType;
//        if (type == TEX_INT) 
//        	glType = GL.GL_INT;
//        else 
//        	glType = GL.GL_UNSIGNED_BYTE;
//
//        gl.glBindTexture(texTarget, tex[0]);
//        
//        //FIXME REMOVE
////        System.out.println("Formats: ");
////        System.out.println(GL.GL_RGB);
////        System.out.println(GL.GL_BGRA);
////        System.out.println(GL.GL_INT);
////        System.out.println(GL.GL_UNSIGNED_BYTE);
////        
////        System.out.println("glFormat: " + glFormat);
////        System.out.println("glType: " + glType);
//        
////      glFormat = GL.GL_RGB;
////      glFormat = GL.GL_RGBA;
////      glFormat = GL.GL_BGR;
////      glFormat = GL.GL_BGRA;
////      glFormat = GL.GL_ABGR_EXT;
//        
////      glType = GL.GL_UNSIGNED_BYTE;
////      glType = GL.GL_BYTE;
//        
//        if (texTarget == GL.GL_TEXTURE_1D){
//            if (glFormat == GL.GL_BGRA){ 
//            	glFormat = GL.GL_RGBA;
//            }
//            gl.glTexSubImage1D(texTarget, 0, 0, width, glFormat, glType, IntBuffer.wrap(intArray));
//        }else{
//            if (usingMipmaps){
////            	System.out.println("usingmipmaps - gluBuild2DMipmaps");
//            	GLU glu = ((PGraphicsOpenGL)this.parent.g).glu;
//            	glu.gluBuild2DMipmaps(texTarget, texInternalFormat, width, height, glFormat, glType, IntBuffer.wrap(intArray));
////            	glstate.glu.gluBuild2DMipmaps(texTarget, texInternalFormat, width, height, glFormat, glType, IntBuffer.wrap(intArray));
//            }
//            else{
////            	System.out.println("glTexSubImage2D - no mipmaps; rectangular: " + (texTarget==GL.GL_TEXTURE_RECTANGLE_ARB));
//                gl.glTexSubImage2D(texTarget, 0, 0, 0, width, height, glFormat, glType, IntBuffer.wrap(intArray));
//            	
//            	//FIXME TEST
////            	gl.glTexSubImage2D(texTarget, 0, 0, 0, width, height, GL.GL_LUMINANCE, glType, IntBuffer.wrap(intArray));
////            	gl.glTexSubImage2D(texTarget, 0, 0, 0, width, height, GL.GL_ALPHA, glType, IntBuffer.wrap(intArray));
//            	
////            	gl.glTexImage2D(texTarget, 0, texInternalFormat, width, height, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, IntBuffer.wrap(intArray));
//            }
//        }
//        gl.glBindTexture(texTarget, 0);
    }

    
    /**
     * Copies buffer into the texture, using the format and type specified.
     * 
     * @param buffer IntBuffer
     * @param format int
     * @param type int
     */	
    public void putBuffer(IntBuffer buffer, int format, int type){
        if (tex[0] == 0){
            initTexture(width, height);
        }      
  
        int glFormat;
        if (format == TEX1) 
        	glFormat = GL.GL_LUMINANCE;
        else if (format == TEX3) 
        	glFormat = GL.GL_RGB;
        else 
        	glFormat = GL.GL_BGRA;

        int glType;
        if (type == TEX_INT) 
        	glType = GL.GL_INT;
        else 
        	glType = GL.GL_UNSIGNED_BYTE;

        gl.glBindTexture(texTarget, tex[0]);

        if (texTarget == GL.GL_TEXTURE_1D){
            if (glFormat == GL.GL_BGRA){ 
            	glFormat = GL.GL_RGBA;
            }
            gl.glTexSubImage1D(texTarget, 0, 0, width, glFormat, glType, buffer);
        }else{
            if (usingMipmaps){
//            	System.out.println("usingmipmaps - gluBuild2DMipmaps");
            	GLU glu = ((PGraphicsOpenGL)this.parent.g).glu;
            	glu.gluBuild2DMipmaps(texTarget, texInternalFormat, width, height, glFormat, glType, buffer);
//            	glstate.glu.gluBuild2DMipmaps(texTarget, texInternalFormat, width, height, glFormat, glType, buffer);
            }
            else{
                gl.glTexSubImage2D(texTarget, 0, 0, 0, width, height, glFormat, glType, buffer);
            }
        }

        gl.glBindTexture(texTarget, 0);
    }

    /**
     * Copies floatArray into the texture, assuming that the array has 4 components.
     * 
     * @param floatArray float[]
     */
    public void putBuffer(float[] floatArray){
        putBuffer(floatArray, TEX4);
    }

    /**
     * Copies floatArray into the texture, using the specified format.
     * 
     * @param floatArray float[]
     * @param format int
     */
    public void putBuffer(float[] floatArray, int format){
        if (tex[0] == 0){
            initTexture(width, height);
        }

        int glFormat;
        if (format == TEX1) 
        	glFormat = GL.GL_LUMINANCE;
        else if 
        	(format == TEX3) glFormat = GL.GL_RGB;
        else 
        	glFormat = GL.GL_RGBA;

        gl.glBindTexture(texTarget, tex[0]);

        if (texTarget == GL.GL_TEXTURE_1D)
            gl.glTexSubImage1D(texTarget, 0, 0, width, glFormat, GL.GL_FLOAT, FloatBuffer.wrap(floatArray));
        else
            gl.glTexSubImage2D(texTarget, 0, 0, 0, width, height, glFormat, GL.GL_FLOAT, FloatBuffer.wrap(floatArray));

        gl.glBindTexture(texTarget, 0);
    }

    /**
     * Copies the texture into intArray, assuming that the array has 4 components and the pixels are unsigned bytes.
     * 
     * @param intArray int[]
     */
    public void getBuffer(int[] intArray){
        getBuffer(intArray, TEX4, TEX_UBYTE);
    }

    /**
     * Copies the texture into intArray, using the specified format and assuming that the pixels are unsigned bytes.
     * 
     * @param intArray int[]
     * @param format int
     */
    public void getBuffer(int[] intArray, int format){
        getBuffer(intArray, format, TEX_UBYTE);
    }

    /**
     * Copies the texture into intArray, using the specified format and assuming that the pixels are unsigned bytes.
     * 
     * @param intArray int[]
     * @param format int
     */
    public void getByteBuffer(int[] intArray, int format){
        getBuffer(intArray, format, TEX_UBYTE);
    }

    /**
     * Copies the texture into intArray, using the specified format and assuming that the pixels are integers.
     * 
     * @param intArray int[]
     * @param format int
     */
    public void getIntBuffer(int[] intArray, int format){
        getBuffer(intArray, format, TEX_INT);
    }

    /**
     * Copies the texture into intArray, using the specified format and type.
     * 
     * @param intArray int[]
     * @param format int
     * @param type int
     */
    public void getBuffer(int[] intArray, int format, int type){
        int mult;
        int glFormat;
        if (format == TEX1){ 
            mult = 1;
            glFormat = GL.GL_LUMINANCE;
        }else if (format == TEX3){ 
            mult = 3;
            glFormat = GL.GL_RGB;
        }else{ 
            mult = 4;
            glFormat = GL.GL_RGBA;
        }

        int size;
        int glType;
        if (type == TEX_INT){
            glType = GL.GL_INT;
        }else{
            mult = 1;
            glType = GL.GL_UNSIGNED_BYTE;
        }
        size = mult * width * height;

        if (intArray.length != size){
            System.err.println("Wrong size of buffer!");
            return;
        }

        IntBuffer buffer = BufferUtil.newIntBuffer(size);
		
        gl.glBindTexture(texTarget, tex[0]);
        gl.glGetTexImage(texTarget, 0, glFormat, glType, buffer);
        gl.glBindTexture(texTarget, 0);
		
        buffer.get(intArray);

        if (flippedX) flipArrayOnX(intArray, mult);
        if (flippedY) flipArrayOnY(intArray, mult);
    }

    /**
     * Copies the texture into floatArray.
     * 
     * @param floatArray float[]
     * @param format int
     */
    public void getBuffer(float[] floatArray, int format){
        int mult;
        int glFormat;
        if (format == TEX1){ 
            mult = 1;
            glFormat = GL.GL_LUMINANCE;
        }else if (format == TEX3){ 
            mult = 3;
            glFormat = GL.GL_RGB;
        }else{ 
            mult = 4;
            glFormat = GL.GL_RGBA;
        }

        int size = mult * width * height;
        if (floatArray.length != size){
            System.err.println("Wrong size of buffer!");
            return;
        }

        FloatBuffer buffer = BufferUtil.newFloatBuffer(size);
		
        gl.glBindTexture(texTarget, tex[0]);
        gl.glGetTexImage(texTarget, 0, glFormat, GL.GL_FLOAT, buffer);
        gl.glBindTexture(texTarget, 0);
		
		buffer.get(floatArray);

        if (flippedX) 
        	flipArrayOnX(floatArray, mult);
        if (flippedY) 
        	flipArrayOnY(floatArray, mult);
    }


    /**
     * Sets the texture to have the same given float value in each component.
     * 
     * @param r float
     * @param g float
     * @param b float
     * @param a float
     */
    public void setValue(float r, float g, float b, float a){
        float valBuffer[] = new float[4 * width * height];
        for (int j = 0; j < height; j++)
            for (int i = 0; i < width; i++)
            {
                valBuffer[i * 4 + j * width * 4] = r;
                valBuffer[i * 4 + j * width * 4 + 1] = g;
                valBuffer[i * 4 + j * width * 4 + 2] = b;
                valBuffer[i * 4 + j * width * 4 + 3] = a;
            }
        putBuffer(valBuffer);
    }


    /**
 * Flips intArray along the X axis.
 * 
 * @param intArray int[]
 * @param mult int
 */
    protected void flipArrayOnX(int[] intArray, int mult){
        int index = 0;
        int xindex = mult * (width - 1);
        for (int x = 0; x < width / 2; x++) 
        {
             for (int y = 0; y < height; y++)
             {
                 int i = index + mult * y * width;
                 int j = xindex + mult * y * width;

                 for (int c = 0; c < mult; c++) 
                 {
                     int temp = intArray[i];
                     intArray[i] = intArray[j];
                     intArray[j] = temp;
                 
                     i++;
                     j++;
                 }

            }
            index += mult;
            xindex -= mult;
        }
    }

    /**
     * Flips intArray along the Y axis.
     * 
     * @param intArray int[]
     * @param mult int
     */
    protected void flipArrayOnY(int[] intArray, int mult){
        int index = 0;
        int yindex = mult * (height - 1) * width;
        for (int y = 0; y < height / 2; y++) 
        {
             for (int x = 0; x < mult * width; x++) 
             {
                  int temp = intArray[index];
                  intArray[index] = intArray[yindex];
                  intArray[yindex] = temp;

                  index++;
                  yindex++;
            }
            yindex -= mult * width * 2;
        }
	}

    /**
     * Flips floatArray along the X axis.
     * 
     * @param mult int
     * @param floatArray the float array
     */
    protected void flipArrayOnX(float[] floatArray, int mult){
        int index = 0;
        int xindex = mult * (width - 1);
        for (int x = 0; x < width / 2; x++){
             for (int y = 0; y < height; y++){
                 int i = index + mult * y * width;
                 int j = xindex + mult * y * width;

                 for (int c = 0; c < mult; c++){
                     float temp = floatArray[i];
                     floatArray[i] = floatArray[j];
                     floatArray[j] = temp;
                 
                     i++;
                     j++;
                 }
            }
            index += mult;
            xindex -= mult;
        }
    }

    /**
     * Flips floatArray along the Y axis.
     * 
     * @param mult int
     * @param floatArray the float array
     */
    protected void flipArrayOnY(float[] floatArray, int mult){
        int index = 0;
        int yindex = mult * (height - 1) * width;
        for (int y = 0; y < height / 2; y++){
             for (int x = 0; x < mult * width; x++){
                  float temp = floatArray[index];
                  floatArray[index] = floatArray[yindex];
                  floatArray[yindex] = temp;

                  index++;
                  yindex++;
            }
            yindex -= mult * width * 2;
        }
	}

    /**
     * Inits the opengl texture object.
     * 
     * @param w int
     * @param h int
     * 
     * @invisible
     * Creates an empty opengl texture object.
     * Deletes the an already existing gl texture object if this object contained one.
     * The gl texture object can  be filled with a call to <code>putPixelsIntoTexture</code>.
     * It then uses the PImage pixels to fill the texture.
     */
    public void initTexture(int w, int h){
    	this.glTextureInitialized = true;
    	
        if (tex[0] != 0){
            deleteTextureGL();
        }
        //Give me a free texture allocation adress ID and save it into the int array (tex)
        //1 is for the number of ids to generate, since it is "1" here, it will
        //place one id in the arrays first index tex[0] will be the id we can use
        gl.glGenTextures(1, tex, 0);
        
        //tells opengl which texture to reference in following calls from now on!
        //the first parameter is eigher GL.GL_TEXTURE_2D or ..1D
        gl.glBindTexture(texTarget, tex[0]);
        
        //SET texture mag/min FILTER mode
        gl.glTexParameteri(texTarget, GL.GL_TEXTURE_MAG_FILTER, magFilter);
        gl.glTexParameteri(texTarget, GL.GL_TEXTURE_MIN_FILTER, minFilter);
        
        //SET texture WRAP mode
        //GL_REPEAT with a RECTANGULAR texture target are not supported! -> so use GL_CLAMP then.
        if (this.texTarget == GL.GL_TEXTURE_RECTANGLE_ARB){
        	//FIXME gluBuildmipmaps staucht NPOT texture auf pot zusammen?
        	//BEi clamp komischer wasser fbo error
        	if (this.wrap_s == GL.GL_REPEAT){
        		this.wrap_s = GL.GL_CLAMP;
        	}
        	if (this.wrap_t == GL.GL_REPEAT){
        		this.wrap_t = GL.GL_CLAMP;
        	}
        	maxTexCoordS = w;
        	maxTexCoordT = h;
        }else{
        	maxTexCoordS = 1.0f;
        	maxTexCoordT = 1.0f; 
        }

		gl.glTexParameteri(texTarget, GL.GL_TEXTURE_WRAP_S, this.wrap_s);
		gl.glTexParameteri(texTarget, GL.GL_TEXTURE_WRAP_T, this.wrap_t);
        
		//Load image DATA into texture
        if (texTarget == GL.GL_TEXTURE_1D) {
        	gl.glTexImage1D(texTarget, 0, texInternalFormat, w, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null);
        }
        else{
        	 //This call will upload the texture to the video memory where it
        	//will be ready for us to use in our programs. 
//        	gl.glTexImage2D(texTarget, 0, texInternalFormat, w, h, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null);
        	
        	gl.glTexImage2D(texTarget, 0, texInternalFormat, w, h, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, null);//ORIGINAL
        	
        	//FIXME TEST
//        	gl.glTexImage2D(texTarget, 0, GL.GL_INTENSITY, w, h, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, null);
//        	gl.glTexImage2D(texTarget, 0, GL.GL_INTENSITY, w, h, 0, GL.GL_LUMINANCE_ALPHA, GL.GL_UNSIGNED_BYTE, null);
//        	gl.glTexImage2D(texTarget, 0, GL.GL_ALPHA8, w, h, 0, GL.GL_ALPHA, GL.GL_UNSIGNED_BYTE, null);
//        	gl.glTexImage2D(texTarget, 0, GL.GL_LUMINANCE, w, h, 0, GL.GL_LUMINANCE, GL.GL_UNSIGNED_BYTE, null);
//        	gl.glTexImage2D(texTarget, 0, 4, w, h, 0, 4, GL.GL_UNSIGNED_BYTE, null);
        	
//        	gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_ALPHA8,  w, h, 0, GL.GL_ALPHA, GL.GL_INT, null); //von fbo code REMOVE!
        }
        gl.glBindTexture(texTarget, 0); //Unbind texture
    }

    /**
     * Delete texture gl.
     * 
     * @invisible
     * Deletes the opengl texture object.
     */
    public void deleteTextureGL(){
    	if (tex[0] != 0){
	        gl.glDeleteTextures(1, tex, 0);  
	        tex[0] = 0;
    	}
    }
    
    
    /** The texture params. */
    private GLTextureParameters textureParams;//FIXME THIS WAS ADDED
    
    /**
     * Sets the texture params.
     * 
     * @param params GLTextureParameters
     * 
     * @invisible
     * Sets texture target and internal format according to the target and  type specified.
     */		
	protected void setTextureParams(GLTextureParameters params){
		textureParams = params;//FIXME THIS WAS ADDED
		
		//Set texture TARGET
		if (params.target == GLConstants.NORMAL){
			texTarget = GL.GL_TEXTURE_2D;
		}else if (params.target == RECTANGULAR){
            texTarget = GL.GL_TEXTURE_RECTANGLE_ARB;		
        }else if (params.target == ONEDIM){
            texTarget = GL.GL_TEXTURE_1D;
        }

		//Set texture color FOMRAT
	    if (params.format == COLOR){
            texInternalFormat = GL.GL_RGBA;
        }else if (params.format == FLOAT4){
            texInternalFormat = GL.GL_RGBA16F_ARB;
	    }else if (params.format == DOUBLE4){
            texInternalFormat = GL.GL_RGBA32F_ARB;
	    }		

	    minFilter = GL.GL_LINEAR;
	    //Set texture MIN filter
	    if (params.minFilter == NEAREST){
            minFilter = GL.GL_NEAREST;
        }else if (params.minFilter == LINEAR){
            minFilter = GL.GL_LINEAR;
	    }else if (params.minFilter == NEAREST_MIPMAP_NEAREST && texTarget != GL.GL_TEXTURE_RECTANGLE_ARB){
            minFilter = GL.GL_NEAREST_MIPMAP_NEAREST;
	    }else if (params.minFilter == LINEAR_MIPMAP_NEAREST && texTarget != GL.GL_TEXTURE_RECTANGLE_ARB){
            minFilter = GL.GL_LINEAR_MIPMAP_NEAREST;
	    }else if (params.minFilter == NEAREST_MIPMAP_LINEAR && texTarget != GL.GL_TEXTURE_RECTANGLE_ARB){
            minFilter = GL.GL_NEAREST_MIPMAP_LINEAR;
	    }else if (params.minFilter == LINEAR_MIPMAP_LINEAR && texTarget != GL.GL_TEXTURE_RECTANGLE_ARB){
            minFilter = GL.GL_LINEAR_MIPMAP_LINEAR;
	    }	
	    
//	    minFilter = GL.GL_LINEAR_MIPMAP_NEAREST; //FIXME REMOVE
	    //FIXME glubuild2dMipmaps seems to work with rectangle_arb textures!?
	    
	    magFilter = GL.GL_LINEAR;
	    //Set texture MAG filter
	    if (params.magFilter == NEAREST){
            magFilter = GL.GL_NEAREST;
        }else if (params.magFilter == LINEAR){
            magFilter = GL.GL_LINEAR;
	    }
	    
	    //Set MipMapping
        usingMipmaps = (minFilter == GL.GL_NEAREST_MIPMAP_NEAREST) ||
                       (minFilter == GL.GL_LINEAR_MIPMAP_NEAREST) ||
                       (minFilter == GL.GL_NEAREST_MIPMAP_LINEAR) ||
                       (minFilter == GL.GL_LINEAR_MIPMAP_LINEAR);
					   
        flippedX = false;
        flippedY = false;	
        
        //Set texture WRAP mode
        this.wrap_s = params.wrap_s;
        this.wrap_t = params.wrap_t;
        
        /*
        if (params.wrap_s == GL.GL_CLAMP){
        	this.wrap_s = GL.GL_CLAMP;
        }else if (params.wrap_s == GL.GL_REPEAT){
        	this.wrap_s = GL.GL_REPEAT;
        }else{
        	this.wrap_s = GL.GL_CLAMP;
        }
        if (params.wrap_t == GL.GL_CLAMP){
        	this.wrap_t = GL.GL_CLAMP;
        }else if (params.wrap_t == GL.GL_REPEAT){
        	this.wrap_t = GL.GL_REPEAT;
        }else{
        	this.wrap_t = GL.GL_CLAMP;
        }
        */
	}	
	
	
	public void setWrap(int wrap_s, int wrap_t){
		this.wrap_s = wrap_s;
		this.textureParams.wrap_s = this.wrap_s;
		this.wrap_t = wrap_t;
		this.textureParams.wrap_s = this.wrap_t;
		
		if (this.isGLTexObjectInitialized()){
			gl.glBindTexture(this.getTextureTarget(), this.getTextureID());
			gl.glTexParameteri(texTarget, GL.GL_TEXTURE_WRAP_S, this.wrap_s);
			gl.glTexParameteri(texTarget, GL.GL_TEXTURE_WRAP_T, this.wrap_t);
			gl.glBindTexture(this.getTextureTarget(), 0);
		}
	}
	
	
	public void setFilter(int minFilter, int magFilter){
        this.minFilter = minFilter;
		this.textureParams.minFilter = this.minFilter;
		this.magFilter = magFilter;
		this.textureParams.magFilter = this.magFilter;
		
		if (this.isGLTexObjectInitialized()){
			gl.glBindTexture(this.getTextureTarget(), this.getTextureID());
			 gl.glTexParameteri(texTarget, GL.GL_TEXTURE_MAG_FILTER, this.magFilter);
		     gl.glTexParameteri(texTarget, GL.GL_TEXTURE_MIN_FILTER, this.minFilter);
			gl.glBindTexture(this.getTextureTarget(), 0);
		}
		
		boolean usedMipMapPreviously = usingMipmaps;
		
		usingMipmaps = (minFilter == GL.GL_NEAREST_MIPMAP_NEAREST) ||
        (minFilter == GL.GL_LINEAR_MIPMAP_NEAREST) ||
        (minFilter == GL.GL_NEAREST_MIPMAP_LINEAR) ||
        (minFilter == GL.GL_LINEAR_MIPMAP_LINEAR);
		
		if (!usedMipMapPreviously && usingMipmaps){
			putBuffer(this.pixels);
		}
	}
	
	
    /**
     * Calculate width height.
     * 
     * @param size int
     * 
     * @invisible
     * Generates a power-of-two box width and height so that width * height is closest to size.
     */	
    protected void calculateWidthHeight(int size){
        int w, h;
        float l = PApplet.sqrt(size);
        for (w = 2; w < l; w *= 2);
        int n0 = w * w;
        int n1 = w * w / 2;
        if (Math.abs(n0 - size) < Math.abs(n1 - size)) h = w;
        else h = w / 2;
        
        width = w;
        height = h;
    }

    
    
    /** The gl. */	
    protected GL gl;
	
    /** The pgl. */			
    protected PGraphicsOpenGL pgl;
	
    /** The tex. */	
//    protected int[] tex = { 0 }; 
    public int[] tex = { 0 }; //FIXME REMOVE!!
	
    /** The tex target. */			
    protected int texTarget;	
	
    /** The tex internal format. */			
    protected int texInternalFormat;

    /** The min filter. */			
    protected int minFilter;	

    /** The mag filter. */			
    protected int magFilter;
	
    /** The using mipmaps. */		
    protected boolean usingMipmaps;	
	
    /** The max tex coord s. */		
    protected float maxTexCoordS;
	
    /** The max tex coord t. */		
    protected float maxTexCoordT;
	
    /** The flipped x. */		
    protected boolean flippedX;	
	
    /** The flipped y. */		
    protected boolean flippedY;
    
    protected int wrap_s = GL.GL_CLAMP;
    
    protected int wrap_t  = GL.GL_CLAMP;

	/**
	 * Gets the texture params.
	 * 
	 * @return the texture params
	 */
	public GLTextureParameters getTextureParams() {
		return textureParams;
	}


    
	
//    /**
//     * @invisible
//     */			
//    protected GLState glstate;
}
