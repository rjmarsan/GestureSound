package advanced.plasmaPong;

import java.awt.Color;

import msafluid.MSAFluidSolver2D;

import org.mt4j.components.MTComponent;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;

public class PongGame extends MTComponent {


	Ball b;
	PFont font;

	int goalBoarder = 70;
	Color goalColor = new Color(150, 150, 150, 150);

	int scoreP1 = 0;
	int scoreP2 = 0;

	int maxScore = 5;
	int waitPeriod = 60; // number of frames to keep the status message

	int justScored = 0; // the frame it happened
	int lastScored = 0; // the player who last scored
	int gameOver = 0; // the frame it happened
	int initWait = 0; // the frame it happened

	int eventFrame = -1;
	String statusMessage;
	MSAFluidSolver2D fluidSolver;
	PApplet applet;

	public PongGame(PApplet applet, MSAFluidSolver2D fluidSolver) {
		super(applet);
		// TODO Auto-generated constructor stub
		this.fluidSolver = fluidSolver;
		this.applet = applet;
		initPong(applet, fluidSolver);
	}
	
	void initPong(PApplet p, MSAFluidSolver2D fluidSolver) {
		// textMode(SHAPE);
		p.textMode(p.MODEL);
		b = new Ball(p, fluidSolver);
		// load font
		font = p.loadFont("GillSans-Bold-48.vlw");
		// font = loadFont("SansSerif-48.vlw");
		p.textFont(font, 48);
		p.textAlign(p.CENTER);
		p.rectMode(p.CENTER);
		initGameLogic();
	}

	void initGameLogic() {
		scoreP1 = 0;
		scoreP2 = 0;

		justScored = 0;
		lastScored = 0;
		gameOver = 0;
		initWait = 0;
		
		resetFluid();

		setStatus("Ready? Go!");
	}
	@Override
	public void drawComponent(PGraphics g) {
		drawPong(g);
	}
	void drawPong(PGraphics p) {
		p.pushStyle();
		p.colorMode(p.RGB, 255, 255, 255, 255);
		p.fill(150, 150, 150, 150);

		b.draw(p, fluidSolver);
		drawStatusMessage(p);
		drawScore(p);
		drawGoals(p);
		updateGameLogic();
		p.popStyle();
	}

	void setStatus(String s) {
		applet.println("New status at frame " + applet.frameCount + ": " + s);
		eventFrame = applet.frameCount;
		statusMessage = s;
	}

	void updateGameLogic() {
		if (justScored == 0) {
			if (b.x < goalBoarder) {
				justScored = applet.frameCount;
				scoreP2 += 1;
				lastScored = 1;
				if (scoreP2 < maxScore) {
					setStatus("Player " + lastScored + " Scores!");
				} else {
					setStatus("Player " + lastScored + " wins!");
					gameOver = applet.frameCount;
				}
			} else if (b.x > applet.width - goalBoarder) {
				justScored = applet.frameCount;
				scoreP1 += 1;
				lastScored = 2;
				if (scoreP1 < maxScore) {
					setStatus("Player " + lastScored + " Scores!");
				} else {
					setStatus("Player " + lastScored + " wins!");
					gameOver = applet.frameCount;
				}

			}

		}
		makeGameHarder();
		resetPuck();
		if (gameOver != 0) {
			gameOver();
		}
	}

	void makeGameHarder() {
		b.scalingFactor += 2;
		fluidSolver.setVisc((float) (fluidSolver.getVisc() / 1.003));
		//applet.println(fluidSolver.getVisc());
	}

	void drawStatusMessage(PGraphics p) {
		if (eventFrame != -1) {
			if (applet.frameCount - eventFrame < waitPeriod) {
				drawStatusText(p, statusMessage);
				drawCountdownBar(p, eventFrame);
			} else {
				eventFrame = -1;
			}
		}
	}

	void gameOver() {
		if (applet.frameCount - gameOver > waitPeriod) {
			initGameLogic();
		}
	}

	void resetPuck() {
		if (justScored != 0 && applet.frameCount - justScored > waitPeriod) {
			b.resetBall();
			justScored = 0;
			resetFluid();
		}

	}

	void resetFluid() {
		setupFluid();
		fluidSolver.reset();
	}

	void drawScore(PGraphics p) {
		p.stroke(0);
		p.text(scoreP1, 100, 100);
		p.text(scoreP2, applet.width - 100, 100);
	}

	void drawStatusText(PGraphics p, String s) {
		p.text(s, applet.width / 2, applet.height / 2);
	}

	void drawCountdownBar(PGraphics p, int frameSince) {
		p.rect(applet.width / 2, applet.height / 2 + 20,
				3 * (waitPeriod - (applet.frameCount - frameSince)), 10);
	}

	void drawGoals(PGraphics p) {
		// for now
		p.stroke(150,150,150,150);
		p.strokeWeight(3);
		p.line(goalBoarder, goalBoarder, goalBoarder, applet.height - goalBoarder);
		p.line(applet.width - goalBoarder, goalBoarder, applet.width - goalBoarder, applet.height
				- goalBoarder);
	}
	void setupFluid() {
		  fluidSolver.enableRGB(true).setFadeSpeed(0.001f).setDeltaT(0.5f).setVisc(0.0001f);
	}
}
