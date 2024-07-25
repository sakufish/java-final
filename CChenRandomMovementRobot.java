package robotWar;

import becker.robots.*;
import java.awt.Color;
import java.util.Random;

/**
 * Robot that randomly moves on the arena
 */
public class CChenRandomMovementRobot extends FighterRobot {

	// Default value
	private int health = 100; 

	public CChenRandomMovementRobot (City c, int a, int s, Direction d, int id, int maxHealth){
		super(c, a, s, d, id, 3, 3, 4);
		this.health = maxHealth;
		this.setLabel();
		System.out.println(this.getNumMoves());
	}


	@Override
	public TurnRequest takeTurn(int energy, OppData[] data) {
		
		// Creating random generator
		Random generator = new Random();
		 
		// Generating random target location on grid
		int [] moves = moveInstruction(generator.nextInt(CChenBattleManager.WIDTH), generator.nextInt(CChenBattleManager.HEIGHT), energy);
		
		// Returns the move
		return new TurnRequest(this.getAvenue() + moves[0], this.getStreet() + moves[1], -1, 0);
	}


	private int[] moveInstruction(int targetA, int targetS, int energy) {
	    int[] moveInstructions = new int[2];
	    moveInstructions[0] = targetA - this.getAvenue();
	    moveInstructions[1] = targetS - this.getStreet();

	    int movesA = Math.abs(moveInstructions[0]);
	    int movesS = Math.abs(moveInstructions[1]);
	    
	    // Determining the maximum number of moves
	    int maxMovesByEnergy = energy / 5;
	    int maxMoves = Math.min(this.getNumMoves(), maxMovesByEnergy);

	    if (movesA >= maxMoves) {
	        if (moveInstructions[0] < 0) {
	            moveInstructions[0] = maxMoves * (-1);
	        } else {
	            moveInstructions[0] = maxMoves;
	        }
	        moveInstructions[1] = 0;
	    } else {
	        int remainingMoves = maxMoves - movesA;
	        if (remainingMoves <= movesS) {
	            if (moveInstructions[1] < 0) {
	                moveInstructions[1] = remainingMoves * (-1);
	            } else {
	                moveInstructions[1] = remainingMoves;
	            }
	        } 
	    }
	    
	    return moveInstructions;
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
			this.setColor(Color.ORANGE);
		}
	}

	// developer.android.com/studio

}
