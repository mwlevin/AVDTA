/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.dta;

import java.util.Scanner;

/**
 * Used for prepare demand
 * @author micha
 */
public class VehicleRecord implements Comparable<VehicleRecord>
{
    private int id, dtime, origin, dest, type;
    
    public VehicleRecord(String line)
    {
        Scanner chopper = new Scanner(line);
        
        id = chopper.nextInt();
        type = chopper.nextInt();
        origin = chopper.nextInt();
        dest = chopper.nextInt();
        dtime = chopper.nextInt();
    }
    
    public VehicleRecord(int id, int type, int origin, int dest, int dtime)
    {
        this.id = id;
        this.dtime = dtime;
        this.origin = origin;
        this.dest = dest;
        this.type = type;
    }
    
    public String toString()
    {
        return id+"\t"+type+"\t"+origin+"\t"+dest+"\t"+dtime;
    }
    
    public int compareTo(VehicleRecord rhs)
    {
        if(dtime != rhs.dtime)
        {
            return dtime - rhs.dtime;
        }
        else
        {
            return id - rhs.id;
        }
    }
}
