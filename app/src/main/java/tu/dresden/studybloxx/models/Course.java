package tu.dresden.studybloxx.models;

public class Course {
    int mId;
    String mTitle;
    String mUrl;


    public Course(int id, String title, String url) {
        mId = id;
        mTitle = title;
        mUrl = url;
    }


    public int getId() {
        return mId;
    }


    public String getTitle() {
        return mTitle;
    }


    public String getUrl() {
        return mUrl;
    }
}
