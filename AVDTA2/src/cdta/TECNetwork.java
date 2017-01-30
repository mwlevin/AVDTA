/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cdta;

import cdta.cell.Cell;
import avdta.network.Simulator;
import avdta.network.link.CentroidConnector;
import avdta.network.link.Link;
import avdta.network.node.ConflictRegion;
import avdta.network.node.Intersection;
import avdta.network.node.PriorityTBR;
import avdta.project.CDTAProject;
import avdta.vehicle.PersonalVehicle;
import avdta.vehicle.Vehicle;
import cdta.cell.Connector;
import cdta.cell.IntersectionConnector;
import cdta.cell.SameCellConnector;
import cdta.cell.SinkCell;
import cdta.cell.SourceCell;
import cdta.cell.StartCell;
import cdta.cell.TECConflictRegion;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 *
 * @author micha
 */
public class TECNetwork 
{
    private Map<Integer, Set<TECConnector>> zones;
    private Set<TECLink> links;
    private int T;
    
    private List<Vehicle> vehicles;
    
    private boolean calcFFtime;
    
    private CDTAProject project;
    
    /**
     * Note that links that are not CTMLinks or CentroidConnectors will be ignored.
     * @param sim the {@link Simulator}
     */
    public TECNetwork(CDTAProject project)
    {
        this.project = project;
        
        Simulator sim = project.getSimulator();
        
        // construct TECLinks
        links = new HashSet<TECLink>();
        zones = new HashMap<Integer, Set<TECConnector>>();
        
        for(Link l : sim.getLinks())
        {
            if(l instanceof CentroidConnector)
            {
                TECConnector link = new TECConnector((CentroidConnector)l);
                links.add(link);
                
                if(link.isOrigin())
                {
                    Set<TECConnector> temp;
                    
                    if(zones.containsKey(link.getZoneId()))
                    {
                        temp = zones.get(link.getZoneId());
                    }
                    else
                    {
                        zones.put(link.getZoneId(), temp = new HashSet<TECConnector>());
                    }
                    temp.add(link);
                }
            }
            else
            {
                links.add(new TECLink(l));
            }
        }
        
        
        //T = Simulator.duration / Simulator.dt;
        T = 3600*4/Simulator.dt;


        for(TECLink link : links)
        {
            link.createCells(T);
        }
        
        Map<Integer, Link> linksmap = sim.createLinkIdsMap();
        
        Map<Integer, TECLink> tecmap = new HashMap<Integer, TECLink>();
        
        for(TECLink link : links)
        {
            tecmap.put(link.getId(), link);
        }
        
        for(TECLink tec: links)
        {
            Link link = linksmap.get(tec.getId());
            
            if(link.getDest() instanceof Intersection)
            {
                PriorityTBR tbr = new PriorityTBR((Intersection)link.getDest());
                tbr.initialize();
                
                for(int t = 0; t < T-1; t++)
                {
                    IntersectionConnector inter = new IntersectionConnector(tbr);
                    
                    // construct this outside to avoid storing a list inside
                    List<TECConflictRegion> conflicts = new ArrayList<TECConflictRegion>();
        
                    for(ConflictRegion cr : tbr.getConflicts())
                    {
                        conflicts.add(new TECConflictRegion(cr.getId()));
                    }
                    
                    
                    tec.getLastCell(t).setNextCellConnector(inter);
                
                    for(Link o : link.getDest().getOutgoing())
                    {
                        TECLink tec2 = tecmap.get(o.getId());
                        
                        tec2.getFirstCell(t+1).setPrevCellConnector(inter);

                        inter.addConnection(tec, tec2, link, o, t, tbr, conflicts); 
                    }
                }
            }
        }
        
        vehicles = sim.getVehicles();
    }
    
    public int getNumCells()
    {
        int output = 0;
        
        for(TECLink link : links)
        {
            output += link.getNumCells();
        }
        
        return output;
    }
    
    public void setCalcFFtime(boolean c)
    {
        calcFFtime = c;
    }
    
    public int getT()
    {
        return T;
    }
    
