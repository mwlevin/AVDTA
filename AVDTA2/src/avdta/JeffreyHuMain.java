/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta;

import avdta.demand.AST;
import avdta.demand.DemandImportFromVISTA;
import avdta.demand.DemandProfile;
import avdta.demand.DynamicODRecord;
import avdta.demand.DynamicODTable;
import avdta.demand.ReadDemandNetwork;
import avdta.demand.StaticODTable;
import avdta.dta.Assignment;
import avdta.dta.DTAImportFromVISTA;
import avdta.network.link.transit.BusLink;
import avdta.dta.DTAResults;
import avdta.dta.DTASimulator;
import avdta.dta.ReadDTANetwork;
import avdta.dta.VehicleRecord;
import avdta.duer.DUERSimulator;
import avdta.duer.Incident;
import avdta.duer.ValueIteration;
import avdta.fourstep.FourStepSimulator;
import avdta.fourstep.ReadFourStepNetwork;
import avdta.gui.DTAGUI;
import avdta.gui.FourStepGUI;
import avdta.gui.GUI;
import avdta.gui.SAVGUI;
import avdta.gui.editor.Editor;
import avdta.gui.editor.visual.rules.LinkBusRule;
import avdta.gui.editor.visual.rules.LinkDataRule;
import avdta.gui.editor.visual.rules.LinkFileRule;
import avdta.gui.editor.visual.rules.NodeFileRule;
import avdta.gui.editor.visual.rules.data.LinkFileSource;
import avdta.gui.editor.visual.rules.data.VolumeLinkData;
import avdta.network.Path;
import avdta.network.PathList;
import avdta.network.ReadNetwork;
import static avdta.network.ReadNetwork.BEV;
import avdta.network.Simulator;
import avdta.network.cost.TravelCost;
import avdta.project.DTAProject;
import avdta.project.Project;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import avdta.network.node.*;
import avdta.network.link.*;
import avdta.network.link.cell.Cell;
import avdta.network.node.policy.IntersectionPolicy;
import avdta.network.node.policy.TransitFirst;
import avdta.project.DUERProject;
import avdta.project.FourStepProject;
import avdta.project.SAVProject;
import avdta.sav.ReadSAVNetwork;
import avdta.sav.SAVMain;
import avdta.sav.SAVOrigin;
import avdta.sav.SAVSimulator;
import avdta.sav.SAVZone;
import avdta.sav.Taxi;
import avdta.sav.SAVTraveler;
import avdta.sav.dispatch.RealTimeDispatch;
import avdta.traveler.Traveler;
import avdta.util.RunningAvg;
import avdta.vehicle.Bus;
import avdta.vehicle.DriverType;
import avdta.vehicle.EmergencyVehicle;
import avdta.vehicle.PersonalVehicle;
import avdta.vehicle.Vehicle;
import avdta.vehicle.fuel.BEV;
import avdta.vehicle.fuel.ICV;
import avdta.vehicle.fuel.VehicleClass;
import avdta.vehicle.route.Hyperpath;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import org.openstreetmap.gui.jmapviewer.Demo;
import cdta.CDTA;
import java.sql.Connection;
import java.sql.DriverManager;


/**
 *
 * @author micha
 */
public class JeffreyHuMain 
{
    
    public Simulator sim = null;
    //maps from Intersection id to a link id showing the incoming link with the highest demand for each intersection
    public HashMap<Integer, Integer> highestDelayLinks = new HashMap<>();
    
    //maps from Intersection id to the average waiting time for that intersection across all links
    public HashMap<Integer, Double> highestDelayAllLinks = new HashMap<>();
    
    //maps from Intersection id to an Integer showing the turn for every intersection with the highest avg red light time
    //First Integer represents intersection Id, second integer represents the average red light time for the worst case turn for that intersection
    public HashMap<Integer, Double> highestRedLightTurns = new HashMap<>();
    
