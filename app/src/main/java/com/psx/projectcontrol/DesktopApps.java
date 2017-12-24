package com.psx.projectcontrol;

/**
 * Created by user on 20-07-2016.
 */
//model class
public class DesktopApps {
    private String name;
    private String path;
    public DesktopApps (String name, String path){
        this.name = name;
        this.path = path;
    }
    public void setName (String name){
        this.name = name;
    }
    public void setPath (String path){
        this.path = path;
    }
    public String getName (){
        return this.name;
    }
    public String getPath (){
        return this.path;
    }
}
