# Function to load .env variables
function LoadEnvVariables {
    # Created with the help of AI
    param (
        [string]$filePath
    )

    if (Test-Path $filePath) {
        $content = Get-Content -Path $filePath

        foreach ($line in $content) {
            # Skip empty lines or comment lines (start with # or ;)
            if ($line -match '^\s*(#|;|\s*$)') {
                continue
            }

            # Split the line into key-value pairs by the first '=' found
            if ($line -match '^\s*(\S+)\s*=\s*(.*)\s*$') {
                $key = $matches[1]
                $value = $matches[2]

                # Remove any potential escape characters (e.g. \n, \t)
                $value = $value -replace '\\n', "`n" -replace '\\t', "`t"

                # Set the environment variable (or just a variable if you prefer)
                [System.Environment]::SetEnvironmentVariable($key, $value, [System.EnvironmentVariableTarget]::Process)

                #Write-Host "Loaded: $key=$value"
            }
        }
    }
    else {
        Write-Host "Error: The file '$filePath' does not exist."
    }
}

# Compiles the given Klang source file
function CompileFile {
    param (
        [string]$outDir,
        [string]$sourceFilePath
    )
    $sourceFilePath = Resolve-Path -Path $sourceFilePath
    $relativePath = $sourceFilePath.Substring($pwd.Path.Length + 1)  # Strip out the base directory part
    java --enable-preview -cp "build/dev;lib/antlr4-runtime-4.13.2.jar;lib/antlr4-4.13.2.jar;lib/antlr4-4.13.2-complete.jar"`
        cc.crochethk.compilerbau.praktikum.KlangCompiler $outDir $relativePath
}