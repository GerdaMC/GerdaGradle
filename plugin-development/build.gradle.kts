dependencies {
    implementation("org.spongepowered:plugin-meta:0.8.0")
}

val functionalTest by sourceSets.creating

configurations.named(functionalTest.compileClasspathConfigurationName) { extendsFrom(configurations.testCompileClasspath.get()) }
configurations.named(functionalTest.runtimeClasspathConfigurationName) { extendsFrom(configurations.testRuntimeClasspath.get()) }

dependencies {
    functionalTest.implementationConfigurationName("com.google.code.gson:gson:2.8.9")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

indraPluginPublishing {
    plugin(
        "plugin",
        "fun.fotontv.gradle.plugin.GerdaPluginGradle",
        "Gerda Plugin",
        "Set up a project for building Gerda plugins",
        listOf("minecraft", "gerda", "plugin-development")
    )
}