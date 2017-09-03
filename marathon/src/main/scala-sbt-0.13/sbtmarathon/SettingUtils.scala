package sbtmarathon

import sbt._

object SettingUtils {

  def applySettings(state: State, settings: Seq[Setting[_]]): State = {
    val extracted = Project.extract(state)
    val projectRef = extracted.currentRef
    val session = Project.session(state)
    val appendableSettings = Load.transformSettings(
      Load.projectScope(projectRef),
      projectRef.build,
      extracted.rootProject,
      settings
    )
    SessionSettings.reapply(
      session.appendRaw(appendableSettings),
      state
    )
  }
}
