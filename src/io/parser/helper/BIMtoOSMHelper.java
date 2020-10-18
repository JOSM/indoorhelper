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

import static io.parser.ParserUtility.stringVectorToVector3D;

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
        // TODO - WIP: Improve object placement determination
        ArrayList<BIMObject3D> preparedObjects = new ArrayList<>();

        for (EntityInstance objectEntity : bimObjects) {
            // resolve placement
            EntityInstance objectIFCLP = objectEntity.getAttributeValueBNasEntityInstance("ObjectPlacement");
            BIMObject3D object = resolveObjectPlacement(objectIFCLP, new BIMObject3D(objectEntity.getId()));
            object.setType(objectType);

            // get IFCLOCALPLACEMENT of object (origin of object)
            Vector3D cartesianOrigin = object.getTranslation();
            // get rotation matrix of object
            Matrix3D rotMatrix = getObjectRotationMatrix(bimFileRootId, objectEntity);

            // get local points representing shape of object
            ArrayList<Vector3D> shapeDataOfObject = (ArrayList<Vector3D>) getShapeDataOfObject(ifcModel, objectEntity);

            // create PreparedBIMObject3D and save
            if (cartesianOrigin != null && rotMatrix != null && (shapeDataOfObject != null && !shapeDataOfObject.isEmpty())) {
                // transform points
                for (Vector3D point : shapeDataOfObject) {
                    if (point.equalsVector(IfcRepresentationExtractor.defaultPoint)) continue;    // for workaround
                    rotMatrix.transform(point);
                    point.add(object.getTranslation());
                }
                object.setCartesianShapeCoordinates(shapeDataOfObject);

                // Check if data includes IFCShapeDataExtractor.defaultPoint. IFCShapeDataExtractor.defaultPoint got added
                // for workaround handling multiple closed loops in data set
                if (!shapeDataOfObject.contains(IfcRepresentationExtractor.defaultPoint)) {
                    preparedObjects.add(object);
                    continue;
                }

                // Workaround: Check data set for closed loops (separated by defaultPoint). If closed loop in data set, extract and add as own way
                ArrayList<Vector3D> loop = new ArrayList<>();
                for (Vector3D point : shapeDataOfObject) {
                    if (point.equalsVector(IfcRepresentationExtractor.defaultPoint) && !loop.isEmpty()) {
                        object.setCartesianShapeCoordinates(loop);
                        preparedObjects.add(object);
                        loop = new ArrayList<>();
                    } else if (!point.equalsVector(IfcRepresentationExtractor.defaultPoint)) {
                        loop.add(point);
                    }
                    if (shapeDataOfObject.indexOf(point) == shapeDataOfObject.size() - 1 && !loop.isEmpty()) {
                        object.setCartesianShapeCoordinates(loop);
                        preparedObjects.add(object);
                    }
                }
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
     * @param bimFileRootId root IFCLOCALPLACEMENT element of BIM file
     * @param object        to get rotation matrix for
     * @return rotation matrix
     */
    @SuppressWarnings("unchecked")
    private static Matrix3D getObjectRotationMatrix(int bimFileRootId, EntityInstance object) {
        // get objects IFCLOCALPLACEMENT entity
        EntityInstance objectIFCLP = object.getAttributeValueBNasEntityInstance("ObjectPlacement");

        // get all RELATIVEPLACEMENTs to root
        ArrayList<EntityInstance> objectRP = getRelativePlacementsToRoot(bimFileRootId, objectIFCLP, new ArrayList<EntityInstance>());

        double rotAngleX = 0.0;    // in rad
        double rotAngleZ = 0.0;    // in rad
        double[] parentXVector = null;
        double[] parentZVector = null;

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

            // x axis
            Vector3D xAxis = stringVectorToVector3D(xDirectionRatios);
            Vector3D zAxis = stringVectorToVector3D(zDirectionRatios);

            xAxis = retrieveXAxis(zAxis, xAxis);

            if (parentXVector != null) {
                double[] xVector = {xAxis.getX(), xAxis.getY(), xAxis.getZ()};
                rotAngleX += ParserMath.getAngleBetweenVectors(parentXVector, xVector);
            }
            // update parent vector
            parentXVector = new double[3];
            parentXVector[0] = xAxis.getX();
            parentXVector[1] = xAxis.getY();
            parentXVector[2] = xAxis.getZ();

            // z axis
            if (parentZVector != null) {
                double[] zVector = {zAxis.getX(), zAxis.getY(), zAxis.getZ()};
                rotAngleZ += ParserMath.getAngleBetweenVectors(parentZVector, zVector);
            }
            // update parent vector
            parentZVector = new double[3];
            parentZVector[0] = zAxis.getX();
            parentZVector[1] = zAxis.getY();
            parentZVector[2] = zAxis.getZ();
        }


        double[][] mX = ParserMath.getRotationMatrixAboutZAxis(rotAngleX);
        double[][] mZ = ParserMath.getRotationMatrixAboutXAxis(rotAngleZ);

        Matrix3D xMatrix = new Matrix3D(mX[0][0], mX[0][1], mX[0][2],
                mX[1][0], mX[1][1], mX[1][2],
                mX[2][0], mX[2][1], mX[2][2]);
        Matrix3D zMatrix = new Matrix3D(mZ[0][0], mZ[0][1], mZ[0][2],
                mZ[1][0], mZ[1][1], mZ[1][2],
                mZ[2][0], mZ[2][1], mZ[2][2]);
        xMatrix.multiply(zMatrix);

        return xMatrix;
    }

    /**
     * Gets the actual x-axis vector from reference system
     * @param zAxis of IfcAxis2Placement3D
     * @param refDirection of IfcAxis2Placement3D
     * @return actual x-axis vector
     */
    private static Vector3D retrieveXAxis(Vector3D zAxis, Vector3D refDirection){
        double d = refDirection.dot(zAxis) / zAxis.lengthSquared();
        Vector3D xAxis = new Vector3D(refDirection);
        Vector3D refZ = new Vector3D(zAxis);
        refZ.scale(d);
        xAxis.sub(refZ);
        return xAxis;
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
    public static List<IfcRepresentation> identifyRepresentationsOfObject(EntityInstance object) {
        ArrayList<IfcRepresentation> repObjectIdentities = new ArrayList<>();

        // get representation objects
        ArrayList<EntityInstance> objectRepresentations = getRepresentationsOfObject(object);

        // identify each object
        for (EntityInstance repObject : objectRepresentations) {
            //identify IFCSHAPEREPRESENTATION type
            IfcRepresentation repIdentity = IFCShapeRepresentationIdentifier.identifyShapeRepresentation(repObject);
            repIdentity.setRootEntity(object);
            if (!repIdentity.isFilled()) return null;
            repObjectIdentities.add(repIdentity);
        }

        return repObjectIdentities;
    }


}
