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
 * Binary heap with a hash table for updating priorities
 * @author Kijung Shin (kijungs@cs.cmu.edu)
 */
public class HashIndexedMinHeap implements IMinHeap {

    /**
     * array of values
     */
    private int[] array;

    /**
     * Number of values in the heap
     */
    private int size;

    /**
     * Maximum number of values in the heap
     */
    private int capacity;

    /**
     * value -> position
     */
    private int[] positions;

    /**
     * value -> priority
     */
    private int[] priorities;

    /**
     * Position indicates that keys do not exist
     */
    private final int missingPosition = -1;

    /**
     *
     * @param capacity maximum number of values in the heap
     */
    public HashIndexedMinHeap(int capacity){
        this.capacity = capacity;
        this.array = new int[capacity];
        this.priorities = new int[capacity];
        this.positions = new int[capacity];
        this.size = 0;
        for(int i = 0; i < capacity; i++) {
            this.positions[i] = missingPosition;
        }
    }

    /**
     * return a value with minimum priority
     * @return (value, priority)
     */
    public Pair<Integer, Integer> peek(){
        if(size == 0){
            return null;
        }
        return new Pair(array[0], priorities[array[0]]);
    }

    /**
     * return a value with minimum priority after removing it from the heap
     * @return (value, priority)
     */
    public Pair<Integer, Integer> poll(){

        if(size == 0){
            return null;
        }

        Pair<Integer, Integer> top = this.peek();
        positions[top.getKey()] = missingPosition;

        if(size != 1){
            int last = array[size-1];
            array[0] = last;
            positions[last] = 0;

            size--;
            this.minHeapfy(0);
        }
        else{
            size--;
        }
        array[size] = 0;

        return top;
    }

    /**
     * update the priority of the given value to the given priority
     * @param value value
     * @param priority priority
     */
    public void updatePriority(int value, int priority){

        priorities[value] = priority;
        int pos = positions[value];
        boolean shiftedDown = this.minHeapfy(pos);

        if(!shiftedDown){
            if(pos > 0){
                int cur = value;
                int parentPos = ((pos + 1) / 2) - 1;
                int pel = array[parentPos];
                while(pos > 0 && priorities[pel] > priorities[cur]){
                    array[parentPos] = cur;
                    positions[cur] = parentPos;
                    array[pos] = pel;
                    positions[pel] = pos;
                    pos = parentPos;
                    parentPos = ((pos + 1) / 2) - 1;
                    if(pos > 0){
                        pel = array[parentPos];
                    }
                }

            }
        }
    }

    /**
     * return the priority of the given value
     * @param value   value
     * @return  priority
     */
    public int getPriority(int value){
        return priorities[value];
    }

    /**
     * insert the given value with the given priority to the heap
     * @param value value
     * @param priority  priority
     * @return  return false if the heap is already full return true otherwise
     */
    public boolean insert(int value, int priority){

        if(size >= capacity)
            return false;

        int pos = size;
        size++;
        array[pos] = value;
        positions[value] = pos;
        priorities[value] = priority;
        this.updatePriority(value, priority);
        return true;
    }

    private boolean minHeapfy(int pos){

        int posLeft = (2*(pos+1))-1;
        int posRight = (2*(pos+1));

        int keyCur = array[pos];

        int smallest = pos;
        int nsmallest = keyCur;

        if(posLeft < size){
            int keyLeft = array[posLeft];
            if(priorities[keyLeft] < priorities[keyCur]){
                smallest = posLeft;
                nsmallest = keyLeft;
            }
        }

        if(posRight < size){
            int keyRight = array[posRight];
            if(priorities[keyRight] < priorities[nsmallest]){
                smallest = posRight;
                nsmallest = keyRight;
            }
        }

        if(smallest != pos){

            array[pos] = nsmallest;
            positions[nsmallest] = pos;

            array[smallest] = keyCur;
            positions[keyCur] = smallest;

            this.minHeapfy(smallest);
            return true;
        }

        return false;
    }
}