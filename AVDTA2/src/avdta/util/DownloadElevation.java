package avdta.util;

import avdta.gui.util.StatusUpdate;
import avdta.network.node.NodeRecord;
import avdta.network.ReadNetwork;
import avdta.network.ReadNetwork;
import avdta.project.Project;
import java.awt.Component;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.JOptionPane;

public class DownloadElevation
{
    private StatusUpdate update;
    
    public DownloadElevation()
    {
        
    }
    public DownloadElevation(StatusUpdate update)
    {
        
        this.update = update;
    }
    public void download(Project project) throws IOException
    {
        download(null, project);
    }
        
    public void download(Component parent, Project project) throws IOException
    {
        update.update(0.0, "Reading nodes");

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

        update.update(0.0, "Downloading elevations");

        int count = 0;

        for(NodeRecord node : nodes)
        {
            if(node.getElevation() == 0)
            {
                try
                {
                    node.setElevation(download(node.getLongitude(), node.getLatitude()));
                    count++;
                    update.update((double)count/missingCount, "Downloading elevations");
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
            update.update(1);
        }
    }

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