package com.psx.projectcontrol;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

/**
 * Created by user on 20-07-2016.
 */
public class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.MyViewHolder> {
    private List<DesktopApps> appsList;
    private Context mContext;
    private DesktopApps desktopApp;
    private int Curr_app;
    private String name_new_app="";
    private String path_new_app="";

    public AppsAdapter (List<DesktopApps> appsList, Context context){
        this.appsList = appsList;
        this.mContext = context;
    }

    public void checkCommand (int pos){
        if (mContext instanceof DeviceActivity){
            try {
                Log.d("APPS","here"+pos);
                switch (pos) {
                    case 0:
                        //send command for this pc
                        Log.d("APPS","this pc");
                        ((DeviceActivity) mContext).sendCommand("explorer shell:MyComputerFolder");
                        break;
                    case 1:
                        //send ocmmand for my documents
                        Log.d("APPS","my Documents");
                        ((DeviceActivity) mContext).sendCommand("explorer shell:My Documents");
                        break;
                    case 2:
                        // send command for Libraries
                        Log.d("APPS","libraries");
                        ((DeviceActivity) mContext).sendCommand("explorer shell:Libraries");
                        break;
                    case 3:
                        Log.d("APPS","control panel");
                        ((DeviceActivity) mContext).sendCommand("explorer shell:ControlPanelFolder");
                        // send command for control panel
                        break;
                    case 4:
                        // send commands for Downloads
                        Log.d("APPS","downloads");
                        ((DeviceActivity) mContext).sendCommand("explorer shell:Downloads");
                        break;
                    case 5:
                        // send command for TaskView
                        Log.d("APPS","taskview");
                        ((DeviceActivity) mContext).sendCommand("taskview");
                        break;
                    default:
                        // handle code for all the other options
                        String path = appsList.get(pos).getPath();
                        ((DeviceActivity) mContext).sendCommand("explorer "+"\""+path+"\"");
                        Log.d("APPS","default");
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public AppsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_row_list,parent,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(AppsAdapter.MyViewHolder holder, final int position) {
        desktopApp = appsList.get(position);
        holder.title.setText(desktopApp.getName());
        holder.path.setText(desktopApp.getPath());
        if (position < 6){
            holder.deleteButton.setImageResource(R.drawable.delete_not_available);
            holder.editButton.setImageResource(R.drawable.edit_not_available);
        }
        else{
            holder.deleteButton.setImageResource(R.drawable.garbage);
            holder.editButton.setImageResource(R.drawable.edit);
        }
        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mContext instanceof DeviceActivity){
                    ((DeviceActivity) mContext).dismissCurrDialog(1);
                }
                checkCommand(position);
            }
        });
        holder.path.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mContext instanceof DeviceActivity){
                    ((DeviceActivity) mContext).dismissCurrDialog(1);
                }
                checkCommand(position);
            }
        });
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (position > 5){
                    removeAt(position);
                }
                else{
                    Toast.makeText(mContext,"Delete Unavailable for this Application",Toast.LENGTH_LONG).show();
                }
            }
        });
        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //display a dialog to allow to change the path of tha application project
                if (position > 5) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("Edit App");
                    final EditText editText_name = new EditText(mContext);
                    editText_name.setInputType(InputType.TYPE_CLASS_TEXT);
                    editText_name.setHint("New Application/Folder Name");
                    final EditText editText_path = new EditText(mContext);
                    editText_path.setInputType(InputType.TYPE_CLASS_TEXT);
                    editText_path.setHint("New Application/Folder Complete Path");
                    LinearLayout linearLayout = new LinearLayout(mContext);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    linearLayout.addView(editText_name);
                    linearLayout.addView(editText_path);
                    builder.setView(linearLayout);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            name_new_app = editText_name.getText().toString();
                            path_new_app = editText_path.getText().toString();
                            if (name_new_app.equals("") || path_new_app.equals("")) {
                                Toast.makeText(mContext, "Name or Path is empty", Toast.LENGTH_LONG).show();
                            } else {
                                //edit the desktop app
                                editGameAt(position, name_new_app, path_new_app);
                            }
                        }
                    });
                    builder.show();
                }
                else {
                    Toast.makeText(mContext,"Edit Unavailable for this Application",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void editGameAt (int pos,String name, String path){
        DesktopApps app = appsList.get(pos);
        app.setName(name);
        app.setPath(path);
        notifyItemChanged(pos);
    }

    @Override
    public int getItemCount() {
        return appsList.size();
    }

    public void removeAt (int pos){
        appsList.remove(pos);
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos,appsList.size());
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public TextView path;
        public ImageButton deleteButton;
        public ImageButton editButton;

        public MyViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            deleteButton = (ImageButton) itemView.findViewById(R.id.delete_button);
            editButton = (ImageButton) itemView.findViewById(R.id.edit_image);
            path = (TextView) itemView.findViewById(R.id.app_path);
        }
    }
}
