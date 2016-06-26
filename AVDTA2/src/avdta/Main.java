/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta;

import avdta.network.RunningAvg;
import avdta.network.Simulator;
import avdta.network.ReadNetwork;
import avdta.cost.TTCost;
import avdta.gui.GUI;
import avdta.network.link.CentroidConnector;
import avdta.network.link.LTMLink;
import avdta.network.link.Cell;
import avdta.network.link.CTMLink;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.network.Path;
import avdta.network.node.Location; 
import avdta.network.node.Zone;
import avdta.network.node.P0Obj;
import avdta.network.node.MaxFlowObj;
import avdta.network.node.MCKSTBR;
import avdta.network.node.MCKSPriority;
import avdta.network.node.Turn;
import avdta.vehicle.PersonalVehicle;
import avdta.vehicle.Vehicle;
import avdta.vehicle.DriverType;
import avdta.vehicle.FairWallet;
import avdta.network.node.Diverge;
import avdta.network.node.Intersection;
import avdta.network.node.Merge;
import avdta.network.node.BadEnergyObj;
import avdta.network.node.IntersectionControl;
import avdta.intersection.candidate.CandidatesTBR;
import avdta.network.node.BackPressureObj;
import avdta.network.node.EnergyObj;
import avdta.network.node.IntersectionPolicy;
import avdta.network.node.PriorityTBR;
import avdta.network.node.Phase;
import avdta.network.node.StopSign;
import avdta.network.node.TrafficSignal;
import avdta.microtoll.MTMain;
import avdta.moves.EvaluateLinks;
import avdta.sav.SAVMain;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author ut
 */
public class Main
{
    public static void main(String[] args) throws IOException
    {
        //System.setOut(new PrintStream(new FileOutputStream(new File("results/log.txt")), true));
        //test_phasedTBR("training_scenario2");
        //test_phasedTBR("nicosia");
        
        
        //MTMain.main(args);
        //testMoves(0, false);
        //testMoves(100, false);
        //testMoves(100, true);
        //testMoves(25, false);
        //testMoves(50, false);
        //testMoves(75, false);
        
        GUI.main(args);
        
        
        //test_FCFS("coacongress2");
        
        
        
        
        
        
        
        //CLI();
        //test_FCFS("coacongress2");

                
        //testCandidateSolutions(new int[]{6, 6, 6, 6}, new int[]{2, 2, 2, 2}, new int[]{1800, 1800, 1800, 1800}, new int[]{30, 30, 30, 30}, 0.1, 0.2);
        
        //GraphicalCLI();
        //Fix("dallas_hwy75");
        //Fix("dallas_oakLawn");
        
        
        //test_signals("coacongress2", "coacongress2_signals", "demand.txt", 0);
        //test_TBR("coacongress2", "coacongress2_TBR", "demand.txt", 1);
        
        //SAVMain.main(args);
    }
    
    public static void testMoves(int AV_proportion, boolean reservations) throws IOException
    {
        String network = "coacongress";
        int linktype = Link.CTM;

        
        PrintStream out;
        
        Simulator sim;
        
        if(reservations)
        {
            sim = Simulator.readTBRNetwork(network, IntersectionPolicy.FCFS, Node.CR, linktype);
        }
        else
        {
            sim = Simulator.readSignalsNetwork(network, linktype);
        }
        
        ReadNetwork.readGrade(sim);
        sim.setScenario((reservations?"FCFS":"signals")+"_"+AV_proportion);
        
        
        for(Vehicle v : sim.getVehicles())
        {
            if(Math.random() < AV_proportion)
            {
                v.setDriver(DriverType.AV);
            }
            else
            {
                v.setDriver(DriverType.HV);
            }
        }

        int max_iter = 100;
        double min_gap = 2;
        
        sim.msa(max_iter, min_gap);
        
        sim.simulate(true);
        sim.importResults();
        sim.writeVehicleResults();
        
        EvaluateLinks test = new EvaluateLinks();
        test.calculate(sim);
        test.printLinkSource();
        test.printLink();
        
        test.parseCellEnter(sim);
        
    }
    
    public static void Fix(String name)throws IOException
    {
        System.out.println("Results for network: "+name);
        //ConvertVISTAoutput.convert(name);
        //test_VISTA(name);
        
        //test_signals("lamar_38", "lamar_38_signals", "demand_100.txt", 0);
        //first: data/folder/
        //second: results/folder/
        //third: data/folder/demand_100.txt
        test_signals(name, name, "demand.txt", 0);
        //test_TBR("lamar_38", "lamar_38_TBR", "demand.txt", 1);
    }
    
    
    
