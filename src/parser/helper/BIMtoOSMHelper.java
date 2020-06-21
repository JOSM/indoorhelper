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
import parser.helper.IFCShapeRepresentationCatalog.AdvancedBrepRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.AdvancedSweptSolidRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.BoundingBoxRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.BrepRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.CSGRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.ClippingRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.CurveRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.MappedRepresentatiobTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.RepresentationIdentifier;
import parser.helper.IFCShapeRepresentationCatalog.SurfaceModelRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.SweptSolidRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.TessellationRepresentationTypeItems;

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

			// get IFCLOCALPLACEMENT of object (origin of object)
			Point3D cartesianCornerOfObject = getCartesianOriginOfObject(ifcModel, BIMFileRootId, object);

			// get points representing shape of object
			ArrayList<Point3D> shapeDataOfObject = getShapeDataOfObject(ifcModel, BIMFileRootId, object);

			// create PreparedBIMObject3D and save
			if(cartesianCornerOfObject != null && (shapeDataOfObject != null && !shapeDataOfObject.isEmpty())) {
				// TODO add shapeData
				preparedObjects.add(new PreparedBIMObject3D(objectType, cartesianCornerOfObject));
			}
		}
		return preparedObjects;
	}

	/**
	 * Method gets shape representation of IFC object
	 * @param ifcModel ifcModel
	 * @param BIMFileRootId Root IFCLOCALPLACEMENT element of BIM file
	 * @param object BIM objects
	 * @return Array including points of shape representation
	 */
	private static ArrayList<Point3D> getShapeDataOfObject(ModelPopulation ifcModel, int BIMFileRootId, EntityInstance object) {
		ArrayList<Point3D> shapeData = new ArrayList<>();

		// get IfcProductDefinitionShape.Representations objects
		ArrayList<Integer> objectRepresentations = getRepresentationsOfObject(ifcModel, object);

		// identify and keep type of IfcProductDefinitionShape.Representations objects
		ArrayList<Pair<Integer, IFCShapeRepresentationIdentity>> repObjectIdentities =
				identifyRepresentationsOfObject(ifcModel, objectRepresentations);

		//INFO: For now only RepresentationIdentifier axis, body and box are supported because those
		//are mostly used for representing considered objects

		// first check if IfcProductDefinitionShape.Representations include IfcShapeRepresentation of type "body"
		Pair<Integer, IFCShapeRepresentationIdentity> bodyRepresentation = getRepresentationSpecificObjectType(repObjectIdentities, RepresentationIdentifier.Body);
		if(bodyRepresentation != null) {
			return getDataFromBodyRepresentation(ifcModel, bodyRepresentation);
		}

		// if no IfcShapeRepresentation of type "body" check if IfcShapeRepresentation of type "box" exists
		Pair<Integer, IFCShapeRepresentationIdentity> boxRepresentation = getRepresentationSpecificObjectType(repObjectIdentities, RepresentationIdentifier.Box);
		if(boxRepresentation != null) {
			return getDataFromBoxRepresentation(ifcModel, boxRepresentation);

		}

		// if no IfcShapeRepresentation of type "box" check if IfcShapeRepresentation of type "axis" exists
		Pair<Integer, IFCShapeRepresentationIdentity> axisRepresentation = getRepresentationSpecificObjectType(repObjectIdentities, RepresentationIdentifier.Axis);
		if(axisRepresentation != null) {
			return getDataFromAxisRepresentation(ifcModel, axisRepresentation);
		}

		return shapeData;
	}

	/**
	 * Check the IfcShapeRepresentation objects for object with IfcShapeRepresentation.RepresentationIdentifier = "identifier" and
	 * returns it
	 * @param repObjectIdentities IfcShapeRepresentation objects
	 * @param identifier RepresentationIdentifier
	 * @return returns IfcShapeRepresentation "identifier" or null if not in list
	 */
	private static Pair<Integer, IFCShapeRepresentationIdentity> getRepresentationSpecificObjectType (
			ArrayList<Pair<Integer, IFCShapeRepresentationIdentity>> repObjectIdentities, RepresentationIdentifier identifier){

		for(Pair<Integer, IFCShapeRepresentationIdentity> repObject : repObjectIdentities) {
			IFCShapeRepresentationIdentity repObjectType = repObject.getValue();
			if(repObjectType.getIdentifier().equals(identifier))	return repObject;
		}
		return null;
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

	/**
	 * Get Ids of IFCSHAPEREPRESENTATIONs of object
	 * @param ifcModel ifcModel
	 * @param object to find origin for
	 * @return List with Ids of IFCSHAPEREPRESENTATIONs
	 */
	private static ArrayList<Integer> getRepresentationsOfObject (ModelPopulation ifcModel, EntityInstance object){
		ArrayList<Integer> objectIFCSRIds = new ArrayList<>();

		// get IFCPRODUCTDEFINITIONSHAPE of object
		Object objectIFCPDS = object.getAttributeValueBN("Representation");
		int objectIFCPDSId = getIntFromBIMId(objectIFCPDS.toString());

		// get all IFCSHAPEREPRESENTATIONS of IFCOBJECT
		@SuppressWarnings("unchecked")
		Vector<Object> objectIFCSR = (Vector<Object>) ifcModel.getEntity(objectIFCPDSId).getAttributeValueBN("Representations");

		// extract from each IFCSHAPEREPRESENTATION object the Id and save it into objectIFCPDSId
		objectIFCSR.forEach(shapeId ->{
			objectIFCSRIds.add(getIntFromBIMId(shapeId.toString()));
		});

		return objectIFCSRIds;
	}

	/**
	 * Identifies the type of an IfcRepresentation object.
	 * @param ifcModel ifcModel
	 * @param objectRepresentations List with Ids of IfcProductDefinitionShape.Representations objects
	 * @return List of Pairs with IfcRepresentation object id and identifier. Returns null if type is not allowed in IFC standard
	 */
	private static ArrayList<Pair<Integer, IFCShapeRepresentationIdentity>> identifyRepresentationsOfObject(ModelPopulation ifcModel, ArrayList<Integer> objectRepresentations){
		ArrayList<Pair<Integer, IFCShapeRepresentationIdentity>> repObjectIdentities = new ArrayList<>();
		for(Integer repObjectId : objectRepresentations) {
			// get IfcShapeRepresentation object from id
			EntityInstance objectIFCSR = ifcModel.getEntity(repObjectId);
			//identify IfcShapeRepresentation type
			IFCShapeRepresentationIdentity repIdentity = IFCShapeRepresentationIdentifier.identifyShapeRepresentation(objectIFCSR);
			if(!repIdentity.isFilled())	return null;
			repObjectIdentities.add(new Pair<>(objectIFCSR.getId(), repIdentity));
		}
		return repObjectIdentities;
	}

	/**
	 * Extract representation data from IfcRepresentationItem body
	 * @param ifcModel ifc Model
	 * @param bodyRepresentation representation of body
	 * @return List of points representing object shape or null if object type not supported
	 */
	private static ArrayList<Point3D> getDataFromBodyRepresentation(ModelPopulation ifcModel, Pair<Integer, IFCShapeRepresentationIdentity> bodyRepresentation) {
		ArrayList<Point3D> shapeRep = new ArrayList<>();

		// get IFCObject and RepresentationIdentifier
		EntityInstance repObject = ifcModel.getEntity(bodyRepresentation.getKey());
		IFCShapeRepresentationIdentity repObjectType = bodyRepresentation.getValue();

		// get IfcRepresentationItems
		@SuppressWarnings("unchecked")
		Vector<String> bodyItems = (Vector<String>) repObject.getAttributeValueBN("Items");

		// extract informations from IfcRepresentationItems
		for(String item : bodyItems) {
			// get type of item
			String repItemType = IFCShapeRepresentationIdentifier.getRepresentationItemType(ifcModel, repObjectType, item);
			if(repItemType == null) return null;

			// handle types
			if(repItemType.equals(AdvancedBrepRepresentationTypeItems.IfcAdvancedBrep.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(AdvancedBrepRepresentationTypeItems.IfcFacetedBrep.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(AdvancedSweptSolidRepresentationTypeItems.IfcSweptDiskSolid.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(AdvancedSweptSolidRepresentationTypeItems.IfcSweptDiskSolidPolygonal.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(BrepRepresentationTypeItems.IfcFacetedBrep.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(CSGRepresentationTypeItems.IfcBooleanResult.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(CSGRepresentationTypeItems.IfcCsgSolid.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(CSGRepresentationTypeItems.IfcPrimitive3D.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(TessellationRepresentationTypeItems.IfcTessellatedFaceSet.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(ClippingRepresentationTypeItems.IfcBooleanClippingResult.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(SurfaceModelRepresentationTypeItems.IfcTessellatedItem.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(SurfaceModelRepresentationTypeItems.IfcShellBasedSurfaceModel.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(SurfaceModelRepresentationTypeItems.IfcFaceBasedSurfaceModel.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(SurfaceModelRepresentationTypeItems.IfcFacetedBrep.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(SweptSolidRepresentationTypeItems.IfcExtrudedAreaSolid.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(SweptSolidRepresentationTypeItems.IfcRevolvedAreaSolid.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(MappedRepresentatiobTypeItems.IfcMappedItem.name())) {
				// TODO extract data
			}
		}

		return shapeRep;
	}

	/**
	 * Extract representation data from IfcRepresentationItem box
	 * @param ifcModel ifc Model
	 * @param boxRepresentation representation of box
	 * @return List of points representing object shape or null if object type not supported
	 */
	private static ArrayList<Point3D> getDataFromBoxRepresentation(ModelPopulation ifcModel, Pair<Integer, IFCShapeRepresentationIdentity> boxRepresentation) {
		ArrayList<Point3D> shapeRep = new ArrayList<>();

		// get IFCObject and RepresentationIdentifier
		EntityInstance repObject = ifcModel.getEntity(boxRepresentation.getKey());
		IFCShapeRepresentationIdentity repObjectType = boxRepresentation.getValue();

		// get IfcRepresentationItems
		@SuppressWarnings("unchecked")
		Vector<String> boxItems = (Vector<String>) repObject.getAttributeValueBN("Items");

		// extract informations from IfcRepresentationItems
		for(String item : boxItems) {
			// get type of IfcRepresentationItem
			String repItemType = IFCShapeRepresentationIdentifier.getRepresentationItemType(ifcModel, repObjectType, item);
			if(repItemType == null) return null;

			if(repItemType.equals(BoundingBoxRepresentationTypeItems.IfcBoundingBox.name())) {
				// TODO extract data
			}
		}

		return shapeRep;
	}

	/**
	 * Extract representation data from IfcRepresentationItem axis
	 * @param ifcModel ifc Model
	 * @param axisRepresentation representation of axis
	 * @return List of points representing object shape or null if object type not supported
	 */
	private static ArrayList<Point3D> getDataFromAxisRepresentation(ModelPopulation ifcModel, Pair<Integer, IFCShapeRepresentationIdentity> axisRepresentation) {
		ArrayList<Point3D> shapeRep = new ArrayList<>();

		// get IFCObject and RepresentationIdentifier
		EntityInstance repObject = ifcModel.getEntity(axisRepresentation.getKey());
		IFCShapeRepresentationIdentity repObjectType = axisRepresentation.getValue();

		// get IfcRepresentationItems of object
		@SuppressWarnings("unchecked")
		Vector<String> axisItems = (Vector<String>) repObject.getAttributeValueBN("Items");

		// extract informations from IfcRepresentationItems
		for(String item : axisItems) {
			// get type of IfcRepresentationItem
			String repItemType = IFCShapeRepresentationIdentifier.getRepresentationItemType(ifcModel, repObjectType, item);
			if(repItemType == null) return null;

			if(repItemType.equals(CurveRepresentationTypeItems.IfcBoundedCurve.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(CurveRepresentationTypeItems.IfcPolyline.name())) {
				// TODO extract data
			}
		}

		return shapeRep;
	}

	/**
	 * Helper class. Stores a pair.
	 * @author rebsc
	 *
	 * @param <T1> key
	 * @param <T2> value
	 */
	private static class Pair<T1, T2> {
		private T1 key;
		private T2 value;

		public Pair(T1 key, T2 value) {
			this.key = key;
			this.value = value;
		}

		public T1 getKey() {
			return key;
		}

		public T2 getValue() {
			return value;
		}
	}


}
