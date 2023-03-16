/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package DatabaseConnect;

import java.util.Scanner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import LinkedStructures.*;
import KVPair.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSetMetaData;
import java.util.regex.Pattern;

/**
 * This class provides a menu to the user, with appropriate prompts and data returned for what type of user they are.
 * They can be an administrator, a teacher, or a helper.
 */
public class UserInterface {
    //userAccess determines what tables the user will be able to interact with on whatever way. 
    private String[] userAccess;
    //numTables is just to signify how big the tableFields hash map should be
    int numTables = 10;
    //table fields stores the name of each table, along with the fields in that table, and is used when asking the user to enter data.
    private LinkedHashMap<String, KVPair<String[], int[]>> tableFields = new LinkedHashMap(numTables);
    
    //these two variables store data on the user that is needed for queries and access permission.
    private int userID;
    private String userType;
    private Scanner keyboard = new Scanner(System.in);
    private Connection conn = null;
    
    //isAssignmentsAffected tracks if changes have been made to the table. If so, then the tables need to be updated.
    private Assignments assignments= null;
    //these patterns are compiled here so that they don't have to made evry time String.matches() is called.
    private Pattern numericFields = Pattern.compile("HelperID|TeacherID|ClassID|PeriodID|YearGroup|ClassYear|HelpersNeeded|RoomNumber|PeriodNumber");
    private Pattern costFields = Pattern.compile("Willingness|HelperRating|LessonPriority");
    private Pattern userTables = Pattern.compile("Helper|Teacher|Administrator");
    private Pattern sqlStatements = Pattern.compile("INSERT\\s|SELECT\\s|UPDATE\\s|\\sTABLE\\s|\\sOR\\s|\\sAND\\s|=|;");
    private Pattern assigningTables = Pattern.compile("HelperAvailability|HelperSubject|ClassTimes");
    private Pattern SelfIDTables = Pattern.compile("Helper|Teacher|Period|Administrator|Class");
    
