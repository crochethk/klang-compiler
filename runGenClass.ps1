<#
 # Compile and execute the source file specified by "SOURCEFILE" in the .env file.
 #>

# import helper functions
. .\utils.ps1

# Load environment variables from file
$envFilePath = ".\L1Compiler.env"
LoadEnvVariables -filePath $envFilePath

# Construct fully qualified classname
$sourcefile = $env:SOURCEFILE
$outDir = $env:OUTDIR
$fileDir = [System.IO.Path]::GetDirectoryName($sourcefile)
$fileNameWithoutExtension = [System.IO.Path]::GetFileNameWithoutExtension($sourcefile)

$packageName = ($fileDir -replace '[\\/]', '.')
# only add "." inbetween package- and classname if packagename was not empty
if ($packageName) {
    $packageName += "."
}
$fullClassName = $packageName + $fileNameWithoutExtension

Write-Host "Compiling..."
CompileFile -outDir $outDir -sourceFilePath $sourcefile

Write-Host "Executing: '$fullClassName'"

java -cp $outDir $fullClassName

