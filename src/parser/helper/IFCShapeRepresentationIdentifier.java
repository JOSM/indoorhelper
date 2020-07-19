// License: GPL. For details, see LICENSE file.
package parser.helper;

import java.util.ArrayList;

import org.openstreetmap.josm.tools.Logging;

import model.io.BIMtoOSMCatalog.BIMObject;
import nl.tue.buildingsmart.express.population.EntityInstance;
import nl.tue.buildingsmart.express.population.ModelPopulation;
import parser.data.ifc.IFCShapeRepresentationIdentity;
import parser.helper.IFCShapeRepresentationCatalog.AdvancedBrepRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.AdvancedSweptSolidRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.Axis2PlacementRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.BoundingBoxRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.BrepRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.CSGRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.ClippingRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.CurveRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.IfcBooleanOperandType;
import parser.helper.IFCShapeRepresentationCatalog.IfcBoundedCurveTypes;
import parser.helper.IFCShapeRepresentationCatalog.IfcRelVoidsElementTypes;
import parser.helper.IFCShapeRepresentationCatalog.LoopSubRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.MappedRepresentatiobTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.ProfileDefRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.RepresentationIdentifier;
import parser.helper.IFCShapeRepresentationCatalog.RepresentationType;
import parser.helper.IFCShapeRepresentationCatalog.SurfaceModelRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.SweptSolidRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.TessellationRepresentationTypeItems;

/**
 * Class to identify the type on an IFCSHAPEREPRESENTATION object
 * @author rebsc
 *
 */
public class IFCShapeRepresentationIdentifier {

	/**
	 * Method gets IFCSHAPEREPRESENTATION.REPRESENTATIONIDENTIFIER and IFCSHAPEREPRESENTATION.REPRESENTATIONTYPE of object
	 * @param shapeRepresentation IFCShapeRepresentationIdentity
	 * @return Returns object containing IFCSHAPEREPRESENTATION.REPRESENTATIONIDENTIFIER and IFCSHAPEREPRESENTATION.REPRESENTATIONTYPE
	 */
	public static IFCShapeRepresentationIdentity identifyShapeRepresentation(EntityInstance shapeRepresentation){
		IFCShapeRepresentationIdentity rep = new IFCShapeRepresentationIdentity();
		String identifier = prepareRepresentationAttribute(shapeRepresentation.getAttributeValueBN("RepresentationIdentifier").toString());
		String type = prepareRepresentationAttribute(shapeRepresentation.getAttributeValueBN("RepresentationType").toString());

		// set representationObjectId
		rep.setRepresentationObjectEntity(shapeRepresentation);

		// TODO the performance of iterating thru every value might needs to be improved!
		// get identifier
		for (RepresentationIdentifier i : RepresentationIdentifier.values()) {
			for(String ident : getIdentifierTags(i.name())) {
				if(identifier.equals(ident)) {
			    	rep.setIdentifier(i);
			    	break;
			    }
			}
		}
		// get type
		for (RepresentationType t : RepresentationType.values()) {
			for(String typ : getIdentifierTags(t.name())) {
				if(type.equals(typ)) {
			    	rep.setType(t);
			    	break;
			    }
			}
		}
		return rep;
	}

