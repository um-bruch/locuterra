package org.umbruch.locuterra.pilot

/**
 * Ephemeral in-memory state store for the LOCUTERRA Android Pilot App.
 *
 * Implements the domain rules documented in:
 * - DECISIONS.md (ephemeral client state, resettable, zero server persistence, no real citizen data)
 * - INFORMATIONSKANAL_FLOW.md (strict separation between channel subscription, companion chat, and contact sharing consent)
 * - ROLES_AND_RIGHTS.md (citizen account as single login type; open vs. approval-gated vs. invite-only group membership)
 * - RESSOURCEN_FLOW.md & RESSOURCEN_UND_MARKTPLATZ.md (strictly non-commercial local resource inquiries; marketplace/payment rejected)
 * - GOVERNANCE.md & BACKOFFICE_EINSPRUCHS_FLOW.md (transparent reporting and two-stage appeal/moderation trail)
 */
class PilotSessionStore {

    data class ChannelConsentState(
        val channelId: String,
        val channelName: String,
        val subscribed: Boolean,
        val companionChatEnabled: Boolean,
        val contactSharingConsented: Boolean
    )

    enum class GroupJoinStatus {
        NOT_JOINED,
        MEMBER,
        PENDING_STEWARD_APPROVAL,
        INVITATION_REQUIRED
    }

    data class GroupMembershipState(
        val groupId: String,
        val groupName: String,
        val policy: String, // "offen", "genehmigung", "einladung"
        val status: GroupJoinStatus
    )

    data class ResourceInquiry(
        val id: String,
        val resourceId: String,
        val resourceTitle: String,
        val recipientDisplayName: String,
        val message: String,
        val nonCommercialConfirmed: Boolean
    )

    data class EphemeralMessage(
        val id: String,
        val conversationId: String,
        val senderAccountId: String,
        val senderDisplayName: String,
        val body: String
    )

    data class ModerationCase(
        val id: String,
        val targetRef: String,
        val reason: String,
        val appealNote: String?,
        val status: String // "gemeldet_lokal" | "einspruch_eingereicht_lokal"
    )

    private val channels = linkedMapOf<String, ChannelConsentState>()
    private val groups = linkedMapOf<String, GroupMembershipState>()
    private val resourceInquiries = mutableListOf<ResourceInquiry>()
    private val messages = mutableListOf<EphemeralMessage>()
    private val moderationCases = mutableListOf<ModerationCase>()

    init {
        resetToSeed()
    }

    @Synchronized
    fun resetToSeed() {
        channels.clear()
        channels["gemeinde-info"] = ChannelConsentState(
            channelId = "gemeinde-info",
            channelName = "Gemeinde Grüntal — Offiziell",
            subscribed = true,
            companionChatEnabled = false,
            contactSharingConsented = false
        )
        channels["nachbarschafts-kanal"] = ChannelConsentState(
            channelId = "nachbarschafts-kanal",
            channelName = "Nachbarschaft Grüntal-Mitte",
            subscribed = false,
            companionChatEnabled = false,
            contactSharingConsented = false
        )

        groups.clear()
        groups["nachbarschaftshilfe"] = GroupMembershipState(
            groupId = "nachbarschaftshilfe",
            groupName = "Nachbarschaftshilfe Grüntal-Mitte",
            policy = "offen",
            status = GroupJoinStatus.MEMBER
        )
        groups["gartenfreunde"] = GroupMembershipState(
            groupId = "gartenfreunde",
            groupName = "Gartenfreunde Grüntal",
            policy = "offen",
            status = GroupJoinStatus.NOT_JOINED
        )
        groups["elterntreff"] = GroupMembershipState(
            groupId = "elterntreff",
            groupName = "Elterntreff Grüntal",
            policy = "genehmigung",
            status = GroupJoinStatus.NOT_JOINED
        )
        groups["lesekreis"] = GroupMembershipState(
            groupId = "lesekreis",
            groupName = "Lesekreis an der Bibliothek",
            policy = "einladung",
            status = GroupJoinStatus.NOT_JOINED
        )
        groups["willkommen"] = GroupMembershipState(
            groupId = "willkommen",
            groupName = "Willkommen in Grüntal",
            policy = "offen",
            status = GroupJoinStatus.MEMBER
        )

        resourceInquiries.clear()
        messages.clear()
        moderationCases.clear()
    }

