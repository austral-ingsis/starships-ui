import org.gradle.kotlin.dsl.`maven-publish`

plugins {
    id("edu.austral.ingsis.starships.kotlin-common-conventions")
    id("org.openjfx.javafxplugin")
}

javafx {
    version = "18"
    modules = listOf("javafx.graphics")
}
