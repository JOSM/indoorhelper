// License: AGPL. For details, see LICENSE file.
package io.parser.utils;

import io.parser.math.Vector3D;
import org.openstreetmap.josm.tools.Logging;

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


}
