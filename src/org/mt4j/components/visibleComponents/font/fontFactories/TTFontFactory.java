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
package org.mt4j.components.visibleComponents.font.fontFactories;

import java.awt.Toolkit;
import java.util.ArrayList;

import org.apache.batik.svggen.font.Font;
import org.apache.batik.svggen.font.Glyph;
import org.apache.batik.svggen.font.Point;
import org.apache.batik.svggen.font.table.CmapFormat;
import org.apache.batik.svggen.font.table.Table;
import org.mt4j.components.visibleComponents.font.VectorFont;
import org.mt4j.components.visibleComponents.font.VectorFontCharacter;
import org.mt4j.util.MT4jSettings;
import org.mt4j.util.MTColor;
import org.mt4j.util.UnitTranslator;
import org.mt4j.util.math.Vector3D;
import org.mt4j.util.math.Vertex;
import org.mt4j.util.xml.svg.CustomPathHandler;

import processing.core.PApplet;

/**
 * A factory for creating vector font objects.
 * @author Christopher Ruff
 */
public class TTFontFactory implements IFontFactory{
	
//	//Register the factory
//	static{
//		//FIXME not working!!? never gets called
//		FontManager.getInstance().registerFontFactory(".ttf", new TTFontFactory());
//	}
	
	private PApplet pa;
	
	private Font f;
	private float scaleFactor;
	private short unitsPerEm;
	private int fontDefaultXAdvancing; 
	private int fontSize; 
	private String fontPath;
	

	public TTFontFactory(){
//		this.fontPath = fontPath;
//		this.f = Font.create(fontPath);
//		this.setSize(50);
//		this.fontDefaultXAdvancing 	= 500; 
//		this.fontSize 				= 50; 
//		this.unitsPerEm = f.getHeadTable().getUnitsPerEm();
	}

