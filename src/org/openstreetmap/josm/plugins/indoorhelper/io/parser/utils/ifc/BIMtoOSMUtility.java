// License: AGPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.indoorhelper.io.parser.utils.ifc;

import org.openstreetmap.josm.plugins.indoorhelper.io.parser.data.ifc.IfcRepresentation;
import org.openstreetmap.josm.plugins.indoorhelper.io.parser.data.ifc.IfcRepresentationCatalog.IfcSlabTypeEnum;
import org.openstreetmap.josm.plugins.indoorhelper.io.parser.data.ifc.IfcRepresentationCatalog.RepresentationIdentifier;
import org.openstreetmap.josm.plugins.indoorhelper.io.model.BIMtoOSMCatalog;
import org.openstreetmap.josm.plugins.indoorhelper.io.parser.data.BIMDataCollection;
import org.openstreetmap.josm.plugins.indoorhelper.io.parser.data.BIMObject3D;
import org.openstreetmap.josm.plugins.indoorhelper.io.parser.math.*;
import nl.tue.buildingsmart.express.population.EntityInstance;
import nl.tue.buildingsmart.express.population.ModelPopulation;

import java.util.ArrayList;
import java.util.List;

import static org.openstreetmap.josm.plugins.indoorhelper.io.parser.utils.ParserUtility.stringVectorToVector3D;

/**
 * Class providing useful methods to parse BIM data to OSM data
 */
public class BIMtoOSMUtility {

    /**
     * Types of geometry precision
     */
    public enum GeometrySolution {
        BODY,
        BOUNDING_BOX
    }

    /**
     * Filters important OSM data into internal data structure
     *
     * @param ifcModel ifcModel
     * @return FilteredBIMData including BIM objects of ways, rooms, etc.
     */
    public static BIMDataCollection extractMajorBIMData(ModelPopulation ifcModel) {
        BIMDataCollection bimData = new BIMDataCollection();

        // get the root element IfcSite
        List<EntityInstance> ifcSiteObjects = new ArrayList<>();
        BIMtoOSMCatalog.getIFCSITETags().forEach(tag -> ifcSiteObjects.addAll(ifcModel.getInstancesOfType(tag)));

        if (!ifcSiteObjects.isEmpty()) {
            bimData.setIfcSite(ifcSiteObjects.get(0));
        }

        // get all relevant areas
        List<EntityInstance> areaObjects = new ArrayList<>();
        BIMtoOSMCatalog.getAreaTags().forEach(tag -> ifcModel.getInstancesOfType(tag).forEach(entity -> {
            String identifier = (String) entity.getAttributeValueBN("PredefinedType");
            if (!identifier.equals("." + IfcSlabTypeEnum.ROOF + ".")) {
                areaObjects.add(entity);
            }
        }));
        bimData.setAreaObjects(areaObjects);

        // get all walls
        List<EntityInstance> wallObjects = new ArrayList<>();
        BIMtoOSMCatalog.getWallTags().forEach(tag -> wallObjects.addAll(ifcModel.getInstancesOfType(tag)));
        bimData.setWallObjects(wallObjects);

        // get all columns
        List<EntityInstance> colObjects = new ArrayList<>();
        BIMtoOSMCatalog.getColumnTags().forEach(tag -> colObjects.addAll(ifcModel.getInstancesOfType(tag)));
        bimData.setColumnObjects(colObjects);

        // get all doors
        List<EntityInstance> doorObjects = new ArrayList<>();
        BIMtoOSMCatalog.getDoorTags().forEach(tag -> doorObjects.addAll(ifcModel.getInstancesOfType(tag)));
        bimData.setDoorObjects(doorObjects);

        // get all doors
        List<EntityInstance> stairObjects = new ArrayList<>();
        BIMtoOSMCatalog.getStairTags().forEach(tag -> stairObjects.addAll(ifcModel.getInstancesOfType(tag)));
        bimData.setStairObjects(stairObjects);

        // get all windows
        List<EntityInstance> windowObjects = new ArrayList<>();
        BIMtoOSMCatalog.getWindowTags().forEach(tag -> windowObjects.addAll(ifcModel.getInstancesOfType(tag)));
        bimData.setWindowObjects(windowObjects);

        return bimData;
    }

    /**
     * Transforms BIM objects for further operations. Extracts OSM relevant information and puts it into {@link BIMObject3D}
     *
     * @param ifcModel   ifcModel
     * @param solution   geometry solution type
     * @param objectType relating BIMtoOSMCatalog.BIMObject
     * @param bimObjects All BIM objects of objectType
     * @return Transformed BIM objects
     */
    public static List<BIMObject3D> transformBIMObjects(ModelPopulation ifcModel, GeometrySolution solution,
                                                        BIMtoOSMCatalog.BIMObject objectType, List<EntityInstance> bimObjects) {
        ArrayList<BIMObject3D> transformedObjects = new ArrayList<>();

        for (EntityInstance objectEntity : bimObjects) {

            BIMObject3D object = transformBIMObject(ifcModel, solution, objectType, objectEntity);
            if (object == null) {
                continue;
            }

            // check for loops in data set
            List<Vector3D> objectGeometry = object.getCartesianGeometryCoordinates();
            if (!objectGeometry.contains(IfcGeometryExtractor.defaultPoint)) {
                List<List<Vector3D>> loops = splitClosedLoops(objectGeometry);
                loops.forEach(l -> {
                    object.setCartesianGeometryCoordinates(l);
                    transformedObjects.add(object);
                });
            } else {
                transformedObjects.add(object);
            }
        }

        return transformedObjects;
    }

