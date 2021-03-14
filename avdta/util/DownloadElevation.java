package avdta.util;

import avdta.gui.util.StatusUpdate;
import avdta.network.node.NodeRecord;
import avdta.network.node.Node;
import avdta.network.ReadNetwork;
import avdta.network.ReadNetwork;
import avdta.project.Project;
import java.awt.Component;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.JOptionPane;

/**
 * 
 * @author Michael
 */
public class DownloadElevation
{
    private StatusUpdate update;
    
    /**
     * Constructs this {@link DownloadElevation}).
     */
    public DownloadElevation()
    {
        this(null);
    }
    
    /**
     * Constructs this {@link DownloadElevation} with the given {@link StatusUpdate}.
     * The {@link StatusUpdate}, if not null, will be updated throughout the download process to show progress.
     * @param update the {@link StatusUpdate}
     */
    public DownloadElevation(StatusUpdate update)
    {
        
        this.update = update;
    }
    
    /**
     * Calls {@link DownloadElevation#download(java.awt.Component, avdta.project.Project)} with a null {@link Component}.
     * @param project the {@link Project}
     * @throws IOException if a file cannot be accessed
     */
    public void download(Project project) throws IOException
    {
        download(null, project);
    }
    
    /**
     * This attempts to download all elevation data for the given {@link Project}.
     * This scans through the project, looking for {@link Node}s with a recorded elevation of 0 (see {@link Node#getElevation()}).
     * 
     * This method calls {@link DownloadElevation#download(double, double)} for each {@link Node}.
     * To avoid throttling by Google Maps API, this method waits 1s after each download.
     * 
     * If the connection is lost, any downloaded data will be recorded. 
     * If called again, this method will continue where it left off (because it looks for {@link Node}s with 0 elevation).
     * 
     * @param parent the component used to send {@link JOptionPane} message updates
     * @param project the {@link Project}
     * @throws IOException if a file cannot be accessed
     */
    public void download(Component parent, Project project) throws IOException
    {
        if(update != null)
        {
            update.update(0.0, 0, "Reading nodes");
        }

        Scanner filein = new Scanner(project.getNodesFile());

        filein.nextLine();

        ArrayList<NodeRecord> nodes = new ArrayList<NodeRecord>();

        int missingCount = 0;

        while(filein.hasNextInt())
        {
            NodeRecord node = new NodeRecord(filein.nextLine());
            nodes.add(node);

            if(node.getElevation() == 0)
            {
                missingCount++;
            }
        }

        filein.close();

        if(update != null)
        {
            update.update(0.0, 0, "Downloading elevations");
        }
        
        int count = 0;

        for(NodeRecord node : nodes)
        {
            if(node.getElevation() == 0)
            {
                try
                {
                    node.setElevation(download(node.getLongitude(), node.getLatitude()));
                    count++;
                    
                    if(update != null)
                    {
                        update.update((double)count/missingCount, 0, "Downloading elevations");
                    }
                    
                    Thread.sleep(1000);
                }
                catch(Exception ex)
                {
                    JOptionPane.showMessageDialog(parent, "Download failed to complete due to connection error. Progress was saved. Restart download.", "Error", JOptionPane.ERROR_MESSAGE);
                    break;
                }
            }
        }

        PrintStream fileout = new PrintStream(new FileOutputStream(project.getNodesFile()), true);

        fileout.println(ReadNetwork.getNodesFileHeader());
        for(NodeRecord n : nodes)
        {
            fileout.println(n);
        }

        fileout.close();

        if(update != null)
        {
            update.update(1, 0);
        }
    }

    /**
     * Calls Google Maps API to download elevation for the given longitude and latitude.
     * It is recommended not to call this too frequently due to throttling by Google Maps API.
     * @param x the longitude
     * @param y the latitude
     * @return the elevation from Google Maps API (ft)
     * @throws Exception if the connection to Google Maps API cannot be made
     */
    public static double download(double x, double y) throws Exception
    {
        URL url = new URL("http://maps.googleapis.com/maps/api/elevation/json?locations="+y+","+x+"&sensor=true");

        Scanner webin = new Scanner(url.openStream());

        while(webin.next().indexOf("elevation") < 0)
        {
                webin.nextLine();
        }

        webin.next();

        String temp = webin.next();

        double elevation = Double.parseDouble(temp.substring(0, temp.length()-1));

        webin.close();

        return elevation;
    }
}