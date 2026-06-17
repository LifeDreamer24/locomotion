plugins {
	id("dev.kikugie.stonecutter")
	id("dev.architectury.loom") version "1.17.477" apply false
	id("architectury-plugin") version "3.5.167" apply false
	id("com.gradleup.shadow") version "9.4.2" apply false
}
stonecutter active "26.2" /* [SC] DO NOT EDIT */

// Builds every version into `build/libs/{mod.version}/{loader}`
stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) {
	group = "project"
	ofTask("buildAndCollect")
}

stonecutter registerChiseled tasks.register("chiseledRunDatagen", stonecutter.chiseled) {
	group = "project"
	ofTask("runDatagen")
}

// Builds loader-specific versions into `build/libs/{mod.version}/{loader}`
for (it in stonecutter.tree.branches) {
	if (it.id.isEmpty()) continue
	val loader = it.id.replaceFirstChar { it.uppercaseChar() }
	stonecutter registerChiseled tasks.register("chiseledBuild$loader", stonecutter.chiseled) {
		group = "project"
		versions { branch, _ -> branch == it.id }
		ofTask("buildAndCollect")
	}
}

// Runs active versions for each loader
for (it in stonecutter.tree.nodes) {
	if (it.metadata != stonecutter.current || it.branch.id.isEmpty()) continue
	val types = listOf("Client", "Server")
	val loader = it.branch.id.replaceFirstChar { it.uppercaseChar() }
	for (type in types) it.project.tasks.register("runActive$type$loader") {
		group = "project"
		dependsOn("run$type")
	}
}
