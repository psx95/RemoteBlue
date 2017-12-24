package com.psx.projectcontrol;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.jar.Manifest;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView_bonded, recyclerView_available;
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    List<BluetoothDevice> bondedDevices;
    Set<BluetoothDevice> bondedDevicesSet;
    BluetoothDevicesAdapter bonded_adapter,available_adapter;
    TextView no_paired_devices, no_available_devices;
    private Context context;
    List<BluetoothDevice> availableDevices = new ArrayList<>();
    IntentFilter intentFilter = new IntentFilter();
    ProgressDialog progressDialog;
    boolean disable_bluetooth;
    //test
    int count =0;
    SharedPreferences sharedPreferences;
    //for vibration functions
    boolean vibrate;
    Vibrator vibrator;
    int vibrator_time;
    //for permission
    private static final int permission = 0;
    Activity activity;
    boolean canScan = false;
    AlertDialog alertDialog_reason;
    SharedPreferences sharedPreferences_settings;
    //for alert dialog
    CheckBox dontshowagain;
    boolean wentThroughDialofInfo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        no_paired_devices = (TextView) findViewById(R.id.no_paired_devices);
        no_available_devices = (TextView) findViewById(R.id.no_available_devices);

        recyclerView_bonded = (RecyclerView) findViewById(R.id.recyclerview_bonded_devices);
        recyclerView_available = (RecyclerView) findViewById(R.id.recyclerview_available_devices);

        if (!adapter.isEnabled()) {
            //give intent to start the bluetoth
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, 1);
        }
        // all work on boded devices
        // this will work in case    the bluetooth is enabled prior to launching the app
        bondedDevicesSet = adapter.getBondedDevices();
        bondedDevices = new ArrayList<>(bondedDevicesSet);
        bonded_adapter = new BluetoothDevicesAdapter(bondedDevices);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView_bonded.setLayoutManager(linearLayoutManager);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL_LIST);
        recyclerView_bonded.addItemDecoration(itemDecoration);
        recyclerView_bonded.setItemAnimator(new DefaultItemAnimator());
        recyclerView_bonded.setAdapter(bonded_adapter);

        if (bondedDevices.size() == 0){
            no_paired_devices.setVisibility(View.VISIBLE);
        }
        else {
            //there are bondedDevices
            no_paired_devices.setVisibility(View.INVISIBLE);
            Log.d("bonded","Change data called 1");
            changeData ();
        }
        //bonded devices over
        context = this;
        activity = (Activity) context;
        //start working on available devices
        available_adapter = new BluetoothDevicesAdapter(availableDevices);
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(this);
        recyclerView_available.setLayoutManager(linearLayoutManager1);
        RecyclerView.ItemDecoration itemDecoration1 = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL_LIST);
        recyclerView_available.addItemDecoration(itemDecoration1);
        recyclerView_available.setItemAnimator(new DefaultItemAnimator());
        recyclerView_available.setAdapter(available_adapter);
        if (availableDevices.size() > 0){
            no_available_devices.setVisibility(View.INVISIBLE);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!adapter.isEnabled()){
                    //ask to enable bluetooth again
                    Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBT, 1);
                }
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && adapter.isEnabled()){
                    Snackbar.make(view, "Searching for nearby devices",12000)
                            .setAction("Action", null).show();
                    vibrator.vibrate(vibrator_time);
                    adapter.startDiscovery();
                }
                else {
                    if (!adapter.isEnabled()){
                        Snackbar.make(view, "Bluetooth is Switched Off, cannot search for nearby devices.",Snackbar.LENGTH_SHORT).show();
                    }
                    else {
                        Snackbar.make(view, "Location Permission denied. Please enable location permission to use this feature",Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });

        registerReceiver(broadcastReceiver, intentFilter);


        //implemeting recycler touch listener
        recyclerView_bonded.addOnItemTouchListener(new MainActivity.RecyclerTouchListener(this,recyclerView_bonded, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                vibrator.vibrate(vibrator_time);
                List<BluetoothDevice> checkList = new ArrayList<BluetoothDevice>(adapter.getBondedDevices());
                if (checkList.contains(bondedDevices.get(position))){

                   checkDevice(bondedDevices.get(position),position);
                }
                else {
                    new AlertDialog.Builder(context).setTitle("Device Not Paired").setMessage("Seems this device is no longer paired with your android device. Please pair them again.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).show();
                }
            }

           /* ActivityCompat.OnRequestPermissionsResultCallback resultCallback = new ActivityCompat.OnRequestPermissionsResultCallback() {
                @Override
                public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
                    switch (requestCode) {
                    }
                }
            };*/

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        //implement recycler touch listener in available devices
        recyclerView_available.addOnItemTouchListener(new MainActivity.RecyclerTouchListener(this, recyclerView_available, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                //cancel discovery if its still going on
                vibrator.vibrate(vibrator_time);
                if (adapter.isDiscovering()){
                    adapter.cancelDiscovery();
                }
                //check if the available device is bonded
                List<BluetoothDevice> checkList = new ArrayList<BluetoothDevice>(adapter.getBondedDevices());
                if (checkList.contains(availableDevices.get(position))) {
                    //check if device is laptop computer
                    checkDevice(availableDevices.get(position),position);
                }
                else {
                    // pair the available device
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    LayoutInflater layoutInflater = LayoutInflater.from(context);
                    View checkbox = layoutInflater.inflate(R.layout.checkbox,null);
                    final SharedPreferences prefs = getSharedPreferences("checkboxpref",MODE_PRIVATE);
                    boolean skipMessage = prefs.getBoolean("checked",false);
                    dontshowagain = (CheckBox) checkbox.findViewById(R.id.skip);
                    builder.setView(checkbox);
                    builder.setTitle("Information");
                    builder.setMessage("Please make sure that your device is not alreday paired in your computer, if already paired, remove device from computer first.");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            if (dontshowagain.isChecked()){
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putBoolean("checked",true);
                                editor.commit();
                            }
                            else
                            {
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putBoolean("checked",true);
                                editor.commit();
                            }
                            wentThroughDialofInfo = true;
                        }
                    });
                    builder.setCancelable(true);
                    if (!skipMessage){
                        builder.show();
                    }
                    if (wentThroughDialofInfo || skipMessage){
                        pairDevice(availableDevices.get(position));
                    }
                    //now start the activity
                  /*  Intent intent = new Intent(context,DeviceActivity.class);
                    intent.putExtra("result",res);
                    startActivity(intent);*/
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        int permissionCheck  = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED){
            // ask for permission explicitly
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION)){
                //show the user the explanation as to why the permisision is needed
                alertDialog_reason = new AlertDialog.Builder(this).setTitle("Location Permission Needed")
                        .setMessage("This app needs the location permission to search for nearby Devices. Starting Android 6.0, it is necessary to allow an app location services to search for nearby bluetooth devices. Please enable Location in Settings->Apps->RemoteBlue->Permissions").setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //open the application settings page
                                final Intent intent = new Intent();
                                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.addCategory(Intent.CATEGORY_DEFAULT);
                                intent.setData(Uri.parse("package:"+context.getPackageName()));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                context.startActivity(intent);
                                alertDialog_reason.dismiss();
                            }
                        }).setNegativeButton("Later", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
                alertDialog_reason.setCancelable(false);
                alertDialog_reason.setCanceledOnTouchOutside(false);
            }
            else{
                // no need to show the rationale directly request for permission
                ActivityCompat.requestPermissions(activity,new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},permission);
            }
        }

        sharedPreferences_settings = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1){
            if (!(resultCode == Activity.RESULT_OK)){
                Toast.makeText(context,"This Application requires bluetooth to run, Please enable bluetooth in settings.",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case permission : {
                // check for cancelled results
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //permssion for location granted
                   // Toast.makeText(this,"Location permission given. ",Toast.LENGTH_SHORT).show();
                }
                else {
                    canScan = false;
                    Toast.makeText(this,"Location permission denied. Will not be able to scan for nearby bluetooth devices ",Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
        return;
    }

    public void checkDevice (BluetoothDevice device, final int position){
        if (device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.COMPUTER_LAPTOP){
            Intent intent = new Intent(context,DeviceActivity.class);
            intent.putExtra("position",position);
            startActivity(intent);
        }
        else {
            AlertDialog alertDialog = new AlertDialog.Builder(context).setTitle("Device not a Computer").setMessage("The Device you selected does not seem to be a computer, it is likely to not connect with this application. Continue ?").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(context,DeviceActivity.class);
                    vibrator.vibrate(vibrator_time);
                    intent.putExtra("position",position);
                    startActivity(intent);
                }
            }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    vibrator.vibrate(vibrator_time);

                }
            }).show();
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
        }
    }

    public void pairDevice (BluetoothDevice device){
        try{
            Method method = device.getClass().getMethod("createBond",(Class[])null);
            method.invoke(device, (Object[])null);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (bondedDevices.size() == 0){
            //bonded_adapter.notifyDataSetChanged();
            Log.d("bonded","Change function called");
            recyclerView_bonded.setAdapter(bonded_adapter);
        }
        Log.d("OnStart","called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("OnResume","called");
        vibrate = sharedPreferences.getBoolean("vibrate_check",false);
        vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        if (vibrator.hasVibrator()){
            if (vibrate){
                vibrator_time = 30;
            }
            else {
                vibrator_time = 0;
            }
        }
        else {
            Toast.makeText(this,"Vibrator hardware could not be found.",Toast.LENGTH_LONG).show();
        }
    }

    @UiThread
    public void changeData ()
    {
        bonded_adapter.notifyDataSetChanged();
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                Log.d("Bluetooth","state changed");
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d("Bluetooth","State off");
                        //show an alert dialog
                        AlertDialog alertDialog = new AlertDialog.Builder(context).setTitle("Bluetooth Switched Off").setMessage("Seems Bluetooth was Switched off. This application requires bluetooth to run. Enable Bluetooth ?").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                adapter.enable();
                                vibrator.vibrate(vibrator_time);
                                dialogInterface.dismiss();
                            }
                        }).setNegativeButton("Exit App", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                vibrator.vibrate(vibrator_time);
                                finish();
                            }
                        }).show();
                        alertDialog.setCancelable(false);
                        alertDialog.setCanceledOnTouchOutside(false);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d("Bluetooth","state on");
                        //working
                        bondedDevicesSet = adapter.getBondedDevices();
                        bondedDevices = new ArrayList<>(bondedDevicesSet);
                        if (bondedDevices!=null){
                            no_paired_devices.setVisibility(View.INVISIBLE);
                        }
                        bonded_adapter = new BluetoothDevicesAdapter(bondedDevices);
                        bonded_adapter.notifyDataSetChanged();
                        recyclerView_bonded.setAdapter(bonded_adapter);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
            else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){
                count = 0;
            }
            else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                Toast.makeText(context,"Finished Discovering devices "+count,Toast.LENGTH_SHORT).show();
            }
            else if (action.equals(BluetoothDevice.ACTION_FOUND)){
                //add the device to the availabel devices list
                count++;
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!availableDevices.contains(bluetoothDevice)){
                    availableDevices.add(bluetoothDevice);
                    no_available_devices.setVisibility(View.INVISIBLE);
                    Log.d("ACTION_FOUND","here");
                    available_adapter.notifyDataSetChanged();
                }
            }
            else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int currState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,0);
                //String prevState = intent.getStringExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE);
                switch (currState){
                    case BluetoothDevice.BOND_BONDED :
                        bondedDevices.add(device);
                        bonded_adapter.notifyDataSetChanged();
                        progressDialog.dismiss();
                        Log.d("BOND","BONDED");
                        break;
                    case BluetoothDevice.BOND_BONDING :
                        progressDialog = ProgressDialog.show(context,"Pairing Devices","Please wait while the devices are being paired. Click Pair on the other device. You can touch to dismiss the dialog.",true);
                        progressDialog.setCanceledOnTouchOutside(true);
                        progressDialog.setCancelable(true);
                        Log.d("BOND","BONDING");
                        break;
                }
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this,Settings.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.action_about){
            Intent intent = new Intent(this,AboutActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //to add a recycler touch listener
    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private MainActivity.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final MainActivity.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    public List<BluetoothDevice> getBondedDevices() {
        return bondedDevices;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("onPause","called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disable_bluetooth = sharedPreferences_settings.getBoolean("bluetooth_check",false);
        if (disable_bluetooth){
            adapter.disable();
        }
        unregisterReceiver(broadcastReceiver);
    }
}
