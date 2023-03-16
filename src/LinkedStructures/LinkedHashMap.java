/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LinkedStructures;
import KVPair.*;

/**
 * a hash map stores a collection of keys that will have values associated with them.
 * It stores them based on the key, which allows for quick adding and deleting.
 * @author a-salta
 * @param <K> This is the getKey for the hash map
 * @param <V> This is the getValue stored with the getKey.
 *  
 */
public class LinkedHashMap <K,V> extends LinkedHashSet<K>
{
    private GenericElement<V>[] values;
    private LinkedList<V> valueList = new LinkedList();
    //pairs is the collection of keys and values. It also stores the pointers between the elements to be able to turn this into a linked list.
   
    /**
     * Default constructor for the hash map, makes it with a maximum size of 11.
     */
    public LinkedHashMap()
    {
        super();
        values = new GenericElement[MAX_SIZE];
    }
    /**
     * Lets the user set a max size for the hash map
     * The max size will be set to the size they set divided by the load factor.
     * This means that there should be a decent amount of empty spaces in the case of collisions, to avoid lookups taking too long.
     * @param size The size they want their array to be.
     */
    public LinkedHashMap (int size)
    {
        super();
        values = new GenericElement[MAX_SIZE];
    }
    /**
     * This returns all of the keys and values as a linked list.
     * This is done by using the front and back pointers stored in the class. 
     * all elements stored in the hash map have pointers to each other to be able to quickly make a linked list without having to iterate through the array.
     * @return a linked list of the keys and values together.
     */
    
    public LinkedList<V> getValues()
    {
        return valueList;
    }
    /**
     * the key and values are added to the array that makes the hash map.
     * This is done by storing it in an index in the array of pairs.
     * The index is determined by the key. It is hashed to get a number, and it is modulused by the max size to get an index in the array.
     * @param key This getKey is used to determine where the getValue is stored in the array, and allow the getValue to be accessed quickly.
     * @param value This getValue is stored with the getKey
     */
    public void add (K key, V value) 
    {
        super.add(key);
        int index = accessedIndex;
        GenericElement<V> insert = new GenericElement(value, null, null);
        values[index] = insert;
        valueList.addToBack(insert);   
    }
    /**
     * This just lets the user add many items in one go.
     * It allows for some slightly faster logic than adding one by one because most of the pointers are already done by the linked list.
     */
    
    public void add(LinkedList<K> keyListIn, LinkedList<V> valueListIn)
    {
        if(keyListIn.length() + length > MAX_SIZE)
            throw new UnsupportedOperationException("The hash table cannot add this many items to it.");
        if(keyListIn.length() != valueListIn.length())
            throw new IllegalArgumentException("The key list is not the same length as the value list, they need to be of equal length");
        for (K key : keyListIn) 
        {
            if(contains(key))
                throw new UnsupportedOperationException("At least one of the keys in the list is already in the hash map, so you cannot add it");
        }
        keyListIn = keyListIn.copy(); // this is done to make sure the original list isn't changed.
        valueListIn = valueListIn.copy();
        // this handles the logic to connect the pointers of the hash map and the linked itemList.
        valueList.add(valueListIn);
        keyList.add(keyListIn);
        GenericElement<K> currentKey = keyListIn.front;
        GenericElement<V> currentValue = valueListIn.front;
        
        // this while loop adds each element from the itemList into the hash map with the arrays
        int index;
        while(currentKey != null) 
        {
            index = freeSpot(currentKey.data());
            keys[index] = currentKey;
            values[index] = currentValue;
            currentValue = currentValue.getNext();
            currentKey = currentKey.getNext();
        }
        length += keyListIn.length();
    }
    /**
     * This allows the user to replace the getValue stored with a list.
     * @param key
     * @param value 
     */
    public void replaceValue(K key, V value)
    {
        if(!contains(key))
            throw new IllegalArgumentException("This key isn't stored, there is nothing to replace");
        
        KVPair<K,V> pair = new KVPair(key, value);
        int index = indexOfKey(key);
        GenericElement<V> temp = values[index];
        temp.data = value;
    }

    /**
     * returns the item associated with the given getKey.
     * @param key
     * @return 
     */
    public V item(K key) 
    {
        int index = indexOfKey(key);
        if(index == -1) // This checks if the hash map contains the getKey.
            throw new IllegalArgumentException("This node is not stored in this hash table");
        return values[index].data();
    }

    /**
     * This deletes the getKey and the associated getKey from the hash map.
     * 
     * @param key 
     */
    @Override
    public void delete(K key) 
    {
        super.delete(key);
        int index = accessedIndex;
       
        GenericElement<V> temp = values[index];
        valueList.removeElement(temp);
        values[index] = null;
    } 
}