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
                if (isLocal(lib)) {
                    // set the source dependencies
                    def dep = ":libs:$lib.name:$lib.subproject"
                    prj.logger.lifecycle "Using local source for $dep"
                    prj.dependencies {
                        compile project(path:dep)
                    }
                } else {
                    // set the artifact dependency sine the source is not local
                    prj.logger.lifecycle "Using artifact for $lib.name"
                    prj.dependencies {
                        compile lib.artifact
                    }
                }
            }
        }

        prj.task('wordpressCreateLibDir') << {
            libDir.mkdirs()
        }

        prj.task('devSetup', dependsOn:'wordpressCreateLibDir') {
            doLast {
                libraries.each { library -> 
                    def dir = localDirectory(library)
                    def uri = library.repo
                    if (!isLocal(library)) {
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

    def boolean isLocal(Library library) {
        return localDirectory(library).isDirectory()
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