/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

import avdta.network.Network;
import avdta.network.Simulator;
import avdta.network.node.Node;

/**
 *
 * @author ml26893
 */
public class CACCLTMLink extends LTMLink
{
    public double k2, wavespd2, kc, wavespd1;
    
    public static double scaleCapacity(double ffspd)
    {
        double C5 = 4.46;
        double C4 = 0.6;
        
        double B20 = ffspd;
        double C20 = B20*0.44704;
        double F20 = C4 * C20;
        double J20 = 1.0/(F20+C5);
        
        
        double N20 = C20 * J20;
        
        return N20 * 3600;
    }
    
    public static double scaleWavespd(double ffspd)
    {
        double C5 = 4.46;
        double C4 = 0.6;
        
        double B20 = ffspd;
        double C20 = B20*0.44704;
        double F20 = C4 * C20;
        double F10 = 0;
        double J20 = 1.0/(F20+C5);
        double J10 = 1/(F10+C5);
        
        double N20 = C20 * J20;
        
        
        
        double wavespd = N20 / (J10-J20); //m/s
        
        wavespd = wavespd / 1609.34;
        wavespd = wavespd * 3600;
        
        return wavespd;
    }
    public CACCLTMLink(int id, Node source, Node dest, double capacity, double ffspd, double wavespd1, double jamd, double length, int numLanes)
    {
        super(id, source, dest, capacity=scaleCapacity(ffspd), ffspd, wavespd1 = scaleWavespd(ffspd), jamd = 1.0/(6.46) * 1609.34, length, numLanes);
        this.wavespd1 = wavespd1;
        
        wavespd2 = getLength() / Network.dt * 3600.0;
        kc = capacity / ffspd;
        
        k2 = (wavespd2 * getJamDensityPerLane() - wavespd1 * kc - capacity) / (wavespd2 - wavespd1);
        
        //System.out.println(wavespd1+" "+wavespd2+" "+capacity+" "+kc+" "+k2+" "+getJamDensityPerLane()+" "+numLanes);
        
        if(k2 <= kc)
        {
            throw new RuntimeException("Link is too short");
        }
        
        k2 *= getNumLanes();
    }
    
    public static boolean checkK2(double capacity, double ffspd, double length)
    {
        double wavespd1 = scaleWavespd(ffspd);
        capacity = scaleCapacity(ffspd);
        
        double wavespd2 = length / Network.dt * 3600.0;
        double kc = capacity / ffspd;
        double jamd = jamd = 1.0/(6.46) * 1609.34;
        
        double k2 = (wavespd2 * jamd - wavespd1 * kc - capacity) / (wavespd2 - wavespd1);
        
        return k2 > kc;
    }
    
    public double getReceivingFlow()
    {
        
        double output = 
         Math.min(Math.min(getN_down(Simulator.time - getLength()/wavespd2*3600 + Network.dt) 
                + getJamDensity() * getLength() - getN_up(Simulator.time),
                getN_down(Simulator.time - getLength()/wavespd1*3600 + Network.dt) 
                + k2 * getLength() - getN_up(Simulator.time)),
                getCurrentUpstreamCapacity());
        
        /*
        System.out.println("R= "+output);
        if(output < -1)
        {
            System.out.println("\t"+k2+" "+kc+" "+getJamDensity()+" "+getNumLanes());
        }
        */
        
        return output;
    }
    
    public int getDSLookBehind()
    {
        return (int)Math.ceil(Math.max(getLength()/wavespd1*3600, getLength()/wavespd2*3600) / Network.dt);
    }
}
