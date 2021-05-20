// License: AGPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.indoorhelper.io.parser;

import org.openstreetmap.josm.plugins.indoorhelper.io.parser.data.ifc.*;
import org.openstreetmap.josm.plugins.indoorhelper.io.parser.data.ifc.IfcRepresentationCatalog.*;
import org.openstreetmap.josm.plugins.indoorhelper.io.controller.ImportEventListener;
import org.openstreetmap.josm.plugins.indoorhelper.io.model.BIMtoOSMCatalog;
import org.openstreetmap.josm.plugins.indoorhelper.io.optimizer.InputOptimizer;
import org.openstreetmap.josm.plugins.indoorhelper.io.optimizer.OutputOptimizer;
import org.openstreetmap.josm.plugins.indoorhelper.io.parser.data.BIMDataCollection;
import org.openstreetmap.josm.plugins.indoorhelper.io.parser.data.BIMObject3D;
import org.openstreetmap.josm.plugins.indoorhelper.io.parser.math.*;
import org.openstreetmap.josm.plugins.indoorhelper.io.parser.utils.ifc.BIMtoOSMUtility;
import org.openstreetmap.josm.plugins.indoorhelper.io.parser.utils.ifc.IfcGeometryExtractor;
import org.openstreetmap.josm.plugins.indoorhelper.io.parser.utils.ifc.IfcObjectIdentifier;
import org.openstreetmap.josm.plugins.indoorhelper.model.TagCatalog;
import nl.tue.buildingsmart.express.population.EntityInstance;
import nl.tue.buildingsmart.express.population.ModelPopulation;
import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Pair;

import javax.swing.*;
import java.awt.*;
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

import static org.openstreetmap.josm.plugins.indoorhelper.io.parser.utils.ParserUtility.prepareDoubleString;
import static org.openstreetmap.josm.plugins.indoorhelper.io.parser.utils.ParserUtility.stringVectorToVector3D;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Parser for BIM data. Extracts major BIM elements and transforms coordinates into OSM convenient format
 *
 * @author rebsc
 */
public class BIMtoOSMParser {

    private static final String FLAG_IFC2X3_TC1 = "FILE_SCHEMA(('IFC2X3_TC1'))";
    private static final String FLAG_IFC2X3 = "FILE_SCHEMA(('IFC2X3'))";
    private static final String FLAG_IFC4 = "FILE_SCHEMA(('IFC4'))";
    private static final String IFC2X3_TC1_SCHEMA = "IFC2X3_TC1.exp";
    private static final String IFC4_SCHEMA = "IFC4.exp";
    private final String resourcePathDir;
    private String ifcSchemaFilePath;

    private final ImportEventListener importListener;
    private FileInputStream inputFs = null;

    private ModelPopulation ifcModel;
    private final TagCatalog tagCatalog;
    private IfcUnitCatalog.LengthUnit lengthUnit;

    private static final int DEFAULT_LEVEL = 999;

    // configuration parameters
    private BIMtoOSMUtility.GeometrySolution solutionType;
    private boolean optimizeInputFile;
    private InputOptimizer.Configuration optimizeInputConfig;
    private boolean optimizeOutput;
    private OutputOptimizer.Configuration optimizeOutputConfig;

    /**
     * Constructor
     *
     * @param listener        for import events
     * @param pluginDirectory of indoorHelper plugin
     */
    public BIMtoOSMParser(ImportEventListener listener, String pluginDirectory) {
        if (listener == null) {
            throw new IllegalArgumentException("invalid argument value of listener: null");
        }
        importListener = listener;

        if (pluginDirectory == null) {
            resourcePathDir = Preferences.main().getPluginsDirectory().toString() + "/indoorhelper/resources";
        } else {
            resourcePathDir = pluginDirectory + "/resources/";
        }

        ifcSchemaFilePath = resourcePathDir + IFC2X3_TC1_SCHEMA;
        tagCatalog = new TagCatalog();
        lengthUnit = IfcUnitCatalog.LengthUnit.M;
        applyDefaultConfiguration();
    }

    /**
     * Applies default configuration to parser
     */
    private void applyDefaultConfiguration() {
        configure(BIMtoOSMUtility.GeometrySolution.BOUNDING_BOX,
                new InputOptimizer.Configuration(true),
                // default: merge overlapping nodes only (distance < 0.01)
                new OutputOptimizer.Configuration(true, 0.01));
    }

