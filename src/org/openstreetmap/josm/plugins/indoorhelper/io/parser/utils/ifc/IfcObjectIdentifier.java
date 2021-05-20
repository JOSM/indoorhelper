// License: AGPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.indoorhelper.io.parser.utils.ifc;

import org.openstreetmap.josm.plugins.indoorhelper.io.parser.data.ifc.IfcRepresentation;
import org.openstreetmap.josm.plugins.indoorhelper.io.parser.data.ifc.IfcRepresentationCatalog.*;
import org.openstreetmap.josm.plugins.indoorhelper.io.model.BIMtoOSMCatalog.BIMObject;
import nl.tue.buildingsmart.express.population.EntityInstance;
import nl.tue.buildingsmart.express.population.ModelPopulation;
import org.openstreetmap.josm.tools.Logging;

import java.util.ArrayList;

/**
 * Class to identify the type on an IfcShapeRepresentation object
 */
public class IfcObjectIdentifier {

    /**
     * Method gets IfcShapeRepresentation.RepresentationIdentifier and IfcShapeRepresentation.RepresentationType of object
     *
     * @param shapeRepresentation IFCShapeRepresentationIdentity
     * @return Returns object containing IfcShapeRepresentation.RepresentationIdentifier and IfcShapeRepresentation.RepresentationType
     */
    public static IfcRepresentation identifyShapeRepresentation(EntityInstance shapeRepresentation) {
        IfcRepresentation rep = new IfcRepresentation();
        String identifier = prepareRepresentationAttribute(shapeRepresentation.getAttributeValueBN("RepresentationIdentifier").toString());
        String type = prepareRepresentationAttribute(shapeRepresentation.getAttributeValueBN("RepresentationType").toString());

        // set representationObjectId
        rep.setEntity(shapeRepresentation);

        // get identifier
        for (RepresentationIdentifier i : RepresentationIdentifier.values()) {
            if (identifier.equals(i.name())) {
                rep.setIdentifier(i);
                break;
            }

        }
        // get type
        for (RepresentationType t : RepresentationType.values()) {
            if (type.equals(t.name())) {
                rep.setType(t);
                break;
            }
        }
        return rep;
    }

