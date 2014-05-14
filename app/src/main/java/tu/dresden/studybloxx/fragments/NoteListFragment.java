package tu.dresden.studybloxx.fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import tu.dresden.studybloxx.R;
import tu.dresden.studybloxx.adapters.NotesCursorAdapter;
import tu.dresden.studybloxx.database.StudybloxxDBHelper;
import tu.dresden.studybloxx.providers.StudybloxxProvider;


/**
 * A list fragment representing a list of Note. This fragment also supports tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a {@link NoteDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks} interface.
 */
public class NoteListFragment extends ListFragment implements LoaderCallbacks<Cursor> {

    public static final String COURSE_ID = "COURSE_ID";
    /**
     * The serialization (saved instance state) Bundle key representing the activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private static final int NOTE_LOADER = 0;
    private static final int NOTE_LIST_LOADER = 0;
    private static final String TAG = "NoteListFragment";
    /**
     * A dummy implementation of the {@link Callbacks} interface that does nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(long noteId) {
            // TODO Auto-generated method stub
        }
    };
    /**
     * The fragment's current callback object, which is notified of list item clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;
    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;
    private Activity mActivity;
    private int mSelectedCourseId = -1;
    private NotesCursorAdapter mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation changes).
     */
    public NoteListFragment() {
    }

    public static NoteListFragment getInstance(int courseId) {
        NoteListFragment listFragment = new NoteListFragment();
        Bundle args = new Bundle();
        args.putInt(COURSE_ID, courseId);
        listFragment.setArguments(args);
        return listFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_note_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        Bundle args = getArguments();
        if (args != null) {
            mSelectedCourseId = args.getInt(COURSE_ID);
            Log.d(TAG, "Selected Course ID: " + mSelectedCourseId);
        } else {
            Log.d(TAG, "No Selected Course");
        }

        mCallbacks = (Callbacks) activity;
        mActivity = activity;
        mAdapter = new NotesCursorAdapter(mActivity, null, 0);
        setListAdapter(mAdapter);

        LoaderManager loadMan = mActivity.getLoaderManager();
        loadMan.initLoader(NOTE_LIST_LOADER, null, this);

    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        mCallbacks.onItemSelected(id);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        String selection = null;
        String selectionArgs[] = null;
        switch (arg0) {
            case NOTE_LIST_LOADER:
                if (mSelectedCourseId >= 0) {
                    selection = StudybloxxDBHelper.Contract.Note.COURSE + "=? AND " + StudybloxxDBHelper.Contract.Note.SYNC_STATUS + "=0";
                    selectionArgs = new String[]{Integer.toString(mSelectedCourseId)};
                } else {
                    selection = StudybloxxDBHelper.Contract.Note.SYNC_STATUS + ">= 0";
                }
                Log.d(TAG, "Selection: " + selection);
                return new CursorLoader(mActivity, StudybloxxProvider.NOTE_CONTENT_URI, new String[]{StudybloxxDBHelper.Contract.Note.ID,
                        StudybloxxDBHelper.Contract.Note.TITLE, StudybloxxDBHelper.Contract.Note.URL}, selection, selectionArgs, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
        Log.d(TAG, "Load Finished");
        if (arg1 == null) {
            Log.d(TAG, "Cursor is null");
        } else {
            Log.d(TAG, "Number of Notes: " + arg1.getCount());
        }
        mAdapter.swapCursor(arg1);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        mAdapter.swapCursor(null);
    }


    /**
     * A callback interface that all activities containing this fragment must implement. This mechanism allows activities to be notified of item selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(long noteId);
    }
}
