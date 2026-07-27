# Open Hospital launcher - hidden console mode
# Launches oh.ps1 in a hidden PowerShell window and redirects all output to startup.log
param(
    [string]$mode = "PORTABLE",
    [string]$lang = "en"
)

$logFile = Join-Path $PSScriptRoot "startup.log"
$scriptPath = Join-Path $PSScriptRoot "oh.ps1"

& $scriptPath -interactive off -mode $mode -lang $lang *> $logFile