    /**
     * Gets the type of specific IfcShapeRepresentation.ITEM. If item type is not supported for IfcShapeRepresentation.RepresentationType
     * method will return null.
     *
     * @param ifcModel ifcModel
     * @param ident    IfcRepresentationTypeObject object
     * @param item     to get the representation type for (IfcShapeRepresentation.ITEM packed into EntityInstance object)
     * @return String with IfcShapeRepresentation.ITEM type definition or null if not allowed in standard
     */
    public static String getRepresentationItemType(ModelPopulation ifcModel, IfcRepresentation ident, EntityInstance item) {

        if (ident.getType().equals(RepresentationType.AdvancedBrep)) {
            ArrayList<EntityInstance> ifcAdvancedBrep = new ArrayList<>(ifcModel.getInstancesOfType(AdvancedBrepRepresentationTypeItems.IfcAdvancedBrep.name()));
            if (ifcAdvancedBrep.contains(item)) return AdvancedBrepRepresentationTypeItems.IfcAdvancedBrep.name();

            ArrayList<EntityInstance> ifcFacetedBrep = new ArrayList<>(ifcModel.getInstancesOfType(AdvancedBrepRepresentationTypeItems.IfcFacetedBrep.name()));
            if (ifcFacetedBrep.contains(item)) return AdvancedBrepRepresentationTypeItems.IfcFacetedBrep.name();

            Logging.info(IfcObjectIdentifier.class.getName() + ": " + item.getEntityDefinition() + " is not supported");
            return null;
        }

        if (ident.getType().equals(RepresentationType.AdvancedSweptSolid)) {
            ArrayList<EntityInstance> ifcSweptDiskSolids = new ArrayList<>(ifcModel.getInstancesOfType(AdvancedSweptSolidRepresentationTypeItems.IfcSweptDiskSolid.name()));
            if (ifcSweptDiskSolids.contains(item))
                return AdvancedSweptSolidRepresentationTypeItems.IfcSweptDiskSolid.name();

            ArrayList<EntityInstance> ifcSweptDiskSolidPolygonals = new ArrayList<>(ifcModel.getInstancesOfType(AdvancedSweptSolidRepresentationTypeItems.IfcSweptDiskSolidPolygonal.name()));
            if (ifcSweptDiskSolidPolygonals.contains(item))
                return AdvancedSweptSolidRepresentationTypeItems.IfcSweptDiskSolidPolygonal.name();

            Logging.info(IfcObjectIdentifier.class.getName() + ": " + item.getEntityDefinition() + " is not supported");
            return null;
        }

        if (ident.getType().equals(RepresentationType.Brep)) {
            if (ifcModel.getInstancesOfType(BrepRepresentationTypeItems.IfcFacetedBrep.name()).contains(item)) {
                return BrepRepresentationTypeItems.IfcFacetedBrep.name();
            }
            Logging.info(IfcObjectIdentifier.class.getName() + ": " + item.getEntityDefinition() + " is not supported");
            return null;
        }

        if (ident.getType().equals(RepresentationType.CSG)) {
            ArrayList<EntityInstance> ifcBooleanResults = new ArrayList<>(ifcModel.getInstancesOfType(CSGRepresentationTypeItems.IfcBooleanResult.name()));
            if (ifcBooleanResults.contains(item)) return CSGRepresentationTypeItems.IfcBooleanResult.name();

            ArrayList<EntityInstance> ifcCSGSolids = new ArrayList<>(ifcModel.getInstancesOfType(CSGRepresentationTypeItems.IfcCsgSolid.name()));
            if (ifcCSGSolids.contains(item)) return CSGRepresentationTypeItems.IfcCsgSolid.name();

            ArrayList<EntityInstance> ifcPrimitive3Ds = new ArrayList<>(ifcModel.getInstancesOfType(CSGRepresentationTypeItems.IfcPrimitive3D.name()));
            if (ifcPrimitive3Ds.contains(item)) return CSGRepresentationTypeItems.IfcPrimitive3D.name();

            Logging.info(IfcObjectIdentifier.class.getName() + ": " + item.getEntityDefinition() + " is not supported");
            return null;
        }

        if (ident.getType().equals(RepresentationType.Tessellation)) {
            if (ifcModel.getInstancesOfType(TessellationRepresentationTypeItems.IfcTessellatedFaceSet.name()).contains(item)) {
                return TessellationRepresentationTypeItems.IfcTessellatedFaceSet.name();
            }
            Logging.info(IfcObjectIdentifier.class.getName() + ": " + item.getEntityDefinition() + " is not supported");
            return null;
        }

        if (ident.getType().equals(RepresentationType.Clipping)) {
            if (ifcModel.getInstancesOfType(ClippingRepresentationTypeItems.IfcBooleanClippingResult.name()).contains(item)) {
                return ClippingRepresentationTypeItems.IfcBooleanClippingResult.name();
            }
            Logging.info(IfcObjectIdentifier.class.getName() + ": " + item.getEntityDefinition() + " is not supported");
            return null;
        }

        if (ident.getType().equals(RepresentationType.Curve2D) || ident.getType().equals(RepresentationType.Curve3D)) {
            ArrayList<EntityInstance> ifcBoundedCurves = new ArrayList<>(ifcModel.getInstancesOfType(CurveRepresentationTypeItems.IfcBoundedCurve.name()));
            if (ifcBoundedCurves.contains(item)) return CurveRepresentationTypeItems.IfcBoundedCurve.name();

            ArrayList<EntityInstance> ifcPolylines = new ArrayList<>(ifcModel.getInstancesOfType(CurveRepresentationTypeItems.IfcPolyline.name()));
            if (ifcPolylines.contains(item)) return CurveRepresentationTypeItems.IfcPolyline.name();

            Logging.info(IfcObjectIdentifier.class.getName() + ": " + item.getEntityDefinition() + " is not supported");
            return null;
        }

        if (ident.getType().equals(RepresentationType.SurfaceModel)) {
            ArrayList<EntityInstance> ifcTessellatedItems = new ArrayList<>(ifcModel.getInstancesOfType(SurfaceModelRepresentationTypeItems.IfcTessellatedItem.name()));
            if (ifcTessellatedItems.contains(item))
                return SurfaceModelRepresentationTypeItems.IfcTessellatedItem.name();

            ArrayList<EntityInstance> ifcShellBasedSurfaceModels = new ArrayList<>(ifcModel.getInstancesOfType(SurfaceModelRepresentationTypeItems.IfcShellBasedSurfaceModel.name()));
            if (ifcShellBasedSurfaceModels.contains(item))
                return SurfaceModelRepresentationTypeItems.IfcShellBasedSurfaceModel.name();

            ArrayList<EntityInstance> ifcFaceBasedSurfaceModels = new ArrayList<>(ifcModel.getInstancesOfType(SurfaceModelRepresentationTypeItems.IfcFaceBasedSurfaceModel.name()));
            if (ifcFaceBasedSurfaceModels.contains(item))
                return SurfaceModelRepresentationTypeItems.IfcFaceBasedSurfaceModel.name();

            ArrayList<EntityInstance> ifcFacetedBreps = new ArrayList<>(ifcModel.getInstancesOfType(SurfaceModelRepresentationTypeItems.IfcFacetedBrep.name()));
            if (ifcFacetedBreps.contains(item)) return SurfaceModelRepresentationTypeItems.IfcFacetedBrep.name();

            Logging.info(IfcObjectIdentifier.class.getName() + ": " + item.getEntityDefinition() + " is not supported");
            return null;
        }

        if (ident.getType().equals(RepresentationType.SweptSolid)) {
            ArrayList<EntityInstance> ifcExtrudedAreaSolid = new ArrayList<>(ifcModel.getInstancesOfType(SweptSolidRepresentationTypeItems.IfcExtrudedAreaSolid.name()));
            if (ifcExtrudedAreaSolid.contains(item))
                return SweptSolidRepresentationTypeItems.IfcExtrudedAreaSolid.name();

            ArrayList<EntityInstance> ifcRevolvedAreaSolid = new ArrayList<>(ifcModel.getInstancesOfType(SweptSolidRepresentationTypeItems.IfcRevolvedAreaSolid.name()));
            if (ifcRevolvedAreaSolid.contains(item))
                return SweptSolidRepresentationTypeItems.IfcRevolvedAreaSolid.name();

            Logging.info(IfcObjectIdentifier.class.getName() + ": " + item.getEntityDefinition() + " is not supported");
            return null;
        }

        if (ident.getType().equals(RepresentationType.BoundingBox)) {
            if (ifcModel.getInstancesOfType(BoundingBoxRepresentationTypeItems.IfcBoundingBox.name()).contains(item)) {
                return BoundingBoxRepresentationTypeItems.IfcBoundingBox.name();
            }
            Logging.info(IfcObjectIdentifier.class.getName() + ": " + item.getEntityDefinition() + " is not supported");
            return null;
        }

        if (ident.getType().equals(RepresentationType.MappedRepresentation)) {
            if (ifcModel.getInstancesOfType(MappedRepresentationTypeItems.IfcMappedItem.name()).contains(item)) {
                return MappedRepresentationTypeItems.IfcMappedItem.name();
            }
            Logging.info(IfcObjectIdentifier.class.getName() + ": " + item.getEntityDefinition() + " is not supported");
            return null;
        }

        Logging.info(IfcObjectIdentifier.class.getName() + ": " + ident.getType() + " RepresentationType is not supported");
        return null;
    }


