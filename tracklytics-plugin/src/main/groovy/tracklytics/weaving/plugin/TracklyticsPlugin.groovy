package tracklytics.weaving.plugin

import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

class TracklyticsPlugin implements Plugin<Project> {

  @Override
  void apply(Project project) {

    project.dependencies {
      implementation 'org.aspectj:aspectjrt:1.8.10'
      implementation 'com.orhanobut.tracklytics:tracklytics-runtime:2.1.0'
    }

    project.android.applicationVariants.all { variant ->
      JavaCompile javaCompile
      if (variant.hasProperty('javaCompileProvider')) {
        // Android 3.3.0+
        javaCompile = variant.javaCompileProvider.get()
      } else {
        javaCompile = variant.javaCompile
      }

      // Gets the variant name and capitalize the first character
      def variantName = variant.name[0].toUpperCase() + variant.name[1..-1].toLowerCase()

      // Weave the binary for the actual code
      // CompileSources task is invoked after java and kotlin compilers and copy kotlin classes
      // That's the moment we have the finalized byte code and we can weave the aspects
      project.tasks.findByName('compile' + variantName + 'Sources')?.doLast {
        def destinationDir = javaCompile.destinationDir.toString()
        def classPath = javaCompile.classpath.asPath
        def bootClassPath = project.android.bootClasspath.join(File.pathSeparator)
        String[] args = [
                "-showWeaveInfo",
                "-1.7",
                "-inpath", destinationDir,
                "-aspectpath", classPath,
                "-d", destinationDir,
                "-classpath", classPath,
                "-bootclasspath", bootClassPath
        ]
        new Main().run(args, new MessageHandler(true));
        println("----------------------------------------------")
        println("--------------Tracklytics Weave---------------")
        println("----------------------------------------------")
        println("destinationDir: $destinationDir")
        println("classPath: $classPath")
        println("bootClassPath: $bootClassPath")
        println("----------------------------------------------")
      }

      // Weave the binary for unit tests
      // compile unit tests task is invoked after the byte code is finalized
      // This is the time that we can weave the aspects onto byte code
      project.tasks.findByName('compile' + variantName + 'UnitTestSources')?.doLast {
        def destinationDir = javaCompile.destinationDir.toString()
        def classPath = javaCompile.classpath.asPath
        def bootClassPath = project.android.bootClasspath.join(File.pathSeparator)
        String[] args = [
                "-showWeaveInfo",
                "-1.7",
                "-inpath", destinationDir,
                "-aspectpath", classPath,
                "-d", destinationDir,
                "-classpath", classPath,
                "-bootclasspath", bootClassPath
        ]
        new Main().run(args, new MessageHandler(true));
        println("----------------------------------------------")
        println("--------------Tracklytics Weave---------------")
        println("----------------------------------------------")
        println("destinationDir: $destinationDir")
        println("classPath: $classPath")
        println("bootClassPath: $bootClassPath")
        println("----------------------------------------------")
      }
    }
  }
}