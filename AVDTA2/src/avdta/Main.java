/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta;

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
import avdta.gui.DTAGUI;
import avdta.gui.GUI;
import avdta.gui.editor.Editor;
import avdta.gui.editor.visual.rules.LinkBusRule;
import avdta.gui.editor.visual.rules.LinkDataRule;
import avdta.gui.editor.visual.rules.data.LinkFileSource;
import avdta.gui.editor.visual.rules.data.VolumeLinkData;
import avdta.network.Path;
import avdta.network.ReadNetwork;
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
import avdta.network.node.policy.TransitFirst;
import avdta.project.SAVProject;
import avdta.sav.ReadSAVNetwork;
import avdta.util.RunningAvg;
import avdta.vehicle.Bus;
import avdta.vehicle.DriverType;
import avdta.vehicle.PersonalVehicle;
import avdta.vehicle.Vehicle;
import java.awt.Color;
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
    public static void main(String[] args) throws Exception
    {
        //caccTest1("scenario_2_PM", "scenario_2_PM_CACC");
        //caccTest2("scenario_2_PM_2_CACC");
        //caccTest1("coacongress2_LTM", "coacongress2_CACC");
        //caccTest1("scenario_2_pm_sub", "scenario_2_pm_sub_CACC");
        
        //caccTest3("austinI35", "austinI35_CACC");
        
        //caccAnalyze1("coacongress2_LTM", "coacongress2_CACC");
        //caccAnalyze1("scenario_2_pm_sub", "scenario_2_pm_sub_CACC");

        //caccTest2();
        
        new DTAGUI();
        
        /*
        DTAProject project = new DTAProject(new File("projects/coacongress2_LTM"));
        ReadDTANetwork read = new ReadDTANetwork();
        read.prepareDemand(project, 0.8);
        project.loadSimulator();
        DTASimulator sim = project.getSimulator();
        sim.msa(10);
*/
        //GUI.main(args);
        
        //removeDuplicateCentroids();

        /*
        DTAProject project = new DTAProject(new File("projects/coacongress2_CACC"));
        CACCConvert.convertAll(project);
    */
        //caccVisualize("coacongress2");
        
        //createCACCNetwork();
        
        //fixConnectivity();
        //fixConnectivity2();
        //connectivityTest();
        
        
        
    }
    
    public static void removeDuplicateCentroids() throws IOException
    {
        DTAProject project = new DTAProject(new File("projects/coacongress2"));
        DTASimulator sim = project.getSimulator();
        Map<Integer, Link> linksmap = sim.createLinkIdsMap();
        
        DTAProject project2 = new DTAProject(new File("projects/coacongress3"));
        
        Scanner filein = new Scanner(project.getNodesFile());
        
        PrintStream fileout = new PrintStream(new FileOutputStream(project2.getNodesFile()), true);
        
        filein.nextLine();
        fileout.println(ReadNetwork.getNodesFileHeader());
        
        while(filein.hasNextInt())
        {
            NodeRecord node = new NodeRecord(filein.nextLine());
            
            if(node.getId() > 200000)
            {
                if(linksmap.containsKey(node.getId() - 100000))
                {
                    
                }
                else
                {
                    node.setId(node.getId() - 100000);
                    fileout.println(node);
                }
            }
            else
            {
                fileout.println(node);
            }
        }
        fileout.close();
        
        filein = new Scanner(project.getLinksFile());
        
        fileout = new PrintStream(new FileOutputStream(project2.getLinksFile()), true);
        
        filein.nextLine();
        fileout.println(ReadNetwork.getLinksFileHeader());
        
        while(filein.hasNextInt())
        {
            LinkRecord link = new LinkRecord(filein.nextLine());
            
            if(link.getSource() > 200000)
            {
                link.setSource(link.getSource() - 100000);
            }
            if(link.getDest() > 200000)
            {
                link.setDest(link.getDest() - 100000);
            }
            
            fileout.println(link);
        }
        fileout.close();
        
        
        
        filein = new Scanner(project.getDynamicODFile());
        
        fileout = new PrintStream(new FileOutputStream(project2.getDynamicODFile()), true);
        
        filein.nextLine();
        fileout.println(ReadDemandNetwork.getDynamicODFileHeader());
        
        while(filein.hasNextInt())
        {
            DynamicODRecord od = new DynamicODRecord(filein.nextLine());
            
            if(od.getDest() > 200000)
            {
                od.setDest(od.getDest() - 100000);
            }
            
            fileout.println(od);
        }
        fileout.close();
        
        
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
    
    public static void connectivityTest() throws Exception
    {
        
        DTAProject project = new DTAProject(new File("projects/scenario_2_pm_sub_CACC"));
        DTASimulator sim = project.getSimulator();
        
        Set<String> unconnected = new HashSet<String>();
        for(Vehicle v : sim.getVehicles())
        {
            if(!(v instanceof PersonalVehicle))
            {
                continue;
            }
            Path p = null;
            try
            {
                p = sim.findPath((PersonalVehicle)v, TravelCost.ffTime);
            }
            catch(Exception ex){}
            
            if(p == null)
            {
                unconnected.add(v.getOrigin()+"\t"+(int)Math.abs(v.getDest().getId()));
            }
        }
        
        PrintStream fileout =new PrintStream(new FileOutputStream(new File("unconnected.txt")), true);
        for(String x : unconnected)
        {
            fileout.println(x);
        }
        fileout.close();
        
    }
    
    public static void createCACCNetwork() throws IOException
    {
        DTAProject project = new DTAProject(new File("projects/scenario_2_pm"));
        //new DemandImportFromVISTA(project, "data");
        //new DTAImportFromVISTA(project, new File("data/vehicle_path.txt"), new File("data/vehicle_path_time.txt"));
        
        
        DTASimulator sim = project.getSimulator();
        
        Assignment assign = new Assignment(new File(project.getAssignmentsFolder()+"/2543"));
        sim.loadAssignment(assign);
        
        Map<Integer, Link> linksmap = sim.createLinkIdsMap();
        
        Set<Link> newLinks = new HashSet<Link>();
        
        Scanner filein = new Scanner(new File(project.getProjectDirectory()+"/vissim_subnetwork.txt"));
        filein.nextLine();
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            filein.nextLine();
            
            newLinks.add(linksmap.get(id));
            
        }
        filein.close();
        
        DTAProject clone = new DTAProject();
        clone.createProject("scenario_2_pm_sub", new File("projects/scenario_2_pm_sub"));
        sim.createSubnetwork(newLinks, assign.getSimVatFile(), clone);
        
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
    
    public static void caccTest2(String net1) throws IOException
    {
        DTAProject austin = new DTAProject(new File("projects/"+net1));
        
        PrintStream out = new PrintStream(new FileOutputStream(new File("CACC_results2.txt")), true);
        out.println("Proportion\tTSTT\tTSTT w CACC\tDemand");

        
        int max_iter = 100;
        double min_gap = 1;
            
        for(int i = 0; i <= 50; i+= 5)
        {
            Map<Integer, Double> proportions = new HashMap<Integer, Double>();
            proportions.put(ReadDTANetwork.AV+ReadDTANetwork.ICV+ReadDTANetwork.DA_VEHICLE, i/100.0);
            proportions.put(ReadDTANetwork.HV+ReadDTANetwork.ICV+ReadDTANetwork.DA_VEHICLE, (100-i)/100.0);
            
            ReadDTANetwork read1 = new ReadDTANetwork();
            read1.changeDynamicType(austin, proportions);
            read1.prepareDemand(austin, 0.6);
            austin.loadProject();
            
            
            DTASimulator sim1 = austin.getSimulator();
            sim1.partial_demand(5);
            DTAResults results1 = sim1.msa_cont(5, max_iter, min_gap);
            
            sim1.getAssignment().getAssignmentFolder().renameTo(new File(austin.getAssignmentsFolder()+"/100_"+i));
            
            
            out.println(i+"\t"+sim1.getTSTT()+"\t"+sim1.getNumVehicles());
            
            
        }
        out.close();

    }
    
    
    public static void caccTest1(String net1, String net2) throws IOException
    {
        DTAProject austin = new DTAProject(new File("projects/"+net1));
        DTAProject austin_CACC = new DTAProject(new File("projects/"+net2));
        
        PrintStream out = new PrintStream(new FileOutputStream(new File("CACC_results1.txt")), true);
        out.println("% demand\tTSTT\tTSTT w CACC\tDemand");
        
        PrintStream out2 = new PrintStream(new FileOutputStream(new File("CACC_results1_corridor.txt")), true);
        out2.println("% demand\tCorridor TT\tCorridor TT w/ CACC\tDemand");
        
        Map<Integer, Double> proportions = new HashMap<Integer, Double>();
        proportions.put(ReadDTANetwork.AV+ReadDTANetwork.ICV+ReadDTANetwork.DA_VEHICLE, 1.0);

        
        int max_iter = 30;
        double min_gap = 1;
            
        for(int i = 100; i >= 100; i-= 5)
        {
            
            
            ReadDTANetwork read1 = new ReadDTANetwork();
            read1.changeDynamicType(austin, proportions);
            read1.prepareDemand(austin, i/100.0);
            austin.loadProject();
            
            austin_CACC.importDemandFromProject(austin);
            austin_CACC.loadProject();
            
            DTASimulator sim1 = austin.getSimulator();
            sim1.partial_demand(5);
            DTAResults results1 = sim1.msa_cont(5, max_iter, min_gap);
            
            sim1.getAssignment().getAssignmentFolder().renameTo(new File(austin.getAssignmentsFolder()+"/"+i+"_50"));
            
            DTASimulator sim2 = austin_CACC.getSimulator();
            sim2.partial_demand(5);
            DTAResults results2 = sim2.msa_cont(5, max_iter, min_gap);
            
            sim2.getAssignment().getAssignmentFolder().renameTo(new File(austin_CACC.getAssignmentsFolder()+"/"+i+"_50"));
            
            if(sim1.getNumVehicles() != sim2.getNumVehicles())
            {
                throw new RuntimeException("Demand does not match");
            }
            
            out.println(i+"\t"+sim1.getTSTT()+"\t"+sim2.getTSTT()+"\t"+sim1.getNumVehicles());
            
            Set<Link> corridor1 = new HashSet<Link>();
            
            for(Link l : sim1.getLinks())
            {
                if(l.getFFSpeed() >= 60)
                {
                    corridor1.add(l);
                }
            }
            
            double avg1 = sim1.getTT(corridor1, 3600);
            
            corridor1.clear();
            
            for(Link l : sim2.getLinks())
            {
                if(l.getFFSpeed() >= 60)
                {
                    corridor1.add(l);
                }
            }
            
            double avg2 = sim2.getTT(corridor1, 3600);
            
            out2.println(i+"\t"+avg1+"\t"+avg2+"\t"+sim1.getNumVehicles());
            
            // create data source
            if(i == 100)
            {
                VolumeLinkData data1 = new VolumeLinkData();
                data1.initialize(austin);
                
                VolumeLinkData data2 = new VolumeLinkData();
                data2.initialize(austin_CACC);
                
                PrintStream fileout = new PrintStream(new FileOutputStream(new File("VolDiff.txt")), true);
                
                fileout.println("Link\t%Difference\tDifference");
                for(Link l1 : sim1.getLinks())
                {
                    Link l2 = sim2.getLink(l1.getId());
                    
                    double d1 = data1.getData(l1, 0);
                    double d2 = data2.getData(l2, 0);
                    /*
                    if(l2.getFFSpeed() >= 60)
                    {
                        Link l2a = sim2.getLink(l1.getId()+100000);
                        
                        if(l2a != null)
                        {
                            d2 += data2.getData(l2a, 0);
                        }
                    }*/
                    
                    fileout.println(l1.getId()+"\t"+((d2 - d1)/d1)+"\t"+(d2-d1));
                }
                fileout.close();
            }
        }
        out.close();
        out2.close();
    }
    
    // this tests 100% CVs
    public static void caccTest3(String net1, String net2) throws IOException
    {
        DTAProject austin = new DTAProject(new File("projects/"+net1));
        DTAProject austin_CACC = new DTAProject(new File("projects/"+net2));
        
        PrintStream out = new PrintStream(new FileOutputStream(new File("CACC_results3.txt")), true);
        out.println("% demand\tTSTT\tTSTT w CACC\tDemand");
        
        
        Map<Integer, Double> proportions = new HashMap<Integer, Double>();
        proportions.put(ReadDTANetwork.AV+ReadDTANetwork.ICV+ReadDTANetwork.DA_VEHICLE, 1.0);
        
        int max_iter = 3;
        double min_gap = 2;
            
        for(int i = 80; i <= 80; i+= 5)
        {
            
            
            ReadDTANetwork read1 = new ReadDTANetwork();
            read1.changeDynamicType(austin, proportions);
            read1.prepareDemand(austin, i/100.0);
            austin.loadProject();
            
            austin_CACC.importDemandFromProject(austin);
            austin_CACC.loadProject();
            
            DTASimulator sim1 = austin.getSimulator();
            DTAResults results1 = sim1.msa(max_iter, min_gap);
            
            sim1.getAssignment().getAssignmentFolder().renameTo(new File(austin.getAssignmentsFolder()+"/"+i+"_50"));
            
            DTASimulator sim2 = austin_CACC.getSimulator();
            DTAResults results2 = sim2.msa(max_iter, min_gap);
            
            sim2.getAssignment().getAssignmentFolder().renameTo(new File(austin_CACC.getAssignmentsFolder()+"/"+i+"_50"));
            
            if(sim1.getNumVehicles() != sim2.getNumVehicles())
            {
                throw new RuntimeException("Demand does not match");
            }
            
            out.println(i+"\t"+sim1.getTSTT()+"\t"+sim2.getTSTT()+"\t"+sim1.getNumVehicles());
        }
        out.close();

    }
    
    
    public static void caccVisualize(String net1) throws Exception
    {
        DTAProject austin = new DTAProject(new File("projects/"+net1));
        
        Editor editor =  new Editor(austin);

        
        LinkDataRule rule = new LinkDataRule()
        {
            public Color getColor(Link l, int t)
            {
                double val = getDataSource().getData(l, t);
                
                

                Color minColor = getMinColor();
                Color maxColor = getMaxColor();
                
                if(val < 0)
                {
                    maxColor = Color.blue;
                    val = Math.abs(val);
                }
                
                double minValue = getMinValue();
                double maxValue = getMaxValue();

                if(val < minValue)
                {
                    return minColor;
                }
                else if(val >= maxValue)
                {
                    return maxColor;
                }

                int r1 = minColor.getRed();
                int r2 = maxColor.getRed();
                int g1 = minColor.getGreen();
                int g2 = maxColor.getGreen();
                int b1 = minColor.getBlue();
                int b2 = maxColor.getBlue();

                double scale = (val - minValue) / (maxValue - minValue);

                return new Color((int)Math.round(r1 + scale * (r2 - r1)), 
                        (int)Math.round(g1 + scale * (g2 - g1)),
                        (int)Math.round(b1 + scale * (b2 - b1)));
            }
        };
        rule.setDataSource(new LinkFileSource(new File("VolDiff.txt")));
        rule.setMinColor(Color.black);
        rule.setMaxColor(Color.red);
        rule.setMinWidth(3);
        rule.setMaxWidth(3);
        rule.setMinValue(0);
        rule.setMaxValue(1);
        editor.addVisualization(rule);
        
        
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
