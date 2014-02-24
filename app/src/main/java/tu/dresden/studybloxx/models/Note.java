
package tu.dresden.studybloxx.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.Date;

public class Note implements Parcelable {
    protected static final String TAG = "Note";
    private long mId = 0;
    private Date mCreatedDate;
    private Date mUpdatedDate;
    private String mTitle;
    private String mText;
    private String mUrl;
    private long mCourse;

    public Note(long id, Date created, Date updated, String title, String text, String url,
            long course) {
        mId = id;
        mCreatedDate = created;
        mUpdatedDate = updated;
        mTitle = title;
        mCourse = course;
        mText = text;
        mUrl = url;
    }

    public long getNoteId() {
        return mId;
    }

    public Date getNoteDate() {
        return mCreatedDate;
    }

    public Date getLastUpdatedDate() {
        return mUpdatedDate;
    }

    public String getNoteTitle() {
        return mTitle;
    }

    public long getNoteCourse() {
        return mCourse;
    }

    public String getText() {
        return mText;
    }

    public String getUrl() {
        return mUrl;
    }

    public Date getCreatedDate() {
        return mCreatedDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        Log.d(TAG, "writeToParcel()");
        parcel.writeLong(mId);
        parcel.writeLong(mCreatedDate.getTime());
        parcel.writeLong(mUpdatedDate.getTime());
        Log.d(TAG, "writeToParcel: " + mTitle);
        parcel.writeString(mTitle);
        parcel.writeLong(mCourse);
        parcel.writeString(mText);
        Log.d(TAG, "writeToParcel: " + mText);
        parcel.writeString(mUrl);
    }

    public static final Parcelable.Creator<Note> CREATOR = new Creator<Note>() {

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }

        @Override
        public Note createFromParcel(Parcel source) {
            Log.d(TAG, "createFromParcel() called");
            long id = source.readLong();
            Date created = new Date(source.readLong());
            Date updated = new Date(source.readLong());
            String title = source.readString();
            long course = source.readLong();
            String text = source.readString();
            String url = source.readString();
            Log.d(TAG, "createFromParcel: " + text);
            Log.d(TAG, "createFromParcel: " + title);
            return new Note(id, created, updated, title, text, url, course);
        }
    };

}
