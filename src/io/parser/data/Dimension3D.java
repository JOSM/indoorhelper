// License: AGPL. For details, see LICENSE file.
package io.parser.data;

/**
 * Class holding dimension values of 3D object
 * @author rebsc
 *
 */
public class Dimension3D {
	private double dimX;
	private double dimY;
	private double dimZ;

	Dimension3D(double dimensionX, double dimensionY, double dimensionZ){
		setDimX(dimensionX);
		setDimY(dimensionY);
		setDimZ(dimensionZ);
	}

	public double getDimX() {
		return dimX;
	}

	public void setDimX(double dimX) {
		this.dimX = dimX;
	}

	public double getDimY() {
		return dimY;
	}

	public void setDimY(double dimY) {
		this.dimY = dimY;
	}

	public double getDimZ() {
		return dimZ;
	}

	public void setDimZ(double dimZ) {
		this.dimZ = dimZ;
	}
}
