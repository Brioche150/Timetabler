/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DatabaseConnect;
/**
 *
 * @author Antony Admin
 */
public class LessonInfo 
{
    private int helpersNeeded;
    private int lessonPriority;
    private int classID;
    private String subject;

    public LessonInfo(int helpersNeeded, int lessonPriority, int classID, String subject) {
        this.helpersNeeded = helpersNeeded;
        this.lessonPriority = lessonPriority;
        this.classID = classID;
        this.subject = subject;
    }

    public int getClassID() {
        return classID;
    }

    

    public int getLessonPriority() {
        return lessonPriority;
    }

    public int getHelpersNeeded() {
        return helpersNeeded;
    }

    public void setHelpersNeeded(int helpersNeeded) {
        this.helpersNeeded = helpersNeeded;
    }

    public String getSubject() {
        return subject;
    }
    
}
