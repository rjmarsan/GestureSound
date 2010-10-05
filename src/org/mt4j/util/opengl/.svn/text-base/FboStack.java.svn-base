package org.mt4j.util.opengl;

import java.util.Stack;

import javax.media.opengl.GL;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;


public class FboStack{
	/** The Constant logger. */
	private static final Logger logger = Logger.getLogger(FboStack.class.getName());
	static{
//		logger.setLevel(Level.ERROR);
		SimpleLayout l = new SimpleLayout();
		ConsoleAppender ca = new ConsoleAppender(l);
		logger.addAppender(ca);
	}
	
	public GL gl;
	protected int currentFBO;
	protected Stack<Integer> fboNameStack;
	
	private static FboStack instance = null;

	private FboStack(GL gl){
		this.gl = gl;
		fboNameStack = new Stack<Integer>();
		currentFBO = 0;
	}
	
	public static FboStack getInstance(GL gl){
		if (instance == null){
			instance = new FboStack(gl);
			return instance;
		}else{
			return instance;
		}
	}

	/**
	 * Pushes the currently used render target ID on the stack.
	 */
	public void pushFBO(){
		fboNameStack.push(new Integer(currentFBO));
	}

	/**
	 * Binds the specified render target ID and sets it as current.
	 * 
	 * @param fbo the fbo
	 */
	public void useFBO(int fbo){
		currentFBO = fbo;
		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, currentFBO);
	}

	/**
	 * Binds the specified frame buffer object and sets it as current.
	 * 
	 * @param fbo the fbo
	 */
	public void useFBO(GLFBO fbo){
		currentFBO = fbo.getName();
		fbo.bind();
	}
	
	public int peekFBO(){
		if (fboNameStack.isEmpty()){
			return 0;
		}else{
//			return fboNameStack.peek();
			return currentFBO;
		}
	}

	//NOTE THIS UNBINDS A CURRENT FBO IF SET! -> no need for calling unbind()!
	/**
	 * Pops the fbo.
	 * This switches back (binds) to the formely pushed fbo. 
	 */
	public void popFBO(){
		if (fboNameStack.isEmpty()){
			logger.error("Trying to pop() from an empty framebuffer stack!"); //TODO -> just bind 0 !?
		}else{
			currentFBO = fboNameStack.pop().intValue();
			gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, currentFBO);
		}
	}
	
}
