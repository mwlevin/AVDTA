/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta;

import avdta.demand.DemandImportFromVISTA;
import avdta.dta.DTAImportFromVISTA;
import avdta.network.link.transit.BusLink;
import avdta.dta.DTAResults;
import avdta.dta.DTASimulator;
import avdta.dta.ReadDTANetwork;
import avdta.gui.DTAGUI;
import avdta.gui.GUI;
import avdta.gui.editor.Editor;
import avdta.gui.editor.visual.rules.LinkBusRule;
import avdta.network.Path;
import avdta.network.Simulator;
import avdta.project.DTAProject;
import avdta.project.Project;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import avdta.network.node.*;
import avdta.network.link.*;
import avdta.network.link.cell.Cell;
import avdta.network.node.policy.TransitFirst;
import avdta.project.SAVProject;
import avdta.util.RunningAvg;
import avdta.vehicle.Bus;
import avdta.vehicle.DriverType;
import avdta.vehicle.PersonalVehicle;
import avdta.vehicle.Vehicle;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.openstreetmap.gui.jmapviewer.Demo;



/**
 *
 * @author micha
 */
public class Main 
{
    public static void main(String[] args) throws IOException
    {
        //caccTest2();
        //GUI.main(args);


        
        //transitTest3();
        //transitTest2();
        //transitTest1();
        

        DTAProject project = new DTAProject(new File("projects/scenario_2_pm"));
        //new DemandImportFromVISTA(project, "data");
        new DTAImportFromVISTA(project, new File("data/vehicle_path.txt"), new File("data/vehicle_path_time.txt"));
        
        /*
        DTAProject project = new DTAProject(new File("projects/coacongress2"));
        //Editor gui = new Editor(project);
        SAVProject clone = new SAVProject();
        clone.createProject("coacongress2_SAV", new File("projects/coacongress2_SAV"));
        clone.cloneFromProject(project);
        */
    }
    
    public static void signalTimings() throws IOException
    {
        DTAProject project = new DTAProject(new File("projects/SiouxFalls"));
        
        Scanner filein = new Scanner(new File(project.getProjectDirectory()+"/vc.txt"));
        
        filein.nextLine();
        
        Simulator sim = project.getSimulator();
        
        while(filein.hasNextInt())
        {
            int source = filein.nextInt();
            int dest = filein.nextInt();
            double vc = filein.nextDouble();
            
            for(Link l : sim.getLinks())
            {
                if(l.getSource().getId() == source && l.getDest().getId() == dest)
                {
                    l.label = vc;
                }
            }
        }
        filein.close();
        
        PrintStream fileout = new PrintStream(new FileOutputStream(new File(project.getProjectDirectory()+"/phases.txt")), true);
        
        int id = 1;
        for(Node n : sim.getNodes())
        {
            if(!n.isZone())
            {
                int phaseid = 1;
                
                int incCount = 0;
                double vcSum = 0;
                
                for(Link l : n.getIncoming())
                {
                    if(!l.isCentroidConnector())
                    {
                        incCount++;
                        vcSum += l.label * l.getCapacity();
                    }
                }
                
                int outCount = 0;
                
                for(Link l : n.getOutgoing())
                {
                    if(!l.isCentroidConnector())
                    {
                        outCount++;
                    }
                }
                
                double L = incCount*4;
                
                double targetVC = 0.9;
                double PHF = 1;
                
                double C = L/ (1 - Math.min(0.9, (vcSum / (1615 * PHF * targetVC)) ));
                
                C = Math.min(120, C);
                
                double g_tot = C - L;
                
                System.out.println(n.getId()+" "+C+" "+L+" "+vcSum);
                
                for(Link i : n.getIncoming())
                {
                    if(!i.isCentroidConnector())
                    {
                        double g = g_tot * i.label * i.getCapacity() / vcSum;
                        
                        //System.out.println(g+" "+g_tot);
                        
                        fileout.print((id++) + "\t1\t"+n.getId()+"\t0\t"+(phaseid++)+"\t1\t3\t"+((int)g)+"\t"+(outCount-1));
                        
                        String inc = "";
                        String out = "";
                        
                        for(Link j : n.getOutgoing())
                        {
                            if(!j.isCentroidConnector() && j.getDest() != i.getSource())
                            {
                                inc += i.getId()+",";
                                out += j.getId()+",";
                            }
                        }
                        
                        inc = inc.substring(0, inc.length()-1);
                        out = out.substring(0, out.length()-1);
                        
                        fileout.print("\t{"+inc+"}\t{"+out+"}");
                        fileout.println();
                    }
                }
                
                
                
            }
            
        }
        fileout.close();
    }
    
