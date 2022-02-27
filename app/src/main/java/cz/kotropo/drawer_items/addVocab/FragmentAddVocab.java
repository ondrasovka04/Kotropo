package cz.kotropo.drawer_items.addVocab;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

import cz.kotropo.R;
import cz.kotropo.SharedPrefs;
import cz.kotropo.databinding.FragmentAddVocabBinding;


public class FragmentAddVocab extends Fragment {

    private Spinner languagePicker, batchPicker, hundredPicker;
    private EditText vocabForeign, vocabCzech;
    private Button addWord;
    private ImageButton newBatch, newHundred;
    private TextView bg1, bg2, header1, header2, header3;
    private ProgressBar progressBar;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentAddVocabBinding binding = FragmentAddVocabBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        vocabCzech = binding.vocabCzech;
        vocabForeign = binding.vocabForeign;
        addWord = binding.addWord;
        newBatch = binding.newBatch;
        newHundred = binding.newHundred;
        bg1 = binding.bg1;
        bg2 = binding.bg2;
        progressBar = binding.loading;
        batchPicker = binding.batchPicker;
        languagePicker = binding.languagePicker;
        hundredPicker = binding.hundredPicker;
        header1 = binding.header1;
        header2 = binding.header2;
        header3 = binding.header3;

        vocabCzech.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addWord.performClick();
                return true;
            }
            return false;
        });

        vocabForeign.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                vocabCzech.setFocusable(true);
                return true;
            }
            return false;
        });

        languagePicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                new loadHundreds().execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        hundredPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                new loadBatches().execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        newBatch.setOnClickListener(view -> {
            final EditText et = new EditText(getContext());
            et.setHint("Zadejte název nového batche:");
            final AlertDialog dialog = new AlertDialog.Builder(getContext()).setTitle("Nový batch").setView(et).setPositiveButton("OK", null).create();

            dialog.setOnShowListener(dialogInterface -> {
                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(view12 -> {
                    String typedBatch = et.getText().toString();
                    boolean contains = false;
                    for (int i = 0; i < hundredPicker.getAdapter().getCount(); i++) {
                        String s = (String) hundredPicker.getItemAtPosition(i);
                        if (s.equals(typedBatch)) {
                            contains = true;
                            break;
                        }
                    }
                    if (contains) {
                        et.setError("Takový batch již existuje");
                        et.setText("");
                    } else {
                        ArrayAdapter<String> adapter = (ArrayAdapter<String>) batchPicker.getAdapter();
                        adapter.add(typedBatch);
                        adapter.sort(Comparator.naturalOrder());
                        batchPicker.setAdapter(adapter);
                        batchPicker.setSelection(adapter.getPosition(typedBatch));
                        dialog.dismiss();
                    }
                });
            });
            dialog.show();
        });

        newHundred.setOnClickListener(view -> {
            final EditText et = new EditText(getContext());
            et.setHint("Zadejte pořadové číslo nové stovky:");
            et.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
            et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
            final AlertDialog dialog = new AlertDialog.Builder(getContext()).setTitle("Nová stovka").setView(et).setPositiveButton("OK", null).create();

            dialog.setOnShowListener(dialogInterface -> {
                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(view1 -> {
                    try {
                        int typedNumber = Integer.parseInt(et.getText().toString());
                        boolean contains = false;
                        for (int i = 0; i < hundredPicker.getAdapter().getCount(); i++) {
                            String s = (String) hundredPicker.getItemAtPosition(i);
                            int hundredNumber = Integer.parseInt(String.valueOf(s.charAt(0)));
                            if (hundredNumber == typedNumber) {
                                contains = true;
                                break;
                            }
                        }
                        if (contains) {
                            et.setError("Taková stovka již existuje");
                            et.setText("");
                        } else {
                            ArrayAdapter<String> adapter = (ArrayAdapter<String>) hundredPicker.getAdapter();
                            adapter.add(typedNumber + ".stovka");
                            adapter.sort(Comparator.naturalOrder());
                            hundredPicker.setAdapter(adapter);
                            hundredPicker.setSelection(adapter.getPosition(typedNumber + ".stovka"));
                            dialog.dismiss();
                        }
                    } catch (NumberFormatException e) {
                        et.setError("Špatně zadaná hodnota");
                        et.setText("");
                    }
                });
            });
            dialog.show();
        });

        addWord.setOnClickListener(view -> {
            if (vocabForeign.getText().toString().trim().equals("") && vocabCzech.getText().toString().trim().equals("")) {
                vocabForeign.setError("Špatně zadaná hodnota");
                vocabCzech.setError("Špatně zadaná hodnota");
                return;
            }
            if (vocabForeign.getText().toString().trim().equals("")) {
                vocabForeign.setError("Špatně zadaná hodnota");
                return;
            }
            if (vocabCzech.getText().toString().trim().equals("")) {
                vocabCzech.setError("Špatně zadaná hodnota");
                return;
            }
            if (batchPicker.getSelectedItem() == null) {
                Toast.makeText(getContext(), "Není vybrán žádný batch", Toast.LENGTH_SHORT).show();
                return;
            }
            new distinctVocab().execute();
        });

        new loadLanguages().execute();

        return root;
    }

    private void changeVisibility(boolean on) {
        if (on) {
            languagePicker.setVisibility(View.VISIBLE);
            batchPicker.setVisibility(View.VISIBLE);
            hundredPicker.setVisibility(View.VISIBLE);
            vocabCzech.setVisibility(View.VISIBLE);
            vocabForeign.setVisibility(View.VISIBLE);
            header1.setVisibility(View.VISIBLE);
            header2.setVisibility(View.VISIBLE);
            header3.setVisibility(View.VISIBLE);
            newHundred.setVisibility(View.VISIBLE);
            newBatch.setVisibility(View.VISIBLE);
            addWord.setVisibility(View.VISIBLE);
            bg1.setVisibility(View.VISIBLE);
            bg2.setVisibility(View.VISIBLE);

            progressBar.setVisibility(View.INVISIBLE);
        } else {
            languagePicker.setVisibility(View.INVISIBLE);
            batchPicker.setVisibility(View.INVISIBLE);
            hundredPicker.setVisibility(View.INVISIBLE);
            vocabCzech.setVisibility(View.INVISIBLE);
            vocabForeign.setVisibility(View.INVISIBLE);
            header1.setVisibility(View.INVISIBLE);
            header2.setVisibility(View.INVISIBLE);
            header3.setVisibility(View.INVISIBLE);
            newHundred.setVisibility(View.INVISIBLE);
            newBatch.setVisibility(View.INVISIBLE);
            addWord.setVisibility(View.INVISIBLE);
            bg1.setVisibility(View.INVISIBLE);
            bg2.setVisibility(View.INVISIBLE);

            progressBar.setVisibility(View.VISIBLE);
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
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, languages);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            languagePicker.setAdapter(adapter);
            changeVisibility(true);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class loadBatches extends AsyncTask<Void, Void, Void> {

        List<String> batchList = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            changeVisibility(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                String hundred = (String) hundredPicker.getSelectedItem();
                HttpURLConnection con = (HttpURLConnection) new URL("https://kotropo.wp4u.cz/api/api.php?language=" + languagePicker.getSelectedItem() + "&hundred=" + hundred.charAt(0) + "&school=" + SharedPrefs.getString(getContext(), SharedPrefs.SCHOOL) + "&class=" + SharedPrefs.getString(getContext(), SharedPrefs.CLASS)).openConnection();
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
                        if (!batchList.contains(batch)) {
                            batchList.add(batch);
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
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, batchList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            batchPicker.setAdapter(adapter);
            changeVisibility(true);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class loadHundreds extends AsyncTask<Void, Void, Void> {

        List<String> hundreds = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            changeVisibility(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                HttpURLConnection con = (HttpURLConnection) new URL("https://kotropo.wp4u.cz/api/api.php?language=" + languagePicker.getSelectedItem() + "&school=" + SharedPrefs.getString(getContext(), SharedPrefs.SCHOOL) + "&class=" + SharedPrefs.getString(getContext(), SharedPrefs.CLASS)).openConnection();
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
                        String hundred = myArray.getJSONObject(i).getString("hundred");
                        if (!hundreds.contains(hundred)) {
                            hundreds.add(hundred);
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
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, hundreds);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            hundredPicker.setAdapter(adapter);
            changeVisibility(true);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class distinctVocab extends AsyncTask<Void, Void, Void> {

        List<String> foreign = new ArrayList<>(), czech = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            changeVisibility(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String group;
                if (languagePicker.getSelectedItem().equals("AJ")) {
                    group = SharedPrefs.getString(getContext(), SharedPrefs.GROUP_AJ);
                } else {
                    group = SharedPrefs.getString(getContext(), SharedPrefs.GROUP_NJ);
                }
                HttpURLConnection con = (HttpURLConnection) new URL("https://kotropo.wp4u.cz/api/api.php?language=" + languagePicker.getSelectedItem() + "&langGroup=" + group + "&hundred=" + hundredPicker.getSelectedItem() + "&batch=" + batchPicker.getSelectedItem() + "&school=" + SharedPrefs.getString(getContext(), SharedPrefs.SCHOOL) + "&class=" + SharedPrefs.getString(getContext(), SharedPrefs.CLASS)).openConnection();
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
                        foreign.add((String) myArray.getJSONObject(i).get("foreignVocab"));
                        czech.add((String) myArray.getJSONObject(i).get("czechVocab"));
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
            if (czech.contains(vocabCzech.getText().toString()) && foreign.contains(vocabForeign.getText().toString())) {
                vocabCzech.setError("Takové slovíčko se již v databázi nachází.");
                vocabForeign.setError("Takové slovíčko se již v databázi nachází.");
            } else if (czech.contains(vocabCzech.getText().toString())) {
                vocabCzech.setError("Takové slovíčko se již v databázi nachází.");
            } else if (foreign.contains(vocabForeign.getText().toString())) {
                vocabForeign.setError("Takové slovíčko se již v databázi nachází.");
            } else {
                new addWord().execute();
            }
            changeVisibility(true);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class addWord extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            changeVisibility(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL url = new URL("https://kotropo.wp4u.cz/api/api.php");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Authorization", "Basic " + new String(Base64.getEncoder().encode((SharedPrefs.DB_USERNAME + ":" + SharedPrefs.DB_PASSWORD).getBytes(StandardCharsets.UTF_8))));
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);
                String group;
                if (languagePicker.getSelectedItem().equals("AJ")) {
                    group = SharedPrefs.getString(getContext(), SharedPrefs.GROUP_AJ);
                } else {
                    group = SharedPrefs.getString(getContext(), SharedPrefs.GROUP_NJ);
                }
                JSONObject obj = new JSONObject();
                obj.put("language", languagePicker.getSelectedItem());
                obj.put("langGroup", group);
                obj.put("batch", batchPicker.getSelectedItem());
                obj.put("hundred", hundredPicker.getSelectedItem());
                obj.put("foreignVocab", vocabForeign.getText().toString());
                obj.put("czechVocab", vocabCzech.getText().toString());
                obj.put("school", SharedPrefs.getString(getContext(), SharedPrefs.SCHOOL));
                obj.put("class", SharedPrefs.getString(getContext(), SharedPrefs.CLASS));
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


            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            vocabForeign.setText("");
            vocabCzech.setText("");
            changeVisibility(true);
            Toast.makeText(getContext(), "Slovíčko bylo úspěšně přidáno", Toast.LENGTH_LONG).show();
        }
    }
}