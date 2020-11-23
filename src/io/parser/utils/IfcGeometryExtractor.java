// License: AGPL. For details, see LICENSE file.
package io.parser.utils;

import io.parser.data.ifc.IfcRepresentation;
import io.parser.data.ifc.IfcRepresentationCatalog.*;
import io.parser.math.Vector3D;
import nl.tue.buildingsmart.express.population.EntityInstance;
import nl.tue.buildingsmart.express.population.ModelPopulation;
import org.openstreetmap.josm.tools.Logging;

import java.util.ArrayList;
import java.util.List;

import static io.parser.utils.ParserUtility.prepareDoubleString;

/**
 * Class helps parsing BIM data with providing methods to extract OSM relevant data
 *
 * @author rebsc
 */
public class IfcGeometryExtractor {

    public static final Vector3D defaultPoint = new Vector3D(-99.0, -99.0, -99.0);

    /**
     * This type defines the three Boolean operators used in the definition of CSG solids.
     */
    private enum IfcBooleanOperator {
        UNION,
        INTERSECTION,
        DIFFERENCE
    }

    /**
     * Extract representation data from IFCREPRESENTATIONITEM body
     *
     * @param ifcModel           ifc Model
     * @param bodyRepresentation representation of body
     * @return List of points representing object shape or null if object type not supported
     */
    public static List<Vector3D> getDataFromBodyRepresentation(ModelPopulation ifcModel, IfcRepresentation bodyRepresentation) {
        ArrayList<Vector3D> shapeRep = new ArrayList<>();

        // get IFCOBJECT and REPRESENTATIONIDENTIFIER
        EntityInstance repObject = bodyRepresentation.getEntity();

        // get IFCREPRESENTATIONITEMS
        ArrayList<EntityInstance> bodyItems = repObject.getAttributeValueBNasEntityInstanceList("Items");

        // extract informations from IfcRepresentationItems
        for (EntityInstance item : bodyItems) {
            // get type of item
            String repItemType = IfcObjectIdentifier.getRepresentationItemType(ifcModel, bodyRepresentation, item);
            if (repItemType == null) return null;

            // handle types
            if (repItemType.equals(AdvancedBrepRepresentationTypeItems.IfcAdvancedBrep.name())) {
                // TODO extract data
                logUnsupportedRepresentationInfo(repItemType);
            } else if (repItemType.equals(AdvancedSweptSolidRepresentationTypeItems.IfcSweptDiskSolid.name())) {
                // TODO extract data
                logUnsupportedRepresentationInfo(repItemType);
            } else if (repItemType.equals(AdvancedSweptSolidRepresentationTypeItems.IfcSweptDiskSolidPolygonal.name())) {
                // TODO extract data
                logUnsupportedRepresentationInfo(repItemType);
            } else if (repItemType.equals(BrepRepresentationTypeItems.IfcFacetedBrep.name())) {
                ArrayList<Vector3D> shapeData = getIfcFacetedBrepGeometry(ifcModel, item);
                // check if entity includes(floor-)openings and handle them
                // shapeDataWithOpeningHandling will be null, if no openings exists or type of opening not supported
                ArrayList<Vector3D> shapeDataWithOpeningHandling = handleOpenings(ifcModel, shapeData, bodyRepresentation.getRootEntity());
                if (shapeDataWithOpeningHandling != null) shapeRep.addAll(shapeDataWithOpeningHandling);
                else if (shapeData != null) shapeRep.addAll(shapeData);
            } else if (repItemType.equals(CSGRepresentationTypeItems.IfcBooleanResult.name())) {
                String operator = item.getAttributeValueBN("Operator").toString();
                if (operator == null) return null;
                ArrayList<Vector3D> shapeData = null;
                if (operator.equals("." + IfcBooleanOperator.DIFFERENCE + ".")) {
                    shapeData = getIfcBooleanResultGeometry(ifcModel, item, IfcBooleanOperator.DIFFERENCE);
                } else if (operator.equals("." + IfcBooleanOperator.INTERSECTION + ".")) {
                    shapeData = getIfcBooleanResultGeometry(ifcModel, item, IfcBooleanOperator.INTERSECTION);
                } else if (operator.equals("." + IfcBooleanOperator.UNION + ".")) {
                    shapeData = getIfcBooleanResultGeometry(ifcModel, item, IfcBooleanOperator.UNION);
                }
                // check if entity includes(floor-)openings and handle them
                // shapeDataWithOpeningHandling will be null, if no openings exists or type of opening not supported
                ArrayList<Vector3D> shapeDataWithOpeningHandling = handleOpenings(ifcModel, shapeData, bodyRepresentation.getRootEntity());
                if (shapeDataWithOpeningHandling != null) shapeRep.addAll(shapeDataWithOpeningHandling);
                else if (shapeData != null) shapeRep.addAll(shapeData);
            } else if (repItemType.equals(CSGRepresentationTypeItems.IfcCsgSolid.name())) {
                // TODO extract data
                logUnsupportedRepresentationInfo(repItemType);
            } else if (repItemType.equals(CSGRepresentationTypeItems.IfcPrimitive3D.name())) {
                // TODO extract data
                logUnsupportedRepresentationInfo(repItemType);
            } else if (repItemType.equals(TessellationRepresentationTypeItems.IfcTessellatedFaceSet.name())) {
                // TODO extract data
                logUnsupportedRepresentationInfo(repItemType);
            } else if (repItemType.equals(ClippingRepresentationTypeItems.IfcBooleanClippingResult.name())) {
                ArrayList<Vector3D> shapeData = getIfcBooleanResultGeometry(ifcModel, item, IfcBooleanOperator.DIFFERENCE);
                // check if entity includes(floor-)openings and handle them
                // shapeDataWithOpeningHandling will be null, if no openings exists or type of opening not supported
                ArrayList<Vector3D> shapeDataWithOpeningHandling = handleOpenings(ifcModel, shapeData, bodyRepresentation.getRootEntity());
                if (shapeDataWithOpeningHandling != null) shapeRep.addAll(shapeDataWithOpeningHandling);
                else if (shapeData != null) shapeRep.addAll(shapeData);
            } else if (repItemType.equals(SurfaceModelRepresentationTypeItems.IfcTessellatedItem.name())) {
                // TODO extract data
                logUnsupportedRepresentationInfo(repItemType);
            } else if (repItemType.equals(SurfaceModelRepresentationTypeItems.IfcShellBasedSurfaceModel.name())) {
                // TODO extract data
                logUnsupportedRepresentationInfo(repItemType);
            } else if (repItemType.equals(SurfaceModelRepresentationTypeItems.IfcFaceBasedSurfaceModel.name())) {
                // TODO extract data
                logUnsupportedRepresentationInfo(repItemType);
            } else if (repItemType.equals(SweptSolidRepresentationTypeItems.IfcExtrudedAreaSolid.name())) {
                ArrayList<Vector3D> shapeData = getIfcExtrudedAreaSolidGeometry(ifcModel, item);
                // check if entity includes(floor-)openings and handle them
                // shapeDataWithOpeningHandling will be null, if no openings exists or type of opening not supported
                ArrayList<Vector3D> shapeDataWithOpeningHandling = handleOpenings(ifcModel, shapeData, bodyRepresentation.getRootEntity());
                if (shapeDataWithOpeningHandling != null) shapeRep.addAll(shapeDataWithOpeningHandling);
                else if (shapeData != null) shapeRep.addAll(shapeData);
            } else if (repItemType.equals(SweptSolidRepresentationTypeItems.IfcRevolvedAreaSolid.name())) {
                // TODO extract data
                logUnsupportedRepresentationInfo(repItemType);
            } else if (repItemType.equals(MappedRepresentatiobTypeItems.IfcMappedItem.name())) {
                // TODO extract data
                logUnsupportedRepresentationInfo(repItemType);
            } else {
                logUnsupportedRepresentationInfo(repItemType);
            }
        }

        return shapeRep;
    }

