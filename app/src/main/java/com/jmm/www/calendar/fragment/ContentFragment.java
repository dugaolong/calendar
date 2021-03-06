package com.jmm.www.calendar.fragment;

import android.support.design.widget.NavigationView;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.jmm.www.calendar.MainActivity;
import com.jmm.www.calendar.R;
import com.jmm.www.calendar.database.AlarmDBSupport;
import com.jmm.www.calendar.models.CalendarEvent;
import com.jmm.www.calendar.pager.AboutMePager;
import com.jmm.www.calendar.pager.BasePager;
import com.jmm.www.calendar.pager.DayPager;
import com.jmm.www.calendar.pager.HomePager;
import com.jmm.www.calendar.pager.WeekPager;
import com.jmm.www.calendar.utils.BusProvider;
import com.jmm.www.calendar.utils.CalendarManager;
import com.jmm.www.calendar.utils.Events;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by acer-pc on 2016/3/11.
 */
public class ContentFragment extends BaseFragment {

    @Bind(R.id.vp_content)
    ViewPager vpContent;
    private List<BasePager> mPageList;
    private NavigationView navigationView;//菜单栏
    private DrawerLayout drawerLayout;//DrawerLayout


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
