// License: AGPL. For details, see LICENSE file.
package io.parser;

import io.controller.ImportEventListener;
import io.model.BIMtoOSMCatalog;
import io.parser.data.BIMObject3D;
import io.parser.data.FilteredRawBIMData;
import io.parser.data.ifc.IfcRepresentation;
import io.parser.data.ifc.IfcRepresentationCatalog.IfcSpatialStructureElementTypes;
import io.parser.data.ifc.IfcRepresentationCatalog.RepresentationIdentifier;
import io.parser.data.ifc.IfcUnitCatalog;
import io.parser.data.math.Point3D;
import io.parser.data.math.Vector3D;
import io.parser.helper.BIMtoOSMHelper;
import io.parser.helper.IFCShapeRepresentationIdentifier;
import io.parser.helper.IfcRepresentationExtractor;
import io.parser.math.ParserGeoMath;
import io.parser.math.ParserMath;
import model.TagCatalog;
import nl.tue.buildingsmart.express.population.EntityInstance;
import nl.tue.buildingsmart.express.population.ModelPopulation;
import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Pair;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;

import static io.parser.ParserUtility.prepareDoubleString;
import static io.parser.ParserUtility.stringVectorToVector3D;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Parser for BIM data. Extracts major BIM elements and transforms coordinates into OSM convenient format
 *
 * @author rebsc
 */
public class BIMtoOSMParser {

    private final String FLAG_IFC2X3_TC1 = "FILE_SCHEMA(('IFC2X3_TC1'))";
    private final String FLAG_IFC2X3 = "FILE_SCHEMA(('IFC2X3'))";
    private final String FLAG_IFC4 = "FILE_SCHEMA(('IFC4'))";
    private final String IFC2X3_TC1_Schema = "IFC2X3_TC1.exp";
    private final String IFC4_Schema = "IFC4.exp";
    private final String resourcePathDir;
    private String ifcSchemaFilePath;

    private ImportEventListener importListener;
    private FileInputStream inputfs = null;

    private ModelPopulation ifcModel;
    private TagCatalog tagCatalog;
    private IfcUnitCatalog.LENGTHUNIT lengthUnit;
    private IfcUnitCatalog.PLANEANGLEUNIT angleUnit;

    private final int defaultLevel = 999;

    /**
     * Constructor
     *
     * @param listener        for import events
     * @param pluginDirectory of indoorHelper plugin
     */
    public BIMtoOSMParser(ImportEventListener listener, String pluginDirectory) {
        importListener = listener;
        if (pluginDirectory == null) {
            resourcePathDir = Preferences.main().getPluginsDirectory().toString() + "/indoorhelper/resources";
        } else {
            resourcePathDir = pluginDirectory + "/resources/";
        }
        ifcSchemaFilePath = resourcePathDir + IFC2X3_TC1_Schema;
        tagCatalog = new TagCatalog();
        lengthUnit = IfcUnitCatalog.LENGTHUNIT.M;    // default
        angleUnit = IfcUnitCatalog.PLANEANGLEUNIT.RAD;    // default
    }

