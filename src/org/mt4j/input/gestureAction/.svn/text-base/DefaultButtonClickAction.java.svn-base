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
package org.mt4j.input.gestureAction;

import org.mt4j.components.MTComponent;
import org.mt4j.components.TransformSpace;
import org.mt4j.components.interfaces.IclickableButton;
import org.mt4j.components.visibleComponents.shapes.AbstractShape;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent;
import org.mt4j.util.math.Vector3D;


/**
 * Default clickgesture action for image buttons.
 * 
 * @author Christopher Ruff
 */
public class DefaultButtonClickAction implements IGestureEventListener  {
	
	/** The poly button. */
	private AbstractShape polyButton;
	
	/** The size change value. */
	private float sizeChangeValue;
	
	/** The width. */
	float width;
	
	/** The height. */
	float height;
	
	/**
	 * Instantiates a new default button click action.
	 * 
	 * @param poly the poly
	 */
	public DefaultButtonClickAction(AbstractShape poly){
		this.polyButton = poly;
		this.sizeChangeValue = 3;
		
		this.width = polyButton.getWidthXY(TransformSpace.RELATIVE_TO_PARENT);
		
		this.height = polyButton.getHeightXY(TransformSpace.RELATIVE_TO_PARENT);
	}

	
	
	/* (non-Javadoc)
	 * @see com.jMT.input.gestureAction.IGestureAction#processGesture(com.jMT.input.inputAnalyzers.GestureEvent)
	 */
	public boolean processGestureEvent(MTGestureEvent g) {
//		width = polyButton.getWidthLocal();//
		
		if (g instanceof TapEvent){
			TapEvent clickEvent = (TapEvent)g;
			
			
			if (g.getTargetComponent() instanceof MTComponent){ 
				MTComponent comp = (MTComponent)g.getTargetComponent();
				
				//Hack for keeping up with the buttons current width if it was changed
				//due to .scale or something sometime
				if (comp instanceof IclickableButton) {
					IclickableButton button = (IclickableButton) comp;
					if (!button.isSelected()){
//						this.width = ((AbstractShape)button).getWidthLocal();
						this.width = getCurrentUnscaledWidth();
						
//						this.getReferenceComp().getWidthVectObjSpace();
//						//TODO aktuelle width holen zu comptoscale relative
//						this.width = this.getReferenceComp().getWidthLocal();
					}
				}
				
				switch (clickEvent.getId()) {
				case MTGestureEvent.GESTURE_DETECTED:
//					if (comp.isGestureAllowed(TapAnalyzer.class) 
//						&& comp.isVisible()
//					){
						comp.sendToFront();
//						if ( ((TapEvent)g).getId() == TapEvent.BUTTON_DOWN){
						if ( ((TapEvent)g).getTapID() == TapEvent.BUTTON_DOWN){
							
							//Resize button
//							getCompToResize().setSizeLocal(width-5, width-5);
//							Vector3D centerPoint = this.getRefCompCenterLocal();
//							this.getCompToResize().scale(1/this.getReferenceComp().getWidthLocal(), 1/this.getReferenceComp().getHeightLocal(), 1, centerPoint);
//							this.getCompToResize().scale(width-5, width-5, 1, centerPoint);
							
//							this.resize(width-5, width-5);
							
//							this.shrink(width-shrinkValue, width-shrinkValue);
							this.shrink(width-sizeChangeValue, height-sizeChangeValue);
							
							if (comp instanceof IclickableButton){
								IclickableButton polyButton = (IclickableButton)g.getTargetComponent();
								polyButton.fireActionPerformed((TapEvent)g);
								polyButton.setSelected(true);
							}
						}
//					}
					break;
				case MTGestureEvent.GESTURE_UPDATED:
					//NOTE: usually click gesture analyzers dont send gesture update events
//					if (comp.isGestureAllowed(TapAnalyzer.class) 
//							&& comp.isVisible()
//						){
//						if ( ((TapEvent)g).getId() == TapEvent.BUTTON_DOWN){
					if ( ((TapEvent)g).getTapID() == TapEvent.BUTTON_DOWN){
							if (comp instanceof IclickableButton){
								IclickableButton polyButton = (IclickableButton)g.getTargetComponent();
								polyButton.fireActionPerformed((TapEvent)g);
							}
						}
//					}
					break;
				case MTGestureEvent.GESTURE_ENDED:
					
//					if (comp.isGestureAllowed(TapAnalyzer.class) 
//							&& comp.isVisible()
//						){
//						if ( ((TapEvent)g).getId() == TapEvent.BUTTON_CLICKED
//							|| ((TapEvent)g).getId() == TapEvent.BUTTON_UP
//						){
						if ( ((TapEvent)g).getTapID() == TapEvent.BUTTON_CLICKED
								|| ((TapEvent)g).getTapID() == TapEvent.BUTTON_UP
							){
							//Resize button
//							polyButton.setSizeLocal(width, width);
//							this.resize(width, width);
							
//							this.enlarge(width, width);
							this.enlarge(width, height);
							
//							Vector3D centerPoint = this.getRefCompCenterLocal();
//							this.getCompToResize().scale(1/this.getReferenceComp().getWidthLocal(), 1/this.getReferenceComp().getHeightLocal(), 1, centerPoint);
//							this.getCompToResize().scale(width, width, 1, centerPoint);
							
							if (comp instanceof IclickableButton){
								IclickableButton polyButton = (IclickableButton)g.getTargetComponent();
								polyButton.fireActionPerformed((TapEvent)g);
								polyButton.setSelected(false);
							}
						}
//					}
					break;
				default:
					break;
				}
			}
		}
		return false;
	}
	
