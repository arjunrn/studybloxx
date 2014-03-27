package tu.dresden.studybloxx.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class StudybloxxDBHelper extends SQLiteOpenHelper {
    public static final String NOTE_TABLE_NAME = "notes";

    private static final String DROP_NOTES_TABLE = "DROP TABLE IF EXISTS " + NOTE_TABLE_NAME;

    public static final String COURSE_TABLE_NAME = "courses";

    private static final String CREATE_COURSES_TABLE = "CREATE TABLE " + COURSE_TABLE_NAME + "(" + Contract.Course.ID + " INTEGER PRIMARY KEY, " + Contract.Course.TITLE
            + " TEXT NOT NULL, " + Contract.Course.URL + " TEXT NOT NULL, " + Contract.Course.SYNC_STATUS + " INTEGER DEFAULT " + Contract.SyncStatus.CREATED + ")";

    private static final String DROP_COURSES_TABLE = "DROP TABLE IF EXISTS " + COURSE_TABLE_NAME;

    private static final String CREATE_NOTES_TABLE = "CREATE TABLE " + NOTE_TABLE_NAME + "(" + Contract.Note.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Contract.Note.TITLE
            + " TEXT NOT NULL, " + Contract.Note.CONTENT + " TEXT, " + Contract.Note.URL + " TEXT, " + Contract.Note.COURSE + " INTEGER NOT NULL, " + Contract.Note.CREATED + " INTEGER, "
            + Contract.Note.UPDATED + " INTEGER," + Contract.Note.SYNC_STATUS + " INTEGER DEFAULT 0,FOREIGN KEY(" + Contract.Note.COURSE + ") REFERENCES " + COURSE_TABLE_NAME + "(" + Contract.Course.ID + "))";

    private static final String DATABASE_NAME = "studybloxx";
    private static final int DATABASE_VERSION = 1;

    private static final String TAG = "StudybloxxDBHelper";

    public StudybloxxDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, CREATE_COURSES_TABLE);
        db.execSQL(CREATE_COURSES_TABLE);
        db.execSQL(CREATE_NOTES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_COURSES_TABLE);
        db.execSQL(DROP_NOTES_TABLE);
        onCreate(db);
    }


    public interface Contract {

        public interface SyncStatus {
            public int SYNCED = 0;
            public int MODIFIED = 1;
            public int SERVER_DELETED = 2;
            public int CREATED = 3;
            public int CLIENT_DELETED = -1;
        }

        public interface Syncable {
            public static String ID = "_id";
            public static String TITLE = "title";
            public static String SYNC_STATUS = "sync_status";
            public static String CREATED = "created";
            public static String UPDATED = "updated";
            public static String SERVER_ID = "server_id";
            public static String URL = "url";
        }

        public interface Course extends Syncable {
            public static String[] COLUMNS_NEW_COURSE = {ID, TITLE, SYNC_STATUS};
        }

        public interface Note extends Syncable {
            public static String CONTENT = "content";
            public static String COURSE = "course";
        }
    }


}
