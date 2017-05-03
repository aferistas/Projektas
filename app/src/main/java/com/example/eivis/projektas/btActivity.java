package com.example.eivis.projektas;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class btActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private static final String TAG = "btActivity";
    BluetoothAdapter btAdapter;
    ListView folderList;
    Button btOnOff;
    Button button_send;
    TextView selectView;
    int currentState = 0;
    ArrayList<BluetoothDevice> btFoundDevices = new ArrayList<>();
    ArrayList<File> foldersFound = new ArrayList<>();
    ArrayList<String> foldersFoundString = new ArrayList<>();
    ArrayList<File> selectedFolders = new ArrayList<>();
    ArrayList<String> selectedFoldersString = new ArrayList<>();
    ArrayAdapter folderAdapter ;
    int j;
    ArrayAdapter<String> btArrayAdapter;
    ArrayList<Uri> uris = new ArrayList<Uri>();
    IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt);
        btArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = bluetoothManager.getAdapter();
        selectView = (TextView) findViewById(R.id.selectView) ;
        btOnOff = (Button) findViewById(R.id.btOnOff);
        btOnOff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                btONOFF();
            }
        });
        button_send = (Button) findViewById(R.id.button_send) ;
        button_send.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v){
                sendFromFolder();}
        });
        folderList = (ListView) findViewById(R.id.folderList);
        folderList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        folderList.setOnItemClickListener(btActivity.this);

        registerReceiver(mReceiver, filter);
        btState();
        folderList();

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
                        break;
                    case BluetoothAdapter.STATE_ON:
                        btState();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };
/*
* Pradine BT busena
* */
    private void btState() {
        if (btAdapter.isEnabled()){
            btOnOff.setText("Išjungti Bluetooth");
            selectView.setVisibility(View.VISIBLE);
            folderList.setVisibility(View.VISIBLE);
            button_send.setVisibility(View.VISIBLE);
            btOnOff.setText("Išjungti Bluetooth");

        } else {
            btOnOff.setText("Įjungti Bluetooth");
            selectView.setVisibility(View.INVISIBLE);
            folderList.setVisibility(View.INVISIBLE);
            button_send.setVisibility(View.INVISIBLE);
            btOnOff.setText("Įjungti Bluetooth");
        }
    }
/*
* On/Off BT mygtukas
* */
    private void btONOFF(){
        folderList();
        if (btAdapter.isEnabled()){
            btAdapter.disable();

        } else {
            Intent enablebtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enablebtIntent, 1);

        }
    }

/*
* Aplanku /Doumenys/ sarasas, i Array<File> ir Array<String>
* */
    public void folderList() {
        folderAdapter = new ArrayAdapter(btActivity.this, android.R.layout.simple_list_item_multiple_choice, foldersFoundString );
        folderAdapter.clear();
        folderAdapter.notifyDataSetChanged();
        String path = Environment.getExternalStorageDirectory().toString() + "/Duomenys";
        Log.d(TAG, "Path: " + path);
        File directory = new File(path);
        File[] folders = directory.listFiles();
        Log.d("Files", "Size: " + folders.length);
        for (int i = 0; i< folders.length; i++){
            Log.d("Files", "FileName: " + folders[i].getName());
            foldersFound.add(folders[i]);
            foldersFoundString.add(folders[i].getName());
            folderAdapter.notifyDataSetChanged();
        }

        folderList.setAdapter(folderAdapter);
    }
    /*
    * Automatiskai pasizymi visus failus pasirinktuose folderList() aplankuose.
    * Gauna rssi, pagal ka ir siuncia.
    * Sudaro rssi List, kuriame visi suzymeti failai is visu aplanku
    *   @param folder aplankas kurio failus zymi
    * */
    public void fileList(File folder) {
         Log.e(TAG, "Entered fileList");
        String selectedFolder = folder.getName().toString();
        String path = Environment.getExternalStorageDirectory().toString() + "/Duomenys"+ "/" + selectedFolder;
        Log.d(TAG, "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: " + files.length);
        for (int i = 0; i< files.length; i++){
            Log.d("Files", "FileName: " + files[i].getName());
            if (uris.contains(Uri.fromFile(files[i]))){
                uris.remove(Uri.fromFile(files[i]));
            } else {
                uris.add(Uri.fromFile(files[i]));
            }

        }

    }
/*
* Siuncia fileList(File) aplankus automatiskai per BT
* */
    public void sendFiles(){

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("*/*");
        intent.setPackage("com.android.bluetooth");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        startActivity(intent);
    }
/*
* Aplanku susizymejimas
* */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
        if (parent == folderList){
            fileList(foldersFound.get(i));
            Log.e(TAG, "Clicked on " + uris.toString());
            if (selectedFolders.contains(foldersFound.get(i))){
                selectedFolders.remove(foldersFound.get(i));
               selectedFoldersString.remove(foldersFoundString.get(i));
            } else {
                selectedFolders.add(foldersFound.get(i));
               selectedFoldersString.add(foldersFoundString.get(i));
            }
        }
    }
/*
* Log visi suzymeti failai patikrinimui
* */
    public void sendFromFolder(){
        Log.e(TAG, "Clicked on " + uris.toString());
            sendFiles();


    }


    @Override
    public void onStop() {
        super.onStop();

        unregisterReceiver(mReceiver);
    }
    @Override
    public void onStart() {
        super.onStart();
        btState();
        registerReceiver(mReceiver, filter);
    }
}

