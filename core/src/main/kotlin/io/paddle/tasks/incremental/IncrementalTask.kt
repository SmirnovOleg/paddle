package io.paddle.tasks.incremental

import io.paddle.project.Project
import io.paddle.tasks.Task
import io.paddle.terminal.CommandOutput
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable

/**
 * Task that uses caches to make sure that it will not be re-executed each time.
 *
 * Note, that to make use of incremental caching you should define [inputs] and [outputs]]
 */
abstract class IncrementalTask(project: Project) : Task(project) {
    /** Input of the task that should be used during incrementallity check */
    open val inputs: List<Hashable> = emptyList()

    /** Output of the task that should be used during incrementallity check */
    open val outputs: List<Hashable> = emptyList()

    protected fun isUpToDate(): Boolean {
        if (inputs.isEmpty() && outputs.isEmpty()) {
            return false
        }
        return IncrementalCache(project).isUpToDate(id, inputs.hashable(), outputs.hashable())
    }

    override fun execute() {
        if (isUpToDate()) {
            project.terminal.commands.stdout(CommandOutput.Command.Task(id, CommandOutput.Command.Task.Status.UP_TO_DATE))
            return
        }

        super.execute()

        IncrementalCache(project).update(id, inputs.hashable(), outputs.hashable())
    }
}
