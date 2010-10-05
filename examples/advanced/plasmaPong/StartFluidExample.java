package advanced.plasmaPong;

import org.mt4j.MTApplication;

public class StartFluidExample extends MTApplication{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public static void main(String args[]){
		initialize();
	}
	
	@Override
	public void startUp(){
		this.addScene(new PlasmaPongScene(this, "Plasma Pong scene"));
	}
}
