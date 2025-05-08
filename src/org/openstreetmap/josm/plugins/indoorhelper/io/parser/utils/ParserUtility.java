// License: AGPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.indoorhelper.io.parser.utils;

import org.openstreetmap.josm.plugins.indoorhelper.io.parser.math.Vector3D;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.Logging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parser utility methods
 */
public class ParserUtility {

    /**
     * Parses a string vector to {@link Vector3D}
     *
     * @param vector to parse
     * @return parsed vector
     */
    public static Vector3D stringVectorToVector3D(List<String> vector) {
        if (vector.isEmpty()) return null;
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        if (vector.size() == 2) {
            x = prepareDoubleString(vector.get(0));
            y = prepareDoubleString(vector.get(1));
        } else if (vector.size() > 2) {
            x = prepareDoubleString(vector.get(0));
            y = prepareDoubleString(vector.get(1));
            z = prepareDoubleString(vector.get(2));
        }
        if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z)) {
            return null;
        }
        return new Vector3D(x, y, z);
    }

    /**
     * Parses string of double value from ifc file into double
     *
     * @param doubleString String of coordinate
     * @return double representing double
     */
    public static double prepareDoubleString(String doubleString) {
        if (doubleString == null) return Double.NaN;
        if (doubleString.endsWith(".")) {
            doubleString = doubleString + "0";
        }
        try {
            return Double.parseDouble(doubleString);
        } catch (NumberFormatException e) {
            Logging.error(e.getMessage());
            return Double.NaN;
        }
    }

    /**
     * Get list of levels included in dataset
     *
     * @param ds data set
     * @return Level list as {@link ArrayList}{@code <Integer>}}
     */
    public static ArrayList<Integer> getLevelList(DataSet ds) {
        ArrayList<Integer> l = new ArrayList<>();

        // TODO optimize this to avoid running thru all nodes and ways
        ds.getNodes().forEach(node -> {
            try {
                int level = Integer.parseInt(node.get("level"));
                if (!l.contains(level)) l.add(level);
            } catch (NumberFormatException e) {
                // do nothing
            }
        });
        ds.getWays().forEach(way -> {
            try {
                int level = Integer.parseInt(way.get("level"));
                if (!l.contains(level)) l.add(level);
            } catch (NumberFormatException e) {
                // do nothing
            }
        });
        Collections.sort(l);
        return l;
    }

    /**
     * Get level tag of node.
     *
     * @param node to get level tag
     * @return level tag or null
     */
    public static Number getLevelTag(Node node) {
        if (node == null) return null;
        if (node.get("level") != null) {
            return Integer.parseInt(node.get("level"));
        }
        if (!node.getParentWays().isEmpty()) {
            return Integer.parseInt(node.getParentWays().get(0).get("level"));
        }
        return null;
    }

    /**
     * Checks if both nodes are part of the same way
     *
     * @param node1 to check
     * @param node2 to check
     * @return true if both nodes are part of the same way, else false
     */
    public static boolean nodesPartOfSameWay(Node node1, Node node2) {
        for (Way way : node1.getParentWays()) {
            if (way.containsNode(node2)) return true;
        }
        return false;
    }

}
