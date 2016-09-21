/*
 * To change this template, choose Tools | Templates
 * and open the template in the ed_intor.
 */
package avdta.network.node;

import avdta.network.link.Link;
import avdta.network.node.TurningMovement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This class generates ConflictRegions for a node based on the radial division model. The angles for the radial division use the Link start and ending coordinates.
 * 
 * @author Michael
 */
public class ConflictFactory
{
    /**
    * Creates a map containing the set of ConflictRegions that vehicles turning from an incoming link to an outgoing link at the specified node will pass through.
    *
    * @param node the Node for the conflict regions
    * @return map of ConflictRegions
    */
    public static Map<Link, Map<Link, TurningMovement>> generate(Node node)
    {
        Map<Link, Map<Link, TurningMovement>> output1 = new HashMap<Link, Map<Link, TurningMovement>>();
        

        TreeMap<Double, ConflictRegion> division = new TreeMap<Double, ConflictRegion>();

        for(Link l : node.getIncoming())
        {
            if(!l.isCentroidConnector())
            {
                double direction = l.getDirection();

                if(direction < 0)
                {
                    direction += 2*Math.PI;
                }

                if(!division.containsKey(direction))
                {
                    division.put(direction, new ConflictRegion(0));
                    // update capacities later depending on which turning movements pass through
                }
            }
        }

        for(Link l : node.getOutgoing())
        {
            if(!l.isCentroidConnector())
            {
                double direction = l.getDirection();

                // reverse it
                direction += Math.PI;

                if(direction >= 2*Math.PI)
                {
                    direction -= 2*Math.PI;
                }

                if(!division.containsKey(direction))
                {
                    division.put(direction, new ConflictRegion(0));
                    // update capacities later depending on which turning movements pass through
                }
            }
        }





        double shift_epsilon = 0.01;
        double step = 1.0/3;

        for(Link i : node.getIncoming())
        {
            Map<Link, TurningMovement> temp;
            output1.put(i, temp = new HashMap<Link, TurningMovement>());

            // find starting point
            double start_dir = i.getDirection() + Math.PI;
            // shift by epsilon
            double shift_dir = start_dir + Math.PI/2;

            double start_x = Math.cos(start_dir) + Math.cos(shift_dir)*shift_epsilon;
            double start_y = Math.sin(start_dir) + Math.sin(shift_dir)*shift_epsilon;

            for(Link j : node.getOutgoing())
            {
                TurningMovement conflicts;
                temp.put(j, conflicts = new TurningMovement());

                if(i.isCentroidConnector() || j.isCentroidConnector())
                {
                    continue;
                }

                // ignore U-turns for now
                if(i.getSource() == j.getDest())
                {
                    continue;
                }

                double end_dir = j.getDirection();
                shift_dir = end_dir - Math.PI/2;

                double end_x = Math.cos(end_dir) + Math.cos(shift_dir)*shift_epsilon;
                double end_y = Math.sin(end_dir) + Math.sin(shift_dir)*shift_epsilon;

                double move_dir = Math.atan2(end_x - start_x, end_y - start_y);
                
                double x = start_x;
                double y = start_y;
                
                // straight line
                if(end_dir + Math.PI == start_dir)
                {
                    double a = end_x - start_x;
                    double b = end_y - start_y;


                    for(double t = 0; t < 1; t += step)
                    {
                        x += a * step;
                        y += b * step;

                        ConflictRegion cp = findConflict(division, x, y);

                        cp.setCapacity(Math.max(cp.getCapacity(), Math.min(i.getCapacity(), j.getCapacity())));
                        conflicts.add(cp);


                    }
                    
                    
                }
                // turn
                else
                {
                    double x1 = start_x;
                    double y1 = start_y;

                    double x2 = end_x;
                    double y2 = end_y;

                    double dir1 = i.getDirection();
                    double dir2 = j.getDirection() + Math.PI;

                    double a1 = Math.cos(dir1);
                    double b1 = Math.sin(dir1);

                    double a2 = Math.cos(dir2);
                    double b2 = Math.sin(dir2);

                    // solve system of equations
                    double s = (y1 + b1/a1*x2 - b1/a1*x1 - y2) / (b2 - b1/a1*a2);
                    double t;

                    if(Math.abs(a1) > 0.01)
                    {
                        t = (x2 + a2*s - x1)/a1;
                    }
                    else
                    {
                        t = (y2 + b2*s - y1)/b1;
                    }





                    for(double k = 0; k <= 1; k += step)
                    {
                        ConflictRegion cp = findConflict(division, x1, y1);
                        cp.setCapacity(Math.max(cp.getCapacity(), Math.min(i.getCapacity(), j.getCapacity())));
                        conflicts.add(cp);

                        cp = findConflict(division, x2, y2);
                        cp.setCapacity(Math.max(cp.getCapacity(), Math.min(i.getCapacity(), j.getCapacity())));
                        conflicts.add(cp);

                        x1 += a1 * t * step;
                        y1 += b1 * t * step;

                        x2 += a2 * s * step;
                        y2 += b2 * s * step;
                    }
                }

            }
        }


        return output1;
    }

    private static ConflictRegion findConflict(TreeMap<Double, ConflictRegion> division, double x, double y)
    {
        double dir = Math.atan2(y, x);
        if(dir < 0)
        {
            dir += 2*Math.PI;
        }


        ConflictRegion output = null;

        for(double k : division.keySet())
        {
            if(k > dir)
            {
                output = division.get(k);
                break;
            }
        }

        if(output == null)
        {
            output = division.get(division.firstKey());
        }

        return output;
    }

    private static Link findClosestLink(Set<Link> links, double angle)
    {
        double diff = Integer.MAX_VALUE;
        Link output = null;

        for(Link l : links)
        {
            if(l.isCentroidConnector())
            {
                continue;
            }

            double temp = Math.abs(l.getDirection() - angle);

            if(temp < diff)
            {
                diff = temp;
                output = l;
            }
        }

        return output;
    }

}
