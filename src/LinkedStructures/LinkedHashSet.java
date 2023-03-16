package LinkedStructures;

/**
 *
 * @author Antony Admin
 */
/**
 * a hash set stores a collection of keys.
 * It stores each key to an index in an array that is determined by what the key hashes to.
 * @author a-salta
 * @param <K> This is the key being stored for the hash map
 *  
 */
public class LinkedHashSet<K> 
{
    protected final int MAX_SIZE;
    protected final double loadFactor = 0.75;
    protected int length;
    protected boolean[] isDeleteds;
    protected GenericElement<K>[] keys;
    LinkedList<K> keyList = new LinkedList();
    //this is for the hash map, so that if there is an array of values, it has the index that the key was inserted to.
    protected int accessedIndex = -1;
    
    public LinkedHashSet()
    {
        MAX_SIZE = 11;
        length=0;
       // KVPairs = new KVPair[MAX_SIZE];
        keys = new GenericElement[MAX_SIZE];
        isDeleteds = new boolean[MAX_SIZE];
    }
    public LinkedHashSet (int size)
    {
        MAX_SIZE = (int)(size/loadFactor);
        length=0;
        //KVPairs = new KVPair[MAX_SIZE];
        keys = new GenericElement[MAX_SIZE];
        isDeleteds = new boolean[MAX_SIZE];
    }
    
    public LinkedList<K> getKeys()
    {
        return keyList;
    }
    /**
     * Adds a key to the hash map, that is determined by what the key hashes to.
     * @param key 
     */
    public void add (K key) 
    {
       int index = freeSpot(key); 
       if(index >= 0)//only adds the item if it isn't already here
        {
            if(isFull())
                throw new UnsupportedOperationException("The hash table is full, nothing can be added");
            GenericElement<K> insert = new GenericElement(key, null, null);
            keyList.addToBack(insert);
            keys[index] = insert;
            length++;
            accessedIndex = index;
        }
       else
           throw new IllegalArgumentException("This key is already stored in the hash set, it cannot be added again");
    }
    /**
     * This lets the user add many items in one function
 It allows for slightly faster logic than adding one by one because most of the pointers are already done by the linked itemList.
     * @param itemList the linked itemList contains the keys to be added.
     */
    public void add(LinkedList<K> itemList)
    {

        if(itemList.length() + length > MAX_SIZE)
            throw new IllegalArgumentException("The hash table cannot add this many items to it.");
        for (K key : itemList) 
        {
            if(contains(key))
                throw new IllegalArgumentException("At least one of the keys in the list is already in the hash map, so you cannot add it");
        }
        itemList = itemList.copy(); // this is done to make sure the original list isn't changed.
        keyList.add(itemList);
        GenericElement<K> current = itemList.front;
        
        while(current != null)
        {
            keys[freeSpot(current.data())] = current;
            current = current.getNext();
        }
        length += itemList.length();
    }
    
    
    public boolean isFull()
    {
        return length == MAX_SIZE;
    }
    
    public void delete(K key) 
    {
        int index = indexOfKey(key);
        if(index == -1) // This checks if the hash map contains the key.
            throw new IllegalArgumentException("This key is not stored in this hash table");
        accessedIndex = index;
        GenericElement<K> temp = keys[index];
        keyList.removeElement(temp);
        keys[index] = null;
        isDeleteds[index] = true;
        length--; 
    }
    
    /**
     * Says if the key is in the hash map or not.
     * @param key
     * @return 
     */
    public boolean contains(K key) 
    {
        return indexOfKey(key) >= 0;
    }
    /**
     * returns the index of a key if it's there, and -1 if it isn't.
     * It works by looking at where the key hashes to, and then iterates through the array from there.
     * It stops looking once it hits null, because that means nothing was ever there
     * @param key
     * @return 
     */
    protected int indexOfKey(K key) //returns the index of a key if it's there, and -1 if it isn't, stops looking once it hits a 0, because that means nothing was ever there
    {
        int hash = hash(key);
        int index;
        for (int i = 0; i < MAX_SIZE; i++) 
        {
            index =  (i + hash) % MAX_SIZE;
            if(keys[index]==null && isDeleteds[i] == false)
                return -1;
            if(keys[index]==null && isDeleteds[i] == true)
            {
                continue;
            }
            else if(keys[index].data().equals(key))
                return index;
           
        }
        return -1; // If you go through the whole thing without hitting a non-deleted spot.
    }
     /**
     * Returns the index that a key will be stored in. It is only used in the add methods.
     * @param key
     * @return 
     */
    protected int freeSpot(K key)
    {
        int hash = hash(key);
        int index;
        for (int i = 0; i < MAX_SIZE; i++) 
        {
            index =  (i + hash) % MAX_SIZE;
            if(keys[index] == null)
                return index;
        }
        return -1; //This only happens when the hash table is full
    }

    public int length() 
    {
        return length;
    }
    public boolean isEmpty() 
    {
        return length == 0;
    }
    protected boolean isSlotFree(int index) // Tells you if you can add the value to this index
    {
        return keys[index] == null;
    }
    /**
     * returns an integer that a key will hash to. The hash is a positive number that can be an index in the array.
     * @param key
     * @return 
     */
    protected int hash(K key)
    {
        return ( ((key.hashCode()) % MAX_SIZE) + MAX_SIZE ) % MAX_SIZE;
    }    
}