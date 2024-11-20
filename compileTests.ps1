# Compiles all "*.l1" files of "./tests" into "gen/L1CompilerJBC"
Get-ChildItem -Path "tests" -Include "*.l1", "*.L1" -Recurse | ForEach-Object {
    $relativePath = $_.FullName.Substring($pwd.Path.Length + 1)  # Strip out the base directory part
    java --enable-preview -cp "bin;lib/antlr4-runtime-4.13.2.jar;lib/antlr4-4.13.2.jar;lib/opentest4j-1.3.0.jar" L1Compiler gen/L1CompilerJBC $relativePath
}