
package tu.dresden.studybloxx.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import tu.dresden.studybloxx.models.Course;
import tu.dresden.studybloxx.models.Note;

import java.util.Date;

public class StudybloxxData {

    private static final String TAG = "StudybloxxData";
    private StudybloxxDBHelper mHelper;
    private SQLiteDatabase mDB;

    public StudybloxxData(Context context) {
        mHelper = new StudybloxxDBHelper(context);
    }

    public void open() {
        mDB = mHelper.getWritableDatabase();
    }

    public void close() {
        mDB.close();
    }

    public boolean insertUpdateCourse(Course course) {
        ContentValues values = new ContentValues();
        values.put(StudybloxxDBHelper.COURSE_ID, course.getId());
        values.put(StudybloxxDBHelper.COURSE_TITLE, course.getTitle());
        values.put(StudybloxxDBHelper.COURSE_URL, course.getUrl());
        long newId = mDB.insertWithOnConflict(StudybloxxDBHelper.COURSE_TABLE_NAME, null, values,
                SQLiteDatabase.CONFLICT_REPLACE);
        if (newId > 0) {
            return true;
        } else {
            return false;
        }
    }

    public Course[] getUserCourses() {
        Cursor courseCursor = mDB.query(StudybloxxDBHelper.COURSE_TABLE_NAME, new String[] {
                StudybloxxDBHelper.COURSE_ID, StudybloxxDBHelper.COURSE_TITLE,
                StudybloxxDBHelper.COURSE_URL
        }, null, null, null, null, null);

        int courseCount = courseCursor.getCount();
        Course[] allCourses = new Course[courseCount];
        Log.d(TAG, "Number of user courses: " + courseCount);

        courseCursor.moveToFirst();
        int counter = 0;
        while (!courseCursor.isAfterLast()) {
            allCourses[counter++] = new Course(courseCursor.getInt(0), courseCursor.getString(1),
                    courseCursor.getString(2));
            courseCursor.moveToNext();
        }
        return allCourses;
    }

    public boolean insertUpdateNote(Note note) {
        ContentValues values = new ContentValues();
        values.put(StudybloxxDBHelper.NOTE_ID, note.getNoteId());
        values.put(StudybloxxDBHelper.NOTE_TITLE, note.getNoteTitle());
        values.put(StudybloxxDBHelper.NOTE_CONTENT, note.getText());
        values.put(StudybloxxDBHelper.NOTE_URL, note.getUrl());
        Date createdDate = note.getCreatedDate();
        if (createdDate != null) {
            values.put(StudybloxxDBHelper.NOTE_CREATED, createdDate.getTime());
        }

        Date lastUpdatedDate = note.getLastUpdatedDate();
        if (lastUpdatedDate != null) {
            values.put(StudybloxxDBHelper.NOTE_UPDATED, lastUpdatedDate.getTime());
        }

        values.put(StudybloxxDBHelper.NOTE_COURSE, note.getNoteCourse());
        long newId = mDB.insertWithOnConflict(StudybloxxDBHelper.NOTE_TABLE_NAME, null, values,
                SQLiteDatabase.CONFLICT_REPLACE);
        if (newId > 0) {
            return true;
        }
        else {
            return false;
        }
    }

    public Note[] getAllNotes() {
        Cursor cursor = mDB.query(StudybloxxDBHelper.NOTE_TABLE_NAME, new String[] {
                StudybloxxDBHelper.NOTE_ID, StudybloxxDBHelper.NOTE_TITLE,
                StudybloxxDBHelper.NOTE_CONTENT, StudybloxxDBHelper.NOTE_CREATED,
                StudybloxxDBHelper.NOTE_UPDATED, StudybloxxDBHelper.NOTE_URL,
                StudybloxxDBHelper.NOTE_COURSE
        }, null, null, null, null, null);

        int count = cursor.getCount();
        Note[] userNotes = new Note[count];

        cursor.moveToFirst();
        int counter = 0;
        while (!cursor.isAfterLast()) {
            Note n = new Note(cursor.getLong(0), new Date(cursor.getLong(3)), new Date(
                    cursor.getLong(4)), cursor.getString(1), cursor.getString(2),
                    cursor.getString(5), cursor.getLong(6));
            userNotes[counter++] = n;
            cursor.moveToNext();
        }
        return userNotes;
    }

    public void dropAllNotes() {
        int delCount = mDB.delete(StudybloxxDBHelper.NOTE_TABLE_NAME, null, null);
        Log.d(TAG, "Note Delete Count: " + delCount);
    }

}
