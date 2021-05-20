// License: AGPL. For details, see LICENSE file.
package io.parser.math;

/**
 * Class providing transformation and rotation methods
 *
 * @author rebsc
 */
public class ParserMath {

    /**
     * Creates x-axis rotation matrix with given angle
     *
     * @param rotAngle in rad
     * @return x-axis rotation matrix
     */
    public static Matrix3D getRotationMatrixX(double rotAngle) {
        return new Matrix3D(
                1.0, 0.0, 0.0,
                0.0, Math.cos(rotAngle), -Math.sin(rotAngle),
                0.0, Math.sin(rotAngle), Math.cos(rotAngle));
    }

    /**
     * Creates y-axis rotation matrix with given angle
     *
     * @param rotAngle in rad
     * @return y-axis rotation matrix
     */
    public static Matrix3D getRotationMatrixY(double rotAngle) {
        return new Matrix3D(
                Math.cos(rotAngle), 0.0, Math.sin(rotAngle),
                0.0, 1.0, 0.0,
                -Math.sin(rotAngle), 0.0, Math.cos(rotAngle));
    }

    /**
     * Creates z-axis rotation matrix with given angle
     *
     * @param rotAngle in rad
     * @return z-axis rotation matrix
     */
    public static Matrix3D getRotationMatrixZ(double rotAngle) {
        return new Matrix3D(
                Math.cos(rotAngle), -Math.sin(rotAngle), 0.0,
                Math.sin(rotAngle), Math.cos(rotAngle), 0.0,
                0.0, 0.0, 1.0);
    }

}
