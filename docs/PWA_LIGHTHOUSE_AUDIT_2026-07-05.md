# LOCUTERRA PWA/Lighthouse Audit - 2026-07-05

## Scope

Audited the static Next.js demonstrator under `demo/` for the open TODO
`Lighthouse-PWA-Audit durchführen (Installierbarkeit, Offline, Performance)`.

This is a local pre-publication audit against the repository state on
2026-07-05. The worktree was already dirty and `master` was behind
`origin/master` by two commits before this slice, so the result is documented as
a local audit and not as a publish-ready GitHub Pages sign-off.

## Evidence

- `npm run lint` passed.
- `node --test tests\pwa.test.mjs tests\bugsweep-20260611.test.mjs` initially
  failed on installability: the manifest exposed the 192 px and 512 px regular
  icons without `purpose: "any"`.
- `demo/public/manifest.webmanifest` now declares `purpose: "any"` for
  `Icon-192.png` and `Icon-512.png`; existing maskable icons remain separate.
- `npx --yes lighthouse --version` was attempted but timed out after 120 s in
  this Windows/OneDrive automation environment before a Lighthouse binary became
  available. No Lighthouse score is claimed from this run.

## Result

- Installability: local manifest contract fixed and covered by the existing PWA
  regression test.
- Offline: service worker contract remains covered by `tests/pwa.test.mjs` and
  `tests/bugsweep-20260611.test.mjs` (`skipWaiting`, `clients.claim`,
  `/locuterra` scope, and `caches.match(..., { ignoreSearch: true })`).
- Performance: no numeric Lighthouse performance score was produced in this
  run. The checked local boundary is lint plus static PWA contract; a real
  browser Lighthouse score should be rerun after the branch is reconciled and
  the GitHub Pages build is current.

## Follow-up

After publishing or reconciling the branch, run Lighthouse against
`https://um-bruch.github.io/locuterra/` or against a fresh local static export
served over HTTP, then store the JSON/HTML report under
`demo/reports/lighthouse/` or an equivalent ignored report folder.
