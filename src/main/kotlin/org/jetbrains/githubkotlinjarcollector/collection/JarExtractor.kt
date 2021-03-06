package org.jetbrains.githubkotlinjarcollector.collection

import com.googlecode.d2j.dex.Dex2jarModified
import org.jetbrains.githubkotlinjarcollector.helpers.TimeLogger
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipFile


class JarExtractor(private val file: File, private val repo: String) {
    private val buf = ByteArray(1024)
    private val jarsDir = "jars"

    private fun checkAndMoveJar() {
        val pathFolder = "${file.parent}/$jarsDir"
        val fileTargetPath = File("$pathFolder/${file.name}").toPath()

        File(pathFolder).mkdirs()
        if (!Files.exists(fileTargetPath)) {
            Files.move(file.toPath(), fileTargetPath)
        }
    }

    private fun copyFromZip(path: File, zipFile: ZipFile, zipEntry: ZipEntry) {
        File(path.parent).mkdirs()
        val fileoutputstream = FileOutputStream(path)
        val inp: InputStream = zipFile.getInputStream(zipEntry)
        var n: Int = inp.read(buf, 0, 1024)
        while (n > -1) {
            fileoutputstream.write(buf, 0, n)
            n = inp.read(buf, 0, 1024)
        }
    }

    /**
     * @deprecated Filtering of jars implemented in github-kotlin-repo-collector
     */
    private fun relevantJarCheck(filename: String): Boolean {
        return filename.startsWith(repo, ignoreCase = true) && !filename.contains("-sources", ignoreCase = true)
    }

    /**
     * @deprecated Filtering of jars implemented in github-kotlin-repo-collector
     */
    private fun selectRelevantJars(jarsCandidates: MutableList<ZipEntry>): MutableList<ZipEntry> {
        val jars: MutableList<ZipEntry> = mutableListOf()

        jarsCandidates.forEach {
            if (relevantJarCheck(File(it.name).name)) {
                jars.add(it)
            }
        }

        if (jars.size != 0) {
            println("SELECTED from ${jarsCandidates.size} jar candidates ($repo): $jars")
        } else {
            println("NOTHING SELECTED from $jarsCandidates ($repo)")
        }

        return jars
    }

    private fun extractFromZip() {
        val zipArchive = ZipFile(file)
        val pathFolder = "${file.parent}/$jarsDir"

        File(pathFolder).mkdirs()
        zipArchive.use { zipFile ->
            val zipEntries = zipFile.entries()
            val jarCanidates: MutableList<ZipEntry> = mutableListOf()
            while (zipEntries.hasMoreElements()) {
                val zipEntry = zipEntries.nextElement()
                val path = "$pathFolder/${zipEntry.name}"
                val currentFile = File(path)

                if (currentFile.extension == "jar") {
                    jarCanidates.add(zipEntry)
                }
            }
            if (jarCanidates.size != 0) {
                jarCanidates.forEach {
                     copyFromZip(File("$pathFolder/${it.name}"), zipArchive, it)
                }
            }
        }
    }

    private fun extractFromApk() {
        val pathFolder = "${file.parent}/$jarsDir"
        val pathJar = File("$pathFolder/${file.name}.jar").toPath()

        File(pathFolder).mkdirs()
        if (Files.exists(pathJar)) {
            Files.delete(pathJar)
        }

        try {
            Dex2jarModified.from(file).to(pathJar)
        } catch (e: IOException) {
            println("DEX2JAR FAILED (not consist .dex): $file")
        } catch (e: Exception) {
            println("DEX2JAR FAILED ($e): $file")
        }
    }

    fun extract() {
        val timeLogger = TimeLogger(task_name = "JARS EXTRACTION FROM '$file'")

        when (file.extension) {
            "zip" -> extractFromZip()
            "apk" -> extractFromApk()
            "jar" -> checkAndMoveJar()
            else -> {
                println("UNKNOWN ASSET FORMAT: $file")
            }
        }

        timeLogger.finish()
    }
}