	public VectorFont createFont(PApplet pa, String svgFontFileName){
		return this.createFont(pa, svgFontFileName, 50, new MTColor(0,0,0,255), new MTColor(0,0,0,255));
	}
			

	
	/* (non-Javadoc)
	 * @see mTouch.components.visibleComponents.font.IFontFactory#loadFont(processing.core.PApplet, java.lang.String, int, float, float, float, float, float, float, float, float)
	 */
	public VectorFont createFont(
			PApplet pa, 
			String svgFontFileName, 
			int fontSize, 
			MTColor fillColor, 
			MTColor strokeColor)
	{
		//INITIAL SETTINGS
		this.pa 					= pa;
		this.fontPath 				= svgFontFileName;
		this.f 						= Font.create(fontPath);
		this.setSize(fontSize);
		this.fontDefaultXAdvancing 	= 500; //TODO WOHER KRIEGEN?
		this.fontSize 				= fontSize; 
		this.unitsPerEm 			= f.getHeadTable().getUnitsPerEm();
		///
		
		//Get the desired fontsize scaling factor 
		short unitsPerEm = f.getHeadTable().getUnitsPerEm();
		int resolution = Toolkit.getDefaultToolkit().getScreenResolution();
		//System.out.println("Screen resolution: " + resolution);
		
		//This calculates the font in "pt" point size (used in windows)
		this.scaleFactor = ((float)fontSize * (float)resolution) / (72F * (float)unitsPerEm); //original
		
		//FIXME TEST
		float test = UnitTranslator.pointsToPixels(scaleFactor*fontSize, Math.round(resolution));
//		test = Math.round((float)fontSize/(float)unitsPerEm);
		//This calculates the font size in..pixels? at least same as in svg
		test = (float)(1.0/(float)this.unitsPerEm) * fontSize;
		this.scaleFactor = test;
		
		//System.out.println("->Scalefactor: " + this.scaleFactor);
		
		//CREATE FONT CHARACTERS
		VectorFontCharacter[] chars = this.getTTFCharacters(f, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÜabcdefghijklmnopqrstuvwxyzäöü<>|,;.:-_#'+*!\"§$%&/()=?´~²³{[]}\\^°@ß"
														,fillColor, strokeColor);

		VectorFontCharacter[] newArray = new VectorFontCharacter[chars.length+3];
		System.arraycopy(chars, 0, newArray, 0, chars.length);
		
		//Manually add a NEWLINE character to the font
		Vertex[] nlVerts = new Vertex[]{new Vertex(0,0,0), new Vertex(200,0,0),new Vertex(200,100,0),/*new Vertex(0,100,0)*/};
		ArrayList<Vertex[]> nlContours = new ArrayList<Vertex[]>();
		nlContours.add(nlVerts);
		VectorFontCharacter newLine = new VectorFontCharacter(nlContours, pa);
		newLine.setPickable(false);						    		
		newLine.setVisible(false);
		newLine.setNoFill(true);
		newLine.setNoStroke(true);
		newLine.setHorizontalDist(0);
		newLine.setUnicode("\n");
		newLine.setName("newline");
		newArray[newArray.length-3] = newLine;
		
		//Manually add a SPACE character to the font
		int charIndex 			= this.getCmapFormat(f).mapCharCode(32);
		int default_advance_x 	= f.getHmtxTable().getAdvanceWidth(charIndex);
		Glyph glyph  			= f.getGlyph(charIndex);
		int xadvance = 0;
		if (glyph != null){
//			xadvance = Math.round((default_advance_x * (float)(1.0/(float)this.unitsPerEm)) * fontSize);
			xadvance = Math.round(default_advance_x * this.scaleFactor);
		}else{
//			xadvance = Math.round((fontDefaultXAdvancing * (float)(1.0/(float)this.unitsPerEm)) * fontSize);
			xadvance = Math.round(fontDefaultXAdvancing * this.scaleFactor);
		}
		
		Vertex[] spaceVerts = new Vertex[]{new Vertex(0,0,0), new Vertex(xadvance,0,0),new Vertex(xadvance,100,0), /*new Vertex(0,100,0)*/};
		ArrayList<Vertex[]> spaceContours = new ArrayList<Vertex[]>();
		spaceContours.add(spaceVerts);
		VectorFontCharacter space = new VectorFontCharacter(spaceContours, pa);
		space.setPickable(false);						    		
		space.setVisible(false);
		space.setNoFill(true);
		space.setNoStroke(true);
		space.setHorizontalDist(xadvance);
		space.setUnicode(" ");
		space.setName("space");
		newArray[newArray.length-2] = space;

		//Manually add a TAB character to the font
		int defaultTabWidth = fontDefaultXAdvancing*4;
		Vertex[] tabVerts = new Vertex[]{new Vertex(0,0,0), new Vertex(200,0,0),new Vertex(200,100,0),/*new Vertex(0,100,0)*/};
		ArrayList<Vertex[]> tabContours = new ArrayList<Vertex[]>();
		tabContours.add(tabVerts);
		VectorFontCharacter tab = new VectorFontCharacter(tabContours, pa);
		tab.setPickable(false);
		try {
			int tabWidth = 4 * space.getHorizontalDist();
			tab.setHorizontalDist(tabWidth);
			tab.setVertices(new Vertex[]{new Vertex(0,0,0), new Vertex(tabWidth,0,0),new Vertex(tabWidth,100,0),/*new Vertex(0,100,0)*/} );
		} catch (Exception e) {
			tab.setHorizontalDist(defaultTabWidth);
		}
		tab.setUnicode("\t");
		tab.setName("tab");
		tab.setVisible(false);
		tab.setNoFill(true);
		tab.setNoStroke(true);
		newArray[newArray.length-1] = tab;
		////////

		int fontMaxAscent 	= f.getAscent();
		int fontMaxDescent	= f.getDescent();
		
		//Set font max descent and ascent according to font size
//		float tmp = fontMaxAscent * (float)(1.0/(float)this.unitsPerEm);
//		fontMaxAscent = Math.round(tmp * fontSize);
//		float tmp2 = fontMaxDescent * (float)(1.0/(float)this.unitsPerEm);
//		fontMaxDescent = Math.round(tmp2 * fontSize);
		fontMaxAscent = Math.round(fontMaxAscent * this.scaleFactor);
		fontMaxDescent = Math.round(fontMaxDescent * this.scaleFactor);

		//Create Font
		VectorFont svgFont = new VectorFont(newArray, fontDefaultXAdvancing, this.getFamily(f), fontMaxAscent, fontMaxDescent,this.unitsPerEm,  fontSize,
				fillColor,
				strokeColor);
		svgFont.setFontFileName(fontPath);
		svgFont.setFontId("-1");
		
		return svgFont;
	}

	
	
