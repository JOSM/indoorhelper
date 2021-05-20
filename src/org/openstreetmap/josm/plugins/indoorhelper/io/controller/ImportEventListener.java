// License: AGPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.indoorhelper.io.controller;

import org.openstreetmap.josm.data.osm.DataSet;

/**
 * Listener handles import actions.
 *
 * @author rebsc
 */
public interface ImportEventListener {

    /**
     * Will be called when import action started
     *
     * @param filepath Path to BIM file
     */
    void onBIMImport(String filepath);

    /**
     * Will be called after parsing finished
     *
     * @param ds Parsed data kept in DataSet
     */
    void onDataParsed(DataSet ds);

    /**
     * Will be called on parsing status changed to update
     * progress bar
     *
     * @param statusMsg new progress bar info
     */
    void onProcessStatusChanged(String statusMsg);
}