package com.example.shareninsulares;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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

public class MyListings extends AppCompatActivity {

    private RecyclerView rvMyListings;
    private MyListingAdapter listingAdapter;
    private SessionManager sessionManager;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_listings);

        sessionManager = new SessionManager(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvMyListings = findViewById(R.id.rvMyListings);
        bottomNav = findViewById(R.id.bottom_navigation);

        // Set up RecyclerView
        rvMyListings.setLayoutManager(new LinearLayoutManager(this));
        listingAdapter = new MyListingAdapter(new ArrayList<>(), this::handleListingAction);
        rvMyListings.setAdapter(listingAdapter);

        setupBottomNav();
        loadMyListings();
    }

    private void loadMyListings() {
        ApiService api = ApiClient.getClient(sessionManager.getToken()).create(ApiService.class);
        api.getMyListings().enqueue(new Callback<List<ListingResponse>>() {
            @Override
            public void onResponse(Call<List<ListingResponse>> call, Response<List<ListingResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listingAdapter.updateListings(response.body());
                } else if (response.code() == 403) {
                    Toast.makeText(MyListings.this, "Account restricted.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(MyListings.this, "Failed to load listings", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ListingResponse>> call, Throwable t) {
                Toast.makeText(MyListings.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleListingAction(long listingId, String action) {
        ApiService api = ApiClient.getClient(sessionManager.getToken()).create(ApiService.class);
        
        if ("edit".equals(action)) {
            Intent intent = new Intent(this, EditListing.class);
            intent.putExtra("listing_id", listingId);
            startActivity(intent);
        } else if ("delete".equals(action)) {
            api.deleteListing(listingId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(MyListings.this, "Listing deleted", Toast.LENGTH_SHORT).show();
                        loadMyListings();
                    } else {
                        Toast.makeText(MyListings.this, "Failed to delete listing", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(MyListings.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else if ("view".equals(action)) {
            Intent intent = new Intent(this, ListingDetail.class);
            intent.putExtra("listing_id", listingId);
            startActivity(intent);
        }
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

    // Simple My Listing Adapter
    private static class MyListingAdapter extends RecyclerView.Adapter<MyListingAdapter.ViewHolder> {
        private List<ListingResponse> listings;
        private final ListingActionListener listener;

        interface ListingActionListener {
            void onAction(long listingId, String action);
        }

        MyListingAdapter(List<ListingResponse> listings, ListingActionListener listener) {
            this.listings = listings;
            this.listener = listener;
        }

        public void updateListings(List<ListingResponse> newListings) {
            this.listings = newListings;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_my_listing, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ListingResponse listing = listings.get(position);

            holder.tvTitle.setText(listing.title != null ? listing.title : "");
            holder.tvPrice.setText(listing.price != null ? "¥" + listing.price.toPlainString() : "Free");
            holder.tvStatus.setText(listing.status != null ? listing.status : "ACTIVE");
            holder.tvType.setText(listing.type != null ? listing.type : "");

            // Set status color
            if ("ACTIVE".equals(listing.status)) {
                holder.tvStatus.setTextColor(0xFF4CAF50); // Green
            } else if ("SOLD".equals(listing.status)) {
                holder.tvStatus.setTextColor(0xFFF44336); // Red
            } else if ("RESERVED".equals(listing.status)) {
                holder.tvStatus.setTextColor(0xFFFF9800); // Orange
            }

            holder.btnEdit.setOnClickListener(v -> listener.onAction(listing.id, "edit"));
            holder.btnDelete.setOnClickListener(v -> listener.onAction(listing.id, "delete"));
            holder.itemView.setOnClickListener(v -> listener.onAction(listing.id, "view"));
        }

        @Override
        public int getItemCount() {
            return listings != null ? listings.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvPrice, tvStatus, tvType;
            Button btnEdit, btnDelete;

            ViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvMyListingTitle);
                tvPrice = itemView.findViewById(R.id.tvMyListingPrice);
                tvStatus = itemView.findViewById(R.id.tvMyListingStatus);
                tvType = itemView.findViewById(R.id.tvMyListingType);
                btnEdit = itemView.findViewById(R.id.btnEditListing);
                btnDelete = itemView.findViewById(R.id.btnDeleteListing);
            }
        }
    }
}
