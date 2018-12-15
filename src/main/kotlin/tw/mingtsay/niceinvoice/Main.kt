package tw.mingtsay.niceinvoice

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

class Main : Application() {
    override fun start(primaryStage: Stage) {
        val root = FXMLLoader.load<Parent>(javaClass.getResource("/layouts/main.fxml"))

        primaryStage.run {
            scene = Scene(root)
            scene.stylesheets.add("fonts/font-face.css")
            show()
        }
    }
}
