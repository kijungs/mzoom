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
 * common interface for density measures
 * @author Kijung Shin (kijungs@cs.cmu.edu)
 */
public interface IDensityMeasure {

    /**
     * initialize a density measure for a given tensor
     * @param dimension
     * @param cardinalities
     * @param mass
     * @return
     */
    double initialize(int dimension, int[] cardinalities, long mass);

    /**
     * initialize a density measure for a given tensor and a block
     * @param dimension
     * @param cardinalitiesOfAll
     * @param massOfAll
     * @param cardinaltiesOfBlock
     * @param massOfBlock
     * @return
     */
    double initialize(int dimension, int[] cardinalitiesOfAll, long massOfAll, int[] cardinaltiesOfBlock, long massOfBlock);

    /**
     * return density if the given number of values with the given mass sum are removed from the given mode
     * @param mode
     * @param numValues
     * @param sumOfMasses
     * @return
     */
    double ifRemoved(int mode, int numValues, long sumOfMasses);

    /**
     * return density if the given number of values with the given mass sum are inserted from the given mode
     * @param mode
     * @param numValues
     * @param sumOfMasses
     * @return
     */
    double ifInserted(int mode, int numValues, long sumOfMasses);

    /**
     * return density after removing the given number of values with the given mass sum from the given mode
     * @param mode
     * @param numValues
     * @param sumOfMasses
     * @return
     */
    double remove(int mode, int numValues, long sumOfMasses);

    /**
     * return density after inserting the given number of values with the given mass sum from the given mode
     * @param mode
     * @param numValues
     * @param sumOfMasses
     * @return
     */
    double insert(int mode, int numValues, long sumOfMasses);

    /**
     * return density of a block with a given mass and cardinalities
     * @param mass
     * @param cardinalities
     * @return
     */
    double density(long mass, int[] cardinalities);
}
