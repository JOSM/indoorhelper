package parser;

import java.util.ArrayList;
import java.util.Vector;

import org.openstreetmap.josm.tools.Logging;

import model.io.BIMtoOSMCatalog;
import nl.tue.buildingsmart.express.population.EntityInstance;
import nl.tue.buildingsmart.express.population.ModelPopulation;
import parser.data.FilteredRawBIMData;
import parser.data.Point3D;
import parser.data.PreparedBIMObject3D;

/**
 * Class helps parsing BIM data with providing methods to extract OSM relevant data.
 *
 * @author rebsc
 *
 */
public class BIMtoOSMHelper {

	/**
	 * Filters important OSM data into internal data structure
	 * @param ifcModel ifcModel
	 * @return FilteredBIMData including BIM objects of ways, rooms, etc.
	 */
	public static FilteredRawBIMData extractMajorBIMData(ModelPopulation ifcModel) {
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
	public static int getIFCLOCALPLACEMENTRootObject(FilteredRawBIMData filteredBIMdata) {
		Object BIMRoot = filteredBIMdata.getIfcSite().getAttributeValueBN("ObjectPlacement");
		if(BIMRoot == null)	return -1;
		return getIntFromBIMId(BIMRoot.toString());
	}

	/**
	 * Returns integer representing BIM Id.
	 * @param BIMIdString BIM Id
	 * @return Integer representing BIM Id
	 */
	private static int getIntFromBIMId(String BIMIdString) {
		return Integer.valueOf(BIMIdString.substring(1));
	}

	/**
	 * Prepares BIM objects for further operations. Extracts OSM relevant information and puts it into {@link PreparedBIMObject3D}
	 * @param ifcModel ifcModel
	 * @param BIMFileRootId Root IFCLOCALPLACEMENT element of BIM file
	 * @param objectType relating BIMtoOSMCatalog.BIMObject
	 * @param BIMObjects All BIM objects of objectType
	 * @return Prepared BIM objects
	 */
	public static ArrayList<PreparedBIMObject3D> prepareBIMObjects(ModelPopulation ifcModel, int BIMFileRootId, BIMtoOSMCatalog.BIMObject objectType, Vector<EntityInstance> BIMObjects) {
		ArrayList<PreparedBIMObject3D> preparedObjects = new ArrayList<>();
		for(EntityInstance object : BIMObjects) {

			// get IFCLOCALPLACEMENT of object
			Point3D cartesianCornerOfWall = getCartesianOriginOfObject(ifcModel, BIMFileRootId, object);
			// create PreparedBIMObject3D and save
			if(cartesianCornerOfWall != null) {
				preparedObjects.add(new PreparedBIMObject3D(objectType, cartesianCornerOfWall));
			}

			// TODO extract/prepare more relevant information (object width, height etc.)
		}
		return preparedObjects;
	}

	/**
	 * Get global cartesian origin coordinates of object
	 * @param ifcModel ifcModel
	 * @param BIMFileRootId Root IFCLOCALPLACEMENT element of BIM file
	 * @param object to find origin for
	 * @return cartesian origin
	 */
	private static Point3D getCartesianOriginOfObject(ModelPopulation ifcModel, int BIMFileRootId, EntityInstance object) {
		Object objectIFCLP = object.getAttributeValueBN("ObjectPlacement");
		int objectIFCLPId = getIntFromBIMId(objectIFCLP.toString());

		// get all RELATIVEPLACEMENTs to root
		ArrayList<Integer> objectRP = getRelativePlacementsToRoot(ifcModel, BIMFileRootId, objectIFCLPId, new ArrayList<Integer>());

		// calculate cartesian corner of object (origin) by using the relative placements
		Point3D cartesianCornerOfWall = new Point3D(0.0, 0.0, 0.0);

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
				return null;
			}

			// add relative coordinates to finally get relative position to root element
			cartesianCornerOfWall.setX(cartesianCornerOfWall.getX() + relativeX);
			cartesianCornerOfWall.setY(cartesianCornerOfWall.getY() + relativeY);
			cartesianCornerOfWall.setZ(cartesianCornerOfWall.getZ() + relativeZ);
		}
		return cartesianCornerOfWall;
	}

	/**
	 * Method recursive walks thru IFC file and collects the RELATIVEPLACEMENT Ids of from start entity to root entity
	 * @param ifcModel ifcModel
	 * @param BIMFileRootId Root IFCLOCALPLACEMENT element of BIMFile
	 * @param entityId Id of entity you want to collect the RELATIVEPLACEMENT from
	 * @param relativePlacementsToRoot empty list at beginning, needed for recursive iteration
	 * @return List with Ids to RELATIVEPLACEMENTs
	 */
	private static ArrayList<Integer> getRelativePlacementsToRoot(ModelPopulation ifcModel, int BIMFileRootId, int entityId, ArrayList<Integer> relativePlacementsToRoot){
		if(entityId == BIMFileRootId)	return relativePlacementsToRoot;

		// get and add relative placements (RELATIVEPLACEMENT)
		Object relativePlacement = ifcModel.getEntity(entityId).getAttributeValueBN("RelativePlacement");
		int relativePlacementId = getIntFromBIMId(relativePlacement.toString());
		relativePlacementsToRoot.add(relativePlacementId);

		// get id of placement relative to this (PLACEMENTRELTO)
		Object placementRelTo = ifcModel.getEntity(entityId).getAttributeValueBN("PlacementRelTo");
		int placementRelToId = getIntFromBIMId(placementRelTo.toString());
		getRelativePlacementsToRoot(ifcModel, BIMFileRootId, placementRelToId, relativePlacementsToRoot);

		return relativePlacementsToRoot;
	}


	/**
	 * Parses string of coordinates from IFC file into proper double
	 * @param coordString String of coordinate
	 * @return double representing coordinate
	 */
	private static double prepareCoordinateString(String coordString) {
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


}
