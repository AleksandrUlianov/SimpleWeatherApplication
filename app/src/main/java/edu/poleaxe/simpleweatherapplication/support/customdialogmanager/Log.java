package edu.poleaxe.simpleweatherapplication.support.customdialogmanager;

import java.io.File;

/** class to support logs with errors
 * Created by Aleksandr Ulianov (poleaxe) on 11.06.2017.
 */
public class Log {
    //TODO Log class. GetLogFile(), CreateLogFile(), WriteLog(), CollectApplicationData()

    private String CollectApplicationData(){
        //TODO implement gathering of application conditions under which an error happened
        return "";
    }

    private File GetLogFile(){
        //TODO implement getting file from appropriate directory or creation if there is no file
        File logFile = CreateLogFile();
        return logFile;

    }

    private File CreateLogFile() {
        //TODO implement creation of a file in appropriate directory. Add permission.
        File logFile = null;

        return logFile;
    }

    public void WriteLog(){
        //TODO implement writing data into file
        File logFile = GetLogFile();
        String applicationConditions = CollectApplicationData();
    }
}
