/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.dta;

import avdta.network.ImportFromVISTA;
import avdta.project.DTAProject;
import avdta.vehicle.VOT;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;
import java.util.Scanner;


/**
 *
 * @author micha
 */
public class DTAImportFromVISTA
{
    /*
    public DTAImportFromVISTA(DTAProject project, File nodes, File linkdetails, File elevation, File phases)
            throws IOException
    {
        super(project, nodes, linkdetails, elevation, phases);
    }
    */
    
    public DTAImportFromVISTA(DTAProject project, File static_od, File dynamic_od, File demand_profile, File demand)
            throws IOException
    {
        convertStaticOD(project, static_od);
        convertDynamicOD(project, dynamic_od);
        convertDemandProfile(project, demand_profile);
        convertDemand(project, demand);
    }
    
    public void convertDemand(DTAProject project, File demand) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getDemandFile()), true);
        Scanner filein = new Scanner(demand);
        
        fileout.println(ReadDTANetwork.getDemandFileHeader());
        
        Random rand = project.getRandom();
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            int o = filein.nextInt();
            int d = filein.nextInt();
            int ast = filein.nextInt();
            int dtime = filein.nextInt();
            int type = filein.nextInt();
            
            double vot = VOT.dagum_rand(rand);
            
            if(type == 500)
            {
                type = ReadDTANetwork.TRANSIT + ReadDTANetwork.HV + ReadDTANetwork.ICV;
            }
            else
            {
                type = ReadDTANetwork.DA_VEHICLE + ReadDTANetwork.HV + ReadDTANetwork.ICV;
            }
            
            fileout.println(id+"\t"+type+"\t"+o+"\t"+d+"\t"+dtime+"\t"+vot);
        }
        
        filein.close();
        fileout.close();
    }
    
    public void convertDynamicOD(DTAProject project, File dynamic_od) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getStaticODFile()));
        Scanner filein = new Scanner(dynamic_od);
        
        fileout.println(ReadDTANetwork.getDynamicODFileHeader());
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            int type = filein.nextInt();
            int o = filein.nextInt();
            int d = filein.nextInt();
            double dem = filein.nextDouble();
            int ast = filein.nextInt();
            
            type = ReadDTANetwork.DA_VEHICLE + ReadDTANetwork.HV + ReadDTANetwork.ICV;
            
            fileout.println(id+"\t"+type+"\t"+o+"\t"+d+"\t"+ast+"\t"+dem);
        }
        
        filein.close();
        fileout.close();
    }
    
    public void convertStaticOD(DTAProject project, File static_od) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getStaticODFile()));
        Scanner filein = new Scanner(static_od);
        
        fileout.println(ReadDTANetwork.getStaticODFileHeader());
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            int type = filein.nextInt();
            int o = filein.nextInt();
            int d = filein.nextInt();
            double dem = filein.nextDouble();
            
            type = ReadDTANetwork.DA_VEHICLE + ReadDTANetwork.HV + ReadDTANetwork.ICV;
            
            fileout.println(id+"\t"+type+"\t"+o+"\t"+d+"\t"+dem);
        }
        
        filein.close();
        fileout.close();
    }
    
    public void convertDemandProfile(DTAProject project, File demand_profile) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getDemandProfileFile()), true);
        Scanner filein = new Scanner(demand_profile);
        
        fileout.println(ReadDTANetwork.getDemandProfileFileHeader());
        while(filein.hasNext())
        {
            fileout.println(filein.nextLine());
        }
        
        fileout.close();
        filein.close();
    }
}
