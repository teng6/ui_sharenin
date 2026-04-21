package com.example.shareninsulares;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

public class Dashboard extends AppCompatActivity {

    private SessionManager sessionManager;
    private LinearLayout buyCat, rentCat;
    private ImageButton btnNotif;
    private BottomNavigationView bottomNav;
    private RecyclerView rvListings;
    private ListingAdapter listingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        buyCat    = findViewById(R.id.buyCat);
        rentCat   = findViewById(R.id.rentCat);
        btnNotif  = findViewById(R.id.btnNotif);
        bottomNav = findViewById(R.id.bottom_navigation);
        rvListings = findViewById(R.id.rvListings);

        rvListings.setLayoutManager(new LinearLayoutManager(this));
        listingAdapter = new ListingAdapter(new ArrayList<>(), listing -> {
            Intent intent = new Intent(Dashboard.this, ListingDetail.class);
            intent.putExtra("listing_id", listing.id);
            startActivity(intent);
        });
        rvListings.setAdapter(listingAdapter);

        buyCat.setOnClickListener(v -> loadListings(null, "SELL"));
        rentCat.setOnClickListener(v -> loadListings(null, "RENT"));
        btnNotif.setOnClickListener(v ->
                startActivity(new Intent(this, Notification.class)));

        setupBottomNav();
        loadListings(null, null);
    }

    private void loadListings(String campus, String category) {
        ApiService api = ApiClient.getClient(sessionManager.getToken()).create(ApiService.class);
        api.getAllListings(campus, category).enqueue(new Callback<List<ListingResponse>>() {
            @Override
            public void onResponse(Call<List<ListingResponse>> call, Response<List<ListingResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listingAdapter.updateListings(response.body());
                } else if (response.code() == 403) {
                    Toast.makeText(Dashboard.this, "Account restricted.", Toast.LENGTH_LONG).show();
                    logout();
                }
            }

            @Override
            public void onFailure(Call<List<ListingResponse>> call, Throwable t) {
                Toast.makeText(Dashboard.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_search) {
                startActivity(new Intent(this, Search.class));
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

    private void logout() {
        sessionManager.clearSession();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
