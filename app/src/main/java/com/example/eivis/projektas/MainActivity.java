package com.example.eivis.projektas;

import android.Manifest;
import android.animation.TypeConverter;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import static java.lang.Math.round;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private static final String TAG = "MainActivity";
    int searchState=0;
    public int connectedState =0;
    Button connectDevice;
    Button transferButton;
    Button displayData;
    TextView textState;
    ListView folderListView;
    BluetoothAdapter btAdapter;
    BluetoothDevice btDevice;
    BluetoothGatt btGatt;
    Button startButton;
    String subjectT;
    String WriteFolder;
    Button createFolder;
    File fileName = null;
    float dataAdd1 = 0;
    float dataAdd2 = 0;
    double value1 = 0;
    double value2 = 0;
    int count=0;
    byte inputStream1 = 0;
    byte inputStream2 = 0;
    byte inputStream3 = 0;
    byte inputStream4 = 0;
    BluetoothGattDescriptor gattDesc = null;
    BluetoothGattCharacteristic gattChar = null;
    BluetoothGattCharacteristic gattCharWrite = null;
    IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
    Handler handler = new Handler();
    int i;
    int sendStatus;
    byte[] dataCollected;
    byte[][] testArray;
    int dataCollectedLenght;
    int arrayNR;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    int updateCount;
    int inputStreamCount;
    String sdf[];
    AlertDialog.Builder alertDialog;
    String newFolderName = null;
    ArrayList<File> folderToWrite = new ArrayList<>();
    ArrayList<File> foldersAvailible = new ArrayList<>();
    ArrayList<String> foldersAvailibleString = new ArrayList<>();
    ArrayAdapter<String> foldersAvailibleAdapter;
    File selectedFolder;
    int calledFromSS;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectedFolder = null;
        dataCollected = new byte[41];
        testArray = new byte[270][4];
        sdf = new String[270];
        folderListView = (ListView) findViewById(R.id.folderListView);
        folderListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        folderListView.setOnItemClickListener(MainActivity.this);
        foldersAvailibleAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = bluetoothManager.getAdapter();
        connectDevice = (Button) findViewById(R.id.connectDevice);
        textState = (TextView) findViewById(R.id.textState);
        connectDevice.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                folderDuom();
                startScanning();
                textState.setText("Ieškoma jutiklio...");
            }
        });
        startButton = (Button) findViewById(R.id.startButton);
        startButton.setVisibility(View.INVISIBLE);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                createFT();
                beginTest();
            }
        });
        displayData = (Button) findViewById(R.id.displayData);
        displayData.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,displayActivity.class));
            }
        });
        transferButton = (Button) findViewById(R.id.transferButton);
        transferButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                startActivity(new Intent(MainActivity.this,btActivity.class));
            }
        });

        registerReceiver(mReceiver, filter);
        alertDialog = new AlertDialog.Builder(MainActivity.this);
        createFolder = (Button) findViewById(R.id.createFolder);
        createFolder.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                folderList();
                alertPrompt();
                folderList();
            }
        });

        btState();

