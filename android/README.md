# LOCUTERRA Android-Pilot-App (`org.umbruch.locuterra.pilot`)

Diese Android-App setzt den LOCUTERRA-Pilotauftrag (`T-20261025-180241913` / BACH `#2622`) auf Basis des bestehenden Next.js/React/TypeScript-Webkerns (`demo/`) und der synthetischen Seed-Daten der fiktiven Kommune **„Grüntal“** um.

## 1. Architektur- & Projektentscheidung

Gemäß `DECISIONS.md` (Entscheidungen vom 2026-05-16, 2026-05-26 und 2026-05-30) sowie `ARCHITECTURE.md` und `PORTIERUNGSPLAN.md` gilt für LOCUTERRA:

1. **Gemeinsamer Web/PWA-Kern statt paralleler Codebasen:** Die fachliche Domänenlogik (Bürgerkonto, Orte, Gruppen, Reichweiten-Hierarchie, nicht-kommerzielle Ressourcen, Informationskanäle, Direktnachrichten) wird nicht in eine separate native Zweit-Codebasis dupliziert, sondern nutzt den statischen Next.js-Export (`demo/out`) direkt weiter.
2. **100 % Offline-Auslieferung aus lokalen APK-Assets:** Über `LocuterraAssetResolver` werden sämtliche 11 MVP-Screens (`SCREENMAP.md`) unter der virtuellen Origin `https://appassets.androidplatform.net/locuterra/` direkt aus `app/src/main/assets/locuterra/` geladen.
3. **Architektonischer Datenschutz (Zero-Network-Fence):** Das `AndroidManifest.xml` verzichtet bewusst auf `android.permission.INTERNET`. Zusätzlich blockiert `shouldInterceptRequest` im `WebViewClient` alle Nicht-Asset-Ursprünge fail-closed (`403 Forbidden`). Es gibt keinen Server, keine Datenbank und keine Übertragung echter Bürgerdaten.
4. **Zusammenhängender interaktiver Pilot-Ablauf (`PilotSessionStore`):** Während der statische Web-Export im Browser Schreibbuttons als `(Demo)` deaktiviert lässt, schaltet die Android-Pilot-App über eine native Kopf-/Fußleiste sowie eine Laufzeit-Bridge (`LocuterraAndroidPilot`) einen kompletten ephemeren Pilot-Durchlauf im lokalen Arbeitsspeicher frei:
   - **Informationskanal-Flow (`INFORMATIONSKANAL_FLOW.md`):** Strikte Trennung der drei Schalter (1) Kanal-Abonnement, (2) optionaler Begleitchat und (3) separate Kontaktfreigabe. Ein Kanal-Abo aktiviert niemals automatisch die Kontaktfreigabe.
   - **Nicht-kommerzieller Ressourcen-Flow (`RESSOURCEN_FLOW.md` & `RESSOURCEN_UND_MARKTPLATZ.md`):** Lokale Nachbarschaftsanfragen (z. B. Bohrmaschine, Lernhilfe) mit Pflichtbestätigung der Nicht-Kommerzialität (keine Zahlungen, keine Gebühren, kein Marktplatz-Checkout).
   - **Gruppen-Beitrittsregeln (`ROLES_AND_RIGHTS.md`):** Unterscheidung zwischen offenen Gruppen (`offen` -> direkter Beitritt), genehmigungspflichtigen Gruppen (`genehmigung` -> Steward-Anfrage ausstehend) und Einladungsgruppen (`einladung` -> gesperrt ohne Einladung).
   - **Moderation & Einspruch (`GOVERNANCE.md` & `BACKOFFICE_EINSPRUCHS_FLOW.md`):** Lokales Vormerken einer Meldung samt Zweitprüfungs-Einspruch.
   - **Ein-Klick-Reset:** Jede ephemere Änderung im Pilot-RAM lässt sich jederzeit über `Pilot-Ablauf -> Pilot-Zustand auf Grüntal-Seed zurücksetzen` auf den Ausgangszustand zurücksetzen.