    //maps from Intersection id to an Integer showing the turn for every intersection with the highest avg wait time
    //First Integer represents intersection Id, second integer represents the average wait time for the worst case turn for that intersection
    public HashMap<Integer, Double> highestWaitTurns = new HashMap<>();
    
    public static void main(String[] args)
    {
        
    }
    
    public void simulate(int demand_factor, int iteration_num) throws Exception
    {
        for (int run_time = 1; run_time <= 1; run_time++) {
            //int demand_factor = 18;
            int use_MP = 1;
            int duration = 4*3600;
            int vehicle_per_hour = 2000*demand_factor;

            DTAProject msa_mp = new DTAProject(new File("projects/coacongress2_ttmp"));
            ReadDTANetwork read = new ReadDTANetwork();

            DemandProfile profile = new DemandProfile();
            profile.add(new AST(1, 0, duration, 1.0));
            profile.save(msa_mp);

            StaticODTable staticOd = new StaticODTable(msa_mp);
            read.createDynamicOD(msa_mp, duration / 3600.0 * vehicle_per_hour / staticOd.getTotal());

            //creates vehicles and writes them to demand file (project/demand/demand.txt)
            read.prepareDemand(msa_mp);

            Set<Node> nodes = read.readNodes(msa_mp);
            Set<Link> links = read.readLinks(msa_mp);        
            read.readIntersections(msa_mp);
            read.readPhases(msa_mp);
            
            System.out.println("Before msa_map.getSimulator()");
            //within msa_mp.getSimulator(), vehicles within msa_mp are set as well.
            DTASimulator sim_mp = msa_mp.getSimulator();
            System.out.println("After msa_mp.getSimulator()" + sim_mp.getVehicles().size());
            sim_mp.initialize();
            sim_mp.recordQueueLengths(1800);
            MaxPressure.weight_function = new MPWeight()
            {
                public double calcMPWeight(MPTurn turn)
                {
                    return turn.getQueue();
                }
            };        

            if (use_MP ==1 ){
                sim_mp.MP_msa_cont(1, iteration_num, 0.01);
            }
            else{
                sim_mp.msa(iteration_num);
            }
        }
    }
    
    public void simulateFixedProportions(int demand_factor) throws Exception
    {
        DTAProject project = new DTAProject(new File("projects/coacongress2_ttmp"));
        MaxPressure.weight_function = new MPWeight()
        {
            public double calcMPWeight(MPTurn turn)
            {
                return turn.getQueue();
            }    
        };
        int demand = 1000*demand_factor; // vehicles per hour
        int duration = 3600 *3; // 3 hours * 3600 seconds
        sim = MaxPressureTest.createMPSimulator(project, demand, duration);
        
        sim.recordQueueLengths(1800);
        Simulator.duration = 3600*3;
        sim.simulateMP(demand_factor, -1);       
        sim.printLinkTT(0, duration);
        System.out.println("Average TT: " + sim.getAvgTT(DriverType.HV));
        
        for (Node n : sim.nodes) {
            Intersection i = null;
            if (n instanceof Intersection) {
                i = (Intersection) n;
            } else {
                continue;
            }
            
            double highestAvgLinkTT = 0;
            int highestLinkId = 0;
            double cumulativeLinkTT = 0.0;
            for (Link l : i.getIncoming()) {
                if (l.getCumulativeAvgTT() >= highestAvgLinkTT) {
                    highestAvgLinkTT = l.getCumulativeAvgTT();
                    highestLinkId = l.getId();
                }
                
                cumulativeLinkTT = cumulativeLinkTT + l.getCumulativeAvgTT();
            }
            double avgLinkTT = cumulativeLinkTT/i.getIncoming().size();
            
            double highestAvgRedLight = 0;
            double highestAvgWait = 0;
            if (i.getControl() instanceof MaxPressure) {
                for (Phase p : ((MaxPressure) i.getControl()).getPhases()) {
                    for (Turn t : p.getAllowed()) {
                        if (t.avgRedLightTime.getAverage() > highestAvgRedLight) {
                            highestAvgRedLight = t.avgRedLightTime.getAverage();
                        }
                        if (t.avgWaitingTime.getAverage() > highestAvgWait) {
                            highestAvgWait = t.avgWaitingTime.getAverage();
                        }
                    }
                }
            }
            
            if (highestLinkId != 0) {
                highestDelayLinks.put(i.getId(), highestLinkId);
                highestDelayAllLinks.put(i.getId(), avgLinkTT);
            }
            
            if (highestAvgRedLight != 0) {
                highestRedLightTurns.put(i.getId(), highestAvgRedLight);
            }
            
            if (highestAvgWait != 0) {
                highestWaitTurns.put(i.getId(), highestAvgWait);
            }
        }
    }
    
