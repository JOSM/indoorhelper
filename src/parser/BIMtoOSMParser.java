// License: GPL. For details, see LICENSE file.
package parser;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
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
import parser.data.FilteredRawBIMData;
import parser.data.Point3D;
import parser.data.PreparedBIMObject3D;

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
	private boolean fileIsCorrupted = false;

	public BIMtoOSMParser(ImportEventListener listener) {
		importListener = listener;
		osmData = new DataSet();
	}

	/**
	 * Method parses data from IFC file into OSM data
	 * @param filepath of IFC file
	 */
	public void parse(String filepath) {
		// load data into IFC model
		if(!loadFile(filepath))	return;

		// extract important data and put them into internal data structure
		FilteredRawBIMData filteredBIMdata = extractMajorBIMData();

		// for preparation of filtered BIM data find Id of BIM root IFCLOCALPLACEMENT element (kept in IFCSITE flag)
		int BIMRootId = getIFCLOCALPLACEMENTRootObject(filteredBIMdata);
		if(BIMRootId == -1) {
			showsParsingErrorView(filepath, "Could not import IFC file.\nIFC file doesn't contain IFCSITE element.");
			return;
		}

		// prepare filtered BIM data - find global object coordinates and other attributes like object height, width etc.
		ArrayList<PreparedBIMObject3D> preparedBIMdata = new ArrayList<>();
		preparedBIMdata.addAll(prepareBIMObjects(BIMtoOSMCatalog.BIMObject.IfcSlab, filteredBIMdata.getAreaObjects(), BIMRootId));
		preparedBIMdata.addAll(prepareBIMObjects(BIMtoOSMCatalog.BIMObject.IfcWall, filteredBIMdata.getWallObjects(), BIMRootId));
		preparedBIMdata.addAll(prepareBIMObjects(BIMtoOSMCatalog.BIMObject.IfcColumn, filteredBIMdata.getColumnObjects(), BIMRootId));
		preparedBIMdata.addAll(prepareBIMObjects(BIMtoOSMCatalog.BIMObject.IfcDoor, filteredBIMdata.getDoorObjects(), BIMRootId));
		preparedBIMdata.addAll(prepareBIMObjects(BIMtoOSMCatalog.BIMObject.IfcStair, filteredBIMdata.getStairObjects(), BIMRootId));
		// TODO extract/prepare more relevant information (object width, height etc.)

		// TODO transform coordinates
		// TODO parse FilteredBIMData into OSM DataSet

		if(fileIsCorrupted) {
			showsParsingErrorView(filepath, "Caution!\nImported data might include errors!");
		}
		// send parsed data to controller
		importListener.onDataParsed(osmData);
		Logging.info(this.getClass().getName() + ": " + filepath + " parsed successfully");
	}

	/**
	 * Load file into ifcModel
	 * @param filepath of IFC file
	 * @return true if loading successful, else false
	 */
	private boolean loadFile(String filepath) {
		try {
			// find used IFC schema
			String usedIfcSchema = chooseSchemaFile(filepath);

			if(usedIfcSchema.isEmpty()) {
				showsLoadingErrorView(filepath, "Could not load IFC file.\nIFC schema is no supported.");
				return false;
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
				return false;
			}
		} catch (FileNotFoundException e) {
			Logging.error(e.getMessage());
			return false;
		}
		finally {
			if ( inputfs != null ){
				try { inputfs.close(); } catch ( IOException e ) { }
			}
		}
		Logging.info(this.getClass().getName() + ": " + filepath + " loaded successfully");
		return true;
	}

	/**
	 * Read the FILE_SCHEMA flag from IFC file and return used schema
	 * @param filepath path of IFC file
	 * @return Used IFC file schema as string
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
	 * Filters important OSM data into internal data structure
	 * @return FilteredBIMData including BIM objects of ways, rooms, etc.
	 */
	private FilteredRawBIMData extractMajorBIMData() {
		FilteredRawBIMData bimData = new FilteredRawBIMData();

		// get the root element IFCSITE
		Vector<EntityInstance> ifcSiteObjects = new Vector<>();
		BIMtoOSMCatalog.getIFCSITETags().forEach(tag ->{
			ifcModel.getInstancesOfType(tag).forEach(entity ->{
				ifcSiteObjects.add(entity);
			});
		});
		if(!ifcSiteObjects.isEmpty()) {
			bimData.setIfcSite(ifcSiteObjects.firstElement());
		}

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
		bimData.setStairObjects(stairObjects);

		return bimData;
	}

	/**
	 * Gets the LOCALPLACEMENT root element of IFC file
	 * @param filteredBIMdata Data including IFCSITE flag of IFC file
	 * @return Root LOCALPLACEMENT element of IFC file
	 */
	private int getIFCLOCALPLACEMENTRootObject(FilteredRawBIMData filteredBIMdata) {
		Object BIMRoot = filteredBIMdata.getIfcSite().getAttributeValueBN("ObjectPlacement");
		if(BIMRoot == null)	return -1;
		return getIntFromBIMId(BIMRoot.toString());
	}

	/**
	 * Prepares BIM objects for further operations. Extracts OSM relevant information and puts it into {@link PreparedBIMObject3D}
	 * @param objectType relating BIMtoOSMCatalog.BIMObject
	 * @param wallObjects All BIM wall objects
	 * @param BIMFileRootId Root IFCLOCALPLACEMENT element of BIM file
	 * @return prepared BIM object
	 */
	private ArrayList<PreparedBIMObject3D> prepareBIMObjects(BIMtoOSMCatalog.BIMObject objectType, Vector<EntityInstance> wallObjects, int BIMFileRootId) {
		ArrayList<PreparedBIMObject3D> preparedWalls = new ArrayList<>();

		wallObjects.forEach(object ->{
			// get IFCLOCALPLACEMENT of object
			Object objectIFCLP = object.getAttributeValueBN("ObjectPlacement");
			int objectIFCLPId = getIntFromBIMId(objectIFCLP.toString());

			// get all RELATIVEPLACEMENTs to root
			ArrayList<Integer> objectRP = getRelativePlacementsToRoot(BIMFileRootId, objectIFCLPId, new ArrayList<Integer>());

			// calculate cartesian corner of object (origin) by using the relative placements
			Point3D cartesianCornerOfWall = new Point3D(0.0, 0.0, 0.0);
			boolean corruptedObject = false;

			for(Integer relativeId : objectRP) {

				// get LOCATION (IFCCARTESIANPOINT) of IFCAXIS2PLACEMENT2D/3D including relative coordinates
				Object objectCP = ifcModel.getEntity(relativeId).getAttributeValueBN("Location");
				int objectCPId = getIntFromBIMId(objectCP.toString());
				@SuppressWarnings("unchecked")
				Vector<String> objectCoords = (Vector<String>)ifcModel.getEntity(objectCPId).getAttributeValueBN("Coordinates");
				double relativeX = prepareCoordinateString(objectCoords.get(0));
				double relativeY = prepareCoordinateString(objectCoords.get(1));
				double relativeZ = prepareCoordinateString(objectCoords.get(2));
				if(Double.isNaN(relativeX) || Double.isNaN(relativeY) || Double.isNaN(relativeZ)) {
					corruptedObject = true;
					fileIsCorrupted = true;
					break;
				}

				// add relative coordinates to finally get relative position to root element
				cartesianCornerOfWall.setX(cartesianCornerOfWall.getX() + relativeX);
				cartesianCornerOfWall.setY(cartesianCornerOfWall.getY() + relativeY);
				cartesianCornerOfWall.setZ(cartesianCornerOfWall.getZ() + relativeZ);
			}

			// create PreparedBIMObject3D and save
			if(!corruptedObject) {
				preparedWalls.add(new PreparedBIMObject3D(objectType, cartesianCornerOfWall));
			}
		});

		return preparedWalls;
	}

	/**
	 * Method recursive walks thru IFC file and collects the RELATIVEPLACEMENT Ids of from start entity to root entity
	 * @param BIMFileRootId Root IFCLOCALPLACEMENT element of BIMFile
	 * @param entityId Id of entity you want to collect the RELATIVEPLACEMENT from
	 * @param relativePlacementsToRoot empty list at beginning, needed for recursive iteration
	 * @return List with Ids to RELATIVEPLACEMENTs
	 */
	private ArrayList<Integer> getRelativePlacementsToRoot(int BIMFileRootId, int entityId, ArrayList<Integer> relativePlacementsToRoot){
		if(entityId == BIMFileRootId)	return relativePlacementsToRoot;

		// get and add relative placements (RELATIVEPLACEMENT)
		Object relativePlacement = ifcModel.getEntity(entityId).getAttributeValueBN("RelativePlacement");
		int relativePlacementId = getIntFromBIMId(relativePlacement.toString());
		relativePlacementsToRoot.add(relativePlacementId);

		// get id of placement relative to this (PLACEMENTRELTO)
		Object placementRelTo = ifcModel.getEntity(entityId).getAttributeValueBN("PlacementRelTo");
		int placementRelToId = getIntFromBIMId(placementRelTo.toString());
		getRelativePlacementsToRoot(BIMFileRootId, placementRelToId, relativePlacementsToRoot);

		return relativePlacementsToRoot;
	}

	/**
	 * Returns integer representing BIM Id.
	 * @param BIMIdString BIM Id
	 * @return Integer representing BIM Id
	 */
	private int getIntFromBIMId(String BIMIdString) {
		return Integer.valueOf(BIMIdString.substring(1));
	}

	/**
	 * Parses string of coordinates from IFC file into proper double
	 * @param coordString String of coordinate
	 * @return double representing coordinate
	 */
	private double prepareCoordinateString(String coordString) {
		if(coordString.endsWith(".")) {
			coordString = coordString + "0";
		}
		try {
			return Double.parseDouble(coordString);
		}catch(NumberFormatException e) {
			Logging.error(e.getMessage());
			return Double.NaN;
		}
	}

	/**
	 * Shows error dialog is file loading failed
	 * @param filepath of ifc file
	 * @param msg Error message
	 */
	private void showsLoadingErrorView(String filepath, String msg) {
		showErrorView(tr(msg));
		Logging.info(this.getClass().getName() + ": " + filepath + " loading failed");
	}

	/**
	 * Shows error dialog is file loading failed
	 * @param filepath of ifc file
	 * @param msg Error message
	 */
	private void showsParsingErrorView(String filepath, String msg) {
		showErrorView(tr(msg));
		Logging.info(this.getClass().getName() + ": " + filepath + " parsing failed");
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


}