    public static void transitCount() throws IOException
    {
        DTAProject project = new DTAProject(new File("projects/coacongress2_transit"));
        
        DTASimulator sim = project.getSimulator();
        sim.initialize();
        
        LinkBusRule rule = new LinkBusRule(project, true);
        
        int count1 = 0;
        int count2 = 0;
        
        for(Link l : sim.getLinks())
        {
            if(rule.matches(l, 0))
            {
                if(l.getNumLanes() == 1)
                {
                    count1++;
                }
                else
                {
                    count2++;
                }
            }
        }
        
        System.out.println(count1+" "+count2);
    }
    
    public static void transitTest3() throws IOException
    {
        //String[] projects = new String[]{"coacongress2_CR_transit", "coacongress2_transit", "coacongress2_DTL", "coacongress2_CR_DTL", "coacongress2_TF_DTL"};
        String[] projects = new String[]{"coacongress2_TL", "coacongress2_TF_TL"};
        
        int max_iter = 50;
        double min_gap = 0.1;
        
        for(String x : projects)
        {
            PrintStream fileout = new PrintStream(new FileOutputStream(new File("results_"+x+".txt")), true);
            
            fileout.println("Prop\tDemand\tTSTT\tDA time\tBus FF time\t Bus time\t Avg. bus time\tBus time ratio\tAvg. bus delay");
            
            for(int i = 70; i <= 120; i += 5)
            {
                System.out.println(x+"\t"+i);
                transitTest3(new File("projects/"+x), i, fileout, max_iter, min_gap);
            }
            
            fileout.close();
        }
    }
    
