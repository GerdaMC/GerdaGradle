import fun.fotontv.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    `java-library`
    id("fun.fotontv.gradle.plugin")
}

group = "fun.fotontv.test"
version = "1.0-SNAPSHOT"

java {
    withJavadocJar()
    withSourcesJar()
}

gerda {
    apiVersion("1.0.0-SNAPSHOT")
    license("CHANGEME")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    plugin("example") {
        displayName("Example")
        entrypoint("fun.fotontv.example.Example")
        description("Just testing things...")
        links {
            homepage("https://fotontv.fun")
            source("https://fotontv.fun/source")
            issues("https://fotontv.fun/issues")
        }
        contributor("FOTONTV") {
            description("Lead Developer")
        }
        dependency("gerdaapi") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
    }
}