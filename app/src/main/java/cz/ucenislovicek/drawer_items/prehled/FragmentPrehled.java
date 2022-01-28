package cz.ucenislovicek.drawer_items.prehled;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cz.ucenislovicek.R;
import cz.ucenislovicek.databinding.FragmentPrehledBinding;


public class FragmentPrehled extends Fragment {

    TextView tv_jazyk, tv_batch;
    Spinner jazyk;
    Button zobraz;
    List<String> childList = new ArrayList<>(), groupList = new ArrayList<>();
    Map<String, List<String>> mobileCollection = new LinkedHashMap<>();
    ExpandableListView expandableListView;
    MyExpandableListAdapter expandableListAdapter;

    @Override
    public void onResume() {
        super.onResume();
        new FragmentPrehled.getJazyky().execute();
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentPrehledBinding binding = FragmentPrehledBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        tv_jazyk = binding.textView10;
        tv_batch = binding.textView11;
        jazyk = binding.jazyk2;
        expandableListView = binding.elvMobiles;
        zobraz = binding.button5;

        jazyk.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                new loadBatches().execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        new FragmentPrehled.getJazyky().execute();
        return root;
    }

    @SuppressLint("StaticFieldLeak")
    private class getSlovicka extends AsyncTask<Void, Void, Void> {

        List<String> batches, stovky;
        ArrayList<String> cesky = new ArrayList<>();
        ArrayList<String> cizi = new ArrayList<>();
        ArrayList<String> batch = new ArrayList<>();

        public getSlovicka(List<String> batches, List<String> stovky) {
            this.batches = batches;
            this.stovky = stovky;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost/dk-313_uceniSlovicek?serverTimezone=Europe/Prague", "dk-313", "GyArab14");
                StringBuilder s = new StringBuilder("select ceskeSlovicko, ciziSlovicko, batch from slovicka where jazyk=? and ");
                for (int i = 0; i < batches.size(); i++) {
                    s.append("batch=\"").append(batches.get(i)).append("\" or ");
                }
                for (int i = 0; i < stovky.size(); i++) {
                    s.append("stovka=\"").append(stovky.get(i).charAt(0)).append("\" or ");
                }
                String sql = s.substring(0, s.length() - 4);
                sql += " order by stovka, batch";

                PreparedStatement st = con.prepareStatement(sql);
                st.setString(1, (String) jazyk.getSelectedItem());
                ResultSet rs = st.executeQuery();
                while (rs.next()) {
                    cesky.add(rs.getString("ceskeSlovicko"));
                    cizi.add(rs.getString("ciziSlovicko"));
                    batch.add(rs.getString("batch"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            Intent i = new Intent(getContext(), Prehled.class);
            i.putStringArrayListExtra("cesky", cesky);
            i.putStringArrayListExtra("cizi", cizi);
            i.putStringArrayListExtra("batch", batch);
            startActivity(i);
        }
    }

    @SuppressLint("StaticFieldLeak")
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
            new loadBatches().execute();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class loadBatches extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost/dk-313_uceniSlovicek?serverTimezone=Europe/Prague", "dk-313", "GyArab14");
                PreparedStatement st = con.prepareStatement("select distinct stovka, batch from slovicka where jazyk=? order by stovka");
                st.setString(1, (String) jazyk.getSelectedItem());
                ResultSet rs = st.executeQuery();

                childList = new ArrayList<>();
                mobileCollection = new LinkedHashMap<>();
                groupList = new ArrayList<>();
                int lastStovka = 1;
                groupList.add(lastStovka + ".stovka");
                while (rs.next()) {
                    int aktualStovka = rs.getInt("stovka");
                    if (lastStovka == aktualStovka) {
                        childList.add(rs.getString("batch"));
                    } else {
                        mobileCollection.put(lastStovka + ".stovka", childList);
                        lastStovka = aktualStovka;
                        groupList.add(aktualStovka + ".stovka");
                        childList = new ArrayList<>();
                        childList.add(rs.getString("batch"));
                    }
                }
                mobileCollection.put(lastStovka + ".stovka", childList);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            expandableListAdapter = new MyExpandableListAdapter(getContext(), groupList, mobileCollection);
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
            zobraz.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    List<String> batch = new ArrayList<>();
                    List<String> stovky = new ArrayList<>();
                    MyExpandableListAdapter.Group[] groupes = expandableListAdapter.getGroupes();
                    for (MyExpandableListAdapter.Group g : groupes) {
                        for (CheckBox cb : g.getChilds()) {
                            if (cb != null) {
                                if (cb.isChecked()) {
                                    batch.add((String) cb.getTag());
                                }
                            } else {
                                if (g.getGroupBox().isChecked()) {
                                    String s = (String) g.getGroupBox().getTag();
                                    stovky.add(s.split("@")[0]);
                                    break;
                                }
                            }
                        }
                    }
                    if(stovky.isEmpty() && batch.isEmpty()){
                        Toast.makeText(getContext(), "Musíte vybrat alespoň jednu stovku nebo batch.", Toast.LENGTH_LONG).show();
                    } else {
                        new getSlovicka(batch, stovky).execute();
                    }
                }
            });
        }
    }
}

