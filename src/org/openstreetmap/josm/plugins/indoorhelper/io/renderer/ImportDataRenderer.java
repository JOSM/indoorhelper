// License: AGPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.indoorhelper.io.renderer;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

/**
 * Class handles rendering of imported data
 */
public class ImportDataRenderer {

    public static void renderDataOnNewLayer(DataSet ds, String layerName) {
        OsmDataLayer importLayer = new OsmDataLayer(ds, layerName, null);
        MainApplication.getLayerManager().addLayer(importLayer);
        MainApplication.getLayerManager().setActiveLayer(importLayer);
    }
}
