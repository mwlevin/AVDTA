/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.duer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hdx
 */
public class Incident 
{
    public static final Incident NULL = new Incident(0, 0, 0, new ArrayList<IncidentEffect>());
    
    private List<IncidentEffect> effects;
    
    private int id;
    private double pOff, pOn;
    
    public Incident(int id, double pOn, double pOff,  List<IncidentEffect> effects)
    {
        this.id = id;
        this.pOff = pOff;
        this.pOn = pOn;
        this.effects = effects;
    }
    
    public double getProbabilityOn()
    {
        return pOn;
    }
    
    public double getProbabilityOff()
    {
        return pOff;
    }
    
    public int getId()
    {
        return id;
    }
    
    public int hashCode()
    {
        return id;
    }
    
    public List<IncidentEffect> getEffects()
    {
        return effects;
    }
}
