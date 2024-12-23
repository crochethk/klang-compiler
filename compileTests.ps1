# import helper functions
. .\utils.ps1

# Load environment variables from file
$envFilePath = ".\klangc.env"
LoadEnvVariables -filePath $envFilePath

# Compiles all "*.k" files in "./tests" into "$OUTDIR"
$testsSourcesDir = ./tests
Get-ChildItem -Path $testsSourcesDir -Include "*.k" -Recurse | ForEach-Object {
    Write-Host "+-----------------------"
    Write-Host "+ Source: '$_'"
    Write-Host "+-----------------------"
    CompileFile -outDir $env:OUTDIR -sourceFilePath $_.FullName
    Write-Host ""
}
