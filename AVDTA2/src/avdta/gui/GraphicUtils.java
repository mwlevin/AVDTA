package avdta.gui;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.awt.*;
import java.io.*;
import javax.imageio.*;
import java.awt.image.BufferedImage;

public abstract class GraphicUtils
{
	// only static methods, don't instantiate

	public static void constrain(Container container, Component component, int grid_x, int grid_y, int grid_width, int grid_height){
		constrain(container, component, grid_x, grid_y, grid_width, grid_height, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, 5, 5, 5, 5);
	}
	public static void constrain(Container container, Component component, int grid_x, int grid_y, int grid_width, int grid_height, int align){
		constrain(container, component, grid_x, grid_y, grid_width, grid_height, GridBagConstraints.NONE, align, 0.0, 0.0, 5, 5, 5, 5);
	}
	public static void constrain(Container container, Component component, int grid_x, int grid_y, int grid_width, int grid_height, int top, int left, int bottom, int right){
		constrain(container, component, grid_x, grid_y, grid_width, grid_height, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, top, left, bottom, right);
	}
	public static void constrain(Container container, Component component, int grid_x, int grid_y, int grid_width, int grid_height, int align, int top, int left, int bottom, int right){
		constrain(container, component, grid_x, grid_y, grid_width, grid_height, GridBagConstraints.NONE, align, 0.0, 0.0, top, left, bottom, right);
	}
	public static void constrain(Container container, Component component, int grid_x, int grid_y, int grid_width, int grid_height,
		int fill, int anchor, double weight_x, double weight_y, int top, int left, int bottom, int right)
	{
		GridBagConstraints c=new GridBagConstraints();
		c.gridx=grid_x;
		c.gridy=grid_y;
		c.gridwidth=grid_width;
		c.gridheight=grid_height;
		c.fill=fill;
		c.anchor=anchor;
		c.weightx=weight_x;
		c.weighty=weight_y;
		if(top+bottom+left+right>0){
			c.insets=new Insets(top, left, bottom, right);
		}
		((GridBagLayout)container.getLayout()).setConstraints(component, c);
		container.add(component);
	}

	public static void drawImage(Graphics window, Image image, int x, int y, int width, int height)
	{
		double ratio=(double)width/image.getWidth(null);

		if(Math.floor(image.getHeight(null)*ratio)>height)
		{
			ratio=(double)height/image.getHeight(null);

		}

		int xDiff=(width-(int)Math.floor(image.getWidth(null)*ratio))/2;
		int yDiff=(height-(int)Math.floor(image.getHeight(null)*ratio))/2;

		window.drawImage(image, x+xDiff, y+yDiff, (int)Math.floor(ratio*image.getWidth(null)), (int)Math.floor(ratio*image.getHeight(null)), null);



	}
	
	
	public static BufferedImage screenshot()
	{
		try
		{
			Robot robot = new Robot();
			Rectangle captureSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
			BufferedImage bufferedImage = robot.createScreenCapture(captureSize);
			
			return bufferedImage;
		}
		catch(AWTException e)
		{
			System.err.println("Someone call a doctor!");
			
			return null;
		}
	}

}