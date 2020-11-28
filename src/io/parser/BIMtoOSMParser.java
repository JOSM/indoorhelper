// License: AGPL. For details, see LICENSE file.
package io.parser;

import io.controller.ImportEventListener;
import io.model.BIMtoOSMCatalog;
import io.parser.data.BIMDataCollection;
import io.parser.data.BIMObject3D;
import io.parser.data.ifc.IfcRepresentation;
import io.parser.data.ifc.IfcRepresentationCatalog.IfcSpatialStructureElementTypes;
import io.parser.data.ifc.IfcRepresentationCatalog.RepresentationIdentifier;
import io.parser.data.ifc.IfcUnitCatalog;
import io.parser.math.Matrix3D;
import io.parser.math.ParserGeoMath;
import io.parser.math.ParserMath;
import io.parser.math.Vector3D;
import io.parser.utils.BIMtoOSMUtility;
import io.parser.utils.FileOptimizer;
import io.parser.utils.IfcGeometryExtractor;
import io.parser.utils.IfcObjectIdentifier;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import static io.parser.utils.ParserUtility.prepareDoubleString;
import static io.parser.utils.ParserUtility.stringVectorToVector3D;
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
    private final TagCatalog tagCatalog;
    private IfcUnitCatalog.lengthUnit lengthUnit;
    private IfcUnitCatalog.planeAngleUnit angleUnit;

    private final int defaultLevel = 999;

    // configuration parameters
    private BIMtoOSMUtility.GeometrySolution solutionType;
    private boolean optimizeInputFile;
    private boolean optimizeOutput;


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
        lengthUnit = IfcUnitCatalog.lengthUnit.M;
        angleUnit = IfcUnitCatalog.planeAngleUnit.RAD;
        applyDefaultConfiguration();
    }

    /**
     * Applies default configuration to parser
     */
    private void applyDefaultConfiguration() {
        solutionType = BIMtoOSMUtility.GeometrySolution.BOUNDING_BOX;
        optimizeInputFile = true;
        optimizeOutput = true;
    }

    /**
     * Sets configuration values of parser
     *
     * @param solution       type of parsed data. {@link BIMtoOSMUtility.GeometrySolution} represents precision of parsed data
     * @param optimizeInput  true if IFC file should be pre-optimized (comments will be removed before loading), else false
     * @param optimizeOutput true if OSM output should be optimized (optimization rules see operating method)
     */
    public void configure(BIMtoOSMUtility.GeometrySolution solution, boolean optimizeInput, boolean optimizeOutput) {
        solutionType = solution;
        optimizeInputFile = optimizeInput;
        this.optimizeOutput = optimizeOutput;
    }

    /**
     * Method parses data from IFC file into OSM data
     *
     * @param filepath of IFC file
     */
    public boolean parse(String filepath) {
        if (!loadFile(filepath)) return false;

        BIMDataCollection rawFilteredData = BIMtoOSMUtility.extractMajorBIMData(ifcModel);

        if (!checkForIFCSITE(rawFilteredData)) {
            showParsingErrorView(filepath, "Could not import IFC file.\nIFC file does not contains IFCSITE element.", true);
            return false;
        }

        ArrayList<BIMObject3D> preparedData = (ArrayList<BIMObject3D>) prepareRawBIMData(rawFilteredData);
        setUnits();

        LatLon llBuildingOrigin = getLatLonBuildingOrigin(rawFilteredData.getIfcSite());
        transformToGeodetic(llBuildingOrigin, preparedData);

        Pair<ArrayList<Node>, ArrayList<Way>> packedOSMData = packIntoOSMData(preparedData);
        ArrayList<Node> nodes = packedOSMData.a;
        ArrayList<Way> ways = packedOSMData.b;

        if (optimizeOutput) {
            // TODO optimizer OSM output
        }

        if (preparedData.size() != rawFilteredData.getSize()) {
            showParsingErrorView(filepath, "Caution!\nImported data might include errors!", false);
        }

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
            // pre-optimize and load IFC file
            File file;
            if (optimizeInputFile) {
                file = FileOptimizer.optimizeIfcFile(filepath);
            } else {
                file = new File(filepath);
            }

            inputfs = new FileInputStream(file);

            // find used IFC schema
            String usedIfcSchema = chooseSchemaFile(file);
            if (usedIfcSchema.isEmpty()) {
                showLoadingErrorView(filepath, "Could not load IFC file.\nIFC schema is no supported.");
                return false;
            }
            if (usedIfcSchema.equals(FLAG_IFC4)) {
                ifcSchemaFilePath = resourcePathDir + IFC4_Schema;
            }

            // load IFC file data into model
            ifcModel = new ModelPopulation(inputfs);
            ifcModel.setSchemaFile(Paths.get(ifcSchemaFilePath));
            ifcModel.load();

            // if loading throws ParseException check if ifcModel is empty to recognize something went wrong
            if (ifcModel.getInstances() == null) {
                showLoadingErrorView(filepath, "Could not load IFC file.");
                return false;
            }
        } catch (IOException e) {
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
     * @param ifcFile (if necessary) optimized IFC file
     * @return Used IFC file schema as string
     */
    private String chooseSchemaFile(File ifcFile) {
        String schema = "";
        try {
            Scanner reader = new Scanner(ifcFile, StandardCharsets.UTF_8.name());
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
     * Checks if IFCSITE element exists in data
     *
     * @param data to check
     * @return true if exists, else false
     */
    private boolean checkForIFCSITE(BIMDataCollection data) {
        try {
            data.getIfcSite().getAttributeValueBNasEntityInstance("ObjectPlacement").getId();
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * Extracts the BIM object geometry and prepares data as {@link BIMObject3D}
     *
     * @param rawBIMData to prepare
     * @return prepared data for rendering
     */
    private List<BIMObject3D> prepareRawBIMData(BIMDataCollection rawBIMData) {
        List<BIMObject3D> preparedData = new ArrayList<>();
        List<BIMObject3D> slabs = BIMtoOSMUtility.prepareBIMObjects(ifcModel, solutionType, BIMtoOSMCatalog.BIMObject.IfcSlab, rawBIMData.getAreaObjects());
        List<BIMObject3D> walls = BIMtoOSMUtility.prepareBIMObjects(ifcModel, solutionType, BIMtoOSMCatalog.BIMObject.IfcWall, rawBIMData.getWallObjects());
        List<BIMObject3D> columns = BIMtoOSMUtility.prepareBIMObjects(ifcModel,solutionType, BIMtoOSMCatalog.BIMObject.IfcColumn, rawBIMData.getColumnObjects());
        List<BIMObject3D> doors = BIMtoOSMUtility.prepareBIMObjects(ifcModel, solutionType, BIMtoOSMCatalog.BIMObject.IfcDoor, rawBIMData.getDoorObjects());
        List<BIMObject3D> windows = BIMtoOSMUtility.prepareBIMObjects(ifcModel, solutionType, BIMtoOSMCatalog.BIMObject.IfcWindow, rawBIMData.getWindowObjects());
        List<BIMObject3D> stairs = BIMtoOSMUtility.prepareBIMObjects(ifcModel, solutionType, BIMtoOSMCatalog.BIMObject.IfcStair, rawBIMData.getStairObjects());
        preparedData.addAll(slabs);
        preparedData.addAll(walls);
        preparedData.addAll(columns);
//        preparedData.addAll(doors);
//        preparedData.addAll(windows);
        preparedData.addAll(stairs);
        return preparedData;
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
            int level = getLevelTag(object, levelIdentifier);

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
    private int getLevelTag(BIMObject3D object, ArrayList<Pair<Double, Integer>> levelIdentifierList) {
        int level = defaultLevel;

        // get all IfcRelContainedInSpatialStructure elements
        List<EntityInstance> relContainedInSpatialStructureElements = ifcModel.getInstancesOfType("IfcRelContainedInSpatialStructure");

        for (EntityInstance entity : relContainedInSpatialStructureElements) {
            // for each element get contained entities
            ArrayList<EntityInstance> containedElements = entity.getAttributeValueBNasEntityInstanceList("RelatedElements");

            // check if object is part of contained entities
            for (EntityInstance element : containedElements) {

                if (element.getId() == object.getId()) {
                    // if part of contained elements get Elevation entity from object
                    EntityInstance relatingStructure = entity.getAttributeValueBNasEntityInstance("RelatingStructure");

                    String relatingStructureType = IfcObjectIdentifier.getSpatialStructureElementType(ifcModel, relatingStructure);
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
        List<EntityInstance> relContainedInSpatialStructureElements = ifcModel.getInstancesOfType("IfcRelContainedInSpatialStructure");

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
     * @param llBuildingOrigin building origin latlon
     * @param preparedBIMData  data to set the geodetic shapes
     */
    private void transformToGeodetic(LatLon llBuildingOrigin, ArrayList<BIMObject3D> preparedBIMData) {
        if (llBuildingOrigin != null) {

            // get building rotation matrix
            Vector3D projectNorth = getProjectNorth();
            Vector3D trueNorth = getTrueNorth();
            Matrix3D rotationMatrix = null;
            if (projectNorth != null && trueNorth != null) {
                double rotationAngle = trueNorth.angleBetween(projectNorth);
                rotationMatrix = ParserMath.getRotationMatrixAboutZAxis(rotationAngle);
            }

            if (rotationMatrix == null) return;

            for (BIMObject3D object : preparedBIMData) {
                ArrayList<LatLon> transformedCoordinates = new ArrayList<>();
                for (Vector3D point : object.getCartesianShapeCoordinates()) {
                    // rotate point
                    rotationMatrix.transform(point);
                    // transform point
                    LatLon llPoint = ParserGeoMath.cartesianToGeodetic(point, new Vector3D(0.0, 0.0, 0.0), llBuildingOrigin, lengthUnit);
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
    private LatLon getLatLonBuildingOrigin(EntityInstance ifcSite) {
        Vector3D ifcSiteOffset = null;
        if (ifcSite.getAttributeValueBNasEntityInstance("Representation") != null) {
            // get the offset between IFCSITE geodetic coordinates and building origin coordinate
            // handle IFCSITE offset if IFCBOUNDINGBOX representation
            List<IfcRepresentation> repObjectIdentities = BIMtoOSMUtility.getIfcRepresentations(ifcSite);
            if (repObjectIdentities == null) return null;

            IfcRepresentation boxRepresentation =
                    BIMtoOSMUtility.getIfcRepresentation(repObjectIdentities, RepresentationIdentifier.Box);
            if (boxRepresentation != null) {
                // get offset
                EntityInstance bb = boxRepresentation.getEntity();
                EntityInstance bbItem = bb.getAttributeValueBNasEntityInstanceList("Items").get(0);
                EntityInstance cartesianCorner = bbItem.getAttributeValueBNasEntityInstance("Corner");
                ifcSiteOffset = IfcGeometryExtractor.ifcCoordinatesToVector3D(cartesianCorner);
            }
        }

        // get RefLatitude and RefLongitude of IFCSITE
        List<String> refLat;
        List<String> refLon;
        try {
            refLat = (List<String>) ifcSite.getAttributeValueBN("RefLatitude");
            refLon = (List<String>) ifcSite.getAttributeValueBN("RefLongitude");
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
            return ParserGeoMath.cartesianToGeodetic(new Vector3D(0.0, 0.0, 0.0), ifcSiteOffset, new LatLon(lat, lon), lengthUnit);
        }

        return new LatLon(lat, lon);
    }

    /**
     * Get project north of building
     *
     * @return project north as {@link Vector3D}
     */
    @SuppressWarnings("unchecked")
    private Vector3D getProjectNorth() {
        List<String> projectNorthDirectionRatios;
        try {
            EntityInstance ifcProject = ifcModel.getInstancesOfType("IfcProject").get(0);
            EntityInstance geometricContext = ifcProject.getAttributeValueBNasEntityInstanceList("RepresentationContexts").get(0);
            EntityInstance worldCoordinates = geometricContext.getAttributeValueBNasEntityInstance("WorldCoordinateSystem");
            EntityInstance projectNorth = worldCoordinates.getAttributeValueBNasEntityInstance("RefDirection");
            projectNorthDirectionRatios = (List<String>) projectNorth.getAttributeValueBN("DirectionRatios");
        } catch (NullPointerException e) {
            return null;
        }
        return stringVectorToVector3D(projectNorthDirectionRatios);
    }

    /**
     * get true north vector of building
     *
     * @return true north as {@link Vector3D}
     */
    @SuppressWarnings("unchecked")
    private Vector3D getTrueNorth() {
        List<String> trueNorthDirectionRatios;
        try {
            EntityInstance ifcProject = ifcModel.getInstancesOfType("IfcProject").get(0);
            EntityInstance geometricContext = ifcProject.getAttributeValueBNasEntityInstanceList("RepresentationContexts").get(0);
            EntityInstance trueNorth = geometricContext.getAttributeValueBNasEntityInstance("TrueNorth");
            trueNorthDirectionRatios = (List<String>) trueNorth.getAttributeValueBN("DirectionRatios");
        } catch (NullPointerException e) {
            return null;
        }
        return stringVectorToVector3D(trueNorthDirectionRatios);
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
                            if (unitPrefix.equals(".CENTI.")) lengthUnit = IfcUnitCatalog.lengthUnit.CM;
                            if (unitPrefix.equals(".MILLI.")) lengthUnit = IfcUnitCatalog.lengthUnit.MM;
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
                        angleUnit = IfcUnitCatalog.planeAngleUnit.DEG;
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
