package org.jetbrains.githubkotlinjarcollector

import com.xenomachina.argparser.ArgParser

fun main(args : Array<String>) {
    val parser = ArgParser(args)
    val packagesPath by parser.storing("-d", "--directory", help="path to folder with package files (jar, jar zipped or apk)")
    val stage by parser.mapping("--collecting" to Stage.COLLECTING, "--extracting" to Stage.EXTRACTING, help = "stage (--collecting or --extracting)")

    Runner.run(stage, packagesPath)
}