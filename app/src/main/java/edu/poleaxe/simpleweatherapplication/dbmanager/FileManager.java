package edu.poleaxe.simpleweatherapplication.dbmanager;

import android.Manifest;
import android.app.Activity;
import edu.poleaxe.simpleweatherapplication.support.CheckRuntimePermissions;
import edu.poleaxe.simpleweatherapplication.support.LogManager;

import java.io.File;
import java.io.IOException;

/**Class to work with Files on storages. Check permissions to work, check existance of dirs/files, create dirs/files
 * Created by Aleksandr Ulianov (poleaxe) on 14.06.2017.
 */
public class FileManager {
    private Activity parentActivity;
    private CheckRuntimePermissions checkRuntimePermissions =  new CheckRuntimePermissions();

    /**
     * requests permission to work with storages.
     */
    private void RequestPermission(){
        checkRuntimePermissions.SetParametersToRun(parentActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        checkRuntimePermissions.RequestPermission();
    }

    /**
     * method to check permissions to work with storages that could be changed by end user after App was installed
     * @return Boolean value are permissions grated or not
     */
    private boolean CheckForPermissionsToWorkWithFiles(){

        if (parentActivity == null) {
            throw new IllegalArgumentException("Ooooops! Critical internal error occured. There is no possibility to work with saved settings! We are really sorry");
        }

        checkRuntimePermissions.SetParametersToRun(parentActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (!checkRuntimePermissions.CheckForPermission()){
            checkRuntimePermissions.execute();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                (new LogManager()).captureLog(parentActivity, e.getMessage());
            }
        }

        return checkRuntimePermissions.CheckForPermission();
    }

    /**
     * method to check existance of a directory and create it if not exists
     * @param dirToCreate full path to the directory to check and create
     * @throws SecurityException in cases when there is no access because of security reasons.
     * @throws IOException in case of inability ro create a dir because of hardware issues.
     */
    private void CheckOrMakeDir(File dirToCreate) throws SecurityException, IOException {
        if (!dirToCreate.exists()){
            if (!dirToCreate.mkdir()){
                throw new IOException("Had no possibility to create dir: " + dirToCreate.getAbsolutePath());
            }
        }
    }

    /**
     * method to check existance of a file and create it if not exists
     * @param fileToCreate full path to the file to check and create
     * @throws SecurityException in cases when there is no access because of security reasons.
     * @throws IOException in case of inability ro create a dir because of hardware issues.
     */
    private void CheckOrCreateFile(File fileToCreate) throws SecurityException, IOException {
        if(!fileToCreate.exists()){
            if (!fileToCreate.createNewFile()){
                throw new IOException("Unable to create file on the path " + fileToCreate.getAbsolutePath());
            }
        }
    }

    /**
     * method to check if file with DB is available. In case there is no file - create new one to store settings DB
     * @param filePath String full path to directory where the DB file should be stored
     * @param fileNameWithExtentios String file name of the DB
     * @param parentActivity Activity where from method was called
     * @return File where DB will be stored. could be NULL in case it is impossible to instantiate a file because of different reasonss
     */
    public File CheckOrCreateFileByPath(String filePath, String fileNameWithExtentios, Activity parentActivity){

        this.parentActivity = parentActivity;

        if (!CheckForPermissionsToWorkWithFiles()){
            (new LogManager()).captureLog(parentActivity, "Permission to work with files on storage was not granted");
            return null;
        }

        RequestPermission();

        File fileDirToCheck = new File(filePath);
        try {
            CheckOrMakeDir(fileDirToCheck);
        } catch (IOException|SecurityException e) {
            new LogManager().captureLog(parentActivity.getApplicationContext(), e.getMessage());
            return null;

        }

        File fileToCheck = new File(fileDirToCheck + "/" + fileNameWithExtentios);
        try {
            CheckOrCreateFile(fileToCheck);
        } catch (IOException|SecurityException e) {
            new LogManager().captureLog(parentActivity.getApplicationContext(), e.getMessage());
            return null;
        }

        return fileToCheck;
    }
}