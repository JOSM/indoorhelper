// License: GPL. For details, see LICENSE file.
package parser.data;

import model.io.BIMtoOSMCatalog;

/**
 * Class holding OSM relevant data of a 3D BIM object
 * @author rebsc
 *
 */
public class PreparedBIMObject3D {

	private BIMtoOSMCatalog.BIMObject type;
	private Point3D cartesianCorner;
	//private Dimension3D dimension;

	public PreparedBIMObject3D(BIMtoOSMCatalog.BIMObject type, Point3D cartesianCorner) {
		this.setType(type);
		this.setCartesianCorner(cartesianCorner);
	}

	public BIMtoOSMCatalog.BIMObject getType() {
		return type;
	}

	public void setType(BIMtoOSMCatalog.BIMObject type) {
		this.type = type;
	}

	public Point3D getCartesianCorner() {
		return cartesianCorner;
	}

	public void setCartesianCorner(Point3D cartesianCorner) {
		this.cartesianCorner = cartesianCorner;
	}
}