	/**
	 * Gets the type of specific IFCSHAPEREPRESENTATION.ITEM. If item type is not supported for IFCSHAPEREPRESENTATION.REPRESENTATIONTYPE
	 * method will return null.
	 * @param ifcModel ifcModel
	 * @param ident IFCREPRESENTATIONTYPEOBJECT object
	 * @param item to get the representation type for (IFCSHAPEREPRESENTATION.ITEM packed into EntityInstance object)
	 * @return String with IFCSHAPEREPRESENTATION.ITEM type definition or null if not allowed in standard
	 */
	public static String getRepresentationItemType(ModelPopulation ifcModel, IFCShapeRepresentationIdentity ident, EntityInstance item) {

		if(ident.getType().equals(RepresentationType.AdvancedBrep)) {
			// here IFC standard only allows IFCADVANCEDBREP and IFCFACETEDBREP as item

			ArrayList<EntityInstance> ifcAdvancedBrep = new ArrayList<>();
			for(String flag : getIdentifierTags(AdvancedBrepRepresentationTypeItems.IfcAdvancedBrep.name())) {
				ifcAdvancedBrep.addAll(ifcModel.getInstancesOfType(flag));
			}
			if(ifcAdvancedBrep.contains(item))	return AdvancedBrepRepresentationTypeItems.IfcAdvancedBrep.name();

			ArrayList<EntityInstance> ifcFacetedBrep = new ArrayList<>();
			for(String flag : getIdentifierTags(AdvancedBrepRepresentationTypeItems.IfcFacetedBrep.name())) {
				ifcFacetedBrep.addAll(ifcModel.getInstancesOfType(flag));
			}
			if(ifcFacetedBrep.contains(item))	return AdvancedBrepRepresentationTypeItems.IfcFacetedBrep.name();

			Logging.info(IFCShapeRepresentationIdentifier.class.getName() + ": " + item.getEntityDefinition() + " is not supported");
			return null;
		}

		if(ident.getType().equals(RepresentationType.AdvancedSweptSolid)) {
			// here IFC standard only allows IFCSWEPTDISKSOLID and IFCSWEPTDISKSOLIDPOLYGON as item

			ArrayList<EntityInstance>  ifcSweptDiskSolids = new ArrayList<>();
			for(String flag : getIdentifierTags(AdvancedSweptSolidRepresentationTypeItems.IfcSweptDiskSolid.name())) {
				ifcSweptDiskSolids.addAll(ifcModel.getInstancesOfType(flag));
			}
			if(ifcSweptDiskSolids.contains(item))			return AdvancedSweptSolidRepresentationTypeItems.IfcSweptDiskSolid.name();

			ArrayList<EntityInstance> IfcSweptDiskSolidPolygonals = new ArrayList<>();
			for(String flag : getIdentifierTags(AdvancedSweptSolidRepresentationTypeItems.IfcSweptDiskSolidPolygonal.name())) {
				IfcSweptDiskSolidPolygonals.addAll(ifcModel.getInstancesOfType(flag));
			}
			if(IfcSweptDiskSolidPolygonals.contains(item))	return AdvancedSweptSolidRepresentationTypeItems.IfcSweptDiskSolidPolygonal.name();

			Logging.info(IFCShapeRepresentationIdentifier.class.getName() + ": " + item.getEntityDefinition() + " is not supported");
			return null;
		}

		if(ident.getType().equals(RepresentationType.Brep)) {
			// here IFC standard only allows IFCFACETEDBREP as item

			for(String flag : getIdentifierTags(BrepRepresentationTypeItems.IfcFacetedBrep.name())) {
				if(ifcModel.getInstancesOfType(flag).contains(item)){
					return BrepRepresentationTypeItems.IfcFacetedBrep.name();
				}
			}
			Logging.info(IFCShapeRepresentationIdentifier.class.getName() + ": " + item.getEntityDefinition() + " is not supported");
			return null;
		}

		if(ident.getType().equals(RepresentationType.CSG)) {
			// here IFC standard only allows IFCCSGSOLID, IFCBOOLEANRESULT and IFCPRIMITIVE3D as items

			ArrayList<EntityInstance> ifcBooleanResults = new ArrayList<>();
			for(String flag : getIdentifierTags(CSGRepresentationTypeItems.IfcBooleanResult.name())) {
				ifcBooleanResults.addAll(ifcModel.getInstancesOfType(flag));
			}
			if(ifcBooleanResults.contains(item))	return CSGRepresentationTypeItems.IfcBooleanResult.name();

			ArrayList<EntityInstance> ifcCSGSolids = new ArrayList<>();
			for(String flag : getIdentifierTags(CSGRepresentationTypeItems.IfcCsgSolid.name())) {
				ifcCSGSolids.addAll(ifcModel.getInstancesOfType(flag));
			}
			if(ifcCSGSolids.contains(item))			return CSGRepresentationTypeItems.IfcCsgSolid.name();

			ArrayList<EntityInstance> ifcPrimitive3Ds = new ArrayList<>();
			for(String flag : getIdentifierTags(CSGRepresentationTypeItems.IfcPrimitive3D.name())) {
				ifcPrimitive3Ds.addAll(ifcModel.getInstancesOfType(flag));
			}
			if(ifcPrimitive3Ds.contains(item))		return CSGRepresentationTypeItems.IfcPrimitive3D.name();

			Logging.info(IFCShapeRepresentationIdentifier.class.getName() + ": " + item.getEntityDefinition() + " is not supported");
			return null;
		}

		if(ident.getType().equals(RepresentationType.Tessellation)) {
			// here IFC standard only allows IFCTESSELATEDFACESET as item

			for(String flag : getIdentifierTags(TessellationRepresentationTypeItems.IfcTessellatedFaceSet.name())) {
				if(ifcModel.getInstancesOfType(flag).contains(item)){
					return TessellationRepresentationTypeItems.IfcTessellatedFaceSet.name();
				}
			}
			Logging.info(IFCShapeRepresentationIdentifier.class.getName() + ": " + item.getEntityDefinition() + " is not supported");
			return null;
		}

		if(ident.getType().equals(RepresentationType.Clipping)) {
			// here IFC standard only allows IFCBOOLEANCLIPPINGRESULT as item

			for(String flag : getIdentifierTags(ClippingRepresentationTypeItems.IfcBooleanClippingResult.name())) {
				if(ifcModel.getInstancesOfType(flag).contains(item)){
					return ClippingRepresentationTypeItems.IfcBooleanClippingResult.name();
				}
			}
			Logging.info(IFCShapeRepresentationIdentifier.class.getName() + ": " + item.getEntityDefinition() + " is not supported");
			return null;
		}

		if(ident.getType().equals(RepresentationType.Curve2D) || ident.getType().equals(RepresentationType.Curve3D)) {
			// here IFC standard only allows IFCBOUNDEDCURVE as item

			ArrayList<EntityInstance> ifcBoundedCurves = new ArrayList<>();
			for(String flag : getIdentifierTags(CurveRepresentationTypeItems.IfcBoundedCurve.name())) {
				ifcBoundedCurves.addAll(ifcModel.getInstancesOfType(flag));
			}
			if(ifcBoundedCurves.contains(item))		return CurveRepresentationTypeItems.IfcBoundedCurve.name();

			ArrayList<EntityInstance> ifcPolylines = new ArrayList<>();
			for(String flag : getIdentifierTags(CurveRepresentationTypeItems.IfcPolyline.name())) {
				ifcPolylines.addAll(ifcModel.getInstancesOfType(flag));
			}
			if(ifcPolylines.contains(item))			return CurveRepresentationTypeItems.IfcPolyline.name();

			Logging.info(IFCShapeRepresentationIdentifier.class.getName() + ": " + item.getEntityDefinition() + " is not supported");
			return null;
		}

		if(ident.getType().equals(RepresentationType.SurfaceModel)) {
			// here IFC standard only allows IFCTESSELLATEDITEM, IFCSCHELLBASEDSURFACEMODEL and
			// IFCFACEBASEDSURFACEMODEL as item

			ArrayList<EntityInstance> ifcTessellatedItems = new ArrayList<>();
			for(String flag : getIdentifierTags(SurfaceModelRepresentationTypeItems.IfcTessellatedItem.name())) {
				ifcTessellatedItems.addAll(ifcModel.getInstancesOfType(flag));
			}
			if(ifcTessellatedItems.contains(item))			return SurfaceModelRepresentationTypeItems.IfcTessellatedItem.name();

			ArrayList<EntityInstance> ifcShellBasedSurfaceModels = new ArrayList<>();
			for(String flag : getIdentifierTags(SurfaceModelRepresentationTypeItems.IfcShellBasedSurfaceModel.name())) {
				ifcShellBasedSurfaceModels.addAll(ifcModel.getInstancesOfType(flag));
			}
			if(ifcShellBasedSurfaceModels.contains(item))	return SurfaceModelRepresentationTypeItems.IfcShellBasedSurfaceModel.name();

			ArrayList<EntityInstance> ifcFaceBasedSurfaceModels = new ArrayList<>();
			for(String flag : getIdentifierTags(SurfaceModelRepresentationTypeItems.IfcFaceBasedSurfaceModel.name())) {
				ifcFaceBasedSurfaceModels.addAll(ifcModel.getInstancesOfType(flag));
			}
			if(ifcFaceBasedSurfaceModels.contains(item))	return SurfaceModelRepresentationTypeItems.IfcFaceBasedSurfaceModel.name();

			ArrayList<EntityInstance> ifcFacetedBreps = new ArrayList<>();
			for(String flag : getIdentifierTags(SurfaceModelRepresentationTypeItems.IfcFacetedBrep.name())) {
				ifcFacetedBreps.addAll(ifcModel.getInstancesOfType(flag));
			}
			if(ifcFacetedBreps.contains(item))				return SurfaceModelRepresentationTypeItems.IfcFacetedBrep.name();

			Logging.info(IFCShapeRepresentationIdentifier.class.getName() + ": " + item.getEntityDefinition() + " is not supported");
			return null;
		}

		if(ident.getType().equals(RepresentationType.SweptSolid)) {
			// here IFC standard only allows IFCEXTRUDEDAREASOLID, IFCREVOLVEDAREASOLID as item

			ArrayList<EntityInstance> ifcExtrudedAreaSolid = new ArrayList<>();
			for(String flag : getIdentifierTags(SweptSolidRepresentationTypeItems.IfcExtrudedAreaSolid.name())) {
				ifcExtrudedAreaSolid.addAll(ifcModel.getInstancesOfType(flag));
			}
			if(ifcExtrudedAreaSolid.contains(item))		return SweptSolidRepresentationTypeItems.IfcExtrudedAreaSolid.name();

			ArrayList<EntityInstance> ifcRevolvedAreaSolid = new ArrayList<>();
			for(String flag : getIdentifierTags(SweptSolidRepresentationTypeItems.IfcRevolvedAreaSolid.name())) {
				ifcRevolvedAreaSolid.addAll(ifcModel.getInstancesOfType(flag));
			}
			if(ifcRevolvedAreaSolid.contains(item))		return SweptSolidRepresentationTypeItems.IfcRevolvedAreaSolid.name();

			Logging.info(IFCShapeRepresentationIdentifier.class.getName() + ": " + item.getEntityDefinition() + " is not supported");
			return null;
		}

		if(ident.getType().equals(RepresentationType.BoundingBox)) {
			// here IFC standard only allows IFCBOUNDINGBOX as item

			for(String flag : getIdentifierTags(BoundingBoxRepresentationTypeItems.IfcBoundingBox.name())) {
				if(ifcModel.getInstancesOfType(flag).contains(item)){
					return BoundingBoxRepresentationTypeItems.IfcBoundingBox.name();
				}
			}
			Logging.info(IFCShapeRepresentationIdentifier.class.getName() + ": " + item.getEntityDefinition() + " is not supported");
			return null;
		}

		if(ident.getType().equals(RepresentationType.MappedRepresentation)) {
			// here IFC standard only allows IFCMAPPEDITEM as item

			for(String flag : getIdentifierTags(MappedRepresentatiobTypeItems.IfcMappedItem.name())) {
				if(ifcModel.getInstancesOfType(flag).contains(item)){
					return MappedRepresentatiobTypeItems.IfcMappedItem.name();
				}
			}
			Logging.info(IFCShapeRepresentationIdentifier.class.getName() + ": " + item.getEntityDefinition() + " is not supported");
			return null;
		}

		Logging.info(IFCShapeRepresentationIdentifier.class.getName() + ": " + ident.getType() + " RepresentationType is not supported");
		return null;
	}


