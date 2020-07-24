// License: GPL. For details, see LICENSE file.
package io.model;

import java.util.ArrayList;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;

/**
 * Import data model class holding imported data
 *
 * @author rebsc
 */
public class ImportDataModel {

	private ArrayList<Way> ways;
	private ArrayList<Node> nodes;

	public void setImportData(ArrayList<Way> ways, ArrayList<Node> nodes) {
		this.setWays(ways);
		this.setNodes(nodes);
	}

	public ArrayList<Way> getWays() {
		if(ways.isEmpty())	return new ArrayList<>();
		return ways;
	}

	public void setWays(ArrayList<Way> ways) {
		this.ways = ways;
	}

	public ArrayList<Node> getNodes() {
		if(nodes.isEmpty())	return new ArrayList<>();
		return nodes;
	}

	public void setNodes(ArrayList<Node> nodes) {
		this.nodes = nodes;
	}


}
