package com.jmm.www.calendar.pager;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;

import com.jmm.www.calendar.R;
import com.jmm.www.calendar.utils.BusProvider;
import com.jmm.www.calendar.utils.Events;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.xiaomi.ad.internal.common.h.R;

/**
 * Created by jmm on 2016/5/14.
 */
public class AboutMePager extends BasePager implements View.OnClickListener {

    @Bind(R.id.about_bg)
    LinearLayout about_bg;
//    @Bind(R.id.github)
//    TextView github;
//    @Bind(R.id.weibo)
//    TextView weibo;

    public AboutMePager(Activity mActivity) {
        super(mActivity);
    }

    @Override
    public View initView() {
        View view = View.inflate(mActivity, R.layout.aboutme_pager, null);

        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void initData() {
        about_bg.setOnClickListener(v -> BusProvider.getInstance().send(new Events.AgendaListViewTouchedEvent()));

//        github.setOnClickListener(this);
//        weibo.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
//            case R.id.github:
//                Intent it1 = new Intent(Intent.ACTION_VIEW, Uri.parse(github.getText().toString()));
//                it1.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
//                mActivity.startActivity(it1);
//                break;
//            case R.id.weibo:
//                Intent it2 = new Intent(Intent.ACTION_VIEW, Uri.parse(weibo.getText().toString()));
//                it2.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
//                mActivity.startActivity(it2);
//                break;
        }
    }
}
