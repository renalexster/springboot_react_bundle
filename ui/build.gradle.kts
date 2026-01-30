plugins {
    id("com.github.node-gradle.node") version "7.0.1"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

description = "ui"

node {
    // Use a specific Node version and download it locally so users don't need Node installed
    version.set("20.12.2")
    download.set(true)
}

// Ensure install runs before build/dev
tasks.named("npm_run_build") {
    dependsOn("npmInstall")
}

tasks.register("build") {
    group = "build"
    description = "Builds the React app using Vite"
    dependsOn("npm_run_build")
}

// Convenience task to start Vite dev server
tasks.register("dev") {
    group = "application"
    description = "Starts the React dev server (Vite)"
    dependsOn("npm_run_dev")
}

// Clean output directory
tasks.register<Delete>("clean") {
    delete(file("dist"))
}
