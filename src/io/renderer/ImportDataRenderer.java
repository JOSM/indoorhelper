// License: AGPL. For details, see LICENSE file.
package io.renderer;

import java.util.List;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

/**
 * Class handles rendering of imported data
 *
 * @author rebsc
 */
public class ImportDataRenderer {

    public static void renderDataOnNewLayer(List<Way> ways, List<Node> nodes, String layerName) {
        // create new layer
        DataSet dataSet = new DataSet();
        nodes.forEach(dataSet::addPrimitive);
        ways.forEach(dataSet::addPrimitive);

        OsmDataLayer importLayer = new OsmDataLayer(dataSet, layerName, null);
        MainApplication.getLayerManager().addLayer(importLayer);
        MainApplication.getLayerManager().setActiveLayer(importLayer);
    }
}