	/**
	 * 
	 * @return
	 */
	/*
	public SvgFont getFont(){
		SvgFontCharacter[] chars = this.getTTFCharacters("123456789ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÜabcdefghijklmnopqrstuvwxyzäöü<>|,;.:-_#'+*!\"§$%&/()=?´´~#²³456{[]}\\^°@ß");

		SvgFontCharacter[] newArray = new SvgFontCharacter[chars.length+3];
		System.arraycopy(chars, 0, newArray, 0, chars.length);
		
		//Manually add a NEWLINE character to the font
		SvgFontCharacter newLine = new SvgFontCharacter(new Vertex[]{new Vertex(0,0,0), new Vertex(200,0,0),new Vertex(200,100,0),new Vertex(0,100,0)}, new ArrayList<Vertex[]>(), pa);
		newLine.setPickable(false);						    		
		newLine.setVisible(false);
		newLine.setHorizontalDist(0);
		newLine.setUnicode("\n");
		newLine.setName("newline");
		newArray[newArray.length-3] = newLine;
		
		//Manually add a SPACE character to the font
		int charIndex 			= this.getCmapFormat(f).mapCharCode(32);
		int default_advance_x 	= f.getHmtxTable().getAdvanceWidth(charIndex);
		Glyph glyph  			= f.getGlyph(charIndex);
		
		int xadvance = 0;
		if (glyph != null){
			xadvance = Math.round((default_advance_x * (float)(1.0/(float)this.unitsPerEm)) * fontSize);
		}else{
			xadvance = Math.round((fontDefaultXAdvancing * (float)(1.0/(float)this.unitsPerEm)) * fontSize);
		}
		SvgFontCharacter space = new SvgFontCharacter(new Vertex[]{new Vertex(0,0,0), new Vertex(xadvance,0,0),new Vertex(xadvance,100,0),new Vertex(0,100,0)}, new ArrayList<Vertex[]>(), pa);
		space.setPickable(false);						    		
		space.setVisible(false);
		space.setHorizontalDist(xadvance);
		space.setUnicode(" ");
		space.setName("space");
		newArray[newArray.length-2] = space;
		System.out.println("Advance of character space " + charIndex + " :" + default_advance_x);

		//Manually add a TAB character to the font
		int defaultTabWidth = fontDefaultXAdvancing*4;
		SvgFontCharacter tab = new SvgFontCharacter(new Vertex[]{new Vertex(0,0,0), new Vertex(defaultTabWidth,0,0),new Vertex(defaultTabWidth,100,0),new Vertex(0,100,0)}, new ArrayList<Vertex[]>(), pa);
		tab.setPickable(false);
		try {
			int tabWidth = 4*space.getHorizontalDist();
			tab.setHorizontalDist(tabWidth);
			tab.setVerticesLocal(new Vertex[]{new Vertex(0,0,0), new Vertex(tabWidth,0,0),new Vertex(tabWidth,100,0),new Vertex(0,100,0)} );
			tab.setUseBoundingPickRect();
		} catch (Exception e) {
			tab.setHorizontalDist(defaultTabWidth);
		}
		tab.setUnicode("tab");
		tab.setName("tab");
		tab.setVisible(false);
		newArray[newArray.length-1] = tab;
		////////

		int fontMaxAscent 	= f.getAscent();
		int fontMaxDescent	= f.getDescent();
		
		//Set font max descent and ascent according to font size
		float tmp = fontMaxAscent * (float)(1.0/(float)this.unitsPerEm);
		fontMaxAscent = Math.round(tmp * fontSize);
		float tmp2 = fontMaxDescent * (float)(1.0/(float)this.unitsPerEm);
		fontMaxDescent = Math.round(tmp2 * fontSize);

		//Create Font
		SvgFont svgFont = new SvgFont(newArray, fontDefaultXAdvancing, this.getFamily(), fontMaxAscent, fontMaxDescent,this.unitsPerEm,  fontSize);
		svgFont.setFontFileName(fontPath);
		svgFont.setFontId("-1");
		
		return svgFont;
	}
	 */
	
	

