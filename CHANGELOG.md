# Changelog

Alle nennenswerten Änderungen an LOCUTERRA werden hier dokumentiert.

## [Unreleased]

- **Android-Asset-Packaging-Fix (`v0.2.1-pilot`, `versionCode 4`, `T-20261124-206377961`):**
  - `android/app/build.gradle.kts` überschreibt nun `androidResources.ignoreAssetsPattern` ohne `<dir>_*`, sodass Next.js-Assets unter `assets/locuterra/_next/` (CSS- und JS-Chunks inkl. Civic-Editorial-Stylesheet) vollständig in die APK gepackt werden.
  - Automatische Gradle-Verifikationstasks (`verifyDebugApkWebAssets` / `verifyReleaseApkWebAssets`) prüfen nach jedem APK-Build direkt an der erzeugten ZIP/APK-Datei, dass alle von `index.html` referenzierten `/locuterra/_next/`-CSS- und JS-Chunks real enthalten sind.
  - Zusätzliche JUnit-Tests in `PilotUnitTest` prüfen CSS-MIME-Resolution, Vollständigkeit aller `_next`-Referenzen aus `index.html` sowie die Gradle-Konfiguration gegen `<dir>_*`-Regressionen.
- **Redesign nach `/software-in-worten` (`v0.2.0-pilot`, `versionCode 3`):**
  - Neuer UI-Blueprint unter `docs/UI-BLUEPRINT-SOFTWARE-IN-WORTEN.md` mit warmer Civic-Editorial-Farbpalette (`#F4F6F1`, `#0F5132`, `#E2F2E8`).
  - Native Android-Shell (`MainActivity.kt`) komplett überarbeitet: eine ruhige weiße Top App Bar, eine 5-Tab-Material-3-Bottom-Navigation mit Salbei-Aktiv-Pille und Vektor-Icons (`ic_nav_*.xml`), echte Edge-to-Edge-`WindowInsetsCompat.Type.systemBars()`, automatische Routen-zu-Tab-Synchronisierung und laienverständliche Dialoge.
  - Web-Doppelnavigation & Doppel-Header per `html.android-pilot`-Regel innerhalb der Android-App sauber ausgeblendet (`59/59` Demo-Tests grün, Android-15-Emulator-Smoke-Test mit 0 Crashes).
- Web-/PWA-Demonstrator (`demo/`): UX-/Accessibility-Verbesserungen und Vertragstests umgesetzt:
  - Kanäle-Übersicht (`src/app/kanaele/page.tsx`) verlinkt jeden Informationskanal direkt auf die Detailseite `/kanaele/[id]`.
  - Skip-Link (`#main-content`) und sichtbare `:focus-visible`-Fokusstile in `layout.tsx` und `globals.css` ergänzt; `<nav aria-label="Hauptnavigation">`, `aria-current="page"` und `aria-hidden="true"` auf dekorativen Emojis verankert.
  - Überschriftenhierarchie über `Card` (`headingLevel = "h2"`, Detail-Unterlisten `"h3"`) bereinigt und kontrastschwaches `text-gray-400`/`text-gray-500` bei lesbaren Metadaten auf `text-gray-600`/`text-gray-700` angehoben.
  - Neue Vertragstest-Suite `demo/tests/ux_and_contracts.test.mjs` (12 Tests: Verlinkung, Landmarks, Fokus, Kontrast, Android-Bridge-Kompatibilität, Grüntal-Referenzintegrität und Nicht-Kommerzialitäts-Invariante; insgesamt 59/59 Demo-Tests grün) sowie synchronisierte Web-Assets für `android/`.
- Installierbare Android-Pilot-App unter `android/` (`org.umbruch.locuterra.pilot`,
  `minSdk 26`, `targetSdk 36`, Kotlin/Gradle) auf Basis des bestehenden
  Grüntal-Demonstrators (`demo/out`) mit lokalem `LocuterraAssetResolver`,
  ephemerem `PilotSessionStore` (getrennter Kanal-Consent, Gruppen-Policies,
  nicht-kommerzielle Ressourcenanfrage, Moderations-/Einspruchsnotizen),
  explizitem Pilot-Header ohne Echtbetrieb-Versprechen, 0 Netzwerk-Permissions
  und 10 JUnit-Tests (`PilotUnitTest`).
- Android-Pilot gehärtet: `blockNetworkLoads` bleibt beim Standardwert (ohne
  `INTERNET`-Permission löste das Abschalten eine `SecurityException` beim Start
  aus), lokale Pilot-Nachrichten gelangen nur noch als escapte String-Literale
  über `textContent` in das WebView (`PilotDomScript`), externe Links verlassen
  den Piloten ausschließlich als `https`, und die Sync-Konfliktkopie
  `manifest-Mac Studio.webmanifest` ist aus den Assets entfernt.

### Hinzugefügt