    /**
     * Extract representation data from IfcRepresentationItem box
     *
     * @param ifcModel          ifc Model
     * @param boxRepresentation representation of box
     * @return List of points representing object shape or null if object type not supported
     */
    public static List<Vector3D> getDataFromBoxRepresentation(ModelPopulation ifcModel, IfcRepresentation boxRepresentation) {
        ArrayList<Vector3D> shapeRep = new ArrayList<>();

        // get IFCObject and RepresentationIdentifier
        EntityInstance repObject = boxRepresentation.getEntity();

        // get IfcRepresentationItems
        ArrayList<EntityInstance> boxItems = repObject.getAttributeValueBNasEntityInstanceList("Items");

        // extract informations from IfcRepresentationItems
        for (EntityInstance item : boxItems) {
            // get type of IfcRepresentationItem
            String repItemType = IfcObjectIdentifier.getRepresentationItemType(ifcModel, boxRepresentation, item);
            if (repItemType == null) return null;

            if (repItemType.equals(BoundingBoxRepresentationTypeItems.IfcBoundingBox.name())) {
                // get cartesian point of bounding box
                EntityInstance cartesianCorner = item.getAttributeValueBNasEntityInstance("Corner");
                Vector3D cPointAsVector3D = ifcCoordinatesToVector3D(cartesianCorner);
                if (cPointAsVector3D == null) return null;
                double xDim = prepareDoubleString((String) item.getAttributeValueBN("XDim"));
                double yDim = prepareDoubleString((String) item.getAttributeValueBN("YDim"));
                // get points of shape
                ArrayList<Vector3D> cartesianPointsOfBB = new ArrayList<>();
                cartesianPointsOfBB.add(new Vector3D(cPointAsVector3D.getX(), cPointAsVector3D.getY(), cPointAsVector3D.getZ()));
                cartesianPointsOfBB.add(new Vector3D(cPointAsVector3D.getX() + xDim, cPointAsVector3D.getY(), cPointAsVector3D.getZ()));
                cartesianPointsOfBB.add(new Vector3D(cPointAsVector3D.getX() + xDim, cPointAsVector3D.getY() + yDim, cPointAsVector3D.getZ()));
                cartesianPointsOfBB.add(new Vector3D(cPointAsVector3D.getX(), cPointAsVector3D.getY() + yDim, cPointAsVector3D.getZ()));
                cartesianPointsOfBB.add(new Vector3D(cPointAsVector3D.getX(), cPointAsVector3D.getY(), cPointAsVector3D.getZ()));
                shapeRep.addAll(cartesianPointsOfBB);
            } else {
                logUnsupportedRepresentationInfo(repItemType);
            }
        }

        return shapeRep;
    }

