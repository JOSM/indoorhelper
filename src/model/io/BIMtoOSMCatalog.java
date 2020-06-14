package model.io;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.osm.Tag;

/**
 * Class holding information of BIM to OSM translation schema
 * @author rebsc
 */
public class BIMtoOSMCatalog {

	/**
	 * Method to get related OSM tag of BIM object
	 * @param o BIMobject
	 * @return OSM tags of BIM object as list
	 */
    public List<Tag> getOSMTags(BIMObject o) {

        List<Tag> tagList = new ArrayList<>();

        switch(o) {
        case IfcSlab:
            tagList.add(new Tag("indoor", "area"));
            return tagList;
        case IfcSlabStandardCase:
            tagList.add(new Tag("indoor", "area"));
            return tagList;
        case IfcSlabElementedCase:
            tagList.add(new Tag("indoor", "area"));
            return tagList;
        case IfcWall:
            tagList.add(new Tag("indoor", "wall"));
            return tagList;
        case IfcWallStandardCase:
            tagList.add(new Tag("indoor", "wall"));
            return tagList;
        case IfcWallElementedCase:
            tagList.add(new Tag("indoor", "wall"));
            return tagList;
        case IfcColumn:
            tagList.add(new Tag("indoor", "area"));
            return tagList;
        case IfcDoor:
            tagList.add(new Tag("door", "yes"));
            return tagList;
        case IfcDoorStandardCase:
            tagList.add(new Tag("door", "yes"));
            return tagList;
        case IfcStair:
            tagList.add(new Tag("highway", "steps"));
            return tagList;
        default:
            tagList = null;
            return tagList;
        }
    }

    /**
     * Returns all BIM tags representing a wall.
     * @return List of BIMObjects representing a wall.
     */
    public static List<String> getWallTags() {
    	ArrayList<String> wallTags = new ArrayList<>();
    	wallTags.add(BIMObject.IfcWall.toString());
    	wallTags.add(BIMObject.IfcWall.toString().toUpperCase());
    	wallTags.add(BIMObject.IfcWall.toString().toLowerCase());
    	wallTags.add(BIMObject.IfcWallStandardCase.toString());
    	wallTags.add(BIMObject.IfcWallStandardCase.toString().toUpperCase());
    	wallTags.add(BIMObject.IfcWallStandardCase.toString().toLowerCase());
    	wallTags.add(BIMObject.IfcWallElementedCase.toString());
    	wallTags.add(BIMObject.IfcWallElementedCase.toString().toUpperCase());
    	wallTags.add(BIMObject.IfcWallElementedCase.toString().toLowerCase());
    	return wallTags;
    }

    /**
     * Returns all BIM tags representing an area.
     * @return List of BIMObjects representing an area.
     */
    public static List<String> getAreaTags() {
    	ArrayList<String> areaTags = new ArrayList<>();
    	areaTags.add(BIMObject.IfcSlab.toString());
    	areaTags.add(BIMObject.IfcSlab.toString().toUpperCase());
    	areaTags.add(BIMObject.IfcSlab.toString().toLowerCase());
    	areaTags.add(BIMObject.IfcSlabStandardCase.toString());
    	areaTags.add(BIMObject.IfcSlabStandardCase.toString().toUpperCase());
    	areaTags.add(BIMObject.IfcSlabStandardCase.toString().toLowerCase());
    	areaTags.add(BIMObject.IfcSlabElementedCase.toString());
    	areaTags.add(BIMObject.IfcSlabElementedCase.toString().toUpperCase());
    	areaTags.add(BIMObject.IfcSlabElementedCase.toString().toLowerCase());
    	return areaTags;
    }

    /**
     * Returns all BIM tags representing a column.
     * @return List of BIMObjects representing a column.
     */
    public static List<String> getColumnTags() {
    	ArrayList<String> colTags = new ArrayList<>();
    	colTags.add(BIMObject.IfcColumn.toString());
    	colTags.add(BIMObject.IfcColumn.toString().toUpperCase());
    	colTags.add(BIMObject.IfcColumn.toString().toLowerCase());
    	return colTags;
    }

    /**
     * Returns all BIM tags representing a door.
     * @return List of BIMObjects representing a door.
     */
    public static List<String> getDoorTags() {
    	ArrayList<String> doorTags = new ArrayList<>();
    	doorTags.add(BIMObject.IfcDoor.toString());
    	doorTags.add(BIMObject.IfcDoor.toString().toUpperCase());
    	doorTags.add(BIMObject.IfcDoor.toString().toLowerCase());
    	doorTags.add(BIMObject.IfcDoorStandardCase.toString());
      	doorTags.add(BIMObject.IfcDoorStandardCase.toString().toUpperCase());
      	doorTags.add(BIMObject.IfcDoorStandardCase.toString().toLowerCase());
    	return doorTags;
    }

    /**
     * Returns all BIM tags representing a stair.
     * @return List of BIMObjects representing a stair.
     */
    public static List<String> getStairTags() {
    	ArrayList<String> stairTags = new ArrayList<>();
    	stairTags.add(BIMObject.IfcStair.toString());
    	stairTags.add(BIMObject.IfcStair.toString().toUpperCase());
    	stairTags.add(BIMObject.IfcStair.toString().toLowerCase());
    	return stairTags;
    }

    /**
     * Get IFCSITE tag
     * @return List of IFCSITE tags
     */
    public static List<String> getIFCSITETags(){
    	String tag = "IfcSite";
    	ArrayList<String> ifcSiteTags = new ArrayList<>();
    	ifcSiteTags.add(tag);
    	ifcSiteTags.add(tag.toString().toUpperCase());
    	ifcSiteTags.add(tag.toString().toLowerCase());
    	return ifcSiteTags;
    }

    /**
     * Relevant BIM objects.
     * @author rebsc
     *
     */
	public enum BIMObject {
        IfcSlab, IfcSlabStandardCase, IfcSlabElementedCase, IfcSlabType, IfcWall, IfcWallStandardCase,
        IfcWallElementedCase, IfcWallType, IfcColumn, IfcColumnType, IfcDoor, IfcDoorStandardCase, IfcStair
    }
}
