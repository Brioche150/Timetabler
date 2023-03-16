/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LinkedStructures;


/**
 * This is an element that stores data of type T, and then has pointers to the previous and next element that it is linked to.
 * This series of pointers makes a list that can be iterated through.
 * @param <T> 
 */
public class GenericElement<T>{
    protected T data;
    protected GenericElement<T> previous;
    protected GenericElement<T> next;
        
    public GenericElement(T data, GenericElement<T> previous, GenericElement<T> next)
    {
        this.data = data;
        this.previous = previous;
        this.next = next;           
    }      
    
    @Override
    public String toString()
    {       
        return data.toString();
    }

    public T data()
    {
        return data;              
    }
   
    public GenericElement<T> getPrevious()
    {
        return previous;                
    }
    
    public void setPrevious(GenericElement<T> value)
    {
        previous = value;                
    }
    
    public GenericElement<T> getNext()
    {
        return next;            
    }    
    
    public void setNext(GenericElement<T> value)
    {
        next = value;                
    }       
}

