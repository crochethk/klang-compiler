package cc.crochethk.klang.visitor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import cc.crochethk.klang.visitor.Type.*;
import static cc.crochethk.klang.visitor.Type.CheckedParam.toTypes;;

public class BuiltinDefinitions {
    // --------------------[ Static functions ]---------------------------------

    /** Print a string to stdout */
    public static final String NAME_FN_PRINT = "print";
    public static final Signature FN_PRINT_STR = Signature.of(NAME_FN_PRINT, Type.VOID_T,
            new CheckedParam("val", Type.STRING_T));
    public static final Signature FN_PRINT_I64 = Signature.of(NAME_FN_PRINT, Type.VOID_T,
            new CheckedParam("val", Type.LONG_T));
    public static final Signature FN_PRINT_F64 = Signature.of(NAME_FN_PRINT, Type.VOID_T,
            new CheckedParam("val", Type.DOUBLE_T));

    /** Set of builtin auto-generated (static) functions (e.g. "print(myString)"). */
    private static final Map<String, Map<List<Type>, Signature>> autoFunctions = Map.of(
            NAME_FN_PRINT, Map.of(
                    toTypes(FN_PRINT_STR.params()), FN_PRINT_STR,
                    toTypes(FN_PRINT_I64.params()), FN_PRINT_I64,
                    toTypes(FN_PRINT_F64.params()), FN_PRINT_F64//
            ));

    /**
     * If a builtin function identified by {@code name} and {@code args} exists,
     * returns its {@code Signature} wrapped in an {@code Optional}. Otherwise 
     * an empty {@code Optional} is returned.
     */
    public static Optional<Signature> findBuiltinFun(String name, List<Type> argTypes) {
        return Optional.ofNullable(autoFunctions.get(name))
                .map(funSignatures -> funSignatures.get(argTypes));
    }

    // // Makes no sense ATM since syntax does not support
    // // public static Optional<Signature> findBuiltinStaticMethod(Type ownerType, String methName) {
    // //     // return Optional.ofNullable(auto.get(name));
    // //     throw new UnsupportedOperationException("function not implemented");
    // // }

    // --------------------[ Instance Methods ]---------------------------------
    public static Optional<Signature> findBuiltinMethod(Type ownerType, String methName) {
        // return Optional.ofNullable(auto.get(name));
        throw new UnsupportedOperationException("function not implemented");
        //TODO
    }

    /**
     * Instance methods, auto-implemented for all (usable) reference types, such
     * as structs or strings (e.g. "mystruct.to_string()").
     */
    @SuppressWarnings("unused")
    private final Map<String, Signature> generalMethods = Map.of(
            "to_string", Signature.of("to_string", Type.STRING_T));

    /**
     * Instance methods auto-implemented for a specific type (e.g. "myString.len()")
     */
    @SuppressWarnings("unused")
    private final Map<Type, Map<String, Signature>> specialMethods = Map.of(
            Type.STRING_T, Map.of("len", Signature.of("len", Type.STRING_T)));
}
