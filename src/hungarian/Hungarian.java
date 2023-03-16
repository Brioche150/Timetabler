/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package hungarian;
import KVPair.*;
import LinkedStructures.LinkedList;
/**
 *
 * @author Antony Admin
 */
/**
 * Hungarian assigns workers to tasks given a cost matrix, assigning every worker to one task.
 */
public class Hungarian {
    
    private int[][] matrix;
    private int[][] originalMatrix;
    /*
    this is the cost matrix that the algorithm will be assigning from
    there is an original matrix to return the actual costs of each assignment, because 'matrix' will be changed
    it needs to stay to check if the algorithm returns an assignment with 'infinite' getValue, which would happen if dummy columns or rows were made
     */
    private int bigNumber; // the 'infinite getValue'.
   
    private LinkedList<KVPair<Integer,Integer>> zeroCoordinates;
    //this could be slower than just looking iterating through the matrix if there are a lot of covered lines and zeros, but I prefer it.
    
    private boolean[] rowsCrossed;
    private boolean[] columnsCrossed;
    //these just track which columns and lines have been croseed
    private int[] starInRow;
    private int[] starInColumn;
    private int[] primeInRow;
   /*
   The index of these arrays stores the row/column of the starred or primed zero
   the getValue stores then stores the column/row of that zero.
   The way these arrays work, it can only store one starred zero in any row or column
   And it can only store one primed zero in any row, but there can be multiple in the same column
    */
    
    
    /**
     * Hungarian calculates the assignments where all jobs are assigned to one worker and results in the lowest cost.
     * @param matrix Matrix is a square matrix that stores the cost of assigning each worker to each job
     * A rectangular matrix can be made square by adding dummy rows or columns of 'infinite' cost
     */
    public Hungarian(int[][] matrix)
    {
        //this section here just makes sure that the matrix is square, which the algorithm needs to work.
        int longestLength = matrix.length;
        boolean isSquare = true;
        for (int[] row : matrix) 
        {
            
            if(row.length > longestLength)
            {
                longestLength = row.length;
                isSquare = false;
            }
            else if (row.length > longestLength)
                isSquare = false;
        }
        if(!isSquare)
            throw new IllegalArgumentException("The matrix you have passed in is not square, so this algorithm will not work.");
        
        
        /*
        arrays aren't primitive, so clone does shallow reference copy, instead of getValue.
        This is needed to separate the matrices I use from the one passed in, because the user could need that.
        clone doesn't work on a 2D array, but it does on 1D arrays
        */
        this.matrix = new int[longestLength][longestLength];
        for (int i = 0; i < matrix.length; i++) 
        {
            this.matrix[i] = matrix[i].clone(); 
        }
        this.originalMatrix = new int[longestLength][longestLength];
        for (int i = 0; i < matrix.length; i++) 
        {
            this.originalMatrix[i] = matrix[i].clone(); 
        }
        
        zeroCoordinates = new LinkedList();
        rowsCrossed = new boolean[longestLength];
        columnsCrossed = new boolean[longestLength];
        
        starInColumn = fillArray(new int[longestLength], -1);
        starInRow = fillArray(new int[longestLength], -1);
        primeInRow = fillArray(new int[longestLength], -1);
        //these are filled with false values, to show that is nothing there currently, as there is no -1 coordinate.
        bigNumber = 99999;
        
    }
    /**
     * 
     * Hungarian calculates the assignments where all jobs are assigned to one worker and results in the lowest cost.
     * @param matrix Matrix is a square matrix that stores the cost of assigning each worker to each job
     * @param bigNumber this is an optional parameter that lets the user set their own 'infinite' getValue if they need to know it, like when they make dummy entries in the matrix.
 When a user makes a dummy entry, they should make sure that their dummy entries haven't had overflow if they add something to the maximum integer getValue.
     */
    public Hungarian (int[][] matrix, int bigNumber)
    {
        this(matrix);
        this.bigNumber = bigNumber;
        //gives an option to set their own 'infinite' getValue if they need it to line up in their implementation.
    }
    
