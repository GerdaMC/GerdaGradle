/*
 * This file is part of gerdagradle-plugin-development, licensed under the MIT License (MIT).
 *
 * Copyright (c) GerdaPowered <https://www.fotontv.fun>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fun.fotontv.gradle.plugin;

import fun.fotontv.gradle.common.Constants;
import fun.fotontv.gradle.common.GerdaPlatform;
import fun.fotontv.gradle.plugin.config.PluginConfiguration;
import fun.fotontv.gradle.plugin.task.WritePluginMetadataTask;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

public final class GerdaPluginGradle implements Plugin<Project> {

    private @MonotonicNonNull Project project;

    @Override
    public void apply(final Project project) {
        this.project = project;

        project.getLogger().lifecycle("Gerda Plugin 'GRADLE' Toolset Version '{}'", Constants.VERSION);
        project.getPlugins().apply(JavaLibraryPlugin.class);

        final GerdaPluginExtension gerda = project.getExtensions().create("gerda", GerdaPluginExtension.class);

        this.configurePluginMetaGeneration(gerda);

        this.addApiDependency(gerda);
        final NamedDomainObjectProvider<Configuration> gerdaRuntime = this.addRuntimeDependency(gerda);
        final TaskProvider<JavaExec> runServer = this.createRunTask(gerdaRuntime, gerda);

        project.afterEvaluate(a -> {
            if (gerda.apiVersion().isPresent()) {
                gerdaRuntime.configure(config -> {
                    final String apiVersion = gerda.apiVersion().get();

                    final boolean isSnapshot = apiVersion.endsWith(Constants.Dependencies.SNAPSHOT_SUFFIX);
                    config.getAttributes().attribute(
                            GerdaVersioningMetadataRule.API_TARGET,
                            isSnapshot ? apiVersion.substring(0, apiVersion.length() - Constants.Dependencies.SNAPSHOT_SUFFIX.length())
                                    : apiVersion
                    );
                });
            } else {
                project.getLogger().info("GerdaAPI version has not been set within the 'gerda' configuration via the 'version' task. No "
                        + "tasks will be available to run a client or server session for debugging.");
                runServer.configure(t -> t.setEnabled(false));
            }
            if (gerda.injectRepositories().get()) {
                project.getRepositories().maven(r -> {
                    r.setUrl(Constants.Repositories.GERDA);
                    r.setName("gerda");
                });
            }
        });
    }

    private void addApiDependency(final GerdaPluginExtension gerda) {
        // GerdaAPI dependency
        final NamedDomainObjectProvider<Configuration> gerdaApi = this.project.getConfigurations()
                .register("gerdaApi", config -> config
                        .setVisible(false)
                        .defaultDependencies(deps -> {
                            if (gerda.apiVersion().isPresent()) {
                                final String apiVersion = gerda.apiVersion().get();
                                deps.add(
                                        this.project.getDependencies().create(
                                                Constants.Dependencies.GERDA_GROUP
                                                        + ":" + Constants.Dependencies.GERDA_API
                                                        + ":" + apiVersion
                                        )
                                );
                            }
                        }));

        this.project.getPlugins().withType(JavaLibraryPlugin.class, v -> {
            // Add GerdaAPI as a dependency
            this.project.getConfigurations().named(JavaPlugin.COMPILE_ONLY_API_CONFIGURATION_NAME)
                    .configure(config -> config.extendsFrom(gerdaApi.get()));
            // and as an AP
            this.project.getConfigurations().named(JavaPlugin.ANNOTATION_PROCESSOR_CONFIGURATION_NAME)
                    .configure(config -> config.extendsFrom(gerdaApi.get()));
        });
    }

    private NamedDomainObjectProvider<Configuration> addRuntimeDependency(final GerdaPluginExtension gerda) {
        this.project.getDependencies().getComponents().withModule("fun.fotontv:gerdavanilla", GerdaVersioningMetadataRule.class);
        return this.project.getConfigurations().register("gerdaRuntime", conf -> conf.defaultDependencies(a -> {
            final Dependency dep = this.project.getDependencies().create(
                    Constants.Dependencies.GERDA_GROUP
                            + ":" + gerda.platform().get().artifactId()
                            + ":+:universal");

            a.add(dep);
        }));
    }

    @SuppressWarnings("UnstableApiUsage")
    private TaskProvider<JavaExec> createRunTask(final NamedDomainObjectProvider<Configuration> gerdaRuntime, final GerdaPluginExtension gerda) {
        final Directory projectDir = this.project.getLayout().getProjectDirectory();
        final Property<GerdaPlatform> gerdaPlatform = gerda.platform();
        final TaskProvider<JavaExec> runServer = this.project.getTasks().register("runServer", JavaExec.class, task -> {
            task.setGroup(Constants.TASK_GROUP);
            task.setDescription("Run a Gerda server to test this plugin");
            task.setStandardInput(System.in);

            final Provider<FileCollection> gerdaRuntimeFiles = gerdaRuntime.map(c -> c.getIncoming().getFiles());
            task.getInputs().files(gerdaRuntimeFiles);
            task.classpath(gerdaRuntimeFiles);
            task.getMainClass().set(gerda.platform().get().mainClass());
            final Directory workingDirectory = projectDir.dir("run");
            task.setWorkingDir(workingDirectory);

            // Register the javaagent
            task.getJvmArgumentProviders().add(() -> {
                for (final ResolvedArtifactResult dep : gerdaRuntime.get().getIncoming().artifactView(view -> view.setLenient(true))
                        .getArtifacts()) {
                    final ComponentIdentifier id = dep.getVariant().getOwner();
                    task.getLogger().debug("Inspecting artifact {}", id);
                    if (id instanceof ModuleComponentIdentifier) {
                        final ModuleComponentIdentifier moduleId = (ModuleComponentIdentifier) id;
                        if (moduleId.getGroup().equals(Constants.Dependencies.GERDA_GROUP)
                                && moduleId.getModule().equals(gerdaPlatform.get().artifactId())) {
                            task.getLogger().info("Using file {} as Sponge agent", dep.getFile());
                            return Collections.singletonList("-javaagent:" + dep.getFile());
                        }
                    }
                }
                task.getLogger().error("Failed to find a java agent!");
                return Collections.emptyList();
            });

            task.doFirst(a -> {
                final Path path = workingDirectory.getAsFile().toPath();
                try {
                    Files.createDirectories(path);
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });

        this.project.afterEvaluate(p -> {
            final TaskProvider<AbstractArchiveTask> archiveTask;
            if (p.getPlugins().hasPlugin(Constants.Plugins.SHADOW_PLUGIN_ID)) {
                archiveTask = p.getTasks().named(Constants.Plugins.SHADOW_JAR_TASK_NAME, AbstractArchiveTask.class);
            } else {
                archiveTask = p.getTasks().named(JavaPlugin.JAR_TASK_NAME, AbstractArchiveTask.class);
            }
            runServer.configure(it -> {
                it.dependsOn(archiveTask);
                it.classpath(archiveTask.map(AbstractArchiveTask::getArchiveFile));
            });
        });

        return runServer;
    }

    private void configurePluginMetaGeneration(final GerdaPluginExtension gerda) {
        // Configure some useful default values
        gerda.getPlugins().configureEach(new ConfigurePluginAction(this.project, gerda));

        // Then configure the generated sources
        final Provider<Directory> generatedResourcesDirectory = this.project.getLayout().getBuildDirectory().dir("generated/gerda/plugin");

        final TaskProvider<WritePluginMetadataTask> writePluginMetadata = this.project.getTasks().register("writePluginMetadata", WritePluginMetadataTask.class,
                task -> {
                    task.getSourceContainer().set(gerda);
                    task.getOutputDirectory().set(generatedResourcesDirectory);
                }
        );

        this.project.getPlugins().withType(JavaPlugin.class, v -> this.project.getExtensions().getByType(SourceSetContainer.class).named(SourceSet.MAIN_SOURCE_SET_NAME,
                s -> s.getResources().srcDir(writePluginMetadata.map(Task::getOutputs))));
    }

    private static class ConfigurePluginAction implements Action<PluginConfiguration> {
        private final Provider<String> displayName;
        private final Provider<String> version;
        private final Provider<String> description;
        private final Provider<String> gerdaApiVersion;

        ConfigurePluginAction(final Project project, final GerdaPluginExtension gerda) {
            this.displayName = project.provider(project::getName);
            this.version = project.provider(() -> {
                project.getVersion();
                return String.valueOf(project.getVersion());
            });
            this.description = project.provider(project::getDescription);
            this.gerdaApiVersion = gerda.apiVersion();
        }

        @Override
        public void execute(final PluginConfiguration plugin) {
            plugin.getDisplayName().convention(this.displayName);
            plugin.getVersion().convention(this.version);
            plugin.getDescription().convention(this.description);
            plugin.getDependencies().matching(dep -> dep.getName().equals(Constants.Dependencies.GERDA_API))
                    .configureEach(dep -> dep.getVersion().convention(this.gerdaApiVersion));
        }
    }
}
