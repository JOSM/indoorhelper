// License: AGPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.indoorhelper.io.model;

import org.openstreetmap.josm.data.osm.DataSet;

/**
 * Class holding imported data
 *
 * @author rebsc
 */
public class ImportDataModel {

    private DataSet ds;

    public void setImportData(DataSet data) {
        ds = data;
    }

    public DataSet getData(){
        return ds;
    }


}
