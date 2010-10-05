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
package org.mt4j.components.visibleComponents.widgets.keyboard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.mt4j.components.TransformSpace;
import org.mt4j.components.bounds.BoundsArbitraryPlanarPolygon;
import org.mt4j.components.interfaces.IMTComponent3D;
import org.mt4j.components.visibleComponents.font.FontManager;
import org.mt4j.components.visibleComponents.font.IFont;
import org.mt4j.components.visibleComponents.font.VectorFontCharacter;
import org.mt4j.components.visibleComponents.shapes.AbstractShape;
import org.mt4j.components.visibleComponents.shapes.MTRoundRectangle;
import org.mt4j.components.visibleComponents.widgets.buttons.MTSvgButton;
import org.mt4j.input.gestureAction.InertiaDragAction;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.rotateProcessor.RotateProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.scaleProcessor.ScaleProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor;
import org.mt4j.util.MT4jSettings;
import org.mt4j.util.MTColor;
import org.mt4j.util.animation.Animation;
import org.mt4j.util.animation.AnimationEvent;
import org.mt4j.util.animation.IAnimationListener;
import org.mt4j.util.animation.MultiPurposeInterpolator;
import org.mt4j.util.math.Vector3D;
import org.mt4j.util.math.Vertex;

import processing.core.PApplet;

/**
 * A multitouch keyboard using vector graphics.
 * 
 * @author Christopher Ruff
 */
public class MTKeyboard extends MTRoundRectangle {
	
	/** The pa. */
	private PApplet pa;
	
	/** The key font. */
	private IFont keyFont;
	
	/** The key list. */
	private ArrayList<MTKey> keyList;
	
	/** The shift changers. */
	private ArrayList<MTKey> shiftChangers;
	
	/** The shift pressed. */
	private boolean shiftPressed;
	
	/** The key click action. */
	private KeyClickAction keyClickAction;

//	private CloseClickAction closeClickButtonAction;
	
	/** The text input acceptors. */
	private List<ITextInputListener> textInputAcceptors;

	/**
	 * Creates a new keyboard without an text input acceptor.
	 * 
	 * @param pApplet the applet
	 */
	public MTKeyboard(PApplet pApplet) {
		super(0,0,0, 700, 245, 30,30, pApplet);
		this.pa = pApplet;
		//Set drawing mode
		this.setDrawSmooth(true);
		
		this.setName("unnamed mt-keyboard");
		this.textInputAcceptors = new ArrayList<ITextInputListener>();
		
		if (MT4jSettings.getInstance().isOpenGlMode())
			this.setUseDirectGL(true);
		
		//TODO textareas an keyboard andocken lassen zum schreiben, oder doppelklick auf textarea?
		//TODO buttons textarea clear machen
		//TODO keyboard durch button heranimated
		
		// INIT FIELDS
		//Load the Key font
		keyFont = FontManager.getInstance().createFont(pa, 
				"keys.svg", 30, 
				new MTColor(0,0,0,255), 
				new MTColor(0,0,0,255)); 
//				new MTColor(250,250,250,255), 
//				new MTColor(250,250,250,255));
		
		keyList 		= new ArrayList<MTKey>();
		shiftChangers 	= new ArrayList<MTKey>();
		shiftPressed 	= false;
		keyClickAction 	= new KeyClickAction();
		
//		/*
		//TODO load button only once!
		MTSvgButton keybCloseSvg = new MTSvgButton(MT4jSettings.getInstance().getDefaultSVGPath()
				+ "keybClose.svg", pa);
		//Transform
		keybCloseSvg.scale(0.8f, 0.8f, 1, new Vector3D(0,0,0));
		keybCloseSvg.translate(new Vector3D(640,5,0));
		keybCloseSvg.setBoundsPickingBehaviour(AbstractShape.BOUNDS_ONLY_CHECK);
		keybCloseSvg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (arg0.getID() == TapEvent.BUTTON_CLICKED)
					closeButtonClicked();
			}
		});
		this.addChild(keybCloseSvg);
//		*/
		
		//INITIALIZE SPACE-Button "by hand"
		VectorFontCharacter SpaceF = (VectorFontCharacter) keyFont.getFontCharacterByUnicode("k");
//		MTKey space = new MTKey(SpaceF.getGeometryInfo().getVertices(), /*spaceOutlines,*/pa, " ", " ");
		MTKey space = new MTKey(SpaceF.getGeometryInfo(), /*spaceOutlines,*/pa, " ", " ");
		space.setName(SpaceF.getName());
		//Set the contours to draw the outline - do this after settings the geominfo
		//so the outline displaylist will be overridden with the new contours list
		space.setOutlineContours(SpaceF.getContours()); //FIXME do we have to in opengl mode? -> we will use displayLists anyway..
		if (MT4jSettings.getInstance().isOpenGlMode()){
			space.setUseDirectGL(true);
//			System.out.println(SpaceF.getDisplayListIDs()[0] + "  - " + SpaceF.getDisplayListIDs()[1]);
//			space.setDisplayListIDs(SpaceF.getDisplayListIDs()); //Wenn nicht displaylisten, müssen wir geometry auch übernehmen!
			space.getGeometryInfo().setDisplayListIDs(SpaceF.getGeometryInfo().getDisplayListIDs()); //Wenn nicht displaylisten, müssen wir geometry auch übernehmen!
			space.setUseDisplayList(true);
		}
		space.setNoStroke(false);
		space.setDrawSmooth(true);
		space.setPickable(true);
		space.setGestureAllowance(DragProcessor.class, false);
		space.setGestureAllowance(RotateProcessor.class, false);
		space.setGestureAllowance(ScaleProcessor.class, false);
		space.unregisterAllInputProcessors();
