/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor;

import java.awt.Graphics;
import java.awt.Point;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.interfaces.MapPolygon;
import org.openstreetmap.gui.jmapviewer.interfaces.MapRectangle;

/**
 *
 * @author ml26893
 */
public class JMapViewer extends org.openstreetmap.gui.jmapviewer.JMapViewer
{
    private boolean displayOSM;
    
    private static final Point[] move = {new Point(1, 0), new Point(0, 1), new Point(-1, 0), new Point(0, -1)};
    
    public JMapViewer()
    {
        displayOSM = true;
    }
    
    public void setDisplayOSM(boolean d)
    {
        displayOSM = d;
        
        repaint();
    }
    
    public boolean isDisplayOSM()
    {
        return displayOSM;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);


        int iMove = 0;

        int tilesize = tileSource.getTileSize();
        int tilex = center.x / tilesize;
        int tiley = center.y / tilesize;
        int offsx = center.x % tilesize;
        int offsy = center.y % tilesize;

        int w2 = getWidth() / 2;
        int h2 = getHeight() / 2;
        int posx = w2 - offsx;
        int posy = h2 - offsy;

        int diffLeft = offsx;
        int diffRight = tilesize - offsx;
        int diffTop = offsy;
        int diffBottom = tilesize - offsy;

        boolean startLeft = diffLeft < diffRight;
        boolean startTop = diffTop < diffBottom;

        if (startTop) {
            if (startLeft) {
                iMove = 2;
            } else {
                iMove = 3;
            }
        } else {
            if (startLeft) {
                iMove = 1;
            } else {
                iMove = 0;
            }
        } // calculate the visibility borders
        int xMin = -tilesize;
        int yMin = -tilesize;
        int xMax = getWidth();
        int yMax = getHeight();

        // calculate the length of the grid (number of squares per edge)
        int gridLength = 1 << zoom;

        // paint the tiles in a spiral, starting from center of the map
        
        if(displayOSM)
        {
            boolean painted = true;
            int x = 0;
            while (painted) {
                painted = false;
                for (int i = 0; i < 4; i++) {
                    if (i % 2 == 0) {
                        x++;
                    }
                    for (int j = 0; j < x; j++) {
                        if (xMin <= posx && posx <= xMax && yMin <= posy && posy <= yMax) {
                            // tile is visible
                            Tile tile;
                            if (scrollWrapEnabled) {
                                // in case tilex is out of bounds, grab the tile to use for wrapping
                                int tilexWrap = ((tilex % gridLength) + gridLength) % gridLength;
                                tile = tileController.getTile(tilexWrap, tiley, zoom);
                            } else {
                                tile = tileController.getTile(tilex, tiley, zoom);
                            }
                            if (tile != null) {
                                tile.paint(g, posx, posy, tilesize, tilesize);
                                if (tileGridVisible) {
                                    g.drawRect(posx, posy, tilesize, tilesize);
                                }
                            }
                            painted = true;
                        }
                        Point p = move[iMove];
                        posx += p.x * tilesize;
                        posy += p.y * tilesize;
                        tilex += p.x;
                        tiley += p.y;
                    }
                    iMove = (iMove + 1) % move.length;
                }
            }
        }
        else
        {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        
        // outer border of the map
        int mapSize = tilesize << zoom;
        if (scrollWrapEnabled) {
            g.drawLine(0, h2 - center.y, getWidth(), h2 - center.y);
            g.drawLine(0, h2 - center.y + mapSize, getWidth(), h2 - center.y + mapSize);
        } else {
            g.drawRect(w2 - center.x, h2 - center.y, mapSize, mapSize);
        }

        // g.drawString("Tiles in cache: " + tileCache.getTileCount(), 50, 20);

        // keep x-coordinates from growing without bound if scroll-wrap is enabled
        if (scrollWrapEnabled) {
            center.x = center.x % mapSize;
        }

        if (mapPolygonsVisible && mapPolygonList != null) {
            synchronized (this) {
                for (MapPolygon polygon : mapPolygonList) {
                    if (polygon.isVisible())
                        paintPolygon(g, polygon);
                }
            }
        }

        if (mapRectanglesVisible && mapRectangleList != null) {
            synchronized (this) {
                for (MapRectangle rectangle : mapRectangleList) {
                    if (rectangle.isVisible())
                        paintRectangle(g, rectangle);
                }
            }
        }

        if (mapMarkersVisible && mapMarkerList != null) {
            synchronized (this) {
                for (MapMarker marker : mapMarkerList) {
                    if (marker.isVisible())
                        paintMarker(g, marker);
                }
            }
        }

        attribution.paintAttribution(g, getWidth(), getHeight(), getPosition(0, 0), getPosition(getWidth(), getHeight()), zoom, this);
    }
}