    /**
     * Transform BIM object for further operations. Extracts OSM relevant information and puts it into {@link BIMObject3D}
     *
     * @param ifcModel     ifcModel
     * @param solution     geometry solution type
     * @param objectType   relating BIMtoOSMCatalog.BIMObject
     * @param objectEntity BIM object of objectType
     * @return Transformed BIM object
     */
    public static BIMObject3D transformBIMObject(ModelPopulation ifcModel, GeometrySolution solution,
                                                 BIMtoOSMCatalog.BIMObject objectType, EntityInstance objectEntity) {

        EntityInstance objectIFCLP = objectEntity.getAttributeValueBNasEntityInstance("ObjectPlacement");
        BIMObject3D object = resolveObjectPlacement(objectIFCLP, new BIMObject3D(objectEntity.getId()));
        object.setType(objectType);
        Vector3D cartesianOrigin = object.getTranslation();
        Matrix3D rotMatrix = getObjectRotationMatrix(objectEntity);

        // get object geometry
        ArrayList<Vector3D> shapeDataOfObject = (ArrayList<Vector3D>) getShapeData(ifcModel, objectEntity, solution);

        // transform and prepare
        if (cartesianOrigin != null && rotMatrix != null && (shapeDataOfObject != null && !shapeDataOfObject.isEmpty())) {
            transformPoints(shapeDataOfObject, rotMatrix, cartesianOrigin);
            object.setCartesianGeometryCoordinates(shapeDataOfObject);
        } else {
            return null;
        }

        return object;
    }

    /**
     * Method resolves placement of Ifc object and keeps the result in {@link BIMObject3D}
     *
     * @param objectPlacementEntity of {@link BIMObject3D}
     * @param object                to resolve placement of
     * @return {@link BIMObject3D} with resolved placement
     */
    private static BIMObject3D resolveObjectPlacement(EntityInstance objectPlacementEntity, BIMObject3D object) {
        if (object == null) return null;
        if (objectPlacementEntity == null) return object;

        // get objects IfcRelativePlacement entity
        EntityInstance relativePlacement = objectPlacementEntity.getAttributeValueBNasEntityInstance("RelativePlacement");

        // get rotation of this entity
        Matrix3D rotation = getRotationFromRelativePlacement(relativePlacement);
        if (rotation == null) return object;

        // get translation of this entity
        Vector3D translation = getTranslationFromRelativePlacement(relativePlacement);
        if (translation == null) return object;

        // check if this entity has placement parent (PlacementRelTo)
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
        List<String> objectCoords = (List<String>) cPoint.getAttributeValueBN("Coordinates");
        return stringVectorToVector3D(objectCoords);
    }

