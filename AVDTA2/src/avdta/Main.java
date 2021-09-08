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
import avdta.project.DemandProject;
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
public class Main 
{
    public static void main(String[] args) throws Exception
    {
        /*
        DTAProject msa = new DTAProject(new File("projects/coacongress2"));
        DTASimulator sim = msa.getSimulator();
        sim.msa(5);
        */
        
        

        
        
        DTAProject project = new DTAProject(new File("projects/ssmall_network_SPaT"));
        DTASimulator sim = project.getSimulator();       

        ReadDemandNetwork read = new ReadDemandNetwork();
        
        int numVeh = read.prepareDemand(project);
        System.out.println("There are " + numVeh + " vehicles in the network.");
        
        System.out.println(sim.getCostFunction());
        sim.msa(50);
        sim.simulate();

        sim.postProcess();
        
        
        String filename = project.getResultsFolder()+"/link_tt.txt";
        sim.printLinkTT(0, sim.getLastExitTime()+sim.ast_duration, new File(filename));
        
        filename = project.getResultsFolder()+"/link_flow.txt";
        sim.printLinkFlow(0, sim.getLastExitTime()+sim.ast_duration, new File(filename));
        System.out.println("Link 12 TT: " + sim.getLink(12).getAvgTT(1));

    
        // this is Varaiya's function
        /*
        MaxPressure.weight_function = new MPWeight()
        {
            public double calcMPWeight(MPTurn turn)
            {
                return turn.getQueue();
            }
        };
        */
        
        
        /*
        // this is the travel time function
        MaxPressure.weight_function = new MPWeight()
        {
            public double calcMPWeight(MPTurn turn)
            {
                return turn.getQueue() / turn.getCapacity();
            }
        };
        */
        
        /**
        int demand = 5000; // vehicles per hour
        int duration = 3600 * 3; // 3 hours * 3600 seconds
        Simulator sim = MaxPressureTest.createMPSimulator(project, demand, duration);
        sim.recordQueueLengths(1800);

        Simulator.duration = 3600*3;
        sim.simulate();
        System.out.println(sim.getAvgTT(DriverType.HV));
        */
        
        //new DTAGUI();
        //new FourStepGUI();
        

        // GUI.main(args);

        
        


    }
    
    
    
    
    
    public static void fixConnectivity2() throws Exception
    {
        DTAProject project = new DTAProject(new File("projects/scenario_2_pm_sub_CACC"));
        DTASimulator sim = project.getSimulator();
        
        DTAProject project2 = new DTAProject(new File("projects/scenario_2_pm"));
        DTASimulator sim2 = project2.getSimulator();
        
        Set<Link> newLinks = new HashSet<Link>();
        
        for(Link l : sim.getLinks())
        {
            if(l.isCentroidConnector())
            {
                continue;
            }
            
            Node r = l.getSource();
            Node s = l.getDest();
            
            boolean found = false;
            
            for(Link v : r.getIncoming())
            {
                if(v.getSource() == s)
                {
                    found = true;
                    break;
                }
            }
            
            if(!found)
            {
                for(Link l2 : sim2.getLinks())
                {
                    if(l2.getSource().getId() == s.getId() && l2.getDest().getId() == r.getId())
                    {
                        newLinks.add(l2);
                    }
                }
            }
        }
        
        PrintStream fileout = new PrintStream(new FileOutputStream(new File("newLinks.txt")), true);
        for(Link l : newLinks)
        {
            fileout.println(l.createLinkRecord());
        }
        fileout.close();
    }
    public static void fixConnectivity() throws Exception
    {
        
        DTAProject project = new DTAProject(new File("projects/scenario_2_pm_sub_CACC"));
        DTASimulator sim = project.getSimulator();
        
        Set<Link> newLinks = new HashSet<Link>();
        
        int new_id = 80000001;
        
        for(Link l : sim.getLinks())
        {
            if(!l.isCentroidConnector())
            {
                continue;
            }
            
            Node r = l.getSource();
            Node s = l.getDest();
            
            boolean found = false;
            
            for(Link v : r.getIncoming())
            {
                if(v.getSource() == s)
                {
                    found = true;
                    break;
                }
            }
            
            if(!found)
            {
                Link u = new CentroidConnector(new_id++, s, r);
                newLinks.add(u);
            }
        }
        
        PrintStream fileout = new PrintStream(new FileOutputStream(new File("newLinks.txt")), true);
        for(Link l : newLinks)
        {
            fileout.println(l.createLinkRecord());
        }
        fileout.close();
    }
    
}
