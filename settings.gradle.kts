pluginManagement {
    repositories {
        maven(url = "http://maven.fabricmc.net"){
            name = "Fabric"
        }
        gradlePluginPortal()
    }
}

rootProject.name = "multi-mod-core"

Component.components.forEach { (path, component) ->
    include(path)
//    project(path).name = component.name
}