package org.jetbrains.githubkotlinjarcollector.collection

import org.apache.commons.io.FilenameUtils
import org.jetbrains.githubkotlinjarcollector.helpers.TimeLogger
import org.kohsuke.github.*
import java.io.File
import java.net.URL

typealias Assets = MutableList<String>

enum class GithubAssetsCollectorType {
    DIRECT, VIA_USERS
}

class GithubAssetsCollector(private val assetsDirectory: String) {
    private val language = "kotlin"
    private val githubApi = GitHub.connect()

    val repoAssets: MutableList<String> = mutableListOf()

    private fun download(url: String, folder: String, filename: String = FilenameUtils.getName(URL(url).path), isAsset: Boolean = false) {
        File("$assetsDirectory/$folder").mkdirs()
        khttp.async.get(url) {
            val path = "$assetsDirectory/$folder/$filename"
            File(path).writeBytes(content)
            if (isAsset) {
                repoAssets.add(path)
            }
            println("DOWNLOADED: $folder/$filename")
        }
    }

    private fun usersCollect() {
        val searchBuilder = githubApi.searchUsers()
                .language(language)
                .order(GHDirection.DESC)
        val userList = searchBuilder.list()
        val timeLogger = TimeLogger("User repo collecting")
        var errors = 0

        userList.withIndex().forEach {
            val username = it.value.login
            try {
                repoCollect(username)
            } catch (e: GHException) {
                println("ERROR: $e")
                errors++
            }
            println("VIEWED USER (${it.index + 1} out of ${userList.totalCount}): $username")
        }

        timeLogger.finish(fullFinish = true)
        println("Errors: $errors")
    }

    private fun assetsCollect(repo: GHRepository) {
        val path = "${repo.ownerName}/${repo.name}"
        val assets: Assets = mutableListOf()

        repo.latestRelease.assets.forEach {
            assets.add(it.browserDownloadUrl)
        }
        if (assets.size != 0) {
            assets.map { download(it, path, isAsset = true) }
            download(repo.latestRelease.zipballUrl, "$path/sources","${repo.ownerName}:${repo.name}.${GithubAssetsType.ZIP.ext}")
        }
    }

    fun repoCollect(user: String?) {
        val timeLogger = TimeLogger("Assets collecting")
        val searchBuilder = githubApi.searchRepositories()
                .language(language)
                .order(GHDirection.ASC)
                .sort(GHRepositorySearchBuilder.Sort.STARS)
        if (user != null) {
            searchBuilder.user(user)
        }
        val repoList = searchBuilder.list()
        var errors = 0

        repoList.withIndex().forEach {
            try {
                val latestRelease = it.value.latestRelease
                val path = "${it.value.ownerName}/${it.value.name}"

                if (latestRelease != null && latestRelease.assets.size != 0) {
                    assetsCollect(it.value)
                }
                println("VIEWED REPO (${it.index + 1} out of ${repoList.totalCount}): $path")
            } catch (e: HttpException) {
                println("ERROR: $e")
                errors++
            }
        }

        timeLogger.finish(fullFinish = true)
        println("${repoAssets.size} collected, errors: $errors")
    }

    fun repoCollect() {
        repoCollect(user = null)
    }

    fun collect(type: GithubAssetsCollectorType) {
        when (type) {
            GithubAssetsCollectorType.DIRECT -> repoCollect()
            GithubAssetsCollectorType.VIA_USERS -> usersCollect()
        }
    }
}