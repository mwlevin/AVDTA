/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pedestrian;

import java.util.List;

/**
 *
 * @author ml26893
 */
public class Main 
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        Node.MAX_QUEUE = 5;
        
        SimpleNode node = new SimpleNode(1200, 1200, 500, 500, 200, 200);
        
        MDP test = new MDP(node);
        
        test.valueIteration(0.0001);
        
        /*
        State x = new State(new int[]{1, 1, 1, 1});
        
        List<Action> U = node.createActions(x);
        
        for(Action u : U)
        {
            System.out.println(u);
            System.out.println(test.expJ(x, u));
        }
        */
    }
}
