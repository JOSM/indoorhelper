// License: AGPL. For details, see LICENSE file.
package io.parser.helper;

import io.model.BIMtoOSMCatalog;
import io.parser.data.BIMObject3D;
import io.parser.data.FilteredRawBIMData;
import io.parser.data.ifc.IfcRepresentation;
import io.parser.data.ifc.IfcRepresentationCatalog.IfcSlabTypeEnum;
import io.parser.data.ifc.IfcRepresentationCatalog.RepresentationIdentifier;
import io.parser.data.math.Matrix3D;
import io.parser.data.math.Vector3D;
import io.parser.math.ParserMath;
import nl.tue.buildingsmart.express.population.EntityInstance;
import nl.tue.buildingsmart.express.population.ModelPopulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static io.parser.helper.ParserUtility.stringVectorToVector3D;

/**
 * Class helps parsing BIM data with providing methods to extract OSM relevant data.
 *
 * @author rebsc
 */
public class BIMtoOSMUtility {

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
     * Prepares BIM objects for further operations. Extracts OSM relevant information and puts it into {@link BIMObject3D}
     *
     * @param ifcModel   ifcModel
     * @param objectType relating BIMtoOSMCatalog.BIMObject
     * @param bimObjects All BIM objects of objectType
     * @return Prepared BIM objects
     */
    public static List<BIMObject3D> prepareBIMObjects(ModelPopulation ifcModel, BIMtoOSMCatalog.BIMObject objectType, Vector<EntityInstance> bimObjects) {
        ArrayList<BIMObject3D> preparedObjects = new ArrayList<>();

        for (EntityInstance objectEntity : bimObjects) {
            // resolve placement
            EntityInstance objectIFCLP = objectEntity.getAttributeValueBNasEntityInstance("ObjectPlacement");
            BIMObject3D object = resolveObjectPlacement(objectIFCLP, new BIMObject3D(objectEntity.getId()));
            object.setType(objectType);

            // get IFCLOCALPLACEMENT of object (origin of object)
            Vector3D cartesianOrigin = object.getTranslation();
            // get rotation matrix of object
            Matrix3D rotMatrix = getObjectRotationMatrix(objectEntity);

            // get local points representing shape of object
            ArrayList<Vector3D> shapeDataOfObject = (ArrayList<Vector3D>) getShapeDataOfObject(ifcModel, objectEntity);

            // transform and prepare objects
            if (cartesianOrigin != null && rotMatrix != null && (shapeDataOfObject != null && !shapeDataOfObject.isEmpty())) {
                // transform points
                transformPoints(shapeDataOfObject, rotMatrix, cartesianOrigin);
                object.setCartesianShapeCoordinates(shapeDataOfObject);

                // Check if data includes IFCShapeDataExtractor.defaultPoint. IFCShapeDataExtractor.defaultPoint got added
                // for workaround handling multiple closed loops in data set
                if (!shapeDataOfObject.contains(IfcRepresentationExtractor.defaultPoint)) {
                    preparedObjects.add(object);
                    continue;
                }

                // Workaround: Check data set for closed loops (separated by defaultPoint). If closed loop in data set, extract and add as own way
                List<List<Vector3D>> loops = splitClosedLoopsInDataSet(shapeDataOfObject);
                loops.forEach(l -> {
                    object.setCartesianShapeCoordinates(l);
                    preparedObjects.add(object);
                });
            }
        }

        return preparedObjects;
    }

