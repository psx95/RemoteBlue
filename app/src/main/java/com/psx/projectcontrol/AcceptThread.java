package com.psx.projectcontrol;

import android.bluetooth.BluetoothServerSocket;
import android.content.Context;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by Pranav on 08-11-2016.
 */

// this class waits and accepts incoming requests from the server

public class AcceptThread extends Thread {
    // the local server socket
    private DataInputStream inputStream;
    String res="";
    Context context;

    public AcceptThread (DataInputStream dataInputStream, Context context)
    {
        inputStream = dataInputStream;
        this.context = context;
    }

    @Override
    public void run() {
        // implement logic for the thread
        long start = System.currentTimeMillis();
        DeviceActivity.results.clear();
        while (!Thread.currentThread().isInterrupted()) {
            try {
                if (inputStream!=null){
                    res = inputStream.readUTF();
                    Log.d("RES",res);
                    // each res will contain a FilesDirectories object
                    DeviceActivity.result_accept_thread = res;
                    // call an update function to update the changes recieved from this thread
                    //((DeviceActivity)context).updateResult();
                    // error cannot upfdate the UI while the main thread is running.
                    //Possible Fix : create an array of string results, return that too the main UI thread, uodate te entire UI at once -This maybe slow
                    // if there are too many files to list
                    DeviceActivity.results.add(res);
                    // results should contain the entire list of addresses and names of the directories/files
                  //  Log.d("AcceptThread",res);
                    Log.d("AcceptThread","here");
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
            long end = System.currentTimeMillis();
            if (end-start >= 10000 || (res.equals("END_OF_REQUEST_REACHED"))){
                //timeout Thread
                break;
            }
        }
    }
}
