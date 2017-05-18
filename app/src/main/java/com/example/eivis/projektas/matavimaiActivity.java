package com.example.eivis.projektas;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class matavimaiActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private static final String TAG = "matavimaiActivity";
    ArrayList<File> folderArray = new ArrayList<>();
    ArrayList<String> foldersArrayString = new ArrayList<>();
    ArrayList<File> fileArray = new ArrayList<>();
    ArrayList<File> selectedFiles = new ArrayList<>();
    ArrayList<String> fileArrayString = new ArrayList<>();
    ArrayList<Double> MLX_data = new ArrayList<>();
    ArrayList<Double> DS_data = new ArrayList<>();
    ArrayAdapter folderAdapter;
    ArrayAdapter fileAdapter;
    ListView folderLW;
    ListView fileLW;
    Button plotButton;
    Button atgal;
    TextView PlotuTV;
    TextView SpindTV;
    TextView SpindNameTV;
    TextView PltuPavTV;
    LineGraphSeries<DataPoint> series;
    GraphView matavimaiGraph;
    int calcMLX;
    int calcDS;
    int plotMLX;
    int plotDS;
    double[] minmaxMLX = new double[2];
    double[] minmaxDS = new double[2];
    int[][] MonthDay = new int[12][31];
    int[][] HourMinute = new int[24][59];
    ArrayList<Double> Norm_MLX,Norm_DS= new ArrayList<Double>();
    int graphDataPointLength ;
    int FileFolderFlag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matavimai);
        ScrollView scroller1 = new ScrollView(this);
        ScrollView scroller2 = new ScrollView(this);
        PlotuTV =(TextView)findViewById(R.id.PlotuTV);
        SpindTV = (TextView)findViewById(R.id.SpindTV);
        PltuPavTV = (TextView)findViewById(R.id.PltuPavTV) ;
        SpindNameTV = (TextView)findViewById(R.id.SpindNameTV) ;
        PlotuTV.setMovementMethod(new ScrollingMovementMethod());
        SpindTV.setMovementMethod(new ScrollingMovementMethod());
        FileFolderFlag =0;
        folderAdapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        fileAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice);
        atgal = (Button)findViewById(R.id.atgal);
        atgal.setVisibility(View.INVISIBLE);
         atgal.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v){
                if (FileFolderFlag==1){
                    folderLW.setVisibility(View.VISIBLE);
                    fileLW.setVisibility(View.INVISIBLE);
                    atgal.setVisibility(View.INVISIBLE);
                }
            }
        });
        plotButton = (Button)findViewById(R.id.plotButton);
        plotButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v){
                plotMLX=0;
                plotDS=0;
                calcDS=0;
                calcMLX=0;
                SpindNameTV.setVisibility(View.INVISIBLE);
                PltuPavTV.setVisibility(View.INVISIBLE);
                alertPrompt();
               // plotGraphs();
            }
        });
        fileLW = (ListView) findViewById(R.id.fileLW);
        fileLW.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        fileLW.setOnItemClickListener(matavimaiActivity.this);
        folderLW = (ListView) findViewById(R.id.folderLW);
        folderLW.setOnItemClickListener(matavimaiActivity.this);
        series = new LineGraphSeries<DataPoint>();
        matavimaiGraph = (GraphView) findViewById(R.id.matavimaiGraph);
        matavimaiGraph.addSeries(series);
        Viewport viewport = matavimaiGraph.getViewport();
        matavimaiGraph.getViewport().setMinX(0);
        matavimaiGraph.getViewport().setMaxX(270);
        matavimaiGraph.getViewport().setMinY(0);
        matavimaiGraph.getViewport().setMaxY(1);

        matavimaiGraph.getViewport().setYAxisBoundsManual(true);
        matavimaiGraph.getViewport().setXAxisBoundsManual(true);
        folderList();
    }
    void alertPrompt(){
        List<CharSequence> mHelperNames = new ArrayList<CharSequence>();
        mHelperNames.add("Atlikti MLX skaičiavimus");
        mHelperNames.add("Atlikti TMP skaičiavimus");
        mHelperNames.add("Brėžti MLX grafikus");
        mHelperNames.add("Brėžti TMP grafikus");

        final List<Integer> mSelectedItems = new ArrayList<Integer>();

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nustatymai").setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        plotGraphs();
                    }
                });
            }
        })

                .setMultiChoiceItems(mHelperNames.toArray(new CharSequence[mHelperNames.size()]), null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (which==0){
                                if(calcMLX==0){
                                    calcMLX=1;
                                }else { calcMLX=0;}
                            }
                                if (which==1){
                                if(calcDS==0){
                                    calcDS=1;
                                }else { calcDS=0;}
                            }
                                if (which==2){
                                    if(plotMLX==0){
                                        plotMLX=1;
                                    }else { plotMLX=0;}
                                }
                                if (which==3){
                                    if(plotDS==0){
                                        plotDS=1;
                                    }else { plotDS=0;}
                                }
                               /* {mSelectedItems.add(which);
                                    if (which==0){
                                        plotMLX=1;
                                    }
                                    if (which==1){
                                        plotDS=1;
                                    }
                                } else if (mSelectedItems.contains(which))
                                {mSelectedItems.remove(Integer.valueOf(which));
                                    if (which==0){
                                        plotMLX=0;
                                    }
                                    if (which==1){
                                        plotDS=0;
                                    }
                                }*/
                            }
                        });
        builder.show();
                /*
        builder.setItems(stringArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            Log.d(TAG," Dialog onClick entered");
            }
        });
        */
    }
    /*
    * Aplanku /Doumenys/ sarasas, i Array<File> ir Array<String>
    * */
    public void folderList() {
        folderLW.setVisibility(View.VISIBLE);
        fileLW.setVisibility(View.INVISIBLE);
        folderAdapter = new ArrayAdapter(matavimaiActivity.this, android.R.layout.simple_list_item_1, foldersArrayString );
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
        folderLW.setAdapter(folderAdapter);
    }
    public void fileList(File folder) {
        FileFolderFlag =1;
        atgal.setVisibility(View.VISIBLE);
        folderLW.setVisibility(View.INVISIBLE);
        fileLW.setVisibility(View.VISIBLE);
        fileAdapter = new ArrayAdapter(matavimaiActivity.this, android.R.layout.simple_list_item_multiple_choice, fileArrayString );
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
        fileLW.setAdapter(fileAdapter);
    }
    void valuesToFloat(File file){
        double[] rez = new double[2];
        try {
            Scanner s = new Scanner(file);
            while (s.hasNextFloat()) {
                MLX_data.add(s.nextDouble());
                DS_data.add(s.nextDouble());
            }
            s.close();
            rez =calculate(MLX_data,DS_data);
            if (calcMLX==1) {
                String failas = file.getName().toString();
                String verte = String.valueOf(rez[1]);
                SpannableString str = new SpannableString(failas +" - "+ verte+"\n\n");
                str.setSpan(new StyleSpan(Typeface.BOLD), failas.length()+1,failas.length()+3+verte.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
               // SpindTV.append("\n " + file.getName() + " " +" <b>"+rez[1]+"</b>");
                SpindTV.append(str);
                SpindNameTV.setVisibility(View.VISIBLE);

            }
            if (calcDS==1) {
                String failas = file.getName().toString();
                String verte = String.valueOf(rez[0]);
                SpannableString str = new SpannableString(failas +" - "+ verte+"\n\n");
                str.setSpan(new StyleSpan(Typeface.BOLD), failas.length()+1,failas.length()+3+verte.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                // SpindTV.append("\n " + file.getName() + " " +" <b>"+rez[1]+"</b>");
                PlotuTV.append(str);
               // PlotuTV.append("\n "+file.getName()+" "+rez[0]);
                PltuPavTV.setVisibility(View.VISIBLE);

            }
            double energija;
            if (plotMLX==1) {

                makeGraph(Norm_MLX);
                series.setTitle(file.getName() + " MLX");
                matavimaiGraph.getLegendRenderer().setTextSize(14f);
                matavimaiGraph.getLegendRenderer().setVisible(true);
                matavimaiGraph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
            }

            if (plotDS==1) {

                makeGraph(Norm_DS);
                series.setTitle(file.getName() + " TMP");
                matavimaiGraph.getLegendRenderer().setTextSize(14f);
                matavimaiGraph.getLegendRenderer().setVisible(true);
                matavimaiGraph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
            }

        }
        catch (FileNotFoundException E){
            Log.e(TAG,E.getLocalizedMessage());
        }
    }



    void makeGraph(ArrayList<Double> list){
        Log.d(TAG,"Entered makeGraph");
        Random rn = new Random();
        graphDataPointLength = list.size();
        DataPoint[] points = new DataPoint[graphDataPointLength];
        for (int j=0;j<points.length;j++) {
            points[j]=new DataPoint(j, list.get(j));
        }
        series = new LineGraphSeries<>(points);
        series.setColor(Color.rgb((rn.nextInt(255 - 0 + 1) + 0),(rn.nextInt(255 - 0 + 1) + 0),(rn.nextInt(255 - 0 + 1) + 0)));
        matavimaiGraph.addSeries(series);

    }
    void plotGraphs(){
        PlotuTV.setText("");
        SpindTV.setText("");
        Log.d(TAG, "entered plotGraphs"+selectedFiles);
        matavimaiGraph.removeAllSeries();
    for (int i = 0;i<selectedFiles.size();i++){
        MLX_data.clear();
        DS_data.clear();
        valuesToFloat(selectedFiles.get(i));
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "entered onitemclick");
        if (parent==folderLW){
            fileArray.clear();
            fileAdapter.clear();
            fileArrayString.clear();
            fileAdapter.notifyDataSetChanged();
            fileList(folderArray.get(position));
        }
        if (parent==fileLW){

            if (selectedFiles.contains(fileArray.get(position))) {
               selectedFiles.remove(fileArray.get(position));
            } else
            { selectedFiles.add(fileArray.get(position));}
        }
    }
}
