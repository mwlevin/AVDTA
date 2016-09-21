/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.network.ReadNetwork;
import avdta.network.Simulator;
import avdta.network.link.Link;
import avdta.network.node.TBR;
import avdta.network.node.obj.ObjFunction;
import avdta.vehicle.Vehicle;
import java.util.List;
import java.util.Map;
import ilog.concert.*;
import ilog.cplex.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * This class solves the conflict region integer program by calling CPLEX. This will find an optimal solution to the integer program, but may result in high computation times when used for networks with multiple intersections.
 * 
 * @author Michael
 */
public class IPTBR extends TBR
{
    
    private ObjFunction objfunc;
    
    private static IloCplex cplex;
    
    
    private PrintStream fileout;
    
    
    /**
     * Constructs the {@link IPTBR} with the given {@link Intersection} and {@link ObjFunction}
     * @param n the {@link Intersection} this IPTBR controls
     * @param obj the objective function for optimization
     */
    public IPTBR(Intersection n, ObjFunction obj)
    {
        super(n);
        
        
        
        this.objfunc = obj;
        
        try
        {
            if(cplex == null)
            {
                cplex = new IloCplex();
                cplex.setOut(null);
            }
        }
        catch(IloException ex)
        {
            ex.printStackTrace(System.err);
            System.exit(1);
        }
        
    }
    
    /**
     * Returns the type code of {@link IPTBR}
     * @return an int specifying the type code of {@link IPTBR}
     */
    public int getType()
    {
        return objfunc.getType() + ReadNetwork.IP;
    }
    
    /**
     * Returns the {@link Signalized} associated with this {@link IPTBR}, if one exists, to add signal data
     * @return the {@link ObjFunction}, if it is a Signalized, otherwise null
     * @see Signalized
     */
    public Signalized getSignal()
    {
        return (objfunc instanceof Signalized)? (Signalized)objfunc : null;
    }
    
    private static final double M = 1.0e6;

