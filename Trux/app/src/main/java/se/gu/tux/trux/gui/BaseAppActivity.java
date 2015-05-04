package se.gu.tux.trux.gui;

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

import se.gu.tux.trux.appplication.LoginService;

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
    private static int currentViewId;



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
        // return menu
        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.action_settings)
        {
            startActivity(new Intent(this, SettingsMenuActivity.class));
        }
        else if (id == R.id.action_about)
        {
            startActivity(new Intent(this, AboutMenuActivity.class));
        }
        else if (id == R.id.action_contact)
        {
            startActivity(new Intent(this, ContactMenuActivity.class));
        }
        else if (id == R.id.action_help)
        {
            // get the current view id and the about data for it
            String[] dialogData = getAboutData(getCurrentViewId());

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
    protected void showToast(String message)
    {
        // if message is empty, return
        if (message.isEmpty())
        {
            return;
        }
        // make a toast and show
        Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
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
     * Helper method. Gets the about information to display based
     * on the current view showing. Returns a String array with the
     * title at index 0 and the message at index 1.
     *
     * @param viewID    The current view id showing.
     * @return          String[]
     */
    private String[] getAboutData(int viewID)
    {
        // the array to return
        String[] aboutData = new String[2];

        // fill in the data according to view showing
        if (viewID == R.layout.activity_main)
        {
            aboutData[0] = "Main Screen";
            aboutData[1] = getResources().getString(R.string.main_screen_help);
        }
        else if (viewID == R.layout.activity_driver_home_screen)
        {
            aboutData[0] = "Home Screen";
            aboutData[1] = getResources().getString(R.string.driver_home_screen_help);
        }
        else
        {
            aboutData[0] = "No Help Available";
            aboutData[1] = "No help information available yet for this screen.";
        }

        // return the array
        return aboutData;

    } // end getAboutData()



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

        if (check)
        {
            // Right now we don't care about the result, hence the same actions
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
            startActivity(new Intent(this, MainActivity.class));
        }
        else
        {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
            startActivity(new Intent(this, MainActivity.class));
        }

    } // end logout()



    /**
     * Private class. Logs out the user.
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


} // end class
