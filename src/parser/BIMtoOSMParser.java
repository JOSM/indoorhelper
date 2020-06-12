// License: GPL. For details, see LICENSE file.
package parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.Logging;

import controller.io.ImportEventListener;
import model.io.BIMtoOSMCatalog;
import nl.tue.buildingsmart.express.population.EntityInstance;
import nl.tue.buildingsmart.express.population.ModelPopulation;

/**
 * Parser for BIM data. Extracts major BIM elements and transforms coordinates into OSM convenient format
 *
 * @author rebsc
 *
 */
public class BIMtoOSMParser {

	private final String FLAG_IFC2X3_TC1 = "FILE_SCHEMA(('IFC2X3_TC1'))";
	private final String FLAG_IFC2X3 = "FILE_SCHEMA(('IFC2X3'))";
	private final String FLAG_IFC4 = "FILE_SCHEMA(('IFC4'))";
	private final String resourcePathDir = System.getProperty("user.dir") + File.separator + "resources" + File.separator;
	private String ifcSchemaFilePath = resourcePathDir + "IFC2X3_TC1.exp"; // default

	private ImportEventListener importListener;
	private FileInputStream inputfs = null;
	private ModelPopulation ifcModel;
	private DataSet osmData;

	public BIMtoOSMParser(ImportEventListener listener) {
		importListener = listener;
		osmData = new DataSet();
	}

	/**
	 * Method parses data from ifc file into osm data
	 * @param filepath of ifc file
	 */
	public void parse(String filepath) {
		// load data into ifc model
		try {
			// find used ifc schema
			String usedIfcSchema = chooseSchemaFile(filepath);

			if(usedIfcSchema.isEmpty()) {
				showsLoadingErrorView(filepath, "Could not load IFC file.\n"+"IFC schema is no supported.");
				return;
			}
			if(usedIfcSchema.equals(FLAG_IFC4)) {
				ifcSchemaFilePath = resourcePathDir + "IFC4.exp";
			}

			// load ifc file
			inputfs = new FileInputStream(filepath);
			// load ifc file data into model
			ifcModel = new ModelPopulation(inputfs);
			ifcModel.setSchemaFile(Paths.get(ifcSchemaFilePath));
			ifcModel.load();

			// if loading throws ParseException check if ifcModel is empty to recognize something went wrong
			if(ifcModel.getInstances() == null) {
				showsLoadingErrorView(filepath, "Could not load IFC file.");
				return;
			}
		} catch (FileNotFoundException e) {
			Logging.error(e.getMessage());
		}
		finally {
			if ( inputfs != null ){
				try { inputfs.close(); } catch ( IOException e ) { }
			}
		}
		Logging.info(this.getClass().getName() + ": " + filepath + " loaded successfully");

		// extract important data and put them into internal data structure
		FilteredBIMData filteredBIMdata = extractMajorBIMData();
		// TODO parse FilteredBIMData into osm DataSet
		// TODO transform coordinates

		// send parsed data to controller
		importListener.onDataParsed(osmData);
		Logging.info(this.getClass().getName() + ": " + filepath + " parsed successfully");
	}


	/**
	 * Filters important osm data into internal data structure
	 * @return FilteredBIMData including BIM objects of ways, rooms, etc.
	 */
	private FilteredBIMData extractMajorBIMData() {
		FilteredBIMData bimData = new FilteredBIMData();

		// get all areas
		Vector<EntityInstance> areaObjects = new Vector<>();
		BIMtoOSMCatalog.getAreaTags().forEach(tag ->{
			ifcModel.getInstancesOfType(tag).forEach(entity ->{
				areaObjects.add(entity);
			});
		});
		bimData.setAreaObjects(areaObjects);

		// get all walls
		Vector<EntityInstance> wallObjects = new Vector<>();
		BIMtoOSMCatalog.getWallTags().forEach(tag ->{
			ifcModel.getInstancesOfType(tag).forEach(entity ->{
				wallObjects.add(entity);
			});
		});
		bimData.setWallObjects(wallObjects);

		// get all columns
		Vector<EntityInstance> colObjects = new Vector<>();
		BIMtoOSMCatalog.getColumnTags().forEach(tag ->{
			ifcModel.getInstancesOfType(tag).forEach(entity ->{
				colObjects.add(entity);
			});
		});
		bimData.setColumnObjects(colObjects);

		// get all doors
		Vector<EntityInstance> doorObjects = new Vector<>();
		BIMtoOSMCatalog.getDoorTags().forEach(tag ->{
			ifcModel.getInstancesOfType(tag).forEach(entity ->{
				doorObjects.add(entity);
			});
		});
		bimData.setDoorObjects(doorObjects);

		// get all doors
		Vector<EntityInstance> stairObjects = new Vector<>();
		BIMtoOSMCatalog.getStairTags().forEach(tag ->{
			ifcModel.getInstancesOfType(tag).forEach(entity ->{
				stairObjects.add(entity);
			});
		});
		bimData.setDoorObjects(stairObjects);

		return bimData;
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
		        data = data.replaceAll("\\s+","");

		        // schema must be defined before this flag
		        if(data.contains("DATA;"))	break;
		        // check if IFC2X3
		        if(data.contains(FLAG_IFC2X3_TC1) || data.contains(FLAG_IFC2X3)) {
		        	schema = FLAG_IFC2X3_TC1;
		        	break;
				}
		        // check if IFC4
				else if(data.contains(FLAG_IFC4)) {
					schema = FLAG_IFC4;
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
	 * Shows error dialog is file loading failed
	 * @param filepath of ifc file
	 * @param msg Error message
	 */
	private void showsLoadingErrorView(String filepath, String msg) {
		showErrorView(msg);
		Logging.info(this.getClass().getName() + ": " + filepath + " loading failed");
	}

	/**
	 * Shows error dialog
	 * @param msg Error message
	 */
	private void showErrorView(String msg) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
						msg,
	     			    "Error",
	     			    JOptionPane.ERROR_MESSAGE);
	         }
	     });
	}

	/**
	 * Data structure holding specific BIM data elements.
	 * @author rebsc
	 */
	private class FilteredBIMData {
		private Vector<EntityInstance> areaObjects;
		private Vector<EntityInstance> wallObjects;
		private Vector<EntityInstance> columnObjects;
		private Vector<EntityInstance> doorObjects;
		private Vector<EntityInstance> stairObjects;

		public Vector<EntityInstance> getAreaObjects() {
			return areaObjects;
		}
		public void setAreaObjects(Vector<EntityInstance> areaObjects) {
			this.areaObjects = areaObjects;
		}
		public Vector<EntityInstance> getWallObjects() {
			return wallObjects;
		}
		public void setWallObjects(Vector<EntityInstance> wallObjects) {
			this.wallObjects = wallObjects;
		}
		public Vector<EntityInstance> getColumnObjects() {
			return columnObjects;
		}
		public void setColumnObjects(Vector<EntityInstance> columnObjects) {
			this.columnObjects = columnObjects;
		}
		public Vector<EntityInstance> getDoorObjects() {
			return doorObjects;
		}
		public void setDoorObjects(Vector<EntityInstance> doorObjects) {
			this.doorObjects = doorObjects;
		}
		public Vector<EntityInstance> getStairObjects() {
			return stairObjects;
		}
		public void setStairObjects(Vector<EntityInstance> stairObjects) {
			this.stairObjects = stairObjects;
		}
	}

}