    /**
     * Gets the type of IfcLoop. Types can be IfcEdgeLoop, IfcPolyLoop, IfcPolyLoop
     *
     * @param ifcModel ifcModel
     * @param loop     to get type of
     * @return type as string
     */
    public static String getIFCLoopType(ModelPopulation ifcModel, EntityInstance loop) {
        ArrayList<EntityInstance> edgeLoops = new ArrayList<>(ifcModel.getInstancesOfType(LoopSubRepresentationTypeItems.IfcEdgeLoop.name()));
        if (edgeLoops.contains(loop)) return LoopSubRepresentationTypeItems.IfcEdgeLoop.name();

        ArrayList<EntityInstance> polyLoops = new ArrayList<>(ifcModel.getInstancesOfType(LoopSubRepresentationTypeItems.IfcPolyLoop.name()));
        if (polyLoops.contains(loop)) return LoopSubRepresentationTypeItems.IfcPolyLoop.name();

        ArrayList<EntityInstance> vertexLoops = new ArrayList<>(ifcModel.getInstancesOfType(LoopSubRepresentationTypeItems.IfcVertexLoop.name()));
        if (vertexLoops.contains(loop)) return LoopSubRepresentationTypeItems.IfcVertexLoop.name();

        Logging.info(IfcObjectIdentifier.class.getName() + ": " + loop.getEntityDefinition() + " LoopRepresentationType is not supported");
        return null;
    }

