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
      compile 'org.aspectj:aspectjrt:1.8.10'
      compile 'com.orhanobut.tracklytics:tracklytics-runtime:2.0.0'
    }

    project.android.applicationVariants.all { variant ->
      JavaCompile javaCompile = variant.javaCompile
      String[] args = [
          "-showWeaveInfo",
          "-1.7",
          "-inpath", javaCompile.destinationDir.toString(),
          "-aspectpath", javaCompile.classpath.asPath,
          "-d", javaCompile.destinationDir.toString(),
          "-classpath", javaCompile.classpath.asPath,
          "-bootclasspath", project.android.bootClasspath.join(File.pathSeparator)
      ]
      // Gets the variant name and capitalize the first character
      def variantName = variant.name[0].toUpperCase() + variant.name[1..-1].toLowerCase()

      // Weave the binary for the actual code
      // CompileSources task is invoked after java and kotlin compilers and copy kotlin classes
      // That's the moment we have the finalized byte code and we can weave the aspects
      project.tasks.findByName('compile' + variantName + 'Sources')?.doLast {
        new Main().run(args, new MessageHandler(true));
        println("----------------------------------------------")
        println("--------------Tracklytics Weave---------------")
        println("----------------------------------------------")
      }

      // Weave the binary for unit tests
      // compile unit tests task is invoked after the byte code is finalized
      // This is the time that we can weave the aspects onto byte code
      project.tasks.findByName('compile' + variantName + 'UnitTestSources')?.doLast {
        new Main().run(args, new MessageHandler(true));
        println("----------------------------------------------")
        println("--------------Tracklytics Weave---------------")
        println("----------------------------------------------")
      }
    }
  }
}