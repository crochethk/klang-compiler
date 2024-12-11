/// Created with the help of ChatGPT

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import cc.crochethk.compilerbau.praktikum.ast.FunDef;
import cc.crochethk.compilerbau.praktikum.ast.TypeNode;

public class GenCHeader {
    /**
     * Generates a C header file containing all function signatures from the given FunDef nodes.
     *
     * @param functionDefinitions List of FunDef nodes representing functions to include in the header.
     * @param outputDir           Directory to put the generated header file.
     * @param outputFileName      Name of the header file to generate.
     */
    public static void generateHeaderFile(List<FunDef> functionDefinitions, String outputDir, String outputFileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(Path.of(outputDir, outputFileName).toFile()))) {
            var guardName = outputFileName.replace('.', '_').toUpperCase();
            writer.write("// Auto-generated C header file\n");
            writer.write("#ifndef " + guardName + "\n");
            writer.write("#define " + guardName + "\n\n");
            writer.write("#include <stdint.h>\n");
            writer.write("#include <stdbool.h>\n\n");

            for (FunDef function : functionDefinitions) {
                StringBuilder signature = new StringBuilder();

                // Determine return type
                String returnType = mapTypeNodeToCType(function.returnType);
                signature.append(returnType).append(" ");

                // Append function name
                signature.append(function.name).append("(");

                // Add parameters
                if (function.params.isEmpty()) {
                    signature.append("void");
                } else {
                    for (int i = 0; i < function.params.size(); i++) {
                        FunDef.Parameter param = function.params.get(i);
                        String paramType = mapTypeNodeToCType(param.type());
                        signature.append(paramType).append(" ").append(param.name());
                        if (i < function.params.size() - 1) {
                            signature.append(", ");
                        }
                    }
                }
                signature.append(");");

                // Write the signature
                writer.write(signature.toString());
                writer.write("\n");
            }

            writer.write("\n#endif // " + guardName + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Maps a TypeNode to a corresponding C type as a string.
     *
     * @param typeNode The TypeNode to map.
     * @return The corresponding C type as a string.
     */
    private static String mapTypeNodeToCType(TypeNode typeNode) {
        if (typeNode == null) {
            return "void"; // Default to void if no type is specified
        }

        return switch (typeNode.typeToken) {
            case "bool" -> "bool";
            case "void" -> "void";
            case "i64" -> "int64_t";
            case "f64" -> "double";
            case "string" -> "const char*";
            default -> "void*"; // Fallback for user-defined or unknown types
        };
    }
}
