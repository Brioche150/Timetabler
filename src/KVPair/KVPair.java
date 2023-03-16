/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package KVPair;

/**
 *This is a class that just stores two generic values. It's main use is in key value pairs for a hash map, but it works as just storing two pieces of data.
 */
public class KVPair <K,V>
{
    private K key;
    private V value;
    public KVPair (K key, V value)
    {
        this.key = key;
        this.value = value;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public void setValue(V value) {
        this.value = value;
    }
    
    public K getKey()
    {
        return key;
    }
    public V getValue()
    {
        return value;
    }
    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof KVPair)
        {
            return ((KVPair) obj).getKey().equals(key) && ((KVPair) obj).getValue().equals(value);
        }
        else
            return false;
    }
    @Override
    public String toString()
    {
        return key.toString() + ", " + value.toString();
    }
    
}
