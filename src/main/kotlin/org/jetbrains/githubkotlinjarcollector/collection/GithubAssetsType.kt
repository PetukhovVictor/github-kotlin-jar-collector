package org.jetbrains.githubkotlinjarcollector.collection

enum class GithubAssetsType(val contentType: String, val ext: String) {
    JAR("application/java-archive", "jar"),
    ZIP("application/zip", "zip")
}