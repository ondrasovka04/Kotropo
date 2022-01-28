package cz.ucenislovicek.drawer_items.pridaniSlovicek;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import cz.ucenislovicek.R;
import cz.ucenislovicek.databinding.FragmentPridaniSlovicekBinding;

public class FragmentPridaniSlovicek extends Fragment {


    Spinner jazyk, batch, stovka;
    EditText slovickoCizi, slovickoCesky;
    Button pridat;
    ImageButton novyBatch, novaStovka;
    TextView tw1, tw2;
    ProgressBar pb;


    @SuppressLint("NewApi")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentPridaniSlovicekBinding binding = FragmentPridaniSlovicekBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        slovickoCesky = binding.slovickoCesky;
        slovickoCizi = binding.slovickoCizi;
        pridat = binding.pridatslovicko;
        novyBatch = binding.novyBatch;
        novaStovka = binding.novaStovka;
        tw1 = binding.textView3;
        tw2 = binding.textView4;
        pb = binding.progressBar2;
        batch = binding.volbaBatche;
        jazyk = binding.volbaJazyku;
        stovka = binding.volbaStovky;

        slovickoCesky.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_DONE){
                pridat.performClick();
                return true;
            }
            return false;
        });

        slovickoCizi.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_DONE){
                slovickoCesky.setFocusable(true);
                return true;
            }
            return false;
        });

        jazyk.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                new nactiStovky().execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        stovka.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                new loadBatches().execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        novyBatch.setOnClickListener(view -> {
            final EditText et = new EditText(getContext());
            et.setHint("Zadejte název nového batche:");
            final AlertDialog dialog = new AlertDialog.Builder(getContext()).setTitle("Nový batch").setView(et).setPositiveButton("OK", null).create();

            dialog.setOnShowListener(dialogInterface -> {
                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(view12 -> {
                    String zadanyBatche = et.getText().toString();
                    boolean obsahuje = false;
                    for (int i = 0; i < stovka.getAdapter().getCount(); i++) {
                        String s = (String) stovka.getItemAtPosition(i);
                        if (s.equals(zadanyBatche)) {
                            obsahuje = true;
                            break;
                        }
                    }
                    if (obsahuje) {
                        et.setError("Takový batch již existuje");
                        et.setText("");
                    } else {
                        ArrayAdapter<String> adapter = (ArrayAdapter<String>) batch.getAdapter();
                        adapter.add(zadanyBatche);
                        adapter.sort(Comparator.naturalOrder());
                        batch.setAdapter(adapter);
                        batch.setSelection(adapter.getPosition(zadanyBatche));
                        dialog.dismiss();
                    }
                });
            });
            dialog.show();
        });

        novaStovka.setOnClickListener(view -> {
            final EditText et = new EditText(getContext());
            et.setHint("Zadejte pořadové číslo nové stovky:");
            et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
            final AlertDialog dialog = new AlertDialog.Builder(getContext()).setTitle("Nová stovka").setView(et).setPositiveButton("OK", null).create();

            dialog.setOnShowListener(dialogInterface -> {
                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(view1 -> {
                    try {
                        int zadaneCislo = Integer.parseInt(et.getText().toString());
                        boolean obsahuje = false;
                        for (int i = 0; i < stovka.getAdapter().getCount(); i++) {
                            String s = (String) stovka.getItemAtPosition(i);
                            int cisloStovky = Integer.parseInt(String.valueOf(s.charAt(0)));
                            if (cisloStovky == zadaneCislo) {
                                obsahuje = true;
                                break;
                            }
                        }
                        if (obsahuje) {
                            et.setError("Taková stovka již existuje");
                            et.setText("");
                        } else {
                            ArrayAdapter<String> adapter = (ArrayAdapter<String>) stovka.getAdapter();
                            adapter.add(zadaneCislo + ".stovka");
                            adapter.sort(Comparator.naturalOrder());
                            stovka.setAdapter(adapter);
                            stovka.setSelection(adapter.getPosition(zadaneCislo + ".stovka"));
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

        pridat.setOnClickListener(view -> {
            if (slovickoCizi.getText().toString().trim().equals("") && slovickoCesky.getText().toString().trim().equals("")) {
                slovickoCizi.setError("Špatně zadaná hodnota");
                slovickoCesky.setError("Špatně zadaná hodnota");
                return;
            }
            if (slovickoCizi.getText().toString().trim().equals("")) {
                slovickoCizi.setError("Špatně zadaná hodnota");
                return;
            }
            if (slovickoCesky.getText().toString().trim().equals("")) {
                slovickoCesky.setError("Špatně zadaná hodnota");
                return;
            }
            if (batch.getSelectedItem() == null) {
                Toast.makeText(getContext(), "Není vybrán žádný batch", Toast.LENGTH_SHORT).show();
                return;
            }
            new getIdSlovicka().execute();
        });

        new getJazyky().execute();

        return root;
    }

    private class getJazyky extends AsyncTask<Void, Void, Void> {

        List<String> jazyky = new ArrayList<>();

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost/dk-313_uceniSlovicek?serverTimezone=Europe/Prague", "dk-313", "GyArab14");
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("select distinct jazyk from slovicka");
                while (rs.next()) {
                    jazyky.add(rs.getString("jazyk"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, jazyky);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            jazyk.setAdapter(adapter);
        }
    }

    private class loadBatches extends AsyncTask<Void, Void, Void> {

        List<String> batch = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost/dk-313_uceniSlovicek?serverTimezone=Europe/Prague", "dk-313", "GyArab14");
                PreparedStatement st = con.prepareStatement("select distinct batch from slovicka where jazyk=? and stovka=?");
                st.setString(1, (String) jazyk.getSelectedItem());
                String s = (String) stovka.getSelectedItem();
                st.setInt(2, Integer.parseInt(String.valueOf(s.charAt(0))));
                ResultSet rs = st.executeQuery();
                while (rs.next()) {
                    batch.add(rs.getString("batch"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, batch);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            FragmentPridaniSlovicek.this.batch.setAdapter(adapter);

            pb.setVisibility(View.INVISIBLE);
        }
    }

    private class nactiStovky extends AsyncTask<Void, Void, Void> {

        List<String> stovky = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost/dk-313_uceniSlovicek?serverTimezone=Europe/Prague", "dk-313", "GyArab14");
                PreparedStatement st = con.prepareStatement("select distinct stovka from slovicka where jazyk=?");
                st.setString(1, (String) jazyk.getSelectedItem());
                ResultSet rs = st.executeQuery();
                while (rs.next()) {
                    stovky.add(rs.getInt("stovka") + ".stovka");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, stovky);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            stovka.setAdapter(adapter);
            pb.setVisibility(View.INVISIBLE);
        }
    }

    private class pridatSlovicko extends AsyncTask<Void, Void, Void> {

        int idSlovicka;
        boolean proslo = false;

        public pridatSlovicko(int idSlovicka) {
            this.idSlovicka = idSlovicka + 1;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost/dk-313_uceniSlovicek?serverTimezone=Europe/Prague", "dk-313", "GyArab14");
                PreparedStatement st = con.prepareStatement("insert into slovicka (idSlovicka, jazyk, batch, stovka, ciziSlovicko, ceskeSlovicko, skupina) values (?, ?, ?, ?, ?, ?, ?)");
                st.setInt(1, idSlovicka);
                st.setString(2, (String) jazyk.getSelectedItem());
                st.setString(3, (String) batch.getSelectedItem());
                String s = (String) stovka.getSelectedItem();
                st.setInt(4, Character.getNumericValue(s.charAt(0)));
                st.setString(5, slovickoCizi.getText().toString());
                st.setString(6, slovickoCesky.getText().toString());
                st.setString(7, "A2");
                st.executeUpdate();
                proslo = true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            slovickoCizi.setText("");
            slovickoCesky.setText("");
            if (proslo) {
                Toast.makeText(getContext(), "Slovíčko bylo úspěšně přidáno", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class getIdSlovicka extends AsyncTask<Void, Void, Void> {

        int idSlovicka;

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost/dk-313_uceniSlovicek?serverTimezone=Europe/Prague", "dk-313", "GyArab14");
                PreparedStatement st = con.prepareStatement("select max(idSlovicka) from slovicka where jazyk=?");
                st.setString(1, (String) jazyk.getSelectedItem());
                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    idSlovicka = rs.getInt(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            new pridatSlovicko(idSlovicka).execute();
        }
    }


}