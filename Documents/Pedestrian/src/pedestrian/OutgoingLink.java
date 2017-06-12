/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pedestrian;

/**
 *
 * @author ml26893
 */
public class OutgoingLink extends Link
{
    public double R;
    public int y;
    
    public OutgoingLink(Node n, double capacity, double width)
    {
        super(n, capacity, width);
    }
    
    public double getReceivingFlow(double dt)
    {
        return getCapacity() * dt;
    }
}
