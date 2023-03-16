/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DatabaseConnect;
import KVPair.KVPair;
import LinkedStructures.*;
import hungarian.Hungarian;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Antony Admin
 */
/**
 * This class takes the information in the database, and processes it to make assignments for the helpers to certain lessons, and then it will write this into the assignments table in the database.
 * @author Antony Admin
 */
public class Assignments {
    
   
    private Connection conn = null;
    
    
    private final int maxAssignments = 5;
    private final int maxNumSubjects = 5;
    private final int costToDuplicate = 100;
    private final int bigNumber = 99999;
    
    /**
     * This is used when making assignments on its own, so it should be for testing purposes.
     */
    public Assignments()
    {
        try
        {
            Class.forName("org.sqlite.JDBC");//Specify the SQLite Java driver
            conn = DriverManager.getConnection("jdbc:sqlite:HelperSchedule.db");//Specify the database, since relative in the main project folder
            conn.setAutoCommit(false);// Important as you want control of when data is written
            System.out.println("Opened database successfully");
        } catch (Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }   
    }
    /**
     * This is how assignments will normally be constructed, since it will be made in the userInterface class.
     * @param conn a pre-existing connection passed in.
     */
    public Assignments(Connection conn)
    {
        this.conn = conn;
    }
    /**
     * closes the database connection.
     */
    public void close() 
    {
        try
        {
            conn.close();
        } 
        catch (SQLException ex)
        {
            Logger.getLogger(Assignments.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * This is the main function. It will make the assignments and then write them to the Assignments table.
     * It also updates the TempUnavailable table, so that temporary unavailabilities for tasks that they aren't assigned to anymore are deleted.
     */
    public void writeAssignments() throws IOException 
    {
        //assignments contains all of the information needed to insert into the assignments table.
        LinkedList<AssignmentInfo> assignments = makeAssignments();
        Statement stmt = null;
        ResultSet rs = null;
        String sql = "delete from Assignments"; // this will clear the table, so that I can write in the new assignments
        try
        {
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
            conn.commit();

        } catch (SQLException e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        
        //after clearing the table, enter the new assignments.
        sql = "INSERT INTO Assignments (HelperID, PeriodID, ClassID, Cost)\n"
        + "Values ";
        //this adds in all of the assignments to be written.
        for (AssignmentInfo assignment : assignments) 
        {
            int helperID = assignment.getHelperID();
            int periodID = assignment.getPeriodID();
            int classID = assignment.getClassID();
            int cost = assignment.getCost();
            sql += "(" + helperID + "," + periodID + "," + classID + "," + cost + "),";
        }
        sql = sql.substring(0, sql.length() -1);
        try
        {
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
            FileWriter writeOut = new FileWriter("AssignmentStatus.txt");
            writeOut.write("false ");
            writeOut.close();
            conn.commit();

        } catch (SQLException e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        //after redoing the assignments, update the temporary unavailable table
    }
    /**
     * This gets all of the information needed to assign helpers to lessons by querying the database.
     * @return the list of information needed to write to assignments.
     */
    private LinkedList<AssignmentInfo> makeAssignments()
    {
        
        int numHelpers = numHelpers();
         //rows is needed to make the cost matrix later.
        LinkedList<RowInfo> rows = getRowInfo();
        int numPeriods = rows.length();
        //numHelpers and numPeriods are needed for making hash maps and hash sets with the correct size later.
        LinkedHashMap<Integer,Integer> numHelpersForPeriods = numHelpersForPeriods(numPeriods);
        
        //these two functions get costs associated with helpers, their willingness and rating at subjects
        LinkedHashMap<Integer, LinkedHashMap<Integer, Integer>> helperPeriods = helperAvailablePeriods(numHelpers, numPeriods);
        LinkedHashMap<Integer, LinkedHashMap<String, Integer>> helperSubjects = helperSubjects(numHelpers);
        
        LinkedHashSet<Integer> helpers = getHelpers(numHelpers);
        LinkedHashMap<Integer, Integer> helperDuties = makeHelperDuties(helpers);
        int[][] helperColumn = makeHelperColumns(helpers, helperDuties);
        //helper column is a 2d array storing the helperID of that column, and the cost that comes with how many times it has been duplicated

        
        LinkedList<AssignmentInfo> assignments = new LinkedList();
        LinkedList<Integer> maxedHelpers; // this stores the HelperIDs of all maxed helpers, it will be iterated over later.
        //inside getAssignments, rows is deleted from if there aren't any lessons or helpers left in that period anymore, so it will be empty when all assignments are done
        while(!rows.isEmpty())
        {
            //this duplicates the helper columns as much as they can, given helper's number of assignments left.
            helperColumn = duplicateColumns(rows.length(), helperColumn, helperDuties);
            //this then makes the matrix for the hungarian to use.
            int[][] matrix = generateMatrix(helperColumn, rows, helperPeriods, helperSubjects);
            //this function runs the Hungarian with the matrix and updates what is stored accordingly.
            LinkedList[] assignmentResults = getAssignments(matrix, helperColumn, rows, helperDuties, helperSubjects, helperPeriods, numHelpersForPeriods);
            //adds in records that will be written to the database.
            assignments.add(assignmentResults[0]);
            //maxed helpers are helpers that can't help in any more lessons now, so the records of those helpers is deleted from the hash maps.
            maxedHelpers = assignmentResults[1];
            numHelpers -= maxedHelpers.length();
            //this clears the records of all helpers that can't help in any other lessons anymore.
            for (Integer helperID : maxedHelpers) 
            {
                helpers.delete(helperID);
                helperSubjects.delete(helperID);
                helperPeriods.delete(helperID);
                helperDuties.delete(helperID);
            }
            //remakes helper column after getting rid of helpers that ccan't help anymore.
            helperColumn = makeHelperColumns(helpers, helperDuties);
        }
        return assignments;
    }
    /**
     * Gets the assignments generated from the hungarian algorithm and returns the information to do with that, as well as updating the variables.
     * @return 
     */
    private LinkedList[] getAssignments(int[][] matrix, int[][] helperColumn, LinkedList<RowInfo> rows, LinkedHashMap<Integer, Integer> helperDuties, LinkedHashMap<Integer, LinkedHashMap<String, Integer>> helperSubjects, LinkedHashMap<Integer, LinkedHashMap<Integer, Integer>> helperPeriods, LinkedHashMap<Integer, Integer> numHelpersForPeriods)
    {
        LinkedList[] results = new LinkedList[2];
        //runs the hungarian with the matrix given.
        Hungarian hungarian = new Hungarian(matrix, bigNumber);
        LinkedList<KVPair<Integer, Integer>> coordinates = hungarian.assignments();
        LinkedList<Integer> maxedHelpers = new LinkedList();
        LinkedList<AssignmentInfo> assignments = new LinkedList();
        for (KVPair<Integer, Integer> coordinate : coordinates) 
        {
            int row = coordinate.getKey();
            int column = coordinate.getValue();
            //gets the information that goes with the coordinates of the matrix, so the helper and the class.
            RowInfo rowInfo = rows.dataAtIndex(row);
            LessonInfo lessonInfo = rowInfo.getPq().peek();
            lessonInfo.setHelpersNeeded(lessonInfo.getHelpersNeeded() -1); // reduces the number of helpers needed for that class.
            int helperID = helperColumn[column][0];
            int classID = lessonInfo.getClassID();
            int periodID = rowInfo.getPeriodID();
            int cost = matrix[row][column];
            //add the information needed to be written into the assignments table.
            AssignmentInfo assignmentInfo = new AssignmentInfo(helperID, classID, periodID, cost);
            assignments.add(assignmentInfo);
            //reduces the amount of helpers available to help in that period.
            numHelpersForPeriods.replaceValue(periodID, numHelpersForPeriods.item(periodID) -1);
            
            // say that the helper has been assigned to another task.
            helperDuties.replaceValue(helperID, helperDuties.item(helperID) + 1);
            if(helperDuties.item(helperID) == maxAssignments)
            {
                maxedHelpers.add(helperID);
            }
            else
            {
                LinkedHashMap<Integer,Integer> periods = helperPeriods.item(helperID);
                periods.delete(periodID);
                //if the helper isn't available for anything else, then clear their records.
                if(periods.isEmpty())
                {
                    maxedHelpers.add(helperID);
                }
            }
            if(lessonInfo.getHelpersNeeded() == 0) // if the lesson has gotten as many helpers as it needs, pop it from that row.
            {
                rowInfo.getPq().pop();
            }
            if(numHelpersForPeriods.item(periodID) == 0) // if there aren't any helpers available in that period anymore
                rowInfo.getPq().empty();
            
        }
        //this loop is to break any dead locks, where a helper might have had their lesson "stolen", because they weren't as suited, and then their subjects don't match.
        //It also stops an infinite loop if there's a scenario where no helpers can be assigned to any of the classes.
        if(coordinates.isEmpty())
        {
            for (RowInfo rowInfo : rows) 
            {
                rowInfo.getPq().pop();
            }
        }
        // this has to be done outside the loop to prevent the indexing from being messed up
        for (RowInfo rowInfo : rows) 
        {
            if(rowInfo.getPq().isEmpty())
            {
                //this gets rid of that period from all  helpers.
                int periodID = rowInfo.getPeriodID();
                LinkedList<LinkedHashMap<Integer,Integer>> periodsList = helperPeriods.getValues();
                LinkedList<Integer> helperIDList = helperPeriods.getKeys();
                Iterator<Integer> helperIDs = helperIDList.iterator();
                for (LinkedHashMap<Integer, Integer> periods : periodsList) 
                {
                    int helperID = helperIDs.next();
                    if(periods.contains(periodID))
                    {
                        periods.delete(periodID);
                        if(periods.isEmpty())
                            maxedHelpers.add(helperID);
                    }
                    
                }
                
                rows.remove(rowInfo);
            }
        }
        results[0] = assignments;
        results[1] = maxedHelpers;
        return results;
    }
    /**
     * Row info creates a linked list of items. These items are collections of lessons running in the same period.
     * Each item in the list stores the periodID that those lessons run in, and a priority queue of information on those lessons.
     * The lesson's priority is determined by their lesson priority, which is an indicator of how important it is for that lesson to get helpers.
     * @return 
     */
    private LinkedList<RowInfo> getRowInfo()
    {
        Statement stmt = null;
        ResultSet rs = null;
        //this query gets all relevant information needed.
        String sql = 
        "select ClassID, PeriodID, LessonPriority, HelpersNeeded, SubjectName\n" +
        "from ViewAssigning\n" +
        "Group by ClassID, PeriodID\n" +
        "Order By PeriodID, ClassID";
        LinkedList<RowInfo> rows = new LinkedList(); 
        try
        {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            int classID;
            String subject = null;
            int lessonPriority, helpersNeeded;
            int periodID =0;
            int tempPeriodID = 0;
            PriorityQueue<LessonInfo> pq = new PriorityQueue();
            while(rs.next())
            {
                helpersNeeded = rs.getInt("HelpersNeeded");
                lessonPriority = rs.getInt("LessonPriority");
                subject = rs.getString("SubjectName");
                classID = rs.getInt("ClassID");
                if(periodID == 0)
                {
                    periodID = rs.getInt("PeriodID");
                } // this is here to make sure there isn't an empty entry rowInfo made
                tempPeriodID = rs.getInt("PeriodID");
                
                if(tempPeriodID != periodID) //when there's a new period, add all the information collected into the other one.
                {
                    RowInfo rowInfo = new RowInfo(pq, periodID);
                    rows.add(rowInfo);
                    periodID = tempPeriodID;
                    pq = new PriorityQueue<LessonInfo>();
                }
                // a new lesson is added to the priority queue every loop.
                LessonInfo lessonInfo = new LessonInfo(helpersNeeded,lessonPriority, classID, subject);
                pq.add(lessonInfo, lessonPriority);
            }
            //this adds the last record to the list.           
            RowInfo rowInfo = new RowInfo(pq,periodID);
            rows.add(rowInfo);
            
            stmt.close();
            conn.commit();

        } catch (SQLException e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return rows;
    }
    
    /**
     * This returns a hash map of hash maps. For the larger hash map, it stores the helper ID, and then the periods hash map as a value.
     * The periods hash map stores period IDs as keys, and the helper's willingness to help in that period as a value.
     * @param numHelpers
     * @param numPeriods
     * @return 
     */
    private LinkedHashMap<Integer, LinkedHashMap<Integer, Integer>> helperAvailablePeriods(int numHelpers, int numPeriods)
    {
        Statement stmt = null;
        ResultSet rs = null;
        
        // this constructs the query, getting all relevant helper availabilities.
        String sql = 
        "select HelperID, PeriodID, Willingness\n" +
        "from ViewAssigning\n" +
        "group by HelperID, PeriodID\n" +
        "Order by HelperID, PeriodID";
        LinkedHashMap<Integer, LinkedHashMap<Integer, Integer>> helperPeriods = new LinkedHashMap(numHelpers);
        try
        {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            LinkedHashMap<Integer, Integer> periods = new LinkedHashMap(numPeriods);
            int helperID =0;
            int tempHelperID = 0;
            while(rs.next())
            {
                //this is just a condition for the first loop.
                if(helperID ==0)
                    helperID = rs.getInt("HelperID");
                tempHelperID = rs.getInt("HelperID");
                if(tempHelperID != helperID)
                {
                    //once a new Helper's information is being looked at, add all information on the previous helper's in, because there's nothing more there.
                    helperPeriods.add(helperID, periods);
                    periods = new LinkedHashMap(numPeriods); // this resets the hashmap
                    helperID = tempHelperID;
                }
                //this adds the period information every loop.
                periods.add(rs.getInt("PeriodID"), rs.getInt("Willingness"));
            }
            //the last helper's information has to be added after the loop, because of how it's structured.
            helperPeriods.add(helperID, periods);
            
            stmt.close();
            conn.commit();
        } catch (SQLException e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return helperPeriods;
    }
    
    /**
     * This gets the information for each helper's subjects and ratings.
     * @param numHelpers
     * @return 
     */
    private LinkedHashMap<Integer, LinkedHashMap<String, Integer>> helperSubjects(int numHelpers)
    {
        Statement stmt = null;
        ResultSet rs = null;
        //this 
        String sql = 
        "select HelperID, HelperRating, SubjectName\n" +
        "from ViewAssigning\n" +
        "group by HelperID, SubjectName";
        
        try
        {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            LinkedHashMap<Integer, LinkedHashMap<String, Integer>> helperSubjects = new LinkedHashMap(numHelpers);
            LinkedHashMap<String, Integer> subjects = new LinkedHashMap(maxNumSubjects);
            int helperID = 0;
            while(rs.next())
            {
                if(helperID == 0)
                    helperID = rs.getInt("HelperID");
                int tempHelperID = rs.getInt("HelperID");
                if(tempHelperID != helperID)
                {
                    helperSubjects.add(helperID, subjects);
                    subjects = new LinkedHashMap(maxNumSubjects);
                    helperID = tempHelperID;
                }
                subjects.add(rs.getString("SubjectName"), rs.getInt("HelperRating"));
            }            
            helperSubjects.add(helperID, subjects);
            stmt.close();
            conn.commit();
            return helperSubjects;
        } catch (SQLException e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        
        return new LinkedHashMap<Integer, LinkedHashMap<String, Integer>>();
    }
    /**
     * This makes helper columns, which is a 2d array that stores every helper ID, and their duplicate cost. This makes normal ones, so there is no duplicate cost.
     * @param helpers all of the HelperIDs
     * @return 
     */
    private int[][] makeHelperColumns(LinkedHashSet<Integer> helpers, LinkedHashMap<Integer,Integer> helperDuties)
    {
        int[][] helperColumn = new int[helpers.length()][2];
        int i =0;
        LinkedList<Integer> helperIDs = helpers.getKeys();
        for (Integer helperID : helperIDs) 
        {
            helperColumn[i] = new int[]{helperID, costToDuplicate * helperDuties.item(helperID)};
            i++;
        }
        return helperColumn;
    }
    private LinkedHashSet<Integer> getHelpers(int numHelpers)
    {
        Statement stmt = null;
        ResultSet rs = null;
        String sql = "select DISTINCT HelperID\n" +
        "from ViewAssigning";
        try
        {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            LinkedHashSet<Integer> helpers = new LinkedHashSet(numHelpers); 
            while(rs.next())
            {
                helpers.add(rs.getInt("HelperID"));
            }
            stmt.close();
            conn.commit();
            return helpers;
        } catch (SQLException e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return new LinkedHashSet<Integer>(1);
    }
    
    /**
     * 
     * @param helperColumn
     * @return A hash map storing the HelperID, and how many lessons they have been assigned to. This is used when making sure that they aren't assigned to more than 5 lessons.
     */
    private LinkedHashMap<Integer, Integer> makeHelperDuties(LinkedHashSet<Integer> helpers)
    {
        LinkedHashMap<Integer,Integer> helperDuties = new LinkedHashMap(helpers.length());
        //when making, it just sets every helper to 0
        for (Integer helperID : helpers.getKeys()) 
        {
            helperDuties.add(helperID, 0);
        }
        return helperDuties;
    }
    /**
     * This makes copies of the helper columns, duplicating helpers and adding an extra cost to it. The duplicate cost is used to make it so that people are assigned an equal number of tasks.
     * @param numRows
     * @param helperColumn
     * @param helperDuties
     * @return 
     */
    private int[][] duplicateColumns(int numRows, int[][] helperColumn, LinkedHashMap<Integer,Integer> helperDuties)
    {
        int numHelpers = helperColumn.length;
        //if there are lesons running in more periods than there are helpers, the costs for helpers have to be duplicated, so that the hungarian algorithm can work.
        if(numHelpers < numRows)
        {
            // this finds writeOut what the maximum number of assignments that can be made is
            int assignmentsLeft =0;
            for (Integer lessonsAssigned : helperDuties.getValues()) 
            {
                assignmentsLeft += (maxAssignments - lessonsAssigned);
            }
            int numColumnBlocks;
            //numColumnBlocks is the amount of times all helper columns would need to be duplicated for the number of columns to be greater than or equal to the number of rows
            if(numRows % numHelpers == 0)
                numColumnBlocks = numRows/numHelpers;
            else
                numColumnBlocks = (numRows/numHelpers) + 1;
            
            int numColumns;
            if( assignmentsLeft < numColumnBlocks * numHelpers )
            {
                numColumns = assignmentsLeft;
            }
            else
                numColumns = numColumnBlocks * numHelpers;
            
            helperColumn = Arrays.copyOf(helperColumn, numColumns);
            //these two loops will go through simply, just copying the value from the original
            for (int i = 1; i < numColumns/numHelpers; i++) 
            {
                for (int j = 0; j < numHelpers; j++) 
                {
                    helperColumn[( i*numHelpers) + j] = new int[] {helperColumn[j][0], i*costToDuplicate};
                    //this sets the helperID for the next columns to be equal to previous columns
                    
                }
            }
            //this loop goes through the remaining helperIDs, where some might not be assignable.
            if(assignmentsLeft < numColumnBlocks * numHelpers)
            {
                int numSkipped =0;
                for (int i = 0; i < numHelpers; i++) 
                {
                    int helperID = helperColumn[i][0];
                    if(helperDuties.item(helperID) + (numColumns/numHelpers) >= maxAssignments)
                    {
                        numSkipped++;
                        //numSkipped ensures that the indices get filled in with the helpers that should be there
                        continue;
                    }
                    helperColumn[i + (numHelpers * (numColumns/numHelpers)) - numSkipped] = new int[] {helperID, (numColumns/numHelpers) * costToDuplicate};
                }
            }
            
        }
        return helperColumn;
    }
    /**
     * Creates a cost matrix from the given data that can be used in the Hungarian algorithm.
     * @return 
     */
    private int[][] generateMatrix(int[][] helperColumn, LinkedList<RowInfo> rows, LinkedHashMap<Integer, LinkedHashMap<Integer, Integer>> helperPeriods, LinkedHashMap<Integer, LinkedHashMap<String, Integer>> helperSubjects)
    {
        //this all block determines the dimensions of the matrix.
        int numColumns = helperColumn.length;
        int numRows = rows.length();
        int highestLength;
        if(numRows > numColumns)
            highestLength = numRows;
        else
            highestLength = numColumns;
        int[][] matrix = new int[highestLength][highestLength];
        
        
        int rowIndex =0;
        int periodID, helperID, duplicateCost, subjectCost, willingness,lessonPriority, cost;
        String subject = null;
        LinkedHashMap<String, Integer> subjects;
        LinkedHashMap<Integer, Integer> periods;
        //Iterate through the rows to get the necessary information.
        for (RowInfo rowInfo : rows) 
        {
            //this gets the values specific to the rows, so lesson priority, subject and periodID
            LessonInfo lessonInfo = rowInfo.getPq().peek();
            lessonPriority = lessonInfo.getLessonPriority();
            subject = lessonInfo.getSubject();
            periodID = rowInfo.getPeriodID();
            
            for (int i = 0; i < numColumns; i++) 
            {
                //this gets the information to do with the helpers and adds the cost on.
                //If the helper doesn't do the subject of that class or isn't available for that period, then the cost is set to 'infinite'; 
                helperID = helperColumn[i][0];
                duplicateCost = helperColumn[i][1];
                
                subjects = helperSubjects.item(helperID);
                if(subjects.contains(subject))
                    subjectCost = subjects.item(subject);
                else
                    subjectCost = bigNumber;
                
                periods = helperPeriods.item(helperID);
                if(periods.contains(periodID))
                    willingness = periods.item(periodID);
                else
                    willingness = bigNumber;
                
                //this then sets the cost for the matrix
                cost = lessonPriority + willingness + subjectCost + duplicateCost;
                matrix[rowIndex][i] = cost;
            }
            rowIndex++;
        }
        //these loops fill in extra rows or columns with dummy values
        for (int i = numRows; i < numColumns; i++) 
        {
            for (int j = 0; j < matrix.length; j++) 
            {
                matrix[i][j] = bigNumber;
            }
        }
        for (int i = numColumns; i < numRows; i++) 
        {
            for (int j = 0; j < matrix.length; j++) 
            {
                matrix[j][i] = bigNumber;
            }
        }
        return matrix;
    }
    /**
     * This gets the total number of different helpers that can help in a lesson.
     * @return 
     */
    private int numHelpers()
    {
        Statement stmt = null;
        ResultSet rs = null;
        //query to get the number of helpers.
        String sql = "select Count(DISTINCT HelperID) \n" +
        "From ViewAssigning \n";
        int numHelpers = 0;
        try
        {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            rs.next();
            //this gets the result of the query
            numHelpers = rs.getInt(1);
            
            stmt.close();
            conn.commit();
        } catch (SQLException e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return numHelpers;
    }
    
    /**
     * This performs a query to find writeOut how many helpers are available in each period that a class is running.
     * @param numPeriods
     * @return the number of helpers available for each period with the periodID as a key, and number of helpers as the value.
     */
    private LinkedHashMap<Integer,Integer> numHelpersForPeriods(int numPeriods)
    {
        Statement stmt = null;
        ResultSet rs = null;
        //this query gets the required data.
        String sql = "select PeriodID, Count(DISTINCT HelperID) as NumHelpers\n" +
        "from ViewAssigning\n" +
        "group by PeriodID";
        LinkedHashMap<Integer,Integer> numPeriodHelpers = new LinkedHashMap(numPeriods);
        try
        {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            while(rs.next()) // this goes through every result set, so every period and the number of helpers
            {
                numPeriodHelpers.add(rs.getInt("PeriodID"), rs.getInt("numHelpers"));
            }
            
            stmt.close();
            conn.commit();
        } catch (SQLException e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        //this gives an empty hash map if there is nothing in the query.
        return numPeriodHelpers;
    }
    
}