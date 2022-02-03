import fun.fotontv.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    `java-library`
    id("fun.fotontv.gradle.plugin")
}

gerda {
    apiVersion("1.0.0-SNAPSHOT")
    loader {
        // name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    plugin("example") {
        displayName("Example")
        // entrypoint("fun.fotontv.example.Example")
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