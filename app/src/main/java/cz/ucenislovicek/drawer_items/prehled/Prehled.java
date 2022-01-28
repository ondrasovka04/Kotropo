package cz.ucenislovicek.drawer_items.prehled;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import cz.ucenislovicek.R;

public class Prehled extends AppCompatActivity {


    TableLayout tl;
    TableRow radkaProSmazani;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScrollView sv = new ScrollView(this);
        tl = new TableLayout(this);
        tl.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        Intent intent = getIntent();
        List<String> cesky = intent.getStringArrayListExtra("cesky");
        List<String> cizi = intent.getStringArrayListExtra("cizi");
        List<String> batch = intent.getStringArrayListExtra("batch");

        String aktualBatch = "";

        for (int i = 0; i < cesky.size(); i++) {
            if (aktualBatch.equals(batch.get(i))) {
                TableRow tableRow = new TableRow(this);

                TableRow.LayoutParams param = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);

                TextView tv_cizi = new TextView(this);
                tv_cizi.setPadding(10, 10, 10, 10);
                tv_cizi.setGravity(Gravity.CENTER_VERTICAL);
                tv_cizi.setTextSize(20);
                tv_cizi.setText(cizi.get(i));
                tv_cizi.setLayoutParams(param);
                tv_cizi.setTag(aktualBatch);

                TextView tv_cesky = new TextView(this);
                tv_cesky.setPadding(10, 10, 10, 10);
                tv_cesky.setGravity(Gravity.CENTER_VERTICAL);
                tv_cesky.setTextSize(20);
                tv_cesky.setText(cesky.get(i));
                tv_cesky.setLayoutParams(param);
                tv_cesky.setTag(aktualBatch);

                tableRow.addView(tv_cizi);
                tableRow.addView(tv_cesky);

                registerForContextMenu(tableRow);

                tl.addView(tableRow);

                View v = new View(this);
                v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 3));
                v.setBackgroundColor(Color.rgb(51, 51, 51));
                tl.addView(v);
            } else {
                aktualBatch = batch.get(i);
                i--;
                TextView tv = new TextView(this);
                TableRow tr = new TableRow(this);
                tv.setPadding(10, 10, 10, 10);
                tv.setGravity(Gravity.START);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setTextSize(22);
                tv.setText(aktualBatch);
                tr.addView(tv);
                tl.addView(tr);
            }
        }
        sv.addView(tl);
        setContentView(sv);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.delete_menu, menu);
        radkaProSmazani = (TableRow) v;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.delete) {
            TextView cizi = (TextView) radkaProSmazani.getChildAt(0);
            TextView cesky = (TextView) radkaProSmazani.getChildAt(1);
            new smazat_slovicko(cesky.getText().toString(), cizi.getText().toString(), cizi.getTag().toString()).execute();
            tl.removeView(radkaProSmazani);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private class smazat_slovicko extends AsyncTask<Void, Void, Void> {

        String cesky, cizi, batch;

        public smazat_slovicko(String cesky, String cizi, String batch) {
            this.cesky = cesky;
            this.cizi = cizi;
            this.batch = batch;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost/dk-313_uceniSlovicek?serverTimezone=Europe/Prague", "dk-313", "GyArab14");
                PreparedStatement st = con.prepareStatement("delete from slovicka where ciziSlovicko=? and ceskeSlovicko=? and batch=?");
                st.setString(1, cizi);
                st.setString(2, cesky);
                st.setString(3, batch);
                st.executeUpdate();
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}