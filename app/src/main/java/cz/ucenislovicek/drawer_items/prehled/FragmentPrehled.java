package cz.ucenislovicek.drawer_items.prehled;

import android.annotation.SuppressLint;
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
import java.util.List;

import cz.ucenislovicek.R;
import cz.ucenislovicek.databinding.FragmentPrehledBinding;


public class FragmentPrehled extends Fragment {

    TextView tv_jazyk, tv_badge;
    Spinner jazyk, badge;
    Button zobraz;
    List<StateVO> listVOs = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentPrehledBinding binding = FragmentPrehledBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        tv_jazyk = binding.textView10;
        tv_badge = binding.textView11;
        jazyk = binding.jazyk2;
        badge = binding.badge2;
        zobraz = binding.button5;

        zobraz.setOnClickListener(view -> {
            ArrayList<String> finalBadges = new ArrayList<>();
            for (StateVO a : listVOs) {
                if (a.isSelected()) {
                    finalBadges.add(a.getTitle());
                }
            }
            if (!finalBadges.isEmpty()) {
                new FragmentPrehled.getSlovicka(finalBadges).execute();
            } else {
                Toast.makeText(getContext(), "Musíte vybrat alespoň jeden badge", Toast.LENGTH_SHORT).show();
            }
        });

        jazyk.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                new FragmentPrehled.loadBadges().execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        new FragmentPrehled.getJazyky().execute();
        return root;
    }

    public static class StateVO {
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

    @SuppressLint("StaticFieldLeak")
    private class getSlovicka extends AsyncTask<Void, Void, Void> {

        List<String> badges;
        ArrayList<String> cesky = new ArrayList<>();
        ArrayList<String> cizi = new ArrayList<>();
        ArrayList<String> badge = new ArrayList<>();

        public getSlovicka(List<String> badges) {
            this.badges = badges;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost/dk-313_uceniSlovicek?serverTimezone=Europe/Prague", "dk-313", "GyArab14");
                StringBuilder s = new StringBuilder("select ceskeSlovicko, ciziSlovicko, badge from slovicka where jazyk=? and ");
                for (int i = 0; i < badges.size(); i++) {
                    if (i == badges.size() - 1) {
                        s.append("badge=\"").append(badges.get(i)).append("\" order by badge");
                    } else {
                        s.append("badge=\"").append(badges.get(i)).append("\" or ");
                    }
                }
                PreparedStatement st = con.prepareStatement(s.toString());
                st.setString(1, (String) jazyk.getSelectedItem());
                ResultSet rs = st.executeQuery();
                while (rs.next()) {
                    cesky.add(rs.getString("ceskeSlovicko"));
                    cizi.add(rs.getString("ciziSlovicko"));
                    badge.add(rs.getString("badge"));
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
            System.out.println("ahoj");
            Intent i = new Intent(getContext(), Prehled.class);
            i.putStringArrayListExtra("cesky", cesky);
            i.putStringArrayListExtra("cizi", cizi);
            i.putStringArrayListExtra("badge", badge);
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
            new FragmentPrehled.loadBadges().execute();
        }
    }

    @SuppressLint("StaticFieldLeak")
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

    public class MyAdapter extends ArrayAdapter<StateVO> {
        private final Context mContext;

        public MyAdapter(Context context, int resource) {
            super(context, resource, listVOs);
            this.mContext = context;
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            return getCustomView(position, convertView);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView);
        }

        @SuppressLint("InflateParams")
        public View getCustomView(final int position, View convertView) {

            final ViewHolder holder;
            if (convertView == null) {
                LayoutInflater layoutInflator = LayoutInflater.from(mContext);
                convertView = layoutInflator.inflate(R.layout.spinner_checkbox_item, null);
                holder = new ViewHolder();
                holder.mTextView = convertView.findViewById(R.id.text);
                holder.mCheckBox = convertView.findViewById(R.id.checkbox);
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

            holder.mCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> listVOs.get(position).setSelected(isChecked));

            return convertView;
        }

        private class ViewHolder {
            private TextView mTextView;
            private CheckBox mCheckBox;
        }
    }
}

