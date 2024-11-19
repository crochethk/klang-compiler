package cc.crochethk.compilerbau.praktikum;

public sealed interface Type permits Type.PrimType, Type.RefType {
    /**
     * The java type/class name (e.g. "boolean" or "String")
     */
    String name();

    /** 
     * Name of the jvm type as full descriptor string, such as "Z" for boolean or
     * "Ljava/lang/String;" for the reference type String.
     * It can then be passed to {@code ClassDesc.ofDescriptor(...)} to create
     * the matching ClassDesc.
     */
    String jvmDescriptor();

    /**
     * How many slots of the JVM's operand stack this type takes up.
     * All JVM types take 1 slot except for {@code double} and {@code long} which take 2.
     * 
     * See bottom of {@url https://docs.oracle.com/javase/specs/jvms/se23/html/jvms-2.html#jvms-2.6.2} 
     */
    int jvmSize();

    boolean isPrimitive();

    default boolean isReference() {
        return !isPrimitive();
    }

    default boolean isNumeric() {
        return this.equals(LONG_T) || this.equals(DOUBLE_T);
    }

    Type STRING_T = new RefType("String", "java.lang");
    Type LONG_T = new PrimType("long", "J", 2);
    Type BOOLEAN_T = new PrimType("boolean", "Z", 1);
    Type DOUBLE_T = new PrimType("double", "D", 2);
    Type VOID_T = new PrimType("void", "V", 1);

    /**
     * Convert given source type to a corresponding JVM type representation.
     * @param typeName The type name as defined in the source / by grammar.
     * @param packageName The package containing the specified typeName
     */
    static Type of(String typeName, String packageName) {
        return switch (typeName) {
            case "String" -> STRING_T;
            case "int" -> LONG_T;
            case "boolean" -> BOOLEAN_T;
            case "double" -> DOUBLE_T;
            default -> new RefType(typeName, packageName);
        };
    }

    record PrimType(String name, String jvmDescriptor, int jvmSize) implements Type {
        @Override
        public boolean isPrimitive() {
            return true;
        }
    }

    record RefType(String className, String packageName) implements Type {
        @Override
        public String name() {
            return packageName() + "." + className();
        }

        @Override
        public String jvmDescriptor() {
            return "L" + packageName().replace('.', '/') + "/" + className() + ";";
        }

        @Override
        public int jvmSize() {
            return 1;
        }

        @Override
        public boolean isPrimitive() {
            return false;
        }
    }
}
