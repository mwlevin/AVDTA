/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pedestrian;

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
        SimpleNode node = new SimpleNode(1200, 1200, 500, 500, 200, 200);
        
        MDP test = new MDP(node);
        
        State state = new State(new int[]{3, 3, 1, 4});
        System.out.println(node.createActions(state));
    }
}
