package org.mt4j.input.gestureAction;

import org.mt4j.components.MTComponent;
import org.mt4j.components.interfaces.IMTComponent3D;
import org.mt4j.components.interfaces.IMTController;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragEvent;
import org.mt4j.util.math.Vector3D;

public class InertiaDragAction implements IGestureEventListener {

	private float limit;
	private float damping;
	private int integrationTime;
	
	public InertiaDragAction(){
		this(120, 0.85f, 17);
//		this(120, 0.85f, 100);
	}

	
	public InertiaDragAction(int integrationTime, float damping, float maxVelocityLength){
		this.integrationTime = integrationTime;
		this.limit = maxVelocityLength;
		this.damping = damping;
	}

	public boolean processGestureEvent(MTGestureEvent ge) {
		IMTComponent3D t = ge.getTargetComponent();
		if (t instanceof MTComponent) {
			MTComponent comp = (MTComponent) t;
			DragEvent de = (DragEvent)ge;
			IMTController oldController;
			switch (de.getId()) {
			case DragEvent.GESTURE_DETECTED:
				break;
			case DragEvent.GESTURE_UPDATED:
				break;
			case DragEvent.GESTURE_ENDED:
				Vector3D vel = de.getDragCursor().getVelocityVector(integrationTime);
				vel.scaleLocal(0.9f); //Test - integrate over longer time but scale down velocity vec
				vel = vel.getLimited(limit);
				oldController = comp.getController();
				comp.setController(new InertiaController(comp, vel, oldController));
				break;
			default:
				break;
			}
		}
		return false;
	}
	
	
	
	private class InertiaController implements IMTController{
		private MTComponent target;
		private Vector3D startVelocityVec;
//		private float dampingValue = 0.90f;
//		private float dampingValue = 0.80f;
//		private float dampingValue = 0.45f;
		private IMTController oldController;
		
		//TODO use animation instead and ease out?
		
		private int animationTime = 1000;
		
		private int currentAnimationTime = 0;
		private float movePerMilli;
		private Vector3D moveVectNorm;
		private Vector3D moveVect;
		
		public InertiaController(MTComponent target, Vector3D startVelocityVec, IMTController oldController) {
			super();
			this.target = target;
			this.startVelocityVec = startVelocityVec;
			this.oldController = oldController;
			
			//Animation inertiaAnim = new Animation("Inertia anim for " + target, new MultiPurposeInterpolator(startVelocityVec.length(), 0, 100, 0.0f, 0.5f, 1), target);
			/*
			currentAnimationTime = 0;
			movePerMilli = startVelocityVec.length()/animationTime;
			moveVectNorm = startVelocityVec.getNormalized();
			moveVect = new Vector3D();
			*/
		}
		
		//TODO ? inertia animation is frame based, not time - so framerate decides how long it goes..
		
		////@Override
		public void update(long timeDelta) {
			/*
			currentAnimationTime += timeDelta;
			if (currentAnimationTime < animationTime){
				moveVect.setValues(moveVectNorm);
				moveVect.scaleLocal(timeDelta * movePerMilli);
				
				target.translateGlobal(moveVect);	
			}else{
				target.setController(oldController);
				return;
			}
			*/
			
//			/*
			if (Math.abs(startVelocityVec.x) < 0.05f && Math.abs(startVelocityVec.y) < 0.05f){
				startVelocityVec.setValues(Vector3D.ZERO_VECTOR);
				target.setController(oldController);
				return;
			}
			startVelocityVec.scaleLocal(damping);
			target.translateGlobal(startVelocityVec);
//			*/
			
			if (oldController != null){
				oldController.update(timeDelta);
			}
		}
	}

}