    /**
     * Gets the type of IFCPROFILEDEF.
     *
     * @param ifcModel   ifcModel
     * @param profileDef to get type of
     * @return type as string
     */
    public static String getIFCProfileDefType(ModelPopulation ifcModel, EntityInstance profileDef) {
        ArrayList<EntityInstance> rectanglePD = new ArrayList<>(ifcModel.getInstancesOfType(ProfileDefRepresentationTypeItems.IfcRectangleProfileDef.name()));

        if (rectanglePD.contains(profileDef)) return ProfileDefRepresentationTypeItems.IfcRectangleProfileDef.name();

        ArrayList<EntityInstance> trapeziumPD = new ArrayList<>(ifcModel.getInstancesOfType(ProfileDefRepresentationTypeItems.IfcTrapeziumProfileDef.name()));
        if (trapeziumPD.contains(profileDef)) return ProfileDefRepresentationTypeItems.IfcTrapeziumProfileDef.name();

        ArrayList<EntityInstance> circlePD = new ArrayList<>(ifcModel.getInstancesOfType(ProfileDefRepresentationTypeItems.IfcCircleProfileDef.name()));
        if (circlePD.contains(profileDef)) return ProfileDefRepresentationTypeItems.IfcCircleProfileDef.name();

        ArrayList<EntityInstance> ellipsePD = new ArrayList<>(ifcModel.getInstancesOfType(ProfileDefRepresentationTypeItems.IfcEllipseProfileDef.name()));
        if (ellipsePD.contains(profileDef)) return ProfileDefRepresentationTypeItems.IfcEllipseProfileDef.name();

        ArrayList<EntityInstance> shapePD = new ArrayList<>(ifcModel.getInstancesOfType(ProfileDefRepresentationTypeItems.IfcShapeProfileDef.name()));
        if (shapePD.contains(profileDef)) return ProfileDefRepresentationTypeItems.IfcShapeProfileDef.name();

        ArrayList<EntityInstance> arbitraryPD = new ArrayList<>(ifcModel.getInstancesOfType(ProfileDefRepresentationTypeItems.IfcArbitraryClosedProfileDef.name()));
        if (arbitraryPD.contains(profileDef))
            return ProfileDefRepresentationTypeItems.IfcArbitraryClosedProfileDef.name();

        Logging.info(IfcObjectIdentifier.class.getName() + ": " + profileDef.getEntityDefinition() + " ProfileDefRepresentationType is not supported");
        return null;
    }

