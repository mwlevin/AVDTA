/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta;

import avdta.dta.DTASimulator;
import avdta.gui.GUI;
import avdta.network.Simulator;
import avdta.project.DTAProject;
import avdta.project.Project;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import avdta.network.node.*;
import avdta.network.link.*;
import avdta.vehicle.PersonalVehicle;
import avdta.vehicle.Vehicle;
import java.util.ArrayList;
import java.util.List;



/**
 *
 * @author micha
 */
public class Main 
{
    public static void main(String[] args) throws IOException
    {
        transitTest();
        //GUI.main(args);
    }
    
    public static void transitTest() throws IOException
    {
        DTAProject project = new DTAProject();
        project.createProject("transit", new File("projects/transit"));
        DTASimulator sim = project.createEmptySimulator();
        
        List<Node> nodes = new ArrayList<Node>();
        List<Link> links = new ArrayList<Link>();
        
        
        Node n1 = new Zone(1);
        Node n2 = new Intersection(2, new PriorityTBR());
        Node n3 = new Intersection(3, new PriorityTBR());
        Node n4 = new Zone(4);
        
        nodes.add(n1);
        nodes.add(n2);
        nodes.add(n3);
        nodes.add(n4);
        
        Link l12 = new CentroidConnector(12, n1, n2);
        Link l23 = new SharedTransitCTMLink(23, n2, n3, 1200, 30, 15, 5280.0/Vehicle.vehicle_length, 2, 2);
        Link l34 = new CentroidConnector(34, n3, n4);
        
        links.add(l12);
        links.add(l23);
        links.add(l34);
        
        sim.setNetwork(nodes, links);
        
        System.out.println(sim.findPath(n1, n4));
        
        
        List<Vehicle> vehicles = new ArrayList<Vehicle>();
        int rate = 2400;
        
        int num = (int)(rate * 10.0/60);
        
        for(int i = 0; i < num; i++)
        {
            vehicles.add(new PersonalVehicle(i+1, n1, n4, (int)(600.0/num*i)));
        }
        
        sim.setVehicles(vehicles);
        
        sim.msa(2);
        
        
        
    }
}
