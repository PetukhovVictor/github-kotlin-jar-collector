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
    private val repoAssets: MutableList<String> = mutableListOf()

    private fun download(url: String, folder: String, filename: String = FilenameUtils.getName(URL(url).path), isAsset: Boolean = false) {
        val timeLogger = TimeLogger(task_name = "DOWNLOAD ASSET '$filename'")

        File("$assetsDirectory/$folder").mkdirs()
        val response = khttp.get(url)
        val path = "$assetsDirectory/$folder/$filename"
        File(path).writeBytes(response.content)
        if (isAsset) {
            repoAssets.add(path)
        }

        timeLogger.finish()
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

    fun getRepository(name: String): GHRepository {
        return githubApi.getRepository(name)
    }

    fun assetsCollect(repo: GHRepository, subDirectory: String = ""): Boolean {
        val path = "${repo.ownerName}/${repo.name}/$subDirectory"
        val assets: Assets = mutableListOf()

        if (repo.latestRelease == null) {
            return false
        }

        repo.latestRelease.assets.forEach {
            assets.add(it.browserDownloadUrl)
        }
        if (assets.size != 0) {
            assets.map { download(it, path, isAsset = true) }
        }

        return assets.size != 0
    }

    private fun repoCollect(user: String?) {
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
                    val isCollected = assetsCollect(it.value)
                    if (isCollected) {
                        download(latestRelease.zipballUrl, "$path/sources", "${it.value.ownerName}:${it.value.name}.${GithubAssetsType.ZIP.ext}")
                    }
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

    private fun repoCollect() {
        repoCollect(user = null)
    }

    fun collect(type: GithubAssetsCollectorType) {
        when (type) {
            GithubAssetsCollectorType.DIRECT -> repoCollect()
            GithubAssetsCollectorType.VIA_USERS -> usersCollect()
        }
    }
}