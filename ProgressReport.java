package productivity_tracker;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ProgressReport {
    //-------------------------PROGRESS REPORT & STATS----------------------------------
  public static void generateReport() throws Exception {
    
    //create lists for file elements for graphs
    ArrayList<String> days = new ArrayList<>();
    ArrayList<String> dates = new ArrayList<>();
    ArrayList<Double> prodHours = new ArrayList<>();
    ArrayList<Double> prodPercents = new ArrayList<>();
    ArrayList<Double> goalHours = new ArrayList<>();
    ArrayList<String> startTimes = new ArrayList<>();
    ArrayList<String> endTimes = new ArrayList<>();
    //fill lists with file data
    int fillData = populateLists(days, dates, prodHours, prodPercents, goalHours, startTimes, endTimes);

    Stage window = new Stage();
    window.initModality(Modality.NONE);
    window.setTitle("Progress Report & Stats");

    Label select = new Label("Select Report Type:");
    Button daily = new Button("Daily");
    Button weekly = new Button("Weekly");
    Button dayOfWeek = new Button("DayOfWeek");
    Button routine = new Button("Routine");
    
    daily.setOnAction(e -> {
        try {
            daily(dates,prodPercents,prodHours);
        } catch (Exception ex) {
            Logger.getLogger(ProgressReport.class.getName()).log(Level.SEVERE, null, ex);
        }
    });
    weekly.setOnAction(e -> {
      weekly(days,prodPercents,prodHours);
    });
    dayOfWeek.setOnAction(e -> {
        try {
            dayOfWeek(dates,days,prodPercents,prodHours);
        } catch (Exception ex) {
            Logger.getLogger(ProgressReport.class.getName()).log(Level.SEVERE, null, ex);
        }
    });
    routine.setOnAction(e -> {
        try {
            routine(dates, startTimes, endTimes);
        } catch (Exception ex) {
            Logger.getLogger(ProgressReport.class.getName()).log(Level.SEVERE, null, ex);
        }
    });
    
    //MAX
    int maxPercent = getMax(prodPercents);
    int maxHours = getMax(prodHours);
    String mostProdDay = convertDateFormat(dates.get(maxPercent))+" at " 
          + prodPercents.get(maxPercent) + "% and " + (int)(Math.round((prodHours.get(maxPercent)/3600))) + " hrs";
    String mostProdHrs = convertDateFormat(dates.get(maxHours))+" at " 
          + (int)(Math.round((prodHours.get(maxHours)/3600)))+ " hrs and " + prodPercents.get(maxHours) + "%";
    Label stats1 = new Label("Most Productive Day:     " + mostProdDay);
    Label stats2 = new Label("Most Hours Done Day:   " + mostProdHrs);
    
    //AVG
    int avgProd = (int) Math.round(getAvg(prodPercents));
    int avgHrs = (int) (Math.round(getAvg(prodHours)/3600));
    Label avgPercent = new Label("Avg Daily Productivity:   " + avgProd + "%");
    Label avgHours = new Label("Avg Daily Hours Done:   " + avgHrs + " hrs");
 
    //BEST DAYS
    String[] bestDays = getBestDays(dates,days,prodPercents,prodHours);
    Label bestProdDay = new Label("You're most efficient:     " + bestDays[0]);
    Label bestHourDay = new Label("Most hours done on:     " + bestDays[1]);
    
    VBox Reports = new VBox();
    Reports.getChildren().addAll(select, daily, weekly, dayOfWeek, routine);
    Reports.setAlignment(Pos.CENTER);
    Reports.setSpacing(20);
    Reports.setPadding(new Insets(0,0,0,30));
    

    VBox Stats = new VBox();
    Stats.getChildren().addAll(stats1, stats2, avgPercent, avgHours, bestProdDay, bestHourDay);
    Stats.setSpacing(20);
    Stats.setPadding(new Insets(30,10,10,10));
    
    HBox layout = new HBox();
    layout.getChildren().addAll(Reports,Stats);
    layout.setSpacing(50);
    layout.setAlignment(Pos.CENTER);
    Scene scene  = new Scene(layout,660,300);        
    window.setScene(scene);
    window.showAndWait();
    }
   
  public static String[] getBestDays(ArrayList<String> dates, ArrayList<String> days, ArrayList<Double>prodPercents, ArrayList<Double> prodHours) throws Exception
  {   
    double[] percents = byTheDay(prodPercents, dates, days, 0);
    double[] hours = byTheDay(prodHours, dates, days, 1);
    
    String[] bestDays = {"Mondays","Tuesdays","Wednesdays","Thursdays","Fridays","Saturdays","Sundays"};
    String[] result = new String[2];
    result[0] = bestDays[getArrayMax(percents)];
    result[1] = bestDays[getArrayMax(hours)];
    
    return result;
  }
  
  public static int getArrayMax(double[] array)
  {
    int maxPos = 0;
    double max = 0;
    for(int i=0; i<array.length; i++)
    {
      if(array[i]>max)
      {
        max = array[i];
        maxPos = i;
      }
    }
    return maxPos;
  }
  
  public static int getMax(ArrayList<Double> prod)
  {
    int maxPos = 0;
    double max = 0;
    for(int i=0; i<prod.size(); i++)
    {
      if(prod.get(i)>max)
      {
        max = prod.get(i);
        maxPos = i;
      }
    }
    return maxPos;
  }
  
  public static double getAvg(ArrayList<Double> prod)
  {
    double sum=0;
    int size = prod.size();
    for(int i=0; i<size; i++)
      sum += prod.get(i);
    return(sum/size);
  }
  
  public static int getWeekly(double[] weeks, ArrayList<Double> prod, ArrayList<String> days){
    int k=0;
    for(int i=0; i<days.size(); i++)
    {
      String day = days.get(i);
      if(day.equals("Mon"))
      {
        weeks[k] += prod.get(i);
        String nxtDay = "x";
        while(true)
        {
          i++;  
          if(i>=days.size())
            break;
          
          nxtDay = days.get(i);
          if(nxtDay.equals("Mon"))
            break;
          
          weeks[k] += prod.get(i);
        }
        k++;
        i--;
      }
    }
    return k;
  }
  
  //creates 2d array of rows and columns from each line from progress file
  public static String[][] readSpreadsheet(String filename) throws Exception {
    ArrayList<String> lines = readFile(filename);
    if(lines==null)
    {
      System.out.println("Error in reading file into list");
      return null;
    }
    int rows = lines.size();
    String[][] result = new String[rows][];
    
    for(int i=0; i<rows; i++)
    {
      String oneLine = lines.get(i);
      String[] eachLine = oneLine.split(",");
      result[i] = eachLine;
    }
    return result;
  }
  
  //creates scanner opject to read file line by line
  public static ArrayList<String> readFile(String filename) throws Exception {
    ArrayList<String> lines = new ArrayList<String>();  
    File file = new File(filename);
    Scanner fileReader= new Scanner(file);
    
    fileReader.nextLine(); //skip first column name row
    while(fileReader.hasNextLine())
    {
      lines.add(fileReader.nextLine());
    }
    fileReader.close();
    return lines;
  }
  
  //converts YYYY/MM/DD to Mon Jan 5 2018
  public static String convertDateFormat(String dateString) throws Exception {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
    SimpleDateFormat formatterII = new SimpleDateFormat("E MMM dd");
    Date date = formatter.parse(dateString);
    String newDate = null;
    newDate = formatterII.format(date);
    return newDate;
  }
  
    //converts YYYY/MM/DD to DATE
  public static Date convertToDate(String dateString) throws Exception {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
    Date date = null;
    date = formatter.parse(dateString);
    return date;
  }
  
  //fill arraylists with data from file
  public static int populateLists( ArrayList<String> days, ArrayList<String> dates, ArrayList<Double> prodHours, 
                                   ArrayList<Double> prodPercents, ArrayList<Double> goalHours, 
                                   ArrayList<String> startTimes, ArrayList<String> endTimes) throws Exception
  {
      //read data from progress file
      String[][] data = readSpreadsheet("Progress_Info.txt");
      if(data==null)
      {
        return -1;
      }     
      //fill above lists with respective values from data[][]
        for(int row=0; row<data.length; row++)
        {
            days.add(data[row][0]);
            dates.add(data[row][1]);
            prodPercents.add(Double.parseDouble(data[row][2]));
            prodHours.add(Double.parseDouble(data[row][3]));
            goalHours.add(Double.parseDouble(data[row][4]));
            startTimes.add((data[row][5]));
            endTimes.add((data[row][7]));
        }
      return 0;      
  }
  
  public static void daily(ArrayList<String> dates, ArrayList<Double> prodPercents, ArrayList<Double> prodHours) throws Exception {    
    Stage window = new Stage();
    window.initModality(Modality.NONE);
    window.setTitle("Daily Progress Report");
    //defining the axes
    final CategoryAxis xAxis = new CategoryAxis();
    final NumberAxis yAxis = new NumberAxis();
    yAxis.setLabel("Percent(%) or Hours");
    xAxis.setLabel("Date");
    //creating the chart
    final LineChart<String,Number> lineChart1 = new LineChart(xAxis,yAxis);
    //defining a series
    final XYChart.Series<String, Number> seriesPercent = new XYChart.Series();
    final XYChart.Series<String, Number> seriesHour = new XYChart.Series();

    seriesHour.setName("Hour");
    //populating the series with data
    for (int i=0; i<dates.size(); i++) 
    {
      seriesPercent.getData().add(new XYChart.Data(convertDateFormat(dates.get(i)), prodPercents.get(i))); 
      seriesHour.getData().add(new XYChart.Data(convertDateFormat(dates.get(i)), (prodHours.get(i)/3600)));
	}
    seriesPercent.setName("Percent");
    lineChart1.setTitle("Daily Productivity, 2018");
    lineChart1.getData().addAll(seriesPercent,seriesHour);
    Scene scene = new Scene(lineChart1, 1350, 450);
    window.setScene(scene);
    window.showAndWait();
  }
  
  public static double[] byTheDay(ArrayList<Double> prod, ArrayList<String> dates, ArrayList<String> days, int isHour) throws Exception{
    //get # days
    Date date1 = convertToDate(dates.get(1));
    Date date2 = convertToDate(dates.get(dates.size()-1));
    int diffInDays = (int) ((date2.getTime() - date1.getTime()) / (1000 * 60 * 60 * 24));
    int totalDays = diffInDays/7;
   
    //array for storing mon,tue,wed...
    double[] dayArray = new double[7];

    //get count for each day
    for(int i=1; i<dates.size(); i++)
    {
      String day = days.get(i);
      switch(day) 
      {
        case "Mon":  dayArray[0] += prod.get(i); break;
        case "Tue":  dayArray[1] += prod.get(i); break;
        case "Wed":  dayArray[2] += prod.get(i); break;
        case "Thu":  dayArray[3] += prod.get(i); break;
        case "Fri":  dayArray[4] += prod.get(i); break;
        case "Sat":  dayArray[5] += prod.get(i); break;
        case "Sun":  dayArray[6] += prod.get(i); break;
      }
    }
    
    if(isHour == 1) //if using this function for hours
    {
      for(int i=0; i<dayArray.length; i++)
         dayArray[i] = (dayArray[i]/(3600*totalDays));
    }
    else
    {
      for(int i=0; i<dayArray.length; i++)
         dayArray[i] = dayArray[i]/totalDays;
    }
    
    return dayArray;
  }
  
  public static void dayOfWeek(ArrayList<String> dates, ArrayList<String> days, ArrayList<Double> prodPercents, ArrayList<Double> prodHours) throws Exception{    
    Stage window = new Stage();
    window.initModality(Modality.NONE);
    window.setTitle("Day-Type Progress Report");

    double[] percents = byTheDay(prodPercents, dates, days, 0);
    double[] hours = byTheDay(prodHours, dates, days, 1);
    
    //defining the axes
    final CategoryAxis xAxis = new CategoryAxis();
    final NumberAxis yAxis = new NumberAxis();
    yAxis.setLabel("Percent%(P) or Hours(H)");
    xAxis.setLabel("Day Of Week");
    //creating the chart
    final LineChart<String,Number> lineChart = new LineChart(xAxis,yAxis);
    //defining a series
    final XYChart.Series<String, Number> seriesPercent = new XYChart.Series();
    
    String[] daysOfWeek = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
  
    //populating the series with data
    for(int i=0; i<percents.length; i++)
    {
      seriesPercent.getData().add(new XYChart.Data(daysOfWeek[i], percents[i]));
    }
    seriesPercent.setName("Percent(P)");
    
    //populating the second series with data
    final XYChart.Series<String, Number> seriesHour = new XYChart.Series();
    for(int i=0; i<hours.length; i++)
    {
      seriesHour.getData().add(new XYChart.Data(daysOfWeek[i], hours[i]));
    }
    seriesHour.setName("Hour(H)");
    
    lineChart.setTitle("Productivity by Day-of-Week, 2018");
    lineChart.getData().addAll(seriesPercent,seriesHour);
    Scene scene = new Scene(lineChart, 1000, 450);
    window.setScene(scene);
    window.showAndWait();
  }
  
  public static void weekly(ArrayList<String> days, ArrayList<Double> prodPercents, ArrayList<Double> prodHours) {    
    Stage window = new Stage();
    window.initModality(Modality.NONE);
    window.setTitle("Weekly Progress Report");
    
    double[] weeks = new double[50];
    int k = getWeekly(weeks, prodPercents, days);
    
    //defining the axes
    final NumberAxis xAxis = new NumberAxis();
    final NumberAxis yAxis = new NumberAxis();
    yAxis.setLabel("Percent%(P)");
    xAxis.setLabel("Week#");
    //creating the chart
    final LineChart<Number,Number> lineChart = new LineChart(xAxis,yAxis);
    //defining a series
    final XYChart.Series<Number,Number> series = new XYChart.Series();
    //populating the series with data
    for (int i=0; i<k; i++) 
    {
      series.getData().add(new XYChart.Data(i+1, weeks[i]/7));    
	}
    series.setName("Percent Weekly");
    lineChart.setTitle("Percent Weekly Productivity, 2018");
    lineChart.getData().add(series);
    Scene scene = new Scene(lineChart, 1000, 450);
    window.setScene(scene);
    window.showAndWait();
  }
  
  public static ArrayList<Double> getTimes(ArrayList<String> list, int isBed) {
    ArrayList<Double> result = new ArrayList<>();
    for(int i=0; i<list.size(); i++)
    {
      String[] parts = (list.get(i)).split(":"); //01:22
      double hr = Double.parseDouble(parts[0]);
      if(hr<1)
        hr=24;
      if(isBed==1)
      {
        if(hr>=1 && hr<8)
          hr+=24;
      }
      double min = Double.parseDouble(parts[1])/60;
      double time = hr + min;
      result.add(time);
    }
    return result;
  }
  
  public static void routine(ArrayList<String> dates, ArrayList<String> startTimes, ArrayList<String> endTimes) throws Exception {    
    ArrayList<Double> start = getTimes(startTimes,0);
    ArrayList<Double> end = getTimes(endTimes,1);
    
    Stage window = new Stage();
    window.initModality(Modality.NONE);
    window.setTitle("Routine Adherence Report");   
    //defining the axes
    final CategoryAxis xAxis = new CategoryAxis();
    final NumberAxis yAxis = new NumberAxis();
    yAxis.setLabel("Time");
    xAxis.setLabel("Date");
    //creating the chart 
    final LineChart<String,Number> lineChart = new LineChart(xAxis,yAxis);
    //defining a series
    final XYChart.Series<String,Number> startSeries = new XYChart.Series();
    final XYChart.Series<String,Number> endSeries = new XYChart.Series();
    final XYChart.Series<String,Number> NINE = new XYChart.Series();
    final XYChart.Series<String,Number> TWELVE = new XYChart.Series();
    
    startSeries.setName("StartTime");
    endSeries.setName("BedTime");
    //TODO: use saved goal times as graphs here
    NINE.setName("9:00 AM");
    TWELVE.setName("12:00 AM");
    
    //populating the series with data
    for (int i=0; i<dates.size(); i++) 
    {
      endSeries.getData().add(new XYChart.Data(convertDateFormat(dates.get(i)), end.get(i))); 
      startSeries.getData().add(new XYChart.Data(convertDateFormat(dates.get(i)), start.get(i)));  
      NINE.getData().add(new XYChart.Data(convertDateFormat(dates.get(i)), 9));
      TWELVE.getData().add(new XYChart.Data(convertDateFormat(dates.get(i)), 24));
    }  
    
    lineChart.setTitle("Routine Adherence, 2018");
    lineChart.getData().addAll(startSeries, endSeries, NINE, TWELVE);
    Scene scene = new Scene(lineChart, 1000, 450);
    window.setScene(scene);
    window.showAndWait();
  }
  
    
}
