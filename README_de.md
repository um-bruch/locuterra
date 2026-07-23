# LOCUTERRA

> [English Documentation](README.md)

> Konzept und Demonstrator für ein gemeinwohlorientiertes, ortsbasiertes
> Social Network zwischen Bürgern, Institutionen, Kommunen, Organisationen
> und Unternehmen.

LOCUTERRA soll lokale Gesellschaft digital vernetzen, ohne nur ein weiterer
globaler Feed zu sein. Im Zentrum stehen reale Orte, Ebenenreichweiten,
öffentliche und private Gruppen, Ressourcenangebote, Informationskanäle,
Bürgerkontaktstellen, Direktnachrichten und ortsbezogene Warnungen.

**Dieses Repository ist ein Geschenk:** Wir stellen das gesamte Konzept
frei zur Verfügung, damit Kommunen, gemeinnützige Organisationen oder
öffentlich-rechtliche Träger es aufgreifen und umsetzen können.

## Demonstrator

**[Live-Demo ansehen](https://um-bruch.github.io/locuterra/)**

![LOCUTERRA Demo-Startseite](demo/public/demo-preview.png)

Im Ordner [`demo/`](./demo) liegt der Quellcode des Konzept-Demonstrators,
gebaut mit Next.js, React und TypeScript. Er zeigt die fiktive Kommune
**Grüntal** mit synthetischen Daten:

- **9 Orte** (Stadtteile, Treffpunkte, Einrichtungen)
- **5 Gruppen** (Nachbarschaftshilfe, Gartenverein, Elterntreff, ...)
- **8 Ressourcen** (Geräte, Skills, Gesuche, Hilfe)
- **2 Informationskanäle** mit Beiträgen
- **Direktnachrichten** zwischen fiktiven Bürgern

Die Demo-Vorschau wird zugleich als Social-Sharing-Bild für den
GitHub-Pages-Build genutzt.

### Demonstrator starten

```bash
cd demo
npm install
npm run dev
```

Dann im Browser: `http://localhost:3000`

Der Demonstrator ist ein statisch exportierbarer Prototyp ohne Backend.
Alle Daten sind fiktiv und leben im Client.

## Konzeptdokumente

LOCUTERRA besteht aus zwei Konzepten: dem **Plattformkonzept** (wie das
Netzwerk funktioniert) und dem **Finanzierungskonzept** (wie es sich
trägt). Beide gehören zusammen, sind aber bewusst getrennt dokumentiert.

| Dokument | Inhalt |
|---|---|
| [KONZEPT.md](./KONZEPT.md) | Ursprüngliche Produktidee und MVP-Schnitt |
| [FINANZIERUNGSKONZEPT.md](./FINANZIERUNGSKONZEPT.md) | Arena-Modell, Sponsoring und Erlösverteilung |
| [ARCHITECTURE.md](./ARCHITECTURE.md) | Technischer Zielstack und Domänenmodule |
| [DATENMODELL.md](./DATENMODELL.md) | Logisches Datenmodell für den MVP |
| [DATENSCHUTZ.md](./DATENSCHUTZ.md) | Datenschutz- und Einwilligungsmodell |
| [GOVERNANCE.md](./GOVERNANCE.md) | Trägerschaft, Moderation und Einspruch |
| [ROLES_AND_RIGHTS.md](./ROLES_AND_RIGHTS.md) | Akteurs-, Rollen- und Rechtekonzept |
| [REICHWEITENMODELL.md](./REICHWEITENMODELL.md) | Reichweitenstufen (privat bis transnational) |
| [SICHERHEIT_UND_MISSBRAUCH.md](./SICHERHEIT_UND_MISSBRAUCH.md) | Sicherheits- und Missbrauchsrisiken |
| [RESSOURCEN_UND_MARKTPLATZ.md](./RESSOURCEN_UND_MARKTPLATZ.md) | Trennung Ressourcen vs. Marktplatz |
| [RESSOURCEN_FLOW.md](./RESSOURCEN_FLOW.md) | Ablauf für Ressourcenangebote |
| [INFORMATIONSKANAL_FLOW.md](./INFORMATIONSKANAL_FLOW.md) | Abo, Begleitchat und Kontaktfreigabe |
| [BACKOFFICE_EINSPRUCHS_FLOW.md](./BACKOFFICE_EINSPRUCHS_FLOW.md) | Ablauf für Einspruchsstelle und Backoffice-Moderation |
| [GLOSSARY.md](./GLOSSARY.md) | Projektbegriffe |
| [Feature_Analyse_LOCUTERRA.md](./Feature_Analyse_LOCUTERRA.md) | Umfassende Feature-Analyse |

Interne Entscheidungs- und Portierungsnotizen bleiben lokal gepflegt. Die
öffentliche Dokumentation enthält nur die stabilen Konzept- und
Demonstratorunterlagen.

## Zielbild

LOCUTERRA denkt Kommunikation räumlich:

- **Bürger** können Ressourcen, Skills, Gesuche und lokale Gruppen teilen.
- **Institutionen und Kommunen** können Informationskanäle,
  Bürgerkontaktstellen, öffentliche Orte und Krisenwarnungen betreiben.
- **Gemeinnützige Organisationen** können eigene Kanäle, Orte und
  Kontaktangebote pflegen.
- **Ressourcen** bleiben nicht-kommerziell; Geldflüsse gehören in ein
  getrenntes Marktplatzsystem.

## Technischer Zielstack

Der MVP ist als **Webapp/PWA-first** konzipiert:

- **Frontend:** TypeScript, React, Next.js
- **Datenhaltung:** PostgreSQL mit Prisma
- **Validierung:** Zod
- **Tests:** Vitest, Playwright
- **Plattform:** Webapp zuerst; native Shells erst bei echtem Bedarf

## Für Träger und Umsetzer

Dieses Konzept ist vollständig durchspezifiziert:

- MVP-Schnitt, Rollenmodell und Reichweitenmodell stehen
- Datenmodell, Datenschutz und Governance sind ausgearbeitet
- Sicherheits- und Missbrauchsrisiken sind dokumentiert
- Konfliktfälle (Meldung, Triage, Sperre, Einspruch) sind verprobt

Was fehlt: eine **öffentlich-rechtliche oder gemeinnützige Trägerschaft**,
die das System aufbaut, betreibt und verantwortet. Wir haben das Konzept
erarbeitet — ihr müsst es nur noch bauen.

## Lizenz

MIT License. Siehe [LICENSE](./LICENSE).

## Autor

**Lukas Geiger** / [Um:bruch](https://um-bruch.org)
