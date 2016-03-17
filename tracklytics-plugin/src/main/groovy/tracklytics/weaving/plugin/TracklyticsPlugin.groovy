package tracklytics.weaving.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

class TracklyticsPlugin implements Plugin<Project> {

  @Override void apply(Project project) {
    def hasApp = project.plugins.withType(AppPlugin)
    def hasLib = project.plugins.withType(LibraryPlugin)
    if (!hasApp && !hasLib) {
      throw new IllegalStateException("com.android.application or com.android.library plugin required.")
    }

    final def log = project.logger
    final def variants
    if (hasApp) {
      variants = project.android.applicationVariants
    } else {
      variants = project.android.libraryVariants
    }

    project.extensions.create("tracklytics", TracklyticsExtension)
    project.task('startTracklytics') << {

      // Fabric
      if (project.tracklytics.fabric) {
        project.repositories {
          maven { url 'https://maven.fabric.io/public' }
        }
        project.apply(plugin: 'io.fabric')
      }

      project.dependencies {
        compile 'org.aspectj:aspectjrt:1.8.6'
        compile 'com.orhanobut.tracklytics:tracklytics-runtime:0.25-SNAPSHOT@aar'

        // Fabric
        if (project.tracklytics.fabric) {
          compile('com.crashlytics.sdk.android:crashlytics:2.5.2@aar') {
            transitive = true;
          }
        }

        // mixpanel
        if (project.tracklytics.mixpanel) {
          compile('com.mixpanel.android:mixpanel-android:4.6.4') {
            transitive = true;
          }
        }
      }

      variants.all { variant ->
        JavaCompile javaCompile = variant.javaCompile
        javaCompile.doLast {
          String[] args = [
              "-showWeaveInfo",
              "-1.5",
              "-inpath", javaCompile.destinationDir.toString(),
              "-aspectpath", javaCompile.classpath.asPath,
              "-d", javaCompile.destinationDir.toString(),
              "-classpath", javaCompile.classpath.asPath,
              "-bootclasspath", project.android.bootClasspath.join(File.pathSeparator)
          ]
          log.debug "ajc args: " + Arrays.toString(args)

          MessageHandler handler = new MessageHandler(true);
          new Main().run(args, handler);
          for (IMessage message : handler.getMessages(null, true)) {
            switch (message.getKind()) {
              case IMessage.ABORT:
              case IMessage.ERROR:
              case IMessage.FAIL:
                log.error message.message, message.thrown
                break;
              case IMessage.WARNING:
                log.warn message.message, message.thrown
                break;
              case IMessage.INFO:
                log.info message.message, message.thrown
                break;
              case IMessage.DEBUG:
                log.debug message.message, message.thrown
                break;
            }
          }
        }
      }
    }
  }
}