    /**
     * Method extracts shape representation coordinates from IFCFACETEDBREP object
     *
     * @param ifcModel     ifc model
     * @param faceBrepItem to get shape representation coordinates for
     * @return points representing shape of IFCFACETEDBREP
     */
    private static ArrayList<Vector3D> getIfcFacetedBrepGeometry(ModelPopulation ifcModel, EntityInstance faceBrepItem) {
        // get IFCCLOSEDSHELL stored in IFCFACETEDBREP.OUTER
        EntityInstance closedShell = faceBrepItem.getAttributeValueBNasEntityInstance("Outer");
        return getIfcClosedShellGeometry(ifcModel, closedShell);
    }

    /**
     * Method extracts shape representation coordinates from IFCCLOSEDSHELL object
     *
     * @param ifcModel  ifc model
     * @param shellItem to get shape representation coordinates for
     * @return points representing shape of IFCCLOSEDSHELL
     */
    private static ArrayList<Vector3D> getIfcClosedShellGeometry(ModelPopulation ifcModel, EntityInstance shellItem) {
        // get IFCFACEs of IFCCLOSEDSHELL
        ArrayList<EntityInstance> facesOfClosedShell = shellItem.getAttributeValueBNasEntityInstanceList("CfsFaces");

        // get IFCFACEBOUNDs of every IFCFACE
        ArrayList<EntityInstance> faceBoundsOfClosedShell = new ArrayList<>();
        facesOfClosedShell.forEach(face -> faceBoundsOfClosedShell.addAll(face.getAttributeValueBNasEntityInstanceList("Bounds")));

        // get IFCLOOPs of every IFCFACEBOUND
        ArrayList<EntityInstance> loopsOfClosedShell = new ArrayList<>();
        faceBoundsOfClosedShell.forEach(bound -> loopsOfClosedShell.addAll(bound.getAttributeValueBNasEntityInstanceList("Bound")));

        // collect points of IFCLOOPs
        ArrayList<Vector3D> shapePoints = new ArrayList<>();
        for (EntityInstance loop : loopsOfClosedShell) {
            ArrayList<Vector3D> pointsOfLoop = getIfcLoopGeometry(ifcModel, loop);
            if (pointsOfLoop == null) return null;
            // workaround: Add points of every loop to shapePoints but also add a default point after each loop as separator (needed later on for rendering)
            shapePoints.addAll(pointsOfLoop);
            shapePoints.add(defaultPoint);
        }

        return shapePoints;
    }