    public static void transitTest3(File file, int prop, PrintStream output, int max_iter, double min_gap) throws IOException
    {
        DTAProject project = new DTAProject(file);
        ReadDTANetwork read = new ReadDTANetwork();
        read.prepareDemand(project, prop/100.0);
        project.loadSimulator();
        
        DTASimulator sim = project.getSimulator();
        sim.initialize();
        
        sim.msa(max_iter, min_gap);
        
        
        
        
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getResultsFolder()+"/log_"+prop+".txt"), true);
        fileout.println("FF time: "+(sim.getBusFFTime()/60));
        fileout.println("Bus time: "+(sim.getBusTT(true)/60));
        fileout.println("Bus ratio: "+(sim.calcAvgBusTimeRatio()));
        fileout.close();
        
        output.println(prop+"\t"+(sim.getNumVehicles()-sim.getNumBuses())+"\t"+(sim.getTSTT()/3600)+"\t"+(sim.getAvgBusTT(false)/60)+"\t"+(sim.getBusFFTime()/60)+"\t"+
                (sim.getBusTT(true)/60)+"\t"+(sim.getAvgBusTT(true)/60)+"\t"+sim.calcAvgBusTimeRatio()+"\t"+sim.calcAvgBusDelay());
        
        sim.printBusTime(new File(project.getResultsFolder()+"/bus_"+prop+".txt"));
        
        project.deleteAssignments();
        
        project = null;
        sim = null;
        read = null;
    }
    
    public static void caccTest2() throws IOException
    {
        DTAProject austinI35 = new DTAProject(new File("projects/austinI35"));
        DTAProject austinI35_CACC = new DTAProject(new File("projects/austinI35_CACC2"));
        
        PrintStream out = new PrintStream(new FileOutputStream(new File("CACC_results.txt")), true);
        
        out.println("% demand\tTSTT\tTSTT w CACC\tHV avg TT w CACC\tCV avg TT w CACC\tDemand");
        
        for(int i = 0; i <= 100; i+= 5)
        {
            Map<Integer, Double> proportions = new HashMap<Integer, Double>();
            proportions.put(ReadDTANetwork.AV+ReadDTANetwork.ICV+ReadDTANetwork.DA_VEHICLE, (double)i/100.0);
            proportions.put(ReadDTANetwork.HV+ReadDTANetwork.ICV+ReadDTANetwork.DA_VEHICLE, (double)(100-i)/100.0);
            
            ReadDTANetwork read1 = new ReadDTANetwork();
            read1.changeType(austinI35, proportions);
            read1.prepareDemand(austinI35, i/85.0);
            austinI35.loadProject();
            
            ReadDTANetwork read2 = new ReadDTANetwork();
            read2.changeType(austinI35_CACC, proportions);
            read2.prepareDemand(austinI35_CACC, i/85.0);
            austinI35_CACC.loadProject();
            
            DTASimulator sim1 = austinI35.getSimulator();
            DTAResults results1 = sim1.msa(2);
            
            DTASimulator sim2 = austinI35_CACC.getSimulator();
            DTAResults results2 = sim2.msa(2);
            
            if(sim1.getNumVehicles() != sim2.getNumVehicles())
            {
                throw new RuntimeException("Demand does not match");
            }
            out.println(i+"\t"+sim1.getTSTT()+"\t"+sim2.getTSTT()+"\t"+sim2.getAvgTT(DriverType.HV_T)+"\t"+sim2.getAvgTT(DriverType.CV_T)+"\t"+sim1.getNumVehicles());
        }
        out.close();
    }
    
    public static void caccTest1() throws IOException
    {
        DTAProject austinI35 = new DTAProject(new File("projects/austinI35"));
        DTAProject austinI35_CACC = new DTAProject(new File("projects/austinI35_CACC"));
        
        PrintStream out = new PrintStream(new FileOutputStream(new File("CACC_results.txt")), true);
        out.println("% demand\tTSTT\tTSTT w CACC\tDemand");
        
        for(int i = 70; i <= 150; i+= 5)
        {
            ReadDTANetwork read1 = new ReadDTANetwork();
            read1.prepareDemand(austinI35, i/100.0);
            austinI35.loadProject();
            
            ReadDTANetwork read2 = new ReadDTANetwork();
            read2.prepareDemand(austinI35_CACC, i/100.0);
            austinI35_CACC.loadProject();
            
            DTASimulator sim1 = austinI35.getSimulator();
            DTAResults results1 = sim1.msa(2);
            
            DTASimulator sim2 = austinI35_CACC.getSimulator();
            DTAResults results2 = sim2.msa(2);
            
            if(sim1.getNumVehicles() != sim2.getNumVehicles())
            {
                throw new RuntimeException("Demand does not match");
            }
            out.println(i+"\t"+sim1.getTSTT()+"\t"+sim2.getTSTT()+"\t"+sim1.getNumVehicles());
        }
        out.close();
    }
    
    public static void transitTest2() throws IOException
    {

        
        PrintStream fileout = new PrintStream(new FileOutputStream(new File("shared_transit3.txt")), true);
        fileout.println("DA rate\tDA TT\tBus TT");
        for(int rate = 100; rate <= 1600; rate += 10)
        {
            double[] temp = transitTest2(rate, 20);
            fileout.println(rate+"\t"+temp[0]+"\t"+temp[1]);
        }
        
        fileout.close();
    }
    public static double[] transitTest2(int DArate, int busRate) throws IOException
    {
        DTAProject project = new DTAProject();
        project.createProject("transit", new File("projects/transit"));
        DTASimulator sim = project.createEmptySimulator();
        
        Set<Node> nodes = new HashSet<Node>();
        Set<Link> links = new HashSet<Link>();
        
        Node n5 = new Intersection(5, new Location(0, 0), new PriorityTBR(new TransitFirst()));
        
        Node n3 = new Intersection(3, new Location(-1, 0), new PriorityTBR(new TransitFirst()));
        Node n1 = new Zone(1, new Location(-2, 0));
        Node t1 = new Zone(201, new Location(-2, 0));
        Node n7 = new Intersection(7, new Location(1, 0), new PriorityTBR(new TransitFirst()));
        Node n9 = new Zone(9, new Location(2, 0));
        Node t9 = new Zone(209, new Location(2, 0));
        
        Node n4 = new Intersection(4, new Location(0, -1), new PriorityTBR(new TransitFirst()));
        Node n2 = new Zone(2, new Location(0, -2));
        Node t2 = new Zone(202, new Location(0, -2));
        Node n6 = new Intersection(6, new Location(0, 1), new PriorityTBR(new TransitFirst()));
        Node n8 = new Zone(8, new Location(0, 2));
        Node t8 = new Zone(208, new Location(0, 2));
        
        nodes.add(n1);
        nodes.add(n2);
        nodes.add(n3);
        nodes.add(n4);
        nodes.add(n5);
        nodes.add(n6);
        nodes.add(n7);
        nodes.add(n8);
        nodes.add(n9);
        nodes.add(t2);
        nodes.add(t8);
        nodes.add(t1);
        nodes.add(t9);
        
        
        Link l13 = new CentroidConnector(13, n1, n3);
        Link l79 = new CentroidConnector(79, n7, n9);
        Link l24 = new CentroidConnector(24, n2, n4);
        Link l68 = new CentroidConnector(68, n6, n8);
        
        Link t13 = new CentroidConnector(213, t1, n3);
        Link t79 = new CentroidConnector(279, n7, t9);
        Link t24 = new CentroidConnector(224, t2, n4);
        Link t68 = new CentroidConnector(268, n6, t8);
        
        TransitLane t35 = new TransitLane(235, n3, n5, 1200, 30, 15, 5280/Vehicle.vehicle_length, 1.0);
        Link l35 = new SharedTransitCTMLink(35, n3, n5, 1200, 30, 15, 5280/Vehicle.vehicle_length, 1.0, 1, t35);
        TransitLane t57 = new TransitLane(257, n5, n7, 1200, 30, 15, 5280/Vehicle.vehicle_length, 1.0);
        Link l57 = new SharedTransitCTMLink(57, n5, n7, 1200, 30, 15, 5280/Vehicle.vehicle_length, 1.0, 1, t57);
        TransitLane t45 = new TransitLane(245, n4, n5, 1200, 30, 15, 5280/Vehicle.vehicle_length, 1.0);
        Link l45 = new SharedTransitCTMLink(45, n4, n5, 1200, 30, 15, 5280/Vehicle.vehicle_length, 1.0, 1, t45);
        TransitLane t56 = new TransitLane(256, n5, n6, 1200, 30, 15, 5280/Vehicle.vehicle_length, 1.0);
        Link l56 = new SharedTransitCTMLink(56, n5, n6, 1200, 30, 15, 5280/Vehicle.vehicle_length, 1.0, 1, t56);
        
        links.add(t13);
        links.add(t79);
        links.add(t24);
        links.add(t68);
        links.add(l13);
        links.add(l79);
        links.add(l24);
        links.add(l68);
        links.add(l35);
        links.add(l57);
        links.add(l45);
        links.add(l56);
        links.add(t35);
        links.add(t57);
        links.add(t45);
        links.add(t56);
        
        sim.setNetwork(nodes, links);
        
        
        
        List<Vehicle> vehicles = new ArrayList<Vehicle>();
        
        int num = (int)(DArate * 60.0/60);
        
        for(int i = 0; i < num; i++)
        {
            vehicles.add(new PersonalVehicle(i+1, n1, n9, (int)(3600.0/num*i)));
        }
        
        num = (int)(DArate * 60.0/60);
        
        for(int i = 0; i < num; i++)
        {
            vehicles.add(new PersonalVehicle(i+1, n2, n8, (int)(3600.0/num*i)));
        }
        
        
        
        num = (int)(busRate * 60.0/60);
        ArrayList<BusLink> stops = new ArrayList<BusLink>();
        stops.add(new BusLink(n2, n8));
        Path path = new Path(t24, t45, t56, t68);
        
        for(int i = 0; i < num; i++)
        {
            vehicles.add(new Bus(100+i+1, 1, (int)(3600.0/num*i), path, stops));
        }
        
        Collections.sort(vehicles);
        
        
        
        
        sim.setVehicles(vehicles);
        
        sim.msa(2);

        RunningAvg busTime = new RunningAvg();
        RunningAvg vehTime = new RunningAvg();
        
        for(Vehicle v : vehicles)
        {
            if(v instanceof Bus)
            {
                busTime.add(v.getTT());
            }
            else
            {
                vehTime.add(v.getTT());
            }
        }
        
        System.out.println("Bus: "+busTime.getAverage());
        System.out.println("DA: "+vehTime.getAverage());
        
        sim.importResults();
        
        return new double[]{vehTime.getAverage(), busTime.getAverage()};
    }
    
    public static void transitTest1() throws IOException
    {

        PrintStream fileout = new PrintStream(new FileOutputStream(new File("shared_transit1.txt")));
        fileout.println("DA rate\tDA TT\tBus TT");
        for(int rate = 1000; rate <= 2200; rate+=10)
        {
            double[] temp = transitTest1(rate, 200);
            fileout.println(rate+"\t"+temp[0]+"\t"+temp[1]);
        }
        
        fileout.close();
        
        fileout = new PrintStream(new FileOutputStream(new File("shared_transit2.txt")));
        fileout.println("bus rate\tDA TT\tBus TT");
        for(int rate = 0; rate <= 600; rate+=10)
        {
            double[] temp = transitTest1(1800, rate);
            fileout.println(rate+"\t"+temp[0]+"\t"+temp[1]);
        }
        
        fileout.close();
        
    }
    
    public static double[] transitTest1(int DArate, int busRate) throws IOException
    {
        DTAProject project = new DTAProject();
        project.createProject("transit", new File("projects/transit"));
        DTASimulator sim = project.createEmptySimulator();
        
        Set<Node> nodes = new HashSet<Node>();
        Set<Link> links = new HashSet<Link>();
        
        
        Node n1 = new Zone(1);
        Node n2 = new Intersection(2, new PriorityTBR());
        Node n3 = new Intersection(3, new PriorityTBR());
        Node n4 = new Zone(4);
        Node n1a = new Zone(10);
        
        nodes.add(n1);
        nodes.add(n2);
        nodes.add(n3);
        nodes.add(n4);
        nodes.add(n1a);
        
        Link l12 = new CentroidConnector(12, n1, n2);
        Link l12a = new CentroidConnector(110, n1a, n2);
        TransitLane t23 = new TransitLane(230, n2, n3, 1200, 30, 15, 5280/Vehicle.vehicle_length, 2.0);
        CTMLink l23 = new SharedTransitCTMLink(23, n2, n3, 1200, 30, 15, 5280.0/Vehicle.vehicle_length, 2.0, 1, t23);
        Link l34 = new CentroidConnector(34, n3, n4);
        
        links.add(l12);
        links.add(t23);
        links.add(l23);
        links.add(l34);
        links.add(l12a);
        
        sim.setNetwork(nodes, links);
        
        
        
        List<Vehicle> vehicles = new ArrayList<Vehicle>();
        
        int num = (int)(DArate * 60.0/60);
        
        for(int i = 0; i < num; i++)
        {
            vehicles.add(new PersonalVehicle(i+1, n1, n4, (int)(3600.0/num*i)));
        }
        
        
        
        num = (int)(busRate * 60.0/60);
        ArrayList<BusLink> stops = new ArrayList<BusLink>();
        stops.add(new BusLink(n1a, n4));
        Path path = new Path(l12a, t23, l34);
        
        for(int i = 0; i < num; i++)
        {
            vehicles.add(new Bus(100+i+1, 1, (int)(3600.0/num*i), path, stops));
        }
        
        Collections.sort(vehicles);
        
        
        
        
        sim.setVehicles(vehicles);
        
        sim.msa(2);

        RunningAvg busTime = new RunningAvg();
        RunningAvg vehTime = new RunningAvg();
        
        for(Vehicle v : vehicles)
        {
            if(v instanceof Bus)
            {
                busTime.add(v.getTT());
            }
            else
            {
                vehTime.add(v.getTT());
            }
        }
        
        System.out.println("Bus: "+busTime.getAverage());
        System.out.println("DA: "+vehTime.getAverage());
        
        sim.importResults();
        
        return new double[]{vehTime.getAverage(), busTime.getAverage()};
    }
}
