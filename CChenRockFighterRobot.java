package robotWar;

import becker.robots.*;
import java.awt.Color;

/**
 * potentially joy wang's if she doesnt fix her movement
 * 
 * @version 2024-06-12
 */
public class CChenRockFighterRobot extends FighterRobot {

	private static final int ATTACK_VALUE = 4;
	private static final int DEFENCE_VALUE = 4;
	private static final int MOVE_VALUE = 2;

	// Default value 💀
	private int health = 100; 


	public CChenRockFighterRobot (City c, int a, int s, Direction d, int id, int maxHealth){
		super(c, a, s, d, id, ATTACK_VALUE, DEFENCE_VALUE, MOVE_VALUE);
		this.health = maxHealth;
		this.setLabel();
		System.out.println(this.getNumMoves());
	}

	@Override
	public void goToLocation(int a, int s) {

		int xDistance = a - this.getAvenue();
		int yDistance = s - this.getStreet();

		// Moving through street
		if (yDistance < 0) {
			this.turnTo(Direction.NORTH);
		} else if (yDistance > 0) {
			this.turnTo(Direction.SOUTH);
		}

		while (this.getStreet() != s) {
			this.move();
		}

		// Moving through avenue
		if (xDistance < 0) {
			this.turnTo(Direction.WEST);
		} else if (xDistance > 0) {
			this.turnTo(Direction.EAST);
		}

		while (this.getAvenue() != a) {
			this.move();
		}

	}		

	@Override
	public TurnRequest takeTurn(int energy, OppData[] data) {
		
		return new TurnRequest(this.getAvenue(), this.getStreet(), -1, 0);
	}

	@Override
	public void battleResult(int healthLost, int oppID, int oppHealthLost, int numRoundsFought) {
		this.health = health - healthLost;		
	}

	private void turnTo(Direction d) {

		int turnsNeeded = (getDirectionNum(d) - getDirectionNum(this.getDirection()) + 4) % 4;

		if (turnsNeeded == 1) {
			this.turnRight();
		} else if (turnsNeeded == 2) {
			this.turnAround();
		} else if (turnsNeeded == 3) {
			this.turnLeft();
		}
	}

	private int getDirectionNum(Direction d) {
		if (d == Direction.NORTH) {
			return 0;
		} else if (d == Direction.EAST) {
			return 1;
		} else if (d == Direction.SOUTH) {
			return 2;
		} else {
			return 3;
		}
	}

	public void setLabel() {
		this.setLabel("#" + this.getID() + "HP:" + this.health);
		if (this.health <= 0) {
			this.setColor(Color.BLACK);
		} else {
			this.setColor(Color.GRAY);
		}
	}

	// developer.android.com/studio

}
