/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LinkedStructures;

/**
 * This is a subclass of generic element, so also works as a a part of a list that can be iterated over.
 * It also has a rank attribute, which is used to determine where the element should be stored in a priority queue.
 * @param <T> 
 */
public class ElementRank<T> extends GenericElement<T> {
    private int rank;
        
    public ElementRank(T data, int rank, ElementRank<T> previous, ElementRank<T> next)
    {
        super(data, previous, next);
        this.rank = rank;
    }      
    
    @Override
    public String toString()
    {       
        return data.toString();
    }

    public int rank()
    {
        return rank;
    }
    
    @Override
    public ElementRank<T> getPrevious()
    {
        return (ElementRank) previous;                
    }
    @Override
    public ElementRank<T> getNext()
    {
        return (ElementRank) next;                
    }
    
    public void setPrevious(ElementRank<T> value)
    {
        previous = value;                
    }
    
    public void setNext(ElementRank<T> value)
    {
        next = value;                
    }   
   
}

