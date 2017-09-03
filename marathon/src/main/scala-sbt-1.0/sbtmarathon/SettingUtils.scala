package sbtmarathon

import sbt._
import sbt.internal.SessionSettings

object SettingUtils {

  def applySettings(state: State, settings: Seq[Setting[_]]): State = { 
    val extracted = Project.extract(state)
    val projectRef = extracted.currentRef
    val scope = Scope(
      Select(projectRef),
      Scope.Global.config,
      Scope.Global.task,
      Scope.Global.extra
    )
    val appendableSettings = Project.transform(
      Scope.resolveScope(
        scope,
        projectRef.build,
        extracted.rootProject
      ),
      settings
    )
    SessionSettings.reapply(
      Project.session(state).appendRaw(appendableSettings),
      state
    )
  }
}
