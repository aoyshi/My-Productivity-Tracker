package productivity_tracker;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Productivity_Tracker extends Application {

  @Override
  public void start(Stage stage) throws Exception {
      
    FXMLLoader loader = new FXMLLoader(getClass().getResource("MainScreen.fxml"));
    Parent root = (Parent)loader.load();
    MainScreenController controller = (MainScreenController) loader.getController();
    controller.setStage(stage); //send reference of primaryStage to controller class
    
    stage.setTitle("My Productivty Tracker");
    
    Scene scene = new Scene(root);
    stage.setScene(scene);
    stage.show(); 
  }

  public static void main(String[] args) {
    launch(args);
  }
        
        
}
    