	/**
	 * Gets the type of IFCLOOP. Types can be IFCEDGELOOP, IFCPOLYLOOP, IFCVERTEXLOOP
	 * @param ifcModel ifcModel
	 * @param loop to get type of
	 * @return type as string
	 */
	public static String getIFCLoopType(ModelPopulation ifcModel, EntityInstance loop) {
		// here IFC standard only allows IFCEDGELOOP, IFCPOLYLOOP, IFCVERTEXLOOP as item

		ArrayList<EntityInstance> edgeLoops = new ArrayList<>();
		for(String flag : getIdentifierTags(LoopSubRepresentationTypeItems.IfcEdgeLoop.name())) {
			edgeLoops.addAll(ifcModel.getInstancesOfType(flag));
		}
		if(edgeLoops.contains(loop))		return LoopSubRepresentationTypeItems.IfcEdgeLoop.name();

		ArrayList<EntityInstance> polyLoops = new ArrayList<>();
		for(String flag : getIdentifierTags(LoopSubRepresentationTypeItems.IfcPolyLoop.name())) {
			polyLoops.addAll(ifcModel.getInstancesOfType(flag));
		}
		if(polyLoops.contains(loop))		return LoopSubRepresentationTypeItems.IfcPolyLoop.name();

		ArrayList<EntityInstance> vertexLoops = new ArrayList<>();
		for(String flag : getIdentifierTags(LoopSubRepresentationTypeItems.IfcVertexLoop.name())) {
			vertexLoops.addAll(ifcModel.getInstancesOfType(flag));
		}
		if(vertexLoops.contains(loop))		return LoopSubRepresentationTypeItems.IfcVertexLoop.name();

		Logging.info(IFCShapeRepresentationIdentifier.class.getName() + ": " + loop.getEntityDefinition() + " LoopRepresentationType is not supported");
		return null;
	}

