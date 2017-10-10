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
 * Arithmetic Average Mass, one of density measures
 * @author Kijung Shin (kijungs@cs.cmu.edu)
 */
public class Arithmetic implements IDensityMeasure {

    private int dimension;
    private long mass;
    private int sumOfCardinalities;

    public double initialize(int dimension, int[] cardinalities, long mass) {
        this.dimension = dimension;
        this.mass = mass;
        sumOfCardinalities = 0;
        for(int dim = 0; dim < dimension; dim++) {
            sumOfCardinalities += cardinalities[dim];
        }
        return density(mass, sumOfCardinalities);
    }

    public double initialize(int dimension, int[] cardinalitiesOfAll, long massOfAll, int[] cardinaltiesOfBlock, long massOfBlock) {
        this.dimension = dimension;
        this.mass = massOfBlock;
        sumOfCardinalities = 0;
        for(int dim = 0; dim < dimension; dim++) {
            sumOfCardinalities += cardinaltiesOfBlock[dim];
        }
        return density(mass, sumOfCardinalities);
    }

    public double ifRemoved(int attribute, int numValues, long sumOfMasses) {
        return density(this.mass - sumOfMasses, sumOfCardinalities - numValues);
    }

    public double ifInserted(int attribute, int numValues, long sumOfMasses) {
        return density(this.mass + sumOfMasses, sumOfCardinalities + numValues);
    }

    public double remove(int attribute, int numValues, long sumOfMasses) {
        this.mass -= sumOfMasses;
        sumOfCardinalities -= numValues;
        return density(this.mass, sumOfCardinalities);
    }

    public double insert(int attribute, int numValues, long sumOfMasses) {
        this.mass += sumOfMasses;
        sumOfCardinalities += numValues;
        return density(this.mass, sumOfCardinalities);
    }

    public double density(long sumOfPart, int[] cardinalities) {
        int sumOfCardinalitiesPart = 0;
        for(int dim = 0; dim < dimension; dim++) {
            sumOfCardinalitiesPart += cardinalities[dim];
        }
        return density(sumOfPart, sumOfCardinalitiesPart);
    }

    private double density(long sumOfPart, double sumOfCardinalities) {
        if(sumOfCardinalities == 0)
            return - 1;
        return (sumOfPart + 0.0) / sumOfCardinalities * dimension;
    }
}