//		space.translate(new Vector3D(100, space.getOriginalHeight(), 0)); //Translate to 0,0
//		space.translate(new Vector3D(225, 190,0));
		scaleKey(space, 40);
		space.setPositionRelativeToParent(new Vector3D(350,210,0));
		space.setGestureAllowance(TapProcessor.class, true);
		space.registerInputProcessor(new TapProcessor(pa));
		space.addGestureListener(TapProcessor.class, keyClickAction);
		SpaceF = null;
		keyList.add(space);
		this.addChild(space);

		/////////////////////
		/*
		ArrayList<KeyInfo> keyInfos = new ArrayList<KeyInfo>();
		
		float lineY = 35;
//		float advanceMent = keyFont.getFontCharacterByUnicode("A").getHorizontalDist()-10;
		float advanceMent = 42;
		float startX = 60;
//		keyInfos.add(new KeyInfo("^", "^", "^", new Vector3D(startX,lineY), 			  KeyInfo.NORMAL_KEY)); //ESC key
		
		keyInfos.add(new KeyInfo("1", "1", "1", new Vector3D(startX+1*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("!", "!", "!", new Vector3D(startX+1*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("2", "2", "2", new Vector3D(startX+2*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("\"", "\"", "\"", new Vector3D(startX+2*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("3", "3", "3", new Vector3D(startX+3*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		
		keyInfos.add(new KeyInfo("4", "4", "4", new Vector3D(startX+4*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("$", "$", "$", new Vector3D(startX+4*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("5", "5", "5", new Vector3D(startX+5*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("%", "%", "%", new Vector3D(startX+5*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("6", "6", "6", new Vector3D(startX+6*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("&", "&", "&", new Vector3D(startX+6*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("7", "7", "7", new Vector3D(startX+7*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("/", "/", "/", new Vector3D(startX+7*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("8", "8", "8", new Vector3D(startX+8*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("(", "(", "(", new Vector3D(startX+8*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("9", "9", "9", new Vector3D(startX+9*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo(")", ")", ")", new Vector3D(startX+9*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("0", "0", "0", new Vector3D(startX+10*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("=", "=", "=", new Vector3D(startX+10*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("\\", "\\", "\\", new Vector3D(startX+11*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("?", "?", "?", new Vector3D(startX+11*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		//////////////////
		lineY = 77;
		startX = 80; // | + 27
		
		keyInfos.add(new KeyInfo("Q", "q", "Q", new Vector3D(startX+1*advanceMent,lineY),  KeyInfo.NORMAL_KEY)); 
		keyInfos.add(new KeyInfo("W", "w", "W", new Vector3D(startX+2*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("E", "e", "E", new Vector3D(startX+3*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("R", "r", "R", new Vector3D(startX+4*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("T", "t", "T", new Vector3D(startX+5*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("Z", "z", "Z", new Vector3D(startX+6*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("U", "u", "U", new Vector3D(startX+7*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("I", "i", "I", new Vector3D(startX+8*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("O", "o", "O", new Vector3D(startX+9*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("P", "p", "P", new Vector3D(startX+10*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		//Ü
		keyInfos.add(new KeyInfo("111", "ü", "Ü", new Vector3D(startX+11*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		
		keyInfos.add(new KeyInfo("+", "+", "+", new Vector3D(startX+12*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("*", "*", "*", new Vector3D(startX+12*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		lineY = 119;
		startX = 136;  //+58
		keyInfos.add(new KeyInfo("A", "a", "A", new Vector3D(startX,lineY), 			   KeyInfo.NORMAL_KEY)); //
		keyInfos.add(new KeyInfo("S", "s", "S", new Vector3D(startX+1*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("D", "d", "D", new Vector3D(startX+2*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("F", "f", "F", new Vector3D(startX+3*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("G", "g", "G", new Vector3D(startX+4*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("H", "h", "H", new Vector3D(startX+5*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("J", "j", "J", new Vector3D(startX+6*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("K", "k", "K", new Vector3D(startX+7*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("L", "l", "L", new Vector3D(startX+8*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		//Ö
		keyInfos.add(new KeyInfo("1111", "ö", "Ö", new Vector3D(startX+9*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		//Ä
		keyInfos.add(new KeyInfo("11", "ä", "Ä", new Vector3D(startX+10*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		
		
		////////////////////
		lineY = 161;
		startX = 70; // -60
		keyInfos.add(new KeyInfo("<", "<", "<", new Vector3D(startX+1*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo(">", ">", ">", new Vector3D(startX+1*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("Y", "y", "Y", new Vector3D(startX+2*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("X", "x", "X", new Vector3D(startX+3*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("C", "c", "C", new Vector3D(startX+4*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("V", "v", "V", new Vector3D(startX+5*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("B", "b", "B", new Vector3D(startX+6*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("N", "n", "N", new Vector3D(startX+7*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("M", "m", "M", new Vector3D(startX+8*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		
		keyInfos.add(new KeyInfo(",", ",", ",", new Vector3D(startX+9*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo(";", ";", ";", new Vector3D(startX+9*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo(".", ".", ".", new Vector3D(startX+10*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo(":", ":", ":", new Vector3D(startX+10*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("-", "-", "-", new Vector3D(startX+11*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		
		keyInfos.add(new KeyInfo("#", "#", "#", new Vector3D(startX+12*advanceMent,lineY),  KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("'", "'", "'", new Vector3D(startX+12*advanceMent,lineY),  KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		///////////
		//Special keys
		keyInfos.add(new KeyInfo("z", "back", "back", 	new Vector3D(580,35),  KeyInfo.NORMAL_KEY));//Backspace
		keyInfos.add(new KeyInfo("v", "\t", "\t", 	new Vector3D(62,77),  KeyInfo.NORMAL_KEY)); //Tab
		keyInfos.add(new KeyInfo("j", "shift", "shift", new Vector3D(78,120), KeyInfo.NORMAL_KEY)); //Shift
		keyInfos.add(new KeyInfo("f", "\n", "\n", 		new Vector3D(615, 105),KeyInfo.NORMAL_KEY)); //Enter
		*/
		
		KeyInfo[] keyInfos = this.getKeysLayout();
		
		//CREATE THE KEYS \\
