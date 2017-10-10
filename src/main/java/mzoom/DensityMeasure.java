/* =================================================================================
 *
 * M-Zoom: Fast Dense Block Detection in Tensors with Quality Guarantees.
 * Authors: Kijung Shin, Bryan Hooi, and Christos Faloutsos
 *
 * Version: 2.0
 * Date: Nov 8, 2016
 * Main Contact: Kijung Shin (kijungs@cs.cmu.edu)
 *
 * This software is free of charge under research purposes.
 * For commercial purposes, please contact the author.
 *
 * =================================================================================
 */

package mzoom;

/**
 * Density Measures
 */
public class DensityMeasure {

    public static final int Suspiciousness = 0;
    public static final int Arithmetic = 1;
    public static final int Geometric = 2;
    public static final int EntrySurplus = 3;

    public int type;
    public double param = 1;

    public DensityMeasure(int type) {
        this.type = type;
    }

    public DensityMeasure(int type, double param) {
        this.type = type;
        this.param = param;
    }

}
