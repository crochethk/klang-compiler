package cc.crochethk.klang.visitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import cc.crochethk.klang.ast.*;
import cc.crochethk.klang.ast.BinOpExpr.BinaryOp;
import cc.crochethk.klang.ast.MemberAccess.*;
import cc.crochethk.klang.ast.literal.*;
import cc.crochethk.klang.visitor.Type.CheckedParam;

import static cc.crochethk.klang.visitor.BuiltinDefinitions.*;

/** TypeChecker Visitor
 * The main task of this Visitor is to semantically check typing of the visited AST.
 * While doing that each visited Node is annotated with its inferred type information
 * by assigning an appropriate Type object to {@code Node.theType}.
 */
public class TypeChecker implements Visitor {
    int errorsReported = 0;

    private void reportError(Node node, String s) {
        errorsReported++;
        System.err.println("(L" + node.line() + ":" + node.column() + ") Type error: " + s);
    }

    @Override
    public void visit(I64Lit i64Lit) {
        i64Lit.theType = Type.LONG_T;
    }

    @Override
    public void visit(F64Lit f64Lit) {
        f64Lit.theType = Type.DOUBLE_T;
    }

    @Override
    public void visit(BoolLit boolLit) {
        boolLit.theType = Type.BOOL_T;
    }

    @Override
    public void visit(StringLit stringLit) {
        stringLit.theType = Type.STRING_T;
    }

    @Override
    public void visit(NullLit nullLit) {
        nullLit.theType = Type.ANY_T;
    }

    @Override
    public void visit(Var var) {
        var varType = funDefVarTypes.get(var.name);
        if (varType == null) {
            reportError(var, "Use of undefined variable '" + var.name + "'");
        } else if (!initializedFunDefVars.contains(var.name)) {
            reportError(var, "Use of uninitialized variable '" + var.name + "'");
        }
        var.theType = varType;
    }

    @Override
    public void visit(FunCall funCall) {
        funCall.args.forEach(arg -> arg.accept(this));

        var funDef = funDefs.get(funCall.name);
        if (funDef == null) {
            // no such user defined function found
            // -> check builtins
            var argTypes = funCall.args.stream().map(arg -> arg.theType).toList();
            var autoFunSign = findBuiltinFun(funCall.name, argTypes);
            autoFunSign.ifPresentOrElse(funSign -> {
                checkArgsMatchParams(funCall, funCall.args, funSign.params());
                funCall.theType = funSign.returnType();
            }, () -> {
                reportError(funCall, "Unknown function '" + funCall.name + "'");
                funCall.theType = Type.UNKNOWN_T;
            });
        } else {
            //funDef found
            funCall.theType = funDef.returnType.theType;
            checkArgsMatchParams(funCall, funCall.args, Parameter.toChecked(funDef.params));
        }
    }

    /**
     * Check whether provided list of arguments matches the provided list of
     * parameter definitions.
     * @param nodeCtx Node context in which this check is performed.
     */
    private void checkArgsMatchParams(Node nodeCtx, List<Expr> args, List<CheckedParam> params) {
        var paramCount = params.size();
        var argsCount = args.size();
        if (paramCount != argsCount) {
            reportError(nodeCtx, "Wrong number of arguments: expected " + paramCount
                    + " but " + argsCount + " provided");
        }

        var argsIter = args.iterator();
        for (var p : params) {
            if (!argsIter.hasNext()) {
                break;
            }
            var arg = argsIter.next();
            checkAssignmentTypeCompatibility(arg, p.type());
        }
    }

    @Override
    public void visit(MemberAccessChain maChain) {
        maChain.owner.accept(this);

        final var chain = maChain.chain;
        chain.theType = checkMemberAccessType(maChain.owner, chain);
        chain.accept(this);
        var finalMember = chain.getLast();
        Expr finalMemberOwner = finalMember.owner != null ? finalMember.owner : maChain.owner;

        // Basically the field or method-return-type of the end member
        var finalType = checkMemberAccessType(finalMemberOwner, finalMember);
        finalMember.theType = finalType;
        maChain.theType = finalType;
    }