    /**
     * Method resolves placement of Ifc object and safes the result in {@link BIMObject3D}
     *
     * @param objectPlacementEntity of {@link BIMObject3D}
     * @param object                to resolve placement of
     * @return {@link BIMObject3D} with resolved placement
     */
    private static BIMObject3D resolveObjectPlacement(EntityInstance objectPlacementEntity, BIMObject3D object) {
        if (object == null) return null;
        if (objectPlacementEntity == null) return object;

        // get objects IFCRELATIVEPLACEMENT entity
        EntityInstance relativePlacement = objectPlacementEntity.getAttributeValueBNasEntityInstance("RelativePlacement");

        // get rotation of this entity
        Matrix3D rotation = getRotationFromRelativePlacement(relativePlacement);
        if (rotation == null) return object;

        // get translation of this entity
        Vector3D translation = getTranslationFromRelativePlacement(relativePlacement);
        if (translation == null) return object;

        // check if this entity has placement parent (PLACEMENTRELTO)
        if (objectPlacementEntity.getAttributeValueBNasEntityInstance("PlacementRelTo") != null) {
            EntityInstance placementRelTo = objectPlacementEntity.getAttributeValueBNasEntityInstance("PlacementRelTo");
            resolveObjectPlacement(placementRelTo, object);
            // set new rotation
            object.getRotation().multiply(rotation);
            // set new translation
            Matrix3D inverse = new Matrix3D(rotation);
            inverse.invert();
            inverse.transform(object.getTranslation());
            object.getTranslation().add(translation);
        }
        return object;
    }

    /**
     * Method extracts translation vector from relative placement
     *
     * @param relativePlacement to get translation information of
     * @return translation vector for required object
     */
    private static Vector3D getTranslationFromRelativePlacement(EntityInstance relativePlacement) {
        EntityInstance cPoint = relativePlacement.getAttributeValueBNasEntityInstance("Location");
        @SuppressWarnings("unchecked")
        Vector<String> objectCoords = (Vector<String>) cPoint.getAttributeValueBN("Coordinates");
        return stringVectorToVector3D(objectCoords);
    }

    /**
     * Method extracts rotation matrix from relative placement
     *
     * @param relativePlacement to get rotation matrix of
     * @return rotation matrix
     */
    private static Matrix3D getRotationFromRelativePlacement(EntityInstance relativePlacement) {
        Vector<String> refDirection;
        Vector<String> zAxis;
        try {
            // get RefDirection
            EntityInstance refDirectionEntity = relativePlacement.getAttributeValueBNasEntityInstance("RefDirection");
            refDirection = (Vector<String>) refDirectionEntity.getAttributeValueBN("DirectionRatios");
            // get z-Axis
            EntityInstance axisEntity = relativePlacement.getAttributeValueBNasEntityInstance("Axis");
            zAxis = (Vector<String>) axisEntity.getAttributeValueBN("DirectionRatios");
        } catch (NullPointerException e) {
            return null;
        }

        Vector3D refDirectionVector = stringVectorToVector3D(refDirection);
        Vector3D zAxisVector = stringVectorToVector3D(zAxis);
        if (refDirectionVector == null || zAxisVector == null) return null;

        // get x-Axis
        Vector3D xAxisVector = retrieveXAxis(zAxisVector, refDirectionVector);

        // build rotation matrix
        Vector3D xNorm = new Vector3D();
        xNorm.normalize(xAxisVector);
        Vector3D yNorm = new Vector3D();
        yNorm.cross(zAxisVector, xAxisVector);
        yNorm.normalize();
        Vector3D zNorm = new Vector3D();
        zNorm.normalize(zAxisVector);

        return new Matrix3D(
                xNorm.getX(), xNorm.getY(), xNorm.getZ(),
                yNorm.getX(), yNorm.getY(), yNorm.getZ(),
                zNorm.getX(), zNorm.getY(), zNorm.getZ()
        );
    }

