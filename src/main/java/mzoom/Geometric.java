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
 * Geometric Average Mass, one of density measures
 * @author Kijung Shin (kijungs@cs.cmu.edu)
 */
public class Geometric implements IDensityMeasure {

    private int dimension;
    private int[] cardinalities;
    private long mass;
    private double productOfCardinalities;

    public double initialize(int dimension, int[] cardinalities, long mass) {
        this.dimension = dimension;
        this.cardinalities = cardinalities.clone();
        this.mass = mass;
        productOfCardinalities = 1;
        for(int dim = 0; dim < dimension; dim++) {
            productOfCardinalities *= cardinalities[dim];
        }
        return density(mass, productOfCardinalities);
    }

    public double initialize(int dimension, int[] cardinalitiesOfAll, long massOfAll, int[] cardinaltiesOfBlock, long massOfBlock) {
        this.dimension = dimension;
        this.cardinalities = cardinaltiesOfBlock.clone();
        this.mass = massOfBlock;
        productOfCardinalities = 1;
        for(int dim = 0; dim < dimension; dim++) {
            productOfCardinalities *= cardinaltiesOfBlock[dim];
        }
        return density(mass, productOfCardinalities);
    }

    public double ifRemoved(int attribute, int numValues, long sumOfMasses) {
        return density(this.mass - sumOfMasses, productOfCardinalities / cardinalities[attribute] * (cardinalities[attribute] - numValues));
    }

    public double ifInserted(int attribute, int numValues, long sumOfMasses) {
        return density(this.mass + sumOfMasses, productOfCardinalities / cardinalities[attribute] * (cardinalities[attribute] + numValues));
    }

    public double remove(int attribute, int numValues, long sumOfMasses) {
        cardinalities[attribute] -= numValues;
        productOfCardinalities = Suspiciousness.productOfCardinalities(cardinalities); //recompute due to the precision error
        this.mass -= sumOfMasses;
        return density(this.mass, productOfCardinalities);
    }

    public double insert(int attribute, int numValues, long sumOfMasses) {
        cardinalities[attribute] += numValues;
        productOfCardinalities = Suspiciousness.productOfCardinalities(cardinalities); //recompute due to the precision error
        this.mass += sumOfMasses;
        return density(this.mass, productOfCardinalities);
    }

    public double density(long mass, int[] cardinalities) {
        double productOfCardinalities = 1;
        for(int dim = 0; dim < dimension; dim++) {
            productOfCardinalities *= cardinalities[dim];
        }
        return density(mass, productOfCardinalities);
    }

    private double density(double mass, double productOfCardinalities) {
        if(productOfCardinalities == 0)
            return - 1;
        return mass / Math.pow(productOfCardinalities, 1.0/dimension);
    }
}
