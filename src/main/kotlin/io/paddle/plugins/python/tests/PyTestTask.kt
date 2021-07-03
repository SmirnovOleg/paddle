package io.paddle.plugins.python.tests

import io.paddle.project.Project
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.Hashable
import io.paddle.utils.hashable

class PyTestTask(project: Project) : IncrementalTask(project) {
    override val id: String = "test"

    override val inputs: List<Hashable> =
        project.roots.sources.map { it.hashable() } + project.roots.tests.map { it.hashable() } +
            listOf(project.requirements, project.environment.venv.hashable())

    override val dependencies: List<Task> = listOf(project.tasks.getOrFail("venv"))

    override fun act() {
        var anyFailed = false
        for (file in project.roots.tests) {
            val code = project.environment.runModule("pytest", listOf(file.absolutePath))
            anyFailed = anyFailed || code != 0
        }
        if (anyFailed) throw ActException("PyTest tests has failed")
    }
}