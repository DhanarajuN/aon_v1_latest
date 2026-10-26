package pdf.app.library.utils;

import android.os.Environment;

/**
 * Created by frantic on 2/8/18.
 */

public class Constants {

    public static final String EXTENSIONADDER = ".pdf";
    public static final String PDF_FILE_PATH = Environment.getExternalStorageDirectory().toString() + "/PDF/";
    public static final long SPLASH_DISPLAY_LENGTH = 2000;
    public static final String FILEPATH = PDF_FILE_PATH + "sample.pdf";
    public static final String PREF_USERNAME = "pref_username";
    public static final String PREF_LOGGED_IN_EMAIL = "PREF_LOGGED_IN_EMAIL";
    public static  final String PREF_QUOTE_NO="0";
    public static final String PREF_PASSWORD = "pref_password";
    public static final String PREF_LOGGED_IN = "PREF_LOGGED_IN";

    public static final String PREF_USERID = "PREF_USERID";
    public static final String PREF_FULL_NAME = "PREF_FULL_NAME";
    public static final String PREF_MOBILE = "PREF_MOBILE";
    public static final String PREF_IMEI = "PREF_IMEI";
    public static final String PREF_ZONE = "PREF_ZONE";
    public static final String PREF_ZONES = "PREF_ZONES";
    public static final String PREF_RMID = "PREF_RMID";

    public static final String PREF_TAX="PREF_TAX";
    public static final String PREF_USER_STATUS="PREF_USER_STATUS";
    public static final String USER_PASSWORD_EXPIRED="USER_PASSWORD_EXPIRED";

    public static final String PREF_ONLINE_TIME = "PREF_ONLINE_TIME";
    public static final long OFF_TIME_IN_MINS = 4440;
    public static final String PREF_TOKEN="PREF_TOKEN";
}
