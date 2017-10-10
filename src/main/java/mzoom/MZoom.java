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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * M-Zoom Implementation
 * @author Kijung Shin (kijungs@cs.cmu.edu)
 */
public class MZoom {

    /**
     * Main function
     * @param args  input_path, output_path, num_of_attributes, density_measure, num_of_blocks, lower_bound, upper_bound
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
            System.err.println("Unknown Density Measure:" + args[3]);
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
        System.out.println("running the algorithm...");
        System.out.println();
        new MZoom().run(tensor, output, blockNum, lower, upper, densityMeasure);
    }

    private static void printError() {
        System.err.println("Usage: run_mzoom.sh input_path output_path dimension density_measure num_of_blocks lower_bound upper_bound");
        System.err.println("Density_measure should be one of [ari, geo, susp, es_alpha], where alpha should be a number greater than zero");
        System.err.println("Lower_bound and Upper_bound are optional");
        System.out.println("Upper bound should be greater than or equal to Lower_bound");
    }

    public void run(Tensor tensor, final int blockNum, DensityMeasure densityMeasure) throws IOException {
        run(tensor, null, blockNum, 0, Integer.MAX_VALUE, densityMeasure);
    }


    public void run(Tensor tensor, String output, final int blockNum, final int lower, final int upper, DensityMeasure densityMeasure) throws IOException {

        long start = System.currentTimeMillis();
        final Tensor oriTensor = tensor;
        tensor = oriTensor.copy();

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
            System.out.println("Error: Unknown Density");
        }
        measure.initialize(tensor.dimension, tensor.cardinalities, tensor.mass);

        final List<Set<Integer>[]> listOfAttributeToValues = new LinkedList();
        double bestAccuracy = 0;
        for(int i = 0; i < blockNum; i++) {
            BlockInfo blockInfo = findOneBlock(tensor, lower, upper, densityMeasure);
            bestAccuracy = Math.max(bestAccuracy, removeAndEvaluateBlock((i+1), tensor, blockInfo, oriTensor, measure));
            listOfAttributeToValues.add(blockInfo.getAttributeValues(tensor.dimension));
        }
        System.out.println("Running time: " + (System.currentTimeMillis() - start + 0.0)/1000 + " seconds");

        double diversity = diversity(listOfAttributeToValues, tensor.dimension);
        System.out.println("Diversity among blocks found: " + diversity);

        start = System.currentTimeMillis();
        System.out.println("Writing outputs...");
        writeOutput(output, oriTensor, listOfAttributeToValues);
        System.out.println("Outputs were written. " + (System.currentTimeMillis() - start + 0.0)/1000 + " seconds was taken.");
    }

    /**
     * Check whether the given block satisfies size bounds
     * @param sumOfCardinalities
     * @param lower lower size bounds
     * @param upper upper size bounds
     * @return
     */
    private static boolean satisfy(final int sumOfCardinalities, final int lower, final int upper) {
        if(sumOfCardinalities <= upper && sumOfCardinalities >= lower)
            return true;
        else
            return false;
    }

    /**
     * find one dense block from a given tensor
     * @param tensor
     * @param lower ower size bounds
     * @param upper upper size bounds
     * @param densityMeasure
     * @return
     * @throws IOException
     */
    protected BlockInfo findOneBlock(Tensor tensor, int lower, int upper, DensityMeasure densityMeasure) throws IOException {

        final int dimension = tensor.dimension;
        final int[] measureValues = tensor.measureValues.clone(); // clone values
        final int[][][] attributeToValuesToTuples = tensor.attributeToValuesToTuples;
        final int[][] attributes = tensor.attributes;
        final IMinHeap[] heaps = createHeaps(tensor);
        final int sumOfCardinalities = sumOfCardinalities(tensor);
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
            System.out.println("Error: Unknown Density IDensityMeasure");
        }