    public static void printTestNetwork() throws IOException
    {
        int idx = 0;
        Node n1 = new Intersection(++idx, new PriorityTBR());
        Node n2 = new Intersection(++idx, new PriorityTBR());
        Node n3 = new Intersection(++idx, new PriorityTBR());
        Node n4 = new Intersection(++idx, new PriorityTBR());
        Node n5 = new Intersection(++idx, new PriorityTBR());
        Node n6 = new Intersection(++idx, new PriorityTBR());
        Node n7 = new Intersection(++idx, new PriorityTBR());
        Node n8 = new Intersection(++idx, new PriorityTBR());
        Node n9 = new Intersection(++idx, new PriorityTBR());
        
        Node nA = new Intersection(++idx, new PriorityTBR());
        Node nB = new Intersection(++idx, new PriorityTBR());
        Node nC = new Intersection(++idx, new PriorityTBR());
        Node nD = new Intersection(++idx, new PriorityTBR());
        
        
        ArrayList<Node> nodes = new ArrayList<Node>();
        
        nodes.add(n1);
        nodes.add(n2);
        nodes.add(n3);
        nodes.add(n4);
        nodes.add(n5);
        nodes.add(n6);
        nodes.add(n7);
        nodes.add(n8);
        nodes.add(n9);
        nodes.add(nA);
        nodes.add(nB);
        nodes.add(nC);
        nodes.add(nD);
        
        idx = 0;
        double length = 0.2;
        CTMLink l12 = new CTMLink(++idx, n1, n2, 1200, 30, 15, 290, length, 2);
        CTMLink l21 = new CTMLink(++idx, n2, n1, 1200, 30, 15, 290, length, 2);
        CTMLink l23 = new CTMLink(++idx, n2, n3, 1200, 30, 15, 290, length, 2);
        CTMLink l32 = new CTMLink(++idx, n3, n2, 1200, 30, 15, 290, length, 2);
        CTMLink l14 = new CTMLink(++idx, n1, n4, 1200, 30, 15, 290, length, 2);
        CTMLink l41 = new CTMLink(++idx, n4, n1, 1200, 30, 15, 290, length, 2);
        CTMLink l25 = new CTMLink(++idx, n2, n5, 1200, 30, 15, 290, length, 2);
        CTMLink l52 = new CTMLink(++idx, n5, n2, 1200, 30, 15, 290, length, 2);
        CTMLink l36 = new CTMLink(++idx, n3, n6, 1200, 30, 15, 290, length, 2);
        CTMLink l63 = new CTMLink(++idx, n6, n3, 1200, 30, 15, 290, length, 2);
        CTMLink l45 = new CTMLink(++idx, n4, n5, 1200, 30, 15, 290, length, 2);
        CTMLink l54 = new CTMLink(++idx, n5, n4, 1200, 30, 15, 290, length, 2);
        CTMLink l56 = new CTMLink(++idx, n5, n6, 1200, 30, 15, 290, length, 2);
        CTMLink l65 = new CTMLink(++idx, n6, n5, 1200, 30, 15, 290, length, 2);
        CTMLink l47 = new CTMLink(++idx, n4, n7, 1200, 30, 15, 290, length, 2);
        CTMLink l74 = new CTMLink(++idx, n7, n4, 1200, 30, 15, 290, length, 2);
        CTMLink l58 = new CTMLink(++idx, n5, n8, 1200, 30, 15, 290, length, 2);
        CTMLink l85 = new CTMLink(++idx, n8, n5, 1200, 30, 15, 290, length, 2);
        CTMLink l69 = new CTMLink(++idx, n6, n9, 1200, 30, 15, 290, length, 2);
        CTMLink l96 = new CTMLink(++idx, n9, n6, 1200, 30, 15, 290, length, 2);
        CTMLink l78 = new CTMLink(++idx, n7, n8, 1200, 30, 15, 290, length, 2);
        CTMLink l87 = new CTMLink(++idx, n8, n7, 1200, 30, 15, 290, length, 2);
        CTMLink l89 = new CTMLink(++idx, n8, n9, 1200, 30, 15, 290, length, 2);
        CTMLink l98 = new CTMLink(++idx, n9, n8, 1200, 30, 15, 290, length, 2);
        
        
        CTMLink lA2 = new CTMLink(++idx, nA, n2, 7200, 30, 15, 290, 0.1, 1);
        CTMLink l2A = new CTMLink(++idx, n2, nA, 7200, 30, 15, 290, 0.1, 1);
        CTMLink lB4 = new CTMLink(++idx, nB, n4, 7200, 30, 15, 290, 0.1, 1);
        CTMLink l4B = new CTMLink(++idx, n4, nB, 7200, 30, 15, 290, 0.1, 1);
        CTMLink l6C = new CTMLink(++idx, n6, nC, 7200, 30, 15, 290, 0.1, 1);
        CTMLink lC6 = new CTMLink(++idx, nC, n6, 7200, 30, 15, 290, 0.1, 1);
        CTMLink l8D = new CTMLink(++idx, n8, nD, 7200, 30, 15, 290, 0.1, 1);
        CTMLink lD8 = new CTMLink(++idx, nD, n8, 7200, 30, 15, 290, 0.1, 1);
        
        ArrayList<Link> links = new ArrayList<Link>();
        links.add(l12);
        links.add(l21);
        links.add(l23);
        links.add(l32);
        links.add(l14);
        links.add(l41);
        links.add(l25);
        links.add(l52);
        links.add(l36);
        links.add(l63);
        links.add(l45);
        links.add(l54);
        links.add(l56);
        links.add(l65);
        links.add(l47);
        links.add(l74);
        links.add(l58);
        links.add(l85);
        links.add(l69);
        links.add(l96);
        links.add(l78);
        links.add(l87);
        links.add(l89);
        links.add(l98);
        links.add(lA2);
        links.add(l2A);
        links.add(lB4);
        links.add(l4B);
        links.add(l6C);
        links.add(lC6);
        links.add(l8D);
        links.add(lD8);
        
        Simulator test = new Simulator("test", nodes, links, Link.CTM);
        test.initialize();
        test.setDLR(true);
        
        PrintStream fileout = new PrintStream(new FileOutputStream(new File("test_model.dat")), true);
        
        int max_t = 60;
        
        fileout.print("set T :=");
        for(int t = 0; t < max_t; t++)
        {
            fileout.print(" "+t);
        }
        fileout.println(";\n");
        
        Set<Cell> cells = new TreeSet<Cell>();
        
        for(Link l : links)
        {
            CTMLink link = (CTMLink)l;
            
            for(Cell c : link.getCells())
            {
                cells.add(c);
            }
        }
        
        fileout.print("set C:=");
        for(Cell c : cells)
        {
            fileout.print(" "+c.getId());
        }
        
        fileout.println(";");
        fileout.println();
        
        fileout.print("set Cr :=");
        fileout.print(" "+lA2.getCells()[0].getId());
        fileout.print(" "+lD8.getCells()[0].getId());
        fileout.print(" "+lC6.getCells()[0].getId());
        fileout.print(" "+lB4.getCells()[0].getId());
        fileout.println(";");
        
        fileout.print("set Cs :=");
        fileout.print(" "+l2A.getCells()[1].getId());
        fileout.print(" "+l8D.getCells()[1].getId());
        fileout.print(" "+l6C.getCells()[1].getId());
        fileout.print(" "+l4B.getCells()[1].getId());
        fileout.println(";");
        
        fileout.println();
        
        String temp = "";
        for(Cell lhs : cells)
        {
            Cell rhs = lhs.getOpposite();
            
            if(lhs.getId() < rhs.getId())
            {
                temp += "("+lhs.getId()+","+rhs.getId()+"),";
            }
        }
        
        fileout.println("set Opposite = "+temp.substring(0, temp.length()-1)+";");
        fileout.println();
        fileout.println();
        
        Path AD = new Path(lA2, l25, l58, l8D);
        Path DA = new Path(lD8, l85, l52, l2A);
        Path BC = new Path(lB4, l45, l56, l6C);
        Path CB = new Path(lC6, l65, l54, l4B);
        
        int path_idx = 1;
        
        fileout.println("#A-D");
        fileout.print("set paths["+(path_idx++)+"] :=");
        for(Link l : AD)
        {
            for(Cell c : ((CTMLink)l).getCells())
            {
                fileout.print(" "+c.getId());
            }
        }
        fileout.println(";");
        
        fileout.println("#D-A");
        fileout.print("set paths["+(path_idx++)+"] :=");
        for(Link l : DA)
        {
            for(Cell c : ((CTMLink)l).getCells())
            {
                fileout.print(" "+c.getId());
            }
        }
        fileout.println(";");
        
        fileout.println("#B-C");
        fileout.print("set paths["+(path_idx++)+"] :=");
        for(Link l : BC)
        {
            for(Cell c : ((CTMLink)l).getCells())
            {
                fileout.print(" "+c.getId());
            }
        }
        fileout.println(";");
        
        fileout.println("#C-B");
        fileout.print("set paths["+(path_idx++)+"] :=");
        for(Link l : CB)
        {
            for(Cell c : ((CTMLink)l).getCells())
            {
                fileout.print(" "+c.getId());
            }
        }
        fileout.println(";");
        
        fileout.println();
        
        for(Link l : links)
        {
            CTMLink link = (CTMLink)l;
            
            Cell[] c = link.getCells();
            
            for(int i = 0; i < c.length-1; i++)
            {
                fileout.println("set Succ["+c[i].getId()+"] := "+c[i+1].getId()+";");
            }
            
            
            temp = "";
            for(Link j : link.getDest().getOutgoing())
            {
                if(j.getDest() == l.getSource())
                {
                    continue;
                }
                
                CTMLink j_ = (CTMLink)j;
                temp +=(" "+j_.getCells()[0].getId());
            }
            
            //if(temp.length() > 0)
            {
                fileout.println("set Succ["+c[c.length-1].getId()+"] :="+temp+";");
            }
        }
        fileout.println();
        
        
        for(Link l : links)
        {
            CTMLink link = (CTMLink)l;
            
            Cell[] c = link.getCells();
            

            temp = "";
            for(Link i : link.getSource().getIncoming())
            {
                if(i.getSource() == l.getDest())
                {
                    continue;
                }
                
                CTMLink i_ = (CTMLink)i;
                Cell[] c_i = i_.getCells();
                
                temp += (" "+c_i[c_i.length-1].getId());
            }
            
            //if(temp.length() > 0)
            {
                fileout.println("set Pred["+c[0].getId()+"] :="+temp+";");
            }
            
            for(int i = 1; i < c.length; i++)
            {
                fileout.println("set Pred["+c[i].getId()+"] := "+c[i-1].getId()+";");
            }
        }
        fileout.println("\n");
        
        fileout.println("param total_lanes :=");
        
        for(Cell lhs : cells)
        {
            Cell rhs = lhs.getOpposite();
            
            if(lhs.getId() < rhs.getId())
            {
                fileout.println(lhs.getId()+" "+rhs.getId()+" "+(lhs.getLink().getNumLanes()+rhs.getLink().getNumLanes()));
            }
        }
        
        fileout.println(";");
        fileout.println();
        
        
        
        fileout.println("param N :=");
        
        for(Cell c : cells)
        {
            fileout.println(c.getId()+" "+c.getLink().getCellJamdPerLane());
        }
        fileout.println(";");
        fileout.println();
        
        fileout.println("param Q := ");
        for(Cell c : cells)
        {
            fileout.println(c.getId()+" "+c.getLink().getCellCapacityPerLane());
        }
        fileout.println(";");
        fileout.println();
        
        fileout.println("param Dem :=");
        fileout.println("#A-D");
        fileout.println("[*,"+lA2.getFirstCell().getId()+","+l8D.getLastCell().getId()+"]");
        for(int t = 1; t <= max_t; t++)
        {
            fileout.println(t+" 0");
        }
        fileout.println();
        fileout.println("#D-A");
        fileout.println("[*,"+lD8.getFirstCell().getId()+","+l2A.getLastCell().getId()+"]");
        for(int t = 1; t <= max_t; t++)
        {
            fileout.println(t+" 0");
        }
        fileout.println();
        fileout.println("#B-C");
        fileout.println("[*,"+lB4.getFirstCell().getId()+","+l6C.getLastCell().getId()+"]");
        for(int t = 1; t <= max_t; t++)
        {
            fileout.println(t+" 0");
        }
        fileout.println();
        fileout.println("#C-B");
        fileout.println("[*,"+lC6.getFirstCell().getId()+","+l4B.getLastCell().getId()+"]");
        for(int t = 1; t <= max_t; t++)
        {
            fileout.println(t+" 0");
        }
        fileout.println();
        
        
        fileout.close();
    }
    
