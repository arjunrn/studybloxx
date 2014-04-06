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

    public static final String UNIFIED_VIEW_NAME = "joindata";

    private static final String CREATE_UNIFIED_VIEW = "CREATE VIEW IF NOT EXISTS " + UNIFIED_VIEW_NAME + " AS SELECT " + NOTE_TABLE_NAME + "." + Contract.Note.ID + " AS " + Contract.Data.NOTE_ID + ", " + NOTE_TABLE_NAME + "." + Contract.Note.TITLE + " AS " + Contract.Data.NOTE_TITLE + " ," + NOTE_TABLE_NAME + "." + Contract.Note.CONTENT + " AS " + Contract.Data.NOTE_CONTENT + " ," + NOTE_TABLE_NAME + "." + Contract.Note.URL + " AS " + Contract.Data.NOTE_URL + ", " + NOTE_TABLE_NAME + "." + Contract.Note.SYNC_STATUS + " AS " + Contract.Data.NOTE_SYNC_STATUS + ", " + COURSE_TABLE_NAME + "." + Contract.Course.ID + " AS " + Contract.Data.COURSE_ID + ", " + COURSE_TABLE_NAME + "." + Contract.Course.URL + " AS " + Contract.Data.COURSE_URL + " FROM " + COURSE_TABLE_NAME + ", " + NOTE_TABLE_NAME + " WHERE " + NOTE_TABLE_NAME + "." + Contract.Note.COURSE + "=" + COURSE_TABLE_NAME + "." + Contract.Course.ID;
    private static final String DROP_UNIFIED_VIEW = "DROP VIEW IF EXISTS " + UNIFIED_VIEW_NAME;

    private static final String DATABASE_NAME = "studybloxx";
    private static final int DATABASE_VERSION = 1;

    private static final String TAG = "StudybloxxDBHelper";

    public StudybloxxDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_COURSES_TABLE);
        db.execSQL(CREATE_NOTES_TABLE);
        db.execSQL(CREATE_UNIFIED_VIEW);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_COURSES_TABLE);
        db.execSQL(DROP_NOTES_TABLE);
        db.execSQL(DROP_UNIFIED_VIEW);
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
            String[] COLUMNS_MODIFIED_COURSE = {ID, TITLE, SYNC_STATUS, URL};
            String[] COLUMNS_DELETED_COURSE = {ID, URL};
        }

        public interface Data {
            public static String NOTE_ID = "_id";
            public static String NOTE_TITLE = "title";
            public static String NOTE_CONTENT = "content";
            public static String NOTE_URL = "url";
            public static String NOTE_SYNC_STATUS = "sync_status";
            public static String COURSE_ID = "course_id";
            public static String COURSE_URL = "course_url";
        }

        public interface Note extends Syncable {
            public static String CONTENT = "content";
            public static String COURSE = "course";

            String[] COLUMNS_NEW_NOTE = {ID, TITLE, CONTENT, COURSE, SYNC_STATUS};
            String[] COLUMNS_MODIFIED_NOTE = {ID, TITLE, CONTENT, URL, SYNC_STATUS};
            String[] COLUMNS_DELETED_NOTE = {ID, URL};
        }
    }


}
