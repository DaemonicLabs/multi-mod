import moe.nikky.counter.CounterExtension
import net.fabricmc.loom.task.RemapJar
import net.fabricmc.loom.task.RemapSourcesJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    kotlin("jvm") version Jetbrains.Kotlin.version
    id("moe.nikky.persistentCounter") version "0.0.8-SNAPSHOT"
    id("fabric-loom") version Fabric.Loom.version
    `maven-publish`
}
evaluationDependsOnChildren()

allprojects {
    apply(plugin="kotlin")
    apply(plugin="java-library")
    apply(plugin="fabric-loom")


    val component = Component[project]

    // TODO load project specifics from buildSrc

    base {
        archivesBaseName = component.modid
    }

    apply(plugin="moe.nikky.persistentCounter")

    // get branch from jenkins
    val branch = System.getenv("GIT_BRANCH")
        ?.takeUnless { it == "master" }
        ?.let { "-$it" }
        ?: ""

    val counter: CounterExtension = extensions.getByType()

    val buildNumber = counter.variable(id = "buildNumber", key = component.version + branch)
    // TODO: increase buildNumber
    version = component.version + if(System.getenv("BUILD_NUMBER") != null) "+build.$buildNumber" else "+local"

    repositories {
        mavenCentral()
        maven(url = "http://maven.fabricmc.net"){
            name = "Fabric"
        }
    }

    minecraft {

    }

    configurations.modCompile.extendsFrom(configurations.include)
    configurations.compileOnly.extendsFrom(configurations.modCompile)

    // shared dependencies
    dependencies {
//        implementation(kotlin("stdlib-jdk8"))
        minecraft(group = "com.mojang", name = "minecraft", version = Minecraft.version)

        mappings(group = "net.fabricmc", name = "yarn", version = "${Minecraft.version}+build.${Fabric.Yarn.version}")
        modCompile(group = "net.fabricmc", name = "fabric-loader", version = Fabric.Loader.version)

        include(group = "net.fabricmc", name = "fabric", version = Fabric.API.version + ".+")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    tasks.getByName<ProcessResources>("processResources") {
        filesMatching("fabric.mod.json") {
            expand(
                mutableMapOf(
                    "modid" to component.modid,
                    "version" to version,
                    "name" to component.name,
                    "description" to component.description,
                    "issues" to component.issues,
                    "sources" to component.sources,
                    "icon" to component.icon,
                    "kotlinVersion" to Jetbrains.Kotlin.version,
                    "fabricApiVersion" to Fabric.API.version
                )
            )
        }
    }


    val jarTask = tasks.getByName<Jar>("jar") {

    }
    val remapJar = tasks.getByName<RemapJar>("remapJar") {
        (this as Task).dependsOn(jarTask)
        jar = jarTask.archivePath
    }

//    val sourcesJar = tasks.create<Jar>("sourcesJar") {
////        dependsOn(tasks.getByName("classes"))
//        classifier = "sources"
//        from(sourceSets["main"].allSource)
//    }
//    val remapSourcesJar = tasks.getByName<RemapSourcesJar>("remapSourcesJar") {
//        dependsOn(sourcesJar)
//        jar = sourcesJar.archivePath
//    }

    // MAVEN PUBLISH

    project.group = component.group
    apply(plugin="maven-publish")

    publishing {
        publications {
            val mainPublication = create("main", MavenPublication::class.java) {

                groupId = project.group.toString()
                artifactId = component.artifactId.toLowerCase()
                version = project.version.toString()

                artifact(jarTask) {
                    builtBy(remapJar)
                }
//                artifact(sourcesJar) {
//                    builtBy(remapSourcesJar)
//                }
            }
//            create("snapshot", MavenPublication::class.java) {
//                groupId = project.group.toString()
//                artifactId = project.name.toLowerCase()
//                version = "${Constants.modVersion}-SNAPSHOT"
//
//                artifact(shadowJar)
//                artifact(sourcesJar)
//
//                shadowComponents()
//            }
        }
        repositories {
//            maven(url = "http://mavenupload.modmuss50.me/") {
//                val mavenPass: String? = project.properties["mavenPass"] as String?
//                mavenPass?.let {
//                    credentials {
//                        username = "buildslave"
//                        password = mavenPass
//                    }
//                }
//            }
        }
    }

    // TODO: curseforge publish

    task<DefaultTask>("depsize") {
        group = "help"
        description = "prints dependency sizes"
        doLast {
            val formatStr = "%,10.2f"
            val size = configurations.include.resolve()
                .map { it.length() / (1024.0 * 1024.0) }.sum()

            val out = buildString {
                append("Total dependencies size:".padEnd(45))
                append("${String.format(formatStr, size)} Mb\n\n")
                configurations
                    .include
                    .resolve()
                    .sortedWith(compareBy { -it.length() })
                    .forEach {
                        append(it.name.padEnd(45))
                        append("${String.format(formatStr, (it.length() / 1024.0))} kb\n")
                    }
            }
            println(out)
        }
    }
}

//
dependencies {
    include(group = "io.github.prospector.modmenu", name = "ModMenu", version = "+")
}