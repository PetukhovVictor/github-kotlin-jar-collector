package org.jetbrains.githubkotlinjarcollector

import org.jetbrains.githubkotlinjarcollector.collection.GithubAssetsCollector
import org.jetbrains.githubkotlinjarcollector.collection.GithubAssetsCollectorType
import org.jetbrains.githubkotlinjarcollector.collection.JarExtractor
import org.jetbrains.githubkotlinjarcollector.io.DirectoryWalker


fun main(args : Array<String>) {
//    val assetsDirectory = "/Volumes/VICTOR HD/assets"
//    val githubAssetsCollector = GithubAssetsCollector(assetsDirectory)
//    githubAssetsCollector.collect(GithubAssetsCollectorType.DIRECT)

    val assetsDirectory = "/Volumes/VICTOR HD/assets-test"
    DirectoryWalker(assetsDirectory).run {
        JarExtractor(it, it.parentFile.name).extract()
    }
}