    /**
     * Method gets type of IfcBooleanOperand
     *
     * @param ifcModel ifc model
     * @param entity   to get type of
     * @return type of IfcBooleanOperand
     */
    public static String getIfcBooleanOperandType(ModelPopulation ifcModel, EntityInstance entity) {
        ArrayList<EntityInstance> extrudedAreas = new ArrayList<>(ifcModel.getInstancesOfType(IfcBooleanOperandType.IfcExtrudedAreaSolid.name()));
        if (extrudedAreas.contains(entity)) return IfcBooleanOperandType.IfcExtrudedAreaSolid.name();

        ArrayList<EntityInstance> facetedBreps = new ArrayList<>(ifcModel.getInstancesOfType(IfcBooleanOperandType.IfcFacetedBrep.name()));
        if (facetedBreps.contains(entity)) return IfcBooleanOperandType.IfcFacetedBrep.name();

        ArrayList<EntityInstance> solidModels = new ArrayList<>(ifcModel.getInstancesOfType(IfcBooleanOperandType.IfcSolidModel.name()));
        if (solidModels.contains(entity)) return IfcBooleanOperandType.IfcSolidModel.name();

        ArrayList<EntityInstance> csgSolid = new ArrayList<>(ifcModel.getInstancesOfType(IfcBooleanOperandType.IfcCsgSolid.name()));
        if (csgSolid.contains(entity)) return IfcBooleanOperandType.IfcCsgSolid.name();

        ArrayList<EntityInstance> manifoldSolid = new ArrayList<>(ifcModel.getInstancesOfType(IfcBooleanOperandType.IfcManifoldSolidBrep.name()));
        if (manifoldSolid.contains(entity)) return IfcBooleanOperandType.IfcManifoldSolidBrep.name();

        ArrayList<EntityInstance> sweptAreaSolid = new ArrayList<>(ifcModel.getInstancesOfType(IfcBooleanOperandType.IfcSweptAreaSolid.name()));
        if (sweptAreaSolid.contains(entity)) return IfcBooleanOperandType.IfcSweptAreaSolid.name();

        ArrayList<EntityInstance> sweptDiskSolid = new ArrayList<>(ifcModel.getInstancesOfType(IfcBooleanOperandType.IfcSweptDiskSolid.name()));
        if (sweptDiskSolid.contains(entity)) return IfcBooleanOperandType.IfcSweptDiskSolid.name();

        ArrayList<EntityInstance> halfSpaceSolids = new ArrayList<>(ifcModel.getInstancesOfType(IfcBooleanOperandType.IfcHalfSpaceSolid.name()));
        if (halfSpaceSolids.contains(entity)) return IfcBooleanOperandType.IfcHalfSpaceSolid.name();

        ArrayList<EntityInstance> boxedHalfSpaceSolids = new ArrayList<>(ifcModel.getInstancesOfType(IfcBooleanOperandType.IfcBoxedHalfSpace.name()));
        if (boxedHalfSpaceSolids.contains(entity)) return IfcBooleanOperandType.IfcBoxedHalfSpace.name();

        ArrayList<EntityInstance> polygonBoundedHalfSpaces = new ArrayList<>(ifcModel.getInstancesOfType(IfcBooleanOperandType.IfcPolygonalBoundedHalfSpace.name()));
        if (polygonBoundedHalfSpaces.contains(entity)) return IfcBooleanOperandType.IfcPolygonalBoundedHalfSpace.name();

        ArrayList<EntityInstance> booleanResults = new ArrayList<>(ifcModel.getInstancesOfType(IfcBooleanOperandType.IfcBooleanResult.name()));
        if (booleanResults.contains(entity)) return IfcBooleanOperandType.IfcBooleanResult.name();

        ArrayList<EntityInstance> clippingResult = new ArrayList<>(ifcModel.getInstancesOfType(IfcBooleanOperandType.IfcBooleanClippingResult.name()));
        if (clippingResult.contains(entity)) return IfcBooleanOperandType.IfcBooleanClippingResult.name();

        ArrayList<EntityInstance> csgPrimitive3Ds = new ArrayList<>(ifcModel.getInstancesOfType(IfcBooleanOperandType.IfcCsgPrimitive3D.name()));
        if (csgPrimitive3Ds.contains(entity)) return IfcBooleanOperandType.IfcCsgPrimitive3D.name();

        ArrayList<EntityInstance> blocks = new ArrayList<>(ifcModel.getInstancesOfType(IfcBooleanOperandType.IfcBlock.name()));
        if (blocks.contains(entity)) return IfcBooleanOperandType.IfcBlock.name();

        ArrayList<EntityInstance> rectPyramids = new ArrayList<>(ifcModel.getInstancesOfType(IfcBooleanOperandType.IfcRectangularPyramid.name()));
        if (rectPyramids.contains(entity)) return IfcBooleanOperandType.IfcRectangularPyramid.name();

        ArrayList<EntityInstance> rightCircularCones = new ArrayList<>(ifcModel.getInstancesOfType(IfcBooleanOperandType.IfcRightCircularCone.name()));
        if (rightCircularCones.contains(entity)) return IfcBooleanOperandType.IfcRightCircularCone.name();

        ArrayList<EntityInstance> rightCircularCylinders = new ArrayList<>(ifcModel.getInstancesOfType(IfcBooleanOperandType.IfcRightCircularCylinder.name()));
        if (rightCircularCylinders.contains(entity)) return IfcBooleanOperandType.IfcRightCircularCylinder.name();

        ArrayList<EntityInstance> spheres = new ArrayList<>(ifcModel.getInstancesOfType(IfcBooleanOperandType.IfcSphere.name()));
        if (spheres.contains(entity)) return IfcBooleanOperandType.IfcSphere.name();

        Logging.info(IfcObjectIdentifier.class.getName() + ": " + entity.getEntityDefinition() + " is not supported as IfcBooleanOperand");
        return null;
    }

