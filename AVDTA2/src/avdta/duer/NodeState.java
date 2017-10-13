/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.duer;

import avdta.network.link.Link;
import avdta.network.node.Node;

/**
 *
 * @author hdx
 */
public class NodeState 
{
    public Node node;
    public Incident inci;
    public boolean info;
    
    public double util;
    public Link bestAction; 

    public NodeState(Node node, Incident inci , boolean info) 
    {
        this.node = node;
        this.inci = inci;
        this.info = info;
    }
}