/*
* Tikrina ar Marshmellow, reik papildomu permission tinrinimu
* ACCESS_COARSE_LOCATION arba ACCESS_FINE_LOCATION
* */
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_DENIED) {

                Log.d("permission", "permission denied  - requesting it");
                String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION};

                requestPermissions(permissions, PERMISSION_REQUEST_COARSE_LOCATION);

            }
        }
        verifyStoragePermissions(MainActivity.this);

    }
    public static void verifyStoragePermissions(Activity activity) {
        Log.e(TAG,"Entered folderDuom");
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,1

            );
        }
    }
    private void folderDuom() {
        File folderDuomenys = new File(Environment.getExternalStorageDirectory() +
                File.separator + "/Duomenys/");
        Log.e(TAG,"Entered folderDuom");
        boolean success = true;
        if (!folderDuomenys.exists()) {
            Log.e(TAG,"Entered 1");
            success = folderDuomenys.mkdir();
        } else
        {Log.e(TAG,"Entered 2");

        }
        if (success) {
            Log.e(TAG,"Entered 3");

        } else {
            Log.e(TAG,"Entered 4");

        }
    }

    /*
    * Naujo aplanko "input+MM-dd" kurimo pop up
    * */
    void alertPrompt(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Įveskite pavadinimą");
    // Set up the input
        final EditText input = new EditText(MainActivity.this);
    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
    // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newFolderName= input.getText().toString();
                createFF(newFolderName);
                folderList();
            }
        });
        builder.setNegativeButton("Atšaukti", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    /*
    * Tikrina BT busena
    * */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        btState();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        textState.setText("Bluetooth išjungiamas...");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        btState();
                        if (calledFromSS==0){
                            calledFromSS=1;
                            startScanning();
                        }
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        textState.setText("Bluetooth įjungiamas...");
                        break;
                }
            }
        }
    };

    /*
    * On/Off BT mygtukas
    *
    private void btONOFF(){
        if (btAdapter.isEnabled()){
            btAdapter.disable();
            btState();
        } else {
            Intent enablebtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enablebtIntent, 1);
            btState();
        }
    }
    */
    private void btCheck(){
            calledFromSS=0;
            Intent enablebtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enablebtIntent, 1);
            btState();
    }
    /*
    * Pradine BT busena
    * */
    public void btState(){
        if (! btAdapter.isEnabled()){
            textState.setText("Bluetooth išjungtas");
            connectDevice.setVisibility(View.VISIBLE);

        }else {
            textState.setText("Bluetooth įjungtas");
            connectDevice.setVisibility(View.VISIBLE);

        }
    }
    /*
    * Jei nesusijungta su jutikliu connectedState == 0 :
    * ijungia skenavima, jei jau skenuojama resetina skenavima
    * Jei susijungta su jutikliu: connectedState == 1 :
    * Atsijungia juo jutiklio
    * */
    public void startScanning() {
        if (! btAdapter.isEnabled()) {
            btCheck();
        }
        else {
            textState.setText("Ieškoma jutiklio");
            if (connectedState == 0) {
                Log.d(TAG, "Start scan");
                if (searchState == 0) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            btAdapter.startLeScan(leScanCallback);
                            searchState = 1;
                        }
                    });
                } else {
                    btAdapter.stopLeScan(leScanCallback);
                    btAdapter.startLeScan(leScanCallback);
                    searchState = 1;
                }
            }
            if (connectedState == 1) {
                btGatt.disconnect();
                btGatt.close();
                btGatt = null;
                connectedState = 0;
                connectDevice.setText("Prisijungti prie jutiklio");
            }
        }
    }
    /*
    * Nutraukia skenavima po to kai prisijngta pire jutiklio
    * */
    public void stopScanning() {
        Log.d(TAG, "Stop scan");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btAdapter.stopLeScan(leScanCallback);
            }
        });
        searchState = 0;
    }
    /*
    * Automatiksai isijungia prisijungus prie BTLE
    * */
    private BluetoothAdapter.LeScanCallback leScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Added " +device+ " " +device.getName());
                            if (device.getAddress().equals("C9:D9:FF:EA:03:C4"))
                            {
                                btDevice = device;
                                textState.setText("Jutiklis rastas\n Jungiamasi...");
                                stopScanning();
                                Log.e(TAG, "Rastas " + btDevice.getAddress() + btDevice.getName());
                                btGatt = btDevice.connectGatt(getApplicationContext(),false,mGattCallback);
                            }
                        }
                    });
                }
            };
            private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
                /*
                * onConnectionStateChange pasikeite busena.
                * Jei i connected - atisdaro variantai tolimesnei eigai, pradeda discoverServices
                * Jei i disconnected - atvirksicai
                * */
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    super.onConnectionStateChange(gatt, status, newState);
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        connectedState = 1;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                folderList();
                                folderListView.setVisibility(View.VISIBLE);
                                textState.setText("Prisijungta prie jutiklio");
                                connectDevice.setText("Atsijungti nuo jutiklio");

                            }
                        });

                        btGatt = gatt;
                        gatt.discoverServices();
                        Log.e(TAG, "Connected");
                    }
                    if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textState.setText("Atsijungta nuo jutiklio");
                                connectDevice.setText("Prisijungti prie jutiklio");
                                startButton.setVisibility(View.INVISIBLE);
                            }
                        });
                        connectedState = 0;
                        Log.e(TAG, "Disconnected");
                    }
                }
                /*
                * Isijungia nuo discoverServices(), is zinomu UUID gauna read,write charakreristiaks, read descriptor.
                * */
                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    super.onServicesDiscovered(gatt, status);
                    BluetoothGattCharacteristic characteristic = gatt.getService(UUID.fromString("00001234-0000-1000-8000-00805F9B34FB")).getCharacteristic(UUID.fromString("00001236-0000-1000-8000-00805F9B34FB"));
                    gattChar = characteristic;
                    BluetoothGattCharacteristic writeChar = gatt.getService(UUID.fromString("00001234-0000-1000-8000-00805F9B34FB")).getCharacteristic(UUID.fromString("00001235-0000-1000-8000-00805F9B34FB"));
                    gattCharWrite = writeChar;
                    BluetoothGattDescriptor desc = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805F9B34FB"));
                    gattDesc = desc;
                    //startNotifications();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startButton.setVisibility(View.VISIBLE);

                        }
                    });
                }
                /*
                * Kai pasikeicia stebima chrakteristika.
                * Kas 7 issaugo (260ms) pasiekus 50 issiuncia i addToTXT() ir atjungia onCharacteristicChanged
                * stebejima
                * */
                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    super.onCharacteristicChanged(gatt, characteristic);
                   // final byte[] dataInput = characteristic.getValue();
                    Log.e(TAG,"  "+updateCount);
                    if (inputStreamCount==6){
                       if (arrayNR==3){
                           runOnUiThread(new Runnable() {
                               @Override
                               public void run() {
                                   textState.setText("Pasiruoškite uždėti pirštą po 4 sekundžių");

                               }
                           });
                       }
                        if (arrayNR==7){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textState.setText("Pasiruoškite uždėti pirštą po 3 sekundžių");

                                }
                            });
                        }
                        if (arrayNR==11){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textState.setText("Pasiruoškite uždėti pirštą po 2 sekundžių");

                                }
                            });
                        }
                        if (arrayNR==15){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textState.setText("Pasiruoškite uždėti pirštą po 1 sekundės");;

                                }
                            });
                        }
                        if (arrayNR==19){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textState.setText("Laikykite uždėję pirštą");

                                }
                            });
                        }
                        Log.e(TAG,"Chara 5");
                        inputStreamCount=0;
                    byte[] lol=characteristic.getValue();
                    testArray[arrayNR]=characteristic.getValue();
                    arrayNR++;
                    sdf[updateCount]= new SimpleDateFormat("mm:ss.SSS").format(new Date());
                    updateCount++;
                        Log.e(TAG,"lol "+lol);
                    if (updateCount==270)
                    {stopNotifications();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textState.setText("Matavimas baigtas");

                            }
                        });
                        addToTXT(testArray);}}
                    else {inputStreamCount++;}
                    // for (int j = 0; j < dataInput.length; j++) {
                  //     dataCollected[dataCollectedLenght] = dataInput[j];
                  //     dataCollectedLenght++;
                  //  }


                }

                @Override
                public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    super.onDescriptorRead(gatt, descriptor, status);
                }

                @Override
                public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    super.onDescriptorWrite(gatt, descriptor, status);
                }
            };
            /*
            * Predejimo mygtukas, nusiresetina visi stebejimo kintamieji, isijungia startNotifications.
            * */
    public void beginTest() {
        arrayNR=0;
        i=0;
        dataCollectedLenght = 0;
        updateCount = 0;
        inputStreamCount=0;
        //startRepeatingTask();
        startNotifications();
        if (arrayNR==0){
            textState.setText("Pasiruoškite uždėti pirštą po 5 sekundžių");
        }
    }

