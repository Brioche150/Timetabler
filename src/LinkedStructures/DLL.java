/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package LinkedStructures;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 *
 * @author Antony Admin
 */
/**
 * This is an abstract class for a doubly linked list.
 * The DLL is made up of elements with a next and previous pointer.
 * The DLL stores the front and back element of the list.
 * The next and previous pointers go all throughout the list, so that every item can be accessed by going forward or backward through the list.
 * @author a-salta
 * @param <T> 
 */
public abstract class DLL<T> implements Iterable<T> 
{
    protected GenericElement<T> front;
    protected GenericElement<T> back;
    protected int length = 0;
    
    /**
     * The default constructor is empty because very little can be done until items are added.
     */
    protected DLL()
    {
        
    }
    
    /**
     * Works by iterating through the linked list an adding each item to the array.
     * @return the linked list as an array of strings. 
     */
    public String[] asArray()
    {
        String[] items = new String[length];
        int i =0;
        for(T data : this)
        {
            items[i] = data.toString();
            i++;
        }
        return items;
    }
    
    @Override
    /**
     * Works by iterating through the list and concatenating each element and a comma to the string.
     * @return a string of all elements separated by a comma
     */
    public String toString()
    {
        String s = "";
        GenericElement<T> e;
        for (T element : this) 
        {
            s += element.toString() + ",";
        }
        s = s.substring(0, s.length() -1);
        return s;
    }
    
    public boolean isEmpty()
    {
        return length == 0;
    }
    
    public int length()
    {
        return length;
    }
    /**
     * removes the specified element from the list and deals with the rest of the logic.
     * @param element 
     */
    protected void removeElement(GenericElement<T> element)
    {
        if(isEmpty())
            throw new IllegalArgumentException("The list is empty, nothing can be removed from it.");
         if(length ==1)// if it's the only item, then the list is emptied.
            {
                front = null;
                back = null;
            }
            else if(element.getPrevious() == null)// if the first node is deleted
            {
                element.getNext().setPrevious(null); // get next one ready to be front.
                front = element.getNext();
            }
            else if(element.getNext() == null) // if it's at the end of the list
            {
                element.getPrevious().setNext(null); 
                back = element.getPrevious();
            }  
            else// if item deleted is in the middle
            {
                //rearranges to pointers of the element before and after the one to be deleted to point to each other.
                //the element to be deleted then has nothing pointing to it.
                element.getPrevious().setNext(element.getNext());
                element.getNext().setPrevious(element.getPrevious());
            }
            length--;
    }
    /**
     * adds an element before a specified element. 
     * @param insert element to be added. This does not need to point to anything.
     * @param element element to be added before. this does not need to point to the element to be added.
     */
    protected void addBeforeElement(GenericElement<T> insert, GenericElement<T> element)
    {
        insert.setNext(element);
        if(front == element)
            front = insert;
        else
        {
            element.getPrevious().setNext(insert);
            insert.setPrevious(element.getPrevious());
        }
        element.setPrevious(insert);
        length++;
    }
    /**
     * adds the element to the back of the list. Will generally be called when just normally adding to the list.
     * Can be sued to add to an empty list as well
     * @param insert element to be added. insert doesn't need to point to back and back doesn't need to point to insert
     */
    protected void addToBack(GenericElement<T> insert)
    {
        if(isEmpty())
        {
            front = insert;
        }
        else
        {
            back.setNext(insert);
            insert.setPrevious(back);
        }
        back = insert;
        length++;
    }
    /**
     * Removes the first instance of the data found in the list.
     * It finds it by iterating through the list until it is found.
     * @param data the data what is searched for to be deleted.
     */
    public void remove(T data) 
    {  
        boolean isFound = false;
        if(isEmpty())
            throw new IllegalArgumentException("The list is empty, nothing can be removed from it.");
        
        GenericElement<T> current = front;
        if(current.data().equals(data))//edge case helps for when the first one matches
        {
            isFound = true;
        }
        while(current.getNext()!=null && isFound == false)
        {//Iterate through elements in the linked list
            current = current.getNext();
            if(current.data().equals(data))
                isFound = true;      
        }
        if(isFound) 
        {
            removeElement(current);
        }
        //if there isn't any element with that data.
        else
           throw new IllegalArgumentException("This value is not in the list"); 
    }
   /**
    * This just empties the whole linked list. Can achieve the same result by making a new linked list.
    */
    public void empty()
    {
        front = null;
        back = null;
        length =0;
    }
    /**
     * Remove the first element from the queue and reorganise the queue
     * @return the data stored in the first element, which is being popped from the list.
     */
    public T pop()
    {
        //this doesn't use remove element, because the logic is slightly simpler, since the front is always being element.
        if(isEmpty())
            throw new IllegalArgumentException("The list is empty, it cannot be popped from");
        
        GenericElement<T> current = front;
        T temp = front.data;
        if(length==1) // edge case if only one item in list
        {
            front = null;
            back = null;
        }
        else 
        {// this sets the next element to be front.
            current.getNext().setPrevious(null);
            front = current.getNext();
        }  
        length--;
        return temp;
    }
    
