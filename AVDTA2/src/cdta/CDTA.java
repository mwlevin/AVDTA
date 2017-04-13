/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cdta;

import avdta.dta.ReadDTANetwork;
import avdta.network.Simulator;
import avdta.network.node.Node;
import avdta.project.CDTAProject;
import cdta.cell.Cell;
import cdta.priority.ASTAuction;
import cdta.priority.Auction;
import cdta.priority.DepTime;
import java.io.File;
import java.io.IOException;
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
        for(int x = 100; x <= 150; x += 5)
        {
            test("SiouxFalls", x);
        }
        */
        
        //analyze(new File("cdta_ast5_vehicles_150.txt"), 10);

        //test2("coacongress2", 150, 5);
        test2("coacongress2", 150, 15);
    }
    
    public static void test(String name, int prop) throws IOException
    {
        CDTAProject project = new CDTAProject(new File("projects/"+name));

        ReadDTANetwork read = new ReadDTANetwork();
        read.prepareDemand(project, prop/100.0);
        project.loadSimulator();
        
        System.out.println(project.getName());
        
        TECNetwork net = project.createTECNetwork();
        
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
