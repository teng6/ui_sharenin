package com.example.shareninsulares;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shareninsulares.model.ListingResponse;

import java.util.List;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ViewHolder> {

    public interface OnListingClickListener {
        void onListingClick(ListingResponse listing);
    }

    private List<ListingResponse> listings;
    private final OnListingClickListener listener;

    public ListingAdapter(List<ListingResponse> listings, OnListingClickListener listener) {
        this.listings = listings;
        this.listener = listener;
    }

    public void updateListings(List<ListingResponse> newListings) {
        this.listings = newListings;
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
        holder.tvPrice.setText(listing.price != null ? "₱" + listing.price.toPlainString() : "Free");
        holder.tvType.setText(listing.type != null ? listing.type : "");
        holder.tvSellerName.setText(listing.sellerName != null ? "by " + listing.sellerName : "");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onListingClick(listing);
        });
    }

    @Override
    public int getItemCount() {
        return listings != null ? listings.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvPrice, tvType, tvSellerName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle       = itemView.findViewById(R.id.tvListingTitle);
            tvDescription = itemView.findViewById(R.id.tvListingDescription);
            tvPrice       = itemView.findViewById(R.id.tvListingPrice);
            tvType        = itemView.findViewById(R.id.tvListingType);
            tvSellerName  = itemView.findViewById(R.id.tvSellerName);
        }
    }
}
