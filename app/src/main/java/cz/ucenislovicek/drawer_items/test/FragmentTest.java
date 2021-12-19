package cz.ucenislovicek.drawer_items.test;

import android.content.Context;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.ucenislovicek.R;
import cz.ucenislovicek.databinding.FragmentTestBinding;


public class FragmentTest extends Fragment {

    TextView tv_jazyk, tv_bagde;
    Spinner jazyk, badge;
    Button testovat;
    List<StateVO> listVOs = new ArrayList<>();
    HashMap<String, String> slovicka = new HashMap<>();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentTestBinding binding = FragmentTestBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        tv_jazyk = binding.textView2;
        tv_bagde = binding.textView;
        jazyk = binding.jazyk;
        badge = binding.badge;
        testovat = binding.button2;

        testovat.setOnClickListener(view -> {
            ArrayList<String> finalBadges = new ArrayList<>();
            for (StateVO a : listVOs) {
                if (a.isSelected()) {
                    finalBadges.add(a.getTitle());
                }
            }
            if (!finalBadges.isEmpty()) {
                new getSlovicka(finalBadges).execute();
            } else {
                Toast.makeText(getContext(), "Musíte vybrat alespoň jeden badge", Toast.LENGTH_SHORT).show();
            }
        });

        jazyk.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                new loadBadges().execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        new getJazyky().execute();
        return root;
    }


    private class getSlovicka extends AsyncTask<Void, Void, Void> {

        List<String> badges;

        public getSlovicka(List<String> badges) {
            this.badges = badges;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost/dk-313_uceniSlovicek?serverTimezone=Europe/Prague", "dk-313", "GyArab14");
                String s = "select ceskeSlovicko, ciziSlovicko from slovicka where jazyk=? and ";
                for (int i = 0; i < badges.size(); i++) {
                    if (i == badges.size() - 1) {
                        s += "badge=\"" + badges.get(i) + "\"";
                    } else {
                        s += "badge=\"" + badges.get(i) + "\" or ";
                    }
                }
                PreparedStatement st = con.prepareStatement(s);
                st.setString(1, (String) jazyk.getSelectedItem());
                ResultSet rs = st.executeQuery();
                while (rs.next()) {
                    slovicka.put(rs.getString("ciziSlovicko"), rs.getString("ceskeSlovicko"));
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

            List<Map.Entry<String, String>> entries = new ArrayList<>(slovicka.entrySet());
            Collections.shuffle(entries);
            HashMap<String, String> shuffeledslovicka = new HashMap<>();
            entries.forEach(entry -> {
                shuffeledslovicka.put(entry.getKey(), entry.getValue());
            });
            Intent i = new Intent(getContext(), Test.class);
            i.putExtra("mapa", shuffeledslovicka);
            startActivity(i);
        }
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
            new loadBadges().execute();
        }
    }

    private class loadBadges extends AsyncTask<Void, Void, Void> {

        List<String> badges = new ArrayList<>();

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost/dk-313_uceniSlovicek?serverTimezone=Europe/Prague", "dk-313", "GyArab14");
                PreparedStatement st = con.prepareStatement("select distinct badge from slovicka where jazyk=? order by badge");
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
            listVOs.clear();
            StateVO first = new StateVO();
            first.setTitle("Vyberte badge");
            first.setSelected(false);
            listVOs.add(first);

            for (String s : badges) {
                StateVO stateVO = new StateVO();
                stateVO.setTitle(s);
                stateVO.setSelected(false);
                listVOs.add(stateVO);
            }
            MyAdapter myAdapter = new MyAdapter(getContext(), 0);
            badge.setAdapter(myAdapter);
        }
    }

    public class StateVO {
        private String title;
        private boolean selected;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }

    public class MyAdapter extends ArrayAdapter<StateVO> {
        private Context mContext;
        private MyAdapter myAdapter;
        private boolean isFromView = false;

        public MyAdapter(Context context, int resource) {
            super(context, resource, listVOs);
            this.mContext = context;
            this.myAdapter = this;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;
            if (convertView == null) {
                LayoutInflater layoutInflator = LayoutInflater.from(mContext);
                convertView = layoutInflator.inflate(R.layout.spinner_checkbox_item, null);
                holder = new ViewHolder();
                holder.mTextView = (TextView) convertView.findViewById(R.id.text);
                holder.mCheckBox = (CheckBox) convertView.findViewById(R.id.checkbox);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.mTextView.setText(listVOs.get(position).getTitle());
            holder.mCheckBox.setChecked(false);

            if ((position == 0)) {
                holder.mCheckBox.setVisibility(View.INVISIBLE);
            } else {
                holder.mCheckBox.setVisibility(View.VISIBLE);
            }
            holder.mCheckBox.setTag(position);

            holder.mCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                listVOs.get(position).setSelected(isChecked);
            });

            return convertView;
        }

        private class ViewHolder {
            private TextView mTextView;
            private CheckBox mCheckBox;
        }
    }
}