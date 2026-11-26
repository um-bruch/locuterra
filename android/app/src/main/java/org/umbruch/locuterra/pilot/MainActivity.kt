package org.umbruch.locuterra.pilot

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import java.io.ByteArrayInputStream

/**
 * LOCUTERRA Android Pilot v0.2.0 — redesigned using `software-in-worten`:
 * - Single calm native Material-3 Top App Bar (brand + clickable "Grüntal · Pilot" pill + quick icon buttons)
 * - Single native 5-tab Bottom Navigation (Start, Orte, Gruppen, Teilen, Ich) with active sage pill indicator
 * - Proper Android 15/16 Edge-to-Edge WindowInsets so bars never overlap status bar or gesture bar
 * - WebView suppresses duplicate web header/banner/footer via `html.android-pilot`
 * - Human-language, field-legend dialogs without raw spec filenames or unstyled gray button bars
 */
class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var topBar: LinearLayout
    private lateinit var bottomNavContainer: LinearLayout
    private lateinit var pilotPillView: TextView
    private val pilotStore = PilotSessionStore()
    private val tabHolders = mutableListOf<TabViewHolder>()
    private var activeTabIndex = 0

    data class NavTab(
        val label: String,
        val routePath: String,
        val iconRes: Int,
        val matchPrefix: String
    )

    private data class TabViewHolder(
        val tab: NavTab,
        val root: LinearLayout,
        val pill: FrameLayout,
        val icon: ImageView,
        val label: TextView
    )

    private val navTabs = listOf(
        NavTab("Start", "/locuterra/", R.drawable.ic_nav_home, "/locuterra/"),
        NavTab("Orte", "/locuterra/orte", R.drawable.ic_nav_places, "/locuterra/orte"),
        NavTab("Gruppen", "/locuterra/gruppen", R.drawable.ic_nav_groups, "/locuterra/gruppen"),
        NavTab("Teilen", "/locuterra/ressourcen", R.drawable.ic_nav_share, "/locuterra/ressourcen"),
        NavTab("Ich", "/locuterra/profil", R.drawable.ic_nav_profile, "/locuterra/profil")
    )

    private val colorBg = Color.parseColor("#F4F6F1")
    private val colorSurface = Color.WHITE
    private val colorBrand = Color.parseColor("#0F5132")
    private val colorSage = Color.parseColor("#E2F2E8")
    private val colorInk = Color.parseColor("#16221C")
    private val colorMuted = Color.parseColor("#5C6B62")
    private val colorBorder = Color.parseColor("#E2E7DF")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Android 15/16 Edge-to-Edge with clean light bars and real inset padding
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = colorSurface
        window.navigationBarColor = colorSurface
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(colorBg)
        }

        // 1. Single calm Top App Bar
        topBar = buildTopAppBar()
        root.addView(topBar)

        // Subtle divider
        root.addView(buildDivider())

        // 2. Hardened Offline WebView serving bundled Next.js Grüntal kernel
        webView = WebView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            setBackgroundColor(colorBg)
        }
        configureWebView(webView)
        root.addView(webView)

        // Subtle divider above bottom navigation
        root.addView(buildDivider())

        // 3. Single Material-3 5-Tab Bottom Navigation Bar
        bottomNavContainer = buildBottomNavigationBar()
        root.addView(bottomNavContainer)

        // Apply real system bar insets so status bar & gesture bar NEVER overlap content
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            topBar.setPadding(dp(16) + bars.left, dp(10) + bars.top, dp(12) + bars.right, dp(10))
            bottomNavContainer.setPadding(dp(6) + bars.left, dp(6), dp(6) + bars.right, dp(8) + bars.bottom)
            insets
        }

        setContentView(root)
        highlightTab(0)

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

    private fun buildDivider(): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(1)
            )
            setBackgroundColor(colorBorder)
        }
    }

    private fun buildTopAppBar(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(colorSurface)
            setPadding(dp(16), dp(10), dp(12), dp(10))

            // Brand emblem dot + title + clickable Pilot pill
            val brandGroup = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }

            val logoDot = FrameLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(dp(30), dp(30)).apply {
                    marginEnd = dp(9)
                }
                background = roundedDrawable(colorBrand, dp(9))
                addView(ImageView(context).apply {
                    setImageResource(R.drawable.ic_nav_places)
                    setColorFilter(Color.WHITE)
                    layoutParams = FrameLayout.LayoutParams(dp(18), dp(18), Gravity.CENTER)
                })
            }
            brandGroup.addView(logoDot)

            val titleCol = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
            }

            val titleView = TextView(context).apply {
                text = getString(R.string.app_name)
                setTextColor(colorInk)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTypeface(Typeface.DEFAULT_BOLD)
                letterSpacing = 0.03f
            }
            titleCol.addView(titleView)

            pilotPillView = TextView(context).apply {
                text = getString(R.string.pilot_chip_label)
                setTextColor(colorBrand)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                setTypeface(Typeface.DEFAULT_BOLD)
                setOnClickListener { showPilotOverviewSheet() }
            }
            titleCol.addView(pilotPillView)
            brandGroup.addView(titleCol)
            addView(brandGroup)

            // Calm circular icon buttons (Channels, Messages, Pilot Menu)
            addView(buildIconAction(R.drawable.ic_nav_channels, getString(R.string.cd_channels)) {
                navigateToRoute("/locuterra/kanaele")
            })
            addView(buildIconAction(R.drawable.ic_nav_messages, getString(R.string.cd_messages)) {
                navigateToRoute("/locuterra/nachrichten")
            })
            addView(buildIconAction(R.drawable.ic_nav_more, getString(R.string.cd_pilot_menu)) {
                showPilotActionsDialog()
            })
        }
    }

    private fun buildIconAction(iconRes: Int, description: String, onClick: () -> Unit): FrameLayout {
        return FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(42), dp(42)).apply {
                marginStart = dp(4)
            }
            background = roundedDrawable(colorBg, dp(21))
            contentDescription = description
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }
            addView(ImageView(context).apply {
                setImageResource(iconRes)
                setColorFilter(colorInk)
                layoutParams = FrameLayout.LayoutParams(dp(20), dp(20), Gravity.CENTER)
            })
        }
    }

    private fun buildBottomNavigationBar(): LinearLayout {
        tabHolders.clear()
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(colorSurface)
            setPadding(dp(6), dp(6), dp(6), dp(8))

            navTabs.forEachIndexed { idx, tab ->
                val item = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                    minimumHeight = dp(52)
                    isClickable = true
                    isFocusable = true
                    contentDescription = tab.label
                    setOnClickListener {
                        highlightTab(idx)
                        navigateToRoute(tab.routePath)
                    }
                }

                val pill = FrameLayout(context).apply {
                    layoutParams = LinearLayout.LayoutParams(dp(56), dp(30))
                    background = roundedDrawable(Color.TRANSPARENT, dp(15))
                }

                val icon = ImageView(context).apply {
                    setImageResource(tab.iconRes)
                    setColorFilter(colorMuted)
                    layoutParams = FrameLayout.LayoutParams(dp(21), dp(21), Gravity.CENTER)
                }
                pill.addView(icon)

                val label = TextView(context).apply {
                    text = tab.label
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                    setTextColor(colorMuted)
                    gravity = Gravity.CENTER
                    setPadding(0, dp(3), 0, 0)
                }

                item.addView(pill)
                item.addView(label)
                addView(item)
                tabHolders.add(TabViewHolder(tab, item, pill, icon, label))
            }
        }
    }

    private fun highlightTab(index: Int) {
        activeTabIndex = index
        tabHolders.forEachIndexed { i, holder ->
            val selected = i == index
            holder.pill.background = roundedDrawable(
                if (selected) colorSage else Color.TRANSPARENT,
                dp(15)
            )
            holder.icon.setColorFilter(if (selected) colorBrand else colorMuted)
            holder.label.setTextColor(if (selected) colorBrand else colorMuted)
            holder.label.setTypeface(
                if (selected) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            )
        }
    }

    private fun syncTabFromUrl(url: String?) {
        val path = runCatching { Uri.parse(url ?: "").path ?: "" }.getOrDefault("")
        val matched = when {
            path.startsWith("/locuterra/orte") -> 1
            path.startsWith("/locuterra/gruppen") -> 2
            path.startsWith("/locuterra/ressourcen") -> 3
            path.startsWith("/locuterra/profil") -> 4
            path == "/locuterra/" || path == "/locuterra" -> 0
            else -> -1 // channels / messages keep calm unselected state
        }
        if (matched >= 0) {
            highlightTab(matched)
        } else {
            tabHolders.forEach { holder ->
                holder.pill.background = roundedDrawable(Color.TRANSPARENT, dp(15))
                holder.icon.setColorFilter(colorMuted)
                holder.label.setTextColor(colorMuted)
                holder.label.setTypeface(Typeface.DEFAULT)
            }
        }
    }

    fun navigateToRoute(routePath: String) {
        val normalized = if (routePath.startsWith("/")) routePath else "/$routePath"
        val url = "https://${LocuterraAssetResolver.VIRTUAL_HOST}$normalized"
        webView.loadUrl(url)
    }

    private fun updatePilotPill() {
        val activeCount = pilotStore.getResourceInquiries().size +
            pilotStore.getEphemeralMessages().size +
            pilotStore.getModerationCases().size
        pilotPillView.text = if (activeCount > 0) {
            "Grüntal · Pilot ($activeCount lokal)"
        } else {
            getString(R.string.pilot_chip_label)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView(view: WebView) {
        view.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = false
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
                    return true
                }
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Externen Link im Browser öffnen?")
                    .setMessage(
                        "Diese App arbeitet komplett lokal. Möchtest du diese Webseite im Browser öffnen?\n\n$urlStr"
                    )
                    .setPositiveButton("Im Browser öffnen") { _, _ ->
                        runCatching {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlStr)))
                        }
                    }
                    .setNegativeButton("Hier bleiben", null)
                    .show()
                return true
            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {
                super.onPageCommitVisible(view, url)
                injectInteractivePilotEnhancements()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                syncTabFromUrl(url)
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
     * Activates Android-shell presentation (`html.android-pilot` suppresses duplicate web nav/banner)
     * and upgrades static demo controls into live local interactive pilot actions.
     */
    private fun injectInteractivePilotEnhancements() {
        val script = """
            (function() {
              document.documentElement.classList.add('android-pilot');
              if (!window.LocuterraAndroidPilot) return;
              var buttons = document.querySelectorAll('button[disabled]');
              buttons.forEach(function(btn) {
                var txt = (btn.textContent || '').trim();
                btn.removeAttribute('disabled');
                btn.classList.remove('cursor-not-allowed', 'bg-emerald-200', 'text-emerald-600');
                btn.classList.add('bg-[#0f5132]', 'text-white', 'shadow-sm');
                if (txt.indexOf('Abonnieren') !== -1) {
                  btn.textContent = 'Kanal-Einstellungen & Abo';
                  btn.onclick = function() { window.LocuterraAndroidPilot.openChannelConsentFlow(window.location.pathname); };
                } else if (txt.indexOf('Beitreten') !== -1) {
                  btn.textContent = 'Gruppe beitreten';
                  btn.onclick = function() { window.LocuterraAndroidPilot.openGroupJoinFlow(window.location.pathname); };
                } else if (txt.indexOf('Nachricht schreiben') !== -1) {
                  btn.textContent = 'Anfrage schreiben';
                  btn.onclick = function() { window.LocuterraAndroidPilot.openResourceInquiryFlow(window.location.pathname, document.title || ''); };
                } else if (txt.indexOf('Senden') !== -1) {
                  btn.textContent = 'Senden';
                  var input = document.querySelector('input[disabled]');
                  if (input) {
                    input.removeAttribute('disabled');
                    input.placeholder = 'Nachricht schreiben …';
                    input.classList.remove('bg-gray-50', 'text-gray-400');
                    input.classList.add('bg-white', 'text-[#16221c]');
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
                    updatePilotPill()
                    appendMessageToWebViewDom(msg.senderDisplayName, msg.body)
                    Toast.makeText(
                        this@MainActivity,
                        "Nachricht lokal gespeichert (Pilot).",
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
            "Kanal-Abo & Datenschutz testen",
            "Ressource anfragen (Nachbarschaftshilfe)",
            "Gruppen-Beitritt testen",
            "Hinweis oder Einspruch melden",
            "Über den Grüntal-Piloten & Status",
            "Alle lokalen Pilot-Daten zurücksetzen"
        )
        AlertDialog.Builder(this)
            .setTitle("Was möchtest du testen?")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> showChannelConsentDialog("gemeinde-info")
                    1 -> showResourceInquiryDialog("bohrmaschine")
                    2 -> showGroupJoinDialog("elterntreff")
                    3 -> showModerationAppealDialog()
                    4 -> showPilotOverviewSheet()
                    5 -> {
                        pilotStore.resetToSeed()
                        updatePilotPill()
                        highlightTab(0)
                        webView.loadUrl(LocuterraAssetResolver.ENTRY_URL)
                        Toast.makeText(
                            this,
                            "Auf Startzustand (Grüntal) zurückgesetzt.",
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

        val container = dialogContainer()
        container.addView(dialogIntro(
            "Entscheide getrennt, wie du „${current.channelName}“ nutzen möchtest. Ein Abo gibt niemals automatisch deine Kontaktdaten weiter."
        ))

        val subCheck = styledCheckCard(
            "Kanal abonnieren",
            "Beiträge dieses Kanals erscheinen in deiner Übersicht.",
            current.subscribed
        )
        val chatCheck = styledCheckCard(
            "Begleitchat aktivieren (optional)",
            "Mit anderen Abonnent:innen unter Pseudonym austauschen.",
            current.companionChatEnabled
        )
        val consentCheck = styledCheckCard(
            "Kontaktdaten separat freigeben",
            "Nur auf ausdrücklichen Wunsch an den Kanalbetreiber — jederzeit einzeln widerrufbar.",
            current.contactSharingConsented
        )

        container.addView(subCheck.card)
        container.addView(chatCheck.card)
        container.addView(consentCheck.card)

        AlertDialog.Builder(this)
            .setTitle("Wie möchtest du diesen Kanal nutzen?")
            .setView(container)
            .setPositiveButton("Speichern") { _, _ ->
                pilotStore.updateChannelSubscription(
                    channelId = current.channelId,
                    subscribe = subCheck.checkBox.isChecked,
                    enableCompanionChat = chatCheck.checkBox.isChecked
                )
                pilotStore.setChannelContactConsent(
                    channelId = current.channelId,
                    consented = consentCheck.checkBox.isChecked
                )
                updatePilotPill()
                Toast.makeText(
                    this,
                    "Einstellungen für „${current.channelName}“ lokal gespeichert.",
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
        val container = dialogContainer()
        container.addView(dialogIntro(
            "Teilen in Grüntal ist kostenlose Nachbarschaftshilfe ohne Geldfluss. Deine Nachricht bleibt lokal in diesem Piloten."
        ))

        val input = styledEditText(
            hint = "Schreibe eine freundliche Anfrage …",
            initial = "Hallo! Könnte ich das am Samstag kurz ausleihen?",
            multiLine = true
        )
        container.addView(input)

        val nonCommercial = styledCheckCard(
            "Kostenlose Nachbarschaftshilfe bestätigen",
            "Kein Kauf, keine Miete, keine Gebühren.",
            true
        )
        container.addView(nonCommercial.card)

        AlertDialog.Builder(this)
            .setTitle("Anfrage senden ($resourceId)")
            .setView(container)
            .setPositiveButton("Anfrage senden") { _, _ ->
                val result = pilotStore.submitResourceInquiry(
                    resourceId = resourceId,
                    resourceTitle = resourceId,
                    recipientDisplayName = "Anbieter Grüntal",
                    message = input.text.toString(),
                    nonCommercialConfirmed = nonCommercial.checkBox.isChecked
                )
                result.onSuccess {
                    updatePilotPill()
                    Toast.makeText(
                        this,
                        "Anfrage lokal vorgemerkt und unter Nachrichten abgelegt.",
                        Toast.LENGTH_SHORT
                    ).show()
                }.onFailure { err ->
                    Toast.makeText(this, err.message ?: "Bitte prüfe deine Eingabe.", Toast.LENGTH_LONG).show()
                }
            }
            .setNeutralButton("Nachrichten") { _, _ ->
                navigateToRoute("/locuterra/nachrichten")
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun showGroupJoinDialog(groupId: String) {
        val allGroups = pilotStore.getAllGroupStates()
        val selected = pilotStore.getGroupState(groupId) ?: allGroups.first()

        val ruleExplanation = when (selected.policy) {
            "offen" -> "Offene Gruppe — du trittst sofort bei."
            "genehmigung" -> "Mit Freigabe — die Ansprechperson bestätigt deine Anfrage."
            else -> "Nur mit Einladung — ohne persönliche Einladung bleibt der Beitritt gesperrt."
        }
        val statusLabel = when (selected.status) {
            PilotSessionStore.GroupJoinStatus.MEMBER -> "Du bist Mitglied"
            PilotSessionStore.GroupJoinStatus.PENDING_STEWARD_APPROVAL -> "Anfrage wartet auf Freigabe"
            PilotSessionStore.GroupJoinStatus.INVITATION_REQUIRED -> "Einladung erforderlich"
            PilotSessionStore.GroupJoinStatus.NOT_JOINED -> "Noch kein Mitglied"
        }

        val container = dialogContainer()
        container.addView(dialogIntro("$ruleExplanation\n\nAktueller Status: $statusLabel"))

        AlertDialog.Builder(this)
            .setTitle(selected.groupName)
            .setView(container)
            .setPositiveButton("Beitreten / Anfragen") { _, _ ->
                val updated = pilotStore.requestGroupJoin(selected.groupId)
                updatePilotPill()
                val feedback = when (updated.status) {
                    PilotSessionStore.GroupJoinStatus.MEMBER -> "Du bist „${updated.groupName}“ beigetreten."
                    PilotSessionStore.GroupJoinStatus.PENDING_STEWARD_APPROVAL -> "Beitrittsanfrage vorgemerkt (wartet auf Freigabe)."
                    PilotSessionStore.GroupJoinStatus.INVITATION_REQUIRED -> "Für diese Gruppe wird eine Einladung benötigt."
                    else -> "Status aktualisiert."
                }
                Toast.makeText(this, feedback, Toast.LENGTH_LONG).show()
            }
            .setNeutralButton("Austreten") { _, _ ->
                pilotStore.leaveGroup(selected.groupId)
                updatePilotPill()
                Toast.makeText(this, "Mitgliedschaft zurückgesetzt.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Schließen", null)
            .show()
    }

    private fun showModerationAppealDialog() {
        val container = dialogContainer()
        container.addView(dialogIntro(
            "Meldung und Einspruch werden getrennt geprüft. Lege hier einen lokalen Testfall für Grüntal an."
        ))

        val reasonInput = styledEditText(
            hint = "Was soll geprüft werden?",
            initial = "Sichtbarkeit eines Ortsbeitrags prüfen",
            multiLine = false
        )
        val appealInput = styledEditText(
            hint = "Optional: Begründung für Zweitprüfung",
            initial = "",
            multiLine = true
        )
        container.addView(reasonInput)
        container.addView(appealInput)

        AlertDialog.Builder(this)
            .setTitle("Hinweis oder Einspruch melden")
            .setView(container)
            .setPositiveButton("Vormerken") { _, _ ->
                pilotStore.submitModerationOrAppeal(
                    targetRef = "ort:marktplatz",
                    reason = reasonInput.text.toString(),
                    appealNote = appealInput.text.toString()
                ).onSuccess {
                    updatePilotPill()
                    Toast.makeText(this, "Hinweis lokal vorgemerkt.", Toast.LENGTH_SHORT).show()
                }.onFailure { err ->
                    Toast.makeText(this, err.message ?: "Bitte Begründung eingeben.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun showPilotOverviewSheet() {
        val scroll = ScrollView(this)
        val content = dialogContainer()
        content.addView(dialogIntro(
            "LOCUTERRA Pilot (v0.2.0) zeigt die fiktive Kommune Grüntal vollständig offline auf deinem Gerät. " +
                "Es gibt keinen Server, kein Tracking und keine echten Personendaten."
        ))
        content.addView(TextView(this).apply {
            text = pilotStore.buildPilotSummaryText()
            setTextColor(colorInk)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setLineSpacing(dp(3).toFloat(), 1f)
            background = roundedDrawable(colorBg, dp(12))
            setPadding(dp(14), dp(12), dp(14), dp(12))
        })
        scroll.addView(content)

        AlertDialog.Builder(this)
            .setTitle("Grüntal · Lokaler Pilot")
            .setView(scroll)
            .setPositiveButton("Verstanden", null)
            .setNeutralButton("Zurücksetzen") { _, _ ->
                pilotStore.resetToSeed()
                updatePilotPill()
                Toast.makeText(this, "Pilot-Daten zurückgesetzt.", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun dialogContainer(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(10), dp(20), dp(6))
        }
    }

    private fun dialogIntro(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            setTextColor(colorMuted)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f)
            setLineSpacing(dp(2).toFloat(), 1f)
            setPadding(0, 0, 0, dp(12))
        }
    }

    private fun styledEditText(hint: String, initial: String, multiLine: Boolean): EditText {
        return EditText(this).apply {
            this.hint = hint
            setText(initial)
            setTextColor(colorInk)
            setHintTextColor(colorMuted)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            inputType = if (multiLine) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            } else {
                InputType.TYPE_CLASS_TEXT
            }
            if (multiLine) minLines = 2
            background = outlinedDrawable(colorBg, colorBorder, dp(12))
            setPadding(dp(14), dp(12), dp(14), dp(12))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(10)
            }
        }
    }

    private data class CheckCard(val card: LinearLayout, val checkBox: CheckBox)

    private fun styledCheckCard(title: String, subtitle: String, checked: Boolean): CheckCard {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = outlinedDrawable(colorBg, colorBorder, dp(12))
            setPadding(dp(12), dp(10), dp(12), dp(10))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(8)
            }
        }
        val box = CheckBox(this).apply {
            text = title
            isChecked = checked
            setTextColor(colorInk)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTypeface(Typeface.DEFAULT_BOLD)
        }
        val sub = TextView(this).apply {
            text = subtitle
            setTextColor(colorMuted)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setPadding(dp(32), dp(2), 0, 0)
        }
        card.addView(box)
        card.addView(sub)
        return CheckCard(card, box)
    }

    private fun roundedDrawable(fillColor: Int, radiusPx: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(fillColor)
            cornerRadius = radiusPx.toFloat()
        }
    }

    private fun outlinedDrawable(fillColor: Int, strokeColor: Int, radiusPx: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(fillColor)
            setStroke(dp(1), strokeColor)
            cornerRadius = radiusPx.toFloat()
        }
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics
        ).toInt()
    }
}
