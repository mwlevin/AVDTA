/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta;

import avdta.network.Simulator;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.network.Path;
import avdta.vehicle.Vehicle;
import avdta.network.node.Intersection;
import avdta.network.node.StopSign;
import avdta.network.node.TrafficSignal;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 *
 * @author ut
 */
public class SecurityTest
{
    public static void main(String[] args) throws IOException
    {
        //hack_corridor("Guadalupe");
        
        long_test1();
    }
    
    public static void hack_corridor(String corridor) throws IOException
    {
        String network = "coacongress";
        
        Scanner filein = new Scanner(new File(corridor+".txt"));
        
        Set<Integer> hacked = new HashSet<Integer>();
        
        while(filein.hasNextInt())
        {
            hacked.add(filein.nextInt());
        }
        
        filein.close();
        
        Simulator test = Simulator.readSignalsNetwork(network, Link.CTM);
        test.setScenario("coacongress_security");
        test.readVehicles();
        
        int count = 0;
        
        for(Node node : test.getNodes())
        {
            if(hacked.contains(node.getId()) && node instanceof Intersection && ((Intersection)node).getControl() instanceof TrafficSignal)
            {
                ((Intersection)node).setControl(new StopSign());
                count++;
            }
        }
        
        
        test.initialize();
        
        
        test.readVehicles();
        test.simulate();
        
        System.out.println(count);
        System.out.println("Stop signs: "+test.getTSTT()/3600.0+" hr");
        System.out.println("\tAvg. TT: "+(test.getTSTT()/60 / test.getVehicles().size())+" min/veh");
        System.out.println("\tExiting: "+test.getNumExited());
    }
    
    public static void long_test1() throws IOException
    {
        String network = "coacongress";
        
        PrintStream fileout = new PrintStream(new FileOutputStream(new File("security_graph_flow.txt")), true);
        fileout.println("Stop signs\tTSTT\tAvg.\tExiting");
        
        /*
        Simulator test = Simulator.readSignalsNetwork(network, Link.CTM);
        test.setScenario("coacongress_security");
        test.msa(50);
        System.exit(1);
        */
        for(int n = 100; n <= 100; n++)
        {
        Simulator test;
        /*
        test = Simulator.readSignalsNetwork(network, Link.CTM);
        test.setScenario("coacongress_security");
        test.readVehicles();
        test.simulate();
        
        System.out.println("Signals: "+test.getTSTT()/3600.0+" hr");
        System.out.println("\tAvg. TT: "+(test.getTSTT()/60 / test.getVehicles().size())+" min/veh");
        System.out.println("\tExiting: "+test.getNumExited());
        */
        
        
        /*
        Map<Integer, Integer> arr_times = new HashMap<Integer, Integer>();
        
        for(Vehicle v : test.getVehicles())
        {
            arr_times.put(v.getId(), v.getExitTime());
        }
        
        test = null;
        */
        
        
        
        //test = Simulator.readTBRNetwork(network, null, Node.STOP, Link.CTM);
        test = Simulator.readSignalsNetwork(network, Link.CTM);
        test.setScenario("coacongress_security");
        test.readVehicles();
        
        /*
        int count = 0;
        for(Node n : test.getNodes())
        {
            if((n instanceof Intersection) && (((Intersection)n).getControl() instanceof TrafficSignal))
            {
                ((Intersection)n).setControl(new StopSign());
                count++;
            }
            else if((n instanceof Intersection) && (((Intersection)n).getControl() instanceof StopSign))
            {
                count++;
            }
        }
        System.out.println(count+" stop signs");
        */
        
        List<NodeRank> nodes = mostUsedIntersections(test);
        
        changeWorstNodes(test, nodes, n);
        
        test.initialize();
        
        
        test.readVehicles();
        /*
        List<NodeRank> list = mostUsedIntersections(test);
        
        affectMostVehicles(test, list, 10, 10);
        System.exit(1);
        */

        
        
        test.simulate();
        
        test.importResults();
        test.printLinkTT(0, 7200);

        System.out.println(n);
        System.out.println("Stop signs: "+test.getTSTT()/3600.0+" hr");
        System.out.println("\tAvg. TT: "+(test.getTSTT()/60 / test.getVehicles().size())+" min/veh");
        System.out.println("\tExiting: "+test.getNumExited());
        
        fileout.println(n+"\t"+(test.getTSTT()/3600.0)+"\t"+(test.getTSTT()/60 / test.getVehicles().size())+"\t"+test.getNumExited());
        
        
        
        
        double delay = 0.0;
        /*
        for(PersonalVehicle v : test.getVehicles())
        {
            double tt1 = arr_times.get(v.getId()) - v.getDepTime();
            double tt2 = v.getExitTime() - v.getDepTime();
            delay += (tt2 - tt1) / tt1;
            
            if((tt2 - tt1) / tt1 > 5)
            {
                //System.out.println(tt1+" "+tt2);
            }
        }
        */
        System.out.println(delay/ test.getVehicles().size());
        }
     
        fileout.close();
    }
    
