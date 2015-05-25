package com.kostya.cranescale;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import java.util.ArrayList;

//import static com.victjava.scales.ActivityCheck.WeightType.*;

//import static com.victjava.scales.ActivityCheck.*;

/*
 * Created by Kostya on 09.03.2015.
 */

public class OpenDocFragment extends Fragment {

    private TextView viewFirst;
    private TabHost mTabHost;
    private TabsAdapter mTabsAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_open_doc, container, false);
        mTabHost = (TabHost) v.findViewById(android.R.id.tabhost);
        mTabHost.setup();
        ViewPager mViewPager = (ViewPager) v.findViewById(R.id.pager);
        mTabsAdapter = new TabsAdapter(getActivity(), mTabHost, mViewPager);
        mTabsAdapter.addTab(mTabHost.newTabSpec("new_doc").setIndicator(createTabView(getActivity(), "документ")), NewDocFragment.class);
        mTabsAdapter.addTab(mTabHost.newTabSpec("list_check").setIndicator(createTabView(getActivity(), "взвешивания")), ListCheckFragment.class);
        mTabHost.setCurrentTab(0);

        if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        }
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("tab", mTabHost.getCurrentTabTag()); //save the tab selected
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private class TabsAdapter extends FragmentPagerAdapter implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {

        private final FragmentManager fragmentManager;
        private Fragment mCurrentFragment;
        private final Context mContext;
        private final TabHost mTabHost;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<>();

        private class TabInfo {
            private final String tag;
            private final Class<?> mClass;
            private final Bundle args;

            TabInfo(final String _tag, final Class<?> _class, final Bundle _args) {
                tag = _tag;
                mClass = _class;
                args = _args;
            }

            public Class<?> getMClass() {
                return mClass;
            }

            public Bundle getArgs() {
                return args;
            }
        }

        private class DummyTabFactory implements TabHost.TabContentFactory {
            private final Context mContext;

            public DummyTabFactory(final Context context) {
                mContext = context;
            }

            @Override
            public View createTabContent(final String tag) {
                View v = new View(mContext);
                v.setMinimumWidth(0);
                v.setMinimumHeight(0);
                return v;
            }
        }

        public TabsAdapter(final FragmentActivity activity, final TabHost tabHost, final ViewPager pager) {
            super(getChildFragmentManager());

            fragmentManager = getChildFragmentManager();
            mContext = activity;
            mTabHost = tabHost;
            mViewPager = pager;
            mTabHost.setOnTabChangedListener(this);
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
        }

        public void addTab(final TabHost.TabSpec tabSpec, final Class<?> _class) {

            tabSpec.setContent(new DummyTabFactory(mContext));
            String tag = tabSpec.getTag();
            TabInfo info = new TabInfo(tag, _class, null);

            mTabs.add(info);
            mTabHost.addTab(tabSpec);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Fragment getItem(final int position) {
            TabInfo info = mTabs.get(position);
            return Fragment.instantiate(mContext, info.getMClass().getName(), info.getArgs());
        }

        @Override
        public void onTabChanged(final String tabId) {
            int position = mTabHost.getCurrentTab();
            mViewPager.setCurrentItem(position);
            //Fragment fragment = getItem(position);
            //((OnCheckEventListener)fragment).someEvent(WeightType.NONE);
            //((OnCheckEventListener) getItem(position)).someEvent(WeightType.NONE);
            //((OnCheckEventListener) fragmentManager.getFragments().get(position)).someEvent(WeightType.NONE);
        }

        @Override
        public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(final int position) {

            /*Unfortunately when TabHost changes the current tab, it kindly
            also takes care of putting focus on it when not in touch mode.
            The jerk.
            This hack tries to prevent this from pulling focus out of our
            ViewPager.*/
            TabWidget widget = mTabHost.getTabWidget();
            int oldFocusability = widget.getDescendantFocusability();
            widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            mTabHost.setCurrentTab(position);
            widget.setDescendantFocusability(oldFocusability);
            //List<Fragment> fragments = fragmentManager.getFragments();
            /*OnCheckEventListener onCheckEventListener1 = ((OnCheckEventListener)fragments.get(position));*/
            //((OnCheckEventListener)getItem(position)).someEvent(WeightType.NONE);
            //((OnCheckEventListener) getSupportFragmentManager().getFragments().get(position)).someEvent(WeightType.NONE);
        }

        @Override
        public void onPageScrollStateChanged(final int state) {

        }

        public Fragment getCurrentFragment() {
            return mCurrentFragment;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            if (!object.equals(mCurrentFragment)) {
                mCurrentFragment = (Fragment) object;
            }
            super.setPrimaryItem(container, position, object);
        }

    }

    private static View createTabView(final Context context, final CharSequence text) {
        View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
        TextView tv = (TextView) view.findViewById(R.id.tabsText);
        tv.setText(text);
        return view;
    }
}
