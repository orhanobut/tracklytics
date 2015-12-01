package tracklytics.weaving.plugin

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.fail

public class TracklyticsPluginTest {

  @Test public void greeterPluginAddsGreetingTaskToProject() {
    try{
      Project project = ProjectBuilder.builder().build()
      project.pluginManager.apply 'com.orhanobut.tracklytics'
      fail()
    } catch (Exception e){
      assertEquals(e.getMessage(), "com.android.application or com.android.library plugin required.")
    }
  }
}