    private Type checkMemberAccessType(Expr ownerExpr, MemberAccess ma) {
        Type maTheType = switch (ma) {
            case MemberAccess.FieldGet _ -> checkFieldGetOrSetTargetType(ownerExpr, ma.targetName);
            case MemberAccess.FieldSet _ -> checkFieldGetOrSetTargetType(ownerExpr, ma.targetName);
            case MemberAccess.MethodCall methCall -> {
                methCall.args.forEach(arg -> arg.accept(this));
                yield checkMethodCallTargetType(ownerExpr, methCall.targetName, methCall.args);
            }
            default -> {
                throw new IllegalArgumentException("Unknown MemberAccess implementation '" +
                        ma != null ? ma.getClass().getSimpleName() : ma + "'");
            }
        };
        return maTheType;
    }

    /**
     * Common logic for type checking FieldSet and FieldGet MemberAccess nodes.
     * @param structExpr _Already checked_ node of a struct type. Must not be null.
     * @param fieldName Name of the field to check in context of the provided
     *  owner struct instance.
     * @return The type of the struct field referenced by "structExpr.fieldName"
     *  or 'UNKNOWN_T' if a type check error occurred.
     */
    private Type checkFieldGetOrSetTargetType(Node structExpr, String fieldName) {
        var stDef = structDefs.get(structExpr.theType.klangName());
        if (stDef == null) {
            reportError(structExpr, "Node evaluates to '" + prettyTheTypeName(structExpr)
                    + "' but must be a struct type instance.");
            return Type.UNKNOWN_T;
        }

        // Check whether the field does exist in stDef and get its 'theType'
        var fieldTheType = stDef.fields.stream()
                .filter(f -> f.name().equals(fieldName))
                .map(f -> f.type().theType)
                .findFirst();

        return fieldTheType.orElseGet(() -> {
            reportError(structExpr, "Field '" + fieldName + "' not defined for type '"
                    + prettyTheTypeName(structExpr) + "'");
            return Type.UNKNOWN_T;
        });
    }

    /**
     * Check whether the type of {@code ownerExpr} has a method definition matching
     * the method name and arguments.
     * <ul>
     *  <li>{@code ownerExpr} must not be null</li>
     *  <li>{@code ownerExpr} must have already been type checked (i.e. {@code theType!=null})</li>
     *  <li>all {@code args} must have already been checked ({@code arg.theType!=null})</li>
     * </ul>
     */
    private Type checkMethodCallTargetType(Expr ownerExpr, String methName, List<Expr> args) {
        // Get methods associated with ownerExpr's type
        var ownersMethods = methDefs.get(ownerExpr.theType);

        if (ownersMethods == null) {
            reportError(ownerExpr, String.format(
                    "Cannot call method '%s' on type '%s' which has no method definitions.",
                    methName, prettyTheTypeName(ownerExpr)));
            return Type.UNKNOWN_T;
        }

        var methDef = ownersMethods.get(methName);
        if (methDef == null) {
            reportError(ownerExpr, String.format("No '%s' method defined for type '%s'.",
                    methName, prettyTheTypeName(ownerExpr)));
            return Type.UNKNOWN_T;
        }

        // Check whether args match params of definition, skipping self-parameter
        var params = methDef.def.params.stream().skip(1).toList();
        checkArgsMatchParams(ownerExpr, args, Parameter.toChecked(params));
        return methDef.def.returnType.theType;
    }

    @Override
    public void visit(MemberAccess ma) {
        /**
         * - For 'ma.owner==null' we expect the type to already been set externally
         * - 'ma.theType' will be the one the member 'targetName' resolves to
         */
        if (ma.owner != null) {
            ma.theType = checkMemberAccessType(ma.owner, ma);
        } else if (ma.theType == null) {
            throw new IllegalArgumentException("'MemberAccess.owner' and 'MemberAccess.theType' "
                    + "must not be null simultaneously. Make sure if 'MemberAccess.owner' is null, "
                    + "MemberAccess.theType has already been set externally!");
        }

        // Advance to next accessor if there is any
        if (ma.next != null) {
            ma.next.accept(this);
        }
    }

    @Override
    public void visit(FieldGet fieldGet) {
        visit((MemberAccess) fieldGet);
    }

    @Override
    public void visit(FieldSet fieldSet) {
        visit((MemberAccess) fieldSet);
    }

