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
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * M-Biz Implementation
 * @author Kijung Shin (kijungs@cs.cmu.edu)
 */
public class MBizRandom extends MBiz {

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
        System.out.println("running M-Biz (Random)...");
        System.out.println();
        new MBizRandom().run(tensor, output, blockNum, lower, upper, densityMeasure);
    }

    private static void printError() {
        System.err.println("Usage: run_mbiz.sh input_path output_path dimension density_measure num_of_blocks lower_bound upper_bound");
        System.err.println("Density_measure should be one of [ari, geo, susp, es_alpha], where alpha should be a number greater than zero");
        System.err.println("Lower_bound and Upper_bound are optional");
        System.out.println("Upper bound should be greater than or equal to Lower_bound");
    }

    @Override
    protected BlockInfo findOneBlock(Tensor tensor, int lower, int upper, DensityMeasure densityMeasure) throws IOException {

        //TODO: size bounds
        Random random = new Random();
        final boolean[][] attributeToValueToBeIncluded = new boolean[tensor.dimension][];

        for(int attribute = 0; attribute < tensor.dimension; attribute++) {
            int count = 0;
            int modeLength = tensor.cardinalities[attribute];
            attributeToValueToBeIncluded[attribute] = new boolean[modeLength];
            while(count==0) {
                for (int index = 0; index < modeLength; index++) {
                    if (random.nextDouble() > 0.5) {
                        attributeToValueToBeIncluded[attribute][index] = true;
                        count++;
                    }
                }
            }
        }
        return findOne(tensor, lower, upper, densityMeasure, attributeToValueToBeIncluded);
    }



}
