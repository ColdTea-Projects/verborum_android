package de.coldtea.verborum.buildsrc.versioning

import java.io.File
import java.util.Properties

internal data class VersionInfo(
    val code: Int,
    val name: String
){
    companion object{
        private const val VERSION_CODE = "version.code"
        private const val VERSION_MAJOR = "version.major"
        private const val VERSION_MINOR = "version.minor"
        private const val VERSION_PATCH = "version.patch"

        /**
         * Used whenever version.properties cannot be read — it is missing (a fresh checkout, a CI
         * runner that does not ship it), unparseable, or the relative lookup below resolved against
         * a working directory that is not the repo root. Configuration time is the wrong place to
         * fail a build over a version stamp, so this degrades to a placeholder instead of throwing.
         *
         * The code is 1, not 0: AGP rejects a zero versionCode outright ("should be a positive
         * integer"), which would turn the missing file into a different configuration failure.
         */
        private val FALLBACK = VersionInfo(code = 1, name = "0.0.0")

        val version: VersionInfo
            get() {
                val file = File("version.properties")
                if (!file.exists()) return FALLBACK

                val properties = Properties().apply {
                    file.inputStream().use { load(it) }
                }

                val code = properties.getProperty(VERSION_CODE)?.trim()?.toIntOrNull()
                    ?: return FALLBACK
                val major = properties.getProperty(VERSION_MAJOR)?.trim() ?: return FALLBACK
                val minor = properties.getProperty(VERSION_MINOR)?.trim() ?: return FALLBACK
                val patch = properties.getProperty(VERSION_PATCH)?.trim() ?: return FALLBACK

                return VersionInfo(code = code, name = "$major.$minor.$patch")
            }
    }
}