    @Override
    public void visit(MethodCall methodCall) {
        visit((MemberAccess) methodCall);
    }

    private String prettyTheTypeName(Node n) {
        return n.theType.prettyTypeName();
    };

    @Override
    public void visit(ConstructorCall constCall) {
        // Basically same constraints apply as in funCall
        constCall.args.forEach(arg -> arg.accept(this));

        var structDef = structDefs.get(constCall.structName);
        if (structDef == null) {
            reportError(constCall, "Unknown constructor '" + constCall.structName + "'");
            constCall.theType = Type.UNKNOWN_T;
        } else {
            //structDef found
            constCall.theType = structDef.theType;

            var fieldsCount = structDef.fields.size();
            var argsCount = constCall.args.size();
            if (fieldsCount != argsCount) {
                reportError(constCall,
                        "Wrong number of arguments: expected " + fieldsCount
                                + " but " + argsCount + " provided");
            }

            var argsIter = constCall.args.iterator();
            for (var field : structDef.fields) {
                if (!argsIter.hasNext()) {
                    break;
                }
                var arg = argsIter.next();
                checkAssignmentTypeCompatibility(arg, field.type().theType);
            }
        }
    }

    @Override
    public void visit(BinOpExpr binOpExpr) {
        // Compute type of the operands
        binOpExpr.lhs.accept(this);
        binOpExpr.rhs.accept(this);
        var lhsType = binOpExpr.lhs.theType;
        var rhsType = binOpExpr.rhs.theType;
        var op = binOpExpr.op;

        Type exprType;
        if (op.isBoolean()) {
            exprType = Type.BOOL_T;
            if (!lhsType.equals(exprType) || !rhsType.equals(exprType)) {
                reportBinOpExprErrorMsg(binOpExpr, "Operands must be boolean.");
            }
        } else if (op.isArithmetic()) {
            exprType = lhsType;
            if (!lhsType.isNumeric() || !rhsType.isNumeric() || !rhsType.equals(exprType)) {
                reportBinOpExprErrorMsg(binOpExpr, "Operand types must be numeric and equal");
            } else if (op == BinaryOp.mod) {
                if (!lhsType.equals(Type.LONG_T) || !rhsType.equals(Type.LONG_T)) {
                    reportError(binOpExpr, "Modulo only supports integral type operands.");
                }
            }
        } else if (op.isEqualityComparison()) {
            exprType = Type.BOOL_T;
            var isComparable = lhsType.isReference() && rhsType.isReference()
                    || lhsType.equals(rhsType);
            if (!isComparable) {
                reportBinOpExprErrorMsg(binOpExpr, "Operand types must be equal or RefType");
            }
        } else if (op.isOrdinalComparison()) {
            exprType = Type.BOOL_T;
            var isComparable = lhsType.equals(rhsType) && lhsType.isNumeric();
            if (!isComparable) {
                reportBinOpExprErrorMsg(binOpExpr,
                        "Operand types must be of equal, primitive, numerical kind");
            }
        } else {
            throw new UnsupportedOperationException("Unknown binary operator: " + op);
        }

        binOpExpr.theType = Objects.requireNonNull(exprType, "Expected valid Type object but was null");
    }

    private void reportBinOpExprErrorMsg(BinOpExpr expr, String hint) {
        reportError(expr, "Can't use binary '" + expr.op.toLexeme() + "' with operands "
                + prettyTheTypeName(expr.lhs) + ", " + prettyTheTypeName(expr.rhs)
                + (hint == null ? "" : " (" + hint + ")"));
    }

    @Override
    public void visit(UnaryOpExpr unaryOpExpr) {
        unaryOpExpr.operand.accept(this);
        var operandType = unaryOpExpr.operand.theType;
        unaryOpExpr.theType = operandType;
        var op = unaryOpExpr.op;

        if (op.isBoolean() && (operandType.isNumeric() || operandType.isReference())) {
            reportError(unaryOpExpr, "Boolean operator incompatible with '"
                    + operandType.prettyTypeName() + "' operand");
        }
        if (op.isArithmetic() && !operandType.isNumeric()) {
            reportError(unaryOpExpr, "Arithmetic operator '" + op
                    + "' incompatible with '" + operandType.prettyTypeName() + "' operand");
        }
    }