	/**
	 * Gets the current unscaled width.
	 * 
	 * @return the current unscaled width
	 */
	public float getCurrentUnscaledWidth(){
		return this.getReferenceComp().getWidthXY(TransformSpace.RELATIVE_TO_PARENT);
	}
	
	/**
	 * Gets the comp to resize.
	 * 
	 * @return the comp to resize
	 */
	public MTComponent getCompToResize(){
		return this.polyButton;
	}
	
	/**
	 * Gets the reference comp.
	 * 
	 * @return the reference comp
	 */
	protected AbstractShape getReferenceComp(){
		return this.polyButton;
	}
	
	/**
	 * Gets the ref comp center local.
	 * 
	 * @return the ref comp center local
	 */
	protected Vector3D getRefCompCenterLocal(){
		Vector3D centerPoint;
		if (this.getReferenceComp().isBoundingShapeSet()){
			centerPoint = this.getReferenceComp().getBoundingShape().getCenterPointLocal();
			centerPoint.transform(this.getReferenceComp().getLocalMatrix()); //macht den punkt in self space
		}else{
			centerPoint = this.getReferenceComp().getCenterPointGlobal();
			centerPoint.transform(this.getReferenceComp().getGlobalInverseMatrix());
			centerPoint.transform(this.getReferenceComp().getLocalMatrix());
		}
		return centerPoint;
		
		//TODO wieder auf localobj space center umstellen?
//		Vector3D centerPoint;
//		if (this.getReferenceComp().isBoundingShapeSet()){
//			centerPoint = this.getReferenceComp().getBoundingShape().getCenterPointObjSpace();
////			centerPoint.transform(this.getReferenceComp().getLocalBasisMatrix()); //macht den punkt in self space
//		}else{
//			centerPoint = this.getReferenceComp().getCenterPointGlobal();
//			centerPoint.transform(this.getReferenceComp().getAbsoluteWorldToLocalMatrix());
////			centerPoint.transform(this.getReferenceComp().getLocalBasisMatrix());
//		}
//		return centerPoint;
	}
	
	
	/**
	 * Shrink.
	 * 
	 * @param width the width
	 * @param height the height
	 */
	protected void shrink(float width, float height){
		this.resize(width,height);
	}
	
	/**
	 * Enlarge.
	 * 
	 * @param width the width
	 * @param height the height
	 */
	protected void enlarge(float width, float height){
//		this.resize(width, width);
		this.resize(width, height);
	}
	
	//TODO besser nur uniform scalen! nur width nehmen zb!
	/**
	 * Resize.
	 * 
	 * @param width the width
	 * @param height the height
	 */
	protected void resize(float width, float height){
		Vector3D centerPoint = this.getRefCompCenterLocal();
		this.getCompToResize().scale(1/this.getReferenceComp().getWidthXY(TransformSpace.RELATIVE_TO_PARENT), 1/this.getReferenceComp().getWidthXY(TransformSpace.RELATIVE_TO_PARENT), 1, centerPoint);
		this.getCompToResize().scale(width, width, 1, centerPoint);
		
		//this uses the obj space - better?
//		Vector3D centerPoint = this.getRefCompCenterLocal();
//		this.getCompToResize().scale(1/this.getReferenceComp().getWidthXY(TransformSpace.RELATIVE_TO_PARENT), 1/this.getReferenceComp().getWidthXY(TransformSpace.RELATIVE_TO_PARENT), 1, centerPoint, TransformSpace.RELATIVE_TO_SELF);
//		this.getCompToResize().scale(width, width, 1, centerPoint, TransformSpace.RELATIVE_TO_SELF);
	}

	/**
	 * Gets the size change value.
	 * 
	 * @return the size change value
	 */
	public float getSizeChangeValue() {
		return sizeChangeValue;
	}

	/**
	 * Sets the size change value.
	 * 
	 * @param shrinkValue the new size change value
	 */
	public void setSizeChangeValue(float shrinkValue) {
		this.sizeChangeValue = shrinkValue;
	}
	
	
}
