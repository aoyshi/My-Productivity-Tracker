package productivity_tracker;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class MainScreenController implements Initializable {
   
    @FXML private HBox progressIndSpace; 
    @FXML private HBox pieChartSpace;      
    @FXML private TextField startTime;
    @FXML private TextField stopTime;
    @FXML private Button prodStart;
    @FXML private Button prodStop;
    @FXML private Button stopBtn;
    @FXML private Button updateBtn;
    @FXML private Button changeGoalStop;
    @FXML private Button changeGoalStart;
    @FXML private Button goBtn;
    @FXML private Button goalEdit;
    @FXML private Label goalHoursLabel;
    @FXML private Label goalStartTime;
    @FXML private Label goalStopTime;
    @FXML private Label goalRemaining;
    @FXML private Label totalProd;
    @FXML private Label prodField;
    @FXML private Label wasteField;
    @FXML private Label doneByLabel;
    @FXML private Label doneByWordyLabel;
   
    //check if user entered valid end time, so safe to let him close app
    private int stopFlag = 0;
    private int startTimeValidFlag = 0; 
    //total number of hours set as goal
    private int goal = 8;
    private int flag = 0; //for detecting if in productive/wasted state, for update button function
    private double[] perArray = {0,0}; //perArray[0] = percent Prod
                               //perArray[0] = percent Waste
    
    //LocalTime Array
    private LocalTime first = null, //LTarray[0] is first time you hit Go! & start your day
              planstart = null, //LTarray[1] is the desired time you planned to start working for the day
              planstop = null,  //LTarray[2] is the desired time you planned to stop working for the day
              end = null;  //LTarray[3] is the actual time you hit Stop! and end your working day
    private LocalTime[] LTarray = {first, planstart, planstop, end};
    
    //dateArray
    private Date first_date = null, //0 full CST date version of LocalTime[0]
         start_date = null,  //1 full CST date version of LocalTime[1]
         stop_date = null,    //2 full CST date version of LocalTime[2]
         end_date = null;    //3 full CST date version of LocalTime[3]
    private Date[] dateArray = {first_date, start_date, stop_date, end_date};  
    
    private String[] GO_Storage = {"",""}; //GO_Storage[0] stores Go! time, [1] stores end day time
    
    //TOTALS ARRAY
    private double[] tots = new double[3]; //tots[0]=total prod, tots[1]=total waste, tots[3]=totalTimeElapsed
    private String doneBy = "";
    
    //define PIECHART
    ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Productive", 0),
            new PieChart.Data("Wasted", 100));
    final PieChart chart = new PieChart(pieChartData); 
    
    Alert alert = new Alert(AlertType.ERROR);
    
    //progress indicator circle
    ProgressIndicator progressInd = new ProgressIndicator(0);
    
    public void setStage (Stage primaryStage) 
    {      
        primaryStage.setOnCloseRequest(e -> 
        { 
            if(startTimeValidFlag==0) //app has not been started, no valid start time loaded.
            {
               primaryStage.close();
            }
            else if(stopFlag==0) //flag detects valid stop time hasnt been set
            {  
               alert.setTitle("Popup");
               alert.setHeaderText("Warning");
               alert.setContentText("You must enter your Stop Time before closing");
               alert.showAndWait();
               e.consume(); //doesnt close main window if you close alertbox
            }
            else //save info to file
            {
               confirmBox.closeProgram(dateArray[0],perArray,tots,goal,LTarray);  
            }            
        });  
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {       
        //initialize progress indicator to 0% & display on gui
        progressInd.setProgress(0);
        progressIndSpace.getChildren().add(progressInd);       
        //set pie slice color
        applyCustomColorSequence (pieChartData, "#ffe242", "grey");
        //hide pie chart legend
        chart.setLegendVisible(false);
        //display piechart on gui
        pieChartSpace.getChildren().add(chart);  
        
        prodField.setAlignment(Pos.CENTER);
        wasteField.setAlignment(Pos.CENTER);
        goalHoursLabel.setAlignment(Pos.CENTER);
    }  
    
    //Event for GO! button (enable all functionality)
    @FXML
    public void goBtnEvent(ActionEvent event) {
        String userStart = startTime.getText();       
        if(checkValidTime(userStart, LTarray, 1, GO_Storage)) //use int=1 to use validate function for check start time
        {              
            if (LTarray[0].isBefore(LocalTime.now())) 
            {
                /** Go_Storage[0] is Go_Time converted to 24hr military format by checkValidTime function.
                  * this function converts go_storage[0] to Date() and feeds into dateArray[0] (used as FIRST_time) */               
                convertToDate(GO_Storage[0], dateArray, 1);                 
                //enable buttons if valid;
                goBtn.setDisable(true);
                prodStart.setDisable(false);
                stopBtn.setDisable(false);
                changeGoalStop.setDisable(false);
                changeGoalStart.setDisable(false);          
                //indicate valid
                startTimeValidFlag = 1;
            }
            else
            { 
                alert.setTitle("Error");
                alert.setHeaderText("Invalid Start Time!");
                alert.setContentText("Start Time cannot be in the future");
                alert.showAndWait();
            }       
        }               
        else
        {
          alert.setTitle("Error");
          alert.setHeaderText("Invalid Start Time!");
          alert.setContentText("Format must be hh:mm AM|PM \nMust be in 12-hour notation");
          alert.showAndWait();
        } 
    }
    
    @FXML
    public void stopBtnEvent(ActionEvent event) {
        String userStop = stopTime.getText();
            if(checkValidTime(userStop,LTarray,0,GO_Storage)) //use int=0 to use validate function for check stop time
            {             
                if (LTarray[3].isBefore(LocalTime.now())) 
                {
                    alert.setTitle("Error");
                    alert.setHeaderText("Invalid Start Time!");
                    alert.setContentText("Stop Time cannot be in the past");
                    alert.showAndWait();
                }
                else
                {
                  stopFlag = 1; //flag=1 if valid END time has been set. Now may close program.                 
                  convertToDate(GO_Storage[1], dateArray, 0); //puts entered end_time as end_date in dateArray[3]

                  //tots[2]=total run time elapsed for app
                  tots[2] = (dateArray[3].getTime() - dateArray[0].getTime())/(1000);
                                 
                  //only display upon setting END time
                  String total_elapsed = getTime(tots[2]);
                  
                  //disable all buttons upon ending work-day
                  goalEdit.setDisable(true);
                  goBtn.setDisable(true);
                  stopBtn.setDisable(true);
                  stopTime.setEditable(false);
                  updateBtn.fire(); //update one last time before quitting

                  updateBtn.setDisable(true);
                  prodStart.setDisable(true);
                  prodStop.setDisable(true);                   
                }
            }
            else
            {
              alert.setTitle("Error");
              alert.setHeaderText("Invalid Stop Time!");
              alert.setContentText("Format must be hh:mm AM|PM \nMust be in 12-hour notation");
              alert.showAndWait();
            }                      
    }
    
    @FXML
    public void goalEditEvent(ActionEvent event) 
    {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Set Goal Hours");
        dialog.setHeaderText("How many hours do you want to be productive today?");
        dialog.setContentText("Enter a whole number: ");
        // Traditional way to get the response value.
        Optional<String> input = dialog.showAndWait();
        if (input.isPresent())
        {
          String time = input.get().trim();         
          if(time.length()!=0)
          {
                try
                {
                    goal = Integer.parseInt(time);
                    if(goal <= 0)
                    {
                        alert.setTitle("Error");
                        alert.setHeaderText("Invalid Number!");
                        alert.setContentText("Goal Hours must be greater than 0");
                        alert.showAndWait();
                    }
                    else //passed all sanity checks, valid int
                    {
                        goalHoursLabel.setText(Integer.toString(goal));
                        //TODO: update this on DB 
                    }         
                }
                catch (Exception e)
                {
                    alert.setTitle("Error");
                    alert.setHeaderText("Invalid Number!");
                    alert.setContentText("Goal Hours must be a valid WHOLE number");
                    alert.showAndWait();
                }             
           }          
       }   
   }

    @FXML
    public void changeGoalStartEvent(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Set Goal Time");
        dialog.setHeaderText("When do you want to start working every day?");
        dialog.setContentText("Enter Time (hh:mm AM|PM) : ");

        // Traditional way to get the response value.
        Optional<String> input = dialog.showAndWait();
        if (input.isPresent())
        {
          String time = input.get().trim();  
          if(time.length() != 0)
          {
              if(checkValidTime(time, LTarray, 2, null)) 
              {
                  //change label in gui
                  goalStartTime.setText(time);
                  //update DB
              }
              else 
              {
                  alert.setTitle("Error");
                  alert.setHeaderText("Invalid Time!");
                  alert.setContentText("Format must be hh:mm AM|PM \nMust be in 12-hour notation");
                  alert.showAndWait();
              }
          }
        }

    }
    
    @FXML
    public void changeGoalStopEvent(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Set Goal Time");
        dialog.setHeaderText("When do you want to stop working every day?");
        dialog.setContentText("Enter Time (hh:mm AM|PM) : ");
        // Traditional way to get the response value.
        Optional<String> input = dialog.showAndWait();
        if (input.isPresent())
        {
          String time = input.get().trim();  
          if(time.length() != 0)
          {
              if(checkValidTime(time, LTarray, 3, null)) 
              {
                  //change label in gui
                  goalStopTime.setText(time);
                  //update DB
              }
              else 
              {
                  alert.setTitle("Error");
                  alert.setHeaderText("Invalid Time!");
                  alert.setContentText("Format must be hh:mm AM|PM \nMust be in 12-hour notation");
                  alert.showAndWait();
              }
          }
        }

    }
    
    @FXML
    public void onProdStartEvent(ActionEvent event) {
      Date dateStart = new Date();
      dateArray[1] = dateStart; //dateArray[1] = start time (dateArray[0] was FIRST start upon clicking GO!)
      
      flag = 1; //in productive state (for update btn) 
      prodStart.setDisable(true);
      prodStop.setDisable(false);
      updateBtn.setDisable(false); //enable update btn only after first start working click
    }
    
    @FXML
    public void onProdStopEvent(ActionEvent event) {
        
        flag=0; //wasting state (for update btn)

        Date dateStop = new Date();
        dateArray[2] = dateStop; //dateArray[2]=Stop working time

        prodStop.setDisable(true);
        prodStart.setDisable(false);
          
        double diff = (dateArray[2].getTime() - dateArray[1].getTime())/(1000); //convert millisec to sec         
        double total_diff = (dateArray[2].getTime() - dateArray[0].getTime())/(1000);

        //Update goal remaining (total prodhour left)            
        //tots[] is array of totals (in seconds)
        tots[0] += diff;                  //tots[0] = total prod        
        tots[1] = total_diff - tots[0];   //tots[1] = total waste 

        //percents array
        perArray[0]= (tots[0]/total_diff * 100); //% prod
        perArray[1]= (tots[1]/total_diff * 100); //% waste

        //UPDATE PROGRESS BAR----------
        double percentProgress = (tots[0]/(goal*3600)); 
        //goal[0] is total progress time goal set (hours/day)  
        progressInd.setProgress(percentProgress);

        //UPDATE GOAL REMANING (TOTAL PROF HOURS LEFT)
        double prodHoursLeft = (goal*3600)-tots[0];
        goalRemaining.setText("Left : " + getTime(prodHoursLeft));
      
      //get percents in one-decimal place 
      DecimalFormat oneDP = new DecimalFormat("#,##0.0");
      //Update Percents Display--------
      prodField.setText(oneDP.format(perArray[0]));
      wasteField.setText(oneDP.format(perArray[1]));
      
      //UPDATE PIE-CHART-------
      addData("Productive", perArray[0]);
      addData("Wasted", perArray[1]);
        
      //Update Projected Completion Time until goalHours complete    
      if(percentProgress < 1) 
      {
        int remainingGoal = (int) ( goal*3600 - tots[0] ); //in seconds     
        Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
        calendar.add(Calendar.SECOND, remainingGoal);       
        SimpleDateFormat _12HourSDF = new SimpleDateFormat("hh:mm a");
        doneBy = _12HourSDF.format((calendar.getTime()));
        doneByLabel.setText(doneBy);
      }
      else //if completed goal, stop updating projected completion time & permanently set it to when hours was completed
      {
        doneByWordyLabel.setText("You were done by: ");
        doneByLabel.setText(doneBy);
      }
    
      //Turn total prod hours (tots[0]) to String time hr,min,sec
      String totalString = getTime(tots[0]);
      totalProd.setText("Done : " + totalString);
    }
    
    @FXML
    public void onUpdateEvent(ActionEvent event) {
      if (flag==1) //if in productive state
      {
        prodStop.fire();     
        prodStart.fire();
      }
      else //if in wasting state
      {
        prodStart.fire();     
        prodStop.fire();
      }

    }
    
    @FXML
    public void generateProgressReport(ActionEvent event)  {
      
        try {
           ProgressReport.generateReport();
        }
        catch (Exception e) 
        {
            alert.setTitle("Error");
            alert.setHeaderText("Could not generate Progress Report");
            alert.setContentText(e.toString());
            alert.showAndWait();
        }
        
    }
    
    
    
//-----------------------------------AUXILIARY FUNCTIONS--------------------------------------------//

  public static boolean checkValidTime(String input, LocalTime[] array, int start, String[] GO_storage) {     

      //ignore leading zeroes
      if(input.charAt(0)=='0')
          input = input.substring(1);     
        
      //ensure entry is in 00:00 AM/PM format
      Pattern pattern = Pattern.compile("(1[012]|[1-9]):[0-5][0-9](\\s)?(?i)(AM|PM)");
      boolean match = pattern.matcher(input).matches();
      
      if(match) 
      {     
        SimpleDateFormat inputFormat = new SimpleDateFormat("hh:mm a"); //12hour
        SimpleDateFormat finalFormat = new SimpleDateFormat("HH:mm");   //24hour    
        Date date = null;
        
        try 
        {
          date = inputFormat.parse(input); //check if 00:00 input falls into legal 12-hour format
        } 
        catch (ParseException ex) {
          return false;
        }
        
        String convertedTime = finalFormat.format(date); //in 24-hr format
        
        if (start==1)
          GO_storage[0] = convertedTime; //FIRST Go time
        
        if(start==0)
          GO_storage[1] = convertedTime; //END time
          
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        try
        {
           LocalTime time = LocalTime.parse(convertedTime, formatter); 

           if(start==1) //if using this method for FIRST_GO Time 
           {
              array[0] = time; //Use as FIRST in calculations
           }
           if(start==0) //if using this method for END Time 
           {
              array[3] = time; //saved as END STOP TIME in file
           }
           
           //------------PLAN-START AND PLAN-STOP VERIFICATION------------------
           if(start==2) //for LTarray[1] to verify planstart time
           {
              array[1] = time; //LTarray[1] planstart
           }
           if(start==3) //for LTarray[2] to verify planstop time 
           {
              array[2] = time; //LTarray[2] planstop
           }
           
           return true;
         }

         catch(DateTimeParseException e)
         {
             return false;
         } 
        }

        else 
        {
          return false;
        }  

    }
      
  //convert total time in seconds to String ::hr,::min,::sec
  public static String getTime(double totalSec) {
     
    StringBuilder result = new StringBuilder("");
      
    int hours = (int) totalSec/3600;
    result.append(Integer.toString(hours));
    result.append(" hr, ");

    totalSec = totalSec%3600;
    int minutes = (int) totalSec/(60);
    result.append(Integer.toString(minutes));
    result.append(" min, ");

    int seconds = (int) (totalSec%60);
    result.append(Integer.toString(seconds));
    result.append(" sec.");

    return result.toString();
  }
  
  //converts a time (12:13 AM) to a DATE (Tue Jan 02 00:13:00 2018)
  public static void convertToDate(String time, Date[] dateArray, int start) {  
    Date dateNow = new Date();  
    String dateToString = null;
    SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd"); //get "Tue Jan 02" part of dateNow

   try{
	 dateToString = sdf.format(dateNow); //convert date to string
   }
   catch (Exception ex){
	 System.out.println(ex);
   }
    
    String user_dateToString = dateToString + " " + time + ":00 2018"; //form "Tue Jan 02 HH:mm:00 2018"
    SimpleDateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy");

    try {
        Date date = formatter.parse(user_dateToString); //convert string to date
        
        if (start==1) //if using for GO! TIME
          dateArray[0] = date;
        
        else if (start==0) //if using for END TIME
          dateArray[3] = date;    
    } 
    catch (ParseException e) {
      System.out.println(e);
    }
        
  }
        
  //adds new Data to the list
  public void naiveAddData(String name, double value)
  {
      pieChartData.add(new PieChart.Data(name, value));
  }

  //updates existing Data-Object if name matches
  public void addData(String name, double value)
  {
      for(PieChart.Data d : pieChartData)
      {
          if(d.getName().equals(name))
          {
              d.setPieValue(value);
              return;
          }
      }
      naiveAddData(name, value);
  }

  //change pie-slice color
  private void applyCustomColorSequence(ObservableList<PieChart.Data> pieChartData, String... pieColors) {
        int i = 0;
        for (PieChart.Data data : pieChartData) {
          data.getNode().setStyle("-fx-pie-color: " + pieColors[i % pieColors.length] + ";");
          i++;
        }
  } 

    
}
