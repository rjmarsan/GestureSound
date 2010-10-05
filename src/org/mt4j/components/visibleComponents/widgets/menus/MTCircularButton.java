package org.mt4j.components.visibleComponents.widgets.menus;

import org.mt4j.components.visibleComponents.font.FontManager;
import org.mt4j.components.visibleComponents.font.IFont;
import org.mt4j.components.visibleComponents.shapes.MTRectangle.PositionAnchor;
import org.mt4j.components.visibleComponents.widgets.MTTextArea;
import org.mt4j.util.MTColor;
import org.mt4j.util.math.Vector3D;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * This class is an extension of MTCircularItem that incorperates Text as well as Icons, etc.
 * @author rj
 *
 */
public class MTCircularButton extends MTCircularItem {
	
	public static MTColor TRANSPARENT = new MTColor(0,0,0,0);
	MTCircularComponents itemsArea;
	float textPaddingFromCenter = 20f;
	float textPaddingArc = 10f;
	float textLastAngle = 0.0f;
	MTColor textColor = new MTColor(0,0,0);


	/**
	 * Constructors, see superclass
	 * @param pa
	 */
	public MTCircularButton(PApplet pa) {
		this(pa, null, null);
	}
	public MTCircularButton(PApplet pa, MTCircularItem[] children) {
		this(pa, null, children);
	}
	public MTCircularButton(PApplet pa, MTCircularItem parent, MTCircularItem[] children) {
		super(pa, parent, children);
		
		createTextArea(pa);
		this.setText("testing");
	}
	
	/**
	 * Creates the MtCircularContent object to house the content of the button
	 * @param pa
	 */
	private void createTextArea(PApplet pa) {
		itemsArea = new MTCircularComponents(pa, this, innerWidth, outerWidth);
		this.addChild(itemsArea);		
		centerContentInButton();
	}
	
	
	/**
	 * When the button is updated, this method will reposition the content inside to keep it centered
	 */
	public void centerContentInButton() {
		//itemsArea.setPositionRelativeToParent(new Vector3D(100,0,0));
		itemsArea.setArea(innerWidth, outerWidth);
		smartRotateContent(arcWidth/2f);
	}
	
	
	/**
	 * The point of making a content object was so it was easy to rotate and place it within the button
	 * @param degree
	 */
	public void smartRotateContent(float degree) {
		itemsArea.rotateZ(Vector3D.ZERO_VECTOR, -1*textLastAngle);
		itemsArea.rotateZ(Vector3D.ZERO_VECTOR, degree);
		textLastAngle = degree;
	}

	
	/**
	 * Sets the text of the MTCircularComponents child
	 * @param text
	 */
	public void setText(String text) {
		itemsArea.setText(text);
	}
	/**
	 * returns the text of the MTCircularComponents child
	 * @param text
	 */
	public String getText() {
		return itemsArea.getText();
	}
	
	/**
	 * Sets the icon of the button
	 * @param icon
	 */
	public void setIcon(PImage icon) {
		this.itemsArea.setIcon(icon);
	}

	
	
	
	@Override
	public void setSelected(boolean selected) {
		super.setSelected(selected);
		itemsArea.setTriangle(selected);
	}
	
	
	@Override
	public void setShape(float innerWidth, float outerWidth, float arc, float currentArc) {
		super.setShape(innerWidth, outerWidth, arc, currentArc);
		if (itemsArea != null)
			centerContentInButton();
	}
	
}
