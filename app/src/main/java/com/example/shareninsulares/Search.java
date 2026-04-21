package com.example.shareninsulares;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shareninsulares.model.ListingResponse;
import com.example.shareninsulares.network.ApiClient;
import com.example.shareninsulares.network.ApiService;
import com.example.shareninsulares.network.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Search extends AppCompatActivity {

    private EditText searchbar;
    private BottomNavigationView bottomNav;
    private RecyclerView rvResults;
    private ListingAdapter searchAdapter;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        sessionManager = new SessionManager(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        searchbar  = findViewById(R.id.searchbar);
        bottomNav  = findViewById(R.id.bottom_navigation);
        rvResults  = findViewById(R.id.rvResults);

        // Set up RecyclerView
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        searchAdapter = new ListingAdapter(new ArrayList<>(), listing ->
                Toast.makeText(this, listing.title, Toast.LENGTH_SHORT).show()
        );
        rvResults.setAdapter(searchAdapter);

        setupBottomNav();
        loadListings(null, null);

        searchbar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                loadListings(null, query.isEmpty() ? null : query);
            }
        });
    }

    private void loadListings(String campus, String category) {
        ApiService api = ApiClient.getClient(sessionManager.getToken()).create(ApiService.class);
        api.getAllListings(campus, category).enqueue(new Callback<List<ListingResponse>>() {
            @Override
            public void onResponse(Call<List<ListingResponse>> call, Response<List<ListingResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    searchAdapter.updateListings(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<ListingResponse>> call, Throwable t) {
                Toast.makeText(Search.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, Dashboard.class));
                return true;
            } else if (id == R.id.nav_search) {
                return true;
            } else if (id == R.id.nav_post) {
                startActivity(new Intent(this, Post.class));
                return true;
            } else if (id == R.id.nav_chat) {
                startActivity(new Intent(this, Chat.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, Profile.class));
                return true;
            }
            return false;
        });
    }
}
