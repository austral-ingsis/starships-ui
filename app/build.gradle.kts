plugins {
    id("edu.austral.ingsis.starships.kotlin-application-conventions")
    id("edu.austral.ingsis.starships.fx-conventions")
}

dependencies {
    implementation(project(":starships-ui"))
}

application {
    // Define the main class for the application.
    mainClass.set("edu.austral.ingsis.starships.app.AppKt")
}
