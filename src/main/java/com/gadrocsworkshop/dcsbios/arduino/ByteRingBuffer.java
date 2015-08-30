package com.gadrocsworkshop.dcsbios.arduino;

import java.nio.BufferOverflowException;

class ByteRingBuffer {

    private final byte[] elements;

    private int head = 0;
    private int available = 0;

    public ByteRingBuffer(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Buffer capacity must be positive");
        }
        elements = new byte[size];
    }

    @SuppressWarnings("WeakerAccess")
    public void add(byte item) {

        if (available == elements.length) {
            throw new BufferOverflowException();
        }

        elements[head] = item;
        head = (head + 1) % elements.length;

        ++available;
    }

    public void add(byte[] data, int offset, int length) {
        for (int i=0; i<length; i++) {
            add(data[offset+i]);
        }
    }

    public byte get() {
        byte result = elements[(head + (capacity() - available)) % capacity()];
        available--;
        return result;
    }

    @SuppressWarnings("WeakerAccess")
    public int capacity() {
        return elements.length;
    }

    public int size() {
        return available;
    }

    public boolean isEmpty() {
        return size() == 0;
    }
}
