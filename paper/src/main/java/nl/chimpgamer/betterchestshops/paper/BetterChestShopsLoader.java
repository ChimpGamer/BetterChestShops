package nl.chimpgamer.betterchestshops.paper;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class BetterChestShopsLoader implements PluginLoader {

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        var dependencies = new ArrayList<String>() {{
            add("org.jetbrains.kotlin:kotlin-stdlib:2.1.20");
            add("org.jetbrains.exposed:exposed-core:0.59.0");
            add("org.jetbrains.exposed:exposed-dao:0.59.0");
            add("org.jetbrains.exposed:exposed-jdbc:0.59.0");
            add("org.jetbrains.exposed:exposed-java-time:0.59.0");
            add("org.xerial:sqlite-jdbc:3.48.0.0");
            add("org.mariadb.jdbc:mariadb-java-client:3.5.1");
            add("org.incendo:cloud-core:2.0.0");
            add("org.incendo:cloud-paper:2.0.0-beta.10");
            add("org.incendo:cloud-minecraft-extras:2.0.0-beta.10");
            add("org.incendo:cloud-kotlin-coroutines:2.0.0");
            add("dev.dejvokep:boosted-yaml:1.3.7");
            add("com.zaxxer:HikariCP:6.2.1");
        }};

        var mavenLibraryResolver = new MavenLibraryResolver();
        dependencies.forEach(dependency -> mavenLibraryResolver.addDependency(new Dependency(new DefaultArtifact(dependency), null)));
        mavenLibraryResolver.addRepository(new RemoteRepository.Builder("paper", "default", "https://repo.papermc.io/repository/maven-public/").build());

        classpathBuilder.addLibrary(mavenLibraryResolver);
    }
}