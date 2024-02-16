package de.coldtea.verborum.buildsrc.versioning

import java.io.File
import java.util.Properties

internal data class VersionInfo(
    val code: Int,
    val name: String
){
    companion object{
        private val versionCode = "version.code"
        private val versionMajor = "version.major"
        private val versionMinor = "version.minor"
        private val versionPatch = "version.patch"

        val version: VersionInfo
            get() = File("version.properties")
                .also { if (!it.exists()) VersionInfo(0, "0") }
                .inputStream()
                .use {
                    Properties().run {
                        load(it)
                        return@use VersionInfo(
                            code = getProperty(versionCode).toInt(),
                            name = "${getProperty(versionMajor)}.${getProperty(versionMinor)}.${getProperty(
                                versionPatch
                            )}"
                        )
                    }
                }
    }
}
