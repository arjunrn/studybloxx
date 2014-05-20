package tu.dresden.studybloxx;

import tu.dresden.studybloxx.adapters.CourseCursorAdapter;
import tu.dresden.studybloxx.database.StudybloxxDBHelper;
import tu.dresden.studybloxx.providers.StudybloxxProvider;
import tu.dresden.studybloxx.fragments.NoteListFragment;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Toast;


public abstract class NavDrawerActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {


    private static final int COURSE_LIST_LOADER = 10232;
    private static final String TAG = "NavDrawerActivity";
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence note_title;
    // present note's tile
    private CharSequence course_title;
    private ActionBar mActionBar;


    // drawer bar present title

	/*
     * @Override protected void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); //setContentView(R.layout.activity_main);
	 * setupNavigationDrawer(); }
	 */

    protected void setupNavigationDrawer() {
        course_title = note_title = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        // populate the drawer
        mDrawerList.setAdapter(new CourseCursorAdapter(this, null, 0));
        // enable ActionBar app icon to behave as action to toggle nav drawer

        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);

        // click listener

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open, /* "open drawer" description for accessibility */
                R.string.drawer_close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                mActionBar.setTitle(note_title);
                invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }


            public void onDrawerOpened(View drawerView) {
                mActionBar.setTitle(course_title);
                invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        LoaderManager manager = getLoaderManager();
        manager.initLoader(COURSE_LIST_LOADER, null, this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content
        // view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch (item.getItemId()) {
            case R.id.action_search:
                // create intent to perform web search for this planet
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
                // catch event that there's no activity to handle intent
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }


    private void selectItem(int position) {

        FragmentManager fragmentManager = getFragmentManager();
        // fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        Cursor cursor = (Cursor) mDrawerList.getAdapter().getItem(position);
        int courseId = cursor.getInt(0);
        Intent listIntent = new Intent(this, NoteListActivity.class);
        listIntent.putExtra(NoteListFragment.COURSE_ID, courseId);
        startActivity(listIntent);
        mDrawerLayout.closeDrawer(mDrawerList);
    }


    @Override
    public void setTitle(CharSequence title) {
        note_title = title;
        getActionBar().setTitle(note_title);
    }


    /**
     * When using the ActionBarDrawerToggle, you must call it during onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        return new CursorLoader(this, StudybloxxProvider.COURSE_CONTENT_URI, new String[]{StudybloxxDBHelper.Contract.Course.ID, StudybloxxDBHelper.Contract.Course.TITLE},
                null, null, null);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
        Log.d(TAG, "Number of Courses: " + arg1.getCount());
        CourseCursorAdapter adapter = (CourseCursorAdapter) mDrawerList.getAdapter();
        adapter.swapCursor(arg1);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        CourseCursorAdapter adapter = (CourseCursorAdapter) mDrawerList.getAdapter();
        adapter.swapCursor(null);
    }

}
