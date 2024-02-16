import de.coldtea.verborum.buildsrc.versioning.VersionInfo

object Configuration {
    val versionCode = VersionInfo.version.code
    val versionName = VersionInfo.version.name

    val minSdk = 21
    val targetSdk = 34
    val compileSdk = 34
}