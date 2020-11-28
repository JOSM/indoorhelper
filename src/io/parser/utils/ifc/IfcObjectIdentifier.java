// License: AGPL. For details, see LICENSE file.
package io.parser.utils.ifc;

import io.model.BIMtoOSMCatalog.BIMObject;
import io.parser.data.ifc.IfcRepresentationCatalog.*;
import io.parser.data.ifc.IfcRepresentation;
import nl.tue.buildingsmart.express.population.EntityInstance;
import nl.tue.buildingsmart.express.population.ModelPopulation;
import org.openstreetmap.josm.tools.Logging;

import java.util.ArrayList;

/**
 * Class to identify the type on an IFCSHAPEREPRESENTATION object
 *
 * @author rebsc
 */
public class IfcObjectIdentifier {

    /**
     * Method gets IFCSHAPEREPRESENTATION.REPRESENTATIONIDENTIFIER and IFCSHAPEREPRESENTATION.REPRESENTATIONTYPE of object
     *
     * @param shapeRepresentation IFCShapeRepresentationIdentity
     * @return Returns object containing IFCSHAPEREPRESENTATION.REPRESENTATIONIDENTIFIER and IFCSHAPEREPRESENTATION.REPRESENTATIONTYPE
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
     * Gets the type of specific IFCSHAPEREPRESENTATION.ITEM. If item type is not supported for IFCSHAPEREPRESENTATION.REPRESENTATIONTYPE
     * method will return null.
     *
     * @param ifcModel ifcModel
     * @param ident    IFCREPRESENTATIONTYPEOBJECT object
     * @param item     to get the representation type for (IFCSHAPEREPRESENTATION.ITEM packed into EntityInstance object)
     * @return String with IFCSHAPEREPRESENTATION.ITEM type definition or null if not allowed in standard
     */
    public static String getRepresentationItemType(ModelPopulation ifcModel, IfcRepresentation ident, EntityInstance item) {

        if (ident.getType().equals(RepresentationType.AdvancedBrep)) {
            // here IFC standard only allows IFCADVANCEDBREP and IFCFACETEDBREP as item

            ArrayList<EntityInstance> ifcAdvancedBrep = new ArrayList<>(ifcModel.getInstancesOfType(AdvancedBrepRepresentationTypeItems.IfcAdvancedBrep.name()));
            if (ifcAdvancedBrep.contains(item)) return AdvancedBrepRepresentationTypeItems.IfcAdvancedBrep.name();

            ArrayList<EntityInstance> ifcFacetedBrep = new ArrayList<>(ifcModel.getInstancesOfType(AdvancedBrepRepresentationTypeItems.IfcFacetedBrep.name()));
            if (ifcFacetedBrep.contains(item)) return AdvancedBrepRepresentationTypeItems.IfcFacetedBrep.name();

            Logging.info(IfcObjectIdentifier.class.getName() + ": " + item.getEntityDefinition() + " is not supported");
            return null;
        }

        if (ident.getType().equals(RepresentationType.AdvancedSweptSolid)) {
            // here IFC standard only allows IFCSWEPTDISKSOLID and IFCSWEPTDISKSOLIDPOLYGON as item

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
            // here IFC standard only allows IFCFACETEDBREP as item
            if (ifcModel.getInstancesOfType(BrepRepresentationTypeItems.IfcFacetedBrep.name()).contains(item)) {
                return BrepRepresentationTypeItems.IfcFacetedBrep.name();
            }
            Logging.info(IfcObjectIdentifier.class.getName() + ": " + item.getEntityDefinition() + " is not supported");
            return null;
        }

        if (ident.getType().equals(RepresentationType.CSG)) {
            // here IFC standard only allows IFCCSGSOLID, IFCBOOLEANRESULT and IFCPRIMITIVE3D as items

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
            // here IFC standard only allows IFCTESSELATEDFACESET as item
            if (ifcModel.getInstancesOfType(TessellationRepresentationTypeItems.IfcTessellatedFaceSet.name()).contains(item)) {
                return TessellationRepresentationTypeItems.IfcTessellatedFaceSet.name();
            }
            Logging.info(IfcObjectIdentifier.class.getName() + ": " + item.getEntityDefinition() + " is not supported");
            return null;
        }

        if (ident.getType().equals(RepresentationType.Clipping)) {
            // here IFC standard only allows IFCBOOLEANCLIPPINGRESULT as item
            if (ifcModel.getInstancesOfType(ClippingRepresentationTypeItems.IfcBooleanClippingResult.name()).contains(item)) {
                return ClippingRepresentationTypeItems.IfcBooleanClippingResult.name();
            }
            Logging.info(IfcObjectIdentifier.class.getName() + ": " + item.getEntityDefinition() + " is not supported");
            return null;
        }

        if (ident.getType().equals(RepresentationType.Curve2D) || ident.getType().equals(RepresentationType.Curve3D)) {
            // here IFC standard only allows IFCBOUNDEDCURVE as item

            ArrayList<EntityInstance> ifcBoundedCurves = new ArrayList<>(ifcModel.getInstancesOfType(CurveRepresentationTypeItems.IfcBoundedCurve.name()));
            if (ifcBoundedCurves.contains(item)) return CurveRepresentationTypeItems.IfcBoundedCurve.name();

            ArrayList<EntityInstance> ifcPolylines = new ArrayList<>(ifcModel.getInstancesOfType(CurveRepresentationTypeItems.IfcPolyline.name()));
            if (ifcPolylines.contains(item)) return CurveRepresentationTypeItems.IfcPolyline.name();

            Logging.info(IfcObjectIdentifier.class.getName() + ": " + item.getEntityDefinition() + " is not supported");
            return null;
        }

        if (ident.getType().equals(RepresentationType.SurfaceModel)) {
            // here IFC standard only allows IFCTESSELLATEDITEM, IFCSCHELLBASEDSURFACEMODEL and
            // IFCFACEBASEDSURFACEMODEL as item

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
            // here IFC standard only allows IFCEXTRUDEDAREASOLID, IFCREVOLVEDAREASOLID as item

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
            // here IFC standard only allows IFCBOUNDINGBOX as item
            if (ifcModel.getInstancesOfType(BoundingBoxRepresentationTypeItems.IfcBoundingBox.name()).contains(item)) {
                return BoundingBoxRepresentationTypeItems.IfcBoundingBox.name();
            }
            Logging.info(IfcObjectIdentifier.class.getName() + ": " + item.getEntityDefinition() + " is not supported");
            return null;
        }

        if (ident.getType().equals(RepresentationType.MappedRepresentation)) {
            // here IFC standard only allows IFCMAPPEDITEM as item
            if (ifcModel.getInstancesOfType(MappedRepresentatiobTypeItems.IfcMappedItem.name()).contains(item)) {
                return MappedRepresentatiobTypeItems.IfcMappedItem.name();
            }
            Logging.info(IfcObjectIdentifier.class.getName() + ": " + item.getEntityDefinition() + " is not supported");
            return null;
        }

        Logging.info(IfcObjectIdentifier.class.getName() + ": " + ident.getType() + " RepresentationType is not supported");
        return null;
    }


