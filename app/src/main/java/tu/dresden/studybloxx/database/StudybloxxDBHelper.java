package tu.dresden.studybloxx.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class StudybloxxDBHelper extends SQLiteOpenHelper
{

	public static final String NOTE_TABLE_NAME = "notes";
	public static final String NOTE_ID = "_id";
	public static final String NOTE_TITLE = "title";
	public static final String NOTE_CONTENT = "content";
	public static final String NOTE_URL = "url";
	public static final String NOTE_CREATED = "created_timestamp";
	public static final String NOTE_UPDATED = "updated_timestamp";
	public static final String NOTE_COURSE = "course";
	public static final String NOTE_SYNCED = "synced";
	public static final String NOTE_UNSYNCED = "note_unsynced";
	public static final String NOTE_DELETED = "deleted";

	public static final String COURSE_TITLE = "title";
	public static final String COURSE_URL = "url";
	public static final String COURSE_ID = NOTE_ID;
	public static final String COURSE_TABLE_NAME = "courses";
	public static final String COURSE_SYNCED = "deleted";

	private static final String CREATE_COURSES_TABLE = "CREATE TABLE " + COURSE_TABLE_NAME + "(" + COURSE_ID + " INTEGER PRIMARY KEY, " + COURSE_TITLE
		+ " TEXT NOT NULL, " + COURSE_URL + " TEXT NOT NULL, " + COURSE_SYNCED + " INTEGER DEFAULT 0)";
	private static final String CREATE_NOTES_TABLE = "CREATE TABLE " + NOTE_TABLE_NAME + "(" + NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NOTE_TITLE
		+ " TEXT NOT NULL, " + NOTE_CONTENT + " TEXT, " + NOTE_URL + " TEXT, " + NOTE_COURSE + " INTEGER NOT NULL, " + NOTE_CREATED + " INTEGER, "
		+ NOTE_UPDATED + " INTEGER," + NOTE_SYNCED + " INTEGER DEFAULT 0, " + NOTE_UNSYNCED + " INTEGER DEFAULT 0," + NOTE_DELETED
		+ " INTEGER DEFAULT 0,FOREIGN KEY(" + NOTE_COURSE + ") REFERENCES " + COURSE_TABLE_NAME + "(" + COURSE_ID + "))";

	private static final String DATABASE_NAME = "studybloxx";
	private static final int DATABASE_VERSION = 1;
	private static final String DROP_COURSES_TABLE = "DROP TABLE IF EXISTS " + COURSE_TABLE_NAME;
	private static final String DROP_NOTES_TABLE = "DROP TABLE IF EXISTS " + NOTE_TABLE_NAME;

	private static final String TAG = "StudybloxxDBHelper";


	public StudybloxxDBHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}


	@Override
	public void onCreate(SQLiteDatabase db)
	{
		Log.d(TAG, CREATE_COURSES_TABLE);
		db.execSQL(CREATE_COURSES_TABLE);
		db.execSQL(CREATE_NOTES_TABLE);
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL(DROP_COURSES_TABLE);
		db.execSQL(DROP_NOTES_TABLE);
		onCreate(db);
	}

}
