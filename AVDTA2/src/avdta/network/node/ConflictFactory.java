/*
 * To change this template, choose Tools | Templates
 * and open the template in the ed_intor.
 */
package avdta.network.node;

import avdta.network.Simulator;
import avdta.network.link.CentroidConnector;
import avdta.network.link.Link;
import avdta.network.node.TurningMovement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This class generates {@link ConflictRegion}s for a node based on the radial division model. The angles for the radial division use the {@link Link} start and ending {@link Location}s.
 * 
 * @author Michael
 */
public class ConflictFactory
{
    /**
    * Creates a map containing the set of {@link ConflictRegion}s that vehicles turning from an incoming {@link Link} to an outgoing {@link Link} at the specified {@link Node} will pass through.
    *
    * @param node the {@link Node} for the {@link ConflictRegion}s
    * @return map of {@link ConflictRegion}s
    */
    public static Map<Link, Map<Link, TurningMovement>> generate(Node node)
    {
        
        Map<Node, Double> directions = new HashMap<>();
        
        List<Node> links = new ArrayList<>();
        
        for(Link l : node.getIncoming())
        {
            if(!(l instanceof CentroidConnector))
            {
                links.add(l.getSource());
            }
        }
        
        for(Link l : node.getOutgoing())
        {
            if(!(l instanceof CentroidConnector))
            {
                if(!links.contains(l.getDest()))
                {
                    links.add(l.getDest());
                }
            }
        }
        
        Collections.sort(links, new Comparator<Node>()
        {
            public int compare(Node lhs, Node rhs)
            {
                double d1 = node.angleTo(lhs);
                double d2 = node.angleTo(rhs);
                
                if(d1 < d2)
                {
                    return -1;
                }
                else if(d1 > d2)
                {
                    return 1;
                }
                else
                {
                    return 0;
                }
            }
        });
        
        
        directions.put(links.get(0), 0.0);
        
        for(int i = 1; i < links.size(); i++)
        {
            double angle = node.angleTo(links.get(i)) - node.angleTo(links.get(0));
            angle = Math.PI/2 * Math.round(angle / (Math.PI/2));
            directions.put(links.get(i), angle);
        }
        

        
        
        
        Map<Link, Map<Link, TurningMovement>> output1 = new HashMap<Link, Map<Link, TurningMovement>>();
        

        TreeMap<Double, ConflictRegion> division = new TreeMap<Double, ConflictRegion>();

        for(Link l : node.getIncoming())
        {
            if(!l.isCentroidConnector())
            {
                double direction = directions.get(l.getSource()) + Math.PI;

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
                double direction = directions.get(l.getDest());

                // reverse it
                direction += Math.PI;

                if(direction >= 2*Math.PI)
                {
                    direction -= 2*Math.PI;
                }

                boolean contains = false;
                
                for(double dir : division.keySet())
                {
                    if(Math.abs(dir - direction) < 0.01)
                    {
                        contains = true;
                        break;
                    }
                }
                if(!contains)
                {
                    division.put(direction, new ConflictRegion(0));
                    // update capacities later depending on which turning movements pass through
                }
            }
        }


  

        double shift_epsilon = 0.1;
        double step = 1.0/3;

        for(Link i : node.getIncoming())
        {
            Map<Link, TurningMovement> temp;
            output1.put(i, temp = new HashMap<Link, TurningMovement>());

            // find starting point
            double start_dir = directions.get(i.getSource());
            
            if(start_dir >= 2*Math.PI)
            {
                start_dir -= 2*Math.PI;
            }
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

                double end_dir = directions.get(j.getDest());
                
                end_dir = start_dir + Math.PI /2 * Math.round( (end_dir - start_dir) / (Math.PI/2));
                   
                
                
                
                shift_dir = end_dir - Math.PI/2;

                double end_x = Math.cos(end_dir) + Math.cos(shift_dir)*shift_epsilon;
                double end_y = Math.sin(end_dir) + Math.sin(shift_dir)*shift_epsilon;

                double move_dir = Math.atan2(end_x - start_x, end_y - start_y);
                
                double x = start_x;
                double y = start_y;
                
                
                
                
                
                // straight line
  
                
                double start_dir_opp = start_dir + Math.PI;
                
                if(start_dir_opp >= 2*Math.PI)
                {
                    start_dir_opp -= 2* Math.PI;
                }
                if(Math.abs(start_dir_opp -end_dir) < 0.01)
                {
                    double a = end_x - start_x;
                    double b = end_y - start_y;


                    for(double t = 0; t < 1; t += step)
                    {
                        x += a * step;
                        y += b * step;

                        ConflictRegion cp = findConflict(division, x, y);

                        //cp.setCapacity(Math.max(cp.getCapacity(), Math.min(i.getCapacity(), j.getCapacity())));
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

                    double dir1 = start_dir + Math.PI;
                    double dir2 = end_dir + Math.PI;

                    double a1 = start_x + 2*Math.cos(dir1);
                    double b1 = start_y + 2*Math.sin(dir1);

                    double a2 = end_x + 2*Math.cos(dir2);
                    double b2 = end_y + 2*Math.sin(dir2);

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
                        //cp.setCapacity(Math.max(cp.getCapacity(), Math.min(i.getCapacity(), j.getCapacity())));
                        conflicts.add(cp);

                        cp = findConflict(division, x2, y2);
                        //cp.setCapacity(Math.max(cp.getCapacity(), Math.min(i.getCapacity(), j.getCapacity())));
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
