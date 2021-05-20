// License: AGPL. For details, see LICENSE file.
package io.parser.math;

import io.parser.data.ifc.IfcUnitCatalog;
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
    public static LatLon cartesianToGeodetic(Vector3D cartesianPoint, Vector3D cartesianOrigin, LatLon latLonOfCartesianOrigin, IfcUnitCatalog.LengthUnit cartesianUnit) {
        // TODO improve the way of transformation - clean up this method
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
        if (cartesianUnit.equals(IfcUnitCatalog.LengthUnit.CM)) {
            pointX /= 100.0;
            pointY /= 100.0;
        } else if (cartesianUnit.equals(IfcUnitCatalog.LengthUnit.MM)) {
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

    /**
     * Method calculates distance between latlon in meter
     * @param lat1 first point latitude
     * @param lon1 first point longitude
     * @param lat2 second point latitude
     * @param lon2 second point longitude
     * @return distance in meter
     */
    public static double getDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);  // deg2rad below
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (OsmMercator.EARTH_RADIUS * c)*1e3;
    }

}
