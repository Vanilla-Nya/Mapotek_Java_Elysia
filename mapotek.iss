[Setup]
AppName=Mapotek
AppVersion=1.0
DefaultDirName={pf}\Mapotek
DefaultGroupName=Mapotek
OutputBaseFilename=MapotekInstaller
Compression=lzma
SolidCompression=yes

[Files]
; Add the JAR file
Source: "C:\Users\asuna\Documents\Projects\Mapotek\target\Mapotek-1.0-SNAPSHOT-jar-with-dependencies.jar"; DestDir: "{app}"; Flags: ignoreversion
; Add JRE folder (if bundling JRE with your app)
Source: "C:\Program Files\OpenJDK\jdk-23.0.1\*"; DestDir: "{app}\jre"; Flags: recursesubdirs

[Run]
; Run the application using the bundled JRE (if you included one)
Filename: "{app}\jre\bin\java.exe"; Parameters: "-jar {app}\myapp.jar"; WorkingDir: "{app}"; Flags: runhidden
