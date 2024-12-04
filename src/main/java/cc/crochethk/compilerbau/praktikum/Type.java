package cc.crochethk.compilerbau.praktikum;

import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;

/**
 * Basically a wrapper of TypeKind with some additional convenience
 * methods for mapping source types to JVM types, required for code generation.
 */
public sealed interface Type permits Type.PrimType, Type.RefType {
    /**
     * The java type/class name (e.g. "boolean" or "String")
     */
    String jvmName();

    /** 
     * Name of the jvm type as full descriptor string, such as "Z" for boolean or
     * "Ljava/lang/String;" for the reference type String.
     * It can then be passed to {@code ClassDesc.ofDescriptor(...)} to create
     * the matching ClassDesc.
     */
    String jvmDescriptor();

    TypeKind jvmTypeKind();

    default ClassDesc classDesc() {
        return ClassDesc.ofDescriptor(jvmDescriptor());
    }

    default int jvmSize() {
        return jvmTypeKind().slotSize();
    }

    int byteSize();

    boolean isPrimitive();

    default boolean isReference() {
        return !isPrimitive();
    }

    default boolean isNumeric() {
        return this.equals(LONG_T) || this.equals(DOUBLE_T);
    }

    final Type STRING_T = new RefType("String", "java.lang");
    final Type LONG_T = new PrimType(TypeKind.LongType, 8);
    final Type BOOL_T = new PrimType(TypeKind.BooleanType, 1);
    final Type DOUBLE_T = new PrimType(TypeKind.DoubleType, 4);
    final Type VOID_T = new PrimType(TypeKind.VoidType, 0);

    /**
     * Not a real jvm type. Just placeholder to avoid null, where the type
     * couldn't be determined
     */
    Type UNKNOWN_T = new RefType("UNKNOWN", "");

    /**
     * Convert given source type token to a corresponding JVM type representation.
     * @param typeName The type name as defined in the source / by grammar.
     * @param packageName The package containing the specified typeName
     */
    static Type of(String typeName, String packageName) {
        return switch (typeName) {
            case "string" -> STRING_T;
            case "i64" -> LONG_T;
            case "bool" -> BOOL_T;
            case "f64" -> DOUBLE_T;
            case "void" -> VOID_T;
            default -> new RefType(typeName, packageName);
        };
    }

    record PrimType(TypeKind jvmTypeKind, int byteSize) implements Type {
        @Override
        public boolean isPrimitive() {
            return true;
        }

        @Override
        public String jvmName() {
            return jvmTypeKind().name();
        }

        @Override
        public String jvmDescriptor() {
            return jvmTypeKind().descriptor();
        }
    }

    record RefType(String className, String packageName) implements Type {
        @Override
        public boolean isPrimitive() {
            return false;
        }

        @Override
        public String jvmName() {
            return packageName() + "." + className();
        }

        @Override
        public TypeKind jvmTypeKind() {
            return TypeKind.ReferenceType;
        }

        @Override
        public String jvmDescriptor() {
            return jvmTypeKind().descriptor() + packageName().replace('.', '/') + "/" + className() + ";";
        }

        @Override
        public int byteSize() {
            return 8;
        }
    }
}
