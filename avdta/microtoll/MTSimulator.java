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
import avdta.network.cost.TravelCost;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.project.Project;
import avdta.vehicle.PersonalVehicle;
import avdta.vehicle.Vehicle;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 *
 * @author ml26893
 */
public class MTSimulator extends Simulator
{
    public static final TravelCost costFunc = TravelCost.dnlGenCost;
    
    
    
    private int toll_update = 60;
    private double alpha = 0.90;
    private double beta = 10;
    
    public MTSimulator(Project project)
    {
        super(project);
    }
    
    public MTSimulator(Project project, Set<Node> nodes, Set<Link> links)
    {
        super(project, nodes, links);
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
  

    
    public void addVehicles()
    {
        while(veh_idx < vehicles.size())
        {
            Vehicle veh = vehicles.get(veh_idx);
            
            if(veh instanceof PersonalVehicle)
            {
                PersonalVehicle v = (PersonalVehicle)veh;

                if(v.getDepTime() <= time)
                {
                    dijkstras(v.getOrigin(), v.getDest(), Simulator.time, v.getVOT(), v.getDriver(), MTSimulator.costFunc);
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

}
