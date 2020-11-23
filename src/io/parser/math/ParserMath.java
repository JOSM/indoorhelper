// License: AGPL. For details, see LICENSE file.
package io.parser.math;

/**
 * Class providing transformation and rotation methods
 *
 * @author rebsc
 */
public class ParserMath {

    /**
     * Creates rotation matrix about x-axis from rotAngle
     * https://en.wikipedia.org/wiki/Rotation_matrix
     *
     * @param rotAngle in rad
     * @return rotation matrix about x-axis
     */
    public static Matrix3D getRotationMatrixAboutXAxis(double rotAngle) {
        return new Matrix3D(
                1.0, 0.0, 0.0,
                0.0, Math.cos(rotAngle), -Math.sin(rotAngle),
                0.0, Math.sin(rotAngle), Math.cos(rotAngle));
    }

    /**
     * Creates rotation matrix about y-axis from rotAngle
     * https://en.wikipedia.org/wiki/Rotation_matrix
     *
     * @param rotAngle in rad
     * @return rotation matrix about y-axis
     */
    public static Matrix3D getRotationMatrixAboutYAxis(double rotAngle) {
        return new Matrix3D(
                Math.cos(rotAngle), 0.0, Math.sin(rotAngle),
                0.0, 1.0, 0.0,
                -Math.sin(rotAngle), 0.0, Math.cos(rotAngle));
    }

    /**
     * Creates rotation matrix about z-axis from rotAngle
     * https://en.wikipedia.org/wiki/Rotation_matrix
     *
     * @param rotAngle in rad
     * @return rotation matrix about z-axis
     */
    public static Matrix3D getRotationMatrixAboutZAxis(double rotAngle) {
        return new Matrix3D(
                Math.cos(rotAngle), -Math.sin(rotAngle), 0.0,
                Math.sin(rotAngle), Math.cos(rotAngle), 0.0,
                0.0, 0.0, 1.0);
    }

}
