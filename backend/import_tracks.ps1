# PowerShell script to automatically import songs into the Sickimfy project.
# Copies audio files to backend/media/ and registers them in the SQLite DB via the admin API.

[CmdletBinding()]
param (
    [Parameter(Mandatory = $false)]
    [string]$SourceDir = "",

    [Parameter(Mandatory = $false)]
    [string]$ServerUrl = "http://localhost:8080",

    [Parameter(Mandatory = $false)]
    [string]$ClientMediaHost = "http://10.0.2.2:8080",

    [Parameter(Mandatory = $false)]
    [string]$AdminKey = "development-admin-key-change-before-deployment"
)

Clear-Host
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "   Sickimfy Track Importer & Seeder" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan

# 1. Ask for the directory containing the songs
if ([string]::IsNullOrEmpty($SourceDir)) {
    Write-Host "Please enter the absolute path to the folder containing your MP3/audio files:" -ForegroundColor Yellow
    $SourceDir = Read-Host "Path"
}

# Clean path quotes if any
$SourceDir = $SourceDir.Trim('"', "'")

if (-not (Test-Path $SourceDir -PathType Container)) {
    Write-Error "Source directory not found: $SourceDir"
    Exit
}

# 2. Check if Server is running
Write-Host "Checking connection to Ktor server at $ServerUrl ..." -ForegroundColor Gray
try {
    $health = Invoke-RestMethod -Uri "$ServerUrl/api/health" -Method Get -TimeoutSec 5
    if ($health.status -ne "ok") {
        throw "Server health check failed."
    }
    Write-Host "Connected to server successfully!" -ForegroundColor Green
} catch {
    Write-Host "Could not connect to Ktor server at $ServerUrl." -ForegroundColor Red
    Write-Host "Please make sure your backend is running. You can run it using:" -ForegroundColor Yellow
    Write-Host "  cd backend" -ForegroundColor Gray
    Write-Host "  .\..\gradlew.bat run" -ForegroundColor Gray
    Exit
}

# 3. Create or login temp admin user to get JWT token
$registerBody = @{
    email = "temp_admin@sickimfy.local"
    password = "password123"
    displayName = "Temp Admin"
} | ConvertTo-Json

$token = ""
Write-Host "Obtaining JWT access token..." -ForegroundColor Gray
try {
    # Try register first
    $response = Invoke-RestMethod -Uri "$ServerUrl/api/auth/register" -Method Post -Body $registerBody -ContentType "application/json"
    $token = $response.token
    Write-Host "New admin user registered successfully." -ForegroundColor Green
} catch {
    # If already exists (409), try login
    try {
        $loginBody = @{
            email = "temp_admin@sickimfy.local"
            password = "password123"
        } | ConvertTo-Json
        $response = Invoke-RestMethod -Uri "$ServerUrl/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
        $token = $response.token
        Write-Host "LoggedIn successfully." -ForegroundColor Green
    } catch {
        Write-Error "Failed to authenticate with Ktor server: $_"
        Exit
    }
}

# 4. Target media directory in backend
$PSScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$TargetMediaDir = Join-Path $PSScriptRoot "media"
if (-not (Test-Path $TargetMediaDir)) {
    New-Item -ItemType Directory -Path $TargetMediaDir | Out-Null
}

# 5. Scan audio files
$Extensions = @("*.mp3", "*.m4a", "*.wav", "*.ogg")
$AudioFiles = Get-ChildItem -Path $SourceDir -Include $Extensions -Recurse

if ($AudioFiles.Count -eq 0) {
    Write-Host "No audio files found in $SourceDir." -ForegroundColor Red
    Exit
}

Write-Host "Found $($AudioFiles.Count) audio files to import." -ForegroundColor Green

# COM object for reading ID3 metadata tags (if available)
$shell = New-Object -ComObject Shell.Application

$importedCount = 0
foreach ($file in $AudioFiles) {
    Write-Host "---------------------------------------------" -ForegroundColor Gray
    Write-Host "Processing: $($file.Name)..." -ForegroundColor Yellow

    # Extract metadata using Shell namespace details
    $folder = $shell.NameSpace($file.DirectoryName)
    $folderItem = $folder.ParseName($file.Name)
    
    # Standard Index mapping for ID3 tags in Windows Shell:
    # 21 = Title, 20 = Authors (Artist), 14 = Album
    $title = $folder.GetDetailsOf($folderItem, 21)
    $artist = $folder.GetDetailsOf($folderItem, 20)
    $album = $folder.GetDetailsOf($folderItem, 14)
    
    # Fallback to defaults
    if ([string]::IsNullOrWhiteSpace($title)) { $title = [System.IO.Path]::GetFileNameWithoutExtension($file.Name) }
    if ([string]::IsNullOrWhiteSpace($artist)) { $artist = "Unknown Artist" }
    if ([string]::IsNullOrWhiteSpace($album)) { $album = "Single" }
    
    # Copy file to backend/media
    $destPath = Join-Path $TargetMediaDir $file.Name
    if (-not (Test-Path $destPath)) {
        Write-Host "Copying to backend media directory..." -ForegroundColor Gray
        Copy-Item -Path $file.FullName -Destination $destPath
    } else {
        Write-Host "File already exists in media directory, skipping copy." -ForegroundColor Gray
    }

    # Prepare URLs
    # Replace spaces in filename with %20 for URLs
    $escapedFileName = [Uri]::EscapeDataString($file.Name)
    $audioUrl = "$ClientMediaHost/media/$escapedFileName"
    
    # Use the generated default cover
    $coverImageUrl = "$ClientMediaHost/media/default_cover.png"

    # Call Admin API to register track
    $trackData = @{
        title = $title
        artistName = $artist
        albumName = $album
        genre = "Pop"
        coverImageUrl = $coverImageUrl
        audioUrl = $audioUrl
        durationSeconds = 180
        isFeatured = ($importedCount -lt 5) # Set first 5 songs as featured for Carousel
    } | ConvertTo-Json -Compress

    $headers = @{
        Authorization = "Bearer $token"
        "X-Admin-Key" = $AdminKey
    }

    try {
        $trackResponse = Invoke-RestMethod -Uri "$ServerUrl/api/admin/tracks" -Method Post -Body $trackData -ContentType "application/json" -Headers $headers
        Write-Host "Successfully registered in DB: '$title' by '$artist'" -ForegroundColor Green
        $importedCount++
    } catch {
        Write-Host "Failed to register track: $_" -ForegroundColor Red
    }
}

Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "Import completed! Successfully imported $importedCount tracks." -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Cyan
