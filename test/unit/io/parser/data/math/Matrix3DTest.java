package io.parser.data.math;

//import org.junit.jupiter.api.Test;

import org.openstreetmap.josm.plugins.indoorhelper.io.parser.math.Matrix3D;
import org.openstreetmap.josm.plugins.indoorhelper.io.parser.math.Vector3D;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests of {@link Matrix3D} class.
 *
 * @author rebsc
 */
public class Matrix3DTest {
    /**
     * Setup test.
     */
    Vector3D vector1 = new Vector3D(2.1, 3.1, 9.4);

    Matrix3D matrix1 = new Matrix3D(
            1.2, 5.4, 7.1,
            3.0, 4.2, 9.2,
            10.1, 6.23, 9.0);

    Matrix3D matrix2 = new Matrix3D(
            4.9, 5.4, 9.55,
            0.1, 1.7, 1.1,
            8.1, 6.23, 99.1);

    Matrix3D matrix3 = new Matrix3D(
            1, 2, 0,
            2, 4, 1,
            2, 1, 0);

    Vector3D matrix1TransformedPoint1 = new Vector3D(86.0, 105.8, 125.123);

    @Test
    public void testTransform() {
        Vector3D v = new Vector3D(vector1);
        matrix1.transform(v);
        assertEquals(v.getX(), matrix1TransformedPoint1.getX(),0.005);
        assertEquals(v.getY(), matrix1TransformedPoint1.getY(),0.005);
        assertEquals(v.getZ(), matrix1TransformedPoint1.getZ(),0.005);
    }

    @Test
    public void testMultiply() {
        Matrix3D m = new Matrix3D(matrix1);
        m.multiply(10.0);
        assertEquals(m.getM00(), matrix1.getM00() * 10.0,0.005);
        assertEquals(m.getM01(), matrix1.getM01() * 10.0,0.005);
        assertEquals(m.getM02(), matrix1.getM02() * 10.0,0.005);

        assertEquals(m.getM10(), matrix1.getM10() * 10.0,0.005);
        assertEquals(m.getM11(), matrix1.getM11() * 10.0,0.005);
        assertEquals(m.getM12(), matrix1.getM12() * 10.0,0.005);

        assertEquals(m.getM20(), matrix1.getM20() * 10.0,0.005);
        assertEquals(m.getM21(), matrix1.getM21() * 10.0,0.005);
        assertEquals(m.getM22(), matrix1.getM22() * 10.0,0.005);

        Matrix3D m1 = new Matrix3D(matrix2);
        m1.multiply(8.5);
        assertEquals(m1.getM00(), matrix2.getM00() * 8.5,0.005);
        assertEquals(m1.getM01(), matrix2.getM01() * 8.5,0.005);
        assertEquals(m1.getM02(), matrix2.getM02() * 8.5,0.005);

        assertEquals(m1.getM10(), matrix2.getM10() * 8.5,0.005);
        assertEquals(m1.getM11(), matrix2.getM11() * 8.5,0.005);
        assertEquals(m1.getM12(), matrix2.getM12() * 8.5,0.005);

        assertEquals(m1.getM20(), matrix2.getM20() * 8.5,0.005);
        assertEquals(m1.getM21(), matrix2.getM21() * 8.5,0.005);
        assertEquals(m1.getM22(), matrix2.getM22() * 8.5,0.005);
    }

    @Test
    public void testInvert() {
        Matrix3D m = new Matrix3D(matrix3);
        m.invert();

        assertEquals(m.getM00(), -(1.0/3.0),0.005);
        assertEquals(m.getM01(), 0.0,0.005);
        assertEquals(m.getM02(), (2.0/3.0),0.005);

        assertEquals(m.getM10(), (2.0/3.0),0.005);
        assertEquals(m.getM11(), 0.0,0.005);
        assertEquals(m.getM12(), -(1.0/3.0),0.005);

        assertEquals(m.getM20(), -2.0,0.005);
        assertEquals(m.getM21(), 1.0,0.005);
        assertEquals(m.getM22(), 0.0,0.005);
    }

    @Test
    public void testDet(){
        assertEquals(matrix3.det(), 3.0);
    }
}