    public boolean reserveAll() throws IOException
    {
        long time = System.nanoTime();
        
        Collections.sort(vehicles, new Comparator<Vehicle>()
        {
            public int compare(Vehicle v1, Vehicle v2)
            {
                return (int)Math.ceil(10000*(v2.getVOT() - v1.getVOT()));
            }
        });
        
        initializeConnectivity();
        
        PrintStream fileout = new PrintStream(new FileOutputStream(new File(project.getResultsFolder()+"/cdta_vehicles.txt")), true);
        
        fileout.println("id\torigin\tdest\tdep_time\tvot\ttt\tfftime");
        
        int count = 0;
        
        for(Vehicle v : vehicles)
        {
            PersonalVehicle veh = (PersonalVehicle)v;
            Trajectory traj = shortestPath(veh.getOrigin().getId(), veh.getDest().getId(), veh.getDepTime());
            reserve(traj);
            
            int tt = traj.getExitTime() - veh.getDepTime();
            
            int fftime;
            
            if(calcFFtime)
            {
                fftime = calcFFTime(veh.getOrigin().getId(), -veh.getDest().getId(), veh.getDepTime());
            }
            else
            {
                fftime = 0;
            }
            
            fileout.println(veh.getId()+"\t"+veh.getOrigin()+"\t"+(-veh.getDest().getId())+"\t"+veh.getDepTime()+"\t"+veh.getVOT()+"\t"+tt+"\t"+fftime);
            
            count++;
            
            if(count%100 == 0)
            {
                System.out.println(count);
            }
        }
        
        time = System.nanoTime() - time;
        
        fileout.println("Time:\t"+(time/1.0e9)+"\ts");
        
        boolean output = validate();
        fileout.println("Validate:\t"+output);
        
        fileout.close();
        
        
        
        return output;
    }
    
    public int calcFFTime(int origin, int dest, int dtime)
    {
        if(dest > 0)
        {
            dest = -dest;
        }
        
        for(TECLink l : links)
        {
            for(int c = 0; c < l.getNumCells(); c++)
            {
                for(int t = 0; t < T; t++)
                {
                    Cell cell = l.getCell(c, t);
                    
                    cell.label = false;
                    cell.prev = null;
                    cell.added = false;
                }
            }
        }
        
        PriorityQueue<Cell> Q = new PriorityQueue<Cell>();
        
        for(TECConnector link : zones.get(origin))
        {
            Q.add(link.getCell(dtime/Simulator.dt));
        }

        while(!Q.isEmpty())
        {

            
            Cell u = Q.remove();
            
            if(u.getZoneId() == dest)
            {
                return u.getTime()* Simulator.dt - dtime;
            }

            if(u.getNextCellConnector() != null)
            {
                for(Cell v : u.getNextCellConnector().getOutgoing(u))
                {
                    if(!v.added)
                    {
                        v.added = true;
                        Q.add(v);
                        v.label = true;
                        v.prev = u;
                    }
                }
            }
            
            if(u.getSameCellConnector() != null)
            {
                for(Cell v : u.getSameCellConnector().getOutgoing(u))
                {
                    if(!v.added)
                    {
                        v.added = true;
                        Q.add(v);
                        v.label = true;
                        v.prev = u;
                    }
                }
            }
            
        }
        
        return Integer.MAX_VALUE;
    }
    
    public boolean validate()
    {
        for(TECLink link : links)
        {
            for(int c = 0; c < link.getNumCells(); c++)
            {
                for(int t = 0; t < T; t++)
                {
                    Cell cell = link.getCell(c, t);
                    
                    if(cell.getN() > cell.getJamD())
                    {
                        System.err.println("n > jam density");
                        return false;
                    }
                    
                }
                
                for(int t = 0; t < T-1; t++)
                {
                    Cell cell = link.getCell(c, t);
                    
                    double y_tot = 0;
                    
                    if(!(cell instanceof SinkCell))
                    {
                        y_tot = cell.getNextCellConnector().sumYOut(cell);
                        
                        if(!cell.getNextCellConnector().validate())
                        {
                            System.err.println("Connector issue");
                            return false;
                        }
                    }
                    
                    if(!cell.getSameCellConnector().validate())
                    {
                        System.err.println("Connector issue");
                        return false;
                    }
                    
                    
                    
  
                    
                    if(y_tot > cell.getSendingFlow())
                    {
                        System.err.println("y > sending flow");
                        System.err.println("y: "+y_tot);
                        System.err.println("S: "+cell.getSendingFlow());
                        System.err.println("Q: "+cell.getCapacity());
                        System.err.println("n: "+cell.getN());
                        System.err.println("Cell: "+c+" "+t);
                        cell.getNextCellConnector().printConnectivity(cell);
                        
                        return false;
                    }
                }
            }
        }
        
        
        
        return true;
    }
    
    public void initializeConnectivity()
    {
        for(TECLink l : links)
        {
            for(int c = 0; c < l.getNumCells(); c++)
            {
                for(int t = 0; t < T-1; t++)
                {
                    Cell cell = l.getCell(c, t);
                    cell.setN(0);
                    
                    cell.getSameCellConnector().initConnectivity();
                    
                    if(!(cell instanceof SinkCell))
                    {
                        cell.getNextCellConnector().initConnectivity();
                    }
                    
                    
                    
                    if(c == l.getNumCells()-1)
                    {
                        cell.getSameCellConnector().setCongestionConnectivity(cell, cell, true);
                    }
                    
                }
            }
        }
    }
    
