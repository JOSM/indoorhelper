// License: AGPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.indoorhelper.io.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Class holding information of BIM to OSM translation schema
 */
public class BIMtoOSMCatalog {
    /**
     * Returns all BIM tags representing a wall
     *
     * @return List of {@link BIMObject}s representing a wall
     */
    public static List<String> getWallTags() {
        ArrayList<String> wallTags = new ArrayList<>();
        wallTags.add(BIMObject.IfcWall.toString());
        wallTags.add(BIMObject.IfcWallStandardCase.toString());
        wallTags.add(BIMObject.IfcWallElementedCase.toString());
        return wallTags;
    }

    /**
     * Returns all BIM tags representing an area
     *
     * @return List of {@link BIMObject}s representing an area
     */
    public static List<String> getAreaTags() {
        ArrayList<String> areaTags = new ArrayList<>();
        areaTags.add(BIMObject.IfcSlab.toString());
        areaTags.add(BIMObject.IfcSlabStandardCase.toString());
        areaTags.add(BIMObject.IfcSlabElementedCase.toString());
        return areaTags;
    }

    /**
     * Returns all BIM tags representing a column
     *
     * @return List of {@link BIMObject}s representing a column
     */
    public static List<String> getColumnTags() {
        ArrayList<String> colTags = new ArrayList<>();
        colTags.add(BIMObject.IfcColumn.toString());
        return colTags;
    }

    /**
     * Returns all BIM tags representing a door
     *
     * @return List of {@link BIMObject}s representing a door
     */
    public static List<String> getDoorTags() {
        ArrayList<String> doorTags = new ArrayList<>();
        doorTags.add(BIMObject.IfcDoor.toString());
        doorTags.add(BIMObject.IfcDoorStandardCase.toString());
        return doorTags;
    }

    /**
     * Returns all BIM tags representing a window
     *
     * @return List of {@link BIMObject}s representing a window
     */
    public static List<String> getWindowTags() {
        ArrayList<String> stairTags = new ArrayList<>();
        stairTags.add(BIMObject.IfcWindow.toString());
        return stairTags;
    }

    /**
     * Returns all BIM tags representing a stair
     *
     * @return List of {@link BIMObject}s representing a stair
     */
    public static List<String> getStairTags() {
        ArrayList<String> stairTags = new ArrayList<>();
        stairTags.add(BIMObject.IfcStair.toString());
        return stairTags;
    }

    /**
     * Returns all BIM tags representing a RelVoidsElement
     *
     * @return List of {@link BIMObject}s representing a RelVoidsElement
     */
    public static List<String> getRelVoidsElementTags() {
        ArrayList<String> relVoidsTags = new ArrayList<>();
        relVoidsTags.add(BIMObject.IfcRelVoidsElement.toString());
        return relVoidsTags;
    }

    /**
     * Get IfcSite tag
     *
     * @return List of IfcSite tags
     */
    public static List<String> getIFCSITETags() {
        ArrayList<String> ifcSiteTags = new ArrayList<>();
        ifcSiteTags.add(BIMObject.IfcSite.toString());
        return ifcSiteTags;
    }

    /**
     * Relevant BIM objects
     *
     * @author rebsc
     */
    public enum BIMObject {
        IfcSlab, IfcSlabStandardCase, IfcSlabElementedCase, IfcSlabType, IfcWall, IfcWallStandardCase,
        IfcWallElementedCase, IfcWallType, IfcColumn, IfcColumnType, IfcDoor, IfcDoorStandardCase, IfcStair,
        IfcRelVoidsElement, IfcWindow, IfcSite
    }
}
