package tu.dresden.studybloxx.fragments;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import tu.dresden.studybloxx.AddNoteActivity;
import tu.dresden.studybloxx.NoteDetailActivity;
import tu.dresden.studybloxx.NoteListActivity;
import tu.dresden.studybloxx.R;
import tu.dresden.studybloxx.database.StudybloxxDBHelper;
import tu.dresden.studybloxx.providers.StudybloxxProvider;


/**
 * A fragment representing a single Note detail screen. This fragment is either contained in a {@link NoteListActivity} in two-pane mode (on tablets) or a
 * {@link NoteDetailActivity} on handsets.
 */
public class NoteDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * The fragment argument representing the item ID that this fragment represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    private static final String TAG = "NoteDetailFragment";
    private static final int NOTE_DETAIL_LOADER = 12032;
    private TextView mDetailView;
    private TextView mTitleView;

    private long mNoteId;

    private ContentResolver mResolver;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation changes).
     */
    public NoteDetailFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mNoteId = getArguments().getLong(ARG_ITEM_ID);
        }
        setHasOptionsMenu(true);
        mResolver = getActivity().getContentResolver();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_note_detail, container, false);

        mTitleView = (TextView) rootView.findViewById(R.id.note_title);
        mDetailView = (TextView) rootView.findViewById(R.id.note_detail);

        getLoaderManager().initLoader(NOTE_DETAIL_LOADER, null, this);

        return rootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.note_detail_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_note_action: {
                Intent editNoteIntent = new Intent(getActivity(), AddNoteActivity.class);
                Log.d(TAG, "Note ID into Activity: " + mNoteId);
                editNoteIntent.putExtra(AddNoteActivity.NOTE_EDIT_ARG, mNoteId);
                startActivity(editNoteIntent);
                return true;
            }
            case R.id.discard_note_action: {
                ContentValues values = new ContentValues(1);
                values.put(StudybloxxDBHelper.Contract.Note.SYNC_STATUS, StudybloxxDBHelper.Contract.SyncStatus.CLIENT_DELETED);
                Uri noteUri = ContentUris.withAppendedId(StudybloxxProvider.NOTE_CONTENT_URI, mNoteId);
                mResolver.update(noteUri, values, null, null);
                getActivity().finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle data) {
        switch (loaderId) {
            case NOTE_DETAIL_LOADER: {
                return new CursorLoader(getActivity(), ContentUris.withAppendedId(StudybloxxProvider.NOTE_CONTENT_URI, mNoteId), new String[]{
                        StudybloxxDBHelper.Contract.Note.TITLE, StudybloxxDBHelper.Contract.Note.CONTENT}, null, null, null);
            }
        }
        return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case NOTE_DETAIL_LOADER: {
                cursor.moveToFirst();
                mDetailView.setText(cursor.getString(1));
                String title = cursor.getString(0);
                mTitleView.setText(title);
                getActivity().getActionBar().setTitle(title);
                break;
            }
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // TODO Auto-generated method stub

    }

}