    /**
     * Method extracts shape representation coordinates from IFCLOOP object
     *
     * @param ifcModel ifc model
     * @param loop     to get shape representation coordinates for
     * @return points representing shape of IFCLOOP
     */
    private static ArrayList<Vector3D> getIfcLoopGeometry(ModelPopulation ifcModel, EntityInstance loop) {
        // get loop type
        String loopType = IfcObjectIdentifier.getIFCLoopType(ifcModel, loop);
        if (loopType == null) return null;

        if (loopType.equals(LoopSubRepresentationTypeItems.IfcPolyLoop.name())) {
            // get all IFCCARTESIANPOINTs
            ArrayList<Vector3D> cartesianPointsOfClosedShell = new ArrayList<>();
            for (EntityInstance cPoint : loop.getAttributeValueBNasEntityInstanceList("Polygon")) {
                Vector3D cPointAsVector3D = ifcCoordinatesToVector3D(cPoint);
                if (cPointAsVector3D == null) return null;
                cartesianPointsOfClosedShell.add(cPointAsVector3D);
            }
            return cartesianPointsOfClosedShell;
        }

        // other loop types are not supported right now
        logUnsupportedRepresentationInfo(loopType);
        return null;
    }

    /**
     * Method extracts shape representation coordinates from IFCCURVE object
     *
     * @param ifcModel ifc model
     * @param curve    to get shape representation coordinates for
     * @return points representing shape of IFCCURVE
     */
    private static ArrayList<Vector3D> getIfcCurveGeometry(ModelPopulation ifcModel, EntityInstance curve) {
        if (IfcObjectIdentifier.isIfcPolyline(ifcModel, curve)) {
            return getIfcPolylineGeometry(curve);
        } else if (IfcObjectIdentifier.isIfcCompositeCurve(ifcModel, curve)) {
            return getIfcCompositeCurveGeometry(ifcModel, curve);
        } else if (IfcObjectIdentifier.isIfcTrimmedCurve(ifcModel, curve)) {
            // TODO implement proper; handle trim of basis curve
            EntityInstance basisCurve = curve.getAttributeValueBNasEntityInstance("BasisCurve");
            if (basisCurve == null) return null;
            return getIfcCurveGeometry(ifcModel, basisCurve);
        } else if (IfcObjectIdentifier.isIfcCircle(ifcModel, curve)) {
            // TODO implement
            logUnsupportedRepresentationInfo(IfcObjectIdentifier.getIfcCurveType(ifcModel, curve));
            return new ArrayList<>();
        } else {
            logUnsupportedRepresentationInfo(IfcObjectIdentifier.getIfcCurveType(ifcModel, curve));
        }
        return null;
    }

