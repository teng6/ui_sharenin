package com.example.shareninsulares;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shareninsulares.model.ListingResponse;

import java.util.List;
import java.util.ArrayList;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ViewHolder> {

    public interface OnListingClickListener {
        void onListingClick(ListingResponse listing);
    }

    private List<ListingResponse> listings;
    private final OnListingClickListener listener;
    private List<Long> bookedListingIds; // Track which listings user has booked

    public ListingAdapter(List<ListingResponse> listings, OnListingClickListener listener) {
        this.listings = listings;
        this.listener = listener;
        this.bookedListingIds = new ArrayList<>();
    }

    public void setBookedListings(List<Long> bookedIds) {
        this.bookedListingIds = bookedIds != null ? bookedIds : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void updateListings(List<ListingResponse> newListings) {
        // Filter to only show ACTIVE listings (backend should already do this, but double-check)
        List<ListingResponse> activeListings = new ArrayList<>();
        for (ListingResponse listing : newListings) {
            if ("ACTIVE".equals(listing.status)) {
                activeListings.add(listing);
            }
        }
        this.listings = activeListings;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_listing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ListingResponse listing = listings.get(position);

        holder.tvTitle.setText(listing.title != null ? listing.title : "");
        holder.tvDescription.setText(listing.description != null ? listing.description : "");
        holder.tvPrice.setText(listing.price != null ? "¥" + listing.price.toPlainString() : "Free");
        holder.tvType.setText(listing.type != null ? listing.type : "");
        holder.tvSellerName.setText(listing.sellerName != null ? "by " + listing.sellerName : "");

        // Set listing image if available
        if (listing.imageUrl != null && !listing.imageUrl.isEmpty()) {
            // Load image using Glide or similar library
            try {
                // For now, use a simple approach - you can integrate Glide later
                holder.ivListingImage.setVisibility(View.VISIBLE);
                // You can add image loading library here
            } catch (Exception e) {
                holder.ivListingImage.setVisibility(View.GONE);
            }
        } else {
            holder.ivListingImage.setVisibility(View.GONE);
        }

        // Check if user has already booked this listing
        boolean isBookedByUser = bookedListingIds.contains(listing.id);
        
        // Set status with color coding
        String status = listing.status != null ? listing.status : "ACTIVE";
        if (isBookedByUser) {
            holder.tvStatus.setText("BOOKED");
            holder.tvStatus.setBackgroundColor(0xFF9C27B0); // Purple for booked
        } else {
            holder.tvStatus.setText(status);
        }
        
        // Set status color based on status
        if (isBookedByUser) {
            holder.tvStatus.setBackgroundColor(0xFF9C27B0); // Purple for booked
        } else {
            switch (status) {
                case "ACTIVE":
                    holder.tvStatus.setBackgroundColor(0xFF4CAF50); // Green
                    break;
                case "RESERVED":
                    holder.tvStatus.setBackgroundColor(0xFFFF9800); // Orange
                    break;
                case "SOLD":
                    holder.tvStatus.setBackgroundColor(0xFFF44336); // Red
                    break;
                case "INACTIVE":
                    holder.tvStatus.setBackgroundColor(0xFF9E9E9E); // Gray
                    break;
                default:
                    holder.tvStatus.setBackgroundColor(0xFF4CAF50); // Default Green
                    break;
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onListingClick(listing);
        });
    }

    @Override
    public int getItemCount() {
        return listings != null ? listings.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvPrice, tvType, tvSellerName, tvStatus;
        ImageView ivListingImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle       = itemView.findViewById(R.id.tvListingTitle);
            tvDescription = itemView.findViewById(R.id.tvListingDescription);
            tvPrice       = itemView.findViewById(R.id.tvListingPrice);
            tvType        = itemView.findViewById(R.id.tvListingType);
            tvSellerName  = itemView.findViewById(R.id.tvSellerName);
            tvStatus      = itemView.findViewById(R.id.tvListingStatus);
            ivListingImage = itemView.findViewById(R.id.ivListingImage);
        }
    }
}
