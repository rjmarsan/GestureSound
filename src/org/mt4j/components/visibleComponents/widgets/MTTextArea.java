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
package org.mt4j.components.visibleComponents.widgets;

import java.util.ArrayList;
import java.util.Iterator;

import javax.media.opengl.GL;

import org.mt4j.components.TransformSpace;
import org.mt4j.components.clipping.Clip;
import org.mt4j.components.visibleComponents.font.BitmapFont;
import org.mt4j.components.visibleComponents.font.BitmapFontCharacter;
import org.mt4j.components.visibleComponents.font.IFont;
import org.mt4j.components.visibleComponents.font.IFontCharacter;
import org.mt4j.components.visibleComponents.shapes.MTRectangle;
import org.mt4j.components.visibleComponents.widgets.keyboard.ITextInputListener;
import org.mt4j.components.visibleComponents.widgets.keyboard.MTKeyboard;
import org.mt4j.input.inputProcessors.componentProcessors.lassoProcessor.IdragClusterable;
import org.mt4j.util.MT4jSettings;
import org.mt4j.util.MTColor;
import org.mt4j.util.math.Tools3D;
import org.mt4j.util.math.Vector3D;
import org.mt4j.util.math.Vertex;

import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * The Class MTTextArea. This widget allows to display text with a specified font.
 * If the constructor with no fixed text are dimensions is used, the text area will
 * expand itself to fit the text in. 
 * <br>
 * If the constructor with fixed dimensions is used, the text will have word wrapping
 * and be clipped to the specified dimensions.
 * 
 * @author Christopher Ruff
 */
public class MTTextArea extends MTRectangle implements IdragClusterable, ITextInputListener{
		
	/** The pa. */
	private PApplet pa;
	
	/** The character list. */
	private ArrayList<IFontCharacter> characterList;
	
	/** The font. */
	private IFont font;
	
	/** The font b box height. */
	private int fontHeight;
	
	/** The show caret. */
	private boolean showCaret;
	
	/** The show caret time. */
	private long showCaretTime = 1500; //ms
	
	/** The caret time counter. */
	private int caretTimeCounter = 0;
	
	/** The enable caret. */
	private boolean enableCaret;
	
	/** The caret width. */
	private float caretWidth;

	/** The upper left local. */
	private Vertex upperLeftLocal;

	private float innerPadding;

	
	private float totalScrollTextX;
	private float totalScrollTextY;
	
	//TODO create mode : expand vertically but do word wrap horizontally
	//TODO different font sizes in one textarea?
	
	private static final int MODE_EXPAND = 0;
	private static final int MODE_WRAP = 1;
	
	private int mode;
	
	
	/**
	 * Instantiates a new mT text area. 
	 * This constructor creates a textarea with fixed dimensions. 
	 * If the text exceeds the dimensions the text is clipped.
	 * 
	 * @param x the x
	 * @param y the y
	 * @param width the width
	 * @param height the height
	 * @param font the font
	 * @param pApplet the applet
	 */
	public MTTextArea(float x, float y, float width, float height,IFont font, PApplet pApplet) {
		super(	0, -1 * font.getFontMaxAscent(), 	//upper left corner
				width, 	//width
				height,  //height
				pApplet);
		
		init(pApplet, font, MODE_WRAP);
		
		//Position textarea at x,y
		PositionAnchor prevAnchor = this.getAnchor();
		this.setAnchor(PositionAnchor.UPPER_LEFT);
		this.setPositionGlobal(new Vector3D(x,y,0));
		this.setAnchor(prevAnchor);
	}
	
		
	/**
	 * Instantiates a new text area. This constructor creates
	 * a text area with variable dimensions that expands itself when text is added.
	 * 
	 * @param pApplet the applet
	 * @param font the font
	 */
	public MTTextArea(PApplet pApplet, IFont font) {
		super(	0, -1 * font.getFontMaxAscent(), 	//upper left corner
				0, 	//width
				0,  //height
				pApplet);
		
		init(pApplet, font, MODE_EXPAND);
		
		//Position textarea at 0,0
		PositionAnchor prevAnchor = this.getAnchor();
		this.setAnchor(PositionAnchor.UPPER_LEFT);
		this.setPositionGlobal(Vector3D.ZERO_VECTOR);
		this.setAnchor(prevAnchor);
		
		//Expand vertically at enter 
		this.setHeightLocal(this.getTotalLinesHeight());
		this.setWidthLocal(getMaxLineWidth());
	}
	
	
	
