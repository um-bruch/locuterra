# UI-Blueprint LOCUTERRA (nach „Software in Worten“)

> Stand: 2026-11-24 · Methode: `software-in-worten` (Richtung 2 → Richtung 1)
> Gilt für die Android-Pilot-App (`v0.2.0-pilot`) und den gemeinsamen Grüntal-Webkern (`demo/`).

---

## 1. Richtung 2 — Diagnose der bisherigen Oberfläche

Die bisherige Android-Pilot-Oberfläche (`v0.1.1-pilot-debug`) litt an vier strukturellen Design-Brüchen:

1. **Doppelte Navigation & doppelte Banner:** Der native Wrapper klebte einen dunkelgrünen Banner mit zwei grauen Standard-Android-Buttons **über** den gelben Web-DemoBanner und die Web-Navigationsleiste – und zusätzlich eine horizontal scrollende Reihe aus 7 klobigen System-Buttons **darunter**.
2. **Fehlende Android-15/16-Insets:** Bei `targetSdk = 36` erzwingt Android Edge-to-Edge. Ohne `WindowInsetsCompat.Type.systemBars()`-Abstände rutschte die Kopfzeile in die Statusleiste/Notch und die Fußleiste hinter die Gestennavigation.
3. **Interne Mechanik statt Nutzerfragen:** Endnutzer sahen Entwickler-Jargon (*„Gemäß INFORMATIONSKANAL_FLOW.md“*, *„Ephemerer Client-State im lokalen Arbeitsspeicher“*, *„PILOT-MODUS · Synthetische Kommune…“*). Nach `software-in-worten` bleibt reine Mechanik unsichtbar.
4. **Ungestylte Standard-Dialoge:** Rohe `AlertDialog`s mit dicht gedrängten `CheckBox`/`EditText`-Stapeln ohne visuelle Hierarchie, Abstände oder klare Handlungsführung.

---

## 2. Richtung 1 — Wartepunkte & Screen-Blueprint

Ein Screen entsteht dort, wo ein Mensch entscheidet, auswählt oder etwas eingibt. Die primäre Navigation folgt `SCREENMAP.md` mit **genau 5 klaren Wartepunkten** in einer einzigen, ruhigen Bottom-Bar:

```
┌──────────────────────────────────────────────────────────┐
│  ● LOCUTERRA   [ Grüntal · Pilot ]        📢   💬   ⋯   │  ← 1 ruhige Top-Bar (mit Statusbar-Inset)
├──────────────────────────────────────────────────────────┤
│                                                          │
│  ┌────────────────────────────────────────────────────┐  │
│  │  Willkommen in Grüntal                             │  │
│  │  Deine Nachbarschaft – lokal & gemeinwohlorientiert│  │
│  └────────────────────────────────────────────────────┘  │
│                                                          │
│  [ 9 Orte ]  [ 5 Gruppen ]  [ 8 Teilen ]  [ 2 Kanäle ]   │
│                                                          │
│  Neuigkeiten aus Grüntal                                 │
│  ┌────────────────────────────────────────────────────┐  │
│  │  Gemeinde Grüntal · 10.06.                         │  │
│  │  Wochenmarkt am Samstag mit erweitertem Angebot    │  │
│  └────────────────────────────────────────────────────┘  │
│                                                          │
├──────────────────────────────────────────────────────────┤
│   [ Start ]    Orte     Gruppen     Teilen      Ich      │  ← 1 native Material-3-Bottom-Bar (mit Nav-Inset)
└──────────────────────────────────────────────────────────┘
```

### Was weggelassen wurde („Den Rest weglassen“)
- Keine doppelte Web-Navigation und kein zweiter gelber Demo-Banner innerhalb der Android-App (`html.android-pilot` blendet Web-Chrome sauber aus; im normalen Webbrowser bleibt er erhalten).
- Keine 7-fach scrollende graue Button-Leiste. Kanäle (`📢`) und Nachrichten (`💬`) sitzen ruhig in der Top-App-Bar sowie direkt im Start-Feed.
- Keine internen Spec-Dateinamen (`INFORMATIONSKANAL_FLOW.md`, `RESSOURCEN_UND_MARKTPLATZ.md`) in Endnutzer-Dialogen.

---

## 3. Feldlegende der 4 Kern-Dialoge

| Dialog | Frage an den Menschen | Felder & Typ | Zweck / Gate | Wenn leer / abgelehnt | Rückmeldung (Rück-Prompt) |
|---|---|---|---|---|---|
| **Kanal-Einstellungen** | *Wie möchtest du diesen Kanal nutzen?* | 3 getrennte Schalter: (1) Kanal abonnieren, (2) Begleitchat aktivieren, (3) Kontaktdaten separat freigeben | Strikte Trennung: Ein Abo gibt niemals automatisch Kontaktdaten frei | Alles deaktivierbar; Einzelwiderruf jederzeit möglich | „Einstellungen für <Kanal> lokal gespeichert.“ |
| **Ressource anfragen** | *Was möchtest du dem Anbieter schreiben?* | Nachricht (mehrzeiliger Text) + Bestätigung „Ohne Geld / Nachbarschaftshilfe“ | Nicht-kommerzielles Gate (kein Marktplatz / keine Zahlungen im MVP) | Leerer Text oder fehlende Bestätigung → klarer Hinweis statt Absenden | „Anfrage lokal vorgemerkt und unter Nachrichten abgelegt.“ |
| **Gruppe beitreten** | *Möchtest du dieser Gruppe beitreten?* | Auswahl Aktion (Beitreten / Austreten) mit sichtbarer Regel (Offen / Genehmigung / Einladung) | Setzt Mitgliedsstatus gemäß Gruppenregel | Bei „Einladung“ klare Erklärung statt stummer Ablehnung | „Status aktualisiert: Mitglied / Wartet auf Freigabe / Einladung nötig.“ |
| **Hinweis / Einspruch** | *Was möchtest du prüfen lassen?* | Anliegen (Text, Pflicht) + optionaler Einspruch für Zweitprüfung (Text) | Trennt Erstprüfung und unabhängige Zweitprüfung | Leeres Anliegen → Hinweis | „Hinweis lokal vermerkt (Zweitprüfung angefordert / gemeldet).“ |

---

## 4. Durchgängige Design-Policies (Oberfläche)

- **Farbwelt (Civic Editorial):** Warmes Alabaster `#F4F6F1` (Fläche), Reinweiß `#FFFFFF` (Karten/Bars), Tiefes Wald-Smaragd `#0F5132` (Primäraktion & Fokus), Sanftes Salbei `#E2F2E8` (aktive Zustände), Anthrazit `#16221C` (Text, Kontrast > 12:1).
- **Touch-Targets:** Mindestens `48dp` Höhe für alle primären Buttons, Nav-Tabs und Dialog-Schalter.
- **Typografie & Ruhe:** Maximal 2 Gewichte pro Karte (Titel SemiBold + Metadaten Regular), großzügiger Weißraum (`16–20px`), weiche Radien (`16px` Karten, `12px` Buttons, `999px` Badges).
- **Offline- & Datenschutz-Ehrlichkeit:** Ein dezenter, klickbarer `Grüntal · Pilot`-Chip im Header zeigt jederzeit transparent den lokalen Pilot-Zustand und erlaubt den 1-Klick-Reset.