    public void simulateFixedProportions(int demand_factor, int cycleLength) throws Exception
    {
        DTAProject project = new DTAProject(new File("projects/coacongress2_ttmp"));
        MaxPressure.weight_function = new MPWeight()
        {
            public double calcMPWeight(MPTurn turn)
            {
                return turn.getQueue();
            }    
        };
        int demand = 1000*demand_factor; // vehicles per hour
        int duration = 3600 *3; // 3 hours * 3600 seconds
        sim = MaxPressureTest.createMPSimulator(project, demand, duration, cycleLength);        
        sim.recordQueueLengths(1800);
        Simulator.duration = 3600*3;
        sim.simulateMP(demand_factor, cycleLength);       
        sim.printLinkTT(0, duration);
        System.out.println("Average TT: " + sim.getAvgTT(DriverType.HV));
        
        for (Node n : sim.nodes) {
            Intersection i = null;
            if (n instanceof Intersection) {
                i = (Intersection) n;
            } else {
                continue;
            }
            
            double highestAvgLinkTT = 0;
            int highestLinkId = 0;
            double cumulativeLinkTT = 0.0;
            for (Link l : i.getIncoming()) {
                if (l.getCumulativeAvgTT() >= highestAvgLinkTT) {
                    highestAvgLinkTT = l.getCumulativeAvgTT();
                    highestLinkId = l.getId();
                }
                
                cumulativeLinkTT = cumulativeLinkTT + l.getCumulativeAvgTT();
            }
            double avgLinkTT = cumulativeLinkTT/i.getIncoming().size();
            
            double highestAvgRedLight = 0;
            double highestAvgWait = 0;
            if (i.getControl() instanceof MaxPressure) {
                for (Phase p : ((MaxPressure) i.getControl()).getPhases()) {
                    for (Turn t : p.getAllowed()) {
                        if (t.avgRedLightTime.getAverage() > highestAvgRedLight) {
                            highestAvgRedLight = t.avgRedLightTime.getAverage();
                        }
                        if (t.avgWaitingTime.getAverage() > highestAvgWait) {
                            highestAvgWait = t.avgWaitingTime.getAverage();
                        }
                    }
                }
            } else if (i.getControl() instanceof HaiVuTrafficSignal) {
                for (Phase p : ((HaiVuTrafficSignal) i.getControl()).getPhases()) {
                    for (Turn t : p.getAllowed()) {
                        if (t.avgRedLightTime.getAverage() > highestAvgRedLight) {
                            highestAvgRedLight = t.avgRedLightTime.getAverage();
                        }
                        if (t.avgWaitingTime.getAverage() > highestAvgWait) {
                            highestAvgWait = t.avgWaitingTime.getAverage();
                        }
                    }
                }
            }
            
            if (highestLinkId != 0) {
                highestDelayLinks.put(i.getId(), highestLinkId);
                highestDelayAllLinks.put(i.getId(), avgLinkTT);
            }
            
            if (highestAvgRedLight != 0) {
                highestRedLightTurns.put(i.getId(), highestAvgRedLight);
            }
            
            if (highestAvgWait != 0) {
                highestWaitTurns.put(i.getId(), highestAvgWait);
            }
        }
    }
}
