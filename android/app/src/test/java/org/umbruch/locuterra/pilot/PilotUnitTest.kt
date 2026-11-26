package org.umbruch.locuterra.pilot

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PilotUnitTest {

    @Test
    fun resolverMapsRootAndBasePathToIndexHtml() {
        val r1 = LocuterraAssetResolver.resolveUrl("https://appassets.androidplatform.net/locuterra/")
        assertNotNull(r1)
        assertEquals(listOf("locuterra/index.html"), r1!!.candidateAssetPaths)
        assertEquals("text/html", r1.mimeType)

        val r2 = LocuterraAssetResolver.resolveUrl("https://appassets.androidplatform.net/locuterra")
        assertNotNull(r2)
        assertEquals(listOf("locuterra/index.html"), r2!!.candidateAssetPaths)
    }

    @Test
    fun resolverMapsAllScreenmapRoutesToStaticHtmlCandidates() {
        val routes = mapOf(
            "/locuterra/orte" to "locuterra/orte.html",
            "/locuterra/orte/marktplatz" to "locuterra/orte/marktplatz.html",
            "/locuterra/gruppen" to "locuterra/gruppen.html",
            "/locuterra/gruppen/nachbarschaftshilfe" to "locuterra/gruppen/nachbarschaftshilfe.html",
            "/locuterra/ressourcen" to "locuterra/ressourcen.html",
            "/locuterra/ressourcen/bohrmaschine" to "locuterra/ressourcen/bohrmaschine.html",
            "/locuterra/kanaele" to "locuterra/kanaele.html",
            "/locuterra/kanaele/gemeinde-info" to "locuterra/kanaele/gemeinde-info.html",
            "/locuterra/nachrichten" to "locuterra/nachrichten.html",
            "/locuterra/profil" to "locuterra/profil.html"
        )
        for ((route, expectedPrimary) in routes) {
            val resolved = LocuterraAssetResolver.resolveUrl("https://appassets.androidplatform.net$route")
            assertNotNull("Route should resolve: $route", resolved)
            assertEquals(expectedPrimary, resolved!!.candidateAssetPaths.first())
            assertEquals("text/html", resolved.mimeType)
        }
    }

    @Test
    fun resolverHandlesStaticChunksAndStripsQueryParameters() {
        val resolved = LocuterraAssetResolver.resolveUrl(
            "https://appassets.androidplatform.net/locuterra/_next/static/chunks/main.js?v=123"
        )
        assertNotNull(resolved)
        assertEquals(listOf("locuterra/_next/static/chunks/main.js"), resolved!!.candidateAssetPaths)
        assertEquals("application/javascript", resolved.mimeType)
    }

    @Test
    fun resolverMapsNextCssChunkWithTextCssMimeAndUtf8() {
        val resolved = LocuterraAssetResolver.resolveUrl(
            "https://appassets.androidplatform.net/locuterra/_next/static/chunks/3unk5w385e9dg.css"
        )
        assertNotNull(resolved)
        assertEquals(listOf("locuterra/_next/static/chunks/3unk5w385e9dg.css"), resolved!!.candidateAssetPaths)
        assertEquals("text/css", resolved.mimeType)
        assertEquals("UTF-8", resolved.encoding)
    }

    @Test
    fun allNextAssetsReferencedInIndexHtmlExistAndResolve() {
        val projectDir = File(System.getProperty("user.dir") ?: ".")
        val assetsRoot = listOf(
            File(projectDir, "src/main/assets"),
            File(projectDir, "app/src/main/assets")
        ).firstOrNull { it.isDirectory }
        assertNotNull("Expected Android assets root under src/main/assets", assetsRoot)

        val indexFile = File(assetsRoot!!, "locuterra/index.html")
        assertTrue("Expected index.html at ${indexFile.path}", indexFile.isFile)
        val html = indexFile.readText(Charsets.UTF_8)

        val refRegex = Regex("""(?:href|src)="(/locuterra/_next/[^"?#]+)"""")
        val refs = refRegex.findAll(html).map { it.groupValues[1] }.distinct().toList()
        assertTrue("Expected _next stylesheet/script refs in index.html", refs.isNotEmpty())
        assertTrue(
            "Expected at least one CSS stylesheet under /locuterra/_next/",
            refs.any { it.endsWith(".css") }
        )

        for (ref in refs) {
            val resolved = LocuterraAssetResolver.resolveUrl("https://${LocuterraAssetResolver.VIRTUAL_HOST}$ref")
            assertNotNull("Failed to resolve $ref", resolved)
            val assetRelPath = resolved!!.candidateAssetPaths.first()
            val assetFile = File(assetsRoot, assetRelPath)
            assertTrue("Missing bundled asset for $ref at ${assetFile.path}", assetFile.isFile)
            if (ref.endsWith(".css")) {
                assertEquals("text/css", resolved.mimeType)
            }
        }
    }

    @Test
    fun gradleConfigAllowsNextUnderscoreAssetDirectory() {
        val projectDir = File(System.getProperty("user.dir") ?: ".")
        val gradleFile = listOf(
            File(projectDir, "build.gradle.kts"),
            File(projectDir, "app/build.gradle.kts")
        ).firstOrNull { it.isFile }
        assertNotNull("Expected build.gradle.kts", gradleFile)
        val content = gradleFile!!.readText(Charsets.UTF_8)
        assertTrue("ignoreAssetsPattern must be explicitly configured", content.contains("ignoreAssetsPattern"))
        val patternLine = content.lines().firstOrNull { it.contains("ignoreAssetsPattern") && it.contains("=") }
        assertNotNull("Expected ignoreAssetsPattern assignment", patternLine)
        assertFalse("ignoreAssetsPattern must not exclude <dir>_*", patternLine!!.contains("<dir>_*"))
        assertFalse("ignoreAssetsPattern must not exclude !_*", patternLine.contains("!_*"))
    }

    @Test
    fun resolverRejectsExternalOriginsAndPathTraversalFailClosed() {
        assertNull(LocuterraAssetResolver.resolveUrl("https://example.com/locuterra/"))
        assertNull(LocuterraAssetResolver.resolveUrl("http://appassets.androidplatform.net/locuterra/"))
        assertNull(LocuterraAssetResolver.resolvePath("/locuterra/../secrets.txt"))
        assertNull(LocuterraAssetResolver.resolvePath("/locuterra/orte/../../AndroidManifest.xml"))
        assertNull(LocuterraAssetResolver.resolvePath("/locuterra\\orte"))
    }

    @Test
    fun channelSubscriptionNeverImplicitlyGrantsContactSharingConsent() {
        val store = PilotSessionStore()
        val initial = store.getChannelState("nachbarschafts-kanal")!!
        assertFalse(initial.subscribed)
        assertFalse(initial.contactSharingConsented)

        val afterSub = store.updateChannelSubscription(
            channelId = "nachbarschafts-kanal",
            subscribe = true,
            enableCompanionChat = true
        )
        assertTrue(afterSub.subscribed)
        assertTrue(afterSub.companionChatEnabled)
        assertFalse("Subscribing must never auto-enable contact sharing", afterSub.contactSharingConsented)

        val afterConsent = store.setChannelContactConsent("nachbarschafts-kanal", true)
        assertTrue(afterConsent.contactSharingConsented)

        val afterUnsub = store.updateChannelSubscription("nachbarschafts-kanal", subscribe = false)
        assertFalse(afterUnsub.subscribed)
        assertFalse(afterUnsub.companionChatEnabled)

        val afterRevoke = store.setChannelContactConsent("nachbarschafts-kanal", false)
        assertFalse(afterRevoke.contactSharingConsented)
    }

    @Test
    fun groupJoinRespectsOpenApprovalAndInvitationPolicies() {
        val store = PilotSessionStore()

        val openJoin = store.requestGroupJoin("gartenfreunde")
        assertEquals(PilotSessionStore.GroupJoinStatus.MEMBER, openJoin.status)

        val approvalJoin = store.requestGroupJoin("elterntreff")
        assertEquals(PilotSessionStore.GroupJoinStatus.PENDING_STEWARD_APPROVAL, approvalJoin.status)

        val inviteJoin = store.requestGroupJoin("lesekreis")
        assertEquals(PilotSessionStore.GroupJoinStatus.INVITATION_REQUIRED, inviteJoin.status)
    }

    @Test
    fun resourceInquiryRequiresNonCommercialConfirmationAndCreatesEphemeralMessage() {
        val store = PilotSessionStore()

        val rejected = store.submitResourceInquiry(
            resourceId = "bohrmaschine",
            resourceTitle = "Bohrmaschine",
            recipientDisplayName = "Maria Schneider",
            message = "Ich zahle 20 Euro Miete",
            nonCommercialConfirmed = false
        )
        assertTrue(rejected.isFailure)

        val accepted = store.submitResourceInquiry(
            resourceId = "bohrmaschine",
            resourceTitle = "Bohrmaschine + Bit-Set",
            recipientDisplayName = "Maria Schneider",
            message = "Hallo Maria, kann ich die Bohrmaschine am Samstag kurz ausleihen?",
            nonCommercialConfirmed = true
        )
        assertTrue(accepted.isSuccess)
        assertEquals(1, store.getResourceInquiries().size)
        assertEquals(1, store.getEphemeralMessages().size)

        store.resetToSeed()
        assertEquals(0, store.getResourceInquiries().size)
        assertEquals(0, store.getEphemeralMessages().size)
    }

    @Test
    fun jsStringLiteralEscapesQuotesBackslashesAndMarkup() {
        assertEquals("\"abc\"", PilotDomScript.quoteJsString("abc"))
        assertEquals("\"a\\\\b\"", PilotDomScript.quoteJsString("a\\b"))
        assertEquals("\"a\\\"b\"", PilotDomScript.quoteJsString("a\"b"))
        assertEquals("\"\\u0027\\u003c\\u003e\\u0026\"", PilotDomScript.quoteJsString("'<>&"))
        assertEquals("\"a\\nb\\rc\\u2028d\"", PilotDomScript.quoteJsString("a\nb\rc\u2028d"))
    }

    @Test
    fun appendMessageScriptKeepsHostileTextInsideStringLiterals() {
        val hostile = "\\'); window.LocuterraAndroidPilot.sendLocalMessage('x'); ('<img src=x onerror=alert(1)>"
        val script = PilotDomScript.appendMessageScript("Alex \\' Rivera", hostile)

        assertFalse(script.contains("innerHTML"))
        assertFalse(script.contains("<img"))
        assertFalse(script.contains("\\');"))
        assertTrue(script.contains("text.textContent = " + PilotDomScript.quoteJsString(hostile)))
    }

    @Test
    fun onlyHttpsLinksMayLeaveThePilot() {
        assertTrue(PilotDomScript.isOpenableExternalUrl("https://github.com/um-bruch/locuterra"))
        assertFalse(PilotDomScript.isOpenableExternalUrl("http://example.org/"))
        assertFalse(PilotDomScript.isOpenableExternalUrl("intent://scan/#Intent;scheme=zxing;end"))
        assertFalse(PilotDomScript.isOpenableExternalUrl("file:///sdcard/x.html"))
        assertFalse(PilotDomScript.isOpenableExternalUrl("javascript:alert(1)"))
    }

    @Test
    fun statusSubtitleReflectsSeedAndUpdatedCounts() {
        val store = PilotSessionStore()
        assertEquals(
            "Konto: Alex Rivera (Pseudonym) · Kanäle: 1 · Anfragen: 0 · Nachrichten: 0 · Meldungen: 0",
            store.formatStatusSubtitle()
        )
        store.submitResourceInquiry(
            resourceId = "bohrmaschine",
            resourceTitle = "Bohrmaschine",
            recipientDisplayName = "Maria",
            message = "Testanfrage",
            nonCommercialConfirmed = true
        )
        store.submitModerationOrAppeal("ort:marktplatz", "Testgrund")
        assertEquals(
            "Konto: Alex Rivera (Pseudonym) · Kanäle: 1 · Anfragen: 1 · Nachrichten: 1 · Meldungen: 1",
            store.formatStatusSubtitle()
        )
    }
}
