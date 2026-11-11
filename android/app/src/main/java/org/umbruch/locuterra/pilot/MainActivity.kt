package org.umbruch.locuterra.pilot

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayInputStream

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var statusCounterView: TextView
    private val pilotStore = PilotSessionStore()

    data class NavTab(val label: String, val routePath: String)

    private val navTabs = listOf(
        NavTab("🏠 Feed", "/locuterra/"),
        NavTab("📍 Orte", "/locuterra/orte"),
        NavTab("👥 Gruppen", "/locuterra/gruppen"),
        NavTab("🤝 Ressourcen", "/locuterra/ressourcen"),
        NavTab("📢 Kanäle", "/locuterra/kanaele"),
        NavTab("💬 Nachrichten", "/locuterra/nachrichten"),
        NavTab("👤 Profil", "/locuterra/profil")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.WHITE)
        }

        // 1. Native Pilot Honesty & Privacy Header Bar
        val pilotHeader = buildPilotHeaderBar()
        root.addView(pilotHeader)

        // 2. Hardened Offline WebView serving bundled Next.js Grüntal kernel
        webView = WebView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        configureWebView(webView)
        root.addView(webView)

        // 3. Native Bottom Quick-Navigation Bar (Screenmap routes)
        val bottomNav = buildBottomNavigationBar()
        root.addView(bottomNav)

        setContentView(root)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (webView.canGoBack()) {
                        webView.goBack()
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )

        if (savedInstanceState == null) {
            webView.loadUrl(LocuterraAssetResolver.ENTRY_URL)
        }
    }

    private fun buildPilotHeaderBar(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#065F46")) // Emerald-800
            setPadding(dp(12), dp(8), dp(12), dp(8))

            val topRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val titleView = TextView(context).apply {
                text = getString(R.string.pilot_banner_title)
                setTextColor(Color.WHITE)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setTypeface(typeface, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            topRow.addView(titleView)

            val actionsBtn = Button(context).apply {
                text = getString(R.string.pilot_actions_button)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                setOnClickListener { showPilotActionsDialog() }
            }
            topRow.addView(actionsBtn)

            val infoBtn = Button(context).apply {
                text = getString(R.string.pilot_info_button)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                setOnClickListener { showPilotInfoAndBoundsDialog() }
            }
            topRow.addView(infoBtn)

            addView(topRow)

            statusCounterView = TextView(context).apply {
                setTextColor(Color.parseColor("#D1FAE5"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                updateStatusSubtitle()
            }
            addView(statusCounterView)
        }
    }

    private fun buildBottomNavigationBar(): HorizontalScrollView {
        val scroll = HorizontalScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#F9FAFB"))
            isHorizontalScrollBarEnabled = false
        }
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(8), dp(6), dp(8), dp(6))
        }
        for (tab in navTabs) {
            val btn = Button(this).apply {
                text = tab.label
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                setOnClickListener {
                    navigateToRoute(tab.routePath)
                }
            }
            bar.addView(btn)
        }
        scroll.addView(bar)
        return scroll
    }

    fun navigateToRoute(routePath: String) {
        val normalized = if (routePath.startsWith("/")) routePath else "/$routePath"
        val url = "https://${LocuterraAssetResolver.VIRTUAL_HOST}$normalized"
        webView.loadUrl(url)
    }

    private fun updateStatusSubtitle() {
        val subs = pilotStore.getAllChannelStates().count { it.subscribed }
        val inqs = pilotStore.getResourceInquiries().size
        val msgs = pilotStore.getEphemeralMessages().size
        val mods = pilotStore.getModerationCases().size
        statusCounterView.text =
            "Konto: Alex Rivera (Pseudonym) · Kanäle: $subs · Anfragen: $inqs · Nachrichten: $msgs · Meldungen: $mods"
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView(view: WebView) {
        view.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = false
            // blockNetworkLoads stays at its default: without the INTERNET permission it is
            // already true, and setting it to false would throw a SecurityException.
            // Local assets are still served through shouldInterceptRequest below.
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            cacheMode = WebSettings.LOAD_NO_CACHE
        }

        view.addJavascriptInterface(PilotJsBridge(), "LocuterraAndroidPilot")

        view.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val url = request?.url?.toString() ?: return blockedResponse("Empty request")
                val resolved = LocuterraAssetResolver.resolveUrl(url)
                    ?: return blockedResponse("External or invalid URL blocked in offline pilot mode: $url")

                for (candidate in resolved.candidateAssetPaths) {
                    val stream = runCatching { assets.open(candidate) }.getOrNull()
                    if (stream != null) {
                        return WebResourceResponse(
                            resolved.mimeType,
                            resolved.encoding,
                            200,
                            "OK",
                            mapOf(
                                "Cache-Control" to "no-store",
                                "X-Content-Type-Options" to "nosniff"
                            ),
                            stream
                        )
                    }
                }

                // Fallback to 404.html if HTML route not found
                if (resolved.mimeType == "text/html") {
                    val notFoundStream = runCatching { assets.open("locuterra/404.html") }.getOrNull()
                    if (notFoundStream != null) {
                        return WebResourceResponse(
                            "text/html",
                            "UTF-8",
                            404,
                            "Not Found",
                            mapOf("Cache-Control" to "no-store"),
                            notFoundStream
                        )
                    }
                }

                return blockedResponse("Asset not found: ${resolved.candidateAssetPaths}", 404)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val uri = request?.url ?: return true
                val urlStr = uri.toString()
                if (LocuterraAssetResolver.isAllowedLocalUrl(urlStr)) {
                    return false
                }
                if (!PilotDomScript.isOpenableExternalUrl(urlStr)) {
                    // intent:, file:, content:, javascript: and similar never leave the pilot.
                    return true
                }
                // External informational links (GitHub / Um:bruch) open only after explicit user confirmation
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Externer Link außerhalb des Offline-Piloten")
                    .setMessage(
                        "Der Pilot arbeitet vollständig lokal. Möchtest du diesen externen Link im Systembrowser öffnen?\n\n$urlStr"
                    )
                    .setPositiveButton("Im Browser öffnen") { _, _ ->
                        runCatching {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlStr)))
                        }
                    }
                    .setNegativeButton("Im Piloten bleiben", null)
                    .show()
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                injectInteractivePilotEnhancements()
            }
        }
    }

    private fun blockedResponse(reason: String, statusCode: Int = 403): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "UTF-8",
            statusCode,
            if (statusCode == 404) "Not Found" else "Forbidden",
            mapOf("Cache-Control" to "no-store"),
            ByteArrayInputStream(reason.toByteArray(Charsets.UTF_8))
        )
    }

    /**
     * Upgrades the static Demo buttons in the WebView at runtime so that tapping
     * "Abonnieren (Demo)", "Beitreten (Demo)", "Nachricht schreiben (Demo)", or "Senden"
     * triggers the interactive ephemeral Android Pilot flow while preserving the honest Pilot badge.
     */
    private fun injectInteractivePilotEnhancements() {
        val script = """
            (function() {
              if (!window.LocuterraAndroidPilot) return;
              var buttons = document.querySelectorAll('button[disabled]');
              buttons.forEach(function(btn) {
                var txt = (btn.textContent || '').trim();
                btn.removeAttribute('disabled');
                btn.classList.remove('cursor-not-allowed', 'bg-emerald-200', 'text-emerald-600');
                btn.classList.add('bg-emerald-700', 'text-white', 'hover:bg-emerald-800');
                if (txt.indexOf('Abonnieren') !== -1) {
                  btn.textContent = 'Kanal-Abo & Consent testen (Pilot lokal)';
                  btn.onclick = function() { window.LocuterraAndroidPilot.openChannelConsentFlow(window.location.pathname); };
                } else if (txt.indexOf('Beitreten') !== -1) {
                  btn.textContent = 'Gruppen-Beitritt testen (Pilot lokal)';
                  btn.onclick = function() { window.LocuterraAndroidPilot.openGroupJoinFlow(window.location.pathname); };
                } else if (txt.indexOf('Nachricht schreiben') !== -1) {
                  btn.textContent = 'Nicht-kommerzielle Anfrage testen (Pilot lokal)';
                  btn.onclick = function() { window.LocuterraAndroidPilot.openResourceInquiryFlow(window.location.pathname, document.title || ''); };
                } else if (txt.indexOf('Senden') !== -1) {
                  btn.textContent = 'Lokal senden (Pilot)';
                  var input = document.querySelector('input[disabled]');
                  if (input) {
                    input.removeAttribute('disabled');
                    input.placeholder = 'Ephemere Pilot-Nachricht eingeben (nur lokal im RAM)...';
                    input.classList.remove('bg-gray-50', 'text-gray-400');
                    input.classList.add('bg-white', 'text-gray-900');
                  }
                  btn.onclick = function() {
                    var val = input ? input.value : '';
                    window.LocuterraAndroidPilot.sendLocalMessage(val);
                    if (input) input.value = '';
                  };
                }
              });
            })();
        """.trimIndent()
        webView.evaluateJavascript(script, null)
    }

    inner class PilotJsBridge {
        @JavascriptInterface
        fun openChannelConsentFlow(pathname: String) {
            runOnUiThread {
                val slug = pathname.trimEnd('/').substringAfterLast('/')
                val channelId = if (slug == "nachbarschafts-kanal") "nachbarschafts-kanal" else "gemeinde-info"
                showChannelConsentDialog(channelId)
            }
        }

        @JavascriptInterface
        fun openGroupJoinFlow(pathname: String) {
            runOnUiThread {
                val slug = pathname.trimEnd('/').substringAfterLast('/')
                showGroupJoinDialog(slug)
            }
        }

        @JavascriptInterface
        fun openResourceInquiryFlow(pathname: String, pageTitle: String) {
            runOnUiThread {
                val slug = pathname.trimEnd('/').substringAfterLast('/')
                showResourceInquiryDialog(slug)
            }
        }

        @JavascriptInterface
        fun sendLocalMessage(body: String) {
            runOnUiThread {
                val res = pilotStore.sendEphemeralMessage("conv-pilot-direct", body)
                res.onSuccess { msg ->
                    updateStatusSubtitle()
                    appendMessageToWebViewDom(msg.senderDisplayName, msg.body)
                    Toast.makeText(
                        this@MainActivity,
                        "Lokal im Pilot-RAM vermerkt (kein Serverversand).",
                        Toast.LENGTH_SHORT
                    ).show()
                }.onFailure { err ->
                    Toast.makeText(this@MainActivity, err.message ?: "Fehler", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun appendMessageToWebViewDom(sender: String, body: String) {
        webView.evaluateJavascript(PilotDomScript.appendMessageScript(sender, body), null)
    }

    private fun showPilotActionsDialog() {
        val items = arrayOf(
            "1. Informationskanal-Flow (Abo vs. Chat vs. Kontakt-Consent)",
            "2. Ressourcen-Flow (Nicht-kommerzielle Nachbarschaftsanfrage)",
            "3. Gruppen-Beitritt (Offen vs. Steward-Genehmigung vs. Einladung)",
            "4. Moderation & Einspruch vormerken (Governance-Flow)",
            "5. Aktuellen Pilot-Speicher (RAM) inspizieren",
            "6. Pilot-Zustand auf Grüntal-Seed zurücksetzen"
        )
        AlertDialog.Builder(this)
            .setTitle("LOCUTERRA Pilot-Ablauf (Grüntal)")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> showChannelConsentDialog("gemeinde-info")
                    1 -> showResourceInquiryDialog("bohrmaschine")
                    2 -> showGroupJoinDialog("elterntreff")
                    3 -> showModerationAppealDialog()
                    4 -> showPilotStateSummaryDialog()
                    5 -> {
                        pilotStore.resetToSeed()
                        updateStatusSubtitle()
                        webView.loadUrl(LocuterraAssetResolver.ENTRY_URL)
                        Toast.makeText(
                            this,
                            "Pilot-Zustand auf synthetische Grüntal-Seed-Daten zurückgesetzt.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Schließen", null)
            .show()
    }

    private fun showChannelConsentDialog(defaultChannelId: String) {
        val current = pilotStore.getChannelState(defaultChannelId)
            ?: pilotStore.getChannelState("gemeinde-info")!!

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(12), dp(20), dp(4))
        }

        val info = TextView(this).apply {
            text = "Gemäß INFORMATIONSKANAL_FLOW.md sind Abonnement, Begleitchat und Kontaktfreigabe strikt getrennte Teilflüsse. Ein Kanal-Abo gibt niemals automatisch Kontaktdaten frei."
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        }
        container.addView(info)

        val subCheck = CheckBox(this).apply {
            text = "1. Kanal abonnieren (${current.channelName})"
            isChecked = current.subscribed
        }
        val chatCheck = CheckBox(this).apply {
            text = "2. Optionalen Begleitchat aktivieren"
            isChecked = current.companionChatEnabled
        }
        val consentCheck = CheckBox(this).apply {
            text = "3. Separate Kontaktfreigabe an Kanalbetreiber (jederzeit einzeln widerrufbar)"
            isChecked = current.contactSharingConsented
        }

        container.addView(subCheck)
        container.addView(chatCheck)
        container.addView(consentCheck)

        AlertDialog.Builder(this)
            .setTitle("Kanal-Flow: ${current.channelName}")
            .setView(container)
            .setPositiveButton("Lokal speichern") { _, _ ->
                pilotStore.updateChannelSubscription(
                    channelId = current.channelId,
                    subscribe = subCheck.isChecked,
                    enableCompanionChat = chatCheck.isChecked
                )
                pilotStore.setChannelContactConsent(
                    channelId = current.channelId,
                    consented = consentCheck.isChecked
                )
                updateStatusSubtitle()
                Toast.makeText(
                    this,
                    "Kanal- und Consent-Status lokal aktualisiert.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNeutralButton("Zum Kanal") { _, _ ->
                navigateToRoute("/locuterra/kanaele/${current.channelId}")
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun showResourceInquiryDialog(resourceId: String) {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(12), dp(20), dp(4))
        }

        val hintView = TextView(this).apply {
            text = "Gemäß RESSOURCEN_UND_MARKTPLATZ.md sind Ressourcen im MVP strikt nicht-kommerziell (keine Zahlungen, keine Gebühren, kein Checkout). Die Anfrage bleibt rein lokal im Pilot-RAM."
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        }
        container.addView(hintView)

        val input = EditText(this).apply {
            this.hint = "Nachricht an Anbieter in Grüntal..."
            setText("Hallo! Ist die Ressource ($resourceId) am Samstag in Grüntal-Mitte ausleihbar?")
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }
        container.addView(input)

        val nonCommercialBox = CheckBox(this).apply {
            text = "Nicht-kommerzielle Nachbarschaftshilfe bestätigen (kein Marktplatz/Geldfluss)"
            isChecked = true
        }
        container.addView(nonCommercialBox)

        AlertDialog.Builder(this)
            .setTitle("Ressource anfragen ($resourceId)")
            .setView(container)
            .setPositiveButton("Anfrage lokal senden") { _, _ ->
                val result = pilotStore.submitResourceInquiry(
                    resourceId = resourceId,
                    resourceTitle = resourceId,
                    recipientDisplayName = "Anbieter Grüntal",
                    message = input.text.toString(),
                    nonCommercialConfirmed = nonCommercialBox.isChecked
                )
                result.onSuccess {
                    updateStatusSubtitle()
                    Toast.makeText(
                        this,
                        "Ressourcen-Anfrage lokal im Pilot-RAM gespeichert.",
                        Toast.LENGTH_SHORT
                    ).show()
                }.onFailure { err ->
                    Toast.makeText(this, err.message ?: "Fehler", Toast.LENGTH_LONG).show()
                }
            }
            .setNeutralButton("Zu Nachrichten") { _, _ ->
                navigateToRoute("/locuterra/nachrichten")
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun showGroupJoinDialog(groupId: String) {
        val allGroups = pilotStore.getAllGroupStates()
        val selected = pilotStore.getGroupState(groupId) ?: allGroups.first()

        val message = buildString {
            appendLine("Gruppe: ${selected.groupName}")
            appendLine("Beitrittsregel laut ROLES_AND_RIGHTS.md: ${selected.policy}")
            appendLine("Aktueller Status: ${selected.status}")
            appendLine()
            appendLine("• 'offen' -> direkter Beitritt im Pilot-RAM")
            appendLine("• 'genehmigung' -> Anfrage an Gruppen-Steward vorgemerkt")
            appendLine("• 'einladung' -> Beitritt ohne Steward-Einladung gesperrt")
        }

        AlertDialog.Builder(this)
            .setTitle("Gruppen-Flow: ${selected.groupName}")
            .setMessage(message)
            .setPositiveButton("Beitritt / Anfrage auslösen") { _, _ ->
                val updated = pilotStore.requestGroupJoin(selected.groupId)
                updateStatusSubtitle()
                Toast.makeText(
                    this,
                    "Neuer Gruppenstatus (${updated.groupName}): ${updated.status}",
                    Toast.LENGTH_LONG
                ).show()
            }
            .setNeutralButton("Austreten (Reset)") { _, _ ->
                pilotStore.leaveGroup(selected.groupId)
                updateStatusSubtitle()
            }
            .setNegativeButton("Schließen", null)
            .show()
    }

    private fun showModerationAppealDialog() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(12), dp(20), dp(4))
        }

        val info = TextView(this).apply {
            text = "Gemäß GOVERNANCE.md & BACKOFFICE_EINSPRUCHS_FLOW.md trennt LOCUTERRA Meldung, Erstentscheidung und Zweitprüfung (Einspruchsstelle). Hier kannst du einen lokalen Testfall anlegen."
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        }
        container.addView(info)

        val reasonInput = EditText(this).apply {
            setText("Prüfung Reichweiten-Stufe (Dorf vs. Kommune) bei Ortsbeitrag")
        }
        val appealInput = EditText(this).apply {
            setText("Einspruchsbegründung für Zweitprüfung durch unabhängigen Steward")
        }
        container.addView(reasonInput)
        container.addView(appealInput)

        AlertDialog.Builder(this)
            .setTitle("Moderation & Einspruch (Pilot)")
            .setView(container)
            .setPositiveButton("Vormerken") { _, _ ->
                pilotStore.submitModerationOrAppeal(
                    targetRef = "ort:marktplatz",
                    reason = reasonInput.text.toString(),
                    appealNote = appealInput.text.toString()
                )
                updateStatusSubtitle()
                Toast.makeText(
                    this,
                    "Moderations-/Einspruchsfall lokal vermerkt.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun showPilotStateSummaryDialog() {
        AlertDialog.Builder(this)
            .setTitle("Aktueller Ephemerer Pilot-State")
            .setMessage(pilotStore.buildPilotSummaryText())
            .setPositiveButton("OK", null)
            .setNeutralButton("Auf Seed zurücksetzen") { _, _ ->
                pilotStore.resetToSeed()
                updateStatusSubtitle()
                Toast.makeText(this, "Pilot-Speicher zurückgesetzt.", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showPilotInfoAndBoundsDialog() {
        val scroll = ScrollView(this)
        val infoView = TextView(this).apply {
            setPadding(dp(20), dp(14), dp(20), dp(14))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            this.text = buildString {
                appendLine("LOCUTERRA Android-Pilot (v0.1.0-pilot)")
                appendLine("======================================")
                appendLine()
                appendLine("1. Architektur & Wiederverwendung (DECISIONS.md / PORTIERUNGSPLAN.md):")
                appendLine("• Nutzt denselben statischen Next.js/React/TypeScript-Kern (`demo/out`) und dieselben synthetischen Seed-Daten der fiktiven Kommune 'Grüntal' wie der Web-Demonstrator.")
                appendLine("• Alle 11 MVP-Screens (Feed, Orte, Gruppen, Ressourcen, Kanäle, Direktnachrichten, Profil) werden über `LocuterraAssetResolver` direkt aus den lokalen APK-Assets ausgeliefert.")
                appendLine()
                appendLine("2. Ehrliche Pilot- und Backend-Grenzen:")
                appendLine("• KEIN Echtbetrieb, KEINE echten Bürgerdaten, KEINE Server-Datenbank.")
                appendLine("• Die App fordert bewusst KEINE `android.permission.INTERNET`-Berechtigung im Manifest an; jeglicher ungeprüfte Netzwerkverkehr im WebView ist fail-closed gesperrt.")
                appendLine("• Alle Interaktionen (Kanal-Consent, Gruppenbeitritt, Ressourcen-Anfragen, Nachrichten, Einspruch) sind ephemerer Client-State im lokalen Arbeitsspeicher und jederzeit auf den Seed-Zustand zurücksetzbar.")
                appendLine()
                appendLine("3. Fachliche Schutzregeln im Piloten:")
                appendLine("• Pseudonymität als Standard, Reichweiten-Hierarchie (`privat` bis `transnational`) getrennt von Kontaktfreigabe.")
                appendLine("• Kanal-Abo, Begleitchat und Kontaktfreigabe sind drei getrennte Schalter.")
                appendLine("• Ressourcen sind strikt nicht-kommerziell (Marktplatz/Zahlungen technisch getrennt).")
            }
        }
        scroll.addView(infoView)

        AlertDialog.Builder(this)
            .setTitle("Pilot-Information & Systemgrenzen")
            .setView(scroll)
            .setPositiveButton("Verstanden", null)
            .show()
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics
        ).toInt()
    }
}
