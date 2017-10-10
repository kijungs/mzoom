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

import java.io.*;

/**
 * Order by which attribute values are removed
 * @author Kijung Shin (kijungs@cs.cmu.edu)
 */
public class BlockIterInfo {

    private int dimension = 0;
    private byte[] attributes = null;
    private int[] attVals = null;
    private boolean useBuffer = true;
    private int cardinalitySum = 0;
    private int curIndex = 0;
    private ObjectOutputStream out = null;
    private String orderingFilePath = null;

    public BlockIterInfo(int[] modeLengths) throws IOException {
        this.useBuffer = true;
        this.dimension = modeLengths.length;
        for(int mode = 0; mode < dimension; mode++) {
            cardinalitySum += modeLengths[mode];
        }
        this.attributes = new byte[cardinalitySum];
        this.attVals = new int[cardinalitySum];
    }
    
    public void addIterInfo(byte mode, int index) throws IOException {
        if(useBuffer) {
            attributes[curIndex] = mode;
            attVals[curIndex++] = index;
        }
        else {
            out.writeByte(mode);
            out.writeInt(index);
        }
    }

    public BlockInfo returnBlock(int maxIter, String blockInfoPath) throws IOException {
        if (out != null) {
            out.close();
        }

        if(useBuffer) { // write block info in memory
            int[] modeLengths = new int[dimension];
            int newLength = cardinalitySum - maxIter;
            byte[] newModes = new byte[newLength];
            for(int i = 0; i < newLength; i++) {
                newModes[i] = attributes[i+maxIter];
                modeLengths[attributes[i+maxIter]]++;
            }
            int[] newIndices = new int[newLength];
            for(int i = 0; i < newLength; i++) {
                newIndices[i] = attVals[i+maxIter];
            }
            return new BlockInfo(newLength, modeLengths, newModes, newIndices);
        }
        else { //write block info in disk

            int[] modeLengths = new int[dimension];
            int newLength = cardinalitySum - maxIter;
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(blockInfoPath), 8388608));
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(orderingFilePath), 8388608));

            for(int i = 0; i < maxIter; i++) { //throw away
                in.readByte();
                in.readInt();
            }
            for(int i = 0; i < newLength; i++) {
                byte mode = in.readByte();
                out.writeByte(mode);
                modeLengths[mode]++;
                out.writeInt(in.readInt());
            }
            in.close();
            out.close();

            if(new File(orderingFilePath).exists()) {
                new File(orderingFilePath).delete();
            }

            return new BlockInfo(newLength, modeLengths, blockInfoPath);
        }
    }
}
