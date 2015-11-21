package karataiev.dmytro.connectfour;
import java.util.Random;

/**
 *  Simple computer algorithm which tries to prevent you from winning and checks 1 move ahead if you or he can win
 */
public class MyAgent extends Agent
{
    Random r;

    /**
     * Constructs a new agent, giving it the game and telling it whether it is Red or Yellow.
     * 
     * @param game The game the agent will be playing.
     * @param iAmRed True if the agent is Red, False if the agent is Yellow.
     */
    public MyAgent(Connect4Game game, boolean iAmRed)
    {
        super(game, iAmRed);
        r = new Random();
    }

    /**
     * Move algorithm, first move always if available is on column 3 (middle of the field)
     * All the rest moves try to predict a winning move, if there are no possible interesting moves - moves at random.
     */
    public void move()
    {
    	if (iAmRed && myGame.isEmpty() || getLowestEmptyIndex(myGame.getColumn(3)) == 5)
    	{
    		moveOnColumn(3);
    	}
    	else
    	{
    		if (threeLine(myGame.getBoardMatrix(), iAmRed) != -1)
    		{
    			moveOnColumn(threeLine(myGame.getBoardMatrix(), iAmRed));
    		}
    		else if (threeLine(myGame.getBoardMatrix(), !iAmRed) != -1)
    		{
    			moveOnColumn(threeLine(myGame.getBoardMatrix(), !iAmRed));
    		}
    		else
    		{
    			if (getFirstPos(getColor(iAmRed)) != -1 && !predict(getFirstPos(getColor(iAmRed))))
    			{
    				moveOnColumn(getFirstPos(getColor(iAmRed)));
    			}
    			else
        		{
        			int rand = randomMove();
        			int iter = 0;
        			while (predict(rand) && iter < availCol())
        			{
        				rand = randomMove();
        				iter++;
        			}
                    moveOnColumn(rand);
        		}
    		}
    	}
    }

    /**
     * Returns the name of this agent.
     *
     * @return the agent's name
     */
    public String getName()
    {
        return "Beginner";
    }
    
    /**
     * Recommends column to move (tries to put the ball adjacent to other balls of same color)
     * 
     * @param color - color of the ball to find a position
     * @return recommended column to make a move
     */
    public int getFirstPos(char color)
    {
    	char[][] matrix = myGame.getBoardMatrix();
    	
    	for (int row = 0, n = myGame.getRowCount(); row < n; row++)
    	{
    		for (int column = 0, n2 = myGame.getColumnCount(); column < n2; column++)
    		{
    			if (matrix[row][column] == color)
    			{
    				if (column > 0 && matrix[row][column - 1] == 'B' && getLow(matrix, column - 1) == row)
    				{
    					return column - 1;
    				}
    				else if (column < n2 - 1 && matrix[row][column + 1] == 'B' && getLow(matrix, column + 1) == row)
    				{
    					return column + 1;
    				}
    			}
    		}
    	}
    	return -1;
    }
    
    
    
    
    /**
     *  Method to check all available winning moves. Also is used in prediction method.
     *  
     * @param matrix (current or possible field)
     * @param mainPlayer (player to check if he is winning/losing)
     * @return column to win
     */
    public int threeLine(char[][] matrix, boolean mainPlayer)
    {

    	char color = getColor(mainPlayer);

        int vertical = 0;
    	int horizontal = 0;

    	int row = 0;
    	int column = 0;

        // check for three balls vertically aligned
    	for (column = 0; column < matrix[0].length; column++)
    	{
    		vertical = 0;
    		for (int j = matrix.length - 1; j > 0; j--)
    		{
    			if (matrix[j][column] == color)
    			{
    				vertical++;
    				if (vertical == 3 && matrix[j - 1][column] == 'B')
    				{
    					return column;
    				}
    			}
    			else
    			{
    				vertical = 0;
    			}
    		}
    	}

        // check for 2 or 3 balls in a row
    	for (row = matrix.length - 1; row > -1; row--)
    	{
    		horizontal = 0;
    		for (column = 0; column < matrix[0].length; column++)
    		{
    			if (matrix[row][column] == color)
    			{
    				horizontal++;
                    if (horizontal == 3)
                    {
                        if (column - horizontal > -1 && matrix[row][column - horizontal] == 'B')
                        {
                            if (getLow(matrix, (column - horizontal)) == row)
                            {
                                return column - horizontal;
                            }
                        }
                        else if (column < matrix[0].length - 1 && matrix[row][column + 1] == 'B')
                        {
                            if (getLow(matrix, (column + 1)) == row)
                            {
                                return column + 1;
                            }
                        }
                    }
                    else if (horizontal == 2)
    				{
    					if ((column - horizontal > -1 && matrix[row][column - horizontal] == 'B')
    							&& column - horizontal - 1 > -1 && matrix[row][column - horizontal - 1] == color)
    					{
    						if (getLow(matrix, (column - horizontal)) == row)
    						{
    							return column - horizontal;
    						}
    					}
    					else if ((column < matrix[0].length - 1 && matrix[row][column + 1] == 'B') 
    							&& column + 1 < matrix[0].length - 1 && matrix[row][column + 2] == color)
    					{
    						if (getLow(matrix, (column + 1)) == row)
    						{
    							return column + 1;
    						}
    					}
    				}
    			}
    			else { horizontal = 0; }
    		}
    	}
    	
    	// diagonal checks
    	for (column = 0; column < matrix[0].length; column++)
    	{
    		for (row = 0; row < matrix.length; row++)
    		{
    			if (column + 3 < matrix[0].length && row + 3 < matrix.length)
                {
                    if (color == matrix[row][column] && matrix[row][column] == matrix[row + 1][column + 1])
                    {
                    	if (matrix[row][column] == matrix[row + 3][column + 3] || (row > 2 && matrix[row][column] == matrix[row - 2][column - 2]))
                    	{
                    		if ((column > 1 && getLow(matrix, (column - 1)) == row - 1) && (row - 1 != -1))
    						{
                    			return column - 1;
    						}
                    		else if (column < matrix[0].length - 1 && getLow(matrix, (column + 2)) == row + 2)
                    		{
                    			return column + 2;
                    		}
                    	}
                    }
                }
                if (column > 2 && row + 3 < matrix.length)
                {
                    if (color == matrix[row][column] && matrix[row][column] == matrix[row + 1][column - 1])
                    {
                    	if ((matrix[row][column] == matrix[row + 3][column - 3] && matrix[row][column] == matrix[row + 2][column - 2]) || (row > 2 && matrix[row][column] == matrix[row - 2][column + 2]))
                    	{
                    		if (column > 1 && getLow(matrix, (column - 2)) == row + 2)
    						{
                    			return column - 2;
    						}
                    		else if (column < matrix[0].length - 2 && getLow(matrix, (column + 1)) == row - 1)
                    		{
                    			return column + 1;
                    		}
                    	}
                    }
                }
    			if (row > 0 && column + 2 < matrix[0].length && row + 2 < matrix.length)
                {
                    if (color == matrix[row][column] && matrix[row][column] == matrix[row + 1][column + 1] && matrix[row][column] == matrix[row + 2][column + 2])
                    {
                    	if ((column > 0 && getLow(matrix, (column - 1)) == row - 1) && row != -1)
						{
                    		return column - 1;
						}
                    }
                }
                if (row > 0 && column > 2 && row + 2 < matrix.length)
                {
                    if (color == matrix[row][column] && matrix[row][column] == matrix[row + 1][column - 1] && matrix[row][column] == matrix[row + 2][column - 2])
                    {
                    	if ((column < matrix[0].length - 1 && getLow(matrix, (column + 1)) == row - 1) && row != -1)
						{
                    		return column + 1;
						}
                    }
                }
                // Diagonal 0 - 0 0 (one ball separate from 2 in diagonal)
                if (column + 3 < matrix[0].length && row + 3 < matrix.length)
                {
                    if(matrix[row][column] == color && matrix[row + 1][column + 1] == 'B' && matrix[row][column] == matrix[row + 2][column + 2] && matrix[row][column] == matrix[row + 3][column + 3])
                    {
                    	if (getLow(matrix, (column + 1)) == row + 1)
						{
                    		return column + 1;
						}
                    }
                }
                if (column > 2 && row + 3 < matrix.length)
                {
                    if (matrix[row][column] == color && matrix[row + 1][column - 1] == 'B' && matrix[row][column] == matrix[row + 2][column - 2] && matrix[row][column] == matrix[row + 3][column - 3])
                    {
                    	if (getLow(matrix, (column - 1)) == row + 1)
						{
                    		return column - 1;
						}
                    }
                }
    		}
    	}
    	return -1;
    }


