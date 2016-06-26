/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.microtoll;

import avdta.network.link.Link;
import java.io.IOException;

/**
 *
 * @author ml26893
 */
public class MTMain 
{
    public static void main(String[] args) throws IOException
    {
        test("sf");
    }
    
    public static void test(String network) throws IOException
    {
        MTSimulator test = MTSimulator.readTBRNetwork(network, Link.CTM);
        
        test.simulate();
        
        System.out.println("TSTT:\t"+test.getTSTT()/3600.0+"\thr");
        System.out.println("Demand:\t"+test.getNumVehicles());
        System.out.println("Exiting:\t"+test.getNumExited());
    }
}
