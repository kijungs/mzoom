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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Methods for handling tensors
 * @author Kijung Shin (kijungs@cs.cmu.edu)
 */
public class TensorMethods {

    /**
     * load an input tensor in memory
     * @param path  path of the input tensor file
     * @param dimension number of attributes
     * @return  imported tensor
     * @throws IOException
     */
    public static Tensor importTensor(final String path, final int dimension) throws IOException {
        return importTensor(path, ",", dimension);
    }
    
    /**
     * load an input tensor in memory
     * @param path  path of the input tensor file
     * @param delim delimiter used in the file
     * @param dimension number of attributes
     * @return  imported tensor
     * @throws IOException
     */
    public static Tensor importTensor(final String path, final String delim, final int dimension) throws IOException{

        long start = System.currentTimeMillis();

        int omega = 0; // number of tuples
        final int[] maxValues = new int[dimension];
        final Map<String, Integer>[] strToIntValue = new Map[dimension];
        final Map<Integer, Integer>[] attributeToValueToNum = new Map[dimension];
        for(int dim = 0; dim < dimension; dim++) {
            attributeToValueToNum[dim] = new HashMap();
            strToIntValue[dim] = new HashMap();
        }
        final BufferedReader br = new BufferedReader(new FileReader(path));
        while(true){
            final String line = br.readLine();
            if(line==null)
                break;
            omega++;
            final String[] tokens = line.split(delim);
            if(tokens.length < dimension + 1) {
                System.out.println("Skipped Line: " + line);
                continue;
            }
            for(int dim = 0; dim < dimension; dim++) {

                if(!strToIntValue[dim].containsKey(tokens[dim])) {
                    strToIntValue[dim].put(tokens[dim], strToIntValue[dim].size());
                }

                int index = strToIntValue[dim].get(tokens[dim]);
                if(attributeToValueToNum[dim].containsKey(index)) {
                    attributeToValueToNum[dim].put(index, attributeToValueToNum[dim].get(index) + 1);
                }
                else {
                    attributeToValueToNum[dim].put(index, 1);
                }
                maxValues[dim] = Math.max(maxValues[dim], index);
            }
        }
        br.close();
        final int[] cardinalities = new int[dimension];
        final int[][] attributeToValueToNumArr = new int[dimension][];
        for(int dim = 0; dim < dimension; dim++) {
            cardinalities[dim] = maxValues[dim]+1;
            attributeToValueToNumArr[dim] = new int[cardinalities[dim]];
            for (int index : attributeToValueToNum[dim].keySet()) {
                attributeToValueToNumArr[dim][index] = attributeToValueToNum[dim].get(index);
            }
        }

        Tensor tensor = importTensor(path, delim, dimension, omega, cardinalities, attributeToValueToNumArr, strToIntValue);
        System.out.println("input data were loaded. " + (System.currentTimeMillis() - start + 0.0)/1000 + " seconds was taken.");
        return tensor;
    }

    /**
     * load an input tensor in memory
     * @param path  path of the input tensor file
     * @param delim delimiter used in the file
     * @param dimension number of attributes
     * @param omega	number of tuples
     * @param cardinalities	n -> cardinality of the n-th attribute
     * @param attributeToValueToNum n, value -> number of tuples which have the given value as the n-th attribute
     * @param strToIntValue (n, value) -> int attribute value mapped to the given string value of the n-th attribute
     * @return imported tensor
     * @throws IOException
     */
    private static Tensor importTensor(final String path, final String delim, final int dimension, final int omega, final int[] cardinalities, int[][] attributeToValueToNum, Map<String, Integer>[] strToIntValue) throws IOException {

        final int[][][] attributeToValueToTuples = new int[dimension][][]; // n, value -> list of tuples which have the given value as the n-th attribute
        final int[][] attributeToValueToCurrentCardinality = new int[dimension][];
        final int[][] attributes = new int[omega][dimension];
        final int[] values = new int[omega];

        for (int dim = 0; dim < dimension; dim++) {
            attributeToValueToTuples[dim] = new int[cardinalities[dim]][];
            attributeToValueToCurrentCardinality[dim] = new int[cardinalities[dim]];
            for (int i = 0; i < cardinalities[dim]; i++) {
                attributeToValueToTuples[dim][i] = new int[attributeToValueToNum[dim][i]];
            }
        }

        final BufferedReader br = new BufferedReader(new FileReader(path));
        long sum = 0;

        int curTupleNum = 0;
        while (true) {
            String line = br.readLine();
            if (line == null)
                break;
            String[] tokens = line.split(delim);
            if(tokens.length < dimension + 1) {
                System.out.println("Skipped Line: " + line);
                continue;
            }
            for (int dim = 0; dim < dimension; dim++) {
                int index = strToIntValue[dim].get(tokens[dim]);
                attributeToValueToTuples[dim][index][attributeToValueToCurrentCardinality[dim][index]++] = curTupleNum;
                attributes[curTupleNum][dim] = index;
            }
            values[curTupleNum] = Integer.valueOf(tokens[dimension]);
            sum += values[curTupleNum];
            curTupleNum++;
        }

        br.close();

        String[][] intToStrValue = new String[dimension][];
        for(int dim = 0; dim< dimension; dim++) {
            intToStrValue[dim] = new String[strToIntValue[dim].size()];
            for(String key : strToIntValue[dim].keySet()) {
                intToStrValue[dim][strToIntValue[dim].get(key)] = key;
            }
        }

        return new Tensor(dimension, cardinalities, attributeToValueToTuples, omega, attributes, values, sum, intToStrValue);
    }

    /**
     * compute the mass of each attribute value of each attribute of the given tensor
     * @param tensor    tensor
     * @return (n, value) -> mass of the given value of the n-th attribute
     */
    public static int[][] attributeValueMasses(Tensor tensor){

        int dimension = tensor.dimension;
        int[] cardinalities = tensor.cardinalities;
        int[][] mass = new int[dimension][];
        for(int dim=0; dim<dimension; dim++){
            mass[dim] = new int[cardinalities[dim]];
        }

        int[][] attributes = tensor.attributes;
        int[] values = tensor.measureValues;

        for(int i=0; i<tensor.omega; i++){
            int value = values[i];
            for(int dim=0; dim<dimension; dim++){
                mass[dim][attributes[i][dim]] += value;
            }
        }

        return mass;
    }
    
}
