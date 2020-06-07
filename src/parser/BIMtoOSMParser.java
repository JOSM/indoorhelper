// License: GPL. For details, see LICENSE file.
package parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Scanner;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.Logging;

import nl.tue.buildingsmart.express.population.ModelPopulation;

/**
 * Parser for BIM data. Extracts major BIM elements and transforms coordinates into OSM convenient format.
 *
 * @author rebsc
 *
 */
public class BIMtoOSMParser {

	private final String IFC2X3_TC1 = "IFC2X3_TC1";
	private final String IFC2X3 = "IFC2X3";
	private final String IFC4 = "IFC4";
	private final String resourcePathDir = System.getProperty("user.dir") + File.separator + "resources" + File.separator;
	private String ifcSchemaFilePath = resourcePathDir + "IFC2X3_TC1.exp"; // default

	ModelPopulation ifcModel;

	public DataSet parse(String filepath) {
		FileInputStream fs = null;
		ModelPopulation ifcModel;
		DataSet osmData = new DataSet();

		try {

			// find used ifc schema
			String usedIfcSchema = chooseSchemaFile(filepath);
			if(usedIfcSchema.equals(IFC4)) {
				ifcSchemaFilePath = resourcePathDir + "IFC4.exp";
			}
			if(usedIfcSchema == "") {
				showErrorView("Could not load IFC file.\n"+"IFC schema is no supported.");
				return osmData;
			}

			// load ifc file
			fs = new FileInputStream(filepath);

			// load ifc file data into model
			ifcModel = new ModelPopulation(fs);
			ifcModel.setSchemaFile(Paths.get(ifcSchemaFilePath));
			// TODO Add progress bar because loading large files needs some time
			ifcModel.load();

		} catch (FileNotFoundException e) {
			Logging.error(e.getMessage());
		}
		finally {
			if ( fs != null ){
				try { fs.close(); } catch ( IOException e ) { }
			}
		}

		return null;
	}

	/**
	 * Read the FILE_SCHEMA flag from ifc file and return used schema
	 * @param filepath path of icf file
	 * @return Used ifc file schema as string
	 */
	private String chooseSchemaFile(String filepath) {
		String schema = "";
		try {
		      File file = new File(filepath);
		      Scanner reader = new Scanner(file, StandardCharsets.UTF_8.name());
		      while (reader.hasNextLine()) {
		        String data = reader.nextLine();
		        if(data.contains(IFC2X3_TC1) || data.contains(IFC2X3) ) {
		        	schema = IFC2X3_TC1;
		        	break;
				}
				else if(data.contains(IFC4)) {
					schema = IFC4;
					break;
				}
		      }
		      reader.close();
		} catch (FileNotFoundException e) {
		   Logging.error(e.getMessage());
		}

		return schema;
	}

	/**
	 * Shows error dialog
	 * @param msg Error message
	 */
	private void showErrorView(String msg) {
		JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
			    msg,
			    "Error",
			    JOptionPane.ERROR_MESSAGE);
	}

}
