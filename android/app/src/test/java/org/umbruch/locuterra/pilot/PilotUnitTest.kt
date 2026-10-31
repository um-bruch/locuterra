package org.umbruch.locuterra.pilot

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
}
