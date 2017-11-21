/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netdesign;

import avdta.demand.DemandImportFromVISTA;
import avdta.demand.DemandProfile;
import avdta.demand.DemandProfileRecord;
import avdta.demand.DynamicODRecord;
import avdta.demand.DynamicODTable;
import avdta.demand.ReadDemandNetwork;
import avdta.demand.StaticODRecord;
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
	//percentage of total demand simulated
	static int demand = 100;
	
    public static void main(String[] args) throws IOException
    {
    		int maxiter = 1;
    		double prop = demand/100.0;
    		double mingap = 1;
    		
    		//load parent project
        DTAProject project = new DTAProject(new File("AVDTA2/projects/coacongress"));
        
        //adjust demand proportion
        	ReadDTANetwork read = new ReadDTANetwork();
        	read.prepareDemand(project, prop);
        
        	//load simulator
        DTASimulator sim = project.getSimulator();
        
        	//run MSA
        	sim.msa(maxiter, mingap);
        
        	//rename coacongress assignment folder for given demand
        	sim.getAssignment().getAssignmentFolder().renameTo(new File(project.getAssignmentsFolder()+"/"+demand));
        	
        	//for a list of all signal node IDs
        	List<Integer> signals = new ArrayList<>();
        	Scanner filein = new Scanner(project.getSignalsFile());
        	filein.nextLine();
        	while(filein.hasNextLine()) {
        		signals.add(filein.nextInt());
        		filein.nextLine();
        	}
        	filein.close();
        	
        	//create new projects for all signal intersections in coacongress
//        	for(int i : signals) {
//        		createTestIntersection(i, sim, 0);
//        	}
        	
        	//create a single new project for an intersection specified
        	createTestIntersection(signals.get(0), sim, 1);
        	DTAProject testIntersection = new DTAProject(new File("AVDTA2/projects/testIntersections/intersection6336_100"));
        	ReadDTANetwork read2 = new ReadDTANetwork();
        	read2.prepareDemand(testIntersection, prop);
        	DTASimulator sim2 = testIntersection.getSimulator();
        	sim2.msa(5, 1);
    
    }
    
    //Creates a new DTA project and network consisting of just one intersection
    //intControl = (0:signal, 1:reservation)
    public static void createTestIntersection(int nodeid, DTASimulator sim, int intControl) throws IOException
    {
        //Create new project for Test Intersection (in links and nodes lists)
        DTAProject newIntersection = new DTAProject();
        newIntersection.createProject("intersection"+nodeid+"_"+demand, new File("AVDTA2/projects/testIntersections/intersection"+nodeid+"_"+demand));

        Node Intnode = sim.getNode(nodeid);
        
        //Get nodes and links of newIntersection (Maps = ID:Link or Node)
        Map<Integer ,Link> linkMap = new HashMap<>();
        Map<Integer, Node> nodeMap = new HashMap<>();
        nodeMap.put(Intnode.getId(), Intnode);
        
        for(Link l:Intnode.getIncoming()){
	        	if(!l.isCentroidConnector()){
	        		linkMap.put(l.getId(),l);
	        	}
	        
	        	if(!l.getSource().isZone()) {
	        		nodeMap.put(l.getSource().getId(), l.getSource());
	        	}
        }
        
        for(Link l: Intnode.getOutgoing()){
	        	if(!l.isCentroidConnector() && !linkMap.containsKey(l.getId())) {
	        		linkMap.put(l.getId(),l);
	        	}
	        	
	        	if(!l.getDest().isZone() && !nodeMap.containsKey(l.getDest().getId())) {
	        	nodeMap.put(l.getDest().getId(), l.getDest());
	        }
        }
        
        //Create LinkRecord and NodeRecord Lists
        List<LinkRecord> links = new ArrayList<LinkRecord>();
        List<NodeRecord> nodes = new ArrayList<NodeRecord>();
        for(Link l : linkMap.values())
        {
            links.add(l.createLinkRecord());
        }
        for(Node n : nodeMap.values()) {
        		nodes.add(n.createNodeRecord());
        }
        //TODO check if the below is correct and sufficient in changing the network's control
        //change the intersection control based on input intControl
        if(intControl == 1) {
        		for(NodeRecord r : nodes) {
        			r.setType(301);
        		}
        }
        
        //To get the demand at each Turn/OD (for StaticOD)
        		//create list of intersection's phases
        List<PhaseRecord> phases = new ArrayList<>();
        Scanner phasefilein = new Scanner(sim.getProject().getPhasesFile());
        phasefilein.nextLine();
        
        while(phasefilein.hasNextLine()) {
        		PhaseRecord phase = new PhaseRecord(phasefilein.nextLine());
        		
        		if(phase.getNode() == Intnode.getId()) {
        			phases.add(phase);
        		}
        }
        phasefilein.close();
        		//create map of each TurnRecord to its demand
        Map<TurnRecord,Integer> turnCount = new HashMap<>();

        for(Vehicle v : sim.getVehicles()){
        	Path  p = v.getPath();
        	List<Integer> pList = p.getPathIdList();
        	
	        	for(PhaseRecord phase : phases) 
	        	{
	        		for(TurnRecord t : phase.getTurns())
	                {
	                    if(pList.contains(t.getI()) && pList.contains(t.getJ()))
	                    {
	                    	if(turnCount.containsKey(t))  turnCount.put(t, turnCount.get(t)+1);
	                    	else turnCount.put(t, 1);
	                    }           		
	                }  
	        	}
        }
        
        //for getting StaticODRecord List, and adding centroids and centroid connectors to nodes and links Lists
        List<StaticODRecord> ODrecord = new ArrayList<StaticODRecord>();
        int i = 1;
        for(TurnRecord t:turnCount.keySet())
        {
        		//obtain demand in StaticODRecord List (based on counts from coacongress DTA run)
	        	Link in = linkMap.get(t.getI());
	        	Link out = linkMap.get(t.getJ());
	        		//to create centroid nodes, input +100000 to 2nd and 3rd entries in StaticODRecord constructor
	        	StaticODRecord staticOD = new StaticODRecord(i, 121, in.getSource().getId()+100000, out.getDest().getId()+100000, turnCount.get(t));
	        	//	check for duplicates
	        	int j = 0;
	        	for(StaticODRecord s : ODrecord) {
	        		if((s.getOrigin() == staticOD.getOrigin()) && (s.getDest() == staticOD.getDest())) {
	        			j++;
	        		}
	        	}
	        	if(j == 0)		ODrecord.add(staticOD);
	        	
	        	//add Noderecords for Centroid nodes
	        	NodeRecord zoneSource = new NodeRecord(in.getSource().getId()+100000, 1000, 0, 0, 0);
	        	NodeRecord zoneSink = new NodeRecord(out.getDest().getId()+100000, 1000, 0, 0, 0);
	        	//	check for duplicates
	        	int q = 0, k = 0;
	        	for(NodeRecord r : nodes) {
	        		if (r.getId() == zoneSource.getId()) {
	        			q++;
	        		}
	        		if (r.getId() == zoneSink.getId()) {
	        			k++;
	        		}
	        	}
	        	if(q == 0)		nodes.add(zoneSource);	
	        if(k == 0)		nodes.add(zoneSink);
	        
	        	//add LinkRecords for Centroid Connectors/links
	        	LinkRecord centIn = new LinkRecord(70000+i, 1000, in.getSource().getId()+100000, in.getSource().getId(), 500.0, 60.0, 30.0, 100000, 1);
	        	LinkRecord centOut = new LinkRecord(80000+i, 1000, out.getDest().getId(), out.getDest().getId()+100000, 500.0, 60.0, 30.0, 100000, 1);
	        	//	check for duplicates
	        	int x = 0, y = 0;
	        	for(LinkRecord l : links) {
	        		if ((l.getSource() == centIn.getSource()) && (l.getDest() == centIn.getDest())) {
	        			x++;
	        		}
	        		if ((l.getSource() == centOut.getSource()) && (l.getDest() == centOut.getDest())) {
	        			y++;
	        		}
	        	}
	        	if(x == 0)		links.add(centIn);	
	        if(y == 0)		links.add(centOut);
	        
	        	i++;
        }
        
        
        
        //////////////////////////////
        //WRITE TO FILES
        //////////////////////////////
        
        //Write to Phases file
        PrintStream fileout = new PrintStream(new FileOutputStream(newIntersection.getPhasesFile()), true);
        fileout.println(ReadNetwork.getPhasesFileHeader());
        for(PhaseRecord r : phases) {
        		fileout.println(r);
        }
        fileout.close();
        
        
        //Write to Static OD File (based on demand counted during DTA run of coacongress)
        fileout = new PrintStream(new FileOutputStream(newIntersection.getStaticODFile()), true);
        fileout.println(ReadNetwork.getStatidODHeader());
        for(StaticODRecord s : ODrecord) {
        		fileout.println(s);
        }
        fileout.close();
        
        //Write to Demand Profile file (copy from coacongress)
        fileout = new PrintStream(new FileOutputStream(newIntersection.getDemandProfileFile()), true);
        fileout.println(ReadDemandNetwork.getDemandProfileFileHeader());
        Scanner SODfilein = new Scanner(sim.getProject().getDemandProfileFile());
        SODfilein.nextLine();
        
        while(SODfilein.hasNextLine()) {
        		DemandProfileRecord SOD = new DemandProfileRecord(SODfilein.nextLine());
        		fileout.println(SOD);
        }
        fileout.close();
        SODfilein.close();
        
        
        //Write/create Dynamic OD file from Static OD and Demand Profile
        ReadDemandNetwork read = new ReadDemandNetwork();
        read.createDynamicOD(newIntersection);
        
        //TODO do we need this proportion to be the same as coacongress?
        //Write/create Demand file from Dynamic OD and Demand Profile and specified proportion of total demand
        read.prepareDemand(newIntersection, 1);
        
        
        //Write to Links File (including centroid connectors)
        fileout = new PrintStream(new FileOutputStream(newIntersection.getLinksFile()), true);
        fileout.println(ReadNetwork.getLinksFileHeader());
        for(LinkRecord record : links)
        {
            fileout.println(record);
        }
        fileout.close();
        
        
        // Write to Signals File
        fileout = new PrintStream(new FileOutputStream(newIntersection.getSignalsFile()), true);
        fileout.println(ReadNetwork.getSignalsFileHeader());
        fileout.println(Intnode.getId() + "\t" + 0);
        fileout.close();
        
        
        //Write to Nodes File (including centroids)
        fileout = new PrintStream(new FileOutputStream(newIntersection.getNodesFile()), true);
        fileout.println(ReadNetwork.getNodesFileHeader());
        
        for(NodeRecord n : nodes) {
        		fileout.println(n);
        }
        fileout.close();

        
        //Write to Link Coordinates file
        fileout = new PrintStream(new FileOutputStream(newIntersection.getLinkPointsFile()), true);
        fileout.println(ReadNetwork.getLinkPointsFileHeader());
        
        for(Link l : linkMap.values()) {
        		
        		fileout.println(l.getId() + "\t" + "("+l.getSource().getLon()+","+l.getSource().getLat()+"),("+l.getDest().getLon()+","+l.getDest().getLat()+")");

        }
        fileout.close();
        
        
        //Write to Signal Results file
        fileout = new PrintStream(new FileOutputStream(new File("AVDTA2/projects/testIntersections/intersection"+nodeid+"_"+demand+"/results/signalresults.txt")), true);
        fileout.println("Signal Regression Results: Intersection "+nodeid+"@"+demand+"% demand");
        fileout.println();
        fileout.println("time_red\ttime_yellow\ttime_green\tnum_moves\tcapacity\tnum_lanes");
        //TODO Find how to encapsulate variables for a whole intersection for all phases
        fileout.println("TSTT = ");
        fileout.close();
        
        //Write to Reservation Results file
        fileout = new PrintStream(new FileOutputStream(new File("AVDTA2/projects/testIntersections/intersection"+nodeid+"_"+demand+"/results/TBRresults.txt")), true);
        fileout.println("TBR Regression Results: Intersection "+nodeid+"@"+demand+"% demand");
        fileout.println();
        fileout.println("num_moves\tcapacity\tnum_lanes");
        //TODO Find more variables to use for reservations and how to encapsulate current variables for whole intersection
        fileout.println("TSTT = ");
        fileout.close();
}
    
}
