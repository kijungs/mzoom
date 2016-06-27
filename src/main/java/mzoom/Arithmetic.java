/* =================================================================================
 *
 * M-Zoom: Fast Dense Block Detection in Tensors with Quality Guarantees.
 * Authors: Kijung Shin, Bryan Hooi, and Christos Faloutsos
 *
 * Version: 1.0
 * Date: March 10, 2016
 * Main Contact: Kijung Shin (kijungs@cs.cmu.edu)
 *
 * This software is free of charge under research purposes.
 * For commercial purposes, please contact the author.
 *
 * =================================================================================
 */

package mzoom;

/**
 * Arithmetic Average Mass, one of density measures
 * @author Kijung Shin (kijungs@cs.cmu.edu)
 */
public class Arithmetic implements IDensityMeasure {

    private int dimension;
    private long mass;
    private int sumOfCardinalities;

    public double initialize(Tensor tensor) {
        this.dimension = tensor.dimension;
        this.mass = tensor.mass;
        sumOfCardinalities = 0;
        for(int dim = 0; dim < dimension; dim++) {
            sumOfCardinalities += tensor.cardinalities[dim];
        }
        return density(mass, sumOfCardinalities);
    }

    public double ifRemoved(int attribute, int mass) {
        return density(this.mass - mass, sumOfCardinalities - 1);
    }

    public double remove(int attribute, int mass) {
        this.mass -= mass;
        sumOfCardinalities -= 1;
        return density(this.mass, sumOfCardinalities);
    }

    public double density(long sumOfPart, int[] cardinalities) {
        int sumOfattributeLengthsPart = 0;
        for(int dim = 0; dim < dimension; dim++) {
            sumOfattributeLengthsPart += cardinalities[dim];
        }
        return density(sumOfPart, sumOfattributeLengthsPart);
    }

    private double density(long sumOfPart, double sumOfCardinalities) {
        if(sumOfCardinalities == 0)
            return - 1;
        return (sumOfPart + 0.0) / sumOfCardinalities * dimension;
    }
}
