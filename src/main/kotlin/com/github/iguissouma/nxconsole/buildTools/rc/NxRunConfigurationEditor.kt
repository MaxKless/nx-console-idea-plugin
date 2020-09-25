package com.github.iguissouma.nxconsole.buildTools.rc

import com.github.iguissouma.nxconsole.buildTools.NxRunSettings
import com.github.iguissouma.nxconsole.buildTools.NxService
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterField
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.javascript.nodejs.util.NodePackageField
import com.intellij.lang.javascript.buildTools.base.ComponentWithEmptyBrowseButton
import com.intellij.lang.javascript.buildTools.base.JsbtTaskFetchException
import com.intellij.lang.javascript.buildTools.base.JsbtUtil
import com.intellij.lang.javascript.buildTools.grunt.beforeRun.GruntBeforeRunTaskDialog
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.options.ex.SingleConfigurableEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.PopupMenuListenerAdapter
import com.intellij.ui.TextFieldWithHistory
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton
import com.intellij.util.execution.ParametersListUtil
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.SwingHelper
import com.intellij.webcore.ui.PathShortener
import java.util.function.Supplier
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JSeparator
import javax.swing.event.PopupMenuEvent

class NxRunConfigurationEditor(val project: Project) : SettingsEditor<NxRunConfiguration>() {

    private val nxJsonField: TextFieldWithHistoryWithBrowseButton = createNxFileFieldWithBrowseButton(project)
    private var nodeInterpreterField: NodeJsInterpreterField = NodeJsInterpreterField(project, false)
    private val nxPackageField: NodePackageField = NodePackageField(nodeInterpreterField, "nx")
    private val tasksField: TextFieldWithHistory = createTasksField(project, this.nxJsonField)
    private val panel: JPanel = FormBuilder.createFormBuilder()
        .setAlignLabelOnRight(false).setHorizontalGap(10).setVerticalGap(4)
        .addLabeledComponent("&nx.json:", nxJsonField)
        .addLabeledComponent("&Tasks:", ComponentWithEmptyBrowseButton.wrap<TextFieldWithHistory>(this.tasksField))
        .addComponent(JSeparator(), 8)
        .addLabeledComponent("Node &interpreter:", this.nodeInterpreterField, 8)
        .addLabeledComponent("&Package nx-cli:", this.nxPackageField)
        .panel

    init {
        PathShortener.enablePathShortening(this.nxJsonField.childComponent.textEditor, null)
    }

    private fun createTasksField(
        project: Project,
        nxJsonField: TextFieldWithHistoryWithBrowseButton
    ): TextFieldWithHistory {

        val field = createTasksField(
            project
        ) { PathShortener.getAbsolutePath(nxJsonField.childComponent.textEditor) }

        return field
    }

    private fun createTasksField(project: Project, nxJsonPath: Supplier<String>): TextFieldWithHistory {
        val field = TextFieldWithHistory()
        field.setMinimumAndPreferredWidth(0)
        field.setHistorySize(-1)
        JsbtUtil.enableExpandingWithLazyHistoryLoading(field)
        val lastNxJsonRef = Ref.create<VirtualFile>()
        field.addPopupMenuListener(
            object : PopupMenuListenerAdapter() {
                override fun popupMenuWillBecomeVisible(e: PopupMenuEvent) {
                    val nxJson = LocalFileSystem.getInstance().findFileByPath((nxJsonPath.get() as String)!!)
                    if (nxJson != null && nxJson.isValid && !nxJson.isDirectory) {
                        if (nxJson != lastNxJsonRef.get()) {
                            lastNxJsonRef.set(nxJson)
                            try {
                                val structure =
                                    NxService.getInstance(this@NxRunConfigurationEditor.project).fetchBuildfileStructure(
                                        nxJson
                                    )
                                SwingHelper.setHistory(field, JsbtUtil.encodeNames(structure.taskNames), false)
                            } catch (var4: JsbtTaskFetchException) {
                                SwingHelper.setHistory(field, emptyList(), false)
                            }
                        }
                    } else {
                        SwingHelper.setHistory(field, emptyList(), false)
                        lastNxJsonRef.set(null)
                    }
                }
            }
        )

        return field
    }

    override fun resetEditorFrom(nxRc: NxRunConfiguration) {
        resetEditorFrom(nxRc.runSettings)
    }

    override fun applyEditorTo(nxRunConfiguration: NxRunConfiguration) {
        nxRunConfiguration.runSettings = getCurrentSettings()
    }

    override fun createEditor(): JComponent {
        return this.panel
    }

    private fun resetEditorFrom(settings: NxRunSettings) {
        if (settings.interpreterRef != null) {
            nodeInterpreterField.interpreterRef = settings.interpreterRef
        }
        if (settings.nxFileSystemIndependentPath != null) {
            this.nxJsonField.setTextAndAddToHistory(FileUtil.toSystemDependentName(settings.nxFileSystemIndependentPath!!))
        }

        val defaultPackage =
            NodePackage.findDefaultPackage(project, "nx", NodeJsInterpreterRef.createProjectRef().resolve(project))
        if (defaultPackage != null) {
            this.nxPackageField.selected = defaultPackage
        }
        tasksField.setTextAndAddToHistory(ParametersListUtil.join(settings.tasks))

        val dialogWrapper = DialogWrapper.findInstance(this.panel)
        if (dialogWrapper is SingleConfigurableEditor || dialogWrapper is GruntBeforeRunTaskDialog) {
            this.nodeInterpreterField.setPreferredWidthToFitText()
            this.nxPackageField.setPreferredWidthToFitText()
            SwingHelper.resizeDialogToFitTextFor(this.nxJsonField)
        }
    }

    private fun getCurrentSettings(): NxRunSettings {
        val tasksText: String = this.tasksField.text
        var tasks: List<String> = emptyList()
        if (!StringUtil.isEmptyOrSpaces(tasksText)) {
            tasks = ParametersListUtil.parse(tasksText)
        }
        return NxRunSettings(
            interpreterRef = nodeInterpreterField.interpreterRef,
            nxFilePath = PathShortener.getAbsolutePath(this.nxJsonField.childComponent.textEditor),
            tasks = tasks
        )
    }

    private fun createNxFileFieldWithBrowseButton(project: Project): TextFieldWithHistoryWithBrowseButton {
        return SwingHelper.createTextFieldWithHistoryWithBrowseButton(
            project,
            "Select nx.json",
            FileChooserDescriptorFactory.createSingleLocalFileDescriptor()
        ) {
            NxService.getInstance(project).detectAllBuildfiles().map {
                FileUtil.toSystemDependentName(it.path)
            }.sorted()
        }
    }
}
