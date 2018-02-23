package org.jetbrains.githubkotlinjarcollector.collection

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile


class GithubJarExtractor(private val file: File) {
    var buf = ByteArray(1024)

    fun extract() {
        if (file.extension == "zip") {
            val zf = ZipFile(file)
            zf.use { zipFile ->
                val zipEntries = zipFile.entries()
                while (zipEntries.hasMoreElements()) {
                    val zipEntry = zipEntries.nextElement() as ZipEntry
                    val path = "./assets_extracted/${zipEntry.name}"
                    val file = File(path)

                    if (file.extension != "jar") {
                        continue
                    }

                    File(file.parent).mkdirs()
                    val fileoutputstream = FileOutputStream(path)
                    val inp: InputStream = zf.getInputStream(zipEntry)
                    var n: Int = inp.read(buf, 0, 1024)
                    while (n > -1) {
                        fileoutputstream.write(buf, 0, n)
                        n = inp.read(buf, 0, 1024)
                    }
                }
            }
        }
    }
}