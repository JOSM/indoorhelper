// License: AGPL. For details, see LICENSE file.
package io.renderer;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

/**
 * Class handles rendering of imported data
 *
 * @author rebsc
 */
public class ImportDataRenderer {

    public static void renderDataOnNewLayer(DataSet ds, String layerName) {
        // create new layer with data
        OsmDataLayer importLayer = new OsmDataLayer(ds, layerName, null);
        MainApplication.getLayerManager().addLayer(importLayer);
        MainApplication.getLayerManager().setActiveLayer(importLayer);
    }
}
