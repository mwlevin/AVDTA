/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor;

import org.openstreetmap.gui.jmapviewer.tilesources.AbstractOsmTileSource;

/**
 *
 * @author micha
 */
public class OfflineOsmTileSource extends AbstractOsmTileSource {

	private final int minZoom;
	private final int maxZoom;
	
	public OfflineOsmTileSource(String path, int minZoom, int maxZoom) {
		super("Offline from "+path, path, null);
		this.minZoom = minZoom;
		this.maxZoom = maxZoom;
	}
	
	@Override
	public int getMaxZoom() {
		return maxZoom;
	}

	@Override
	public int getMinZoom() {
		return minZoom;
	}
	
        /*
	@Override
	public TileUpdate getTileUpdate() {
		return TileUpdate.None;
	}
        */
}