package org.mt4j.input.inputSources;

import java.util.HashMap;

import javax.swing.SwingUtilities;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.mt4j.MTApplication;
import org.mt4j.input.inputData.ActiveCursorPool;
import org.mt4j.input.inputData.InputCursor;
import org.mt4j.input.inputData.MTFingerInputEvt;
import org.xvolks.jnative.exceptions.NativeException;
import org.xvolks.jnative.misc.basicStructures.HWND;
import org.xvolks.jnative.util.User32;

/**
 * Input source for native Windows 7 WM_TOUCH messages for single/multi-touch.
 * <br>Be careful to instantiate this class only ONCE!
 * 
 * @author C.Ruff
 *
 */
public class Win7NativeTouchSource extends AbstractInputSource {
	/** The Constant logger. */
	private static final Logger logger = Logger.getLogger(Win7NativeTouchSource.class.getName());
	static{
//		logger.setLevel(Level.ERROR);
//		logger.setLevel(Level.DEBUG);
		logger.setLevel(Level.INFO);
		SimpleLayout l = new SimpleLayout();
		ConsoleAppender ca = new ConsoleAppender(l);
		logger.addAppender(ca);
	}
	
	static boolean loaded = false;
	
	private MTApplication app;

	private int sunAwtCanvasHandle;

	private int awtFrameHandle;
	
	private Native_WM_TOUCH_Event wmTouchEvent;

	private boolean initialized;
	
	private HashMap<Integer, Long> touchToCursorID;
	
	private static final String dllName = "Win7Touch";
	
	private static final String canvasClassName = "SunAwtCanvas";
	
	//TODO disable touch delay due to tap&hold gesture
	//-> windows tries to make a tap&hold gesture and doesent send WM_TOUCH! (TWF_WANTPALM? flick gesture? registerTouchWindow on toplvl frame?)
	//-> in control panel-> pen and touch-> disable "Enable multi-touch gestures and inking" ? Or Change "Touch actions"->"Settings..." ?
	/*
	 switch (message)
	  {
	  case WM_TABLET_QUERYSYSTEMGESTURESTATUS:
	    return TABLET_DISABLE_TOUCHUIFORCEOFF;
	    break;
	 */
	
	//TODO remove points[] array? -> if digitizer has more than 255 touchpoints we could get out of bounds in points[]
	
	//TODO did we "delete [] ti;" in wndProc?
	
	//TODO- check dpi, if higher than 96 - if the screen is set to High DPI (more than 96 DPI),
	// you may also need to divide the values by 96 and multiply by the current DPI. (oder schon gehandlet durch ScreenToClient()?)
	
	private boolean success;
	
	public Win7NativeTouchSource(MTApplication mtApp) {
		super(mtApp);
		this.app = mtApp;
		this.success = false;
		
		String platform = System.getProperty("os.name").toLowerCase();
		logger.debug("Platform: \"" + platform + "\"");
		
//		/*
		if (!platform.contains("windows 7")) {
			logger.error("Win7NativeTouchSource input source can only be used on platforms running windows 7!");
			return;
		}
			
		if (!loaded){
			loaded = true;
//			System.load(System.getProperty("user.dir") + File.separator + dllName + ".dll");	
			System.loadLibrary(dllName);
		}else{
			logger.error("Win7NativeTouchSource may only be instantiated once.");
			return;
		}
		
		boolean touchAvailable = this.getSystemMetrics();
		if (!touchAvailable){
			logger.error("Windows 7 Touch Input currently not available!");
			return;
		}else{
			logger.info("Windows 7 Touch Input available.");
		}
//		*/
		
		wmTouchEvent = new Native_WM_TOUCH_Event();
		wmTouchEvent.id = -1;
		wmTouchEvent.type = -1;
		wmTouchEvent.x = -1;
		wmTouchEvent.y = -1;
		
		initialized = false;
		
		touchToCursorID = new HashMap<Integer, Long>();
		
		this.getNativeWindowHandles();
		success = true;
	}
	
	
	private native boolean init(long HWND); 
	
	private native boolean getSystemMetrics(); 
	
	private native boolean quit(); 
	
	private native boolean pollEvent(Native_WM_TOUCH_Event myEvent);
	
	
//	private boolean addedArtificalTouchDown = false; //FIXME REMOVE
	
	public boolean isSuccessfullySetup() {
		return success;
	}