/*
* 20 kartu kas 200ms sendStart(), po 20 kartojimu sustabdo atnaujimina ir i Log suraso surinktus duomenis
* */
    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
               // sendStart(); //this function can change value of mInterval.
                if (i==20)
                {new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        stopRepeatingTask();
                        Log.d("UI thread", "I am the UI thread");
                        i=0;
                      // for (int u = 0; u<dataCollected.length; u++){
                      // Log.d(TAG,"all data collected "+u+" "+ dataCollected[u]);}
                        for (int u = 0; u<testArray.length; u++){
                            for (int p = 0; p<testArray[u].length;p++){
                            Log.e(TAG,"all data collected "+u+" "+ testArray[u][p]);
                            }
                        }
                    }
                });
                    addToTXT(testArray);}
                else {i++;}
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                handler.postDelayed(mStatusChecker,200);
            }
        }
    };
    void startRepeatingTask() {
        mStatusChecker.run();
    }
    void stopRepeatingTask() {
        handler.removeCallbacks(mStatusChecker);
    }

    /*
    * Sukuria aplanka pagal pop up suvesta pavadinima "inpu MM-dd", jei toks yra ismeta atgal su Toast
    * */
    public void createFF(String name) {
        String time = DateFormat.format(" MM-dd",System.currentTimeMillis()).toString();
        boolean success = true;
        int i = 1;
        subjectT=name;
        Log.e(TAG,subjectT);
        File nameF = new File(Environment.getExternalStorageDirectory()+ "/" + "Duomenys", subjectT+time);
        if (!nameF.exists()) {
            nameF.mkdirs();
            WriteFolder = subjectT;
        }
        else {
            Toast.makeText(MainActivity.this, subjectT+" jau yra, badnykite kitą", Toast.LENGTH_LONG).show();
            return;
        }
    }
    /*
    * Sukuria txt faila
    * */
    public void createFT(){
    String time = DateFormat.format(" HH.mm",System.currentTimeMillis()).toString();
        Log.e(TAG,"Entered createFT");
        try {
            subjectT=selectedFolder.getName().toString();
            File txt = new File(Environment.getExternalStorageDirectory()+ "/" +"Duomenys" +"/"+selectedFolder+"/" );
            File filepath = new File(selectedFolder,subjectT+time+".txt");
            if (!filepath.exists()) {
                filepath.createNewFile();
            }
            fileName = filepath;
         //  FileWriter writer = new FileWriter(filepath);
         //  writer.append(WriteFolder);
         //  writer.flush();
         //  writer.close();
        } catch(IOException e) {
            e.printStackTrace();
            Log.e(TAG,"IOException");
        }
    }
    /*
    * Surinktus duomenis suraso i txt
    * */
    public void addToTXT(byte[][] inData) {
        Log.e(TAG, "addToTXT called");
        for (int i = 0; i < inData.length; i++) {
            for (int ii = 0; ii < inData[i].length; ii++) {
                if (ii == 0) {
                    inputStream1 = inData[i][ii];
                    dataAdd1 = (inputStream1 & 0x7F) << 8;
                }
                else if (ii == 1){
                    inputStream2 = inData[i][ii];
                    dataAdd1 = dataAdd1 + (inputStream2&0xFF);
                    count = 2;
                }
                else if (ii == 2){
                    inputStream3 = inData[i][ii];
                    dataAdd2 = (inputStream3&0x7F)<<8;
                    count = 3;
                }
                else if (ii == 3){
                    try {
                        inputStream4 = inData[i][ii];
                        int in5 = (((inputStream3&0x7F)<<8)|(inputStream4&0xFF))>>5;
                      //  dataAdd2 = ((dataAdd2  + (inputStream4&0xFF))>>5);
                        dataAdd2 = in5;
                        value1 = ((dataAdd1 * 0.02) - 275.15);
                        value2 = dataAdd2 *0.125;
                        DecimalFormat df = new DecimalFormat("#.##");
                        value1 = Double.valueOf(df.format(value1));
                        value2 = Double.valueOf(df.format(value2));
                    //    if (value1!=-275.15){
                        FileWriter adder = new FileWriter(fileName, true);
                       //     String time = DateFormat.format("mm:ss.SSS'Z'",System.currentTimeMillis()).toString();
                        adder.write(value1 + "    " + value2 +"\n");//appends the string to the file
                        adder.close();
                        //}
                    } catch (IOException ioe) {
                        System.err.println("IOException: " + ioe.getMessage());
                    }
                }
            }
        }
        Log.e(TAG, "addToTXT finished");
    }
    /*
    * Pradeda onCharacteristicChanged stebejima
    * */
    public void startNotifications(){
        Log.e(TAG,"startNotifications entered");
        btGatt.setCharacteristicNotification(gattChar,true);
        gattDesc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        btGatt.writeDescriptor(gattDesc);
    }
    /*
  * Sustabdo onCharacteristicChanged stebejima
  * */
    public void stopNotifications(){
        Log.e(TAG,"stopNotifications entered");
        btGatt.setCharacteristicNotification(gattChar,false);
        gattDesc.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        btGatt.writeDescriptor(gattDesc);
    }
    /*
  * Nusiuncia "ui" i write charakteristika
  * */
    private void sendStart() {
        Log.e(TAG,"sendStart entered");
        String text;
        text = "ui";
        gattCharWrite.setValue(text.getBytes());
        boolean status = btGatt.writeCharacteristic(gattCharWrite);

    }
    @Override
    public void onStop() {
        super.onStop();
        if (searchState == 1) {
            btAdapter.stopLeScan(leScanCallback);
            searchState = 0;
        }
        unregisterReceiver(mReceiver);
    }
    @Override
    public void onStart() {
        super.onStart();
        btState();
        if (searchState == 1) {
            btAdapter.stopLeScan(leScanCallback);
            searchState = 0;
        }
        registerReceiver(mReceiver, filter);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (searchState == 1) {
            btAdapter.stopLeScan(leScanCallback);
            searchState = 0;
        }
        if (connectedState == 1) {
            btGatt.disconnect();
            btGatt.close();
            btGatt = null;
            connectedState = 0;
            connectDevice.setText("Prisijungti prie jutiklio");
        }
        try {
            unregisterReceiver(mReceiver);
        }catch (IllegalArgumentException E){
            Log.e(TAG,E.getLocalizedMessage());
        }

    }
    /*
    * Sudaro aplanku sarasa
    * */
    public void folderList() {
        foldersAvailibleAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_single_choice, foldersAvailibleString );
        foldersAvailibleAdapter.clear();
        foldersAvailible.clear();
        foldersAvailibleString.clear();
        foldersAvailibleAdapter.notifyDataSetChanged();
        String path = Environment.getExternalStorageDirectory().toString() + "/Duomenys";
        Log.d(TAG, "Path: " + path);
        File directory = new File(path);
        File[] folders = directory.listFiles();
        Log.d("Files", "Size: " + folders.length);
        for (int i = 0; i< folders.length; i++){
            Log.d("Files", "FileName: " + folders[i].getName());
            foldersAvailible.add(folders[i]);
            foldersAvailibleString.add(folders[i].getName());
            foldersAvailibleAdapter.notifyDataSetChanged();
        }
        folderListView.setAdapter(foldersAvailibleAdapter);
    }
    /*
    * Aplanku saraso apsirinkimas
    * */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (parent==folderListView){
            selectedFolder=foldersAvailible.get(position);
            Log.e(TAG,"Selected: " + selectedFolder+"   "+position);
        }
    }
}
