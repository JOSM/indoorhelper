// License: AGPL. For details, see LICENSE file.
package io.parser.math;

/**
 * Class keeping/handling data of 3D vector
 *
 * @author rebsc
 */
public class Vector3D {
    private double x;
    private double y;
    private double z;

    public Vector3D() {
        x = 0.0;
        y = 0.0;
        z = 0.0;
    }

    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3D(Vector3D vector) {
        x = vector.x;
        y = vector.y;
        z = vector.z;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getX() {
        return x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getY() {
        return y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getZ() {
        return z;
    }

    public boolean equalsVector(Vector3D vec) {
        if (Math.abs(this.x - vec.x) > .0000001) return false;
        if (Math.abs(this.y - vec.y) > .0000001) return false;
        if (Math.abs(this.z - vec.z) > .0000001) return false;

        return true;
    }

    /**
     * Adds param vector to this
     *
     * @param vector to add
     */
    public void add(Vector3D vector) {
        this.x += vector.x;
        this.y += vector.y;
        this.z += vector.z;
    }

    public void sub(Vector3D vector) {
        this.x -= vector.x;
        this.y -= vector.y;
        this.z -= vector.z;
    }

    /**
     * Normalizes this vector
     */
    public void normalize() {
        double n = 1.0 / Math.sqrt(Math.pow(x, 2.0) + Math.pow(y, 2.0) + Math.pow(z, 2.0));
        this.x *= n;
        this.y *= n;
        this.z *= n;
    }

    /**
     * Sets values of this vector to normalized values from param vector
     *
     * @param vector to normalize and use as source for this vector
     */
    public void normalize(Vector3D vector) {
        double n = 1.0 / Math.sqrt(Math.pow(vector.x, 2.0) + Math.pow(vector.y, 2.0) + Math.pow(vector.z, 2.0));
        this.x *= n;
        this.y *= n;
        this.z *= n;
    }

    /**
     * Sets values of this vector to cross product of params vector1, vector2
     *
     * @param vector1 first vector
     * @param vector2 second vector
     */
    public void cross(Vector3D vector1, Vector3D vector2) {
        this.x = vector1.y * vector2.z - vector1.z * vector2.y;
        this.y = vector2.x * vector1.z - vector2.z * vector1.x;
        this.z = vector1.x * vector2.y - vector1.y * vector2.x;
    }

    /**
     * Returns dot product of this vector and param vector
     *
     * @param vector for operation
     * @return dot product of this vector and param vector
     */
    public double dot(Vector3D vector) {
        return (this.x * vector.x + this.y * vector.y + this.z * vector.z);
    }

    /**
     * Returns squared length of this vector
     *
     * @return squared length of this vector
     */
    public double lengthSquared() {
        return (this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public void scale(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
        this.z *= scalar;
    }

    public double angleBetween(Vector3D vector) {
        if (this.equalsVector(vector)) return 0.0;

        // check sign - vector order is important for rotation
        double result2DWithSign = Math.atan2(this.getY(), this.getX()) - Math.atan2(vector.getY(), vector.getX());

        double ab;
        double a_abs;
        double b_abs;
        double r;
        if (this.getZ() == 0.0 && vector.getZ() == 0.0) {
            return result2DWithSign;
        } else {
            ab = this.getX() * vector.getX() + this.getY() * vector.getY() + this.getZ() * vector.getZ();
            a_abs = Math.sqrt(Math.pow(this.getX(), 2.0) + Math.pow(this.getY(), 2.0) + Math.pow(this.getZ(), 2.0));
            b_abs = Math.sqrt(Math.pow(vector.getX(), 2.0) + Math.pow(vector.getY(), 2.0) + Math.pow(vector.getZ(), 2.0));
            r = ab / (a_abs * b_abs);
            if (result2DWithSign < 0 && Math.acos(r) > 0) return -Math.acos(r);
            return Math.acos(r);

        }
    }

}