    private File assignmentStatus = new File("AssignmentStatus.txt");
    private Scanner reader;
    FileWriter writeOut;
    /**
     * The constructor sets default values needed, so sets up the database connection and adds all of the table fields
     */
    public UserInterface() throws FileNotFoundException, IOException
    {
        reader = new Scanner(assignmentStatus);
                
        String contents = readAssignmentStatus();
        writeOut = new FileWriter(assignmentStatus);
        //this has to be done, because I want fileWriter to overwrote generally, but it default empties the file then
        writeOut.write(contents);
        writeOut.flush();
        try
        {
            //maybe have encryption on the databse and decrypt it here, or decrypt every time a query is made.
            Class.forName("org.sqlite.JDBC");//Specify the SQLite Java driver
            conn = DriverManager.getConnection("jdbc:sqlite:HelperSchedule.db");//Specify the database, since relative in the main project folder
            conn.setAutoCommit(false);// Important as you want control of when data is written
            System.out.println("Opened database successfully");
        } catch (Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        
       
        //these are organised as the name of the table, it's fields, and then the int array gives the number of fields needed to identify a record, and then the number of required fields in a record.
        //there's also an unavailable table that marks helpers that are temporarily unavailable, but the user doesn't really interact directly with it.
        tableFields.add("Helper", new KVPair(new String[]{"HelperID", "HelperName", "EmailAddress", "YearGroup", "Password"}, new int[] {1, 2}) );
        tableFields.add("Teacher", new KVPair(new String[]{"TeacherID", "TeacherName", "EmailAddress", "Password"}, new int[] {1, 2}) );
        tableFields.add("Class", new KVPair(new String[]{"ClassID", "SubjectName", "TeacherID", "ClassYear", "Form"}, new int[] {1, 3}) );
        tableFields.add("HelperAvailability", new KVPair(new String[]{"HelperID", "PeriodID", "Willingness"}, new int[] {2, 3}) );
        tableFields.add("HelperSubject", new KVPair(new String[]{"HelperID", "SubjectName", "HelperRating"}, new int[] {2, 3}) );
        tableFields.add("ClassTimes", new KVPair(new String[]{"ClassID", "PeriodID", "LessonPriority", "HelpersNeeded", "RoomNumber"}, new int[] {2, 4}) );
        tableFields.add("Assignments", new KVPair(new String[]{"HelperID", "PeriodID", "ClassID"}, new int[] {2, 4}) ); // assignments also has cost, but that only needs to be accessed in the back end. These fields give values that users will be dealing with, so it isn't necessary.
        tableFields.add("Subject", new KVPair(new String[]{"SubjectName"}, new int[] {1, 1}) );
        tableFields.add("Period", new KVPair(new String[]{"PeriodID","Week","Day","PeriodNumber"}, new int[] {1, 4}) );
        tableFields.add("Administrator", new KVPair(new String[]{"AdministratorID", "AdministratorName", "EmailAddress", "Password"}, new int[]{1,2}));   
    }
   /**
    * this closes the database connection.
    */
    public void close() 
    {
        try
        {
            conn.close();
        } 
        catch (SQLException ex)
        {
            Logger.getLogger(UserInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * This provides the initial menu to the user, getting them to identify themselves and sign in.
     */
    public void userMenu() throws Exception
    {
        System.out.println("Please enter the number for which type of user you are");
        System.out.println("1: Helper");
        System.out.println("2: Teacher");
        System.out.println("3: Administrator");
        
        int choice = validateInt(1,3);
        switch(choice)
        {
            case 1: 
                userType = "Helper";
                userAccess = new String[]{"Helper", "HelperAvailability", "HelperSubject", "Assignments"};
                break;
            case 2: 
                userType = "Teacher";
                userAccess = new String[]{"Teacher", "Class", "ClassTimes", "HelperSubject", "Assignments"};
                break;
            case 3: 
                userType = "Administrator";
                userAccess = new String[]{"Helper", "HelperAvailability", "HelperSubject","Teacher", "Class", "ClassTimes", "Subject", "Period", "Assignments", "Administrator"};
                break;
                }
     
        if(signUserIn()) // this means that they can only access things once they're signed in, and the program's finished if not.
        {
            while(tableMenu()); 
            //going as long as the user wants.  table selection allows them to choose tables to access.
            if(isAssignmentsAffected())
            {
                //this will update the assignments table if anything has been changed.
                if(assignments == null)
                    assignments = new Assignments(conn);
                assignments.writeAssignments();
            }
        }
        
        
        
    }
    /**
     * This function gets the user to sign in by entering their ID and the corresponding password.
     * @return whether or not they have managed to sign in
     */
    private boolean signUserIn() throws SQLException
    {
        System.out.println("Please enter your ID number. If you cannot remember your ID number, then enter 0");
        int in = validateInt(0, Integer.MAX_VALUE);
        if(in == 0)
        {
            if(!nameIDMatch())
            {
                return false;
            }
        } 
        else
            userID = checkTable(in);
        return checkPassword();
    }
    /**
     * this takes their password and checks it against the password stored with that ID in the database.
     * The user gets three attempts before they can't try anymore, and are locked out.
     * @return whether or not they successfully entered their password.
     * @throws SQLException 
     */
    private boolean checkPassword() throws SQLException
    {
        
        int attempts = 3;
        boolean isCorrect = false;
        while(attempts > 0 && !isCorrect)
        {
            Statement stmt = null;
            ResultSet rs = null;
            System.out.println("Please enter your password");
            String password = validateString();
            String sql = "select " + userType + "ID" + " from " + userType + " where " + userType + "ID = " + userID + " and Password = '" + password +"';";
            try
            {
                stmt = conn.createStatement();
                rs = stmt.executeQuery(sql);

                if(!rs.isBeforeFirst())
                {
                    attempts--;
                    System.out.println("Your password is incorrect. You have " + attempts + " left;");
                    stmt.close();
                    conn.commit();
                    continue;
                }
                else
                {
                    stmt.close();
                    conn.commit();
                    return true;
                }
            } catch (SQLException e)
            {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
        }
        System.out.println("You have run out of attempts to sign in.");
        return false;
    }
    /**
     * if the user has forgotten their ID number, they can enter their name to see records with the same name.
     * There could be people with the same name, so it also gives their email address to check against.
     * It then gets them to enter their ID, and validates it.
     */
    private boolean nameIDMatch()
    {
        System.out.println("Please enter your name");
        String name = "'" + validateString() + "'";
        Statement stmt = null;
        ResultSet rs = null;
        String sql = "select " + userType + "ID, EmailAddress  from " + userType + " where " + userType + "Name = " + name;
        try
        {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            //this checks if there are any recirds with a matching name.
            if(!rs.isBeforeFirst())
            {
                System.out.println("There is no ID that matches this name. Maybe you mistyped it?\nPlease enter 0 if you would like to exit the program.\nPlease enter 1 if you would like to try and reenter your name.\n");
                int choice = validateInt(0,1);
                if(choice ==1)
                    return nameIDMatch();
                else
                    return false;
            }
            //this gives the rocrs with matching names.
            else
            {
                System.out.println("These are the accounts that match that name: \n");
                while(rs.next())
                {
                    int ID = rs.getInt(1);
                    String emailAddress = rs.getString(2);
                    System.out.println("ID number: " + ID + "\tEmailAddress: " + emailAddress);
                }
                System.out.println("Please enter your ID");
                userID = validateID();
            }
            
            stmt.close();
            conn.commit();
        } catch (SQLException e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }
    /**
     * Table selection is where the user chooses what they would like to do.
     * They can access the tables they have permission to, and then can interact with each table according to their permissions.
     * They an choose between the different tables, updating their password and exiting their program.
     * They can also change their password by modifying their own record
     * @return whether or not the user would like to access another table.
     * @throws Exception 
     */
    public boolean tableMenu() throws Exception
    {
        System.out.println("Please enter the number given before the option you would like to choose");
        int i =0;
        int maxOption, exitOption, tempUnavailableOption = 0;
        while(i < userAccess.length) 
        {
            //options start at 1 rather than 0.
            System.out.println((i+1) + ": Access " + userAccess[i]);
            i++;
        }
        System.out.println((i+1) + ": Update password");
        if(userType.equals("Teacher"))
        {
            exitOption = maxOption =i + 2;
            System.out.println((i+2) +": Exit Program");
        }
        else
        {
            tempUnavailableOption = i + 2;
            exitOption = maxOption = i + 3;
            System.out.println((i +2) + ": Temporarily update availability");
            System.out.println((i+3) +": Exit Program");
        }
        int choice = validateInt(1,maxOption);
        if(choice == exitOption)
            return false;
        else if(choice == i+1)
            updatePassword();
        else if(choice == tempUnavailableOption)
        {
            tempUpdateAssignments();
        }
        else
        {
            String table = userAccess[choice -1];
            tableMaintenance(table);
        }
        
        return true;
    }
    /**
     * This allows the user to update their password. They have to enter their password twice to make sure they don't mistype it.
     */
    public void updatePassword()
    {
        boolean matching = false;
        System.out.println("Please enter your new password.");
        String password = keyboard.nextLine();
        while(!validatePassword(password))
        {
            System.out.println("Your passwords did not match. Please try again");
            System.out.println("Please enter your new password.");
            password = validateString();
        }
        // this then performs the sql needed to update the password in the database.
        Statement stmt = null;
        String sql = "update " + userType + " set Password = '" + password + "' where " + userType + "ID = " + userID;
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
    }
    /**
     * This checks if the password entered matches the one given
     * @param password
     * @return 
     */
    private boolean validatePassword(String password)
    {
        System.out.println("Please enter your new password again.");
        String password2 = validateString();
        if(password.equals(password2))
            return true;
        else
        {
            return false;
        }
    }
    /**
     * This method allows users to query, and depending on their permissions, add, modify and delete records in tables.
     * @param table this is the name of the table the user is accessing.
     * @throws Exception 
     */
    public  void tableMaintenance( String table) throws Exception
    {
        int queryOption     =   1;
        int addOption       =   2;
        int modifyOption    =   3;
        int deleteOption    =   4;
        int maxOption       =   4;
        
        System.out.println("Please enter the number for the option you would like to choose");
        System.out.println("1: Query " + table);
        
        if(table.equals("HelperSubject")) // helper's can't change their subjects, but teachers can modity their ratings for helping in a specific subject.
        {
            
            if(userType.equals("Helper"))
            {
                addOption = deleteOption = modifyOption = 0;
                maxOption = 1;
            }
            else if (userType.equals("Teacher"))
            {
                System.out.println("2: modify " + table);
                modifyOption = maxOption = 2;
                addOption = deleteOption = 0;
                
            }
        }    
        else if(table.equals("Assignments")) //all users only get query acces to the assignments table
        {
            addOption = modifyOption = deleteOption = 0;
            maxOption = 1;
            
        }
        else if(table.equals(userType) && userType.matches("Teacher|Helper")) // so users can't add extra records of their user type. So a teacher can't add another teacher, and a helper can't add anpther helper. But an administrator can.
        {
            System.out.println("2: Modify " + table);
            // if a user deletes their record, it deletes all information on them, so a user isn't allowed to delete their own record.
            modifyOption = maxOption = 2;
            deleteOption = addOption = 0;
            
        }
        else if(table.equals("Class")) // Class can't be modified. There isn't much that should really be modified about a class, rather than just deleting that one and making a new one.
        {
            System.out.println("2: Add a record to " + table);
            System.out.println("3: Delete a record from " + table);
            modifyOption = 0;
            deleteOption = maxOption = 3;
        }
        else 
        {
            System.out.println("2: Add a record to " + table);
            System.out.println("3: Modify a record in " + table);
            System.out.println("4: Delete a record from " + table);
        }
        int choice = validateInt(1,maxOption);
        if(choice == queryOption)
            query(table);
        else if (choice == addOption)
        {
            add(table);
        }
        else if(choice == modifyOption)
        {
            modify(table);
        }
        else if (choice == deleteOption)
        {
            delete(table);
        }
        // can't use switch here because they "aren't constant expressions"
        
    }
    /**
     * This function allows users to query tables they have access to. It will give all relevant data for each query.
     * If they are querying something with that has their ID as a field, it will automatically make the query to give results with their ID.
     * @param table
     * @return
     * @throws Exception 
     */
    public boolean query(String table) throws Exception
    {
        // this ensures the assignments is up to date when being queried. It should be up to date, but if people close the program outside fo the menu, it might not be.
        String[] IDFieldAndID;
        String conditions = "";
        if(table.equals("Assignments"))
        {//maybe have a file to store whether assignments needs to be updated.
            if(isAssignmentsAffected())
            {
                assignments = new Assignments(conn);
                assignments.writeAssignments();
            }
                
        }
        boolean isQueried = false;
        Statement stmt = null;
        
        KVPair<String[], int[]> tableInfo = tableFields.item(table);
        
        
        /*
        what this if statement does is make conditions store the statement needed to only get results based on the user's ID.
        If that isn't applicable (like a teacher querying HelperSubject)then it is based on an ID that the user chooses
        */
        if(userType.equals("Helper") || ( userType.equals("Teacher") && !table.equals("HelperSubject") ) )
        {
            IDFieldAndID = new String[]{tableInfo.getKey()[0], Integer.toString(userID)};
            // this makes it so that the query matches on their ID if the table they are querying stores their ID.
            
            conditions += table + "." + IDFieldAndID[0] + " = " + IDFieldAndID[1] + " and ";
        }
        //at this point it can only be if a teacher is querying helperSubject, or if the user is an admin, so there is no foreign key linked to their ID
        else
        {
            String[] fields = tableInfo.getKey();
            int numIDs = tableInfo.getValue()[0]; // this is the number of identifying fields in the table.
            
            if(numIDs > 1)
            { // this basically just lets the user choose which ID they want to query by. I only let them go by one field because otherwise they're just getting one specific record.
                System.out.println("Please enter the number for which field you would like to query by");
                System.out.println("e.g. If you query by by HelperID, you will get all results in the table with the HelperID that you choose");
                for (int i = 0; i < numIDs; i++) 
                {
                    System.out.println((i+1) + ": " + fields[i]);
                    //the i + 1 is just because I find giving 0 as an option strange
                }
                int choice = validateInt(1,numIDs);
                IDFieldAndID = IDAtIndex(table, choice - 1); // choice -1 to counteract the i+1 I have above.
            }
            else
            {
                IDFieldAndID = IDAtIndex(table, 0);
            }
                
            conditions += table + "." + IDFieldAndID[0] + " = " + IDFieldAndID[1] + " and ";
        }
        
        
        //this bit constructs the query
        LinkedList<String> tableList = new LinkedList();
        String fieldSelect = "";
        tableList.add(table);
        
        String sql = "";
        if(userType.equals("Helper") && table.equals("Assignments"))
        { // this is very specified, and only applies once, so I just hard coded it.
            //this only gives some of the linked information when looking at assignments, rather than everything, because a helper shouldn't see all of it.
            fieldSelect = "ClassTimes.RoomNumber,Period.Week,Period.Day,Period.PeriodNumber,Teacher.TeacherName,Class.SubjectName,Class.ClassYear, Class.Form ";
            
            tableList.add(new String[]{"Teacher","Class","ClassTimes","Period"});
            conditions += "Assignments.ClassID = ClassTimes.ClassID and Assignments.PeriodID = ClassTimes.PeriodID and ClassTimes.PeriodID = Period.PeriodID and ClassTimes.ClassID = Class.ClassID and Class.TeacherID = Teacher.TeacherID and "; 
            //sql += "Order by Period.PeriodID;";
        }
        else 
        {
            /*
            this loop gives all the linked fields to a query. 
            E.g. helperAvailability stores the PeriodID, HelperID and willingness.
            On their own, those are pretty meaningless, so this makes the query give the rest of the information from tables linked by foreign keys, so give the periodNumber, week and day for period ID, and HelperName, yearGroup, email address, etc.
            */
            for (String tableName : tableList) 
            {
                String[] fields = tableFields.item(tableName).getKey();
                for (int i = 0; i < fields.length; i++) 
                {
                    String field = fields[i];
                    /*
                    this check makes sure the foreign getKey check doesn't happen to the IDs of the base tables, which don't have foreign keys
                    If I didn't do it, then it would keep on trying to include the base tables infinitely.
                    */
                    if(!SelfIDTables.matcher(tableName).matches())
                    {
                        switch(field)
                        {
                            //what this does is join tables if a foreign getKey is found, and it isn't to a user's table.
                            case "HelperID":
                                if(!userType.equals("Helper")) // this check makes it so that queries don't give users redundant information on themselves.
                                    //if they aren't explicitly querying their own record, they don't need to see the information that their record stores.
                                {
                                    tableList.add("Helper");
                                    conditions += tableName + ".HelperID = Helper.HelperID and ";
                                }
                                
                                break;
                            case "TeacherID":
                                if(!userType.equals("Teacher")) // same reasoning as above.
                                {
                                    tableList.add("Teacher");
                                    conditions += tableName + ".TeacherID = Teacher.TeacherID and ";
                                }
                                break;
                            case "ClassID": 
                                tableList.add("Class");
                                conditions += tableName + ".ClassID = Class.ClassID and ";
                                break;
                            case "PeriodID":
                                tableList.add("Period");
                                conditions += tableName + ".PeriodID = Period.PeriodID and ";
                                break;       
                        }
                    }
                    else if( i == fields.length -1 && userTables.matcher(tableName).matches()) // this only applies when it is one of the user tables, and it is trying to add the last field
                        //basically it stops the password from turning up in the query
                        continue;
                    
                    //this adds the fields in a table, with a check to make sure that foreign keys aren't enterred twice.
                    if(!(field.contains("ID") && fieldSelect.contains(field)) )                     {
                        fieldSelect += tableName + "." + field + ",";
                    }   
                }
            }
            //assignments is the only table that can link to ClassTimes, so this gets the links needed for that
            if(table.equals("Assignments")) 
            {
                conditions += "Assignments.ClassID = ClassTimes.ClassID and Assignments.PeriodID = ClassTimes.PeriodID and ";
                fieldSelect += "ClassTimes.RoomNumber,ClassTimes.LessonPriority,ClassTimes.HelpersNeeded,";
                sql += "ClassTimes,";
            }
        }
        fieldSelect = fieldSelect.substring(0, fieldSelect.length() -1); //getting rid of the extra comma on the end of the select statements on fields.
        conditions = conditions.substring(0, conditions.length() -4);//getting rid of the extra 'and ' on the end
        
        sql = "Select " + fieldSelect + " \nFrom " + sql;
        for (String tableName : tableList) 
        {
            sql += tableName + ",";
        }
        sql = sql.substring(0, sql.length() -1);
        if(table.equals("Assignments"))
        {
            sql +="\ninner join TempUnavailable on NOT (Assignments.PeriodID = TempUnavailable.PeriodID and Assignments.HelperID = TempUnavailable.PeriodID) ";
        }
        sql += " \nWhere " + conditions;
        ResultSet rs = null;
        
        try
        {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if(!rs.isBeforeFirst())
                System.out.println("Sorry, there aren't any results that match your query.");
            else
            {
                System.out.print("\nResult(s):");
            while(rs.next())
            {
                System.out.println("\n");
                String[] fields = fieldSelect.split(","); // this separates the fields into an array
                for (int i = 0; i < fields.length; i++) 
                {
                    String field = fields[i];
                    String[] parts = field.split("\\."); // this separates the table name from the field name
                    //need the \\ to stop it from doing regex.
                    if(field.contains("Address")) // email address is a field for both helpers and teachers, this makes the output distuinguish it.
                        field = parts[0] + " " + parts[1];
                    else
                        field = parts[1];
                    System.out.println(field + ": "+ rs.getString(i+1));
                    
                }
            }     
            }
                   
            System.out.println();
            stmt.close();
            conn.commit();
            isQueried = true;

        } catch (SQLException e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return isQueried;
    }
   
    /**
     * This gets users to add a record to a table.
     * They have to add the required data for the fields, and then they can add optional fields.
     * @param table the table that they are adding to.
     * @return 
     */
    public boolean add(String table) throws IOException
    {
        boolean bInsert = false;
        Statement stmt = null;
        //fieldsValues is a linked list of strings and values that will be used to add a record to the table.
        LinkedList<String>[] fieldsValues = getValues(table, "add");
        int length = fieldsValues[0].length();
        String fields = "";
        String values = "";
        for (int i = 0; i < length; i++) 
        {
            fields += fieldsValues[0].pop() + ",";
            values += fieldsValues[1].pop() + ",";
        }
        // this removes the comma on the ends
        fields = fields.substring(0,fields.length() -1);
        values = values.substring(0,values.length() -1);
        
        //this will be used to tell the user the getKey that was generated if one was.
        int genKey = -1;
        String genPassword = "";
        try
        {
            stmt = conn.createStatement();
            String sql = "INSERT INTO "+ table + " ("+fields+") \n" +
            "VALUES ("+values+" );";
            int numInserted = stmt.executeUpdate(sql);

            if(numInserted == 0)
            {
                bInsert = false;
                System.out.println("You have not added anything to the table, maybe your IDs were incorrect.");
                if(readAssignmentStatus().equals("pending"))
                {
                    writeOut.write("false ");
                    writeOut.flush();
                }    
            }
            else
            {
                if(SelfIDTables.matcher(table).matches())
                {
                    ResultSet rs = stmt.getGeneratedKeys();
                    if(rs != null && rs.next())
                        genKey = rs.getInt("last_insert_rowid()");
                }
                bInsert = true;   
            }
           
            stmt.close();
            conn.commit();
        } catch (SQLException e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        if(userTables.matcher(table).matches())
        {
            if(!fields.contains("Password"))
            {
                if(table.equals("Helper"))
                    System.out.println("Your password was set to Password+23 by default.\n");
                else
                    System.out.println("Your password was set to PasswordZAQ! by default.\n");
            }
        }
        //this tells the user what getKey was autogenerated by their addition, if one was.
        if(genKey != -1)
        {
            System.out.println("The key generated for your addition was: " + genKey + "\n");
        }
        return bInsert;
    }
    /**
     * This gets the user modify a specific record in a table, by getting them to enter values to specify the field, and then the changes to be made
     * @param table the table to be modified.
     * @return 
     */
    public boolean modify(String table) throws IOException
    {
        boolean bModify = false;
        Statement stmt = null;
        //fields values contains the ID fields and the fields to be changed.
        LinkedList<String>[] fieldsValues = getValues(table, "modify");
        LinkedList<String> fields = fieldsValues[0];
        LinkedList<String> values = fieldsValues[1];
        int numIDs = tableFields.item(table).getValue()[0];
        
        
        //this checks if there any changes. If they didn't enter anything other than the identifying values, then they won't change anything and there's no point executing the query
        if(fields.length() > numIDs)
        {
            int length = fields.length();
            String sql = "update " + table;
            String conditions = " where ";
            //conditions specify that the modification only aplpies to the record specified by the IDs.

            for (int i = 0; i < numIDs; i++) 
            {
                conditions += fields.pop() + " = " + values.pop() + " and ";
            }
            conditions = conditions.substring(0, conditions.length() -4); // this gets rid of the 'and ' on the end
            
            //this then makes the changes to the rest of the records.
            for (int i = 0; i < length; i++) 
            {
                sql += " set " + fields.pop() + " = " + values.pop() +",";
            }
            sql = sql.substring(0, sql.length() -1); // gets rid of the comma on the end.
            sql += conditions;
            try
            {
                stmt = conn.createStatement();

                int numSelected = stmt.executeUpdate(sql);
                if(numSelected ==0)
                {
                    System.out.println("\nYou enterred an incorrect ID, so didn't change a record.\n");
                    bModify = false;
                    if(readAssignmentStatus().equals("pending"))
                    {
                        writeOut.write("false ");
                        writeOut.flush();
                    }
                        
                }
                else
                {
                    bModify = true;
                    
                }

                stmt.close();
                conn.commit();

            } catch (SQLException e)
            {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
        }
        return bModify;
    }
    /**
     * this gets the user to enter identifying information and then deletes that record from the table
     * @param table table that the record to be deleted belongs to.
     * @return 
     */
    public boolean delete(String table) throws IOException
    {
        String conditions = " where ";
        boolean bDelete = false;
        Statement stmt = null;
        
        LinkedList<String> fieldList = new LinkedList();
        LinkedList<String> IDList = new LinkedList();
        LinkedList[] both;
        if(userType.equals("Helper") || userType.equals("Teacher")) // if a teacher or helper deletes a record, it uses their ID.
        {
            fieldList.add(tableFields.item(userType).getKey()[0]);
            IDList.add(Integer.toString(userID));
            for (int i = 1; i < tableFields.item(table).getValue()[0]; i++) { // this then gets the rest of the identifying fields, if there are any
                String[] fieldAndID = IDAtIndex(table, i);
                fieldList.add(fieldAndID[0]);
                IDList.add(fieldAndID[1]);
                
            }
            both = new LinkedList[] {fieldList, IDList};
        }
        else
        {
            //if the user is an administrator, they need to enter all identifying information themselves.
            both = getID(table);
        }
        
        String IDFields = "";
        String IDs = "";
        int length = both[0].length();
        for (int i = 0; i < length; i++) //this sets up the ID to delete from.
        {
            conditions += both[0].pop() + " = "+ both[1].pop() + " and ";
        }
        conditions = conditions.substring(0, conditions.length() -4); // this gets rid of the 'and ' on the end
        IDFields = IDFields.substring(0,IDFields.length() -1);
        IDs = IDs.substring(0,IDs.length() -1);
        
        String sql = "DELETE FROM "+ table + conditions + ";";
        try
        {
            stmt = conn.createStatement();
            int numSelected = stmt.executeUpdate(sql);
            if(numSelected ==0)
            {
                System.out.println("\nYou enterred an incorrect ID, so didn't delete a record.\n");
                bDelete = false;
            }
            else
            {
                bDelete = true;
                if(!table.equals("Administrator")) // warning, may have to change this for the temporary unavailable table.
                {
                    //marks the assignments as needing to be updated.
                    writeOut.write("true ");
                    writeOut.flush();
                }
            }
                
                
            stmt.close();
            conn.commit();

        } catch (SQLException e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }

        return bDelete;
    }
    
    /**
     * This will get the user to give a temporary update to their availability, so they can say that they can't help in that lesson temporarily.
     * Or they can update to say they can now help out again in a lesson that they were unavailable for.
     * It can only be an administrator or a helper doing this.
     * What this function does is mark an assignment as temporarily unavailable, so then gets the next best person for that class, without redoing all other assignments.
     */
    public boolean tempUpdateAssignments()
    {
        //this determines if the user is marking themselves as available again or unavailable.
        System.out.println("Please enter the option you would like to ");
        System.out.println("0: set an assignment to be temporarily unavailable.");
        System.out.println("1: reset an assignment to available again.");
        int choice = validateInt(0,1);
        boolean available;
        if(choice == 0)
            available = false;
        else
            available = true;
        String conditions = "Where ";
        
        // this will get the helperID of the record to update
        int helperID;
        if(userType.equals("Helper"))
        {
            helperID = userID;
            conditions += "HelperID = " + userID + " and ";
        }
        else
        {
            helperID = Integer.parseInt(IDAtIndex("Assignments", 0)[1]);
            conditions += "HelperID = " + helperID + " and ";
        }
        
        int periodID = Integer.parseInt(IDAtIndex("Assignments", 1)[1]);
        conditions += "PeriodID = " + periodID;
        String sql = "select HelperID\n" +
        "From Assignments \n" +
        conditions;
        Statement stmt = null;
        //this checks to see if they have an assignment that they can make available or unavailable, and give more helpful error messages
        try
        {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //this checks if there is no result set. If there isn't, there is not matching assignment.
            if(!rs.isBeforeFirst())
            {
                System.out.println("This isn't an assignment, so it's availability can't be changed.");
                if(available)
                    System.out.println("You may have set yourself to be temporarily unavailable for an assignment you no longer have. So, you aren't considered unavailable anymore.");
            }
                
            stmt.close();
            conn.commit();
            return false;
        } catch (SQLException e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        
        
        if(available)
        {
            sql = "delete from TempUnavailable \n" +
            "where HelperID = " + helperID + " and Periodid = " + periodID;
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
        }
        else
        { // if the user is trying to make themself unavailable.
            sql = "select HelperID\n" +
            "From TempUnavailable\n" +
            conditions;
            stmt = null;
            //this checks to see if they're trying to make themself unavailable for an assignment that they have already made themselves unavailable for.
            try
            {
                boolean unavailable = false;
                stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                if(rs.isBeforeFirst())
                {
                    unavailable = true;
                }

                stmt.close();
                conn.commit();
                if(unavailable)//this skips adding the person to unavailable if they've already done it.
                    return unavailable;
            } catch (SQLException e)
            {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
        
            sql = "insert into TempUnavailable (HelperID, PeriodId) \n" +
            "values(" + helperID + ","+ periodID + ")";
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
        }
        return true;
    }
    /**
     * This just calls ID at index for all identifying fields in the table, so gets all identifying fields from the user.
     * @param table
     * @return 
     */
    private LinkedList<String>[] getID(String table)
    {
        LinkedList<String> IDs = new LinkedList();
        LinkedList<String> IDFields = new LinkedList();
        for (int i = 0; i < tableFields.item(table).getValue()[0]; i++) 
        {
            String[] FieldsAndIDs = IDAtIndex(table, i);
            IDFields.add(FieldsAndIDs[0]);
            IDs.add(FieldsAndIDs[1]);
        }
        
        LinkedList[] both = {IDFields, IDs};
        return both;
    }
    /**
     * This gets the user to enter an ID for a field, either the first or second depending on the index.
     * @param table The table to get an ID from
     * @param index the index in the string array of fields. It will be either 0 or 1
     * @return a string array containing the field, and then the ID for that field.
     */
    public String[] IDAtIndex(String table, int index)
    {
        String IDField = tableFields.item(table).getKey()[index];
        String ID;
        System.out.println("Please enter the " + IDField +" of the record you would like to select.");
        if(numericFields.matcher(IDField).matches())
            ID = Integer.toString(validateInt(1, Integer.MAX_VALUE));
        else
            ID = "'" + validateString() + "'";
        String[] both = {IDField, ID};
        return both;
    }
    
    /**
     * This function gets values from the user, so that they can either add to a table or modify a record in the table.
     * @param table the table that the user is giving values for
     * @param action Whether the user is adding to a table or modifying a record in it.
     * @return 
     */
    private LinkedList<String>[] getValues(String table, String action) throws IOException
    {
        KVPair<String[], int[]> pair = tableFields.item(table);
        String[] fields = pair.getKey(); // this stores the fields that the table has
        int[] neededFields = pair.getValue();// this is an array that has the number of identifying records, and then required fields.
        LinkedList<String> values = new LinkedList();
        LinkedList<String> fieldsEnterred = new LinkedList();
        String field = "";
        int requiredFields =0;
        
        int i =0;
        if(action.equals("add"))
        { 
            requiredFields = neededFields[1]; // this means that they have to enter values for all fields needed for a complete record in the table.
            // this is here so that the IDs for tables with one ID are autogenerated, by making the program skip asking the user for an input on that field
            if(SelfIDTables.matcher(table).matches())
                i =1;
        }
            
        else if(action.equals("modify"))
            requiredFields = neededFields[0]; // this means that they have to enter values for all fields needed to identify a record in the table.
        
        //this while loop goes through all fields that the user must enter a getValue for
        while (i < requiredFields) 
        {
            field = fields[i];
            if(field.equals(userType + "ID")) // this makes it so that dealing with records, they automatically use their ID where applicable, rather than having the option of changing others'
            {
                values.add(Integer.toString(userID));
            }
            else if(costFields.matcher(field).matches())//this is here to make sure the cost fields are kept within range.
            {
                
                System.out.println();
                System.out.println("Please enter the value for the " + field + " field. This value must be between 1 and 10");
                System.out.println("This is a required field, so you must enter something.");
                int temp = validateInt(1, 10);
                if(field.equals("LessonPriority"))
                    temp *= 2; // this is here to essentially make lesson priority more important than the other factors
                values.add(Integer.toString(temp));
            }
            else if(numericFields.matcher(field).matches()) // this makes sure that the user enters a number for fields that require a number, but have no strict range.
            {
                System.out.println();
                System.out.println("Please enter the value for the " + field + " field.");
                if(field.equals("PeriodID"))
                    System.out.println("periodID is basically just a number from 1 to 10 of every period in order, from Monday Period 1 in Week A, to Friday Period 7 in Week B");
                System.out.println("This is a required field, so you must enter something.");
                int temp = validateInt(1, Integer.MAX_VALUE);
                values.add(Integer.toString(temp));
            }
            else // this is for all string values, so it has to be enclosed in apostrophes
            {
                System.out.println();
                System.out.println("Please enter the value for the " + field + " field.");
                System.out.println("This is a required field, so you must enter something.");
                String temp = validateString();
                values.add("'" + temp + "'");               
            }
            fieldsEnterred.add(field); 
            i++;
        }
        //this is for optional fields that the user doesn't have to enter.
        for (int j = requiredFields; j < fields.length; j++) 
        {
            field = fields[j];
            System.out.println();
            System.out.println("Please enter the value for the " + field + " field.");
            if(field.equals("PeriodID"))
                    System.out.println("periodID is basically just a number from 1 to 10 of every period in order, from Monday Period 1 in Week A, to Friday Period 7 in Week B");
            System.out.println("If you would like to leave it empty, then just press enter without inputting anything.");
            
            String temp = validateString();
            //this allows the user to skip fields if they don't want to enter anything for it.
            if(!temp.equals(""))
            {
                if(costFields.matcher(field).matches()) // this validates the cost fields to make sure they stay in range.
                {
                    try
                    {
                        int num = validateInt(temp, 1, 10);
                        if(num < 1)
                        {
                            j--;
                            System.out.println("You have entered an invalid value for this field");
                            continue;
                        }
                        else
                        {
                            //if they are changing or adding a cost field, that could affect the assignments.
                            //this will say that assignments is about to be affected, as long as the update to the database that this function helps to make goes through.
                            //It only says that it's about to be affected if it isn't definitely affected already.
                            if(!isAssignmentsAffected())
                            {
                                writeOut.write("pending "); // this gets read later in the modify and add functions.
                                writeOut.flush();
                            }
                                
                            values.add(temp);
                        }
                    }
                    catch(NumberFormatException e)
                    {
                        //this allows them to choose whether or not they want to change that getValue if they enter an invalid getValue
                         j--;
                        System.out.println("You have entered an invalid value for this field");
                        continue;
                    }
                }
                else if(numericFields.matcher(field).matches())
                {
                    int num = validateInt(temp, 1, Integer.MAX_VALUE);
                    if(num<1)
                        {
                            //this allows them to choose whether or not they want to change that getValue if they enter an invalid getValue
                            j--;
                            System.out.println("You have entered an invalid value for this field");
                            continue;
                        }
                        else
                        {
                            values.add(temp);
                        }
                }
                else if(field.equals("Password")) // validation to make sure they have the right password
                {
                    if(!validatePassword(temp))
                    {
                        System.out.println("Your passwords do not match. \n");
                        j--; 
                        continue;
                    }
                    values.add("'" + temp + "'");
                }
                else // this is for enterring normal string values.
                {
                    values.add("'" + temp + "'");
                    
                }
                fieldsEnterred.add(field);
            }
        }
        LinkedList[] entries = {fieldsEnterred, values};
        return entries;
    }
    
    /**
     * This checks that an inputted ID is stored in the table, and gets the user to try again until they enter the correct ID
     * @return the first correct ID they enter.
     */
    private int validateID()
    {
        /**
         * It takes it in as a string initially to properly validate it
         */
        String in = keyboard.nextLine();
        try
        {
            int ID = Integer.parseInt(in);
            if(ID <1)
            {
                System.out.println("Please reenter your ID. This is a whole number that is 1 or over");
                return validateID();
            }
            return checkTable(ID);

        }
        catch(NumberFormatException e)
        {
            System.out.println("Please enter your ID. This is a whole number that is 1 or over");
            return validateID();
        }
        
    }
    /**
     * This is called as part of the validateID method.
     * It queries the database for the ID and checks to see if it is there or not.
     * @param ID The ID to check if it is stored in the table or not.
     * @return 
     */
    private int checkTable(int ID)
    {
        Statement stmt = null;
        ResultSet rs = null;
        String IDField = tableFields.item(userType).getKey()[0];
        //this makes a query that returns the ID of the user from the table that stores their details
        String sql = "Select " + IDField + " From " + userType + " Where " + IDField + " = " + ID;

        try
        {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            if(!rs.isBeforeFirst()) // this checks if there are no results
            {
                System.out.println("This ID is not stored in the table. Please try again");
                return validateID();
            }   

            stmt.close();
            conn.commit();
            return ID;
        } catch (SQLException e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return validateID();
        }
    }
    
    /**
     * This validates any string input to make sure that they don't put try to inject SQL statements into my queries/
     * @return the first valid string the user enters.
     */
    private String validateString()
    {
        String in = keyboard.nextLine();
        while(!validString(in))
        {
            in = keyboard.nextLine();
        }
        return in;
    }
    /**
     * this returns if a given string is valid or not, so doesn't have SQL keywords in it.
     * @param in
     * @return 
     */
    private boolean validString(String in)
    {
        String temp = in.toUpperCase();
        if(sqlStatements.matcher(temp).matches()) // this checks for sql statements, and stops them
        {
            System.out.println("You can't have some special characters like ' or = in your inputs. Please try again.");
            return false;
        }
        return true;
    }
    /**
     * Validate int validates an integer input between the given range.
     * It is used to validate inputs for navigating the menu, and for some fields that are inputted.
     * @param min minimum getValue the user can enter
     * @param max maximum getValue that can be entered.
     * @return 
     */
    private int validateInt(int min, int max)
    {
        //taken in as a string first to avoid uncaught errors being thrown.
        String in = keyboard.nextLine();
        int num = validateInt(in, min, max);
        while(num < min)
        {
            System.out.println("Please enter a whole number between " + min + " and " + max);
            in = keyboard.nextLine();
            num = validateInt(in,min,max);
        }
        return num;
    }
    /**
     * This checks if a given string is a valid integer within the range of min and max. It doesn't take input from the user.
     * @param in the String input into the function.
     * @param min minimum getValue
     * @param max maximum getValue
     * @return the string as an int if it is valid. An integer that is the minimum -1
     */
    private int validateInt(String in, int min, int max)
    {
        try
        {
            //this is in a try, so that if what was enterred isn't an integer, that program doesn't stop.
            int num = Integer.parseInt(in);
            if(num <min || num > max)
            {
                return min -1;
            }
            return num;
        }
        catch(NumberFormatException e)
        {
            return min -1;
        }
    }
    //this checks to see if the assignments have been permanently affected, so the assignments program has to be run.
    private boolean isAssignmentsAffected() throws FileNotFoundException, IOException
    {
        boolean affected = readAssignmentStatus().equals("true");
        return affected;
        
    }
    //this is here because otherwise reader will try to go to another line after reading, when it only ever has one line and gets overwritten.
    private String readAssignmentStatus() throws FileNotFoundException, IOException
    {
        String status;
        if(reader.hasNext())
        {
            status = reader.next();
            reader.close();
            reader = new Scanner(assignmentStatus);
        }
        else //this covers for if the file is empty. It shouldn't be, but this avoids an error being thrown.
        {
            writeOut.write("false ");
            writeOut.flush();
            status = "false ";
        }
        return status;

    }
}