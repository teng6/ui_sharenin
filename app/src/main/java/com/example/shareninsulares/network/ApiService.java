package com.example.shareninsulares.network;

import com.example.shareninsulares.model.AuthResponse;
import com.example.shareninsulares.model.BookingResponse;
import com.example.shareninsulares.model.CreateBookingRequest;
import com.example.shareninsulares.model.CreateListingRequest;
import com.example.shareninsulares.model.CreateRatingRequest;
import com.example.shareninsulares.model.ListingResponse;
import com.example.shareninsulares.model.LoginRequest;
import com.example.shareninsulares.model.RatingResponse;
import com.example.shareninsulares.model.RegisterRequest;
import com.example.shareninsulares.model.UpdateListingRequest;
import com.example.shareninsulares.model.UserResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Part;
import retrofit2.http.Query;
import okhttp3.MultipartBody;

public interface ApiService {

    // ─── AUTH ────────────────────────────────────────────────────────────────
    @POST("api/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    // ─── USER ────────────────────────────────────────────────────────────────
    @GET("api/users/me")
    Call<UserResponse> getMe();

    // ─── LISTINGS ────────────────────────────────────────────────────────────
    @GET("api/listings")
    Call<List<ListingResponse>> getAllListings(
            @Query("campus") String campus,
            @Query("category") String category
    );

    @GET("api/listings/my")
    Call<List<ListingResponse>> getMyListings();

    @GET("api/listings/{id}")
    Call<ListingResponse> getListingById(@Path("id") long id);

    @POST("api/listings")
    Call<ListingResponse> createListing(@Body CreateListingRequest request);

    @PUT("api/listings/{id}")
    Call<ListingResponse> updateListing(@Path("id") long id, @Body UpdateListingRequest request);

    @DELETE("api/listings/{id}")
    Call<Void> deleteListing(@Path("id") long id);

    // ─── BOOKINGS ────────────────────────────────────────────────────────────
    @POST("api/bookings")
    Call<BookingResponse> createBooking(@Body CreateBookingRequest request);

    @GET("api/bookings/my")
    Call<List<BookingResponse>> getMyBookings();

    @GET("api/bookings/received")
    Call<List<BookingResponse>> getReceivedBookings();

    @PUT("api/bookings/{id}/accept")
    Call<BookingResponse> acceptBooking(@Path("id") long id);

    @PUT("api/bookings/{id}/reject")
    Call<BookingResponse> rejectBooking(@Path("id") long id);

    @PUT("api/bookings/{id}/complete")
    Call<BookingResponse> completeBooking(@Path("id") long id);

    @PUT("api/bookings/{id}/cancel")
    Call<BookingResponse> cancelBooking(@Path("id") long id);

    // ─── RATINGS ─────────────────────────────────────────────────────────────
    @POST("api/ratings")
    Call<RatingResponse> createRating(@Body CreateRatingRequest request);

    // ─── UPLOAD ──────────────────────────────────────────────────────────────
    @Multipart
    @POST("api/upload")
    Call<Map<String, String>> uploadImage(@Part MultipartBody.Part image);

    @GET("api/ratings/user/{userId}")
    Call<List<RatingResponse>> getRatingsForUser(@Path("userId") long userId);
}