	private void init(PApplet pApplet, IFont font, int mode){
		this.pa = pApplet;
		this.font = font;
		
		this.mode = mode;
		switch (this.mode) {
		case MODE_EXPAND:
			//We dont have to clip since we expand the area
			break;
		case MODE_WRAP:
			if (MT4jSettings.getInstance().isOpenGlMode()){ 
				//Clip the text to the area
				this.setClip(new Clip(pApplet, this.getVerticesLocal()[0].x, this.getVerticesLocal()[0].y, this.getWidthXY(TransformSpace.LOCAL), this.getHeightXY(TransformSpace.LOCAL)));
			}
			break;
		default:
			break;
		}
		
		characterList = new ArrayList<IFontCharacter>();
		
		if (MT4jSettings.getInstance().isOpenGlMode())
			this.setUseDirectGL(true);
		
		fontHeight = font.getFontAbsoluteHeight();
		
		caretWidth = 0; 
		innerPadding = 5;
		
		showCaret 	= false;
		enableCaret = false;
		showCaretTime = 1000;
		
		//upper left corner of the textarea rectangle, used later when re-setting the height
		upperLeftLocal = new Vertex(0, -1 * font.getFontMaxAscent(), 0);
		
		this.setStrokeWeight(1.5f);
		this.setStrokeColor(new MTColor(255, 255, 255, 255));
		this.setDrawSmooth(true);
		
		//Draw this component and its children above 
		//everything previously drawn and avoid z-fighting
		this.setDepthBufferDisabled(true);
		
		this.totalScrollTextX = 0.0f;
		this.totalScrollTextY = 0.0f;
	}
	
	

	/* (non-Javadoc)
	 * @see com.jMT.components.MTBaseComponent#updateComponent(long)
	 */
	@Override
	public void updateComponent(long timeDelta) {
		super.updateComponent(timeDelta);
		if (enableCaret){
			caretTimeCounter+=timeDelta;
			if (caretTimeCounter >= showCaretTime && !showCaret){
				showCaret 		 = true;
				caretTimeCounter = 0;
			}else if (caretTimeCounter >= showCaretTime && showCaret){
				showCaret 		 = false;
				caretTimeCounter = 0;
			}
		}
	}
	
	
	@Override
	public void preDraw(PGraphics graphics) {
		super.preDraw(graphics);
		if (this.mode == MODE_WRAP && this.getClip() != null && !this.isNoStroke()){
			noStrokeSettingSaved = this.isNoStroke();
			this.setNoStroke(true);	
		}
	}
	
	
	/* (non-Javadoc)
	 * @see com.jMT.components.visibleComponents.shapes.MTPolygon#drawComponent()
	 */
	@Override
	public void drawComponent(PGraphics g) {
		super.drawComponent(g);
		
		//Add caret if its time 
		if (enableCaret && showCaret){
			characterList.add(this.getFont().getFontCharacterByUnicode("|"));
		}
		
		int charListSize = characterList.size();
		
		float thisLineTotalXAdvancement = 0;
		float lastXAdvancement = innerPadding;
//		/*//
		//To set caret at start pos when charlist empty
		if (enableCaret && showCaret && charListSize == 1){
			lastXAdvancement = 0;
		}
//		*/
		
		if (this.isUseDirectGL()){
			GL gl = Tools3D.beginGL(pa);
			
			gl.glPushMatrix(); //FIXME TEST
			gl.glTranslatef(totalScrollTextX, totalScrollTextY, 0);//FIXME TEST
			
			for (int i = 0; i < charListSize; i++) {
				IFontCharacter character = characterList.get(i);
				//Step to the right by the amount of the last characters x advancement
				gl.glTranslatef(lastXAdvancement, 0, 0);
				//Save total amount gone to the right in this line 
				thisLineTotalXAdvancement += lastXAdvancement;
				lastXAdvancement = 0;

				//Draw the letter
				character.drawComponent(gl);

				//Check if newLine occurs, goto start at new line
				if (character.getUnicode().equals("\n")){
					gl.glTranslatef(-thisLineTotalXAdvancement, fontHeight, 0);
					thisLineTotalXAdvancement = 0;
					lastXAdvancement = innerPadding;
				}else{
					//If caret is showing and we are at index one before caret calc the advancement to include the caret in the text area
					if (enableCaret && showCaret && i == charListSize-2){
						if (character.getUnicode().equals("\t")){
							lastXAdvancement = character.getHorizontalDist() - character.getHorizontalDist()/20;
						}else{
							//approximated value, cant get the real one
							lastXAdvancement = 2+character.getHorizontalDist() - (character.getHorizontalDist()/3);
						}
					}else{
						lastXAdvancement = character.getHorizontalDist();
					}
				}
			}
			
			gl.glPopMatrix(); //FIXME TEST
			
			Tools3D.endGL(pa);
		}
		else{ //P3D rendering
			g.pushMatrix(); //FIXME TEST
			g.translate(totalScrollTextX, totalScrollTextY, 0);//FIXME TEST
			
			for (int i = 0; i < charListSize; i++) {
				IFontCharacter character = characterList.get(i);
				//Step to the right by the amount of the last characters x advancement
				pa.translate(lastXAdvancement, 0, 0); //original
				//Save total amount gone to the right in this line
				thisLineTotalXAdvancement += lastXAdvancement;
				lastXAdvancement = 0;
				
				//Draw the letter
				character.drawComponent(g);
				
				//Check if newLine occurs, goto start at new line
				if (character.getUnicode().equals("\n")){
					pa.translate(-thisLineTotalXAdvancement, fontHeight, 0);
					thisLineTotalXAdvancement = 0;
					lastXAdvancement = innerPadding;
				}else{
					//If caret is showing and we are at index one before caret calc the advancement
					if (enableCaret && showCaret && i == charListSize-2){
						if (character.getUnicode().equals("\t")){
							lastXAdvancement = character.getHorizontalDist() - character.getHorizontalDist()/20;
						}else{
							//approximated value, cant get the real one
							lastXAdvancement = 2+character.getHorizontalDist() - (character.getHorizontalDist()/3);
						}
					}else{
						lastXAdvancement = character.getHorizontalDist();
					}
				}
			}
			g.popMatrix();
		}
		
		//remove caret
		if (enableCaret && showCaret){
			characterList.remove(charListSize-1);
		}
	}
	
	
	private boolean noStrokeSettingSaved;
	
