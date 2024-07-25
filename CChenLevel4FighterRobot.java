package robotWar;

import becker.robots.*;
import java.util.*;
import java.awt.Color;

/**
 * Level4 Robot which uses an extended record to filter and determine moves
 * 
 * @author Caden Chen
 * @version 2024-06-14
 */
public class CChenLevel4FighterRobot extends FighterRobot{

	// Default value
	private int health = 100; 

	// Grid visualization variables
	private int playerValue = 5;
	private int checkRange = 3;

	// Robot States
	private boolean onAttack = false;
	private int roundNum = 0;

	private CChenRobotData [] robotData = new CChenRobotData[CChenBattleManager.NUM_PLAYERS];

	/**
	 * Constructor, initializes variables and creates the robot
	 * 
	 * @param c - robot city
	 * @param a - robot avenue
	 * @param s - robot street
	 * @param d - robot direction
	 * @param id - robot ID
	 * @param maxHealth - default health
	 */
	public CChenLevel4FighterRobot (City c, int a, int s, Direction d, int id, int maxHealth){
		super(c, a, s, d, id, 5, 4, 1);
		this.health = maxHealth;
		this.setLabel();
	}

	/**
	 * The method which returns a turn to the battle manager
	 */
	@Override
	public TurnRequest takeTurn(int energy, OppData[] data) {

		int alivePlayers = 0;
		int aliveID = -1;

		// Updating all data
		this.roundNum++;
		this.updateRobotData(data);
		this.onAttack = false;

		// Updating alive players
		for (int i = 0; i < CChenBattleManager.NUM_PLAYERS; i++) {

			// Checking if alive
			if (this.robotData[i].getHealth() > 0 && this.robotData[i].getID() != this.getID()) {
				alivePlayers ++;
				aliveID = this.robotData[i].getID();
			}
		}

		// Write stats for testing here
		/*
		robotData[this.findRobotIndex(0)].writeStats(2,2,2);
		robotData[this.findRobotIndex(1)].writeStats(6,2,2);
		robotData[this.findRobotIndex(2)].writeStats(2,2,2);
		 */

		// Looks for the best target in a pool of 5 potential candidates
		int target = this.getTarget(5, 0.8, 0.2);
		int maxAttacks = this.getMaxAttacks(energy);
		// System.out.println("TARGET: " + target);

		// Possible move instructions
		int [] movesAttack = moveInstruction(this.robotData[target].getAvenue(), this.robotData[target].getStreet(), energy);
		int [] movesEscape = moveInstruction(this.findEscape()[0][0], this.findEscape()[0][1], energy);

		// case with 1v1. Might activate ROCK STRATEGY!!!!
		if (alivePlayers == 1) {

			// Checks if easily able to kill, if not stays still and defends (hope the other robot attack/defend -- most unfavourable fight for them)
			if (this.robotData[findRobotIndex(aliveID)].getPredictedDefence() < this.getAttack()) {
				
				// Attacks robot
				if (this.getAvenue() + movesAttack[0] == this.robotData[target].getAvenue() && this.getStreet() + movesAttack[1] == this.robotData[target].getStreet()) {
					this.onAttack = true;
					return new TurnRequest(this.getAvenue() + movesAttack[0], this.getStreet() + movesAttack[1], this.robotData[target].getID(), maxAttacks);
				} else {
					return new TurnRequest(this.getAvenue() + movesAttack[0], this.getStreet() + movesAttack[1] , -1, 0);
				}
			} else {
				
				// Checks if is reputable source of data
				if (this.robotData[findRobotIndex(aliveID)].getNumEncounters() > 0) {
					return new TurnRequest(this.getAvenue(), this.getStreet() , -1, 0);
				} else {
					
					// Attacks the robot
					if (this.getAvenue() + movesAttack[0] == this.robotData[target].getAvenue() && this.getStreet() + movesAttack[1] == this.robotData[target].getStreet()) {
						this.onAttack = true;
						return new TurnRequest(this.getAvenue() + movesAttack[0], this.getStreet() + movesAttack[1], this.robotData[target].getID(), maxAttacks);
					} else {
						return new TurnRequest(this.getAvenue() + movesAttack[0], this.getStreet() + movesAttack[1] , -1, 0);
					}
				}
			}
		}

		// If its first round tries to spread out more
		if (this.roundNum == 1) {
			return new TurnRequest(this.getAvenue() + movesEscape[0], this.getStreet() + movesEscape[1], -1, 0);
		} else {

			// If really low energy skips turn to save
			if (energy < 10) {
				return new TurnRequest(this.getAvenue(), this.getStreet(), -1, 0);
			}

			// If health is below a certain amount, attacks to regain health
			if (this.health < 15) {

				// Checks if it can attack the robot on the turn. 
				if (this.getAvenue() + movesAttack[0] == this.robotData[target].getAvenue() && this.getStreet() + movesAttack[1] == this.robotData[target].getStreet()) {
					this.onAttack = true;
					return new TurnRequest(this.getAvenue() + movesAttack[0], this.getStreet() + movesAttack[1], this.robotData[target].getID(), maxAttacks);
				} else {
					return new TurnRequest(this.getAvenue() + movesAttack[0], this.getStreet() + movesAttack[1] , -1, 0);
				}
			}

			// If area threat is high, escapes. Otherwise looks for an attack
			if (this.mapDangerArea()[this.getAvenue()][this.getStreet()] > 30) {
				return new TurnRequest(this.getAvenue() + movesEscape[0], this.getStreet() + movesEscape[1], -1, 0);
			} else {

				// Determines if attacking area is safe. Otherwise stays still
				if (this.mapDangerArea()[this.getAvenue() + movesAttack[0]][this.getStreet() + movesAttack[1]] < 30) {

					// Checks if it can attack the robot on the turn
					if (this.getAvenue() + movesAttack[0] == this.robotData[target].getAvenue() && this.getStreet() + movesAttack[1] == this.robotData[target].getStreet()) {
						this.onAttack = true;
						return new TurnRequest(this.getAvenue() + movesAttack[0], this.getStreet() + movesAttack[1], this.robotData[target].getID(), maxAttacks);
					} else {
						return new TurnRequest(this.getAvenue() + movesAttack[0], this.getStreet() + movesAttack[1] , -1, 0);
					}
				} else {
					return new TurnRequest(this.getAvenue(), this.getStreet(), -1, 0);
				}
			}
		}
	}