    /**
     * Method parses data from IFC file into OSM data
     *
     * @param filepath of IFC file
     */
    public boolean parse(String filepath) {
        // load data into IFC model
        if (!loadFile(filepath)) return false;

        // extract important data and put them into internal data structure
        FilteredRawBIMData filteredRawBIMData = BIMtoOSMHelper.extractMajorBIMData(ifcModel);

        // for preparation of filtered BIM data find Id of BIM root IFCLOCALPLACEMENT element (kept in IFCSITE flag)
        int bimFileRootId = BIMtoOSMHelper.getIfcLocalPlacementRootObject(filteredRawBIMData);
        if (bimFileRootId == -1) {
            showParsingErrorView(filepath, "Could not import IFC file.\nIFC file does not contains IFCSITE element.", true);
            return false;
        }

        // prepare filtered BIM data - find global object coordinates and other attributes like object height, width etc.
        ArrayList<BIMObject3D> preparedBIMData = new ArrayList<>();
        preparedBIMData.addAll(BIMtoOSMHelper.prepareBIMObjects(ifcModel, bimFileRootId, BIMtoOSMCatalog.BIMObject.IfcSlab, filteredRawBIMData.getAreaObjects()));
        preparedBIMData.addAll(BIMtoOSMHelper.prepareBIMObjects(ifcModel, bimFileRootId, BIMtoOSMCatalog.BIMObject.IfcWall, filteredRawBIMData.getWallObjects()));
        preparedBIMData.addAll(BIMtoOSMHelper.prepareBIMObjects(ifcModel, bimFileRootId, BIMtoOSMCatalog.BIMObject.IfcColumn, filteredRawBIMData.getColumnObjects()));
//        preparedBIMData.addAll(BIMtoOSMHelper.prepareBIMObjects(ifcModel, bimFileRootId, BIMtoOSMCatalog.BIMObject.IfcDoor, filteredRawBIMData.getDoorObjects()));
//        preparedBIMData.addAll(BIMtoOSMHelper.prepareBIMObjects(ifcModel, bimFileRootId, BIMtoOSMCatalog.BIMObject.IfcWindow, filteredRawBIMData.getWindowObjects()));
        preparedBIMData.addAll(BIMtoOSMHelper.prepareBIMObjects(ifcModel, bimFileRootId, BIMtoOSMCatalog.BIMObject.IfcStair, filteredRawBIMData.getStairObjects()));

        // set units
        setUnits();

        // transform coordinates from local system to geodetic
        LatLon latlonBuildingOrigin = getLatLonOriginOfBuilding(filteredRawBIMData.getIfcSite());
        transformCoordinatesToLatLon(latlonBuildingOrigin, preparedBIMData);

        // pack FilteredBIMData into OSM data
        Pair<ArrayList<Node>, ArrayList<Way>> packedOSMData = packIntoOSMData(preparedBIMData);
        ArrayList<Node> nodes = packedOSMData.a;
        ArrayList<Way> ways = packedOSMData.b;

        // check if file is corrupted. File is corrupted if some data could not pass the preparation steps
        if (preparedBIMData.size() != filteredRawBIMData.getSize()) {
            showParsingErrorView(filepath, "Caution!\nImported data might include errors!", false);
        }

        // send parsed data to controller
        if (importListener != null) {
            importListener.onDataParsed(ways, nodes);
        }
        Logging.info(this.getClass().getName() + ": " + filepath + " parsed successfully");
        return true;
    }

    /**
     * Load file into ifcModel
     *
     * @param filepath of IFC file
     * @return true if loading successful, else false
     */
    private boolean loadFile(String filepath) {
        try {
            // find used IFC schema
            String usedIfcSchema = chooseSchemaFile(filepath);

            if (usedIfcSchema.isEmpty()) {
                showLoadingErrorView(filepath, "Could not load IFC file.\nIFC schema is no supported.");
                return false;
            }
            if (usedIfcSchema.equals(FLAG_IFC4)) {
                ifcSchemaFilePath = resourcePathDir + IFC4_Schema;
            }

            // load IFC file
            inputfs = new FileInputStream(filepath);
            // load IFC file data into model
            ifcModel = new ModelPopulation(inputfs);
            ifcModel.setSchemaFile(Paths.get(ifcSchemaFilePath));
            ifcModel.load();

            // if loading throws ParseException check if ifcModel is empty to recognize something went wrong
            if (ifcModel.getInstances() == null) {
                showLoadingErrorView(filepath, "Could not load IFC file.");
                return false;
            }
        } catch (FileNotFoundException e) {
            Logging.error(e.getMessage());
            return false;
        } finally {
            if (inputfs != null) {
                try {
                    inputfs.close();
                } catch (IOException ignored) {
                }
            }
        }
        Logging.info(this.getClass().getName() + ": " + filepath + " loaded successfully");
        return true;
    }

