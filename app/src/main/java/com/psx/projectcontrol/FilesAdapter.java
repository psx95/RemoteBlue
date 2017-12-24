package com.psx.projectcontrol;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pranav on 14-11-2016.
 */

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.MyViewHolder> {

    List<FilesDirectories> filesDirectories;
    FilesDirectories curr_fileDirectories;
    Context context;
    //DeviceActivity deviceActivity = new DeviceActivity();
    Gson gson = new Gson();
    Type type = new TypeToken<List<DesktopApps>>(){}.getType();
    RecyclerView recyclerView;
    DataInputStream inputStream;
   // private final View.OnClickListener onClickListener = new MyOnClickListener();
    //constructor
    public FilesAdapter (List<FilesDirectories> filesDirectories, Context context, RecyclerView recyclerView, DataInputStream dataInputStream){
        this.filesDirectories = filesDirectories;
        this.context = context;
        this.recyclerView = recyclerView;
        this.inputStream = dataInputStream;
    }

    public void generateCommand (String path){
        // function to check the command and send it
        // give a call to the sendCommand Fucntion
        /*StringBuilder stringBuilder = new StringBuilder(path);
        for (int i =0; i< stringBuilder.length();i++){
            char x = stringBuilder.charAt(i);
            if (x == '\\'){
                stringBuilder.insert(i,'\\');
            }
        }*/
        // the abive implementation is not feasible taking a lot of memory
        String finalpath ="";
        char x;
        for (int i =0; i<path.length();i++){
            x= path.charAt(i);
            if (x == '\\'){
                finalpath+='\\';
                finalpath+='\\';
            }
            else{
                finalpath+=x;
            }
        }
        try {
            Log.d("GENERATED_COMMAND",""+finalpath);
            ((DeviceActivity)context).sendCommand("explore"+finalpath);
            Log.d("FILESADAPTER","command sent");
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public FilesAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_row_list,parent,false);
       // view.setOnClickListener(onClickListener);
        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final FilesAdapter.MyViewHolder holder, final int position) {
        // set the values in the fields
        curr_fileDirectories = filesDirectories.get(position);
        holder.file_name.setText(curr_fileDirectories.getName());
        holder.file_path.setText(curr_fileDirectories.getPath());
        /*
        * this code is only for testing purpose.*/
        for (int i = 0; i<filesDirectories.size(); i++){
            Log.d("FilesAdapter",filesDirectories.get(i).getName()+" "+filesDirectories.get(i).isFile);
        }
        // check if the current object is file or directory
        if (!curr_fileDirectories.isFile){
            // not a file, change the icon
            holder.type.setImageResource(R.drawable.ic_folder);
        }

        // set onCLick listener on the main_container_holder
        holder.main_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // send the command with the actual path of the file or directory
                // check if its file or directory will occour at server
                TextView textView = (TextView) view.findViewById(R.id.file_path);
                String path = textView.getText().toString();
                Log.d("FILE_PATH",path);
                Toast.makeText(context,"Click recieved on positon "+position,Toast.LENGTH_SHORT).show();
                // if the path relates to a folder
                if (!filesDirectories.get(position).isFile){
                    AcceptThread acceptThread = new AcceptThread(inputStream,context);
                    acceptThread.start();
                    // clear the view recycler view
                    Thread [] arr = {acceptThread};
                    Log.d("AsyncTaskInAdapter","execute()");
                    //DeviceActivity.checkThread.execute(arr);
                    clearData();
                    DeviceActivity.checkThread ch = new DeviceActivity.checkThread();
                    ch.execute(arr);
                    // generate the accurate command to browse the path
                    generateCommand(path);
                    // generate command will generate as well as send the accurate command to explore the path
                    // open a new Accept Thread for the new command result
                }
                else{
                    // apply logic if its a file
                    // if a file is clicked, it should be opened
                    generateOpenCommand(path);
                }
            }

        });

        holder.dot_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context,holder.main_container, Gravity.END);
                popupMenu.getMenuInflater().inflate(R.menu.menu_extra_options, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        switch (id){
                            case R.id.add_to_quick_launch:
                                // not working yet
                                // implement logic for add to quick launch
                                DesktopApps apps = new DesktopApps(filesDirectories.get(position).getName(),filesDirectories.get(position).getPath());
                                ((DeviceActivity)context).getAppsArrayList().add(apps);
                                /*List <DesktopApps> desktopAppsList = new ArrayList<DesktopApps>();
                                SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs",Context.MODE_APPEND);
                                if (sharedPreferences.contains("MyList")){
                                    Log.d("FilesAdapter","retrieving from sharedPreferences");
                                    String json = sharedPreferences.getString("MyList",null);
                                    Log.d("JSON before",json);
                                    if (json != null){
                                        ArrayList<DesktopApps> list = gson.fromJson(json,type);
                                        if (list != null){
                                            desktopAppsList = list;
                                        }
                                        else {
                                            Log.d("SharedPrefrences","error retrieving the list ");
                                        }
                                    }
                                }
                                SharedPreferences preferences = context.getSharedPreferences("MyList",Context.MODE_APPEND);
                                Log.d("FilesAdapter","Saving in sharedPrefrenecs ");
                                desktopAppsList.add(apps);
                                String save = gson.toJson(desktopAppsList,type);
                                Log.d("JSON after",save);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("MyList",save);
                                editor.commit();*/
                               // Toast.makeText(context,"Clicked on",Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.download_file:
                                // implement logic for save file to device
                                //NOTE: can be only applied on files, not on folders
                                if (filesDirectories.get(position).isFile){
                                    Toast.makeText(context,"This feature will be added in the future versions",Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(context,"This feature will be added in the future versions",Toast.LENGTH_SHORT).show();
                                }
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

    }

    private void generateOpenCommand (String path){
        try {
            Log.d("OPEN_COMMAND",path + " is opened");
            ((DeviceActivity)context).sendCommand("open"+path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // function to remove all the data from the recyclerview
    // will be used while starting a new accept thread on recieving a click
    public void clearData ()
    {
        int s = this.filesDirectories.size();
        /*Log.d("FilesDirectories Size:",s+"");
        if (s > 0){
            for (int i = 0; i<s; i++){
                Log.d("removing",filesDirectories.get(0).getPath());
                this.filesDirectories.remove(0);
            }
            this.notifyItemRangeRemoved(0,s);
            this.notifyDataSetChanged();
        }*/
        filesDirectories.clear();
        notifyItemRangeRemoved(0,s);
    }

    @Override
    public int getItemCount() {
        return filesDirectories.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        //create the variables you want in the view
        public TextView file_name; // name of the file/directory
        public TextView file_path; //path of teh fie or directory
        public ImageView type; // an image to identify the type of file/directoy
        public RelativeLayout main_container;
        public ImageButton dot_menu;

        public MyViewHolder(View itemView) {
            super(itemView);
            file_name = (TextView) itemView.findViewById(R.id.file_name);
            main_container = (RelativeLayout) itemView.findViewById(R.id.holder_path_name);
            file_path = (TextView) itemView.findViewById(R.id.file_path);
            type = (ImageView) itemView.findViewById(R.id.file_type_image);
            dot_menu = (ImageButton) itemView.findViewById(R.id.imageButton_menu);
        }
    }
}
