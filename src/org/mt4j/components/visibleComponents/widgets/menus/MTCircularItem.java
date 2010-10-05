package org.mt4j.components.visibleComponents.widgets.menus;


import org.mt4j.components.MTComponent;
import org.mt4j.components.bounds.BoundsArbitraryPlanarPolygon;
import org.mt4j.components.interfaces.IclickableButton;
import org.mt4j.components.visibleComponents.shapes.AbstractShape;
import org.mt4j.components.visibleComponents.widgets.buttons.*;
import org.mt4j.input.gestureAction.DefaultButtonClickAction;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.rotateProcessor.RotateProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.scaleProcessor.ScaleProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor;
import org.mt4j.util.MTColor;
import org.mt4j.util.math.FastMath;
import org.mt4j.util.math.Vector3D;
import org.mt4j.util.math.Vertex;
import org.mt4j.util.xml.svg.SVGLoader;

import processing.core.PApplet;

public class MTCircularItem extends MTGLButton {
	
	protected static float DEFAULT_WIDTH = 70;
	protected static float DEFAULT_MAX_WIDTH = 100f;
	protected static float DEFAULT_PADDING = 20f;
		
	private MTCircularItem parent=null;
	
	public enum States { CLOSED, FULL, PARTIAL };
	private States state;
	
	private boolean selected = false;
	

	public float innerWidth = 30f;
	public float outerWidth = 120f;
	public float childrensOuterWidth = outerWidth;
	public float levelBuffer = 20f;
	public float arcWidth = 50f; 
	public float fullArc = 360f;
	public float currentArc = 0f;
	
	
	private MTColor selectedColor = new MTColor(40f, 120f, 130f);
	private MTColor normalColor = new MTColor(30f, 80f, 90f);
	
	private float lastRotation = 0f;
 	
	MTCircularItem children[];
	
	
	/**
	 * The constructor for MTCircularMenuItem.  All that's really necessary is the Papplet
	 * @param pa
	 * @param children
	 */
	public MTCircularItem(PApplet pa) {
		this(pa, null);
	}
	
	/**
	 * The constructor for MTCircularMenuItem can be passed an array of children, 
	 * in the order you wish them to be presented.  They will be added as children
	 * of this object, and adjusted accordingly.
	 * @param pa
	 * @param children
	 */
	public MTCircularItem(PApplet pa, MTCircularItem children[]) {
		this(pa,null,children);
	}
	
	/**
	 * The constructor for MTCircularMenuItem can be passed an array of children, 
	 * in the order you wish them to be presented.  They will be added as children
	 * of this object, and adjusted accordingly.
	 * @param pa
	 * @param parent menu
	 * @param children
	 */
	public MTCircularItem(PApplet pa, MTCircularItem parent, MTCircularItem children[]) {
		this(pa, parent, children, DEFAULT_MAX_WIDTH-DEFAULT_WIDTH, DEFAULT_MAX_WIDTH, DEFAULT_PADDING);
	}
	
	/**
	 * This is the full constructor.  It controls the initial size, and size of children, along with the parameters mentioned above
	 * @param pa
	 * @param parent
	 * @param children
	 * @param innerRadius
	 * @param outerRadius
	 * @param padding
	 */
	public MTCircularItem(PApplet pa, MTCircularItem parent, MTCircularItem children[], float innerRadius, float outerRadius, float padding) {
		super( new Vertex[]{ 
				new Vertex(0.2f,0f), new Vertex(1f, 0f), 
				new Vertex(0.8f, 0.7f), new Vertex(0.25f,0.3f), 
				new Vertex(0.22f, 0.3f), new Vertex(0.2f,0f),
				}, pa);
		this.setVertices(getVerticies());
		
		this.parent=parent;
		if (parent == null)  {
			this.state=States.FULL;
			this.childrensOuterWidth = outerRadius;
			outerRadius = innerRadius;
			innerRadius = 1f;
		}
		else {
			this.state=States.PARTIAL;
		}
		
		
		this.setShape(innerRadius, outerRadius, arcWidth, 0f);
		
		setupStuff(pa);
		setupVisibleSelf(pa);

		this.children = children;
		addAllChildren();
		//positionChildren(80f, 70f);
		
		addSelfAsListener(pa);
		this.position();
		
	}
	
