package cc.crochethk.klang.visitor;

import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.util.Map;

/**
 * Basically a wrapper of TypeKind with some additional convenience
 * methods for mapping source types to JVM and C types, required for code generation.
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

    String cTypeName();

    boolean isPrimitive();

    default boolean isReference() {
        return !isPrimitive();
    }

    default boolean isNumeric() {
        return this.equals(LONG_T) || this.equals(DOUBLE_T);
    }

    /** Tests whether this and other are compatible in an assignment context. */
    public default boolean isCompatible(Type other) {
        return this.isReference() && other == Type.NULL_T
                || other.isReference() && this == Type.NULL_T
                || this.equals(other);
    }

    public default boolean isBuiltin() {
        return isPrimitive() || this == STRING_T;
    }

    final Type STRING_T = new RefType("String", "java.lang", "char*");
    final Type LONG_T = new PrimType(TypeKind.LongType, AsmTypeKind.LongType);
    final Type BOOL_T = new PrimType(TypeKind.BooleanType, AsmTypeKind.BooleanType);
    final Type DOUBLE_T = new PrimType(TypeKind.DoubleType, AsmTypeKind.DoubleType);
    final Type VOID_T = new PrimType(TypeKind.VoidType, AsmTypeKind.VoidType);
    /**
     * Not an actual type. Just placeholder to avoid NPE, where the type
     * couldn't be determined during type check.
     */
    final Type UNKNOWN_T = new RefType("UNKNOWN", "", "const void*");
    /** Placeholder type for "intentionally" unknown (e.g. null) reference types. */
    final Type NULL_T = new RefType("Object", "java.lang", "void*");

    enum AsmTypeKind {
        LongType(8, "int64_t"),
        BooleanType(1, "bool"),
        DoubleType(8, "double"),
        VoidType(0, "void"),
        ;

        int byteSize;
        String cTypeName;

        AsmTypeKind(int byteSize, String cTypeName) {
            this.byteSize = byteSize;
            this.cTypeName = cTypeName;
        }
    }

    final String STRING_T_NAME = "string";
    final String LONG_T_NAME = "i64";
    final String BOOL_T_NAME = "bool";
    final String DOUBLE_T_NAME = "f64";
    final String VOID_T_NAME = "void";
    final String NULL_T_NAME = "NULL";

    static final Map<Type, String> klangNameMap = Map.of(
            STRING_T, STRING_T_NAME,
            LONG_T, LONG_T_NAME,
            BOOL_T, BOOL_T_NAME,
            DOUBLE_T, DOUBLE_T_NAME,
            VOID_T, VOID_T_NAME,
            NULL_T, NULL_T_NAME);

    default String klangName() {
        var name = klangNameMap.get(this);
        if (name == null)
            if (this instanceof RefType customT)
                return customT.className();
            else
                throw new IllegalArgumentException("Unknown type kind: " + this);
        return name;
    }

    default String prettyTypeName() {
        return this.getClass().getSimpleName() + "[" + klangName() + "]";
    }

    /**
     * Convert given source type token to a corresponding JVM type representation.
     * @param typeName The type name as defined in the source / by grammar.
     * @param packageName The package containing the specified typeName
     */
    static Type of(String typeName, String packageName) {
        return switch (typeName) {
            case STRING_T_NAME -> STRING_T;
            case LONG_T_NAME -> LONG_T;
            case BOOL_T_NAME -> BOOL_T;
            case DOUBLE_T_NAME -> DOUBLE_T;
            case VOID_T_NAME -> VOID_T;
            default -> new RefType(typeName, packageName, "struct " + typeName + "*");
        };
    }

    record PrimType(TypeKind jvmTypeKind, AsmTypeKind asmTypeKind) implements Type {
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

        @Override
        public int byteSize() {
            return asmTypeKind().byteSize;
        }

        @Override
        public String cTypeName() {
            return asmTypeKind().cTypeName;
        }

    }

    record RefType(String className, String packageName, String cTypeName) implements Type {
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