    /**
     * Method extracts local coordinates of polyline
     *
     * @param polyline o get coordinates from
     * @return coordinates of polyline (local)
     */
    private static ArrayList<Vector3D> getIfcPolylineGeometry(EntityInstance polyline) {
        ArrayList<EntityInstance> points = polyline.getAttributeValueBNasEntityInstanceList("Points");
        ArrayList<Vector3D> cartesianPointsOfSArea = new ArrayList<>();
        points.forEach(point -> {
            Vector3D pointAsVector3D = ifcCoordinatesToVector3D(point);
            assert pointAsVector3D != null;
            cartesianPointsOfSArea.add(new Vector3D(pointAsVector3D.getX(), pointAsVector3D.getY(), 0.0));
        });
        return cartesianPointsOfSArea;
    }

    /**
     * Method extracts local coordinates of ifcCompositeCurve
     *
     * @param ifcModel     ifc model
     * @param curveSegment to get coordinates from
     * @return Extracts coordinate data from IFCCOMPOSITECURVE
     */
    private static ArrayList<Vector3D> getIfcCompositeCurveGeometry(ModelPopulation ifcModel, EntityInstance curveSegment) {
        ArrayList<Vector3D> shapeData = new ArrayList<>();
        ArrayList<EntityInstance> curveSegments = curveSegment.getAttributeValueBNasEntityInstanceList("Segments");
        for (EntityInstance segment : curveSegments) {
            EntityInstance parentCurve = segment.getAttributeValueBNasEntityInstance("ParentCurve");
            ArrayList<Vector3D> parentCurveShape = getIfcCurveGeometry(ifcModel, parentCurve);
            if (parentCurveShape == null)
                return null;   // if one element null, return null to void wrong mapped elements
            shapeData.addAll(parentCurveShape);
        }
        if (shapeData.isEmpty()) return null;
        return shapeData;
    }

    /**
     * Extracts coordinate data from IFCBOOLEANRESULT. If IFCBOOLEANRESULT holds operands of type IFCBOOLEANRESULT it will
     * recursive run thru every operation.
     *
     * @param ifcModel     ifc model
     * @param resultEntity to get coordinates from
     * @param operator     IFCBOOLEANOPERATOR
     * @return Extracts coordinate data from IFCBOOLEANRESULT
     */
    private static ArrayList<Vector3D> getIfcBooleanResultGeometry(ModelPopulation ifcModel, EntityInstance resultEntity, IfcBooleanOperator operator) {
        // get and identify both operands
        EntityInstance operand1 = resultEntity.getAttributeValueBNasEntityInstance("FirstOperand");
        EntityInstance operand2 = resultEntity.getAttributeValueBNasEntityInstance("SecondOperand");

        // extract shape data from operands
        ArrayList<Vector3D> pointsOfOperand1 = getBooleanOperandGeometry(ifcModel, operand1);
        ArrayList<Vector3D> pointsOfOperand2 = getBooleanOperandGeometry(ifcModel, operand2);

        if (pointsOfOperand1 == null || pointsOfOperand2 == null) return pointsOfOperand1;

        // do operation
        if (operator.equals(IfcBooleanOperator.DIFFERENCE)) {
            ArrayList<Vector3D> pointsOfOperand1Copy = new ArrayList<>(pointsOfOperand1);
            for (Vector3D point1 : pointsOfOperand1) {
                for (Vector3D point2 : pointsOfOperand2) {
                    if (point1.equalsVector(point2)) {
                        pointsOfOperand1Copy.remove(point1);
                    }
                }
            }
            return pointsOfOperand1Copy;
        }
        if (operator.equals(IfcBooleanOperator.INTERSECTION)) {
            // TODO implement
            logUnsupportedRepresentationInfo(operator.name());
        }
        if (operator.equals(IfcBooleanOperator.UNION)) {
            // TODO implement
            logUnsupportedRepresentationInfo(operator.name());
        }

        logUnsupportedRepresentationInfo(operator.name());
        return null;
    }

