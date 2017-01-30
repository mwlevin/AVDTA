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
        test("SiouxFalls", 1.0);
        //test("coacongress2", 1.0);
    }
    
    public static void test(String name, double prop) throws IOException
    {
        CDTAProject project = new CDTAProject(new File("projects/"+name));

        ReadDTANetwork read = new ReadDTANetwork();
        read.prepareDemand(project, 1);
        project.loadSimulator();
        
        System.out.println(project.getName());
        
        TECNetwork net = project.createTECNetwork();

        //net.setCalcFFtime(true);
        
        System.out.println("Loaded network.");
        
        System.out.println("Cells: "+net.getNumCells());
        System.out.println("T: "+net.getT());

        net.reserveAll();
        
        File file = new File(project.getResultsFolder()+"/cdta_vehicles.txt");
        file.renameTo(new File(project.getResultsFolder()+"/cdta_vehicles_"+((int)(prop*100))+".txt"));
    }
}
