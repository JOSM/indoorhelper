// License: AGPL. For details, see LICENSE file.
package io.parser.data;

import java.util.ArrayList;

import org.openstreetmap.josm.data.coor.LatLon;

import io.model.BIMtoOSMCatalog;

/**
 * Class holding OSM relevant data of a 3D BIM object
 * @author rebsc
 *
 */
public class PreparedBIMObject3D {

	private int objectId;
	private BIMtoOSMCatalog.BIMObject type;
	private Point3D cartesianPlacement;
	private LatLon geodeticPlacement;
	private ArrayList<Point3D> cartesianShapeCoordinates;
	private ArrayList<LatLon> geodeticShapeCoordinates;

	public PreparedBIMObject3D(int id, BIMtoOSMCatalog.BIMObject type, Point3D cartesianCorner, ArrayList<Point3D> shapeCoordinates) {
		this.setObjectId(id);
		this.type = type;
		this.cartesianPlacement = cartesianCorner;
		this.geodeticPlacement = null;
		this.cartesianShapeCoordinates = shapeCoordinates;
		this.geodeticShapeCoordinates = null;
	}

	public BIMtoOSMCatalog.BIMObject getType() {
		return type;
	}

	public void setType(BIMtoOSMCatalog.BIMObject type) {
		this.type = type;
	}

	public Point3D getCartesianPlacement() {
		return cartesianPlacement;
	}

	public void setCartesianPlacement(Point3D cartesianCorner) {
		this.cartesianPlacement = cartesianCorner;
	}

	public ArrayList<Point3D> getCartesianShapeCoordinates() {
		return cartesianShapeCoordinates;
	}

	public void setCartesianShapeCoordinates(ArrayList<Point3D> cartesianShapeCoordinates) {
		this.cartesianShapeCoordinates = cartesianShapeCoordinates;
	}

	public int getObjectId() {
		return objectId;
	}

	public void setObjectId(int objectId) {
		this.objectId = objectId;
	}

	public LatLon getGeodeticPlacement() {
		return geodeticPlacement;
	}

	public void setGeodeticPlacement(LatLon geodeticPlacement) {
		this.geodeticPlacement = geodeticPlacement;
	}

	public ArrayList<LatLon> getGeodeticShapeCoordinates() {
		return geodeticShapeCoordinates;
	}

	public void setGeodeticShapeCoordinates(ArrayList<LatLon> geodeticShapeCoordinates) {
		this.geodeticShapeCoordinates = geodeticShapeCoordinates;
	}
}
