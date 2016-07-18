/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.sav;

import avdta.network.Link;
import avdta.network.Node;
import avdta.network.Simulator;
import avdta.intersection.IntersectionPolicy;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author ut
 */
public class SAVMain 
{
    //static String network = "willco_regional_2008";
    //static String network = "winnipeg";
    static String network = "coacongress2";
    
    static int min_sav = 1000;
    static int max_sav = 60000;
    static int inc_sav = 500;
        
    public static void main(String[] args) throws IOException
    {
        base_test();
        RL_test();
        RL_RS_test();
        RS_test();
        
    }
    
    public static void RS_test() throws IOException
    {
        SAVSimulator.relocate = false;
        SAVSimulator.ride_share = true;
        
        PrintStream fileout = new PrintStream(new FileOutputStream(new File("SAV_RS_"+network+".txt")), true);
        
        fileout.println("Taxis\tOVTT\tIVTT\tTT\tEnergy\tVMT\tEmpty VMT\tExiting");
        
        for(int i = min_sav; i <= max_sav; i += inc_sav)
        {
            Object[] output = test(i);
            
            fileout.print(i);
            
            for(int j = 0; j < output.length; j++)
            {
                fileout.print("\t"+output[j]);
            }
            
            fileout.println();
        }
        
        fileout.close();
        
    }
    
    public static void RL_test() throws IOException
    {
        SAVSimulator.relocate = true;
        SAVSimulator.ride_share = false;
        
        PrintStream fileout = new PrintStream(new FileOutputStream(new File("SAV_RL"+network+".txt")), true);
        
        fileout.println("Taxis\tOVTT\tIVTT\tTT\tEnergy\tVMT\tExiting");
        
        for(int i = min_sav; i <= max_sav; i += inc_sav)
        {
            Object[] output = test(i);
            
            fileout.print(i);
            
            for(int j = 0; j < output.length; j++)
            {
                fileout.print("\t"+output[j]);
            }
            
            fileout.println();
        }
        
        fileout.close();
        
    }
       
    public static void RL_RS_test() throws IOException
    {
        SAVSimulator.relocate = true;
        SAVSimulator.ride_share = true;
        
        PrintStream fileout = new PrintStream(new FileOutputStream(new File("SAV_RL_RS_"+network+".txt")), true);
        
        fileout.println("Taxis\tOVTT\tIVTT\tTT\tEnergy\tVMT\tExiting");
        
        for(int i = min_sav; i <= max_sav; i += inc_sav)
        {
            Object[] output = test(i);
            
            fileout.print(i);
            
            for(int j = 0; j < output.length; j++)
            {
                fileout.print("\t"+output[j]);
            }
            
            fileout.println();
        }
        
        fileout.close();
        
    }
    
    public static void base_test() throws IOException
    {
        SAVSimulator.relocate = false;
        SAVSimulator.ride_share = false;
        
        PrintStream fileout = new PrintStream(new FileOutputStream(new File("SAV_base_"+network+".txt")), true);
        
        fileout.println("Taxis\tOVTT\tIVTT\tTT\tEnergy\tVMT\tExiting");
        
        for(int i = min_sav; i <= max_sav; i += inc_sav)
        {
            Object[] output = test(i);
            
            fileout.print(i);
            
            for(int j = 0; j < output.length; j++)
            {
                fileout.print("\t"+output[j]);
            }
            
            fileout.println();
        }
        
        fileout.close();
        
    }
    public static Object[] test(int taxis) throws IOException
    {
        
        
        long time = System.nanoTime();
        
        ReadSAVNetwork input = new ReadSAVNetwork();
        SAVSimulator test = new SAVSimulator(network);
        
        System.out.println("Reading network...");
        input.readNetwork(test, Link.CTM);
        input.readTBR(test, IntersectionPolicy.FCFS, Node.CR);
        
        System.out.println("Reading demand...");
        input.readTravelers(test, 1);

        
        System.out.println("Creating taxis...");
        test.initialize();
        
        test.createTaxis(taxis);
        
        System.out.println("Simulating...");
        test.simulate();
        
        time = System.nanoTime() - time;
        System.out.printf("%.2f s\n", time/1.0e9);
        
        System.out.println("Avg. delay: "+test.getAvgWait());
        System.out.println("Avg. IVTT: "+test.getAvgIVTT());
        System.out.println("Avg. TT: "+test.getAvgTT());
        System.out.println("Total energy: "+test.getTotalEnergy());
        System.out.println("Avg. MPG: "+test.getAvgMPG());
        System.out.println("Total VMT: "+test.getTotalVMT());
        System.out.println("Empty VMT: "+test.getEmptyVMT());
        
        int inTaxi = 0;
        int waiting = test.getWaiting().size();
        int exited = 0;
        int notDeparted = 0;
        
        for(Taxi t : test.getTaxis())
        {
            inTaxi += t.getNumPassengers();
        }
        
        Map<Integer, Integer> errors = new TreeMap<Integer, Integer>();
        
        for(Traveler t : test.getTravelers())
        {
            if(t.isExited())
            {
                exited++;
            }
            else if(t.getDepTime() > Simulator.time)
            {
                notDeparted ++;
            }
            else
            {
                if(errors.containsKey(t.getOrigin().getId()))
                {
                    errors.put(t.getOrigin().getId(), errors.get(t.getOrigin().getId())+1);
                }
                else
                {
                    errors.put(t.getOrigin().getId(), 1);
                }
            }
        }
        
        //test.relocateTaxis();
        
        if(errors.size() > 0)
        {
            System.out.println("Non-exiting travelers");
            for(int k : errors.keySet())
            {
                System.out.println(k+" "+errors.get(k));
            }
        }
        
        System.out.println("---");
        System.out.println("Exited: "+exited);
        System.out.println("Waiting: "+waiting);
        System.out.println("In taxi: "+inTaxi);
        System.out.println("Departing later: "+notDeparted);
        
        int accountedFor = inTaxi + waiting + exited + notDeparted;
        
        System.out.println("Accounted for: "+accountedFor+" / "+test.getTravelers().size());
        
        /*
        for(Taxi t : test.getTaxis())
        {
            if(t.getNumPassengers() > 0)
            {
                System.out.println(t.getPath());
            }
        }
        */

        Object[] output = new Object[7];

        output[0] = test.getAvgWait();
        output[1] = test.getAvgIVTT();
        output[2] = test.getAvgTT();
        output[3] = test.getTotalEnergy();
        output[4] = test.getTotalVMT();
        output[5] = test.getEmptyVMT();
        output[6] = exited;
        
        return output;
    }
}