	/**
	 * Gets the type of IFCPROFILEDEF.
	 * @param ifcModel ifcModel
	 * @param profileDef to get type of
	 * @return type as string
	 */
	public static String getIFCProfileDefType(ModelPopulation ifcModel, EntityInstance profileDef) {
		// here IFC standard only allows IFCRECTANGLEPROFILEDEF, IFCTRAPEZIUMPROFILEDEF, IFCCIRCLEPROFILEDEF,
		// IFCELLIPSEPROFILEDEF, IFCSHAPEPROFILEDEF as item

		ArrayList<EntityInstance> rectanglePD = new ArrayList<>();
		for(String flag : getIdentifierTags(ProfileDefRepresentationTypeItems.IfcRectangleProfileDef.name())) {
			rectanglePD.addAll(ifcModel.getInstancesOfType(flag));
		}

		if(rectanglePD.contains(profileDef))		return ProfileDefRepresentationTypeItems.IfcRectangleProfileDef.name();

		ArrayList<EntityInstance> trapeziumPD = new ArrayList<>();
		for(String flag : getIdentifierTags(ProfileDefRepresentationTypeItems.IfcTrapeziumProfileDef.name())) {
			trapeziumPD.addAll(ifcModel.getInstancesOfType(flag));
		}
		if(trapeziumPD.contains(profileDef))		return ProfileDefRepresentationTypeItems.IfcTrapeziumProfileDef.name();

		ArrayList<EntityInstance> circlePD = new ArrayList<>();
		for(String flag : getIdentifierTags(ProfileDefRepresentationTypeItems.IfcCircleProfileDef.name())) {
			circlePD.addAll(ifcModel.getInstancesOfType(flag));
		}
		if(circlePD.contains(profileDef))		return ProfileDefRepresentationTypeItems.IfcCircleProfileDef.name();

		ArrayList<EntityInstance> ellipsePD = new ArrayList<>();
		for(String flag : getIdentifierTags(ProfileDefRepresentationTypeItems.IfcEllipseProfileDef.name())) {
			ellipsePD.addAll(ifcModel.getInstancesOfType(flag));
		}
		if(ellipsePD.contains(profileDef))		return ProfileDefRepresentationTypeItems.IfcEllipseProfileDef.name();

		ArrayList<EntityInstance> shapePD = new ArrayList<>();
		for(String flag : getIdentifierTags(ProfileDefRepresentationTypeItems.IfcShapeProfileDef.name())) {
			shapePD.addAll(ifcModel.getInstancesOfType(flag));
		}
		if(shapePD.contains(profileDef))		return ProfileDefRepresentationTypeItems.IfcShapeProfileDef.name();

		ArrayList<EntityInstance> arbitraryPD = new ArrayList<>();
		for(String flag : getIdentifierTags(ProfileDefRepresentationTypeItems.IfcArbitraryClosedProfileDef.name())) {
			arbitraryPD.addAll(ifcModel.getInstancesOfType(flag));
		}
		if(arbitraryPD.contains(profileDef))		return ProfileDefRepresentationTypeItems.IfcArbitraryClosedProfileDef.name();

		Logging.info(IFCShapeRepresentationIdentifier.class.getName() + ": " + profileDef.getEntityDefinition() + " ProfileDefRepresentationType is not supported");
		return null;
	}

