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
 * Suspiciousness, one of density measures
 * @author Kijung Shin (kijungs@cs.cmu.edu)
 */
public class Suspiciousness implements IDensityMeasure {

    private int dimension;
    private int[] cardinalities;
    private long massOfAll; //sum of entries
    private long massOfBlock;
    private double productOfCardinalitiesOfAll;
    private double productOfCardinalitiesOfBlock;

    public double initialize(Tensor tensor) {
        this.dimension = tensor.dimension;
        this.cardinalities = tensor.cardinalities.clone();
        this.massOfAll = tensor.mass;
        this.massOfBlock = tensor.mass;
        productOfCardinalitiesOfAll = 1;
        for(int dim = 0; dim < dimension; dim++) {
            productOfCardinalitiesOfAll *= cardinalities[dim];
        }
        productOfCardinalitiesOfBlock = productOfCardinalitiesOfAll;
        return density(massOfBlock, productOfCardinalitiesOfBlock);
    }

    public double ifRemoved(int attribute, int mass) {
        return density(massOfBlock - mass, productOfCardinalitiesOfBlock / cardinalities[attribute] * (cardinalities[attribute] - 1));
    }

    public double remove(int attribute, int mass) {
        cardinalities[attribute]--;
        productOfCardinalitiesOfBlock = productOfCardinalities(cardinalities); //recompute due to the precision error
        massOfBlock -= mass;
        return density(massOfBlock, productOfCardinalitiesOfBlock);
    }

    public double density(long massOfBlock, int[] cardinalitiesOfBlock) {
        double productOfCardinalitiesOfBlock = 1;
        for(int dim = 0; dim < dimension; dim++) {
            productOfCardinalitiesOfBlock *= cardinalitiesOfBlock[dim];
        }
        return density(massOfBlock, productOfCardinalitiesOfBlock);
    }

    private double density(long massOfBlock, double productOfCardinalitiesOfBlock) {
        if(productOfCardinalitiesOfBlock == 0 || massOfBlock == 0)
            return - 1;
        return massOfBlock * (Math.log((massOfBlock+0.0)/ massOfAll) - 1) + massOfAll * productOfCardinalitiesOfBlock / productOfCardinalitiesOfAll - massOfBlock * Math.log (productOfCardinalitiesOfBlock / productOfCardinalitiesOfAll);
    }

    public static double productOfCardinalities(int[] cardinalities){
        double productOfCardinalities = 1;
        for(int attribute = 0; attribute < cardinalities.length; attribute++) {
            productOfCardinalities *= cardinalities[attribute];
        }
        return productOfCardinalities;
    }
}
