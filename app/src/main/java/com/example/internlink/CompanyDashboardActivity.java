package com.example.internlink;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.internlink.databinding.ActivityCompanyDashboardBinding;

public class CompanyDashboardActivity extends BaseActivity {

    private ActivityCompanyDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCompanyDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar(binding.toolbar, "Company Dashboard", false);

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                loadFragment(new CompanyStatsFragment());
                return true;
            } else if (id == R.id.nav_post_job) {
                loadFragment(new PostJobFragment());
                return true;
            } else if (id == R.id.nav_applicants) {
                loadFragment(new CompanyInternshipsFragment());
                return true;
            } else if (id == R.id.nav_profile) {
                loadFragment(new CompanyProfileFragment());
                return true;
            }
            return false;
        });

        // Default
        loadFragment(new CompanyStatsFragment());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
