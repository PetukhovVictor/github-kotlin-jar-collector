package org.jetbrains.githubkotlinjarcollector

import org.jetbrains.githubkotlinjarcollector.collection.GithubAssetsCollector
import org.jetbrains.githubkotlinjarcollector.collection.GithubAssetsCollectorType
import org.jetbrains.githubkotlinjarcollector.collection.JarExtractor
import org.jetbrains.githubkotlinjarcollector.io.DirectoryWalker

enum class Stage {
    COLLECTING, EXTRACTING
}

class Runner {
    companion object {
        fun run(stage: Stage, packagesPath: String) {
            when (stage) {
                Stage.COLLECTING -> {
                    GithubAssetsCollector(packagesPath).collect(GithubAssetsCollectorType.DIRECT)
                }
                Stage.EXTRACTING -> {
                    DirectoryWalker(packagesPath).run {
                        JarExtractor(it, it.parentFile.name).extract()
                    }
                }
            }
        }
    }
}