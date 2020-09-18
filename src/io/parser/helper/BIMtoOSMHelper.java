// License: AGPL. For details, see LICENSE file.
package io.parser.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import io.parser.data.BIMObject3D;
import io.parser.data.IFCShapeRepresentationIdentity;
import org.openstreetmap.josm.tools.Logging;

import io.model.BIMtoOSMCatalog;
import io.parser.data.FilteredRawBIMData;
import io.parser.data.Point3D;
import io.parser.data.IFCShapeRepresentationCatalog.IfcSlabTypeEnum;
import io.parser.data.IFCShapeRepresentationCatalog.RepresentationIdentifier;
import io.parser.math.ParserMath;
import nl.tue.buildingsmart.express.population.EntityInstance;
import nl.tue.buildingsmart.express.population.ModelPopulation;

/**
 * Class helps parsing BIM data with providing methods to extract OSM relevant data.
 *
 * @author rebsc
 */
public class BIMtoOSMHelper {

    /**
     * Filters important OSM data into internal data structure
     *
     * @param ifcModel ifcModel
     * @return FilteredBIMData including BIM objects of ways, rooms, etc.
     */
    public static FilteredRawBIMData extractMajorBIMData(ModelPopulation ifcModel) {
        FilteredRawBIMData bimData = new FilteredRawBIMData();

        // get the root element IFCSITE
        Vector<EntityInstance> ifcSiteObjects = new Vector<>();
        BIMtoOSMCatalog.getIFCSITETags().forEach(tag -> ifcSiteObjects.addAll(ifcModel.getInstancesOfType(tag)));

        if (!ifcSiteObjects.isEmpty()) {
            bimData.setIfcSite(ifcSiteObjects.firstElement());
        }

        // get all relevant areas
        Vector<EntityInstance> areaObjects = new Vector<>();
        BIMtoOSMCatalog.getAreaTags().forEach(tag -> ifcModel.getInstancesOfType(tag).forEach(entity -> {
            String identifier = (String) entity.getAttributeValueBN("PredefinedType");
            if (!identifier.equals("." + IfcSlabTypeEnum.ROOF + ".")) {
                areaObjects.add(entity);
            }
        }));
        bimData.setAreaObjects(areaObjects);

        // get all walls
        Vector<EntityInstance> wallObjects = new Vector<>();
        BIMtoOSMCatalog.getWallTags().forEach(tag -> wallObjects.addAll(ifcModel.getInstancesOfType(tag)));
        bimData.setWallObjects(wallObjects);

        // get all columns
        Vector<EntityInstance> colObjects = new Vector<>();
        BIMtoOSMCatalog.getColumnTags().forEach(tag -> colObjects.addAll(ifcModel.getInstancesOfType(tag)));
        bimData.setColumnObjects(colObjects);

        // get all doors
        Vector<EntityInstance> doorObjects = new Vector<>();
        BIMtoOSMCatalog.getDoorTags().forEach(tag -> doorObjects.addAll(ifcModel.getInstancesOfType(tag)));
        bimData.setDoorObjects(doorObjects);

        // get all doors
        Vector<EntityInstance> stairObjects = new Vector<>();
        BIMtoOSMCatalog.getStairTags().forEach(tag -> stairObjects.addAll(ifcModel.getInstancesOfType(tag)));
        bimData.setStairObjects(stairObjects);

        // get all windows
        Vector<EntityInstance> windowObjects = new Vector<>();
        BIMtoOSMCatalog.getWindowTags().forEach(tag -> windowObjects.addAll(ifcModel.getInstancesOfType(tag)));
        bimData.setWindowObjects(windowObjects);

        return bimData;
    }

    /**
     * Gets the LOCALPLACEMENT root element of IFC file
     *
     * @param filteredBIMData Data including IFCSITE flag of IFC file
     * @return Root LOCALPLACEMENT element of IFC file
     */
    public static int getIfcLocalPlacementRootObject(FilteredRawBIMData filteredBIMData) {
        try {
            return filteredBIMData.getIfcSite().getAttributeValueBNasEntityInstance("ObjectPlacement").getId();
        } catch (NullPointerException e) {
            return -1;
        }
    }