    /**
     * Method gets local shape representation of IFC object
     *
     * @param ifcModel ifcModel
     * @param object   BIM object
     * @return Array including points of shape representation
     */
    public static List<Vector3D> getShapeDataOfObject(ModelPopulation ifcModel, EntityInstance object) {
        ArrayList<Vector3D> shapeData = new ArrayList<>();

        // identify and keep types of IFCPRODUCTDEFINITIONSHAPE.REPRESENTATIONS objects
        List<IfcRepresentation> repObjectIdentities = identifyRepresentationsOfObject(object);
        if (repObjectIdentities == null) return null;

        // first check if IFCPRODUCTDEFINITIONSHAPE.REPRESENTATIONS include IFCSHAPEREPRESENTATION of type "body"
//        IFCShapeRepresentationIdentity bodyRepresentation = getRepresentationSpecificObjectType(repObjectIdentities, RepresentationIdentifier.Body);
//        if (bodyRepresentation != null && !IFCShapeRepresentationIdentifier.isIfcWindowOrIfcDoor(ifcModel, object)) {
//            return IFCShapeDataExtractor.getDataFromBodyRepresentation(ifcModel, bodyRepresentation);
//        }

        // if no IFCSHAPEREPRESENTATION of type "body" check if IFCSHAPEREPRESENTATION of type "box" exists
        IfcRepresentation boxRepresentation = getRepresentationSpecificObjectType(repObjectIdentities, RepresentationIdentifier.Box);
        if (boxRepresentation != null) {
            return IfcRepresentationExtractor.getDataFromBoxRepresentation(ifcModel, boxRepresentation);
        }

        // if no IFCSHAPEREPRESENTATION of type "box" check if IFCSHAPEREPRESENTATION of type "axis" exists
        IfcRepresentation axisRepresentation = getRepresentationSpecificObjectType(repObjectIdentities, RepresentationIdentifier.Axis);
        if (axisRepresentation != null) {
            return IfcRepresentationExtractor.getDataFromAxisRepresentation(ifcModel, axisRepresentation);
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
    public static IfcRepresentation getRepresentationSpecificObjectType(
            List<IfcRepresentation> repObjectIdentities,
            RepresentationIdentifier identifier) {
        for (IfcRepresentation repObject : repObjectIdentities) {
            if (repObject.getIdentifier().equals(identifier)) return repObject;
        }
        return null;
    }

    /**
     * Gets rotation matrix for ifc object
     *
     * @param object to get rotation matrix for
     * @return rotation matrix
     */
    @SuppressWarnings("unchecked")
    private static Matrix3D getObjectRotationMatrix(EntityInstance object) {
        // get objects IFCLOCALPLACEMENT entity
        EntityInstance objectIFCLP = object.getAttributeValueBNasEntityInstance("ObjectPlacement");

        // get all RELATIVEPLACEMENTs to root
        ArrayList<EntityInstance> objectRP = getRelativePlacementsToRoot(objectIFCLP, new ArrayList<>());

        double rotAngleX = 0.0;    // in rad
        double rotAngleZ = 0.0;    // in rad
        Vector3D parentXVector = null;
        Vector3D parentZVector = null;

        for (EntityInstance relativeObject : objectRP) {
            // get REFDIRECTION (x axis vector)
            Vector<String> xDirectionRatios;
            Vector<String> zDirectionRatios;
            try {
                EntityInstance xAxisEntity = relativeObject.getAttributeValueBNasEntityInstance("RefDirection");
                EntityInstance zAxisEntity = relativeObject.getAttributeValueBNasEntityInstance("Axis");
                xDirectionRatios = (Vector<String>) xAxisEntity.getAttributeValueBN("DirectionRatios");
                zDirectionRatios = (Vector<String>) zAxisEntity.getAttributeValueBN("DirectionRatios");
            } catch (NullPointerException e) {
                return null;
            }

            Vector3D xAxis = stringVectorToVector3D(xDirectionRatios);
            if (xAxis == null) return null;
            Vector3D zAxis = stringVectorToVector3D(zDirectionRatios);
            if (zAxis == null) return null;
            xAxis = retrieveXAxis(zAxis, xAxis);

            // get x-axis rotation angle
            if (parentXVector != null) {
                rotAngleX += parentXVector.angleBetween(xAxis);
            } else {
                parentXVector = new Vector3D();
            }
            // update parent vector
            parentXVector.setX(xAxis.getX());
            parentXVector.setY(xAxis.getY());
            parentXVector.setZ(xAxis.getZ());

            // get z-axis rotation angle
            if (parentZVector != null) {
                rotAngleZ += parentZVector.angleBetween(zAxis);
            } else {
                parentZVector = new Vector3D();
            }
            // update parent vector
            parentZVector.setX(zAxis.getX());
            parentZVector.setY(zAxis.getY());
            parentZVector.setZ(zAxis.getZ());
        }

        Matrix3D xMatrix = ParserMath.getRotationMatrixAboutZAxis(rotAngleX);
        Matrix3D zMatrix = ParserMath.getRotationMatrixAboutXAxis(rotAngleZ);
        xMatrix.multiply(zMatrix);

        return xMatrix;
    }

    /**
     * Method recursive walks thru IFC file and collects the RELATIVEPLACEMENT EntityInstances from start entity to root entity
     *
     * @param entity                   you want to collect the RELATIVEPLACEMENT from
     * @param relativePlacementsToRoot empty list at beginning, needed for recursive iteration
     * @return List with EntityInstances of RELATIVEPLACEMENTs
     */
    private static ArrayList<EntityInstance> getRelativePlacementsToRoot(EntityInstance entity, ArrayList<EntityInstance> relativePlacementsToRoot) {
        if (entity == null) return relativePlacementsToRoot;

        // get objects IFCRELATIVEPLACEMENT entity
        EntityInstance relativePlacement = entity.getAttributeValueBNasEntityInstance("RelativePlacement");
        relativePlacementsToRoot.add(relativePlacement);

        // get id of placement relative to this (PLACEMENTRELTO)
        EntityInstance placementRelTo = entity.getAttributeValueBNasEntityInstance("PlacementRelTo");
        getRelativePlacementsToRoot(placementRelTo, relativePlacementsToRoot);

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
    public static List<IfcRepresentation> identifyRepresentationsOfObject(EntityInstance object) {
        ArrayList<IfcRepresentation> repObjectIdentities = new ArrayList<>();

        // get representation objects
        ArrayList<EntityInstance> objectRepresentations = getRepresentationsOfObject(object);

        // identify each object
        for (EntityInstance repObject : objectRepresentations) {
            //identify IFCSHAPEREPRESENTATION type
            IfcRepresentation repIdentity = IfcShapeRepresentationIdentifier.identifyShapeRepresentation(repObject);
            repIdentity.setRootEntity(object);
            if (!repIdentity.isFilled()) return null;
            repObjectIdentities.add(repIdentity);
        }

        return repObjectIdentities;
    }

    /**
     * Transforms the list of points using the given rotation matrix and translation vector
     *
     * @param points      to transform
     * @param rotation    matrix
     * @param translation vector
     */
    private static void transformPoints(ArrayList<Vector3D> points, Matrix3D rotation, Vector3D translation) {
        points.forEach(p -> {
            rotation.transform(p);
            p.add(translation);
        });
    }

    /**
     * Gets the actual x-axis vector from reference system
     *
     * @param zAxis        of IfcAxis2Placement3D
     * @param refDirection of IfcAxis2Placement3D
     * @return actual x-axis vector
     */
    private static Vector3D retrieveXAxis(Vector3D zAxis, Vector3D refDirection) {
        double d = refDirection.dot(zAxis) / zAxis.lengthSquared();
        Vector3D xAxis = new Vector3D(refDirection);
        Vector3D refZ = new Vector3D(zAxis);
        refZ.scale(d);
        xAxis.sub(refZ);
        return xAxis;
    }

    /**
     * Method finds and slips loops in data set
     *
     * @param data to check for loops
     * @return list with data for each loop
     */
    private static List<List<Vector3D>> splitClosedLoopsInDataSet(List<Vector3D> data) {
        List<List<Vector3D>> loops = new ArrayList<>();
        ArrayList<Vector3D> loop = new ArrayList<>();
        for (Vector3D point : data) {
            if (point.equalsVector(IfcRepresentationExtractor.defaultPoint) && !loop.isEmpty()) {
                loops.add(loop);
                loop = new ArrayList<>();
            } else if (!point.equalsVector(IfcRepresentationExtractor.defaultPoint)) {
                loop.add(point);
            }
            if (data.indexOf(point) == data.size() - 1 && !loop.isEmpty()) {
                loops.add(loop);
            }
        }
        return loops;
    }

}