    public static void test_signals(String network, String scenario, String demandfile, double AV_proportion) throws IOException
    {
        int max_iter = 3;

        Simulator test = Simulator.readSignalsNetwork(network, Link.CTM, demandfile);
        test.setScenario(scenario);
        int count = 0;                      //Added by Sudesh; can be removed later
        for(Vehicle v : test.getVehicles())
        {
            if(Math.random() < AV_proportion)
            {
                v.setDriver(DriverType.AV);
            }
            else
            {
                v.setDriver(DriverType.HV);
            }
            /*
            if(v.isExited())
            {
                count++;
            }
            else
            {
                System.out.println("Vehicle "+v.getId()+" did not exit.");
            }
            */
        }

        //test.partial_demand(5);
        //test.msa_cont(5, max_iter);
        test.msa(max_iter, 1);

        test.printLinkTT(0, 10800, new File("results/"+scenario+"/linktt.txt"));
        test.printLinkFlow(0, 10800, new File("results/"+scenario+"/linkq.txt"));
        test.importResults();
        test.printIntersectionDemands();
    }
    
    public static void test_TBR(String network, String scenario, String demandfile, double AV_proportion) throws IOException
    {
        int max_iter = 50;

        Simulator test = Simulator.readTBRNetwork(network, Link.CTM, demandfile);
        test.setScenario(scenario);
        
        for(Vehicle v : test.getVehicles())
        {
            if(Math.random() < AV_proportion)
            {
                v.setDriver(DriverType.AV);
            }
            else
            {
                v.setDriver(DriverType.HV);
            }
        }

        test.msa(max_iter, 2);

        test.printLinkTT(0, 10800, new File("results/"+scenario+"/linktt.txt"));
        test.printLinkFlow(0, 10800, new File("results/"+scenario+"/linkq.txt"));
        test.importResults();
        
        System.out.println("Energy: "+test.getTotalEnergy());
        System.out.println("VMT: "+test.getTotalVMT());
    }
    

    
    
    
    public static void example_breakFCFS() throws IOException
    {
        Node R = new Zone(1, new Location(0, 0));
        Node A = new Intersection(1, new Location(0, 0), new PriorityTBR());
        Node B = new Intersection(2, new Location(1, 0), new PriorityTBR());
        
        boolean FCFS = true;
        Node C;
        
        TrafficSignal signal = new TrafficSignal();
        
        if(FCFS)
        {
            C = new Intersection(3, new Location(3, 0), new PriorityTBR());
        }
        else
        {
            C = new Intersection(3, new Location(3, 0), signal);
        }

        Node D = new Intersection(4, new Location(4, 0), new PriorityTBR());
        Node E = new Zone(5, new Location(5, 0));
        
        Link RA = new CentroidConnector(0, R, A);
        Link AB = new LTMLink(1, A, B, 1200, 30, 15, 290, 1.5, 3);
        Link BC1 = new LTMLink(2, B, C, 1200, 30, 15, 290, 0.5, 1);
        Link BC2 = new LTMLink(3, B, C, 1200, 30, 15, 290, 1.5, 3);
        Link CD = new LTMLink(4, C, D, 1200, 30, 15, 120, 0.5, 3);
        Link DE = new CentroidConnector(5, D, E);
        
        signal.addPhase(new Phase(new Turn[]{new Turn(BC2, CD)}, 110, 110));
        signal.addPhase(new Phase(new Turn[]{new Turn(BC1, CD)}, 10, 10));
        
        List<Link> links = new ArrayList<Link>();
        List<Node> nodes = new ArrayList<Node>();
        
        nodes.add(A);
        nodes.add(B);
        nodes.add(C);
        nodes.add(D);
        nodes.add(E);
        
        links.add(AB);
        links.add(BC1);
        links.add(BC2);
        links.add(CD);
        links.add(DE);

        Simulator test;
        
        test = new Simulator("test", nodes, links, Link.LTM);
        

        List<PersonalVehicle> vehicles = new ArrayList<PersonalVehicle>();

        int veh_id = 0;
        


        double duration = 3600;

        int demand = 3000;

        for(int j = 0; j < demand*duration/3600.0; j++)
        {
            vehicles.add(new PersonalVehicle(j, A, E, (int)(Math.random()*duration)));
        }
        

        test.setVehicles(vehicles);
        test.msa(10);
        test.importResults();
        
        System.out.println(BC1.getAvgTT(1800)/60);
        System.out.println(BC2.getAvgTT(1800)/60);
        
        int count1 = 0;
        int count2 = 0;
        
        for(Vehicle v : vehicles)
        {
            if(v.getPath().contains(BC1))
            {
                count1++;
            }
            else
            {
                count2++;
            }
        }
        
        System.out.println(count1+" "+count2);
        test.printLinkTT(0, 3600, new File("test.txt"));
        
    }
    
    
    public static void test_mixedSignalsTBR(String network) throws IOException
    {
        int linktype = Link.CTM;
        int max_iter = 3;
        double min_gap = 1;
        
        
        
        Simulator test = Simulator.readTBRNetwork(network, null, Node.MIX_SIGNAL_TBR, linktype);
        test.setScenario("Mix");
        
        test.msa(max_iter, min_gap);
        test.importResults();
        test.writeVehicleResults();
        
        
        System.out.println(test.getTSTT()/3600.0);
    }