    /**
     * Method gets type of IfcBoundedCurve
     *
     * @param ifcModel ifc model
     * @param entity   to get type of
     * @return type of IfcBoundedCurve
     */
    public static String getIfcCurveType(ModelPopulation ifcModel, EntityInstance entity) {
        ArrayList<EntityInstance> compositeCurves = new ArrayList<>(ifcModel.getInstancesOfType(CurveRepresentationTypeItems.IfcCompositeCurve.name()));
        if (compositeCurves.contains(entity)) return CurveRepresentationTypeItems.IfcCompositeCurve.name();

        ArrayList<EntityInstance> polylines = new ArrayList<>(ifcModel.getInstancesOfType(CurveRepresentationTypeItems.IfcPolyline.name()));
        if (polylines.contains(entity)) return CurveRepresentationTypeItems.IfcPolyline.name();

        ArrayList<EntityInstance> trimmedCurves = new ArrayList<>(ifcModel.getInstancesOfType(CurveRepresentationTypeItems.IfcTrimmedCurve.name()));
        if (trimmedCurves.contains(entity)) return CurveRepresentationTypeItems.IfcTrimmedCurve.name();

        ArrayList<EntityInstance> bSplineCurves = new ArrayList<>(ifcModel.getInstancesOfType(CurveRepresentationTypeItems.IfcBSplineCurve.name()));
        if (bSplineCurves.contains(entity)) return CurveRepresentationTypeItems.IfcBSplineCurve.name();

        ArrayList<EntityInstance> conicCurves = new ArrayList<>(ifcModel.getInstancesOfType(CurveRepresentationTypeItems.IfcConic.name()));
        if (conicCurves.contains(entity)) return CurveRepresentationTypeItems.IfcConic.name();

        ArrayList<EntityInstance> circleCurves = new ArrayList<>(ifcModel.getInstancesOfType(CurveRepresentationTypeItems.IfcCircle.name()));
        if (circleCurves.contains(entity)) return CurveRepresentationTypeItems.IfcCircle.name();

        ArrayList<EntityInstance> ellCurves = new ArrayList<>(ifcModel.getInstancesOfType(CurveRepresentationTypeItems.IfcEllipse.name()));
        if (ellCurves.contains(entity)) return CurveRepresentationTypeItems.IfcEllipse.name();

        ArrayList<EntityInstance> lineCurves = new ArrayList<>(ifcModel.getInstancesOfType(CurveRepresentationTypeItems.IfcLine.name()));
        if (lineCurves.contains(entity)) return CurveRepresentationTypeItems.IfcLine.name();

        ArrayList<EntityInstance> offsetCurves = new ArrayList<>(ifcModel.getInstancesOfType(CurveRepresentationTypeItems.IfcOffsetCurve2D.name()));
        if (offsetCurves.contains(entity)) return CurveRepresentationTypeItems.IfcOffsetCurve2D.name();

        ArrayList<EntityInstance> offsetCurves3D = new ArrayList<>(ifcModel.getInstancesOfType(CurveRepresentationTypeItems.IfcOffsetCurve3D.name()));
        if (offsetCurves3D.contains(entity)) return CurveRepresentationTypeItems.IfcOffsetCurve3D.name();

        ArrayList<EntityInstance> indexesCurves = new ArrayList<>(ifcModel.getInstancesOfType(CurveRepresentationTypeItems.IfcIndexedPolyCurve.name()));
        if (indexesCurves.contains(entity)) return CurveRepresentationTypeItems.IfcIndexedPolyCurve.name();

        Logging.info(IfcObjectIdentifier.class.getName() + ": " + entity.getEntityDefinition() + " is not supported as IfcBoundedCurveType");
        return null;
    }

