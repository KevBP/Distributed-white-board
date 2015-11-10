package utils;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.AbstractList;
import java.util.Iterator;

//Taken from http://stackoverflow.com/a/590167
public class CircularBuffer<T> implements Iterable<T> {

    private T[] buffer;

    private int tail;

    private int head;

    @SuppressWarnings("unchecked")
    public CircularBuffer(int n) {
        buffer = (T[]) new Object[n];
        tail = 0;
        head = 0;
    }

    public void add(T toAdd) {
        if (head != (tail - 1)) {
            buffer[head++] = toAdd;
        } else {
            throw new BufferOverflowException();
        }
        head = head % buffer.length;
    }

    public T get() {
        T t = null;
        int adjTail = tail > head ? tail - buffer.length : tail;
        if (adjTail < head) {
            t = (T) buffer[tail++];
            tail = tail % buffer.length;
        } else {
            throw new BufferUnderflowException();
        }
        return t;
    }

    @Override
    public Iterator<T> iterator() {
        return (new AbstractList<T>() {
            @Override
            public int size() {
                return buffer.length;
            }

            @Override
            public T get(int index) {
                return buffer[index];
            }
        }).iterator();
    }
}
