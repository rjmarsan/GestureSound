package org.mt4j.components.visibleComponents.widgets.menus;

import org.mt4j.MTApplication;
import org.mt4j.components.visibleComponents.widgets.MTOverlayContainer;
import org.mt4j.util.math.Vector3D;

import processing.core.PGraphics;

public class MTCircularMenu extends MTOverlayContainer {
	
	MTCircularItem menuItems[];
	MTGLButton mainItem;


	public MTCircularMenu(MTApplication app, String name, MTCircularItem items[]) {
		super(app, name);
		// TODO Auto-generated constructor stub
		
		menuItems = items;
		setupChildren();
		positionChildren(360);
		/*
		mainItem = new MTCircularGLItem(app, menuItems);
		mainItem.setShape(0f, 0.3f, 360);
		mainItem.positionChildren(0.09f,360);
		mainItem.setSizeXYRelativeToParent(100, 100);
		this.addChild(mainItem);
	
		for (MTCircularGLItem m : menuItems) {
			m.setSizeXYRelativeToParent(1.5f, 1.5f);
		}
		*/
	}
	
	public void setupChildren() {
		for (MTGLButton m : menuItems) {
			this.addChild(m);
			m.setSizeXYRelativeToParent(200f, 200f);
		}
	}
	
	/**
	 * Position children in various places about the space.  Its a bit of a hack right now.
	 * @param distanceFromCenter
	 * @param totalArc
	 */
	public void positionChildren(float totalArc) {
		if (menuItems != null) {
			float arc = totalArc/menuItems.length;
			for (int i = 0; i < menuItems.length; i++) {
				MTCircularItem item = menuItems[i];
				this.addChild(item);
				item.rotateZ(Vector3D.ZERO_VECTOR, arc * i);
				item.setArc(arc);
				item.setSelected(false);
			}
		}
	}	
	
}
