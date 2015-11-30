package me.littlecheesecake.uilibrary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import me.littlecheesecake.slidingtablayout.OnTabSelectListener;
import me.littlecheesecake.slidingtablayout.SlidingTabLayout;

/**
 * Created by yulu on 22/11/15.
 */
public class MainActivity2 extends AppCompatActivity implements OnTabSelectListener {

    private Context context = this;
    private ArrayList<Fragment> fragments = new ArrayList<>();

    private final String[] titles = {
            "shoes", "bag", "dress", "top", "bottom", "all"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main2);

        for (String title : titles) {
            fragments.add(SimpleCardFragment.getInstance(title));
        }

        ViewPager vp = (ViewPager) findViewById(R.id.vp);
        vp.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));

        SlidingTabLayout tl_1 = (SlidingTabLayout) findViewById(R.id.tl_1);

        tl_1.setViewPager(vp);

        vp.setCurrentItem(1);
    }

    @Override
    public void onTabSelect(int position) {

    }

    @Override
    public void onTabReselect(int position) {

    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }
    }

    @SuppressLint("ValidFragment")
    private static class SimpleCardFragment extends Fragment {
        private String title;

        public static SimpleCardFragment getInstance(String title) {
            SimpleCardFragment sf = new SimpleCardFragment();
            sf.title = title;
            return sf;
        }

        @Override
        public void onCreate(Bundle saveInstanceState) {
            super.onCreate(saveInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
            View v = inflater.inflate(R.layout.fr_simple_card, null);
            TextView card_title_tv = (TextView) v.findViewById(R.id.card_title_tv);
            card_title_tv.setText(title);

            return v;
        }
    }
}
