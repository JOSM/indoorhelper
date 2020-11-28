// License: AGPL. For details, see LICENSE file.
package io.parser.math;

/**
 * Class keeping/handling data of 3D matrix
 *
 * @author rebsc
 */
public class Matrix3D {

    private double m00;
    private double m01;
    private double m02;
    private double m10;
    private double m11;
    private double m12;
    private double m20;
    private double m21;
    private double m22;

    public Matrix3D() {
        this.m00 = 0.0;
        this.m01 = 0.0;
        this.m02 = 0.0;
        this.m10 = 0.0;
        this.m11 = 0.0;
        this.m12 = 0.0;
        this.m20 = 0.0;
        this.m21 = 0.0;
        this.m22 = 0.0;
    }

    public Matrix3D(double m00, double m01, double m02,
                    double m10, double m11, double m12,
                    double m20, double m21, double m22) {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
    }

    public Matrix3D(Matrix3D matrix) {
        this.m00 = matrix.m00;
        this.m01 = matrix.m01;
        this.m02 = matrix.m02;
        this.m10 = matrix.m10;
        this.m11 = matrix.m11;
        this.m12 = matrix.m12;
        this.m20 = matrix.m20;
        this.m21 = matrix.m21;
        this.m22 = matrix.m22;
    }

    public void setM00(double m00) {
        this.m00 = m00;
    }

    public double getM00() {
        return m00;
    }

    public void setM01(double m01) {
        this.m01 = m01;
    }

    public double getM01() {
        return m01;
    }

    public void setM02(double m02) {
        this.m02 = m02;
    }

    public double getM02() {
        return m02;
    }

    public void setM10(double m10) {
        this.m10 = m10;
    }

    public double getM10() {
        return m10;
    }

    public void setM11(double m11) {
        this.m11 = m11;
    }

    public double getM11() {
        return m11;
    }

    public void setM12(double m12) {
        this.m12 = m12;
    }

    public double getM12() {
        return m12;
    }

    public void setM20(double m20) {
        this.m20 = m20;
    }

    public double getM20() {
        return m20;
    }

    public void setM21(double m21) {
        this.m21 = m21;
    }

    public double getM21() {
        return m21;
    }

    public void setM22(double m22) {
        this.m22 = m22;
    }

    public double getM22() {
        return m22;
    }

    public void print() {
        System.out.print(this.getM00() + ", ");
        System.out.print(this.getM01() + ", ");
        System.out.print(this.getM02() + "\n");
        System.out.print(this.getM10() + ", ");
        System.out.print(this.getM11() + ", ");
        System.out.print(this.getM12() + "\n");
        System.out.print(this.getM20() + ", ");
        System.out.print(this.getM21() + ", ");
        System.out.print(this.getM22() + "\n");
    }

    public boolean equalsMatrix(Matrix3D matrix) {
        if (this.m00 != matrix.m00) return false;
        if (this.m01 != matrix.m01) return false;
        if (this.m02 != matrix.m02) return false;
        if (this.m10 != matrix.m10) return false;
        if (this.m11 != matrix.m11) return false;
        if (this.m12 != matrix.m12) return false;
        if (this.m20 != matrix.m20) return false;
        if (this.m21 != matrix.m21) return false;
        if (this.m22 != matrix.m22) return false;

        return true;
    }

    /**
     * Sets this matrix to identity
     */
    public void setIdentity() {
        this.m00 = 1.0;
        this.m01 = 0.0;
        this.m02 = 0.0;

        this.m10 = 0.0;
        this.m11 = 1.0;
        this.m12 = 0.0;

        this.m20 = 0.0;
        this.m21 = 0.0;
        this.m22 = 1.0;
    }

    /**
     * Set values of this matrix to values from param matrix
     *
     * @param matrix to get values from
     */
    public void set(Matrix3D matrix) {
        this.m00 = matrix.m00;
        this.m01 = matrix.m01;
        this.m02 = matrix.m02;
        this.m10 = matrix.m10;
        this.m11 = matrix.m11;
        this.m12 = matrix.m12;
        this.m20 = matrix.m20;
        this.m21 = matrix.m21;
        this.m22 = matrix.m22;
    }

