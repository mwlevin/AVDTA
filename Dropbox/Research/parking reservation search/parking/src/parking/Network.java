/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parking;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

/**
 *
 * @author micha
 */
public class Network 
{
    private Set<Node> nodes;
    private Set<Zone> zones; // zones are locations with parking
    
    private List<State> states;
    
    private String name;
    private Map<Node, Map<Integer, Map<Zone, State>>> statemap;
    
    public static final double vot = 6.4;
    public static final int time_parked = 3;
    
    public Network(String name)
    {
        this.name = name;
        
        
    }
    
    public Set<Zone> getZones()
    {
        return zones;
    }
    
    public void setParkingProb(double p)
    {
        for(Zone z : zones)
        {
            z.setPrPark(p);
        }
    }
    
    public int getNumStates()
    {
        return states.size();
    }
    
    public int getNumActions()
    {
        int output = 0;
        
        for(State x : states)
        {
            output += x.getU().size();
        }
        
        return output;
    }
    
    public void printOutput() throws IOException
    {
        File file = new File("results/"+name+"/");
        file.mkdirs();
        PrintStream fileout = new PrintStream(new FileOutputStream(new File("results/"+name+"/output.txt")), true);
        
        fileout.println("n\ttau\tr\tJ\ts\trho");
        for(State x : states)
        {
            try
            {
                x.mu_star.getNext();
                fileout.println(x.getNext()+"\t"+x.getTimeRem()+"\t"+x.getReserved()+"\t"+x.J+"\t"+x.mu_star.getNext()+"\t"+x.mu_star.getReserved());
            }
            catch(NullPointerException ex)
            {
                //System.out.println(x+" "+x.J+" "+x.mu_star);
            }
        }
        
        fileout.close();
    }
    
    public void readNetwork() throws IOException
    {
        readNetwork(new File("data/"+name+"/nodes.txt"), new File("data/"+name+"/links.txt"));
    }
    
    public void readNetwork(File nodesFile, File linksFile) throws IOException
    {
        Scanner filein = new Scanner(nodesFile);
        
        while(!filein.hasNextInt())
        {
            filein.nextLine();
        }
        
        Map<Integer, Node> nodesmap = new HashMap<Integer, Node>();
        
        nodes = new HashSet<Node>();
        zones = new HashSet<Zone>();
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            boolean parking = filein.nextInt() == 1;
            
            if(parking)
            {
                double park_cost = filein.nextDouble();
                double hold_cost = filein.nextDouble();
                double walk_time = filein.nextDouble();
                double prob = filein.nextDouble();
                
                Zone zone = new Zone(id, prob, hold_cost, park_cost, walk_time/3600.0);
                zones.add(zone);
                nodes.add(zone);
                nodesmap.put(id, zone);
            }
            else
            {
                Node node = new Node(id);
                nodes.add(node);
                nodesmap.put(id, node);
            }
            
            filein.nextLine();
        }
        
        filein.close();
        
        filein = new Scanner(linksFile);
        
        while(!filein.hasNextInt())
        {
            filein.nextLine();
        }
        
        while(filein.hasNextInt())
        {
            int source = filein.nextInt();
            int dest = filein.nextInt();
            double tt = filein.nextDouble();
            
            new Link(nodesmap.get(source), nodesmap.get(dest), tt);
        }
        
        filein.close();
        
