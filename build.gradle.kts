@file:Suppress("UnstableApiUsage")

plugins {
	id("dev.architectury.loom")
	id("architectury-plugin")
}

val minecraft = stonecutter.current.version

version = "${prop("mod.version")}+$minecraft-playtesting"
base {
	archivesName.set("${prop("mod.id")}-common")
}

architectury.common(stonecutter.tree.branches.mapNotNull {
	if (stonecutter.current.project !in it) null
	else it.project.prop("loom.platform")
})

loom {
	if (stonecutter.eval(minecraft, "<26")) silentMojangMappingsLicense()
	accessWidenerPath = rootProject.file("src/main/resources/${prop("mod.id")}.accesswidener")

	mixin {
		defaultRefmapName.set("${prop("mod.id")}-common-refmap.json")
	}

	decompilers {
		get("vineflower").apply { // Adds names to lambdas - useful for mixins
			options.put("mark-corresponding-synthetics", "1")
		}
	}
}

repositories {
	// Local stub for net.fabricmc:intermediary:26.x (Fabric's published POM has wrong version 0.0.0)
	maven(rootProject.file("local-maven"))

	maven("https://maven.parchmentmc.org/")

	maven("https://maven.terraformersmc.com/")
	maven("https://maven.isxander.dev/releases")
	maven("https://api.modrinth.com/maven")
	maven {
		name = "Gegy"
		url = uri("https://maven.gegy.dev/releases/")
	}
}

dependencies {
	minecraft("com.mojang:minecraft:$minecraft")
	if (stonecutter.eval(minecraft, ">=26")) {
		// 26.1+ ships fully deobfuscated — use a local identity mapping (no renaming needed)
		mappings(files("${rootProject.projectDir}/identity-mappings.jar"))
	} else {
		mappings(loom.layered {
			officialMojangMappings()
			val parchmentMcVersion = versionPropOrNull("parchment_minecraft_version")
			val parchmentMappingsVersion = versionPropOrNull("parchment_mappings_version")
			if (parchmentMcVersion != null && parchmentMappingsVersion != null) {
				parchment("org.parchmentmc.data:parchment-$parchmentMcVersion:$parchmentMappingsVersion@zip")
			}
//			mappings("dev.lambdaurora:${versionProp("yalmm")}")
		})
	}
	modImplementation("net.fabricmc:fabric-loader:${versionProp("fabric_loader")}")

	// Mod implementations
	modCompileOnly("dev.isxander:yet-another-config-lib:${versionProp("yacl_version")}-fabric")
}

tasks.processResources {
	applyProperties(project, listOf("${prop("mod.id")}-common.mixins.json"))
}

java {
	withSourcesJar()
	val java = if (stonecutter.eval(minecraft, ">=26"))
		JavaVersion.VERSION_25 else if (stonecutter.eval(minecraft, ">=1.20.5"))
		JavaVersion.VERSION_21 else JavaVersion.VERSION_17
	targetCompatibility = java
	sourceCompatibility = java
}

tasks.build {
	group = "versioned"
	description = "Must run through 'chiseledBuild'"
}