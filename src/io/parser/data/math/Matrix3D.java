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
}
