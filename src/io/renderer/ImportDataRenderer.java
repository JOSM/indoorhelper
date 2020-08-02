// License: GPL. For details, see LICENSE file.
package io.renderer;

import java.util.ArrayList;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

/**
 * Class handles rendering of imported data
 * @author rebsc
 *
 */
public class ImportDataRenderer {

	public static void renderDataOnNewLayer(ArrayList<Way> ways, ArrayList<Node> nodes, String layerName) {
		// create new layer
		DataSet dataSet = new DataSet();
		nodes.forEach(node ->{
			dataSet.addPrimitive(node);
		});
		ways.forEach(way ->{
			dataSet.addPrimitive(way);
		});

		OsmDataLayer importLayer = new OsmDataLayer(dataSet, layerName, null);
		MainApplication.getLayerManager().addLayer(importLayer);
		MainApplication.getLayerManager().setActiveLayer(importLayer);
	}
}
