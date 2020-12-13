// License: AGPL. For details, see LICENSE file.
package io.parser.utils;

import io.parser.math.Vector3D;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.Logging;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser utility methods
 *
 * @author rebsc
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
     * Parses string of double value from IFC file into proper double
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
     * Returns a deep copy of the node list
     * @param listToCopy to create copy of
     * @return deep copy list
     */
    public static List<Node> deepCopyNodeList(List<Node> listToCopy){
        ArrayList<Node> nodesCopy = new ArrayList<>();
        listToCopy.forEach(n -> nodesCopy.add(new Node(n)));
        return nodesCopy;
    }

    /**
     * Returns a deep copy of the ways list
     * @param listToCopy to create copy of
     * @return deep copy list
     */
    public static List<Way> deepCopyWayList(List<Way> listToCopy){
        ArrayList<Way> waysCopy = new ArrayList<>();
        listToCopy.forEach(w -> {
            Way nW = new Way();
            nW.setOsmId(w.getId(), w.getVersion());
            w.getNodes().forEach(n -> nW.addNode(new Node(n)));
            waysCopy.add(nW);
        });
        return waysCopy;
    }

}