	/**
	 * Gets the maximum attack number for the robot given the energy level
	 * 
	 * @param energy
	 * @return
	 */
	private int getMaxAttacks(int energy) {
		int currentEnergy = energy;
		int counter = 0; 

		// Assume robot loses some rounds... almost :/ 
		while (currentEnergy - 10 > 0) {
			currentEnergy -= 10;
			counter++;
		}

		// Ensures in attack range
		if (counter > this.getAttack()) {
			counter = this.getAttack();
		}

		return counter;
	}

	/**
	 * The method that is called after a battle - used to analyze stats
	 */
	@Override
	public void battleResult(int healthLost, int oppID, int oppHealthLost, int numRoundsFought) {

		this.health = this.health - healthLost;		

		if (oppID != -1 && this.roundNum != 0) {
			// System.out.println("ID: " + oppID);
			// Update the Opponent data
			this.robotData[findRobotIndex(oppID)].updateFightData(!this.onAttack, healthLost, oppHealthLost, numRoundsFought, this.getAttack(), this.getDefence());			
		}
	}

	/**
	 * Updates all robot data
	 * 
	 * @param data - Given array of data
	 */
	private void updateRobotData(OppData [] data) {

		// Loops through each robot in OppData
		for (int i = 0; i < this.robotData.length; i++) {

			// Adding the data to robotData if it doesn't exist already, otherwise updates robotData entries
			if (this.robotData[i] == null) {
				this.robotData[i] = new CChenRobotData(data[i].getID(), data[i].getAvenue(), data[i].getStreet(), data[i].getHealth());
			} else {
				this.robotData[this.findRobotIndex(i)].setAvenue(data[i].getAvenue());
				this.robotData[this.findRobotIndex(i)].setStreet(data[i].getStreet());
				this.robotData[this.findRobotIndex(i)].setHealth(data[i].getHealth());
			}
			this.robotData[this.findRobotIndex(i)].updateData();
		}	
		this.updateMyData();
	}

