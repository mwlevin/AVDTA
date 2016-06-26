/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

import avdta.network.node.Node;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Michael
 */
public class BusLink extends TransitLink
{
    private Set<TTRecord> tts;
    
    public BusLink(Node source, Node dest)
    {
        super(source, dest);
        
        tts = new TreeSet<TTRecord>();
    }
    
    public void reset()
    {
        tts.clear();
    }
    
    public void setTT(int enter, int exit)
    {

        tts.add(new TTRecord(enter, exit-enter));
    }
    
    public double getTT(int dtime)
    {
        for(TTRecord t : tts)
        {
            if(t.getDepTime() >= dtime)
            {
                double output = t.getTT() + t.getDepTime() - dtime;
                
 
                return output;
            }
        }
        
        return Integer.MAX_VALUE;
    }
    
    static class TTRecord implements Comparable<TTRecord>
    {
        private int dtime, tt;
        
        public TTRecord(int dtime, int tt)
        {
            this.dtime = dtime;
            this.tt = tt;
        }
        
        public int getDepTime()
        {
            return dtime;
        }
        
        public int getTT()
        {
            return tt;
        }
        
        public boolean equals(Object o)
        {
            TTRecord rhs = (TTRecord)o;
            return rhs.dtime == dtime;
        }
        
        public int compareTo(TTRecord rhs)
        {
            return dtime - rhs.dtime;
        }
        
        public int hashCode()
        {
            return dtime;
        }
        
    }
}
