package org.mt4j.components.visibleComponents.widgets.menus;

import org.mt4j.components.MTComponent;
import org.mt4j.components.visibleComponents.widgets.buttons.MTSvgButton;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor;
import org.mt4j.util.math.Vector3D;
import org.mt4j.util.xml.svg.SVGLoader;

import processing.core.PApplet;

public class MTCircularSVGItem extends MTSvgButton {
	
	private MTComponent hoverover;
	
	MTCircularSVGItem children[];
	
	public MTCircularSVGItem(PApplet pa) {
		this(pa, null);
	}
	
	/**
	 * The constructor for MTCircularMenuItem can be passed an array of children, 
	 * in the order you wish them to be presented.  They will be added as children
	 * of this object, and adjusted accordingly.
	 * @param pa
	 * @param children
	 */
	public MTCircularSVGItem(PApplet pa, MTCircularSVGItem children[]) {
		super("data/svg/circularmenuslice.svg", pa);
		this.children = children;
		loadHoverover("data/svg/circularmenuslicehover.svg",pa);
		addSelfAsListener(pa);
	}
	
	private void loadHoverover(String filename, PApplet pa) {
		SVGLoader loader = new SVGLoader(pa);
		hoverover = loader.loadSvg(filename);
		this.addChild(getHoverover());
		hoverover.setVisible(false);
	}
	
	
	private void addSelfAsListener(PApplet app) {
		this.registerInputProcessor(new TapProcessor(app));
		this.addGestureListener(TapProcessor.class, this);
	}
	
	public boolean processGestureEvent(MTGestureEvent ge) {
		TapEvent te = (TapEvent)ge;
		switch (te.getId()) {
		case MTGestureEvent.GESTURE_DETECTED:
			this.setSelected(true);
			break;
		case MTGestureEvent.GESTURE_UPDATED:
			break;
		case MTGestureEvent.GESTURE_ENDED:
			this.setSelected(false);
			break;
		}
		return false;
	}


	public MTComponent getHoverover() {
		return hoverover;
	}
	
	@Override
	public void setSelected(boolean selected) {
		super.setSelected(selected);
		hoverover.setVisible(selected);
		setVisibilityOfChildren(selected);
		if (selected)
			this.scale(0.4f,0.4f,1.0f,new Vector3D(0,0));
		else
			this.scale(1f/0.4f,1f/0.4f,1.0f,new Vector3D(0,0));

	}
	
	public void setVisibilityOfChildren(boolean visible) {
		if (children != null) {
			for (MTCircularSVGItem m : children) {
				m.setVisible(true);
			}
		}
	}
	
	public int getNumberOfChildren() {
		return children.length;
	}
	
	
	
}
