package avdta;

import avdta.gui.StatusUpdate;
import java.io.*;
import java.net.*;
import java.util.*;

public class DownloadElevation
{
    private StatusUpdate update;
    
        public void setUpdate(StatusUpdate update)
        {
            this.update = update;
        }
	public void download(String network) throws IOException
	{
		File inputfile = new File("data/"+network+"/nodes.txt");
		File outputfile = new File("data/"+network+"/elevation.txt");
		
		Scanner filein;
		int lastNode = -1;
		try
		{
		
			filein = new Scanner(outputfile);

			while(filein.hasNextInt())
			{
				lastNode = filein.nextInt();
				filein.nextLine();
			}
			filein.close();
		}
		catch(Exception ex){}
                
                filein = new Scanner(inputfile);
		
		
		PrintStream fileout = new PrintStream(new FileOutputStream(outputfile, true), true);
		
  
		filein = new Scanner(inputfile);
                
                int size = 0;
                
                while(filein.hasNextInt())
                {
                    size++;
                    filein.nextLine();
                }
                
                filein.close();
                
                filein = new Scanner(inputfile);
		
                int count = 0;
                
		if(lastNode >= 0)
		{
			while(filein.nextInt() != lastNode)
			{
                                count++;
				filein.nextLine();
			}
			filein.nextLine();
		}
		
                
                
		while(filein.hasNextInt())
		{
			int id = filein.nextInt();
			int type = filein.nextInt();

			if(type == 100)
			{
				filein.nextLine();
				continue;
			}
			
			double x = filein.nextDouble();
			double y = filein.nextDouble();
			filein.nextLine();
			
			double elevation = 0;
			
			try
			{
				elevation = download(x, y);
			}
			catch(Exception ex)
			{
				break;
			}
			
			if(elevation == Integer.MAX_VALUE)
			{
				break;
			}
			
			fileout.println(id+"\t"+elevation);
			fileout.flush();
			
                        count++;
                        
                        if(update != null)
                        {
                            update.update((double)count / size);
                        }
                        
                        try
                        {
                            Thread.sleep(1000);
                        }
                        catch(Exception ex)
                        {
                            
                        }
		}
		filein.close();
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