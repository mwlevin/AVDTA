/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.project;

import cdta.TECNetwork;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author micha
 */
public class CDTAProject extends DTAProject
{
    /**
     * Constructs an empty project
     */
    public CDTAProject()
    {
        super();
    }
    
    /**
     * Constructs the project from the specified directory.
     * This reads the project and constructs the associated {@link DTASimulator}.
     * @param directory the project directory
     * @throws IOException if a file is not found
     */
    public CDTAProject(File dir) throws IOException
    {
        super(dir);
    }
    
    /**
     * Returns the project type
     * @return DTA
     */
    public String getType()
    {
        return "CDTA";
    }
    
    /**
     * Returns the type indicator {@link String} used to create the indicator file to determine the type of this project.
     * @return "dta"
     */
    public String getTypeIndicator()
    {
        return "cdta";
    }
    
    /**
     * Creates time-expanded cell network.
     * The network is constructed based on node, link, and demand data from the simulator.
     * See {@link TECNetwork#TECNetwork(avdta.network.Simulator)}.
     * @return time-expanded cell network based on simulator data
     */
    public TECNetwork createTECNetwork()
    {
        return new TECNetwork(getSimulator());
    }
}
