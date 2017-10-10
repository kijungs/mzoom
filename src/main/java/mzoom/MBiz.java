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

import java.io.IOException;

/**
 * M-Biz Implementation
 * @author Kijung Shin (kijungs@cs.cmu.edu)
 */
public class MBiz extends MZoom {

    /**
     * Main function
     * @param args  input_path, output_path, num_of_attributes, density_measure, seed_methods, num_of_blocks, lower_bound, upper_bound
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if(args.length < 5) {
            printError();
            System.exit(-1);
        }

        final String input = args[0];
        System.out.println("input_path: " + input);

        final String output = args[1];
        System.out.println("output_path: " + output);

        final int dimension = Integer.valueOf(args[2]);
        System.out.println("dimension: " + dimension);

        DensityMeasure densityMeasure = null;
        if(args[3].compareToIgnoreCase("ARI")==0) {
            densityMeasure = new DensityMeasure(DensityMeasure.Arithmetic);
        }
        else if(args[3].compareToIgnoreCase("GEO")==0){
            densityMeasure =  new DensityMeasure(DensityMeasure.Geometric);
        }
        else if(args[3].compareToIgnoreCase("SUSP")==0) {
            densityMeasure =  new DensityMeasure(DensityMeasure.Suspiciousness);
        }
        else if(args[3].startsWith("ES_") || args[3].startsWith("es_")) {
            double param = Double.valueOf(args[3].replace("ES_", "").replace("es_",""));
            densityMeasure = new DensityMeasure(DensityMeasure.EntrySurplus, param);
        }
        else {
            System.err.println("Unknown Density Measure: " + args[3]);
            printError();
            System.exit(-1);
        }
        System.out.println("density_measure: " + args[3]);

        final int blockNum = Integer.valueOf(args[4]);
        System.out.println("num_of_blocks: " + blockNum);

        int lower = 0;
        if(args.length >= 6) {
            lower = Integer.valueOf(args[5]);
            System.out.println("lower_bound: " + lower);
        }

        int upper = Integer.MAX_VALUE;
        if(args.length >= 7) {
            upper = Integer.valueOf(args[6]);
            System.out.println("upper_bound: " + upper);
        }
        System.out.println();

        System.out.println("importing the input tensor...");
        Tensor tensor = TensorMethods.importTensor(input, dimension);
        System.out.println();
        System.out.println("running M-Biz...");
        System.out.println();
        new MBiz().run(tensor, output, blockNum, lower, upper, densityMeasure);
    }

    private static void printError() {
        System.err.println("Usage: run_mbiz.sh input_path output_path dimension density_measure num_of_blocks lower_bound upper_bound");
        System.err.println("Density_measure should be one of [ari, geo, susp, es_alpha], where alpha should be a number greater than zero");
        System.err.println("Lower_bound and Upper_bound are optional");
        System.out.println("Upper bound should be greater than or equal to Lower_bound");
    }

    @Override
    protected BlockInfo findOneBlock(Tensor tensor, int lower, int upper, DensityMeasure densityMeasure) throws IOException {
        final BlockInfo blockInfo = super.findOneBlock(tensor, lower, upper, densityMeasure);
        return findOne(tensor, lower, upper, densityMeasure, blockInfo.getBitMask(tensor.dimension, tensor.cardinalities));
    }

    /**
     * 
     * @param tensor
     * @param lower
     * @param upper
     * @param densityMeasure
     * @param attributeToValueToBeIncluded
     * @return
     */
    private static BlockInfo findOne(Tensor tensor, int lower, int upper, DensityMeasure densityMeasure, boolean[][] attributeToValueToBeIncluded){

        final int dimension = tensor.dimension;
        final int[] measureValues = tensor.measureValues; // clone measureValues
        final byte[] nonmemberCounts = new byte[measureValues.length];
        final int[][][] attributeToValuesToTuples = tensor.attributeToValuesToTuples;
        final int[][] attributes = tensor.attributes;

        IDensityMeasure measure = null;
        if(densityMeasure.type == DensityMeasure.Suspiciousness)
            measure = new Suspiciousness();
        else if(densityMeasure.type == DensityMeasure.Arithmetic)
            measure = new Arithmetic();
        else if(densityMeasure.type == DensityMeasure.Geometric)
            measure = new Geometric();
        else if(densityMeasure.type == DensityMeasure.EntrySurplus)
            measure = new EntrySurplus(densityMeasure.param);
        else {
            System.out.println("Error: Unknown Density IMeasure");
        }

        int[][] attributeToValueToMassChange = new int[dimension][];
        for(int attribute=0; attribute<dimension; attribute++){
            attributeToValueToMassChange[attribute] = new int[tensor.cardinalities[attribute]];
        }

        long blockMass = 0;
        for(int i=0; i<tensor.omega; i++){
            int measureValue = measureValues[i];
            int[] tupleAttVals = attributes[i];

            byte nonmemberCount = 0;
            int nonmemberAttribute = 0;
            for(int attribute=0; attribute<dimension; attribute++){
                if(!attributeToValueToBeIncluded[attribute][tupleAttVals[attribute]]) {
                    nonmemberCount += 1;
                    nonmemberAttribute = attribute;
                }
            }

            nonmemberCounts[i] = nonmemberCount;

            if(nonmemberCount==0) { // in the block
                for(int attribute=0; attribute<dimension; attribute++){
                    attributeToValueToMassChange[attribute][tupleAttVals[attribute]] += measureValue;
                    blockMass += measureValue;
                }
            }
            else if(nonmemberCount == 1){
                attributeToValueToMassChange[nonmemberAttribute][tupleAttVals[nonmemberAttribute]] += measureValue;
            }
        }

        final IMinHeap[] inHeaps = new IMinHeap[tensor.dimension];
        final IMaxHeap[] outHeaps = new IMaxHeap[tensor.dimension];
        final int[] attributeToCardinalities = new int[dimension];
        int sumOfCardinalities = 0;

        for(int attribute = 0; attribute < tensor.dimension; attribute++) {
            boolean[] attValToBeIncluded = attributeToValueToBeIncluded[attribute];
            int[] attValToMassChange = attributeToValueToMassChange[attribute];
            IMinHeap inHeap = new HashIndexedMinHeap(tensor.cardinalities[attribute]);
            IMaxHeap outHeap = new HashIndexedMaxHeap(tensor.cardinalities[attribute]);
            for(int index = 0; index < tensor.cardinalities[attribute]; index++) {
                if(attValToBeIncluded[index]) {
                    inHeap.insert(index, attValToMassChange[index]);
                    attributeToCardinalities[attribute] += 1;
                    sumOfCardinalities ++;
                }
                else {
                    outHeap.insert(index, attValToMassChange[index]);
                }
            }
            inHeaps[attribute] = inHeap;
            outHeaps[attribute] = outHeap;
        }

        blockMass = blockMass/dimension;

        measure.initialize(tensor.dimension, tensor.cardinalities, tensor.mass, attributeToCardinalities, blockMass);

        double currentScore = measure.density(blockMass, attributeToCardinalities);
        while(true) {

            double previousScore = currentScore;
            int maxAttribute = 0;
            boolean action = false; //false: remove, true: insert
            if(sumOfCardinalities > lower) {
                for (int attribute = 0; attribute < dimension; attribute++) {
                    final Pair<Integer, Integer> pair = inHeaps[attribute].peek();
                    if (pair != null) {
                        double tempScore = measure.ifRemoved(attribute, 1, pair.getValue());
                        if (tempScore > currentScore) {
                            maxAttribute = attribute;
                            action = false;
                            currentScore = tempScore;
                        }
                    }
                }
            }

            if(sumOfCardinalities < upper) {
                for (int attribute = 0; attribute < dimension; attribute++) {
                    final Pair<Integer, Integer> pair = outHeaps[attribute].peek();
                    if (pair != null) {
                        double tempScore = measure.ifInserted(attribute, 1, pair.getValue());
                        if (tempScore > currentScore) {
                            maxAttribute = attribute;
                            action = true;
                            currentScore = tempScore;
                        }
                    }
                }
            }

            if(currentScore == previousScore) { // terminates
                break;
            }

            if(action == false) { //remove

                Pair<Integer, Integer> pair = inHeaps[maxAttribute].poll();
                int attValToRemove = pair.getKey();
                currentScore = measure.remove(maxAttribute, 1, pair.getValue());

                sumOfCardinalities--;
                attributeToCardinalities[maxAttribute]--;

                //update degree in
                int massSumOut = 0;
                int[] tuples = attributeToValuesToTuples[maxAttribute][attValToRemove];
                for (int tuple : tuples) {

                    byte nonmemberCount = nonmemberCounts[tuple];

                    if(nonmemberCount > 1) {
                        nonmemberCounts[tuple]++;
                    }
                    else if(nonmemberCount == 1){
                        int[] tupleAttVals = attributes[tuple];
                        int measureValue = measureValues[tuple];
                        int nonMemberAttribute = 0;
                        for(int attribute = 0; attribute < dimension; attribute++) {
                            if(attribute != maxAttribute) {
                                if(!attributeToValueToBeIncluded[attribute][tupleAttVals[attribute]]) {
                                    nonMemberAttribute = attribute;
                                    break;
                                }
                            }
                        }
                        nonmemberCounts[tuple]++;
                        int attVal = tupleAttVals[nonMemberAttribute];
                        outHeaps[nonMemberAttribute].updatePriority(attVal, outHeaps[nonMemberAttribute].getPriority(attVal) - measureValue);
                    }
                    else if(nonmemberCount==0){ //nonmember count == 0;
                        int measureValue = measureValues[tuple];
                        massSumOut += measureValue;
                        nonmemberCounts[tuple]++;
                        for (int attribute = 0; attribute < dimension; attribute++) {
                            if(attribute != maxAttribute) {
                                int attVal = attributes[tuple][attribute];
                                inHeaps[attribute].updatePriority(attVal, inHeaps[attribute].getPriority(attVal) - measureValue);
                            }
                        }
                    }
                    else {
                        System.out.println("error-2!");
                        System.exit(0);
                    }
                }

                outHeaps[maxAttribute].insert(attValToRemove, massSumOut);
                attributeToValueToBeIncluded[maxAttribute][attValToRemove] = false;

            }
            else { //insert

                Pair<Integer, Integer> pair = outHeaps[maxAttribute].poll();
                int attValToInsert = pair.getKey();
                currentScore = measure.insert(maxAttribute, 1, pair.getValue());

                sumOfCardinalities++;
                attributeToCardinalities[maxAttribute]++;

                //update degree in
                int massSumIn = 0;
                int[] tuples = attributeToValuesToTuples[maxAttribute][attValToInsert];
                for (int tuple : tuples) {

                    byte nonmemberCount = nonmemberCounts[tuple];

                    if(nonmemberCount > 2) {
                        nonmemberCounts[tuple]--;
                        continue;
                    }
                    else if(nonmemberCount == 2){
                        int[] tupleAttVals = attributes[tuple];
                        int measureValue = measureValues[tuple];
                        int nonMemberAttribute = 0;
                        for(int attribute = 0; attribute < dimension; attribute++) {
                            if(attribute != maxAttribute) {
                                if(!attributeToValueToBeIncluded[attribute][tupleAttVals[attribute]]) {
                                    nonMemberAttribute = attribute;
                                    break;
                                }
                            }
                        }
                        nonmemberCounts[tuple]--;
                        int index = tupleAttVals[nonMemberAttribute];
                        outHeaps[nonMemberAttribute].updatePriority(index, outHeaps[nonMemberAttribute].getPriority(index) + measureValue);
                    }
                    else if(nonmemberCount == 1){ //nonmember count == 1;
                        int measurevALUE = measureValues[tuple];
                        massSumIn += measurevALUE;
                        nonmemberCounts[tuple]--;
                        for (int attribute = 0; attribute < dimension; attribute++) {
                            if(attribute != maxAttribute) {
                                int index = attributes[tuple][attribute];
                                inHeaps[attribute].updatePriority(index, inHeaps[attribute].getPriority(index) + measurevALUE);
                            }
                        }
                    }
                    else { //nonmemberCount
                        System.out.println(attributeToValueToBeIncluded[maxAttribute][attributes[tuple][maxAttribute]]);
                        System.out.println("error-1!");
                        System.exit(0);
                    }
                }
                inHeaps[maxAttribute].insert(attValToInsert, massSumIn);
                attributeToValueToBeIncluded[maxAttribute][attValToInsert] = true;

            }

        }

        final byte[] blockAttributes = new byte[sumOfCardinalities];
        final int[] blockValues = new int[sumOfCardinalities];
        int count = 0;

        for (byte attribute = 0; attribute < dimension; attribute++) {
            boolean[] attValToBeIncluded = attributeToValueToBeIncluded[attribute];
            for(int attVal =0 ; attVal<attValToBeIncluded.length; attVal++) {
                if(attValToBeIncluded[attVal]) {
                    blockAttributes[count] = attribute;
                    blockValues[count] = attVal;
                    count++;
                }
            }
        }

        return new BlockInfo(sumOfCardinalities, attributeToCardinalities, blockAttributes, blockValues);
    }

}
