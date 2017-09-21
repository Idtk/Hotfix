package com.idtk.fix;


import com.android.annotations.NonNull;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.commons.Method;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

/**
 * Created by Idtk on 2017/8/31.
 */

public class ASMInsert {

    public final static String FIX_FIELD_NAME = "$changeFix";
    public final static String FIX_CLASS_NAME = Type.getDescriptor(com.idtk.base.ChangeFix.class);
    public final static String CHANGE_CLASS_NAME = "com.idtk.base.ChangeFix".replace(".", "/");

    private String className = "";
    private File file;
    private String visitedClassName;

    public ASMInsert(File file) {
        super();
        this.file = file;
    }

    public byte[] transformCode(String className, byte[] fb) {
        this.className = className;
        ClassReader cr = new ClassReader(fb);
//        ClassReader cr = new ClassReader(className);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new ClassInsert(cw);
        cr.accept(cv, ClassReader.EXPAND_FRAMES);

        /*byte[] code = cw.toByteArray();
        File file = new File("Idtk.class");
        try {
            FileUtils.writeByteArrayToFile(file,code);
        }catch (IOException e){
            System.out.println("=======IOException=======");
        }*/
        return cw.toByteArray();
    }

    public class ClassInsert extends ClassVisitor {

        private ClassWriter cw;

        public ClassInsert(ClassWriter cw) {
            super(Opcodes.ASM5, cw);
            this.cw = cw;
            this.cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, FIX_FIELD_NAME, FIX_CLASS_NAME, null, null);
        }



        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            return super.visitField(access, name, desc, signature, value);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            System.out.println("method ===> " + access + " === " + name + " === " + desc + " === " + signature);
            if (name.equals(ByteCodeUtils.CLASS_INITIALIZER)||name.equals(ByteCodeUtils.CONSTRUCTOR)){
                return super.visitMethod(access, name, desc, signature, exceptions);
            }else {
                MethodVisitor defaultVisitor = super.visitMethod(access, name, desc, signature, exceptions);
//                JSRInlinerAdapter jsrInlinerAdapter = new JSRInlinerAdapter(defaultVisitor, access, name, desc, signature, exceptions);
//            return new MethodInsert(jsrInlinerAdapter, access, name, desc);
                return new MethodInsert(defaultVisitor, access, name, desc);
            }
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            System.out.println("method2 ===> " + access + " === " + name + " === " + superName + " === " + signature);
            visitedClassName = name;
            /*super.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC
                            | Opcodes.ACC_VOLATILE | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_TRANSIENT,
                    FIX_FIELD_NAME, FIX_CLASS_NAME, null, null);*/
            super.visit(version, access, name, signature, superName, interfaces);
        }
    }

    /*@NonNull
    protected static String getRuntimeTypeName(@NonNull Type type) {
        return "L" + type.getInternalName() + ";";
    }*/

    public class MethodInsert extends GeneratorAdapter {

        private MethodVisitor mv;
        private final Label start;
        private int change;
        private Type returnType;
        private String name;
        private String desc;
        private List<Type> types=new ArrayList();
        private boolean isStatic;

        protected MethodInsert(MethodVisitor mv, int access, String name, String desc) {
            super(Opcodes.ASM5, mv, access, name, desc);
            this.mv = mv;
            this.start = new Label();
            this.change = -1;
            this.returnType = Type.getReturnType(desc);
            this.name = name;
            this.desc = desc;
            this.types = new ArrayList<Type>(Arrays.asList(Type.getArgumentTypes(desc)));
            this.isStatic = (access & Opcodes.ACC_STATIC) != 0;
            if (!isStatic){
                types.add(0, Type.getType(Object.class));
            }
        }

        @Override
        public void visitCode() {
            // local var
            super.visitLabel(start);
            change = newLocal(Type.getType(FIX_CLASS_NAME));
            System.out.println("change ===> " + change+":"+Type.getType(FIX_CLASS_NAME)+":"+Type.getObjectType(FIX_CLASS_NAME));
            mv.visitFieldInsn(Opcodes.GETSTATIC, visitedClassName, FIX_FIELD_NAME, FIX_CLASS_NAME);
            storeLocal(change);
            // localChangeFix != null
            loadLocal(change);
            // redirect
            Label l1 = new Label();
            visitJumpInsn(IFNULL, l1);
            // localChangeFix.isSupport("name+'.'+desc") == true
            loadLocal(change);
            push(name+"."+desc);
            mv.visitMethodInsn(INVOKEINTERFACE, "com/idtk/base/ChangeFix", "isSupport", "(Ljava/lang/String;)Z", true);
            mv.visitJumpInsn(IFEQ, l1);

            // object
            loadLocal(change);
            // param 1
            visitInsn(isStatic ? Opcodes.ICONST_1 : Opcodes.ICONST_0);
            // param 2
            push(name+"."+desc);
            // param 3
            createObjectArray();
            // invoke
            visitMethodInsn(INVOKEINTERFACE, "com/idtk/base/ChangeFix", "accessDispatch",
                    "(ZLjava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;", true);

            // return
            if (Type.getReturnType(desc) == Type.VOID_TYPE) {
                pop();
            } else {
                ByteCodeUtils.unbox(this, Type.getReturnType(desc));
            }
            returnValue();

            // end redirect
            visitLabel(l1);

            /*visitFieldInsn(Opcodes.GETSTATIC, visitedClassName, FIX_FIELD_NAME, FIX_CLASS_NAME);
            visitVarInsn(ASTORE, 2);
            Label l2 = new Label();
            // if
            visitVarInsn(ALOAD, 2);
            visitJumpInsn(IFNULL, l2);
            // 实例
            visitVarInsn(ALOAD, 2);
            // param 1
            visitVarInsn(ALOAD,0);
            // param 2
            visitLdcInsn(name+"."+desc);
            // param 3
            visitInsn(ICONST_1);
            visitTypeInsn(ANEWARRAY, "java/lang/Object");
//            visitVarInsn(ASTORE, 3);
//            visitVarInsn(ALOAD,3);
//            visitVarInsn(ALOAD,3);
            visitInsn(DUP);
            visitInsn(ICONST_0);
            visitVarInsn(ALOAD, 0);
            visitInsn(AASTORE);

            visitMethodInsn(INVOKEINTERFACE, "com/idtk/base/ChangeFix", "accessDispatch",
                    "(Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;", true);
            visitInsn(POP);
            visitInsn(RETURN);
            visitLabel(l2);*/

            super.visitCode();
        }

        public void createObjectArray(){
            push(types.size());
            newArray(Type.getType(Object.class));
            int stack = 0;
            for (int i=0; i<types.size(); i++){
                Type type = types.get(i);
                dup();
                push(i);
                visitVarInsn(type.getOpcode(ILOAD), stack);
                box(type);
//                System.out.println("Otype ===> " + Type.getType(Object.class));
                arrayStore(Type.getType(Object.class));
                stack += type.getSize();
            }
        }


        @Override
        public void visitEnd() {
            super.visitEnd();
        }
    }
}
