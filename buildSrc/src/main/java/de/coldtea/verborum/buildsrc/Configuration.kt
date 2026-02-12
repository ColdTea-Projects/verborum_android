import de.coldtea.verborum.buildsrc.versioning.VersionInfo

object Configuration {
    val versionCode = VersionInfo.version.code
    val versionName = VersionInfo.version.name

    val minSdk = 23
    val targetSdk = 35
    val compileSdk = 35
}