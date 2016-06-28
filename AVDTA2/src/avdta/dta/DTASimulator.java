/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.dta;

import avdta.network.Path;
import avdta.network.Simulator;
import avdta.network.link.CentroidConnector;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.project.DTAProject;
import avdta.vehicle.DriverType;
import avdta.vehicle.PersonalVehicle;
import avdta.vehicle.fuel.VehicleClass;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author micha
 */
public class DTASimulator extends Simulator
{
    
    private int iteration;
    
    public DTASimulator(DTAProject project)
    {
        super(project);
    }
    public DTASimulator(DTAProject project, List<Node> nodes, List<Link> links)
    {
        super(project, nodes, links);
        
        iteration = 1;
    }
    
    public DTAResults partial_demand(int iter) throws IOException
    {
        List<PersonalVehicle> temp = new ArrayList<PersonalVehicle>();
        
        for(PersonalVehicle v : vehicles)
        {
            temp.add(v);
        }
        
        vehicles.clear();
        
        int count = 0;
        
        DTAResults output = null;
        
        if(print_status)
        {
            out.println("Iter\tStep\tGap %\tAEC\tTTT\tTrips\tNon-exiting\ttime");
        }
        
        
        for(int i = 0; i < iter; i++)
        {
            long time = System.nanoTime();
            
            Iterator<PersonalVehicle> iterator = temp.iterator();
            
            while(iterator.hasNext())
            {
                if(Math.random() < 1.0 / (iter - i))
                {
                    vehicles.add(iterator.next());
                    iterator.remove();
                    count++;
                }
                else
                {
                    iterator.next();
                }
            }
            
            Collections.sort(vehicles);
            output = pathgen(0);
            
            time = System.nanoTime() - time;
            
            if(print_status)
            {
                out.println((i+1)+"\t"+String.format("%.4f", 0.0)+"\t"+String.format("%.2f", output.getGapPercent())+"%\t"+
                        String.format("%.1f", output.getAEC())+"\t"+String.format("%.1f", output.getTSTT())+"\t"+output.getTrips()+"\t"+
                        output.getNonExiting()+"\t"+String.format("%.2f", time / 1.0e9));
            }
        }
        
        return output;
    }
    
    public DTAResults pathgen(double stepsize) throws IOException
    {
        int error_count = 0;

        Map<Node, Map<Node, Path[][]>> newpaths = new HashMap<Node, Map<Node, Path[][]>>();

        double tstt = 0;
        double min = 0;
        int exiting = 0;

        int count = 0;
        
        for(PersonalVehicle v : vehicles)
        {
            Node o = v.getOrigin();
            Node d = v.getDest();
            int ast = v.getAST();
            int dep_time = v.getDepTime();

            if(v.getExitTime() < Simulator.duration)
            {
                exiting++;
            }

            Map<Node, Path[][]> temp1;
            Path[][] temp2;

            if(newpaths.containsKey(o))
            {
                temp1 = newpaths.get(o);
            }
            else
            {
                newpaths.put(o, temp1 = new HashMap<Node, Path[][]>());
            }

            if(temp1.containsKey(d))
            {
                temp2 = temp1.get(d);
            }
            else
            {
                temp1.put(d, temp2 = new Path[Simulator.num_asts][2]);
            }

            if(temp2[ast][v.getDriver().typeIndex()] == null)
            {
                temp2[ast][v.getDriver().typeIndex()] = findPath(o, d, getAvgDepTime(ast), v.getVOT(), v.getDriver(), costFunc);
            }
            
            

            if(v.getPath() != null)
            {
                tstt += v.getPath().getAvgCost(dep_time, v.getVOT(), costFunc);
                //tt += v.getTT();


                if(v.getTT() < 0)
                {
                	out.println("TT < 0 "+v.getDepTime()+" "+v.getExitTime());
                }
            }
            min += temp2[ast][v.getDriver().typeIndex()].getAvgCost(dep_time, v.getVOT(), costFunc);


            // move vehicle random chance
            if(v.getPath() == null || rand.nextDouble() < stepsize)
            {
                try
                {
                    v.setPath(temp2[ast][v.getDriver().typeIndex()]);
                }
                catch(Exception ex)
                {
                    out.println("Path unable: "+o+" "+d);
                    for(Link l : d.getIncoming())
                    {
                        out.println((l instanceof CentroidConnector)+" "+l.getSource());
                    }
                    error_count++;
                }
            }
            

            count ++;

        }
        
        if(error_count > 0)
        {
        	System.err.println("Unable: "+error_count);
        }

        simulate(false);
        

        if(tstt == 0)
        {
            tstt = min;
        }


        return new DTAResults(min, tstt, vehicles.size(), exiting);

    }
    
