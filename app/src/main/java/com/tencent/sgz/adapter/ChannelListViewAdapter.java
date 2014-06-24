package com.tencent.sgz.adapter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.sgz.AppContext;
import com.tencent.sgz.AppDataProvider;
import com.tencent.sgz.R;
import com.tencent.sgz.bean.News;
import com.tencent.sgz.common.UIHelper;
import com.tencent.sgz.entity.AppData;
import com.tencent.sgz.entity.Article;
import com.tencent.sgz.entity.ArticleList;
import com.tencent.sgz.entity.ChannelGroup;
import com.tencent.sgz.entity.ChannelItem;

import java.util.ArrayList;
import java.util.HashMap;

import in.xsin.widget.GridViewForScrollView;

/**
 * 
 * @author Administrator
 * 
 */
public class ChannelListViewAdapter extends BaseExpandableListAdapter implements
		OnItemClickListener
{
	public static final int ItemHeight = 48;//
	public static final int PaddingLeft = 36;//
	private int myPaddingLeft = 0;

	private GridViewForScrollView toolbarGrid;

	private Context parentContext;

	private LayoutInflater layoutInflater;

    private ArrayList<ChannelGroup> treeNodes = new ArrayList<ChannelGroup>();


	public ChannelListViewAdapter(Context view, int myPaddingLeft)
	{
		this(view,myPaddingLeft,new ArrayList<ChannelGroup>());
	}

    public ChannelListViewAdapter(Context view, int myPaddingLeft, ArrayList<ChannelGroup> groups){
        parentContext = view;
        this.myPaddingLeft = myPaddingLeft;
        this.treeNodes = groups;
    }

	public ArrayList<ChannelGroup> GetNodes()
	{
		return treeNodes;
	}

    public void UpdateTreeNode(ArrayList<ChannelGroup> groups){
        this.treeNodes = groups;
    }

	public void RemoveAll()
	{
		treeNodes.clear();
	}

	public Object getChild(int groupPosition, int childPosition)
	{
		return treeNodes.get(groupPosition).getItems().get(childPosition);
	}

	public int getChildrenCount(int groupPosition)
	{
		return 1;//这里是一维数据，即每个组只有一个items数组
	}

	static public TextView getTextView(Context context)
	{
		AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ItemHeight);

		TextView textView = new TextView(context);
		textView.setLayoutParams(lp);
		textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
		return textView;
	}

	/**
	 * ExpandableListView
	 */
    @Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			layoutInflater = (LayoutInflater) parentContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.channellist_grid, null);

			toolbarGrid = (GridViewForScrollView) convertView
					.findViewById(R.id.gvChannellist);
			toolbarGrid.setNumColumns(4);// 列数
			toolbarGrid.setGravity(Gravity.CENTER);// 居中
			toolbarGrid.setHorizontalSpacing(10);// 水平空间
            toolbarGrid.setVerticalSpacing(10);//垂直空间
			toolbarGrid.setAdapter(getMenuAdapter(getGroup(groupPosition)));// Adapter
			toolbarGrid.setOnItemClickListener(this);

		}

		return convertView;
	}

	/**
	 *
	 */
    @Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent)
	{

		TextView textView = getTextView(this.parentContext);
		textView.setText(getGroup(groupPosition).getGroup());
		textView.setPadding(myPaddingLeft + PaddingLeft, 0, 0, 0);

        //覆盖expandablelistview标题默认滴点击展开行为
        textView.setClickable(true);

		return textView;
	}

	public long getChildId(int groupPosition, int childPosition)
	{
		return childPosition;
	}

	public ChannelGroup getGroup(int groupPosition)
	{
		return treeNodes.get(groupPosition);
	}

	public int getGroupCount()
	{
		return treeNodes.size();
	}

	public long getGroupId(int groupPosition)
	{
		return groupPosition;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition)
	{
		return true;
	}

	public boolean hasStableIds()
	{
		return true;
	}

	/**
	 * SimpleAdapter
	 * 
	 * @param item
	 * @return SimpleAdapter
	 */
	private SimpleAdapter getMenuAdapter(ChannelGroup item)
	{
		ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
        final ArrayList<ChannelItem> items = item.getItems();
		for (int i = 0; i < items.size(); i++)
		{
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("itemImage", items.get(i).getIcon());
			map.put("itemText", items.get(i).getName());
            map.put("itemAction",items.get(i).getAction());
            map.put("itemMD5",items.get(i).getMD5());
			data.add(map);
		}
		SimpleAdapter simperAdapter = new SimpleAdapter(parentContext, data,
				R.layout.channellist_item, new String[] { "itemImage", "itemText" },
				new int[] { R.id.channellist_itemicon, R.id.channellist_itemtext }){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                View view = super.getView(position,convertView,parent);
                view.setTag(items.get(position));
                return view;
            }
        };

        simperAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Object data,
                                        String textRepresentation) {
                if (view instanceof ImageView) {
                    ImageView iv = (ImageView) view;
                    UIHelper.showLoadImage(iv, data.toString(), "Error loading image:" + data);
                    return true;
                }
                return false;
            }
        });



        return simperAdapter;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id)
	{

        ChannelItem item =(ChannelItem)view.getTag();

        if(null==item){
            return;
        }

        String url = item.getAction();
        if(url.indexOf("app://")>-1){
            return;
        }

        News news = new News();
        news.setTitle(item.getName());
        news.setFace(item.getIcon());
        news.setUrl(AppDataProvider.assertUrl(AppContext.Instance,item.getAction()));
        UIHelper.showNewsDetailByInstance(parentContext,news);

        final Handler onDataGot = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                Bundle data = msg.getData();
                int errCode = data.getInt("errCode");
                String errMsg = data.getString("errMsg");

                if(errMsg!=null){
                    UIHelper.ToastMessage(parentContext,errMsg);
                    return;
                }

                ChannelGroup item = (ChannelGroup)data.getSerializable("data");

                if(item!=null&&item.getItems().size()>0){
                    treeNodes.remove(0);
                    treeNodes.add(0,item);
                    notifyDataSetChanged();
                }

            }
        };

        AppDataProvider.addFavChannelItem(AppContext.Instance,item,onDataGot);

	}
}