    /**
     * Gets the type of IFCLOOP. Types can be IFCEDGELOOP, IFCPOLYLOOP, IFCVERTEXLOOP
     *
     * @param ifcModel ifcModel
     * @param loop     to get type of
     * @return type as string
     */
    public static String getIFCLoopType(ModelPopulation ifcModel, EntityInstance loop) {
        // here IFC standard only allows IFCEDGELOOP, IFCPOLYLOOP, IFCVERTEXLOOP as item
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
        // here IFC standard only allows IFCRECTANGLEPROFILEDEF, IFCTRAPEZIUMPROFILEDEF, IFCCIRCLEPROFILEDEF,
        // IFCELLIPSEPROFILEDEF, IFCSHAPEPROFILEDEF as item

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
     * Method gets type of IFCBOOLEANOPERAND
     *
     * @param ifcModel ifc model
     * @param entity   to get type of
     * @return type of IFCBOOLEANOPERAND
     */
    public static String getIfcBooleanOperandType(ModelPopulation ifcModel, EntityInstance entity) {
        // allowed types

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
     * Method gets type of IFCBOUNDEDCURVE
     *
     * @param ifcModel ifc model
     * @param entity   to get type of
     * @return type of IFCBOUNDEDCURVE
     */
    public static String getIfcCurveType(ModelPopulation ifcModel, EntityInstance entity) {
        // here IFCCOMPOSITECURVE, IFCPOLYLINE, IFCTRIMMEDCURVE, IFCBSPLINECURVE allowed

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
     * Method gets type of IFCSPATIALSTRUCTUREELEMENT
     *
     * @param ifcModel ifc model
     * @param entity   to get type of
     * @return type of IFCSPATIALSTRUCTUREELEMENT
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
     * Checks if entity is part of IFCRELVOIDSELEMENT, if yes than returns IFCRELVOIDSELEMENT, else null
     *
     * @param ifcModel ifc model
     * @param entity   to get IFCRELVOIDSELEMENT for
     * @return if entity part of an IFCRELVOIDSELEMENT the EntityInstance if IFCRELVOIDSELEMENT, else null
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
     * Checks if entity is of type IFCPOLYLINE
     *
     * @param ifcModel ifc model
     * @param entity   to check type of
     * @return true if IFCPOLYLINE, else false
     */
    public static boolean isIfcPolyline(ModelPopulation ifcModel, EntityInstance entity) {
        ArrayList<EntityInstance> polylines = new ArrayList<>(ifcModel.getInstancesOfType(CurveRepresentationTypeItems.IfcPolyline.name()));
        return polylines.contains(entity);
    }

    /**
     * Checks if entity is of type IFCCOMPOSITECURVE
     *
     * @param ifcModel ifc model
     * @param entity   to check type of
     * @return true if IFCCOMPOSITECURVE, else false
     */
    public static boolean isIfcCompositeCurve(ModelPopulation ifcModel, EntityInstance entity) {
        ArrayList<EntityInstance> cCurves = new ArrayList<>(ifcModel.getInstancesOfType(CurveRepresentationTypeItems.IfcCompositeCurve.name()));
        return cCurves.contains(entity);
    }

    /**
     * Checks if entity is of type IFCTRIMMEDCURVE
     *
     * @param ifcModel ifc model
     * @param entity   to check type of
     * @return true if IFCTRIMMEDCURVE, else false
     */
    public static boolean isIfcTrimmedCurve(ModelPopulation ifcModel, EntityInstance entity) {
        ArrayList<EntityInstance> tCurves = new ArrayList<>(ifcModel.getInstancesOfType(CurveRepresentationTypeItems.IfcTrimmedCurve.name()));
        return tCurves.contains(entity);
    }

    /**
     * Checks if entity is of type IFCCIRCLE
     *
     * @param ifcModel ifc model
     * @param entity   to check type of
     * @return true if IFCCIRCLE, else false
     */
    public static boolean isIfcCircle(ModelPopulation ifcModel, EntityInstance entity) {
        ArrayList<EntityInstance> cCurves = new ArrayList<>(ifcModel.getInstancesOfType(CurveRepresentationTypeItems.IfcCircle.name()));
        return cCurves.contains(entity);
    }

    /**
     * Checks if entity is of type IFCAXIS2PLACEMENT3D
     *
     * @param ifcModel ifc model
     * @param entity   to check type of
     * @return true if IFCAXIS2PLACEMENT3D, else false
     */
    public static boolean isIfcAxis2Placement3D(ModelPopulation ifcModel, EntityInstance entity) {
        ArrayList<EntityInstance> axis2placement3Ds = new ArrayList<>(ifcModel.getInstancesOfType(Axis2PlacementRepresentationTypeItems.IfcAxis2Placement3D.name()));
        return axis2placement3Ds.contains(entity);
    }

    /**
     * Checks if entity is of type IFCOPENINGELEMENT
     *
     * @param ifcModel ifc model
     * @param entity   to check type of
     * @return true if IFCOPENINGELEMENT, else false
     */
    public static boolean isIfcOpeningElement(ModelPopulation ifcModel, EntityInstance entity) {
        ArrayList<EntityInstance> openingElement = new ArrayList<>(ifcModel.getInstancesOfType(IfcRelVoidsElementTypes.IfcOpeningElement.name()));
        return openingElement.contains(entity);
    }

    /**
     * Checks if entity is of type IFCSLAB
     *
     * @param ifcModel ifc model
     * @param entity   to check type of
     * @return true if IFCSLAB, else false
     */
    public static boolean isIfcSlab(ModelPopulation ifcModel, EntityInstance entity) {
        ArrayList<EntityInstance> slabElements = new ArrayList<>(ifcModel.getInstancesOfType(BIMObject.IfcSlab.name()));
        return slabElements.contains(entity);
    }

    /**
     * Checks if entity is of type IFCDOOR
     *
     * @param ifcModel ifc model
     * @param entity   to check type of
     * @return true if IFCDOOR, else false
     */
    public static boolean isIfcDoor(ModelPopulation ifcModel, EntityInstance entity) {
        ArrayList<EntityInstance> doorElements = new ArrayList<>(ifcModel.getInstancesOfType(BIMObject.IfcDoor.name()));
        return doorElements.contains(entity);
    }

    /**
     * Checks if entity is of type IFCWINDOW
     *
     * @param ifcModel ifc model
     * @param entity   to check type of
     * @return true if IFCWINDOW, else false
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
     * @param attribute representation attribute (REPRESENTATIONIDENTIFIER, REPRESENTATIONTYPE)
     * @return prepared string
     */
    private static String prepareRepresentationAttribute(String attribute) {
        return attribute.substring(1, attribute.length() - 1);
    }

}

