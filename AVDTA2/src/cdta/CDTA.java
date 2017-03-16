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
import java.util.Map;

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
}