    /**
     * Read the FILE_SCHEMA flag from IFC file and return used schema
     *
     * @param filepath path of IFC file
     * @return Used IFC file schema as string
     */
    private String chooseSchemaFile(String filepath) {
        String schema = "";
        try {
            File file = new File(filepath);
            Scanner reader = new Scanner(file, StandardCharsets.UTF_8.name());

            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                data = data.replaceAll("\\s+", "");

                // schema must be defined before this flag
                if (data.contains("DATA;")) break;
                // check if IFC2X3
                if (data.contains(FLAG_IFC2X3_TC1) || data.contains(FLAG_IFC2X3)) {
                    schema = FLAG_IFC2X3_TC1;
                    break;
                }
                // check if IFC4
                else if (data.contains(FLAG_IFC4)) {
                    schema = FLAG_IFC4;
                    break;
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            Logging.error(e.getMessage());
        }

        return schema;
    }

    /**
     * Method packs prepared BIM data into OSM ways and nodes
     *
     * @param preparedBIMData to transform to OSM data
     * @return packed data
     */
    private Pair<ArrayList<Node>, ArrayList<Way>> packIntoOSMData(ArrayList<BIMObject3D> preparedBIMData) {
        ArrayList<Way> ways = new ArrayList<>();
        ArrayList<Node> nodes = new ArrayList<>();
        ArrayList<Pair<Double, Integer>> levelIdentifier = extractAndIdentifyLevels();

        for (BIMObject3D object : preparedBIMData) {
            int level = getLevelTagOfPreparedBIMObject(object, levelIdentifier);

            ArrayList<Node> tmpNodes = new ArrayList<>();
            for (LatLon point : object.getGeodeticShapeCoordinates()) {
                Node n = new Node(point);
                tmpNodes.add(n);
            }

            if (tmpNodes.size() < 2) continue;

            if (tmpNodes.get(0).lat() == tmpNodes.get(tmpNodes.size() - 1).lat() && tmpNodes.get(0).lon() == tmpNodes.get(tmpNodes.size() - 1).lon()) {
                tmpNodes.remove(tmpNodes.size() - 1);
                nodes.addAll(tmpNodes);
                tmpNodes.add(tmpNodes.get(0));
            } else {
                nodes.addAll(tmpNodes);
            }
            Way w = new Way();
            w.setNodes(tmpNodes);
            getObjectTags(object).forEach(w::put);
            if (level != defaultLevel) w.put(new Tag("level", Integer.toString(level)));
            ways.add(w);
        }

        return new Pair<>(nodes, ways);
    }

    /**
     * Method gets level tag of PreparedBIMObject3D
     *
     * @param object              to get level tag for
     * @param levelIdentifierList with identified levels
     * @return level
     */
    private int getLevelTagOfPreparedBIMObject(BIMObject3D object, ArrayList<Pair<Double, Integer>> levelIdentifierList) {
        int level = defaultLevel;

        // get all IfcRelContainedInSpatialStructure elements
        Vector<EntityInstance> relContainedInSpatialStructureElements = ifcModel.getInstancesOfType("IfcRelContainedInSpatialStructure");

        for (EntityInstance entity : relContainedInSpatialStructureElements) {
            // for each element get contained entities
            ArrayList<EntityInstance> containedElements = entity.getAttributeValueBNasEntityInstanceList("RelatedElements");

            // check if object is part of contained entities
            for (EntityInstance element : containedElements) {

                if (element.getId() == object.getId()) {
                    // if part of contained elements get Elevation entity from object
                    EntityInstance relatingStructure = entity.getAttributeValueBNasEntityInstance("RelatingStructure");

                    String relatingStructureType = IFCShapeRepresentationIdentifier.getSpatialStructureElementType(ifcModel, relatingStructure);
                    // get type of relatingStructure
                    if (!relatingStructureType.equals(IfcSpatialStructureElementTypes.IfcBuildingStorey.name()))
                        return 0;
                    // if of type IFCBUILDINGSTOREY
                    double storeyElevation = prepareDoubleString((String) relatingStructure.getAttributeValueBN("Elevation"));

                    // get assigned level tag to Elevation entity
                    for (Pair<Double, Integer> identifier : levelIdentifierList) {
                        if (identifier.a == storeyElevation) {
                            level = identifier.b;
                            break;
                        }
                    }
                }

            }
        }
        return level;
    }

    /**
     * Method extracts and identifies level tags from IfcRelContainedInSpatialStructure elements
     *
     * @return List with pairs of level Elevation entity (Double) and assigned level tag (Integer)
     */
    private ArrayList<Pair<Double, Integer>> extractAndIdentifyLevels() {
        // get all IfcRelContainedInSpatialStructure elements
        Vector<EntityInstance> relContainedInSpatialStructureElements = ifcModel.getInstancesOfType("IfcRelContainedInSpatialStructure");

        ArrayList<Pair<Double, Integer>> levelIdentifier = new ArrayList<>();
        ArrayList<Double> levelList = new ArrayList<>();

        // run thru IfcRelContainedInSpatialStructure and get the buildingStorey elements. Those elements include an Elevation entity
        for (EntityInstance entity : relContainedInSpatialStructureElements) {
            EntityInstance buildingStorey = entity.getAttributeValueBNasEntityInstance("RelatingStructure");
            double storeyElevation = prepareDoubleString((String) buildingStorey.getAttributeValueBN("Elevation"));
            levelList.add(storeyElevation);
        }

        // Sort the Elevation entity ascending
        Collections.sort(levelList);

        int level0Index = -1;
        double level0 = 999.0;
        for (Double level : levelList) {
            double d = Math.abs(0.0 - level);
            if (d < level0) {
                level0Index = levelList.indexOf(level);
                level0 = d;
            }
        }

        for (Double level : levelList) {
            int index = levelList.indexOf(level) - level0Index;
            levelIdentifier.add(new Pair<>(level, index));
        }

        return levelIdentifier;
    }

    /**
     * Method sets geodetic shape coordinates of PreparedBIMObject3D
     *
     * @param latlonBuildingOrigin building origin latlon
     * @param preparedBIMdata      data to set the geodetic shapes
     */
    private void transformCoordinatesToLatLon(LatLon latlonBuildingOrigin, ArrayList<BIMObject3D> preparedBIMdata) {
        if (latlonBuildingOrigin != null) {

            // get building rotation matrix
            Point3D projectNorth = getProjectNorth();
            Point3D trueNorth = getTrueNorth();
            double[][] rotationMatrix = null;
            if (projectNorth != null && trueNorth != null) {
                double[] projectNorthVector = {projectNorth.getX(), projectNorth.getY(), projectNorth.getZ()};
                double[] trueNorthVector = {trueNorth.getX(), trueNorth.getY(), trueNorth.getZ()};
                double rotationAngle = ParserMath.getAngleBetweenVectors(trueNorthVector, projectNorthVector);
                rotationMatrix = ParserMath.getRotationMatrixAboutZAxis(rotationAngle);
            }

            for (BIMObject3D object : preparedBIMdata) {
                ArrayList<LatLon> transformedCoordinates = new ArrayList<>();
                for (Point3D point : object.getCartesianShapeCoordinates()) {
                    // rotate point
                    double[] pointAsVector = {point.getX(), point.getY(), point.getZ()};
                    double[] rotatedPoint = ParserMath.rotate3DPoint(pointAsVector, rotationMatrix);
                    point.setX(rotatedPoint[0]);
                    point.setY(rotatedPoint[1]);
                    point.setZ(rotatedPoint[2]);
                    // transform point
                    LatLon llPoint = ParserGeoMath.cartesianToGeodetic(point, new Point3D(0.0, 0.0, 0.0), latlonBuildingOrigin, lengthUnit);
                    transformedCoordinates.add(llPoint);
                }
                object.setGeodeticShapeCoordinates(transformedCoordinates);
            }
        }
    }

    /**
     * Method calculates the latlon coordinates of building origin corner
     *
     * @param ifcSite IFCSITE entity
     * @return latlon coordinates of building corner
     */
    @SuppressWarnings("unchecked")
    private LatLon getLatLonOriginOfBuilding(EntityInstance ifcSite) {
        Point3D ifcSiteOffset = null;
        if (ifcSite.getAttributeValueBNasEntityInstance("Representation") != null) {
            // get the offset between IFCSITE geodetic coordinates and building origin coordinate
            // handle IFCSITE offset if IFCBOUNDINGBOX representation
            List<IfcRepresentation> repObjectIdentities = BIMtoOSMHelper.identifyRepresentationsOfObject(ifcSite);
            if (repObjectIdentities == null) return null;

            IfcRepresentation boxRepresentation =
                    BIMtoOSMHelper.getRepresentationSpecificObjectType(repObjectIdentities, RepresentationIdentifier.Box);
            if (boxRepresentation != null) {
                // get offset
                EntityInstance bb = boxRepresentation.getEntity();
                EntityInstance bbItem = bb.getAttributeValueBNasEntityInstanceList("Items").get(0);
                EntityInstance cartesianCorner = bbItem.getAttributeValueBNasEntityInstance("Corner");
                ifcSiteOffset = IfcRepresentationExtractor.ifcCartesianCoordinateToPoint3D(cartesianCorner);
            }
        }

        // get RefLatitude and RefLongitude of IFCSITE
        Vector<String> refLat;
        Vector<String> refLon;
        try {
            refLat = (Vector<String>) ifcSite.getAttributeValueBN("RefLatitude");
            refLon = (Vector<String>) ifcSite.getAttributeValueBN("RefLongitude");
        } catch (NullPointerException e) {
            return null;
        }

        if (refLat == null || refLon == null) return null;

        // transform angle measurement to latlon
        double lat = ParserGeoMath.degreeMinutesSecondsToLatLon(
                prepareDoubleString(refLat.get(0)),
                prepareDoubleString(refLat.get(1)),
                prepareDoubleString(refLat.get(2)));
        double lon = ParserGeoMath.degreeMinutesSecondsToLatLon(
                prepareDoubleString(refLon.get(0)),
                prepareDoubleString(refLon.get(1)),
                prepareDoubleString(refLon.get(2)));

        // if offset, calculate building origin without offset
        if (ifcSiteOffset != null && ifcSiteOffset.getX() != 0.0 && ifcSiteOffset.getY() != 0.0) {
            return ParserGeoMath.cartesianToGeodetic(new Point3D(0.0, 0.0, 0.0), ifcSiteOffset, new LatLon(lat, lon), lengthUnit);
        }

        return new LatLon(lat, lon);
    }

    /**
     * Get project north of building
     *
     * @return project north as Point3D
     */
    @SuppressWarnings("unchecked")
    private Point3D getProjectNorth() {
        Vector<String> projectNorthDirectionRatios = null;
        try {
            EntityInstance ifcProject = ifcModel.getInstancesOfType("IfcProject").get(0);
            EntityInstance geometricContext = ifcProject.getAttributeValueBNasEntityInstanceList("RepresentationContexts").get(0);
            EntityInstance worldCoordinates = geometricContext.getAttributeValueBNasEntityInstance("WorldCoordinateSystem");
            EntityInstance projectNorth = worldCoordinates.getAttributeValueBNasEntityInstance("RefDirection");
            projectNorthDirectionRatios = (Vector<String>) projectNorth.getAttributeValueBN("DirectionRatios");
        } catch (NullPointerException e) {
            return null;
        }
        Vector3D pNDRVector = stringVectorToVector3D(projectNorthDirectionRatios);
        if(pNDRVector == null) return null;
        return new Point3D(pNDRVector.getX(), pNDRVector.getY(), pNDRVector.getZ());
    }

    /**
     * get true north vector of building
     *
     * @return true north as Point3D
     */
    @SuppressWarnings("unchecked")
    private Point3D getTrueNorth() {
        Vector<String> trueNorthDirectionRatios = null;
        try {
            EntityInstance ifcProject = ifcModel.getInstancesOfType("IfcProject").get(0);
            EntityInstance geometricContext = ifcProject.getAttributeValueBNasEntityInstanceList("RepresentationContexts").get(0);
            EntityInstance trueNorth = geometricContext.getAttributeValueBNasEntityInstance("TrueNorth");
            trueNorthDirectionRatios = (Vector<String>) trueNorth.getAttributeValueBN("DirectionRatios");
        } catch (NullPointerException e) {
            return null;
        }
        Vector3D tNDRVector = stringVectorToVector3D(trueNorthDirectionRatios);
        if(tNDRVector == null) return null;
        return new Point3D(tNDRVector.getX(), tNDRVector.getY(), tNDRVector.getZ());
    }

    /**
     * Method sets length unit of file
     */
    private void setUnits() {
        ArrayList<EntityInstance> units =
                ifcModel.getInstancesOfType("IfcUnitAssignment").get(0).getAttributeValueBNasEntityInstanceList("Units");
        for (EntityInstance unit : units) {
            try {
                String unitType = (String) unit.getAttributeValueBN("UnitType");
                String unitLabel = (String) unit.getAttributeValueBN("Name");
                if (unitType.equals(".LENGTHUNIT.")) {
                    if (unitLabel.equals(".METRE.")) {
                        try {
                            String unitPrefix = (String) unit.getAttributeValueBN("Prefix");
                            if (unitPrefix.equals(".CENTI.")) lengthUnit = IfcUnitCatalog.LENGTHUNIT.CM;
                            if (unitPrefix.equals(".MILLI.")) lengthUnit = IfcUnitCatalog.LENGTHUNIT.MM;
                            break;
                            // TODO handle more prefixes
                        } catch (NullPointerException e) {
                            // do nothing
                        }
                        break;
                    }
                }
                if (unitType.equals(".PLANEANGLEUNIT.")) {
                    if (unitLabel.equals(".DEGREE.")) {
                        angleUnit = IfcUnitCatalog.PLANEANGLEUNIT.DEG;
                        break;
                    }
                }
            } catch (NullPointerException e) {
                // do nothing
            }
        }
    }

    /**
     * Method get OSM tags describing BIM object
     *
     * @param object to get OSM tags for
     * @return OSM Tags as array
     */
    private ArrayList<Tag> getObjectTags(BIMObject3D object) {
        if (object.getType().name().contains("Slab")) {
            return (ArrayList<Tag>) tagCatalog.getTags(TagCatalog.IndoorObject.ROOM);
        }
        if (object.getType().name().contains("Wall")) {
            return (ArrayList<Tag>) tagCatalog.getTags(TagCatalog.IndoorObject.CONCRETE_WALL);
        }
        if (object.getType().name().contains("Column")) {
            return (ArrayList<Tag>) tagCatalog.getTags(TagCatalog.IndoorObject.CONCRETE_WALL);
        }
        if (object.getType().name().contains("Door")) {
            return (ArrayList<Tag>) tagCatalog.getTags(TagCatalog.IndoorObject.DOOR_PRIVATE);
        }
        if (object.getType().name().contains("Window")) {
            return (ArrayList<Tag>) tagCatalog.getTags(TagCatalog.IndoorObject.GLASS_WALL);
        }
        if (object.getType().name().contains("Stair")) {
            return (ArrayList<Tag>) tagCatalog.getTags(TagCatalog.IndoorObject.STEPS);
        }
        return new ArrayList<>();
    }

    /**
     * Shows error dialog is file loading failed
     *
     * @param filepath of ifc file
     * @param msg      Error message
     */
    private void showLoadingErrorView(String filepath, String msg) {
        showErrorView(tr(msg));
        Logging.info(this.getClass().getName() + ": " + filepath + " loading failed");
    }

    /**
     * Shows error dialog is file loading failed
     *
     * @param filepath of IFC file
     * @param msg      Error message
     * @param logInfo  log info to console
     */
    private void showParsingErrorView(String filepath, String msg, boolean logInfo) {
        showErrorView(tr(msg));
        if (logInfo) {
            Logging.info(this.getClass().getName() + ": " + filepath + " parsing failed");
        }
    }

    /**
     * Shows error dialog
     *
     * @param msg Error message
     */
    private void showErrorView(String msg) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                msg,
                "Error",
                JOptionPane.ERROR_MESSAGE));
    }

}
