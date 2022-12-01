package eu.boxwork.dhbw.capturethecode.runner

import eu.boxwork.dhbw.capturethecode.model.GameGround
import java.util.concurrent.atomic.AtomicBoolean

class CompetitionRunner(
    private val gameGround: GameGround,
    private val delay: Int,
): Runnable {
    var run = AtomicBoolean(true)

    override fun run() {
        while (!gameGround.performRound() && run.get()) Thread.sleep(delay.toLong())
    }

    fun finish() {
        run.set(false)
    }
}