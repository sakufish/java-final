package robotWar;

import becker.robots.*;
import java.awt.Color;

public class CChenDummyRobot extends FighterRobot {

	private static final int ATTACK_VALUE = 4;
	private static final int DEFENCE_VALUE = 4;
	private static final int MOVE_VALUE = 2;

	// Default value
	private int health = 100; 


	public CChenDummyRobot (City c, int a, int s, Direction d, int id, int maxHealth){
		super(c, a, s, d, id, ATTACK_VALUE, DEFENCE_VALUE, MOVE_VALUE);
		this.health = maxHealth;
		this.setLabel();
		System.out.println(this.getNumMoves());
	}

	@Override
	public void goToLocation(int a, int s) {
	}		

	@Override
	public TurnRequest takeTurn(int energy, OppData[] data) {

		return new TurnRequest(this.getAvenue(), this.getStreet(), -1, 0);
	}

	@Override
	public void battleResult(int healthLost, int oppID, int oppHealthLost, int numRoundsFought) {
		this.health = health - healthLost;		
	}


	public void setLabel() {
		this.setLabel("#" + this.getID() + "HP:" + this.health);
		if (this.health <= 0) {
			this.setColor(Color.BLACK);
		} else {
			this.setColor(Color.ORANGE);
		}
	}

	// developer.android.com/studio

}
