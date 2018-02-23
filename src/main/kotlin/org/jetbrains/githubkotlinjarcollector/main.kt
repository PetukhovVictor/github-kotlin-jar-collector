package org.jetbrains.githubkotlinjarcollector

import org.jetbrains.githubkotlinjarcollector.collection.GithubAssetsCollector
import org.jetbrains.githubkotlinjarcollector.collection.GithubAssetsCollectorType
import org.jetbrains.githubkotlinjarcollector.collection.GithubJarExtractor
import org.jetbrains.githubkotlinjarcollector.io.DirectoryWalker
import org.jetbrains.githubkotlinjarcollector.io.FileWriter
import java.io.File


fun main(args : Array<String>) {
    val assetsDirectory = "/Volumes/VICTOR HD/assets"
    val githubAssetsCollector = GithubAssetsCollector(assetsDirectory)
    githubAssetsCollector.collect(GithubAssetsCollectorType.DIRECT)

//    val assetsDirectory = "./assets"
//    DirectoryWalker(assetsDirectory).run {
//        GithubJarExtractor(it).extract()
//    }
}