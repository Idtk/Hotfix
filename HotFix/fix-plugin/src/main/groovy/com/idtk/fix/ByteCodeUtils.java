package com.idtk.fix;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

/**
 * Created by mzc on 2017/9/7.
 * from instant run
 */

public class ByteCodeUtils {
    public static final String CONSTRUCTOR = "<init>";
    public static final String CLASS_INITIALIZER = "<clinit>";
    private static final Type NUMBER_TYPE = Type.getObjectType("java/lang/Number");
    private static final Method SHORT_VALUE = Method.getMethod("short shortValue()");
    private static final Method BYTE_VALUE = Method.getMethod("byte byteValue()");

    /**
     * Generates unboxing bytecode for the passed type. An {@link Object} is expected to be on the
     * stack when these bytecodes are inserted.
     *
     * ASM takes a short cut when dealing with short/byte types and convert them into int rather
     * than short/byte types. This is not an issue on the jvm nor Android's ART but it is an issue
     * on Dalvik.
     *
     * @param mv the {@link GeneratorAdapter} generating a method implementation.
     * @param type the expected un-boxed type.
     */
    public static void unbox(GeneratorAdapter mv, Type type) {
        if (type.equals(Type.SHORT_TYPE)) {
            mv.checkCast(NUMBER_TYPE);
            mv.invokeVirtual(NUMBER_TYPE, SHORT_VALUE);
        } else if (type.equals(Type.BYTE_TYPE)) {
            mv.checkCast(NUMBER_TYPE);
            mv.invokeVirtual(NUMBER_TYPE, BYTE_VALUE);
        } else {
            mv.unbox(type);
        }
    }
}
