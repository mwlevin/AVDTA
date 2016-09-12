/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import java.util.List;

/**
 *
 * @author ml26893
 */
public class PhaseRecord implements java.io.Serializable, Comparable<PhaseRecord>
{
    private int node, sequence;
    private double time_red, time_yellow, time_green;
    private List<TurnRecord> turns;
    
    
    public PhaseRecord(Node node, int sequence, double time_red, double time_yellow, double time_green, List<TurnRecord> turns)
    {
        this(node.getId(), sequence, time_red, time_yellow, time_green, turns);
    }
    
    public PhaseRecord(int node, int sequence, double time_red, double time_yellow, double time_green, List<TurnRecord> turns)
    {
        this.node = node;
        this.sequence = sequence;
        this.time_red = time_red;
        this.time_yellow = time_yellow;
        this.time_green = time_green;
        this.turns = turns;
    }
    
    public String toString()
    {
        String inc = "";
        String out = "";
        
        for(TurnRecord t : turns)
        {
            inc += t.getI()+",";
            out += t.getJ()+",";
        }
        
        if(inc.length() > 0)
        {
            inc = inc.substring(0, inc.length()-1);
            out = out.substring(0, out.length()-1);
        }
        
        return node+"\t1\t"+sequence+"\t"+time_red+"\t"+time_yellow+"\t"+time_green+"\t"+turns.size()+"\t{"+inc+"}\t{"+out+"}";
    }
    
    public void addTurn(Turn t)
    {
        turns.add(new TurnRecord(t));
    }
    public void addTurn(TurnRecord t)
    {
        turns.add(t);
    }
    
    public void setTurns(List<TurnRecord> turns)
    {
        this.turns = turns;
    }
    
    public List<TurnRecord> getTurns()
    {
        return turns;
    }
    
    public double getTimeGreen()
    {
        return time_green;
    }
    
    public void setTimeGreen(double t)
    {
        time_green = t;
    }
    
    public double getTimeRed()
    {
        return time_red;
    }
    
    public void setTimeRed(double t)
    {
        time_red = t;
    }
    
    public double getTimeYellow()
    {
        return time_yellow;
    }
    
    public void setTimeYellow(double t)
    {
        time_yellow = t;
    }
    
    public int getNode()
    {
        return node;
    }
    
    public void setNode(int n)
    {
        node = n;
    }
    
    public void setNode(Node n)
    {
        node = n.getId();
    }
    
    public int getSequence()
    {
        return sequence;
    }
    
    public void setSequence(int seq)
    {
        this.sequence = seq;
    }
    
    
    public int compareTo(PhaseRecord rhs)
    {
        if(rhs.node != node)
        {
            return node - rhs.node;
        }
        else
        {
            return sequence - rhs.sequence;
        }
    }
}
