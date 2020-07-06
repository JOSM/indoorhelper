// License: GPL. For details, see LICENSE file.
package parser.helper;

import java.util.ArrayList;
import java.util.Vector;

import org.openstreetmap.josm.tools.Logging;

import model.io.BIMtoOSMCatalog;
import nl.tue.buildingsmart.express.population.EntityInstance;
import nl.tue.buildingsmart.express.population.ModelPopulation;
import parser.data.FilteredRawBIMData;
import parser.data.Point3D;
import parser.data.PreparedBIMObject3D;
import parser.data.ifc.IFCShapeRepresentationIdentity;
import parser.helper.IFCShapeRepresentationCatalog.RepresentationIdentifier;

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
	public static int getIfcLocalPlacementRootObject(FilteredRawBIMData filteredBIMdata) {
		try {
			EntityInstance BIMRoot = filteredBIMdata.getIfcSite().getAttributeValueBNasEntityInstance("ObjectPlacement");
			return BIMRoot.getId();
		}
		catch(NullPointerException e) {
			return -1;
		}
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

			// get IFCLOCALPLACEMENT of object (origin of object)
			Point3D cartesianPlacementOfObject = getCartesianOriginOfObject(BIMFileRootId, object);

			// get local points representing shape of object
			ArrayList<Point3D> shapeDataOfObject = getShapeDataOfObject(ifcModel, object);

			// create PreparedBIMObject3D and save
			if(cartesianPlacementOfObject != null && (shapeDataOfObject != null && !shapeDataOfObject.isEmpty())) {
				// transform points to object placement origin
				shapeDataOfObject.forEach(point ->{
					point.setX(point.getX() + cartesianPlacementOfObject.getX());
					point.setY(point.getY() + cartesianPlacementOfObject.getY());
				});

				preparedObjects.add(new PreparedBIMObject3D(objectType, cartesianPlacementOfObject, shapeDataOfObject));
			}
		}
		return preparedObjects;
	}

	/**
	 * Method gets local shape representation of IFC object
	 * @param ifcModel ifcModel
	 * @param object BIM object
	 * @return Array including points of shape representation
	 */
	private static ArrayList<Point3D> getShapeDataOfObject(ModelPopulation ifcModel, EntityInstance object) {
		ArrayList<Point3D> shapeData = new ArrayList<>();

		// get EntityInstances of IFCPRODUCTDEFINITIONSHAPE.REPRESENTATIONS objects
		ArrayList<EntityInstance> objectRepresentations = getRepresentationsOfObject(object);

		// identify and keep types of IFCPRODUCTDEFINITIONSHAPE.REPRESENTATIONS objects
		ArrayList<IFCShapeRepresentationIdentity> repObjectIdentities = identifyRepresentationsOfObject(objectRepresentations);

		//INFO: For now only REPRESENTATIONIDENFITIER axis, body and box are supported because those
		//are mostly used for representing considered objects

		// first check if IFCPRODUCTDEFINITIONSHAPE.REPRESENTATIONS include IFCSHAPEREPRESENTATION of type "body"
		IFCShapeRepresentationIdentity bodyRepresentation = getRepresentationSpecificObjectType(repObjectIdentities, RepresentationIdentifier.Body);
		if(bodyRepresentation != null) {
			return IFCShapeDataExtractor.getDataFromBodyRepresentation(ifcModel, bodyRepresentation);
		}

		// if no IFCSHAPEREPRESENTATION of type "body" check if IFCSHAPEREPRESENTATION of type "box" exists
		IFCShapeRepresentationIdentity boxRepresentation = getRepresentationSpecificObjectType(repObjectIdentities, RepresentationIdentifier.Box);
		if(boxRepresentation != null) {
			return IFCShapeDataExtractor.getDataFromBoxRepresentation(ifcModel, boxRepresentation);
		}

		// if no IFCSHAPEREPRESENTATION of type "box" check if IFCSHAPEREPRESENTATION of type "axis" exists
		IFCShapeRepresentationIdentity axisRepresentation = getRepresentationSpecificObjectType(repObjectIdentities, RepresentationIdentifier.Axis);
		if(axisRepresentation != null) {
			return IFCShapeDataExtractor.getDataFromAxisRepresentation(ifcModel, axisRepresentation);
		}

		return shapeData;
	}

	/**
	 * Checks the IFCSHAPEREPRESENTATION objects for object with IFCSHAPEREPRESENTATION.REPRESENTATIONIDENTIFIER = "identifier" and
	 * returns it
	 * @param repObjectIdentities IfcShapeRepresentation objects
	 * @param identifier RepresentationIdentifier
	 * @return returns IfcShapeRepresentation "identifier" or null if not in list
	 */
	private static IFCShapeRepresentationIdentity getRepresentationSpecificObjectType (
			ArrayList<IFCShapeRepresentationIdentity> repObjectIdentities, RepresentationIdentifier identifier){

		for(IFCShapeRepresentationIdentity repObject : repObjectIdentities) {
			if(repObject.getIdentifier().equals(identifier))	return repObject;
		}
		return null;
	}

	/**
	 * Get global cartesian origin coordinates of object
	 * @param BIMFileRootId Root IFCLOCALPLACEMENT element of BIM file
	 * @param object to find origin for
	 * @return cartesian origin
	 */
	private static Point3D getCartesianOriginOfObject(int BIMFileRootId, EntityInstance object) {

		// get objects IFCLOCALPLACEMENT entity
		EntityInstance objectIFCLP = object.getAttributeValueBNasEntityInstance("ObjectPlacement");

		// get all RELATIVEPLACEMENTs to root
		ArrayList<EntityInstance> objectRP = getRelativePlacementsToRoot(BIMFileRootId, objectIFCLP, new ArrayList<EntityInstance>());

		// calculate cartesian corner of object (origin) by using the relative placements
		Point3D cartesianCornerOfWall = new Point3D(0.0, 0.0, 0.0);

		for(EntityInstance relativeObject : objectRP) {
			// get LOCATION (IFCCARTESIANPOINT) of IFCAXIS2PLACEMENT2D/3D including relative coordinates
			EntityInstance cPoint = relativeObject.getAttributeValueBNasEntityInstance("Location");
			@SuppressWarnings("unchecked")
			Vector<String> objectCoords = (Vector<String>)cPoint.getAttributeValueBN("Coordinates");
			if(objectCoords.isEmpty())	return null;
			double relativeX = prepareDoubleString(objectCoords.get(0));
			double relativeY = prepareDoubleString(objectCoords.get(1));
			double relativeZ = prepareDoubleString(objectCoords.get(2));
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
	 * Method recursive walks thru IFC file and collects the RELATIVEPLACEMENT EntityInstances from start entity to root entity
	 * @param BIMFileRootId of root IFCLOCALPLACEMENT element of BIMFile
	 * @param entity you want to collect the RELATIVEPLACEMENT from
	 * @param relativePlacementsToRoot empty list at beginning, needed for recursive iteration
	 * @return List with EntityInstances of RELATIVEPLACEMENTs
	 */
	private static ArrayList<EntityInstance> getRelativePlacementsToRoot(int BIMFileRootId, EntityInstance entity, ArrayList<EntityInstance> relativePlacementsToRoot){
		if(entity.getId() == BIMFileRootId)	return relativePlacementsToRoot;

		// get objects IFCRELATIVEPLACEMENT entity
		EntityInstance relativePlacement = entity.getAttributeValueBNasEntityInstance("RelativePlacement");
		relativePlacementsToRoot.add(relativePlacement);

		// get id of placement relative to this (PLACEMENTRELTO)
		EntityInstance placementRelTo = entity.getAttributeValueBNasEntityInstance("PlacementRelTo");
		getRelativePlacementsToRoot(BIMFileRootId, placementRelTo, relativePlacementsToRoot);

		return relativePlacementsToRoot;
	}

	/**
	 * Get EntityInstances of IFCSHAPEREPRESENTATIONs of object
	 * @param object to find origin for
	 * @return List with EntityInstances of IFCSHAPEREPRESENTATIONs
	 */
	private static ArrayList<EntityInstance> getRepresentationsOfObject (EntityInstance object){
		// get IFCPRODUCTDEFINITIONSHAPE of object
		EntityInstance objectIFCPDS = object.getAttributeValueBNasEntityInstance("Representation");

		// get all IFCSHAPEREPRESENTATIONS of IFCOBJECT
		ArrayList<EntityInstance> objectIFCSR = objectIFCPDS.getAttributeValueBNasEntityInstanceList("Representations");

		return objectIFCSR;
	}

	/**
	 * Identifies the type of an IFCREPRESENTATION object.
	 * @param objectRepresentations List with EntityInstances of IFCPRODUCTDEFINITIONSHAPE.REPRESENTATIONS objects
	 * @return List of IFCShapeRepresentationIdentity holding an IFC representation object and it's identifier
	 */
	private static ArrayList<IFCShapeRepresentationIdentity> identifyRepresentationsOfObject(ArrayList<EntityInstance> objectRepresentations){
		ArrayList< IFCShapeRepresentationIdentity> repObjectIdentities = new ArrayList<>();

		for(EntityInstance repObject : objectRepresentations) {
			//identify IFCSHAPEREPRESENTATION type
			IFCShapeRepresentationIdentity repIdentity = IFCShapeRepresentationIdentifier.identifyShapeRepresentation(repObject);
			if(!repIdentity.isFilled())	return null;
			repObjectIdentities.add(repIdentity);
		}

		return repObjectIdentities;
	}

	/**
	 * Parses string of double value from IFC file into proper double
	 * @param doubleString String of coordinate
	 * @return double representing double
	 */
	private static double prepareDoubleString(String doubleString) {
		if(doubleString.endsWith(".")) {
			doubleString = doubleString + "0";
		}
		try {
			return Double.parseDouble(doubleString);
		}catch(NumberFormatException e) {
			Logging.error(e.getMessage());
			return Double.NaN;
		}
	}

}
