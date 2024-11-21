# Created with the help of AI
# Function to load .env variables
function LoadEnvVariables {
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
