/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.type;

import avdta.network.link.Link;
import avdta.network.link.LinkRecord;

/**
 *
 * @author mlevin
 */
public class Type 
{
    private int type;
    private String description;
    
    public Type(int type, String description)
    {
        this.type = type;
        this.description = description;
    }
    
    
    public Type getBase()
    {
        return this;
    }
    
    public boolean isValid(LinkRecord link)
    {
        return true;
    }
    
    public int getCode()
    {
        return type;
    }
    
    public String getDescription()
    {
        return description;
    }
    
    public String toString()
    {
        return description;
    }

}
