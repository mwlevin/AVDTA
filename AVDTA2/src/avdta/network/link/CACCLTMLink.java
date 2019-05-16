


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

import avdta.network.Network;
import avdta.network.ReadNetwork;
import avdta.network.Simulator;
import avdta.network.node.Node;
import avdta.network.type.Type;
import avdta.vehicle.DriverType;
import avdta.vehicle.VehTime;
import avdta.vehicle.Vehicle;

/**
 * This models the cooperative adaptive cruise control, which has several effects on vehicle behavior. 
 * Vehicles can move even at jam density, and the congested wave speed is much higher.
 * The fundamental diagram is based on the MIXIC car following model.
 * @author Michael
 */
public class CACCLTMLink extends LTMLink
{
    public double k2, wavespd2, kc, wavespd1;
    
    /**
     * Scales the capacity according to MIXIC based on the free flow speed
     * @param ffspd the free flow speed (mi/hr)
     * @return the scaled capacity
     */
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

    
    /**
     * Scales the congested wave speed according to MIXIC based on the free flow speed
     * @param ffspd the free flow speed (mi/hr)
     * @return the scaled congested wave speed
     */
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
    
    /**
     * Checks whether the length and free flow speed are sufficiently high to allow cooperative adaptive cruise control
     * @param capacity the capacity (veh/hr)
     * @param ffspd the free flow speed (mi/hr)
     * @param length the length (mi)
     * @return whether the fundamental diagram is valid
     */
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
    
    /**
     * Constructs the link with the given parameters. The second congested wave speed is calculated based on the remaining parameters.
     * @param id the link id
     * @param source the source node
     * @param dest the destination node
     * @param capacity the capacity per lane (veh/hr)
     * @param ffspd the free flow speed (mi/hr)
     * @param wavespd1 the first congested wave speed in the congested regime (mi/hr)
     * @param jamd the jam density (veh/mi)
     * @param length the length (mi)
     * @param numLanes the number of lanes
     */
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
    
    
    /**
     * Returns the receiving flow, which is based on a piecewise concave fundamental diagram.
     * @return the receiving flow
     */

    
    
       public double getReceivingFlow()
    {
        double A =Math.round(getN_down(Simulator.time - getLength()/(0.7143/1609.344*3600) + Network.dt) 
                + 1.08988*1609.344* getLength() - getN_up(Simulator.time));
        double B =Math.round(getN_down(Simulator.time - getLength()/(2.2373/1609.344*3600) + Network.dt) 
                + 0.42967*1609.344 * getLength()- getN_up(Simulator.time));
        double inner=  Math.min(A,B);
        
        double output =Math.min(inner,getCurrentUpstreamCapacity());
                /*Math.min(getJamDensity() * getLength() - getOccupancy(), getCurrentUpstreamCapacity()));*/
                /*
        System.out.println("inner = "+inner);
        System.out.println("A = "+ A);
        System.out.println("B = "+ B);
          System.out.println("output for receiving ="+output);*/
        return output;
    }
       
        public int getNumSendingFlow()
    {
        double A=Math.round(getN_up(Simulator.time - getLength()/(21.213/1609.344) + Network.dt) - getN_down(Simulator.time));
        double B=Math.round(getN_up(Simulator.time - getLength()/(7.0711/1609.344) + Network.dt) +0.04*1609.344 * getLength() - getN_down(Simulator.time));
        double value1 =Math.min(A,B);
        //System.out.println(getN_up(Simulator.time - getLength()/21.213*3600 + Network.dt) - getN_up(Simulator.time));
        
        //System.out.println("A=" +A);
        //System.out.println("B=" +B);
        
        
        double value2 =Math.min( value1, Math.round(getN_up(Simulator.time - getLength()/(4.1194/1609.344)+Network.dt) +0.0973*1609.344*getLength()- getN_down(Simulator.time)));
        
        
        double value3 = Math.min( value2,Math.round(getN_up(Simulator.time - getLength()/(2.2373/1609.344)+Network.dt) + 0.2297*1609.344 *getLength()- getN_down(Simulator.time)));
        
        double inner = Math.min( value3,Math.round(getN_up(Simulator.time - getLength()/(0.7143/1609.344)+Network.dt) +0.89*1609.344*getLength()- getN_down(Simulator.time)));
     
        int output = (int)Math.min(inner ,getCurrentDownstreamCapacity());
        //System.out.println(inner);
        if(getId() == 23)
        {
            System.out.println("A=" +A);
            System.out.println("B=" +B);
            System.out.println("Value1 = "+value1);
            System.out.println("Value2 ="+value2);
            System.out.println("Value3 ="+value3);
            System.out.println("Occupancy = "+getOccupancy());
            System.out.println("getCurrentDownstreamCapacity() ="+getCurrentDownstreamCapacity());
            
                    /*Math.min(getJamDensity() * getLength() - getOccupancy(), getCurrentUpstreamCapacity()));*/
            System.out.println("output for sending ="+output);
        }
        return output;
    }
       
    /**
     * Returns how far to look backwards in time for the downstream end
     * @return {@link Link#getLength()}/{@link Link#getWaveSpeed()} (s)
     */
    /* old :public int getDSLookBehind()
    {
        return (int)Math.ceil(Math.max(getLength()/wavespd1*3600, getLength()/wavespd2*3600) / Network.dt);
    }
    
        /**
     * Returns how far to look backwards in time for the upstream end
     * @return {@link Link#getLength()}/{@link Link#getFFSpeed()} (s)
     */
    public int getUSLookBehind()
    {
        return (int)Math.ceil(getLength()/(0.7143/1609.344)/ Network.dt);
    }
    
    /**
     * Returns how far to look backwards in time for the downstream end
     * @return {@link Link#getLength()}/{@link Link#getWaveSpeed()} (s)
     */
    public int getDSLookBehind()
    {
        return (int)Math.ceil(getLength()/(0.7143/1609.344) / Network.dt);
    }
    
    
    /**
     * Returns whether the given {@link DriverType} can use this link.
     * @param driver the {@link DriverType}
     * @return if the {@link DriverType} is a connected vehicle
     */
    public boolean canUseLink(DriverType driver)
    {
        return driver.isCV();
    }
    
    public Type getType()
    {
        return ReadNetwork.CACC;
    }
}