    @Override
    public void visit(TypeCast typeCast) {
        typeCast.expr.accept(this);
        typeCast.targetType.accept(this);
        Type exprType = typeCast.expr.theType;
        Type targetType = typeCast.targetType.theType;

        if (!exprType.isNumeric() || !targetType.isNumeric()) {
            reportError(typeCast, "Can't convert '" + exprType.prettyTypeName()
                    + "' to '" + targetType.prettyTypeName() + "': incompatible types.");
        }
        typeCast.theType = targetType;
    }

    @Override
    public void visit(TernaryConditionalExpr ternaryConditionalExpr) {
        ternaryConditionalExpr.condition.accept(this);
        ternaryConditionalExpr.then.accept(this);
        ternaryConditionalExpr.otherwise.accept(this);
        var condType = ternaryConditionalExpr.condition.theType;
        var thenType = ternaryConditionalExpr.then.theType;
        var otherwiseType = ternaryConditionalExpr.otherwise.theType;
        ternaryConditionalExpr.theType = thenType;

        if (!thenType.isCompatible(otherwiseType)) {
            reportError(ternaryConditionalExpr.then, "Conditional branches return incompatible types");
        }
        if (!condType.equals(Type.BOOL_T)) {
            reportError(ternaryConditionalExpr.condition, "Condition must return a boolean type");
        }
    }

    @Override
    public void visit(VarDeclareStat varDeclareStat) {
        var declaredType = varDeclareStat.declaredType;
        declaredType.ifPresent(typeNode -> typeNode.accept(this));

        var initializer = varDeclareStat.initializer;
        initializer.ifPresent(init -> {
            init.expr.accept(this);
            init.theType = init.expr.theType;
            // mark as initialized
            initializedFunDefVars.add(varDeclareStat.varName());
        });

        if (declaredType.isPresent()) {
            varDeclareStat.theType = declaredType.get().theType;
            if (initializer.isPresent()) {
                // check compatibility
                checkAssignmentTypeCompatibility(initializer.get().expr, varDeclareStat.theType);
            }
        } else if (initializer.isPresent()) {
            // only initializer -> infer the type
            varDeclareStat.theType = initializer.get().theType;
        } else {
            varDeclareStat.theType = Type.UNKNOWN_T;
            reportError(varDeclareStat, "Variable declaration must provide "
                    + "an explicit type and/or an initializer.");
        }

        var previous = funDefVarTypes.put(varDeclareStat.varName(), varDeclareStat.theType);
        if (previous != null) {
            reportError(varDeclareStat,
                    "Attempt to redeclare variable '" + varDeclareStat.varName() + "'");
        }
    }

    @Override
    public void visit(VarAssignStat varAssignStat) {
        var varType = funDefVarTypes.get(varAssignStat.targetVarName);
        varAssignStat.expr.accept(this);
        // mark as initialized
        initializedFunDefVars.add(varAssignStat.targetVarName);

        if (varType == null) {
            varType = Type.UNKNOWN_T;
            reportError(varAssignStat, "Assignment to undeclared variable '"
                    + varAssignStat.targetVarName + "'");
        } else {
            checkAssignmentTypeCompatibility(varAssignStat.expr, varType);
        }
        varAssignStat.theType = varType;
    }

    /**
     * Check whether the type of expression {@code exprNode} is assignable to a
     * variable declared or inferred as type {@code varType}.
     */
    void checkAssignmentTypeCompatibility(Expr exprNode, Type varType) {
        if (!exprNode.theType.isCompatible(varType)) {
            reportError(exprNode, String.format("Cannot assign expression "
                    + "of type '%s' to variable with incompatible type '%s'.",
                    prettyTheTypeName(exprNode), varType.prettyTypeName()));
        }
    }

    @Override
    public void visit(FieldAssignStat fieldAssStat) {
        fieldAssStat.maChain.accept(this);
        fieldAssStat.expr.accept(this);
        var fType = fieldAssStat.maChain.theType;
        var exprType = fieldAssStat.expr.theType;
        fieldAssStat.theType = fType;
        if (!exprType.isCompatible(fType)) {
            reportError(fieldAssStat, "Attempt to assign expression of type '"
                    + exprType.prettyTypeName() + "' to field '" + fieldAssStat.maChain.getLast().targetName
                    + "' of incompatible type '" + fType.prettyTypeName() + "'");
        }
        if (fieldAssStat.maChain.getLast() instanceof MethodCall) {
            reportError(fieldAssStat, "Cannot assign to a method call");
        }
    }

