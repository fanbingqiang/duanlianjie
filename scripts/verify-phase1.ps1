# ============================================================
# 阶段1 接口验收脚本（需要应用已启动在 8080 端口）
# 兼容 Windows PowerShell 5.1 / PowerShell 7
# 用法：powershell -NoProfile -ExecutionPolicy Bypass -File scripts/verify-phase1.ps1
# 可选：-BaseUrl http://localhost:8080
# ============================================================
param(
    [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"
$script:results = @()

function Add-Result {
    param([string]$Name, [bool]$Passed, [string]$Detail)
    $script:results += [pscustomobject]@{ Case = $Name; Result = $(if ($Passed) { "PASS" } else { "FAIL" }); Detail = $Detail }
    $color = if ($Passed) { "Green" } else { "Red" }
    $label = if ($Passed) { "PASS" } else { "FAIL" }
    Write-Host ("[{0}] {1} - {2}" -f $label, $Name, $Detail) -ForegroundColor $color
}

function Invoke-ApiJson {
    param([string]$Method, [string]$Path, [string]$Body)
    $params = @{
        Uri             = "$BaseUrl$Path"
        Method          = $Method
        UseBasicParsing = $true
    }
    if ($Body) {
        $params.Body = $Body
        $params.ContentType = "application/json"
    }
    $resp = Invoke-WebRequest @params
    return ($resp.Content | ConvertFrom-Json)
}

function Invoke-RawRequest {
    param([string]$Path)
    $url = "$BaseUrl$Path"

    # 使用 .NET HttpWebRequest 并关闭自动重定向：兼容 PowerShell 5.1，
    # 可以准确拿到 302 的 Location 和 404 的状态码。
    $request = [System.Net.HttpWebRequest]::Create($url)
    $request.AllowAutoRedirect = $false
    $request.UserAgent = "shortlink-phase1-verify"

    $response = $null
    try {
        $response = $request.GetResponse()
    } catch [System.Net.WebException] {
        $response = $_.Exception.Response
    }

    if ($null -eq $response) {
        throw "请求失败：$url"
    }

    $status = [int]$response.StatusCode
    $location = $null
    try { $location = $response.Headers["Location"] } catch { }

    $content = $null
    try {
        $stream = $response.GetResponseStream()
        if ($null -ne $stream) {
            $reader = New-Object System.IO.StreamReader($stream)
            $content = $reader.ReadToEnd()
            $reader.Close()
        }
    } catch { }

    $response.Close()

    return [pscustomobject]@{
        Status   = $status
        Location = $location
        Content  = $content
    }
}

Write-Host "检查应用是否已启动：$BaseUrl"
$up = $false
for ($i = 0; $i -lt 30; $i++) {
    try {
        $resp = Invoke-WebRequest -Uri "$BaseUrl/" -UseBasicParsing -TimeoutSec 3
        if ([int]$resp.StatusCode -eq 200) { $up = $true; break }
    } catch {
        Start-Sleep -Seconds 1
    }
}
if (-not $up) {
    Write-Host "应用未启动或端口不可达，请先运行：mvn spring-boot:run" -ForegroundColor Red
    exit 1
}
Add-Result "首页可访问" $true "GET / 返回 200"

$longUrl = "https://www.baidu.com/s?wd=" + [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()

# 用例1：创建短链接
$body = @{ longUrl = $longUrl } | ConvertTo-Json -Compress
$json = Invoke-ApiJson -Method POST -Path "/api/shortlink/create" -Body $body
$shortCode = $json.data.shortCode
$created = ($json.code -eq 200 -and $shortCode -match '^[0-9a-zA-Z]{1,10}$')
Add-Result "创建短链接" $created "code=$($json.code), shortCode=$shortCode, shortUrl=$($json.data.shortUrl)"

# 用例2：同一长链接二次创建应幂等
$json2 = Invoke-ApiJson -Method POST -Path "/api/shortlink/create" -Body $body
$sameCode = ($json2.code -eq 200 -and $json2.data.shortCode -eq $shortCode)
Add-Result "幂等返回同一短码" $sameCode "第一次=$shortCode, 第二次=$($json2.data.shortCode)"

# 用例3：查询详情
$json3 = Invoke-ApiJson -Method GET -Path "/api/shortlink/$shortCode"
$detailOk = ($json3.code -eq 200 -and $json3.data.longUrl -eq $longUrl -and $json3.data.shortCode -eq $shortCode)
Add-Result "查询详情" $detailOk "longUrl 一致=$($json3.data.longUrl -eq $longUrl), visitCount=$($json3.data.visitCount)"

# 用例4：302 跳转
$redirectResp = Invoke-RawRequest -Path "/$shortCode"
$redirectOk = ($redirectResp.Status -eq 302 -and $redirectResp.Location -eq $longUrl)
Add-Result "短码 302 跳转" $redirectOk "HTTP=$($redirectResp.Status), Location=$($redirectResp.Location)"

# 用例5：不存在的短码返回 404
$notFound = Invoke-RawRequest -Path "/zzzzNotExist9"
Add-Result "不存在短码返回 404" ($notFound.Status -eq 404) "HTTP=$($notFound.Status)"

# 用例6：非法长链接返回 50002
$badBody = @{ longUrl = "ftp://example.com" } | ConvertTo-Json -Compress
$json6 = Invoke-ApiJson -Method POST -Path "/api/shortlink/create" -Body $badBody
Add-Result "非法长链接返回 50002" ($json6.code -eq 50002) "code=$($json6.code), msg=$($json6.msg)"

# 用例7：空长链接返回 50001
$emptyBody = @{ longUrl = "" } | ConvertTo-Json -Compress
$json7 = Invoke-ApiJson -Method POST -Path "/api/shortlink/create" -Body $emptyBody
Add-Result "空长链接返回 50001" ($json7.code -eq 50001) "code=$($json7.code), msg=$($json7.msg)"

# 用例8：详情接口查询不存在的短码返回 50003
$json8 = Invoke-ApiJson -Method GET -Path "/api/shortlink/zzzzNotExist9"
Add-Result "详情不存在返回 50003" ($json8.code -eq 50003) "code=$($json8.code), msg=$($json8.msg)"

Write-Host ""
Write-Host "验收汇总："
$script:results | Format-Table -AutoSize

$failed = @($script:results | Where-Object { $_.Result -eq "FAIL" })
if ($failed.Count -gt 0) {
    Write-Host "存在失败用例：$($failed.Count) 个" -ForegroundColor Red
    exit 1
}
Write-Host "全部 $($script:results.Count) 个用例通过" -ForegroundColor Green
exit 0