    public static void test_phasedTBR(String network) throws IOException
    {
        int linktype = Link.CTM;
        
        long time = System.nanoTime();
        
        PrintStream out;
        
        int max_iter = 3;
        double min_gap = 1;
        
        
        Simulator test_weighted = Simulator.readTBRNetwork(network, null, Node.WEIGHTED_TBR, linktype);
        test_weighted.setScenario("Weighted TBR");
        
        test_weighted.msa(max_iter, min_gap);
        test_weighted.importResults();
        test_weighted.writeVehicleResults();
        
        double weighted_tt = test_weighted.getTSTT()/3600.0;
        
        test_weighted = null;
        
        Simulator test_phased = Simulator.readTBRNetwork(network, null, Node.PHASED_TBR, linktype);
        test_phased.setScenario("Phased TBR");
        
        test_phased.msa(max_iter, min_gap);
        test_phased.importResults();
        test_phased.writeVehicleResults();
        
        double phased_tt = test_phased.getTSTT()/3600.0;
        
        test_phased = null;
        
        Simulator test_FCFS = Simulator.readTBRNetwork(network, IntersectionPolicy.FCFS, Node.CR, linktype);
        test_FCFS.setScenario("FCFS");
        

        
        test_FCFS.msa(max_iter, min_gap);
        test_FCFS.importResults();
        test_FCFS.writeVehicleResults();
        
        double fcfs_tt = test_FCFS.getTSTT()/3600.0;
        
        test_FCFS = null;

        
        
        
        
        
        Simulator test_signals = Simulator.readSignalsNetwork(network, linktype);
        test_signals.setScenario("Signals");
        
        test_signals.msa(max_iter, min_gap);
        test_signals.importResults();
        test_signals.writeVehicleResults();
        
        
        
        
        double signals_tt = test_signals.getTSTT()/3600.0;
        
        test_signals = null;
        
        System.out.println(network);
        System.out.println("FCFS: "+fcfs_tt);
        System.out.println("Signals: "+signals_tt);
        System.out.println("Weighted: "+weighted_tt);
        System.out.println("Phased: "+phased_tt);
        
        /*
        List<PersonalVehicle> v_fcfs = test_FCFS.getVehicles();
        List<PersonalVehicle> v_signals= test_signals.getVehicles();
        
        Collections.sort(v_fcfs, new Comparator<PersonalVehicle>()
        {
            public int compare(PersonalVehicle lhs, PersonalVehicle rhs)
            {
                return lhs.getId() - rhs.getId();
            }
        });
        
        Collections.sort(v_signals, new Comparator<PersonalVehicle>()
        {
            public int compare(PersonalVehicle lhs, PersonalVehicle rhs)
            {
                return lhs.getId() - rhs.getId();
            }
        });
        
        System.out.println("Id\tOrigin\tDest\tEnter\tExit_FCFS\tExit_signals");
        for(int i = 0; i < v_fcfs.size(); i++)
        {
            PersonalVehicle v = v_fcfs.get(i);
            PersonalVehicle v2 = v_signals.get(i);
            
            System.out.println(v.getId()+"\t"+v.getOrigin()+"\t"+v.getDest()+"\t"+v.getDepTime()+"\t"+v.getExitTime()+"\t"+v2.getExitTime());
        }
        */
    }
    
    
    public static void test_FCFS(String network) throws IOException
    {
        int linktype = Link.CTM;
        
        long time = System.nanoTime();
        
        PrintStream out;
        
        Simulator test_FCFS = Simulator.readTBRNetwork(network, IntersectionPolicy.FCFS, Node.CR, linktype);
        test_FCFS.setScenario("FCFS");
        
        int max_iter = 200;
        double min_gap = 1;
        
        Simulator test_signals = Simulator.readSignalsNetwork(network, linktype);
        test_signals.setScenario("Signals");
        
        test_signals.msa(max_iter, min_gap);
        test_signals.importResults();
        test_signals.writeVehicleResults();
        
        double signals_tt = test_signals.getTSTT();
        
        System.out.println(Simulator.duration);
        test_FCFS.msa(max_iter, min_gap);
        test_FCFS.importResults();
        test_FCFS.writeVehicleResults();
        
        double fcfs_tt = test_FCFS.getTSTT();
        
        
        
        
        System.out.println(network);
        System.out.println("FCFS: "+fcfs_tt);
        System.out.println("Signals: "+signals_tt);
    }
    
    public static void test_DLR_net(String network, boolean writeToFile) throws IOException
    {
        int linktype = Link.CTM;
        
        long time = System.nanoTime();
        
        double[][] results = new double[3][2];
        int max_iter = 80;
        double min_gap = 2;
        
        Simulator test_FCFS = Simulator.readTBRNetwork(network, IntersectionPolicy.FCFS, Node.CR, linktype);
        test_FCFS.setScenario("fixed");

        PrintStream out;
        
        if(writeToFile)
        {
            out = new PrintStream(new FileOutputStream("log_"+network+".txt"), true);
            test_FCFS.setOutStream(out);
        }
        else
        {
            out = System.out;
        }
        
        
        //test_FCFS.msa(max_iter, min_gap);
        //test_FCFS.writeVehicles();
        
        results[0][0] = test_FCFS.getTSTT();
        results[0][1] = test_FCFS.getAvgMPG();
        
        test_FCFS = null;
        
        Simulator.setDLR(true);
        Simulator test_DLR = Simulator.readTBRNetwork(network, IntersectionPolicy.FCFS, Node.CR, linktype);
        test_DLR.setScenario("DLR");

        if(writeToFile)
        {
            test_DLR.setOutStream(out);
        }
        
        test_DLR.msa(max_iter, min_gap);    
        test_DLR.writeVehicles();
        
        results[1][0] = test_DLR.getTSTT();
        results[1][1] = test_DLR.getAvgMPG();
        
        test_DLR = null;
        
        
        out.println("\n\n\n***** Results for "+network+" *****");
        out.println("Obj.\tTSTT\tMPG");
        out.println("Fixed\t" + String.format("%.2f", (results[0][0] / 3600.0))+" "+String.format("%.2f", results[0][1]));
        out.println("DLR\t" + String.format("%.2f", (results[1][0] / 3600.0))+" "+String.format("%.2f", results[1][1]));
        
        if(writeToFile)
        {
            out.close();
        }
        
        time = System.nanoTime() - time;
        System.out.println("Finished "+network+" "+String.format("%.2f", time/1.0e9 / 60)+" min");
    }
    