    public static void long_test2() throws IOException
    {
        String network = "coacongress";
        
        PrintStream fileout = new PrintStream(new FileOutputStream(new File("security_graph_veh.txt")), true);
        fileout.println("Stop signs\tTSTT\tAvg.\tExiting");
        
        for(int n = 100; n <= 100; n++)
        {
            Simulator test;

            test = Simulator.readSignalsNetwork(network, Link.CTM);
            test.setScenario("coacongress_security");
            test.readVehicles();

            List<NodeRank> nodes = mostUsedIntersections(test);

            affectMostVehicles(test, nodes, n);

            test.initialize();


            test.readVehicles();


            test.simulate();
        
            System.out.println(n);
            System.out.println("Stop signs: "+test.getTSTT()/3600.0+" hr");
            System.out.println("\tAvg. TT: "+(test.getTSTT()/60 / test.getVehicles().size())+" min/veh");
            System.out.println("\tExiting: "+test.getNumExited());

            fileout.println(n+"\t"+(test.getTSTT()/3600.0)+"\t"+(test.getTSTT()/60 / test.getVehicles().size())+"\t"+test.getNumExited());




        }
     
        fileout.close();
    }
    
    public static void intervention_test1() throws IOException
    {
        String network = "coacongress";
        
        PrintStream fileout = new PrintStream(new FileOutputStream(new File("security_graph_intervention_1.txt")), true);
        fileout.println("Stop signs\tTSTT\tAvg.\tExiting");
        
        // secure up to 20 signals, hack 20 remaining
        for(int n = 0; n <= 20; n++)
        {
            Simulator test;

            test = Simulator.readSignalsNetwork(network, Link.CTM);
            test.setScenario("coacongress_security");
            test.readVehicles();

            List<NodeRank> nodes = mostUsedIntersections(test);

            changeWorstNodes(test, nodes, n, n+20);

            test.initialize();


            test.readVehicles();


            test.simulate();
        
            System.out.println(n);
            System.out.println("Stop signs: "+test.getTSTT()/3600.0+" hr");
            System.out.println("\tAvg. TT: "+(test.getTSTT()/60 / test.getVehicles().size())+" min/veh");
            System.out.println("\tExiting: "+test.getNumExited());

            fileout.println(n+"\t"+(test.getTSTT()/3600.0)+"\t"+(test.getTSTT()/60 / test.getVehicles().size())+"\t"+test.getNumExited());




        }
     
        fileout.close();
    }
    
    public static void intervention_test2() throws IOException
    {
        String network = "coacongress";
        
        PrintStream fileout = new PrintStream(new FileOutputStream(new File("security_graph_intervention_2.txt")), true);
        fileout.println("Stop signs\tTSTT\tAvg.\tExiting");
        
        // secure up to 20 signals, hack 20 total
        for(int n = 0; n <= 20; n++)
        {
            Simulator test;

            test = Simulator.readSignalsNetwork(network, Link.CTM);
            test.setScenario("coacongress_security");
            test.readVehicles();

            List<NodeRank> nodes = mostUsedIntersections(test);

            changeWorstNodes(test, nodes, n, 20-n);

            test.initialize();


            test.readVehicles();


            test.simulate();
        
            System.out.println(n);
            System.out.println("Stop signs: "+test.getTSTT()/3600.0+" hr");
            System.out.println("\tAvg. TT: "+(test.getTSTT()/60 / test.getVehicles().size())+" min/veh");
            System.out.println("\tExiting: "+test.getNumExited());

            fileout.println(n+"\t"+(test.getTSTT()/3600.0)+"\t"+(test.getTSTT()/60 / test.getVehicles().size())+"\t"+test.getNumExited());




        }
     
        fileout.close();
    }
    
    public static void changeWorstNodes(Simulator test, List<NodeRank> nodes, int n)
    {
        int count = 0;
        int idx = 0;
        
        while(count < n && idx < nodes.size())
        {
            Intersection node = (Intersection)nodes.get(idx++).node;
            
            if(node.getControl() instanceof TrafficSignal)
            {
                node.setControl(new StopSign());
                count++;
            }
        }
        
        test.initialize();
    }
    
    public static void changeWorstNodes(Simulator test, List<NodeRank> nodes, int skip, int n)
    {
        int count = 0;
        int idx = 0;
        
        while(count < skip+n && idx < nodes.size())
        {
            Intersection node = (Intersection)nodes.get(idx++).node;
            
            if(node.getControl() instanceof TrafficSignal)
            {
                if(count >= skip)
                {
                    node.setControl(new StopSign());
                }
                count++;
            }
        }
        
        test.initialize();
    }
    
