# import helper functions
. .\utils.ps1

# Load environment variables from file
$envFilePath = ".\L1Compiler.env"
LoadEnvVariables -filePath $envFilePath

# Compiles all "*.l1" files of "./tests" into "gen_jbc"
$testsSourcesDir = $env:TESTS_DIR
Get-ChildItem -Path $testsSourcesDir -Include "*.l1", "*.L1" -Recurse | ForEach-Object {
    CompileFile -outDir $env:OUTDIR -sourceFilePath $_.FullName
}
