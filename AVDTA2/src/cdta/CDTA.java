/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cdta;

import avdta.project.CDTAProject;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author ml26893
 */
public class CDTA 
{
    public static void main(String[] args) throws IOException
    {
        CDTAProject project = new CDTAProject(new File("projects/SiouxFalls"));
        
        TECNetwork net = project.createTECNetwork();
        net.initializeConnectivity();
    }
}
