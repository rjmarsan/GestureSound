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
package org.mt4j.components.visibleComponents.font;

import java.util.HashMap;

import org.mt4j.util.MTColor;

/**
 * The Class BitmapFont.
 * @author Christopher Ruff
 */
public class BitmapFont implements IFont {
	
	/** The characters. */
	private BitmapFontCharacter[] characters;
	
	/** The default horizontal adv x. */
	private int defaultHorizontalAdvX;
	
	/** The font family. */
	private String fontFamily;
	
	/** The original font size. */
	private int originalFontSize;
	
	/** The font max ascent. */
	private int fontMaxAscent;
	
	/** The font max descent. */
	private int fontMaxDescent;
	
	/** The units per em. */
	private int unitsPerEM;
	
	/** The font file name. */
	private String fontFileName;
	
	/** The uni code to char. */
	private HashMap<String, BitmapFontCharacter> uniCodeToChar;
	
	/** The char name to char. */
	private HashMap<String, BitmapFontCharacter> charNameToChar;
	
	/** The fill color. */
	private MTColor fillColor;
	
	/** The stroke color. */
	private MTColor strokeColor;
	
	
	/**
	 * Instantiates a new bitmap font.
	 * 
	 * @param characters the characters
	 * @param defaultHorizontalAdvX the default horizontal adv x
	 * @param fontFamily the font family
	 * @param fontMaxAscent the font max ascent
	 * @param fontMaxDescent the font max descent
	 * @param unitsPerEm the units per em
	 * @param originalFontSize the original font size
	 * @param fillColor the fill color
	 * @param strokeColor the stroke color
	 */
	public BitmapFont(BitmapFontCharacter[] characters, int defaultHorizontalAdvX, String fontFamily, int fontMaxAscent, int fontMaxDescent, int unitsPerEm, int originalFontSize,
			MTColor fillColor,
			MTColor strokeColor) {
		this.characters = characters;
		this.defaultHorizontalAdvX = defaultHorizontalAdvX;
		this.fontFamily = fontFamily;
		this.originalFontSize = originalFontSize;
		this.fillColor = fillColor;
		this.strokeColor = strokeColor;
		
//		this.fontId = "";
		
		this.fontMaxAscent 	= fontMaxAscent;
		this.fontMaxDescent = fontMaxDescent;
		
		this.unitsPerEM = unitsPerEm;
		
		//Put characters in hashmaps for quick access
		uniCodeToChar 	= new HashMap<String, BitmapFontCharacter>();
		charNameToChar 	= new HashMap<String, BitmapFontCharacter>();
		
		for (int i = 0; i < characters.length; i++) {
			BitmapFontCharacter currentChar = characters[i];
			uniCodeToChar.put(currentChar.getUnicode(), currentChar);
			charNameToChar.put(currentChar.getName(), currentChar);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see mTouch.components.visibleComponents.font.IVectorFont#getFontCharacterByName(java.lang.String)
	 */
	//@Override
	public IFontCharacter getFontCharacterByName(String characterName){
		BitmapFontCharacter returnChar = charNameToChar.get(characterName);
		if (returnChar == null)
			System.err.println("Font couldnt load charactername: " + characterName);
		return returnChar;
	}
	
	
	
	/* (non-Javadoc)
	 * @see mTouch.components.visibleComponents.font.IVectorFont#getFontCharacterByUnicode(java.lang.String)
	 */
	//@Override
	public IFontCharacter getFontCharacterByUnicode(String unicode){
		BitmapFontCharacter returnChar = uniCodeToChar.get(unicode);
		if (returnChar == null)
			System.err.println("Font couldnt load characterunicode: " + unicode);
		return returnChar;
	}

	/* (non-Javadoc)
	 * @see org.mt4j.components.visibleComponents.font.IFont#getCharacters()
	 */
	//@Override
	public IFontCharacter[] getCharacters() {
		return this.characters;
	}

	/* (non-Javadoc)
	 * @see org.mt4j.components.visibleComponents.font.IFont#getDefaultHorizontalAdvX()
	 */
	//@Override
	public int getDefaultHorizontalAdvX() {
		return this.defaultHorizontalAdvX;
	}

	/* (non-Javadoc)
	 * @see org.mt4j.components.visibleComponents.font.IFont#getFontAbsoluteHeight()
	 */
	//@Override
	public int getFontAbsoluteHeight() {
		return ((Math.abs(this.getFontMaxAscent())) + (Math.abs(this.getFontMaxDescent())));
	}

	/* (non-Javadoc)
	 * @see org.mt4j.components.visibleComponents.font.IFont#getFontFamily()
	 */
	//@Override
	public String getFontFamily() {
		return this.fontFamily;
	}
	
	/**
	 * Sets the font file name.
	 * 
	 * @param fileName the new font file name
	 */
	public void setFontFileName(String fileName){
		this.fontFileName = fileName;
	}

	/* (non-Javadoc)
	 * @see org.mt4j.components.visibleComponents.font.IFont#getFontFileName()
	 */
	//@Override
	public String getFontFileName() {
		return this.fontFileName;
	}

	/* (non-Javadoc)
	 * @see org.mt4j.components.visibleComponents.font.IFont#getFontMaxAscent()
	 */
	//@Override
	public int getFontMaxAscent() {
		return this.fontMaxAscent;
	}

	/* (non-Javadoc)
	 * @see org.mt4j.components.visibleComponents.font.IFont#getFontMaxDescent()
	 */
	//@Override
	public int getFontMaxDescent() {
		return this.fontMaxDescent;
	}

	/* (non-Javadoc)
	 * @see org.mt4j.components.visibleComponents.font.IFont#getOriginalFontSize()
	 */
	//@Override
	public int getOriginalFontSize() {
		return this.originalFontSize;
	}

	/* (non-Javadoc)
	 * @see org.mt4j.components.visibleComponents.font.IFont#getUnitsPerEM()
	 */
	//@Override
	public int getUnitsPerEM() {
		return this.unitsPerEM;
	}

	/* (non-Javadoc)
	 * @see org.mt4j.components.visibleComponents.font.IFont#getFillColor()
	 */
	//@Override
	public MTColor getFillColor() {
		return fillColor;
	}

	/* (non-Javadoc)
	 * @see org.mt4j.components.visibleComponents.font.IFont#getStrokeColor()
	 */
	//@Override
	public MTColor getStrokeColor() {
		return strokeColor;
	}

	
	
	
	

}