	/**
	 * Method gets type of IFCBOOLEANOPERAND
	 * @param ifcModel ifc model
	 * @param entity to get type of
	 * @return type of IFCBOOLEANOPERAND
	 */
	public static String getIfcBooleanOperandType(ModelPopulation ifcModel, EntityInstance entity) {
		// allowed types

		ArrayList<EntityInstance> extrudedAreas = new ArrayList<>();
		for(String flag : getIdentifierTags(IfcBooleanOperandType.IfcExtrudedAreaSolid.name())) {
			extrudedAreas.addAll(ifcModel.getInstancesOfType(flag));
		}
		if(extrudedAreas.contains(entity))	return IfcBooleanOperandType.IfcExtrudedAreaSolid.name();

		ArrayList<EntityInstance> facetedBreps = new ArrayList<>();
		for(String flag : getIdentifierTags(IfcBooleanOperandType.IfcFacetedBrep.name())) {
			facetedBreps.addAll(ifcModel.getInstancesOfType(flag));
		}
		if(facetedBreps.contains(entity))	return IfcBooleanOperandType.IfcFacetedBrep.name();

		ArrayList<EntityInstance> solidModels = new ArrayList<>();
		for(String flag : getIdentifierTags(IfcBooleanOperandType.IfcSolidModel.name())) {
			solidModels.addAll(ifcModel.getInstancesOfType(flag));
		}
		if(solidModels.contains(entity))	return IfcBooleanOperandType.IfcSolidModel.name();

		ArrayList<EntityInstance> csgSolid = new ArrayList<>();
		for(String flag : getIdentifierTags(IfcBooleanOperandType.IfcCsgSolid.name())) {
			csgSolid.addAll(ifcModel.getInstancesOfType(flag));
		}
		if(csgSolid.contains(entity))	return IfcBooleanOperandType.IfcCsgSolid.name();

		ArrayList<EntityInstance> manifoldSolid = new ArrayList<>();
		for(String flag : getIdentifierTags(IfcBooleanOperandType.IfcManifoldSolidBrep.name())) {
			manifoldSolid.addAll(ifcModel.getInstancesOfType(flag));
		}
		if(manifoldSolid.contains(entity))	return IfcBooleanOperandType.IfcManifoldSolidBrep.name();

		ArrayList<EntityInstance> sweptAreaSolid = new ArrayList<>();
		for(String flag : getIdentifierTags(IfcBooleanOperandType.IfcSweptAreaSolid.name())) {
			sweptAreaSolid.addAll(ifcModel.getInstancesOfType(flag));
		}
		if(sweptAreaSolid.contains(entity))	return IfcBooleanOperandType.IfcSweptAreaSolid.name();

		ArrayList<EntityInstance> sweptDiskSolid = new ArrayList<>();
		for(String flag : getIdentifierTags(IfcBooleanOperandType.IfcSweptDiskSolid.name())) {
			sweptDiskSolid.addAll(ifcModel.getInstancesOfType(flag));
		}
		if(sweptDiskSolid.contains(entity))	return IfcBooleanOperandType.IfcSweptDiskSolid.name();

		ArrayList<EntityInstance> halfSpaceSolids = new ArrayList<>();
		for(String flag : getIdentifierTags(IfcBooleanOperandType.IfcHalfSpaceSolid.name())) {
			halfSpaceSolids.addAll(ifcModel.getInstancesOfType(flag));
		}
		if(halfSpaceSolids.contains(entity))	return IfcBooleanOperandType.IfcHalfSpaceSolid.name();

		ArrayList<EntityInstance> boxedHalfSpaceSolids = new ArrayList<>();
		for(String flag : getIdentifierTags(IfcBooleanOperandType.IfcBoxedHalfSpace.name())) {
			boxedHalfSpaceSolids.addAll(ifcModel.getInstancesOfType(flag));
		}
		if(boxedHalfSpaceSolids.contains(entity))	return IfcBooleanOperandType.IfcBoxedHalfSpace.name();

		ArrayList<EntityInstance> polygonBoundedHalfSpaces = new ArrayList<>();
		for(String flag : getIdentifierTags(IfcBooleanOperandType.IfcPolygonalBoundedHalfSpace.name())) {
			polygonBoundedHalfSpaces.addAll(ifcModel.getInstancesOfType(flag));
		}
		if(polygonBoundedHalfSpaces.contains(entity))	return IfcBooleanOperandType.IfcPolygonalBoundedHalfSpace.name();

		ArrayList<EntityInstance> booleanResults = new ArrayList<>();
		for(String flag : getIdentifierTags(IfcBooleanOperandType.IfcBooleanResult.name())) {
			booleanResults.addAll(ifcModel.getInstancesOfType(flag));
		}
		if(booleanResults.contains(entity))	return IfcBooleanOperandType.IfcBooleanResult.name();

		ArrayList<EntityInstance> clippingResult = new ArrayList<>();
		for(String flag : getIdentifierTags(IfcBooleanOperandType.IfcBooleanClippingResult.name())) {
			clippingResult.addAll(ifcModel.getInstancesOfType(flag));
		}
		if(clippingResult.contains(entity))	return IfcBooleanOperandType.IfcBooleanClippingResult.name();

		ArrayList<EntityInstance> csgPrimitive3Ds = new ArrayList<>();
		for(String flag : getIdentifierTags(IfcBooleanOperandType.IfcCsgPrimitive3D.name())) {
			csgPrimitive3Ds.addAll(ifcModel.getInstancesOfType(flag));
		}
		if(csgPrimitive3Ds.contains(entity))	return IfcBooleanOperandType.IfcCsgPrimitive3D.name();

		ArrayList<EntityInstance> blocks = new ArrayList<>();
		for(String flag : getIdentifierTags(IfcBooleanOperandType.IfcBlock.name())) {
			blocks.addAll(ifcModel.getInstancesOfType(flag));
		}
		if(blocks.contains(entity))	return IfcBooleanOperandType.IfcBlock.name();

		ArrayList<EntityInstance> rectPyramids = new ArrayList<>();
		for(String flag : getIdentifierTags(IfcBooleanOperandType.IfcRectangularPyramid.name())) {
			rectPyramids.addAll(ifcModel.getInstancesOfType(flag));
		}
		if(rectPyramids.contains(entity))	return IfcBooleanOperandType.IfcRectangularPyramid.name();

		ArrayList<EntityInstance> rightCircularCones = new ArrayList<>();
		for(String flag : getIdentifierTags(IfcBooleanOperandType.IfcRightCircularCone.name())) {
			rightCircularCones.addAll(ifcModel.getInstancesOfType(flag));
		}
		if(rightCircularCones.contains(entity))	return IfcBooleanOperandType.IfcRightCircularCone.name();

		ArrayList<EntityInstance> rightCircularCylinders = new ArrayList<>();
		for(String flag : getIdentifierTags(IfcBooleanOperandType.IfcRightCircularCylinder.name())) {
			rightCircularCylinders.addAll(ifcModel.getInstancesOfType(flag));
		}
		if(rightCircularCylinders.contains(entity))	return IfcBooleanOperandType.IfcRightCircularCylinder.name();

		ArrayList<EntityInstance> spheres = new ArrayList<>();
		for(String flag : getIdentifierTags(IfcBooleanOperandType.IfcSphere.name())) {
			spheres.addAll(ifcModel.getInstancesOfType(flag));
		}
		if(spheres.contains(entity))	return IfcBooleanOperandType.IfcSphere.name();

		Logging.info(IFCShapeRepresentationIdentifier.class.getName() + ": " + entity.getEntityDefinition() + " is not supported as IfcBooleanOperand");
		return null;
	}

