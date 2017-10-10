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
 * Data structure to store tensor data
 * @author Kijung Shin (kijungs@cs.cmu.edu)
 */
public class Tensor {

	public int dimension; //number of attributess

	public int[] cardinalities; // n -> cardinality of the n-th attribute

    public int[][][] attributeToValuesToTuples; // (n, value) -> list of tuples which has the given value for the n-th attribute

	public int omega; // number of tuples

	public int[][] attributes; // (i, n) -> the n-th attribute value of the i-th tuple

	public int[] measureValues; // i -> measure attribute value of i-th tuple

    public long mass; // sum of measures attributes values

    public String[][] intToStrValue; // (n, value) -> str attribute value mapped to the given integer value of the n-th attribute

	/**
	 * Create a tensor with the given properties
	 * @param dimension dimension
	 * @param cardinalities n -> cardinality of the n-th attribute
     * @param attributeToValuesToTuples; (n, value) -> list of tuples which has the given value for the n-th attribute
	 * @param omega number of tuples
	 * @param attributes   (i, n) -> the n-th attribute value of the i-th tuple
	 * @param measureValues    i -> measure attribute value of i-th tuple
     * @param intToStrValue // (n, value) -> str attribute value mapped to the given integer value of the n-th attribute
	 */
	public Tensor(int dimension, int[] cardinalities, int[][][] attributeToValuesToTuples, int omega, int[][] attributes, int[] measureValues, long mass, String[][] intToStrValue) {
        this.dimension = dimension;
        this.cardinalities = cardinalities;
        this.attributeToValuesToTuples = attributeToValuesToTuples;
        this.omega = omega;
        this.attributes = attributes;
        this.measureValues = measureValues;
        this.mass = mass;
        this.intToStrValue = intToStrValue;
    }

    /**
     * Copy the given tensor (all fields except measureValues are shared)
     * @param tensor a tensor to copy
     */
    private Tensor(Tensor tensor) {
        this.dimension = tensor.dimension;
        this.cardinalities = tensor.cardinalities;
        this.attributeToValuesToTuples = tensor.attributeToValuesToTuples;
        this.omega = tensor.omega;
        this.attributes = tensor.attributes;
        this.measureValues = tensor.measureValues.clone();
        this.mass = tensor.mass;
        this.intToStrValue = tensor.intToStrValue;
    }

    /**
     * Copy the given tensor (all fields except measureValues are shared)
     * @return copied tensor
     */
    public Tensor copy() {
        return new Tensor(this);
    }
}
