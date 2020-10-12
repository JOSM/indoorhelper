// License: AGPL. For details, see LICENSE file.
package io.parser.math;

/**
 * Class providing transformation and rotation methods
 *
 * @author rebsc
 */
public class ParserMath {

    /**
     * Calculates angle between two vectors
     *
     * @param vector1 vector of parent system
     * @param vector2 vector of current system
     * @return angle between angle in rad
     */
    public static double getAngleBetweenVectors(double[] vector1, double[] vector2) {
        if (vector1.length != vector2.length) return Double.NaN;
        if (vector1[0] == vector2[0] && vector1[1] == vector2[1]) return 0.0;

        // check sign - vector order is important for rotation
        double result2DWithSign = Math.atan2(vector1[1], vector1[0]) - Math.atan2(vector2[1], vector2[0]);

        double ab;
        double a_abs;
        double b_abs;
        double r;
        if (vector1.length == 2) {
//			ab = vector1[0] * vector2[0] + vector1[1] * vector2[1];
//			a_abs = Math.sqrt(Math.pow(vector1[0], 2.0) + Math.pow(vector1[1], 2.0));
//			b_abs = Math.sqrt(Math.pow(vector2[0], 2.0) + Math.pow(vector2[1], 2.0));
//			r = ab / (a_abs + b_abs);
//			if(result2DWithSign < 0 && Math.acos(r) > 0)	return -(Math.acos(r));
            return result2DWithSign;
        } else if (vector1.length == 3) {
            ab = vector1[0] * vector2[0] + vector1[1] * vector2[1] + vector1[2] * vector2[2];
            a_abs = Math.sqrt(Math.pow(vector1[0], 2.0) + Math.pow(vector1[1], 2.0) + Math.pow(vector1[2], 2.0));
            b_abs = Math.sqrt(Math.pow(vector2[0], 2.0) + Math.pow(vector2[1], 2.0) + Math.pow(vector2[2], 2.0));
            r = ab / (a_abs * b_abs);
            if (result2DWithSign < 0 && Math.acos(r) > 0) return -Math.acos(r);
            return Math.acos(r);

        }
        return Double.NaN;
    }

    public static double[] rotate3DPoint(double[] point, double[][] rotationMatrix) {
        if (rotationMatrix == null || point == null) return point;
        double[] result = new double[3];
        result[0] = rotationMatrix[0][0] * point[0] + rotationMatrix[0][1] * point[1] + rotationMatrix[0][2] * point[2];
        result[1] = rotationMatrix[1][0] * point[0] + rotationMatrix[1][1] * point[1] + rotationMatrix[1][2] * point[2];
        result[2] = rotationMatrix[2][0] * point[0] + rotationMatrix[2][1] * point[1] + rotationMatrix[2][2] * point[2];
        return result;
    }

    /**
     * Creates rotation matrix about x-axis from rotAngle
     * https://en.wikipedia.org/wiki/Rotation_matrix
     *
     * @param rotAngle in rad
     * @return rotation matrix about x-axis
     */
    public static double[][] getRotationMatrixAboutXAxis(double rotAngle) {
        double[][] matrix = new double[3][3];
        matrix[0][0] = 1;
        matrix[0][1] = 0;
        matrix[0][2] = 0;
        matrix[1][0] = 0;
        matrix[1][1] = Math.cos(rotAngle);
        matrix[1][2] = -Math.sin(rotAngle);
        matrix[2][0] = 0;
        matrix[2][1] = Math.sin(rotAngle);
        matrix[2][2] = Math.cos(rotAngle);
        return matrix;
    }

    /**
     * Creates rotation matrix about y-axis from rotAngle
     * https://en.wikipedia.org/wiki/Rotation_matrix
     *
     * @param rotAngle in rad
     * @return rotation matrix about y-axis
     */
    public static double[][] getRotationMatrixAboutYAxis(double rotAngle) {
        double[][] matrix = new double[3][3];
        matrix[0][0] = Math.cos(rotAngle);
        matrix[0][1] = 0;
        matrix[0][2] = Math.sin(rotAngle);
        matrix[1][0] = 0;
        matrix[1][1] = 1;
        matrix[1][2] = 0;
        matrix[2][0] = -Math.sin(rotAngle);
        matrix[2][1] = 0;
        matrix[2][2] = Math.cos(rotAngle);
        return matrix;
    }

    /**
     * Creates rotation matrix about z-axis from rotAngle
     * https://en.wikipedia.org/wiki/Rotation_matrix
     *
     * @param rotAngle in rad
     * @return rotation matrix about z-axis
     */
    public static double[][] getRotationMatrixAboutZAxis(double rotAngle) {
        double[][] matrix = new double[3][3];
        matrix[0][0] = Math.cos(rotAngle);
        matrix[0][1] = -Math.sin(rotAngle);
        matrix[0][2] = 0;
        matrix[1][0] = Math.sin(rotAngle);
        matrix[1][1] = Math.cos(rotAngle);
        matrix[1][2] = 0;
        matrix[2][0] = 0;
        matrix[2][1] = 0;
        matrix[2][2] = 1;
        return matrix;
    }

}
