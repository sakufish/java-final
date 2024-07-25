package robotWar;

import becker.robots.*;

import java.awt.Color;

public class CChenLevel3FighterRobot extends FighterRobot{

	private static final int ATTACK_VALUE = 4;
	private static final int DEFENCE_VALUE = 4;
	private static final int MOVE_VALUE = 2;

	// Default value
	private int health = 100; 

	// Grid visualization variables
	private int playerValue = 2;
	private int checkRange = 3;

	public CChenLevel3FighterRobot (City c, int a, int s, Direction d, int id, int maxHealth){
		super(c, a, s, d, id, ATTACK_VALUE, DEFENCE_VALUE, MOVE_VALUE);
		this.health = maxHealth;
		this.setLabel();
	}

	/**
	 * Creates a decision value based on battle situation
	 * @param positionVal - The value on battle grid of current position
	 * @param energyVal - Current energy value 
	 * @param maxPos - The proportion of position value
	 * @param maxEng - The proportion of energy value
	 * @param maxHP - The proportion of health value
	 * @return decision value
	 */
	private double decision(int positionVal, int energyVal, double maxPos, double maxEng, double maxHP) {
		
		// Setting weights with respect to the proportions
		double energyWeight = (1.0 - (energyVal / 100.0)) * maxEng;
		double healthWeight = (1.0 - (this.health / 100.0)) * maxHP;
		double positionWeight = 0;
		
		// Makes position value increase faster
		if (positionVal < 7) {
			positionWeight = positionVal / (7 - positionVal);
		} else {
			positionWeight = 0.6;
		}
		
		// Applying proportion
		positionWeight *= maxPos;

		return positionWeight + energyWeight + healthWeight;
	}

	@Override
	public TurnRequest takeTurn(int energy, OppData[] data) {
		
		// Updating all data
		int positionVal = this.mapDangerArea(data)[this.getAvenue()][this.getStreet()];
		
		// Finds a potential target
		int target = this.targetRobot(data, 0.4, 0.6);

		// Possible move instructions
		int [] movesAttack = moveInstruction(data[target].getAvenue(), data[target].getStreet(), energy);
		int [] movesEscape = moveInstruction(this.findEscape(data)[0][0], this.findEscape(data)[0][1], energy);
		
		// Makes a decision - if above a certain threshold defends/replenish energy/escapes. Otherwise attacks
		if (this.decision(positionVal, energy, 0.3, 0.2, 0.5) <= 0.5 && energy > 20) {
			
			// Determines if can attack robot this turn
			if (this.getAvenue() + movesAttack[0] == data[target].getAvenue() && this.getStreet() + movesAttack[1] == data[target].getStreet()) {
				return new TurnRequest(this.getAvenue() + movesAttack[0], this.getStreet()+ movesAttack[1], data[target].getID(), ATTACK_VALUE);
			} else {
				return new TurnRequest(this.getAvenue() + movesAttack[0], this.getStreet()+ movesAttack[1], -1, ATTACK_VALUE);
			}
		} else {
			
			// Conserves energy priority, then escapes
			if (energy > 20) {
				return new TurnRequest(this.getAvenue() + movesEscape[0], this.getStreet()+ movesEscape[1], -1,0);
			} else {
				return new TurnRequest(this.getAvenue(), this.getStreet(), -1, 0);
			}
		}
	}
	
	/**
	 * Finds a robot to target based on opponent data 
	 * @param data - The opponent data
	 * @param maxDistanceValue - The proportion of the total weight for distance
	 * @param maxHealthValue - The proportion of the total weight for health
	 * @return Index position of robot in the data based on sorted array
	 */
	private int targetRobot(OppData [] data, double maxDistanceValue, double maxHealthValue) {

		// Using insertion sort to sort data by weight
		for (int i = 1; i < data.length; i++) {
			int j = i;
			while (j > 0 &&  (this.findDistance(this.getAvenue(), this.getStreet(), data[j].getAvenue(), data[j].getStreet())/30.0)*maxDistanceValue + ((data[j].getHealth())/100.0)*maxHealthValue <  (this.findDistance(this.getAvenue(), this.getStreet(), data[j-1].getAvenue(), data[j-1].getStreet())/30.0)*maxDistanceValue + ((data[j-1].getHealth())/100.0)*maxHealthValue) {
				OppData temp = data[j-1];
				data[j-1] = data[j];
				data[j] = temp;
				j--;
			}
		}

		int index = -1;
		
		// Looking for valid robot to target
		for (int i = 0; i < data.length; i++) {
			if (data[i].getHealth() > 0 && data[i].getID() != this.getID()) {
				index = i;
				break;
			}
		}

		return index;	
	}
	
