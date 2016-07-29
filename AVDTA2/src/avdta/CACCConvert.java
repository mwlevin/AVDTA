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
    
    public static void main(File file) throws IOException
    {

        Scanner filein = new Scanner(file);
        PrintStream fileout = new PrintStream(new FileOutputStream(new File("links_cacc.txt")), true);
        
        filein.nextLine();
        fileout.println(ReadNetwork.getLinksFileHeader());
        
        while(filein.hasNext())
        {
            LinkRecord link = new LinkRecord(filein.nextLine());
            
            
            
            
            if(link.getType() != ReadNetwork.CENTROID && CACCLTMLink.checkK2(link.getCapacity(), link.getFFSpd(), link.getLength()) && link.getFFSpd() >= 60
                    && link.getNumLanes() > 1)
            {
                LinkRecord l2 = link.clone();
                l2.setNumLanes(1);
                link.setNumLanes(link.getNumLanes()-1);
                l2.setId(l2.getId()+100000);
                l2.setType(ReadNetwork.LTM+ReadNetwork.CACC);
                
                fileout.println(link);
                fileout.println(l2);
            }
            else
            {
                fileout.println(link);
            }
            
           
            
        }
        
        filein.close();
        fileout.close();
    }
}