//		for (int i = 0; i < keyInfos.size(); i++) {
//			KeyInfo keyInfo = keyInfos.get(i);
		for (int i = 0; i < keyInfos.length; i++) {
			KeyInfo keyInfo = keyInfos[i];
			
			VectorFontCharacter fontChar = (VectorFontCharacter) keyFont.getFontCharacterByUnicode(keyInfo.keyfontUnicode);
			//FIXME expensive..
//			MTKey key = new MTKey(fontChar.getGeometryInfo().getVertices(), fontChar.getContours(),pa, keyInfo.charUnicodeToWrite, keyInfo.charUnicodeToWriteShifted);
//			MTKey key = new MTKey(new Vertex[]{new Vertex(0,0,0),new Vertex(10,0,0),new Vertex(0,10,0),}, /*fontChar.getContours(),*/ pa, keyInfo.charUnicodeToWrite, keyInfo.charUnicodeToWriteShifted);
			MTKey key = new MTKey(fontChar.getGeometryInfo(),  pa, keyInfo.charUnicodeToWrite, keyInfo.charUnicodeToWriteShifted);
//			key.setGeometryInfo(fontChar.getGeometryInfo());
			key.setName(fontChar.getName());
			key.setPickable(true);
			key.unregisterAllInputProcessors();
			
			key.setOutlineContours(fontChar.getContours());
			if (MT4jSettings.getInstance().isOpenGlMode()){
				key.setUseDirectGL(true);
				//Use the display lists already created for the font characters of the key-font
				key.getGeometryInfo().setDisplayListIDs(fontChar.getGeometryInfo().getDisplayListIDs());
				key.setUseDisplayList(true);
			}
//			key.setOutlineContours(fontChar.getContours());
			
			//Translate to 0,0
//			key.translate(new Vector3D(0,key.getOriginalHeight(),0));
			//Translate to its designated position
//			key.translate(keyInfo.position);
			
			scaleKey(key, 40);
			
			//Scale ENTER and BACKSPACE
			if (key.getCharacterToWrite().equals("\n")){
//				key.scale(1.60f, 1.60f, 1, key.getCenterPointLocal());
//				key.scale(1.70f, 1.70f, 1, new Vector3D(0,0,0), TransformSpace.LOCAL);
				key.scale(1.70f, 1.70f, 1, key.getCenterPointLocal(), TransformSpace.LOCAL);
			}
			
			key.setPositionRelativeToParent(keyInfo.position);
			
			//this is a hack to fit the bounding shape of the "enter" key to its non-rectangular shape
			if (key.getCharacterToWrite().equals("\n")){
				Vector3D[] v = key.getBoundingShape().getVectorsLocal();
				float indent = (v[1].getX()-v[0].getX())/2f;
				Vertex[] vNew = new Vertex[]{
						 new Vertex(v[0].getX(),v[0].getY()+indent,0)
						,new Vertex(v[0].getX()+indent  -indent/8f ,v[0].getY()+indent,0) //
						,new Vertex(v[0].getX()+indent  -indent/8f,v[0].getY(),0) //
						,new Vertex(v[1])
						,new Vertex(v[2])
						,new Vertex(v[3])
						,new Vertex(v[0].getX(),v[0].getY()+indent,0)
				};
				BoundsArbitraryPlanarPolygon newBounds = new BoundsArbitraryPlanarPolygon(key, vNew); //Expensive..
				key.setBoundsBehaviour(AbstractShape.BOUNDS_ONLY_CHECK);
				key.setBoundingShape(newBounds);
			}
			
			keyList.add(key); 
			key.setGestureAllowance(TapProcessor.class, true);
			key.registerInputProcessor(new TapProcessor(pa));
			key.addGestureListener(TapProcessor.class, keyClickAction);
			
			//Add keys that change during SHIFT to a list
			if (keyInfo.visibilityInfo 			== KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED){
				shiftChangers.add(key);
			}else if (keyInfo.visibilityInfo 	== KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED){
				key.setVisible(false);
				shiftChangers.add(key);
			}
			
			fontChar = null;
			this.addChild(key); 
		}
		
		//Draw this component and its children above 
		//everything previously drawn and avoid z-fighting
		this.setDepthBufferDisabled(true);
	}
	
	
	private KeyInfo[] getKeysLayout(){
		Locale l = Locale.getDefault();
		/*
	      System.out.println("   Language, Country, Variant, Name");
	      System.out.println("");
	      System.out.println("Default locale: ");
	      System.out.println("   "+l.getLanguage()+", "+l.getCountry()+", "
	         +", "+l.getVariant()+", "+l.getDisplayName());
	    */
	    if (l.getLanguage().equalsIgnoreCase(Locale.GERMANY.getLanguage())){
	    	return getGermanLayout();
	    }else{
	    	return getUSLayout();
	    }
	}
	
	
	private KeyInfo[] getGermanLayout(){
		ArrayList<KeyInfo> keyInfos = new ArrayList<KeyInfo>();
		
		float lineY = 35;
//		float advanceMent = keyFont.getFontCharacterByUnicode("A").getHorizontalDist()-10;
		float advanceMent = 42;
		float startX = 60;
//		keyInfos.add(new KeyInfo("^", "^", "^", new Vector3D(startX,lineY), 			  KeyInfo.NORMAL_KEY)); //ESC key
		
		keyInfos.add(new KeyInfo("1", "1", "1", new Vector3D(startX+1*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("!", "!", "!", new Vector3D(startX+1*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("2", "2", "2", new Vector3D(startX+2*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("\"", "\"", "\"", new Vector3D(startX+2*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("3", "3", "3", new Vector3D(startX+3*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		
		keyInfos.add(new KeyInfo("4", "4", "4", new Vector3D(startX+4*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("$", "$", "$", new Vector3D(startX+4*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("5", "5", "5", new Vector3D(startX+5*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("%", "%", "%", new Vector3D(startX+5*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("6", "6", "6", new Vector3D(startX+6*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("&", "&", "&", new Vector3D(startX+6*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("7", "7", "7", new Vector3D(startX+7*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("/", "/", "/", new Vector3D(startX+7*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("8", "8", "8", new Vector3D(startX+8*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("(", "(", "(", new Vector3D(startX+8*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("9", "9", "9", new Vector3D(startX+9*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo(")", ")", ")", new Vector3D(startX+9*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("0", "0", "0", new Vector3D(startX+10*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("=", "=", "=", new Vector3D(startX+10*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("\\", "\\", "\\", new Vector3D(startX+11*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("?", "?", "?", new Vector3D(startX+11*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		//////////////////
		lineY = 77;
		startX = 80; // | + 27
		
		keyInfos.add(new KeyInfo("Q", "q", "Q", new Vector3D(startX+1*advanceMent,lineY),  KeyInfo.NORMAL_KEY)); 
		keyInfos.add(new KeyInfo("W", "w", "W", new Vector3D(startX+2*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("E", "e", "E", new Vector3D(startX+3*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("R", "r", "R", new Vector3D(startX+4*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("T", "t", "T", new Vector3D(startX+5*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("Z", "z", "Z", new Vector3D(startX+6*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("U", "u", "U", new Vector3D(startX+7*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("I", "i", "I", new Vector3D(startX+8*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("O", "o", "O", new Vector3D(startX+9*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("P", "p", "P", new Vector3D(startX+10*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		//Ü
		keyInfos.add(new KeyInfo("111", "ü", "Ü", new Vector3D(startX+11*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		
		keyInfos.add(new KeyInfo("+", "+", "+", new Vector3D(startX+12*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("*", "*", "*", new Vector3D(startX+12*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		lineY = 119;
		startX = 136;  //+58
		keyInfos.add(new KeyInfo("A", "a", "A", new Vector3D(startX,lineY), 			   KeyInfo.NORMAL_KEY)); //
		keyInfos.add(new KeyInfo("S", "s", "S", new Vector3D(startX+1*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("D", "d", "D", new Vector3D(startX+2*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("F", "f", "F", new Vector3D(startX+3*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("G", "g", "G", new Vector3D(startX+4*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("H", "h", "H", new Vector3D(startX+5*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("J", "j", "J", new Vector3D(startX+6*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("K", "k", "K", new Vector3D(startX+7*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("L", "l", "L", new Vector3D(startX+8*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		//Ö
		keyInfos.add(new KeyInfo("1111", "ö", "Ö", new Vector3D(startX+9*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		//Ä
		keyInfos.add(new KeyInfo("11", "ä", "Ä", new Vector3D(startX+10*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		
		
		////////////////////
		lineY = 161;
		startX = 70; // -60
		keyInfos.add(new KeyInfo("<", "<", "<", new Vector3D(startX+1*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo(">", ">", ">", new Vector3D(startX+1*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("Y", "y", "Y", new Vector3D(startX+2*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("X", "x", "X", new Vector3D(startX+3*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("C", "c", "C", new Vector3D(startX+4*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("V", "v", "V", new Vector3D(startX+5*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("B", "b", "B", new Vector3D(startX+6*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("N", "n", "N", new Vector3D(startX+7*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("M", "m", "M", new Vector3D(startX+8*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		
		keyInfos.add(new KeyInfo(",", ",", ",", new Vector3D(startX+9*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo(";", ";", ";", new Vector3D(startX+9*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo(".", ".", ".", new Vector3D(startX+10*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo(":", ":", ":", new Vector3D(startX+10*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("-", "-", "-", new Vector3D(startX+11*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		
		keyInfos.add(new KeyInfo("#", "#", "#", new Vector3D(startX+12*advanceMent,lineY),  KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("'", "'", "'", new Vector3D(startX+12*advanceMent,lineY),  KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		///////////
		//Special keys
		keyInfos.add(new KeyInfo("z", "back", "back", 	new Vector3D(580,35),  KeyInfo.NORMAL_KEY));//Backspace
		keyInfos.add(new KeyInfo("v", "\t", "\t", 	new Vector3D(62,77),  KeyInfo.NORMAL_KEY)); //Tab
		keyInfos.add(new KeyInfo("j", "shift", "shift", new Vector3D(78,120), KeyInfo.NORMAL_KEY)); //Shift
		keyInfos.add(new KeyInfo("f", "\n", "\n", 		new Vector3D(615, 105),KeyInfo.NORMAL_KEY)); //Enter
		
		return keyInfos.toArray(new KeyInfo[keyInfos.size()]);
	}
	
	
	
	
	//FIXME no "@" available in key font?
	
	
	
	private KeyInfo[] getUSLayout(){
		ArrayList<KeyInfo> keyInfos = new ArrayList<KeyInfo>();
		
		float lineY = 35;
//		float advanceMent = keyFont.getFontCharacterByUnicode("A").getHorizontalDist()-10;
		float advanceMent = 42;
		float startX = 60;
//		keyInfos.add(new KeyInfo("^", "^", "^", new Vector3D(startX,lineY), 			  KeyInfo.NORMAL_KEY)); //ESC key
		
		keyInfos.add(new KeyInfo("1", "1", "1", new Vector3D(startX+1*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("!", "!", "!", new Vector3D(startX+1*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("2", "2", "2", new Vector3D(startX+2*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		//FIXME should be "@" here
		
		
		keyInfos.add(new KeyInfo("3", "3", "3", new Vector3D(startX+3*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("#", "#", "#", new Vector3D(startX+3*advanceMent,lineY),  KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("4", "4", "4", new Vector3D(startX+4*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("$", "$", "$", new Vector3D(startX+4*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("5", "5", "5", new Vector3D(startX+5*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("%", "%", "%", new Vector3D(startX+5*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("6", "6", "6", new Vector3D(startX+6*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		//FIXME "^" missing
		
		keyInfos.add(new KeyInfo("7", "7", "7", new Vector3D(startX+7*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("&", "&", "&", new Vector3D(startX+7*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("8", "8", "8", new Vector3D(startX+8*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("*", "*", "*", new Vector3D(startX+8*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("9", "9", "9", new Vector3D(startX+9*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("(", "(", "(", new Vector3D(startX+9*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("0", "0", "0", new Vector3D(startX+10*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo(")", ")", ")", new Vector3D(startX+10*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("=", "=", "=", new Vector3D(startX+11*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("+", "+", "+", new Vector3D(startX+11*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		
		//////////////////
		lineY = 77;
		startX = 80; // | + 27
		
		keyInfos.add(new KeyInfo("Q", "q", "Q", new Vector3D(startX+1*advanceMent,lineY),  KeyInfo.NORMAL_KEY)); 
		keyInfos.add(new KeyInfo("W", "w", "W", new Vector3D(startX+2*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("E", "e", "E", new Vector3D(startX+3*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("R", "r", "R", new Vector3D(startX+4*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("T", "t", "T", new Vector3D(startX+5*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("Y", "y", "Y", new Vector3D(startX+6*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		
		keyInfos.add(new KeyInfo("U", "u", "U", new Vector3D(startX+7*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("I", "i", "I", new Vector3D(startX+8*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("O", "o", "O", new Vector3D(startX+9*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("P", "p", "P", new Vector3D(startX+10*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		//Ü
//		keyInfos.add(new KeyInfo("111", "ü", "Ü", new Vector3D(startX+11*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		
		//FIXME woanders hin
		keyInfos.add(new KeyInfo("\\", "\\", "\\", new Vector3D(startX+11*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		
		keyInfos.add(new KeyInfo("-", "-", "-", new Vector3D(startX+12*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		
		
		lineY = 119;
		startX = 136;  //+58
		keyInfos.add(new KeyInfo("A", "a", "A", new Vector3D(startX,lineY), 			   KeyInfo.NORMAL_KEY)); //
		keyInfos.add(new KeyInfo("S", "s", "S", new Vector3D(startX+1*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("D", "d", "D", new Vector3D(startX+2*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("F", "f", "F", new Vector3D(startX+3*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("G", "g", "G", new Vector3D(startX+4*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("H", "h", "H", new Vector3D(startX+5*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("J", "j", "J", new Vector3D(startX+6*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("K", "k", "K", new Vector3D(startX+7*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("L", "l", "L", new Vector3D(startX+8*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		//Ö
//		keyInfos.add(new KeyInfo("1111", "ö", "Ö", new Vector3D(startX+9*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		//Ä
//		keyInfos.add(new KeyInfo("11", "ä", "Ä", new Vector3D(startX+10*advanceMent,lineY),  KeyInfo.NORMAL_KEY));
		
		keyInfos.add(new KeyInfo(";", ";", ";", new Vector3D(startX+9*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo(":", ":", ":", new Vector3D(startX+9*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo("'", "'", "'", new Vector3D(startX+10*advanceMent,lineY),  KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("\"", "\"", "\"", new Vector3D(startX+10*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		////////////////////
		lineY = 161;
		startX = 70; // -60
		
		
		keyInfos.add(new KeyInfo("Z", "z", "Z", new Vector3D(startX+2*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("X", "x", "X", new Vector3D(startX+3*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("C", "c", "C", new Vector3D(startX+4*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("V", "v", "V", new Vector3D(startX+5*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("B", "b", "B", new Vector3D(startX+6*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("N", "n", "N", new Vector3D(startX+7*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		keyInfos.add(new KeyInfo("M", "m", "M", new Vector3D(startX+8*advanceMent,lineY), KeyInfo.NORMAL_KEY));
		
		keyInfos.add(new KeyInfo(",", ",", ",", new Vector3D(startX+9*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("<", "<", "<", new Vector3D(startX+9*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		keyInfos.add(new KeyInfo(".", ".", ".", new Vector3D(startX+10*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo(">", ">", ">", new Vector3D(startX+10*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
				
		//FIXME wohin
		keyInfos.add(new KeyInfo("/", "/", "/", new Vector3D(startX+11*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED));
		keyInfos.add(new KeyInfo("?", "?", "?", new Vector3D(startX+11*advanceMent,lineY), KeyInfo.KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED));
		
		
		
		
		
		
		///////////
		//Special keys
		keyInfos.add(new KeyInfo("z", "back", "back", 	new Vector3D(580,35),  KeyInfo.NORMAL_KEY));//Backspace
		keyInfos.add(new KeyInfo("v", "\t", "\t", 	new Vector3D(62,77),  KeyInfo.NORMAL_KEY)); //Tab
		keyInfos.add(new KeyInfo("j", "shift", "shift", new Vector3D(78,120), KeyInfo.NORMAL_KEY)); //Shift
		keyInfos.add(new KeyInfo("f", "\n", "\n", 		new Vector3D(615, 105),KeyInfo.NORMAL_KEY)); //Enter
		
		return keyInfos.toArray(new KeyInfo[keyInfos.size()]);
	}
	
	
	
	
	
	
	
	
	
	
	
	private void scaleKey(MTKey key, float scale){
//		Vector3D scalingPoint = new Vector3D(0,0,0);
		Vector3D scalingPoint = key.getCenterPointLocal();
		key.scale(1/key.getHeightXY(TransformSpace.RELATIVE_TO_PARENT), 1/key.getHeightXY(TransformSpace.RELATIVE_TO_PARENT), 1, scalingPoint);
		key.scale(scale, scale, 1, scalingPoint);
	}
	
	
	
	private boolean setWidthRelativeToParent(float width){
		if (width > 0){
			Vector3D centerPoint;
			if (this.isBoundingShapeSet()){
				centerPoint = this.getBoundingShape().getCenterPointLocal();
				centerPoint.transform(this.getLocalMatrix());
			}else{
				centerPoint = this.getCenterPointGlobal();
				centerPoint.transform(this.getGlobalInverseMatrix());
			}
			this.scale(1/this.getWidthXY(TransformSpace.RELATIVE_TO_PARENT), 1/this.getWidthXY(TransformSpace.RELATIVE_TO_PARENT), 1, centerPoint);
			this.scale(width, width, 1, centerPoint);
//			this.scale(1/this.getWidthLocal(), 1/this.getWidthLocal(), 1, centerPoint, TransformSpace.RELATIVE_TO_SELF);
//			this.scale(width, width, 1, centerPoint, TransformSpace.RELATIVE_TO_SELF);
			return true;
		}else
			return false;
	}
	
	
	@Override
	protected void setDefaultGestureActions() {
		super.setDefaultGestureActions();
		this.addGestureListener(DragProcessor.class, new InertiaDragAction());
	}
	
	
	/**
	 * The Class KeyInfo.
	 * 
	 * @author C.Ruff
	 */
	private class KeyInfo{
		
		/** The keyfont unicode. */
		String keyfontUnicode;
		
		/** The char unicode to write. */
		String charUnicodeToWrite;
		
		/** The char unicode to write shifted. */
		String charUnicodeToWriteShifted;
		
		/** The position. */
		Vector3D position;
		
		/** The visibility info. */
		int visibilityInfo;
		
		/** The Constant NORMAL_KEY. */
		public static final int NORMAL_KEY 								= 0;
		
		/** The Constant KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED. */
		public static final int KEY_ONLY_VISIBLE_WHEN_SHIFT_NOTPRESSED 	= 1;
		
		/** The Constant KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED. */
		public static final int KEY_ONLY_VISIBLE_WHEN_SHIFT_PRESSED 	= 2;
		
		/**
		 * Instantiates a new key info.
		 * 
		 * @param keyfontUnicode the keyfont unicode
		 * @param charUnicodeToWrite the char unicode to write
		 * @param charUnicodeToWriteShifted the char unicode to write shifted
		 * @param position the position
		 * @param visibilityInfo the visibility info
		 */
		public KeyInfo(String keyfontUnicode, String charUnicodeToWrite, String charUnicodeToWriteShifted, Vector3D position, int visibilityInfo) {
			super();
			this.keyfontUnicode = keyfontUnicode;
			this.charUnicodeToWrite = charUnicodeToWrite;
			this.charUnicodeToWriteShifted = charUnicodeToWriteShifted;
			this.position = position;
			this.visibilityInfo = visibilityInfo;
		}
		
	}
	
	
	
	/**
	 * The Class KeyClickAction.
	 * 
	 * @author C.Ruff
	 */
	private class KeyClickAction implements IGestureEventListener {
//		private HashMap<MTKey, MTKey> keysNotToScaleBack;
		
		/** The key press indent. */
		private int keyPressIndent;
		
		/**
		 * Instantiates a new key click action.
		 */
		public KeyClickAction(){
//			keysNotToScaleBack = new HashMap<MTKey, MTKey>();
			keyPressIndent = 3;
		}
		
		/* (non-Javadoc)
		 * @see com.jMT.input.gestureAction.IGestureAction#processGesture(com.jMT.input.inputAnalyzers.GestureEvent)
		 */
		public boolean processGestureEvent(MTGestureEvent g) {
			if (g instanceof TapEvent){
				TapEvent clickEvent = (TapEvent)g;
				IMTComponent3D clicked = clickEvent.getTargetComponent();
				
				if (clicked != null && clicked instanceof MTKey){
					MTKey clickedKey = (MTKey)clicked;
					
					switch (clickEvent.getTapID()) {
					case TapEvent.BUTTON_DOWN:
						clickedKey.setPressed(true);
						float keyHeight = clickedKey.getHeightXY(TransformSpace.RELATIVE_TO_PARENT);
						float keyWidth 	= clickedKey.getWidthXY(TransformSpace.RELATIVE_TO_PARENT);
						
//						clickedKey.setSizeXYRelativeToParent(keyWidth-keyPressIndent, keyHeight-keyPressIndent);
						setSizeXYRelativeToParent(clickedKey, keyWidth-keyPressIndent, keyHeight-keyPressIndent);
						
						if (clickedKey.getCharacterToWrite().equals("shift")){
							shiftPressed = true;
							// Make certain keys visible / not visible when shift pressed!
							for (MTKey key: shiftChangers){
								key.setVisible(!key.isVisible());
							}
						}
						
						keyboardButtonDown(clickedKey, shiftPressed);
							
						break;
					case TapEvent.BUTTON_UP:
					case TapEvent.BUTTON_CLICKED:
						clickedKey.setPressed(false);
						float kHeight = clickedKey.getHeightXY(TransformSpace.RELATIVE_TO_PARENT);
						float kWidth = clickedKey.getWidthXY(TransformSpace.RELATIVE_TO_PARENT);
						
//						if (keysNotToScaleBack.get(clickedKey) == null){
//							clickedKey.setSizeLocal(kWidth+keyPressIndent, kHeight+keyPressIndent);
//						}else{
//							keysNotToScaleBack.remove(clickedKey);
//						}
						
//						clickedKey.setSizeXYRelativeToParent(kWidth+keyPressIndent, kHeight+keyPressIndent);
						setSizeXYRelativeToParent(clickedKey, kWidth+keyPressIndent, kHeight+keyPressIndent);
						
						
						//System.out.println("Button CLICKED: " + clickedKey.getCharacterToWrite());
						if (clickedKey.getCharacterToWrite().equals("shift")){
							shiftPressed = false;
							//Set shift visible keys visible/not visible
							for (MTKey key: shiftChangers){
								key.setVisible(!key.isVisible());
							}
						}
						
						keyboardButtonClicked(clickedKey, shiftPressed);
						break;
					default:
						break;
					}//switch
				}//instance of key
			}//instanceof clickevent
			return false;
		}//method
	}//class

	/**
	 * Sets the size xy relative to parent.
	 * 
	 * @param shape the shape
	 * @param width the width
	 * @param height the height
	 * 
	 * @return true, if successful
	 */
	private boolean setSizeXYRelativeToParent(AbstractShape shape, float width, float height){
		if (width > 0 && height > 0){
			Vector3D centerPoint;
			if (shape.isBoundingShapeSet()){
				centerPoint = shape.getBoundingShape().getCenterPointLocal();
				centerPoint.transform(shape.getLocalMatrix()); //TODO nötig?
			}else{
				centerPoint = shape.getCenterPointGlobal();
				centerPoint.transform(shape.getGlobalInverseMatrix());
			}
			shape.scale(1/shape.getWidthXY(TransformSpace.RELATIVE_TO_PARENT), 1/shape.getHeightXY(TransformSpace.RELATIVE_TO_PARENT), 1, centerPoint);
			shape.scale(width, height, 1, centerPoint);
			return true;
		}else
			return false;
	}
	
	/**
	 * Called after keyboard button pressed.
	 * 
	 * @param clickedKey the clicked key
	 * @param shiftPressed the shift pressed
	 */
	protected void keyboardButtonDown(MTKey clickedKey, boolean shiftPressed){
		ITextInputListener[] listeners = this.getTextInputListeners();
		for (int i = 0; i < listeners.length; i++) {
			ITextInputListener textInputListener = listeners[i];
			if (clickedKey.getCharacterToWrite().equals("back")){
				textInputListener.removeLastCharacter();
			}else if (clickedKey.getCharacterToWrite().equals("shift")){
					//no nothing
			}else{
				String charToAdd = shiftPressed ? clickedKey.getCharacterToWriteShifted() : clickedKey.getCharacterToWrite();
				textInputListener.appendCharByUnicode(charToAdd);
			}
		}
	}
	
	/**
	 * Keyboard button up.
	 * 
	 * @param clickedKey the clicked key
	 * @param shiftPressed the shift pressed
	 */
	protected void keyboardButtonUp(MTKey clickedKey, boolean shiftPressed){
//		keyboardButtonClicked(clickedKey, shiftPressed);
	}
	
	/**
	 * Keyboard button clicked.
	 * 
	 * @param clickedKey the clicked key
	 * @param shiftPressed the shift pressed
	 */
	protected void keyboardButtonClicked(MTKey clickedKey, boolean shiftPressed){
		
	}
	
//	/**
//	 * Gets the text input acceptor.
//	 * 
//	 * @return the text input acceptor
//	 */
//	public ITextInputAcceptor getTextInputAcceptor() {
//		return textInputAcceptor;
//	}
//
//	/**
//	 * Sets the text input acceptor.
//	 * 
//	 * @param textInputAcceptor the new text input acceptor
//	 */
//	public void setTextInputAcceptor(ITextInputAcceptor textInputAcceptor) {
//		this.textInputAcceptor = textInputAcceptor;
//	}
	
	public synchronized void addTextInputListener(ITextInputListener textListener){
		if (!this.textInputAcceptors.contains(textListener)){
			this.textInputAcceptors.add(textListener);
		}
	}
	
	public synchronized ITextInputListener[] getTextInputListeners(){
		return this.textInputAcceptors.toArray(new ITextInputListener[this.textInputAcceptors.size()]);
	}
	
	public synchronized void removeTextInputListener(ITextInputListener textListener){
		if (this.textInputAcceptors.contains(textListener)){
			this.textInputAcceptors.remove(textListener);
		}
	}
	
	

	public void close(){
		this.closeKeyboard();
	}
	
	protected void closeKeyboard(){
		float width = this.getWidthXY(TransformSpace.RELATIVE_TO_PARENT);
		Animation keybCloseAnim = new Animation("Keyboard Fade", new MultiPurposeInterpolator(width, 1, 300, 0.2f, 0.5f, 1), this);
		keybCloseAnim.addAnimationListener(new IAnimationListener(){
			public void processAnimationEvent(AnimationEvent ae) {
//				float delta = ae.getAnimation().getInterpolator().getCurrentStepDelta();
				switch (ae.getId()) {
				case AnimationEvent.ANIMATION_STARTED:
				case AnimationEvent.ANIMATION_UPDATED:
					float currentVal = ae.getAnimation().getInterpolator().getCurrentValue();
//					keyboard.setWidthXYRelativeToParent(currentVal);
					setWidthRelativeToParent(currentVal);
					break;
				case AnimationEvent.ANIMATION_ENDED:
					setVisible(false);
					destroy();
					break;	
				default:
					break;
				}//switch
			}//processanimation
		});
		keybCloseAnim.start();
	}

	
	protected void closeButtonClicked(){
		this.close();
	}
	
	
	@Override
	protected void destroyComponent() {
		super.destroyComponent();
		
		keyFont = null;
		keyList.clear();
		shiftChangers.clear();
		textInputAcceptors.clear();
	}

}
