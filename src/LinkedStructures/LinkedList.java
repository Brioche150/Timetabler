/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LinkedStructures;

/**
 * This is a regular doubly linked list. Items that are added are put on the end unless it is added by index.
 * @param <T> 
 */
public class LinkedList<T> extends DLL<T>
{  
    /**
     * This creates a linked list. A linked list can be any size and adding will add to the end of the list.
     */
    public LinkedList()
    {
        super();
    }
    
    /**
     * inserts the data into an element in the appropriate index.
     * @param data data to be stored.
     * @param index index that the element should be at.
     */
    public void insert(T data, int index)
    {
        //only lets them insert into the list, not the end, otherwise they can just append.
        if(isEmpty()) 
            throw new IllegalArgumentException(("The list is empty, so can't be inserted into. Append instead"));
        
        GenericElement<T> element = elementAtIndex(index);
        GenericElement<T> insert = new GenericElement(data, null,null);
        addBeforeElement(insert, element);
       
    }
    /**
     * adds the data to the end of the list.
     * @param data 
     */
    public void add(T data)
    {   
        GenericElement insert =new GenericElement(data, null, null);
        addToBack(insert);
    }
    /**
     * adds each item in the array to the back of the list.
     * @param items 
     */
    public void add(T[] items)
    {
        for (T item : items) 
        {
            add(item);
        }
    }
    /**
     * this is slightly faster than adding one by one, if you already have a linked list of the things to add.
     * It isn't faster if the linked list is just being made to add it in to another list, however.
     * @param items 
     */
    public void add(LinkedList<T> items)
    {
        items = items.copy();
        if(isEmpty())
        {
            front = items.front;
            back = items.back;
        }
        else
        {
            back.setNext(items.front);
            if(!items.isEmpty())
                items.front.setPrevious(back);
        }
        length += items.length();
    }
    /**
     * this makes a copy of the list. It works to stop a list that is passed in from being affected.
     * @return 
     */
    @Override 
    public LinkedList copy()
    {
        LinkedList<T> copy = new LinkedList();
        copy.front = front;
        copy.back = back;
        copy.length = length;
        return copy;
    }   
}