/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.util;

import java.lang.reflect.Array;

/**
 *
 * @author mlevin
 */
public final class Util
{
    public static<T> int indexOf(T[] array, T search)
    {
        for(int i = 0; i < array.length; i++)
        {
            if(array[i] == search)
            {
                return i;
            }
        }
        return -1;
    }
    
    public static<T> T[] concatenate(T[] array1, T[] array2)
    {
         T[] output = (T[]) Array.newInstance(array1.getClass().getComponentType(), array1.length+array2.length);
        
        for(int i = 0; i < array1.length; i++)
        {
            output[i] = array1[i];
        }
        
        int idx = array1.length;
        
        for(int i = 0; i < array2.length; i++)
        {
            output[i+idx] = array2[i];
        }
        
        return output;
    }
}
