$ErrorActionPreference = 'Stop'
$compilador = if ($env:JAVA_HOME) {
    Join-Path $env:JAVA_HOME 'bin\javac.exe'
} else {
    (Get-Command javac -ErrorAction Stop).Source
}
$saida = Join-Path $PSScriptRoot 'bin'
$fontes = Get-ChildItem (Join-Path $PSScriptRoot 'src') -Recurse -Filter '*.java' |
    ForEach-Object { $_.FullName }
New-Item -ItemType Directory -Force -Path $saida | Out-Null
& $compilador --release 8 -encoding UTF-8 -Xlint:all,-options -d $saida $fontes
if ($LASTEXITCODE -ne 0) { throw 'Falha na compilacao. Use um JDK 11 ou superior.' }
Write-Host 'Compilacao concluida. Execute: java -ea -cp bin testes.TesteGrafo'
