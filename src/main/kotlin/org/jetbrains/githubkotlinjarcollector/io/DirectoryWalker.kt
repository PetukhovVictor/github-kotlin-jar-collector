package org.jetbrains.githubkotlinjarcollector.io

import java.io.File

class DirectoryWalker(private val dirPath: String) {
    private fun walkDirectory(callback: (File) -> Unit) {
        val dir = File(dirPath)
        dir.walkTopDown().maxDepth(3).forEach {
            if (it.isFile) {
                callback(it)
            }
        }
    }

    fun run(callback: (File) -> Unit) {
        walkDirectory { file: File -> callback(file) }
    }
}