        nodesmap = null;
    }
    
    public Node findNode(int id)
    {
        for(Node n : nodes)
        {
            if(n.getId() == id)
            {
                return n;
            }
        }
        
        return null;
    }
    
    public void dijkstras(Node source)
    {
        Set<Node> unsettled = new HashSet<Node>();
        
        
        for(Node n : nodes)
        {
            n.label = Integer.MAX_VALUE;
            n.prev = null;
        }
        
        source.label = 0;
        
        unsettled.add(source);
        
        while(!unsettled.isEmpty())
        {
           
            Node min = null;
            int minCost = Integer.MAX_VALUE;
            
            for(Node n : unsettled)
            {
                if(n.label < minCost)
                {
                    minCost = n.label;
                    min = n;
                }
            }
            
            unsettled.remove(min);
            
            for(Link l : min.getIncoming())
            {
                int temp = minCost + l.getTT();
                if(temp < l.getSource().label)
                {
                    l.getSource().label = temp;
                    l.getSource().prev = l;
                    unsettled.add(l.getSource());
                }
            }
        }
    }
    
    public int getDistance(State x)
    {
        return x.getNext().label + x.getTimeRem();
    }
    
    public void populateStates()
    {
        statemap = new HashMap<Node, Map<Integer, Map<Zone, State>>>();
        
        zones.add(Zone.NULL);
        
        states = new ArrayList<State>();
        states.add(State.PARK);
        
        for(Node n : nodes)
        {
            for(Zone z : zones)
            {
                int max_tau = 0;

                for(Link l : n.getIncoming())
                {
                    max_tau = (int)Math.max(max_tau, l.getTT());
                }
                
                for(int t = 0; t <= max_tau-1; t++)
                {
                    State x = new State(n, t, z);
                    states.add(x);
                    
                    if(z == Zone.NULL)
                    {
                        for(Link l : n.getIncoming())
                        {
                            if(l.getSource().isOrigin() && t == l.getTT()-1)
                            {
                                x.setOrigin(true);
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        for(State x : states)
        {
            Map<Integer, Map<Zone, State>> temp;
            
            if(statemap.containsKey(x.getNext()))
            {
                temp = statemap.get(x.getNext());
            }
            else
            {
                statemap.put(x.getNext(), temp = new HashMap<Integer, Map<Zone, State>>());
            }
            
            Map<Zone, State> temp2;
            
            if(temp.containsKey(x.getTimeRem()))
            {
                temp2 = temp.get(x.getTimeRem());
            }
            else
            {
                temp.put(x.getTimeRem(), temp2 = new HashMap<Zone, State>());
            }
            
            temp2.put(x.getReserved(), x);
        }
        
        for(State x : states)
        {
            List<Action> U = createU(x);
            
            for(Action u : U)
            {
                x.add(u, transition(x, u));
            }
        }
        
        
        statemap = null;
    }
    
    public double getAvgOriginJ()
    {
        double total = 0.0;
        int num = 0;
        for(State x : states)
        {
            if(x.isOrigin() && x.J < 1.0e6)
            {
                total += x.J;
                num++;
            }
        }
        
        return total / num;
    }
    
    public List<Action> createU(State x)
    {
        
        List<Action> output = new ArrayList<Action>();
        
        if(x == State.PARK)
        {
            output.add(Action.PARK);
            return output;
        }
        
        
        if(x.getNext() == x.getReserved() && x.getTimeRem() == 0)
        {
            output.add(Action.PARK);
        }
        

        if(x.getTimeRem() == 0)
        {
            for(Link l : x.getNext().getOutgoing())
            {
                for(Zone z : zones)
                {
                    output.add(new Action(l, z));
                }
            }
        }
        else
        {
            for(Zone z : zones)
            {
                output.add(new Action(null, z));
            }
        }
        
        return output;
    }
    
    public void setHoldCost(double c)
    {
        for(Zone z : zones)
        {
            z.setHoldCost(c);
        }
    }
    
    public double g(State x, Action u)
    {
        if(x == State.PARK)
        {
            return 0;
        }
        else if(u == Action.PARK)
        {
            return x.getReserved().getParkCost() * (time_parked + x.getReserved().getWalkTime()*2)+ x.getReserved().getWalkTime() *vot;
        }
        else
        {
            return Link.dt/3600.0* vot + x.getReserved().getHoldCost() * Link.dt/3600.0;
        }
    }
    
    public Transition transition(State x, Action u)
    {
        Transition output = new Transition();
        
        if(x == State.PARK || u == Action.PARK)
        {
            output.put(State.PARK, 1.0);
            return output;
        }
        
        Node newNode;
        int newTau;
        
        if(x.getTimeRem() == 0)
        {
            newNode = u.getNext().getDest();
            newTau = u.getNext().getTT()-1;
        }
        else
        {
            newNode = x.getNext();
            newTau = x.getTimeRem() - 1;
        }
        
        if(u.getReserved() == x.getReserved())
        {
            output.put(statemap.get(newNode).get(newTau).get(u.getReserved()),
                    1.0);
        }
        else
        {
            output.put(statemap.get(newNode).get(newTau).get(u.getReserved()),
                    u.getReserved().getPrPark());
            if(u.getReserved().getPrPark() < 1)
            {
                output.put(statemap.get(newNode).get(newTau).get(x.getReserved()),
                        1.0 - u.getReserved().getPrPark());
            }
        }
        
        
        
        return output;
    }
    
    public double getExpectedCost(State x, Action u)
    {
        double output = g(x, u);
        
        Map<State, Double> nextState = x.getU().get(u);
        
        for(State newX : nextState.keySet())
        {
            output += newX.J * nextState.get(newX);
        }
        
        return output;
    }
    
    
    public void valueIteration(double epsilon, int max_iter)
    {
        for(State x : states)
        {
            if(x.getU().size() == 0)
            {
                x.J = Integer.MAX_VALUE;
            }
            else
            {
                x.J = 0.0;
            }
        }
        
        System.out.println("Iteration\tError");
        double max_error = Integer.MAX_VALUE;
        int iteration = 0;
        
        while(max_error > epsilon && iteration < max_iter)
        {
            max_error = 0.0;
            iteration++;

            for(State x : states)
            {
                double newJ = Integer.MAX_VALUE;
                
                Map<Action, Transition> U = x.getU();
                
                for(Action u : U.keySet())
                {
                    double temp = getExpectedCost(x, u);
                    
                    if(temp < newJ)
                    {
                        newJ = temp;
                    }
                }
                
                max_error = Math.max(max_error, Math.abs(newJ - x.J));
                
                x.J = newJ;
            }
            
            System.out.println(iteration+"\t"+String.format("%.4f", max_error));
        }
        
        for(State x : states)
        {
            double newJ = Integer.MAX_VALUE;
            Action best = null;
                
            Map<Action, Transition> U = x.getU();

            for(Action u : U.keySet())
            {
                double temp = getExpectedCost(x, u);

                if(temp < newJ)
                {
                    newJ = temp;
                    best = u;
                }
            }
            
            x.mu_star = best;
        }
    }
    
    public List<State> getStates()
    {
        return states;
    }
}
