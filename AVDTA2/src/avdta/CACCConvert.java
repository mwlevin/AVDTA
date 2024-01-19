/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta;

import avdta.network.Network;
import avdta.network.ReadNetwork;
import avdta.network.link.CACCLTMLink;
import avdta.network.link.CTMLink;
import avdta.network.link.CentroidConnector;
import avdta.network.link.DLRCTMLink;
import avdta.network.link.LTMLink;
import avdta.network.link.Link;
import avdta.network.link.LinkRecord;
import avdta.network.link.SharedTransitCTMLink;
import avdta.network.link.TransitLane;
import avdta.network.node.NodeRecord;
import avdta.project.Project;
import avdta.vehicle.Vehicle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author ml26893
 */
public class CACCConvert {
    
    public static void convertAll(Project project) throws IOException
    {
        ArrayList<LinkRecord> newLinks = new ArrayList<LinkRecord>();
        
       
        
        Scanner filein = new Scanner(project.getLinksFile());
        filein.nextLine();
        
        while(filein.hasNext())
        {
            LinkRecord link = new LinkRecord(filein.nextLine());
            
            
            
            
            if(link.getType() != ReadNetwork.CENTROID.getCode() && CACCLTMLink.checkK2(link.getCapacity(), link.getFFSpd(), link.getLength()) && link.getFFSpd() >= 60)
            {
                link.setType(ReadNetwork.CACC);
            }
            
            newLinks.add(link);
           
            
        }
        
        filein.close();
        
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getLinksFile()), true);
        fileout.println(ReadNetwork.getLinksFileHeader());
        
        for(LinkRecord l : newLinks)
        {
            fileout.println(l);
        }
        fileout.close();
    }
    
    public static void convert(Project project, int max_lanes) throws IOException
    {
        Map<Integer, NodeRecord> nodes = new HashMap<Integer, NodeRecord>();
        
        Scanner filein = new Scanner(project.getNodesFile());
        filein.nextLine();
        
        while(filein.hasNext())
        {
            NodeRecord node = new NodeRecord(filein.nextLine());
            nodes.put(node.getId(), node);
        }
        filein.close();
        
        filein = new Scanner(project.getLinksFile());
        filein.nextLine();
        
        ArrayList<LinkRecord> newLinks = new ArrayList<LinkRecord>();
        
       
        
        
        
        
        while(filein.hasNext())
        {
            LinkRecord link = new LinkRecord(filein.nextLine());
            
            
            
            
            if(link.getType() != ReadNetwork.CENTROID.getCode() && CACCLTMLink.checkK2(link.getCapacity(), link.getFFSpd(), link.getLength()) && link.getFFSpd() >= 60
                    && link.getNumLanes() > 1)
            {
                int newLanes = link.getNumLanes()/2;
                if(newLanes > 0)
                {
                    LinkRecord l2 = link.clone();
                    l2.setNumLanes(newLanes);
                    link.setNumLanes(link.getNumLanes()-newLanes);
                    l2.setId(l2.getId()+100000);
                    l2.setType(ReadNetwork.CACC);
                    newLinks.add(l2);
                    
                    nodes.get(l2.getSource()).setType(ReadNetwork.HIGHWAY);
                    nodes.get(l2.getDest()).setType(ReadNetwork.HIGHWAY);

                }
                
                newLinks.add(link);
                
            }
            else
            {
                newLinks.add(link);
            }
            
           
            
        }
        
        filein.close();
        
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getNodesFile()), true);
        fileout.println(ReadNetwork.getNodesFileHeader());
        
        for(int id : nodes.keySet())
        {
            fileout.println(nodes.get(id));
        }
        
        fileout.close();
        
        fileout = new PrintStream(new FileOutputStream(project.getLinksFile()), true);
        fileout.println(ReadNetwork.getLinksFileHeader());
        
        for(LinkRecord l : newLinks)
        {
            fileout.println(l);
        }
        fileout.close();
    }
}
