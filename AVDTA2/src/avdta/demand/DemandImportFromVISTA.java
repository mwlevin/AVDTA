/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.demand;

import avdta.dta.ReadDTANetwork;
import avdta.network.ImportFromVISTA;
import avdta.project.DemandProject;
import avdta.project.Project;
import avdta.project.SAVProject;
import avdta.sav.ReadSAVNetwork;
import avdta.vehicle.VOT;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;
import java.util.Scanner;


/**
 * This class defines methods to convert demand data from the VISTA data format to the AVDTA data format. 
 * This class uses the following tables from VISTA: static_od, dynamic_od, demand_profile, and demand.
 * To use it, copy the required tables into files, and construct a new {@link DemandImportFromVISTA} with the files. 
 * The constructor will call all conversion methods.
 * @author Michael
 */
public class DemandImportFromVISTA
{
    /**
     * Calls {@link DTAImportFromVISTA#DTAImportFromVISTA(avdta.project.DTAProject, java.io.File, java.io.File)} with a {@code static_od.txt} file, a {@code dynamic_od.txt} file, a {@code demand_profile.txt} file, and a {@code demand.txt} file from the specified directory.
     * @param project the project
     * @param directory the directory to look for files
     * @throws IOException if a file cannot be accessed
     */
    public DemandImportFromVISTA(DemandProject project, String directory) throws IOException
    {
        this(project, new File(directory+"/static_od.txt"), new File(directory+"/dynamic_od.txt"), new File(directory+"/demand_profile.txt"), 
                new File(directory+"/demand.txt"));
    }
    
    /**
     * Converts VISTA network data into the AVDTA data format from the following files.
     * @param project the project
     * @param static_od the file containing the static_od table
     * @param dynamic_od the file containing the dynamic_od table
     * @param demand_profile the file containing the demand_profile table
     * @param demand the file containing the demand table
     * @throws IOException if a file cannot be accessed
     */
    public DemandImportFromVISTA(DemandProject project, File static_od, File dynamic_od, File demand_profile, File demand)
            throws IOException
    {
        convertStaticOD(project, static_od);
        convertDynamicOD(project, dynamic_od);
        convertDemandProfile(project, demand_profile);
        convertDemand(project, demand);
    }
    
    /**
     * Converts the demand table.
     * @param project the project
     * @param demand the file containing the database table
     * @throws IOException if a file is not found
     */
    public void convertDemand(DemandProject project, File demand) throws IOException
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
            
            if(type >= 500)
            {
                //type = ReadDTANetwork.BUS + ReadDTANetwork.HV + ReadDTANetwork.ICV;
                continue;
            }
            else
            {
                type = ReadDTANetwork.DA_VEHICLE + ReadDTANetwork.HV + ReadDTANetwork.ICV;
            }
            
            if(project instanceof SAVProject)
            {
                type = ReadSAVNetwork.TRAVELER;
            }
            
            fileout.println(id+"\t"+type+"\t"+o+"\t"+d+"\t"+dtime+"\t"+vot);
        }
        
        filein.close();
        fileout.close();
    }
    
    /**
     * Converts the dynamic_od table.
     * @param project the {@link Project}
     * @param dynamic_od the file containing the database table
     * @throws IOException if a file is not found
     */
    public void convertDynamicOD(DemandProject project, File dynamic_od) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getDynamicODFile()));
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
            
            if(project instanceof SAVProject)
            {
                type = ReadSAVNetwork.TRAVELER;
            }
            
            fileout.println(id+"\t"+type+"\t"+o+"\t"+d+"\t"+ast+"\t"+dem);
        }
        
        filein.close();
        fileout.close();
    }
    
    /**
     * Converts the static_od table.
     * @param project the project
     * @param static_od the file containing the database table
     * @throws IOException if a file is not found
     */
    public void convertStaticOD(DemandProject project, File static_od) throws IOException
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
            
            if(project instanceof SAVProject)
            {
                type = ReadSAVNetwork.TRAVELER;
            }
            
            fileout.println(id+"\t"+type+"\t"+o+"\t"+d+"\t"+dem);
        }
        
        filein.close();
        fileout.close();
    }
    
    /**
     * Converts the demand_profile table.
     * @param project the {@link Project}
     * @param demand_profile the file containing the database table
     * @throws IOException if a file is not found
     */
    public void convertDemandProfile(DemandProject project, File demand_profile) throws IOException
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
