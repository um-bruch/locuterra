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

test('Nachrichten-Seite exponiert die Konversationsliste als benannten Listenbereich', () => {
  const src = read('src/app/nachrichten/page.tsx');
  assert.ok(src.includes('role="list"'), 'Konversationsliste hat kein list-Rollenattribut');
  assert.ok(src.includes('aria-label="Konversationen"'), 'Konversationsliste hat keinen sprechenden Namen');
});

test('Konversationsbuttons exponieren Auswahlzustand und Zielbereich', () => {
  const src = read('src/app/nachrichten/page.tsx');
  assert.ok(src.includes('aria-controls={conversationPanelId}'), 'Konversationsbuttons steuern keinen Zielbereich an');
  assert.ok(src.includes('aria-pressed={isActive}'), 'Konversationsbuttons exponieren keinen Auswahlzustand');
  assert.ok(src.includes('aria-label={conversationLabel}'), 'Konversationsbuttons haben kein sprechendes Label');
});

test('Nachrichtenbereich und Demo-Composer haben klaren Accessibility-Kontext', () => {
  const src = read('src/app/nachrichten/page.tsx');
  assert.ok(src.includes('role="region"'), 'Nachrichtenbereich ist nicht als Region markiert');
  assert.ok(src.includes('aria-label={conv ? `Nachrichten zu ${conv.subject}` : "Nachrichten"}'), 'Nachrichtenbereich hat keinen dynamischen Namen');
  assert.ok(src.includes('aria-label="Nachricht schreiben (Demo deaktiviert)"'), 'Deaktiviertes Eingabefeld hat keinen Demo-Hinweis');
  assert.ok(src.includes('aria-label="Nachricht senden (im Demo-Modus deaktiviert)"'), 'Deaktivierter Senden-Button hat keinen Demo-Hinweis');
});
