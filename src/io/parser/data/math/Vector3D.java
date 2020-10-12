package io.parser.data.math;

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

    public void print() {
        System.out.print(this.getX() + ",");
        System.out.print(this.getY() + ",");
        System.out.print(this.getZ() + "\n");
    }

    public boolean equalsVector(Vector3D vec) {
        if (this.x != vec.x) return false;
        if (this.y != vec.y) return false;
        if (this.z != vec.z) return false;

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

}
