# OrganLink Database Population Script
# This script populates the database with initial sample data for testing

$API_BASE = "http://localhost:8081/api/v1"

# Function to make REST API calls
function Invoke-OrganLinkAPI {
    param(
        [string]$Method = "GET",
        [string]$Endpoint,
        [object]$Body = $null,
        [hashtable]$Headers = @{}
    )
    
    $uri = "$API_BASE$Endpoint"
    $defaultHeaders = @{
        "Content-Type" = "application/json"
        "Accept" = "application/json"
    }
    
    $allHeaders = $defaultHeaders + $Headers
    
    try {
        if ($Body) {
            $jsonBody = $Body | ConvertTo-Json -Depth 10
            Write-Host "📡 $Method $uri" -ForegroundColor Cyan
            Write-Host "📦 Body: $jsonBody" -ForegroundColor Gray
            $response = Invoke-RestMethod -Uri $uri -Method $Method -Headers $allHeaders -Body $jsonBody
        } else {
            Write-Host "📡 $Method $uri" -ForegroundColor Cyan
            $response = Invoke-RestMethod -Uri $uri -Method $Method -Headers $allHeaders
        }
        
        Write-Host "✅ Success: $($response.message)" -ForegroundColor Green
        return $response
    } catch {
        Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "📄 Response: $responseBody" -ForegroundColor Yellow
        }
        return $null
    }
}

# Function to wait for backend to be ready
function Wait-ForBackend {
    Write-Host "⏳ Waiting for backend to start..." -ForegroundColor Yellow
    $maxAttempts = 30
    $attempt = 0
    
    while ($attempt -lt $maxAttempts) {
        try {
            $response = Invoke-RestMethod -Uri "$API_BASE/locations/countries" -Method GET -TimeoutSec 5
            Write-Host "✅ Backend is ready!" -ForegroundColor Green
            return $true
        } catch {
            $attempt++
            Write-Host "⏳ Attempt $attempt/$maxAttempts - Backend not ready yet..." -ForegroundColor Yellow
            Start-Sleep -Seconds 2
        }
    }
    
    Write-Host "❌ Backend failed to start within expected time" -ForegroundColor Red
    return $false
}

Write-Host "🏥 OrganLink Database Population Script" -ForegroundColor Magenta
Write-Host "=======================================" -ForegroundColor Magenta

# Wait for backend to be ready
if (-not (Wait-ForBackend)) {
    exit 1
}

# Step 1: Login as admin (try default credentials)
Write-Host "`n🔐 Step 1: Admin Login" -ForegroundColor Yellow
$adminCredentials = @{
    username = "admin"
    password = "admin123"
}

$adminLoginResponse = Invoke-OrganLinkAPI -Method "POST" -Endpoint "/admin/login" -Body $adminCredentials

if (-not $adminLoginResponse) {
    Write-Host "❌ Admin login failed. Cannot proceed with data population." -ForegroundColor Red
    exit 1
}

$adminToken = $adminLoginResponse.data.token
$adminHeaders = @{
    "Authorization" = "Bearer $adminToken"
}

Write-Host "✅ Admin logged in successfully" -ForegroundColor Green

# Step 2: Create sample hospitals
Write-Host "`n🏥 Step 2: Creating Sample Hospitals" -ForegroundColor Yellow

$hospitals = @(
    @{
        hospitalId = "H001"
        hospitalName = "Mumbai Central Hospital"
        address = "123 Medical Street, Fort"
        city = "Mumbai"
        state = "Maharashtra"
        country = "India"
        pincode = "400001"
        phoneNumber = "+91-22-1234-5678"
        emailAddress = "admin@mumbaicentral.hospital"
        websiteUrl = "https://mumbaicentral.hospital"
        hospitalType = "GENERAL"
        bedCapacity = 500
        doctorCount = 120
        specializations = @("Cardiology", "Nephrology", "Hepatology")
        operatingHours = "24x7"
        emergencyServices = $true
        traumaCenter = $true
        organTransplantCenter = $true
    },
    @{
        hospitalId = "H002"
        hospitalName = "Delhi Heart Institute"
        address = "456 Health Avenue, CP"
        city = "New Delhi"
        state = "Delhi"
        country = "India"
        pincode = "110001"
        phoneNumber = "+91-11-2345-6789"
        emailAddress = "contact@delhiheart.org"
        websiteUrl = "https://delhiheart.org"
        hospitalType = "SPECIALTY"
        bedCapacity = 300
        doctorCount = 80
        specializations = @("Cardiology", "Cardiac Surgery")
        operatingHours = "24x7"
        emergencyServices = $true
        traumaCenter = $false
        organTransplantCenter = $true
    },
    @{
        hospitalId = "H003"
        hospitalName = "Bangalore Kidney Center"
        address = "789 Science Park Road"
        city = "Bangalore"
        state = "Karnataka"
        country = "India"
        pincode = "560001"
        phoneNumber = "+91-80-3456-7890"
        emailAddress = "info@bangalorekidney.in"
        websiteUrl = "https://bangalorekidney.in"
        hospitalType = "SPECIALTY"
        bedCapacity = 200
        doctorCount = 60
        specializations = @("Nephrology", "Urology")
        operatingHours = "6 AM - 10 PM"
        emergencyServices = $true
        traumaCenter = $false
        organTransplantCenter = $true
    }
)

