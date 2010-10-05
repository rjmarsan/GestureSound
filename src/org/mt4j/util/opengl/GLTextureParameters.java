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

import javax.media.opengl.GL;


/**
 * This class stores the parameters for a texture: target, internal format, minimization filter
 * and magnification filter.
 * Original file Copyright (c) by Andrés Colubri
 */
public class GLTextureParameters implements GLConstants 
{

	//http://gregs-blog.com/2008/01/17/opengl-texture-filter-parameters-explained/

	/**
	 * Creates an instance of GLTextureParameters, setting all the parameters to default values.
	 */
	public GLTextureParameters(){
		target = NORMAL;
		format = COLOR;

		//Texture filtering
		minFilter = LINEAR_MIPMAP_NEAREST; //Per default enable mip mapping, bi-linear filtering, scaled down textures
		magFilter = LINEAR;
//		minFilter = NEAREST_MIPMAP_NEAREST; //ugly..
//		magFilter = LINEAR_MIPMAP_LINEAR;
//		minFilter = LINEAR;
//		magFilter = LINEAR;

		//Texture wrap mode
		this.wrap_s = GL.GL_REPEAT;
		this.wrap_t = GL.GL_REPEAT;
//		this.wrap_s = GL.GL_CLAMP;
//		this.wrap_t = GL.GL_CLAMP;
//		//newer, not supported everywhere?
//		this.wrap_s = GL.GL_CLAMP_TO_EDGE;
//		this.wrap_t = GL.GL_CLAMP_TO_EDGE;

	}
	
    /** Texture target. */
    public int target;
	
    /** Texture internal format. */
    public int format;
	
    /** Texture minimization filter. */
    public int minFilter;
	
    /** Texture magnification filter. */
    public int magFilter;	
    
    public int wrap_s;
    
    public int wrap_t;
}