	/**
	 * Sorts robotData to find the weakest robots based on their attack and defense values
	 */
	private void sortPotentialTargets(double distanceWeight, double healthWeight) {

		// Insertion sort to find best robots to target 
		for (int i = 1; i < this.robotData.length; i++) {
			int j = i;
			while (j > 0 && (this.findDistance(this.getAvenue(), this.getStreet(), this.robotData[j].getAvenue(), this.robotData[j].getStreet())/30.0)*distanceWeight + ((this.robotData[j].getHealth())/100.0)*healthWeight <  (this.findDistance(this.getAvenue(), this.getStreet(), this.robotData[j-1].getAvenue(), this.robotData[j-1].getStreet())/30.0)*distanceWeight + ((this.robotData[j-1].getHealth())/100.0)*healthWeight){
				CChenRobotData temp = this.robotData[j-1];
				this.robotData[j-1] = this.robotData[j];
				this.robotData[j] = temp;
				j--;
			}
		}

		/*
		for (int i = 0; i < this.robotData.length; i++) {
			System.out.println("iD: " + this.robotData[i].getID() + " distance: " + this.findDistance(this.getAvenue(), this.getStreet(), this.robotData[i].getAvenue(), this.robotData[i].getStreet()));
		}
		 */
	}

	/**
	 * Finds the lowest statistically ranking robot based on predicted attack value, predicted defense value, and win/loss rates
	 * 
	 * @param searchRange - Amount of robots to consider
	 * @return - The index of robot
	 */
	private int findLowestStat(int searchRange) {

		int avaliableRobots = 0;

		// Finding number of alive robots 
		for (int i = 0; i < this.robotData.length; i++) {
			if (this.robotData[i].getHealth() > 0 && this.robotData[i].getID() != this.getID()) {
				avaliableRobots++;
			}
		}

		// If searches more than the alive robots limits the search
		if (searchRange > avaliableRobots) {
			searchRange = avaliableRobots;
		}

		// Counters & targets
		int countedRobots = 0;
		int index = 0;
		int target = -1;
		int minStat = Integer.MAX_VALUE;

		// Searches until looks through desired number of robots
		while (countedRobots < searchRange) {

			// Determines if its a valid search
			if (this.robotData[index].getID() != this.getID() && this.robotData[index].getHealth() > 0) {

				// Records if it has the lowest statistics
				if (this.robotData[index].getAttackWinLoss() + this.robotData[index].getDefenceWinLoss() + this.robotData[index].getPredictedAttack() + this.robotData[index].getPredictedDefence() < minStat) {
					minStat = this.robotData[index].getAttackWinLoss() + this.robotData[index].getDefenceWinLoss() + this.robotData[index].getPredictedAttack() + this.robotData[index].getPredictedDefence();
					target = index;
				}

				countedRobots++;
			}
			index++;
		}

		return target;

	}

	/**
	 * Gets the target for the robot.
	 * 
	 * @param searchRange - number of robots to search in the second filter
	 * @param distanceWeight - Weight of distance in first filter
	 * @param healthWeight - Weight of health in the first filter
	 * 
	 * @return - index of wanted target robot
	 */
	private int getTarget(int searchRange, double distanceWeight, double healthWeight) {
		this.sortPotentialTargets(distanceWeight, healthWeight);
		return this.findLowestStat(searchRange);
	}

