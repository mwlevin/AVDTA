/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netdesign;

// delete sim.vat, times file, demand file to reduce storage!

import java.io.IOException;


/**
 *
 * @author micha
 */
public abstract class GeneticAlgorithm<T extends Organism>
{
    
    
    
    public abstract boolean isFeasible(T org) throws IOException;
    
    public abstract void evaluate(T parent1, T parent2, T child) throws IOException;
    
    public abstract void mutate(T child) throws IOException;
    public abstract T cross(T parent1, T parent2) throws IOException;
}
