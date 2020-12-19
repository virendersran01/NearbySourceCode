package com.droideve.apps.nearbystores.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;

import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.Services.NotifyDataNotificationEvent;
import com.droideve.apps.nearbystores.appconfig.AppConfig;
import com.droideve.apps.nearbystores.classes.Notification;
import com.droideve.apps.nearbystores.controllers.sessions.SessionsController;
import com.droideve.apps.nearbystores.customView.SwipeDisabledViewPager;
import com.droideve.apps.nearbystores.dtmessenger.MessengerHelper;
import com.droideve.apps.nearbystores.events.UnseenMessagesEvent;
import com.droideve.apps.nearbystores.navigationdrawer.NavigationDrawerFragment;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.droideve.apps.nearbystores.appconfig.AppConfig.APP_DEBUG;
import static com.droideve.apps.nearbystores.appconfig.AppConfig.SHOW_ADS;
import static com.droideve.apps.nearbystores.appconfig.AppConfig.SHOW_ADS_IN_HOME;


public class V2MainFragment extends Fragment {

    public final static String TAG = "mainfragment";
    static BottomNavigationView navigation;
    //navigation bottom
    @BindView(R.id.navigation_bottom)
    View navigation_bottom_view;
    //viewpager
    @BindView(R.id.viewpager)
    SwipeDisabledViewPager viewPager;
    private Context myContext;
    private MenuItem prevMenuItem;
    private Listener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.v2_fragment_main, container, false);
        ButterKnife.bind(this, rootView);

        //init view pager adapter
        initViewPagerAdapter();

        //init Navigation Bottom
        initNavigationBottomView();


        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void initAmob() {
        //Show Banner Ads
        if (SHOW_ADS && SHOW_ADS_IN_HOME) {

            if (AppConfig.APP_DEBUG)
                Toast.makeText(myContext, "SHOW_ADS_IN_HOME:" + getResources()
                        .getString(R.string.banner_ad_unit_id), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    private void initViewPagerAdapter() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(4);
    }

    public boolean goToPreviousView() {
        if (viewPager.getCurrentItem() > 0) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            navigation.getMenu().getItem(viewPager.getCurrentItem()).setChecked(true);
            return true;
        }
        return false;
    }

    public boolean setCurrentFragment(int position) {

        //if (viewPager.getCurrentItem() > 0) {
        viewPager.setCurrentItem(position);
        navigation.getMenu().getItem(position).setChecked(true);
        return true;
        /*}
        return false;*/

    }

    public boolean ifFirstFragment() {
        return viewPager.getCurrentItem() == 0;
    }

    private void initNavigationBottomView() {

        if (mListener != null)
            mListener.onScrollHorizontal(0);


        navigation = navigation_bottom_view.findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {


                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        viewPager.setCurrentItem(0);

                        /*//enable nav drawer when fragment is closed
                        NavigationDrawerFragment.getInstance().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);*/

                        if (mListener != null)
                            mListener.onScrollHorizontal(0);

                        return true;
                    case R.id.navigation_categories:
                        viewPager.setCurrentItem(1);

                        if (mListener != null)
                            mListener.onScrollHorizontal(1);

                        return true;
                    case R.id.navigation_favorites:
                        viewPager.setCurrentItem(2);

                        if (mListener != null)
                            mListener.onScrollHorizontal(2);

                        return true;
                    case R.id.navigation_notification:
                        viewPager.setCurrentItem(3);

                        if (mListener != null)
                            mListener.onScrollHorizontal(3);

                        return true;
                    case R.id.navigation_account:

                        if (NavigationDrawerFragment.getInstance() != null)
                            NavigationDrawerFragment.getInstance().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

                        FragmentManager manager = getActivity().getSupportFragmentManager();
                        Fragment fragmentAcc;

                        if (SessionsController.isLogged()) {
                            fragmentAcc = new ProfileFragment();

                        } else {
                            fragmentAcc = new AuthenticationFragment();
                        }

                        if (manager != null) {
                            manager.beginTransaction()
                                    .add(R.id.frame_container, fragmentAcc, "accountFrag")
                                    .addToBackStack("accountFrag")
                                    .commit();
                        }
                        return true;

                }


                return false;
            }
        });
    }

    private void updateNotificationBadge() {

        //notificationsUnseen
        NotificationChatVPFragment.NotificationChaCount = Notification.notificationsUnseen + MessengerHelper.NbrMessagesManager.getNbrTotalMessages();

        BottomNavigationMenuView bottomNavigationMenuView =
                (BottomNavigationMenuView) navigation.getChildAt(0);
        View v = bottomNavigationMenuView.getChildAt(3);
        BottomNavigationItemView itemView = (BottomNavigationItemView) v;

        View badge = LayoutInflater.from(myContext)
                .inflate(R.layout.nav_btm_notif_badge, itemView, true);

        if (NotificationChatVPFragment.NotificationChaCount > 0) {
            badge.findViewById(R.id.notifications_badge).setVisibility(View.VISIBLE);
            int notificationCounter = NotificationChatVPFragment.NotificationChaCount;
            ((TextView) badge.findViewById(R.id.notifications_badge)).setText(notificationCounter >= 100 ? "+99" : String.valueOf(notificationCounter));
        } else {
            badge.findViewById(R.id.notifications_badge).setVisibility(View.GONE);
        }

    }

    // This method will be called when a Notification is posted (in the UI thread for Toast)
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NotifyDataNotificationEvent event) {
        if (event.message.equals("update_badges")) {
            updateNotificationBadge();
        }
    }

    // This method will be called when a Messages is posted (in the UI thread for Toast)
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UnseenMessagesEvent event) {
        if (APP_DEBUG)
            if (MessengerHelper.NbrMessagesManager.getNbrTotalMessages() > 0) {
                Toast.makeText(getActivity(), "New message " + MessengerHelper.NbrMessagesManager.getNbrTotalMessages()
                        , Toast.LENGTH_LONG).show();

            }
        updateNotificationBadge();
    }

    private void pageChangedListener(final SwipeDisabledViewPager mViewPager, final BottomNavigationView mBottomNavigationView) {


        mViewPager.addOnPageChangeListener(new SwipeDisabledViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                prevMenuItem = mBottomNavigationView.getMenu().getItem(position);


                if (prevMenuItem != null)
                    prevMenuItem.setChecked(false);
                else {
                    mBottomNavigationView.getMenu().getItem(0).setChecked(false);
                    mBottomNavigationView.getMenu().getItem(position).setChecked(true);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {


            }

        });
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myContext = activity;
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);

        //display notification badge counter
        updateNotificationBadge();


    }

    @Override
    public void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.home_menu, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().finish();
        } else if (item.getItemId() == R.id.search_icon) {

            CustomSearchFragment fragment = new CustomSearchFragment();
            FragmentManager manager = getFragmentManager();
            manager.beginTransaction()
                    .add(R.id.main_container, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack("customSearchFrag")
                    .commit();

        } else {
            Toast.makeText(myContext, item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void setListener(final Listener mItemListener) {
        this.mListener = mItemListener;
    }

    public interface Listener {
        void onScrollHorizontal(int position);

        void onScrollVertical(int scrollXs, int scrollY);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter implements HomeFragment.Listener {

        int FRAGS_ITEMS_NUM = 5;

        public ViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return FRAGS_ITEMS_NUM;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    HomeFragment frag = HomeFragment.newInstance(0, "Page # 1");
                    frag.setListener(this);
                    return frag;
                case 1:
                    return CategoryFragment.newInstance(1, "Page # 2");
                case 2:
                    return BookmarkFragment.newInstance(2, "Page # 2");
                case 3:
                    return NotificationChatVPFragment.newInstance(3, "Page # 4");
                case 4:
                    if (SessionsController.isLogged())
                        return ProfileFragment.newInstance(4, "Page # 3");
                    else return AuthenticationFragment.newInstance(4, "Page # 3");

                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }

        @Override
        public void onScroll(int scrollX, int scrollY) {
            Log.e("ViewPagerAdapter", scrollX + " - " + scrollY);
            if (mListener != null)
                mListener.onScrollVertical(scrollX, scrollY);
        }
    }

}
