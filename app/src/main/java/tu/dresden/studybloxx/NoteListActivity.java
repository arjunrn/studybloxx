package tu.dresden.studybloxx;

import tu.dresden.studybloxx.fragments.NoteDetailFragment;
import tu.dresden.studybloxx.fragments.NoteListFragment;
import tu.dresden.studybloxx.services.SyncService;
import tu.dresden.studybloxx.utils.Helper;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;


/**
 * An activity representing a list of Note. This activity has different presentations for handset and tablet-size devices. On handsets, the activity presents a
 * list of items, which when touched, lead to a {@link NoteDetailActivity} representing item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a {@link NoteListFragment} and the item details (if present) is a {@link NoteDetailFragment}.
 * <p>
 * This activity also implements the required {@link NoteListFragment.Callbacks} interface to listen for item selections.
 */
public class NoteListActivity extends NavDrawerActivity implements NoteListFragment.Callbacks
{

	private static final String TAG = "NoteListActivity";
	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
	 */
	private boolean mTwoPane;
	private ActionBar mActionBar;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note_list);
		setupNavigationDrawer();
		if (findViewById(R.id.note_detail_container) != null)
		{
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((NoteListFragment) getSupportFragmentManager().findFragmentById(R.id.lecture_list)).setActivateOnItemClick(true);
		}

		mActionBar = getActionBar();
		NoteListFragment noteListFrag;
		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.containsKey(NoteListFragment.COURSE_ID))
		{

			int courseId = extras.getInt(NoteListFragment.COURSE_ID);
			Log.d(TAG, "Selected Course ID: " + courseId);
			noteListFrag = NoteListFragment.getInstance(courseId);
		}
		else
		{
			Log.d(TAG, "No Course ID present");
			noteListFrag = new NoteListFragment();
		}

		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.note_list_container, noteListFrag).commit();

	}


	/**
	 * Callback method from {@link NoteListFragment.Callbacks} indicating that the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(long noteId)
	{
		if (mTwoPane)
		{
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putLong(NoteDetailFragment.ARG_ITEM_ID, noteId);
			NoteDetailFragment fragment = new NoteDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction().replace(R.id.note_detail_container, fragment).commit();

		}
		else
		{
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, NoteDetailActivity.class);
			detailIntent.putExtra(NoteDetailFragment.ARG_ITEM_ID, noteId);
			startActivity(detailIntent);
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.notes_list, menu);
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_refresh_note:
			{
				Intent serviceIntent = new Intent(this, SyncService.class);
				startService(serviceIntent);
				Toast.makeText(this, "Syncing notes", Toast.LENGTH_SHORT).show();
				return true;
			}
			case R.id.action_quick_add_note:
			{
				Intent addNoteIntent = new Intent(this, AddNoteActivity.class);
				startActivity(addNoteIntent);
				return true;
			}
			case R.id.action_logout:
			{
				Helper.logout(this);
				finish();
				return true;
			}
		}

		return super.onOptionsItemSelected(item);
	}
}
