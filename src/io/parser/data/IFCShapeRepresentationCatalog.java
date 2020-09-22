// License: AGPL. For details, see LICENSE file.
package io.parser.data;

/**
 * @author rebsc
 */
public class IFCShapeRepresentationCatalog {

    /**
     * RepresentationIdentifier of IFCSHAPEREPRESENTATION
     */
    public enum RepresentationIdentifier {
        Annotation, Axis, Box, Body, Clearance, CoG, FootPrint, Lighting, Profile, Reference, Surface
    }

    /**
     * RepresentationType of IFCSHAPEREPRESENTATION
     */
    public enum RepresentationType {
        AdvancedBrep, AdvancedSweptSolid, Annotation2D, Brep, CSG, Clipping, SurfaceModel, Point, PointCloud, Curve, Curve2D,
        Curve3D, Surface, Surface2D, Surface3D, FillArea, Text, AdvancedSurface, GeometricSet, GeometricCurveSet,
        Tessellation, SolidModel, SweptSolid, BoundingBox, SectionedSpine, LightSoure,
        MappedRepresentation
    }

    /**
     * Allowed IFCSHAPEREPRESENTATION.Items types for IFCSHAPEREPRESENTATION.RepresentationType ADVANCEDBREP
     */
    public enum AdvancedBrepRepresentationTypeItems {
        IfcAdvancedBrep, IfcFacetedBrep
    }

    /**
     * Allowed IFCSHAPEREPRESENTATION.Items types for IFCSHAPEREPRESENTATION.RepresentationTypes ADVANCEDWEPTSOLID
     */
    public enum AdvancedSweptSolidRepresentationTypeItems {
        IfcSweptDiskSolid, IfcSweptDiskSolidPolygonal
    }

    /**
     * Allowed IFCSHAPEREPRESENTATION.Items types for IFCSHAPEREPRESENTATION.RepresentationType BREP
     */
    public enum BrepRepresentationTypeItems {
        IfcFacetedBrep
    }

    /**
     * Allowed IFCSHAPEREPRESENTATION.Items types for IFCSHAPEREPRESENTATION.RepresentationType CSG
     */
    public enum CSGRepresentationTypeItems {
        IfcCsgSolid, IfcBooleanResult, IfcPrimitive3D
    }

    /**
     * Allowed IFCSHAPEREPRESENTATION.Items types for IFCSHAPEREPRESENTATION.RepresentationTypes CLIPPING
     */
    public enum ClippingRepresentationTypeItems {
        IfcBooleanClippingResult
    }

    /**
     * Allowed IFCSHAPEREPRESENTATION.Items types for IFCSHAPEREPRESENTATION.RepresentationType CURVE.
     * Includes supertypes
     */
    public enum CurveRepresentationTypeItems {
        IfcBoundedCurve, IfcPolyline, IfcCompositeCurve, IfcTrimmedCurve, IfcBSplineCurve,
        IfcConic, IfcCircle, IfcEllipse, IfcLine, IfcOffsetCurve2D, IfcOffsetCurve3D, IfcIndexedPolyCurve
    }

    public enum MappedRepresentatiobTypeItems {
        IfcMappedItem
    }

    /**
     * Allowed IFCSHAPEREPRESENTATION.Items types for IFCSHAPEREPRESENTATION.RepresentationTypes TESSELATION
     */
    public enum TessellationRepresentationTypeItems {
        IfcTessellatedFaceSet
    }

    /**
     * Allowed IFCSHAPEREPRESENTATION.Items types for IFCSHAPEREPRESENTATION.RepresentationTypes SURFACEMODEL
     */
    public enum SurfaceModelRepresentationTypeItems {
        IfcTessellatedItem, IfcShellBasedSurfaceModel, IfcFaceBasedSurfaceModel, IfcFacetedBrep
    }

    /**
     * Allowed IFCSHAPEREPRESENTATION.Items types for IFCSHAPEREPRESENTATION.RepresentationTypes SWEPTSOLID
     */
    public enum SweptSolidRepresentationTypeItems {
        IfcExtrudedAreaSolid, IfcRevolvedAreaSolid
    }

    /**
     * Allowed IFCSHAPEREPRESENTATION.Items types for IFCSHAPEREPRESENTATION.RepresentationTypes BOUNDINGBOX
     */
    public enum BoundingBoxRepresentationTypeItems {
        IfcBoundingBox
    }


    // Items of IFC representation subtypes

    /**
     * Allowed IFCSHAPEREPRESENTATION.Items types for IFCREPRESENTATIONITEM IFCLOOP
     */
    public enum LoopSubRepresentationTypeItems {
        IfcEdgeLoop, IfcPolyLoop, IfcVertexLoop
    }

    /**
     * Allowed IFCSHAPEREPRESENTATION.Items types for IFCREPRESENTATIONITEM IFCPROFILEDEF
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
