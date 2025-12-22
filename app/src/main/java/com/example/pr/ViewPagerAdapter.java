package com.example.pr;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new EditFragment();
            case 1:
                return new AddFragment();
            case 2:
                return new RecordFragment();
            default:
                return new EditFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Количество вкладок
    }
}
