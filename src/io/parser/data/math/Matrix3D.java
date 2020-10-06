package io.parser.data.math;

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

    public Matrix3D(){
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

    /**
     * Sets the values of this matrix to multiply result of this matrix ans param matrix
     * @param matrix to multiply
     */
    public void multiply(Matrix3D matrix){
        this.m00 = this.m00 * matrix.m00 + this.m01 * matrix.m10 + this.m02 * matrix.m20;
        this.m01 = this.m00 * matrix.m01 + this.m01 * matrix.m11 + this.m02 * matrix.m21;
        this.m02 = this.m00 * matrix.m02 + this.m01 * matrix.m12 + this.m02 * matrix.m22;

        this.m10 = this.m10 * matrix.m00 + this.m11 * matrix.m10 + this.m12 * matrix.m20;
        this.m11 = this.m10 * matrix.m01 + this.m11 * matrix.m11 + this.m12 * matrix.m21;
        this.m12 = this.m10 * matrix.m02 + this.m11 * matrix.m12 + this.m12 * matrix.m22;

        this.m20 = this.m20 * matrix.m00 + this.m21 * matrix.m10 + this.m22 * matrix.m20;
        this.m21 = this.m20 * matrix.m01 + this.m21 * matrix.m11 + this.m22 * matrix.m21;
        this.m22 = this.m20 * matrix.m02 + this.m21 * matrix.m12 + this.m22 * matrix.m22;

    }
}
