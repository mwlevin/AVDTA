/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import avdta.network.node.IntersectionPolicy;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.sav.ReadSAVNetwork;
import avdta.sav.SAVSimulator;
import java.io.IOException;

/**
 *
 * @author micha
 */
public class CreateSAVSimulator extends CreateSimulator
{
    public CreateSAVSimulator(String name)
    {
        super(name, false);
    }
    
    public void readNetwork(String name, String scenario, int linktype, String nodetype)
    {
        ReadSAVNetwork input = new ReadSAVNetwork();
        SAVSimulator test = new SAVSimulator(name);
        test.setScenario(scenario);
        
        try
        {
            input.readNetwork(test, Link.CTM);
            input.readTBR(test, IntersectionPolicy.FCFS, Node.CR);
        
            input.readTravelers(test, 1);
            
            setVisible(false);
            new SAVs(test);
        }
        catch(IOException ex)
        {
            ex.printStackTrace(System.err);
        }
        
        
    }
}