	private VectorFontCharacter[] getTTFCharacters(Font f, String text,
			MTColor fillColor, 
			MTColor strokeColor)  
	throws RuntimeException{
		
		// Decide upon a cmap table to use for our character to glyph look-up
		CmapFormat cmapFmt = this.getCmapFormat(f);
		if (cmapFmt == null) {
			throw new RuntimeException("Cannot find a suitable cmap table");
		}

		// If this font includes arabic script, we want to specify
		// substitutions for initial, medial, terminal & isolated
		// cases.
		/*
	      GsubTable gsub = (GsubTable) f.getTable(Table.GSUB);
	      SingleSubst initialSubst = null;
	      SingleSubst medialSubst = null;
	      SingleSubst terminalSubst = null;
	      if (gsub != null) {
	      Script s = gsub.getScriptList().findScript(ScriptTags.SCRIPT_TAG_ARAB);
	      if (s != null) {
	      LangSys ls = s.getDefaultLangSys();
	      if (ls != null) {
	      Feature init = gsub.getFeatureList().findFeature(ls, FeatureTags.FEATURE_TAG_INIT);
	      Feature medi = gsub.getFeatureList().findFeature(ls, FeatureTags.FEATURE_TAG_MEDI);
	      Feature fina = gsub.getFeatureList().findFeature(ls, FeatureTags.FEATURE_TAG_FINA);

	      initialSubst = (SingleSubst)
	      gsub.getLookupList().getLookup(init, 0).getSubtable(0);
	      medialSubst = (SingleSubst)
	      gsub.getLookupList().getLookup(medi, 0).getSubtable(0);
	      terminalSubst = (SingleSubst)
	      gsub.getLookupList().getLookup(fina, 0).getSubtable(0);
	      }
	      }
	      }*/

		ArrayList<VectorFontCharacter> characters = new ArrayList<VectorFontCharacter>();

		int x = 0;
		for (short i = 0; i < text.length(); i++) {
			int glyphIndex 			= cmapFmt.mapCharCode(text.charAt(i));
			Glyph glyph 			= f.getGlyph(glyphIndex);
			int default_advance_x 	= f.getHmtxTable().getAdvanceWidth(glyphIndex);

			if (glyph != null) {
//				glyph.scale(Math.round(scaleFactor)); //Scaling has changed to int!?
				// Add the Glyph to the Shape with an horizontal offset of x
				VectorFontCharacter fontChar = getGlyphAsShape(f, glyph, glyphIndex, x, fillColor, strokeColor);

				if (fontChar != null){
					//Sets characters horizontal advancement and unicode value
					fontChar.setHorizontalDist(default_advance_x);

//					float tmp = fontChar.getHorizontalDist() * (float)(1.0/(float)this.unitsPerEm);
//					fontChar.setHorizontalDist(Math.round(tmp * fontSize));
					
					fontChar.setHorizontalDist(Math.round(fontChar.getHorizontalDist() * this.scaleFactor)); //FIXME TRIAL
					
					fontChar.setUnicode(Character.toString(text.charAt(i)));
					fontChar.setName(Character.toString(text.charAt(i)));

					characters.add(fontChar);
				}

				x += glyph.getAdvanceWidth();
			}else{
				System.err.println("Couldnt find character: \"" + text.charAt(i) + "\" in " + fontPath);
				x += (int)((float)default_advance_x /* *scaleFactor */);
			}

		}
		VectorFontCharacter[] chars = characters.toArray(new VectorFontCharacter[characters.size()]);
		return chars;
	}
	
	
	/**
	 * 
	 * @param font
	 * @param glyph
	 * @param glyphIndex
	 * @param xadv
	 * @return
	 */
	private VectorFontCharacter getGlyphAsShape(
			Font font, 
			Glyph glyph, 
			int glyphIndex,
			float xadv,
			MTColor fillColor, 
			MTColor strokeColor
	) {
		int firstIndex = 0;
		int count = 0;
		int i;

		ArrayList<Vertex[]> allContours = new ArrayList<Vertex[]>();

		if (glyph != null) {
			for (i = 0; i < glyph.getPointCount(); i++) {
				count++;
				if (glyph.getPoint(i).endOfContour) {
					Vertex[] contour = getContourAsShape(glyph, firstIndex, count, xadv);
					if (contour != null){
						allContours.add(contour);
					}
					firstIndex = i + 1;
					count = 0;
				}
			}
		}

		if (!allContours.isEmpty()){
			
			for(Vertex[] contour: allContours){
				Vertex.xRotateVectorArray(contour, new Vector3D((float)(xadv/2.0),0,0), 180);
//				Vertex.scaleVectorArray(contour, new Vector3D(0,0,0), (float)(1.0/(float)this.unitsPerEm));
//				Vertex.scaleVectorArray(contour, new Vector3D(0,0,0), fontSize);
				Vertex.scaleVectorArray(contour, new Vector3D(0,0,0), this.scaleFactor);
			}
			
			VectorFontCharacter character = new VectorFontCharacter(allContours,  pa);
			
			if (MT4jSettings.getInstance().isOpenGlMode())
				character.setUseDirectGL(true);
			
			character.setStrokeWeight(0.7f);
			character.setPickable(false);
			character.setNoStroke(false);

//			for(Vertex[] contour: character.getContours()){
//				Vertex.xRotateVectorArray(contour, new Vector3D((float)(xadv/2.0),0,0), 180);
//				Vertex.scaleVectorArray(contour, new Vector3D(0,0,0), (float)(1.0/(float)this.unitsPerEm));
//				Vertex.scaleVectorArray(contour, new Vector3D(0,0,0), fontSize);
//			}

//			Vertex[] v = character.getGeometryInfo().getVertices();
//			Vertex.xRotateVectorArray(v, new Vector3D((float)(xadv/2.0),0,0), 180);
//			Vertex.scaleVectorArray(v, new Vector3D(0,0,0), (float)(1.0/(float)this.unitsPerEm));
//			Vertex.scaleVectorArray(v, new Vector3D(0,0,0), fontSize);
//			character.setVertices(v);
			
			/*
		  character.xRotate(new Vector3D((float)(xadv/2.0),0,0), 180);
		  character.scale((float)(1.0/(float)this.unitsPerEm), new Vector3D(0,0,0));
		  character.scale(fontSize , new Vector3D(0,0,0));
			 */

			character.setStrokeColor(new MTColor(strokeColor.getR(), strokeColor.getG(), strokeColor.getB(), strokeColor.getAlpha()));
			character.setFillColor(new MTColor(fillColor.getR(), fillColor.getG(), fillColor.getB(), fillColor.getAlpha()));
			
			if (MT4jSettings.getInstance().isOpenGlMode())
				character.generateAndUseDisplayLists();
			
			return character;
		}else{
			return null;
		}
	}



