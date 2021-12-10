package cz.ucenislovicek.BakalariAPI;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;

import cz.ucenislovicek.BakalariAPI.schoolsDatabase.SchoolsAdapter;
import cz.ucenislovicek.BakalariAPI.schoolsDatabase.SchoolsDatabase;
import cz.ucenislovicek.BakalariAPI.schoolsDatabase.SchoolsDatabaseAPI;
import cz.ucenislovicek.BakalariAPI.schoolsDatabase.SchoolsViewModel;
import cz.ucenislovicek.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class SchoolsListFragment extends Fragment {
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    SchoolsAdapter adapter = null;
    SchoolsViewModel viewModel = null;

    ProgressBar progressBar;
    TextView twInfo;
    EditText etSearch;
    TextView twError;
    ImageView ivError;

    OnItemClickListener listener = url -> {};

    RequestQueue requestQueue = null;
    SchoolsDatabase database = null;


    public SchoolsListFragment() {
        // Required empty public constructor
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_schools_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        twInfo = view.findViewById(R.id.textViewInfo);
        etSearch = view.findViewById(R.id.editTextSearch);
        twError = view.findViewById(R.id.textViewError);
        ivError = view.findViewById(R.id.imageViewError);

        recyclerView.setVisibility(View.GONE);
        twError.setVisibility(View.GONE);
        ivError.setVisibility(View.GONE);

        viewModel = ViewModelProviders.of(this).get(SchoolsViewModel.class);
        database = viewModel.init(getContext());

        etSearch.addTextChangedListener(new TextWatcher() {//<editor-fold desc="unused methods">
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            //</editor-fold>
            @Override
            public void afterTextChanged(Editable s) {
                if (viewModel != null){
                    viewModel.setQuery(s.toString());
                }
            }
        });

        requestQueue = SchoolsDatabaseAPI.getAllSchools(getContext(), successful -> {
            if (successful) {
                layoutManager = new LinearLayoutManager(getContext());
                recyclerView.setLayoutManager(layoutManager);

                adapter = new SchoolsAdapter(getContext(), listener);

                viewModel.getQueriedSchools().observe(getViewLifecycleOwner(), adapter::submitList);

                recyclerView.setAdapter(adapter);
                recyclerView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                twInfo.setVisibility(View.GONE);

                viewModel.setQuery(etSearch.getText().toString());
            }else {
                progressBar.setVisibility(View.GONE);
                twInfo.setVisibility(View.GONE);
                twError.setVisibility(View.VISIBLE);
                ivError.setVisibility(View.VISIBLE);
            }
        },database, progressBar);

        //automatically show keyboard
        etSearch.requestFocus();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (requestQueue != null){
            requestQueue.cancelAll(request -> true/*all requests*/);
        }
    }

    public interface OnItemClickListener{
        void onClick(String url);
    }
}
