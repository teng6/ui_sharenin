package com.example.shareninsulares;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shareninsulares.model.ListingResponse;
import com.example.shareninsulares.model.UpdateListingRequest;
import com.example.shareninsulares.network.ApiClient;
import com.example.shareninsulares.network.ApiService;
import com.example.shareninsulares.network.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditListing extends AppCompatActivity {

    private TextInputEditText etTitle, etDescription, etPrice;
    private AutoCompleteTextView categoryDropdown, locationDropdown, typeDropdown;
    private Button btnUpdate, btnCancel;
    private BottomNavigationView bottomNav;
    private SessionManager sessionManager;
    private long listingId;
    private ListingResponse currentListing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_listing);

        sessionManager = new SessionManager(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etTitle = findViewById(R.id.etEditTitle);
        etDescription = findViewById(R.id.etEditDescription);
        etPrice = findViewById(R.id.etEditPrice);
        categoryDropdown = findViewById(R.id.etEditCategory);
        locationDropdown = findViewById(R.id.etEditLocation);
        typeDropdown = findViewById(R.id.etEditType);
        btnUpdate = findViewById(R.id.btnUpdateListing);
        btnCancel = findViewById(R.id.btnCancelEdit);
        bottomNav = findViewById(R.id.bottom_navigation);

        listingId = getIntent().getLongExtra("listing_id", -1);
        if (listingId == -1) {
            Toast.makeText(this, "Invalid listing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupDropdowns();
        setupBottomNav();
        loadListing();

        btnUpdate.setOnClickListener(v -> updateListing());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void setupDropdowns() {
        // Category dropdown
        List<String> categories = Arrays.asList(
                "School Supplies", "Books", "Electronics", "Clothing", 
                "Furniture", "Food", "Services", "Others"
        );
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, categories);
        categoryDropdown.setAdapter(categoryAdapter);

        // Location/Campus dropdown
        List<String> campuses = Arrays.asList(
                "Balanga", "Orani", "Samal", "Abucay", 
                "Pilar", "Bagac", "Morong", "Hermosa", "Limay"
        );
        ArrayAdapter<String> campusAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, campuses);
        locationDropdown.setAdapter(campusAdapter);

        // Type dropdown
        List<String> types = Arrays.asList("SELL", "RENT");
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, types);
        typeDropdown.setAdapter(typeAdapter);
    }

    private void loadListing() {
        ApiService api = ApiClient.getClient(sessionManager.getToken()).create(ApiService.class);
        api.getListingById(listingId).enqueue(new Callback<ListingResponse>() {
            @Override
            public void onResponse(Call<ListingResponse> call, Response<ListingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentListing = response.body();
                    populateFields();
                } else if (response.code() == 403) {
                    Toast.makeText(EditListing.this, "Account restricted.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(EditListing.this, "Failed to load listing", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ListingResponse> call, Throwable t) {
                Toast.makeText(EditListing.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateFields() {
        if (currentListing == null) return;

        etTitle.setText(currentListing.title != null ? currentListing.title : "");
        etDescription.setText(currentListing.description != null ? currentListing.description : "");
        etPrice.setText(currentListing.price != null ? currentListing.price.toString() : "");
        categoryDropdown.setText(currentListing.category != null ? currentListing.category : "", false);
        locationDropdown.setText(currentListing.campus != null ? currentListing.campus : "", false);
        typeDropdown.setText(currentListing.type != null ? currentListing.type : "", false);
    }

    private void updateListing() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String category = categoryDropdown.getText().toString().trim();
        String campus = locationDropdown.getText().toString().trim();
        String type = typeDropdown.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || priceStr.isEmpty() || 
            category.isEmpty() || campus.isEmpty() || type.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        BigDecimal price;
        try {
            price = new BigDecimal(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price", Toast.LENGTH_SHORT).show();
            return;
        }

        UpdateListingRequest request = new UpdateListingRequest();
        request.title = title;
        request.description = description;
        request.price = price;
        request.category = category;
        request.campus = campus;
        request.type = type;

        ApiService api = ApiClient.getClient(sessionManager.getToken()).create(ApiService.class);
        api.updateListing(listingId, request).enqueue(new Callback<ListingResponse>() {
            @Override
            public void onResponse(Call<ListingResponse> call, Response<ListingResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditListing.this, "Listing updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (response.code() == 403) {
                    Toast.makeText(EditListing.this, "Account restricted.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(EditListing.this, "Failed to update listing", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ListingResponse> call, Throwable t) {
                Toast.makeText(EditListing.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
}
