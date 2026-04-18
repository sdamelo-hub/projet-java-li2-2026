package ProjetUniv_scheduler;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Point d'entrée JavaFX.
 *
 * FLUX CORRIGÉ :
 *  1. Charge le FXML → crée MainController (initialize() s'exécute mais
 *     on override la scène juste après via LoginController.show())
 *  2. Prépare la mainScene SANS l'afficher
 *  3. Passe mainScene au LoginController → c'est lui qui gère l'affichage
 *  4. Après auth réussie, LoginController bascule primaryStage sur mainScene
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Chargement FXML (déclenche initialize() dans MainController)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
        Parent mainRoot = loader.load();
        MainController mainController = loader.getController();

        // 2. Scène principale prête mais non affichée
        Scene mainScene = new Scene(mainRoot, 1280, 800);
        primaryStage.setTitle("UNIV-SCHEDULER — Infrastructure Control");

        // 3. Le LoginController prend le contrôle du stage
        LoginController loginController = new LoginController(primaryStage, mainController, mainScene);
        loginController.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
