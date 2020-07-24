// License: GPL. For details, see LICENSE file.
package io.parser;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Pair;

import io.controller.ImportEventListener;
import io.model.BIMtoOSMCatalog;
import io.parser.data.FilteredRawBIMData;
import io.parser.data.Point3D;
import io.parser.data.PreparedBIMObject3D;
import io.parser.data.helper.BIMtoOSMHelper;
import io.parser.data.helper.IFCShapeDataExtractor;
import model.TagCatalog;
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
	private TagCatalog tagCatalog;

	private int defaultLevel = 999;

	public BIMtoOSMParser(ImportEventListener listener) {
		importListener = listener;
		tagCatalog = new TagCatalog();
	}

	/**
	 * Method parses data from IFC file into OSM data
	 * @param filepath of IFC file
	 */
	public void parse(String filepath) {
		// load data into IFC model
		if(!loadFile(filepath))	return;

		// extract important data and put them into internal data structure
		FilteredRawBIMData filteredBIMdata = BIMtoOSMHelper.extractMajorBIMData(ifcModel);

		// for preparation of filtered BIM data find Id of BIM root IFCLOCALPLACEMENT element (kept in IFCSITE flag)
		int BIMRootId = BIMtoOSMHelper.getIfcLocalPlacementRootObject(filteredBIMdata);
		if(BIMRootId == -1) {
			showParsingErrorView(filepath, "Could not import IFC file.\nIFC file does not contains IFCSITE element.", true);
			return;
		}

		// prepare filtered BIM data - find global object coordinates and other attributes like object height, width etc.
		ArrayList<PreparedBIMObject3D> preparedBIMdata = new ArrayList<>();
		preparedBIMdata.addAll(BIMtoOSMHelper.prepareBIMObjects(ifcModel, BIMRootId, BIMtoOSMCatalog.BIMObject.IfcSlab, filteredBIMdata.getAreaObjects()));
		preparedBIMdata.addAll(BIMtoOSMHelper.prepareBIMObjects(ifcModel, BIMRootId, BIMtoOSMCatalog.BIMObject.IfcWall, filteredBIMdata.getWallObjects()));
		preparedBIMdata.addAll(BIMtoOSMHelper.prepareBIMObjects(ifcModel, BIMRootId, BIMtoOSMCatalog.BIMObject.IfcColumn, filteredBIMdata.getColumnObjects()));
//		preparedBIMdata.addAll(BIMtoOSMHelper.prepareBIMObjects(ifcModel, BIMRootId, BIMtoOSMCatalog.BIMObject.IfcDoor, filteredBIMdata.getDoorObjects()));
//		preparedBIMdata.addAll(BIMtoOSMHelper.prepareBIMObjects(ifcModel, BIMRootId, BIMtoOSMCatalog.BIMObject.IfcWindow, filteredBIMdata.getWindowObjects()));
		preparedBIMdata.addAll(BIMtoOSMHelper.prepareBIMObjects(ifcModel, BIMRootId, BIMtoOSMCatalog.BIMObject.IfcStair, filteredBIMdata.getStairObjects()));
		//TODO add IFCBEAM!

		// TODO transform coordinates

		// TODO parse FilteredBIMData into OSM DataSet
		ArrayList<Way> ways = new ArrayList<>();
		ArrayList<Node> nodes = new ArrayList<>();


		// TODO fix, for development only -----------
		ArrayList<Pair<Double,Integer>> levelIdentifier = extractAndIdentifyLevels();

		for(PreparedBIMObject3D object : preparedBIMdata){

			int level = getLevelTagOfPreparedBIMObject(object, levelIdentifier);

			ArrayList<Node> tmpNodes = new ArrayList<>();
			for(Point3D point : object.getCartesianShapeCoordinates()) {
				Node n = new Node(new LatLon(point.getY(), point.getX()));
				tmpNodes.add(n);
			}
			if(tmpNodes.isEmpty())	continue;

			if(tmpNodes.get(0).lat() == tmpNodes.get(tmpNodes.size()-1).lat() && tmpNodes.get(0).lon() == tmpNodes.get(tmpNodes.size()-1).lon()) {
				tmpNodes.remove(tmpNodes.size()-1);
				nodes.addAll(tmpNodes);
				tmpNodes.add(tmpNodes.get(0));
			}
			else {
				nodes.addAll(tmpNodes);
			}
			Way w = new Way();
			w.setNodes(tmpNodes);
			getObjectTags(object).forEach(tag->{
				w.put(tag);
			});
			if(level != defaultLevel)	w.put(new Tag("level", Integer.toString(level)));
			ways.add(w);
		}
		// -----------


		// check if file is corrupted. File is corrupted if some data could not pass the preparation steps
		if(preparedBIMdata.size() != filteredBIMdata.getSize()) {
			showParsingErrorView(filepath, "Caution!\nImported data might include errors!", false);
		}

		// send parsed data to controller
		importListener.onDataParsed(ways, nodes);
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
				showLoadingErrorView(filepath, "Could not load IFC file.\nIFC schema is no supported.");
				return false;
			}
			if(usedIfcSchema.equals(FLAG_IFC4)) {
				ifcSchemaFilePath = resourcePathDir + "IFC4.exp";
			}

			// load IFC file
			inputfs = new FileInputStream(filepath);
			// load IFC file data into model
			ifcModel = new ModelPopulation(inputfs);
			ifcModel.setSchemaFile(Paths.get(ifcSchemaFilePath));
			ifcModel.load();

			// if loading throws ParseException check if ifcModel is empty to recognize something went wrong
			if(ifcModel.getInstances() == null) {
				showLoadingErrorView(filepath, "Could not load IFC file.");
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
	 * Shows error dialog is file loading failed
	 * @param filepath of ifc file
	 * @param msg Error message
	 */
	private void showLoadingErrorView(String filepath, String msg) {
		showErrorView(tr(msg));
		Logging.info(this.getClass().getName() + ": " + filepath + " loading failed");
	}

	/**
	 * Shows error dialog is file loading failed
	 * @param filepath of IFC file
	 * @param msg Error message
	 * @param logInfo log info to console
	 */
	private void showParsingErrorView(String filepath, String msg, boolean logInfo) {
		showErrorView(tr(msg));
		if(logInfo) {
			Logging.info(this.getClass().getName() + ": " + filepath + " parsing failed");
		}
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
	 * Method gets level tag of PreparedBIMObject3D
	 * @param object to get level tag for
	 * @param levelIdentifierList with identified levels
	 * @return level
	 */
	private int getLevelTagOfPreparedBIMObject(PreparedBIMObject3D object, ArrayList<Pair<Double,Integer>> levelIdentifierList) {
		int level = defaultLevel;

		// get all IfcRelContainedInSpatialStructure elements
		Vector<EntityInstance> relContainedInSpatialStructureElements = ifcModel.getInstancesOfType("IfcRelContainedInSpatialStructure");

		for(EntityInstance entity : relContainedInSpatialStructureElements) {
			// for each element get contained entities
			ArrayList<EntityInstance> containedElements = entity.getAttributeValueBNasEntityInstanceList("RelatedElements");

			// check if object is part of contained entities
			for(EntityInstance element : containedElements) {

				if(element.getId() == object.getObjectId()){
					// if part of contained elements get Elevation entity from object
					EntityInstance buildingStorey = entity.getAttributeValueBNasEntityInstance("RelatingStructure");
					double storeyElevation = IFCShapeDataExtractor.prepareDoubleString((String)buildingStorey.getAttributeValueBN("Elevation"));

					// get assigned level tag to Elevation entity
					for(Pair<Double,Integer> identifier : levelIdentifierList) {
						if(identifier.a == storeyElevation) {
							level = identifier.b;
							break;
						}
					}
				}

			}
		}
		return level;
	}

	/**
	 * Method extracts and identifies level tags from IfcRelContainedInSpatialStructure elements
	 * @return List with pairs of level Elevation entity (Double) and assigned level tag (Integer)
	 */
	private ArrayList<Pair<Double,Integer>> extractAndIdentifyLevels() {

		// get all IfcRelContainedInSpatialStructure elements
		Vector<EntityInstance> relContainedInSpatialStructureElements = ifcModel.getInstancesOfType("IfcRelContainedInSpatialStructure");

		ArrayList<Pair<Double, Integer>> levelIdentifier = new ArrayList<>();
		ArrayList<Double> levelList = new ArrayList<>();

		// run thru IfcRelContainedInSpatialStructure and get the buildingStorey elements. Those elements include an Elevation entity
		for(EntityInstance entity : relContainedInSpatialStructureElements) {
			EntityInstance buildingStorey = entity.getAttributeValueBNasEntityInstance("RelatingStructure");
			double storeyElevation = IFCShapeDataExtractor.prepareDoubleString((String)buildingStorey.getAttributeValueBN("Elevation"));
			levelList.add(storeyElevation);
		}

		// Sort the Elevation entity ascending
	    Collections.sort(levelList);

	    // assign level integer to every Elevation entity
	    int i = 0;
	    int k = -1;
	    for(Double level : levelList) {
	    	if(level < 0.0) {
	    		levelIdentifier.add(new Pair<>(level, k));
	    		--k;
	    	}
	    	if(level >= 0.0) {
	    		levelIdentifier.add(new Pair<>(level, i));
	    		++i;
	    	}
	    }
	    return levelIdentifier;
	}

	private ArrayList<Tag> getObjectTags(PreparedBIMObject3D object){
		if(object.getType().name().contains("Slab")) {
			return (ArrayList<Tag>) tagCatalog.getTags(TagCatalog.IndoorObject.ROOM);
		}
		if(object.getType().name().contains("Wall")) {
			return (ArrayList<Tag>) tagCatalog.getTags(TagCatalog.IndoorObject.CONCRETE_WALL);
		}
		if(object.getType().name().contains("Column")) {
			return (ArrayList<Tag>) tagCatalog.getTags(TagCatalog.IndoorObject.CONCRETE_WALL);
		}
		if(object.getType().name().contains("Door")) {
			return (ArrayList<Tag>) tagCatalog.getTags(TagCatalog.IndoorObject.DOOR_PRIVATE);
		}
		if(object.getType().name().contains("Window")) {
			return (ArrayList<Tag>) tagCatalog.getTags(TagCatalog.IndoorObject.GLASS_WALL);
		}
		if(object.getType().name().contains("Stair")) {
			return (ArrayList<Tag>) tagCatalog.getTags(TagCatalog.IndoorObject.STEPS);
		}
		return new ArrayList<>();
	}

}