foreach ($hospital in $hospitals) {
    Write-Host "Creating hospital: $($hospital.hospitalName)..." -ForegroundColor Cyan
    $createResponse = Invoke-OrganLinkAPI -Method "POST" -Endpoint "/admin/hospitals" -Body $hospital -Headers $adminHeaders
    
    if ($createResponse) {
        Write-Host "✅ Created: $($hospital.hospitalName)" -ForegroundColor Green
    } else {
        Write-Host "❌ Failed to create: $($hospital.hospitalName)" -ForegroundColor Red
    }
}

# Step 3: Create sample organizations
Write-Host "`n🏛️ Step 3: Creating Sample Organizations" -ForegroundColor Yellow

$organizations = @(
    @{
        organizationId = "ORG001"
        organizationName = "National Organ Donation Council"
        organizationType = "GOVERNMENT"
        address = "Health Ministry Building, New Delhi"
        city = "New Delhi"
        state = "Delhi"
        country = "India"
        pincode = "110011"
        contactEmail = "council@nodc.gov.in"
        contactPhone = "+91-11-1111-1111"
        websiteUrl = "https://nodc.gov.in"
        establishedDate = "2010-01-15T00:00:00"
        description = "National regulatory body for organ donation policies"
        isActive = $true
    },
    @{
        organizationId = "ORG002"
        organizationName = "Indian Medical Association"
        organizationType = "PROFESSIONAL"
        address = "IMA House, Medical District"
        city = "Mumbai"
        state = "Maharashtra"
        country = "India"
        pincode = "400020"
        contactEmail = "info@ima.org.in"
        contactPhone = "+91-22-2222-2222"
        websiteUrl = "https://ima.org.in"
        establishedDate = "1950-01-01T00:00:00"
        description = "Professional medical association representing doctors"
        isActive = $true
    }
)

foreach ($organization in $organizations) {
    Write-Host "Creating organization: $($organization.organizationName)..." -ForegroundColor Cyan
    $createResponse = Invoke-OrganLinkAPI -Method "POST" -Endpoint "/admin/organizations" -Body $organization -Headers $adminHeaders
    
    if ($createResponse) {
        Write-Host "✅ Created: $($organization.organizationName)" -ForegroundColor Green
    } else {
        Write-Host "❌ Failed to create: $($organization.organizationName)" -ForegroundColor Red
    }
}

# Step 4: Verify data creation
Write-Host "`n🔍 Step 4: Verifying Data Creation" -ForegroundColor Yellow

Write-Host "Checking countries..." -ForegroundColor Cyan
$countriesResponse = Invoke-OrganLinkAPI -Method "GET" -Endpoint "/locations/countries"

if ($countriesResponse -and $countriesResponse.data.Count -gt 0) {
    Write-Host "✅ Found $($countriesResponse.data.Count) countries" -ForegroundColor Green
    $countriesResponse.data | ForEach-Object { Write-Host "  - $($_.name)" -ForegroundColor Gray }
} else {
    Write-Host "❌ No countries found" -ForegroundColor Red
}

Write-Host "Checking hospitals..." -ForegroundColor Cyan
$hospitalsResponse = Invoke-OrganLinkAPI -Method "GET" -Endpoint "/admin/hospitals" -Headers $adminHeaders

if ($hospitalsResponse -and $hospitalsResponse.data.content.Count -gt 0) {
    Write-Host "✅ Found $($hospitalsResponse.data.content.Count) hospitals" -ForegroundColor Green
    $hospitalsResponse.data.content | ForEach-Object { Write-Host "  - $($_.hospitalName) ($($_.hospitalId))" -ForegroundColor Gray }
} else {
    Write-Host "❌ No hospitals found" -ForegroundColor Red
}

Write-Host "`n🎉 Database population completed!" -ForegroundColor Green
Write-Host "You can now test the hospital login flow at: http://localhost:3001/hospital/login" -ForegroundColor Cyan
Write-Host "Sample credentials:" -ForegroundColor Yellow
Write-Host "  - Country: India" -ForegroundColor Gray
Write-Host "  - State: Maharashtra" -ForegroundColor Gray
Write-Host "  - City: Mumbai" -ForegroundColor Gray
Write-Host "  - Hospital: Mumbai Central Hospital (H001)" -ForegroundColor Gray
Write-Host "  - User ID: hosp001" -ForegroundColor Gray
Write-Host "  - Password: hospital123" -ForegroundColor Gray
