package se.gu.tux.trux.gui;

import android.app.FragmentManager;
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
        else if (id == R.id.action_logout)
        {
            logout();
        }

        return super.onOptionsItemSelected(item);

    } // end onOptionsItemSelected()



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
          // TODO: react to successful or failed logout attempt
        }
        else
        {

        }

        // Make sure there is no history for the back button
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