    /**
     * Sets the values of this matrix to multiply result of this matrix with param matrix
     *
     * @param matrix to multiply
     */
    public void multiply(Matrix3D matrix) {
        double n00 = this.m00 * matrix.m00 + this.m01 * matrix.m10 + this.m02 * matrix.m20;
        double n01 = this.m00 * matrix.m01 + this.m01 * matrix.m11 + this.m02 * matrix.m21;
        double n02 = this.m00 * matrix.m02 + this.m01 * matrix.m12 + this.m02 * matrix.m22;

        double n10 = this.m10 * matrix.m00 + this.m11 * matrix.m10 + this.m12 * matrix.m20;
        double n11 = this.m10 * matrix.m01 + this.m11 * matrix.m11 + this.m12 * matrix.m21;
        double n12 = this.m10 * matrix.m02 + this.m11 * matrix.m12 + this.m12 * matrix.m22;

        double n20 = this.m20 * matrix.m00 + this.m21 * matrix.m10 + this.m22 * matrix.m20;
        double n21 = this.m20 * matrix.m01 + this.m21 * matrix.m11 + this.m22 * matrix.m21;
        this.m22 = this.m20 * matrix.m02 + this.m21 * matrix.m12 + this.m22 * matrix.m22;

        this.m00 = n00;
        this.m01 = n01;
        this.m02 = n02;

        this.m10 = n10;
        this.m11 = n11;
        this.m12 = n12;

        this.m20 = n20;
        this.m21 = n21;
    }

    /**
     * Transforms param vector with using this matrix
     *
     * @param vec to transform
     */
    public void transform(Vector3D vec) {
        double x = this.m00 * vec.getX() + this.m01 * vec.getY() + this.m02 * vec.getZ();
        double y = this.m10 * vec.getX() + this.m11 * vec.getY() + this.m12 * vec.getZ();
        vec.setZ(this.m20 * vec.getX() + this.m21 * vec.getY() + this.m22 * vec.getZ());
        vec.setX(x);
        vec.setY(y);
    }

    /**
     * Sets the values of this matrix to multiply result of this matrix with param scalar
     *
     * @param scalar multiplier
     */
    public void multiply(double scalar) {
        this.m00 *= scalar;
        this.m01 *= scalar;
        this.m02 *= scalar;

        this.m10 *= scalar;
        this.m11 *= scalar;
        this.m12 *= scalar;

        this.m20 *= scalar;
        this.m21 *= scalar;
        this.m22 *= scalar;
    }

    /**
     * Inverts and sets this matrix
     */
    public void invert() {
        Matrix3D inverse = new Matrix3D();
        double det = det();
        if (det == 0) return;
        double factor = 1.0 / det;

        double inverse00 = this.m11 * this.m22 - this.m12 * this.m21;
        double inverse01 = this.m02 * this.m21 - this.m01 * this.m22;
        double inverse02 = this.m01 * this.m12 - this.m02 * this.m11;

        double inverse10 = this.m12 * this.m20 - this.m10 * this.m22;
        double inverse11 = this.m00 * this.m22 - this.m02 * this.m20;
        double inverse12 = this.m02 * this.m10 - this.m00 * this.m12;

        double inverse20 = this.m10 * this.m21 - this.m11 * this.m20;
        double inverse21 = this.m01 * this.m20 - this.m00 * this.m21;
        inverse.m22 = this.m00 * this.m11 - this.m01 * this.m10;

        inverse.m00 = inverse00;
        inverse.m01 = inverse01;
        inverse.m02 = inverse02;

        inverse.m10 = inverse10;
        inverse.m11 = inverse11;
        inverse.m12 = inverse12;

        inverse.m20 = inverse20;
        inverse.m21 = inverse21;

        inverse.multiply(factor);
        this.set(inverse);
    }

    /**
     * Calculates the determinant of this matrix
     *
     * @return determinant of matrix
     */
    public double det() {
        return (this.m00 * this.m11 * this.m22 +
                this.m01 * this.m12 * this.m20 +
                this.m02 * this.m10 * this.m21 -
                this.m02 * this.m11 * this.m20 -
                this.m01 * this.m10 * this.m22 -
                this.m00 * this.m12 * this.m21);
    }
}
