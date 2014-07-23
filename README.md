# WordPress Android Gradle Library Plugin

The WordPress Android app is being split into Android Libraries. This plugin is to aid developers who want to modify the libraries while working on the main WordPress application.

This plugin can be used for both the [main WordPress project][app-gradle] as well as libraries that depend on other libraries (e.g. [WordPressNetworking][net-gradle]).

## Usage

Add the `buildscript` dependency to the root `build.gradle` and then apply the plugin to all projects.


```groovy
buildscript {
    dependency {
        classpath 'com.automattic.android:gradle-wordpresslibraries:1.1.+'
    }
}

allprojects {
    apply plugin:'wordpress'
}
```

In the `build.gradle` for your specific project (e.g. `WordPressNetworking`) declare the libraries the project depends on:

```groovy
wordpress {
    utils {
        repo 'WordPress-Utils-Android'
        subproject 'WordPressUtils'
        artifact 'org.wordpress:wordpress-utils:1.0.+'
    }
    wpcomrest {
        repo 'Automattic/android-wordpress-com-rest'
        subproject 'WordPressComRest'
        artifact 'com.automattic:wordpresscom-rest:1.0.0'
    }
}
```

## Configuring a library

The `repo` can be a full URL to a git repository. For convenience it will expand repo names following this pattern:

1. If there is no slash append `.git` and prepend `https://github.com/WordPress-Mobile`
2. If there is a slash append `.git` and prepend `https://github.com/`

So using `WordPress-Utils-Android` expands to `https://github.com/WordPress-Mobile/WordPress-Utils-Android.git`.

The `subproject` is the gradle project to link to within the repository.

The `artifact` is the artificat to use when the library is not cloned locally.

The other step you must perform to configure a library is to declare the correct `include` in `settings.gradle`. For example, the `utils` item in the `wordpress` block with a subproject of `WordPressUtils` will need to have this `include` declared:

```groovy

include ':libs:utils:WordPressUtils'

```

## Building from local sources

To checkout all the projects locally run the `cloneWordPressLibraries` task.

    gradle cloneWordPressLibraries

This task will create a `libs` directory in the root project directory and clone each repository here. WordPress libraries declared within other libraries will also be cloned here (e.g. `WordPressNetworking` declares the `WordPressUtils` library so it will reuse the library already declared by the main `WordPress` project).

Now when building you will see notices that the projects are building from source:

```
WordPress using local source for :libs:networking:WordPressNetworking
WordPress using local source for :libs:utils:WordPressUtils
WordPressNetworking using local source for :libs:utils:WordPressUtils
WordPressNetworking using local source for :libs:wpcomrest:WordPressComRest
```

## Hack this plugin

Start by copying our example `gradle.properties` and change default values to match your configuration:

    $ cp WordPressLibraries/gradle.properties-example WordPressLibraries/gradle.properties