	/**
	 * Method gets type of IFCBOUNDEDCURVE
	 * @param ifcModel ifc model
	 * @param entity to get type of
	 * @return type of IFCBOUNDEDCURVE
	 */
	public static String getIfcBoundedCurveType(ModelPopulation ifcModel, EntityInstance entity) {
		// here IFCCOMPOSITECURVE, IFCPOLYLINE, IFCTRIMMEDCURVE, IFCBSPLINECURVE allowed

		ArrayList<EntityInstance> compositeCurves = new ArrayList<>();
		for(String flag : getIdentifierTags(IfcBoundedCurveTypes.IfcCompositeCurve.name())) {
			compositeCurves.addAll(ifcModel.getInstancesOfType(flag));
		}
		if(compositeCurves.contains(entity))	return IfcBoundedCurveTypes.IfcCompositeCurve.name();

		ArrayList<EntityInstance> polylines = new ArrayList<>();
		for(String flag : getIdentifierTags(IfcBoundedCurveTypes.IfcPolyline.name())) {
			polylines.addAll(ifcModel.getInstancesOfType(flag));
		}
		if(polylines.contains(entity))	return IfcBoundedCurveTypes.IfcPolyline.name();

		ArrayList<EntityInstance> trimmedCurves = new ArrayList<>();
		for(String flag : getIdentifierTags(IfcBoundedCurveTypes.IfcTrimmedCurve.name())) {
			trimmedCurves.addAll(ifcModel.getInstancesOfType(flag));
		}
		if(trimmedCurves.contains(entity))	return IfcBoundedCurveTypes.IfcTrimmedCurve.name();

		ArrayList<EntityInstance> bSplineCurves = new ArrayList<>();
		for(String flag : getIdentifierTags(IfcBoundedCurveTypes.IfcBSplineCurve.name())) {
			bSplineCurves.addAll(ifcModel.getInstancesOfType(flag));
		}
		if(bSplineCurves.contains(entity))	return IfcBoundedCurveTypes.IfcBSplineCurve.name();

		Logging.info(IFCShapeRepresentationIdentifier.class.getName() + ": " + entity.getEntityDefinition() + " is not supported as IfcBoundedCurveType");
		return null;
	}