    /**
     * Sets configuration values of parser
     *
     * @param solution             type of parsed data. {@link BIMtoOSMUtility.GeometrySolution} represents
     *                             precision of parsed data
     * @param optimizeInputConfig  not null if IFC file should be pre-optimized, else null
     * @param optimizeOutputConfig not null if OSM output should be optimized, else null
     * @return true if config set successfully, else false
     */
    public boolean configure(BIMtoOSMUtility.GeometrySolution solution,
                             InputOptimizer.Configuration optimizeInputConfig,
                             OutputOptimizer.Configuration optimizeOutputConfig) {
        if (solution == null) {
            Logging.info(BIMtoOSMParser.class.getName()
                    + ": Failed to set parser configuration. Solution equals null!");
            return false;
        }
        if (optimizeInputConfig == null) {
            Logging.info(BIMtoOSMParser.class.getName()
                    + ": Failed to set parser configuration. optimizeInputConfig equals null!");
            return false;
        }
        if (optimizeOutputConfig == null) {
            Logging.info(BIMtoOSMParser.class.getName()
                    + ": Failed to set parser configuration. optimizeOutputConfig equals null!");
            return false;
        }

        solutionType = solution;
        Logging.info(String.format("%s-ConfigurationReport: solution set to %s",
                BIMtoOSMParser.class.getName(), solutionType.name()));

        optimizeInputFile = optimizeInputConfig.REMOVE_BLOCK_COMMENTS;
        this.optimizeInputConfig = optimizeInputConfig;
        Logging.info(String.format("%s-ConfigurationReport: optimizeInputFile %s; RemoveBlockCommands %s",
                BIMtoOSMParser.class.getName(),
                optimizeInputConfig.REMOVE_BLOCK_COMMENTS ? "enabled" : "disabled",
                optimizeInputConfig.REMOVE_BLOCK_COMMENTS ? "enabled" : "disabled"));

        optimizeOutput = optimizeOutputConfig.MERGE_CLOSE_NODES;
        this.optimizeOutputConfig = optimizeOutputConfig;
        Logging.info(String.format("%s-ConfigurationReport: optimizeOutput %s; MergeCloseNodes %s; " +
                        "MergeDistance set to %.2f m",
                BIMtoOSMParser.class.getName(),
                optimizeOutputConfig.MERGE_CLOSE_NODES ? "enabled" : "disabled",
                optimizeOutputConfig.MERGE_CLOSE_NODES ? "enabled" : "disabled",
                optimizeOutputConfig.MERGE_CLOSE_NODES ? optimizeOutputConfig.MERGE_DISTANCE : -999));

        return true;
    }

    /**
     * Method parses data from ifc file into OSM data
     *
     * @param filepath of ifc file
     */
    public boolean parse(String filepath) {
        if (!loadFile(filepath)) return false;

        // get osm relevant data
        BIMDataCollection rawFilteredData = BIMtoOSMUtility.extractMajorBIMData(ifcModel);

        if (!checkForIFCSITE(rawFilteredData)) {
            showParsingErrorView(filepath, "Could not import IFC file.\nIFC " +
                    "file does not contain IFCSITE element.", true);
            return false;
        }

        // transform osm relevant data into BIMObject3D
        ArrayList<BIMObject3D> preparedData = (ArrayList<BIMObject3D>) transformToBIMData(rawFilteredData);

        // transform building coordinates to WCS
        setUnits();
        LatLon llBuildingOrigin = getLatLonBuildingOrigin(rawFilteredData.getIfcSite());
        transformToGeodetic(llBuildingOrigin, preparedData);

        // pack parsed data into osm format
        DataSet packedOSMData = packIntoOSMData(preparedData);
        if (optimizeOutput) {
            importListener.onProcessStatusChanged("optimizing data");
            OutputOptimizer.optimize(optimizeOutputConfig, packedOSMData);
        }

        if (preparedData.size() != rawFilteredData.getSize()) {
            showParsingErrorView(filepath, "Caution!\nImported data might include errors!", false);
        }

        // trigger rendering
        importListener.onDataParsed(packedOSMData);

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
                file = InputOptimizer.optimizeIfcFile(optimizeInputConfig, filepath);
            } else {
                file = new File(filepath);
            }

            inputFs = new FileInputStream(file);

            // find used IFC schema
            String usedIfcSchema = chooseSchemaFile(file);
            if (usedIfcSchema.isEmpty()) {
                showLoadingErrorView(filepath, "Could not load IFC file.\nIFC schema is not supported.");
                return false;
            }
            if (usedIfcSchema.equals(FLAG_IFC4)) {
                ifcSchemaFilePath = resourcePathDir + IFC4_SCHEMA;
            }

