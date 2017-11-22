/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.duer;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ml26893
 */
public class VMS 
{
    public static final VMS NULL = new VMS();
    public static final VMS ALL = new VMS()
    {
        public double getProbOfInformation(Incident i)
        {
            return 1.0;
        }
    };
    
    private Map<Incident, Double> information;
    
    public VMS()
    {

    }
    
    public VMS(Map<Incident, Double> information)
    {
        this.information = information;
    }
    
    public Map<Incident, Double> getInformationProvided()
    {
        return information;
    }
    
    public double getProbOfInformation(Incident i)
    {
        if(information != null && information.containsKey(i))
        {
            return information.get(i);
        }
        else
        {
            return 0;
        }
    }
}