    /**
     * Method extracts shape data from boolean operand. For this the operand will be identified and
     * handled dependent on type.
     *
     * @param ifcModel ifc model
     * @param operand  to get shape data from
     * @return points representing shape of operand
     */
    private static ArrayList<Vector3D> getBooleanOperandGeometry(ModelPopulation ifcModel, EntityInstance operand) {
        String operandType = IfcObjectIdentifier.getIfcBooleanOperandType(ifcModel, operand);

        if (operandType == null) return null;

        if (operandType.equals(IfcBooleanOperandType.IfcSolidModel.name())) {
            // TODO implement
            logUnsupportedRepresentationInfo(operandType);
        }
        if (operandType.equals(IfcBooleanOperandType.IfcHalfSpaceSolid.name())) {
            // TODO implement
            logUnsupportedRepresentationInfo(operandType);
        }
        if (operandType.equals(IfcBooleanOperandType.IfcPolygonalBoundedHalfSpace.name())) {
            return getIfcPolygonalBoundedHalfSpaceGeometry(ifcModel, operand);
        }
        if (operandType.equals(IfcBooleanOperandType.IfcBooleanResult.name()) || operandType.equals(IfcBooleanOperandType.IfcBooleanClippingResult.name())) {
            String operand1Operator = (String) operand.getAttributeValueBN("Operator");
            if (operand1Operator.equals("." + IfcBooleanOperator.DIFFERENCE + ".")) {
                return getIfcBooleanResultGeometry(ifcModel, operand, IfcBooleanOperator.DIFFERENCE);
            }
            if (operand1Operator.equals("." + IfcBooleanOperator.INTERSECTION + ".")) {
                return getIfcBooleanResultGeometry(ifcModel, operand, IfcBooleanOperator.INTERSECTION);
            }
            if (operand1Operator.equals("." + IfcBooleanOperator.UNION + ".")) {
                return getIfcBooleanResultGeometry(ifcModel, operand, IfcBooleanOperator.UNION);
            }
        }
        if (operandType.equals(IfcBooleanOperandType.IfcCsgPrimitive3D.name())) {
            // TODO implement
            logUnsupportedRepresentationInfo(operandType);
        }
        if (operandType.equals(SweptSolidRepresentationTypeItems.IfcExtrudedAreaSolid.name())) {
            return getIfcExtrudedAreaSolidGeometry(ifcModel, operand);
        }
        if (operandType.equals(IfcBooleanOperandType.IfcFacetedBrep.name())) {
            return getIfcFacetedBrepGeometry(ifcModel, operand);
        }

        // other types are not supported right now
        logUnsupportedRepresentationInfo(operandType);
        return null;
    }

    /**
     * Method gets result shape data from IFCFEATUREELEMENTSUBTRACTION operation
     *
     * @param masterElement    of operation (RelatingBuildingElement)
     * @param dependentElement of operation (RelatedOpeningElement)
     * @return result of IFCFEATUREELEMENTSUBTRACTION
     */
    private static ArrayList<Vector3D> getIfcFeatureElementSubtractionGeometry(ArrayList<Vector3D> masterElement, ArrayList<Vector3D> dependentElement) {
        ArrayList<Vector3D> masterElementsCopy = new ArrayList<>(masterElement);
        // first remove all points from master which are in both lists
        for (Vector3D masterPoint : masterElementsCopy) {
            dependentElement.forEach(dependentPoint -> {
                if (masterPoint.equalsVector(dependentPoint)) {
                    masterElement.remove(masterPoint);
                }
            });
        }
        // second add all points from dependent to master which are not already in master
        // for workaround add default point
        masterElement.add(defaultPoint);
        dependentElement.forEach(dependentPoint -> {
            if (!masterElement.contains(dependentPoint)) {
                masterElement.add(dependentPoint);
            }
        });

        return masterElement;
    }

