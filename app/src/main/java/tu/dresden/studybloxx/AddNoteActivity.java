package tu.dresden.studybloxx;

import tu.dresden.studybloxx.adapters.CourseCursorAdapter;
import tu.dresden.studybloxx.database.StudybloxxDBHelper;
import tu.dresden.studybloxx.providers.StudybloxxProvider;
import tu.dresden.studybloxx.fragments.AddCourseDialogFragment;
import tu.dresden.studybloxx.services.UploadService;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;


public class AddNoteActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>, android.view.View.OnClickListener,
	AddCourseDialogFragment.AddCourseListener
{

	private static final String TAG = "AddNoteActivity";
	public static final String NOTE_EDIT_ARG = "tu.dresden.studybloxx.NOTE_URL_ARG";
	private static final int NOTE_LOADER = 1200;
	private static final int COURSE_LOADER = 1201;
	private static final int CALENDAR_LOADER = 1202;
	private EditText mNoteTitle;
	private EditText mNoteContent;
	private long mNoteId;
	private Spinner mCourseSpinner;
	private long mNoteCourse;
	private String mTitle;
	private String mContent;
	private ImageButton mAddCourse;
	private FragmentManager mFragmentManager;
	private String mAppointmentName;
	private ContentResolver mResolver;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_note);

		LoaderManager manager = getLoaderManager();
		mFragmentManager = getFragmentManager();
		mResolver = getContentResolver();

		mNoteTitle = (EditText) findViewById(R.id.add_note_title);
		mNoteContent = (EditText) findViewById(R.id.add_note_content);
		mCourseSpinner = (Spinner) findViewById(R.id.note_course_selector);
		mAddCourse = (ImageButton) findViewById(R.id.add_course_button);

		mNoteId = getIntent().getLongExtra(NOTE_EDIT_ARG, 0);
		if (mNoteId != 0)
		{
			manager.initLoader(NOTE_LOADER, null, this);
		}
		else
		{
			mNoteTitle.setEnabled(true);
			mNoteContent.setEnabled(true);
		}

		mCourseSpinner.setAdapter(new CourseCursorAdapter(this, null, 0));

		manager.initLoader(COURSE_LOADER, null, this);
		manager.initLoader(CALENDAR_LOADER, null, this);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_note, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_save_note:
				String title = mNoteTitle.getText().toString();
				String content = mNoteContent.getText().toString();
				if (title.equals("") || content.equals(""))
				{
					new AlertDialog.Builder(this).setTitle(R.string.comfirm_dialog_title).setMessage(R.string.comfirm_dialog_content)
						.setPositiveButton(R.string.confirm_dialog_positive, new OnClickListener()
						{

							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								saveNote();

							}
						}).setNegativeButton(R.string.confirm_dialog_negative, null).show();

				}
				else
				{
					saveNote();
					Intent uploadIntent = new Intent(getApplicationContext(), UploadService.class);
					startService(uploadIntent);
					finish();
				}
		}
		return super.onOptionsItemSelected(item);
	}


	private void saveNote()
	{
		String title = mNoteTitle.getText().toString();
		String content = mNoteContent.getText().toString();
		long courseID = mCourseSpinner.getSelectedItemId();
		if (mNoteId == 0)
		{
			ContentValues values = new ContentValues();
			values.put(StudybloxxDBHelper.Contract.Note.TITLE, title);
			values.put(StudybloxxDBHelper.Contract.Note.CONTENT, content);
			values.put(StudybloxxDBHelper.Contract.Note.COURSE, courseID);
			values.put(StudybloxxDBHelper.Contract.Note.SYNC_STATUS, 1);
			getContentResolver().insert(StudybloxxProvider.NOTE_CONTENT_URI, values);
		}
		else
		{
			ContentValues values = new ContentValues();
			if (!mTitle.equals(title))
			{
				values.put(StudybloxxDBHelper.Contract.Note.TITLE, title);
			}
			if (!mContent.equals(content))
			{
				values.put(StudybloxxDBHelper.Contract.Note.CONTENT, content);
			}
			values.put(StudybloxxDBHelper.Contract.Note.SYNC_STATUS, 2);
			getContentResolver().update(ContentUris.withAppendedId(StudybloxxProvider.NOTE_CONTENT_URI, mNoteId), values, StudybloxxDBHelper.Contract.Note.ID + "=?",
				new String[] { Long.toString(mNoteId) });
		}
	}


	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle data)
	{
		switch (id)
		{
			case NOTE_LOADER:
			{
				return new CursorLoader(this, ContentUris.withAppendedId(StudybloxxProvider.NOTE_CONTENT_URI, mNoteId), new String[] {
					StudybloxxDBHelper.Contract.Note.TITLE, StudybloxxDBHelper.Contract.Note.CONTENT, StudybloxxDBHelper.Contract.Note.COURSE }, null, null, null);
			}
			case COURSE_LOADER:
			{
				return new CursorLoader(this, StudybloxxProvider.COURSE_CONTENT_URI, new String[] { StudybloxxDBHelper.Contract.Course.ID,
					StudybloxxDBHelper.Contract.Course.TITLE }, null, null, null);
			}
			case CALENDAR_LOADER:
			{
				String[] calendarColumns = new String[] { CalendarContract.Events.TITLE };
				String currentTimeMillis = Long.toString(System.currentTimeMillis());

				String calendarQueryString = CalendarContract.Events.DTSTART + " < " + currentTimeMillis + " AND " + CalendarContract.Events.DTEND + " > "
					+ currentTimeMillis;
				Log.d(TAG, calendarQueryString);

				CursorLoader loader = new CursorLoader(AddNoteActivity.this, CalendarContract.Events.CONTENT_URI, calendarColumns, calendarQueryString, null,
					null);

				return loader;
			}
		}
		return null;
	}


	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
	{

		switch (loader.getId())
		{
			case NOTE_LOADER:
			{
				cursor.moveToFirst();
				mTitle = cursor.getString(0);
				mContent = cursor.getString(1);
				mNoteTitle.setText(mTitle);
				mNoteTitle.setEnabled(true);
				mNoteContent.setText(mContent);
				mNoteContent.setEnabled(true);
				mNoteCourse = cursor.getLong(2);
				CourseCursorAdapter adapter = (CourseCursorAdapter) mCourseSpinner.getAdapter();
				Cursor adapterCursor = adapter.getCursor();
				if (adapterCursor != null)
				{
					int position = 0;
					adapterCursor.moveToFirst();
					while (!adapterCursor.isAfterLast())
					{
						if (adapterCursor.getLong(0) == mNoteCourse)
						{
							break;
						}
						position++;
						adapterCursor.moveToNext();
					}
					if (position < adapterCursor.getCount())
					{
						mCourseSpinner.setSelection(position);
					}
				}
				break;
			}
			case COURSE_LOADER:
			{
				CourseCursorAdapter adapter = (CourseCursorAdapter) mCourseSpinner.getAdapter();
				adapter.swapCursor(cursor);
				mAddCourse.setOnClickListener(this);
				break;
			}
			case CALENDAR_LOADER:
			{
				if (cursor != null && cursor.getCount() > 0)
				{
					Log.d(TAG, "Number of Appointments:" + cursor.getCount());
					cursor.moveToFirst();
					mAppointmentName = cursor.getString(0);
					Log.d(TAG, "TITLE: " + mAppointmentName);
				}
				break;
			}
		}
	}


	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{
		switch (loader.getId())
		{
			case NOTE_LOADER:
			{
				break;
			}
			case COURSE_LOADER:
			{
				CourseCursorAdapter adapter = (CourseCursorAdapter) mCourseSpinner.getAdapter();
				adapter.swapCursor(null);
			}
		}
	}


	@Override
	public void onClick(View view)
	{
		switch (view.getId())
		{
			case R.id.add_course_button:
			{
				AddCourseDialogFragment.getInstance(mAppointmentName).show(mFragmentManager, null);
				break;
			}
		}

	}


	@Override
	public void addCourse(String courseName)
	{
		ContentValues values = new ContentValues();
		values.put(StudybloxxDBHelper.Contract.Course.TITLE, courseName);
		values.put(StudybloxxDBHelper.Contract.Course.URL, "");
		mResolver.insert(StudybloxxProvider.COURSE_CONTENT_URI, values);
	}

}
