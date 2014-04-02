package tu.dresden.studybloxx.utils;

public class Constants {
    public static final String STUDYBLOXX_USERNAME = "tu.dresden.studybloxx.STUDYBLOXX_USERNAME";
    public static final String STUDYBLOXX_PASSWORD = "tu.dresden.studybloxx.STUDYBLOXX_PASSWORD";
    public static final String NOTE_UPLOAD_URL = "/bloxxdata/add-note/";
    public static final String NOTE_UPDATE_URL = "/bloxxdata/update-note/";
    public static final String COURSE_UPLOAD_URL = "/bloxxdata/add-course/";
    //public static final String STUDYBLOXX_DEFAULT_SERVER_ADDRESS = "http://127.0.0.1:8000";
    public static final String STUDYBLOXX_DEFAULT_SERVER_ADDRESS = "http://10.0.2.2:8000";
    public static final String LOGIN_URL = "%s/login/";
    public static final String COURSE_ROOT_URL = "%s/bloxxdata/api/v1/course/";


    public interface JSON {
        public interface Course {
            public String TITLE = "course_name";
            public String URI = "resource_uri";
        }

        public class Note {
            public static final String CONTENT = "text";
            public static final String COURSE = "course";
            public static final String TITLE="title";
            public static final String URI = "resource_uri";
        }
    }
}
