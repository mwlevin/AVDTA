/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netdesign;

// delete sim.vat, times file, demand file to reduce storage!

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 *
 * @author micha
 */
public abstract class GeneticAlgorithm<T extends Organism>
{
    private int population_size;
    private double proportion_kept;
    private double mutate_percent;
    
    private Map<Integer, List<T>> saved;
    
    
    public abstract boolean isFeasible(T org) throws IOException;
    
    public abstract void evaluate(T child) throws IOException;
    
    public abstract void mutate(T child) throws IOException;
    public abstract T cross(T parent1, T parent2) throws IOException;
    public abstract T createRandom() throws IOException;
    
    public void solve(int max_iters) throws IOException
    {
        List<T> population = new ArrayList<T>();
        
        for(int i = 0; i < population_size; i++)
        {
            T child = createRandom();
            calculateObj(child);
            population.add(child);
        }
        
        Collections.sort(population);
        
        int iteration = 1;
        
        while(iteration <= max_iters)
        {
            List<T> newPopulation = new ArrayList<T>();
            
            int parents = (int)(population_size * proportion_kept);
            for(int i = 0; i < parents; i++)
            {
                newPopulation.add(population.get(i));
            }
            
            for(int i = parents; i < population_size; i++)
            {
                T parent1 = newPopulation.get((int)(Math.random()*parents));
                T parent2;
                
                do
                {
                    parent2 = newPopulation.get((int)(Math.random()*parents));
                }
                while(parent1 != parent2);
                
                T child = cross(parent1, parent2);
                
                if(Math.random() < mutate_percent)
                {
                    mutate(child);
                }
                
                calculateObj(child);
                
                newPopulation.add(child);
            }
            
            population = newPopulation;
        }
    }
    
    public void calculateObj(T child) throws IOException
    {
        int hash = child.hashCode();
        
        if(saved.containsKey(hash))
        {
            for(T i : saved.get(hash))
            {
                if(i.equals(child))
                {
                    child.setObj(i.getObj());
                    child.setAssignment(i.getAssignment());
                    return;
                }
            }
        }
        
        evaluate(child);
        if(!saved.containsKey(hash))
        {
            saved.put(hash, new ArrayList<T>());
        }
        saved.get(hash).add(child);
    }
}