    public static void test_DLR() throws IOException
    {
        int demand1 = 3300;
        int demand2 = 1100;
        
        DLR_scenario(demand1, demand2, false);
        DLR_scenario(demand1, demand2, true);
        
    }
    
    public static void DLR_scenario(int demand1, int demand2, boolean dlr) throws IOException
    {
        Zone n1 = new Zone(1, new Location(-3, 0));
        Zone n2 = new Zone(2, new Location(3, 0));
        
        Node n3 = new Intersection(3, new Location(-2, 0), new PriorityTBR());
        Node n4 = new Intersection(4, new Location(2, 0), new PriorityTBR());
        Node n5 = new Intersection(5, new Location(0, 0), new PriorityTBR());
        
        CTMLink l35 = new CTMLink(35, n3, n5, 1200, 30, 15, 120, 1, 2);
        CTMLink l54 = new CTMLink(54, n5, n4, 1200, 30, 15, 120, 1, 2);
        CTMLink l53 = new CTMLink(53, n5, n3, 1200, 30, 15, 120, 1, 2);
        CTMLink l45 = new CTMLink(45, n4, n5, 1200, 30, 15, 120, 1, 2);
        
        
        Link l13 = new CentroidConnector(13, n1, n3);
        Link l31 = new CentroidConnector(31, n3, n1);
        Link l42 = new CentroidConnector(42, n4, n2);
        Link l24 = new CentroidConnector(24, n2, n4);
        
        List<Link> links = new ArrayList<Link>();
        List<Node> nodes = new ArrayList<Node>();
        
        nodes.add(n1);
        nodes.add(n2);
        nodes.add(n3);
        nodes.add(n4);
        nodes.add(n5);
        
        links.add(l13);
        links.add(l31);
        links.add(l42);
        links.add(l24);
        links.add(l35);
        links.add(l53);
        links.add(l45);
        links.add(l54);
        
        double duration = 3600;

        
        int veh_id = 0;
        
        Simulator test1 = new Simulator("test", nodes, links, Link.CTM);
        test1.setDLR(dlr);
        System.out.println("DLR: "+dlr);

        
        List<PersonalVehicle> vehicles = new ArrayList<PersonalVehicle>();
        
        for(int j = 0; j < demand1*duration/3600; j++)
        {
            ++ veh_id;
            vehicles.add(new PersonalVehicle(veh_id, n1, n2, (int)(Math.random()*duration)));
        }
        
        for(int j = 0; j < demand2*duration/3600; j++)
        {
            ++ veh_id;
            vehicles.add(new PersonalVehicle(veh_id, n2, n1, (int)(Math.random()*duration)));
        }
        
        test1.setVehicles(vehicles);
        
        test1.msa(3);

        System.out.println("Avg. TT:\t"+String.format("%.1f", test1.getTSTT() / vehicles.size() / 60.0));
        System.out.println("Min. tt:\t"+((l35.getFFTime()+l54.getFFTime())) / 60);
    }
    
    public static void test_network_energy(String network) throws IOException
    {
        long time = System.nanoTime();
        
        double[][] results = new double[3][3];
        int max_iter = 200;
        double min_gap = 1;
        
        Simulator test_FCFS = Simulator.readTBRNetwork(network, new BadEnergyObj(), Node.MCKS, Link.CTM);
        test_FCFS.setScenario("bad_energy");


        //test_FCFS.msa(max_iter, min_gap);
        //test_FCFS.importResults();
        
        //results[0][0] = test_FCFS.getTSTT();
        //results[0][1] = test_FCFS.getAvgMPG();
        //results[0][2] = test_FCFS.getTotalEnergy();
        
        test_FCFS = null;
        
        Simulator test_MCKS = Simulator.readTBRNetwork(network, new EnergyObj(), Node.MCKS, Link.CTM);
        test_MCKS.setScenario("good_energy");
        

        test_MCKS.msa(max_iter, min_gap);
        test_MCKS.importResults();
        
        results[1][0] = test_MCKS.getTSTT();
        results[1][1] = test_MCKS.getAvgMPG();
        results[1][2] = test_MCKS.getTotalEnergy();
        
        test_MCKS = null;
        
        Simulator test_MCKS2 = Simulator.readTBRNetwork(network, new MaxFlowObj(1), Node.MCKS, Link.CTM);
        test_MCKS2.setScenario("max_flow");
        
        
        test_MCKS2.msa(max_iter, min_gap);
        test_MCKS2.importResults();
        
        results[2][0] = test_MCKS2.getTSTT();
        results[2][1] = test_MCKS2.getAvgMPG();
        results[2][2] = test_MCKS2.getTotalEnergy();
        
        test_MCKS2 = null;
        
        
        System.out.println("\n\n\n***** Results for "+network+" *****");
        System.out.println("Obj.\tTSTT\tMPG\tEnergy");
        System.out.println("Bad-energy\t" + String.format("%.2f", (results[0][0] / 3600.0))+"\t"+String.format("%.2f", results[0][1])+"\t"+String.format("%.2f", results[0]
[2]));
        System.out.println("Good energy 1\t" + String.format("%.2f", (results[1][0] / 3600.0))+"\t"+String.format("%.2f", results[1][1])+"\t"+String.format("%.2f", results[1]
[2]));
        System.out.println("Max flow\t" + String.format("%.2f", (results[2][0] / 3600.0))+"\t"+String.format("%.2f", results[2][1])+"\t"+String.format("%.2f", results[2]
[2]));
        
        time = System.nanoTime() - time;
        System.out.println("Finished "+network+" "+String.format("%.2f", time/1.0e9 / 60)+" min");
    }
            
    public static void test_cells_austin() throws IOException
    {
        int max_iter = 20;

        String network = "coacongress";

        for(int x = 100; x >= 100; x -= 10)
        {
            double AV_proportion = x / 100.0;

            Simulator test2 = Simulator.readTBRNetwork(network, IntersectionPolicy.FCFS, Node.CR, Link.LTM);
            //ReadNetwork.readGrade(test2, new File("data/elevation_"+network+".txt"));
            
            for(Vehicle v : test2.getVehicles())
            {
                if(Math.random() < AV_proportion)
                {
                    v.setDriver(DriverType.AV);
                }
                else
                {
                    v.setDriver(DriverType.HV);
                }
            }

            test2.type = "fcfs";
            test2.msa(max_iter);

            //System.setOut(new PrintStream(new FileOutputStream("test_"+x+"_LEMITM.txt"), true));
            
            test2 = null;
            
        }
        
        
    }