	@Override
	public void postDraw(PGraphics graphics) {
		super.postDraw(graphics);
		if (this.mode == MODE_WRAP && this.getClip()!=null && !noStrokeSettingSaved){
			this.setNoStroke(noStrokeSettingSaved);
			boolean noFillSavedSetting = this.isNoFill();
			this.setNoFill(true);
			super.drawComponent(graphics);//Draw only stroke line after we ended clipping do preserve anti aliasing - hack
			this.setNoFill(noFillSavedSetting);
		}
	}
	
	//FIXME TEST
	protected void scrollTextX(float amount){
		this.totalScrollTextX += amount;
	}
	protected void scrollTextY(float amount){
		this.totalScrollTextY += amount;
	}
	protected float getScrollTextX() {
		return this.totalScrollTextX;
	}
	protected float getScrollTextY() {
		return this.totalScrollTextY;
	}
	
	
	//FIXME TEST ?
	/**
	 * Changes the texture filtering for the textarea's bitmap font.
	 * (if a bitmap font is used).
	 * If the parameter is "true" this will allow the text being scaled without getting
	 * too pixelated. If the text isnt going to be scaled ever, it is best to leave or
	 * set this to "false" for a sharper text.
	 * <br>NOTE: Only applies if OpenGL is the renderer and the textarea uses a bitmap font.
	 * <br>NOTE: This affects the whole bitmap font so if it is used elsewhere it is changed 
	 * there, too.
	 * 
	 * @param scalable the new bitmap font scalable
	 */
	public void setBitmapFontTextureFiltered(boolean scalable){
		if (MT4jSettings.getInstance().isOpenGlMode()){
			if (this.getFont() instanceof BitmapFont){
				BitmapFont font = (BitmapFont)this.getFont();
				IFontCharacter[] characters = font.getCharacters();
				for (int i = 0; i < characters.length; i++) {
					IFontCharacter fontCharacter = characters[i];
					if (fontCharacter instanceof BitmapFontCharacter) {
						BitmapFontCharacter bChar = (BitmapFontCharacter) fontCharacter;
						bChar.setTextureFiltered(scalable);
					}
				}
			}
		}
	}
	
