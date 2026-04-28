package com.friendzone.android.data.local

import android.content.Context

data class AppConfig(
    val apiBaseUrl: String?
)

class ConfigLoader(private val context: Context) {
    fun load(): AppConfig {
        return runCatching {
            val content = context.assets.open("config.yaml")
                .bufferedReader()
                .use { it.readText() }
            parseYaml(content)
        }.getOrElse { AppConfig(apiBaseUrl = null) }
    }

    private fun parseYaml(content: String): AppConfig {
        val lines = content.lines()
        val apiLine = lines.firstOrNull { it.trim().startsWith("apiBaseUrl:") }
        val raw = apiLine
            ?.substringAfter("apiBaseUrl:")
            ?.trim()
            ?.trim('"')
        return AppConfig(apiBaseUrl = raw)
    }
}


