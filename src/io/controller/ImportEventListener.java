// License: GPL. For details, see LICENSE file.
package io.controller;

import java.util.ArrayList;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;

/**
 * Listener handles import actions.
 * @author rebsc
 *
 */
public interface ImportEventListener {

	/**
	 * Will be called when import action started
	 * @param filepath Path to BIM file
	 */
	void onBIMImport(String filepath);

	/**
	 * Will be called after finishing parsing
	 * @param ways to render
	 * @param nodes to render
	 */
	void onDataParsed(ArrayList<Way> ways, ArrayList<Node> nodes);
}