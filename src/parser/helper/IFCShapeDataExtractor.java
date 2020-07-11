// License: GPL. For details, see LICENSE file.
package parser.helper;

import java.util.ArrayList;
import java.util.Vector;

import org.openstreetmap.josm.tools.Logging;

import nl.tue.buildingsmart.express.population.EntityInstance;
import nl.tue.buildingsmart.express.population.ModelPopulation;
import parser.data.Point3D;
import parser.data.ifc.IFCShapeRepresentationIdentity;
import parser.helper.IFCShapeRepresentationCatalog.AdvancedBrepRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.AdvancedSweptSolidRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.BoundingBoxRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.BrepRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.CSGRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.ClippingRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.CurveRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.MappedRepresentatiobTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.ProfileDefRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.SurfaceModelRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.SweptSolidRepresentationTypeItems;
import parser.helper.IFCShapeRepresentationCatalog.TessellationRepresentationTypeItems;

/**
 * Class helps parsing BIM data with providing methods to extract OSM relevant data
 * @author rebsc
 *
 */
public class IFCShapeDataExtractor {

	/**
	 * Extract representation data from IFCREPRESENTATIONITEM body
	 * @param ifcModel ifc Model
	 * @param bodyRepresentation representation of body
	 * @return List of points representing object shape or null if object type not supported
	 */
	public static ArrayList<Point3D> getDataFromBodyRepresentation(ModelPopulation ifcModel, IFCShapeRepresentationIdentity bodyRepresentation) {
		ArrayList<Point3D> shapeRep = new ArrayList<>();

		// get IFCOBJECT and REPRESENTATIONIDENTIFIER
		EntityInstance repObject = bodyRepresentation.getRepresentationObjectEntity();

		// get IFCREPRESENTATIONITEMS
		ArrayList<EntityInstance> bodyItems = repObject.getAttributeValueBNasEntityInstanceList("Items");

		// extract informations from IfcRepresentationItems
		for(EntityInstance item : bodyItems) {
			// get type of item
			String repItemType = IFCShapeRepresentationIdentifier.getRepresentationItemType(ifcModel, bodyRepresentation, item);
			if(repItemType == null) return null;

			// handle types
			if(repItemType.equals(AdvancedBrepRepresentationTypeItems.IfcAdvancedBrep.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(AdvancedBrepRepresentationTypeItems.IfcFacetedBrep.name())) {
				return getDataFromIfcFacetedBrep(ifcModel, item);
			}
			else if(repItemType.equals(AdvancedSweptSolidRepresentationTypeItems.IfcSweptDiskSolid.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(AdvancedSweptSolidRepresentationTypeItems.IfcSweptDiskSolidPolygonal.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(BrepRepresentationTypeItems.IfcFacetedBrep.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(CSGRepresentationTypeItems.IfcBooleanResult.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(CSGRepresentationTypeItems.IfcCsgSolid.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(CSGRepresentationTypeItems.IfcPrimitive3D.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(TessellationRepresentationTypeItems.IfcTessellatedFaceSet.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(ClippingRepresentationTypeItems.IfcBooleanClippingResult.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(SurfaceModelRepresentationTypeItems.IfcTessellatedItem.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(SurfaceModelRepresentationTypeItems.IfcShellBasedSurfaceModel.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(SurfaceModelRepresentationTypeItems.IfcFaceBasedSurfaceModel.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(SurfaceModelRepresentationTypeItems.IfcFacetedBrep.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(SweptSolidRepresentationTypeItems.IfcExtrudedAreaSolid.name())) {
				return getDataFromIfcExtrudedAreaSolid(ifcModel, item);
			}
			else if(repItemType.equals(SweptSolidRepresentationTypeItems.IfcRevolvedAreaSolid.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(MappedRepresentatiobTypeItems.IfcMappedItem.name())) {
				// TODO extract data
			}
		}

		return shapeRep;
	}

	/**
	 * Extract representation data from IfcRepresentationItem box
	 * @param ifcModel ifc Model
	 * @param boxRepresentation representation of box
	 * @return List of points representing object shape or null if object type not supported
	 */
	public static ArrayList<Point3D> getDataFromBoxRepresentation(ModelPopulation ifcModel, IFCShapeRepresentationIdentity boxRepresentation) {
		ArrayList<Point3D> shapeRep = new ArrayList<>();

		// get IFCObject and RepresentationIdentifier
		EntityInstance repObject = boxRepresentation.getRepresentationObjectEntity();

		// get IfcRepresentationItems
		ArrayList<EntityInstance> boxItems = repObject.getAttributeValueBNasEntityInstanceList("Items");

		// extract informations from IfcRepresentationItems
		for(EntityInstance item : boxItems) {
			// get type of IfcRepresentationItem
			String repItemType = IFCShapeRepresentationIdentifier.getRepresentationItemType(ifcModel, boxRepresentation, item);
			if(repItemType == null) return null;

			if(repItemType.equals(BoundingBoxRepresentationTypeItems.IfcBoundingBox.name())) {
				// TODO extract data
			}
		}

		return shapeRep;
	}

	/**
	 * Extract representation data from IfcRepresentationItem axis
	 * @param ifcModel ifc Model
	 * @param axisRepresentation representation of axis
	 * @return List of points representing object shape or null if object type not supported
	 */
	static ArrayList<Point3D> getDataFromAxisRepresentation(ModelPopulation ifcModel, IFCShapeRepresentationIdentity axisRepresentation) {
		ArrayList<Point3D> shapeRep = new ArrayList<>();

		// get IFCObject and RepresentationIdentifier
		EntityInstance repObject = axisRepresentation.getRepresentationObjectEntity();

		// get IfcRepresentationItems of object
		ArrayList<EntityInstance> axisItems = repObject.getAttributeValueBNasEntityInstanceList("Items");

		// extract informations from IfcRepresentationItems
		for(EntityInstance item : axisItems) {
			// get type of IfcRepresentationItem
			String repItemType = IFCShapeRepresentationIdentifier.getRepresentationItemType(ifcModel, axisRepresentation, item);
			if(repItemType == null) return null;

			if(repItemType.equals(CurveRepresentationTypeItems.IfcBoundedCurve.name())) {
				// TODO extract data
			}
			else if(repItemType.equals(CurveRepresentationTypeItems.IfcPolyline.name())) {
				// TODO extract data
			}
		}

		return shapeRep;
	}

	/**
	 * Method extracts shape representation coordinates from IFCFACETEDBREP object
	 * @param ifcModel ifc model
	 * @param faceBrepItem to get shape representation coordinates for
	 * @return points representing shape of IFCFACETEDBREP
	 */
	private static ArrayList<Point3D> getDataFromIfcFacetedBrep(ModelPopulation ifcModel, EntityInstance faceBrepItem) {
		/*
		// get IFCCLOSEDSHELL stored in IFCFACETEDBREP.OUTER
		EntityInstance closedShell = faceBrepItem.getAttributeValueBNasEntityInstance("Outer");

		// get IFCFACEs of IFCCLOSEDSHELL
		ArrayList<EntityInstance> facesOfClosedShell = closedShell.getAttributeValueBNasEntityInstanceList("CfsFaces");

		// get IFCFACEBOUNDs of every IFCFACE
		ArrayList<EntityInstance> faceBoundsOfClosedShell = new ArrayList<>();
		facesOfClosedShell.forEach(face->{
			faceBoundsOfClosedShell.addAll(face.getAttributeValueBNasEntityInstanceList("Bounds"));
		});

		// get IFCLOOPs of every IFCFACEBOUND
		ArrayList<EntityInstance> loopsOfClosedShell = new ArrayList<>();
		faceBoundsOfClosedShell.forEach(bound->{
			loopsOfClosedShell.addAll(bound.getAttributeValueBNasEntityInstanceList("Bound"));
		});

		// get points of IFCLOOPS
		for(EntityInstance loop : loopsOfClosedShell) {
			// handle different loop types
			String loopType = IFCShapeRepresentationIdentifier.getIFCLoopType(ifcModel, loop);
			if(loopType == null)	return null;

			if(loopType.equals(LoopSubRepresentationTypeItems.IfcPolyLoop.name())) {
				// get all IFCCARTESIANPOINTs
				ArrayList<Point3D> cartesianPointsOfClosedShell = new ArrayList<>();

				for(EntityInstance cPoint : loop.getAttributeValueBNasEntityInstanceList("Polygon")){
					// get coordinates of point and turn it into Point3D
					Point3D cPointAsPoint3D	= IfcCartesianCoordinateToPoint3D(cPoint);
					if(cPointAsPoint3D == null)	return null;
					cartesianPointsOfClosedShell.add(cPointAsPoint3D);
				}
				// add last point again to close loop
				//cartesianPointsOfClosedShell.add(cartesianPointsOfClosedShell.get(0));

				return cartesianPointsOfClosedShell;
			}
			// other loop types are not supported right now
		}
		*/
		return null;
	}

	/**
	 * Method extracts shape representation coordinates from IFCEXTRUDEDAREASOLID object
	 * @param ifcModel ifc model
	 * @param extrudedArea to get shape representation for
	 * @return points representing shape of IFCEXTRUDEDAREASOLID
	 */
	private static ArrayList<Point3D> getDataFromIfcExtrudedAreaSolid(ModelPopulation ifcModel, EntityInstance extrudedArea) {
		// get POSITION attribute and extract local object origin coordinates
		EntityInstance axisPlacement = extrudedArea.getAttributeValueBNasEntityInstance("Position");
		EntityInstance locationPoint = axisPlacement.getAttributeValueBNasEntityInstance("Location");
		//object axis origin
		Point3D locationPoint3D = IfcCartesianCoordinateToPoint3D(locationPoint);
		if(locationPoint3D == null)	return null;

		// get IFCPROFILEDEF attribute
		EntityInstance profileDef = extrudedArea.getAttributeValueBNasEntityInstance("SweptArea");
		// handle different SweptArea types
		String sweptAreaType = IFCShapeRepresentationIdentifier.getIFCProfileDefType(ifcModel, profileDef);
		if(sweptAreaType == null)	return null;

		if(sweptAreaType.equals(ProfileDefRepresentationTypeItems.IfcRectangleProfileDef.name())) {
			// extract XDim, YDim
			double xDim = prepareDoubleString((String)profileDef.getAttributeValueBN("XDim"));
			double yDim = prepareDoubleString((String)profileDef.getAttributeValueBN("YDim"));
			double halfxDim = xDim/2.0;
			double halfyDim = yDim/2.0;

			// TODO handle rotation

			// get points of shape
			ArrayList<Point3D> cartesianPointsOfSArea = new ArrayList<>();
			cartesianPointsOfSArea.add(new Point3D(locationPoint3D.getX()-halfxDim, locationPoint3D.getY()-halfyDim, 0.0));
			cartesianPointsOfSArea.add(new Point3D(locationPoint3D.getX()+halfxDim, locationPoint3D.getY()-halfyDim, 0.0));
			cartesianPointsOfSArea.add(new Point3D(locationPoint3D.getX()+halfxDim, locationPoint3D.getY()+halfyDim, 0.0));
			cartesianPointsOfSArea.add(new Point3D(locationPoint3D.getX()-halfxDim, locationPoint3D.getY()+halfyDim, 0.0));
			cartesianPointsOfSArea.add(new Point3D(locationPoint3D.getX()-halfxDim, locationPoint3D.getY()-halfyDim, 0.0));
			return cartesianPointsOfSArea;
		}
		if(sweptAreaType.equals(ProfileDefRepresentationTypeItems.IfcArbitraryClosedProfileDef.name())) {
			String profileType = (String)profileDef.getAttributeValueBN("ProfileType");

			if(profileType.equals(".AREA.")) {
				// extract polyline coordinates
				EntityInstance outerCurve = profileDef.getAttributeValueBNasEntityInstance("OuterCurve");

				// check if curve is represented by polyloop
				if(IFCShapeRepresentationIdentifier.isIfcPolyline(ifcModel, outerCurve)) {
					// extract coordinates
					ArrayList<EntityInstance> curvePoints = outerCurve.getAttributeValueBNasEntityInstanceList("Points");
					ArrayList<Point3D> cartesianPointsOfSArea = new ArrayList<>();
					curvePoints.forEach(point ->{
						Point3D pointAsPoint3D = IfcCartesianCoordinateToPoint3D(point);
						cartesianPointsOfSArea.add(
								new Point3D(locationPoint3D.getX()+ pointAsPoint3D.getX(), locationPoint3D.getY() + pointAsPoint3D.getY(), 0.0));
					});
					return cartesianPointsOfSArea;
				}
			}
			if(profileType.equals(".CURVE.")) {
				// not supported right now
			}
		}
		// other types are not supported right now
		return null;
	}

	/**
	 * Transforms IFCCARTESIANCOORDINATE entity into Point3D
	 * @param cartesianCoordinate to transform
	 * @return coordinate as Point3D
	 */
	private static Point3D IfcCartesianCoordinateToPoint3D(EntityInstance cartesianCoordinate) {
		@SuppressWarnings("unchecked")
		Vector<String> objectCoords = (Vector<String>)cartesianCoordinate.getAttributeValueBN("Coordinates");
		if(objectCoords.isEmpty())	return null;
		double x = prepareDoubleString(objectCoords.get(0));
		double y = prepareDoubleString(objectCoords.get(1));
		double z = 0.0;
		if(objectCoords.size() == 3) 	prepareDoubleString(objectCoords.get(2));
		if(Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z)) {
			return null;
		}
		return new Point3D(x, y, z);
	}

	/**
	 * Parses string of double value from IFC file into proper double
	 * @param doubleString String of coordinate
	 * @return double representing double
	 */
	private static double prepareDoubleString(String doubleString) {
		if(doubleString.endsWith(".")) {
			doubleString = doubleString + "0";
		}
		try {
			return Double.parseDouble(doubleString);
		}catch(NumberFormatException e) {
			Logging.error(e.getMessage());
			return Double.NaN;
		}
	}


}
