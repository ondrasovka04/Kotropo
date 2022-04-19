package cz.kotropo.drawer_items.importVocab;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;
import static android.os.Build.VERSION.SDK_INT;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.OpenCVLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import cz.kotropo.R;
import cz.kotropo.SharedPrefs;
import cz.kotropo.databinding.FragmentImportVocabBinding;

public class FragmentImportVocab extends Fragment {
    private static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/Tess";
    private static final String TESS_DATA = "/tessdata";
    private Spinner languagePicker, batchPicker, hundredPicker;
    private Button importVocab;
    private ImageButton newBatch, newHundred;
    private TextView bg1, header1, header2, header3;
    private ProgressBar progressBar;
    private boolean b = false;
    private ImageView imageView;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentImportVocabBinding binding = FragmentImportVocabBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        importVocab = binding.addWord;
        newBatch = binding.newBatch;
        newHundred = binding.newHundred;
        bg1 = binding.bg1;
        progressBar = binding.loading;
        batchPicker = binding.batchPicker;
        languagePicker = binding.languagePicker;
        hundredPicker = binding.hundredPicker;
        header1 = binding.header1;
        header2 = binding.header2;
        header3 = binding.header3;
        imageView = binding.imageView2;

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, 100);
        }

        if (ContextCompat.checkSelfPermission(requireContext(), WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{WRITE_EXTERNAL_STORAGE}, 100);
        }

        if (SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse(String.format("package:%s", requireContext().getPackageName())));
                    startActivityForResult(intent, 2296);
                } catch (Exception e) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, 2296);
                }
            }
        }

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

        importVocab.setOnClickListener(view -> {
            final CharSequence[] options = {"Vyfotit obrázek", "Vybrat obrázek", "Zrušit"};
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Importovat slovíčka");
            builder.setItems(options, (dialog, item) -> {
                if (options[item].equals("Vyfotit obrázek")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 1);
                } else if (options[item].equals("Vybrat obrázek")) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Vybrát obrázek"), 2);

                } else if (options[item].equals("Zrušit")) {
                    dialog.dismiss();
                }
            });
            builder.show();
        });

        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(getContext(), "Nepodařilo se načíst OpenCV knihovnu", Toast.LENGTH_SHORT).show();
        }

        new loadLanguages().execute();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (b) {
            b = false;
            changeVisibility(true);
        }
    }

    private void prepareTessData() {
        try {
            File dir = new File(DATA_PATH + TESS_DATA);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String[] fileList = requireContext().getAssets().list("");
            for (String fileName : fileList) {
                String pathToDataFile = DATA_PATH + TESS_DATA + "/" + fileName;
                if (!(new File(pathToDataFile)).exists()) {
                    InputStream is = requireContext().getAssets().open(fileName);
                    OutputStream os = new FileOutputStream(pathToDataFile);
                    byte[] buff = new byte[1024];
                    int len;
                    while ((len = is.read(buff)) > 0) {
                        os.write(buff, 0, len);
                    }
                    is.close();
                    os.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String language = languagePicker.getSelectedItem().equals("AJ") ? "eng" : "deu";
        changeVisibility(false);
        prepareTessData();

        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bitmap bitmap = (Bitmap) Objects.requireNonNull(data).getExtras().get("data");

            Thread thread = new Thread(() -> {
                OCR ocr = new OCR(bitmap, language, DATA_PATH);
                Intent i = new Intent(getContext(), ImportVocab.class);
                i.putStringArrayListExtra("czech", ocr.getCzech());
                i.putStringArrayListExtra("foreign", ocr.getForeign());
                i.putExtra("language", (String) languagePicker.getSelectedItem());
                i.putExtra("hundred", Integer.parseInt((String) hundredPicker.getSelectedItem()));
                i.putExtra("batch", (String) batchPicker.getSelectedItem());
                startActivity(i);
                b = true;
            });
            thread.start();

        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "picture.jpg");
            try {
                InputStream inputStream;
                try {
                    inputStream = requireActivity().getContentResolver().openInputStream(data.getData());
                } catch (NullPointerException e) {
                    Toast.makeText(getContext(), "Nahrání fotky se nepodařilo", Toast.LENGTH_SHORT).show();
                    return;
                }
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
                fileOutputStream.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

            Thread thread = new Thread(() -> {
                OCR ocr = new OCR(bitmap, language, DATA_PATH);
                Intent i = new Intent(getContext(), ImportVocab.class);
                i.putStringArrayListExtra("czech", ocr.getCzech());
                i.putStringArrayListExtra("foreign", ocr.getForeign());
                i.putExtra("language", (String) languagePicker.getSelectedItem());
                i.putExtra("hundred", Integer.parseInt((String) hundredPicker.getSelectedItem()));
                i.putExtra("batch", (String) batchPicker.getSelectedItem());
                startActivity(i);
                b = true;
            });
            thread.start();

        } else if (requestCode == 2296) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    Toast.makeText(getContext(), "Dejte aplikaci povolení", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void changeVisibility(boolean on) {
        if (on) {
            languagePicker.setVisibility(View.VISIBLE);
            batchPicker.setVisibility(View.VISIBLE);
            hundredPicker.setVisibility(View.VISIBLE);
            header1.setVisibility(View.VISIBLE);
            header2.setVisibility(View.VISIBLE);
            header3.setVisibility(View.VISIBLE);
            newHundred.setVisibility(View.VISIBLE);
            newBatch.setVisibility(View.VISIBLE);
            importVocab.setVisibility(View.VISIBLE);
            bg1.setVisibility(View.VISIBLE);

            progressBar.setVisibility(View.INVISIBLE);
        } else {
            languagePicker.setVisibility(View.INVISIBLE);
            batchPicker.setVisibility(View.INVISIBLE);
            hundredPicker.setVisibility(View.INVISIBLE);
            header1.setVisibility(View.INVISIBLE);
            header2.setVisibility(View.INVISIBLE);
            header3.setVisibility(View.INVISIBLE);
            newHundred.setVisibility(View.INVISIBLE);
            newBatch.setVisibility(View.INVISIBLE);
            importVocab.setVisibility(View.INVISIBLE);
            bg1.setVisibility(View.INVISIBLE);

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
                HttpURLConnection con = (HttpURLConnection) new URL("https://kotropo.wp4u.cz/api/api.php?school=" + SharedPrefs.getString(getContext(), SharedPrefs.SCHOOL) + "&class=" + SharedPrefs.getString(getContext(), SharedPrefs.CLASS) + "&sortBy=language").openConnection();
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
                HttpURLConnection con = (HttpURLConnection) new URL("https://kotropo.wp4u.cz/api/api.php?language=" + languagePicker.getSelectedItem() + "&hundred=" + hundred.charAt(0) + "&school=" + SharedPrefs.getString(getContext(), SharedPrefs.SCHOOL) + "&class=" + SharedPrefs.getString(getContext(), SharedPrefs.CLASS) + "&sortBy=batch").openConnection();
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
                HttpURLConnection con = (HttpURLConnection) new URL("https://kotropo.wp4u.cz/api/api.php?language=" + languagePicker.getSelectedItem() + "&school=" + SharedPrefs.getString(getContext(), SharedPrefs.SCHOOL) + "&class=" + SharedPrefs.getString(getContext(), SharedPrefs.CLASS) + "&sortBy=hundred").openConnection();
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
}
