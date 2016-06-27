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
 * common interface for minimum heaps
 * @author Kijung Shin (kijungs@cs.cmu.edu)
 */
public interface IMinHeap {

    /**
     * return a value with minimum priority
     * @return (value, priority)
     */
    Pair<Integer, Integer> peek();

    /**
     * return a value with minimum priority after removing it from the heap
     * @return (value, priority)
     */
    Pair<Integer, Integer> poll();

    /**
     * update the priority of the given value to the given priority
     * @param value value
     * @param priority priority
     */
    void updatePriority(int value, int priority);

    /**
     * return the priority of the given value
     * @param value   value
     * @return  priority
     */
    int getPriority(int value);

    /**
     * insert the given value with the given priority to the heap
     * @param value value
     * @param priority  priority
     * @return  return false if the heap is already full return true otherwise
     */
    boolean insert(int value, int priority);

}
