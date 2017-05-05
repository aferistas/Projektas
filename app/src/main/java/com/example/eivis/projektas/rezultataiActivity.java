package com.example.eivis.projektas;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.sql.Time;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class rezultataiActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private static final String TAG = "rezultataiActivity";
    ListView folderLV;
    ListView fileLV;
    Button beginButton;
    Button buttonBack;
    TextView textPlotas;
    ArrayList<File> folderArray = new ArrayList<>();
    ArrayList<String> foldersArrayString = new ArrayList<>();
    ArrayList<File> fileArray = new ArrayList<>();
    ArrayList<File> selectedFiles = new ArrayList<>();
    ArrayList<String> fileArrayString = new ArrayList<>();
    ArrayAdapter folderAdapter;
    ArrayAdapter fileAdapter;
 //    PointsGraphSeries<DataPoint> series;
 //   PointsGraphSeries<DataPoint> series2;
    GraphView rezultataiGraph;
    ArrayList<Integer> MonthArray = new ArrayList<>();
    ArrayList<Integer> DayArray = new ArrayList<>();
    ArrayList<Integer> HourArray = new ArrayList<>();
    ArrayList<Integer> MinuteArray = new ArrayList<>();
    ArrayList<Double> MLX_Array = new ArrayList<>();
    ArrayList<Double> DS_Array = new ArrayList<>();
    ArrayList<String> Name = new ArrayList<>();
    ArrayList<Integer> Numbers= new ArrayList<>();
    ArrayList<Double> MLX_data = new ArrayList<>();
    ArrayList<Double> DS_data = new ArrayList<>();
    double[] minmaxMLX = new double[2];
    double[] minmaxDS = new double[2];
    ArrayList<Double> Norm_MLX,Norm_DS= new ArrayList<Double>();
    int FileFolderFlag;
    int graphDataPointLength ;
    int fileCount;
    int optionsDiena;
    int optionsMenuo;
    GridLabelRenderer gridLabel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rezultatai);
        textPlotas=(TextView)findViewById(R.id.textPlotas) ;
        buttonBack = (Button)findViewById(R.id.buttonBack);
        buttonBack.setVisibility(View.INVISIBLE);
        FileFolderFlag =0;
        buttonBack.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v){
                if (FileFolderFlag==1){
                    selectedFiles.clear();
                    folderList();
                    folderLV.setVisibility(View.VISIBLE);
                    fileLV.setVisibility(View.INVISIBLE);
                    buttonBack.setVisibility(View.INVISIBLE);
                }
            }
        });
        fileCount=0;
        beginButton = (Button)findViewById(R.id.beginButton);
        beginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG,"1");
                if (selectedFiles.size()!=0) {
                    Log.e(TAG,"2");
                    fileCount = 0;
                    optionsDiena=optionsMenuo=0;
                    alertPrompt();
                }
            }
        });
        folderAdapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        fileAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice);
        fileLV = (ListView) findViewById(R.id.fileLV);
        fileLV.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        fileLV.setOnItemClickListener(rezultataiActivity.this);
        folderLV = (ListView) findViewById(R.id.folderLV);
        folderLV.setOnItemClickListener(rezultataiActivity.this);
        rezultataiGraph = (GraphView) findViewById(R.id.rezultataiGraph);
        gridLabel = rezultataiGraph.getGridLabelRenderer();

        folderList();
    }
    void alertPrompt(){
        List<CharSequence> mHelperNames = new ArrayList<CharSequence>();
        mHelperNames.add("Dienos rezultatai");
        mHelperNames.add("Mėnesio rezultatai");
        CharSequence[] array = {"Dienos rezultatai", "Mėnesio rezultatai"};
        final List<Integer> mSelectedItems = new ArrayList<Integer>();

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nustatymai")
                .setSingleChoiceItems(array,-1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            dialog.dismiss();
                            /// runOnUiThread(new Runnable() {
                            ///     @Override
                            ///     public void run() {
                            optionsDiena = 1;
                            optionsMenuo = 0;
                            rezultataiGraph = null;
                            rezultataiGraph = (GraphView) findViewById(R.id.rezultataiGraph);
                            rezultataiGraph.removeAllSeries();
                            rezultataiGraph.refreshDrawableState();
                            startGraph();
                            ///           }
                            ///       });
                              }
                            if (which == 1) {
                                dialog.dismiss();
                                //     runOnUiThread(new Runnable() {
                                //         @Override
                                //         public void run() {

                                optionsDiena = 0;
                                optionsMenuo = 1;
                                rezultataiGraph = null;
                                rezultataiGraph = (GraphView) findViewById(R.id.rezultataiGraph);
                                rezultataiGraph.removeAllSeries();
                                rezultataiGraph.refreshDrawableState();
                                startGraph();
                                //           }
                                //       });
                                //   }
                                //      }
                            }
                        }
                });
        builder.show();
    }
    private void startGraph() {
        MLX_data.clear();
        DS_data.clear();
        MLX_Array.clear();
        DS_Array.clear();
        Name.clear();
        Numbers.clear();
        MinuteArray.clear();
        HourArray.clear();
        MonthArray.clear();
        DayArray.clear();
        Log.d(TAG, "entered startGraph"+selectedFiles);
       // matavimaiGraph.removeAllSeries();
        for (int i = 0;i<selectedFiles.size();i++){
            fileNameScan(selectedFiles.get(i),i);
            Log.e(TAG, selectedFiles.get(i).getName());
        }
        for (int i = 0;i<selectedFiles.size();i++){
            valuesToFloat(selectedFiles.get(i));
        }
        if (optionsDiena==1){
        plotDay();}
        if (optionsMenuo==1){
            plotMonth();}
    }
    void valuesToFloat(File file){
        Log.d(TAG, "entered startGraph"+file);
        double[] rez = new double[2];
        try {
            Scanner s = new Scanner(file);
            while (s.hasNextFloat()) {
                MLX_data.add(s.nextDouble());
                DS_data.add(s.nextDouble());
            }
            s.close();
            rez = calculate(MLX_data, DS_data);
            MLX_Array.add(rez[1]);
            DS_Array.add(rez[0]);
         //   plotDay();
           // plotMonth();
        }catch (FileNotFoundException E){
            Log.e(TAG,E.getLocalizedMessage());
        }
    }

    double[] calculate(ArrayList<Double> MLX,ArrayList<Double> DS){
        Log.d(TAG,"Entered calculateArea");
        int startPoint;
        int i =0;
        int count=0;
        double Rezultatai[] = new double[2];
        minmaxDS(DS);
        minmaxMLX(MLX);
        DecimalFormat df = new DecimalFormat("#.###");

        Norm_MLX=normalize(minmaxMLX,MLX);
        Norm_DS=normalize(minmaxDS,DS);
        for (i=0;i<DS_data.size();i++) {
            if (Norm_DS.get(i) > 0.05 && Norm_DS.get(i)<0.9) {
                Rezultatai[0] = Rezultatai[0]+(minmaxDS[1]-DS_data.get(i));
            }
            if (Norm_MLX.get(i)>0.95){
                count++;
                Rezultatai[1]=Rezultatai[1]+MLX.get(i);

            }
        }
        Rezultatai[1]=Rezultatai[1]/count;
        Rezultatai[1]=0.96*5.67E-8* Math.pow( Rezultatai[1],4);
        Rezultatai[1] = Double.valueOf(df.format( Rezultatai[1]));
        Rezultatai[0] = Double.valueOf(df.format( Rezultatai[0]));
        return Rezultatai;
    }

    private ArrayList<Double> normalize(double[] data,ArrayList<Double> list) {
        Log.d(TAG,"Entered normalize");
        double K;
        ArrayList<Double> norm_list = new ArrayList<Double>();
        for (int i=0;i<list.size();i++){
            norm_list.add((list.get(i)-data[0])/(data[1]-data[0]));

        }
        return norm_list;
    }
    private void minmaxMLX(ArrayList<Double> data) {
        Log.d(TAG,"Entered minmaxMLX");
        double min=0xFFFF;
        double max = 0;

        for (int i =0;i<data.size();i++){
            if (data.get(i)>max){
                max=data.get(i);
            }
            if (data.get(i)<min){
                min=data.get(i);
            }
        }
        minmaxMLX[0]=min;
        minmaxMLX[1]=max;
    }

    private void minmaxDS(ArrayList<Double> data) {
        Log.d(TAG,"Entered minmaxDS");
        double min=0xFFFF;
        double max = 0;

        for (int i =0;i<data.size();i++){
            if (data.get(i)>max){
                max=data.get(i);
            }
            if (data.get(i)<min){
                min=data.get(i);
            }
        }
        minmaxDS[0]=min;
        minmaxDS[1]=max;
    }

    private int[] findMinMax(ArrayList<Integer> big, ArrayList<Integer> small) {
        Log.d(TAG,"Entered findMinMax");
        int[] res = new int[4];
        int minBIG =  big.get(0);
        int maxBIG =  big.get(0);
        int minSMALL =  35;
        int maxSMALL =  0;
        int valueBIG;
        int valueSMALL;
        for(int i =0; i<big.size();i++) {
            valueBIG = big.get(i);
            valueSMALL = small.get(i);
            if(valueBIG < minBIG) minBIG = valueBIG;
            if(valueBIG > maxBIG) maxBIG = valueBIG;
        }
        for (int i =0; i<big.size();i++){
            if (big.get(i).equals(minBIG))
            {if (minSMALL>small.get(i)){
                minSMALL=small.get(i);
            }}
            if (big.get(i).equals(maxBIG))
            {if (maxSMALL<small.get(i)){
                maxSMALL=small.get(i);
            }}
        }
        res[0]=maxBIG;
        res[1]=minBIG;
        res[2]=maxSMALL;
        res[3]=minSMALL;
        return res;
    }

    private void plotDay() {

        ArrayList<Time> laikas = new ArrayList<>();
        Time temp_time;
        Log.d(TAG,"Entered plotDay");
        for (int i =0;i<selectedFiles.size();i++){
            temp_time= new Time(HourArray.get(i),+MinuteArray.get(i),0);
            laikas.add(temp_time);
        }
        Log.d(TAG,"laikas   "+laikas.toString());
        //maxBIG    minBIG  maxSMALL    minSMALL;
        int[] minmax = new int[4];
        minmax=findMinMax(HourArray,MinuteArray);
        Log.d(TAG, "maxH "+minmax[0]+"minH "+minmax[1]+"maxM "+minmax[2]+"minM "+minmax[3]);
      // Date date = new Date(97, 1, 23);
       // Time timeMIN = new Time(minmax[1],+minmax[3],0);
        //  Time timeMAX = new Time(minmax[0],+minmax[2],0);
        Time timeMIN = new Time(7,0,0);
        Time timeMAX = new Time(24,0,0);
        Calendar calendar = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
      //  calendar.add(Calendar.HOUR,minmax[1]);
       // calendar.add(Calendar.MINUTE,minmax[3]);
        Date d1 = calendar.getTime();
       // calendar2.add(Calendar.HOUR,minmax[0]);
       // calendar2.add(Calendar.MINUTE,minmax[2]);
        Date d2 = calendar2.getTime();
       // long diff = date.getTime();
        //DateFormat HourMinute = new DateFormat.getDateTimeInstance();
        DataPoint[] points = new DataPoint[selectedFiles.size()];
        DataPoint[] points2 = new DataPoint[selectedFiles.size()];
        for (int j=0;j<points.length;j++) {
            Double value = MLX_Array.get(j);
            value=value*10000;
            points[j]=new DataPoint(laikas.get(j), value);
            points2[j]=new DataPoint(laikas.get(j), DS_Array.get(j));
            Log.e(TAG,"Laikas   "+laikas.get(j));
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        rezultataiGraph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(rezultataiActivity.this,dateFormat));
        rezultataiGraph.getViewport().setMinX(timeMIN.getTime());
        rezultataiGraph.getViewport().setMaxX(timeMAX.getTime());
        rezultataiGraph.getViewport().setXAxisBoundsManual(true);
        PointsGraphSeries<DataPoint>series = new PointsGraphSeries<>(points);
        series.setColor(Color.RED);
        series.setTitle("MLX");
        rezultataiGraph.getLegendRenderer().setVisible(true);
        rezultataiGraph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        PointsGraphSeries<DataPoint>series2 = new PointsGraphSeries<>(points2);
        series2.setTitle("DS");
        rezultataiGraph.getLegendRenderer().setVisible(true);
        rezultataiGraph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        Log.d(TAG,"        "+timeMIN.getTime()+"        "+timeMAX.getTime());
        Log.d(TAG,"min "+timeMIN+"        max "+timeMAX);
        rezultataiGraph.addSeries(series);
        rezultataiGraph.addSeries(series2);
        laikas.clear();


    }


    private void plotMonth() {
        Date[] Data = new Date[selectedFiles.size()];
        Date temp_time = new Date();
        Log.d(TAG,"Entered plotMonth");
        Calendar calendar3 = Calendar.getInstance();
        for (int i =0;i<selectedFiles.size();i++){
            calendar3.clear();
            calendar3.set(Calendar.YEAR,2017);
            calendar3.set(Calendar.MONTH,MonthArray.get(i)-1);
            calendar3.set(Calendar.DAY_OF_MONTH,DayArray.get(i));
          //  temp_time.setTime(calendar3.getTime());

            Data[i]=calendar3.getTime();
            Log.d(TAG,"laikas   "+calendar3.getTime().getTime()+"  "+calendar3.getTime());
            Log.d(TAG,"Data   "+Data[i]);
        }
        for (int i=0;i+1<Data.length;i++){
           ArrayList<Integer> ii = new ArrayList<>(Data.length);
            if (!ii.contains(i)) {
                for (int j = 1; j < Data.length; j++) {
                    if (Data[i].getTime() == Data[j].getTime() && i != j) {
                        Log.e(TAG, Data[i] + "    " + Data[j]);
                        MLX_Array.set(i, (MLX_Array.get(i) + MLX_Array.get(j)) / 2);
                        DS_Array.set(i, (DS_Array.get(i) + DS_Array.get(j)) / 2);
                        ii.add(j);
                        MLX_Array.remove(j);
                        DS_Array.remove(j);
                    }
                }
            }
        }
        //maxBIG    minBIG  maxSMALL    minSMALL;
        int[] minmax = new int[4];
        minmax=findMinMax(MonthArray,DayArray);
        Log.d(TAG, "maxH "+minmax[0]+"minH "+minmax[1]+"maxM "+minmax[2]+"minM "+minmax[3]);
        Calendar calendar = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR,2017);
        calendar.set(Calendar.MONTH,minmax[1]-1);
        calendar.set(Calendar.DAY_OF_MONTH,minmax[3]);
        Date d1 = calendar.getTime();
        calendar2.clear();
        calendar2.set(Calendar.YEAR,2017);
        calendar2.set(Calendar.MONTH,minmax[0]-1);
        calendar2.set(Calendar.DAY_OF_MONTH,minmax[2]);
        Date d2 = calendar2.getTime();
        Date minDate = new Date(2017,minmax[1],minmax[3]);
        Date maxDate = new Date(2017,minmax[0],minmax[2]);
        Log.d(TAG,"d1 "+ d1+" d2  "+d2);

        Log.d(TAG,"        "+d1.getTime()+"        "+d2.getTime());

        DataPoint[] points2 = new DataPoint[MLX_Array.size()];
        DataPoint[] points = new DataPoint[MLX_Array.size()];
        Arrays.fill(points2,null);
        Arrays.fill(points,null);
        for (int j=0;j<points.length;j++) {
            Double value = MLX_Array.get(j);
            value=value*10000;
            points[j]=new DataPoint(Data[j].getTime(), value);
            points2[j]=new DataPoint(Data[j].getTime(), DS_Array.get(j));
            Log.d(TAG, "Points  " + value+" points2 "+ DS_Array.get(j));
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd");
        rezultataiGraph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(rezultataiActivity.this,dateFormat));
      //  rezultataiGraph.getGridLabelRenderer().setHumanRounding(false);
        PointsGraphSeries<DataPoint> series1 = new PointsGraphSeries<>(points);
        PointsGraphSeries<DataPoint> series2 = new PointsGraphSeries<>(points2);
        series1.setTitle("MLX");
        rezultataiGraph.getLegendRenderer().setVisible(true);
        rezultataiGraph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        series2.setTitle("DS");
        rezultataiGraph.getLegendRenderer().setVisible(true);
        rezultataiGraph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        rezultataiGraph.addSeries(series1);
        rezultataiGraph.addSeries(series2);
        series1.setColor(Color.RED);
        rezultataiGraph.getViewport().setMinX(d1.getTime());
        rezultataiGraph.getViewport().setMaxX(d2.getTime());
        rezultataiGraph.getViewport().setXAxisBoundsManual(true);
    }

    private double maxArray(ArrayList<Double> array) {
        double max = array.get(0);
        for (int i =0;i<array.size();i++){
            if (array.get(i)>max) max=array.get(i);
        }
        return max;
    }



    void fileNameScan(File file, int count){
        int i = count;
        Log.d(TAG, "entered fileNameScan"+file.getName());
        Name.add(file.getName());
        Log.d(TAG,"   "+Name);
        int nameSize = Name.get(i).length();
        for (int ii = 0; ii < nameSize; ii++) {
            char c =  Name.get(i).charAt(ii);
            if (Character.isDigit(c)) {

               Numbers.add(Character.getNumericValue(c));
            }
        }
        int numberLength = Numbers.size();
        Log.e(TAG,"numberLength   "+numberLength);
        MinuteArray.add(Numbers.get(numberLength-2)*10+Numbers.get(numberLength-1));  Log.d(TAG,"1");
        HourArray.add(Numbers.get(numberLength-4)*10+Numbers.get(numberLength-3));  Log.d(TAG,"2");
        DayArray.add(Numbers.get(numberLength-6)*10+Numbers.get(numberLength-5));  Log.d(TAG,"3");
        MonthArray.add(Numbers.get(numberLength-8)*10+Numbers.get(numberLength-7));  Log.d(TAG,"4");
        fileCount++;
        Log.d(TAG,"Post convertion   "+Numbers);
        Numbers.clear();
        
    }

    public void folderList() {
        folderLV.setVisibility(View.VISIBLE);
        fileLV.setVisibility(View.INVISIBLE);
        folderAdapter = new ArrayAdapter(rezultataiActivity.this, android.R.layout.simple_list_item_1, foldersArrayString );
        folderAdapter.clear();
        folderAdapter.notifyDataSetChanged();
        String path = Environment.getExternalStorageDirectory().toString() + "/Duomenys";
        Log.d(TAG, "Path: " + path);
        File directory = new File(path);
        File[] folders = directory.listFiles();
        Log.d("Files", "Size: " + folders.length);
        for (int i = 0; i< folders.length; i++){
            Log.d("Files", "FileName: " + folders[i].getName());
            folderArray.add(folders[i]);
            foldersArrayString.add(folders[i].getName());
            folderAdapter.notifyDataSetChanged();
        }
        folderLV.setAdapter(folderAdapter);
    }
    public void fileList(File folder) {
        FileFolderFlag =1;
        buttonBack.setVisibility(View.VISIBLE);
        folderLV.setVisibility(View.INVISIBLE);
        folderAdapter.clear();
        fileLV.setVisibility(View.VISIBLE);
        fileAdapter = new ArrayAdapter(rezultataiActivity.this, android.R.layout.simple_list_item_multiple_choice, fileArrayString );
        Log.e(TAG, "Entered fileList");
        String selectedFolder = folder.getName().toString();
        String path = Environment.getExternalStorageDirectory().toString() + "/Duomenys"+ "/" + selectedFolder;
        Log.d(TAG, "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: " + files.length);
        for (int i = 0; i< files.length; i++){
            Log.d("Files", "FileName: " + files[i].getName());
            fileArray.add(files[i]);
            fileArrayString.add(files[i].getName());
            fileAdapter.notifyDataSetChanged();
        }
        fileLV.setAdapter(fileAdapter);
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Log.d(TAG, "entered onitemclick");
        if (parent==folderLV){
            Log.d(TAG, "parent folderLV");
            fileArray.clear();
            fileAdapter.clear();
            fileArrayString.clear();
            fileAdapter.notifyDataSetChanged();
            fileList(folderArray.get(position));

        }
        if (parent==fileLV){
            Log.d(TAG, "parent fileLV");
            if (!selectedFiles.contains(fileArray.get(position))) {
                selectedFiles.add(fileArray.get(position));
            } else
            {  selectedFiles.remove(fileArray.get(position));}
        }
        Log.e(TAG,selectedFiles.toString());
    }
}
