package productivity_tracker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class confirmBox {
  
  public static void closeProgram(Date AppStartDate, double[] perArray, double[] tots, 
                                  int goal, LocalTime[] ltarray){
    
    Stage window = new Stage();
    window.initModality(Modality.APPLICATION_MODAL); //stop all main window interaction until closed
    window.setTitle("Confirmation");
    
    Label label = new Label();
    label.setText("Are you sure you want to exit?");
    Button close = new Button("Close without Saving");
    Button save = new Button("Save and Close");
    
    window.setOnCloseRequest(e -> {
      e.consume(); //dont let user close using [x] top right without deciding if save file or not.
    });
    
    close.setOnAction(e -> window.close());
    
    //save and quit
    save.setOnAction(event -> {  
      //AppStartDate is dateArray[0] (First Go Time);
      DateFormat dateFormat = new SimpleDateFormat("E,yyyy/MM/dd");
      String DATE = dateFormat.format(AppStartDate);
        try 
        {
            //TODO: let user define file path
            //TODO: switch to sqlite instead of text file
              File saveFile = new File("Progress_Info.txt");

              if (saveFile.createNewFile()) //returns false if file already exists; creates if not exists.
              {  
                System.out.println("Progress_Info file has been created!");
                PrintWriter writer = null;
                    while(true)
                    {
                      try {
                        writer = new PrintWriter("Progress_Info.txt"); //open file for writing
                      }
                      catch (Exception e)
                      {
                        break;
                      }
                             
                      writer.printf("Day,Date,ProdPercent,ProdSeconds,GoalHours,Start,PlanStart,Stop,PlanStop\r\n");
                      
                      if((ltarray[1]==null)&&(ltarray[2]==null))  //if plan start & stop both null
                      {
                        writer.printf("%s,%.2f,%.2f,%d,%s,09:00,%s,00:15\r\n",DATE,perArray[0],tots[0],goal,ltarray[0],ltarray[3]);
                      }
                      
                      else if((ltarray[1]!=null)&&(ltarray[2]==null))  //if plan stop  null
                      {
                        writer.printf("%s,%.2f,%.2f,%d,%s,%s,%s,00:15\r\n",DATE,perArray[0],tots[0],goal,ltarray[0],ltarray[1],ltarray[3]);
                      }
                      
                      else if((ltarray[1]==null)&&(ltarray[2]!=null))  //if plan start  null
                      {
                        writer.printf("%s,%.2f,%.2f,%d,%s,09:00,%s,%s\r\n",DATE,perArray[0],tots[0],goal,ltarray[0],ltarray[3],ltarray[2]);
                      }
                                          
                      else
                       writer.printf("%s,%.2f,%.2f,%d,%s,%s,%s,%s\r\n",DATE,perArray[0],tots[0],goal,ltarray[0],ltarray[1],ltarray[3],ltarray[2]);
                      
                      writer.close();
                      break;
                    }        
              }

              else //file already exists, dont create; only append
              {
                      try
                      (FileWriter fw = new FileWriter("Progress_Info.txt", true); //true = append, dont overwrite
                      BufferedWriter bw = new BufferedWriter(fw); //bypasses long disk read/write time
                      PrintWriter out = new PrintWriter(bw))
                      {
                          if((ltarray[1]==null)&&(ltarray[2]==null))  //if plan start & stop both null
                          {
                            out.printf("%s,%.2f,%.2f,%d,%s,09:00,%s,00:15\r\n",DATE,perArray[0],tots[0],goal,ltarray[0],ltarray[3]);
                          }

                          else if((ltarray[1]!=null)&&(ltarray[2]==null))  //if plan stop  null
                          {
                            out.printf("%s,%.2f,%.2f,%d,%s,%s,%s,00:15\r\n",DATE,perArray[0],tots[0],goal,ltarray[0],ltarray[1],ltarray[3]);
                          }

                          else if((ltarray[1]==null)&&(ltarray[2]!=null))  //if plan start  null
                          {
                            out.printf("%s,%.2f,%.2f,%d,%s,09:00,%s,%s\r\n",DATE,perArray[0],tots[0],goal,ltarray[0],ltarray[3],ltarray[2]);
                          }

                          else
                           out.printf("%s,%.2f,%.2f,%d,%s,%s,%s,%s\r\n",DATE,perArray[0],tots[0],goal,ltarray[0],ltarray[1],ltarray[3],ltarray[2]);

                      } 
                      catch (IOException e) {
                        System.out.println("Error appending to file.");
                      }
                }

    	} //first try ends         
        catch (IOException e) 
        {
	      System.out.println("Error with file IO in main TRY{}block.");
        }
  
      window.close();
      
    }); //end button event
    
    HBox buttons = new HBox(10);
    buttons.getChildren().addAll(save, close);
    buttons.setAlignment(Pos.CENTER);
    
    VBox layout = new VBox(10);
    layout.getChildren().addAll(label,buttons);
    layout.setAlignment(Pos.CENTER);
    
    Scene scene = new Scene(layout, 350, 250);
    
    window.setScene(scene);
    window.showAndWait(); //goes with init.modality
  }
  
}