    @Override
    public void visit(IfElseStat ifElseStat) {
        ifElseStat.condition.accept(this);
        ifElseStat.then.accept(this);
        var condType = ifElseStat.condition.theType;
        var thenType = ifElseStat.then.theType;
        ifElseStat.otherwise.accept(this);

        if (!condType.equals(Type.BOOL_T)) {
            reportError(ifElseStat.condition, "Condition must evaluate to boolean but is '"
                    + condType.prettyTypeName() + "'");
        }

        ifElseStat.theType = thenType;
    }

    /** Indicates that the visitor is currently in a loop context */
    private boolean isLoopContext = false;

    @Override
    public void visit(LoopStat loopStat) {
        var prevState = isLoopContext;

        isLoopContext = true;
        loopStat.body.accept(this);
        loopStat.theType = Type.VOID_T;

        isLoopContext = prevState;
    }

    @Override
    public void visit(StatementList statementList) {
        statementList.statements.forEach(s -> s.accept(this));
        statementList.theType = statementList.isEmpty()
                ? Type.VOID_T
                : statementList.statements.getLast().theType;
    }

    @Override
    public void visit(ReturnStat returnStat) {
        var retType = currentFun.returnType.theType;
        currentFunReturnCount += 1;
        returnStat.theType = retType;

        returnStat.expr.accept(this);
        var exprType = returnStat.expr.theType;
        if (!exprType.isCompatible(retType)) {
            reportError(returnStat, "Expected return type '" + retType.prettyTypeName()
                    + "' but found incompatible '" + exprType.prettyTypeName() + "'");
        }
    }

    @Override
    public void visit(BreakStat breakStat) {
        if (!isLoopContext) {
            reportError(breakStat, "'break' only allowed inside loop body");
        }
        breakStat.theType = Type.VOID_T;
    }

    @Override
    public void visit(DropStat dropStat) {
        dropStat.refTypeVar.accept(this);
        if (!dropStat.refTypeVar.theType.isReference())
            reportError(dropStat, "Only reference type variables can be dropped");
        dropStat.theType = Type.VOID_T;
    }

    @Override
    public void visit(VoidResultExprStat voidResultExprStat) {
        voidResultExprStat.expr.accept(this);
        voidResultExprStat.theType = voidResultExprStat.expr.theType;
        if (voidResultExprStat.theType != Type.VOID_T) {
            reportError(voidResultExprStat, "Non-void result type must be assigned.");
        }
    }

    @Override
    public void visit(TypeNode type) {
        var theType = Type.of(type.typeToken, "" /*default package */);
        type.theType = theType;
        if (!theType.isBuiltin() && !structDefs.containsKey(type.typeToken)) {
            reportError(type, "Undefined type '" + type.typeToken + "'");
        }
    }

    /**
     * Mapping of a function's local variable names and their  associated declared/inferred types.
     * Here only references to "Node.theType" are to be added.
     */
    private Map<String, Type> funDefVarTypes = new HashMap<>();
    private Set<String> initializedFunDefVars = new HashSet<>();

    /** Reference to currently inspected function def */
    private FunDef currentFun = null;
    /** Return statements count for currentFun */
    private int currentFunReturnCount = 0;

    @Override
    public void visit(FunDef funDef) {
        // Hint: Signature nodes are already evaluated in Prog
        currentFun = funDef;
        currentFunReturnCount = 0;

        // Make sure the local variables lookup tables are empty before proceeding
        funDefVarTypes.clear();
        initializedFunDefVars.clear();

        funDef.params.forEach(p -> {
            var previous = funDefVarTypes.put(p.name(), p.type().theType);
            if (previous != null) {
                reportError(funDef, "Duplicate parameter name '" + p.name()
                        + "' in function '" + funDef.name + "'");
            }
            initializedFunDefVars.add(p.name());
        });

        // Check return type consistency in the according node(s)
        // using "currentFun" and the "currentFunReturnCount" counter
        funDef.body.accept(this);

        // Infer 'void' for no returns
        if (currentFunReturnCount == 0 && !funDef.returnType.theType.equals(Type.VOID_T)) {
            reportError(funDef, "Declared return type '" + prettyTheTypeName(funDef.returnType)
                    + "' but no return statement was found");
        }

        funDef.theType = funDef.returnType.theType;
    }

