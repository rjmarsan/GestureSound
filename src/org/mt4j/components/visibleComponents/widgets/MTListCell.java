package org.mt4j.components.visibleComponents.widgets;


import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor;
import org.mt4j.util.MTColor;

import processing.core.PApplet;

public class MTListCell 
//extends MTRectangle{
extends MTClipRectangle{


	public MTListCell(float width, float height, PApplet applet) {
		super(0, 0, 0, width, height, applet);
		this.setStrokeColor(new MTColor(0,0,0));
		this.setComposite(true);
		
	}
	
	@Override
	protected void setDefaultGestureActions() {
		this.registerInputProcessor(new DragProcessor(getRenderer()));
//		this.addGestureListener(DragProcessor.class, new DefaultDragAction());
	}
}
