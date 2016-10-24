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
import avdta.project.Project;
import avdta.vehicle.Vehicle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author ml26893
 */
public class CACCConvert {
    
    public static void convert(Project project, int max_lanes) throws IOException
    {

        Scanner filein = new Scanner(project.getLinksFile());
        filein.nextLine();
        
        ArrayList<LinkRecord> newLinks = new ArrayList<LinkRecord>();
        
        
        while(filein.hasNext())
        {
            LinkRecord link = new LinkRecord(filein.nextLine());
            
            
            
            
            if(link.getType() != ReadNetwork.CENTROID && CACCLTMLink.checkK2(link.getCapacity(), link.getFFSpd(), link.getLength()) && link.getFFSpd() >= 60
                    && link.getNumLanes() > 1)
            {
                int newLanes = (int)Math.min(link.getNumLanes()-1, max_lanes);
                if(newLanes > 0)
                {
                    LinkRecord l2 = link.clone();
                    l2.setNumLanes(newLanes);
                    link.setNumLanes(link.getNumLanes()-newLanes);
                    l2.setId(l2.getId()+100000);
                    l2.setType(ReadNetwork.LTM+ReadNetwork.CACC);
                    newLinks.add(l2);
                }
                
                newLinks.add(link);
                
            }
            else
            {
                newLinks.add(link);
            }
            
           
            
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
}
