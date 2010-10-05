package org.mt4j.components.visibleComponents.widgets.menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import org.mt4j.components.interfaces.IclickableButton;
import org.mt4j.components.visibleComponents.shapes.MTPolygon;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent;
import org.mt4j.util.math.Vertex;

import processing.core.PApplet;

public abstract class MTGLButton extends MTPolygon implements IclickableButton {

	/** The registered action listeners. */
	private ArrayList<ActionListener> registeredActionListeners = new ArrayList<ActionListener>(3);

	public MTGLButton(PApplet applet, Vertex[] vertices) {
		super(applet, vertices);
	}

	public MTGLButton(Vertex[] vertices, PApplet applet) {
		super(vertices, applet);
	}

	/**
	 * Adds the action listener.
	 * 
	 * @param listener the listener
	 */
	public synchronized void addActionListener(ActionListener listener) {
		if (!registeredActionListeners.contains(listener)){
			registeredActionListeners.add(listener);
		}
	}

	/**
	 * Removes the action listener.
	 * 
	 * @param listener the listener
	 */
	public synchronized void removeActionListener(ActionListener listener) {
		if (registeredActionListeners.contains(listener)){
			registeredActionListeners.remove(listener);
		}
	}

	/**
	 * Gets the action listeners.
	 * 
	 * @return the action listeners
	 */
	public synchronized ActionListener[] getActionListeners() {
		return (ActionListener[])registeredActionListeners.toArray(new ActionListener[this.registeredActionListeners.size()]);
	}

	/**
	 * Fire action performed.
	 */
	protected void fireActionPerformed() {
		ActionListener[] listeners = this.getActionListeners();
		synchronized(listeners) {
			for (int i = 0; i < listeners.length; i++) {
				ActionListener listener = (ActionListener)listeners[i];
				listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "action performed on tangible button"));
			}
		}
	}

	/**
	 * fires an action event with a ClickEvent Id as its ID.
	 * 
	 * @param ce the ce
	 */
	public void fireActionPerformed(TapEvent ce) {
		ActionListener[] listeners = this.getActionListeners();
		synchronized(listeners) {
			for (int i = 0; i < listeners.length; i++) {
				ActionListener listener = (ActionListener)listeners[i];
				listener.actionPerformed(new ActionEvent(this, ce.getTapID(),  "action performed on tangible button"));
			}
		}
	}


}