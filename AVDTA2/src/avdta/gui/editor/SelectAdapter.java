/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor;

import avdta.network.link.Link;
import avdta.network.node.Location;
import avdta.network.node.Node;

/**
 *
 * @author micha
 */
public class SelectAdapter implements SelectListener
{
    public void nodeSelected(Node n){}
    public void linkSelected(Link[] l){}
    public void pointSelected(Location loc){}
}
