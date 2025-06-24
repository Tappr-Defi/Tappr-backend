# PowerShell script to fix incorrect import statements
$projectPath = "C:/Users/DELL USER/Desktop/Tappr-backend/src"

# Find all Java files with incorrect imports
$javaFiles = Get-ChildItem -Path $projectPath -Recurse -Filter "*.java" | ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    if ($content -match "org\.vomzersocials") {
        $_.FullName
    }
}

# Replace incorrect imports with correct ones
foreach ($file in $javaFiles) {
    Write-Host "Fixing imports in: $file"
    $content = Get-Content $file -Raw
    
    # Fix package imports
    $content = $content -replace "import org\.vomzersocials\.user\.data\.models\.", "import com.semicolon.africa.tapprbackend.user.data.models."
    $content = $content -replace "import org\.vomzersocials\.user\.data\.repositories\.", "import com.semicolon.africa.tapprbackend.user.data.repositories."
    $content = $content -replace "import org\.vomzersocials\.user\.dtos\.requests\.", "import com.semicolon.africa.tapprbackend.user.dtos.requests."
    $content = $content -replace "import org\.vomzersocials\.user\.dtos\.responses\.", "import com.semicolon.africa.tapprbackend.user.dtos.responses."
    $content = $content -replace "import org\.vomzersocials\.user\.enums\.", "import com.semicolon.africa.tapprbackend.user.enums."
    $content = $content -replace "import org\.vomzersocials\.user\.services\.interfaces\.", "import com.semicolon.africa.tapprbackend.user.services.interfaces."
    $content = $content -replace "import org\.vomzersocials\.user\.exceptions\.", "import com.semicolon.africa.tapprbackend.user.exceptions."
    $content = $content -replace "import org\.vomzersocials\.user\.springSecurity\.", "import com.semicolon.africa.tapprbackend.security."
    
    # Fix wildcard imports
    $content = $content -replace "import org\.vomzersocials\.user\.dtos\.requests\.\*;", "import com.semicolon.africa.tapprbackend.user.dtos.requests.*;"
    $content = $content -replace "import org\.vomzersocials\.user\.dtos\.responses\.\*;", "import com.semicolon.africa.tapprbackend.user.dtos.responses.*;"
    
    # Handle zkLogin and other external dependencies - comment them out for now
    $content = $content -replace "import org\.vomzersocials\.zkLogin\.", "// import org.vomzersocials.zkLogin."
    $content = $content -replace "import org\.vomzersocials\.user\.utils\.", "// import org.vomzersocials.user.utils."
    
    # Fix commented imports too
    $content = $content -replace "//import org\.vomzersocials\.user\.data\.models\.", "//import com.semicolon.africa.tapprbackend.user.data.models."
    $content = $content -replace "//import org\.vomzersocials\.user\.dtos\.requests\.", "//import com.semicolon.africa.tapprbackend.user.dtos.requests."
    $content = $content -replace "//import org\.vomzersocials\.user\.dtos\.responses\.", "//import com.semicolon.africa.tapprbackend.user.dtos.responses."
    $content = $content -replace "//import org\.vomzersocials\.user\.enums\.", "//import com.semicolon.africa.tapprbackend.user.enums."
    
    Set-Content -Path $file -Value $content -NoNewline
}

Write-Host "Import fixing completed!"