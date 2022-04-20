/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.demand;

import avdta.dta.VehicleRecord;
import avdta.network.node.Node;
import avdta.network.node.Zone;
import avdta.project.DemandProject;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

/**
 * This class provides an interface for modifying and printing the dynamic OD and static OD tables.
 * Note that the class references assignment intervals in the project's demand profile table (see {@link DemandProfile}).
 * @author Michael
 */
public class StaticODTable implements Iterable<StaticODRecord>
{
    // the order is origin, dest, type, ast
    private Map<Integer, Map<Integer, Map<Integer, Double>>> table;
    private double total;
    
    /**
     * Constructs an empty dynamic OD table.
     */
    public StaticODTable()
    {
        table = new HashMap<Integer, Map<Integer, Map<Integer, Double>>>();
    }
    
    public Iterator<StaticODRecord> iterator()
    {
        return new TableIterator();
    }
    
    /**
     * Empties all entries in this dynamic OD table.
     */
    public void clear()
    {
        table.clear();
        total = 0;
    }
    
    /**
     * Reads in the dynamic OD table from the specified project (see {@link DemandProject#getDynamicODFile()}).
     * @param project the project
     * @throws IOException if a file cannot be accessed
     */
    public StaticODTable(DemandProject project) throws IOException
    {
        this();
        
        Scanner filein = new Scanner(project.getStaticODFile());
        filein.nextLine();
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            int type = filein.nextInt();
            int o = filein.nextInt();
            int d = (int)Math.abs(filein.nextInt());
            double dem = filein.nextDouble();
            filein.nextLine();
            
            addDemand(o, d, type, dem);
            total += dem;
        }
        
