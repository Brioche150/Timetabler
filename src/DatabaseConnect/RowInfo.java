/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DatabaseConnect;
import LinkedStructures.PriorityQueue;
/**
 *
 * @author Antony Admin
 */
public class RowInfo 
{
    private PriorityQueue<LessonInfo> pq;
    
    private int periodID;

    public RowInfo(PriorityQueue<LessonInfo> pq, int periodID) {
        this.pq = pq;
        this.periodID = periodID;
    }

    public PriorityQueue<LessonInfo> getPq() {
        return pq;
    }
    public int getPeriodID() {
        return periodID;
    }
    
}
