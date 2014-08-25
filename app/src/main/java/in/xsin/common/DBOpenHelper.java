package in.xsin.common;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tencent.tgiapp1.R;

public class DBOpenHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION  = 2;

    public DBOpenHelper(Context context) {
        super(context, context.getString(R.string.xg_db), null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("" +
                "CREATE TABLE notification (" +
                "id integer primary key autoincrement," +
                "msg_id varchar(64)," +
                "title varchar(128)," +
                "activity varchar(256)," +
                "notificationActionType varchar(512)," +
                "content text," +
                "update_time varchar(16)," +
                "meta text,"+
                "cntClick integer)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //drop old tables if exists
        db.execSQL("DROP TABLE IF EXISTS notification");
        // re-create tables
        onCreate(db);
    }

}