    public void reserve(Trajectory path)
    {
        // update n and y
        for(int x = 0; x < path.size()-1; x++)
        {
            Cell i = path.get(x);
            Cell j = path.get(x+1);
            
            i.addN();
            
            if(i.getId() != j.getId())
            {
                i.getNextCellConnector().addY(i, j);
            }
        }
        path.get(path.size()-1).addN();
        
        // update connectivity

        for(int x = 0; x < path.size()-1; x++)
        {
            Cell j = path.get(x);
            Cell k = path.get(x+1);
            
            int t = j.getTime();
            
            if(j.getN() +1 > j.getCapacity())
            {
                if(t > 0)
                {
                    j.getLink().getCell(j.getId(), t-1).getSameCellConnector().setReservationConnectivity(false);
                }
                j.getSameCellConnector().setCongestionConnectivity(true);
                
                for(Cell kp : j.getNextCellConnector().getOutgoing(j))
                {
                    j.getNextCellConnector().setCongestionConnectivity(j, kp, false);
                }
                
            }
            
            // receiving flow check
            
            if(!(j instanceof SourceCell))
            {
                Connector connect = j.getPrevCellConnector();

                if(connect.sumYIn(j) > j.getReceivingFlow())
                {
                    for(Cell i : connect.getIncoming(j))
                    {
                        connect.setCongestionConnectivity(i, j, false);
                        connect.setCongestionConnectivity(i, i, true);
                    }
                }
            }
            
            if(k.getJamD() - 1.0/k.getMesoDelta() * k.getPrevCellConnector().sumYIn(k) < k.getN()-1)
            {
                k.getLink().getCell(k.getId(), k.getTime()-1).getSameCellConnector().setReservationConnectivity(false);
            }
            
            // add conflict region check
            if(j.getLink() != k.getLink())
            {
                IntersectionConnector inter = (IntersectionConnector)j.getNextCellConnector();
                
                inter.checkConnectivity(j, k);
            }
        }

        
    }
    
    public Trajectory shortestPath(int origin, int dest, int dtime)
    {
        if(dest > 0)
        {
            dest = -dest;
        }
        Cell end = dijkstras(origin, dest, dtime);
        //System.out.println(origin+" "+dest+" "+dtime+" "+end);
        return trace(origin, end, dtime);
    }
    
    public Cell dijkstras(int origin, int dest, int dtime)
    {
        for(TECLink l : links)
        {
            for(int c = 0; c < l.getNumCells(); c++)
            {
                for(int t = 0; t < T; t++)
                {
                    Cell cell = l.getCell(c, t);
                    
                    cell.label = false;
                    cell.prev = null;
                    cell.added = false;
                }
            }
        }
        
        PriorityQueue<Cell> Q = new PriorityQueue<Cell>();
        
        for(TECConnector link : zones.get(origin))
        {
            Q.add(link.getCell(dtime/Simulator.dt));
        }

        while(!Q.isEmpty())
        {

            
            Cell u = Q.remove();
            
            if(u.getZoneId() == dest)
            {
                return u;
            }

            if(u.getNextCellConnector() != null)
            {
                for(Cell v : u.getNextCellConnector().getOutgoing(u))
                {
                    if(!v.added && u.getNextCellConnector().isConnected(u, v))
                    {
                        v.added = true;
                        Q.add(v);
                        v.label = true;
                        v.prev = u;
                    }
                }
            }
            
            if(u.getSameCellConnector() != null)
            {
                for(Cell v : u.getSameCellConnector().getOutgoing(u))
                {
                    if(!v.added && u.getSameCellConnector().isConnected(u, v))
                    {
                        v.added = true;
                        Q.add(v);
                        v.label = true;
                        v.prev = u;
                    }
                }
            }
        }
        
        return null;
    }
    
    public Trajectory trace(int origin, Cell dest, int dtime)
    {
        
        
        int start_time = dtime/Simulator.dt;
        ArrayList<Cell> path = new ArrayList<Cell>();
        
        Cell curr = dest;

        while(curr.getZoneId() != origin)
        {
            path.add(curr);
            curr = curr.prev;
        }
        
        path.add(curr);
        
        Trajectory output = new Trajectory();
        
        for(int i = path.size()-1; i >= 0; i--)
        {
            output.add(path.get(i));
        }
        
        return output;
    }
    
}