    /**
     * Method extracts rotation matrix from relative placement
     *
     * @param relativePlacement to get rotation matrix of
     * @return rotation matrix
     */
    private static Matrix3D getRotationFromRelativePlacement(EntityInstance relativePlacement) {
        List<String> refDirection;
        List<String> zAxis;
        try {
            // get RefDirection
            EntityInstance refDirectionEntity = relativePlacement.getAttributeValueBNasEntityInstance("RefDirection");
            refDirection = (List<String>) refDirectionEntity.getAttributeValueBN("DirectionRatios");
            // get z-Axis
            EntityInstance axisEntity = relativePlacement.getAttributeValueBNasEntityInstance("Axis");
            zAxis = (List<String>) axisEntity.getAttributeValueBN("DirectionRatios");
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
     * Method gets local shape representation of ifc object
     *
     * @param ifcModel ifcModel
     * @param object   BIM object
     * @return Array including points of shape representation
     */
    public static List<Vector3D> getShapeData(ModelPopulation ifcModel, EntityInstance object, GeometrySolution solution) {

        List<IfcRepresentation> repObjectIdentities = getIfcRepresentations(object);
        if (repObjectIdentities == null) return null;

        if (solution.equals(GeometrySolution.BODY)) {
            IfcRepresentation bodyRepresentation = getIfcRepresentation(repObjectIdentities, RepresentationIdentifier.Body);
            if (bodyRepresentation != null) {
                return IfcGeometryExtractor.getDataFromBodyRepresentation(ifcModel, bodyRepresentation);
            }
        } else if (solution.equals(GeometrySolution.BOUNDING_BOX)) {
            IfcRepresentation boxRepresentation = getIfcRepresentation(repObjectIdentities, RepresentationIdentifier.Box);
            if (boxRepresentation != null) {
                return IfcGeometryExtractor.getDataFromBoxRepresentation(ifcModel, boxRepresentation);
            }
        }
        return null;
    }

    /**
     * Checks the IfcShapeRepresentation objects for object with
     * IfcShapeRepresentation.RepresentationIdentifier = "identifier" and returns it
     *
     * @param repObjectIdentities IfcShapeRepresentation objects
     * @param identifier          RepresentationIdentifier
     * @return returns IfcShapeRepresentation "identifier" or null if not in list
     */
    public static IfcRepresentation getIfcRepresentation(List<IfcRepresentation> repObjectIdentities, RepresentationIdentifier identifier) {
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
        // get objects IfcLocalPlacement entity
        EntityInstance objectIFCLP = object.getAttributeValueBNasEntityInstance("ObjectPlacement");

        // get all RelativePlacements to root
        ArrayList<EntityInstance> objectRP = getRelativePlacementsToRoot(objectIFCLP, new ArrayList<>());

        double rotAngleX = 0.0;    // in rad
        double rotAngleZ = 0.0;    // in rad
        Vector3D parentXVector = null;
        Vector3D parentZVector = null;

        // TODO use one rotation matrix instead of each for each axis
        for (EntityInstance relativeObject : objectRP) {
            // get RefDirection (x axis vector)
            List<String> xDirectionRatios;
            List<String> zDirectionRatios;
            try {
                EntityInstance xAxisEntity = relativeObject.getAttributeValueBNasEntityInstance("RefDirection");
                EntityInstance zAxisEntity = relativeObject.getAttributeValueBNasEntityInstance("Axis");
                xDirectionRatios = (List<String>) xAxisEntity.getAttributeValueBN("DirectionRatios");
                zDirectionRatios = (List<String>) zAxisEntity.getAttributeValueBN("DirectionRatios");
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

        Matrix3D xMatrix = ParserMath.getRotationMatrixZ(rotAngleX);
        Matrix3D zMatrix = ParserMath.getRotationMatrixX(rotAngleZ);
        xMatrix.multiply(zMatrix);

        return xMatrix;
    }

    /**
     * Method runs recursively through ifc file and collects RelativePlacement EntityInstances from
     * start to root entity
     *
     * @param entity                   you want to collect the RelativePlacement for
     * @param relativePlacementsToRoot empty list at beginning, needed for recursive iteration
     * @return List with EntityInstances of RelativePlacement
     */
    private static ArrayList<EntityInstance> getRelativePlacementsToRoot(EntityInstance entity, ArrayList<EntityInstance> relativePlacementsToRoot) {
        if (entity == null) return relativePlacementsToRoot;

        // get objects IfcRelativePlacement entity
        EntityInstance relativePlacement = entity.getAttributeValueBNasEntityInstance("RelativePlacement");
        relativePlacementsToRoot.add(relativePlacement);

        // get id of placement relative to this (PlacementRelTo)
        EntityInstance placementRelTo = entity.getAttributeValueBNasEntityInstance("PlacementRelTo");
        getRelativePlacementsToRoot(placementRelTo, relativePlacementsToRoot);

        return relativePlacementsToRoot;
    }

    /**
     * Identifies the type of an IfcRepresentation object.
     *
     * @param object object to get the IfcProductDefinitionShape.Representations from which will be identified
     * @return List of IFCShapeRepresentationIdentity holding an IFC representation object and it's identifier
     */
    public static List<IfcRepresentation> getIfcRepresentations(EntityInstance object) {
        ArrayList<IfcRepresentation> repObjectIdentities = new ArrayList<>();

        // get IfcProductDefinitionShape of object
        EntityInstance objectIFCPDS = object.getAttributeValueBNasEntityInstance("Representation");
        // get all IfcShapeRepresentation of object
        ArrayList<EntityInstance> objectRepresentations =
                objectIFCPDS.getAttributeValueBNasEntityInstanceList("Representations");

        // identify each object
        for (EntityInstance repObject : objectRepresentations) {
            //identify IfcShapeRepresentation type
            IfcRepresentation repIdentity = IfcObjectIdentifier.identifyShapeRepresentation(repObject);
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
    private static List<List<Vector3D>> splitClosedLoops(List<Vector3D> data) {
        List<List<Vector3D>> loops = new ArrayList<>();
        ArrayList<Vector3D> loop = new ArrayList<>();
        for (Vector3D point : data) {
            if (point.equalsVector(IfcGeometryExtractor.defaultPoint) && !loop.isEmpty()) {
                loops.add(loop);
                loop = new ArrayList<>();
            } else if (!point.equalsVector(IfcGeometryExtractor.defaultPoint)) {
                loop.add(point);
            }
            if (data.indexOf(point) == data.size() - 1 && !loop.isEmpty()) {
                loops.add(loop);
            }
        }
        return loops;
    }

}
