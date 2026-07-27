# 結合テスト一括実行: シナリオ毎にアプリを初期化(再起動)して打鍵する
$ErrorActionPreference = 'Stop'
$node = "C:\Users\hatsu\AppData\Roaming\fnm\node-versions\v22.14.0\installation\node.exe"
$app  = "C:\work\Biz\workspace\ai-training\training-bookshelf"
$exec = "C:\work\Biz\workspace\ai-training\tools\ita-executor"
$scenarios = @('SC01','SC02','SC03','SC04','SC05','SC06')

function Stop-App {
  $c = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
  if ($c) { $c.OwningProcess | Select-Object -Unique | ForEach-Object { Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue } }
  Start-Sleep -Seconds 2
}
function Start-App {
  Push-Location $app
  Start-Process -FilePath "$app\mvnw.cmd" -ArgumentList 'spring-boot:run' -WindowStyle Hidden -RedirectStandardOutput "$exec\_app.out" -RedirectStandardError "$exec\_app.err"
  Pop-Location
}
function Wait-App {
  for ($i=0; $i -lt 90; $i++) {
    try { $r = (Invoke-WebRequest 'http://localhost:8080/book/list' -UseBasicParsing -TimeoutSec 3).StatusCode; if ($r -eq 200) { return $true } } catch {}
    Start-Sleep -Seconds 2
  }
  return $false
}

foreach ($sc in $scenarios) {
  Write-Output "===== $sc : restart app ====="
  Stop-App
  Start-App
  if (-not (Wait-App)) { Write-Output "$sc : APP DID NOT START"; continue }
  Write-Output "===== $sc : run ====="
  & $node "$exec\ita-run.cjs" $sc
}
Stop-App
Write-Output "===== ALL DONE ====="
