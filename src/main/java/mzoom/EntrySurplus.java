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
 * Entry Surplus, one of density measures
 * @author Kijung Shin (kijungs@cs.cmu.edu)
 */
public class EntrySurplus implements IDensityMeasure {

    private int dimension;
    private int[] cardinalities;
    private long massOfAll; //sum of entries
    private long massOfBlock;
    private double productOfCardinalitiesOfAll;
    private double productOfCardinalitiesOfBlock;
    private double alpha = 1;

    public EntrySurplus(double alpha) {
        this.alpha = alpha;
    }

    public double initialize(int dimension, int[] cardinalities, long mass) {
        this.dimension = dimension;
        this.cardinalities = cardinalities.clone();
        this.massOfAll = mass;
        this.massOfBlock = mass;
        productOfCardinalitiesOfAll = 1;
        for(int dim = 0; dim < dimension; dim++) {
            productOfCardinalitiesOfAll *= cardinalities[dim];
        }
        productOfCardinalitiesOfBlock = productOfCardinalitiesOfAll;
        return density(massOfBlock, productOfCardinalitiesOfBlock);
    }

    public double initialize(int dimension, int[] cardinalitiesOfAll, long massOfAll, int[] cardinaltiesOfBlock, long massOfBlock) {
        this.dimension = dimension;
        this.cardinalities = cardinaltiesOfBlock.clone();
        this.massOfAll = massOfAll;
        this.massOfBlock = massOfBlock;
        productOfCardinalitiesOfAll = 1;
        productOfCardinalitiesOfBlock = 1;
        for(int dim = 0; dim < dimension; dim++) {
            productOfCardinalitiesOfAll *= cardinalitiesOfAll[dim];
            productOfCardinalitiesOfBlock *= cardinaltiesOfBlock[dim];
        }
        return density(massOfBlock, productOfCardinalitiesOfBlock);
    }

    public double ifRemoved(int attribute, int numValues, long sumOfMasses) {
        return density(massOfBlock - sumOfMasses, productOfCardinalitiesOfBlock / cardinalities[attribute] * (cardinalities[attribute] - numValues));
    }

    public double ifInserted(int attribute, int numValues, long sumOfMasses) {
        return density(massOfBlock + sumOfMasses, productOfCardinalitiesOfBlock / cardinalities[attribute] * (cardinalities[attribute] + numValues));
    }

    public double remove(int attribute, int numValues, long sumOfMasses) {
        cardinalities[attribute] -= numValues;
        productOfCardinalitiesOfBlock = productOfCardinalities(cardinalities); //recompute due to the precision error
        massOfBlock -= sumOfMasses;
        return density(massOfBlock, productOfCardinalitiesOfBlock);
    }

    public double insert(int attribute, int numValues, long sumOfMasses) {
        cardinalities[attribute] += numValues;
        productOfCardinalitiesOfBlock = productOfCardinalities(cardinalities); //recompute due to the precision error
        massOfBlock += sumOfMasses;
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
            return - Integer.MAX_VALUE;;
        return massOfBlock - alpha * productOfCardinalitiesOfBlock * massOfAll / productOfCardinalitiesOfAll;
    }

    public static double productOfCardinalities(int[] cardinalities){
        double productOfCardinalities = 1;
        for(int attribute = 0; attribute < cardinalities.length; attribute++) {
            productOfCardinalities *= cardinalities[attribute];
        }
        return productOfCardinalities;
    }
}
