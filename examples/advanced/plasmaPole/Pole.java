package advanced.plasmaPole;

import java.io.IOException;

import msafluid.MSAFluidSolver2D;

import org.mt4j.components.MTComponent;

import processing.core.PApplet;
import processing.core.PGraphics;
import de.sciss.jcollider.Synth;

public class Pole extends MTComponent{
  float height = 0;
  float width = 0;
  float x=width/2;
  float y=height/2;
  
  float vx=0;
  float vy=0;
  
  
  float yPadding = 20;
  float upperBoundsY = yPadding;
  float lowerBoundsY = height - yPadding;
  
  String paramX;
  String paramY;
  SynthParamMapper mapper;
  float scaleX;
  float scaleY;
  
  float scalingFactor = 2000;
  MSAFluidSolver2D fluidSolver;
  
  Synth associatedSynth;
  
  public Pole(PApplet applet, MSAFluidSolver2D fluidSolver ) {
	  super(applet);
	  
	  
	  height = applet.height;
	  width = applet.width;
	  
	  vx=0;
	  vy=0;
	  
	  
	  yPadding = 20;
	  upperBoundsY = yPadding;
	  lowerBoundsY = height - yPadding;
	  scalingFactor = 2000;
	  this.fluidSolver = fluidSolver;
  }
  
  public void setSynthAndParams(Synth associatedSynth, String paramX, String paramY, SynthParamMapper mapper) {
	  this.associatedSynth = associatedSynth;
	  this.paramX = paramX;
	  this.paramY = paramY;
	  this.mapper = mapper;
  }
  
  public void placeBall(float x, float y) {
	  this.x = x;
	  this.y = y;
  }
  
  public static class SynthParamMapper {
	  public float mapX(float x) {
		  return x;
	  }
	  public float mapY(float y) {
		  return y;
	  }
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
    p.line(x, y, x+vx*3, y+vy*3);
    p.popStyle();
    
		if (associatedSynth != null) {
			try {
				associatedSynth.set(new String[] { paramX, paramY },
						new float[] { mapper.mapX(vx), mapper.mapY(vy) });
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
  }

		
}