    public static void test_VISTA(String network) throws IOException
    {
        //Simulator test2 = ReadNetwork.readSignalsNetwork(network, new File("data/nodes_"+network+".txt"), new File("data/links_"+network+".txt"), new File("data/demand_"+network+".txt"), new File("data/phases_"+network+".txt"), Link.CTM);
        //Simulator test2 = Simulator.readTBRNetwork(network, IntersectionPolicy.FCFS, Node.CR, Link.CTM);
        Simulator test2 = Simulator.readSignalsNetwork(network, Link.CTM);
        //Simulator test2 = Simulator.readTBRNetwork(network, IntersectionPolicy.FCFS, Node.CR, Link.CTM);
        test2.readVehicles();
        test2.simulate();
        test2.writeVehicleResults();
        
        Map<Integer, Integer> vista = new HashMap<Integer, Integer>();
        
        Scanner filein = new Scanner(new File(network+"_vehicle_path_time.txt"));
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            filein.next();
            filein.next();
            int dtime = filein.nextInt();
            int etime = filein.nextInt();
  
            vista.put(id, etime - dtime);

            
            filein.nextLine();
            
        }
        
        filein.close();
        int vista_TSTT = 0;
        
        System.out.println("Vehicle Id\tDeparture Time\tVista TT\tSimulator TT");
        for(Vehicle v : test2.getVehicles())
        {
            //if(v.getId() != 24052) continue;
            
            //System.out.print("VISTA: "+v.getId()+"\t"+v.getNetEnterTime()+"\t"+vista.get(v.getId())+"\t"+v.getTT());
            System.out.print(v.getId()+"\t"+v.getNetEnterTime()+"\t"+vista.get(v.getId())+"\t"+v.getTT());
            
            if(v.getNextLink() != null)
            {
                System.out.print("\t"+v.getNextLink()+"\t"+v.getPath());
            }
            
            System.out.println();
            vista_TSTT += vista.get(v.getId());
        }
        
