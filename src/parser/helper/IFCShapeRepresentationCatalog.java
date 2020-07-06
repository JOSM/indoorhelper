// License: GPL. For details, see LICENSE file.
package parser.helper;

/**
 *
 * @author rebsc
 *
 */
public class IFCShapeRepresentationCatalog {

	/**
     *  RepresentationIdentifier of IFCSHAPEREPRESENTATION
     *  Note: Not all of those types are supported
     *
     */
	public enum RepresentationIdentifier {
		Annotation, Axis, Box, Body, Clearance, CoG, FootPrint, Lighting, Profile, Reference, Surface
    }

	/**
     *  RepresentationType of IFCSHAPEREPRESENTATION
     *  Note: Not all of those types are supported
     *
     */
	public enum RepresentationType {
		AdvancedBrep, AdvancedSweptSolid, Annotation2D, Brep, CSG, Clipping, SurfaceModel, Point, PointCloud, Curve, Curve2D,
		Curve3D, Surface, Surface2D, Surface3D, FillArea, Text, AdvancedSurface, GeometricSet, GeometricCurveSet,
		Tessellation, SolidModel, SweptSolid, BoundingBox, SectionedSpine, LightSoure,
		MappedRepresentation
    }

	/**
	 * Allowed IFCSHAPEREPRESENTATION.Items types for IFCSHAPEREPRESENTATION.RepresentationType ADVANCEDBREP
	 *
	 */
	public enum AdvancedBrepRepresentationTypeItems {
		IfcAdvancedBrep, IfcFacetedBrep
	}

	/**
	 * Allowed IFCSHAPEREPRESENTATION.Items types for IFCSHAPEREPRESENTATION.RepresentationTypes ADVANCEDWEPTSOLID
	 *
	 */
	public enum AdvancedSweptSolidRepresentationTypeItems {
		 IfcSweptDiskSolid, IfcSweptDiskSolidPolygonal
	}

	/**
	 * Allowed IFCSHAPEREPRESENTATION.Items types for IFCSHAPEREPRESENTATION.RepresentationType BREP
	 *
	 */
	public enum BrepRepresentationTypeItems {
		IfcFacetedBrep
	}

	/**
	 * Allowed IFCSHAPEREPRESENTATION.Items types for IFCSHAPEREPRESENTATION.RepresentationType CSG
	 *
	 */
	public enum CSGRepresentationTypeItems {
		IfcCsgSolid, IfcBooleanResult, IfcPrimitive3D
	}

	/**
	 * Allowed IFCSHAPEREPRESENTATION.Items types for IFCSHAPEREPRESENTATION.RepresentationTypes CLIPPING
	 *
	 */
	public enum ClippingRepresentationTypeItems {
		IfcBooleanClippingResult
	}

	/**
	 * Allowed IFCSHAPEREPRESENTATION.Items types for IFCSHAPEREPRESENTATION.RepresentationType CURVE2D/ CURVE3D
	 *
	 */
	public enum CurveRepresentationTypeItems {
		 IfcBoundedCurve, IfcPolyline
	}

	public enum MappedRepresentatiobTypeItems {
		IfcMappedItem
	}

	/**
	 * Allowed IFCSHAPEREPRESENTATION.Items types for IFCSHAPEREPRESENTATION.RepresentationTypes TESSELATION
	 *
	 */
	public enum TessellationRepresentationTypeItems {
		IfcTessellatedFaceSet
	}

	/**
	 * Allowed IFCSHAPEREPRESENTATION.Items types for IFCSHAPEREPRESENTATION.RepresentationTypes SURFACEMODEL
	 *
	 */
	public enum SurfaceModelRepresentationTypeItems {
		IfcTessellatedItem, IfcShellBasedSurfaceModel, IfcFaceBasedSurfaceModel, IfcFacetedBrep
	}

	/**
	 * Allowed IFCSHAPEREPRESENTATION.Items types for IFCSHAPEREPRESENTATION.RepresentationTypes SWEPTSOLID
	 *
	 */
	public enum SweptSolidRepresentationTypeItems {
		 IfcExtrudedAreaSolid, IfcRevolvedAreaSolid
	}

	/**
	 * Allowed IFCSHAPEREPRESENTATION.Items types for IFCSHAPEREPRESENTATION.RepresentationTypes BOUNDINGBOX
	 *
	 */
	public enum BoundingBoxRepresentationTypeItems {
		IfcBoundingBox
	}



	// Items of IFC representation subtypes
	// Note: Not all of those subtypes are supported right now

	/**
	 * Allowed IFCSHAPEREPRESENTATION.Items types for IFCREPRESENTATIONITEM IFCLOOP
	 *
	 */
	public enum LoopSubRepresentationTypeItems {
		IfcEdgeLoop, IfcPolyLoop, IfcVertexLoop
	}

	/**
	 * Allowed IFCSHAPEREPRESENTATION.Items types for IFCREPRESENTATIONITEM IFCPROFILEDEF
	 *
	 */
	public enum ProfileDefRepresentationTypeItems{
		IfcRectangleProfileDef, IfcTrapeziumProfileDef, IfcCircleProfileDef, IfcEllipseProfileDef,
		IfcShapeProfileDef
	}



}
