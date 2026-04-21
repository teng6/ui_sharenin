package com.example.shareninsulares;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.activity.EdgeToEdge;

import com.example.shareninsulares.model.CreateListingRequest;
import com.example.shareninsulares.model.ListingResponse;
import com.example.shareninsulares.network.ApiClient;
import com.example.shareninsulares.network.ApiService;
import com.example.shareninsulares.network.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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
    private Uri selectedImageUri;

    // Image picker launcher
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post);

        sessionManager = new SessionManager(this);

        // Initialize image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            ivImagePreview.setImageURI(selectedImageUri);
                            ivImagePreview.setVisibility(View.VISIBLE);
                            selectedImagePath = selectedImageUri.toString();
                            Toast.makeText(this, "Image selected successfully!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

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
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void uploadImage(Uri imageUri, ImageUploadCallback callback) {
        try {
            // Check if we can read the image
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                callback.onFailure("Cannot read image file");
                return;
            }
            
            File tempFile = new File(getCacheDir(), "temp_image_" + System.currentTimeMillis() + ".jpg");
            
            OutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();

            // Create request body with proper media type
            MediaType mediaType = MediaType.parse("image/jpeg");
            RequestBody requestFile = RequestBody.create(tempFile, mediaType);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", tempFile.getName(), requestFile);

            // For now, use the actual image URI as the "URL"
            // In a real app, this would upload to a server and return a URL
            new Thread(() -> {
                try {
                    // Simulate upload delay
                    Thread.sleep(500);
                    
                    // Use the actual image URI as the "URL"
                    String imageUrl = imageUri.toString();
                    
                    // Clean up temp file
                    tempFile.delete();
                    
                    // Return success on main thread
                    runOnUiThread(() -> callback.onSuccess(imageUrl));
                    
                } catch (Exception e) {
                    tempFile.delete();
                    runOnUiThread(() -> callback.onFailure("Upload failed: " + e.getMessage()));
                }
            }).start();
        } catch (IOException e) {
            callback.onFailure("Error reading image: " + e.getMessage());
        }
    }

    private interface ImageUploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(String error);
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
            Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
            return;
        }

        btnPost.setEnabled(false);

        // Make variables effectively final for callback
        final String finalTitle = title;
        final String finalDescription = description;
        final BigDecimal finalPrice = price;
        final String finalType = type;
        final String finalCampus = campus;

        // If there's an image, upload it first
        if (selectedImageUri != null) {
            uploadImage(selectedImageUri, new ImageUploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    createListing(finalTitle, finalDescription, finalPrice, finalType, finalCampus, imageUrl);
                }

                @Override
                public void onFailure(String error) {
                    btnPost.setEnabled(true);
                    // Show error and offer to continue without image
                    Toast.makeText(Post.this, "Image upload failed: " + error, Toast.LENGTH_LONG).show();
                    // Continue with listing creation without image
                    createListing(finalTitle, finalDescription, finalPrice, finalType, finalCampus, null);
                }
            });
        } else {
            // Create listing without image
            createListing(finalTitle, finalDescription, finalPrice, finalType, finalCampus, null);
        }
    }

    private void createListing(String title, String description, BigDecimal price, String type, String campus, String imageUrl) {
        CreateListingRequest request = new CreateListingRequest(title, description, price, type, campus, imageUrl);
        ApiService api = ApiClient.getClient(sessionManager.getToken()).create(ApiService.class);
        api.createListing(request).enqueue(new Callback<ListingResponse>() {
            @Override
            public void onResponse(Call<ListingResponse> call, Response<ListingResponse> response) {
                btnPost.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(Post.this, "Listing created successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(Post.this, "Failed to create listing", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ListingResponse> call, Throwable t) {
                btnPost.setEnabled(true);
                Toast.makeText(Post.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