	//FIXME REMOVE!?
//	private boolean filteringDone;
//	private boolean isBitmapFont;
//	
//	@Override
//	public void setMatricesDirty(boolean baseMatrixDirty) {
//		super.setMatricesDirty(baseMatrixDirty);
//		
//		if (isBitmapFont && !filteringDone){
////			filteringDone = true;
//			
//			MTComponent current = this;
//			boolean hasScale;
//			do {
//				Matrix local = current.getLocalMatrix();
//				current = current.getParent();
//			} while (current != null);
//			while (current.getParent() != null) {
//				
//				
//			}
//			
//			//TODO change fonts' filtering from NEAREST to LINEAR once after scaling is done
//		}
//	}
//	
//	private boolean checkForScaling(){
//		return checkForScalingRecursive(this);
//	}
//	
//	private MTComponent checkForScalingRecursive(MTComponent current){
//		//System.out.println("Processing: " + current.getName());
//		if (current.getParent() != null){
//			
//		}
//	}
	
	
	/**
	 * Sets the width local.
	 * 
	 * @param width the new width local
	 */
	@Override
	public void setWidthLocal(float width){
		Vertex[] v = this.getVerticesLocal();
		MTColor c = this.getFillColor();
		this.setVertices(
				new Vertex[]{
						v[0], 
						new Vertex(width, v[1].getY(), v[1].getZ(), c.getR(), c.getG(), c.getB(), c.getAlpha()), 
						new Vertex(width, v[2].getY(), v[2].getZ(), c.getR(), c.getG(), c.getB(), c.getAlpha()), 
						v[3], 
						v[4]});
	}
	/**
	 * Sets the height local.
	 * 
	 * @param height the new height local
	 */
	@Override
	public void setHeightLocal(float height){ 
		Vertex[] v = this.getVerticesLocal();
//		this.setVertices(
//				new Vertex[]{
//						new Vertex(v[2].getX(), upperLeftLocal.y + height, v[2].getZ(), this.getFillRed(), this.getFillGreen(), this.getFillBlue(), this.getFillAlpha()),
//						v[0], 
//						v[1] , 
//						new Vertex(v[2].getX(), upperLeftLocal.y + height, v[2].getZ(), this.getFillRed(), this.getFillGreen(), this.getFillBlue(), this.getFillAlpha()),
//						new Vertex(v[3].getX(), upperLeftLocal.y + height, v[3].getZ(), this.getFillRed(), this.getFillGreen(), this.getFillBlue(), this.getFillAlpha()),
//						v[4]});
		
		this.setVertices(new Vertex[]{
						new Vertex(v[0].x,	-font.getFontMaxAscent() - innerPadding, 		v[0].z, v[0].getTexCoordU(), v[0].getTexCoordV(), v[0].getR(), v[0].getG(), v[0].getB(), v[0].getA()), 
						new Vertex(v[1].x, 	-font.getFontMaxAscent() - innerPadding, 		v[1].z, v[1].getTexCoordU(), v[1].getTexCoordV(), v[1].getR(), v[1].getG(), v[1].getB(), v[1].getA()), 
						new Vertex(v[2].x, 	-font.getFontMaxAscent() - innerPadding + height + (2 * innerPadding), 	v[2].z, v[2].getTexCoordU(), v[2].getTexCoordV(), v[2].getR(), v[2].getG(), v[2].getB(), v[2].getA()), 
						new Vertex(v[3].x,	-font.getFontMaxAscent() - innerPadding + height + (2 * innerPadding),	v[3].z, v[3].getTexCoordU(), v[3].getTexCoordV(), v[3].getR(), v[3].getG(), v[3].getB(), v[3].getA()), 
						new Vertex(v[4].x,	-font.getFontMaxAscent() - innerPadding,			v[4].z, v[4].getTexCoordU(), v[4].getTexCoordV(), v[4].getR(), v[4].getG(), v[4].getB(), v[4].getA()), 
		});
	}
	
	
	
	
	/**
	 * Appends the string to the textarea.
	 * 
	 * @param string the string
	 */
	synchronized public void appendText(String string){
		for (int i = 0; i < string.length(); i++) {
			appendCharByUnicode(string.substring(i, i+1));
		}
	}
	
