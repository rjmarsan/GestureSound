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
package advanced.gestureSound;

import java.awt.event.KeyEvent;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;

import msafluid.MSAFluidSolver2D;

import org.mt4j.MTApplication;
import org.mt4j.components.MTComponent;
import org.mt4j.input.IMTInputEventListener;
import org.mt4j.input.inputData.AbstractCursorInputEvt;
import org.mt4j.input.inputData.InputCursor;
import org.mt4j.input.inputData.MTInputEvent;
import org.mt4j.sceneManagement.AbstractScene;
import org.mt4j.util.MT4jSettings;
import org.mt4j.util.math.Vector3D;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.opengl.PGraphicsOpenGL;

import com.sun.opengl.util.BufferUtil;

/**
 * The Class FluidSimulationScene.
 * 
 * The original fluid simulation code was taken from
 * memo akten (www.memo.tv)
 * 
 */
public class GestureSoundScene extends AbstractScene{
	
	private GestureSound pong;
	
	private MTApplication app;

	public GestureSoundScene(MTApplication mtApplication, String name) {
		super(mtApplication, name);
		this.app = mtApplication;
		
		if (!MT4jSettings.getInstance().isOpenGlMode()){
			System.err.println("Scene only usable when using the OpenGL renderer! - See settings.txt");
        	return;
        }
		
		pong = new GestureSound(mtApplication, this);
        
        this.getCanvas().addChild(pong);
        
	}
	
		
	
	@Override
	public void drawAndUpdate(PGraphics graphics, long timeDelta) {
		super.drawAndUpdate(graphics, timeDelta);
	}
	
	
	
	
	
	//@Override
	public void init() {
		app.registerKeyEvent(this);
	}

	//@Override
	public void shutDown() {
		app.unregisterKeyEvent(this);
	}
	
	
	/**
	 * 
	 * @param e
	 */
	public void keyEvent(KeyEvent e){
		int evtID = e.getID();
		if (evtID != KeyEvent.KEY_PRESSED)
			return;
		switch (e.getKeyCode()){
		case KeyEvent.VK_BACK_SPACE:
			app.popScene();
			break;
			default:
				break;
		}
	}

	
	
}