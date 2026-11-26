import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const __dir = dirname(fileURLToPath(import.meta.url));
const root = resolve(__dir, '..');

function read(rel) {
  return readFileSync(resolve(root, rel), 'utf-8');
}

// --- 1. Navigations-, Verlinkungs- und Landmark-Verträge ---

test('Kanäle-Übersicht verlinkt jeden Kanal auf seine Detailseite /kanaele/[id]', () => {
  const src = read('src/app/kanaele/page.tsx');
  assert.ok(src.includes('from "next/link"'), 'kanaele/page.tsx importiert next/link nicht');
  assert.ok(src.includes('href={`/kanaele/${ch.id}`}'), 'kanaele/page.tsx verlinkt nicht auf /kanaele/${ch.id}');
});

test('Layout stellt Skip-Link und benannten Hauptinhalt (#main-content) bereit', () => {
  const layout = read('src/app/layout.tsx');
  assert.ok(layout.includes('href="#main-content"'), 'Skip-Link auf #main-content fehlt');
  assert.ok(layout.includes('id="main-content"'), 'id="main-content" auf <main> fehlt');
});

test('globals.css definiert sichtbare :focus-visible-Stile', () => {
  const css = read('src/app/globals.css');
  assert.ok(css.includes(':focus-visible'), ':focus-visible fehlt in globals.css');
});

test('Navigation setzt aria-label, aria-current und aria-hidden auf dekorativen Icons', () => {
  const nav = read('src/components/Navigation.tsx');
  assert.ok(nav.includes('aria-label="Hauptnavigation"'), '<nav> hat kein aria-label');
  assert.ok(nav.includes('aria-current={active ? "page" : undefined}'), 'aria-current="page" fehlt');
  assert.ok(!nav.includes('text-gray-500'), 'Mobile-Navigation nutzt noch kontrastschwaches text-gray-500');
  const hiddenMatches = nav.match(/aria-hidden="true"/g) || [];
  assert.ok(hiddenMatches.length >= 3, 'Dekorative Navigations-Emojis brauchen aria-hidden="true"');
});

// --- 2. Überschriftenhierarchie & Kontrast ---

test('Card unterstützt headingLevel mit Standard h2 und dekorativem aria-hidden-Emoji', () => {
  const card = read('src/components/Card.tsx');
  assert.ok(card.includes('headingLevel'), 'Card hat keine headingLevel-Prop');
  assert.ok(card.includes('headingLevel = "h2"'), 'Card-Standardüberschrift muss h2 sein (kein h1->h3-Sprung)');
  assert.ok(card.includes('aria-hidden="true"'), 'Card-Emoji braucht aria-hidden="true"');
  assert.ok(!card.includes('text-gray-500'), 'Card-Untertitel sollte text-gray-600 statt text-gray-500 nutzen');
});

test('Ortsdetail-Unterkarten setzen headingLevel="h3" unter h2-Abschnitten', () => {
  const orteDetail = read('src/app/orte/[id]/page.tsx');
  const h3Props = orteDetail.match(/headingLevel="h3"/g) || [];
  assert.equal(h3Props.length, 3, 'Alle 3 Unterabschnitte in orte/[id]/page.tsx sollten Card mit headingLevel="h3" nutzen');
});

test('Startseite hat keinen h2->h3-Ebenensprung bei den drei Einstiegskarten', () => {
  const home = read('src/app/page.tsx');
  assert.ok(home.includes('<h2 className="font-semibold text-emerald-800 mb-1">'), 'Einstiegskarten auf der Startseite sollten h2 statt h3 nutzen');
});

test('Lesbare Metadaten verzichten auf kontrastschwaches text-gray-400', () => {
  const noGray400Files = [
    'src/app/page.tsx',
    'src/app/kanaele/page.tsx',
    'src/app/kanaele/[id]/page.tsx',
    'src/app/orte/[id]/page.tsx',
    'src/app/gruppen/page.tsx',
    'src/app/gruppen/[id]/page.tsx',
    'src/app/ressourcen/[id]/page.tsx',
    'src/app/profil/page.tsx',
  ];
  for (const rel of noGray400Files) {
    assert.ok(!read(rel).includes('text-gray-400'), `${rel} enthält noch kontrastschwaches text-gray-400`);
  }
  const msgSrc = read('src/app/nachrichten/page.tsx');
  const occurrences = msgSrc.match(/text-gray-400/g) || [];
  assert.equal(occurrences.length, 1, 'nachrichten/page.tsx darf text-gray-400 nur noch am input[disabled] für Android-Bridge führen');
});

test('Dekorative Emojis in Inhaltsseiten tragen aria-hidden="true"', () => {
  const emojiPages = [
    'src/app/page.tsx',
    'src/app/kanaele/page.tsx',
    'src/app/kanaele/[id]/page.tsx',
    'src/app/gruppen/[id]/page.tsx',
    'src/app/ressourcen/[id]/page.tsx',
    'src/app/profil/page.tsx',
    'src/app/nachrichten/page.tsx',
  ];
  for (const rel of emojiPages) {
    assert.ok(read(rel).includes('aria-hidden="true"'), `${rel} hat kein aria-hidden="true" auf dekorativen Emojis`);
  }
});

