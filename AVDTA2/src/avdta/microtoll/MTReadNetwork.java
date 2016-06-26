/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.microtoll;

import avdta.network.ReadNetwork;
import avdta.network.Simulator;
import avdta.network.node.Zone;
import avdta.vehicle.PersonalVehicle;
import avdta.vehicle.StaticWallet;
import avdta.vehicle.Wallet;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author ml26893
 */
public class MTReadNetwork extends ReadNetwork
{
    public void readVehicles(Simulator sim, File demandfile) throws IOException
    {
        List<PersonalVehicle> vehicles = new ArrayList<PersonalVehicle>();
        
        Scanner filein = new Scanner(demandfile);
        
        while(filein.hasNext())
        {
            int id = filein.nextInt();
            int origin_id = filein.nextInt();
            int dest_id = filein.nextInt();
            
            filein.next();
            int dtime = filein.nextInt();
            int type = filein.nextInt();
  
            if(type != 501)
            {
                double vot = Simulator.dagum_rand();
            
                Wallet wallet = new StaticWallet(vot);
            
                Zone origin = (Zone)nodesmap.get(origin_id);
                Zone dest = (Zone)nodesmap.get(dest_id);
                
                origin.addProductions(1);
                dest.addAttractions(1);
                
                vehicles.add(new MTPersonalVehicle(id, origin, dest, dtime, vot));
            }
        }
        

        filein.close();

        sim.setVehicles(vehicles);
    }
    
}
