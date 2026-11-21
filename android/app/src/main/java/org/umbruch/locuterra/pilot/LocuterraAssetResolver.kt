package org.umbruch.locuterra.pilot

import java.net.URI

/**
 * Pure, deterministic path resolver mapping LOCUTERRA Web/PWA routes (`basePath: "/locuterra"`)
 * on `https://appassets.androidplatform.net` to packaged Android APK assets under `assets/locuterra/`.
 *
 * Enforces fail-closed security:
 * - Rejects non-local hosts and non-https schemes.
 * - Rejects path traversal (`..`), backslashes, and null bytes.
 * - Resolves Next.js static export HTML pages (`/locuterra/orte` -> `locuterra/orte.html`)
 *   even when a sibling directory (`locuterra/orte/marktplatz.html`) exists.
 */
object LocuterraAssetResolver {

    const val VIRTUAL_HOST = "appassets.androidplatform.net"
    const val BASE_PATH = "/locuterra"
    const val ASSET_PREFIX = "locuterra"
    const val ENTRY_URL = "https://$VIRTUAL_HOST$BASE_PATH/"

    data class ResolvedAsset(
        val candidateAssetPaths: List<String>,
        val mimeType: String,
        val encoding: String?
    )

    fun isAllowedLocalUrl(rawUrl: String): Boolean {
        val uri = runCatching { URI(rawUrl) }.getOrNull() ?: return false
        val scheme = uri.scheme?.lowercase() ?: return false
        val host = uri.host?.lowercase() ?: return false
        return scheme == "https" && host == VIRTUAL_HOST
    }

    fun resolveUrl(rawUrl: String): ResolvedAsset? {
        val uri = runCatching { URI(rawUrl) }.getOrNull() ?: return null
        val scheme = uri.scheme?.lowercase() ?: return null
        val host = uri.host?.lowercase() ?: return null
        if (scheme != "https" || host != VIRTUAL_HOST) {
            return null
        }
        return resolvePath(uri.rawPath ?: "/")
    }

    fun resolvePath(rawPath: String): ResolvedAsset? {
        if (rawPath.contains('\\') || rawPath.contains('\u0000')) {
            return null
        }

        val strippedQuery = rawPath.substringBefore('?').substringBefore('#').trim()
        if (strippedQuery.isEmpty()) {
            return ResolvedAsset(
                candidateAssetPaths = listOf("$ASSET_PREFIX/index.html"),
                mimeType = "text/html",
                encoding = "UTF-8"
            )
        }

        val normalizedSlashes = strippedQuery.replace(Regex("/+"), "/")
        val segments = normalizedSlashes.trim('/').split('/').filter { it.isNotEmpty() }
        if (segments.any { it == "." || it == ".." || it.contains("..") }) {
            return null
        }

        // Strip leading "locuterra" segment if present so both "/locuterra/..." and "/_next/..." map cleanly
        val relativeSegments = if (segments.firstOrNull() == "locuterra") {
            segments.drop(1)
        } else {
            segments
        }

        if (relativeSegments.isEmpty()) {
            return ResolvedAsset(
                candidateAssetPaths = listOf("$ASSET_PREFIX/index.html"),
                mimeType = "text/html",
                encoding = "UTF-8"
            )
        }

        val relPath = relativeSegments.joinToString("/")
        val lastSegment = relativeSegments.last()
        val hasExtension = lastSegment.contains('.') && !lastSegment.startsWith(".")

        return if (hasExtension) {
            val mime = guessMimeType(lastSegment)
            ResolvedAsset(
                candidateAssetPaths = listOf("$ASSET_PREFIX/$relPath"),
                mimeType = mime,
                encoding = if (isTextMime(mime)) "UTF-8" else null
            )
        } else {
            ResolvedAsset(
                candidateAssetPaths = listOf(
                    "$ASSET_PREFIX/$relPath.html",
                    "$ASSET_PREFIX/$relPath/index.html"
                ),
                mimeType = "text/html",
                encoding = "UTF-8"
            )
        }
    }

    fun guessMimeType(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "html", "htm" -> "text/html"
            "js", "mjs" -> "application/javascript"
            "css" -> "text/css"
            "json", "webmanifest" -> "application/json"
            "txt" -> "text/plain"
            "svg" -> "image/svg+xml"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "webp" -> "image/webp"
            "ico" -> "image/x-icon"
            "woff2" -> "font/woff2"
            "woff" -> "font/woff"
            else -> "application/octet-stream"
        }
    }

    private fun isTextMime(mime: String): Boolean {
        return mime.startsWith("text/") ||
            mime == "application/javascript" ||
            mime == "application/json" ||
            mime == "image/svg+xml"
    }
}
