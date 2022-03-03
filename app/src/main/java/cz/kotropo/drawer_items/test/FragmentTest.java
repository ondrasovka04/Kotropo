package cz.kotropo.drawer_items.test;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cz.kotropo.R;
import cz.kotropo.SharedPrefs;
import cz.kotropo.databinding.FragmentTestBinding;
import cz.kotropo.drawer_items.vocabList.MyExpandableListAdapter;


public class FragmentTest extends Fragment {

    private final Map<String, String> vocabulary = new LinkedHashMap<>();
    private TextView header1, header2, bg1, bg2;
    private Spinner languagePicker;
    private Button test;
    private List<String> hundredList = new ArrayList<>();
    private Map<String, List<String>> vocabCollection = new LinkedHashMap<>();
    private ExpandableListView expandableListView;
    private MyExpandableListAdapter expandableListAdapter;
    private ProgressBar loading;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentTestBinding binding = FragmentTestBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        header1 = binding.header1;
        header2 = binding.header2;
        languagePicker = binding.languagePicker;
        expandableListView = binding.elv;
        test = binding.test;
        bg1 = binding.bg1;
        bg2 = binding.bg2;
        loading = binding.loading;

        languagePicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                new loadBatches().execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        new loadLanguages().execute();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        vocabulary.clear();
    }

    private void changeVisibility(boolean on) {
        if (on) {
            bg1.setVisibility(View.VISIBLE);
            bg2.setVisibility(View.VISIBLE);
            test.setVisibility(View.VISIBLE);
            languagePicker.setVisibility(View.VISIBLE);
            expandableListView.setVisibility(View.VISIBLE);
            header1.setVisibility(View.VISIBLE);
            header2.setVisibility(View.VISIBLE);

            loading.setVisibility(View.INVISIBLE);
        } else {
            bg1.setVisibility(View.INVISIBLE);
            bg2.setVisibility(View.INVISIBLE);
            test.setVisibility(View.INVISIBLE);
            languagePicker.setVisibility(View.INVISIBLE);
            expandableListView.setVisibility(View.INVISIBLE);
            header1.setVisibility(View.INVISIBLE);
            header2.setVisibility(View.INVISIBLE);

            loading.setVisibility(View.VISIBLE);
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class loadVocab extends AsyncTask<Void, Void, Void> {

        List<String> batches, hundreds;

        public loadVocab(List<String> batches, List<String> hundreds) {
            this.batches = batches;
            this.hundreds = hundreds;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            changeVisibility(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (String batch : batches) {
                callApi(batch, "batch");
            }
            for (String hundred : hundreds) {
                callApi(hundred.substring(0, 1), "hundred");
            }
            return null;
        }

        private void callApi(String b, String parameter) {
            try {
                String group;
                if (languagePicker.getSelectedItem().equals("AJ")) {
                    group = SharedPrefs.getString(getContext(), SharedPrefs.GROUP_AJ);
                } else {
                    group = SharedPrefs.getString(getContext(), SharedPrefs.GROUP_NJ);
                }
                HttpURLConnection con = (HttpURLConnection) new URL("https://kotropo.wp4u.cz/api/api.php?language=" + languagePicker.getSelectedItem() + "&" + parameter + "=" + b + "&langGroup=" + group + "&school=" + SharedPrefs.getString(getContext(), SharedPrefs.SCHOOL) + "&class=" + SharedPrefs.getString(getContext(), SharedPrefs.CLASS)).openConnection();
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
                        vocabulary.put((String) myArray.getJSONObject(i).get("foreignVocab"), (String) myArray.getJSONObject(i).get("czechVocab"));
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
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            List<Map.Entry<String, String>> entries = new ArrayList<>(vocabulary.entrySet());
            Collections.shuffle(entries);
            LinkedHashMap<String, String> shuffeledVocab = new LinkedHashMap<>();
            entries.forEach(entry -> shuffeledVocab.put(entry.getKey(), entry.getValue()));
            Intent i = new Intent(getContext(), Test.class);
            i.putExtra("map", shuffeledVocab);
            startActivity(i);
            changeVisibility(true);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class loadLanguages extends AsyncTask<Void, Void, Void> {

        List<String> languages = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            changeVisibility(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                HttpURLConnection con = (HttpURLConnection) new URL("https://kotropo.wp4u.cz/api/api.php?school=" + SharedPrefs.getString(getContext(), SharedPrefs.SCHOOL) + "&class=" + SharedPrefs.getString(getContext(), SharedPrefs.CLASS)).openConnection();
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
                        String language = (String) myArray.getJSONObject(i).get("language");
                        if (!languages.contains(language)) {
                            languages.add(language);
                        }
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
            changeVisibility(true);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, languages);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            languagePicker.setAdapter(adapter);
            new loadBatches().execute();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class loadBatches extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            changeVisibility(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            List<Integer> hundreds = new ArrayList<>();
            List<String> batches = new ArrayList<>();

            try {
                String group;
                if (languagePicker.getSelectedItem().equals("AJ")) {
                    group = SharedPrefs.getString(getContext(), SharedPrefs.GROUP_AJ);
                } else {
                    group = SharedPrefs.getString(getContext(), SharedPrefs.GROUP_NJ);
                }
                HttpURLConnection con = (HttpURLConnection) new URL("https://kotropo.wp4u.cz/api/api.php?language=" + languagePicker.getSelectedItem() + "&sortBy=hundred,batch&langGroup=" + group + "&school=" + SharedPrefs.getString(getContext(), SharedPrefs.SCHOOL) + "&class=" + SharedPrefs.getString(getContext(), SharedPrefs.CLASS)).openConnection();
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
                        String batch = (String) myArray.getJSONObject(i).get("batch");
                        int hundred = (Integer) myArray.getJSONObject(i).get("hundred");
                        if (!batches.contains(batch) || (batches.contains(batch) && hundreds.get(batches.indexOf(batch)) != hundred)) {
                            hundreds.add(hundred);
                            batches.add(batch);
                        }
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

                List<String> batchList = new ArrayList<>();

                vocabCollection = new LinkedHashMap<>();
                hundredList = new ArrayList<>();
                int lastHundred = 1;
                hundredList.add(lastHundred + ".stovka");
                for (int i = 0; i < batches.size(); i++) {
                    int aktualHundred = hundreds.get(i);
                    if (lastHundred == aktualHundred) {
                        batchList.add(batches.get(i));
                    } else {
                        vocabCollection.put(lastHundred + ".stovka", batchList);
                        lastHundred = aktualHundred;
                        hundredList.add(aktualHundred + ".stovka");
                        batchList = new ArrayList<>();
                        batchList.add(batches.get(i));
                    }
                }
                vocabCollection.put(lastHundred + ".stovka", batchList);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            changeVisibility(true);
            expandableListAdapter = new MyExpandableListAdapter(getContext(), hundredList, vocabCollection);
            expandableListView.setAdapter(expandableListAdapter);
            expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
                int lastExpandedPosition = -1;

                @Override
                public void onGroupExpand(int i) {
                    if (lastExpandedPosition != -1 && i != lastExpandedPosition) {
                        expandableListView.collapseGroup(lastExpandedPosition);
                    }
                    lastExpandedPosition = i;
                }
            });
            test.setOnClickListener(view -> {
                List<String> batch = new ArrayList<>();
                List<String> hundreds = new ArrayList<>();
                MyExpandableListAdapter.Group[] groupes = expandableListAdapter.getGroupes();
                for (MyExpandableListAdapter.Group g : groupes) {
                    for (CheckBox cb : g.getBatches()) {
                        if (cb != null) {
                            if (cb.isChecked()) {
                                batch.add((String) cb.getTag());
                            }
                        } else {
                            if (g.getHundredBox().isChecked()) {
                                String s = (String) g.getHundredBox().getTag();
                                hundreds.add(s.split("@")[0]);
                                break;
                            }
                        }
                    }
                }
                if (hundreds.isEmpty() && batch.isEmpty()) {
                    Toast.makeText(getContext(), "Musíte vybrat alespoň jednu stovku nebo batch.", Toast.LENGTH_LONG).show();
                } else {
                    new loadVocab(batch, hundreds).execute();
                }
            });
        }
    }
}