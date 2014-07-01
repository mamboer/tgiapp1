package com.tencent.sgz.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.tencent.sgz.AppContext;
import com.tencent.sgz.R;
import com.tencent.sgz.bean.User;
import com.tencent.sgz.common.StringUtils;
import com.tencent.sgz.common.UIHelper;
import com.tencent.sgz.entity.Article;
import com.tencent.sgz.entity.UserFavArticleList;
import com.tencent.sgz.entity.UserRemindArticleList;

import java.util.ArrayList;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 * Created by levin on 6/4/14.
 */
@ContentView(R.layout.msg_center)
public class MsgCenter extends BaseActivity {

    @InjectView(R.id.txtNoticeNum) TextView mTxtNoticeNum;

    UserRemindArticleList remindData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    protected void onResume(){
        super.onResume();
        this.initView();
    }

    void initView(){
        //获取活动提醒数据
        reload();
    }

    public void gotoEventCenter(View preView){
        Bundle data = new Bundle();
        data.putSerializable("data",remindData);
        UIHelper.showEventCenter(this,data);
    }

    private void reload(){

        AppContext appContext = AppContext.Instance;

        //更新活动提醒总数
        remindData =appContext.getData().getRemindArticles();
        int itemCnt = remindData.getItems().size();
        int itemCnt1 = 0;
        ArrayList<Article> items = remindData.getItems();

        for(Article item:items){
            if(StringUtils.isLargerThanToday(item.getEvtEndAt())){
                itemCnt1++;
            }
        }

        mTxtNoticeNum.setText(itemCnt1+"");
        if(itemCnt1>0){
            mTxtNoticeNum.setVisibility(View.VISIBLE);
        }else{
            mTxtNoticeNum.setVisibility(View.GONE);
        }


    }
}