   /**
    * 
    * @returns a linked list of KVPairs, which are the coordinates of the assigned workers to tasks.
    * The linked list is in order of rows, as in the outer array of the 2d array. So it will give the assignment in the first index of the outer array, and then the second, etc.
    * It does not return assignments that are of 'infinite' cost, considering them as dummy values to make the matrix square.
    */
    public LinkedList<KVPair<Integer,Integer>> assignments()
    {
        //these are the initial steps needed to generate assignments.
        
        //this just simplifies the matrix to make zeros in the matrix.
        reduceRowCost();
        reduceColumnCost();
        
        /*
        These two functions star zeros and cover columns with them.
        There can only ever be one starred zero in any row or column
        */
        initialAssignments();
        crossThroughStars();
        //once there is a m
        while(!readyToAssign())
        {
            int[] primedZero  = getPrimedZero();
            int row = primedZero[0];
            int column = primedZero[1];
            /*
            what this if statement does.
            If there is a star in the same row as the primed zero,
            Cover the row that they are in
            Uncover the column that the starred zero is in.
            */
            if(starInRow[row] != -1) 
            {
                rowsCrossed[row] = true;
                columnsCrossed[starInRow[row]] = false;
            }
            /*
            if there isn't a starred zero in the same row as the primed zero.
            It will get the coordinates of primed and starred zeros in a path.
            How this path is made is described in the goThroughPath function description.
            With these zeros, primed zeros in the path will be made into starred zeros.
            Starred zeros in the path will be unmarked.
            Then all primed zeros will be unmarked, and all covers removed.
            Then the columns of starred zeros are covered and the while loop continues.
            */
            else
            {
                LinkedList<KVPair<Integer,Integer>> zerosInPath = new LinkedList();
                zerosInPath = goThroughPath(row, column, zerosInPath);
                
                KVPair<Integer,Integer> coordinate;
                
                while(!zerosInPath.isEmpty())
                {
                    //primed zeros can only be set to be starred zeros once the starred zero in its column is removed.
                    coordinate = zerosInPath.pop();
                    int primeRow = coordinate.getKey();
                    int primeColumn = coordinate.getValue();
                    if(!zerosInPath.isEmpty())
                    {
                        coordinate = zerosInPath.pop();
                        int starRow = coordinate.getKey();
                        int starColumn = coordinate.getValue();
                        starInColumn[starColumn] = -1;
                        starInRow[starRow] = -1;
                    }
                    starInColumn[primeColumn] = primeRow;
                    starInRow[primeRow] = primeColumn;
                    
                }
                
               //this removes all primed zeros and all covers on the matrix.
                primeInRow = fillArray(primeInRow, -1);
                rowsCrossed = new boolean[matrix.length];
                columnsCrossed = new boolean[matrix.length];
                //this then covers the columns of starred zeros.
                crossThroughStars();
            }
        }
        //once the matrix is ready to have assignments made
        //this is when there is a starred zero in every row and column
        LinkedList<KVPair<Integer,Integer>> assignmentCoordinates = new LinkedList();
        for (int i = 0; i < matrix.length; i++) {
            int row = i;
            int column = starInRow[row];
            //this condition makes it so that only non-dummy assignments are passed back, because if a dummy row is made with 'infinite' cost, the assignment doesn't matter.
            if(originalMatrix[row][column] < bigNumber)
                assignmentCoordinates.add(new KVPair<Integer,Integer>(row, column));
        }
        return assignmentCoordinates;
    }
    
