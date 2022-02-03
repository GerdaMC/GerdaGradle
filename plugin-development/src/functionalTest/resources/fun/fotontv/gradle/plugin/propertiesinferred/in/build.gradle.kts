import fun.fotontv.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    `java-library`
    id("fun.fotontv.gradle.plugin")
}

group = "fun.fotontv.test"
version = "1234"
description = "An example of properties coming from build configuration"

// This is the 'minimal' build configuration
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
    }
}