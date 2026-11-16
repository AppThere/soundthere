package app.loki.soundthere

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * `run` (the workspace's and the panels') executes a command by its own id. `engine.execute` is a method, for
 * `call`, not a command: `run("engine.execute", …)` fails with "unknown command" and does nothing. It was
 * written that way once and went unseen, because a screenshot does not show a button that did nothing.
 */
class RunCallsTest {
    @Test fun nothingRunsEngineExecuteAsACommand() {
        val wrong = Regex("""\brun(\?\.invoke)?\(\s*"engine\.execute"""")
        val found = File("src/commonMain").walkTopDown().filter { it.extension == "kt" }.flatMap { f ->
            f.readLines().mapIndexedNotNull { i, line -> if (wrong.containsMatchIn(line)) "${f.name}:${i + 1}" else null }
        }.toList()
        assertEquals(emptyList(), found, "run() takes a command id; use call(\"engine.execute\", …) for the method")
    }
}
