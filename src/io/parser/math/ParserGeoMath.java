// License: AGPL. For details, see LICENSE file.
package io.parser.math;

import io.parser.data.ifc.IfcUnitCatalog;
import io.parser.data.math.Vector3D;
import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.josm.data.coor.LatLon;

/**
 * Class providing math functions for geodetic operations
 *
 * @author rebsc
 */
public class ParserGeoMath {

    /**
     * Method transforms cartesian point to latlon point with given latlon origin coordinate (latlon for cartesian 0.0/0.0)
     * and cartesian unit like m or cm
     *
     * @param cartesianPoint          to translate to latlon
     * @param cartesianOrigin         cartesian representation of latLonOfCartesianOrigin
     * @param latLonOfCartesianOrigin latlon of cartesian origin (0.0/0.0)
     * @param cartesianUnit           m or cm
     * @return latlon of cartesian point
     */
    public static LatLon cartesianToGeodetic(Vector3D cartesianPoint, Vector3D cartesianOrigin, LatLon latLonOfCartesianOrigin, IfcUnitCatalog.LENGTHUNIT cartesianUnit) {
        double originCartX = cartesianOrigin.getX();
        double originCartY = cartesianOrigin.getY();
        double originLat = Math.toRadians(latLonOfCartesianOrigin.lat());
        double originLon = Math.toRadians(latLonOfCartesianOrigin.lon());
        double pointX = cartesianPoint.getX();
        double pointY = cartesianPoint.getY();

        // get bearing
        double bearing = Math.atan2(pointY - originCartY, pointX - originCartX);
        bearing = Math.toRadians(90.0) - bearing;

        // get distance
        if (cartesianUnit.equals(IfcUnitCatalog.LENGTHUNIT.CM)) {
            pointX /= 100.0;
            pointY /= 100.0;
        } else if (cartesianUnit.equals(IfcUnitCatalog.LENGTHUNIT.MM)) {
            pointX /= 1000.0;
            pointY /= 1000.0;
        }
        double d = Math.sqrt(Math.pow((pointX - originCartX), 2) + Math.pow((pointY - originCartY), 2));

        double pointLat = Math.asin(
                Math.sin(originLat) * Math.cos(d / OsmMercator.EARTH_RADIUS) +
                        Math.cos(originLat) * Math.sin(d / OsmMercator.EARTH_RADIUS) * Math.cos(bearing));
        double pointLon = originLon +
                Math.atan2(
                        Math.sin(bearing) * Math.sin(d / OsmMercator.EARTH_RADIUS) * Math.cos(originLat),
                        Math.cos(d / OsmMercator.EARTH_RADIUS) - Math.sin(originLat) * Math.sin(pointLat));

        return new LatLon(Math.toDegrees(pointLat), Math.toDegrees(pointLon));
    }

    public static double degreeMinutesSecondsToLatLon(double degrees, double minutes, double seconds) {
        return degrees + (minutes / 60.0) + (seconds / 3600.0);
    }

}