	/**
	 * Checks if entity is of type IFCPOLYLINE
	 * @param ifcModel ifc model
	 * @param entity to check type of
	 * @return true if IFCPOLYLINE, else false
	 */
	public static boolean isIfcPolyline(ModelPopulation ifcModel, EntityInstance entity) {
		ArrayList<EntityInstance> polylines = new ArrayList<>();
		for(String flag : getIdentifierTags(CurveRepresentationTypeItems.IfcPolyline.name())) {
			polylines.addAll(ifcModel.getInstancesOfType(flag));
		}
		return polylines.contains(entity);
	}

	/**
	 * Checks if entity is of type IFCAXIS2PLACEMENT3D
	 * @param ifcModel ifc model
	 * @param entity to check type of
	 * @return true if IFCAXIS2PLACEMENT3D, else false
	 */
	public static boolean isIfcAxis2Placement3D(ModelPopulation ifcModel, EntityInstance entity) {
		ArrayList<EntityInstance> axis2placement3Ds = new ArrayList<>();
		for(String flag : getIdentifierTags(Axis2PlacementRepresentationTypeItems.IfcAxis2Placement3D.name())) {
			axis2placement3Ds.addAll(ifcModel.getInstancesOfType(flag));
		}
		return axis2placement3Ds.contains(entity);
	}