---

## 2. Reproduzierbarer Build (Windows / macOS / Linux)

Voraussetzungen:
- **JDK 17** (`JAVA_HOME` auf Temurin/OpenJDK 17)
- **Android SDK** (`compileSdk = 36`, `minSdk = 26`, `targetSdk = 36`) in `local.properties` (`sdk.dir=...`) oder via `ANDROID_HOME`

### Schritt 1: Web-Kern aktualisieren (optional, falls `demo/` geändert wurde)
```bash
cd demo
npm ci
npm run build
```
Der Gradle-Task `syncWebAssets` kopiert `../demo/out` vor jedem Android-Build automatisch nach `android/app/src/main/assets/locuterra/`.

### Schritt 2: Unit-Tests & Debug-APK bauen
```powershell
cd android
.\gradlew.bat testDebugUnitTest assembleDebug
```

Erzeugtes APK-Artefakt:
- Pfad: `android/app/build/outputs/apk/debug/app-debug.apk`
- Application ID (Debug): `org.umbruch.locuterra.pilot.debug`
- Version: `0.1.0-pilot-debug` (`versionCode = 1`)

---

## 3. Installation & Pilot-Smoke-Test (Emulator / Gerät)

### Installation per ADB
```powershell
C:\dev\Android\Sdk\platform-tools\adb.exe install -r app\build\outputs\apk\debug\app-debug.apk
```

### Starten der Pilot-App
```powershell
C:\dev\Android\Sdk\platform-tools\adb.exe shell am start -n org.umbruch.locuterra.pilot.debug/org.umbruch.locuterra.pilot.MainActivity
```

### Abnahme-Checkliste (Pilot-Smoke-Test)
1. **Ehrlicher Pilot-Header sichtbar:** Am oberen Bildschirmrand erscheint dauerhaft das smaragdgrüne Banner `PILOT-MODUS · Synthetische Kommune Grüntal (Offline, kein Echtbetrieb)` samt Statuszähler (`Konto: Alex Rivera (Pseudonym) · Kanäle · Anfragen · Nachrichten · Meldungen`).
2. **Offline-Routennavigation (Screenmap):** Über die untere Schnellnavigation (`🏠 Feed`, `📍 Orte`, `👥 Gruppen`, `🤝 Ressourcen`, `📢 Kanäle`, `💬 Nachrichten`, `👤 Profil`) laden alle 11 Routen und Detailseiten verzögerungsfrei aus den lokalen APK-Assets.
3. **Kanal-Consent-Trennung prüfen:** Im Kanal `Gemeinde Grüntal — Offiziell` oder über `Pilot-Ablauf -> 1. Informationskanal-Flow` Kanal abonnieren -> prüfen, dass `Kontaktfreigabe` separat deaktiviert bleibt, bis sie explizit angehakt wird.
4. **Nicht-kommerzielle Ressourcen-Anfrage prüfen:** In `Ressourcen -> Bohrmaschine + Bit-Set` auf `Nicht-kommerzielle Anfrage testen (Pilot lokal)` tippen -> Anfrage absenden -> Zähler `Anfragen` und `Nachrichten` im Header steigen um `+1`.
5. **Ephemere Direktnachricht prüfen:** Unter `💬 Nachrichten` im freigeschalteten Pilot-Eingabefeld eine Testnachricht eingeben und auf `Lokal senden (Pilot)` tippen -> Nachricht erscheint sofort mit Badge `Lokal (Pilot) · Ephemerer Client-State` im Chatverlauf.
6. **Seed-Reset prüfen:** Über `Pilot-Ablauf -> 6. Pilot-Zustand auf Grüntal-Seed zurücksetzen` alle Zähler auf den Ausgangszustand (`Kanäle: 1 · Anfragen: 0 · Nachrichten: 0 · Meldungen: 0`) zurücksetzen.