	/**
	 * Sets the provided string as the text of this textarea.
	 * 
	 * @param string the string
	 */
	synchronized public void setText(String string){
		clear();
		for (int i = 0; i < string.length(); i++) {
			appendCharByUnicode(string.substring(i, i+1));
		}
	}
	
	
	/* (non-Javadoc)
	 * @see mTouch.components.visibleComponents.keyboard.ITextInputAcceptor#getText()
	 */
	public String getText(){
		String returnString = "";
		for (Iterator<IFontCharacter> iter = this.characterList.iterator(); iter.hasNext();) {
			IFontCharacter character = (IFontCharacter) iter.next();
			String unicode = character.getUnicode();
			if (unicode.equalsIgnoreCase("tab")){
				returnString += "    ";
			}
//			else if (unicode.equalsIgnoreCase("\n")){
//				returnString += " ";
//			}
			else{
				returnString += unicode;
			}
		}
		return returnString;
	}
	
	
	/**
	 * Append char by name.
	 * 
	 * @param characterName the character name
	 */
	synchronized public void appendCharByName(String characterName){
		//Get the character from the font
		IFontCharacter character = font.getFontCharacterByName(characterName);
		if (character == null){
			System.err.println("Error adding character with name '" + characterName + "' to the textarea. The font couldnt find the character. -> Trying to use 'missing glyph'");
			character = font.getFontCharacterByName("missing-glyph");
			if (character != null)
				addCharacter(character);
		}else{
			addCharacter(character);
		}
	}
	
	
	
	/* (non-Javadoc)
	 * @see com.jMT.components.visibleComponents.keyboard.ITextInputAcceptor#appendCharByUnicode(java.lang.String)
	 */
	synchronized public void appendCharByUnicode(String unicode){
		//Get the character from the font
		IFontCharacter character = font.getFontCharacterByUnicode(unicode);
		if (character == null){
			System.err.println("Error adding character with unicode '" + unicode + "' to the textarea. The font couldnt find the character. ->Trying to use 'missing glyph'");
			character = font.getFontCharacterByUnicode("missing-glyph");
			if (character != null)
				addCharacter(character);
		}else{
			addCharacter(character);
		}
	}
	
	
	/**
	 * Gets the characters.
	 * @return the characters
	 */
	public IFontCharacter[] getCharacters(){
		return this.characterList.toArray(new IFontCharacter[this.characterList.size()]);
	}
	
	
	/**
	 * Adds the character.
	 * 
	 * @param character the character
	 */
	private void addCharacter(IFontCharacter character){
		this.characterList.add(character);
		
		this.characterAdded(character);
	}
	
	protected void characterAdded(IFontCharacter character){
		switch (this.mode) {
		case MODE_EXPAND:
			if (character.getUnicode().equals("\n")){
				//Expand vertically at enter 
				this.setHeightLocal(this.getTotalLinesHeight());
				//TODO make behaviour settable
				//Moves the Textarea up at a enter character instead of down 
				this.translate(new Vector3D(0, -fontHeight, 0));
			}else{
				//Expand the textbox to the extend of the widest line width
				this.setWidthLocal(getMaxLineWidth());
			}
			break;
		case MODE_WRAP:
			float localWidth = this.getWidthXY(TransformSpace.LOCAL);
//			float maxLineWidth = this.getMaxLineWidth(); //TODO last line width instead?
			float maxLineWidth = this.getLastLineWidth();
			
			if (maxLineWidth > localWidth  && this.characterList.size() > 0) {
				this.characterList.add(this.characterList.size() -1 , this.font.getFontCharacterByUnicode("\n"));
			}
			break;
		default:
			break;
		}
	}
	
	protected void characterRemoved(IFontCharacter character){
		switch (this.mode) {
		case MODE_EXPAND:
			//Resize text field
			if (character.getUnicode().equals("\n")){
				//Reduce field vertically at enter
				this.setHeightLocal(this.getTotalLinesHeight());
				//makes the textarea go down when a line is removed instead staying at the same loc.
				this.translate(new Vector3D(0, fontHeight, 0));
			}else{
				//Reduce field horizontally
				this.setWidthLocal(getMaxLineWidth());
			}
			break;
		case MODE_WRAP:
			
			break;
		default:
			break;
		}
	}
	
