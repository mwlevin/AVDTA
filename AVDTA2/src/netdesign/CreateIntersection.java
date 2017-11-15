/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package netdesign;

import avdta.dta.DTASimulator;
import avdta.network.ReadNetwork;
import avdta.network.link.Link;
import avdta.network.link.LinkRecord;
import avdta.network.node.Node;
import avdta.network.node.PhaseRecord;
import avdta.network.node.SignalRecord;
import avdta.network.node.TurnRecord;
import avdta.project.DTAProject;
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
public class CreateIntersection 
{
    public static void createTestIntersection(int nodeid) throws IOException
    {
        // requires 4 incoming and 4 outgoing links (not including centroid connectors)
        
        DTAProject project = new DTAProject(new File("AVDTA2/projects/coacongress"));
        DTASimulator sim = project.getSimulator();
        
        DTAProject newIntersection = new DTAProject();
        newIntersection.createProject("intersection_"+nodeid, new File("AVDTA2/projects/intersection"));
        
        Node node = sim.getNode(nodeid);
        
        // create list link records for outputs
            
        List<LinkRecord> links = new ArrayList<LinkRecord>();
        
        for(Link l : sim.getLinks())
        {
            links.add(l.createLinkRecord());
        }
        
        ////////////To find the angular direction of each link
   
        // find incoming and outgoing links in each direction
        Link northO = null, southO = null, eastO = null, westO = null;
        Link northI = null, southI = null, eastI = null, westI = null;
        
        double angle = Integer.MIN_VALUE;
        
        for(Link l : node.getOutgoing())
        {
            if(l.isCentroidConnector())
            {
                continue;
            }
            
            if(Math.abs(l.getDirection() - 3*Math.PI/2) < Math.abs(angle - 3*Math.PI/2))
            {
                angle = l.getDirection();
                southO = l;
            }
        }
        
        angle = Integer.MIN_VALUE;
        
        for(Link l : node.getOutgoing())
        {
            if(l.isCentroidConnector())
            {
                continue;
            }
            
            if(Math.abs(l.getDirection() - Math.PI/2) < Math.abs(angle - Math.PI/2))
            {
                angle = l.getDirection();
                northO = l;
            }
        }
        
        angle = Integer.MIN_VALUE;
        
        for(Link l : node.getOutgoing())
        {
            if(l.isCentroidConnector())
            {
                continue;
            }
            
            if(Math.abs(l.getDirection() - Math.PI) < Math.abs(angle - Math.PI))
            {
                angle = l.getDirection();
                westO = l;
            }
        }
        
        for(Link l : node.getOutgoing())
        {
            if(l.isCentroidConnector())
            {
                continue;
            }
            
            if(l != northO && l != westO && l != southO)
            {
                eastO = l;
                break;
            }
        }
        
        
        
        for(Link l : node.getIncoming())
        {
            if(l.getSource() == northO.getDest())
            {
                northI = l;
            }
            else if(l.getSource() == westO.getDest())
            {
                westI = l;
            }
            else if(l.getSource() == eastO.getDest())
            {
                eastI = l;
            }
            else if(l.getSource() == southO.getDest())
            {
                southI = l;
            }
        }
        
        // map new links to old:
        Map<Integer, Link> map = new HashMap<Integer, Link>();
        map.put(13, southI);
        map.put(31, southO);
        map.put(21, eastI);
        map.put(12, eastO);
        map.put(15, northO);
        map.put(51, northI);
        map.put(14, westO);
        map.put(41, westI);
        
        
        // copy link details to mapped links
        for(LinkRecord record : links)
        {
            if(map.containsKey(record.getId()))
            {
                Link downtownLink = map.get(record.getId());
                record.setCapacity(downtownLink.getCapacityPerLane());
                record.setLength(downtownLink.getLength());
                record.setFFSpd(downtownLink.getFFSpeed());
                record.setWavespd(downtownLink.getWaveSpeed());
                record.setNumLanes(downtownLink.getNumLanes());
            }
        }
        
        // write links to file
        PrintStream fileout = new PrintStream(new FileOutputStream(newIntersection.getLinksFile()), true);
        fileout.println(ReadNetwork.getLinksFileHeader());
        for(LinkRecord record : links)
        {
            fileout.println(record);
        }
        fileout.close();
        
       
        // create reverse map of above
        Map<Integer, Integer> reverseMap = new HashMap<>();
        for(int k : map.keySet())
        {
            reverseMap.put(map.get(k).getId(), k);
        }
        
        // print 0 signal offset
        fileout = new PrintStream(new FileOutputStream(newIntersection.getSignalsFile()), true);
        fileout.println(ReadNetwork.getSignalsFileHeader());
        fileout.println(new SignalRecord(1, 0));
        fileout.close();
        
        // copy phases data
        fileout = new PrintStream(new FileOutputStream(newIntersection.getPhasesFile()), true);
        fileout.println(ReadNetwork.getPhasesFileHeader());
        
        Scanner filein = new Scanner(project.getPhasesFile());
        filein.nextLine();
        
        while(filein.hasNextLine())
        {
            PhaseRecord phase = new PhaseRecord(filein.nextLine());
            
            if(phase.getNode() == node.getId())
            {
                // change phase link ids
                for(TurnRecord t : phase.getTurns())
                {
                    t.setI(reverseMap.get(t.getI()));
                    t.setJ(reverseMap.get(t.getJ()));
                }
                
                fileout.println(phase);
            }
        }
        fileout.close();
        
    }
}
