/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cdta;

import avdta.dta.DTASimulator;
import avdta.dta.ReadDTANetwork;
import avdta.network.Simulator;
import avdta.network.node.Node;
import avdta.project.CDTAProject;
import avdta.project.DTAProject;
import avdta.vehicle.DriverType;
import cdta.cell.Cell;
import cdta.priority.ASTAuction;
import cdta.priority.Auction;
import cdta.priority.DepTime;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author ml26893
 */
public class CDTA 
{
    public static void main(String[] args) throws IOException
    {
        /*
        int type = 2;
        
        String network;

        
        if(type <= 1)
        {
            network = "SiouxFalls";
        }
        else
        {
            network = "coacongress";
        }

        

        PrintStream fileout = new PrintStream(new FileOutputStream(new File("cdta_"+network+".txt")), true);
        PrintStream fileout2 = new PrintStream(new FileOutputStream(new File("cdtaff_"+network+".txt")), true);
        PrintStream fileout3 = new PrintStream(new FileOutputStream(new File("dta_"+network+".txt")), true);
        
        for(int x = 100; x <= 150; x += 5)
        {
            double avgtt = test(network, x);
            fileout.println(x+"\t"+avgtt);
            
            avgtt = test1a(network, x);
            fileout2.println(x+"\t"+avgtt);
            
            DTAProject project = new DTAProject(new File("projects/"+network));

            DTASimulator sim = project.getSimulator();
            sim.msa(50);

            fileout3.println(x+"\t"+sim.getAvgTT(DriverType.AV));
        }
        fileout.close();
        fileout2.close();
        fileout3.close();

*/

        
        analyze(new File("cdta_vehicles_100.txt"), 10);
        System.out.println();
        analyze(new File("cdta_vehicles_150.txt"), 10);

        //test2("coacongress2", 150, 5);
        //test2("coacongress2", 150, 15);
    }
    
    public static double test(String name, int prop) throws IOException
    {
        CDTAProject project = new CDTAProject(new File("projects/"+name));

        ReadDTANetwork read = new ReadDTANetwork();
        read.prepareDemand(project, prop/100.0);
        project.loadSimulator();
        
        System.out.println(project.getName());
        
        TECNetwork net = project.createTECNetwork();
        net.KEEP_FREE_FLOW = false;
        
        //net.setPriority(new Auction());
        net.setPriority(new Auction());

        net.setCalcFFtime(true);
        
        System.out.println("Loaded network.");
        
        System.out.println("Cells: "+net.getNumCells());
        System.out.println("T: "+net.getT());

        net.reserveAll();
        
        File file = new File(project.getResultsFolder()+"/cdta_vehicles.txt");
        File target = new File(project.getResultsFolder()+"/cdta_vehicles_"+prop+".txt");
        if(target.exists())
        {
            target.delete();
        }
        file.renameTo(target);
        
        return net.getAvgTT();
    }
    
    public static double test1a(String name, int prop) throws IOException
    {
        CDTAProject project = new CDTAProject(new File("projects/"+name));

        System.out.println(project.getName());
        
        TECNetwork net = project.createTECNetwork();
        net.KEEP_FREE_FLOW = true;
        
        //net.setPriority(new Auction());
        net.setPriority(new Auction());

        net.setCalcFFtime(true);
        
        System.out.println("Loaded network.");
        
        System.out.println("Cells: "+net.getNumCells());
        System.out.println("T: "+net.getT());

        net.reserveAll();
        
        File file = new File(project.getResultsFolder()+"/cdtaff_vehicles.txt");
        File target = new File(project.getResultsFolder()+"/cdtaff_vehicles_"+prop+".txt");
        if(target.exists())
        {
            target.delete();
        }
        file.renameTo(target);
        
        return net.getAvgTT();
    }
    
    public static void test2(String name, int prop, int ast_duration) throws IOException
    {
        CDTAProject project = new CDTAProject(new File("projects/"+name));

        ReadDTANetwork read = new ReadDTANetwork();
        read.prepareDemand(project, prop/100.0);
        project.loadSimulator();
        
        System.out.println(project.getName());
        
        TECNetwork net = project.createTECNetwork();
        
        //net.setPriority(new Auction());
        net.setPriority(new ASTAuction(ast_duration*60));

        net.setCalcFFtime(true);
        
        System.out.println("Loaded network.");
        
        System.out.println("Cells: "+net.getNumCells());
        System.out.println("T: "+net.getT());

        net.reserveAll();
        
        File file = new File(project.getResultsFolder()+"/cdta_vehicles.txt");
        File target = new File(project.getResultsFolder()+"/cdta_ast"+ast_duration+"_vehicles_"+prop+".txt");
        if(target.exists())
        {
            target.delete();
        }
        file.renameTo(target);
    }
    
    public static void analyze(File file, int bins) throws IOException
    {
        Scanner filein = new Scanner(file);
        
        filein.nextLine();
        
        List<Tuple> list = new ArrayList<Tuple>();
        
        while(filein.hasNextInt())
        {
            filein.nextInt();
            filein.nextInt();
            filein.nextInt();
            
            list.add(new Tuple(filein.nextInt(), filein.nextDouble(), filein.nextInt(), filein.nextInt()));
        }
        
        filein.close();
        
        Collections.sort(list);
        
        int start = 0;
        
        for(int i = 1; i <= bins; i++)
        {
            int stop = (int)Math.round((double)i/bins * list.size());
            
            int count = stop - start;
            
            double avg = 0.0;
            
            for(int j = start; j < stop; j++)
            {
                Tuple t = list.get(j);
                
                avg += (double)(t.tt - t.fftime) / t.fftime;
            }
            
            avg /= count;
            
            
            double stdev = 0.0;
            
            double part2 = 0.0;
            
            for(int j = start; j < stop; j++)
            {
                Tuple t = list.get(j);
                
                double val = (double)(t.tt - t.fftime) / t.fftime;
                
                part2 += val*val;
            }
            
            part2 /= count;
            
            stdev = Math.sqrt(part2 - avg * avg);
            
            System.out.println((100*(i-1)/bins)+"-"+(100*i/bins)+"%"+"\t"+avg+"\t"+stdev);
            
            start = stop;
        }
    }
    
    static class Tuple implements Comparable<Tuple>
    {
        public double vot;
        public int dtime;
        public int tt;
        public int fftime;
        
        public Tuple(int dtime, double vot, int tt, int fftime)
        {
            this.vot = vot;
            this.dtime = dtime;
            this.tt = tt;
            this.fftime = fftime;
        }
        
        public int compareTo(Tuple rhs)
        {
            if(vot > rhs.vot)
            {
                return -1;
            }
            else if(vot < rhs.vot)
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
    }
}
