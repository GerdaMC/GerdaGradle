# GerdaGradle

Набор инструментов для разработчиков плагинов Gerda и собственных проектов Gerda.

Плагин gradle для помощи в разработке плагинов.

- Создает файлы метаданных плагина для `main`
- Добавляет задачу для запуска сервера GerdaVanilla

```kotlin
plugins {
    // [...any plugins...] 
    id("fun.fotontv.gradle.plugin") version "1.0.0"
}

gerda {
    apiVersion("1.0.0")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    license("CHANGEME")
    plugin("example") {
        displayName("Example")
        version("0.1")
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

// [...]

```