package org.umbruch.locuterra.pilot

/**
 * Builds the JavaScript snippets the pilot injects into its WebView.
 *
 * Pure Kotlin on purpose (no android.* or org.json), so the escaping is covered
 * by plain JUnit tests. User-controlled text never becomes HTML or JavaScript
 * source: it is passed as an escaped string literal and assigned via textContent.
 */
object PilotDomScript {

    /** Returns [value] as a double-quoted JavaScript string literal. */
    fun quoteJsString(value: String): String {
        val out = StringBuilder(value.length + 2)
        out.append('"')
        for (ch in value) {
            when {
                ch == '\\' -> out.append("\\\\")
                ch == '"' -> out.append("\\\"")
                ch == '\n' -> out.append("\\n")
                ch == '\r' -> out.append("\\r")
                ch == '\t' -> out.append("\\t")
                // Escaped as well so the literal can never close a quote, a tag or a line.
                ch < ' ' || ch == '\'' || ch == '<' || ch == '>' || ch == '&' ||
                    ch == ' ' || ch == ' ' ->
                    out.append("\\u").append(ch.code.toString(16).padStart(4, '0'))
                else -> out.append(ch)
            }
        }
        out.append('"')
        return out.toString()
    }

    /** Script that appends one locally stored pilot message to the conversation panel. */
    fun appendMessageScript(sender: String, body: String): String {
        val senderLiteral = quoteJsString(sender)
        val bodyLiteral = quoteJsString(body)
        return """
            (function() {
              var panel = document.querySelector('#conversation-panel .space-y-3');
              if (!panel) return;
              var wrap = document.createElement('div');
              wrap.className = 'flex justify-end';
              var bubble = document.createElement('div');
              bubble.className = 'max-w-[80%] rounded-xl px-3 py-2 bg-emerald-100 text-emerald-900 border border-emerald-300';
              var head = document.createElement('p');
              head.className = 'text-xs font-medium mb-0.5';
              head.appendChild(document.createTextNode('🧑 ' + $senderLiteral + ' · '));
              var mark = document.createElement('span');
              mark.className = 'underline';
              mark.textContent = 'Lokal (Pilot)';
              head.appendChild(mark);
              var text = document.createElement('p');
              text.className = 'text-sm';
              text.textContent = $bodyLiteral;
              var note = document.createElement('p');
              note.className = 'text-[10px] text-emerald-700 mt-1';
              note.textContent = 'Ephemerer Client-State · wird beim Reset gelöscht';
              bubble.appendChild(head);
              bubble.appendChild(text);
              bubble.appendChild(note);
              wrap.appendChild(bubble);
              panel.appendChild(wrap);
            })();
        """.trimIndent()
    }

    /** External links may only leave the pilot as plain https URLs. */
    fun isOpenableExternalUrl(url: String): Boolean =
        url.startsWith("https://", ignoreCase = true)
}