            // load IFC file data into model
            ifcModel = new ModelPopulation(inputFs);
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
            if (inputFs != null) {
                try {
                    inputFs.close();
                } catch (IOException ignored) {
                    // do nothing
                }
            }
        }
        Logging.info(this.getClass().getName() + ": " + filepath + " loaded successfully");
        return true;
    }

    /**
     * Read the FILE_SCHEMA flag from ifc file and return used schema
     *
     * @param ifcFile (if necessary) optimized IFC file
     * @return Used ifc file schema as string
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
     * Checks if IfcSite element exists in data
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
     * Extracts the BIM object geometry and transform data to {@link BIMObject3D}
     *
     * @param rawBIMData to transform
     * @return transformed data for rendering
     */
    private List<BIMObject3D> transformToBIMData(BIMDataCollection rawBIMData) {
        List<BIMObject3D> transformedData = new ArrayList<>();
        List<BIMObject3D> slabs = BIMtoOSMUtility.transformBIMObjects(ifcModel, solutionType, BIMtoOSMCatalog.BIMObject.IfcSlab, rawBIMData.getAreaObjects());
        List<BIMObject3D> walls = BIMtoOSMUtility.transformBIMObjects(ifcModel, solutionType, BIMtoOSMCatalog.BIMObject.IfcWall, rawBIMData.getWallObjects());
        List<BIMObject3D> columns = BIMtoOSMUtility.transformBIMObjects(ifcModel, solutionType, BIMtoOSMCatalog.BIMObject.IfcColumn, rawBIMData.getColumnObjects());
//        List<BIMObject3D> doors = BIMtoOSMUtility.transformBIMObjects(ifcModel, solutionType, BIMtoOSMCatalog.BIMObject.IfcDoor, rawBIMData.getDoorObjects());
//        List<BIMObject3D> windows = BIMtoOSMUtility.transformBIMObjects(ifcModel, solutionType, BIMtoOSMCatalog.BIMObject.IfcWindow, rawBIMData.getWindowObjects());
        List<BIMObject3D> stairs = BIMtoOSMUtility.transformBIMObjects(ifcModel, solutionType, BIMtoOSMCatalog.BIMObject.IfcStair, rawBIMData.getStairObjects());
        transformedData.addAll(slabs);
        transformedData.addAll(walls);
        transformedData.addAll(columns);
//        transformedData.addAll(doors);
//        transformedData.addAll(windows);
        transformedData.addAll(stairs);
        return transformedData;
    }

    /**
     * Method packs prepared BIM data into OSM ways and nodes
     *
     * @param preparedBIMData to transform to OSM data
     * @return packed data as {@link DataSet}
     */
    private DataSet packIntoOSMData(ArrayList<BIMObject3D> preparedBIMData) {
        ArrayList<Way> ways = new ArrayList<>();
        ArrayList<Node> nodes = new ArrayList<>();
        ArrayList<Pair<Double, Integer>> levelIdentifier = extractAndIdentifyLevels();

        for (BIMObject3D object : preparedBIMData) {
            int level = getLevelTag(object, levelIdentifier);

            ArrayList<Node> tmpNodes = new ArrayList<>();
            for (LatLon point : object.getGeodeticGeometryCoordinates()) {
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
            if (level != DEFAULT_LEVEL) w.put(new Tag("level", Integer.toString(level)));
            ways.add(w);
        }

        DataSet ds = new DataSet();
        nodes.forEach(ds::addPrimitive);
        ways.forEach(ds::addPrimitive);
        return ds;
    }

    /**
     * Method gets level tag of PreparedBIMObject3D
     *
     * @param object              to get level tag for
     * @param levelIdentifierList with identified levels
     * @return level
     */
    private int getLevelTag(BIMObject3D object, ArrayList<Pair<Double, Integer>> levelIdentifierList) {
        int level = DEFAULT_LEVEL;

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
                    // if of type IfcBuildingStorey
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
                rotationMatrix = ParserMath.getRotationMatrixZ(rotationAngle);
            }

            if (rotationMatrix == null) return;

            for (BIMObject3D object : preparedBIMData) {
                ArrayList<LatLon> transformedCoordinates = new ArrayList<>();
                for (Vector3D point : object.getCartesianGeometryCoordinates()) {
                    // rotate point
                    rotationMatrix.transform(point);
                    // transform point
                    LatLon llPoint = ParserGeoMath.cartesianToGeodetic(point, new Vector3D(0.0, 0.0, 0.0), llBuildingOrigin, lengthUnit);
                    transformedCoordinates.add(llPoint);
                }
                object.setGeodeticGeometryCoordinates(transformedCoordinates);
            }
        }
    }

    /**
     * Method calculates the latlon coordinates of building origin corner
     *
     * @param ifcSite IfcSite entity
     * @return latlon coordinates of building corner
     */
    @SuppressWarnings("unchecked")
    private LatLon getLatLonBuildingOrigin(EntityInstance ifcSite) {
        Vector3D ifcSiteOffset = null;
        if (ifcSite.getAttributeValueBNasEntityInstance("Representation") != null) {
            // get the offset between IfcSite geodetic coordinates and building origin coordinate
            // handle IfcSite offset if IfcBoundingBox representation
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

        // get RefLatitude and RefLongitude of IfcSite
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
                            if (unitPrefix.equals(".CENTI.")) lengthUnit = IfcUnitCatalog.LengthUnit.CM;
                            if (unitPrefix.equals(".MILLI.")) lengthUnit = IfcUnitCatalog.LengthUnit.MM;
                            break;
                            // TODO handle more prefixes
                        } catch (NullPointerException e) {
                            // do nothing
                        }
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
     * @param filepath of ifc file
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
        Logging.error(msg.replaceAll("\n", " "));
        if (!GraphicsEnvironment.isHeadless()) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                    msg,
                    tr("Error"),
                    JOptionPane.ERROR_MESSAGE));
        }
    }

}
