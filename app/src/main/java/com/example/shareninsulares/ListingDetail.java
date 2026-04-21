package com.example.shareninsulares;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.shareninsulares.model.ListingResponse;
import com.example.shareninsulares.network.ApiClient;
import com.example.shareninsulares.network.ApiService;
import com.example.shareninsulares.network.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListingDetail extends AppCompatActivity {

    private SessionManager sessionManager;
    private TextView tvTitle, tvDescription, tvPrice, tvType, tvCategory, tvCampus, tvSellerName;
    private ImageView ivImage;
    private Button btnBook, btnMessage;
    private ImageButton btnNotif, btnLogout;
    private BottomNavigationView bottomNav;
    private long listingId;
    private ListingResponse currentListing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_listing_detail);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        listingId = getIntent().getLongExtra("listing_id", -1);
        if (listingId == -1) {
            Toast.makeText(this, "Invalid listing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupBottomNav();
        loadListingDetails();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvDescription = findViewById(R.id.tvDetailDescription);
        tvPrice = findViewById(R.id.tvDetailPrice);
        tvType = findViewById(R.id.tvDetailType);
        tvCategory = findViewById(R.id.tvDetailCategory);
        tvCampus = findViewById(R.id.tvDetailCampus);
        tvSellerName = findViewById(R.id.tvDetailSellerName);
        ivImage = findViewById(R.id.ivDetailImage);
        btnBook = findViewById(R.id.btnBook);
        btnMessage = findViewById(R.id.btnMessage);
        btnNotif = findViewById(R.id.btnNotif);
        btnLogout = findViewById(R.id.btnLogout);
        bottomNav = findViewById(R.id.bottom_navigation);
    }

    private void loadListingDetails() {
        ApiService api = ApiClient.getClient(sessionManager.getToken()).create(ApiService.class);
        api.getListingById(listingId).enqueue(new Callback<ListingResponse>() {
            @Override
            public void onResponse(Call<ListingResponse> call, Response<ListingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentListing = response.body();
                    displayListingDetails();
                } else if (response.code() == 403) {
                    Toast.makeText(ListingDetail.this, "Account restricted.", Toast.LENGTH_LONG).show();
                    logout();
                } else {
                    Toast.makeText(ListingDetail.this, "Failed to load listing", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ListingResponse> call, Throwable t) {
                Toast.makeText(ListingDetail.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayListingDetails() {
        if (currentListing == null) return;

        tvTitle.setText(currentListing.title != null ? currentListing.title : "");
        tvDescription.setText(currentListing.description != null ? currentListing.description : "");
        tvPrice.setText(currentListing.price != null ? "¥" + currentListing.price.toPlainString() : "Free");
        tvType.setText(currentListing.type != null ? currentListing.type : "");
        tvCategory.setText(currentListing.category != null ? currentListing.category : "");
        tvCampus.setText(currentListing.campus != null ? currentListing.campus : "");
        tvSellerName.setText(currentListing.sellerName != null ? "by " + currentListing.sellerName : "");

        if (currentListing.imageUrl != null && !currentListing.imageUrl.isEmpty()) {
            Picasso.get().load(currentListing.imageUrl).into(ivImage);
        }

        btnBook.setOnClickListener(v -> bookListing());
        btnMessage.setOnClickListener(v -> messageSeller());
        btnLogout.setOnClickListener(v -> logout());
    }

    private void bookListing() {
        if (currentListing == null) return;

        ApiService api = ApiClient.getClient(sessionManager.getToken()).create(ApiService.class);
        
        // Create booking request
        com.example.shareninsulares.model.CreateBookingRequest request = 
            new com.example.shareninsulares.model.CreateBookingRequest();
        request.listingId = currentListing.id;
        
        api.createBooking(request).enqueue(new Callback<com.example.shareninsulares.model.BookingResponse>() {
            @Override
            public void onResponse(Call<com.example.shareninsulares.model.BookingResponse> call, 
                                 Response<com.example.shareninsulares.model.BookingResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ListingDetail.this, "Booking request sent!", Toast.LENGTH_SHORT).show();
                } else if (response.code() == 403) {
                    Toast.makeText(ListingDetail.this, "Account restricted.", Toast.LENGTH_LONG).show();
                    logout();
                } else {
                    Toast.makeText(ListingDetail.this, "Failed to book listing", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.example.shareninsulares.model.BookingResponse> call, Throwable t) {
                Toast.makeText(ListingDetail.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void messageSeller() {
        Intent intent = new Intent(this, Chat.class);
        intent.putExtra("seller_id", currentListing.sellerId);
        intent.putExtra("seller_name", currentListing.sellerName);
        startActivity(intent);
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_home);
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
