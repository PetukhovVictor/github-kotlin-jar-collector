# github-kotlin-jar-collector
Collection and extraction JAR assets (also jar's inside zip or apk) from GitHub

## Program use

The program collects repo assets via GitHub API (using [kohsuke/github-api](https://github.com/kohsuke/github-api)). It's collecting stage.

On the extracting stage the program unpack zip and apk archives and extracts from them of jars.
Program use modified [Dex2Jar](https://github.com/pxb1988/dex2jar) for decompiling dex files in APK archives to jars.

### Program arguments

* `-i` or `--input`: path to folder, in which will be written asstes and sources (if this is a collecting stage) or unziped jars (from zip, apk or just copied jars);
* `--collecting`: collecting stage: collecting repo assets via GitHub API (direct or by users - it is specified in Runner object);
* `--extracting`: extracting stage: unpacking zip and apk archives and extracts from them of jars (or just copying jars).

### How to run

To run program you must run `main` function in `main.kt`, not forgetting to set the program arguments.

Also you can run jar file (you can download from the [release assets](https://github.com/PetukhovVictor/github-kotlin-jar-collector/releases)):
```
java -jar ./github-kotlin-jar-collector-0.1.jar -i ./assets --collecting
```
