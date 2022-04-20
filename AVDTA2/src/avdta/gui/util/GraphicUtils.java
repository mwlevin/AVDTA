package avdta.gui.util;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.awt.Container;

/**
 * This class provides static methods to align {@link Component}s.
 * It uses a table based method: each {@link Component} has a x and y location of the top left corner, as well as a width and height specifying the number of rows and columns occupied.
 * Note that {@link Component}s specified to overlap will visually overlap.
 * 
 * This class should only be used with {@link Container}s using a {@link GridBagLayout}.
 * If the layout is different (see {@link Container#setLayout(java.awt.LayoutManager)}), this class will throw a {@link ClassCastException}.
 * @author Michael
 */
public abstract class GraphicUtils
{
    /**
     * Adds the component at the specified row and column with the specified width and height.
     * Borders between components are set at 2 pixels.
     * The alignment is {@link GridBagConstraints#NORTHWEST}.
     * @param container the container
     * @param component the component
     * @param grid_x the column
     * @param grid_y the row
     * @param grid_width the width
     * @param grid_height the height
     */
    public static void constrain(Container container, Component component, int grid_x, int grid_y, int grid_width, int grid_height)
    {
        constrain(container, component, grid_x, grid_y, grid_width, grid_height, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, 2, 2, 2, 2);
    }
    
    /**
     * Adds the component at the specified row and column with the specified width and height.
     * Borders between components are set at 2 pixels.
     * @param container the container
     * @param component the component
     * @param grid_x the column
     * @param grid_y the row
     * @param grid_width the width
     * @param grid_height the height
     * @param align the alignment (see {@link GridBagConstraints})
     */
    public static void constrain(Container container, Component component, int grid_x, int grid_y, int grid_width, int grid_height, int align)
    {
        constrain(container, component, grid_x, grid_y, grid_width, grid_height, GridBagConstraints.NONE, align, 0.0, 0.0, 2, 2, 2, 2);
    }
	
    /**
     * Adds the component at the specified row and column with the specified width and height.
     * The alignment is {@link GridBagConstraints#NORTHWEST}.
     * @param container the container
     * @param component the component
     * @param grid_x the column
     * @param grid_y the row
     * @param grid_width the width
     * @param grid_height the height
     * @param top the top border (px)
     * @param left the left border (px)
     * @param bottom the bottom border (px)
     * @param right the right border (px)
     */
    public static void constrain(Container container, Component component, int grid_x, int grid_y, int grid_width, int grid_height, int top, int left, int bottom, int right)
    {
        constrain(container, component, grid_x, grid_y, grid_width, grid_height, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, top, left, bottom, right);
    }
	
    /**
     * Adds the component at the specified row and column with the specified width and height.
     * @param container the container
     * @param component the component
     * @param grid_x the column
     * @param grid_y the row
     * @param grid_width the width
     * @param grid_height the height
     * @param top the top border (px)
     * @param left the left border (px)
     * @param bottom the bottom border (px)
     * @param right the right border (px)
     * @param align the alignment (see {@link GridBagConstraints})
     */
    public static void constrain(Container container, Component component, int grid_x, int grid_y, int grid_width, int grid_height, int align, int top, int left, int bottom, int right)
    {
        constrain(container, component, grid_x, grid_y, grid_width, grid_height, GridBagConstraints.NONE, align, 0.0, 0.0, top, left, bottom, right);
    }
	
    /**
     * Adds the component at the specified row and column with the specified width and height.
     * @param container the container
     * @param component the component
     * @param grid_x the column
     * @param grid_y the row
     * @param grid_width the width
     * @param grid_height the height
     * @param fill the fill
     * @param align the alignment (see {@link GridBagConstraints})
     * @param weight_x the column weight
     * @param weight_y the row height
     * @param top the top border (px)
     * @param left the left border (px)
     * @param bottom the bottom border (px)
     * @param right the right border (px)
     */
    public static void constrain(Container container, Component component, int grid_x, int grid_y, int grid_width, int grid_height,
		int fill, int align, double weight_x, double weight_y, int top, int left, int bottom, int right)
    {
        GridBagConstraints c=new GridBagConstraints();
        c.gridx=grid_x;
        c.gridy=grid_y;
        c.gridwidth=grid_width;
        c.gridheight=grid_height;
        c.fill=fill;
        c.anchor=align;
        c.weightx=weight_x;
        c.weighty=weight_y;
        if(top+bottom+left+right>0){
                c.insets=new Insets(top, left, bottom, right);
        }
        ((GridBagLayout)container.getLayout()).setConstraints(component, c);
        container.add(component);
    }

    /**
     * Draws the image in the specified {@link Graphics} with the given maximum width and height.
     * The image will be scaled, constrained by the maximum width and height, and drawn centered.
     * @param window the {@link Graphics} to draw in
     * @param image the {@link Image} to be drawn
     * @param x the x location (px)
     * @param y the y location (px)
     * @param width the maximum width (px)
     * @param height the maximum height (px)
     */
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

}