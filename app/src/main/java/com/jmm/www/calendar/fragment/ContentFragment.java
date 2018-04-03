package com.jmm.www.calendar.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jmm.www.calendar.MainActivity;
import com.jmm.www.calendar.R;
import com.jmm.www.calendar.database.AlarmDBSupport;
import com.jmm.www.calendar.models.CalendarEvent;
import com.jmm.www.calendar.pager.AboutMePager;
import com.jmm.www.calendar.pager.BasePager;
import com.jmm.www.calendar.pager.DayPager;
import com.jmm.www.calendar.pager.HomePager;
import com.jmm.www.calendar.pager.WeekPager;
import com.jmm.www.calendar.setting.ClearDataActivity;
import com.jmm.www.calendar.utils.BusProvider;
import com.jmm.www.calendar.utils.CalendarManager;
import com.jmm.www.calendar.utils.Events;
import com.jmm.www.calendar.utils.NetUtil;
import com.jmm.www.calendar.weather.DialogUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * Created by jmm on 2016/3/11.
 */
public class ContentFragment extends BaseFragment {

    @Bind(R.id.vp_content)
    ViewPager vpContent;
    private List<BasePager> mPageList;
    private NavigationView navigationView;//菜单栏
    private DrawerLayout drawerLayout;//DrawerLayout
    private String qqNum = "3132526099";

    private List<CalendarEvent> eventList;
    private AlarmDBSupport support;
    private HomePager homePager;
    private DayPager dayPager;
    private WeekPager weekPager;
    private AboutMePager aboutMePager;



    @Override
    public View initView() {

        View view= View.inflate(mActivity, R.layout.fragment_content,null);

        ButterKnife.bind(this,view);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void initDate() {

        homePager = new HomePager(mActivity);
        dayPager = new DayPager(mActivity);
        weekPager = new WeekPager(mActivity);
        aboutMePager = new AboutMePager(mActivity);

        //主界面添加数据
        mPageList= new ArrayList<>();

        mPageList.add(homePager);
        mPageList.add(dayPager);
        mPageList.add(weekPager);
        mPageList.add(aboutMePager);

        vpContent.setAdapter(new VpContentAdapter());

        //获取侧边栏
        MainActivity mainUi= (MainActivity) mActivity;
        navigationView = mainUi.getNavigationView();
        navigationView.setCheckedItem(R.id.schedule);
        buildHomePager();
        drawerLayout=mainUi.getDrawerLayout();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch(item.getItemId()){
                    case R.id.schedule:
                        vpContent.setCurrentItem(0, false);//设置当前的页面，取消平滑滑动
                        buildHomePager();
                        break;
                    case R.id.day:
                        vpContent.setCurrentItem(1,false);
                        dayPager.initData();
                        break;
                    case R.id.week:
                        vpContent.setCurrentItem(2,false);
                        weekPager.initData();
                        break;
                    case R.id.rl_share:
                        shareText(getActivity(), "欢迎使用【知日历】", "http://android.myapp.com/myapp/detail.htm?apkName=com.jmm.www.calendar");
                        break;
                    case R.id.rl_clean:
                        startActivity(new Intent(getActivity(), ClearDataActivity.class));
                        getActivity().overridePendingTransition(R.anim.push_bottom_in, R.anim.push_bottom_out);
                        break;
                    case R.id.rl_update:
                        if (NetUtil.checkNetState(getActivity())&&NetUtil.isNetworkAvailable(getActivity())) {
                            DialogUtil.showProgressDialog(getActivity(), "正在检查，请稍候...");
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    DialogUtil.closeProgressDialog();
                                    Toast.makeText(getActivity(), "已是最新版本！", Toast.LENGTH_SHORT).show();
                                }
                            }, 1000);
                        } else {
                            Toast.makeText(getActivity(), "当前网络不可用！", Toast.LENGTH_SHORT).show();
                        }

                        break;
                    case R.id.rl_feedback:
                        if (checkApkExist(getActivity(), "com.tencent.mobileqq")) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin=" + qqNum + "&version=1")));
                        } else {
                            Toast.makeText(getActivity(), "本机未安装QQ应用", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.rl_mark:
                        try {
                            String mAddress = "market://details?id=" + getActivity().getPackageName();
                            Intent marketIntent = new Intent("android.intent.action.VIEW");
                            marketIntent.setData(Uri.parse(mAddress));
                            startActivity(marketIntent);
                        }catch (Exception e){
                            Toast.makeText(getActivity(), "未找到应用市场！", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                        break;
                    case R.id.aboutMe:
                        vpContent.setCurrentItem(3,false);
                        aboutMePager.initData();
                        break;
                }
                item.setChecked(true);//点击了设置为选中状态
                drawerLayout.closeDrawers();
                return true;
            }
        });

    }

    public boolean checkApkExist(Context context, String packageName) {
        if (packageName == null || "".equals(packageName))
            return false;
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void shareText(Context context, String title, String url) {
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        //noinspection deprecation
//        share.setAction()
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        share.putExtra(Intent.EXTRA_TEXT,
                title + "\n " + url + "\n 您重要的时刻，我们帮您谨记。");
        context.startActivity(Intent.createChooser(share, "分享到…"));
    }

    /**
     * 主界面设置
     */
    private void buildHomePager(){
        homePager.initData();
        BusProvider.getInstance().toObserverable().subscribe(event ->{
            if(event instanceof Events.GoBackToDay){
                homePager.agenda_view.getAgendaListView().scrollToCurrentDate(CalendarManager.getInstance().getToday());
            }
        });
    }




    /**
     * viewPager数据适配器
     */
    class VpContentAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mPageList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            BasePager pager =mPageList.get(position);
            container.addView(pager.mRootView);
            return pager.mRootView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mPageList.get(position).mRootView);
        }
    }

}