- `BACKOFFICE_EINSPRUCHS_FLOW.md`, `docs/PWA_LIGHTHOUSE_AUDIT_2026-07-05.md`,
  Next.js-App-Router-Icons (`icon.png`/`apple-icon.png`) und ein
  vervollständigtes Public-Favicon-Set nachzertifiziert und committet
  (waren seit Wochen unkommittet im Arbeitsbaum; FABLE-SOL Software-Loop
  Punkt 34, 2026-07-23).
- Demo-Screenshot `demo/public/demo-preview.png` als README-Vorschau,
  Web-App-Manifest-Screenshot und Social-Sharing-Bild.
- `llms.txt` für LLM-Crawler-Indexierung (Audience, Search Phrases, Last-checked).
- `README_de.md` als dedizierte deutsche Übersetzung.

### Geändert

- `WORKFLOWS.md` verlinkt nur noch getrackte öffentliche Unterlagen und
  kennzeichnet `START.md`, `STATE.md` und `TODO.md` ausdrücklich als lokale,
  interne Referenzen statt als tote öffentliche Links. Ein begrenzter
  repository-relativer Linkcheck des Routers ist vollständig grün.
- Next.js-Metadaten um Canonical-URL, Keywords, OpenGraph- und Twitter-Preview
  für bessere Auffindbarkeit der GitHub-Pages-Demo erweitert.
- `llms.txt` auf `Last-checked: 2026-06-12` aktualisiert und um die Demo-Vorschau
  ergänzt.
- `README.md` auf English-first umgestellt; German content in `README_de.md` verlagert.
- `README_EN.md` entfernt (Inhalt jetzt in `README.md`).
- GitHub-Topics gesetzt: civic-tech, social-network, location-based, pwa, nextjs, react, typescript, open-concept, municipalities, community-platform.

---

### Geändert (ältere Unreleased-Einträge)

- Öffentliche README-Tabellen auf stabile Konzeptdokumente begrenzt; interne
  Entscheidungs- und Portierungsnotizen bleiben lokal.
- Demo-README vom generischen Next.js-Template auf den LOCUTERRA-Demonstrator
  umgestellt.
- Next.js-Demo auf `next@16.2.7` und `eslint-config-next@16.2.7`
  aktualisiert.
- Optionale `@emnapi/*`-Peer-Abhängigkeiten explizit als Dev-Dependencies
  gelockt, damit Linux-`npm ci` dieselbe Dependency-Grafik wie lokal nutzt.

### Hinzugefügt

- GitHub-Actions-Workflow `demo-ci.yml` für Lint und statischen Demo-Build.

- Projekt-Onboarding mit Agentenregeln, Status, TODOs, Feature-Analyse,
  Architekturkonzept, Begriffen und Arbeitsabläufen.
- `.gitignore` für späteren Git-Start.
- `ROLES_AND_RIGHTS.md` als Spezifikation für Akteure, Rollen, Rechte,
  Verifikation und MVP-Abgrenzung.
- `GOVERNANCE.md` als Spezifikation für Trägerschaft, Moderation, Einspruch
  und Audit.
- `PORTIERUNGSPLAN.md` als Plattform- und Rolloutplan mit Webapp/PWA als
  aktiver Linie und klaren Folgepfaden für Android, iOS und Desktop.

### Behoben

- Falsche Pfadverweise von `.LOCUTERRA` auf `LOCUTERRA` in den
  Onboarding-Dokumenten korrigiert.
- Onboarding-Status in `AUFGABEN.txt` und `STATE.md` auf den verifizierten
  Stand gebracht.

### Geändert

- `README.md`, `CLAUDE.md`, `STATE.md`, `TODO.md`, `DONE.md`, `AUFGABEN.txt`
  und `DECISIONS.md` auf den neuen Rollenmodell-Stand synchronisiert.
- Projektordner auf `DECIDE_LOCUTERRA` umgestellt; Lifecycle-Entscheidung und
  Statusdateien nachgezogen.
- Governance-Rahmen ergänzt und die nächsten Beispielszenarien für Meldung und
  Einspruch vorbereitet.
- Konkrete Konfliktfälle für Meldung, Triage, Sperre und Einspruch in einem
  eigenen Workflow verprobt; `GOVERNANCE.md`, `STATE.md`, `README.md`,
  `TODO.md`, `AUFGABEN.txt`, `DONE.md` und `DECISIONS.md` entsprechend
  nachgezogen.
- Plattformfokus auf Webapp/PWA konkretisiert; `README.md`,
  `ARCHITECTURE.md`, `STATE.md`, `TODO.md`, `AUFGABEN.txt` und `DECISIONS.md`
  auf den neuen Rolloutpfad synchronisiert.

## [0.1.0] - 2026-05-09

### Hinzugefügt

- Initiales Konzept in `KONZEPT.md`.
