// License: GPL. For details, see LICENSE file.
package controller.io;

import org.openstreetmap.josm.data.osm.DataSet;

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
	 * @param osmData DataSet with parsed data
	 */
	void onDataParsed(DataSet osmData);
}