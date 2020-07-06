// License: GPL. For details, see LICENSE file.
package parser.data;

import java.util.ArrayList;

import model.io.BIMtoOSMCatalog;

/**
 * Class holding OSM relevant data of a 3D BIM object
 * @author rebsc
 *
 */
public class PreparedBIMObject3D {

	private BIMtoOSMCatalog.BIMObject type;
	private Point3D cartesianPlacement;
	private ArrayList<Point3D> cartesianShapeCoordinates;

	public PreparedBIMObject3D(BIMtoOSMCatalog.BIMObject type, Point3D cartesianCorner, ArrayList<Point3D> shapeCoordinates) {
		this.type = type;
		this.cartesianPlacement = cartesianCorner;
		this.cartesianShapeCoordinates = shapeCoordinates;
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
}
