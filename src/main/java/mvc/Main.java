package mvc;

import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Creates a window that runs a simulation
 * of the critter world given user inputs.
 */
public class Main extends Application {

   public static void main(String[] args) {
      launch(args);
   }

   @Override
   public void start(Stage stage) {
      try {
    	 System.out.println(getClass().getResource("controller.fxml"));
         URL r = getClass().getResource("controller.fxml");
         if (r == null) throw new Exception("No FXML resource found.");
         Scene scene = new Scene(FXMLLoader.load(r));
         stage.setTitle("Critter World");
         stage.setScene(scene);
         stage.sizeToScene();
         stage.show();

      } catch (Exception e) {
         System.out.println(e.getMessage());
         e.printStackTrace();
      }
   }

}