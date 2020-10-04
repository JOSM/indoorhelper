// License: AGPL. For details, see LICENSE file.
package io.parser.data.math;

/**
 * Class holding coordinates of 3D point
 * @author rebsc
 *
 */
public class Point3D {
	private double x;
	private double y;
	private double z;

	public Point3D(){
		x = 0.0;
		y = 0.0;
		z = 0.0;
	}
	public Point3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
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

	public boolean equalsPoint3D(Point3D point) {
		return (this.x == point.getX() && this.y == point.getY() && this.z == point.getZ());
	}
}
