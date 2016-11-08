/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parking;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 *
 * @author micha
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException
    {
        holdCostTest();
        probTest();
        //distanceTest();
        //noReservationTest();
        
        //firstTest();
    }
    
    public static void firstTest() throws IOException
    {
        long time = System.nanoTime();
        Network test = new Network("coacongress");
        
        test.readNetwork();
        test.populateStates();
        
        
        test.valueIteration(0.00001, 2000);
        
        
        test.dijkstras(test.findNode(5454));
            
        int max_dist = 0;

        for(State x : test.getStates())
        {
            if(x.getReserved() != null && x.getReserved() == Zone.NULL && x.J < 10000 && x.mu_star.getReserved() != null && x.mu_star.getReserved() != Zone.NULL)
            {
                double temp = test.getDistance(x);

                if(x.J < 10000)
                {
                    max_dist = (int)Math.max(max_dist, temp);
                }
            }
        }
        
        System.out.println(max_dist);
        
        System.out.println(test.getAvgOriginJ());
        
        /*
        for(State x : test.getStates())
        {
            if(x.getReserved() != null && x.getReserved() == Zone.NULL && x.J < 10000 && x.mu_star.getReserved() != null && x.mu_star.getReserved() != Zone.NULL)
            {
                System.out.println(x);
                System.out.println("\t"+x.getNext());
                System.out.println("\t"+x.J);
                System.out.println("\t"+x.getReserved());
                System.out.println("\t"+test.getDistance(x));
            }
        }
        */
        
        //test.dijkstras(test.findNode(5454));

        time = System.nanoTime() - time;
        System.out.println(test.getNumStates());
        System.out.println(test.getNumActions());
        System.out.println(String.format("%.2f", time/1.0e9)+"s");
        
        test.printOutput();
        
        /*
        for(State x : test.getStates())
        {
            System.out.println(x);
            System.out.println("\t"+x.J);
            System.out.println("\t"+x.mu_star);
        }
        */
    }
    
    public static void holdCostTest() throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(new File("holdcost.txt")), true);
        
        fileout.println("Cost\tavg origin J\tmax dist");
        for(int i = 0; i <= 500; i += 5)
        {
            double c = i/100.0;
            
            Network test = new Network("coacongress");
            test.readNetwork();
            
            test.setHoldCost(c);
            
            test.populateStates();
            test.valueIteration(0.00001, 2000);
            
            test.dijkstras(test.findNode(5454));
            
            int max_dist = 0;
            
            for(State x : test.getStates())
            {
                if(x.getReserved() != null && x.getReserved() == Zone.NULL && x.J < 10000 && x.mu_star.getReserved() != null && x.mu_star.getReserved() != Zone.NULL)
                {
                    double temp = test.getDistance(x);
                    
                    max_dist = (int)Math.max(max_dist, temp);
                }
            }
            
            fileout.println(c+"\t"+test.getAvgOriginJ()+"\t"+max_dist);
            
        }
        
        fileout.close();
    }
    
    public static void noReservationTest() throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(new File("no_reserve.txt")), true);
        
        fileout.println("p\tno reserve J\t reserve J");
        
        for(int i = 1; i <= 100; i += 1)
        {
            double p = i/100.0;
            
            Network test = new NoReserveNetwork("coacongress");
            test.readNetwork();
            test.setParkingProb(p);
            
            test.populateStates();
            test.valueIteration(0.00001, 2000);
            
            test.dijkstras(test.findNode(5454));
            
            int max_dist = 0;
            
            for(State x : test.getStates())
            {
                if(x.getNext() != null)
                {
                    max_dist = (int)Math.max(max_dist, test.getDistance(x));
                }
            }
            
            fileout.print(p+"\t"+test.getAvgOriginJ());
            
            
            Network test2 = new Network("coacongress");
            test2.readNetwork();
            test2.setParkingProb(p);
            
            test2.populateStates();
            test2.valueIteration(0.00001, 2000);
            
            fileout.print("\t"+test2.getAvgOriginJ());
            fileout.println();
        }
        
        fileout.close();
    }
    
    public static void distanceTest() throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(new File("distancetest.txt")), true);
        
        fileout.println("Mult\tJ");
        
        for(int i = 0; i <= 120; i+= 1)
        {
            Network test = new Network("coacongress");
            test.readNetwork();
            
            for(Zone z : test.getZones())
            {
                z.setWalkTime(z.getWalkTime() * i);
            }
            
            test.populateStates();
            test.valueIteration(0.00001, 2000);
            
            int count = 0;
            
            for(State x : test.getStates())
            {
                if(x.mu_star != null && x.getReserved() != null && x.getReserved() == Zone.NULL)
                {
                    Zone rho = x.mu_star.getReserved();
                    
                    if(rho != Zone.NULL && rho.getId() != 5454)
                    {
                        count++;
                    }
                }
            }
            
            fileout.println(i+"\t"+test.getAvgOriginJ()+"\t"+count);
        }
        
        fileout.close();
    }
    
    public static void probTest() throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(new File("max_distance.txt")), true);
        PrintStream fileout2 = new PrintStream(new FileOutputStream(new File("avg_origin_cost.txt")), true);
        
        fileout.println("p\tmax_dist");
        fileout2.println("p\tavg. J");
        
        for(int i = 1; i <= 100; i += 1)
        {
            double p = i/100.0;
            
            Network test = new Network("coacongress");
            test.readNetwork();
            test.setParkingProb(p);
            
            test.populateStates();
            test.valueIteration(0.00001, 2000);
            
            test.dijkstras(test.findNode(5454));
            
            int max_dist = 0;
            
            for(State x : test.getStates())
            {
                if(x.getReserved() != null && x.getReserved() == Zone.NULL && x.J < 10000 && x.mu_star.getReserved() != null && x.mu_star.getReserved() != Zone.NULL)
                {
                    double temp = test.getDistance(x);
                    
                    max_dist = (int)Math.max(max_dist, temp);
                }
            }
            
            fileout.println(p+"\t"+max_dist);
            
            fileout2.println(p+"\t"+test.getAvgOriginJ());
            
        }
        
        fileout.close();
        fileout2.close();
    }
    
    
    
}