    /**
     * Prepares BIM objects for further operations. Extracts OSM relevant information and puts it into {@link BIMObject3D}
     *
     * @param ifcModel      ifcModel
     * @param bimFileRootId Root IFCLOCALPLACEMENT element of BIM file
     * @param objectType    relating BIMtoOSMCatalog.BIMObject
     * @param bimObjects    All BIM objects of objectType
     * @return Prepared BIM objects
     */
    public static List<BIMObject3D> prepareBIMObjects(ModelPopulation ifcModel, int bimFileRootId, BIMtoOSMCatalog.BIMObject objectType, Vector<EntityInstance> bimObjects) {
        // TODO improve object placement determination
        ArrayList<BIMObject3D> preparedObjects = new ArrayList<>();

        for (EntityInstance object : bimObjects) {
            // get IFCLOCALPLACEMENT of object (origin of object)
            Point3D cartesianPlacementOfObject = getCartesianOriginOfObject(bimFileRootId, object);

            // get rotation matrices of object
            double[][] xRotMatrix = getXAxisRotationMatrix(bimFileRootId, object);
            double[][] zRotMatrix = getZAxisRotationMatrix(ifcModel, bimFileRootId, object);

            // get local points representing shape of object
            List<Point3D> shapeDataOfObject = getShapeDataOfObject(ifcModel, object);

            // create PreparedBIMObject3D and save
            if (cartesianPlacementOfObject != null && (shapeDataOfObject != null && !shapeDataOfObject.isEmpty())) {
                // rotation about z-axis
                if (zRotMatrix != null) {
                    for (Point3D point : shapeDataOfObject) {
                        double[] pointAsVector = {point.getX(), point.getY(), point.getZ()};
                        double[] rotatedPoint = ParserMath.rotate3DPoint(pointAsVector, zRotMatrix);
                        point.setX(rotatedPoint[0]);
                        point.setY(rotatedPoint[1]);
                        point.setZ(rotatedPoint[2]);
                    }
                }

                // rotation about x-axis
                for (Point3D point : shapeDataOfObject) {
                    if (point.equalsPoint3D(IFCShapeDataExtractor.defaultPoint)) continue;    // for workaround
                    double[] pointAsVector = {point.getX(), point.getY(), point.getZ()};
                    double[] rotatedPoint = ParserMath.rotate3DPoint(pointAsVector, xRotMatrix);
                    if (rotatedPoint != null) {
                        point.setX(rotatedPoint[0]);
                        point.setY(rotatedPoint[1]);
                        point.setZ(rotatedPoint[2]);
                    }
                }

                // translate points to object placement origin
                for (Point3D point : shapeDataOfObject) {
                    if (point.equalsPoint3D(IFCShapeDataExtractor.defaultPoint)) {
                        continue;    // for workaround
                    }
                    point.setX(point.getX() + cartesianPlacementOfObject.getX());
                    point.setY(point.getY() + cartesianPlacementOfObject.getY());
                }

                // Check if data includes IFCShapeDataExtractor.defaultPoint. IFCShapeDataExtractor.defaultPoint got added
                // for workaround handling multiple closed loops in data set
                if (!shapeDataOfObject.contains(IFCShapeDataExtractor.defaultPoint)) {
					preparedObjects.add(new BIMObject3D(object.getId(), objectType, cartesianPlacementOfObject, shapeDataOfObject));
                    continue;
                }

                // Workaround: Check data set for closed loops (separated by defaultPoint). If closed loop in data set, extract and add as own way
                ArrayList<Point3D> loop = new ArrayList<>();
                for (Point3D point : shapeDataOfObject) {
                    if (point.equalsPoint3D(IFCShapeDataExtractor.defaultPoint) && !loop.isEmpty()) {
						preparedObjects.add(new BIMObject3D(object.getId(), objectType, cartesianPlacementOfObject, loop));
                        loop = new ArrayList<>();
                    } else if (!point.equalsPoint3D(IFCShapeDataExtractor.defaultPoint)) {
                        loop.add(point);
                    }
                    if (shapeDataOfObject.indexOf(point) == shapeDataOfObject.size() - 1 && !loop.isEmpty()) {
						preparedObjects.add(new BIMObject3D(object.getId(), objectType, cartesianPlacementOfObject, loop));
                    }
                }

            }
        }

        return preparedObjects;
    }

