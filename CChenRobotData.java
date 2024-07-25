package robotWar;

/**
 * Extended record to hold more statistic and battle information
 * @author Caden Chen
 * @version 2024-06-13
 */
public class CChenRobotData extends OppData{

	// Position of the robot
	private int[] currentPosition = {this.getAvenue(),this.getStreet()};
	private int[] pastPosition = {this.getAvenue(), this.getStreet()};
	
	// Predicted Statistical Values
	private int predictedMoveValue = 1;
	private double predictedAttackValue = 3;
	private double predictedDefenceValue = 3;
	
	// If data was actually logged
	private boolean fakeAttack = true;
	
	// Wins and losses
	private int attackWinLoss = 0;
	private int defenceWinLoss = 0;
	private int totalAttackEncounters = 0;
	private int totalDefenceEncounters = 0;
	
	/**
	 * Initializes robot data
	 * 
	 * @param id - ID of the robot
	 * @param a - avenue of the robot
	 * @param s - street of the robot
	 * @param health - health of the robot
	 */
	public CChenRobotData (int id, int a, int s, int health) {
		super(id,a,s,health);
	}
	
	public void updateData() {
		this.updatePosition();
	}
	
	/**
	 * Updates and analyzes the post fight data to predict statistics
	 * 
	 * @param onAttack - If the robot was attacking or defending
	 * @param oppHealthLost - The amount of health opponent lost
	 * @param myHealthLost - The health robot lost
	 * @param numRoundsFought - The number of rounds
	 * @param oppAttackVal - The opponents attack value
	 * @param oppDefenceVal - the opponents defence value 
	 */
	public void updateFightData(boolean onAttack, int oppHealthLost, int myHealthLost, int numRoundsFought, int oppAttackVal, int oppDefenceVal) {
				
		// Updating win and loss records - ties are not recorded
		if (onAttack) {
			this.totalAttackEncounters++;
		} else {
			this.totalDefenceEncounters++;
		}
		
		// Updating attack and defence wins
		if (oppHealthLost > myHealthLost) {
			if (onAttack) {
				this.attackWinLoss++;
			} else {
				this.defenceWinLoss++;
			}
		} else if (oppHealthLost < myHealthLost) {
			if (onAttack) {
				this.attackWinLoss--;
			} else {
				this.defenceWinLoss--;
			}
		}
		
		// Creating learning rates based on total encounters
	    double attackLearningRate = 1.0/this.totalAttackEncounters;
	    double defenceLearningRate = 1.0/this.totalDefenceEncounters;

	    // Prediction adjusts based on number of encounters
	    if (onAttack) {
	        this.predictedAttackValue += attackLearningRate * (oppHealthLost - this.predictedAttackValue);
	        this.fakeAttack = false;
	    } else {
	        this.predictedDefenceValue += defenceLearningRate * (myHealthLost - this.predictedDefenceValue);
	    }
		
		// Final adjustment 
		if (onAttack) {
			if (this.predictedAttackValue < numRoundsFought || this.fakeAttack) {
				this.predictedAttackValue = numRoundsFought;
				this.fakeAttack = false;
			}
		}

		// Making sure it adds up
		this.predictedDefenceValue = 10 - this.predictedAttackValue - this.predictedMoveValue;
	}
	
	/**
	 * Updates the position tracking of the robot and finds robot displacement. Updates predicted move value 
	 */
	public void updatePosition() {

		// Update robot position
		this.currentPosition[0] = this.getAvenue();
		this.currentPosition[1] = this.getStreet();

		// Find moves taken
		int moves = this.findDistance(currentPosition[0],currentPosition[1],pastPosition[0],pastPosition[1]);
		this.pastPosition[0] = this.currentPosition[0];
		this.pastPosition[1] = this.currentPosition[1];

		// Updates moves if more than previous value
		if (moves > this.predictedMoveValue) {
			this.predictedMoveValue = moves;
			System.out.println("Robot #" + this.getID() + ":" + " Updated moves to " + moves);
		}
	}

	/**
	 * Finds the distance between 2 points on the battlefield (Avenue + Street)
	 * 
	 * @param x1 - Avenue of first point
	 * @param y1 - Street of first point
	 * @param x2 - Avenue of second point
	 * @param y2 - Street of second point 
	 * 
	 * @return The distance between the two points
	 */
	private int findDistance(int x1, int y1, int x2, int y2) {
		return Math.abs(x1-x2) + Math.abs(y1-y2);
	}
	
	/**
	 * Gets the predicted move value of the robot
	 * 
	 * @return Predicted move value
	 */
	public int getPredictedMoveNum() {
		return this.predictedMoveValue;
	}
	
	/**
	 * Gets the predicted attack value of the robot
	 * 
	 * @return The predicted attack value
	 */
	public int getPredictedAttack() {
		return (int) Math.round(this.predictedAttackValue);
	}
	
	/**
	 * Gets the predicted defence value of the robot
	 * 
	 * @return the predicted defence value
	 */
	public int getPredictedDefence() {
		return (int) Math.round(this.predictedDefenceValue);
	}
	
	/**
	 * Gets the attack/loss of the robot on attack
	 * 
	 * @return an integer which represents rounds won - rounds lost for attack
	 */
	public int getAttackWinLoss() {
		return this.attackWinLoss;
	}
	
	/**
	 * Gets the attack/loss of the robot on defence
	 * 
	 * @return an integer which represents rounds won - rounds lost for defence
	 */
	public int getDefenceWinLoss() {
		return this.defenceWinLoss;
	}
	
	/**
	 * Gets the total encounters for the robot
	 * 
	 * @return Attack + Defence encounters
	 */
	public int getNumEncounters() {
		return this.totalAttackEncounters + this.totalDefenceEncounters;
	}
	
	
	/**
	 * Writes statistics to the robot.
	 * 
	 * @param attackVal - Attack value to write
	 * @param defenceVal - Defence value to write
	 * @param moveVal - Move value to write
	 */
	public void writeStats(int attackVal, int defenceVal, int moveVal) {
		this.predictedAttackValue = attackVal;
		this.predictedDefenceValue = defenceVal;
		this.predictedMoveValue = moveVal;
	}


}