	/**
	 * Iterates through all assigned children menu items, and adds them as MTchildren of
	 * this
	 */
	public void addAllChildren() {
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				MTCircularItem item = children[i];
				this.addChild(item);
				item.setState(States.PARTIAL);
			}
			this.setVisibilityOfChildren(isSelected());

		}
	}
	
	private void setupVisibleSelf(PApplet pa) {
		//this.setSizeXYRelativeToParent(200,200);
		//this.setPositionRelativeToParent(new Vector3D(0,0));		
		this.setFillColor(normalColor);
		this.setStrokeColor(new MTColor(0,0,0));
		this.setStrokeWeight(3);
		this.setVisible(true);
	
	}

	//several methods copied over from MTImageButton about setup and config... dunno yet
	private void setupStuff(PApplet pa) {
		/** This was copied over from the MTImageButton class **/
		
		this.setGestureAllowance(DragProcessor.class, false);
		this.setGestureAllowance(RotateProcessor.class, false);
		this.setGestureAllowance(ScaleProcessor.class, false);
		
		this.setEnabled(true);
		this.setBoundsBehaviour(AbstractShape.BOUNDS_ONLY_CHECK);
		
		//Make clickable
		this.setGestureAllowance(TapProcessor.class, true);
		this.registerInputProcessor(new TapProcessor(pa));
		this.addGestureListener(TapProcessor.class, new DefaultButtonClickAction(this));
		
		//Draw this component and its children above 
		//everything previously drawn and avoid z-fighting
		this.setDepthBufferDisabled(true);
		/** Done copying **/
	}

	/**
	 * 
	 *  
	 *  
	 *  
	 *  
	 *      begin               Getters and setters 
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  **/
	
	public void setParent(MTCircularItem parent) {
		this.parent = parent;
	}
	public void setChildren(MTCircularItem[] children) {
		this.children = children;
		addAllChildren();
	}
	
	public void setState(States state) {
		this.state = state;
	}
	public States getState() {
		return this.state;
	}

	
	
	/**
	 * Generates a new set of verticies for MTPolygon based off of the extisting object parameters
	 * requires a granularity level
	 * @return
	 */
	public Vertex[] getVerticies(int granularity) {
		Vertex[] verts = new Vertex [granularity * 2 + 1];
		for (int i=0; i<verts.length/2;i++) {
			verts[i] = rotate(outerWidth, arcWidth/(verts.length/2-1) * i);
		}
		for (int i=1; i<=verts.length/2;i++) {
			verts[verts.length-i-1] = rotate(innerWidth, arcWidth/(verts.length/2-1) * i);
		}
		verts[verts.length-1] = rotate(outerWidth, 0);
		return verts;

	}
	public Vertex[] getVerticies() {
		return getVerticies(30);
	}
	/**
	 * This function changes the shape of the menu item, useful for recursing.
	 * @param innerWidth
	 * @param outerWidth
	 * @param arc
	 */
	public void setArc(float arc) {
		arcWidth = arc;
		regenerateVerticies();
	}

	/**
	 * This function changes the shape of the menu item, useful for recursing.
	 * @param innerWidth
	 * @param outerWidth
	 * @param arc
	 */
	public void setShape(float innerWidth, float outerWidth, float arc, float currentArc) {
		this.arcWidth = arc;
		this.innerWidth = innerWidth;
		this.outerWidth = outerWidth;
		this.currentArc = currentArc;
		regenerateVerticies();
		swapHoverover(isSelected());
	}

	/**
	 * sets all of the children to the given visibility, called when we're clicked
	 * @param visible
	 */
	public void setVisibilityOfChildren(boolean visible) {
		if (children != null) {
			for (MTCircularItem m : children) {
				m.setVisible(visible);
			}
		}
	}

	public int getNumberOfChildren() {
		if (children != null)
			return children.length;
		else
			return 0;
	}

	/**
	 * This function is for placement of items.  Every time we want to position something,
	 * we need to figure out how many circular menu items are visible on the same level. 
	 * this function allows us to count recursively to get an accurate number.
	 * @return
	 */
	public int countVisibleItems() {
		if (this.isVisible())
			return 1 + countVisibleChildren();
		else {
			return 0;
		}
	}

	/**
	 * This helper function for countVisibleItems iterates through children,
	 * summing together the visible children
	 * @return
	 */
	public int countVisibleChildren() {
		if (children == null) return 0;
		int visibleItemCount = 0;
		for (MTCircularItem childitem : children) {
			visibleItemCount += childitem.countVisibleItems();
		}
		return visibleItemCount;
	}

	@Override
	public boolean isSelected() {
		// TODO Auto-generated method stub
		return selected;
	}

	public void setSelected(boolean selected, TapEvent tapevent) {
		setSelected(selected);
		this.fireActionPerformed(tapevent);
	}
	
	@Override
	public void setSelected(boolean selected) {
		System.out.println(this+"We're "+selected+" selected!");
		swapHoverover(selected);
		setVisibilityOfChildren(selected);
		this.handleSelectEvent(this, selected);
	}
	
	public void setSelectedColor(MTColor selectedColor) {
		this.selectedColor = selectedColor;
	}
	public void setNormalColor(MTColor normalColor) {
		this.normalColor = normalColor;
	}
	

	/**
	 * 
	 *  
	 *  
	 *  
	 *  
	 *      end                 Getters and setters 
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  **/
	
	
	

	//internal method
	private Vertex rotate(float radius, float arc) {
		return new Vertex(radius * FastMath.cos(arc*FastMath.DEG_TO_RAD), radius * FastMath.sin(arc*FastMath.DEG_TO_RAD));
	}
	

	
	/** 
	 * reset our current shape
	 */
	public void regenerateVerticies() {
		this.setVertices(getVerticies());
		this.setBoundingShape(new BoundsArbitraryPlanarPolygon(this,getVerticies()));
	}
	
	
	/**
	 * Adds this menu item as a gesture listener for tap clicks of itself
	 * @param app
	 */
	private void addSelfAsListener(PApplet app) {
		this.addGestureListener(TapProcessor.class, this);
	}
	
	
	
	
	/**
	 * This stuff all deals with the placement of the buttons about its parent.  very complicated.
	 */
	
	
	/**
	 * if passed with no parameters, we assume we just use the current values of the object
	 * @return
	 */
	public float position() {
		return position(innerWidth, outerWidth, 0.0f, arcWidth);
	}
	/**
	 * Based on the state, this positions the menu item, and children items, recursively.
	 * Nothing will be drawn closer than minDistance, nor farther than maxDistance
	 * The startingArc parameter is where the menu will start to draw things counterclockwise
	 * the defaultArc is the size of the menu if its children aren't visible
	 * @param distanceFromCenter
	 * @param maxDistance
	 * @param startingArc
	 * @param totalArc
	 * @return The ending arc position of the last subelement of it
	 */
	public float position(float minDistance, float maxDistance, float startingArc, float defaultArc) {
		switch (state) {
		case FULL:
			return positionFull(minDistance, maxDistance);
		case PARTIAL:
			return positionPartial(minDistance, maxDistance, startingArc, defaultArc, levelBuffer);
		case CLOSED:
			return positionClosed(minDistance, maxDistance);
		}
		return 0.0f;
	}
	
	/**
	 * Currently the closed state is not used, however in the future, this would be called to position it
	 * @param minDistance
	 * @param maxDistance
	 * @return
	 */
	private float positionClosed(float minDistance, float maxDistance) {
		return 0.0f;
	}

	/**
	 * This method will position the object so all of its children fan around it
	 * @param minDistance
	 * @param maxDistance
	 * @return
	 */
	private float positionFull(float minDistance, float maxDistance) {
		//System.out.println("minDistance:" + minDistance);
		this.setShape(minDistance, maxDistance, fullArc, 0f);
		if (children != null && this.isSelected() && this.countVisibleChildren() != 0) {
			float defaultArc = fullArc/this.countVisibleChildren();
			positionChildren(maxDistance, childrensOuterWidth, 0.0f, defaultArc, 0.0f);
		}
		return fullArc;
	}
	
	/**
	 * Like positionFull, but instead, it positions its children based off of the boundaries given to it by its parent
	 * @param minDistance
	 * @param maxDistance
	 * @param startingArc
	 * @param defaultArc
	 * @param levelBuffer
	 * @return
	 */
	private float positionPartial(float minDistance, float maxDistance, float startingArc, float defaultArc, float levelBuffer) {
		this.setShape(minDistance, maxDistance, defaultArc, startingArc);
		smartRotateZ(startingArc);
		//System.out.println("Resizing this slice to min:"+minDistance+", max:"+maxDistance+", arc:"+defaultArc);
		//System.out.println("Positioning this slice to "+startingArc);
		
		return positionChildren(minDistance,maxDistance,startingArc+defaultArc,defaultArc,levelBuffer);
	}

	/**
	 * In both of the above cases, the children are positioned around the object by this method
	 * @param minDistance
	 * @param maxDistance
	 * @param startingArc
	 * @param defaultArc
	 * @param levelBuffer
	 * @return
	 */
	public float positionChildren(float minDistance, float maxDistance, float startingArc, float defaultArc, float levelBuffer) {
		float currentArc = startingArc; 
		if (children != null  && this.isSelected()) {
			//System.out.println("PRinting children, all "+this.countVisibleChildren()+" of them");
			
			this.setDefaultOrientation(startingArc);
			
			currentArc = defaultArc; //we reset the rotation because the children are relative to us
			for (MTCircularItem item : children) {
				item.setParent(this);
				currentArc = item.position(minDistance+levelBuffer, maxDistance+levelBuffer, currentArc, defaultArc);
			}
			currentArc += startingArc - defaultArc;  //and we restore the rotation for other siblings
		}
		return currentArc;
	}	
	
	/**
	 * I had many issues with MT4J's rotation scheme; this fixed those.
	 * @param arc
	 */
	private void smartRotateZ(float arc) {
		this.rotateZ(Vector3D.ZERO_VECTOR, -1*lastRotation);
		this.rotateZ(Vector3D.ZERO_VECTOR, arc);
		lastRotation=arc;
	}
	
	/**
	 * A method I wanted to do so the current selection stays in the same place
	 * @param degrees
	 */
	private void setDefaultOrientation(float degrees) {
		float rotationFix = 1f;
		if (this.children != null)
			rotationFix = ((float)this.children.length) / this.countVisibleItems();
		if (parent != null) {
			parent.setDefaultOrientation(degrees*rotationFix + currentArc);
			return;
		}
		else {
			this.smartRotateZ(degrees*rotationFix);
		}
	}



	/**
	 * An odd class to override... we know the center of the button is... not techincally.
	 */
	@Override
	public Vector3D getCenterPointLocal(){
		return new Vector3D(0,0,0);
	}
	
	
	/**
	 * The handler of gesture events.  Its quite a nice handler.
	 */
	public boolean processGestureEvent(MTGestureEvent ge) {
		TapEvent te = (TapEvent)ge;
		switch (te.getId()) {
		case MTGestureEvent.GESTURE_DETECTED:
			if (this.children == null) {
				this.selected=true;
				this.setSelected(true, te);
			}
			swapHoverover(true);
			break;
		case MTGestureEvent.GESTURE_UPDATED:
			break;
		case MTGestureEvent.GESTURE_ENDED:
			if (this.children == null) {
				this.selected=false;
				this.setSelected(false, te);
			}
			else {
				this.selected = !selected;
				this.setSelected(selected, te);
			}
			break;
		}
		return false;
	}

	
	/**
	 * Called when selected to swap the color of the background
	 * @param selected
	 */
	public void swapHoverover(boolean selected) {
		if (selected)
			this.setFillColor(selectedColor);
		else
			this.setFillColor(normalColor);

	}
	
	
	
	/**
	 * This is called when one of our children is clicked
	 * The idea is once we reach the top, reposition everything.
	 * @param child
	 */
	public void handleSelectEvent(MTCircularItem child, boolean selected) {
		if (parent != null)
			parent.handleSelectEvent(child , selected);
		else
			//if (selected)
				this.position();
	}
	
	
}
