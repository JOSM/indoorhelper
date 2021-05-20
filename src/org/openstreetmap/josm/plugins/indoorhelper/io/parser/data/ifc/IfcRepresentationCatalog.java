// License: AGPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.indoorhelper.io.parser.data.ifc;

/**
 * Class classify ifc object types.
 * Notice: Enum elements are not defined uppercase to use them as strings
 *
 * @author rebsc
 */
public class IfcRepresentationCatalog {

    /**
     * RepresentationIdentifier of IfcShapeRepresentations
     */
    public enum RepresentationIdentifier {
        Annotation, Axis, Box, Body, Clearance, CoG, FootPrint, Lighting, Profile, Reference, Surface
    }

    /**
     * RepresentationType of IfcShapeRepresentations
     */
    public enum RepresentationType {
        AdvancedBrep, AdvancedSweptSolid, Annotation2D, Brep, CSG, Clipping, SurfaceModel, Point, PointCloud, Curve, Curve2D,
        Curve3D, Surface, Surface2D, Surface3D, FillArea, Text, AdvancedSurface, GeometricSet, GeometricCurveSet,
        Tessellation, SolidModel, SweptSolid, BoundingBox, SectionedSpine, LightSoure,
        MappedRepresentation
    }

    /**
     * Permitted IfcShapeRepresentation.Items types for IfcShapeRepresentation.RepresentationType AdvancedBrep
     */
    public enum AdvancedBrepRepresentationTypeItems {
        IfcAdvancedBrep, IfcFacetedBrep
    }

    /**
     * Permitted IfcShapeRepresentation.Items types for IfcShapeRepresentation.RepresentationTypes AdvancedSweptSolid
     */
    public enum AdvancedSweptSolidRepresentationTypeItems {
        IfcSweptDiskSolid, IfcSweptDiskSolidPolygonal
    }

    /**
     * Permitted IfcShapeRepresentation.Items types for IfcShapeRepresentation.RepresentationType Brep
     */
    public enum BrepRepresentationTypeItems {
        IfcFacetedBrep
    }

    /**
     * Permitted IfcShapeRepresentation.Items types for IfcShapeRepresentation.RepresentationType CSG
     */
    public enum CSGRepresentationTypeItems {
        IfcCsgSolid, IfcBooleanResult, IfcPrimitive3D
    }

    /**
     * Permitted IfcShapeRepresentation.Items types for IfcShapeRepresentation.RepresentationTypes Clipping
     */
    public enum ClippingRepresentationTypeItems {
        IfcBooleanClippingResult
    }

    /**
     * Permitted IfcShapeRepresentation.Items types for IfcShapeRepresentation.RepresentationType Curve.
     * Includes supertypes
     */
    public enum CurveRepresentationTypeItems {
        IfcBoundedCurve, IfcPolyline, IfcCompositeCurve, IfcTrimmedCurve, IfcBSplineCurve,
        IfcConic, IfcCircle, IfcEllipse, IfcLine, IfcOffsetCurve2D, IfcOffsetCurve3D, IfcIndexedPolyCurve
    }

    public enum MappedRepresentationTypeItems {
        IfcMappedItem
    }

    /**
     * Permitted IfcShapeRepresentation.Items types for IfcShapeRepresentation.RepresentationTypes Tesselation
     */
    public enum TessellationRepresentationTypeItems {
        IfcTessellatedFaceSet
    }

    /**
     * Permitted IfcShapeRepresentation.Items types for IfcShapeRepresentation.RepresentationTypes SurfaceModel
     */
    public enum SurfaceModelRepresentationTypeItems {
        IfcTessellatedItem, IfcShellBasedSurfaceModel, IfcFaceBasedSurfaceModel, IfcFacetedBrep
    }

    /**
     * Permitted IfcShapeRepresentation.Items types for IfcShapeRepresentation.RepresentationTypes SweptSolid
     */
    public enum SweptSolidRepresentationTypeItems {
        IfcExtrudedAreaSolid, IfcRevolvedAreaSolid
    }

    /**
     * Permitted IfcShapeRepresentation.Items types for IfcShapeRepresentation.RepresentationTypes BoundingBox
     */
    public enum BoundingBoxRepresentationTypeItems {
        IfcBoundingBox
    }


    // Ifc representation subtypes

    /**
     * Permitted IfcShapeRepresentation.Items types for IfcShapeRepresentation IfcLoop
     */
    public enum LoopSubRepresentationTypeItems {
        IfcEdgeLoop, IfcPolyLoop, IfcVertexLoop
    }

    /**
     * Permitted IfcShapeRepresentation.Items types for IfcShapeRepresentation IfcProfileDef
     */
    public enum ProfileDefRepresentationTypeItems {
        IfcRectangleProfileDef, IfcTrapeziumProfileDef, IfcCircleProfileDef, IfcEllipseProfileDef,
        IfcShapeProfileDef, IfcArbitraryClosedProfileDef
    }

    public enum Axis2PlacementRepresentationTypeItems {
        IfcAxis2Placement2D, IfcAxis2Placement3D
    }

    /**
     * This enumeration defines the available predefined types of a slab
     */
    public enum IfcSlabTypeEnum {
        FLOOR, ROOF, LANDING, BASESLAB, USERDEFINED, NOTDEFINED
    }

    /**
     * Types of entities which may participate in a Boolean operation to form a CSG solid
     */
    public enum IfcBooleanOperandType {
        IfcSolidModel, IfcCsgSolid, IfcManifoldSolidBrep, IfcSweptAreaSolid, IfcSweptDiskSolid,
        IfcHalfSpaceSolid, IfcBoxedHalfSpace, IfcPolygonalBoundedHalfSpace,
        IfcBooleanResult, IfcBooleanClippingResult,
        IfcCsgPrimitive3D, IfcBlock, IfcRectangularPyramid, IfcRightCircularCone, IfcRightCircularCylinder, IfcSphere,
        IfcExtrudedAreaSolid, IfcFacetedBrep
    }

    // Items of IFC void elements

    public enum IfcRelVoidsElementTypes {
        IfcOpeningElement, IfcVoidingFeature
    }


    public enum IfcSpatialStructureElementTypes {
        IfcBuilding, IfcBuildingStorey, IfcSpace, IfcSite
    }

}
