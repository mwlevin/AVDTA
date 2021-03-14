/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.moves;

/**
 *
 * @author ml26893
 */
public class CellRecord implements Comparable<CellRecord>
{
    private int vehid, enter, exit;
    
    public CellRecord(int vehid, int enter, int exit)
    {
        this.vehid = vehid;
        this.enter = enter;
        this.exit = exit;
    }
    
    public int getVehId()
    {
        return vehid;
    }
    
    public int getEnter()
    {
        return enter;
    }
    
    public int getExit()
    {
        return exit;
    }
    
    
    public int compareTo(CellRecord rhs)
    {
        if(vehid != rhs.vehid)
        {
            return vehid - rhs.vehid;
        }
        else
        {
            return enter - rhs.enter;
        }
    }
}
