package advanced.plasmaPong;

import org.mt4j.MTApplication;
import org.mt4j.components.MTComponent;

import processing.core.PApplet;
import processing.core.PGraphics;

import msafluid.MSAFluidSolver2D;

public class Ball extends MTComponent{
  float height = 0;
  float width = 0;
  float x=width/2;
  float y=height/2;
  
  float vx=0;
  float vy=0;
  
  
  float yPadding = 20;
  float upperBoundsY = yPadding;
  float lowerBoundsY = height - yPadding;
  
  float scalingFactor = 2000;
  MSAFluidSolver2D fluidSolver;
  
  public Ball(PApplet applet, MSAFluidSolver2D fluidSolver ) {
	  super(applet);
	  
	  height = applet.height;
	  width = applet.width;
	  x=width/2;
	  y=height/2;
	  
	  vx=0;
	  vy=0;
	  
	  
	  yPadding = 20;
	  upperBoundsY = yPadding;
	  lowerBoundsY = height - yPadding;
	  scalingFactor = 2000;
	  this.fluidSolver = fluidSolver;
  }
  
	@Override
	public void drawComponent(PGraphics g) {
		draw(g, fluidSolver);
	}
  public void draw(PGraphics p, MSAFluidSolver2D fluidSolver) {
    p.pushStyle();
    p.fill(150,100,150,150);
    p.stroke(0);
    p.strokeWeight(3);
    int index = fluidSolver.getIndexForNormalizedPosition(x/width,y/height);
    float fluidvy = fluidSolver.v[index]*scalingFactor;
    float fluidvx = fluidSolver.u[index]*scalingFactor;
    
    float fluidvscale = 10;
    if (Math.abs(fluidvx + fluidvy) < 3) {
      fluidvscale = 100;  //now it flys a lot better when you push it
  }

  //fluidvscale = 100/abs(fluidvx + fluidvy+0.001); 
    vy = (fluidvy)/fluidvscale+(fluidvscale-1)*vy/fluidvscale;
    vx = (fluidvx)/fluidvscale+(fluidvscale-1)*vx/fluidvscale;
      
    p.ellipse(x,y,50,50);
    p.popStyle();
    
    x = x+vx;
    y = y+vy;
    checkBounds();
  }
  public void checkBounds() {
    if (x < 0) {
      x=0;
      vx = -vx;
    }
    else if (x > width) {
      x=width;
      vx = -vx;
    }
    if (y < upperBoundsY) {
      y=upperBoundsY;
      vy = -vy;
    }
    else if (y > lowerBoundsY) {
      y=lowerBoundsY;
      vy = -vy;
    }

  }
  
  
  public void resetBall() {
    x = width/2;
    y = height/2;
    vx = 0;
    vy = 0;
    scalingFactor = 1000;
  }
		
}