    /**
     * Subscribes or unsubscribes to a channel (`INFORMATIONSKANAL_FLOW.md`).
     * Invariant: Subscribing NEVER automatically enables companionChat or contactSharingConsented!
     * Unsubscribing ends the subscription and companion chat, while explicit contact-sharing
     * can also be revoked separately via `setChannelContactConsent`.
     */
    @Synchronized
    fun updateChannelSubscription(
        channelId: String,
        subscribe: Boolean,
        enableCompanionChat: Boolean = false
    ): ChannelConsentState {
        val current = channels[channelId] ?: ChannelConsentState(
            channelId = channelId,
            channelName = channelId,
            subscribed = false,
            companionChatEnabled = false,
            contactSharingConsented = false
        )
        val updated = if (subscribe) {
            current.copy(
                subscribed = true,
                companionChatEnabled = enableCompanionChat
                // contactSharingConsented intentionally untouched (never implicitly true)
            )
        } else {
            current.copy(
                subscribed = false,
                companionChatEnabled = false
            )
        }
        channels[channelId] = updated
        return updated
    }

    @Synchronized
    fun setChannelContactConsent(channelId: String, consented: Boolean): ChannelConsentState {
        val current = channels[channelId] ?: ChannelConsentState(
            channelId = channelId,
            channelName = channelId,
            subscribed = false,
            companionChatEnabled = false,
            contactSharingConsented = false
        )
        val updated = current.copy(contactSharingConsented = consented)
        channels[channelId] = updated
        return updated
    }

    @Synchronized
    fun getChannelState(channelId: String): ChannelConsentState? = channels[channelId]

    @Synchronized
    fun getAllChannelStates(): List<ChannelConsentState> = channels.values.toList()

    /**
     * Requests group membership according to `membershipPolicy` (`ROLES_AND_RIGHTS.md`).
     */
    @Synchronized
    fun requestGroupJoin(groupId: String): GroupMembershipState {
        val current = groups[groupId] ?: GroupMembershipState(
            groupId = groupId,
            groupName = groupId,
            policy = "offen",
            status = GroupJoinStatus.NOT_JOINED
        )
        val nextStatus = when (current.policy) {
            "offen" -> GroupJoinStatus.MEMBER
            "genehmigung" -> GroupJoinStatus.PENDING_STEWARD_APPROVAL
            "einladung" -> GroupJoinStatus.INVITATION_REQUIRED
            else -> GroupJoinStatus.PENDING_STEWARD_APPROVAL
        }
        val updated = current.copy(status = nextStatus)
        groups[groupId] = updated
        return updated
    }

    @Synchronized
    fun leaveGroup(groupId: String): GroupMembershipState? {
        val current = groups[groupId] ?: return null
        val updated = current.copy(status = GroupJoinStatus.NOT_JOINED)
        groups[groupId] = updated
        return updated
    }

    @Synchronized
    fun getGroupState(groupId: String): GroupMembershipState? = groups[groupId]

    @Synchronized
    fun getAllGroupStates(): List<GroupMembershipState> = groups.values.toList()

