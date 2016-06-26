/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.microtoll;

import avdta.network.ReadNetwork;
import avdta.network.Simulator;
import static avdta.network.Simulator.dt;
import static avdta.network.Simulator.duration;
import static avdta.network.Simulator.time;
import avdta.cost.TravelCost;
import avdta.network.node.FCFSPolicy;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.vehicle.PersonalVehicle;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author ml26893
 */
public class MTSimulator extends Simulator
{
    public static final TravelCost costFunc = TravelCost.dnlGenCost;
    
    public static MTSimulator readTBRNetwork(String network, int linktype) throws IOException
    {
        return readTBRNetwork(network, new FCFSPolicy(), Node.CR, linktype);
    }
    public static MTSimulator readTBRNetwork(String network, int linktype, String demandfile) throws IOException
    {
        return readTBRNetwork(network, new FCFSPolicy(), Node.CR, linktype, demandfile);
    }
    public static MTSimulator readTBRNetwork(String network, Object policy, int nodetype, int linktype) throws IOException
    {
        MTReadNetwork input = new MTReadNetwork();
        MTSimulator output = new MTSimulator(network);
        
        input.readNetwork(output, linktype);
        input.readTBR(output, policy, nodetype);
        input.readVehicles(output);
        
        output.initialize();
        
        return output;
    }
    public static MTSimulator readTBRNetwork(String network, Object policy, int nodetype, int linktype, String demandfile) throws IOException
    {
        MTReadNetwork input = new MTReadNetwork();
        MTSimulator output = new MTSimulator(network);
        
        input.readNetwork(output, linktype);
        input.readTBR(output, policy, nodetype);
        input.readVehicles(output, new File("data/"+network+"/"+demandfile));
        
        output.initialize();
        
        return output;
    }
    
    private int toll_update = 60;
    private double alpha = 0.90;
    private double beta = 10;
    
    public MTSimulator(String name)
    {
        super(name);
    }
    
    public MTSimulator(String name, List<Node> nodes, List<Link> links, int linktype)
    {
        super(name, nodes, links, linktype);
    }
    
    public void setTollUpdate(int t)
    {
        toll_update = t;
    }
    
    public void setAlpha(double a)
    {
        alpha = a;
    }
    
    public void setBeta(double b)
    {
        beta = b;
    }
  
    
    public void simulate() throws IOException
    {
        simulate(false);
    }
    

    
    protected void addVehicles()
    {
        while(veh_idx < vehicles.size())
        {
            PersonalVehicle v = vehicles.get(veh_idx);

            if(v.getDepTime() <= time)
            {
                dijkstras(v.getOrigin(), Simulator.time, v.getVOT(), v.getDriver(), MTSimulator.costFunc);
                v.setPath(trace(v.getOrigin(), v.getDest()));
                
                v.entered();
                v.getNextLink().addVehicle(v);
                veh_idx++;
            }
            else
            {
                break;
            }
        }
        
        if(time % toll_update == 0)
        {
            updateTolls();
        }
    }
    
    public void updateTolls()
    {
        for(Link l : links)
        {
            if(!l.isCentroidConnector())
            {
                int oldTime = time - 60*10;
                double tollStar = beta*(l.getAvgTT(oldTime) - l.getFFTime());
                double newToll = tollStar * alpha + (1-alpha) * l.getToll(oldTime);
                l.setToll(time, newToll);
            }
        }
    }
    
    protected void propagateFlow()
    {
        if(getLinkType() == Link.CTM)
        {
            for(Link l : links)
            {
                l.prepare();
            }
        }

        for(Link l : links)
        {
            l.step();
        }

        for(Node n : nodes)
        {
            exit_count += n.step();
        }

        if(getLinkType() == Link.CTM)
        {
            for(Link l : links)
            {
                l.update();
            }
        }
    }
}
