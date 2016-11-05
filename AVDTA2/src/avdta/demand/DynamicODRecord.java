/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.demand;

import avdta.network.node.Node;
import avdta.project.DemandProject;
import java.io.Serializable;
import java.util.Scanner;
/**
 * This class represents an entry in the {@link DemandProject#getDynamicODFile()} file.
 * @author Michael
 */
public class DynamicODRecord implements Serializable 
{
    private int id, type, origin, ast, destination;
    private double demand;
    
    /**
     * Instantiates this {@link DynamicODRecord} with the given parameters.
     * @param id the id
     * @param type the vehicle type code
     * @param origin the origin node id
     * @param destination the destination node id
     * @param ast the assignment interval
     * @param demand the demand
     */
    public DynamicODRecord(int id, int type, int origin, int destination, int ast, double demand)
    {
        this.id = id;
        this.type = type;
        this.origin = origin;
        this.destination = destination;
        this.ast = ast;
        this.demand = demand;
    }
    
    /**
     * Instantiates this {@link DynamicODRecord} with the given parameters.
     * @param id the id
     * @param type the vehicle type code
     * @param origin the origin
     * @param destination the destination
     * @param ast the assignment interval
     * @param demand the demand
     */
    public DynamicODRecord(int id, int type, Node origin, Node destination, int ast, double demand)
    {
        this(id, type, origin.getId(), destination.getId(), ast, demand);
    }
    
    /**
     * Constructs this {@link StaticODRecord} from the given line of input data.
     * @param line the input data
     */
    public DynamicODRecord(String line)
    {
        Scanner chopper = new Scanner(line);
        
        id = chopper.nextInt();
        type = chopper.nextInt();
        origin = chopper.nextInt();
        destination = chopper.nextInt();
        ast = chopper.nextInt();
        demand = chopper.nextDouble();
    }
    
    /**
     * Returns the vehicle type code.
     * @return the vehicle type code
     */
    public int getType()
    {
        return type;
    }
    
    /**
     * Updates the vehicle type code.
     * @param type the new vehicle type coe
     */
    public void setType(int type)
    {
        this.type = type;
    }
    
    /**
     * Returns the id.
     * @return the id
     */
    public int getId()
    {
        return id;
    }
    
    /**
     * Updates the id.
     * @param id the new id
     */
    public void setId(int id)
    {
        this.id = id;
    }
    
    /**
     * Returns the id of the origin node.
     * @return the id of the origin node
     */
    public int getOrigin()
    {
        return origin;
    }
    
    /**
     * Updates the id of the origin node.
     * @param origin the id of the new origin node
     */
    public void setOrigin(int origin)
    {
        this.origin = origin;
    }
    
    /**
     * Updates the id of the origin node.
     * @param origin the new origin 
     */
    public void setOrigin(Node origin)
    {
        setOrigin(origin.getId());
    }
    
    /**
     * Returns the id of the destination node.
     * @return the id of the destination node
     */
    public int getDest()
    {
        return destination;
    }
    
    /**
     * Updates the id of the destination node.
     * @param dest the id of the new destination node
     */
    public void setDest(int dest)
    {
        this.destination = destination;
    }
    
    /**
     * Updates the id of the destination node.
     * @param dest the new destination 
     */
    public void setDest(Node dest)
    {
        setDest(dest.getId());
    }
    
    /**
     * Returns the demand.
     * @return the demand
     */
    public double getDemand()
    {
        return demand;
    }
    
    /**
     * Updates the demand.
     * @param demand the new demand
     */
    public void setDemand(double demand)
    {
        this.demand = demand;
    }
    
    /**
     * Returns the assignment interval.
     * @return the assignment interval
     */
    public int getAST()
    {
        return ast;
    }
    
    /**
     * Updates the assignment interval.
     * @param ast the new assignment interval
     */
    public void setAST(int ast)
    {
        this.ast = ast;
    }
    
    
    /**
     * Returns a {@link String} representation that can be written to the data file.
     * @return the {@link String} representation
     */
    public String toString()
    {
        return ""+id+"\t"+type+"\t"+origin+"\t"+destination+"\t"+ast+"\t"+demand;
    }
}
