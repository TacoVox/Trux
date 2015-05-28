package se.gu.tux.trux.gui.base;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.application.LoginService;

import se.gu.tux.trux.application.SettingsHandler;
import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.User;
import se.gu.tux.trux.gui.main_home.MainActivity;
import se.gu.tux.trux.technical_services.BackgroundService;
import se.gu.tux.trux.technical_services.NotificationService;
import se.gu.tux.trux.technical_services.ServerConnector;
import tux.gu.se.trux.R;

/**
 * Created by ivryashkov on 2015-04-30.
 *
 * Base class for the application.
 *
 */
public class BaseAppActivity extends ActionBarActivity
{

    // keeps track of the current view showing on the screen
    private int currentViewId;
    private MenuItem logoutItem;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // inflate menu
        getMenuInflater().inflate(R.menu.menu, menu);
        logoutItem = menu.getItem(4);

        validateOptions();
        // return menu
        return super.onCreateOptionsMenu(menu);
    }



    public boolean onPrepareOptionsMenu (Menu menu)
    {
        validateOptions();
        return super.onPrepareOptionsMenu(menu);
    }



    /**
     * Enables and disables the logout item in the menu
     * based on if the user is logged in or not.
     */
    public void validateOptions()
    {
        if (DataHandler.getInstance().isLoggedIn())
        {
            logoutItem.setEnabled(true);
        }
        else
        {
            logoutItem.setEnabled(false);
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.action_settings)
        {
            Intent intent = new Intent(this, SettingsMenuActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.action_about)
        {
            Intent intent = new Intent(this, AboutMenuActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.action_contact)
        {
            Intent intent = new Intent(this, ContactMenuActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.action_help)
        {
            // get the current view id and the about data for it
            String[] dialogData = getHelpData(getCurrentViewId());

            // display a dialog with the about information
            showDialogBox(dialogData[0], dialogData[1]);
        }
        else if (id == R.id.action_logout)
        {
            logout();
        }

        return super.onOptionsItemSelected(item);

    } // end onOptionsItemSelected()



    /**
     * Sets the current view id showing on the screen.
     *
     * @param viewId    The view id.
     */
    public void setCurrentViewId(int viewId)
    {
        currentViewId = viewId;
    }



    /**
     * Returns the current view id showing on the screen.
     *
     * @return      The current view id showing on the screen.
     */
    public int getCurrentViewId()
    {
        return currentViewId;
    }



    /**
     * Shows a short toast on the screen. Toast is not displayed
     * if message is empty.
     *
     * @param message   The message to show.
     */
    public void showToast(String message)
    {
        // if message is empty, return
        if (message.isEmpty())
        {
            return;
        }
        // make a toast and show
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }



    /**
     * Shows a dialog on the screen. Dialog is not displayed
     * if message is empty.
     *
     * @param title     The title of the dialog box.
     * @param message   The message to display.
     */
    protected void showDialogBox(String title, String message)
    {
        // if message is empty, return
        if (message.isEmpty())
        {
            return;
        }

        // create a dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        // build the dialog, set title, set message, etc.
        dialogBuilder.setTitle(title).setMessage(message).setPositiveButton("OK",
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        dialogInterface.dismiss();
                    }
                }).create();

        // show dialog
        dialogBuilder.show();
    }



    /**
     * Helper method. Gets the help information to display based
     * on the current view showing. Returns a String array with the
     * title at index 0 and the message at index 1.
     *
     * @param viewID    The current view id showing.
     * @return          String[]
     */
    private String[] getHelpData(int viewID)
    {
        // the array to return
        String[] aboutData = new String[2];

        // fill in the data according to view showing
        if (viewID == R.layout.activity_main)
        {
            aboutData[0] = "Main Screen";
            aboutData[1] = getResources().getString(R.string.main_screen_help);
        }
        else if (viewID == R.layout.activity_home)
        {
            aboutData[0] = "Help";
            aboutData[1] = getResources().getString(R.string.driver_home_screen_help);
        }
        else
        {
            aboutData[0] = "Help";
            aboutData[1] = getResources().getString(R.string.driver_home_screen_help);
        }

        // return the array
        return aboutData;

    } // end getHelpData()



    /**
     * Logs out the user from the application. Redirects to main screen.
     */
    private void logout()
    {
        AsyncTask<Void, Void, Boolean> task = new LogoutTask().execute();

        boolean check = false;

        try
        {
            check = task.get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }

        Intent background = new Intent(this, BackgroundService.class);
        stopService(background);
        showToast("Background service stopped");

        Intent notification = new Intent(this, NotificationService.class);
        stopService(notification);
        showToast("Notification service stopped");

        // Make sure there is no history for the back button
        if (getFragmentManager().getBackStackEntryCount() > 0)
        {
            getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    } // end logout()



    /**
     * Private class to perform async task. Logs out the user.
     */
    private class LogoutTask extends AsyncTask<Void, Void, Boolean>
    {

        @Override
        protected void onPreExecute()
        {
            Toast.makeText(getApplicationContext(), "Logging out. Please wait...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Boolean doInBackground(Void... voids)
        {
            // return the result from logout request
            return LoginService.getInstance().logout();
        }

    } // end inner class


    /**
     * We make sure to store anything critical that we need to make sure isn't killed by
     * garbage collector
     * @param outState      The bundle we save to
     */
    @Override
    protected void onSaveInstanceState (Bundle outState) {
        //
        outState.putSerializable("user", DataHandler.gI().getUser());
        outState.putSerializable("settingsHandler", SettingsHandler.getInstance());
        super.onSaveInstanceState(outState);
    }


    /**
     * Restore things, see comments on onSaveInstanceState.
     * @param savedInstanceState    The bundle we restore from
     */
    @Override
    protected void onRestoreInstanceState (Bundle savedInstanceState) {
        DataHandler.gI().setUser((User)savedInstanceState.getSerializable("user"));
        SettingsHandler.setInstance(
                (SettingsHandler)savedInstanceState.getSerializable("settingsHandler"));
        super.onRestoreInstanceState(savedInstanceState);
    }
} // end class
