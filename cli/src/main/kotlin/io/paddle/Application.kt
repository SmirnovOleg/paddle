package io.paddle

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import io.paddle.plugin.docker.DockerPlugin
import io.paddle.plugin.python.PythonPlugin
import io.paddle.plugin.standard.StandardPlugin
import io.paddle.project.Project
import io.paddle.tasks.Task
import io.paddle.terminal.TerminalUI
import io.paddle.terminal.CommandOutput
import java.io.File

class Paddle(private val project: Project) : CliktCommand() {
    val task by argument("task", "Use name of task")

    override fun run() {
        try {
            project.execute(task)
        } catch (e: Task.ActException) {
            return
        }
    }
}

fun main(args: Array<String>) {
    val file = File("paddle.yaml")
    if (!file.exists()) {
        TerminalUI(CommandOutput.Console).stderr("Can't find paddle.yaml in root")
        return
    }

    val project = Project.load(file).also {
        it.register(StandardPlugin)
        it.register(PythonPlugin)
        it.register(DockerPlugin)
    }

    Paddle(project).main(args)
}