    /**
     * Method extracts shape representation coordinates from IFCPOLYGONALBUNDEDHALFSPACE object
     *
     * @param ifcModel ifc model
     * @param polygon  object to get shape coordinates from
     * @return points representing shape of IFCPOLYGONALBUNDEDHALFSPACE
     */
    private static ArrayList<Vector3D> getIfcPolygonalBoundedHalfSpaceGeometry(ModelPopulation ifcModel, EntityInstance polygon) {
        // TODO rotation to parent system necessary?

        // get local origin position
        EntityInstance localSystemPosition = polygon.getAttributeValueBNasEntityInstance("Position");
        EntityInstance locationPoint = localSystemPosition.getAttributeValueBNasEntityInstance("Location");
        Vector3D locationVector3D = ifcCoordinatesToVector3D(locationPoint);
        if (locationVector3D == null) return null;

        // get boundary
        EntityInstance localPolygonBoundary = polygon.getAttributeValueBNasEntityInstance("PolygonalBoundary");

        // get coordinates of boundary
        if (localPolygonBoundary == null) return null;
        ArrayList<Vector3D> pointsOfPolygonBoundary = getIfcCurveGeometry(ifcModel, localPolygonBoundary);
        if (pointsOfPolygonBoundary != null) {
            pointsOfPolygonBoundary.forEach(point -> point = new Vector3D(locationVector3D.getX() + point.getX(), locationVector3D.getY() + point.getY(), 0.0));
            return pointsOfPolygonBoundary;
        }

        // other types are not supported right now
        logUnsupportedRepresentationInfo(IfcObjectIdentifier.getIfcCurveType(ifcModel, localPolygonBoundary));
        return null;
    }

    /**
     * Method extracts shape representation coordinates from IFCEXTRUDEDAREASOLID object
     *
     * @param ifcModel     ifc model
     * @param extrudedArea to get shape representation for
     * @return points representing shape of IFCEXTRUDEDAREASOLID
     */
    private static ArrayList<Vector3D> getIfcExtrudedAreaSolidGeometry(ModelPopulation ifcModel, EntityInstance extrudedArea) {
        // get POSITION attribute and extract local object origin coordinates
        EntityInstance axisPlacement = extrudedArea.getAttributeValueBNasEntityInstance("Position");
        EntityInstance locationPoint = axisPlacement.getAttributeValueBNasEntityInstance("Location");
        //object axis origin
        Vector3D locationVector3D = ifcCoordinatesToVector3D(locationPoint);
        if (locationVector3D == null) return null;

        // get IFCPROFILEDEF attribute
        EntityInstance profileDef = extrudedArea.getAttributeValueBNasEntityInstance("SweptArea");
        // handle different SweptArea types
        String sweptAreaType = IfcObjectIdentifier.getIFCProfileDefType(ifcModel, profileDef);
        if (sweptAreaType == null) return null;

        if (sweptAreaType.equals(ProfileDefRepresentationTypeItems.IfcRectangleProfileDef.name())) {
            // extract XDim, YDim
            double xDim = prepareDoubleString((String) profileDef.getAttributeValueBN("XDim"));
            double yDim = prepareDoubleString((String) profileDef.getAttributeValueBN("YDim"));
            double halfxDim = xDim / 2.0;
            double halfyDim = yDim / 2.0;

            // get points of shape
            ArrayList<Vector3D> cartesianPointsOfSArea = new ArrayList<>();
            cartesianPointsOfSArea.add(new Vector3D(locationVector3D.getX() - halfxDim, locationVector3D.getY() - halfyDim, 0.0));
            cartesianPointsOfSArea.add(new Vector3D(locationVector3D.getX() + halfxDim, locationVector3D.getY() - halfyDim, 0.0));
            cartesianPointsOfSArea.add(new Vector3D(locationVector3D.getX() + halfxDim, locationVector3D.getY() + halfyDim, 0.0));
            cartesianPointsOfSArea.add(new Vector3D(locationVector3D.getX() - halfxDim, locationVector3D.getY() + halfyDim, 0.0));
            cartesianPointsOfSArea.add(new Vector3D(locationVector3D.getX() - halfxDim, locationVector3D.getY() - halfyDim, 0.0));
            return cartesianPointsOfSArea;
        }
        if (sweptAreaType.equals(ProfileDefRepresentationTypeItems.IfcArbitraryClosedProfileDef.name())) {
            String profileType = (String) profileDef.getAttributeValueBN("ProfileType");

            if (profileType.equals(".AREA.")) {
                EntityInstance outerCurve = profileDef.getAttributeValueBNasEntityInstance("OuterCurve");
                ArrayList<Vector3D> curvePoints = getIfcCurveGeometry(ifcModel, outerCurve);
                if (curvePoints == null) return null;
                curvePoints.forEach(point -> point = new Vector3D(locationVector3D.getX() + point.getX(), locationVector3D.getY() + point.getY(), 0.0));
                return curvePoints;
            }
            if (profileType.equals(".CURVE.")) {
                // TODO implement
                logUnsupportedRepresentationInfo(sweptAreaType);
            }
        }

        // other types are not supported right now
        logUnsupportedRepresentationInfo(sweptAreaType);
        return null;
    }

