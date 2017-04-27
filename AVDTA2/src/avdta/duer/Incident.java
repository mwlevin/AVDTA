/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.duer;

/**
 *
 * @author hdx
 */
public class Incident 
{
    private int id;
    
    public Incident(int id)
    {
        this.id = id;
    }
    
    public int getId()
    {
        return id;
    }
    
        public int hashCode()
    {
        return id;
    }
}
