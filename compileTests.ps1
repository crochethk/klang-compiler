# import helper functions
. .\loadEnvVars.ps1

# Load environment variables from file
$envFilePath = ".\L1Compiler.env"
LoadEnvVariables -filePath $envFilePath

# Compiles all "*.l1" files of "./tests" into "gen_jbc"
$testsSourcesDir = $env:TESTS_DIR
Get-ChildItem -Path $testsSourcesDir -Include "*.l1", "*.L1" -Recurse | ForEach-Object {
    $relativePath = $_.FullName.Substring($pwd.Path.Length + 1)  # Strip out the base directory part
    java --enable-preview -cp "bin;lib/antlr4-runtime-4.13.2.jar;lib/antlr4-4.13.2.jar;lib/antlr4-4.13.2-complete.jar" L1Compiler $env:OUTDIR $relativePath
}