package cz.ucenislovicek.BakalariAPI;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import cz.ucenislovicek.R;
import cz.ucenislovicek.SharedPrefs;


public class SchoolsListActivity extends AppCompatActivity {
    public static final int RESULT_OK = 0;
    public static final int RESULT_CANCEL = 1;
    SchoolsListFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_schools);

        fragment = (SchoolsListFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentSchools);
        if (fragment != null) {
            fragment.setOnItemClickListener(url -> {

                SharedPrefs.setString(this, SharedPrefs.URL, url);
                setResult(RESULT_OK, new Intent());
                finish();
            });
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCEL);
        super.onBackPressed();
    }
}