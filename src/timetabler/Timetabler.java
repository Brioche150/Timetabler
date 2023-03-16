/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package timetabler;
import LinkedStructures.*;
import hungarian.Hungarian;
import DatabaseConnect.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Scanner;
import java.util.regex.Pattern;
/**
 *
 * @author Antony Admin
 */
public class Timetabler {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception 
    {
        //TODO
        //Finish off the code for making temporary assignment updates. Maybe have cost to keep some balance among those eligible to cover someone's place
        //make rows a hash map, see if that works, and maybe flip around helperPeriods
        // last one is, have some basic symmetric encryption on the passwords.
       
       
        /*
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        PrivateKey privateKey = pair.getPrivate();
        PublicKey publicKey = pair.getPublic();
        try (FileOutputStream fos = new FileOutputStream("public.key")) {
            fos.write(publicKey.getEncoded());
        }
        */
       
        UserInterface ui = new UserInterface();
        ui.userMenu();
                
        //Assignments assignments = new Assignments();
        //assignments.writeAssignments();
        //System.out.println("We done");
        /*
        File assignmentStatus = new File("AssignmentStatus.txt");
        Scanner reader = new Scanner(assignmentStatus);
        FileWriter writeOut = new FileWriter(assignmentStatus, true);
        writeOut.write("Boo");
        writeOut.close();
        //String in = reader.next();
        //System.out.println(in);
        */
    }
    /*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
    
}