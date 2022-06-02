package io.gitlab.arturbosch.detekt.idea.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.annotations.Tag

@State(
    name = "DetektProjectConfiguration",
    storages = [Storage("detekt.xml")]
)
class DetektConfigStorage : PersistentStateComponent<DetektConfigStorage> {

    @Tag
    var enableDetekt: Boolean = false

    @Tag
    var buildUponDefaultConfig: Boolean = true

    @Tag
    var treatAsError: Boolean = false


    override fun getState(): DetektConfigStorage = this

    override fun loadState(state: DetektConfigStorage) {
        this.enableDetekt = state.enableDetekt
        this.buildUponDefaultConfig = state.buildUponDefaultConfig
        this.treatAsError = state.treatAsError
    }

    companion object {

        /**
         * Get instance of [DetektConfigStorage] for given project.
         *
         * @param project the project
         */
        fun instance(project: Project): DetektConfigStorage =
            project.getService(DetektConfigStorage::class.java)
    }
}