	/**
	 * 
	 * @param glyph
	 * @param startIndex
	 * @param count
	 * @param xadv
	 * @return
	 */
	private Vertex[] getContourAsShape(Glyph glyph, int startIndex, int count, float xadv) {
		// If this is a single point on it's own, weSystem.out.println("Value of pointx: "+pointx); can't do anything with it
		if (glyph.getPoint(startIndex).endOfContour) {
			return null;
		}

		//System.out.println("Leftsidebear " + glyph.getLeftSideBearing());
		//System.out.println("Advx " + glyph.getAdvanceWidth());
		
		int offset = 0;
		//float originx = 0F,originy = 0F;

		CustomPathHandler pathHandler = new CustomPathHandler();

		while (offset < count) {
			Point point 		= glyph.getPoint(startIndex + offset%count);
			Point point_plus1 	= glyph.getPoint(startIndex + (offset+1)%count);
			Point point_plus2 	= glyph.getPoint(startIndex + (offset+2)%count);

			float pointx = ((float)point.x);
			float pointy = ((float)point.y);
			float point_plus1x = ((float)point_plus1.x);
			float point_plus1y = ((float)point_plus1.y);
			float point_plus2x = ((float)point_plus2.x);
			float point_plus2y = ((float)point_plus2.y);

			if (offset == 0) {
				pathHandler.movetoAbs(pointx,pointy);
			}

			if (point.onCurve && point_plus1.onCurve) {
				// line command
				pathHandler.linetoAbs(point_plus1x,point_plus1y);
				offset++;
			} else if (point.onCurve && !point_plus1.onCurve && point_plus2.onCurve) {
				// This is a curve with no implied points
				// quadratic bezier command
				pathHandler.curvetoQuadraticAbs(point_plus1x, point_plus1y, point_plus2x, point_plus2y);
				offset+=2;
			} else if (point.onCurve && !point_plus1.onCurve && !point_plus2.onCurve) {
				// This is a curve with one implied point
				pathHandler.curvetoQuadraticAbs(point_plus1x, point_plus1y, midValue(point_plus1x, point_plus2x), midValue(point_plus1y, point_plus2y));
				offset+=2;
			} else if (!point.onCurve && !point_plus1.onCurve) {
				// This is a curve with two implied points
				// quadratic bezier with
				pathHandler.curvetoQuadraticAbs(pointx, pointy, midValue(pointx, point_plus1x), midValue(pointy, point_plus1y));
				offset++;
			} else if (!point.onCurve && point_plus1.onCurve) {
				// This is a curve with no implied points
				pathHandler.curvetoQuadraticAbs(pointx, pointy, point_plus1x, point_plus1y);
				offset++;
			} else {
				System.out.println("drawGlyph case not catered for!!");
				break;
			}
		}

		pathHandler.closePath();


		Vertex[] p = pathHandler.getPathPointsArray();

		/*
		  //Get Sub-Paths
		  ArrayList<Vertex[]> subPaths = pathHandler.getContours();

		  System.out.println("Pathpoints array:");
		  for (int i = 0; i < p.length; i++) {
			  Vertex vertex = p[i];
			  System.out.println(vertex);
		  }

		  System.out.println("Subpaths");
		  for(Vertex[] subPath : subPaths)
			  for (int i = 0; i < subPath.length; i++) {
				Vertex vertex = subPath[i];
				System.out.println(vertex);
			}
		 */
		return p;
	}

	
	/**
	 * Gets the vectorFontCharacter by charcode.
	 * 
	 * @param charCodeMapIndex the char code map index
	 * @param pa the pa
	 * @param fontFile the font file
	 * 
	 * @return the char charcode
	 */
	public VectorFontCharacter getCharCharcode(PApplet pa, String fontFile, int charCodeMapIndex){
		this.pa = pa;
		this.fontPath = fontFile;
		this.f = Font.create(fontPath);
		this.setSize(fontSize);
		this.fontDefaultXAdvancing 	= 500; 
		this.fontSize 				= 50; 
		this.unitsPerEm = f.getHeadTable().getUnitsPerEm();
		CmapFormat cmapFmt = this.getCmapFormat(f);
		
		int charIndex = cmapFmt.mapCharCode(charCodeMapIndex);
		int default_advance_x 	= f.getHmtxTable().getAdvanceWidth(charIndex);
		
		Glyph glyph  = f.getGlyph(charIndex);
		if (glyph != null){
			VectorFontCharacter character = this.getGlyphAsShape(f, glyph, charIndex, 0, new MTColor(0,0,0,255), new MTColor(0,0,0,255));
			if (character != null){
				character.setHorizontalDist(default_advance_x);
				return character;
			}else{
				return null;
			}
		}else{
			return null;
		}
	}
	
	
	