    /**
     * Records a non-commercial resource inquiry (`RESSOURCEN_UND_MARKTPLATZ.md`).
     * Rejects empty messages or commercial/payment keywords in MVP resource flow.
     */
    @Synchronized
    fun submitResourceInquiry(
        resourceId: String,
        resourceTitle: String,
        recipientDisplayName: String,
        message: String,
        nonCommercialConfirmed: Boolean = true
    ): Result<ResourceInquiry> {
        val trimmed = message.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Nachricht darf nicht leer sein."))
        }
        if (!nonCommercialConfirmed) {
            return Result.failure(
                IllegalArgumentException("Ressourcen im MVP sind strikt nicht-kommerziell (getrennt vom Marktplatz).")
            )
        }
        val inquiry = ResourceInquiry(
            id = "inq-${resourceInquiries.size + 1}",
            resourceId = resourceId,
            resourceTitle = resourceTitle,
            recipientDisplayName = recipientDisplayName,
            message = trimmed,
            nonCommercialConfirmed = true
        )
        resourceInquiries.add(inquiry)
        messages.add(
            EphemeralMessage(
                id = "msg-ephemeral-${messages.size + 1}",
                conversationId = "conv-res-$resourceId",
                senderAccountId = "demo-user",
                senderDisplayName = "Alex Rivera (Pilot-Konto)",
                body = "[Ressourcen-Anfrage: $resourceTitle] $trimmed"
            )
        )
        return Result.success(inquiry)
    }

    @Synchronized
    fun sendEphemeralMessage(conversationId: String, body: String): Result<EphemeralMessage> {
        val trimmed = body.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Nachrichtentext darf nicht leer sein."))
        }
        val msg = EphemeralMessage(
            id = "msg-ephemeral-${messages.size + 1}",
            conversationId = conversationId,
            senderAccountId = "demo-user",
            senderDisplayName = "Alex Rivera (Pilot-Konto)",
            body = trimmed
        )
        messages.add(msg)
        return Result.success(msg)
    }

    @Synchronized
    fun submitModerationOrAppeal(
        targetRef: String,
        reason: String,
        appealNote: String? = null
    ): Result<ModerationCase> {
        val cleanReason = reason.trim()
        if (cleanReason.isEmpty()) {
            return Result.failure(IllegalArgumentException("Begründung darf nicht leer sein."))
        }
        val cleanAppeal = appealNote?.trim()?.takeIf { it.isNotEmpty() }
        val item = ModerationCase(
            id = "mod-${moderationCases.size + 1}",
            targetRef = targetRef.trim().ifEmpty { "allgemein" },
            reason = cleanReason,
            appealNote = cleanAppeal,
            status = if (cleanAppeal != null) "einspruch_eingereicht_lokal" else "gemeldet_lokal"
        )
        moderationCases.add(item)
        return Result.success(item)
    }

    @Synchronized
    fun getResourceInquiries(): List<ResourceInquiry> = resourceInquiries.toList()

    @Synchronized
    fun getEphemeralMessages(): List<EphemeralMessage> = messages.toList()

    @Synchronized
    fun getModerationCases(): List<ModerationCase> = moderationCases.toList()

    @Synchronized
    fun buildPilotSummaryText(): String {
        val activeSubs = channels.values.count { it.subscribed }
        val activeConsents = channels.values.count { it.contactSharingConsented }
        val joinedGroups = groups.values.count { it.status == GroupJoinStatus.MEMBER }
        val pendingGroups = groups.values.count { it.status == GroupJoinStatus.PENDING_STEWARD_APPROVAL }
        return buildString {
            appendLine("Aktiver Pilot-Zustand (nur lokal im Arbeitsspeicher):")
            appendLine("• Bürgerkonto: Alex Rivera (Pseudonym, Ortsteil Grüntal-Mitte)")
            appendLine("• Kanal-Abos: $activeSubs / ${channels.size} aktiv (Kontaktfreigaben separat: $activeConsents)")
            appendLine("• Gruppen: $joinedGroups Mitglied, $pendingGroups Steward-Genehmigung ausstehend")
            appendLine("• Nicht-kommerzielle Ressourcen-Anfragen: ${resourceInquiries.size}")
            appendLine("• Ephemere Direktnachrichten: ${messages.size}")
            appendLine("• Moderations-/Einspruchs-Vormerkungen: ${moderationCases.size}")
        }
    }
}