    /**
     * gets color, depending on who moves first
     *
     * @param color of current player
     * @return char of color
     */
    public char getColor(boolean color)
    {
    	if (color)
    	{
    		return 'R';
    	}
    	else
    	{
    		return 'Y';
    	}
    }

    /**
     * Method to predict 1 future move, returns winning move if it exists
     * @param column to check if winning move is possible
     * @return true if there is a winning move
     */
    public boolean predict(int column)
    {
    	char[][] matrix = myGame.getBoardMatrix();

        // temp array to check a move
    	char[][] temp = new char[myGame.getRowCount()][myGame.getColumnCount()];
    	arrayCopy(matrix, temp);

    	char color = getColor(!iAmRed);
    	int row = getLowestEmptyIndex(myGame.getColumn(column));
    	
    	temp[row][column] = color;
    	
    	if (threeLine(temp, !iAmRed) != -1)
    	{
    		return true;
    	}

    	return false;
    }

    /**
     * Method to copy an array
     * @param aSource source array
     * @param aDestination new array
     */
    public static void arrayCopy(char[][] aSource, char[][] aDestination) {
        for (int i = 0; i < aSource.length; i++) {
            System.arraycopy(aSource[i], 0, aDestination[i], 0, aSource[i].length);
        }
    }

    /**
     * Method to return number of not full columns
     * @return number of columns where move is possible
     */
    public int availCol()
    {
    	int availColumns = 0;
    	
    	for (int i = 0; i < myGame.getColumnCount(); i++)
    	{
    		if (!myGame.getColumn(i).getIsFull())
    		{
    			availColumns++;
    		}
    	}
    	
    	return availColumns;
    }

    /**
     * Method to find the lowest possible row to move
     * @param matrix to check
     * @param column to check
     * @return lowest row to make a move to
     */
    public int getLow(char[][] matrix, int column)
    {
    	int lowest = -1;
    	
    	for (int row = 0; row < matrix.length; row++)
    	{
    		if (matrix[row][column] == 'B')
    		{
    			lowest = row;
    		}
    	}
    	return lowest;
    }

    /**
     * Returns a random valid move. If your agent doesn't know what to do, making a random move
     * can allow the game to go on anyway.
     *
     * @return a random valid move.
     */
    public int randomMove()
    {
        int i = r.nextInt(myGame.getColumnCount());
        while (getLowestEmptyIndex(myGame.getColumn(i)) == -1)
        {
            i = r.nextInt(myGame.getColumnCount());
        }
        return i;
    }


}
