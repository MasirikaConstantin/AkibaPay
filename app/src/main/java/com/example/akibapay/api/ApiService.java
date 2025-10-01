package com.example.akibapay.api;

import com.example.akibapay.models.ApiResponse;
import com.example.akibapay.models.Devices;
import com.example.akibapay.models.PaymentStatus;
import com.example.akibapay.models.Payments;
import com.example.akibapay.models.SessionStatus;
import com.example.akibapay.models.Sessions;
import com.example.akibapay.models.Users;
import com.example.akibapay.request.LoginRequest;
import com.example.akibapay.request.PaymentCallbackRequest;
import com.example.akibapay.request.PaymentRequest;
import com.example.akibapay.request.UpdatePasswordRequest;
import com.example.akibapay.request.UpdateTypeValueRequest;
import com.example.akibapay.request.UpdateUserRequest;

import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.UUID;

public interface ApiService {

    // ==================== AUTHENTICATION ====================

    @POST("/api/auth/login")
    Call<Void> login(@Body LoginRequest loginRequest);

    @POST("/api/auth/logout")
    Call<Void> logout(@Query("userId") String userId);

    // ==================== DEVICES ====================

    @GET("/api/devices")
    Call<List<Devices>> getAllDevices();

    @GET("/api/devices/{id}")
    Call<Devices> getDeviceById(@Path("id") UUID deviceId);

    @POST("/api/devices")
    Call<Devices> createDevice(@Body Devices device);

    @PUT("/api/devices/{id}")
    Call<Void> updateDevice(@Path("id") UUID deviceId, @Body Devices device);

    @DELETE("/api/devices/{id}")
    Call<Void> deleteDevice(@Path("id") UUID deviceId);

    @PATCH("/api/devices/{id}/bind-user")
    Call<Void> bindUserToDevice(@Path("id") UUID deviceId, @Body UUID userId);

    @PATCH("/api/devices/{id}/unbind-user")
    Call<Void> unbindUserFromDevice(@Path("id") UUID deviceId);

    // ==================== PAYMENTS ====================

    @GET("/api/payments/{id}")
    Call<Payments> getPaymentById(@Path("id") UUID paymentId);

    @PUT("/api/payments/{id}")
    Call<Void> updatePayment(@Path("id") UUID paymentId, @Body PaymentCallbackRequest callbackRequest);

    @DELETE("/api/payments/{id}")
    Call<Void> deletePayment(@Path("id") String paymentId);

    @GET("/api/payments/status/{status}")
    Call<List<Payments>> getPaymentsByStatus(@Path("status") PaymentStatus status);
    @GET("api/payments/recent")
    Call<List<Payments>> getRecentPayments(@Query("limit") int limit);
    @POST("/api/payments")
    Call<Payments> createPayment(@Body PaymentRequest paymentRequest);

    // ==================== SESSIONS ====================

    @GET("/api/sessions/{id}")
    Call<Sessions> getSessionById(@Path("id") UUID sessionId);

    @GET("/api/sessions/user/{userId}")
    Call<List<Sessions>> getSessionsByUser(@Path("userId") UUID userId);

    @POST("/api/sessions")
    Call<Sessions> createSession(@Body Sessions session);

    @PATCH("/api/sessions/{id}/status")
    Call<Void> updateSessionStatus(@Path("id") UUID sessionId, @Body SessionStatus status);

    // ==================== USERS ====================

    @GET("/api/users")
    Call<List<Users>> getAllUsers();

    @GET("/api/users/{id}")
    Call<Users> getUserById(@Path("id") UUID userId);

    @POST("/api/users")
    Call<Users> createUser(@Body Users user);

    @PUT("/api/users/{id}")
    Call<Void> updateUser(@Path("id") UUID userId, @Body UpdateUserRequest updateRequest);

    @PATCH("/api/users/{id}")
    Call<Void> partialUpdateUser(@Path("id") UUID userId, @Body UpdateTypeValueRequest updateRequest);

    @DELETE("/api/users/{id}")
    Call<Void> deleteUser(@Path("id") UUID userId);

    @GET("/api/users/phone/{phoneNumber}")
    Call<Users> getUserByPhone(@Path("phoneNumber") String phoneNumber);

    @PATCH("/api/users/{id}/password")
    Call<Void> updateUserPassword(@Path("id") UUID userId, @Body UpdatePasswordRequest passwordRequest);
}