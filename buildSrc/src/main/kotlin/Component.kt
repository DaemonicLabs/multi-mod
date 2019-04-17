import org.gradle.api.Project

sealed class Component {
    abstract val name: String
    abstract val version: String
    abstract val modid: String
    abstract val description: String
    abstract val icon: String
    open val group: String = "multi-mod"
    abstract val artifactId: String
    open val issues: String = "https://github.com/Organization/project/issues"
    open val sources: String = "https://github.com/Organization/project"

    operator fun component1(): String = name
    operator fun component2(): String = version
    operator fun component3(): String = modid

    object Core: Component() {
        override val name = "Core"
        override val version = "1.0.0"
        override val modid = "core"
        override val description = "Fill me in"
        override val icon = "assets/$modid/icon512.png"
        override val artifactId = "core"
    }
    object FeatureA: Component() {
        override val name = "Feature A"
        override val version = "1.0.0"
        override val modid = "core.feature.a"
        override val description = "Fill me in"
        override val icon = "assets/$modid/icon512.png"
        override val artifactId = "feature_a"
    }

    companion object {
        // map of project path to component info
        val components = mapOf(
            ":" to Core,
            ":featureA" to FeatureA
        )
        operator fun get(project: Project): Component = components[project.path]
            ?: throw IllegalStateException("unhandled project: '${project.path}'")
    }
}