        BlockIterInfo iterInfo = new BlockIterInfo(tensor.cardinalities);
        int maxIters = 0;
        double maxDensityAmongIters = measure.initialize(tensor.dimension, tensor.cardinalities, tensor.mass);
        maxDensityAmongIters = satisfy(sumOfCardinalities, lower, upper) ? maxDensityAmongIters : -Double.MAX_VALUE;
        for (int i = 0; i < sumOfCardinalities; i++) {
            byte maxAttribute = 0;
            double maxDensityAmongAttributes = -Double.MAX_VALUE;
            for (byte attribute = 0; attribute < dimension; attribute++) {
                final Pair<Integer, Integer> pair = heaps[attribute].peek();
                if (pair != null) {
                    double tempDensity = measure.ifRemoved(attribute, 1, pair.getValue());
                    if (tempDensity > maxDensityAmongAttributes) {
                        maxAttribute = attribute;
                        maxDensityAmongAttributes = tempDensity;
                    }
                }
            }
            Pair<Integer, Integer> pair = heaps[maxAttribute].poll();
            int valueToRemove = pair.getKey();
            double density = measure.remove(maxAttribute, 1, pair.getValue());
            if (satisfy(sumOfCardinalities-i-1, lower, upper) && density > maxDensityAmongIters) {
                maxDensityAmongIters = density;
                maxIters = i + 1;
            }
            iterInfo.addIterInfo((byte)maxAttribute, valueToRemove);

            //update degress
            int[] entries = attributeToValuesToTuples[maxAttribute][valueToRemove];
            for (int entry : entries) {
                int measureValue = measureValues[entry];
                if (measureValue > 0) {
                    for (int dim = 0; dim < dimension; dim++) {
                        if(dim != maxAttribute) {
                            int attributeValue = attributes[entry][dim];
                            heaps[dim].updatePriority(attributeValue, heaps[dim].getPriority(attributeValue) - measureValue);
                        }
                    }
                }
                measureValues[entry] = 0;
            }
        }

