package me.faso.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

import com.tencent.sgz.R;

public class PullToRefreshListView extends PullToRefreshBase<ListView> {

	public PullToRefreshListView(Context paramContext) {
		super(paramContext);
	}

	public PullToRefreshListView(Context paramContext,
			AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
	}

	protected final ListView createAdapterView(Context paramContext,
			AttributeSet paramAttributeSet) {
		ListView listV = new ListView(paramContext, paramAttributeSet);
		listV.setId(R.id.pull_adapter_view);
		return listV;
	}
}