	/**
	 * Provides movement instructions relative to the robot's current position
	 * @param targetA - Target avenue position
	 * @param targetS - Target street position
	 * @param energy - Current energy level
	 * @return - The avenue and street move instructions
	 */
	private int[] moveInstruction(int targetA, int targetS, int energy) {
		
		int[] moveInstructions = new int[2];
		
		// Initializing the move instructions
		moveInstructions[0] = targetA - this.getAvenue();
		moveInstructions[1] = targetS - this.getStreet();
		
		// Finding needed moves for x and y direction
		int movesA = Math.abs(moveInstructions[0]);
		int movesS = Math.abs(moveInstructions[1]);

		// Determining the maximum number of moves possible
		int maxMovesByEnergy = energy / 5;
		int maxMoves = Math.min(this.getNumMoves(), maxMovesByEnergy);
		
		// Ensuring that move instructions has up to max moves - starts by attempting to move avenues, then streets
		if (movesA >= maxMoves) {
			
			// handling different movement direction 
			if (moveInstructions[0] < 0) {
				moveInstructions[0] = maxMoves * (-1);
			} else {
				moveInstructions[0] = maxMoves;
			}
			moveInstructions[1] = 0;
		} else {
			int remainingMoves = maxMoves - movesA;
			
			// Checks if not moves cap out at limit
			if (remainingMoves <= movesS) {
				
				// Handles directions
				if (moveInstructions[1] < 0) {
					moveInstructions[1] = remainingMoves * (-1);
				} else {
					moveInstructions[1] = remainingMoves;
				}
			} 
		}

		return moveInstructions;
	}
	
	/**
	 * Finds the movement difference between two points
	 * @param x1 - First avenue location
	 * @param y1 - First street location
	 * @param x2 - Second avenue location
	 * @param y2 - Second street location
	 * @return - Horizontal + vertical distance 
	 */
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
	
	/**
	 * Turns the robot to a direction
	 * @param d - Target direction
	 */
	private void turnTo(Direction d) {
		
		// Finding number of turns needed
		int turnsNeeded = (getDirectionNum(d) - getDirectionNum(this.getDirection()) + 4) % 4;
		
		// Doing optimal turns
		if (turnsNeeded == 1) {
			this.turnRight();
		} else if (turnsNeeded == 2) {
			this.turnAround();
		} else if (turnsNeeded == 3) {
			this.turnLeft();
		}
	}
	
