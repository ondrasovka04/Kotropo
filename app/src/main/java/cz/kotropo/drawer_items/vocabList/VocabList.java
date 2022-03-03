package cz.kotropo.drawer_items.vocabList;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import cz.kotropo.R;
import cz.kotropo.SharedPrefs;

public class VocabList extends AppCompatActivity {

    private TableLayout tl;
    private TableRow lineForDelete;
    private String group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScrollView sv = new ScrollView(this);
        tl = new TableLayout(this);
        tl.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        Intent intent = getIntent();
        List<String> czech = intent.getStringArrayListExtra("czech");
        List<String> foreign = intent.getStringArrayListExtra("foreign");
        List<String> batch = intent.getStringArrayListExtra("batch");
        group = intent.getStringExtra("group");

        String actualBatch = "";

        for (int i = 0; i < czech.size(); i++) {
            if (actualBatch.equals(batch.get(i))) {
                TableRow tableRow = new TableRow(this);

                TableRow.LayoutParams param = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);

                TextView tv_foreign = new TextView(this);
                tv_foreign.setPadding(30, 10, 10, 10);
                tv_foreign.setGravity(Gravity.CENTER_VERTICAL);
                tv_foreign.setTextSize(20);
                tv_foreign.setText(foreign.get(i));
                tv_foreign.setTextColor(getResources().getColor(R.color.textColor));
                tv_foreign.setLayoutParams(param);
                tv_foreign.setTag(actualBatch);

                TextView tv_czech = new TextView(this);
                tv_czech.setPadding(10, 10, 30, 10);
                tv_czech.setGravity(Gravity.CENTER_VERTICAL);
                tv_czech.setTextSize(20);
                tv_czech.setTextColor(getResources().getColor(R.color.textColor));
                tv_czech.setText(czech.get(i));
                tv_czech.setLayoutParams(param);
                tv_czech.setTag(actualBatch);

                tableRow.addView(tv_foreign);
                tableRow.addView(tv_czech);

                registerForContextMenu(tableRow);

                tl.addView(tableRow);

                View v = new View(this);
                v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 3));
                v.setBackgroundColor(Color.rgb(51, 51, 51));
                tl.addView(v);
            } else {
                actualBatch = batch.get(i);
                i--;
                TextView tv = new TextView(this);
                TableRow tr = new TableRow(this);
                tv.setPadding(30, 10, 10, 10);
                tv.setGravity(Gravity.START);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setTextSize(22);
                tv.setTextColor(getResources().getColor(R.color.textColor));
                tv.setText(actualBatch);
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
        lineForDelete = (TableRow) v;
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.delete) {
            TextView foreign = (TextView) lineForDelete.getChildAt(0);
            TextView czech = (TextView) lineForDelete.getChildAt(1);
            new deleteVocab(czech.getText().toString(), foreign.getText().toString(), foreign.getTag().toString()).execute();
            tl.removeView(lineForDelete);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @SuppressLint("StaticFieldLeak")
    private class deleteVocab extends AsyncTask<Void, Void, Void> {

        String czech, foreign, batch;

        public deleteVocab(String czech, String foreign, String batch) {
            this.czech = czech;
            this.foreign = foreign;
            this.batch = batch;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                if (group.equals("AJ")) {
                    group = SharedPrefs.getString(getApplicationContext(), SharedPrefs.GROUP_AJ);
                } else {
                    group = SharedPrefs.getString(getApplicationContext(), SharedPrefs.GROUP_NJ);
                }
                HttpURLConnection con = (HttpURLConnection) new URL("https://kotropo.wp4u.cz/api/api.php?batch=" + batch + "&foreignVocab=" + foreign + "&czechVocab=" + czech + "&langGroup=" + group + "&school=" + SharedPrefs.getString(getApplicationContext(), SharedPrefs.SCHOOL) + "&class=" + SharedPrefs.getString(getApplicationContext(), SharedPrefs.CLASS)).openConnection();
                con.setRequestProperty("Authorization", "Basic " + new String(Base64.getEncoder().encode((SharedPrefs.DB_USERNAME + ":" + SharedPrefs.DB_PASSWORD).getBytes(StandardCharsets.UTF_8))));
                con.setRequestMethod("DELETE");

                if (con.getResponseCode() != 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();
                    throw new IllegalStateException((String) new JSONObject(content.toString()).get("error_description"));
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}