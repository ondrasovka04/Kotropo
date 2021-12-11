package cz.ucenislovicek.drawer_items.pridaniSlovicek;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

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


    Spinner jazyk, badge;
    EditText slovickoCizi, slovickoCesky;
    Button pridat, novyBadge;
    TextView tw1, tw2;
    ProgressBar pb;


    @SuppressLint("NewApi")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentPridaniSlovicekBinding binding = FragmentPridaniSlovicekBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        slovickoCesky = binding.slovickoCesky;
        slovickoCizi = binding.slovickoCizi;
        pridat = binding.pridatslovicko;
        novyBadge = binding.novyBadge;
        tw1 = binding.textView3;
        tw2 = binding.textView4;
        pb = binding.progressBar2;
        badge = binding.volbaBadge;
        jazyk = binding.volbaJazyku;


        jazyk.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                new loadBadges().execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        novyBadge.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

            final EditText et = new EditText(getContext());
            et.setHint("Název nového bagde:");
            builder.setView(et);

            builder.setCancelable(false).setPositiveButton("OK", (dialogInterface, i) -> {
                String newBadge = et.getText().toString();
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) badge.getAdapter();
                adapter.add(newBadge);
                adapter.sort(Comparator.naturalOrder());
                badge.setAdapter(adapter);
                badge.setSelection(adapter.getPosition(newBadge));
            });

            builder.create().show();
        });

        pridat.setOnClickListener(view -> new getIdSlovicka().execute());


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

    private class loadBadges extends AsyncTask<Void, Void, Void> {

        List<String> badges = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost/dk-313_uceniSlovicek?serverTimezone=Europe/Prague", "dk-313", "GyArab14");
                PreparedStatement st = con.prepareStatement("select distinct badge from slovicka where jazyk=?");
                st.setString(1, (String) jazyk.getSelectedItem());
                ResultSet rs = st.executeQuery();
                while (rs.next()) {
                    badges.add(rs.getString("badge"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, badges);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            badge.setAdapter(adapter);

            pb.setVisibility(View.INVISIBLE);
        }
    }

    private class pridatSlovicko extends AsyncTask<Void, Void, Void> {

        int idSlovicka;

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
                PreparedStatement st = con.prepareStatement("insert into slovicka (idSlovicka, jazyk, badge, ciziSlovicko, ceskeSlovicko) values (?, ?, ?, ?, ?)");
                st.setInt(1, idSlovicka);
                st.setString(2, (String) jazyk.getSelectedItem());
                st.setString(3, (String) badge.getSelectedItem());
                st.setString(4, slovickoCizi.getText().toString());
                st.setString(5, slovickoCesky.getText().toString());
                st.executeUpdate();
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