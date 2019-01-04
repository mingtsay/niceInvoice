package tw.mingtsay.niceinvoice

import javafx.application.Application
import javafx.application.Platform
import javafx.stage.Stage
import tw.mingtsay.niceinvoice.model.State
import java.util.logging.Level
import java.util.logging.Logger

class Main : Application() {

    override fun start(primaryStage: Stage) {
        logger.log(Level.INFO, "App starting…")

        Platform.setImplicitExit(true)
        state = State.load()

        MainController.stage.show()
    }

    override fun stop() {
        logger.log(Level.INFO, "App stopping…")

        state.save()
    }

    companion object {
        lateinit var state: State
        val logger = Logger.getLogger(Main::class.java.name)!!
    }
}
