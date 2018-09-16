package com.example.riskipradana.loader;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener{

    public static final String TAG = "ContactApp";
    private ListView lvContact;
    private ProgressBar progressBar;
    private ContactAdapter adapter;
    private final int CONTACT_LOAD_ID = 110;
    private final int CONTACT_PHONE_ID = 120;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lvContact = findViewById(R.id.lv_contact);
        lvContact.setVisibility(View.INVISIBLE);
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        adapter = new ContactAdapter(this, null, true);
        lvContact.setAdapter(adapter);
        lvContact.setOnItemClickListener(this);

        //Loader with id =  CONTACT_LOAD_ID started -> Load Contact
        getSupportLoaderManager().initLoader(CONTACT_LOAD_ID, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

        Log.d(TAG, "LoadStarted");

        /**
         * Cursor object (CursorLoader) is look like table which has column and row
         */
        CursorLoader cursorLoader = null;

        if(id == CONTACT_LOAD_ID){

            String [] projectionFields = new String[]{
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts.PHOTO_URI,
            };

            cursorLoader =
                    new CursorLoader(
                            this,
                            ContactsContract.Contacts.CONTENT_URI,
                            projectionFields,
                            ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1",
                            null,
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                    );
        }

        if(id == CONTACT_PHONE_ID){

            String [] phoneProjectionFields =
                    new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};

            cursorLoader =
                    new CursorLoader(
                            this,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            phoneProjectionFields,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " =? AND "
                                    + ContactsContract.CommonDataKinds.Phone.TYPE + " = "
                                    + ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE + " AND "
                                    + ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER +"=1",
                            new String[] {args.getString("id")},
                            null
                    );
        }

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

        Log.d(TAG, "LoadFinished");

        if(loader.getId() == CONTACT_LOAD_ID){
            if(data.getCount() > 0){
                lvContact.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                adapter.swapCursor(data);
            }
        }
        if(loader.getId() == CONTACT_PHONE_ID){

            String contactNumber = null;

            if(data.moveToFirst()){
                contactNumber = data.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }

            Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contactNumber));
            startActivity(dialIntent);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

        if(loader.getId() == CONTACT_LOAD_ID){
            lvContact.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            adapter.swapCursor(null);
            Log.d(TAG, "LoaderReset");
        }
    }

    //Destroy Loader when the activity/fragment is closed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        getSupportLoaderManager().destroyLoader(CONTACT_LOAD_ID);
        getSupportLoaderManager().destroyLoader(CONTACT_PHONE_ID);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Cursor cursor = adapter.getCursor();
        //Move to the selected contact
        cursor.moveToPosition(position);
        //Get the _ID value
        long contactId = cursor.getLong(0);

        Log.d(TAG, "Position : "+position+" "+contactId);

        getPhoneNumber(String.valueOf(contactId));

    }

    private void getPhoneNumber(String contactId) {

        Bundle bundle = new Bundle();
        //Use bundle to put contactId and used contactId as a parameter when Call following Loader
        bundle.putString("id", contactId);
        //Loader with id =  CONTACT_PHONE_ID started -> Get Contact Detail with Number
        getSupportLoaderManager().restartLoader(CONTACT_PHONE_ID, bundle, this);
    }
}
