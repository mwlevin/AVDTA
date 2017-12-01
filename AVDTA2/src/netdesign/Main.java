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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
	static int demandprops[] = {10};
	//30, 50, 75, 85, 100
	//static int demandprops[] = {30};
	static int demand;
	
    public static void main(String[] args) throws IOException
    {
    	
// 	PrintStream fileout = new PrintStream(new FileOutputStream(new File("REGresults/dallas_downtown_results/results.txt")), true);
// 	fileout.println("TSTT (hrs) and AvgTT (min/veh) for: 1. 100% Signals\t2. 100% Reservations\t3. Regression Results");
// 	fileout.println();
// 	
// 	//Change all vehicles to AVs
//	DTAProject project = new DTAProject(new File("AVDTA2/projects/dallas_downtown"));
//	ReadDTANetwork demandread = new ReadDTANetwork();
//	Map<Integer, Double> proportionmap = new HashMap<Integer, Double>();
//	proportionmap.put(121, 1.0);
//	demandread.changeDynamicType(project, proportionmap);
//	demandread.prepareDemand(project, 1.0);
//	
//	//100% Signals
//	changeAllNodes(project, 100);
//	
//	for(int i: demandprops) {
//		fileout.print("SIG_TSTT_"+i+"\tSIG_AvgTT_"+i+"\t");
//	}
//	fileout.println();
//	
//    	for(int i: demandprops) {
//    		ReadDTANetwork read = new ReadDTANetwork();
//    		read.prepareDemand(project, i/100.0);
//    		DTASimulator sim = project.getSimulator();
//    		sim.msa(30, 1);
//    		fileout.print(sim.getTSTT()/3600.0 + "\t" + sim.getTSTT()/60.0/sim.getNumVehicles() + "\t");
//    	}
//    	fileout.println();
//    	
//    	//100% Reservations
//    	changeAllNodes(project, 301);
//    	
//    	for(int i: demandprops) {
//    		fileout.print("TBR_TSTT_"+i+"\tTBR_AvgTT_"+i+"\t");
//    	}
//    	fileout.println();
//    	
//    	for(int i: demandprops) {
//    		ReadDTANetwork read = new ReadDTANetwork();
//    		read.prepareDemand(project, i/100.0);
//    		DTASimulator sim = project.getSimulator();
//    		sim.msa(30, 1);
//    		fileout.print(sim.getTSTT()/3600.0 + "\t" + sim.getTSTT()/60.0/sim.getNumVehicles() + "\t");
//    	}
//    	fileout.println();
//    	
//    	//Regression decided intersections
//    	Map<Integer, ArrayList<Integer>> keepsignals = new HashMap<Integer, ArrayList<Integer>>();
//    	keepsignals.put(10, new ArrayList<Integer>(Arrays.asList(55185, 55157, 55257, 54886, 70471, 70481)));
//    	keepsignals.put(30, new ArrayList<Integer>(Arrays.asList(55185,	55157,	55257,	54886,	70460,	70461,	70471,	70481,	70482)));
//    	keepsignals.put(50, new ArrayList<Integer>(Arrays.asList(55185,	55157,	55257,	54886,	70471,	70481)));
//    	keepsignals.put(75, new ArrayList<Integer>(Arrays.asList(55185,	55157,	55257,	54886,	70471,	70481)));
//    	keepsignals.put(85, new ArrayList<Integer>(Arrays.asList(55185,	55157,	55257,	54886,	70471,	70481)));
//    	keepsignals.put(100, new ArrayList<Integer>(Arrays.asList(55185,	54886,	70471,	70481)));
//    	
//    	for(int i: demandprops) {
//    		fileout.print("REG_TSTT_"+i+"\tREG_AvgTT_"+i+"\t");
//    	}
//    	fileout.println();
//    	
//    	for(int i: demandprops) {
//    		changeSomeNodes(project, keepsignals.get(i));
//    		ReadDTANetwork read = new ReadDTANetwork();
//    		read.prepareDemand(project, i/100.0);
//    		DTASimulator sim = project.getSimulator();
//    		sim.msa(30, 1);
//    		fileout.print(sim.getTSTT()/3600.0 + "\t" + sim.getTSTT()/60.0/sim.getNumVehicles() + "\t");
//    	}
//    	fileout.println();
//    	
//    	fileout.close();
    	
    	DTAProject project = new DTAProject(new File("AVDTA2/projects/coacongress"));

    	
    	Map<Integer, List<Map<Double, Map<String, Double>>>> signalTurns = createAllIntersections();
    	    	
    	runRegressionDTA(project);
    	
    	printIntersectionChar(signalTurns, project);
    	
    }
    
    /*
		// Load coacongress project and get list of all intersections
		DTAProject project = new DTAProject(new File("AVDTA2/projects/coacongress"));
		List<Integer> signals = new ArrayList<>();
		Scanner filein = new Scanner(project.getSignalsFile());
		filein.nextLine();
		while (filein.hasNextLine()) {
			signals.add(filein.nextInt());
			filein.nextLine();
		}
		filein.close();

		// Create intersection characteristic file
		PrintStream fileout = new PrintStream(new FileOutputStream(new File("REGresults/inter_characteristics")), true);
		fileout.println("Characteristics of all Test Intersections");
		fileout.println("ID\tnum_phases\ttime_red\ttime_yellow\ttime_green\tnum_moves\tnum_lanes\tavglanecapacity");

		for (int i : signals) {
			double avgred = 0;
			double avgyellow = 0;
			double avggreen = 0;
			int nummoves = 0;
			int numphases = 0;
			int numlanes = 0;
			double avglanecapacity = 0;
			List<PhaseRecord> phaserec = new ArrayList<>();
			List<LinkRecord> linkrec = new ArrayList<>();

			DTAProject testIntersection = new DTAProject(
					new File("AVDTA2/projects/testIntersections/SIG_intersection" + i + "_" + demandprops[0]));
			Scanner phasesfilein = new Scanner(testIntersection.getPhasesFile());
			Scanner linksfilein = new Scanner(testIntersection.getLinksFile());
			phasesfilein.nextLine();
			linksfilein.nextLine();

			while (phasesfilein.hasNextLine()) {
				PhaseRecord temp = new PhaseRecord(phasesfilein.nextLine());
				phaserec.add(temp);
			}
			while (linksfilein.hasNextLine()) {
				LinkRecord temp = new LinkRecord(linksfilein.nextLine());
				if (temp.getType() != 1000) {
					linkrec.add(temp);
				}
			}
			for (LinkRecord l : linkrec) {
				int lanes = l.getNumLanes();
				numlanes += lanes;
				double capacity = l.getCapacity();
				avglanecapacity += capacity;
			}
			for (PhaseRecord p : phaserec) {
				double red = p.getTimeRed();
				avgred += red;
				double yellow = p.getTimeYellow();
				avgyellow += yellow;
				double green = p.getTimeGreen();
				avggreen += green;
				int moves = p.getTurns().size();
				nummoves += moves;
			}
			avglanecapacity = avglanecapacity / numlanes;
			avgred = avgred / phaserec.size();
			avgyellow = avgyellow / phaserec.size();
			avggreen = avggreen / phaserec.size();
			numphases = phaserec.size();

			fileout.println(i + "\t" + numphases + "\t" + avgred + "\t" + avgyellow + "\t" + avggreen + "\t" + nummoves
					+ "\t" + numlanes + "\t" + avglanecapacity);

			phasesfilein.close();
			linksfilein.close();
		}
		fileout.close();

     */
    //  PRINT INTERSECTION CHARACTERISTICS
    public static void printIntersectionChar(Map<Integer, List<Map<Double, Map<String, Double>>>> signalTurns, Project project) throws IOException{
		// Load downtown_dallas project and get list of all intersections
		List<Integer> signals = new ArrayList<>();
		Scanner filein = new Scanner(project.getSignalsFile());
		filein.nextLine();
		while (filein.hasNextLine()) {
			signals.add(filein.nextInt());
			filein.nextLine();
		}
		filein.close();

		// Create intersection characteristic file
		PrintStream fileout = new PrintStream(
				new FileOutputStream(new File("REGresults/" + project.getName() + "_inter_characteristics")), true);
		fileout.println(
				"ID\tnum_phases\ttime_red\ttime_yellow\ttime_green\tnum_moves\tnum_lanes\tavglanecapacity\tthroughturns\tleftturns\trightturns\tdemandprop");

		for (int d : demandprops) {
			for (int i : signals) {
				double avgred = 0;
				double avgyellow = 0;
				double avggreen = 0;
				int nummoves = 0;
				int numphases = 0;
				int numlanes = 0;
				double avglanecapacity = 0;
				List<PhaseRecord> phaserec = new ArrayList<>();
				List<LinkRecord> linkrec = new ArrayList<>();

				Scanner phasesfilein = new Scanner(project.getPhasesFile());
				Scanner linksfilein = new Scanner(project.getLinksFile());
				phasesfilein.nextLine();
				linksfilein.nextLine();

				while (phasesfilein.hasNextLine()) {
					PhaseRecord temp = new PhaseRecord(phasesfilein.nextLine());
					if (temp.getNode() == i) {
						phaserec.add(temp);
					}
				}
				while (linksfilein.hasNextLine()) {
					LinkRecord temp = new LinkRecord(linksfilein.nextLine());
					if ((temp.getType() == 100) && (temp.getDest() == i || temp.getSource() == i)) {
						linkrec.add(temp);
					}
				}
				for (LinkRecord l : linkrec) {
					int lanes = l.getNumLanes();
					numlanes += lanes;
					double capacity = l.getCapacity();
					avglanecapacity += capacity;
				}
				for (PhaseRecord p : phaserec) {
					double red = p.getTimeRed();
					avgred += red;
					double yellow = p.getTimeYellow();
					avgyellow += yellow;
					double green = p.getTimeGreen();
					avggreen += green;
					int moves = p.getTurns().size();
					nummoves += moves;
				}
				avglanecapacity = avglanecapacity / numlanes;
				avgred = avgred / phaserec.size();
				avgyellow = avgyellow / phaserec.size();
				avggreen = avggreen / phaserec.size();
				numphases = phaserec.size();
				
				List<Map<Double, Map<String, Double>>> demandTurns = signalTurns.get(i);
				Map<String, Double> dTurn = new HashMap<>();
				for(Map<Double, Map<String, Double>> j : demandTurns){
					if(j.containsKey((double)d)){
						dTurn = j.get((double)d);
						break;
					}
				}

				fileout.println(i + "\t" + numphases + "\t" + avgred + "\t" + avgyellow + "\t" + avggreen + "\t"
						+ nummoves + "\t" + numlanes + "\t" + avglanecapacity + "\t"
						+ dTurn.get("through") + "\t" + dTurn.get("left") + "\t"
						+ dTurn.get("right") + "/t" + d);

				phasesfilein.close();
				linksfilein.close();
			}
		}
	    	fileout.close();
    }
    
    // RUN DTA ON ALL INTERSECTIONS
    public static void runRegressionDTA(DTAProject project) throws IOException{
    	
    	 
    	//Load coacongress project and get list of all intersections
//    	DTAProject project = new DTAProject(new File("AVDTA2/projects/coacongress"));
    	
    	
    	List<Integer> signals = new ArrayList<>();
    	Scanner filein = new Scanner(project.getSignalsFile());
    	filein.nextLine();
    	while(filein.hasNextLine()) {
    		signals.add(filein.nextInt());
    		filein.nextLine();
    	}
    	filein.close();
    	
    	PrintStream fileout = new PrintStream(new FileOutputStream(new File("REGresults/results")), true);
    	
	    fileout.println("Int ID\tTSTT_DELTA (min)\tAvgTT_DELTA (sec/veh)\tdemandprop");
    	
    	//Create new results folder for each demand scenario (results files currently contain TSTT and AvgTT for SIG and TBR intersections)
    	for(int d : demandprops) {
	    
    		
    		//Solve DTAUE using MSA on all signalized Test Intersections
	    	for(int i : signals) {
	    		DTAProject SIGtestIntersection = new DTAProject(new File("AVDTA2/projects/testIntersections/SIG_intersection"+i+"_"+d));
	    		DTASimulator SIGsim = SIGtestIntersection.getSimulator();
	    		//run MSA and print to results file
	    		SIGsim.msa(30, 2);
	    		
	    		System.out.println("You are "+d/100+"through for Signals");

	    		
	    		DTAProject TBRtestIntersection = new DTAProject(new File("AVDTA2/projects/testIntersections/TBR_intersection"+i+"_"+d));
	    		DTASimulator TBRsim = TBRtestIntersection.getSimulator();
	    		//run MSA and print to results file
	    		TBRsim.msa(30, 2);
	    		
	    		System.out.println("You are "+d/100+"through for TBR");
	    		
	    		fileout.println(i+"\t"+String.format("%.2f", (SIGsim.getTSTT()-TBRsim.getTSTT())/60.0)+"\t"+String.format("%.2f", ((SIGsim.getTSTT()/SIGsim.getVehicles().size())-(TBRsim.getTSTT()/TBRsim.getVehicles().size())))+"\t"+d);
	    	}
	    	
    	}
    	fileout.close();

    }
    
    
    //CREATE ALL TEST INTERSECTIONS 
    public static Map<Integer, List<Map<Double, Map<String, Double>>>> createAllIntersections() throws IOException{
    	Map<Integer, List<Map<Double, Map<String, Double>>>> turns = new HashMap<Integer,  List<Map<Double, Map<String, Double>>>>();
    	
		for (int d : demandprops) {
			demand = d;
			double prop = demand / 100.0;
    		

			// load parent project
			DTAProject project = new DTAProject(new File("AVDTA2/projects/coacongress"));
	    	Map<Integer, Double> proportionmap = new HashMap<Integer, Double>();
	    	proportionmap.put(121, 1.0);
	    	

			// adjust demand proportion
			ReadDTANetwork read = new ReadDTANetwork();

			read.changeDynamicType(project, proportionmap);
			read.prepareDemand(project, prop);

			// load simulator
			DTASimulator sim = project.getSimulator();

			// run MSA
			sim.msa(30, 2);
			
			System.out.println("You are "+d/100+"through");

			// rename coacongress assignment folder for given demand
			sim.getAssignment().getAssignmentFolder()
					.renameTo(new File(project.getAssignmentsFolder() + "/" + demand + "_demand"));

			// for a list of all signal node IDs
			List<Integer> signals = new ArrayList<>();
			Scanner filein = new Scanner(project.getSignalsFile());
			filein.nextLine();
			while (filein.hasNextLine()) {
				signals.add(filein.nextInt());
				filein.nextLine();
			}
			filein.close();
			
				// create new projects for all signal intersections in
				// coacongress
			Map<Integer, Map<String, Double>> tempTurns = new HashMap<>();
				for (int i : signals) {
					tempTurns.put(i, createTestIntersection(i, sim, 0));
					createTestIntersection(i, sim, 1);
				}
				
				for(int i : signals){
					Map<Double, Map<String,Double>> demandTurns = new HashMap<>();
					demandTurns.put((double) d, tempTurns.get(i));
					if(turns.containsKey(i)){
						turns.get(i).add(demandTurns);
					}
					else{
						turns.put(i, new ArrayList());
						turns.get(i).add(demandTurns);
					}
				}
			
		

		}
		return turns;
    
    }

    //Creates a new Nodes file with all intersection nodes being of the same type (100 for Signals, 301 for FCFS)
    public static void changeAllNodes(DTAProject project, Integer intcontrol) throws IOException
    {
        Scanner filein = new Scanner(project.getNodesFile());
        File newFile = new File(project.getProjectDirectory()+"/new_nodes.txt");
        PrintStream fileout = new PrintStream(new FileOutputStream(newFile), true);
        
        fileout.println(filein.nextLine());
        
        while(filein.hasNextLine())
        {
            NodeRecord node = new NodeRecord(filein.nextLine());
            if(!node.isZone())
            {
                node.setType(intcontrol);
            }
            fileout.println(node);
        }
        filein.close();
        fileout.close();
        
        project.getNodesFile().delete();
        newFile.renameTo(project.getNodesFile());
    }
    
    //Creates a new Nodes file with the specified List of nodes changed to signals and the rest changed to TBR
    public static void changeSomeNodes(DTAProject project, List<Integer> keepsignals) throws IOException
    {
        Scanner filein = new Scanner(project.getNodesFile());
        File newFile = new File(project.getProjectDirectory()+"/new_nodes.txt");
        PrintStream fileout = new PrintStream(new FileOutputStream(newFile), true);
        
        fileout.println(filein.nextLine());
        
        while(filein.hasNextLine())
        {
            NodeRecord node = new NodeRecord(filein.nextLine());
            if(!node.isZone())
            {
            		if(keepsignals.contains(node.getId())) {
            			node.setType(100);
            		}
                node.setType(301);
            }
            fileout.println(node);
        }
        filein.close();
        fileout.close();
        
        project.getNodesFile().delete();
        newFile.renameTo(project.getNodesFile());
    }
    
    //Creates a new DTA project and network consisting of just one intersection (inControl is 0 for signal, 1 for reservation)
    public static Map<String, Double> createTestIntersection(int nodeid, DTASimulator sim, int intControl) throws IOException
    {
        //Create new project for Test Intersection (in links and nodes lists)
        DTAProject newIntersection = new DTAProject();
        
        if(intControl == 0) {
        		newIntersection.createProject("SIG_intersection"+nodeid+"_"+demand, new File("AVDTA2/projects/testIntersections/SIG_intersection"+nodeid+"_"+demand));
        }
        else if(intControl == 1) {
        		newIntersection.createProject("TBR_intersection"+nodeid+"_"+demand, new File("AVDTA2/projects/testIntersections/TBR_intersection"+nodeid+"_"+demand));
        }
        else {
        	System.out.println("Invalid intersection control type");
        }
        
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
        
        //change the intersection control based on input intControl
        if(intControl == 0) {
        		for(NodeRecord r : nodes) {
        			if(r.getId() == Intnode.getId()){
        				r.setType(100);
        			}
        			else{
        				r.setType(301);
        			}
        		}
        }
        else if(intControl == 1) {
        		for(NodeRecord r : nodes) {
        			r.setType(301);
        		}
        }
        else {
        	System.out.println("Invalid intersection control type");
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
        double leftTurns = 0;
        double rightTurns = 0;
        double throughTurns = 0;
        for(TurnRecord t:turnCount.keySet())
        {
        		//obtain demand in StaticODRecord List (based on counts from coacongress DTA run)
	        	Link in = linkMap.get(t.getI());
	        	double inAngle = in.getDirection();
	        	
	        	Link out = linkMap.get(t.getJ());	        	
	        	double outAngle = out.getDirection();
	        	double theta = out.getDirection() - in.getDirection() > 0 ? out.getDirection() - in.getDirection() : out.getDirection() - in.getDirection() + 2*Math.PI;
	        	if(theta >= 5*Math.PI/4){
	        		leftTurns+=turnCount.get(t);
	        	}
	        	else if(theta <= Math.PI/4){
	        		rightTurns+=turnCount.get(t);
	        	}
	        	else
	        	{
	        		throughTurns += turnCount.get(t);
	        	}
	        	
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
	        	LinkRecord centIn = new LinkRecord(70000+i, 1000, in.getSource().getId()+100000, in.getSource().getId(), 50.0, 60.0, 30.0, 100000, 1);
	        	LinkRecord centOut = new LinkRecord(80000+i, 1000, out.getDest().getId(), out.getDest().getId()+100000, 50.0, 60.0, 30.0, 100000, 1);
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
        
        Map<String, Double> turnMap = new HashMap();
        turnMap.put("through",throughTurns);
        turnMap.put("left",leftTurns);
        turnMap.put("right",rightTurns);
        return turnMap;
        
}
    
}
