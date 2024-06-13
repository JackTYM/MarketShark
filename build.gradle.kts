import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import org.apache.commons.lang3.SystemUtils

plugins {
    idea
    java
    id("gg.essential.loom") version "0.10.0.5"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

// Constants:
val baseGroup: String by project
val mcVersion: String by project
val version: String by project
val mixinGroup = "$baseGroup.mixin"
val modid: String by project

// Toolchains:
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

// Minecraft configuration:
loom {
    log4jConfigs.from(file("log4j2.xml"))
    launchConfigs {
        "client" {
            // If you don't want mixins, remove these lines
            property("mixin.debug", "true")
            arg("--tweakClass", "gg.essential.loader.stage0.EssentialSetupTweaker")
        }
    }
    runConfigs {
        "client" {
            if (SystemUtils.IS_OS_MAC_OSX) {
                // This argument causes a crash on macOS
                vmArgs.remove("-XstartOnFirstThread")
            }
        }
        remove(getByName("server"))
    }
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
        // If you don't want mixins, remove these lines
        mixinConfig("mixins.$modid.json")
    }
    // If you don't want mixins, remove these lines
    mixin {
        defaultRefmapName.set("mixins.$modid.refmap.json")
    }
}

sourceSets.main {
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
}

// Dependencies:
repositories {
    mavenCentral()
    google()
    maven("https://repo.spongepowered.org/maven/")
    maven("https://repo.sk1er.club/repository/maven-public")
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    modImplementation("org.spongepowered:mixin:0.8.5-SNAPSHOT")
    shadowImpl("gg.essential:loader-launchwrapper:1.1.3")
    shadowImpl("org.java-websocket:Java-WebSocket:1.5.2")
    shadowImpl("com.neovisionaries:nv-websocket-client:2.14")
    modImplementation("gg.essential:essential-1.8.9-forge:2581")
    modImplementation(files("libs/CoflMod-1.5.5-alpha.jar"))
}

// Tasks:
tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
}

tasks.withType(Jar::class) {
    archiveBaseName.set(modid)
    manifest.attributes.run {
        this["FMLCorePluginContainsFMLMod"] = "true"
        this["ForceLoadAsMod"] = "true"
        this["TweakClass"] = "gg.essential.loader.stage0.EssentialSetupTweaker"
        this["MixinConfigs"] = "mixins.$modid.json"
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("mcversion", mcVersion)
    inputs.property("modid", modid)
    inputs.property("basePackage", baseGroup)

    filesMatching(listOf("mcmod.info", "mixins.$modid.json")) {
        expand(inputs.properties)
    }

    rename("(.+_at.cfg)", "META-INF/$1")
}

val remapJar by tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    archiveClassifier.set("")
    from(tasks.shadowJar)
    input.set(tasks.shadowJar.get().archiveFile)
}

tasks.jar {
    archiveClassifier.set("without-deps")
    destinationDirectory.set(layout.buildDirectory.dir("badjars"))
}

tasks.shadowJar {
    destinationDirectory.set(layout.buildDirectory.dir("badjars"))
    archiveClassifier.set("all-dev")
    configurations = listOf(shadowImpl)
    doLast {
        configurations.forEach {
            println("Copying jars into mod: ${it.files}")
        }
    }
    fun relocate(name: String) = relocate(name, "$baseGroup.deps.$name")
}

tasks.assemble.get().dependsOn(tasks.remapJar)

tasks.register("updateConfigJson") {
    group = "build"
    description = "Updates config.json with the latest JAR file"

    val jarName = "${rootProject.name}-$version.jar"
    val latestJar = "../build/libs/$jarName"
    var outputJar = "../build/libs/${rootProject.name}-$version-Obf.jar"

    val configFile = file("${projectDir}/grunt/config.json")
    val jsonSlurper = JsonSlurper()
    val config = jsonSlurper.parse(configFile)
    (config as MutableMap<String, MutableMap<String, Any>>)["Settings"]?.set("Input", latestJar)
    config["Settings"]?.set("Output", outputJar)

    configFile.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(config)))
    println("Updated config.json with latest JAR: ${latestJar}")
}

tasks.register("obfuscate") {
    group = "build"
    description = "Runs the Java obfuscator"
    dependsOn("updateConfigJson")

    doLast {
        exec {
            workingDir("./grunt")
            executable("java")
            args("-jar", "grunt-1.5.7.jar")
        }
    }
}

tasks.build {
    finalizedBy("obfuscate")
}


val remapHammerheadJar by tasks.register<net.fabricmc.loom.task.RemapJarTask>("remapHammerheadJar") {
    group = "build"
    description = "Remap the Hammerhead JAR"

    // Set the input and output for the remap task
    archiveClassifier.set("")

    // Use the output of buildHammerhead as the input for remapJar
    input.set(layout.buildDirectory.file("badjars/${project.name}-${project.version}-Hammerhead.jar"))
    from(layout.buildDirectory.file("badjars/${project.name}-${project.version}-Hammerhead.jar"))

}

tasks.register<Jar>("buildHammerhead") {
    destinationDirectory.set(layout.buildDirectory.dir("badjars"))
    archiveClassifier.set("Hammerhead")
    doFirst {
        System.setProperty("marketshark.version", "Hammerhead")
    }
    // Include all compiled classes and resources in the JAR

    from(sourceSets.main.get().output)

    // Configure the manifest attributes
    manifest {
        attributes(
            "FMLCorePluginContainsFMLMod" to "true",
            "ForceLoadAsMod" to "true",
            "TweakClass" to "gg.essential.loader.stage0.EssentialSetupTweaker",
            "MixinConfigs" to "mixins.$modid.json"
        )
    }

    // Ensure dependencies are included
    dependsOn("shadowJar")
    from({
        shadowImpl.map { if (it.isDirectory) it else zipTree(it) }
    })
    dependsOn("remapHammerheadJar")
}

tasks.named("build") {
    dependsOn("buildHammerhead")
}

tasks.register<Jar>("buildWobbegong") {
    archiveClassifier.set("Wobbegong")
    doFirst {
        System.setProperty("marketshark.version", "Wobbegong")
    }
    finalizedBy("build")
}

tasks.register<Jar>("buildGreatWhite") {
    archiveClassifier.set("GreatWhite")
    doFirst {
        System.setProperty("marketshark.version", "GreatWhite")
    }
    finalizedBy("build")
}

tasks.register<Jar>("buildMegalodon") {
    archiveClassifier.set("Megalodon")
    doFirst {
        System.setProperty("marketshark.version", "Megalodon")
    }
    finalizedBy("build")
}

// Custom Task to Run All Builds
tasks.register("buildAll") {
    dependsOn("buildHammerhead", "buildWobbegong", "buildGreatWhite", "buildMegalodon")
}

// Example usage in your code:
tasks.register("printVersion") {
    doLast {
        println("MarketShark Version: " + System.getProperty("marketshark.version"))
    }
}

// Ensure printVersion runs after buildAll
tasks.named("buildAll") {
    finalizedBy("printVersion")
}
