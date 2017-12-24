package com.psx.projectcontrol;

/**
 * Created by Pranav on 14-11-2016.
 */
//POJO class for representing a file/folder
public class FilesDirectories {

    public boolean isFile;
    public String name;
    public String path;

    //constructor
    public FilesDirectories (boolean isFile, String name, String path) {
        this.isFile  = isFile; // true if its a file, else its a directory
        this.name = name;
        this.path = path;
    }

    // empty constructor
    public FilesDirectories (){

    }

    public void setFile (boolean isFile){
        this.isFile = isFile;
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
