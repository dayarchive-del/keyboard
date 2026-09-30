
package dev.cluely.keyboard.di

import android.content.Context
import android.view.WindowManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.cluely.keyboard.data.api.OpenRouterClient
import dev.cluely.keyboard.data.storage.ApiKeyStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(Logging) {
            // Do NOT log request bodies: screenshots and API keys must never enter logs.
            level = LogLevel.HEADERS
        }
    }

    @Provides
    @Singleton
    fun provideApiKeyStore(
        @ApplicationContext context: Context
    ): ApiKeyStore = ApiKeyStore(context)

    @Provides
    @Singleton
    fun provideOpenRouterClient(
        httpClient: HttpClient,
        apiKeyStore: ApiKeyStore
    ): OpenRouterClient = OpenRouterClient(httpClient, apiKeyStore)

    @Provides
    @Singleton
    fun provideWindowManager(
        @ApplicationContext context: Context
    ): WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
}
