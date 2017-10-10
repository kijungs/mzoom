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
 * @author Kijung Shin (kijungs@cs.cmu.edu)
 */
public class HashIndexedMaxHeap implements IMaxHeap {

    /**
     * heap: array of keys
     */
    private int[] array;

    /**
     * Number of objects in the heap
     */
    private int size;

    /**
     * Maximum number of objects in the heap
     */
    private int capacity;

    /**
     * index -> position
     */
    private int[] positions;

    /**
     * index -> value
     */
    private int[] priorities;

    /**
     * Position indicates that keys do not exist
     */
    private final int missingPosition = -1;

    public HashIndexedMaxHeap(int capacity){
        this.capacity = capacity;
        this.array = new int[capacity];
        this.priorities = new int[capacity];
        this.positions = new int[capacity];
        this.size = 0;
        for(int i = 0; i < capacity; i++) {
            this.positions[i] = missingPosition;
        }
    }

    public int size(){
        return size;
    }

    public boolean containsKey(int key){
        return (positions[key] == missingPosition) ? false : true;
    }

    public Pair<Integer, Integer> peek(){
        if(size == 0){
            return null;
        }
        return new Pair(array[0], priorities[array[0]]);
    }

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
            this.maxHeapfy(0);
        }
        else{
            size--;
        }
        array[size] = 0;

        return top;
    }

    public boolean insert(int key, int value){

        if(size >= capacity)
            return false;

        int pos = size;
        size++;
        array[pos] = key;
        positions[key] = pos;
        priorities[key] = value;
        this.updatePriority(key, value);
        return true;
    }

    public int getPriority(int key){
        return priorities[key];
    }

    public void updatePriority(int key, int value){

        priorities[key] = value;
        int pos = positions[key];
        boolean shiftedDown = this.maxHeapfy(pos);

        if(!shiftedDown){
            if(pos > 0){
                int cur = key;
                int parentPos = ((pos + 1) / 2) - 1;
                int pel = array[parentPos];
                while(pos > 0 && priorities[pel] < priorities[cur]){
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

    private boolean maxHeapfy(int pos){

        boolean shiftedDown = false;
        while(true) {

            int posLeft = (2 * (pos + 1)) - 1;
            int posRight = (2 * (pos + 1));

            int keyCur = array[pos];

            int largest = pos;
            int nlargest = keyCur;

            if (posLeft < size) {
                int keyLeft = array[posLeft];
                if (priorities[keyLeft] > priorities[keyCur]) {
                    largest = posLeft;
                    nlargest = keyLeft;
                }

            }

            if (posRight < size) {
                int keyRight = array[posRight];
                if (priorities[keyRight] > priorities[nlargest]) {
                    largest = posRight;
                    nlargest = keyRight;
                }
            }

            if (largest != pos) {

                array[pos] = nlargest;
                positions[nlargest] = pos;

                array[largest] = keyCur;
                positions[keyCur] = largest;

                pos = largest;
                shiftedDown = true;
                continue;
            }

            break;
        }

        return shiftedDown;
    }


}