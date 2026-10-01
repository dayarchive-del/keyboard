package dev.cluely.keyboard.data.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.cluely.keyboard.domain.models.ProviderCatalog
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores API keys per-provider (encrypted with Keystore) plus the selected
 * provider, model and custom base URL. Migrates the old single-key prefs.
 */
@Singleton
class ApiKeyStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = EncryptedSharedPreferences.create(
        context,
        "cluely_secure_settings",
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    init { migrateLegacy() }

    private fun migrateLegacy() {
        // Old keys: "openrouter_api_key" / "openrouter_model" -> per-provider keys.
        val legacyKey = prefs.getString("openrouter_api_key", null)
        val legacyModel = prefs.getString("openrouter_model", null)
        if (!legacyKey.isNullOrBlank() && !prefs.contains(keyFor(ProviderCatalog.OPENROUTER_ID))) {
            prefs.edit().putString(keyFor(ProviderCatalog.OPENROUTER_ID), legacyKey).apply()
        }
        if (!legacyModel.isNullOrBlank() && !prefs.contains(modelKeyFor(ProviderCatalog.OPENROUTER_ID))) {
            prefs.edit().putString(modelKeyFor(ProviderCatalog.OPENROUTER_ID), legacyModel).apply()
        }
    }

    fun getProviderId(): String =
        prefs.getString(KEY_PROVIDER, ProviderCatalog.ZEN_ID) ?: ProviderCatalog.ZEN_ID

    fun setProviderId(id: String) {
        prefs.edit().putString(KEY_PROVIDER, id).apply()
    }

    fun getCustomBaseUrl(): String = prefs.getString(KEY_CUSTOM_URL, "") ?: ""

    fun setCustomBaseUrl(value: String) {
        prefs.edit().putString(KEY_CUSTOM_URL, value.trim().trimEnd('/')).apply()
    }

    /** Resolved base URL for the currently selected provider. */
    fun getBaseUrl(): String {
        val provider = ProviderCatalog.byId(getProviderId())
        return if (provider.isCustom) {
            getCustomBaseUrl().ifBlank { DEFAULT_CUSTOM_URL }
        } else {
            provider.baseUrl
        }
    }

    fun getApiKey(): String = getApiKey(getProviderId())

    fun getApiKey(providerId: String): String =
        prefs.getString(keyFor(providerId), "") ?: ""

    fun setApiKey(value: String) = setApiKey(getProviderId(), value)

    fun setApiKey(providerId: String, value: String) {
        prefs.edit().putString(keyFor(providerId), value.trim()).apply()
    }

    fun clearApiKey() {
        prefs.edit().remove(keyFor(getProviderId())).apply()
    }

    fun getModel(): String = getModel(getProviderId())

    fun getModel(providerId: String): String {
        val fallback = ProviderCatalog.byId(providerId).defaultModel
        return prefs.getString(modelKeyFor(providerId), null) ?: fallback
    }

    fun setModel(value: String) = setModel(getProviderId(), value)

    fun setModel(providerId: String, value: String) {
        prefs.edit().putString(modelKeyFor(providerId), value.trim()).apply()
    }

    fun isConfigured(): Boolean = getApiKey().isNotBlank()

    private fun keyFor(providerId: String) = "api_key_$providerId"
    private fun modelKeyFor(providerId: String) = "model_$providerId"

    companion object {
        private const val KEY_PROVIDER = "provider_id"
        private const val KEY_CUSTOM_URL = "custom_base_url"
        const val DEFAULT_MODEL = "mimo-v2.5-free"
        const val DEFAULT_CUSTOM_URL = "https://api.openai.com/v1"
    }
}
