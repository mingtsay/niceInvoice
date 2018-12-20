package tw.mingtsay.niceinvoice

import javafx.application.Application
import javafx.stage.Stage

class Main : Application() {
    override fun start(primaryStage: Stage) {
        MainController.stage.show()
    }
}