    public int getIteration()
    {
        return iteration;
    }
    
    public DTAResults msa(int max_iter) throws IOException
    {
        return msa(max_iter, -1);
    }
    
    public DTAResults msa(int max_iter, double min_gap) throws IOException
    {
        return msa_cont(1, max_iter, min_gap);
    }
    
    public DTAResults msa_cont(int max_iter, double min_gap) throws IOException
    {
        return msa_cont(iteration, max_iter, min_gap);
    }
    
    public DTAResults msa_cont(int start_iter, int max_iter) throws IOException
    {
        return msa_cont(start_iter, max_iter, -1);
    }
    
    public DTAResults msa_cont(int start_iter, int max_iter, double min_gap) throws IOException
    {
        if(statusUpdate != null)
        {
            statusUpdate.update(0);
        }
        
        
        PrintStream fileout = new PrintStream(new FileOutputStream(new File(getProject().getResultsFolder()+"/log.txt")), true);
        
        iteration = start_iter;
        DTAResults output = null;

        if(print_status)
        {
            out.println("Iter\tStep\tGap %\tAEC\tTTT\tTrips\tNon-exit\ttime");
        }
        
        fileout.println("Iter\tStep\tGap %\tAEC\tTTT\tTrips\tNon-exit\ttime");

        do
        {
            long time = System.nanoTime();

            double stepsize = 1.0/iteration;
            output = pathgen(stepsize);


            time = System.nanoTime() - time;

            if(print_status)
            {
                out.println(iteration+"\t"+String.format("%.4f", stepsize)+"\t"+String.format("%.2f", output.getGapPercent())+"%\t"+
                        String.format("%.1f", output.getAEC())+"\t"+String.format("%.1f", output.getTSTT())+"\t"+output.getTrips()+"\t"+
                        output.getNonExiting()+"\t"+String.format("%.2f", time / 1.0e9));
            }
            fileout.println(iteration+"\t"+String.format("%.4f", stepsize)+"\t"+String.format("%.2f", output.getGapPercent())+"%\t"+
                        String.format("%.1f", output.getAEC())+"\t"+String.format("%.1f", output.getTSTT())+"\t"+output.getTrips()+"\t"+
                        output.getNonExiting()+"\t"+String.format("%.2f", time / 1.0e9));

            if(statusUpdate != null)
            {
                statusUpdate.update((double)iteration / max_iter);
            }
        }
        while(iteration++ < max_iter && (iteration == 2 || min_gap < output.getGapPercent()));
        
        if(statusUpdate != null)
        {
            statusUpdate.update(1);
        }

        out.println(String.format("TSTT\t%.1f", getTSTT()/3600.0)+"\thr\nAvg. TT\t"+String.format("%.2f", getTSTT() / 60 / vehicles.size())+"\tmin/veh");
        
        fileout.println(String.format("TSTT\t%.1f", getTSTT()/3600.0)+"\thr\nAvg. TT\t"+String.format("%.2f", getTSTT() / 60 / vehicles.size())+"\tmin/veh");
        fileout.println("Energy:\t"+getTotalEnergy());
        fileout.println("VMT:\t"+getTotalVMT());
        fileout.println("MPG:\t"+(getTotalVMT() / (getTotalEnergy() / VehicleClass.E_PER_GALLON)));
        fileout.println("HV TT:\t"+(getAvgTT(DriverType.HV)/60)+"\tmin");
        fileout.println("AV TT:\t"+(getAvgTT(DriverType.AV)/60)+"\tmin");
        
        fileout.close();
        
        //simulate(true);

        return output;
    }
}