	/**
	 * Helper to turn robot
	 * @param d - A direction
	 * @return Number associated with direction
	 */
	private int getDirectionNum(Direction d) {
		
		// Getting associated direction number
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
	
	/**
	 * Finding safe escape positions for the robot
	 * @param data - The data of all the robots
	 * @return - The safest coordinate
	 */
	private int[][] findEscape(OppData [] data) {
		
		// Creating arrays
		int[][] grid = this.mapDangerArea(data);
		int[][] rangeCoordinates = drawDangerCircle(grid,this.getAvenue(), this.getStreet(), MOVE_VALUE, true);
		int[][] escape = new int[1][2];

		int smallestValue = Integer.MAX_VALUE;

		// Searches for smallest value
		for (int i = 0; i < rangeCoordinates.length; i++) {
			if (rangeCoordinates[i][0] != -1 && grid[rangeCoordinates[i][0]][rangeCoordinates[i][1]] < smallestValue) {
				smallestValue = grid[rangeCoordinates[i][0]][rangeCoordinates[i][1]];
			}
		}

		// Scans for multiple occurrences of smallest value
		int count = 0;
		for (int i = 0; i < rangeCoordinates.length; i++) {
			if (rangeCoordinates[i][0] != -1 && grid[rangeCoordinates[i][0]][rangeCoordinates[i][1]] == smallestValue) {
				count++;
			}
		}

		// Gets the values of the coordinates
		int [][] possibleEscapes = new int[count][2];
		int indexI = 0;

		// Adding possible escape coordinates
		for (int i = 0; i < rangeCoordinates.length; i++) {
			if (rangeCoordinates[i][0] != -1 && grid[rangeCoordinates[i][0]][rangeCoordinates[i][1]] == smallestValue) {
				possibleEscapes[indexI][0] = rangeCoordinates[i][0];
				possibleEscapes[indexI][1] = rangeCoordinates[i][1];
				indexI++;
			}
		}

		// Second layer of checking for most optimal coordinate for safe spots AFTER turn progresses
		int smallestSum = Integer.MAX_VALUE;
		int index = 0;
		for (int i = 0; i < possibleEscapes.length; i++) {
			int num = sumOfEscape(grid, possibleEscapes[i][0], possibleEscapes[i][1]);
			if (num < smallestSum) {
				smallestSum = num;
				index = i;
			}
		}

		// Adding escape location
		escape[0][0] = possibleEscapes[index][0];
		escape[0][1] = possibleEscapes[index][1];

		return escape;

	}
	
	/**
	 * Helper to find the sum of a range on the battle grid
	 * @param grid - The battle grid
	 * @param ave - Avenue position
	 * @param str - Street position
	 * @return The sum
	 */
	private int sumOfEscape(int[][] grid, int ave, int str) {
		
		// Getting surrounding escape locations
		int[][] coordinates = drawDangerCircle(grid, ave, str, this.checkRange, true);
		int sum = 0;
		
		// For each set of coordinates finds the danger value
		for (int i = 0; i < coordinates.length; i++) {
			if (coordinates[i][0] != -1) {
				sum += grid[coordinates[i][0]][coordinates[i][1]];
			}
		}

		return sum;
	}
	
	/**
	 * Helper to draw danger range around robots and points on the battle grid
	 * @param grid - Battle grid
	 * @param ave - Avenue position
	 * @param str - Street position 
	 * @param dangerRange - The range of the robot/point movement 
	 * @param forPlayerRange - If the range is for a robot or for a point
	 * @return
	 */
	private int[][] drawDangerCircle(int[][] grid, int ave, int str, int dangerRange, boolean forPlayerRange) {

		// max number of coordinates for pattern (explicit formula for the pattern around the robot/object)
		int [][] rangeCoordinates = new int[2*(dangerRange+1)*(dangerRange+1)-2*(dangerRange+1)][2];
		int index = 0;

		// dividing and creating top half and bottom half of circle
		for (int i = 0; i < dangerRange; i++) {
			int count = 0;

			// Looping through number of top rows
			for (int j = 0; j < 1 + 2 * i; j++) {

				// Getting row and column position (starting)
				int rowPosition = ave - i + count;
				int colPosition = str - dangerRange + i;

				// only drawing if location exists on the grid
				if (rowPosition >= 0 && rowPosition < grid.length && colPosition >= 0 && colPosition < grid[0].length) {
					if (forPlayerRange) {
						rangeCoordinates[index][0] = rowPosition;
						rangeCoordinates[index][1] = colPosition;
						index++;
					} else {
						grid[rowPosition][colPosition] += 1;
					}
				}
				count++;
			}
			count = 0;
			// Looping through number of bottom rows
			for (int j = 0; j < 1 + 2 * i; j++) {

				// Getting row and column position (starting)
				int rowPosition = ave - i + count;
				int colPosition = str + dangerRange - i;		

				// only drawing if location exists on the grid
				if (rowPosition >= 0 && rowPosition < grid.length && colPosition >= 0 && colPosition < grid[0].length) {
					if (forPlayerRange) {
						rangeCoordinates[index][0] = rowPosition;
						rangeCoordinates[index][1] = colPosition;
						index++;
					} else {
						grid[rowPosition][colPosition] += 1;
					}
				}
				count++;
			}
		}
		// Middle Row of circle
		for (int i = 0; i < 1 + dangerRange * 2; i++) {

			// Getting starting row and column positions
			int rowPosition = ave - dangerRange + i;
			int colPosition = str;

			// Drawing row
			if (rowPosition >= 0 && rowPosition < grid.length && colPosition >= 0 && colPosition < grid[0].length && rowPosition != ave) {
				if (forPlayerRange) {
					rangeCoordinates[index][0] = rowPosition;
					rangeCoordinates[index][1] = colPosition;
					index++;
				} else {
					grid[rowPosition][colPosition] += 1;
				}
			}
		}

		// Setting rest of empty indexes to -1
		while (index != rangeCoordinates.length) {
			rangeCoordinates[index][0] = -1;
			index++;
		}
		return rangeCoordinates;
	}
	
	/**
	 * Draws out battle grid and determines dangerous areas
	 * @param data - All info about other robots
	 * @return the mapped 2D battle grid 
	 */
	private int[][] mapDangerArea(OppData [] data) {
		
		// Creates a grid which represents the battle arena
		int [][] grid = new int[CChenBattleManager.WIDTH][CChenBattleManager.HEIGHT];

		// Drawing possible threats onto the grid
		for (int i = 0; i < data.length; i++) {

			// Only alive robots will be considered
			if (data[i].getHealth() > 0 && i != this.getID()) {
				grid[data[i].getAvenue()][data[i].getStreet()] += this.playerValue;
				drawDangerCircle(grid, data[i].getAvenue(),data[i].getStreet(), 3, false);
			}
		}

		return grid;
	}
	
	/**
	 * Sets robot label and color
	 */
	public void setLabel() {
		
		// Sets the robot label and color
		this.setLabel("#" + this.getID() + "HP:" + this.health);
		if (this.health <= 0) {
			this.setColor(Color.BLACK);
		} else {
			this.setColor(Color.BLUE);
		}
	}
}
