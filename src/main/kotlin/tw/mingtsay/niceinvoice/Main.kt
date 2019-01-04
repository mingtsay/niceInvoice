package tw.mingtsay.niceinvoice

import javafx.application.Application
import javafx.application.Platform
import javafx.stage.Stage
import tw.mingtsay.niceinvoice.model.State

class Main : Application() {

    override fun start(primaryStage: Stage) {
        Platform.setImplicitExit(true)
        state = State.load()

        MainController.stage.show()
    }

    override fun stop() {
        state.save()
    }

    companion object {
        lateinit var state: State
    }
}