// --- 3. Android-Pilot-DOM-Bridge-Kompatibilität ---

test('Deaktivierte Demo-Buttons behalten vom Android-Piloten erwartete Labels und Klassen', () => {
  assert.ok(read('src/app/kanaele/[id]/page.tsx').includes('Abonnieren (Demo)'));
  assert.ok(read('src/app/gruppen/[id]/page.tsx').includes('Beitreten (Demo)'));
  assert.ok(read('src/app/ressourcen/[id]/page.tsx').includes('Nachricht schreiben (Demo)'));
  const msg = read('src/app/nachrichten/page.tsx');
  assert.ok(msg.includes('Senden'));
  assert.ok(msg.includes('bg-emerald-200 text-emerald-600'));
});

// --- 4. Domain-Daten- & Nicht-Kommerzialitäts-Vertrag (Grüntal-Seed) ---

function extractIds(tsSource) {
  return [...tsSource.matchAll(/^\s+id:\s*"([^"]+)"/gm)].map((m) => m[1]);
}

function extractFieldValues(tsSource, field) {
  const re = new RegExp(`^\\s+${field}:\\s*"([^"]+)"`, 'gm');
  return [...tsSource.matchAll(re)].map((m) => m[1]);
}

test('Grüntal-Seed-Daten besitzen eindeutige IDs und gültige Referenzen', () => {
  const placesSrc = read('src/data/places.ts');
  const accountsSrc = read('src/data/accounts.ts');
  const groupsSrc = read('src/data/groups.ts');
  const resourcesSrc = read('src/data/resources.ts');
  const channelsSrc = read('src/data/channels.ts');
  const messagesSrc = read('src/data/messages.ts');

  const placeIds = new Set(extractIds(placesSrc));
  const accountIds = new Set(extractIds(accountsSrc));
  const groupIds = extractIds(groupsSrc);
  const resourceIds = extractIds(resourcesSrc);

  assert.equal(placeIds.size, 9, 'Erwartet 9 eindeutige Orte');
  assert.equal(accountIds.size, 6, 'Erwartet 6 eindeutige Konten');
  assert.equal(new Set(groupIds).size, 5, 'Erwartet 5 eindeutige Gruppen');
  assert.equal(new Set(resourceIds).size, 8, 'Erwartet 8 eindeutige Ressourcen');

  for (const pid of extractFieldValues(placesSrc, 'parentPlaceId')) {
    assert.ok(placeIds.has(pid), `Unbekannte parentPlaceId: ${pid}`);
  }
  for (const pid of extractFieldValues(accountsSrc, 'primaryPlaceId')) {
    assert.ok(placeIds.has(pid), `Unbekannte primaryPlaceId: ${pid}`);
  }
  for (const pid of extractFieldValues(groupsSrc, 'placeId')) {
    assert.ok(placeIds.has(pid), `Unbekannte Gruppen-placeId: ${pid}`);
  }
  for (const aid of extractFieldValues(groupsSrc, 'stewardAccountId')) {
    assert.ok(accountIds.has(aid), `Unbekannte stewardAccountId: ${aid}`);
  }
  for (const pid of extractFieldValues(resourcesSrc, 'placeId')) {
    assert.ok(placeIds.has(pid), `Unbekannte Ressourcen-placeId: ${pid}`);
  }
  for (const aid of extractFieldValues(resourcesSrc, 'ownerId')) {
    assert.ok(accountIds.has(aid), `Unbekannte ownerId: ${aid}`);
  }
  for (const pid of extractFieldValues(channelsSrc, 'placeId')) {
    assert.ok(placeIds.has(pid), `Unbekannte Kanal-placeId: ${pid}`);
  }
  for (const aid of extractFieldValues(channelsSrc, 'operatorId')) {
    assert.ok(accountIds.has(aid), `Unbekannte operatorId: ${aid}`);
  }
  for (const aid of extractFieldValues(messagesSrc, 'senderAccountId')) {
    assert.ok(accountIds.has(aid), `Unbekannte senderAccountId: ${aid}`);
  }
});

test('Ressourcen bleiben im MVP strikt nicht-kommerziell ohne Preis-/Zahlungsfelder', () => {
  const typesSrc = read('src/data/types.ts');
  const resourcesSrc = read('src/data/resources.ts');
  for (const forbidden of ['price', 'currency', 'checkout', 'payment', 'fee']) {
    assert.ok(!typesSrc.toLowerCase().includes(forbidden), `types.ts enthält verbotenes Marktplatzfeld: ${forbidden}`);
    assert.ok(!resourcesSrc.toLowerCase().includes(forbidden), `resources.ts enthält verbotenes Marktplatzfeld: ${forbidden}`);
  }
});
