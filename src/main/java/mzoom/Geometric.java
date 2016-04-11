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
 * Geometric Average Mass, one of density measures
 * @author Kijung Shin (kijungs@cs.cmu.edu)
 */
public class Geometric implements IDensityMeasure {

    private int dimension;
    private int[] cardinalities;
    private long mass;
    private double productOfCardinalities;

    public double initialize(Tensor tensor) {
        this.dimension = tensor.dimension;
        this.cardinalities = tensor.cardinalities.clone();
        this.mass = tensor.mass;
        productOfCardinalities = 1;
        for(int dim = 0; dim < dimension; dim++) {
            productOfCardinalities *= cardinalities[dim];
        }
        return density(mass, productOfCardinalities);
    }

    public double ifRemoved(int attribute, int mass) {
        return density(this.mass - mass, productOfCardinalities / cardinalities[attribute] * (cardinalities[attribute] - 1));
    }

    public double remove(int attribute, int mass) {
        cardinalities[attribute]--;
        productOfCardinalities = Suspiciousness.productOfCardinalities(cardinalities); //recompute due to the precision error
        this.mass -= mass;
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