	/**
	 * Checks if entity is of type IFCOPENINGELEMENT
	 * @param ifcModel ifc model
	 * @param entity to check type of
	 * @return true if IFCOPENINGELEMENT, else false
	 */
	public static boolean isIfcOpeningElement(ModelPopulation ifcModel, EntityInstance entity) {
		ArrayList<EntityInstance> openingElement = new ArrayList<>();
		for(String flag : getIdentifierTags(IfcRelVoidsElementTypes.IfcOpeningElement.name())) {
			openingElement.addAll(ifcModel.getInstancesOfType(flag));
		}
		return openingElement.contains(entity);
	}

	/**
	 * Checks if entity is of type IFCSLAB
	 * @param ifcModel ifc model
	 * @param entity to check type of
	 * @return true if IFCSLAB, else false
	 */
	public static boolean isIfcSlab(ModelPopulation ifcModel, EntityInstance entity) {
		ArrayList<EntityInstance> slabElements = new ArrayList<>();
		for(String flag : getIdentifierTags(BIMObject.IfcSlab.name())) {
			slabElements.addAll(ifcModel.getInstancesOfType(flag));
		}
		return slabElements.contains(entity);
	}

	/**
	 * Checks if entity is of type IFCDOOR
	 * @param ifcModel ifc model
	 * @param entity to check type of
	 * @return true if IFCDOOR, else false
	 */
	public static boolean isIfcDoor(ModelPopulation ifcModel, EntityInstance entity) {
		ArrayList<EntityInstance> doorElements = new ArrayList<>();
		for(String flag : getIdentifierTags(BIMObject.IfcDoor.name())) {
			doorElements.addAll(ifcModel.getInstancesOfType(flag));
		}
		return doorElements.contains(entity);
	}

	/**
	 * Checks if entity is of type IFCWINDOW
	 * @param ifcModel ifc model
	 * @param entity to check type of
	 * @return true if IFCWINDOW, else false
	 */
	public static boolean isIfcWindow(ModelPopulation ifcModel, EntityInstance entity) {
		ArrayList<EntityInstance> windowElements = new ArrayList<>();
		for(String flag : getIdentifierTags(BIMObject.IfcWindow.name())) {
			windowElements.addAll(ifcModel.getInstancesOfType(flag));
		}
		return windowElements.contains(entity);
	}

	public static boolean isIfcWindowOrIfcWall(ModelPopulation ifcModel, EntityInstance entity) {
		if(!isIfcWindow(ifcModel, entity) && !isIfcDoor(ifcModel, entity)) return false;
		return true;
	}

	/**
	 * Checks if entity is part of IFCRELVOIDSELEMENT, if yes than returns IFCRELVOIDSELEMENT, else null
	 * @param ifcModel ifc model
	 * @param entity to get IFCRELVOIDSELEMENT for
	 * @return if entity part of an IFCRELVOIDSELEMENT the EntityInstance if IFCRELVOIDSELEMENT, else null
	 */
	public static EntityInstance getRelVoidsElementOfEntity(ModelPopulation ifcModel, EntityInstance entity) {
		ArrayList<EntityInstance> relVoidsElements = new ArrayList<>();
		for(String flag : getIdentifierTags(BIMObject.IfcRelVoidsElement.name())) {
			relVoidsElements.addAll(ifcModel.getInstancesOfType(flag));
		}
		for(EntityInstance relVoidsElement : relVoidsElements) {
			int relatingBuildingElementId = relVoidsElement.getAttributeValueBNasEntityInstance("RelatingBuildingElement").getId();
			if(relatingBuildingElementId == entity.getId())	return relVoidsElement;
		}
		return null;
	}

	/**
	 * Removes unnecessary chars from representation attribute string
	 * @param attribute representation attribute (REPRESENTATIONIDENTIFIER, REPRESENTATIONTYPE)
	 * @return prepared string
	 */
	private static String prepareRepresentationAttribute(String attribute) {
		return attribute.substring(1, attribute.length()-1);
	}

	/**
	 * Returns array of strings including identifier in origin version, uppercase and lowercase
	 * @param identifier string
	 * @return array with versions of identifier
	 */
	private static ArrayList<String> getIdentifierTags(String identifier){
		ArrayList<String> idents = new ArrayList<>();
		idents.add(identifier);
		idents.add(identifier.toLowerCase());
		idents.add(identifier.toUpperCase());
		return idents;
	}

}

