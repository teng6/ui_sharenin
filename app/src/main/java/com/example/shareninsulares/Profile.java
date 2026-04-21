package com.example.shareninsulares;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shareninsulares.model.UserResponse;
import com.example.shareninsulares.network.ApiClient;
import com.example.shareninsulares.network.ApiService;
import com.example.shareninsulares.network.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Profile extends AppCompatActivity {

    private TextView tvName, tvProgram, tvCampus, tvStudentId;
    private TextView tvListingCount, tvRatingScore, tvWalletBalance;
    private MaterialButton btnMyListing, btnRatings, btnWallet, btnLogout;
    private BottomNavigationView bottomNav;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvName          = findViewById(R.id.tvName);
        tvProgram       = findViewById(R.id.tvProgram);
        tvCampus        = findViewById(R.id.tvCampus);
        tvStudentId     = findViewById(R.id.tvStudentId);
        tvListingCount  = findViewById(R.id.tvListingCount);
        tvRatingScore   = findViewById(R.id.tvRatingScore);
        tvWalletBalance = findViewById(R.id.tvWalletBalance);

        btnMyListing = findViewById(R.id.MyListingButton);
        btnRatings   = findViewById(R.id.RatingsButton);
        btnWallet    = findViewById(R.id.WalletButton);
        btnLogout    = findViewById(R.id.btnLogout);
        bottomNav    = findViewById(R.id.bottom_navigation);

        btnMyListing.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyListings.class);
            startActivity(intent);
        });
        btnRatings.setOnClickListener(v ->
                Toast.makeText(this, "Ratings coming soon", Toast.LENGTH_SHORT).show());
        btnWallet.setOnClickListener(v ->
                Toast.makeText(this, "Wallet coming soon", Toast.LENGTH_SHORT).show());
        btnLogout.setOnClickListener(v -> logout());

        setupBottomNav();
        loadProfile();
    }

    private void loadProfile() {
        ApiService api = ApiClient.getClient(sessionManager.getToken()).create(ApiService.class);
        api.getMe().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();

                    if (tvName      != null) tvName.setText(user.fullName);
                    if (tvStudentId != null) tvStudentId.setText(user.studentId);
                    if (tvCampus    != null) tvCampus.setText(user.campus);
                    if (tvProgram   != null) tvProgram.setText(user.campus); // no program field in backend yet

                    // Stats card
                    if (tvWalletBalance != null)
                        tvWalletBalance.setText(String.valueOf(user.shareCoinsBalance));
                    if (tvRatingScore != null && user.reputationScore != null)
                        tvRatingScore.setText(user.reputationScore.toPlainString());
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(Profile.this,
                        "Could not load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_profile);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, Dashboard.class));
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
