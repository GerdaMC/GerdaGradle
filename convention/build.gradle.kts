plugins {
    groovy
}

tasks.withType(GroovyCompile::class).configureEach {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
    options.release.set(indra.javaVersions().target())
    options.compilerArgs.add("-Xlint:all")
}

dependencies {
    api(project(":gerdagradle-plugin-development"))
    implementation(localGroovy())
    api("net.kyori:indra-common:2.0.6")
    api("gradle.plugin.org.cadixdev.gradle:licenser:0.6.1")
    api("com.google.code.gson:gson:2.8.9")
}

indraPluginPublishing {
    plugin(
        "gerda.dev",
        "fun.fotontv.gradle.convention.GerdaConventionPlugin",
        "GerdaMC Convention",
        "Gradle conventions for Gerda organization projects",
        listOf("gerda", "convention")
    )
}