	/**
	 * 
	 * @param f
	 * @return
	 */
	private CmapFormat getCmapFormat(Font f){
		//Decide upon a cmap table to use for our character to glyph look-up
		CmapFormat cmapFmt = null;
		boolean forceAscii = false;
		if (forceAscii) {
			// We've been asked to use the ASCII/Macintosh cmap format
			cmapFmt = f.getCmapTable().getCmapFormat(
					Table.platformMacintosh,
					Table.encodingRoman );

			//System.out.println("Forced ASCII.  Loading ASCII/Macintosh cmap format...");
		} else {
			// The default behaviour is to use the Unicode cmap encoding
			cmapFmt = f.getCmapTable().getCmapFormat(
					Table.platformMicrosoft,
					Table.encodingUGL );

			if(cmapFmt != null) {
//				System.out.println("Loading Unicode cmap format...");
			}

			if (cmapFmt == null){
				cmapFmt = f.getCmapTable().getCmapFormat(
						Table.platformMicrosoft,
						Table.encodingKorean );

				if(cmapFmt != null) {
//					System.out.println("Loading Microsoft/Korean cmap format...");
				}
			}
			if (cmapFmt == null){
				cmapFmt = f.getCmapTable().getCmapFormat(
						Table.platformMicrosoft,
						Table.encodingHebrew );

				if(cmapFmt != null) {
//					System.out.println("Loading Microsoft/Hebrew cmap format...");
				}
			}
			if (cmapFmt == null) {
				// This might be a symbol font, so we'll look for an "undefined" encoding
				cmapFmt = f.getCmapTable().getCmapFormat(
						Table.platformMicrosoft,
						Table.encodingUndefined );

				if(cmapFmt != null) {
//					System.out.println("Loading Undefined cmap format...");
				}
			}
		}
		return cmapFmt;
	}
	
	

	private static float midValue(float a, float b) {
		return a + (b - a)/2;
	}


	/**
	 * Use this method to reset the point size of the font.
	 * @eexample setSize
	 * @param size int, the point size of the font in points.
	 * @related size
	 * @related RFont
	 */
	private void setSize(int size){
		int resolution = Toolkit.getDefaultToolkit().getScreenResolution();
		this.scaleFactor = ((float)size * (float)resolution) / (72F * (float)unitsPerEm);
		//this.scaleFactorFixed = (int)(this.scaleFactor * 65536F);
		//System.out.println(scaleFactor);
		//System.out.println(scaleFactorFixed);
	}


	/**
	 * @invisible
	 **/
	private String getFamily(Font f){
		return f.getNameTable().getRecord(Table.nameFontFamilyName);
	}


}
