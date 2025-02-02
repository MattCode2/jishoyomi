package eu.kanade.tachiyomi.data.updater

import android.content.Context
import eu.kanade.tachiyomi.BuildConfig
import eu.kanade.tachiyomi.network.NetworkHelper
import kotlinx.serialization.json.Json
import tachiyomi.core.preference.Preference
import tachiyomi.core.preference.PreferenceStore
import uy.kohesive.injekt.injectLazy

class AppUpdateChecker {

    private val networkService: NetworkHelper by injectLazy()
    private val preferenceStore: PreferenceStore by injectLazy()
    private val json: Json by injectLazy()

    private val lastAppCheck: Preference<Long> by lazy {
        preferenceStore.getLong("last_app_check", 0)
    }

    suspend fun checkForUpdate(context: Context, isUserPrompt: Boolean = false): AppUpdateResult {
        // TODO: Add Update checking back in for Jishoyomi
        return AppUpdateResult.NoNewUpdate

        /*
        // Limit checks to once a every 3 days at most
        if (isUserPrompt.not() && Date().time < lastAppCheck.get() + 3.days.inWholeMilliseconds) {
            return AppUpdateResult.NoNewUpdate
        }

        return withIOContext {
            val result = with(json) {
                networkService.client
                    .newCall(GET("https://api.github.com/repos/$GITHUB_REPO/releases/latest"))
                    .awaitSuccess()
                    .parseAs<GithubRelease>()
                    .let {
                        lastAppCheck.set(Date().time)

                        // Check if latest version is different from current version
                        if (isNewVersion(it.version)) {
                            if (context.isInstalledFromFDroid()) {
                                AppUpdateResult.NewUpdateFdroidInstallation
                            } else {
                                AppUpdateResult.NewUpdate(it)
                            }
                        } else {
                            AppUpdateResult.NoNewUpdate
                        }
                    }
            }

            when (result) {
                is AppUpdateResult.NewUpdate -> AppUpdateNotifier(context).promptUpdate(result.release)
                is AppUpdateResult.NewUpdateFdroidInstallation -> AppUpdateNotifier(context).promptFdroidUpdate()
                else -> {}
            }

            result
        }
         */
    }

    private fun isNewVersion(versionTag: String): Boolean {
        // Removes prefixes like "r" or "v"
        val newVersion = versionTag.replace("[^\\d.]".toRegex(), "")

        return if (BuildConfig.PREVIEW) {
            // Preview builds: based on releases in "aniyomiorg/aniyomi-preview" repo
            // tagged as something like "r1234"
            newVersion.toInt() > BuildConfig.COMMIT_COUNT.toInt()
        } else {
            // Release builds: based on releases in "aniyomiorg/aniyomi" repo
            // tagged as something like "v0.1.2"
            val oldVersion = BuildConfig.VERSION_NAME.replace("[^\\d.]".toRegex(), "")

            val newSemVer = newVersion.split(".").map { it.toInt() }
            val oldSemVer = oldVersion.split(".").map { it.toInt() }

            oldSemVer.mapIndexed { index, i ->
                if (newSemVer[index] > i) {
                    return true
                }
            }

            false
        }
    }
}

val GITHUB_REPO: String by lazy {
    if (BuildConfig.PREVIEW) {
        "aniyomiorg/aniyomi-preview"
    } else {
        "aniyomiorg/aniyomi"
    }
}

val RELEASE_TAG: String by lazy {
    if (BuildConfig.PREVIEW) {
        "r${BuildConfig.COMMIT_COUNT}"
    } else {
        "v${BuildConfig.VERSION_NAME}"
    }
}

val RELEASE_URL = "https://github.com/$GITHUB_REPO/releases/tag/$RELEASE_TAG"