	/**
	 * Removes the last character in the textarea.
	 */
	synchronized public void removeLastCharacter(){
		if (this.characterList.isEmpty())
			return;
		
		//REMOVE THE CHARACTER
		IFontCharacter lastCharacter = this.characterList.get(this.characterList.size()-1);
		this.characterList.remove(this.characterList.size()-1);
		
		this.characterRemoved(lastCharacter);
	}
	
	
	/**
	 * resets the textarea, clears all characters.
	 */
	public void clear(){
		while (!characterList.isEmpty()){
			removeLastCharacter();
		}
	}
	
	
	protected float getLastLineWidth(){
		float currentLineWidth = 2 * this.getInnerPadding() + caretWidth;
		for (int i = 0; i < this.characterList.size(); i++) {
			IFontCharacter character = this.characterList.get(i);
			if (character.getUnicode().equals("\n")){
				currentLineWidth = 2 * this.getInnerPadding() + caretWidth;; 
			}else{
				currentLineWidth += character.getHorizontalDist();
			}
		}
		return currentLineWidth;
	}
	
	
	/**
	 * Gets the max line width. The padding is also added.
	 * 
	 * @return the max line width
	 */
	protected float getMaxLineWidth(){
		float currentLineWidth = 2 * this.getInnerPadding() + caretWidth;
		float maxWidth = currentLineWidth;
		
		for (int i = 0; i < this.characterList.size(); i++) {
			IFontCharacter character = this.characterList.get(i);
			
			if (character.getUnicode().equals("\n")){
				if (currentLineWidth > maxWidth){
					maxWidth = currentLineWidth;
				}
				currentLineWidth = 2 * this.getInnerPadding() + caretWidth;
			}else{
				currentLineWidth += character.getHorizontalDist();
				if (currentLineWidth > maxWidth){
					maxWidth = currentLineWidth;
				}
			}
		}
		return maxWidth;
	}

	
	/**
	 * Gets the total lines height. Padding is not included
	 * 
	 * @return the total lines height
	 */
	protected float getTotalLinesHeight(){
		float height = font.getFontAbsoluteHeight() ;//
		for (int i = 0; i < this.characterList.size(); i++) {
			IFontCharacter character = this.characterList.get(i);
			if (character.getUnicode().equals("\n")){
				height += fontHeight;
			}
		}
		return height;
	}
	
	
	public float getInnerPadding(){
		return this.innerPadding;
	}
	
	public void setInnerPadding(float innerPadding){
		this.innerPadding = innerPadding;
	}
	


	/**
	 * Gets the line count.
	 * 
	 * @return the line count
	 */
	public int getLineCount(){
		int count = 0;
		for (int i = 0; i < this.characterList.size(); i++) {
			IFontCharacter character = this.characterList.get(i);
			if (character.getUnicode().equals("\n")){
				count++;
			}
		}
		return count;
	}
	
	
	
	/**
	 * Gets the font.
	 * 
	 * @return the font
	 */
	public IFont getFont() {
		return font;
	}

	/**
	 * Snap to keyboard.
	 * 
	 * @param mtKeyboard the mt keyboard
	 */
	public void snapToKeyboard(MTKeyboard mtKeyboard){
		//OLD WAY
//		this.translate(new Vector3D(30, -(getFont().getFontAbsoluteHeight() * (getLineCount())) + getFont().getFontMaxDescent() - borderHeight, 0));
		mtKeyboard.addChild(this);
		this.setPositionRelativeToParent(new Vector3D(40, -this.getHeightXY(TransformSpace.LOCAL)*0.5f));
	}


	/* (non-Javadoc)
	 * @see com.jMT.input.inputAnalyzers.clusterInputAnalyzer.IdragClusterable#isSelected()
	 */
	public boolean isSelected() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.jMT.input.inputAnalyzers.clusterInputAnalyzer.IdragClusterable#setSelected(boolean)
	 */
	public void setSelected(boolean selected) {
		// TODO Auto-generated method stub
		
	}


	/**
	 * Checks if is enable caret.
	 * 
	 * @return true, if is enable caret
	 */
	public boolean isEnableCaret() {
		return enableCaret;
	}


	/**
	 * Sets the enable caret.
	 * 
	 * @param enableCaret the new enable caret
	 */
	public void setEnableCaret(boolean enableCaret) {
		if (this.getFont().getFontCharacterByUnicode("|") != null){
			this.enableCaret = enableCaret;
			
			if (enableCaret){
				this.caretWidth = 10;
			}else{
				this.caretWidth = 0;
			}
			
			if (this.mode == MODE_EXPAND){
				this.setWidthLocal(this.getMaxLineWidth());
			}
		}else{
			System.err.println("Cant enable caret for this textfield, the font doesent include the letter '|'");
		}
	}



}
