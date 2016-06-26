/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.intersection.candidate;

import avdta.network.link.Link;
import avdta.network.Simulator;
import avdta.network.node.ConflictRegion;
import avdta.vehicle.Vehicle;
import avdta.network.node.TBR;
import avdta.network.node.TBR;
import avdta.network.node.VehIndex;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author ut
 * 
 * 
 * WARNING
 * only use this for single intersection, pulses, VOT-objective
 */
public class CandidatesTBR extends TBR
{
    private File candidatesFile;
    
    private List<CandidateSolution> candidates;
    private Map<Link, List<Vehicle>> sendingFlows;
    
    private CandidateSolution pulseSol;
    
    public CandidatesTBR(File candidatesFile)
    {
        this.candidatesFile = candidatesFile;
        candidates = new ArrayList<CandidateSolution>();
    }
    
    public void initialize()
    {
        super.initialize();
        
        if(candidates.size() > 0)
        {
            return;
        }
        
        candidates.clear();
        try
        {
            Scanner filein = new Scanner(candidatesFile);
            
            Map<Integer, Link> incoming = new HashMap<Integer, Link>();
            
            for(Link l : getNode().getIncoming())
            {
                incoming.put(l.getId(), l);
            }
            
            while(filein.hasNextLine())
            {
                Scanner chopper = new Scanner(filein.nextLine());
                
                List<VehIndex> list = new ArrayList<VehIndex>();
                
                while(chopper.hasNext())
                {
                    String next = chopper.next();
                    int linkid = Integer.parseInt(next.substring(0, next.indexOf('-')));
                    int vehorder = Integer.parseInt(next.substring(next.indexOf('-')+1));
                    
                    list.add(new VehIndex(incoming.get(linkid), vehorder));
                }
                
                candidates.add(new CandidateSolution(list));
            }
        }
        catch(IOException ex)
        {
            ex.printStackTrace(System.err);
        }
        
        System.out.println(candidates.size()+" candidate solutions");
    }
    
    public double evaluateCandidate(CandidateSolution sol)
    {
        double output = 0.0;
        
        int time = 0;
        
        do
        {
            
            for(ConflictRegion cr : allConflicts)
            {
                cr.tempR = cr.R;
            }

            for(Link l : getNode().getOutgoing())
            {
                l.tempR = l.R;
            }
            
            
            Iterator<VehIndex> iterator = sol.iterator();
            

            
            while(iterator.hasNext())
            {
                VehIndex index = iterator.next();
                
                List<Vehicle> sending = sendingFlows.get(index.link);
                
                if(sending.size() <= index.order)
                {
                    continue;
                }
                Vehicle v = sending.get(index.order);
                
                Link i = v.getPrevLink();
                Link j = v.getNextLink();
                double flow = v.getDriver().getEquivFlow(i.getFFSpeed());
                
                // not checking FIFO...
                if(tempHasCapacity(i, j, flow))
                {
                    iterator.remove();
                    
                    output += time * v.getVOT();
                    
                    j.tempR --;
                    for(ConflictRegion cr : conflicts.get(i).get(j))
                    {
                        cr.tempR -= flow*cr.adjustFlow(i, j);
                    }
                }
            }
            

            time += Simulator.dt;
            
        }
        while(time < 5 * Simulator.dt && sol.getNumServed() < sol.size());
        
        // penalize solutions that don't move all vehicles
        output += (sol.size() - sol.getNumServed() ) * time * 1000;
        
        return output;
    }
    
    public boolean tempHasCapacity(Link i, Link j, double flow)
    {
        boolean output = j.tempR >= flow;

        for(ConflictRegion cr : conflicts.get(i).get(j))
        {
            if(cr.tempR < flow*cr.adjustFlow(i, j))
            {
                return false;

            }

        }
        
        return true;
    }
    
    public int step()
    {
        for(Link l : getNode().getOutgoing())
        {
            l.R = l.getReceivingFlow();
        }
        
        for(ConflictRegion cr : allConflicts)
        {
            cr.newTimestep();
        }
        
        sendingFlows = new HashMap<Link, List<Vehicle>>();
        
        
        
        
        
        if(pulseSol == null && hasSendingFlow())
        {
            for(Link l : getNode().getIncoming())
            {
                sendingFlows.put(l, l.getSendingFlow());

                l.q = 0;
                l.Q = Math.ceil(l.getCapacityPerTimestep());
                l.lanes_blocked = 0;

            }
            
            CandidateSolution best = null;
            double bestVal = Integer.MAX_VALUE;
            
            for(CandidateSolution sol : candidates)
            {
                double temp = evaluateCandidate(sol);
                
                if(temp < bestVal)
                {
                    bestVal = temp;
                    best = sol;
                }
            }
            
            pulseSol = best;
            pulseSol.clear();
        }
        else
        {
            for(Link l : getNode().getIncoming())
            {
                l.q = 0;
                l.Q = Math.ceil(l.getCapacityPerTimestep());
                l.lanes_blocked = 0;

            }
        }
         
        if(moveVehicles() == 0)
        {
            pulseSol = null;
        }
        

        
        return 0;
    }
    
    public boolean hasSendingFlow()
    {
        for(Link l : sendingFlows.keySet())
        {
            if(sendingFlows.get(l).size() > 0)
            {
                return true;
            }
        }
        
        return false;
    }
    
    public int moveVehicles()
    {
        if(pulseSol == null)
        {
            return 0;
        }
        
        Iterator<VehIndex> iterator = pulseSol.iterator();
        
        int moved = 0;
        
        while(iterator.hasNext())
        {
            VehIndex index = iterator.next();
            
            List<Vehicle> sending = sendingFlows.get(index.link);
                
            if(sending.size() <= index.order)
            {
                continue;
            }
            Vehicle v = sending.get(index.order);
            
            Link i = v.getPrevLink();
            Link j = v.getNextLink();
            
            double equiv_flow = v.getDriver().getEquivFlow(i.getFFSpeed());
            
            if(hasAvailableCapacity(i, j, equiv_flow))
            {
                j.R -= equiv_flow;
                i.q += equiv_flow;
                
                for(ConflictRegion cr : conflicts.get(i).get(j))
                {
                    cr.update(i, j, equiv_flow);
                }
                        
                i.removeVehicle(v);
                j.addVehicle(v);
                
                moved++;
            }
        }

        
        int waiting = 0;
        
        for(Link l : sendingFlows.keySet())
        {
            waiting += sendingFlows.get(l).size();
        }
        System.out.println("Moved "+moved+" / "+waiting);
        
        return 0;
    }
}
