/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.microtoll;

import avdta.dta.ReadDTANetwork;
import avdta.network.Simulator;
import avdta.network.node.Zone;
import avdta.project.DTAProject;
import avdta.vehicle.PersonalVehicle;
import avdta.vehicle.wallet.StaticWallet;
import avdta.vehicle.Vehicle;
import avdta.vehicle.wallet.Wallet;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author ml26893
 */
public class MTReadNetwork extends ReadDTANetwork
{
    public List<Vehicle> readVehicles(DTAProject project) throws IOException
    {
        List<Vehicle> vehicles = new ArrayList<Vehicle>();
        
        Scanner filein = new Scanner(project.getDemandFile());
        filein.nextLine();
        
        while(filein.hasNext())
        {
            int id = filein.nextInt();
            int type = filein.nextInt();
            int origin_id = filein.nextInt();
            int dest_id = filein.nextInt();
            int dtime = filein.nextInt();
            double vot = filein.nextDouble();
            filein.nextLine();
  
            if(type != 501)
            {
                Wallet wallet = new StaticWallet(vot);
            
                Zone origin = (Zone)nodesmap.get(origin_id);
                Zone dest = (Zone)nodesmap.get(dest_id);
                
                origin.addProductions(1);
                dest.addAttractions(1);
                
                vehicles.add(new MTPersonalVehicle(id, origin, dest, dtime, vot));
            }
        }
        

        filein.close();

        return vehicles;
    }
}
