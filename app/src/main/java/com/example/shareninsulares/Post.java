package com.example.shareninsulares;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shareninsulares.model.CreateListingRequest;
import com.example.shareninsulares.model.ListingResponse;
import com.example.shareninsulares.network.ApiClient;
import com.example.shareninsulares.network.ApiService;
import com.example.shareninsulares.network.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Post extends AppCompatActivity {

    private TextInputEditText etTitle, etDescription, etPrice;
    private AutoCompleteTextView categoryDropdown, locationDropdown;
    private TextInputEditText etType, etCategory, etCampus;
    private Button btnPost, btnUploadImage;
    private ImageView ivImagePreview;
    private BottomNavigationView bottomNav;
    private SessionManager sessionManager;
    private String selectedImagePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post);

        sessionManager = new SessionManager(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        categoryDropdown = findViewById(R.id.categoryDropdown);
        locationDropdown = findViewById(R.id.locationDropdown);
        btnPost          = findViewById(R.id.addProductbtn);
        btnUploadImage   = findViewById(R.id.btnUploadImage);
        ivImagePreview   = findViewById(R.id.ivImagePreview);
        bottomNav        = findViewById(R.id.bottom_navigation);
        etTitle          = findViewById(R.id.etTitle);
        etDescription    = findViewById(R.id.etDescription);
        etPrice          = findViewById(R.id.etPrice);

        // Listing type options
        String[] typeOptions = {"SELL", "RENT", "FREE"};
        categoryDropdown.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, typeOptions));

        // Campus options
        String[] campusOptions = {"Main Campus", "Balanga Campus"};
        locationDropdown.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, campusOptions));

        // Pre-fill campus from session
        String sessionCampus = sessionManager.getCampus();
        if (sessionCampus != null && !sessionCampus.isEmpty()) {
            locationDropdown.setText(sessionCampus, false);
        }

        btnPost.setOnClickListener(v -> submitListing());
        btnUploadImage.setOnClickListener(v -> selectImage());
        setupBottomNav();
    }

    private void selectImage() {
        // For now, show a toast - you can integrate image picker later
        Toast.makeText(this, "Image upload feature coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void submitListing() {
        String title       = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String priceStr    = etPrice.getText() != null ? etPrice.getText().toString().trim() : "";
        String type        = categoryDropdown.getText().toString().trim();
        String campus      = locationDropdown.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (type.isEmpty()) type = "SELL";
        if (campus.isEmpty()) campus = "Main Campus";

        BigDecimal price;
        try {
            price = new BigDecimal(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter a valid price", Toast.LENGTH_SHORT).show();
            return;
        }

        btnPost.setEnabled(false);

        ApiService api = ApiClient.getClient(sessionManager.getToken()).create(ApiService.class);
        api.createListing(new CreateListingRequest(title, description, price, "General", type, null))
                .enqueue(new Callback<ListingResponse>() {
                    @Override
                    public void onResponse(Call<ListingResponse> call, Response<ListingResponse> response) {
                        btnPost.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(Post.this, "Listing posted!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(Post.this, "Failed to post listing", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ListingResponse> call, Throwable t) {
                        btnPost.setEnabled(true);
                        Toast.makeText(Post.this,
                                "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_post);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, Dashboard.class));
                return true;
            } else if (id == R.id.nav_search) {
                startActivity(new Intent(this, Search.class));
                return true;
            } else if (id == R.id.nav_post) {
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