	@Override
	public void pre(){ //we dont have to call registerPre() again (already in superclass and called there)
		if (initialized){ //Only poll events if native c++ core was initialized successfully
			while (pollEvent(wmTouchEvent)) {
				/*
				 //FIXME TEST, make a artifical TOUCH_DOWN event REMOVE LATER!
				if (!addedArtificalTouchDown){
					addedArtificalTouchDown = true;
					wmTouchEvent.type = Native_WM_TOUCH_Event.TOUCH_DOWN;
				}
				 */
				
				switch (wmTouchEvent.type) {
				case Native_WM_TOUCH_Event.TOUCH_DOWN:{
//					logger.debug("TOUCH_DOWN ==> ID:" + wmTouchEvent.id + " x:" +  wmTouchEvent.x + " y:" +  wmTouchEvent.y);
					
					InputCursor c = new InputCursor();
					long cursorID = c.getId();
					MTFingerInputEvt touchEvt = new MTFingerInputEvt(this, wmTouchEvent.x, wmTouchEvent.y, MTFingerInputEvt.INPUT_DETECTED, c);
					int touchID = wmTouchEvent.id;
					ActiveCursorPool.getInstance().putActiveCursor(cursorID, c);
					touchToCursorID.put(touchID, cursorID);
					this.enqueueInputEvent(touchEvt);
					
					break;
				}case Native_WM_TOUCH_Event.TOUCH_MOVE:{
//					logger.debug("TOUCH_MOVE ==> ID:" + wmTouchEvent.id + " x:" +  wmTouchEvent.x + " y:" +  wmTouchEvent.y);
					
					Long cursorID = touchToCursorID.get(wmTouchEvent.id);
					if (cursorID != null){
						InputCursor c = ActiveCursorPool.getInstance().getActiveCursorByID(cursorID);
						if (c != null){
							MTFingerInputEvt te = new MTFingerInputEvt(this, wmTouchEvent.x, wmTouchEvent.y, MTFingerInputEvt.INPUT_UPDATED, c);
							this.enqueueInputEvent(te);	
						}
					}
					
					break;
				}case Native_WM_TOUCH_Event.TOUCH_UP:{
//					logger.debug("TOUCH_UP ==> ID:" + wmTouchEvent.id + " x:" +  wmTouchEvent.x + " y:" +  wmTouchEvent.y);

					Long cursorID = touchToCursorID.get(wmTouchEvent.id);
					if (cursorID != null){
						InputCursor c = ActiveCursorPool.getInstance().getActiveCursorByID(cursorID);
						if (c != null){
							MTFingerInputEvt te;
							te = new MTFingerInputEvt(this, wmTouchEvent.x, wmTouchEvent.y, MTFingerInputEvt.INPUT_ENDED, c);
							this.enqueueInputEvent(te);
						}
						ActiveCursorPool.getInstance().removeCursor(cursorID);
						touchToCursorID.remove(wmTouchEvent.id);
					}
					
					break;
				}default:
					break;
				}
			}
		}

		super.pre();
	}
	
	
	private int getNativeWindowHandles(){
		final int handle = -1;
		
		//TODO kind of hacky way of getting the HWND..but there seems to be no real alternative(?)
		final String oldTitle = app.frame.getTitle();
		final String tmpTitle = "Initializing Native Windows 7 Touch Input " + Math.random();
		app.frame.setTitle(tmpTitle);
		logger.debug("Temp title: " + tmpTitle);
		
		//Invokelater because of some crash issue 
		//-> maybe we need to wait a frame until windows is informed of the window name change
		SwingUtilities.invokeLater(new Runnable() { 
			public void run() {
				//TODO also search for window class?
				//Find top level window
				int applicationWindowHandle = 0;
				try {
					HWND appHWND = User32.FindWindow(null, tmpTitle);
					applicationWindowHandle = appHWND.getValue();
				} catch (NativeException e1) {
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				} 
				
				/*
				//this always return 0...
				try {
					HWND appHWND = User32.GetActiveWindow();
					applicationWindowHandle = appHWND.getValue();
				} catch (Exception e1) {
					e1.printStackTrace();
				} 
				*/
				
				setTopWindowHandle(applicationWindowHandle);

				try {
//					logger.debug("Find SunAwtCanvas Handle:");
					HWND topLvlHandle = new HWND(applicationWindowHandle);
					HWND sunAwtCanvasHWND;
					
					//-> make sure it is the processing canvas, check with spy++ for more info
					sunAwtCanvasHWND = User32.FindWindowEx(topLvlHandle, new HWND(0), canvasClassName, null); //Find child canvas
					setSunAwtCanvasHandle(sunAwtCanvasHWND.getValue());
				} catch (NativeException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

				app.frame.setTitle(oldTitle); //Reset title text
			}
		});
		return handle;
	}

	
	private void setTopWindowHandle(int HWND){
		if (HWND > 0){
			this.awtFrameHandle = HWND;
			logger.debug("-> Found AWT HWND: " + this.awtFrameHandle);
		}else{
			logger.error("-> Couldnt retrieve the top window handle!");
		}
	}
	
	
	private void setSunAwtCanvasHandle(int HWND){
		if (HWND > 0){
			this.sunAwtCanvasHandle = HWND;
			logger.debug("-> Found SunAwtCanvas HWND: " + this.sunAwtCanvasHandle);
			
			//Initialize c++ core (subclass etc)
			this.init(this.sunAwtCanvasHandle);
			this.initialized = true;
		}else{
			logger.error("-> Couldnt retrieve the SunAwtCanvas handle!");
		}
	}
	
	private class Native_WM_TOUCH_Event{
		//can be real enums in Java 5.0.
	    /** The Constant TOUCH_DOWN. */
	    public static final int TOUCH_DOWN = 0;
	    
	    /** The Constant TOUCH_MOVE. */
	    public static final int TOUCH_MOVE = 1;
	    
	    /** The Constant TOUCH_UP. */
	    public static final int TOUCH_UP = 2;
	    
	    /** The type. */
	    public int type;
	    
	    /** The id. */
	    public int id;
	    
	    /** The x value. */
	    public int x;
	    
	    /** The y value. */
	    public int y;
	}

}