    @Override
    public void visit(StructDef structDef) {
        // structDef.theType is set already in 'visit(Prog)'
        Set<String> fnames = new HashSet<>();
        structDef.fields.forEach(f -> {
            if (!fnames.add(f.name())) {
                reportError(structDef, "Duplicate field '" + structDef.name + "." + f.name() + "'");
            }
            f.type().accept(this);
        });
        structDef.methods.forEach(meth -> meth.accept(this));
    }

    @Override
    public void visit(MethDef methDef) {
        // Hint: Signature nodes are already evaluated in Prog
        methDef.def.accept(this);
        methDef.theType = methDef.def.theType;
    }

    private Map<String, FunDef> funDefs = new HashMap<>();
    private Map<String, StructDef> structDefs = new HashMap<>();

    /**
     * Lookup for all methods definitions with following semantics:
     * {@code theType -> (methName -> MethDef)}
     */
    private Map<Type, Map<String, MethDef>> methDefs = new HashMap<>();

    @Override
    public void visit(Prog prog) {
        prog.structDefs.forEach(def -> {
            var previous = structDefs.put(def.name, def);
            if (previous != null) {
                reportError(def, "Struct type '" + def.name + "' defined multiple times");
            }
        });

        // Before traversing the tree, we need to make funDefs available for other "visit"s
        prog.funDefs.forEach(funDef -> {
            var previous = funDefs.put(funDef.name, funDef);
            if (previous != null || BuiltinDefinitions.isBuiltinFunName(funDef.name)) {
                reportError(funDef, "Function '" + funDef.name + "' defined multiple times");
            }

            // Enables call to fun whose body was not checked yet
            funDef.params.forEach(p -> p.type().accept(this));
            funDef.returnType.accept(this);
        });

        /**
         * We need to also check all method definition signatures in advance,
         * since if we were to step into each structDef and start to check its
         * methods, it'd be possible to encounter method calls of other types
         * whose definitions/methods were not checked yet, thus we could not be
         * sure what the method call's type resolves to... at least not cleanly...
         */
        // Type check struct method signatures collecting them in lookup table 
        prog.structDefs.forEach(stDef -> {
            stDef.theType = Type.of(stDef.name, "" /*default package */);
            var methMap = new HashMap<String, MethDef>();
            methDefs.put(stDef.theType, methMap);

            stDef.methods.forEach(meth -> {
                var previous = methMap.put(meth.name(), meth);
                if (previous != null) {
                    reportError(meth, "Method '" + meth.name() + "' defined multiple times");
                }

                // Enables call to methods whose body was not checked yet
                meth.params().forEach(p -> p.type().accept(this));
                meth.returnType().accept(this);
            });
        });

        // finally go and check the actual definitions
        prog.structDefs.forEach(def -> def.accept(this));
        prog.funDefs.forEach(funDef -> funDef.accept(this));

        prog.theType = Type.VOID_T;

        // Check optional entrypoint function conventions
        prog.entryPoint.ifPresent(entryPoint -> {
            entryPoint.accept(this);
            var epName = entryPoint.name;
            var epDef = funDefs.get(epName);

            if (epDef.params.size() > 0) {
                reportError(prog, "Entry point function '" + epName
                        + "' must not have any parameters.");
            }
            if (epDef.returnType.theType != Type.VOID_T) {
                reportError(prog, "Main function must return '" + Type.VOID_T.prettyTypeName()
                        + "', but returns '" + prettyTheTypeName(epDef.returnType) + "'.");
            }

            prog.theType = entryPoint.theType;
        });

        if (errorsReported != 0) {
            throw new TypeCheckFailedException();
        }
    }

    @Override
    public void visit(EmptyExpr emptyExpr) {
        emptyExpr.theType = Type.VOID_T;
    }

    public class TypeCheckFailedException extends RuntimeException {
        public TypeCheckFailedException() {
            super("TypeCheck resulted in " + errorsReported + " errors.");
        }
    }
}