    /**
     * Removes the element at the index specified and the returns the data stored there
     * @param index index of element to be deleted.
     * @return the data stored at the element being element
     */
    public T pop(int index)
    {
        GenericElement<T> element = elementAtIndex(index);
        T temp = element.data();
        removeElement(element);
        return temp;
    }
    
    /**
     * 
     * @return the data stored in the first element of the list.
     */
    public T peek()
    {
        return front.data();
    }
    
    /**
     * This iterates through the list until it finds the first instance of the data passed in.
     * @param data the data it is searching for.
     * @return the index of the first instance of the data passed, -1 if it isn't present.
     */
    public int dataIndex(T data)
    {//Return the position of the value in the linked list, -1 if not present
        if(isEmpty())
            return -1;
        boolean isFound = false;
        GenericElement<T> current = front;
        int count = 0;
        if(current.data().equals(data)) //edge case for if the first value matches
        {
            return count;
        }
        while(current.getNext()!=null &&  isFound == false)
        {//Iterate through elements in the LinkedList
            current = current.getNext();
            count++;
            if(current.data().equals(data))
                isFound = true;
        }
        if(!isFound)
            return -1;
        return count;
    }
    
    /**
     * 
     * @param data
     * @return true if the list contains the specified data.
     */
    public boolean contains(T data)
    {
        return dataIndex(data) >=0;
    }
    
    
    public abstract DLL copy(); // suclasses have to make a copy of the list
    
    /**
     * returns the data stored at a specified index
     * @param index
     * @return 
     */
    public T dataAtIndex(int index)
    {
        GenericElement<T> e = elementAtIndex(index);
        return e.data();
    }
    /**
     * returns the element at the specified index on the list.
     * It will either go from the front or the back, depending on which is closer to the index.
     * @param index the index to go to
     * @return 
     */
    protected GenericElement<T> elementAtIndex(int index)
    {
        if(isEmpty())
            throw new IllegalArgumentException("The list is empty, so can't be searched");
        if(index >= length || index <0)
            throw new IndexOutOfBoundsException("The index specified was outside of the range of the list");
        GenericElement<T> current;
        if(index >= length /2) // this determines if it starts from the front or back of the list.
        {
            current = back;
            for(int i =0; i< length - (index +1); i++)
            {// iterate through the list to the index wanted
                current = current.getPrevious();
            }
        }
        else
        {
            current = front;
            for(int i =0 ; i <index; i++)
            {// iterate through the list to the index wanted
                current = current.getNext();
            }
        }
        return current;
    }

    @Override
    /**
     * allows an action to be done to every element in the list.
     */
    public void forEach(Consumer<? super T> action) { // this function is needed for the list to be iterable.
        Iterable.super.forEach(action);   
    }
    
    @Override
    /**
     * returns an iterator object of the list.
     */
    public Iterator<T> iterator()
    {
        return new ListIterator<T>();
    }
    
    /**
     * this private class is needed to be an iterator when iterating through a list with the for each shorthand
     * @param <T> 
     */
    private class ListIterator<T> implements Iterator<T>
    {
        //current is set to be be before the first element, because the for each shortcut calls next immediately.
        private GenericElement<T> current = new GenericElement(null,null,front);
        /**
         * nothing is needed in this, it's purely determined by the list it's made from
         */
        public ListIterator()
        {
            
        }
        @Override
        /**
         * checks if there is another element in the iterator
         */
        public boolean hasNext()
        {
            return current.next != null;
        }
        @Override
        /**
         * sets current to the next element and returns the data stored in that next element.
         */
        public T next()
        {
            current = current.next;
            return current.data;
        }
        
    }
}