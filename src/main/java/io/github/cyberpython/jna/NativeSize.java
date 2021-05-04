package io.github.cyberpython.jna;

import com.sun.jna.IntegerType;
import com.sun.jna.Native;

/**
 * JNA integer type for Size_T
 */
public class NativeSize extends IntegerType {
    /** Size of size_t */
    public static int SIZE = Native.SIZE_T_SIZE;

    public NativeSize() {
        this(0);
    }

    public NativeSize(long value) {
        super(SIZE, value);
    }
}