    /**
     * This is a check to see if the matrix is ready to assign tasks. It is based on if there is a starred zero in every row and column.
     * @return 
     */
    private boolean readyToAssign()
    {
        for (int i = 0; i < matrix.length; i++) 
        {
            //if there isn't a star in that row and column
            if(starInColumn[i] == -1 || starInRow[i] == -1 )
                return false;
        }
        return true;
    }
    /**
     * this reduces every row by subtracting the smallest getValue in the row from every getValue in that row.
     */
    private void reduceRowCost() 
    {
        
        for (int i = 0; i < matrix.length; i++) //goes through the rows 
        {
            int lowest = bigNumber;
            int[] row = matrix[i];
            //finds the lowest number in the row
            for (int j = 0; j < row.length; j++) 
            {
                if(lowest > row[j])
                    lowest = row[j];
            }
            if(lowest != 0)
            {
                //after getting the lowest getValue, it is subtracted from all of the entries on that row.
                for (int j = 0; j < row.length; j++) 
                {
                    row[j] -= lowest;
                    if(row[j] == 0)
                    {
                        // coordinates is ordered with the first being the row index, then the column index
                        // whenever a 0 is made, it's added to the zeroCoordinates list.
                        KVPair<Integer,Integer> coordinate = new KVPair(i,j); 
                        zeroCoordinates.add(coordinate);
                    }
                        
                }
            }
            
            
        }
    }
    /**
     * this reduces every column by subtracting the smallest getValue in the column from every getValue in that column.
     */
    private void reduceColumnCost()
    {
        
        for (int i = 0; i < matrix.length; i++) 
        { 
            int lowest =  bigNumber;
            for (int j = 0; j < matrix.length; j++) 
            {
                if(lowest > matrix[j][i]) // getting the values in the same place in each row, so basically a column.
                    lowest = matrix[j][i];
            }
            if(lowest != 0)
            {
                for (int j = 0; j < matrix.length; j++) 
                {
                    matrix[j][i] -= lowest;
                    if(matrix[j][i] == 0)
                    {
                        KVPair<Integer,Integer> coordinate = new KVPair(j,i);
                        zeroCoordinates.add(coordinate); 
                    }

                }
            }
            
            
            
        }
    }
   /**
    * assigns zeros by marking them as starred zeros. There can only be one starred zero in any row or column.
    */
    private void initialAssignments()
    {
        //stars the zeros being assigned for the first time.
        for (KVPair<Integer, Integer> coordinate : zeroCoordinates) 
        {
            //going through every zero can be more inefficient than writing off whole columns or rows that are covered, but that's only really if there are a lot of zeros.
            int row = coordinate.getKey();
            int column = coordinate.getValue();
            if(!(starInColumn[column] != -1 || starInRow[row] != -1))
            {
                //this means that the coordinates of a starred zero is saved if there isn't starred zero in the same row or column
                starInColumn[column] = row;
                starInRow[row] = column;
            }
        }
    }
    /**
     * This covers all columns that have starred zeros in them.
     */
    private void crossThroughStars()
    {
        for (int i = 0; i < matrix.length; i++) 
        {
            if(starInColumn[i] != -1)
                columnsCrossed[i] = true;   
        }
    }
    /**
     * A primed zero is just a zero that isn't covered by any other line at the moment.
     * If there aren't any uncovered zeros in the matrix, then zeros are generated. This is done by getting the smallest uncovered value.
     * This value is then subtracted from all uncovered values, and added to any values covered by 2 lines.
     * @return an int array of length 2. It will be the coordinates of the primed zero
     */
    @SuppressWarnings("empty-statement")
    private int[] getPrimedZero()
    {
        int[] primeCoordinates = new int[2];
        for (KVPair<Integer, Integer> coordinate : zeroCoordinates) 
        {
            int row = coordinate.getKey();
            int column = coordinate.getValue();
            if(columnsCrossed[column] == false && rowsCrossed[row] == false) 
            {
                primeInRow[row] = column;
                primeCoordinates[0] = row;
                primeCoordinates[1] = column;
                return primeCoordinates;
                //returns the coordinates of a zero that fits the prime criteria.
            }
        }
        
        //If there is no zero that can be primed, then make take away the smallest uncovered getValue from all uncovered values and add it to doubly covered values.
        int smallestValue = bigNumber; 
        // this gets the smallest uncovered getValue in the matrix
        for (int i = 0; i < matrix.length; i++) 
        {
            if(rowsCrossed[i] == false)
            {
                int[] row = matrix[i];
                for (int j = 0; j < row.length; j++) 
                {
                    if(columnsCrossed[j] == false && row[j] < smallestValue)
                    {
                        smallestValue = row[j];
                    }
                }
            }
        }
        //this removes that getValue from all uncovered values and adds it to all doubly covered values.
        for (int i = 0; i < matrix.length; i++) 
        {
            int[] row = matrix[i];
            for (int j = 0; j < row.length; j++) 
            {
                //subtracts the smallest getValue from all uncovered values
                if(rowsCrossed[i] == false && columnsCrossed[j] == false)
                {
                    row[j] -= smallestValue;
                    if(row[j] == 0)
                    {
                        KVPair<Integer,Integer> coordinate = new KVPair(i,j);
                        zeroCoordinates.add(coordinate);
                        primeCoordinates = new int[]{i,j};
                    }
                }
                //adds the smallest getValue to all doubly covered values
                else if(rowsCrossed[i] ==true && columnsCrossed[j] == true )
                {
                    if(row[j] == 0)
                    {
                        KVPair<Integer,Integer> coordindate = new KVPair(i,j);
                        zeroCoordinates.remove(coordindate);
                    }
                    row[j] += smallestValue;
                }
            }
        }
        return primeCoordinates;
        //this will return the the last zero made when changing the matrix.
    }
    
