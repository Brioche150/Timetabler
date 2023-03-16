/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LinkedStructures;

/**
 * This is a priority queue. The position that an element is put in the list is determined by its rank.
 * a lower rank means that the element is put toward the front of the queue.
 * @param <T> 
 */
public class PriorityQueue<T> extends DLL<T>
{
    /**
     * Makes a priority queue. A priority queue can be any size, and is made of elements that have data and a rank. 
     * Lower ranks are put towards the front of the queue.
     */
    public PriorityQueue()
    {
        super();
    }
    
    /**
     * This is the normal way of adding elements to the list. This will make elements come before the first element that they encounter that has a higher rank than it.
     * @param value this is the data to be stored
     * @param rank This is the priority of this element, which determines its position.
     */
    public void add(T value, int rank)
    {
        if(isEmpty())
        {
            addToBack(new ElementRank(value, rank, null, null));
        }
        else
        {
            ElementRank<T> insert;
            ElementRank<T> current = (ElementRank<T>) front;
            boolean isInserted =false;
            
            if(rank<current.rank()) //edge case for the element being added to the front
                isInserted =true;
            while(current.getNext()!=null && isInserted == false) 
            {
                current = current.getNext();
                if(rank<current.rank())
                    isInserted =true;
                
            }
            if(isInserted) 
            {
                insert = new ElementRank(value, rank, null, null);
                addBeforeElement(insert, current);
            }
            else //put at the end of the list
            {
                insert = new ElementRank(value, rank, null, null);
                addToBack(insert);
            }
        }
    }
    /**
     * iterates through the list until it finds the first instance of this rank.
     * @param rank
     * @return The index of the first instance of this rank in the queue.
     */
    public int indexOfRank(int rank)
    {
        if(isEmpty())
            throw new IllegalArgumentException("The list is empty, so can't be searched");
        boolean isFound = false;
        ElementRank<T> current = (ElementRank<T>) front;
        int count = 0;
        if(current.rank() == rank)
        {
            return count;
        }
        while (current.getNext() != null && isFound == false)
        {//Iterate through elements in the LinkedList
            current = current.getNext();
            count++;
            if(current.rank() == rank)
                isFound = true;
            
        }
        if(!isFound)
            throw new IllegalArgumentException("this rank is not held in this list");
        return count;
    }
   
   /**
    * this makes a copy of the queue, so that you can do things to the queue without affecting the original.
    */
    @Override
   public PriorityQueue copy()
    {
        PriorityQueue<T> copy = new PriorityQueue();
        copy.front = front;
        copy.back = back;
        copy.length = length;
        return copy;
    }
    
   
}
