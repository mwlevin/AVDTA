/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * A record of a {@link Phase} used to represent and manipulate the data used to construct a {@link Phase}.
 * @author Michael
 */
public class PhaseRecord implements java.io.Serializable, Comparable<PhaseRecord>
{
    private int node, sequence;
    private double time_red, time_yellow, time_green;
    private List<TurnRecord> turns;
    private int type;
    
    /**
     * Constructs this {@link PhaseRecord} with all data
     * @param node the {@link Node} this {@link  Phase} is used for
     * @param sequence the sequence this {@link Phase} occurs in the signal cycle
     * @param time_red the all red time
     * @param time_yellow the yellow time
     * @param time_green the green time
     * @param turns list of allowed {@link Turn}s
     */
    public PhaseRecord(Node node, int sequence, double time_red, double time_yellow, double time_green, List<TurnRecord> turns)
    {
        this(node.getId(), sequence, time_red, time_yellow, time_green, turns);
    }
    
    /**
     * Constructs this {@link PhaseRecord} with all data
     * @param node the id of the {@link Node} this {@link  Phase} is used for
     * @param sequence the sequence this {@link Phase} occurs in the signal cycle
     * @param time_red the all red time
     * @param time_yellow the yellow time
     * @param time_green the green time
     * @param turns list of allowed {@link Turn}s
     */
    public PhaseRecord(int node, int sequence, double time_red, double time_yellow, double time_green, List<TurnRecord> turns)
    {
        this.node = node;
        this.sequence = sequence;
        this.time_red = time_red;
        this.time_yellow = time_yellow;
        this.time_green = time_green;
        this.turns = turns;
        this.type = 1;
    }
    
    /**
     * Constructs this {@link PhaseRecord} from the line of input data.
     * @param line the line of input data
     */
    public PhaseRecord(String line)
    {
        Scanner chopper = new Scanner(line);
        node = chopper.nextInt();
        type = chopper.nextInt();
        sequence = chopper.nextInt();
        time_red = chopper.nextDouble();
        time_yellow = chopper.nextDouble();
        time_green = chopper.nextDouble();
        
        line = chopper.nextLine();
        
        turns = new ArrayList<TurnRecord>();
        
        
        String from = line.substring(line.indexOf('{')+1, line.indexOf('}')).trim();
        line = line.substring(line.indexOf('}')+1);
        String to = line.substring(line.indexOf('{')+1, line.indexOf('}')).trim();
        
        if(from.length() == 0 && to.length() == 0)
        {
            return;
        }
        
        String[] from_s = from.split(",");
        String[] to_s = to.split(",");
        
        if(from_s.length != to_s.length)
        {
            throw new RuntimeException("Too many from or to links.");
        }
        
        
        for(int i = 0; i < from_s.length; i++)
        {
            turns.add(new TurnRecord(Integer.parseInt(from_s[i].trim()), Integer.parseInt(to_s[i].trim())));
        }
    }
    
    /**
     * Returns a {@link String} form that can be written into the phases data file
     * @return a {@link String} form that can be written into the phases data file
     */
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
        
        return node+"\t"+type+"\t"+sequence+"\t"+time_red+"\t"+time_yellow+"\t"+time_green+"\t"+turns.size()+"\t{"+inc+"}\t{"+out+"}";
    }
    
    public int getType()
    {
        return type;
    }
    
    public void setType(int type)
    {
        this.type = type;
    }
    
    /**
     * Adds a new {@link Turn} to the list of allowed {@link Turn}s
     * @param t the new {@link Turn}
     */
    public void addTurn(Turn t)
    {
        turns.add(new TurnRecord(t));
    }
    
    /**
     * Adds a new {@link Turn} to the list of allowed {@link Turn}s
     * @param t the new {@link Turn}
     */
    public void addTurn(TurnRecord t)
    {
        turns.add(t);
    }
    
    /**
     * Updates the list of allowed {@link Turn}s
     * @param turns the new list of allowed {@link Turn}s
     */
    public void setTurns(List<TurnRecord> turns)
    {
        this.turns = turns;
    }
    
    /**
     * Returns the list of allowed {@link Turn}s
     * @return the list of allowed {@link Turn}s
     */
    public List<TurnRecord> getTurns()
    {
        return turns;
    }
    
    /**
     * Returns the green time
     * @return the green time
     */
    public double getTimeGreen()
    {
        return time_green;
    }
    
    /**
     * Updates the green time
     * @param t the new green time
     */
    public void setTimeGreen(double t)
    {
        time_green = t;
    }
    
    /**
     * Returns the all red time
     * @return the all red time
     */
    public double getTimeRed()
    {
        return time_red;
    }
    
    /**
     * Updates the all red time
     * @param t the new all red time
     */
    public void setTimeRed(double t)
    {
        time_red = t;
    }
    
    /**
     * Returns the yellow time
     * @return the yellow time
     */
    public double getTimeYellow()
    {
        return time_yellow;
    }
    
    /**
     * Updates the yellow time
     * @param t the new yellow time
     */
    public void setTimeYellow(double t)
    {
        time_yellow = t;
    }
    
    /**
     * Returns the id of the {@link Node} this {@link Phase} is for
     * @return the id of the {@link Node} this {@link Phase} is for
     */
    public int getNode()
    {
        return node;
    }
    
    /**
     * Updates the id of the {@link Node} this {@link Phase} is for
     * @param n the new id of the {@link Node} this {@link Phase} is for
     */
    public void setNode(int n)
    {
        node = n;
    }
    
    /**
     * Updates the {@link Node} this {@link Phase} is for
     * @param n the new {@link Node} this {@link Phase} is for
     */
    public void setNode(Node n)
    {
        node = n.getId();
    }
    
    /**
     * Returns the sequence this {@link Phase} occurs in the signal cycle. {@link Phase}s for the same {@link Node} must have unique sequences.
     * @return the sequence this {@link Phase} occurs in the signal cycle
     */
    public int getSequence()
    {
        return sequence;
    }
    
    /**
     * Updates the sequence this {@link Phase} occurs in the signal cycle. {@link Phase}s for the same {@link Node} must have unique sequences.
     * @param seq the new sequence
     */
    public void setSequence(int seq)
    {
        this.sequence = seq;
    }
    
    /**
     * Orders this {@link PhaseRecord} with respect to other {@link PhaseRecord}s. {@link PhaseRecord}s are sorted according to {@link Node}, then according to {@link PhaseRecord#getSequence()}.
     * @param rhs the {@link PhaseRecord} to compare against
     * @return whether this {@link PhaseRecord} should be before or after {@code rhs}
     */
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