    /**
     * Method gets type of IfcSpatialStructureElement
     *
     * @param ifcModel ifc model
     * @param entity   to get type of
     * @return type of IfcSpatialStructureElement
     */
    public static String getSpatialStructureElementType(ModelPopulation ifcModel, EntityInstance entity) {
        ArrayList<EntityInstance> buildings = new ArrayList<>(ifcModel.getInstancesOfType(IfcSpatialStructureElementTypes.IfcBuilding.name()));
        if (buildings.contains(entity)) return IfcSpatialStructureElementTypes.IfcBuilding.name();

        ArrayList<EntityInstance> storeys = new ArrayList<>(ifcModel.getInstancesOfType(IfcSpatialStructureElementTypes.IfcBuildingStorey.name()));
        if (storeys.contains(entity)) return IfcSpatialStructureElementTypes.IfcBuildingStorey.name();

        ArrayList<EntityInstance> spaces = new ArrayList<>(ifcModel.getInstancesOfType(IfcSpatialStructureElementTypes.IfcSpace.name()));
        if (spaces.contains(entity)) return IfcSpatialStructureElementTypes.IfcSpace.name();

        ArrayList<EntityInstance> sites = new ArrayList<>(ifcModel.getInstancesOfType(IfcSpatialStructureElementTypes.IfcSite.name()));
        if (sites.contains(entity)) return IfcSpatialStructureElementTypes.IfcSite.name();

        Logging.info(IfcObjectIdentifier.class.getName() + ": " + entity.getEntityDefinition() + " is not supported as IfcSpatialStructureElement");
        return null;
    }

    /**
     * Checks if entity is part of IfcRelVoidsElement, if yes than returns IfcRelVoidsElement, else null
     *
     * @param ifcModel ifc model
     * @param entity   to get IfcRelVoidsElement for
     * @return if entity part of an IfcRelVoidsElement the EntityInstance if IfcRelVoidsElement, else null
     */
    public static EntityInstance getRelVoidsElementOfEntity(ModelPopulation ifcModel, EntityInstance entity) {
        ArrayList<EntityInstance> relVoidsElements = new ArrayList<>(ifcModel.getInstancesOfType(BIMObject.IfcRelVoidsElement.name()));
        for (EntityInstance relVoidsElement : relVoidsElements) {
            int relatingBuildingElementId = relVoidsElement.getAttributeValueBNasEntityInstance("RelatingBuildingElement").getId();
            if (relatingBuildingElementId == entity.getId()) return relVoidsElement;
        }
        return null;
    }

    /**
     * Checks if entity is of type IfcPolyline
     *
     * @param ifcModel ifc model
     * @param entity   to check type of
     * @return true if IfcPolyline, else false
     */
    public static boolean isIfcPolyline(ModelPopulation ifcModel, EntityInstance entity) {
        ArrayList<EntityInstance> polylines = new ArrayList<>(ifcModel.getInstancesOfType(CurveRepresentationTypeItems.IfcPolyline.name()));
        return polylines.contains(entity);
    }

    /**
     * Checks if entity is of type IfcCompositeCurve
     *
     * @param ifcModel ifc model
     * @param entity   to check type of
     * @return true if IfcCompositeCurve, else false
     */
    public static boolean isIfcCompositeCurve(ModelPopulation ifcModel, EntityInstance entity) {
        ArrayList<EntityInstance> cCurves = new ArrayList<>(ifcModel.getInstancesOfType(CurveRepresentationTypeItems.IfcCompositeCurve.name()));
        return cCurves.contains(entity);
    }