	/**
	 * Provides movement instructions relative to the robot's current position
	 * 
	 * @param targetA - Target avenue position
	 * @param targetS - Target street position
	 * @param energy - Current energy level
	 * 
	 * @return - The avenue and street move instructions
	 */
	private int[] moveInstruction(int targetA, int targetS, int energy) {

		// allocate some energy
		energy = energy - 10;

		int[] moveInstructions = new int[2];

		// returns no moves if energy is 0
		if (energy < 0) {
			moveInstructions[0] = 0;
			moveInstructions[1] = 0;
			return moveInstructions;
		}

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
	 * 
	 * @param x1 - First avenue location
	 * @param y1 - First street location
	 * @param x2 - Second avenue location
	 * @param y2 - Second street location
	 * 
	 * @return - Horizontal + vertical distance 
	 */
	private int findDistance(int x1, int y1, int x2, int y2) {
		return Math.abs(x1-x2) + Math.abs(y1-y2);
	}

	/**
	 * Moves the robot towards a specific location
	 */
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

	/**
	 * Finds a robot index in the robotData array given its ID
	 * 
	 * @param id - The robot ID
	 * 
	 * @return The robot index position
	 */
	private int findRobotIndex(int id) {

		int index = 0;

		// Loops until finds the correct index of id
		for (int i = 0; i < this.robotData.length; i++) {
			if (this.robotData[i].getID() == id) {
				index = i;
				break;
			}
		}

		return index;
	}

	/**
	 * Turns the robot to a direction
	 * 
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
	 * 
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
	 * 
	 * @param data - The data of all the robots
	 * @return - The safest coordinate
	 */
	private int[][] findEscape() {

		// Creating arrays
		int[][] grid = this.mapDangerArea();
		int[][] rangeCoordinates = drawDangerCircle(grid,this.getAvenue(), this.getStreet(), this.getNumMoves(), 0, true);
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
	 * 
	 * @param grid - The battle grid
	 * @param ave - Avenue position
	 * @param str - Street position
	 * 
	 * @return The sum
	 */
	private int sumOfEscape(int[][] grid, int ave, int str) {

		// Getting surrounding escape locations
		int[][] coordinates = drawDangerCircle(grid, ave, str, this.checkRange, 0,  true);
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
	 * 
	 * @param grid - Battle grid
	 * @param ave - Avenue position
	 * @param str - Street position 
	 * @param dangerRange - The range of the robot/point movement 
	 * @param forPlayerRange - If the range is for a robot or for a point
	 * 
	 * @return - Danger coordinates for other uses
	 */
	private int[][] drawDangerCircle(int[][] grid, int ave, int str, int dangerRange, int gridValue, boolean forPlayerRange) {

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
						grid[rowPosition][colPosition] += gridValue;
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
						grid[rowPosition][colPosition] += gridValue;
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
					grid[rowPosition][colPosition] += gridValue;
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
	 * 
	 * @param data - All info about other robots
	 * @return the mapped 2D battle grid 
	 */
	private int[][] mapDangerArea() {

		// Creates a grid which represents the battle arena
		int [][] grid = new int[CChenBattleManager.WIDTH][CChenBattleManager.HEIGHT];

		// Drawing possible threats onto the grid
		for (int i = 0; i < this.robotData.length; i++) {

			// Only alive robots will be considered
			if (this.robotData[i].getHealth() > 0 && this.robotData[i].getID() != this.getID()) {

				grid[this.robotData[i].getAvenue()][this.robotData[i].getStreet()] += this.playerValue;
				drawDangerCircle(grid, this.robotData[i].getAvenue(), this.robotData[i].getStreet(), this.robotData[i].getPredictedMoveNum(),  this.robotData[i].getPredictedAttack(), false);
			}
		}

		/*
		for (int i =0 ; i < grid[i].length; i++) {
			for (int j = 0; j < grid.length; j++) {
				System.out.print(grid[j][i] + " ");
			}
			System.out.println();
		}
		 */

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
			this.setColor(new Color(216,185,255));
		}
	}

	/**
	 * Updates this robots data and other robots data for testing
	 */
	private void updateMyData() {
		robotData[this.findRobotIndex(this.getID())].writeStats(this.getAttack(), this.getDefence(), this.getNumMoves());
	}
}
