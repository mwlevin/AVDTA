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
        
        

        
        
       // DTAProject project = new DTAProject(new File("projects/ssmall_network_SPaT"));
        DTAProject project = new DTAProject(new File("projects/Twin-Cities-MP-LTM-SPAT"));
        DTASimulator sim = project.getSimulator();  
        System.out.println("Reading simulator");
        ReadDTANetwork read = new ReadDTANetwork();
        read.prepareDemand(project);
        //project.loadSimulator();
        

        //System.out.println("There are " + numVeh + " vehicles in the network.");
        //System.out.println(sim.getCostFunction());
        System.out.println("Starting MSA");
        sim.initialize();
        sim.msa(40);
        sim.postProcess();
        
        
        ArrayList<Node> SPaT = new ArrayList<Node>();
        SPaT.add(sim.getNode(6359));
        SPaT.add(sim.getNode(6089));
        SPaT.add(sim.getNode(5896));
        SPaT.add(sim.getNode(5688));
        SPaT.add(sim.getNode(5604));
        SPaT.add(sim.getNode(5443));
        SPaT.add(sim.getNode(5418));
        SPaT.add(sim.getNode(5376));
        SPaT.add(sim.getNode(5305));
        SPaT.add(sim.getNode(5181));
        SPaT.add(sim.getNode(5100));
        SPaT.add(sim.getNode(4981));
        SPaT.add(sim.getNode(4855));
        SPaT.add(sim.getNode(4810));
        SPaT.add(sim.getNode(19823));
        SPaT.add(sim.getNode(4362));
        SPaT.add(sim.getNode(4298));
        SPaT.add(sim.getNode(4174));
        SPaT.add(sim.getNode(18678));
        SPaT.add(sim.getNode(4080));
        SPaT.add(sim.getNode(4030));
        SPaT.add(sim.getNode(4004));
        SPaT.add(sim.getNode(3955));
       
        

        int numSpatRoute = 0;
        int numCVSpatRoute = 0;
        int numHVSpatRoute = 0;
        double avgTTspatCV = 0;
        double lenConnectTraveled = 0;
        double lenI55traveled = 0;
        
        for(Vehicle v: sim.getVehicles()){
            if(usedConnectedCorridor(v, SPaT)){
                numSpatRoute++;
                if(v.getType() == 111){
                    numHVSpatRoute++;
                } else {
                    numCVSpatRoute++;
                    avgTTspatCV += v.getTT();  
                    for(Link l : v.getPath()){
                        for(Node n: SPaT){
                            if(l.getDest().equals(n)){
                                lenConnectTraveled += l.getLength();
                            }
                        }
                    }
                }
                
                for(Link l : v.getPath()){
                    for(Node n: SPaT){
                        if(l.getDest().equals(n)){
                            lenI55traveled += l.getLength();
                        }
                    }
                }
            }
        }
        System.out.println("MPR = 0%");
        System.out.println("Total Num veh taking SPaT Route: " + numSpatRoute);
        System.out.println("Num CV taking SPaT Route: " + numCVSpatRoute);
        System.out.println("Num HV taking SPaT Route: " + numHVSpatRoute);
        System.out.println("AVG TT of CVs on SPaT Route: " + avgTTspatCV/numCVSpatRoute);


        
        
        String filename = project.getResultsFolder()+"/link_tt.txt";
        sim.printLinkTT(0, sim.getLastExitTime()+sim.ast_duration, new File(filename));
        
        filename = project.getResultsFolder()+"/link_flow.txt";
        sim.printLinkFlow(0, sim.getLastExitTime()+sim.ast_duration, new File(filename));
    }
    
    public static Boolean usedConnectedCorridor(Vehicle v, ArrayList<Node> SPaT){
        
        for(Link l : v.getPath()){
            for(Node n: SPaT){
                if(l.getSource().equals(n) || l.getDest().equals(n)){
                    return true;
                }
            }
        }
        return false;
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