    /**
     * Checks if entity is of type IfcTrimmedCurve
     *
     * @param ifcModel ifc model
     * @param entity   to check type of
     * @return true if IfcTrimmedCurve, else false
     */
    public static boolean isIfcTrimmedCurve(ModelPopulation ifcModel, EntityInstance entity) {
        ArrayList<EntityInstance> tCurves = new ArrayList<>(ifcModel.getInstancesOfType(CurveRepresentationTypeItems.IfcTrimmedCurve.name()));
        return tCurves.contains(entity);
    }

    /**
     * Checks if entity is of type IfcCircle
     *
     * @param ifcModel ifc model
     * @param entity   to check type of
     * @return true if IfcCircle, else false
     */
    public static boolean isIfcCircle(ModelPopulation ifcModel, EntityInstance entity) {
        ArrayList<EntityInstance> cCurves = new ArrayList<>(ifcModel.getInstancesOfType(CurveRepresentationTypeItems.IfcCircle.name()));
        return cCurves.contains(entity);
    }

    /**
     * Checks if entity is of type IfcAxis2Placement3D
     *
     * @param ifcModel ifc model
     * @param entity   to check type of
     * @return true if IfcAxis2Placement3D, else false
     */
    public static boolean isIfcAxis2Placement3D(ModelPopulation ifcModel, EntityInstance entity) {
        ArrayList<EntityInstance> axis2placement3Ds = new ArrayList<>(ifcModel.getInstancesOfType(Axis2PlacementRepresentationTypeItems.IfcAxis2Placement3D.name()));
        return axis2placement3Ds.contains(entity);
    }

    /**
     * Checks if entity is of type IfcOpeningElement
     *
     * @param ifcModel ifc model
     * @param entity   to check type of
     * @return true if IfcOpeningElement, else false
     */
    public static boolean isIfcOpeningElement(ModelPopulation ifcModel, EntityInstance entity) {
        ArrayList<EntityInstance> openingElement = new ArrayList<>(ifcModel.getInstancesOfType(IfcRelVoidsElementTypes.IfcOpeningElement.name()));
        return openingElement.contains(entity);
    }

    /**
     * Checks if entity is of type IfcSlab
     *
     * @param ifcModel ifc model
     * @param entity   to check type of
     * @return true if IfcSlab, else false
     */
    public static boolean isIfcSlab(ModelPopulation ifcModel, EntityInstance entity) {
        ArrayList<EntityInstance> slabElements = new ArrayList<>(ifcModel.getInstancesOfType(BIMObject.IfcSlab.name()));
        return slabElements.contains(entity);
    }

    /**
     * Checks if entity is of type IfcDoor
     *
     * @param ifcModel ifc model
     * @param entity   to check type of
     * @return true if IfcDoor, else false
     */
    public static boolean isIfcDoor(ModelPopulation ifcModel, EntityInstance entity) {
        ArrayList<EntityInstance> doorElements = new ArrayList<>(ifcModel.getInstancesOfType(BIMObject.IfcDoor.name()));
        return doorElements.contains(entity);
    }

    /**
     * Checks if entity is of type IfcWindow
     *
     * @param ifcModel ifc model
     * @param entity   to check type of
     * @return true if IfcWindow, else false
     */
    public static boolean isIfcWindow(ModelPopulation ifcModel, EntityInstance entity) {
        ArrayList<EntityInstance> windowElements = new ArrayList<>(ifcModel.getInstancesOfType(BIMObject.IfcWindow.name()));
        return windowElements.contains(entity);
    }

    public static boolean isIfcWindowOrIfcDoor(ModelPopulation ifcModel, EntityInstance entity) {
        return isIfcWindow(ifcModel, entity) || isIfcDoor(ifcModel, entity);
    }

    /**
     * Removes unnecessary chars from representation attribute string
     *
     * @param attribute representation attribute (RepresentationIdentifier, RepresentationType)
     * @return prepared string
     */
    private static String prepareRepresentationAttribute(String attribute) {
        return attribute.substring(1, attribute.length() - 1);
    }

}

