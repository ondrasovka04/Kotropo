package cz.ucenislovicek.login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cz.ucenislovicek.R;
import cz.ucenislovicek.SharedPrefs;

public class ListSkol extends AppCompatActivity {
    private final List<myItem> cities = new ArrayList<>(), schools = new ArrayList<>();
    private final Context activityContext = this;
    boolean schoolsDisplayed = false;
    private MyAdapter adapter;
    private SearchView searchView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_schools);
        progressBar = findViewById(R.id.loading);
        new getCities(this).execute();
    }


    private void setUpRecyclerView(List<myItem> list) {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setBackgroundColor(Color.parseColor("#FFAAAAAA"));
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new MyAdapter(list);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        if (schoolsDisplayed) {
            setUpRecyclerView(cities);
            schoolsDisplayed = false;
        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.skola_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();

        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setClickable(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }


    static class myItem {
        private final String name;
        private final String url;

        public myItem(String name, String url) {
            this.name = name;
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> implements Filterable {
        private final List<myItem> itemList;
        private final List<myItem> itemListFull;
        private final Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<myItem> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(itemListFull);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (myItem item : itemListFull) {
                        String name = withoutDiacritics(item.getName()).toLowerCase();
                        String url = withoutDiacritics(item.getUrl()).toLowerCase();
                        if (name.contains(filterPattern) || url.contains(filterPattern) || name.replace(" ", "").contains(filterPattern) || url.replace(" ", "").contains(filterPattern)) {
                            filteredList.add(item);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredList;

                return results;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                itemList.clear();
                itemList.addAll((List) results.values);
                notifyDataSetChanged();
            }
        };

        MyAdapter(List<myItem> itemList) {
            this.itemList = itemList;
            itemListFull = new ArrayList<>(itemList);
        }

        public String withoutDiacritics(String value) {
            String abcWithDiacritics = "áÁčČďĎéÉěĚíÍňŇóÓřŘšŠťŤúÚůŮýÝžŽ";
            String abcWithoutDiacritics = "aAcCdDeEeEiInNoOrRsStTuUuUyYzZ";
            char[] valueArray = value.toCharArray();
            char[] diacriticsArray = abcWithDiacritics.toCharArray();
            char[] noDiacriticsArray = abcWithoutDiacritics.toCharArray();
            for (int i = 0; i < valueArray.length; i++) {
                for (int j = 0; j < diacriticsArray.length; j++) {
                    if (valueArray[i] == diacriticsArray[j]) {
                        valueArray[i] = noDiacriticsArray[j];
                    }
                }
            }
            return String.valueOf(valueArray);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.school_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            myItem currentItem = itemList.get(position);

            holder.name.setText(currentItem.getName());
            if (currentItem.getUrl().equals("")) {
                holder.url.setVisibility(View.GONE);
            }
            holder.url.setText(currentItem.getUrl());
        }

        @Override
        public int getItemCount() {
            return itemList.size();
        }

        @Override
        public Filter getFilter() {
            return filter;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView name;
            TextView url;

            ViewHolder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.hundredName);
                url = itemView.findViewById(R.id.url);

                itemView.setOnClickListener(v -> {
                    if (schoolsDisplayed) {
                        SharedPrefs.setString(activityContext, SharedPrefs.URL, url.getText().toString());
                        startActivity(new Intent(activityContext, Login.class));
                        finish();
                    } else {
                        if (schools.isEmpty()) {
                            progressBar.setVisibility(View.VISIBLE);
                            searchView.setClickable(false);
                            new getSchools(itemView.getContext(), name.getText().toString()).execute();
                        } else {
                            setUpRecyclerView(schools);
                        }
                    }
                });
            }
        }
    }

    public class getCities extends AsyncTask<Void, Void, Void> {
        @SuppressLint("StaticFieldLeak")
        public Context context;

        public getCities(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                URL url = new URL("https://sluzby.bakalari.cz/api/v1/municipality");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Accept", "application/json");

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                JSONArray myArray = new JSONArray(content.toString());
                cities.clear();
                for (int i = 1; i < myArray.length(); i++) {
                    cities.add(new myItem(myArray.getJSONObject(i).getString("name"), ""));
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            schoolsDisplayed = false;
            setUpRecyclerView(cities);
            searchView.setClickable(true);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public class getSchools extends AsyncTask<Void, Void, Void> {
        @SuppressLint("StaticFieldLeak")
        public Context context;
        public String city;

        public getSchools(Context context, String city) {
            this.context = context;
            this.city = city;
        }

        public String URLEncode(String value) {
            if (value.contains(".")) {
                value = value.substring(0, value.indexOf("."));
            }
            return value.replace("ě", "%C4%9B").replace("š", "%C5%A1").replace("č", "%C4%8D").replace("ř", "%C5%99").replace("ž", "%C5%BE").replace("ý", "%C3%BD").replace("á", "%C3%A1").replace("í", "%C3%AD").replace("é", "%C3%A9").replace("ď", "%C4%8F").replace("ť", "%C5%A5").replace("ň", "%C5%88").replace("ú", "%C3%BA").replace("ů", "%C5%AF").replace("Š", "%C5%A0").replace("Č", "%C4%8C").replace("Ř", "%C5%98").replace("Ž", "%C5%BD").replace("Á", "%C3%81").replace("Ú", "%C3%9A").replace(" ", "%20").replace("ü", "%C3%BC");
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                URL url = new URL("https://sluzby.bakalari.cz/api/v1/municipality/" + URLEncode(city));
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Accept", "application/json");

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                JSONArray myArray = new JSONObject(content.toString()).getJSONArray("schools");
                for (int i = 0; i < myArray.length(); i++) {
                    schools.add(new myItem((String) myArray.getJSONObject(i).get("name"), (String) myArray.getJSONObject(i).get("schoolUrl")));
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            progressBar.setVisibility(View.INVISIBLE);
            searchView.setClickable(true);
            schoolsDisplayed = true;
            setUpRecyclerView(schools);
            searchView.setQuery("", true);
        }
    }
}