    /**
     * Method gets local shape representation of IFC object
     *
     * @param ifcModel ifcModel
     * @param object   BIM object
     * @return Array including points of shape representation
     */
    public static List<Point3D> getShapeDataOfObject(ModelPopulation ifcModel, EntityInstance object) {
        ArrayList<Point3D> shapeData = new ArrayList<>();

        // identify and keep types of IFCPRODUCTDEFINITIONSHAPE.REPRESENTATIONS objects
        List<IFCShapeRepresentationIdentity> repObjectIdentities = identifyRepresentationsOfObject(object);

        // first check if IFCPRODUCTDEFINITIONSHAPE.REPRESENTATIONS include IFCSHAPEREPRESENTATION of type "body"
        assert repObjectIdentities != null;
        IFCShapeRepresentationIdentity bodyRepresentation = getRepresentationSpecificObjectType(repObjectIdentities, RepresentationIdentifier.Body);
        if (bodyRepresentation != null && !IFCShapeRepresentationIdentifier.isIfcWindowOrIfcDoor(ifcModel, object)) {
            return IFCShapeDataExtractor.getDataFromBodyRepresentation(ifcModel, bodyRepresentation);
        }

        // if no IFCSHAPEREPRESENTATION of type "body" check if IFCSHAPEREPRESENTATION of type "box" exists
        IFCShapeRepresentationIdentity boxRepresentation = getRepresentationSpecificObjectType(repObjectIdentities, RepresentationIdentifier.Box);
        if (boxRepresentation != null) {
            return IFCShapeDataExtractor.getDataFromBoxRepresentation(ifcModel, boxRepresentation);
        }

        // if no IFCSHAPEREPRESENTATION of type "box" check if IFCSHAPEREPRESENTATION of type "axis" exists
        IFCShapeRepresentationIdentity axisRepresentation = getRepresentationSpecificObjectType(repObjectIdentities, RepresentationIdentifier.Axis);
        if (axisRepresentation != null) {
            return IFCShapeDataExtractor.getDataFromAxisRepresentation(ifcModel, axisRepresentation);
        }

        return shapeData;
    }

    /**
     * Checks the IFCSHAPEREPRESENTATION objects for object with IFCSHAPEREPRESENTATION.REPRESENTATIONIDENTIFIER = "identifier" and
     * returns it
     *
     * @param repObjectIdentities IfcShapeRepresentation objects
     * @param identifier          RepresentationIdentifier
     * @return returns IfcShapeRepresentation "identifier" or null if not in list
     */
    public static IFCShapeRepresentationIdentity getRepresentationSpecificObjectType(
            List<IFCShapeRepresentationIdentity> repObjectIdentities,
            RepresentationIdentifier identifier) {
        for (IFCShapeRepresentationIdentity repObject : repObjectIdentities) {
            if (repObject.getIdentifier().equals(identifier)) return repObject;
        }
        return null;
    }

