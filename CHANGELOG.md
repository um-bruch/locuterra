# Changelog

Alle nennenswerten Änderungen an LOCUTERRA werden hier dokumentiert.

## [Unreleased]

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
