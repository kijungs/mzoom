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
 * common interface for density measures
 * @author Kijung Shin (kijungs@cs.cmu.edu)
 */
public interface IDensityMeasure {

    /**
     * initialize a density measure for a given tensor
     * @param tensor
     * @return
     */
    double initialize(Tensor tensor);

    /**
     * return density if an attribute value with a given mass is removed from a given attribute
     * @param attribute
     * @param mass
     * @return
     */
    double ifRemoved(int attribute, int mass);

    /**
     * return density after removing an attribute value with a given mass from a given attribute
     * @param attribute
     * @param mass
     * @return
     */
    double remove(int attribute, int mass);

    /**
     * return density of a block with a given mass and cardinalities
     * @param mass
     * @param cardinalities
     * @return
     */
    double density(long mass, int[] cardinalities);
}