    /**
     * Helper method to handle (floor-)openings in entities. Adds opening coordinates to entity shape data. If no opening, returns null
     *
     * @param ifcModel                    ifc model
     * @param shapeDataOfEntity           shape data of entity without opening handling
     * @param rootEntityOfShapeDataEntity root entity of shape representation entity
     * @return shape data of entity with opening handling or null if no handling or no opening
     */
    private static ArrayList<Vector3D> handleOpenings(ModelPopulation ifcModel, ArrayList<Vector3D> shapeDataOfEntity, EntityInstance rootEntityOfShapeDataEntity) {
        if (shapeDataOfEntity == null) return null;
        // for now opening handling supported for IFCSLAB only
        if (!IfcObjectIdentifier.isIfcSlab(ifcModel, rootEntityOfShapeDataEntity)) return null;

        // get relVoidsElement which describes the opening
        EntityInstance relVoidsElement = IfcObjectIdentifier.getRelVoidsElementOfEntity(ifcModel, rootEntityOfShapeDataEntity);
        if (relVoidsElement == null) return null;

        // get element which describes the opening
        EntityInstance openingElement = relVoidsElement.getAttributeValueBNasEntityInstance("RelatedOpeningElement");

        if (IfcObjectIdentifier.isIfcOpeningElement(ifcModel, openingElement)) {
            // get shape data of RelatedOpeningElement and RelatingBuildingObject
            BIMtoOSMUtility.GeometrySolution activeSolution = BIMtoOSMUtility.GeometrySolution.BOUNDING_BOX; // set for now
            List<Vector3D> shapeDataOfRelatedOpeningElement = BIMtoOSMUtility.getShapeData(ifcModel, openingElement, activeSolution);
            // subtract points of shapeDataOfRelatinBuildingElement from shapeDataOfRelatinBuildingElement
            return getIfcFeatureElementSubtractionGeometry(shapeDataOfEntity, (ArrayList<Vector3D>) shapeDataOfRelatedOpeningElement);
        }

        // IfcVoidingFeature as opening element is not supported right now
        Logging.info(IfcGeometryExtractor.class.getName() + ": IfcVoidingFeature is not supported right now");
        return null;
    }

    /**
     * Transforms IFCCARTESIANCOORDINATE entity into {@link Vector3D}
     *
     * @param cartesianCoordinate to transform
     * @return coordinate as {@link Vector3D}
     */
    public static Vector3D ifcCoordinatesToVector3D(EntityInstance cartesianCoordinate) {
        @SuppressWarnings("unchecked")
        List<String> objectCoords = (List<String>) cartesianCoordinate.getAttributeValueBN("Coordinates");
        if (objectCoords.isEmpty()) return null;
        double x = prepareDoubleString(objectCoords.get(0));
        double y = prepareDoubleString(objectCoords.get(1));
        double z = 0.0;
        if (objectCoords.size() == 3) {
            z = prepareDoubleString(objectCoords.get(2));
        }
        if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z)) {
            return null;
        }
        return new Vector3D(x, y, z);
    }

    /**
     * Logs info if representation type is not supported.
     *
     * @param representationItemType representation item as string
     */
    private static void logUnsupportedRepresentationInfo(String representationItemType) {
        Logging.info(IfcGeometryExtractor.class.getName() + ": " + representationItemType + " is not supported right now");
    }

}
