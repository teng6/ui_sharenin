package com.example.shareninsulares;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shareninsulares.model.AuthResponse;
import com.example.shareninsulares.model.LoginRequest;
import com.example.shareninsulares.network.ApiClient;
import com.example.shareninsulares.network.ApiService;
import com.example.shareninsulares.network.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        // Auto-redirect if already logged in
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, Dashboard.class));
            finish();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Matches your XML IDs: email, password, loginbtn, registerbtn
        etEmail    = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        btnLogin   = findViewById(R.id.loginbtn);
        btnRegister = findViewById(R.id.registerbtn);

        btnLogin.setOnClickListener(v -> attemptLogin());
        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterForm.class)));
    }

    private void attemptLogin() {
        // NOTE: Your XML uses "Email address" hint but your backend logs in via studentId.
        // The user should type their Student ID into the email field,
        // OR you can rename the hint in your XML to "Student ID".
        String studentId = etEmail.getText().toString().trim();
        String password  = etPassword.getText().toString().trim();

        if (studentId.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);

        ApiService api = ApiClient.getPublicClient().create(ApiService.class);
        api.login(new LoginRequest(studentId, password)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                btnLogin.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();
                    sessionManager.saveSession(
                            auth.token, auth.id, auth.studentId,
                            auth.email, auth.fullName, auth.campus, auth.role
                    );
                    startActivity(new Intent(MainActivity.this, Dashboard.class));
                    finish();
                } else {
                    Toast.makeText(MainActivity.this,
                            "Invalid student ID or password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                Toast.makeText(MainActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
