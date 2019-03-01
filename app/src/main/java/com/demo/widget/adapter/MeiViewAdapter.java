package com.demo.widget.adapter;

import android.content.Intent;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.demo.widget.R;
import com.demo.widget.bean.IntentBean;

import java.util.ArrayList;

/**
 * Created by wenshi on 2019/3/1.
 * Description
 */
public class MeiViewAdapter extends BaseQuickAdapter<IntentBean, BaseViewHolder> {

    public MeiViewAdapter() {
        super(R.layout.item_mei_view, new ArrayList<IntentBean>());
    }

    @Override
    protected void convert(BaseViewHolder helper, final IntentBean item) {
        helper.getView(R.id.cv_sv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.targetClass == null) {
                    return;
                }
                v.getContext().startActivity(new Intent(v.getContext(), item.targetClass));
            }
        });
        helper.setText(R.id.tv_name, item.name);
    }
}
