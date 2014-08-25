package com.tencent.tgiapp1.widget;

import com.tencent.tgiapp1.R;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

/**
 * 加载对话框控件
 * @author lv (http://t.qq.com/badstyle)
 * @version 1.0
 * @created 2014-4-21
 */
public class LoadingDialog extends Dialog {

	private Context mContext;
	private LayoutInflater inflater;
	private LayoutParams lp;
	private TextView loadtext;

	public LoadingDialog(Context context) {
		super(context, R.style.Dialog);
		
		this.mContext = context;
		
		inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.loadingdialog, null);
		loadtext = (TextView) layout.findViewById(R.id.loading_text);
		setContentView(layout);
		
		// 设置window属性
		lp = getWindow().getAttributes();
		lp.gravity = Gravity.CENTER;
        //dimAmount在0.0f和1.0f之间，0.0f完全不暗，即背景是可见的 ，1.0f时候，背景全部变黑暗。
		lp.dimAmount = 0.0f; // 去背景遮盖
		//alpha在0.0f到1.0f之间。1.0完全不透明，0.0f完全透明，自身不可见。
        lp.alpha = 1.0f;
        //lp.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL;
        getWindow().setAttributes(lp);

	}

    /**
     * show the loading dialog with specified text.
     * 尽量避免用模态（不可关闭）对话框，用户体验糟糕！
     * @param txt loading text
     * @param unCancelable whether un-cancelable
     */
    public void showWithText(String txt,boolean unCancelable){
        this.setCancelable(!unCancelable);
        this.setLoadText(txt);
        this.show();
    }

    /**
     * show the loading dialog with specified text
     * @param txt loading text
     */
    public void showWithText(String txt){
        this.showWithText(txt,false);
    }

	public void setLoadText(String content){
		loadtext.setText(content);
	}
}