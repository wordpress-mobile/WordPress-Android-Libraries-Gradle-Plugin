package org.wordpress

import org.ajoberstar.grgit.Grgit

import org.gradle.api.Plugin
import org.gradle.api.Project

class WordPressLibraries implements Plugin<Project> {
    def File libDir

    void apply(Project prj) {
        libDir = new File("${prj.rootDir}/libs")
        def libraries = prj.container(Library)

        prj.afterEvaluate {
            libraries.each { lib ->
                if (!isValidLibrary(lib)) {
                    prj.logger.warn("WARNING: $libDir/$lib.name is not a valid library directory, " +
                        "you won't be able to build the DEBUG configuration.\n" +
                        "WARNING: It'll cause a \"Configuration with name 'default' not found\" error.")
                }

                // set the source dependencies
                def dep = ":libs:$lib.name:$lib.subproject"
                prj.logger.lifecycle "$prj.name debug using local source for $lib.name $dep"
                prj.logger.lifecycle "$prj.name release using artifact for $lib.name $lib.artifact"

                // HACK: force libraries to build using `debug` buildType when building from source
                prj.android {
                    defaultPublishConfig 'debug'
                }

                prj.dependencies {
                    debugCompile project(path:dep)
                    releaseCompile lib.artifact
                }
            }
        }

        prj.task('createWordPressLibraryDirectory') << {
            libDir.mkdirs()
        }

        prj.task('cloneWordPressLibraries', dependsOn:'createWordPressLibraryDirectory') {
            doLast {
                libraries.each { library ->
                    def dir = localDirectory(library)
                    def uri = library.repo
                    if (!isValidLibrary(library)) {
                        prj.logger.info "Setting up WordPress library $library.name from $uri"
                        def repo = Grgit.clone(dir:dir, uri:uri)
                    } else {
                        prj.logger.info "WordPress library $library.name building from source at $dir"
                    }
                }
            }
        }

        prj.extensions.wordpress = libraries
    }

    /**
     * Check the library directory exists and contains a build.gradle file
     */
    def boolean isValidLibrary(Library library) {
        File gradleBuildFile = new File("$libDir/$library.name/build.gradle")
        return gradleBuildFile.exists()
    }

    def File localDirectory(Library library) {
        return new File("$libDir/$library.name")
    }
}

class Library {
    final String name
    String repo
    String subproject
    String artifact

    Library(String name) {
        this.name = name
    }

    def boolean isLocal() {
    }

    def subproject(String name) {
        subproject = name
    }

    def artifact(String name) {
        artifact = name
    }

    def repo(String name) {

        // if no "/" then assume in WordPres-Mobile
        if (!name.contains("/")) {
            name = "WordPress-Mobile/$name"
        }

        if (!name.endsWith(".git")) {
            name = "${name}.git"
        }

        if (!name.startsWith("https://github.com/")) {
            name = "https://github.com/$name"
        }

        this.repo = name
    }
}
