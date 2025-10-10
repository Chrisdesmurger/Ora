# Script PowerShell pour surveiller les logs Firestore en temps réel
# Usage: .\scripts\watch-firestore-logs.ps1

$adb = "C:\Users\chris\AppData\Local\Android\Sdk\platform-tools\adb.exe"

Write-Host "======================================" -ForegroundColor Cyan
Write-Host " SURVEILLANCE LOGS FIRESTORE ORA" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Filtres actifs:" -ForegroundColor Yellow
Write-Host "  - SyncManager" -ForegroundColor Gray
Write-Host "  - UserProfileRepository" -ForegroundColor Gray
Write-Host "  - UserStatsRepository" -ForegroundColor Gray
Write-Host "  - OraAuthViewModel" -ForegroundColor Gray
Write-Host "  - Firestore" -ForegroundColor Gray
Write-Host "  - PERMISSION" -ForegroundColor Gray
Write-Host ""
Write-Host "ACTION REQUISE:" -ForegroundColor Green
Write-Host "  1. Lancez l'app Ora sur votre device" -ForegroundColor White
Write-Host "  2. Créez un nouveau compte (email + password)" -ForegroundColor White
Write-Host "  3. Observez les logs ci-dessous" -ForegroundColor White
Write-Host ""
Write-Host "Appuyez sur CTRL+C pour arrêter" -ForegroundColor Red
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# Nettoyer les logs avant de commencer
& $adb logcat -c

# Surveiller les logs en continu
& $adb logcat | Select-String -Pattern "SyncManager|UserProfileRepository|UserStatsRepository|OraAuthViewModel|Firestore|PERMISSION"