    public static void affectMostVehicles(Simulator test, List<NodeRank> nodes, int n)
    {
        // greedy heuristic
        Map<Node, NodeRank> map = new HashMap<Node, NodeRank>();
        
        Set<Vehicle> vehicles = new HashSet<Vehicle>();
        
        for(NodeRank nr : nodes)
        {
            map.put(nr.node, nr);
        }
        
        Set<Node> hacked = new HashSet<Node>();
        
        int i = 0;
        
        while(i++ < n)
        {
            Intersection node;
            
            int idx = 0;
            do
            {
                node = (Intersection)nodes.get(idx).node;
                idx++;
            }
            while((hacked.contains(node) || !(node.getControl() instanceof TrafficSignal)) && idx < nodes.size());
            
            node.setControl(new StopSign());
            hacked.add(node);
            
            
            
            System.out.println("Node: "+node.getId());
            
            for(Vehicle v : test.getVehicles())
            {
                if(usesNode(node, v))
                {
                    Path p = v.getPath();
                    
                    for(int x = 1; x < p.size(); x++)
                    {
                        Node node2 = p.get(x).getSource();
                        
                        if(map.containsKey(node2))
                        {
                            map.get(node2).addWeight(-1);
                        }
                    }
                    
                    vehicles.add(v);
                }
            }
            
            Collections.sort(nodes);
        }
        
        System.out.println("Vehicles affected: "+vehicles.size());
        
        test.initialize();
    }
    
    private static boolean usesNode(Node node, Vehicle v)
    {
        Path path = v.getPath();
        
        for(int i = 0; i < path.size(); i++)
        {
            if(path.get(i).getSource() == node)
            {
                return true;
            }
        }
        
        if(path.get(path.size()-1).getDest() == node)
        {
            return true;
        }
        
        return false;
    }
    
    public static void affectMostVehicles(Simulator test, List<NodeRank> nodes, int skip, int n)
    {
        Set<Node> secured = new HashSet<Node>();
        
        Map<Node, NodeRank> map = new HashMap<Node, NodeRank>();
        
        Set<Vehicle> vehicles = new HashSet<Vehicle>();
        
        for(NodeRank nr : nodes)
        {
            map.put(nr.node, nr);
        }
        
        Set<Node> hacked = new HashSet<Node>();
        
        int i = 0;
        
        while(i++ < skip)
        {
            Intersection node;
            
            int idx = 0;
            do
            {
                node = (Intersection)nodes.get(idx).node;
                idx++;
            }
            while((secured.contains(node) || !(node.getControl() instanceof TrafficSignal)) && idx < nodes.size());
            
            secured.add(node);
            
            System.out.println("Node: "+node.getId());
            
            for(Vehicle v : test.getVehicles())
            {
                if(usesNode(node, v))
                {
                    Path p = v.getPath();
                    
                    for(int x = 1; x < p.size(); x++)
                    {
                        Node node2 = p.get(x).getSource();
                        
                        if(map.containsKey(node2))
                        {
                            map.get(node2).addWeight(-1);
                        }
                    }
                }
            }
            
            Collections.sort(nodes);
        }
        
        nodes = mostUsedIntersections(test);
        
        i = 0;
        while(i++ < n)
        {
            Intersection node;
            
            int idx = 0;
            do
            {
                node = (Intersection)nodes.get(idx).node;
                idx++;
            }
            while((hacked.contains(node) || secured.contains(node) || !(node.getControl() instanceof TrafficSignal)) && idx < nodes.size());
            
            node.setControl(new StopSign());
            hacked.add(node);
            
            System.out.println("Node: "+node.getId());
            
            for(Vehicle v : test.getVehicles())
            {
                if(usesNode(node, v))
                {
                    Path p = v.getPath();
                    
                    for(int x = 1; x < p.size(); x++)
                    {
                        Node node2 = p.get(x).getSource();
                        
                        if(map.containsKey(node2))
                        {
                            map.get(node2).addWeight(-1);
                        }
                    }
                    
                    vehicles.add(v);
                }
            }
            
            Collections.sort(nodes);
        }
        
        
        System.out.println("Vehicles affected: "+vehicles.size());
        
        test.initialize();
    }
    
    public static List<NodeRank> mostUsedIntersections(Simulator test)
    {
        Map<Node, Integer> map = new HashMap<Node, Integer>();
        
        for(Vehicle v : test.getVehicles())
        {
            Path p = v.getPath();
            
            for(int i = 1; i < p.size(); i++)
            {
                Node node = p.get(i).getSource();
                
                if(!(node instanceof Intersection))
                {
                    continue;
                }
                
                if(map.containsKey(node))
                {
                    map.put(node, map.get(node)+1);
                }
                else
                {
                    map.put(node, 1);
                }
            }
        }
        
        List<NodeRank> output = new ArrayList<NodeRank>();
        
        for(Node node : map.keySet())
        {
            output.add(new NodeRank(node, map.get(node)));
        }
        
        Collections.sort(output);
        return output;
    }
    
    static class NodeRank implements Comparable<NodeRank>
    {
        public Node node;
        public int weight;
        
        public NodeRank(Node node)
        {
            this.node = node;
            weight = 0;
        }
        
        public NodeRank(Node node, int weight)
        {
            this.node = node;
            this.weight = weight;
        }
        
        public void addWeight(int amount)
        {
            weight += amount;
        }
        
        public int compareTo(NodeRank rhs)
        {
            if(rhs.weight != weight)
            {
                return rhs.weight - weight;
            }
            else
            {
                return node.getId() - rhs.node.getId();
            }
        }
        
        public String toString()
        {
            return node.toString()+" - "+weight;
        }
    }
}
