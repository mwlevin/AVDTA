/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netdesign;

import avdta.demand.DemandImportFromVISTA;
import avdta.demand.DemandProfile;
import avdta.demand.DynamicODRecord;
import avdta.demand.DynamicODTable;
import avdta.demand.ReadDemandNetwork;
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

import java.io.IOException;

/**
 *
 * @author micha
 */
public class Main 
{
	static DTASimulator sim;
    public static void main(String[] args) throws IOException
    {
    		int maxiter = 20;
    		double[] prop = {0.75}; //,0.85,1.0
    		double mingap = 1;
    		
    		
//    		PrintStream out = new PrintStream(new FileOutputStream(new File("AVDTA2/projects/coacongress/results/test1_100.txt")));
//    		out.println("Test output 1 for coacongress network at 100% demand");
//    		out.println("TSTT");
    		//load project
        DTAProject project = new DTAProject(new File("AVDTA2/projects/coacongress"));
        
        //adjust demand proportion
        for(double i:prop){
        	ReadDTANetwork read1 = new ReadDTANetwork();
        	read1.prepareDemand(project, i);
        
        	//load simulator
        	sim = project.getSimulator();
        
        	//run MSA
        	sim.msa(maxiter, mingap);
        
        	sim.getAssignment().getAssignmentFolder().renameTo(new File(project.getAssignmentsFolder()+"/100"));
        	System.out.println("End of MSA");
            
        	Scanner filein = new Scanner(sim.getProject().getPhasesFile());
            while(filein.hasNextLine()){
            	SignalRecord signal = new SignalRecord(filein.nextLine());
        		createTestIntersection(signal.getNode());
            }
        	
        
//        	out.println(sim1.getTSTT());
//        	out.close();
        }
    }
    
    public static void createTestIntersection(int nodeid) throws IOException
    {
        // requires 4 incoming and 4 outgoing links (not including centroid connectors)
        
        //DTAProject project = new DTAProject(new File("projects/coacongress"));
       // DTASimulator sim = project.getSimulator();
        
        DTAProject newIntersection = new DTAProject();
        newIntersection.createProject("intersection"+nodeid, new File("projects/intersection/"+nodeid));
        //DTASimulator sim2 = newIntersection.getSimulator();
        System.out.println("Project intersection");
        Node node = sim.getNode(nodeid);
        
        Map<Integer ,Link> linkMap = new HashMap();
        
        for(Link l:node.getIncoming()){
        	if(l.isCentroidConnector()){
        		continue;
        	}
        	linkMap.put(l.getId(),l);
        }
        
        for(Link l: node.getOutgoing()){
        	if(l.isCentroidConnector() || linkMap.containsKey(l.getId())){
        		continue;
        	}
        	linkMap.put(l.getId(),l);
        }
        
        System.out.println(linkMap.size());
        // create list link records for outputs
        List<LinkRecord> links = new ArrayList<LinkRecord>();
        
        for(Link l : linkMap.values())
        {
            links.add(l.createLinkRecord());
        }

        
        // copy link details to mapped links
//        for(Link l : linkMap.values())
//        {
//            if(map.containsKey(record.getId()))
//            {
//        		LinkRecord record = new LinkRecord(l.getId(), l.getType(), l.getSource().getId(), l.getDest().getId(), l.getLength(), l.getFFSpeed(), l.getWaveSpeed(), l.getCapacity(), l.getNumLanes());
//                links.add(record);
//            }
//        }
        
        // write links to file
        PrintStream fileout = new PrintStream(new FileOutputStream(newIntersection.getLinksFile()), true);
        fileout.println(ReadNetwork.getLinksFileHeader());
        for(LinkRecord record : links)
        {
            fileout.println(record);
        }
        fileout.close();
        
       
        // create reverse map of above
        Map<Integer, Integer> reverseMap = new HashMap<>();
        for(int k : linkMap.keySet())
        {
            reverseMap.put(linkMap.get(k).getId(), k);
        }
        
        // print 0 signal offset
        fileout = new PrintStream(new FileOutputStream(newIntersection.getSignalsFile()), true);
        fileout.println(ReadNetwork.getSignalsFileHeader());
        fileout.println(new SignalRecord(1, 0));
        fileout.close();
        
        // copy phases data
        fileout = new PrintStream(new FileOutputStream(newIntersection.getPhasesFile()), true);
        fileout.println(ReadNetwork.getPhasesFileHeader());
        
        Scanner filein = new Scanner(sim.getProject().getPhasesFile());
        filein.nextLine();
        
        while(filein.hasNextLine())
        {
            PhaseRecord phase = new PhaseRecord(filein.nextLine());
            
            if(phase.getNode() == node.getId())
            {
                // change phase link ids
                for(TurnRecord t : phase.getTurns())
                {
                    t.setI(linkMap.get(t.getI()));
                    t.setJ(linkMap.get(t.getJ()));
                }
                
                fileout.println(phase);
            }
        }
        fileout.close();
        
    }
    
}
