/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pedestrian;

/**
 *
 * @author micha
 */
public class Action 
{
    private double duration;
    private int[] queueLen;
    private boolean[] crosswalk;
    
    public Action(boolean[] crosswalk, int[] queueLen, double duration)
    {
        this.crosswalk = crosswalk;
        this.queueLen = queueLen;
        this.duration = duration;
    }
    
    public double getDuration()
    {
        return duration;
    }
    
    public boolean[] getCrosswalkActivation()
    {
        return crosswalk;
    }
    
    public int[] getQueueChanges()
    {
        return queueLen;
    }
}
