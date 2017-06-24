package edu.poleaxe.simpleweatherapplication.support.customdialogmanager;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;
import edu.poleaxe.simpleweatherapplication.customenums.DialogsTypesEnum;
import edu.poleaxe.simpleweatherapplication.support.LogManager;

/**class to display appropriate dialog or toast according to input parameters. Also processes parameter to be valid.
 * Created by Aleksandr Ulianov (poleaxe) on 28.05.2017.
 */
public class DialogManager {

    /**
     * Method to show a toast
     * @param activityToDisplay activity where a toast should be shown. Activity type
     * @param messageToDisplay of String type
     */
    private void ShowToast(Activity activityToDisplay, String messageToDisplay){
        Toast toast = Toast.makeText(activityToDisplay, messageToDisplay, Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Method to show a toast
     * @param activityToDisplay activity where a toast should be shown. Activity type
     * @param messageToDisplay of String type
     */
    private void ShowAlertDialog(Activity activityToDisplay, String messageToDisplay){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activityToDisplay);
        alertDialogBuilder.setMessage(messageToDisplay).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialogBuilder.create();
        alertDialogBuilder.show();
    }

    /**
     *
     * @param dialogTypeToDisplay of DialogTypesEnum type
     * @param messageToDisplay of String type
     * @param activityToDisplay of Activity type
     */
    public void DisplayDialog(DialogsTypesEnum dialogTypeToDisplay, String messageToDisplay, Activity activityToDisplay){

        if (messageToDisplay == null || messageToDisplay.trim().equals("")) {
            new LogManager().captureLog(activityToDisplay.getApplicationContext(), "Empty message to display ina  dialog");
            messageToDisplay = "Unknown error happened";
        }

        if (dialogTypeToDisplay == null){
            dialogTypeToDisplay = DialogsTypesEnum.ERROR;
        }

        if (activityToDisplay == null){
            //unfortunately, application will fall here
            return;
        }

        switch (dialogTypeToDisplay){
            case  TOAST: {
                ShowToast(activityToDisplay, messageToDisplay);
                break;
            }
            case ALERT:
            default:{

                ShowAlertDialog(activityToDisplay, messageToDisplay);
            }
        }
    }
}