        filein.close();
    }
    
    public double getTotal()
    {
        return total;
    }
    
    
    /**
     * Adds the specified {@link DynamicODRecord} to the table.
     * @param odt the {@link DynamicODRecord} to be added
     */
    public void addDemand(StaticODRecord odt)
    {
        addDemand(odt.getOrigin(), odt.getDest(), odt.getType(), odt.getDemand());
        total += odt.getDemand();
    }
    
    /**
     * Adds the specified amount of demand.
     * @param origin the origin
     * @param dest the destination
     * @param ast the assignment interval
     * @param type the type code of the vehicle
     * @param dem the amount of demand
     */
    public void addDemand(Zone origin, Zone dest,int type, double dem)
    {
        addDemand(origin.getId(), dest.getId(),type, dem);
    }
    
    /**
     * Adds the specified amount of demand.
     * @param origin the origin
     * @param dest the destination
     * @param ast the assignment interval
     * @param type the type code of the vehicle
     * @param dem the amount of demand
     */
    public void setDemand(Zone origin, Zone dest, int type, double dem)
    {
        setDemand(origin.getId(), dest.getId(),type, dem);
    }
    
    /**
     * Adds the specified amount of demand.
     * @param origin the origin
     * @param dest the destination
     * @param ast the assignment interval
     * @param type the type code of the vehicle
     * @param dem the amount of demand
     */
    public void setDemand(int origin, int dest, int type, double dem)
    {
        if(dem <= 0.0)
        {
            return;
        }
        
        dest = (int)Math.abs(dest);

        
        Map<Integer, Map<Integer, Double>> temp1;
        
        if(table.containsKey(origin))
        {
            temp1 = table.get(origin);
        }
        else
        {
            table.put(origin, temp1 = new HashMap<Integer, Map<Integer, Double>>());
        }
        
        Map<Integer, Double> temp2;
        
        if(temp1.containsKey(dest))
        {
            temp2 = temp1.get(dest);
        }
        else
        {
            temp1.put(dest, temp2 = new HashMap<Integer, Double>());
        }
        
        

        
        if(temp2.containsKey(type))
        {
            temp2.put(type, temp2.get(type) + dem);
        }
        else
        {
            temp2.put(type, dem);
        }
        
        total += dem;

        
    }
    
    /**
     * Adds the specified amount of demand.
     * @param origin the origin
     * @param dest the destination
     * @param ast the assignment interval
     * @param type the type code of the vehicle
     * @param dem the amount of demand
     */
    public void addDemand(int origin, int dest, int type, double dem)
    {
        if(dem <= 0.0)
        {
            return;
        }
        
        dest = (int)Math.abs(dest);
   
        
        Map<Integer, Map<Integer, Double>> temp1;
        
        if(table.containsKey(origin))
        {
            temp1 = table.get(origin);
        }
        else
        {
            table.put(origin, temp1 = new HashMap<Integer, Map<Integer, Double>>());
        }
        
        Map<Integer, Double> temp2;
        
        if(temp1.containsKey(dest))
        {
            temp2 = temp1.get(dest);
        }
        else
        {
            temp1.put(dest, temp2 = new HashMap<Integer, Double>());
        }
        
        Map<Integer, Double> temp3;
        
        if(temp2.containsKey(type))
        {
            total += dem - temp2.get(type);
            temp2.put(type, temp2.get(type) + dem);
        }
        else
        {
            temp2.put(type, dem);
            total += dem;
        }
        

    }
    
    /**
     * Adds the specified {@link VehicleRecord}: calls {@link DynamicODTable#addDemand(int, int, int, int, double)} with the appropriate parameters.
     * @param trip the trip
     * @param profile the demand profile
     */
    public void addDemand(VehicleRecord trip)
    {
        addDemand(trip.getOrigin(), trip.getDestination(), trip.getType(), 1);
    }
    
    /**
     * Prints the dynamic OD table to the specified project (see {@link DemandProject#getDynamicODFile()}).
     * @param project the project
     * @throws IOException if a file cannot be accessed
     */
    public void printStaticOD(DemandProject project) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getDynamicODFile()), true);
        
        fileout.println(ReadDemandNetwork.getDynamicODFileHeader());
        
        int id = 1;
        
        for(int o : table.keySet())
        {
            Map<Integer, Map<Integer, Double>> temp1 = table.get(o);
            
            for(int d : temp1.keySet())
            {
                Map<Integer, Double> temp2 = temp1.get(d);
                
                for(int type : temp2.keySet())
                {
                    fileout.println((id++)+"\t"+type+"\t"+o+"\t"+d+"\t"+temp2.get(type));                   
                }
            }
        }
        
        fileout.close();
    }
    
    public boolean isEmpty()
    {
        return table.isEmpty();
    }
    
    

    /**
     * Returns the number of trips for the specified parameters. 
     * @param origin the origin
     * @param dest the destination
     * @param ast the assignment interval
     * @param type the type
     * @return the number of trips
     */
    public double getTrips(Node origin, Node dest, int type)
    {
        return getTrips(origin.getId(), dest.getId(), type);
    }
    
    /**
     * Returns the number of trips for the specified parameters. 
     * @param origin the origin id
     * @param dest the destination id
     * @param ast the assignment interval
     * @param type the type
     * @return the number of trips
     */
    public double getTrips(int origin, int dest, int type)
    {
        dest = (int)Math.abs(dest);
        try
        {
            return table.get(origin).get(dest).get(type);
        }
        catch(NullPointerException ex)
        {
            return 0.0;
        }
    }
    
    public double getTrips(Node origin, Node dest)
    {
        return getTrips(origin.getId(), dest.getId());
    }
    
    public double getTrips(int origin, int dest)
    {
        dest = (int)Math.abs(dest);
        
        try
        {
            Map<Integer, Double> temp = table.get(origin).get(dest);
            
            double output = 0.0;
            for(int type : temp.keySet())
            {
                output += temp.get(type);
            }
            
            return output;
        }
        catch(NullPointerException ex)
        {
            return 0.0;
        }
    }
   
    
    
    private void delete(int o, int d, int type)
    {
        remove(o, d, type);
    }
    
    public void remove(int o, int d, int type)
    {
        d = (int)Math.abs(d);
        
        table.get(o).get(d).remove(type);

        if(table.get(o).get(d).isEmpty())
        {
            table.get(o).remove(d);
        }
        
        if(table.get(o).isEmpty())
        {
            table.remove(o);
        }
    }
    
    class TableIterator implements Iterator<StaticODRecord> 
    {
        private int o, d, type;

        private Iterator<Integer> origins;
        private Iterator<Integer> destinations;

        private Iterator<Integer> types;

        public TableIterator() 
        {
            origins = table.keySet().iterator();
            destinations = null;
     
            types = null;
        }
        
        public boolean hasNext()
        {
            if(types != null && types.hasNext())
            {
                return true;
            }
            

            
            if(destinations != null && destinations.hasNext())
            {
                d = destinations.next();
                
                types = table.get(o).get(d).keySet().iterator();
                
                return hasNext();
            }
            
            if(origins != null && origins.hasNext())
            {
                o = origins.next();
                
                destinations = table.get(o).keySet().iterator();
      
                return hasNext();
            }
            
            return false;
        }
        

        public StaticODRecord next()
        {
            if(types != null && types.hasNext())
            {
                type = types.next();
                double trips = table.get(o).get(d).get(type);
                
                return new StaticODRecord(0, type, o, d, trips);
            }

            
            if(destinations != null && destinations.hasNext())
            {
                d = destinations.next();
                
                types = table.get(o).get(d).keySet().iterator();
                
                return next();
            }
            
            if(origins != null && origins.hasNext())
            {
                o = origins.next();
                
                destinations = table.get(o).keySet().iterator();

                return next();
            }
            
            return null;
            
        }
        
        public void remove()
        {
            delete(o, d, type);
        }

    }
    
}
