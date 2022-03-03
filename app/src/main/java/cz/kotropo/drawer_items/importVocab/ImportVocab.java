package cz.kotropo.drawer_items.importVocab;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import cz.kotropo.R;
import cz.kotropo.SharedPrefs;

public class ImportVocab extends AppCompatActivity {

    private final List<TableRow> tableRows = new ArrayList<>();
    private String language, batch;
    private int hundred;
    private ProgressBar pb;
    private ScrollView sv;
    private TableLayout tl;
    private TableRow lineForDelete;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        List<String> foreign = intent.getStringArrayListExtra("foreign");
        List<String> czech = intent.getStringArrayListExtra("czech");
        batch = intent.getStringExtra("batch");
        language = intent.getStringExtra("language");
        hundred = intent.getIntExtra("hundred", 0);

        ConstraintLayout cl = new ConstraintLayout(this);
        pb = new ProgressBar(this);
        pb.setLayoutParams(new LinearLayout.LayoutParams(350, 350));
        pb.setVisibility(View.INVISIBLE);

        sv = new ScrollView(this);

        cl.addView(sv);
        cl.addView(pb);
        pb.setId(1);
        sv.setId(2);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(cl);
        constraintSet.connect(pb.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        constraintSet.connect(pb.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        constraintSet.connect(pb.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraintSet.connect(pb.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);

        constraintSet.connect(sv.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        constraintSet.connect(sv.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        constraintSet.connect(sv.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraintSet.connect(sv.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);

        constraintSet.applyTo(cl);

        tl = new TableLayout(this);
        tl.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        TextView tv = new TextView(this);
        TableRow tr_first = new TableRow(this);
        tv.setPadding(30, 10, 10, 10);
        tv.setGravity(Gravity.START);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextSize(22);
        tv.setTextColor(getResources().getColor(R.color.textColor));
        tv.setText(batch);
        tr_first.addView(tv);
        tl.addView(tr_first);

        int size = Math.max(foreign.size(), czech.size());

        for (int i = 0; i < size; i++) {
            TableRow tableRow = new TableRow(this);
            TableRow.LayoutParams param = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);

            EditText et_foreign = new EditText(this);
            et_foreign.setPadding(30, 10, 10, 10);
            et_foreign.setGravity(Gravity.CENTER_VERTICAL);
            et_foreign.setBackgroundResource(android.R.color.transparent);
            et_foreign.setTextSize(20);
            if(i < foreign.size()){
                String s = foreign.get(i);
                StringBuilder f = new StringBuilder();
                for(char c : s.toCharArray()){
                    if(!Character.isDigit(c)){
                        f.append(c);
                    }
                }
                et_foreign.setText(f.toString());
            }
            et_foreign.setTextColor(getResources().getColor(R.color.textColor));
            et_foreign.setLayoutParams(param);

            EditText et_czech = new EditText(this);
            et_czech.setPadding(10, 10, 30, 10);
            et_czech.setGravity(Gravity.CENTER_VERTICAL);
            et_czech.setTextSize(20);
            et_czech.setBackgroundResource(android.R.color.transparent);
            et_czech.setTextColor(getResources().getColor(R.color.textColor));
            if(i < czech.size()){
                et_czech.setText(czech.get(i));
            }
            et_czech.setLayoutParams(param);

            registerForContextMenu(tableRow);

            tableRow.addView(et_foreign);
            tableRow.addView(et_czech);

            tl.addView(tableRow);
            tableRows.add(tableRow);

            View v = new View(this);
            v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 3));
            v.setBackgroundColor(Color.rgb(51, 51, 51));
            tl.addView(v);
        }
        sv.addView(tl);
        setContentView(cl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_tick, menu);
        return super.onCreateOptionsMenu(menu);
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
            tl.removeView(lineForDelete);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.tick) {
            new AlertDialog.Builder(this).setTitle("Kontrola").setMessage("Opravdu chcete importovat všechna slovíčka?").setPositiveButton("Ano", (dialog, which) -> new distinctVocab().execute()).setNegativeButton("Ne", null).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    @SuppressLint("StaticFieldLeak")
    private class addWords extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setVisibility(View.VISIBLE);
            sv.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (TableRow tr : tableRows) {
                EditText foreign, czech;
                try {
                    foreign = (EditText) tr.getChildAt(0);
                    czech = (EditText) tr.getChildAt(1);
                } catch (ClassCastException e) {
                    continue;
                }
                callApi(foreign, czech);
            }
            return null;
        }

        private void callApi(EditText foreign, EditText czech) {
            try {
                URL url = new URL("https://kotropo.wp4u.cz/api/api.php");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Authorization", "Basic " + new String(Base64.getEncoder().encode((SharedPrefs.DB_USERNAME + ":" + SharedPrefs.DB_PASSWORD).getBytes(StandardCharsets.UTF_8))));
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);
                String group;
                if (language.equals("AJ")) {
                    group = SharedPrefs.getString(getApplicationContext(), SharedPrefs.GROUP_AJ);
                } else {
                    group = SharedPrefs.getString(getApplicationContext(), SharedPrefs.GROUP_NJ);
                }
                JSONObject obj = new JSONObject();
                obj.put("language", language);
                obj.put("langGroup", group);
                obj.put("batch", batch);
                obj.put("hundred", hundred);
                obj.put("foreignVocab", foreign.getText().toString());
                obj.put("czechVocab", czech.getText().toString());
                obj.put("school", SharedPrefs.getString(getApplicationContext(), SharedPrefs.SCHOOL));
                obj.put("class", SharedPrefs.getString(getApplicationContext(), SharedPrefs.CLASS));
                try (OutputStream os = con.getOutputStream()) {
                    byte[] input = obj.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

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
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            Toast.makeText(getApplicationContext(), "Slovíčka byla nahrána", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class distinctVocab extends AsyncTask<Void, Void, Void> {

        private final List<String> foreignList = new ArrayList<>(), czechList = new ArrayList<>();
        private boolean correct = true;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setVisibility(View.VISIBLE);
            sv.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String group;
                if (language.equals("AJ")) {
                    group = SharedPrefs.getString(getApplicationContext(), SharedPrefs.GROUP_AJ);
                } else {
                    group = SharedPrefs.getString(getApplicationContext(), SharedPrefs.GROUP_NJ);
                }
                HttpURLConnection con = (HttpURLConnection) new URL("https://kotropo.wp4u.cz/api/api.php?language=" + language + "&langGroup=" + group + "&hundred=" + hundred + "&batch=" + batch + "&school=" + SharedPrefs.getString(getApplicationContext(), SharedPrefs.SCHOOL) + "&class=" + SharedPrefs.getString(getApplicationContext(), SharedPrefs.CLASS)).openConnection();
                con.setRequestProperty("Authorization", "Basic " + new String(Base64.getEncoder().encode((SharedPrefs.DB_USERNAME + ":" + SharedPrefs.DB_PASSWORD).getBytes(StandardCharsets.UTF_8))));
                con.setRequestMethod("GET");

                if (con.getResponseCode() == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();

                    JSONArray myArray = new JSONArray(content.toString());
                    for (int i = 0; i < myArray.length(); i++) {
                        foreignList.add((String) myArray.getJSONObject(i).get("foreignVocab"));
                        czechList.add((String) myArray.getJSONObject(i).get("czechVocab"));
                    }
                } else {
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

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            for (TableRow tr : tableRows) {
                EditText foreign, czech;
                try {
                    foreign = (EditText) tr.getChildAt(0);
                    czech = (EditText) tr.getChildAt(1);
                } catch (ClassCastException e) {
                    continue;
                }
                if (foreignList.contains(foreign.getText().toString()) || czechList.contains(czech.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Slovíčko " + czech.getText() + " = " + foreign.getText() + " se již v databázi nachází", Toast.LENGTH_LONG).show();
                    correct = false;
                    break;
                }
            }

            if (correct) {
                new addWords().execute();
            } else {
                pb.setVisibility(View.INVISIBLE);
                sv.setVisibility(View.VISIBLE);
            }
        }
    }
}