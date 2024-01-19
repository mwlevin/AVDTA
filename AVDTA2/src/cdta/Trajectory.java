/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cdta;

import avdta.network.Simulator;
import cdta.cell.Cell;
import java.util.ArrayList;

/**
 *
 * @author ml26893
 */
public class Trajectory extends ArrayList<Cell>
{
    public int getTT()
    {
        return (get(size()-1).getTime() - get(0).getTime()) * Simulator.dt;
    }
    
    public int getExitTime()
    {
        return get(size()-1).getTime() * Simulator.dt;
    }
    
    public int getEnterTime()
    {
        return get(0).getTime() * Simulator.dt;
    }
}
