// License: GPL. For details, see LICENSE file.
package model.io;

import org.openstreetmap.josm.data.osm.DataSet;

/**
 * Import data model class holding imported data
 *
 * @author rebsc
 */
public class ImportDataModel {

	private DataSet importData;

	public void setImportData(DataSet data) {
		this.importData = data;
	}

	public DataSet getImportData() {
		if(importData == null)	return new DataSet();
		return importData;
	}

}