        System.out.println(test2.getTSTT() / 3600.0);
        System.out.println("Total Vista time: "+vista_TSTT / 3600.0);
    }
    
    
    // order of outputs: avg. TT for {l59, l69, l79, l89, overall}; non-exit; avg. MPG for {l59, l69, l79, l89, overall}
    public static double[] test_intersection(int[] dem, int[] numLanes, int[] speeds, double dem_rt, double dem_lt, IntersectionControl intersection) throws IOException
    {

        boolean ctm = true;
        
        if(ctm)
        {
            Simulator.setTimestep(Link.CTM);
        }
        else
        {
            Simulator.setTimestep(Link.LTM);
        }
        
        int node_id = 1;
        Zone n1 = new Zone(node_id++, new Location(0, 2));
        Zone n2 = new Zone(node_id++, new Location(0, -2));
        Zone n3 = new Zone(node_id++, new Location(2, 0));
        Zone n4 = new Zone(node_id++, new Location(-2, 0));

        Node n5 = new Intersection(node_id++, new Location(0, 1), new PriorityTBR());
        Node n6 = new Intersection(node_id++, new Location(0, -1), new PriorityTBR());
        Node n7 = new Intersection(node_id++, new Location(1, 0), new PriorityTBR());
        Node n8 = new Intersection(node_id++, new Location(-1, 0), new PriorityTBR());

        // intersection
        Node n9 = new Intersection(node_id++, new Location(0, 0), intersection);
        n9.setX(0);
        n9.setY(0);

        Link l15, l26, l37, l48, l51, l62, l73, l84, l59, l69, l79, l89, l95, l96, l97, l98;

        l15 = new CentroidConnector(15, n1, n5);
        l26 = new CentroidConnector(26, n2, n6);
        l37 = new CentroidConnector(37, n3, n7);
        l48 = new CentroidConnector(48, n4, n8);

        l51 = new CentroidConnector(51, n5, n1);
        l62 = new CentroidConnector(62, n6, n2);
        l73 = new CentroidConnector(73, n7, n3);
        l84 = new CentroidConnector(84, n8, n4);
        

        double capacity = 1200;

        if(ctm)
        {
            l59 = new CTMLink(59, n5, n9, capacity, speeds[0], speeds[0]/2.0, 115, 1, numLanes[0]);
            l69 = new CTMLink(69, n6, n9, capacity, speeds[1], speeds[1]/2.0, 120, 1, numLanes[1]);
            l79 = new CTMLink(79, n7, n9, capacity, speeds[2], speeds[2]/2.0, 120, 1, numLanes[2]);
            l89 = new CTMLink(89, n8, n9, capacity, speeds[3], speeds[3]/2.0, 120, 1, numLanes[3]);

            l95 = new CTMLink(95, n9, n5, capacity, speeds[0], speeds[0]/2.0, 120, 1, numLanes[0]);
            l96 = new CTMLink(96, n9, n6, capacity, speeds[1], speeds[1]/2.0, 120, 1, numLanes[1]);
            l97 = new CTMLink(97, n9, n7, capacity, speeds[2], speeds[2]/2.0, 120, 1, numLanes[2]);
            l98 = new CTMLink(98, n9, n8, capacity, speeds[3], speeds[3]/2.0, 120, 1, numLanes[3]);
        }
        else
        {
            l59 = new LTMLink(59, n5, n9, capacity, speeds[0], speeds[0]/2.0, 120, 1, numLanes[0]);
            l69 = new LTMLink(69, n6, n9, capacity, speeds[1], speeds[1]/2.0, 120, 1, numLanes[1]);
            l79 = new LTMLink(79, n7, n9, capacity, speeds[2], speeds[2]/2.0, 120, 1, numLanes[2]);
            l89 = new LTMLink(89, n8, n9, capacity, speeds[3], speeds[3]/2.0, 120, 1, numLanes[3]);

            l95 = new LTMLink(95, n9, n5, capacity, speeds[0], speeds[0]/2.0, 120, 1, numLanes[0]);
            l96 = new LTMLink(96, n9, n6, capacity, speeds[1], speeds[1]/2.0, 120, 1, numLanes[1]);
            l97 = new LTMLink(97, n9, n7, capacity, speeds[2], speeds[2]/2.0, 120, 1, numLanes[2]);
            l98 = new LTMLink(98, n9, n8, capacity, speeds[3], speeds[3]/2.0, 120, 1, numLanes[3]);      
        }

        List<Link> links = new ArrayList<Link>();
        List<Node> nodes = new ArrayList<Node>();


        nodes.add(n1);
        nodes.add(n2);
        nodes.add(n3);
        nodes.add(n4);
        nodes.add(n5);
        nodes.add(n6);
        nodes.add(n7);
        nodes.add(n8);
        nodes.add(n9);

        links.add(l15);
        links.add(l26);
        links.add(l37);
        links.add(l48);
        links.add(l51);
        links.add(l62);
        links.add(l73);
        links.add(l84);

        links.add(l59);
        links.add(l69);
        links.add(l79);
        links.add(l89);
        links.add(l95);
        links.add(l96);
        links.add(l97);
        links.add(l98);




        Simulator test;
        
        if(ctm)
        {
            test = new Simulator("test", nodes, links, Link.CTM);
        }
        else
        {
            test = new Simulator("test", nodes, links, Link.LTM);
        }
        
        test.initialize();

        List<PersonalVehicle> vehicles = new ArrayList<PersonalVehicle>();

        int veh_id = 0;

        // origin, dest, demand, path
        Object[][] trip_data = new Object[12][];

        // through
        double dem_st = 1 - dem_rt - dem_lt;
        trip_data[0] = new Object[]{n1, n2, dem[0] * dem_st, null};
        trip_data[1] = new Object[]{n2, n1, dem[1] * dem_st, null};
        trip_data[2] = new Object[]{n3, n4, dem[2] * dem_st, null};
        trip_data[3] = new Object[]{n4, n3, dem[3] * dem_st, null};

        // right turn
        trip_data[4] = new Object[]{n1, n3, (dem[0]*dem_rt), null};
        trip_data[5] = new Object[]{n3, n2, (dem[1]*dem_rt), null};
        trip_data[6] = new Object[]{n2, n4, (dem[2]*dem_rt), null};
        trip_data[7] = new Object[]{n4, n1, (dem[3]*dem_rt), null};

        // left turn
        trip_data[8] = new Object[]{n1, n4, (dem[0]*dem_lt), null};
        trip_data[9] = new Object[]{n4, n2, (dem[1]*dem_lt), null};
        trip_data[10] = new Object[]{n2, n3, (dem[2]*dem_lt), null};
        trip_data[11] = new Object[]{n3, n1, (dem[3]*dem_lt), null};


        for(int i = 0; i < trip_data.length; i++)
        {
            trip_data[i][3] = test.findPath((Zone)trip_data[i][0], (Zone)trip_data[i][1], 0, 1.0, DriverType.AV, TTCost.ttCost);
            //trip_data[i][3] = null;
        }


        double duration = 3600;

        for(int i = 0; i < trip_data.length; i++)
        {
            int demand = (int)Math.round((Double)trip_data[i][2]);

            for(int j = 0; j < demand*duration/3600; j++)
            {
                double vot = Simulator.dagum_rand();
                ++ veh_id;
                vehicles.add(new PersonalVehicle(veh_id*trip_data.length + i, (Zone)trip_data[i][0], (Zone)trip_data[i][1], (int)(Math.random()*duration), vot, new 
FairWallet(vot), (Path)trip_data[i][3]));
                
            }
        }

        test.setVehicles(vehicles);
        test.msa(2);

        int count = 0;
        
        for(Vehicle v : vehicles)
        {
            if(!v.isExited())
            {
                count++;
            }
            else    //Added by Sudesh; can be removed later
            {
                //System.out.println("Vehicle "+v.getId()+" did not exit.");
            }
        }
        
        
        double[] output = new double[11];
        output[0] = l59.getAvgTT(901);
        output[1] = l69.getAvgTT(901);
        output[2] = l79.getAvgTT(901);
        output[3] = l89.getAvgTT(901);
        output[4] = test.getTSTT()/3600.0;
        output[5] = count;
        
        RunningAvg a59 = new RunningAvg();
        RunningAvg a69 = new RunningAvg();
        RunningAvg a79 = new RunningAvg();
        RunningAvg a89 = new RunningAvg();
        
        
        double avg_mpg = 0;
        
        for(PersonalVehicle v : vehicles)
        {
            avg_mpg += v.getMPG();
            
            switch(v.getOrigin().getId())
            {
                case 1: 
                    a59.add(v.getMPG());
                    break;
                case 2: 
                    a69.add(v.getMPG());
                    break;
                case 3: 
                    a79.add(v.getMPG());
                    break;
                case 4: 
                    a89.add(v.getMPG());
                    break;
            }
        }

        avg_mpg /= vehicles.size();
        
        output[6] = a59.getAverage();
        output[7] = a69.getAverage();
        output[8] = a79.getAverage();
        output[9] = a89.getAverage();
        
        output[10] = avg_mpg;
        
        
        return output;
    }

    
    
    
    
    // order of outputs: avg. TT for {l59, l69, l79, l89, overall}; non-exit; avg. MPG for {l59, l69, l79, l89, overall}
    public static void createCandidateSolutions(int[] dem, int[] numLanes, int[] capacities, int[] speeds, double lt_pct, double rt_pct) throws IOException
    {
        for(int i = 0; i < 4; i++)
        {
            if(dem[i] > capacities[i]*numLanes[i])
            {
                throw new RuntimeException("Pulse exceeds 1 timestep");
            }
        }
        
        Simulator.setTimestep(Link.CTM);

        
        int node_id = 1;
        Zone n1 = new Zone(node_id++, new Location(0, 2));
        Zone n2 = new Zone(node_id++, new Location(0, -2));
        Zone n3 = new Zone(node_id++, new Location(2, 0));
        Zone n4 = new Zone(node_id++, new Location(-2, 0));

        Node n5 = new Intersection(node_id++, new Location(0, 1), new PriorityTBR());
        Node n6 = new Intersection(node_id++, new Location(0, -1), new PriorityTBR());
        Node n7 = new Intersection(node_id++, new Location(1, 0), new PriorityTBR());
        Node n8 = new Intersection(node_id++, new Location(-1, 0), new PriorityTBR());

        // intersection
        //Node n9 = new Intersection(node_id++, new Location(0, 0), new IPTBR(new VOTObj()));
        Node n9 = new Intersection(node_id++, new Location(0, 0), new PriorityTBR());
        n9.setX(0);
        n9.setY(0);

        Link l15, l26, l37, l48, l51, l62, l73, l84, l59, l69, l79, l89, l95, l96, l97, l98;

        l15 = new CentroidConnector(15, n1, n5);
        l26 = new CentroidConnector(26, n2, n6);
        l37 = new CentroidConnector(37, n3, n7);
        l48 = new CentroidConnector(48, n4, n8);

        l51 = new CentroidConnector(51, n5, n1);
        l62 = new CentroidConnector(62, n6, n2);
        l73 = new CentroidConnector(73, n7, n3);
        l84 = new CentroidConnector(84, n8, n4);
        

        l59 = new CTMLink(101, n5, n9, capacities[0], speeds[0], speeds[0]/2.0, 264, 0.5, numLanes[0]);
        l69 = new CTMLink(102, n6, n9, capacities[1], speeds[1], speeds[1]/2.0, 264, 0.5, numLanes[1]);
        l79 = new CTMLink(103, n7, n9, capacities[2], speeds[2], speeds[2]/2.0, 264, 0.5, numLanes[2]);
        l89 = new CTMLink(104, n8, n9, capacities[3], speeds[3], speeds[3]/2.0, 264, 0.5, numLanes[3]);

        l95 = new CTMLink(201, n9, n5, capacities[0], speeds[0], speeds[0]/2.0, 264, 0.5, numLanes[0]);
        l96 = new CTMLink(202, n9, n6, capacities[1], speeds[1], speeds[1]/2.0, 264, 0.5, numLanes[1]);
        l97 = new CTMLink(203, n9, n7, capacities[2], speeds[2], speeds[2]/2.0, 264, 0.5, numLanes[2]);
        l98 = new CTMLink(204, n9, n8, capacities[3], speeds[3], speeds[3]/2.0, 264, 0.5, numLanes[3]);


        List<Link> links = new ArrayList<Link>();
        List<Node> nodes = new ArrayList<Node>();


        nodes.add(n1);
        nodes.add(n2);
        nodes.add(n3);
        nodes.add(n4);
        nodes.add(n5);
        nodes.add(n6);
        nodes.add(n7);
        nodes.add(n8);
        nodes.add(n9);

        links.add(l15);
        links.add(l26);
        links.add(l37);
        links.add(l48);
        links.add(l51);
        links.add(l62);
        links.add(l73);
        links.add(l84);

        links.add(l59);
        links.add(l69);
        links.add(l79);
        links.add(l89);
        links.add(l95);
        links.add(l96);
        links.add(l97);
        links.add(l98);




        Simulator test;
        

        test = new Simulator("test", nodes, links, Link.CTM);

        
        test.initialize();

        List<PersonalVehicle> vehicles = new ArrayList<PersonalVehicle>();

        int veh_id = 0;

        
        int N = 500;
        int pulse_time = 10;
        
        Zone[] lt = new Zone[]{n3, n4, n2, n1};
        Zone[] rt = new Zone[]{n4, n3, n1, n2};
        Zone[] st = new Zone[]{n2, n1, n4, n3};
        Zone[] allZones = new Zone[]{n1, n2, n3, n4};
        
        for(int p = 0; p < N; p++)
        {
            System.out.println("Creating pulse: "+p+" "+(p*Simulator.dt*pulse_time));
            for(int k = 0; k < 4; k++)
            {
                for(int n = 0; n < dem[k]; n++)
                {
                    Zone r = allZones[k];
                    Zone s;
                    
                    double rand = Math.random();
                    if(rand < lt_pct/100.0)
                    {
                        s = lt[k];
                    }
                    else if(rand - lt_pct/100.0 < rt_pct/100.0)
                    {
                        s = rt[k];
                    }
                    else
                    {
                        s = st[k];
                    }
                    
                    double vot = Simulator.dagum_rand();
                    PersonalVehicle veh = new PersonalVehicle(++veh_id, r, s, p * pulse_time * Simulator.dt);
                    veh.setVOT(vot);
                    
                    vehicles.add(veh);
                }
            }
        }


        test.setVehicles(vehicles);
        test.msa(1);

    }
    
    public static void testCandidateSolutions(int[] dem, int[] numLanes, int[] capacities, int[] speeds, double lt_pct, double rt_pct) throws IOException
    {
        for(int i = 0; i < 4; i++)
        {
            if(dem[i] > capacities[i]*numLanes[i])
            {
                throw new RuntimeException("Pulse exceeds 1 timestep");
            }
        }
        
        Simulator.setTimestep(Link.CTM);

        
        int node_id = 1;
        Zone n1 = new Zone(node_id++, new Location(0, 2));
        Zone n2 = new Zone(node_id++, new Location(0, -2));
        Zone n3 = new Zone(node_id++, new Location(2, 0));
        Zone n4 = new Zone(node_id++, new Location(-2, 0));

        Node n5 = new Intersection(node_id++, new Location(0, 1), new PriorityTBR());
        Node n6 = new Intersection(node_id++, new Location(0, -1), new PriorityTBR());
        Node n7 = new Intersection(node_id++, new Location(1, 0), new PriorityTBR());
        Node n8 = new Intersection(node_id++, new Location(-1, 0), new PriorityTBR());

        // intersection
        Node n9 = new Intersection(node_id++, new Location(0, 0), new CandidatesTBR(new File("output.txt")));
        n9.setX(0);
        n9.setY(0);

        Link l15, l26, l37, l48, l51, l62, l73, l84, l59, l69, l79, l89, l95, l96, l97, l98;

        l15 = new CentroidConnector(15, n1, n5);
        l26 = new CentroidConnector(26, n2, n6);
        l37 = new CentroidConnector(37, n3, n7);
        l48 = new CentroidConnector(48, n4, n8);

        l51 = new CentroidConnector(51, n5, n1);
        l62 = new CentroidConnector(62, n6, n2);
        l73 = new CentroidConnector(73, n7, n3);
        l84 = new CentroidConnector(84, n8, n4);
        

        l59 = new CTMLink(101, n5, n9, capacities[0], speeds[0], speeds[0]/2.0, 264, 0.5, numLanes[0]);
        l69 = new CTMLink(102, n6, n9, capacities[1], speeds[1], speeds[1]/2.0, 264, 0.5, numLanes[1]);
        l79 = new CTMLink(103, n7, n9, capacities[2], speeds[2], speeds[2]/2.0, 264, 0.5, numLanes[2]);
        l89 = new CTMLink(104, n8, n9, capacities[3], speeds[3], speeds[3]/2.0, 264, 0.5, numLanes[3]);

        l95 = new CTMLink(201, n9, n5, capacities[0], speeds[0], speeds[0]/2.0, 264, 0.5, numLanes[0]);
        l96 = new CTMLink(202, n9, n6, capacities[1], speeds[1], speeds[1]/2.0, 264, 0.5, numLanes[1]);
        l97 = new CTMLink(203, n9, n7, capacities[2], speeds[2], speeds[2]/2.0, 264, 0.5, numLanes[2]);
        l98 = new CTMLink(204, n9, n8, capacities[3], speeds[3], speeds[3]/2.0, 264, 0.5, numLanes[3]);


        List<Link> links = new ArrayList<Link>();
        List<Node> nodes = new ArrayList<Node>();


        nodes.add(n1);
        nodes.add(n2);
        nodes.add(n3);
        nodes.add(n4);
        nodes.add(n5);
        nodes.add(n6);
        nodes.add(n7);
        nodes.add(n8);
        nodes.add(n9);

        links.add(l15);
        links.add(l26);
        links.add(l37);
        links.add(l48);
        links.add(l51);
        links.add(l62);
        links.add(l73);
        links.add(l84);

        links.add(l59);
        links.add(l69);
        links.add(l79);
        links.add(l89);
        links.add(l95);
        links.add(l96);
        links.add(l97);
        links.add(l98);




        Simulator test;
        

        test = new Simulator("test", nodes, links, Link.CTM);

        
        test.initialize();

        List<PersonalVehicle> vehicles = new ArrayList<PersonalVehicle>();

        int veh_id = 0;

        
        int N = 1;
        int pulse_time = 10;
        
        Zone[] lt = new Zone[]{n3, n4, n2, n1};
        Zone[] rt = new Zone[]{n4, n3, n1, n2};
        Zone[] st = new Zone[]{n2, n1, n4, n3};
        Zone[] allZones = new Zone[]{n1, n2, n3, n4};
        
        for(int p = 0; p < N; p++)
        {
            System.out.println("Creating pulse: "+p+" "+(p*Simulator.dt*pulse_time));
            for(int k = 0; k < 4; k++)
            {
                for(int n = 0; n < dem[k]; n++)
                {
                    Zone r = allZones[k];
                    Zone s;
                    
                    double rand = Math.random();
                    if(rand < lt_pct/100.0)
                    {
                        s = lt[k];
                    }
                    else if(rand - lt_pct/100.0 < rt_pct/100.0)
                    {
                        s = rt[k];
                    }
                    else
                    {
                        s = st[k];
                    }
                    
                    double vot = Simulator.dagum_rand();
                    PersonalVehicle veh = new PersonalVehicle(++veh_id, r, s, p * pulse_time * Simulator.dt);
                    veh.setVOT(vot);
                    
                    vehicles.add(veh);
                }
            }
        }


        test.setVehicles(vehicles);
        test.msa(1);

    }
}
