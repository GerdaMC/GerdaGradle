import net.kyori.indra.IndraExtension
import net.kyori.indra.gradle.IndraPluginPublishingExtension
import org.cadixdev.gradle.licenser.LicenseExtension

plugins {
    id("com.gradle.plugin-publish") apply false
    id("net.kyori.indra") apply false
    id("net.kyori.indra.license-header") apply false
    id("net.kyori.indra.publishing.gradle-plugin") apply false
}

group = "fun.fotontv"
version = "1.0.0"

subprojects {
    plugins.apply {
        apply(JavaGradlePluginPlugin::class)
        apply("com.gradle.plugin-publish")
        apply("net.kyori.indra")
        apply("net.kyori.indra.license-header")
        apply("net.kyori.indra.publishing.gradle-plugin")
        apply("net.kyori.indra.git")
    }

    repositories {
        maven("https://repo.spongepowered.org/repository/maven-public/") {
            name = "sponge"
        }
    }

    dependencies {
        "compileOnlyApi"("org.checkerframework:checker-qual:3.17.0")
    }

    val indraGit = extensions.getByType(net.kyori.indra.git.IndraGitExtension::class)
    tasks.withType(Jar::class).configureEach {
        indraGit.applyVcsInformationToManifest(manifest)
        manifest.attributes(
            "Specification-Title" to project.name,
            "Specification-Vendor" to "FOTONTV",
            "Specification-Version" to project.version,
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "FOTONTV"
        )
    }

    extensions.configure(IndraExtension::class) {
        github("GerdaMC", "GerdaGradle") {
            ci(true)
            publishing(true)
        }
        mitLicense()

        configurePublications {
            pom {
                developers {
                    developer {
                        name.set("GerdaMC Team")
                        email.set("obraztsov568@gmail.com")
                    }
                }
            }
        }

        val gerdaSnapshotRepo = project.findProperty("gerdaSnapshotRepo") as String?
        val gerdaReleaseRepo = project.findProperty("gerdaReleaseRepo") as String?
        if (gerdaReleaseRepo != null && gerdaSnapshotRepo != null) {
            publishSnapshotsTo("gerda", gerdaSnapshotRepo)
            publishReleasesTo("gerda", gerdaReleaseRepo)
        }
    }

    extensions.configure(LicenseExtension::class) {
        val name: String by project
        val organization: String by project
        val projectUrl: String by project

        properties {
            this["name"] = name
            this["organization"] = organization
            this["url"] = projectUrl
        }
        header(rootProject.file("HEADER.txt"))
    }

    extensions.configure(SigningExtension::class) {
        val gerdaSigningKey = project.findProperty("gerdaSigningKey") as String?
        val gerdaSigningPassword = project.findProperty("gerdaSigningPassword") as String?
        if (gerdaSigningKey != null && gerdaSigningPassword != null) {
            val keyFile = file(gerdaSigningKey)
            if (keyFile.exists()) {
                useInMemoryPgpKeys(file(gerdaSigningKey).readText(Charsets.UTF_8), gerdaSigningPassword)
            } else {
                useInMemoryPgpKeys(gerdaSigningKey, gerdaSigningPassword)
            }
        } else {
            signatories = PgpSignatoryProvider() // don't use gpg agent
        }
    }

    extensions.findByType(IndraPluginPublishingExtension::class)?.apply {
        pluginIdBase("$group.gradle")
        website("https://fotontv.fun/")
    }
}