    /**
     * Constructs a CPLEX program and solves it, then moves the vehicles specified by the solution
     * 
     * @return the number of exiting vehicles
     */
    public int step()
    {
        Node node = getNode();
        
        int exited = 0;
        
        // if 0 vehicles break
        int num_vehicles = 0;
        
        for(Link l : node.getIncoming())
        {
            num_vehicles += l.getNumSendingFlow();
        }
        
        if(num_vehicles == 0)
        {
            return exited;
        }
        
        
        fileout.println("Timestep\t" + Simulator.time + "\t");
        
        for(Link i : node.getIncoming())
        {
            for(Vehicle v : i.getSendingFlow())
            {
//                fileout.println(v.getId()+"\t"+i+"\t"+v.getNextLink().getId()+"\t"+v.getVOT());
                fileout.println("Car\t" + v.getId()+"\t"+i + "\t");
            }
        }
        
        
        
        List<Vehicle> vehicles = new ArrayList<Vehicle>();
        
        try
        {
            cplex.clearModel();
            
            
            for(ConflictRegion cp : allConflicts)
            {
                cp.reset();
                
                cp.capacityUseSum = null;
            }

            Map<Link, IloLinearNumExpr> receivingCheck = new HashMap<Link, IloLinearNumExpr>();
                    
            // update receiving flows
            for(Link l : node.getOutgoing())
            {
                //l.R = (int)Math.floor(l.getReceivingFlow());

                // account for fractions of receiving flow.
                
                l.R = l.getReceivingFlow();

                
                
            }    
            
            int t_max = Simulator.time + Simulator.dt;
            

            IloLinearNumExpr obj = cplex.linearNumExpr();
            
            // loop through all waiting vehicles
            for(Link l : node.getIncoming())
            {

                int numLanes = l.getNumLanes();
                
                IloIntExpr vehiclesBefore = null;
                int numVehiclesBefore = 0;
                
                // sending flow is sorted by FIFO
                for(Vehicle v : l.getSendingFlow())
                {
                    Link i = v.getPrevLink();
                    Link j = v.getNextLink();
                
                    if(j == null)
                    {
                        num_vehicles --;
                        i.removeVehicle(v);
                        v.exited();
                        exited++;
                    }
                    
                    if(v.reservation_time < 0)
                    {
                        v.reservation_time = Simulator.time;
                    }
                    v.x = cplex.intVar(0, 1);
                    
                    obj.addTerm(objfunc.value(v, this), v.x);
                    
                    
                    vehicles.add(v);
                    
                    // FIFO constraint
                    if(vehiclesBefore != null)
                    {
                        cplex.addLe(v.x, cplex.sum(1, 
                               cplex.prod(cplex.sum(numLanes - 1 - numVehiclesBefore, vehiclesBefore), 1.0/M)));
                    }
                    
                    // receiving flow sum
                    
                    double equiv_flow = v.getDriver().getEquivFlow(i.getFFSpeed());
                    
                    if(receivingCheck.get(j) == null)
                    {
                        // initialize receiving flow check
                        IloLinearNumExpr temp = cplex.linearNumExpr();
                        temp.addTerm(equiv_flow, v.x);
                        receivingCheck.put(j, temp);
                    }
                    else
                    {
                        receivingCheck.get(j).addTerm(equiv_flow, v.x);
                    }

                    // conflict region capacity sum
                    for(ConflictRegion cp : conflicts.get(i).get(j))
                    {
                        if(cp.capacityUseSum == null)
                        {
                            cp.capacityUseSum = cplex.linearNumExpr();
                        }
                        
                        cp.capacityUseSum.addTerm(cp.getCapacity() / i.getCapacity() * equiv_flow, v.x);
                    }
                    
                    // update vehiclesBefore
                    if(vehiclesBefore == null)
                    {
                        vehiclesBefore = v.x;
                    }
                    else
                    {
                        vehiclesBefore = cplex.sum(vehiclesBefore, v.x);
                    }
                    
                    numVehiclesBefore++;
                }
            }
            
            if(num_vehicles == 0)
            {
                return exited;
            }
            
            // receiving flow constraints
            for(Link l : receivingCheck.keySet())
            {
                if(receivingCheck.get(l) != null)
                {
                    cplex.addLe(receivingCheck.get(l), l.R);
                }
            }
            
            // conflict region capacity constraints
            for(ConflictRegion cp : allConflicts)
            {
                if(cp.capacityUseSum != null)
                {
                    cplex.addLe(cp.capacityUseSum, cp.getRemainingTimestepFlow());
                }
            }
            
            if(objfunc.isMinimize())
            {
                cplex.addMinimize(obj);
            }
            else
            {
                cplex.addMaximize(obj);
            }

            cplex.solve();
            
            
            
            
            // move vehicles
            int count = 0;

            fileout.print("Ordering\t");

            for(Vehicle v : vehicles)
            {
                
                if(cplex.getValue(v.x) == 1)
                {
                    fileout.print(v.getId()+"\t");
                    Link i = v.getPrevLink();
                    Link j = v.getNextLink();
                    
                
                    count++;

                    // move vehicle

                    i.removeVehicle(v);
                    
                    double equiv_flow = v.getDriver().getEquivFlow(i.getFFSpeed());
                    
                    for(ConflictRegion cp : conflicts.get(i).get(j))
                    {
                        cp.update(i, j, equiv_flow);
                    }

                    // vehicles that exit at the node are removed earlier
                    
                    if(j != null)
                    {
                        j.addVehicle(v);
                    }
                    
                }
            }
            fileout.println();
            fileout.println("-");
            
            for(Vehicle v : vehicles)
            {
                if(cplex.getValue(v.x) == 0)
                {
                    // update conflict regions - whether they are capacity blocked
                    
                    // vehicles that exit at te node are removed earlier
                    // if(j != null)
                    //{
                    
                    Link i = v.getPrevLink();
                    Link j = v.getNextLink();
                    
                    double equiv_flow = v.getDriver().getEquivFlow(i.getFFSpeed());
                    
                    for(ConflictRegion cp : conflicts.get(i).get(j))
                    {
                        cp.canMove(i, j, equiv_flow);
                    }
                    
                    //}
                }
            }
            
            
            return exited;
        }
        catch(IloException ex)
        {
            ex.printStackTrace(System.err);
            System.err.println("Vehicles: "+vehicles.size());
            
            for(Link l : node.getOutgoing())
            {
                System.out.println(l.getId()+"\t"+l.R);
            }
            
            for(ConflictRegion cp : allConflicts)
            {
                System.out.println(cp.getRemainingTimestepFlow());
            }
            
            
            System.exit(1);
            return exited;
        }
    }
    
}
