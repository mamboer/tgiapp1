package in.xsin.common;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;

import com.tencent.tgiapp1.entity.XGNotification;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by levin on 7/31/14.
 */
public class XGMsgService {
    private DBOpenHelper dbOpenHelper;
    private static XGMsgService instance = null;

    private static String TB_FIELDS = "id,msg_id,title,content,activity,notificationActionType,update_time,meta,cntClick";

    public XGMsgService(Context context) {
        this.dbOpenHelper = new DBOpenHelper(context);
    }

    public synchronized static XGMsgService getInstance(Context ctx) {
        if (null == instance) {
            instance = new XGMsgService(ctx);
        }
        return instance;
    }

    public void save(XGNotification notification) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("msg_id", notification.getMsg_id());
        values.put("title", notification.getTitle());
        values.put("content", notification.getContent());
        values.put("activity", notification.getActivity());
        values.put("notificationActionType", notification.getNotificationActionType());
        values.put("update_time", notification.getUpdate_time());
        values.put("meta",notification.getMeta());
        values.put("cntClick",notification.getCntClick());
        db.insert("notification", null, values);
    }

    public void delete(Integer id) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        db.delete("notification", "id=?", new String[] { id.toString() });
    }

    public void delete(Integer id,Handler handler) {
        Message msg = new Message();
        try {
            SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
            db.delete("notification", "id=?", new String[]{id.toString()});
            msg.what = 1;
            msg.obj = "";
        }catch (Exception e){
            msg.what = -1;
            msg.obj = e.getMessage();
            e.printStackTrace();

        }
        handler.sendMessage(msg);

    }

    public void deleteAll() {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        db.delete("notification", "", null);
    }

    public void update(XGNotification notification) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("msg_id", notification.getMsg_id());
        values.put("title", notification.getTitle());
        values.put("content", notification.getContent());
        values.put("activity", notification.getActivity());
        values.put("notificationActionType", notification.getNotificationActionType());
        values.put("update_time", notification.getUpdate_time());
        values.put("meta",notification.getMeta());
        values.put("cntClick",notification.getCntClick());
        db.update("notification", values, "id=?", new String[] { notification
                .getId().toString() });
    }

    public XGNotification find(Integer id) {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        Cursor cursor = db
                .query("notification",
                        new String[] { TB_FIELDS },
                        "id=?", new String[] { id.toString() }, null, null,
                        null, "1");
        try {
            if (cursor.moveToFirst()) {
                return getEntifyFromCursor(cursor);
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    public XGNotification findByMsgId(long msgId) {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        Cursor cursor = db
                .query("notification",
                        new String[] { TB_FIELDS },
                        "msg_id=?", new String[] { msgId+"" }, null, null,
                        null, "1");
        try {
            if (cursor.moveToFirst()) {
                return getEntifyFromCursor(cursor);
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    public void updateCntClick(long msgId,int diffCnt){
        XGNotification notif = null;
        notif = findByMsgId(msgId);
        if(null!=notif){
            notif.setCntClick(notif.getCntClick()+diffCnt);
            update(notif);
        }
    }

    static XGNotification getEntifyFromCursor(Cursor cursor){
        return new XGNotification(
                cursor.getInt(cursor.getColumnIndex("id")),
                cursor.getLong(cursor.getColumnIndex("msg_id")),
                cursor.getString(cursor.getColumnIndex("title")),
                cursor.getString(cursor.getColumnIndex("content")),
                cursor.getString(cursor.getColumnIndex("activity")),
                cursor.getInt(cursor.getColumnIndex("notificationActionType")),
                cursor.getString(cursor.getColumnIndex("update_time")),
                cursor.getString(cursor.getColumnIndex("meta")),
                cursor.getInt(cursor.getColumnIndex("cntClick"))
        );
    }

    public List<XGNotification> getScrollData(int currentPage, int lineSize,
                                              String msg_id) {
        String firstResult = String.valueOf((currentPage - 1) * lineSize);
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            if (msg_id == null || "".equals(msg_id)) {
                cursor = db
                        .query("notification",
                                new String[] { TB_FIELDS },
                                null, null, null, null, "update_time DESC",
                                firstResult + "," + lineSize);
            } else {
                cursor = db
                        .query("notification",
                                new String[] { TB_FIELDS },
                                "msg_id like ?", new String[] { msg_id + "%" },
                                null, null, "update_time DESC", firstResult
                                        + "," + lineSize);
            }
            List<XGNotification> notifications = new ArrayList<XGNotification>();
            while (cursor.moveToNext()) {
                notifications.add(getEntifyFromCursor(cursor));
            }
            return notifications;
        } finally {
            cursor.close();
        }
    }

    public int getCount() {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select count(*) from notification", null);
        try {
            cursor.moveToFirst();
            return cursor.getInt(0);
        } finally {
            cursor.close();
        }
    }
}
