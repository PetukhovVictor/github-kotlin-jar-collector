package org.jetbrains.githubkotlinjarcollector.collection

import com.googlecode.d2j.dex.Dex2jar
import com.googlecode.dex2jar.tools.Dex2jarCmd
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile


class JarExtractor(private val file: File, private val repo: String) {
    private val buf = ByteArray(1024)
    private val jarsDir = "jars"

    private fun checkAndCopyJar() {
        if (relevantJarCheck(file.name)) {
            val pathFolder = "${file.parent}/$jarsDir"
            File(pathFolder).mkdirs()
            Files.copy(file.toPath(), File("$pathFolder/${file.name}").toPath())
            println("SELECTED ${file.name} jar ($repo)")
        } else {
            println("NOT SELECTED ${file.name} jar ($repo)")
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

    private fun relevantJarCheck(filename: String): Boolean {
        return filename.startsWith(repo, ignoreCase = true) && !filename.contains("-sources", ignoreCase = true)
    }

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
                selectRelevantJars(jarCanidates).forEach {
                     copyFromZip(File("$pathFolder/${it.name}"), zipArchive, it)
                }
            }
        }
    }

    private fun extractFromApk() {
        val pathFolder = File("${file.parent}/$jarsDir")
        pathFolder.mkdirs()
        Dex2jar.from(file).to(File("$pathFolder/${file.name}.jar").toPath())
        println("DEX2JAR SUCCESSFUL: $file")
    }

    fun extract() {
        when (file.extension) {
            "zip" -> extractFromZip()
            "apk" -> extractFromApk()
            "jar" -> checkAndCopyJar()
            else -> {
                println("UNKNOWN ASSET FORMAT: $file")
            }
        }
    }
}