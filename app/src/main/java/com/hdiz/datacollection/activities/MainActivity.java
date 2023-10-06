package com.hdiz.datacollection.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.util.Log;

import com.google.android.material.tabs.TabLayout;
import com.hdiz.datacollection.objects.Patients;
import com.hdiz.datacollection.R;
import com.hdiz.datacollection.RetrofitAPI;
import com.hdiz.datacollection.fragments.ActiveFragment;
import com.hdiz.datacollection.fragments.CompleteFragment;
import com.hdiz.datacollection.fragments.RecentFragment;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {


    private ViewPager viewPager;
    private TabLayout tabLayout;

    private RecentFragment recentFragment;
    private ActiveFragment activeFragment;
    private CompleteFragment completeFragment;

    private Patients patients ;

    private OnAboutDataReceivedListener mAboutDataListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);

        getData();

        recentFragment = new RecentFragment();
        activeFragment = new ActiveFragment();
        completeFragment = new CompleteFragment();
        tabLayout.setupWithViewPager(viewPager);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        //add fragments and set the adapter
        viewPagerAdapter.addFragment(recentFragment,"Recent");
        viewPagerAdapter.addFragment(activeFragment,"Active");
        viewPagerAdapter.addFragment(completeFragment,"Complete");
        viewPager.setAdapter(viewPagerAdapter);

        //set the icons
        tabLayout.getTabAt(0).setIcon(R.drawable.recent);
        tabLayout.getTabAt(1).setIcon(R.drawable.active);
        tabLayout.getTabAt(2).setIcon(R.drawable.complete);

    }
    public interface OnAboutDataReceivedListener {
        void onDataReceived(Patients model);
    }

    public void setAboutDataListener(OnAboutDataReceivedListener listener) {
        this.mAboutDataListener = listener;
        Log.d("here","2");
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments = new ArrayList<>();
        private List<String> fragmentTitles = new ArrayList<>();
        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }
        //add fragment to the viewpager
        public void addFragment(Fragment fragment, String title){
            fragments.add(fragment);
            fragmentTitles.add(title);
        }
        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }
        @Override
        public int getCount() {
            return fragments.size();
        }
        //to setup title of the tab layout
        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitles.get(position);
        }
    }
    private RetrofitAPI RetrofitBuilder() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://ph-test.kronikare.ai/detector/sereno/api/detector/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        return retrofitAPI;
    }
    private void getData(){
        RetrofitAPI retrofitAPI = RetrofitBuilder();
        Call<Patients> listCall = retrofitAPI.getPosts();
        listCall.enqueue(new Callback<Patients>() {
            @Override
            public void onResponse(Call<Patients> call, Response<Patients> response) {

                if (!response.isSuccessful()) {
                    //textView.setText("Code " + response.code());
                    Log.d("isNotSuccessful:", response.message());
                    return;
                }

                patients = response.body();
                mAboutDataListener.onDataReceived(patients);

//                for (Patient patient : patients) {
//                    String content = "";
//                    content += "ID: " + patient.getId() + "\n";
//                    content += "FIRSTNAME: " + patient.getFirstName() + "\n";
//                    content += "LASTNAME: " + patient.getLastName() + "\n";
//                    content += "AGE: " + patient.getAge() + "\n\n";
//                    Log.d("INFORMATION", content);
//                }

            }

            @Override
            public void onFailure(Call<Patients> call, Throwable t) {
                //textView.setText(t.getMessage());
                Log.d("failllll", t.getMessage());
            }
        });
    }
}