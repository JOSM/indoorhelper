// License: GPL. For details, see LICENSE file.
package parser.data;

/**
 * Class holding coordinates of 3D point
 * @author rebsc
 *
 */
public class Point3D {
	private double x;
	private double y;
	private double z;

	public Point3D(double x, double y, double z) {
		this.setX(x);
		this.setY(y);
		this.setZ(z);
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}
}