    /**
     * Get global cartesian origin coordinates of object
     *
     * @param bimFileRootId Root IFCLOCALPLACEMENT element of BIM file
     * @param object        to find origin for
     * @return cartesian origin
     */
    private static Point3D getCartesianOriginOfObject(int bimFileRootId, EntityInstance object) {

        // get objects IFCLOCALPLACEMENT entity
        EntityInstance objectIFCLP = object.getAttributeValueBNasEntityInstance("ObjectPlacement");

        // get all RELATIVEPLACEMENTs to root
        ArrayList<EntityInstance> objectRP = getRelativePlacementsToRoot(bimFileRootId, objectIFCLP, new ArrayList<EntityInstance>());

        // calculate cartesian corner of object (origin) by using the relative placements
        Point3D cartesianCornerOfWall = new Point3D(0.0, 0.0, 0.0);

        for (EntityInstance relativeObject : objectRP) {
            // get LOCATION (IFCCARTESIANPOINT) of IFCAXIS2PLACEMENT2D/3D including relative coordinates
            EntityInstance cPoint = relativeObject.getAttributeValueBNasEntityInstance("Location");
            @SuppressWarnings("unchecked")
            Vector<String> objectCoords = (Vector<String>) cPoint.getAttributeValueBN("Coordinates");
            if (objectCoords.isEmpty()) return null;
            double relativeX = prepareDoubleString(objectCoords.get(0));
            double relativeY = prepareDoubleString(objectCoords.get(1));
            double relativeZ = 0.0;
            if (objectCoords.size() == 3) relativeZ = prepareDoubleString(objectCoords.get(2));
            if (Double.isNaN(relativeX) || Double.isNaN(relativeY) || Double.isNaN(relativeZ)) {
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
     * Gets rotation matrix about x-axis for ifc object
     *
     * @param bimFileRootId root IFCLOCALPLACEMENT element of BIM file
     * @param object        to get rotation matrix for
     * @return rotation matrix about x-axis for object
     */
    @SuppressWarnings("unchecked")
    private static double[][] getXAxisRotationMatrix(int bimFileRootId, EntityInstance object) {
        // get objects IFCLOCALPLACEMENT entity
        EntityInstance objectIFCLP = object.getAttributeValueBNasEntityInstance("ObjectPlacement");

        // get all RELATIVEPLACEMENTs to root
        ArrayList<EntityInstance> objectRP = getRelativePlacementsToRoot(bimFileRootId, objectIFCLP, new ArrayList<EntityInstance>());

        double rotAngle = 0.0;    // in rad
        double[] parentXVector = null;

        for (EntityInstance relativeObject : objectRP) {
            // get REFDIRECTION (x axis vector)
            Vector<String> xDirectionRatios = null;
            try {
                EntityInstance xRefDirection = relativeObject.getAttributeValueBNasEntityInstance("RefDirection");
                // get DIRECTIONRATIOS from REFDIRECTION (vector values)
                xDirectionRatios = (Vector<String>) xRefDirection.getAttributeValueBN("DirectionRatios");
                if (xDirectionRatios.isEmpty()) return null;
            } catch (NullPointerException e) {
                return null;
            }

            if (xDirectionRatios.size() == 2) {
                double x = prepareDoubleString(xDirectionRatios.get(0));
                double y = prepareDoubleString(xDirectionRatios.get(1));
                if (parentXVector != null) {
                    double[] xVector = {x, y};
                    rotAngle += ParserMath.getAngleBetweenVectors(parentXVector, xVector);
                }
                // update parent vector
                parentXVector = new double[2];
                parentXVector[0] = x;
                parentXVector[1] = y;
            }
            if (xDirectionRatios.size() == 3) {
                double x = prepareDoubleString(xDirectionRatios.get(0));
                double y = prepareDoubleString(xDirectionRatios.get(1));
                double z = prepareDoubleString(xDirectionRatios.get(2));
                if (parentXVector != null) {
                    double[] xVector = {x, y, z};
                    rotAngle += ParserMath.getAngleBetweenVectors(parentXVector, xVector);
                }
                // update parent vector
                parentXVector = new double[3];
                parentXVector[0] = x;
                parentXVector[1] = y;
                parentXVector[2] = z;
            }
        }

        // pack and return rotation matrix about x-axis
        return ParserMath.getRotationMatrixAboutZAxis(rotAngle);
    }

    /**
     * Gets rotation matrix about z-axis for ifc object
     *
     * @param bimFileRootId root IFCLOCALPLACEMENT element of BIM file
     * @param ifcModel      ifc model
     * @param object        to get rotation matrix for
     * @return rotation matrix about z-axis for object
     */
    @SuppressWarnings("unchecked")
    private static double[][] getZAxisRotationMatrix(ModelPopulation ifcModel, int bimFileRootId, EntityInstance object) {
        // get objects IFCLOCALPLACEMENT entity
        EntityInstance objectIFCLP = object.getAttributeValueBNasEntityInstance("ObjectPlacement");

        // get all RELATIVEPLACEMENTs to root
        ArrayList<EntityInstance> objectRP = getRelativePlacementsToRoot(bimFileRootId, objectIFCLP, new ArrayList<EntityInstance>());

        double rotAngle = 0.0;    // in rad
        double[] parentZVector = null;

        for (EntityInstance relativeObject : objectRP) {
            // if IFCAXIS2PLACEMENT2D return
            if (!IFCShapeRepresentationIdentifier.isIfcAxis2Placement3D(ifcModel, relativeObject)) continue;

            Vector<String> zDirectionRatios = null;
            try {
                // get AXIS (z-axis vector)
                EntityInstance zAxis = relativeObject.getAttributeValueBNasEntityInstance("Axis");
                // get DIRECTIONRATIOS from AXIS (vector values)
                zDirectionRatios = (Vector<String>) zAxis.getAttributeValueBN("DirectionRatios");
                if (zDirectionRatios.isEmpty()) return null;
            } catch (NullPointerException e) {
                return null;
            }

            if (zDirectionRatios.size() == 2) {
                double x = prepareDoubleString(zDirectionRatios.get(0));
                double y = prepareDoubleString(zDirectionRatios.get(1));
                if (parentZVector != null) {
                    double[] zVector = {x, y};
                    rotAngle += ParserMath.getAngleBetweenVectors(parentZVector, zVector);
                }
                // update parent vector
                parentZVector = new double[2];
                parentZVector[0] = x;
                parentZVector[1] = y;

            }
            if (zDirectionRatios.size() == 3) {
                double x = prepareDoubleString(zDirectionRatios.get(0));
                double y = prepareDoubleString(zDirectionRatios.get(1));
                double z = prepareDoubleString(zDirectionRatios.get(2));
                if (parentZVector != null) {
                    double[] zVector = {x, y, z};
                    rotAngle += ParserMath.getAngleBetweenVectors(parentZVector, zVector);
                }
                // update parent vector
                parentZVector = new double[3];
                parentZVector[0] = x;
                parentZVector[1] = y;
                parentZVector[2] = z;
            }
        }

        // pack and return rotation matrix about z-axis
        return ParserMath.getRotationMatrixAboutYAxis(rotAngle);
    }

    /**
     * Method recursive walks thru IFC file and collects the RELATIVEPLACEMENT EntityInstances from start entity to root entity
     *
     * @param bimFileRootId            of root IFCLOCALPLACEMENT element of BIMFile
     * @param entity                   you want to collect the RELATIVEPLACEMENT from
     * @param relativePlacementsToRoot empty list at beginning, needed for recursive iteration
     * @return List with EntityInstances of RELATIVEPLACEMENTs
     */
    private static ArrayList<EntityInstance> getRelativePlacementsToRoot(int bimFileRootId, EntityInstance entity, ArrayList<EntityInstance> relativePlacementsToRoot) {
        if (entity.getId() == bimFileRootId) return relativePlacementsToRoot;

        // get objects IFCRELATIVEPLACEMENT entity
        EntityInstance relativePlacement = entity.getAttributeValueBNasEntityInstance("RelativePlacement");
        relativePlacementsToRoot.add(relativePlacement);

        // get id of placement relative to this (PLACEMENTRELTO)
        EntityInstance placementRelTo = entity.getAttributeValueBNasEntityInstance("PlacementRelTo");
        getRelativePlacementsToRoot(bimFileRootId, placementRelTo, relativePlacementsToRoot);

        return relativePlacementsToRoot;
    }

    /**
     * Get EntityInstances of IFCSHAPEREPRESENTATIONs of object
     *
     * @param object to find origin for
     * @return List with EntityInstances of IFCSHAPEREPRESENTATIONs
     */
    private static ArrayList<EntityInstance> getRepresentationsOfObject(EntityInstance object) {
        // get IFCPRODUCTDEFINITIONSHAPE of object
        EntityInstance objectIFCPDS = object.getAttributeValueBNasEntityInstance("Representation");

        // get all IFCSHAPEREPRESENTATIONS of IFCOBJECT
        return objectIFCPDS.getAttributeValueBNasEntityInstanceList("Representations");
    }

    /**
     * Identifies the type of an IFCREPRESENTATION object.
     *
     * @param object object to get the IFCPRODUCTDEFINITIONSHAPE.REPRESENTATIONS from which will be identified
     * @return List of IFCShapeRepresentationIdentity holding an IFC representation object and it's identifier
     */
    public static List<IFCShapeRepresentationIdentity> identifyRepresentationsOfObject(EntityInstance object) {
        ArrayList<IFCShapeRepresentationIdentity> repObjectIdentities = new ArrayList<>();

        // get representation objects
        ArrayList<EntityInstance> objectRepresentations = getRepresentationsOfObject(object);

        // identify each object
        for (EntityInstance repObject : objectRepresentations) {
            //identify IFCSHAPEREPRESENTATION type
            IFCShapeRepresentationIdentity repIdentity = IFCShapeRepresentationIdentifier.identifyShapeRepresentation(repObject);
            repIdentity.setRootObjectEntity(object);
            if (!repIdentity.isFilled()) return null;
            repObjectIdentities.add(repIdentity);
        }

        return repObjectIdentities;
    }

    /**
     * Parses string of double value from IFC file into proper double
     *
     * @param doubleString String of coordinate
     * @return double representing double
     */
    public static double prepareDoubleString(String doubleString) {
        if (doubleString.endsWith(".")) {
            doubleString = doubleString + "0";
        }
        try {
            return Double.parseDouble(doubleString);
        } catch (NumberFormatException e) {
            Logging.error(e.getMessage());
            return Double.NaN;
        }
    }

}