        return iterInfo.returnBlock(maxIters, null);
    }

    /**
     * compute the sum of the cardinalities of the attributes of the given tensor
     * @param tensor    tensor
     * @return  sume of the ca
     */
    private static int sumOfCardinalities(Tensor tensor){
        int sumOfCardinalities = 0;
        for(int dim = 0; dim < tensor.dimension; dim++) {
            sumOfCardinalities += tensor.cardinalities[dim];
        }
        return sumOfCardinalities;
    }

    /**
     * create heaps for each attribute
     * @param tensor
     * @return
     */
    private static IMinHeap[] createHeaps(final Tensor tensor) {
        int[][] mass = TensorMethods.attributeValueMasses(tensor);
        IMinHeap[] heaps = new IMinHeap[tensor.dimension];
        for(int dim = 0; dim < tensor.dimension; dim++) {
            IMinHeap heap = new HashIndexedMinHeap(tensor.cardinalities[dim]);
            int[] attributeMass = mass[dim];
            for(int index = 0; index < tensor.cardinalities[dim]; index++) {
                heap.insert(index, attributeMass[index]);
            }
            heaps[dim] = heap;
        }
        return heaps;
    }

    /**
     * evaluate the density of the given block, and remove tuples in the block from the relation
     * @param curTensor
     * @param blockInfo
     * @param oriTensor
     * @param measure
     * @return
     */
    private static double removeAndEvaluateBlock(int blockIndex, final Tensor curTensor, final BlockInfo blockInfo, final Tensor oriTensor, final IDensityMeasure measure) throws IOException {

        final boolean[][] attributeToValuesToRemove = blockInfo.getBitMask(oriTensor.dimension, oriTensor.cardinalities);
        final int[][] attributes = curTensor.attributes;
        final int[] measureValues = curTensor.measureValues;
        final int[] oriValues = oriTensor.measureValues;
        long mass = 0;
        for(int i=0; i<curTensor.omega; i++) {
            int[] attributeValues = attributes[i];
            boolean removed = true;
            for(int dim = 0; dim < curTensor.dimension; dim++) {
                if(!attributeToValuesToRemove[dim][attributeValues[dim]]) {
                    removed = false;
                    break;
                }
            }
            if(removed) {
                curTensor.mass -= measureValues[i];
                measureValues[i] = 0; //remove tuple
                mass += oriValues[i];
            }
        }

        System.out.println("Block: " + blockIndex);
        System.out.print("Volume: ");
        for(int dim = 0; dim < curTensor.dimension; dim++) {
            System.out.print(blockInfo.blockCardinalities[dim]);
            if(dim < curTensor.dimension - 1) {
                System.out.print(" X ");
            }
        }
        System.out.println();
        double density = measure.density(mass, blockInfo.blockCardinalities);
        System.out.println("Density: " + density);
        System.out.println("Mass: " + mass);
        System.out.println();
        return density;
    }

    /**
     * compute the diversity among blocks found
     * @param listOfAttributeToValues (block number, attribute, value) -> list of attribute values contained in each block
     * @return diversity among blocks found
     */
    public static double diversity(List<Set<Integer>[]> listOfAttributeToValues, int dimension) {

        int blockNum = listOfAttributeToValues.size();
        int count = 0;
        double jaccardSum = 0;
        for(int i=0; i<blockNum; i++) {
            for(int j=i+1; j<blockNum; j++) {
                long intersect = 0;
                long union = 0;
                for(int dim = 0; dim < dimension; dim++) {
                    Map<Integer, Integer> map = new HashMap();
                    for(int index : listOfAttributeToValues.get(i)[dim]) {
                        if(map.containsKey(index)){
                            map.put(index, map.get(index)+1);
                        }
                        else {
                            map.put(index, 1);
                        }
                    }
                    for(int index : listOfAttributeToValues.get(j)[dim]) {
                        if(map.containsKey(index)){
                            map.put(index, map.get(index)+1);
                        }
                        else {
                            map.put(index, 1);
                        }
                    }
                    union += map.size();
                    for(int vals : map.values()){
                        if(vals == 2) {
                            intersect++;
                        }
                        else if(vals > 2) {
                            System.out.println("ERROR!!");
                        }
                    }
                }

                double jaccard = (0.0+intersect) / union;
                jaccardSum += jaccard;
                count++;
            }
        }

        return (1 - jaccardSum/count);
    }


    /**
     * write blocks found to the given output folder
     * @param output    output path
     * @param tensor    tensor
     * @param listOfAttributeToValues   blocks found
     * @throws IOException
     */
    private static void writeOutput(String output, Tensor tensor, List<Set<Integer>[]> listOfAttributeToValues) throws IOException {
        File dir = new File(output);
        try{
            dir.mkdir();
        }
        catch(Exception e){
        }

        int blockNum = listOfAttributeToValues.size();
        int dimension = tensor.dimension;
        String[][] intToStrValue = tensor.intToStrValue;
        for(int blockIndex = 0; blockIndex < blockNum; blockIndex++) {

            Set<Integer>[] attributeToValues = listOfAttributeToValues.get(blockIndex);

            //write attribute values
            BufferedWriter bw = new BufferedWriter(new FileWriter(output + File.separator + "block_"+(blockIndex+1)+".attributes"));
            final boolean[][] attributeToValuesToWrite = new boolean[tensor.dimension][];
            for(int dim = 0; dim < tensor.dimension; dim++) {
                attributeToValuesToWrite[dim] = new boolean[tensor.cardinalities[dim]];
                for(int value : attributeToValues[dim]) {
                    attributeToValuesToWrite[dim][value] = true;
                    bw.write(dim+","+intToStrValue[dim][value]);
                    bw.newLine();
                }
            }
            bw.close();

            //write blocks
            bw = new BufferedWriter(new FileWriter(output + File.separator + "block_"+(blockIndex+1)+".tuples"));
            final int[][] attributes = tensor.attributes;
            final int[] measureValues = tensor.measureValues;
            for(int i=0; i<tensor.omega; i++) {
                int[] attributeValues = attributes[i];
                boolean write = true;
                for(int dim = 0; dim < dimension; dim++) {
                    if(!attributeToValuesToWrite[dim][attributeValues[dim]]) {
                        write = false;
                        break;
                    }
                }
                if(write) {
                    for(int dim = 0; dim < dimension; dim++) {
                        bw.write(intToStrValue[dim][attributeValues[dim]] + ",");
                    }
                    bw.write(""+measureValues[i]);
                    bw.newLine();
                }
            }
            bw.close();
        }

    }

}
