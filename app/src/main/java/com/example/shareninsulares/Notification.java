package com.example.shareninsulares;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import com.example.shareninsulares.model.BookingResponse;
import com.example.shareninsulares.network.ApiClient;
import com.example.shareninsulares.network.ApiService;
import com.example.shareninsulares.network.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Notification extends AppCompatActivity {

    private MaterialButton btnBack;
    private RecyclerView rvMyBookings, rvReceivedBookings;
    private TextView tvNoMyBookings, tvNoReceivedBookings;
    private BookingAdapter myBookingsAdapter, receivedBookingsAdapter;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);

        sessionManager = new SessionManager(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnBack              = findViewById(R.id.Backbtn);
        rvMyBookings         = findViewById(R.id.rvMyBookings);
        rvReceivedBookings   = findViewById(R.id.rvReceivedBookings);
        tvNoMyBookings       = findViewById(R.id.tvNoMyBookings);
        tvNoReceivedBookings = findViewById(R.id.tvNoReceivedBookings);

        btnBack.setOnClickListener(v -> finish());

        myBookingsAdapter = new BookingAdapter(new ArrayList<>(), false, this::handleBookingAction);
        receivedBookingsAdapter = new BookingAdapter(new ArrayList<>(), true, this::handleBookingAction);

        rvMyBookings.setLayoutManager(new LinearLayoutManager(this));
        rvMyBookings.setAdapter(myBookingsAdapter);
        rvMyBookings.setNestedScrollingEnabled(false);

        rvReceivedBookings.setLayoutManager(new LinearLayoutManager(this));
        rvReceivedBookings.setAdapter(receivedBookingsAdapter);
        rvReceivedBookings.setNestedScrollingEnabled(false);

        loadMyBookings();
        loadReceivedBookings();
    }

    private void loadMyBookings() {
        ApiService api = ApiClient.getClient(sessionManager.getToken()).create(ApiService.class);
        api.getMyBookings().enqueue(new Callback<List<BookingResponse>>() {
            @Override
            public void onResponse(Call<List<BookingResponse>> call, Response<List<BookingResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BookingResponse> bookings = response.body();
                    myBookingsAdapter.updateBookings(bookings);
                    if (tvNoMyBookings != null)
                        tvNoMyBookings.setVisibility(bookings.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<BookingResponse>> call, Throwable t) {
                Toast.makeText(Notification.this, "Failed to load bookings", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadReceivedBookings() {
        ApiService api = ApiClient.getClient(sessionManager.getToken()).create(ApiService.class);
        api.getReceivedBookings().enqueue(new Callback<List<BookingResponse>>() {
            @Override
            public void onResponse(Call<List<BookingResponse>> call, Response<List<BookingResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BookingResponse> received = response.body();
                    receivedBookingsAdapter.updateBookings(received);
                    if (tvNoReceivedBookings != null)
                        tvNoReceivedBookings.setVisibility(received.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<BookingResponse>> call, Throwable t) { }
        });
    }

    public void handleBookingAction(long bookingId, String action) {
        ApiService api = ApiClient.getClient(sessionManager.getToken()).create(ApiService.class);
        Call<BookingResponse> call;
        switch (action) {
            case "accept":   call = api.acceptBooking(bookingId);   break;
            case "reject":   call = api.rejectBooking(bookingId);   break;
            case "complete": call = api.completeBooking(bookingId); break;
            case "cancel":   call = api.cancelBooking(bookingId);   break;
            default: return;
        }
        call.enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(Call<BookingResponse> call, Response<BookingResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(Notification.this, "Done!", Toast.LENGTH_SHORT).show();
                    loadMyBookings();
                    loadReceivedBookings();
                }
            }
            @Override
            public void onFailure(Call<BookingResponse> call, Throwable t) { }
        });
    }

    // ─── Inline BookingAdapter ────────────────────────────────────────────────

    public interface BookingActionListener {
        void onAction(long bookingId, String action);
    }

    static class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {

        private List<BookingResponse> bookings;
        private final boolean isReceived; // true = show accept/reject, false = show cancel
        private final BookingActionListener listener;

        BookingAdapter(List<BookingResponse> bookings, boolean isReceived, BookingActionListener listener) {
            this.bookings   = bookings;
            this.isReceived = isReceived;
            this.listener   = listener;
        }

        void updateBookings(List<BookingResponse> newBookings) {
            this.bookings = newBookings;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_booking, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BookingResponse booking = bookings.get(position);

            holder.tvTitle.setText(booking.listingTitle != null ? booking.listingTitle : "");
            holder.tvStatus.setText(booking.status != null ? booking.status : "");
            holder.tvMessage.setText(booking.message != null ? booking.message : "");

            if (isReceived && "PENDING".equals(booking.status)) {
                holder.tvOtherParty.setText("From: " + (booking.buyerName != null ? booking.buyerName : ""));
                holder.btnPrimary.setVisibility(View.VISIBLE);
                holder.btnSecondary.setVisibility(View.VISIBLE);
                holder.btnPrimary.setText("Accept");
                holder.btnSecondary.setText("Reject");
                holder.btnPrimary.setOnClickListener(v -> listener.onAction(booking.id, "accept"));
                holder.btnSecondary.setOnClickListener(v -> listener.onAction(booking.id, "reject"));
            } else if (!isReceived && "PENDING".equals(booking.status)) {
                holder.tvOtherParty.setText("Seller: " + (booking.sellerName != null ? booking.sellerName : ""));
                holder.btnPrimary.setVisibility(View.VISIBLE);
                holder.btnSecondary.setVisibility(View.GONE);
                holder.btnPrimary.setText("Cancel");
                holder.btnPrimary.setOnClickListener(v -> listener.onAction(booking.id, "cancel"));
            } else if ("ACCEPTED".equals(booking.status) && !isReceived) {
                holder.tvOtherParty.setText("Seller: " + (booking.sellerName != null ? booking.sellerName : ""));
                holder.btnPrimary.setVisibility(View.VISIBLE);
                holder.btnSecondary.setVisibility(View.GONE);
                holder.btnPrimary.setText("Mark Complete");
                holder.btnPrimary.setOnClickListener(v -> listener.onAction(booking.id, "complete"));
            } else {
                holder.tvOtherParty.setText(isReceived
                        ? "From: " + (booking.buyerName != null ? booking.buyerName : "")
                        : "Seller: " + (booking.sellerName != null ? booking.sellerName : ""));
                holder.btnPrimary.setVisibility(View.GONE);
                holder.btnSecondary.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() { return bookings != null ? bookings.size() : 0; }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvStatus, tvMessage, tvOtherParty;
            MaterialButton btnPrimary, btnSecondary;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle      = itemView.findViewById(R.id.tvBookingTitle);
                tvStatus     = itemView.findViewById(R.id.tvBookingStatus);
                tvMessage    = itemView.findViewById(R.id.tvBookingMessage);
                tvOtherParty = itemView.findViewById(R.id.tvOtherParty);
                btnPrimary   = itemView.findViewById(R.id.btnBookingPrimary);
                btnSecondary = itemView.findViewById(R.id.btnBookingSecondary);
            }
        }
    }
}
