/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DatabaseConnect;

/**
 *
 * @author Antony Admin
 */
public class AssignmentInfo 
{
    private int HelperID;
    private int PeriodID;
    private int ClassID;
    private int cost;
    
    public AssignmentInfo(int HelperID, int ClassID, int PeriodID, int cost) {
        this.HelperID = HelperID;
        this.PeriodID = PeriodID;
        this.ClassID = ClassID;
        this.cost = cost;
    }

    public int getCost() {
        return cost;
    }
    
    public int getHelperID() {
        return HelperID;
    }

    public int getPeriodID() {
        return PeriodID;
    }

    public int getClassID() {
        return ClassID;
    }

}