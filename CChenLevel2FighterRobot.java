package robotWar;

import becker.robots.*;
import java.awt.Color;

public class CChenLevel2FighterRobot extends FighterRobot{

	private static final int ATTACK_VALUE = 4;
	private static final int DEFENCE_VALUE = 4;
	private static final int MOVE_VALUE = 2;	
	
	// Default value
	private int health = 100; 

	public CChenLevel2FighterRobot (City c, int a, int s, Direction d, int id, int maxHealth){
		super(c, a, s, d, id, ATTACK_VALUE, DEFENCE_VALUE, MOVE_VALUE);
		this.health = maxHealth;
		this.setLabel();
	}
	
	@Override
	public TurnRequest takeTurn(int energy, OppData[] data) {
		int target = targetRobot(data, 0, 1);
		int [] moves = moveInstruction(data[target].getAvenue(),data[target].getStreet(),energy);
		//*************************************************
		System.out.println("ID: " + this.getID());
		System.out.println("Target: " + target);
		System.out.println(moves[0] + " | " + moves[1]);
		//*************************************************
		
		if (this.getAvenue()+ moves[0] == data[target].getAvenue() && this.getStreet() + moves[1] == data[target].getStreet()) {
			return new TurnRequest(this.getAvenue() + moves[0], this.getStreet() + moves[1], target, ATTACK_VALUE);
		} else { 
			return new TurnRequest(this.getAvenue() + moves[0], this.getStreet() + moves[1], -1, 0);
		}

	}

	private int targetRobot(OppData [] data, double maxDistanceValue, double maxHealthValue) {
		double [] robotWeight = new double [data.length];
		double smallestWeight = Double.MAX_VALUE;
		int index = -1;
		for (int i = 0; i < data.length; i++) {
			robotWeight[i] = (this.findDistance(this.getAvenue(), this.getStreet(), data[i].getAvenue(), data[i].getStreet())/30.0)*maxDistanceValue + ((data[i].getHealth())/100.0)*maxHealthValue;
			if (robotWeight[i] < smallestWeight && i != this.getID() && data[i].getHealth() > 0) {
				smallestWeight = robotWeight[i];
				index = i;
			} else if (robotWeight[i] > 1) {
				System.out.println("SOMETHING WENT WRONG");
			}
		}
		System.out.println("weight: " + robotWeight[index]);
		return index;
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

	private int findDistance(int x1, int y1, int x2, int y2) {
		return Math.abs(x1-x2) + Math.abs(y1-y2);
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
			this.setColor(Color.CYAN);
		}
	}

	// developer.android.com/studio

}