   /**
    * This function follows some steps.
    * It starts by adding the coordinates of the primed zero to the list of coordinates.
    * It will then see if there is a starred zero in that column.
    * If there is, then it will add that starred zero's coordinates to the list.
    * then it will call itself, with the coordinates of the primed zero in the row of that starred zero, because there will always be one.
    * If there isn't a starred zero in the column of a primed zero, then the function is finished, and will return the list of all of the coordinates
    * @param row This is the row that the primed zero is stored in.
    * @param column This is the column that the primed zero is stored in.
    * @param coordinates this is a coordinates containing the coordinates of all zeros that are along the path made by the above steps.
    * @return 
    */
    private LinkedList<KVPair<Integer,Integer>> goThroughPath(int row, int column, LinkedList<KVPair<Integer,Integer>> coordinates)
    {
        coordinates.add(new KVPair(row, column));
        //adds the primed zero's coordinates to the coordinates.
        if(starInColumn[column] != -1) // this checks if there is a star in that column
        {
            row = starInColumn[column]; // changes the row to be the row that the star is in
            coordinates.add(new KVPair(row, column)); //adds the starred zero to the coordinates of zeros
            column = primeInRow[row];// changes the column to be the column that the primed zero is in.
            return goThroughPath(row, column, coordinates); // repeat the steps
        }
        return coordinates; // return the coordinates once there isn't a starred zero in the same column as the the primed zero.
    }
    /**
     * fills an int array with the number passed in
     * @param nums array to be filled
     * @param num number that the array is to be filled with
     * @return the array, after being filled.
     */
    private int[] fillArray(int[] nums, int num)
    {
        for (int i = 0; i < nums.length; i++) 
        {
            nums[i] = num;    
        }
        return nums;
    }
    /**
     * fills a boolean array with falses.
     * @param array 
     * @return 
     */
    public boolean[] fillFalse(boolean[] array)
    {
        for (int i = 0; i < array.length; i++) {
            array[i] = false;
            
        }
        return array;
    }
    public static void main(String[] args) 
    {
       /* 
        int[][] matrix = {
      //col0  col1  col2  col3
        {76,  78,   70,   62 },  //row0
        {74,  80,   66,   62 },  //row1
        {72,  76,   68,   58 },  //row2
        {78,  80,   66,   60 }, //row3

      };
       */
        int[][] matrix = 
        {
            {3,0,93,19,74,0},
            {0,9,42,0,50,39},
            {3,17,0,0,65,30},
            {38,44,52,33,0,0},
            {16,70,64,15,0,16},
            {11,0,46,27,41,40}
        };
        Hungarian hungry = new Hungarian(matrix);
        LinkedList<KVPair<Integer,Integer>> coordinates = hungry.assignments();
        int totalCost =0;
        while(!coordinates.isEmpty())
        {
            KVPair<Integer,Integer> coordinate = coordinates.pop();
            int row = coordinate.getKey();
            int column = coordinate.getValue();
            System.out.println("row: " + row + " column: " + column + " = "+ matrix[row][column]);
            totalCost += matrix[row][column];
        }
        System.out.println("Total Cost: " +totalCost);
        
    }
}

















































