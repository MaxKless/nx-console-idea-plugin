package com.github.iguissouma.nxconsole.buildTools.rc

import com.github.iguissouma.nxconsole.NxBundle
import com.github.iguissouma.nxconsole.buildTools.NxRunSettings
import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionResult
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.ui.ConsoleView
import com.intellij.javascript.debugger.CommandLineDebugConfigurator
import com.intellij.javascript.nodejs.NodeCommandLineUtil
import com.intellij.javascript.nodejs.NodeConsoleAdditionalFilter
import com.intellij.javascript.nodejs.NodeStackTraceFilter
import com.intellij.javascript.nodejs.debug.NodeLocalDebuggableRunProfileStateSync
import com.intellij.javascript.nodejs.interpreter.NodeCommandLineConfigurator
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter
import com.intellij.javascript.nodejs.npm.NpmUtil
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.javascript.buildTools.TypeScriptErrorConsoleFilter
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.EnvironmentUtil
import com.intellij.util.execution.ParametersListUtil
import com.intellij.webcore.util.CommandLineUtil
import java.io.File
import java.nio.charset.StandardCharsets

class NxRunProfileState(
    val environment: ExecutionEnvironment,
    val runSettings: NxRunSettings,
    val nxPackage: NodePackage
) : NodeLocalDebuggableRunProfileStateSync() {

    override fun executeSync(configurator: CommandLineDebugConfigurator?): ExecutionResult {
        val nodeInterpreter: NodeJsInterpreter =
            this.runSettings.interpreterRef.resolveNotNull(this.environment.project)
        val commandLine = NodeCommandLineUtil.createCommandLine(true)
        val envData = this.runSettings.envData
        val npmPackageRef = this.runSettings.packageManagerPackageRef
        val project = this.environment.getProject()
        NodeCommandLineUtil.configureCommandLine(
            commandLine,
            configurator
        ) { debugMode: Boolean? ->
            this.configureCommandLine(
                commandLine, nodeInterpreter, npmPackageRef,
                project, envData
            )
        }
        val processHandler: ProcessHandler = NodeCommandLineUtil.createProcessHandler(commandLine, true)
        ProcessTerminatedListener.attach(processHandler)
        val console: ConsoleView = this.createConsole(processHandler, commandLine.workDirectory)
        console.attachToProcess(processHandler)
        return DefaultExecutionResult(console, processHandler)
    }

    private fun configureCommandLine(
        commandLine: GeneralCommandLine,
        nodeInterpreter: NodeJsInterpreter,
        npmPackageRef: NodePackageRef,
        project: Project,
        envData: EnvironmentVariablesData
    ) {

        commandLine.withCharset(StandardCharsets.UTF_8)
        CommandLineUtil.setWorkingDirectory(commandLine, File(this.runSettings.nxFilePath).parentFile, false)

        val pkg = NpmUtil.resolveRef(npmPackageRef, project, nodeInterpreter)
        if (pkg == null) {
            throw ExecutionException(
                NxBundle.message(
                    "nx.npm.dialog.message.cannot.resolve.package.manager",
                    npmPackageRef.identifier
                )
            )
        } else {
            if (NpmUtil.isYarnAlikePackage(pkg) || NpmUtil.isPnpmPackage(pkg)) {
                commandLine.addParameter(NpmUtil.getValidNpmCliJsFilePath(pkg, nodeInterpreter))
                commandLine.addParameter("nx")
            } else {
                commandLine.addParameter(getNxBinFile(nxPackage).absolutePath)
            }
        }

        val tasks = this.runSettings.tasks
        if (tasks.size > 1) {
            commandLine.addParameters("run-many")
            val target = tasks.first().substringAfter(":")
            commandLine.addParameter("--target=$target")
            commandLine.addParameters("--projects=" + tasks.joinToString(",") { it.substringBefore(":") })
        } else {
            commandLine.addParameters("run")
            commandLine.addParameters(tasks.firstOrNull() ?: "")
        }

        commandLine.addParameters(this.runSettings.arguments?.let { ParametersListUtil.parse(it) } ?: emptyList())
        envData.configureCommandLine(commandLine, true)
        NodeCommandLineUtil.configureUsefulEnvironment(commandLine)
        val nodeModuleBinPath =
            commandLine.workDirectory.path + File.separator + "node_modules" + File.separator + ".bin"
        val shellPath = EnvironmentUtil.getValue("PATH")
        val separator = if (SystemInfo.isWindows) ";" else ":"
        commandLine.environment["PATH"] =
            listOfNotNull(shellPath, nodeModuleBinPath).joinToString(separator = separator)
        NodeCommandLineConfigurator.find(nodeInterpreter).configure(commandLine)
    }

    private fun getNxBinFile(nxPackage: NodePackage): File {
        return File(nxPackage.systemDependentPath, "bin" + File.separator + "nx.js")
    }

    private fun createConsole(processHandler: ProcessHandler, cwd: File?): ConsoleView {
        val project: Project = this.environment.project
        val consoleView = NodeCommandLineUtil.createConsole(processHandler, project, false)
        consoleView.addMessageFilter(NodeStackTraceFilter(project, cwd))
        consoleView.addMessageFilter(NodeConsoleAdditionalFilter(project, cwd))
        consoleView.addMessageFilter(TypeScriptErrorConsoleFilter(project, cwd))
        return consoleView
    }
}
