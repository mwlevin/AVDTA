/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cdta;

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
        CDTAProject project = new CDTAProject(new File("projects/SiouxFalls"));

        
        
        TECNetwork net = project.createTECNetwork();

        net.initializeConnectivity();
        net.setCalcFFtime(false);
        
        System.out.println("Loaded network.");
        
        System.out.println("T: "+net.getT());

        net.reserveAll();
        
        /*
        
        Trajectory traj = net.shortestPath(101, -102, 0);
        net.reserve(traj);
        
        System.out.println(traj);
        
        
        for(int c = 0; c < traj.size()-1; c++)
        {
            Cell i = traj.get(c);
            Cell j = traj.get(c+1);
            System.out.println(i.getNextCellConnector().isConnected(i, j));
            i.getNextCellConnector().printConnectivity(i);
        }
        
        //net.reserveAll();
        
        Trajectory traj2 = net.shortestPath(101, -102, 0);
        
        System.out.println(traj2);
        
        for(int c = 0; c < traj2.size()-1; c++)
        {
            Cell i = traj2.get(c);
            Cell j = traj2.get(c+1);
            System.out.println(i.getNextCellConnector().isConnected(i, j));
            i.getNextCellConnector().printConnectivity(i);
        }
        
        net.reserve(traj2);
        */
        
        System.out.println(net.validate());

    }
}
