package com.psx.projectcontrol;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.KeyboardShortcutGroup;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class DeviceActivity extends AppCompatActivity {

    boolean doubleBackToExitPressedOnce = false;
    boolean doubleTap = false;
    long time_double_tap = 0;
    float doublexpos, doubleypos;
    ViewFlipper mViewFlipper;
    private Context mContext;
    private SwipeGestureListener gestureListener;
    private CustomEditText editText;
    private BluetoothDevice connectedDevice;
    List<BluetoothDevice> bondedDevices;
    Set<BluetoothDevice> bondedDevicesSet;
    BluetoothAdapter adapter;
    private GestureDetector mGestureDetector;
    private GestureDetector doubleTapGestureDetector;
    final UUID SERIAL_UUID = UUID.fromString("04c6093b-0000-1000-8000-00805f9b34fb");
    DataOutputStream outputStream;
    DataInputStream inputStream; // modification
    BluetoothSocket socket = null;
    Button cancel_button,cancel_button_power;
    ImageButton power,keyboard,media,apps,play,fast_forward,fast_reverse,vol_up,vol_down,shutdown,hibernate,lock,restart,seek_forward,seek_backward,presentation_mode;
    ImageView scroll_view;
    Button lmb,rmb;
    static int position = 0;
    private boolean connected = false;
    private TextView trackpad;
    float xpos,disx;
    float ypos,disy;
    float ypos_scroll,disy_scroll;
    boolean mouse_moved = false, seperateTouch = false;
    long firstTime= 0;
    boolean up;
    int amt=0;
    byte twoFingerTapCount = 0;
    private static final int TIMEOUT = ViewConfiguration.getDoubleTapTimeout() + 100;
    private RecyclerView recyclerView_apps;
    private AppsAdapter appsAdapter;
    // variables required for shred preferences work
    boolean firstrun = true;
    private List<DesktopApps> appsList = new ArrayList<>();
    private Gson gson = new Gson();
    private String save = "null";
    Type type = new TypeToken<List<DesktopApps>>(){}.getType();
    //variables required for adding a new quicklanch
    String name_new_app = "";
    String path_new_app = "";
    //dialog for apps
    Dialog dialog_app;
    Dialog dialog_file_explorer;
    View intro;
    //for retrieving settings
    SharedPreferences sharedPreferences_settings;
    boolean right_click_two_finger_touch;
    boolean vibrate;
    boolean vibrator_na = false;
    Vibrator vibrator;
    int vibrator_time;
    boolean sleepIndicator = false;
    //for showcase
    private static final String SHOWCASE_ID = "sequence example";
    //to prevent the memory leaks from the laert dialogs
    AlertDialog alertDialog_disconnect;
    // static variable to store the result of accept Thread
    public static String result_accept_thread = "";
    // file explorer dialofg varibles that need to be updated aftr the ost execute method of AsyncTask
    TextView testView;
    static RecyclerView recyclerView_file_explorer;
    static List<FilesDirectories> filesDirectories; // list of FilesDirectories that have to be inflated
    static List<String> results = new ArrayList<String>();
    static FilesAdapter filesAdapter;
    private static ImageButton file_explorer_back_button;
    // variable to keep a track of if the file explorer has yet been opened (EVEN ONCE) - to provide a check for the statement in onPause
    private boolean is_explorer_active = false;
    // List varibale to store the explored files temporarilyy
    private List<FilesDirectories> temp_list = new ArrayList<>();
    private static boolean isLaserOn = false;
    private Dialog dialog_presenation_mode;
    // Click Listener for the back button
    private View.OnClickListener back_button_listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // implement the back button fucntionality of the file explorer
            Log.d("BACK_BUTTON","click recieveed on back button");
            if (filesDirectories.size() > 0 && !(filesDirectories.get(0).getName().equals("Root Directory"))){
                // get the parent file of the first entry in this list
                String curr_path = filesDirectories.get(0).getPath();
                Log.d("BACK_BUTTON","Current_path"+curr_path);
                int last_seperator = curr_path.lastIndexOf('\\');
                int sec_last_sep = (curr_path.substring(0,last_seperator)).lastIndexOf('\\');
                String parent_path = filesDirectories.get(0).getPath().substring(0,sec_last_sep+1);
                Log.d("BACK_BUTTON","Parent_Path"+parent_path);
                // explore this path
                AcceptThread acceptThread = new AcceptThread(inputStream,mContext);
                acceptThread.start();
                Thread [] arr  = {acceptThread};
                // clear the current recycler view
                int s = filesDirectories.size();
                filesDirectories.clear();
                filesAdapter.notifyItemRangeRemoved(0,s);
                // start a check thread
                new DeviceActivity.checkThread().execute(arr);
                if (parent_path.lastIndexOf('\\') == 2){
                    // send command STARTTHREAD
                    try {
                        sendCommand("STARTTHREAD");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {

                    filesAdapter.generateCommand(parent_path);
                }
            }
            else {
                Log.d("BACK_BUTTON","already in root");
            }
        }
    };

    private boolean isAClick(float startX, float endX, float startY, float endY) {
        float differenceX = Math.abs(startX - endX);
        float differenceY = Math.abs(startY - endY);
        if (differenceX > 5 || differenceY > 5) {
            return false;
        }
        return true;
    }

    private void reset (long time){
        //function to reset the multitouch counts and stats
        firstTime = time;
        seperateTouch = false;
        twoFingerTapCount = 0;
    }

    public void dismissCurrDialog(int i){
        if (i == 1){
            dialog_app.dismiss();
        }
        else if (i == 2){
            dialog_file_explorer.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        mViewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);
        mContext = this;
        gestureListener = new SwipeGestureListener(mContext);
        AndroidBug5497Workaround.assistActivity(this);
        mGestureDetector = new GestureDetector(mContext,gestureListener);
        DoubleTapGestureListener doubleTapGestureListener = new DoubleTapGestureListener();
        doubleTapGestureDetector = new GestureDetector(mContext, doubleTapGestureListener);
        doubleTapGestureDetector.setOnDoubleTapListener(doubleTapGestureListener);
        intro = findViewById(R.id.intro);
        power = (ImageButton) findViewById(R.id.imageButton_power);
        media = (ImageButton) findViewById(R.id.imageButton_media);
        keyboard = (ImageButton) findViewById(R.id.imageButton_keyboard);
        apps = (ImageButton) findViewById(R.id.imageButton_app);
        lmb= (Button) findViewById(R.id.lmb);
        rmb = (Button) findViewById(R.id.rmb);
        presentation_mode = (ImageButton) findViewById(R.id.image_button_presentation_mode);
        scroll_view = (ImageView) findViewById(R.id.mouseScroll);
        editText = (CustomEditText) findViewById(R.id.editText);
        editText.addTextChangedListener(watcher);
        trackpad = (TextView) findViewById(R.id.trackpad_mouse);
        trackpad.setOnTouchListener(trackpad_listener);
        mViewFlipper.setOnTouchListener(gestureListener);
        power.setOnTouchListener(gestureListener);
        media.setOnTouchListener(gestureListener);
        keyboard.setOnTouchListener(gestureListener);
        apps.setOnTouchListener(gestureListener);
        lmb.setOnTouchListener(gestureListener);
        rmb.setOnTouchListener(gestureListener);
        presentation_mode.setOnTouchListener(gestureListener);
        intro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presentShowCaseSequence();
            }
        });
        scroll_view.setOnTouchListener(new ImageView.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //handleTouch(motionEvent);
                presentShowCaseSequence();
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        if(!vibrator_na){
                            vibrator.vibrate(vibrator_time);
                            Log.d("deviceActivityvibrate","vibrate");
                        }
                        scroll_view.setImageResource(R.drawable.scroll_pressed);
                        ypos_scroll = motionEvent.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        scroll_view.setImageResource(R.drawable.scroll_not_pressed);

                        break;
                    case MotionEvent.ACTION_MOVE:
                        disy_scroll = motionEvent.getY() - ypos;
                        ypos = motionEvent.getY();
                        try {
                            if (disy_scroll < 0){
                                //disy_scroll = (float) Math.floor(disy_scroll);
                                amt = -1;
                            }
                            else {
                                //disy_scroll = (float) Math.ceil(disy_scroll);
                                amt = 1;
                            }
                            Log.d("Scroll",""+disy_scroll);
                            sendCommand("   mouse"+amt);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                }
                return true;
            }
        });
        adapter = BluetoothAdapter.getDefaultAdapter();
        bondedDevicesSet = adapter.getBondedDevices();
        bondedDevices = new ArrayList<>(bondedDevicesSet);
        Bundle extras = getIntent().getExtras();
        if (extras != null){
            int pos = extras.getInt("position");
            connectedDevice = bondedDevices.get(pos);
        }
        if (firstrun){
            // add the default applications in the list
            appsList.add(new DesktopApps("ThisPC"," "));
            appsList.add(new DesktopApps("My Documents"," "));
            appsList.add(new DesktopApps("Libraries"," "));
            appsList.add(new DesktopApps("Control Panel"," "));
            appsList.add(new DesktopApps("Downloads"," "));
            appsList.add(new DesktopApps("Task View","(Only for Windows 10)"));
        }

        sharedPreferences_settings = PreferenceManager.getDefaultSharedPreferences(this);
        //start the connection
        presentShowCaseSequence();
    }

    View.OnClickListener introduction = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            presentShowCaseSequence();
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private void presentShowCaseSequence (){
        ShowcaseConfig showcaseConfig =new ShowcaseConfig();
        showcaseConfig.setDelay(500);

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID);

        sequence.setOnItemShownListener(new MaterialShowcaseSequence.OnSequenceItemShownListener() {
            @Override
            public void onShow(MaterialShowcaseView materialShowcaseView, int i) {
                //do something
            }
        });

        sequence.setConfig(showcaseConfig);
        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(mViewFlipper)
                .setDismissText("SWEET!").setContentText("This is your Dashboard for quick actions")
                .withRectangleShape(true)
                .build());
        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(scroll_view).setDismissText("GOT IT!")
                .setContentText("This Sidebar is scroll control for mouse")
                .withoutShape()
                .build());
        sequence.addSequenceItem(intro,"Swipe Here to switch between mouse buttons & Dashboard","THANKS!");

        sequence.start();
    }

    class DoubleTapGestureListener extends GestureDetector.SimpleOnGestureListener implements GestureDetector.OnDoubleTapListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
            Log.d("GestureListener","Single tap confirmed");
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent motionEvent) {
            time_double_tap = motionEvent.getEventTime();
            doublexpos = motionEvent.getX();
            doubleypos = motionEvent.getY();
            Log.d("GestureListener","Double tap confirmed at "+time_double_tap);
            try {
                if(!vibrator_na){
                    vibrator.vibrate(vibrator_time);
                }
                if(!vibrator_na){
                    vibrator.vibrate(vibrator_time);
                }
                // change here
                // test
               // sendCommand("lmb");
               // sendCommand("longClick");
                sendCommand("doubletap");
            } catch (IOException e) {
                e.printStackTrace();
            }
            doubleTap = true;
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent motionEvent) {
            return false;
        }
    }

    class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener implements
            View.OnTouchListener {
        Context context;
        float startX, startY;
        GestureDetector gDetector;
        static final int SWIPE_MIN_DISTANCE = 60;
        static final int SWIPE_MAX_OFF_PATH = 125;
        static final int SWIPE_THRESHOLD_VELOCITY = 100;

        public SwipeGestureListener() {
            super();
        }

        public SwipeGestureListener(Context context) {
            this(context, null);
        }

        public SwipeGestureListener(Context context, GestureDetector gDetector) {

            if (gDetector == null)
                gDetector = new GestureDetector(context, this);

            this.context = context;
            this.gDetector = gDetector;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
                if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH
                        || Math.abs(velocityY) < SWIPE_THRESHOLD_VELOCITY) {

                    return false;
                }
                if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE) {
                    //bottom to top swipe
                } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE) {
                   //top to bottom swipe
                }
            } else {
                if (Math.abs(velocityX) < SWIPE_THRESHOLD_VELOCITY) {
                    return false;
                }
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE) {
                   //right to left swipe
                    Log.d("anim","r2l");
                    mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(context, R.anim.left_in));
                    mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(context, R.anim.left_out));
                    mViewFlipper.showNext();
                    return true;

                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE) {
                    //left to right swipe
                    Log.d("anim","l2r");
                    mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(mContext, R.anim.right_in));
                    mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(mContext,R.anim.right_out));
                    mViewFlipper.showPrevious();
                    return true;
                }
            }

            return super.onFling(e1, e2, velocityX, velocityY);

        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //touch event
            presentShowCaseSequence();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getX();
                    startY = event.getY();
                    break;
                case MotionEvent.ACTION_UP: {
                    float endX = event.getX();
                    float endY = event.getY();
                    if (isAClick(startX, endX, startY, endY)) {
                        // WE HAVE A CLICK!!
                        if(!vibrator_na){
                            vibrator.vibrate(vibrator_time);
                            Log.d("vibrator","vibrate is a click on lower panel "+vibrator_time);
                        }
                        int id = v.getId();
                        if (id == R.id.imageButton_keyboard){
                            //launch keyboard
                            editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                                @Override
                                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                                    if (i == EditorInfo.IME_ACTION_DONE){
                                        Log.d("onEditorAction","Enter");
                                        try {
                                            sendCommand("enter");
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        return true;
                                    }
                                    else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DEL){
                                        Log.d("onEditorAction","Backspace");
                                        return true;
                                    }
                                    else{
                                        return false;
                                    }
                                }
                            });
                            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (inputMethodManager.isActive()){
                                inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0); // hide
                            }
                            else {
                                inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY); // show
                            }
                            if (editText.isFocused()){
                                editText.clearFocus();
                            }
                            else{
                                editText.requestFocus();
                            }
                            //editText.addTextChangedListener(watcher);
                            Log.d("click","keyboard");
                            return true;
                        }
                        else if (id == R.id.imageButton_media){
                            //launch the media remote
                            Log.d("click","media");
                            //show the media dialog
                            final Dialog dialog = new Dialog(mContext);
                            dialog.setContentView(R.layout.media_dialog_layout);
                            play = (ImageButton) dialog.findViewById(R.id.imageButton_play);
                            vol_up = (ImageButton) dialog.findViewById(R.id.imageButton_vol_up);
                            vol_down = (ImageButton) dialog.findViewById(R.id.imageButton_vol_down);
                            fast_forward = (ImageButton) dialog.findViewById(R.id.imageButton_fast_forward);
                            fast_reverse = (ImageButton) dialog.findViewById(R.id.imageButton_previous);
                            cancel_button = (Button) dialog.findViewById(R.id.media_panel_close);
                            play.setOnTouchListener(panel_listener);
                            vol_up.setOnTouchListener(panel_listener);
                            vol_down.setOnTouchListener(panel_listener);
                            fast_forward.setOnTouchListener(panel_listener);
                            fast_reverse.setOnTouchListener(panel_listener);
                            cancel_button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if(!vibrator_na){
                                        vibrator.vibrate(vibrator_time);
                                        Log.d("vibrator","vibrate");
                                    }
                                    dialog.dismiss();
                                }
                            });
                            dialog.setCancelable(false);
                            dialog.setCanceledOnTouchOutside(false);
                            dialog.show();
                            return true;
                        }
                        else {
                            if (id == R.id.imageButton_app) {
                                // launch the app selection screen
                                dialog_app = new Dialog(mContext);
                                dialog_app.setContentView(R.layout.apps_dialog_layout);
                                recyclerView_apps = (RecyclerView) dialog_app.findViewById(R.id.recyclerview_apps);
                                Button cancel = (Button) dialog_app.findViewById(R.id.cancel_button_apps);
                                ImageButton app_add = (ImageButton) dialog_app.findViewById(R.id.imageButton_add_new_app);
                                final ImageButton file_explorer = (ImageButton) dialog_app.findViewById(R.id.imageButton_file_explorer);
                                appsAdapter = new AppsAdapter(appsList, mContext);
                                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
                                recyclerView_apps.setLayoutManager(linearLayoutManager);
                                RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST);
                                recyclerView_apps.addItemDecoration(itemDecoration);
                                recyclerView_apps.setItemAnimator(new DefaultItemAnimator());
                                recyclerView_apps.setAdapter(appsAdapter);
                                cancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (!vibrator_na) {
                                            vibrator.vibrate(vibrator_time);
                                        }
                                        dismissCurrDialog(1);
                                    }
                                });
                                file_explorer.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        // dismiss the current dialog - desktop apps dialog
                                        dismissCurrDialog(1);
                                        is_explorer_active = true;
                                        // open a new dialog for the file explorer
                                        dialog_file_explorer = new Dialog(mContext,android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
                                        dialog_file_explorer.setContentView(R.layout.file_explorer_dialog);
                                        Button cancel = (Button) dialog_file_explorer.findViewById(R.id.cancel_button_file_explorer);
                                      //  testView = (TextView) dialog_file_explorer.findViewById(R.id.testView);
                                        recyclerView_file_explorer = (RecyclerView) dialog_file_explorer.findViewById(R.id.recyclerview_file_explorer);
                                        final AcceptThread acceptThread = new AcceptThread(inputStream,mContext);
                                        filesDirectories = new ArrayList<FilesDirectories>();

                                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
                                        recyclerView_file_explorer.setLayoutManager(linearLayoutManager);
                                        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL_LIST);
                                        recyclerView_file_explorer.addItemDecoration(itemDecoration);
                                        recyclerView_file_explorer.setItemAnimator(new DefaultItemAnimator());
                                        filesAdapter = new FilesAdapter (filesDirectories,mContext,recyclerView_file_explorer,inputStream);
                                        //filesAdapter1 = filesAdapter;
                                        recyclerView_file_explorer.setAdapter(filesAdapter);
                                        file_explorer_back_button = (ImageButton) dialog_file_explorer.findViewById(R.id.back_button_file_explorer);
                                        file_explorer_back_button.setOnClickListener(back_button_listener);

                                        cancel.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                //dismiss currDialog - file explorer dialog
                                                if (acceptThread.isAlive()){
                                                    acceptThread.interrupt();
                                                }
                                                is_explorer_active = false;
                                                dismissCurrDialog(2);
                                            }
                                        });
                                        //apply logic for browsing files
                                        // expecting a response from the server
                                        //open an accept thread variable
                                        acceptThread.start();
                                        // open an async task to check if the accepth Thread insatnce is terminated
                                        Thread [] arr = {acceptThread};
                                        Log.d("AsyncTask","execute()");
                                        new checkThread().execute(arr);
                                        // send command to start a new accept thread at the receiever side
                                        try {
                                            sendCommand("STARTTHREAD");
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        dialog_file_explorer.show();
                                    }
                                });
                                app_add.setOnTouchListener(new View.OnTouchListener() {
                                    @Override
                                    public boolean onTouch(View view, MotionEvent motionEvent) {
                                        int action = motionEvent.getAction();
                                        switch (action) {
                                            case MotionEvent.ACTION_DOWN:
                                                ((ImageButton) view).setImageResource(R.drawable.add_pressed);
                                                if (!vibrator_na) {
                                                    vibrator.vibrate(vibrator_time);
                                                }
                                                break;
                                            case MotionEvent.ACTION_UP:
                                                ((ImageButton) view).setImageResource(R.drawable.add);
                                                //open a new dialog
                                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                                builder.setTitle("Add a New QuickLaunch App");
                                                final EditText editText_name = new EditText(mContext);
                                                editText_name.setInputType(InputType.TYPE_CLASS_TEXT);
                                                editText_name.setHint("Application/Folder Name");
                                                final EditText editText_path = new EditText(mContext);
                                                editText_path.setInputType(InputType.TYPE_CLASS_TEXT);
                                                editText_path.setHint("Complete Path eg. C:\\Games\\Microsoft\\game.exe");
                                                LinearLayout linearLayout = new LinearLayout(mContext);
                                                linearLayout.setOrientation(LinearLayout.VERTICAL);
                                                linearLayout.addView(editText_name);
                                                linearLayout.addView(editText_path);
                                                builder.setView(linearLayout);
                                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        if (!vibrator_na) {
                                                            vibrator.vibrate(vibrator_time);
                                                        }
                                                        name_new_app = editText_name.getText().toString();
                                                        path_new_app = editText_path.getText().toString();
                                                        if (name_new_app.equals("") || path_new_app.equals("")) {
                                                            Toast.makeText(mContext, "Name or Path is Empty", Toast.LENGTH_LONG).show();
                                                        } else {
                                                            // add the new quick launch to the applist
                                                            DesktopApps app = new DesktopApps(name_new_app, path_new_app);
                                                            appsList.add(app);
                                                            appsAdapter.notifyDataSetChanged();
                                                            recyclerView_apps.setAdapter(appsAdapter);
                                                        }
                                                    }
                                                });
                                                builder.show();
                                                break;
                                        }
                                        return true;
                                    }
                                });
                                dialog_app.show();
                                Log.d("click", "app");
                                return true;
                            } else if  (id == R.id.image_button_presentation_mode){
                                // implement the logic for presentation mode
                                dialog_presenation_mode = new Dialog(mContext,android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
                                dialog_presenation_mode.setContentView(R.layout.layout_presentation_mode);
                                TextView presetation  = (TextView) dialog_presenation_mode.findViewById(R.id.trackpad_presentation);
                                Button cancel = (Button) dialog_presenation_mode.findViewById(R.id.close_button_presentation);
                                ImageButton next_slide = (ImageButton) dialog_presenation_mode.findViewById(R.id.presentation_next_slide);
                                ImageButton prev_slide = (ImageButton) dialog_presenation_mode.findViewById(R.id.presentation_back_slide);
                                ImageButton start_slide_show = (ImageButton) dialog_presenation_mode.findViewById(R.id.start_presentation);
                                ImageButton pause_slide_show = (ImageButton) dialog_presenation_mode.findViewById(R.id.imageButton_pause_presentation);
                                ImageButton ink_annotation = (ImageButton) dialog_presenation_mode.findViewById(R.id.presentation_ink_annotations);
                                final CheckBox lasers = (CheckBox) dialog_presenation_mode.findViewById(R.id.checkBox_lasers);
                                cancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Log.d("PRESENTATION_MDOE","clicked on cancel button");
                                        try {
                                            Log.d("PRESENTATION_MODE","laser swicthed off");
                                            sendCommand("LASER_OFF");
                                            sendCommand("presentation_pause_slide_show");
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        lasers.setChecked(false);
                                        isLaserOn = false;
                                        Log.d("PRESENTATION_MODE_CLOSE","islaserOn"+isLaserOn);
                                        dialog_presenation_mode.dismiss();
                                    }
                                });
                                // test
                                presetation.setOnTouchListener(trackpad_listener);
                                // working
                                View.OnClickListener options_onClickListener = new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        int id = view.getId();
                                        switch (id){
                                            case R.id.presentation_back_slide:
                                                // implement what happens on pressing the back button;
                                                //Toast.makeText(context,"Clicked on back",Toast.LENGTH_SHORT).show();
                                                try {
                                                    sendCommand("presenation_back_slide");
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                break;
                                            case R.id.presentation_next_slide:
                                                // implement what happens on pressing the next button;
                                                //Toast.makeText(context,"Clicked on next",Toast.LENGTH_SHORT).show();
                                                try {
                                                    sendCommand("presenatation_next_slide");
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                break;
                                            case R.id.start_presentation:
                                                // implement what happens on prssing the start button;
                                              //  Toast.makeText(context,"Clicked on start",Toast.LENGTH_SHORT).show();
                                                try {
                                                    sendCommand("presentation_start_slideshow");
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                break;
                                            case R.id.presentation_ink_annotations:
                                                // implement what happens on pressing the ink annotations button;
                                                Toast.makeText(context,"Clicked on ink_anoottations",Toast.LENGTH_SHORT).show();
                                                try {
                                                    sendCommand("presenation_toggle_annotation");
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                break;
                                            case R.id.imageButton_pause_presentation:
                                                // stop the presentation
                                                Toast.makeText(context, "Clicked on Pause presentation", Toast.LENGTH_SHORT).show();
                                                try {
                                                    sendCommand("presentation_pause_slide_show");
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                break;
                                        }
                                    }
                                };
                                lasers.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                        if (b){
                                            // the checkbox is checked
                                            isLaserOn = true;
                                            try {
                                                Log.d("PRESENTATION_MODE","islaserOn"+isLaserOn);
                                                sendCommand("LASER_ON");
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        else {
                                            isLaserOn = false;
                                            try {
                                                Log.d("PRESENTATION_MODE","islaserOn"+isLaserOn);
                                                sendCommand("LASER_OFF");
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                });
                                next_slide.setOnClickListener(options_onClickListener);
                                prev_slide.setOnClickListener(options_onClickListener);
                                start_slide_show.setOnClickListener(options_onClickListener);
                                pause_slide_show.setOnClickListener(options_onClickListener);
                                ink_annotation.setOnClickListener(options_onClickListener);
                                dialog_presenation_mode.show();
                                //Toast.makeText(mContext,"Click Recieved",Toast.LENGTH_SHORT).show();
                                Log.d("PRESENTATION_MODE","Click recieved");
                                return true;
                            }
                            else if (id == R.id.imageButton_power) {
                                //launch the power panel
                                Log.d("click", "power");
                                final Dialog dialog = new Dialog(mContext);
                                dialog.setContentView(R.layout.power_dialog_layout);
                                shutdown = (ImageButton) dialog.findViewById(R.id.imageButton_shutdown);
                                hibernate = (ImageButton) dialog.findViewById(R.id.imageButton_hibernate);
                                lock = (ImageButton) dialog.findViewById(R.id.imageButton_lock);
                                restart = (ImageButton) dialog.findViewById(R.id.imageButton_restart);
                                cancel_button_power = (Button) dialog.findViewById(R.id.close_button_power);
                                View.OnClickListener onClickListener_power = new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (!vibrator_na) {
                                            vibrator.vibrate(vibrator_time);
                                        }
                                        int id = view.getId();
                                        switch (id) {
                                            case R.id.imageButton_hibernate:
                                                try {
                                                    sendCommand("power_hibernate");
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                dialog.dismiss();
                                                break;
                                            case R.id.imageButton_lock:
                                                try {
                                                    sendCommand("power_lock");
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                dialog.dismiss();
                                                break;
                                            case R.id.imageButton_restart:
                                                try {
                                                    sendCommand("power_restart");
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                dialog.dismiss();
                                                break;
                                            case R.id.imageButton_shutdown:
                                                try {
                                                    sendCommand("power_shutdown");
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                dialog.dismiss();
                                                break;
                                            case R.id.close_button_power:
                                                dialog.dismiss();
                                                break;
                                        }
                                    }
                                };
                                shutdown.setOnClickListener(onClickListener_power);
                                hibernate.setOnClickListener(onClickListener_power);
                                lock.setOnClickListener(onClickListener_power);
                                restart.setOnClickListener(onClickListener_power);
                                cancel_button_power.setOnClickListener(onClickListener_power);
                                dialog.show();
                                return true;
                            }
                            else if (id == R.id.lmb) {
                                //perform the left click operation
                                Log.d("click", "lmb");
                                try {
                                    sendCommand("lmb");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return true;
                            } else if (id == R.id.rmb) {
                                // perform the right click operation
                                try {
                                    sendCommand("rmb");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                Log.d("click", "rmb");
                                return true;
                            }
                        }
                    }
                    break;
                }
            }
            return gDetector.onTouchEvent(event);
        }

        public GestureDetector getDetector() {
            return gDetector;
        }
    }

    //handling keyboard events

    TextWatcher watcher = new TextWatcher() {
        String wordbefore="";
        String wordafter="";
        int startbefore,startafter,lengthbefore,lengthafter;
        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            //after -length of charcters that are going to replace count characters beginning at start in charSequence
            wordbefore +=charSequence.subSequence(start,start+count).toString();
            startbefore = start;
            lengthafter = after;
            Log.d("beforeTextChanged","start before"+start);
            Log.d("beforeTextChanged",charSequence.subSequence(start,start+count).toString());
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            //before-length of text that has just been replaced by count characters beginning at start within charSequence
            wordafter +=charSequence.subSequence(start,start+count).toString();
            startafter = start;
            lengthbefore = before;
            Log.d("onTextChanged","start after"+start);
            Log.d("onTextChanged",charSequence.subSequence(start,start+count).toString());
        }

        @Override
        public void afterTextChanged(Editable editable) {
            Log.d("TEXT WATCHER","aftertextchanged");
            if (startafter == startbefore){
                Log.d("afterTextChanged","startafter=startbefore");
                for (int i = 0; i<lengthbefore;i++){
                    try {
                        Log.d("afterTextChanged","backspace sent");
                        sendCommand("backspace");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (wordafter.equals(" ")){
                    Log.d("afterTextChanged","space");
                    try {
                        sendCommand("keyboard");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    try {
                        sendCommand("keyboard"+wordafter.trim());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            else {
                if (wordafter.equals(" ")){
                    try {
                        sendCommand("keyboard");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    try {
                        sendCommand("keyboard"+wordafter.trim());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            wordafter ="";
            wordbefore = "";
        }
    };

    // on Touch Listener for the panel buttons
    View.OnTouchListener panel_listener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int id = view.getId();
            ImageButton imageButton;
            Thread thread =null;
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                up = false;
                if(!vibrator_na){
                    vibrator.vibrate(vibrator_time);
                }
                switch (id) {
                    // begin handling media panel
                    case R.id.imageButton_vol_up :
                        //send command to press the vol_up button
                        //send command to press the vol_up button
                        Log.d ("butotn_press","vol_up pressed");
                        ((ImageButton) view).setImageResource(R.drawable.volume_up_pressed);
                        thread = new DoThread("volup_press");
                        thread.start();
                        break;
                    case R.id.imageButton_vol_down :
                        ((ImageButton) view).setImageResource(R.drawable.volume_down_pressed);
                        thread = new DoThread("voldown_press");
                        thread.start();
                        break;
                    case R.id.imageButton_play :
                        ((ImageButton) view).setImageResource(R.drawable.play_button_pressed);
                        try {
                            sendCommand("play_press");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case R.id.imageButton_previous:
                        ((ImageButton) view).setImageResource(R.drawable.previous_pressed);
                        thread = new DoThread("fast_reverse_press");
                        thread.start();
                        break;
                    case R.id.imageButton_fast_forward :
                        ((ImageButton) view).setImageResource(R.drawable.fast_forward_pressed);
                        thread = new DoThread("fast_forward_press");
                        thread.start();
                        break;
                    //media panel events handled
                }
            }
            else if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                up=true;
                switch (id) {
                        // begin handling media panel
                    case R.id.imageButton_vol_up :
                        //send command to release the vol_up button
                        ((ImageButton) view).setImageResource(R.drawable.volume_up_not_pressed);
                        try {
                            sendCommand("volup_release");
                            if (thread != null){
                                thread.interrupt();
                            }
                            else
                                Log.d("thread","null");

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.d("button_press","vol_up released");
                        break;
                    case R.id.imageButton_vol_down :
                        ((ImageButton) view).setImageResource(R.drawable.volume_down_not_pressed);
                        try {
                            sendCommand("voldown_release");
                            if (thread !=null){
                                thread.interrupt();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case R.id.imageButton_play :
                        ((ImageButton) view).setImageResource(R.drawable.play_button_not_pressed);
                        try {
                            sendCommand("play_release");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case R.id.imageButton_previous:
                        ((ImageButton) view).setImageResource(R.drawable.previous_not_pressed);
                        try {
                            sendCommand("fast_reverse_release");
                            if (thread != null){
                                thread.interrupt();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case R.id.imageButton_fast_forward :
                        ((ImageButton) view).setImageResource(R.drawable.fast_forward_not_pressed);
                        try {
                            sendCommand("fast_forward_release");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (thread != null){
                            thread.interrupt();
                        }
                        break;
                        //media panel events handled
                }
            }
            return false;
        }
    };

    public List<DesktopApps> getAppsArrayList (){
        return this.appsList;
    }

    public boolean getUp (){
        return up;
    }
    class DoThread extends Thread implements Runnable{
        String command;
        DoThread(String command){
            this.command = command;
        }
        @Override
        public void run() {
            while (!Thread.interrupted() && !getUp()){
                try {
                    sendCommand(command);
                    Thread.sleep(40);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.d("thread_while","out of loop");
        }
    }

    //testing- worked

    class ConnectTask extends AsyncTask <Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(mContext, "Initiating Connection", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try{
                socket = connectedDevice.createRfcommSocketToServiceRecord(SERIAL_UUID);
                socket.connect();
                Log.d ("SOCKET","connected");
                outputStream = new DataOutputStream(socket.getOutputStream());
                inputStream = new DataInputStream(socket.getInputStream());
                Log.d("CHECK","inputStream is null :"+inputStream.equals(null)+"outputStream null "+outputStream.equals(null));
                connected = true ;
            }
            catch (IOException e){
                e.printStackTrace();
                Log.d ("SOCKET","not connected");
                connected = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (connected){
                Toast.makeText(mContext,"Connection Successfully",Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(mContext,"Unable to Connect, Try Again",Toast.LENGTH_SHORT).show();
            }
        }
    }

    //create the trackpad listener
    View.OnTouchListener trackpad_listener = new View.OnTouchListener(){

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            doubleTapGestureDetector.onTouchEvent(event);
            if (connected) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        xpos = event.getX();
                        ypos = event.getY();
                        mouse_moved = false;
                        if (firstTime == 0 || event.getEventTime() -firstTime >TIMEOUT)
                            reset(event.getDownTime());
                        //time_drag = event.getDownTime();
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        if (event.getPointerCount() == 2){
                            //Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
                            seperateTouch = true;
                            mouse_moved = false;
                        }
                        else{
                            firstTime = 0;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!seperateTouch){
                            disx = event.getX() - xpos;
                            disy = event.getY() - ypos;
                            xpos = event.getX();
                            ypos = event.getY();
                            // if mouse displacement!=0 then send the new mouse coordinates
                            if (disx != 0 || disy != 0) {
                                //send the mouse coordinates
                                String cmd=disx+"   "+disy;
                                if (doubleTap){
                                    // if double tap was withing 5 units, consider it by accident
                                    if (Math.abs(xpos - doublexpos)>=5 && (Math.abs(ypos - doubleypos))>=5){
                                        try {
                                            sendCommand(cmd);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                else
                                {
                                    try {
                                        sendCommand(cmd);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                Log.d("mouse",cmd);
                            }
                            mouse_moved = true;}
                        break;
                    case MotionEvent.ACTION_UP:
                        //consider a tap only if user did not move mouse after action down
                        Log.d("ACTION_UP","motion recieved");
                        String cmd="";
                        if (!mouse_moved) {
                            //send a mouse click left or right
                            if (seperateTouch && right_click_two_finger_touch)
                            {
                                if(!vibrator_na){
                                    vibrator.vibrate(vibrator_time);
                                }
                                //right click
                                cmd = "rmb";
                                try {
                                    Log.d("ACTION_UP","send rmb");
                                    if (!isLaserOn){
                                        sendCommand(cmd);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                seperateTouch = false;
                            }
                            else {
                                //Log.d("ACTION_UP","here");
                                if(!vibrator_na){
                                    vibrator.vibrate(vibrator_time);
                                }
                                cmd = "lmb"; //left click
                                try {
                                    Log.d("ACTION_UP","send lmb");
                                    if (!isLaserOn){
                                        sendCommand(cmd);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (doubleTap && !isLaserOn){
                                try {
                                    sendCommand("mouserelease");
                                    Log.d("ACTION UP","mouserelease double tap when mouse not moved");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                doubleTap = false;
                            }
                        }
                        else{
                            //mouse is moved
                            if (doubleTap && !isLaserOn){
                                try {
                                    sendCommand("longClickUp");
                                    Log.d("ACTION UP","mouserelease double tap when mouse is moved");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                }
            }
            return true;
        }
    };
    public void sendCommand (String cmd) throws IOException{
        if (outputStream == null){
             alertDialog_disconnect = new AlertDialog.Builder(getmContext()).setTitle("Disconnection Found").setMessage("Seems like you lost the connection. Please restart the reciever side application.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(!vibrator_na){
                        vibrator.vibrate(vibrator_time);
                    }
                    dialogInterface.dismiss();
                    Intent intent = new Intent(getmContext(),MainActivity.class);
                    startActivity(intent);
                }
            }).show();
            alertDialog_disconnect.setCanceledOnTouchOutside(false);
            alertDialog_disconnect.setCancelable(false);

        }
        else
        {
            outputStream.writeUTF(cmd);
        }
    }

    public Context getmContext(){
        return mContext;
    }

    @Override
    protected void onPause() {
        super.onPause();
       // InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        try {
            sendCommand("pauseThread");
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // save the apps list in sharedPreferences
        SharedPreferences preferences = getSharedPreferences("MyPrefs",Context.MODE_APPEND);
        save = gson.toJson(appsList,type);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("MyList",save);
        editor.putBoolean("firstrun",false);
        editor.commit();
        Log.d("SharedPreferences","onPause List is saved");
        if (alertDialog_disconnect != null){
            alertDialog_disconnect.dismiss();
          //  dismissCurrDialog(2);
        }
       /* if (is_explorer_active) {
            String save2 = "";
            // task to save the data before the connection thread is paused
            int s = filesDirectories.size();
            temp_list = filesDirectories;
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs",MODE_APPEND);
            SharedPreferences.Editor editor1 = sharedPreferences.edit();
            editor1.putBoolean("EXPLORER_ACTIVE",is_explorer_active);
            editor1.commit();
            filesDirectories.clear();
            filesAdapter.notifyItemRangeRemoved(0,s);
        }*/
        if (is_explorer_active){
            // this shows that the file explorer dialog has been opened during app run
            if (dialog_file_explorer.isShowing()){
                //dismiss the dialog
                dialog_file_explorer.dismiss();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        Log.d("onKeyDown","called");
        if (keyCode == 67){
            try {
                sendCommand("backspace");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return super.onKeyDown(keyCode, event);
    }



    @Override
    protected void onResume() {
        super.onResume();
       // connectTask.cancel(true);
        // task can be executed only once
        new ConnectTask().execute();
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs",Context.MODE_APPEND);
        if (sharedPreferences.contains("MyList")){
            //this is not the first time
            firstrun = false;
            String json = sharedPreferences.getString("MyList",null);
            if (json != null){
                ArrayList<DesktopApps> list = gson.fromJson(json,type);
                if (list != null){
                    appsList = list;
                }
                else {
                    Log.d("SharedPrefrences","error retrieving the list ");
                }
            }
        }
        right_click_two_finger_touch = sharedPreferences_settings.getBoolean("two_finger_tap",true);
        vibrate = sharedPreferences_settings.getBoolean("vibrate_check",false);
        vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        sleepIndicator = sharedPreferences_settings.getBoolean("sleep_check",false);
        if (sleepIndicator){
            trackpad.setKeepScreenOn(true);
        }
        else{
            trackpad.setKeepScreenOn(false);
        }
        if (vibrator.hasVibrator()){
            if (vibrate){
                vibrator_time = 50;
            }
            else {
                vibrator_time = 0;
            }
        }
        else {
            vibrator_na = true;
            Toast.makeText(this,"Vibrator hardware could not be found.",Toast.LENGTH_LONG).show();
        }
        /*if  (sharedPreferences.getBoolean("EXPLORER_ACTIVE",false)){
            // task to execute the same command as before the thread was paused
            AcceptThread acceptThread  = new AcceptThread(inputStream,mContext);
            Thread arr [] = {acceptThread};
            new checkThread().execute(arr);
            try {
                sendCommand("STARTTHREAD");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // complete this class
    public static class checkThread extends AsyncTask <Thread,Void,Void> {

        private Thread acceptThread;
        //this class will check when an accept threda will ocmplete
        // once the accept Thread is destroyed, then we can upfdate the ui to display the result/error
        @Override
        protected Void doInBackground(Thread... threads) {
            acceptThread = threads[0];
            Log.d("AcceptThread","AsyncTaskStarted");
            while (acceptThread.isAlive()){
                // keep looping untill thread is runnning
                //Log.d("AcceptThread,Run","Running");
            }
            return null;
        }

        // Thread execution finished, update the UI for changes
        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d("AcceptThread", "AsyncTaskFinished");
           // testView.setText(DeviceActivity.result_accept_thread);
            //Toast.makeText(mContext, DeviceActivity.result_accept_thread, Toast.LENGTH_SHORT).show();
            // update the recyclerView in the file explorer_Dialog
            /*DeviceActivity.filesDirectories = generateListFromResult();
            filesAdapter = new FilesAdapter (filesDirectories,mContext);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
            recyclerView_file_explorer.setLayoutManager(linearLayoutManager);
            RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL_LIST);
            recyclerView_file_explorer.addItemDecoration(itemDecoration);
            recyclerView_file_explorer.setItemAnimator(new DefaultItemAnimator());
            recyclerView_file_explorer.setAdapter(filesAdapter);*/
            // all the explored paths hsould now be stored inside the results variable
            new DeviceActivity().updateResult();
        }
    }


    public void updateResult (){
        // update the recycler view with the file recieved,
        // clear the previous explored files
        //filesAdapter.clearData();
        //filesAdapter.notifyDataSetChanged();
        filesAdapter.clearData();
        filesAdapter.notifyDataSetChanged();
        recyclerView_file_explorer.setAdapter(filesAdapter);
        //results holds the new list
        for (int i = 0; i<DeviceActivity.results.size(); i++){
            // loop through each result and add the result to the recyclerview
            DeviceActivity.result_accept_thread = DeviceActivity.results.get(i); // not necesssray
            String props[]  = DeviceActivity.result_accept_thread.split("\\n");
            if (props.length == 3) {
                for (int j = 0; j<props.length; j++){
                    Log.d("PROPS_LENGTH",props.length+"");
                    props[j] = props[j].trim();
                    Log.d("PROPS",props[j]+" "+j);
                }
                FilesDirectories file = new FilesDirectories();
                if (props[2].equals("file")){
                    file.setFile(true);
                    Log.d("Check",props[2]);
                }
                file.setName(props[0]);
                file.setPath(props[1]);
                filesDirectories.add(file);
                Log.d("FilesAdapter",filesAdapter.equals(null)+"");
                filesAdapter.notifyDataSetChanged();
            }
        }
        for (int j = 0;j<filesDirectories.size();j++){
            Log.d("CHECK_ARRAY",filesDirectories.get(j).getPath()+"\n");
        }
    }
}
