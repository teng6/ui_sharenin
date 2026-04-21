package com.example.shareninsulares;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shareninsulares.model.AuthResponse;
import com.example.shareninsulares.model.RegisterRequest;
import com.example.shareninsulares.network.ApiClient;
import com.example.shareninsulares.network.ApiService;
import com.example.shareninsulares.network.SessionManager;

import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterForm extends AppCompatActivity {

    // Your XML TextInputEditText fields have no android:id set.
    // We need to add IDs to them. See the comment below for instructions.
    // For now, we reference them by tag after adding IDs.

    private AutoCompleteTextView campusDropdown;
    private Button btnRegister;
    private SessionManager sessionManager;

    // These will work once you add IDs to the TextInputEditText views in the XML
    // (see instructions at bottom of this file)
    private TextInputEditText etFullName, etStudentId, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_form);

        sessionManager = new SessionManager(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // These IDs exist in your XML
        campusDropdown = findViewById(R.id.campus_dropdown);
        btnRegister    = findViewById(R.id.Registerbtn);

        // These IDs need to be added to your XML (see instructions below)
        etFullName  = findViewById(R.id.etFullName);
        etStudentId = findViewById(R.id.etStudentId);
        etPassword  = findViewById(R.id.etPassword);

        String[] campusOptions = {"Main Campus", "Balanga Campus"};
        campusDropdown.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, campusOptions));

        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String fullName  = etFullName  != null ? etFullName.getText().toString().trim()  : "";
        String studentId = etStudentId != null ? etStudentId.getText().toString().trim() : "";
        String password  = etPassword  != null ? etPassword.getText().toString().trim()  : "";
        String campus    = campusDropdown.getText().toString().trim();

        // Email is auto-generated from studentId for BPSU pattern,
        // or you can add an email field. For now we use studentId@bpsu.edu.ph
        String email = studentId + "@bpsu.edu.ph";

        if (fullName.isEmpty() || studentId.isEmpty() || password.isEmpty() || campus.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);

        ApiService api = ApiClient.getPublicClient().create(ApiService.class);
        api.register(new RegisterRequest(studentId, email, fullName, password, campus))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        btnRegister.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null) {
                            AuthResponse auth = response.body();
                            sessionManager.saveSession(
                                    auth.token, auth.id, auth.studentId,
                                    auth.email, auth.fullName, auth.campus, auth.role
                            );
                            Toast.makeText(RegisterForm.this,
                                    "Registered successfully!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterForm.this, Dashboard.class));
                            finish();
                        } else {
                            Toast.makeText(RegisterForm.this,
                                    "Registration failed. Check your details.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        btnRegister.setEnabled(true);
                        Toast.makeText(RegisterForm.this,
                                "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

/*
 ══════════════════════════════════════════════════════════════
 ACTION REQUIRED — Add these IDs to activity_register_form.xml
 ══════════════════════════════════════════════════════════════

 Find the TextInputEditText for "Enter Full Name" and add:
     android:id="@+id/etFullName"

 Find the TextInputEditText for "e.g: 24-00000" and add:
     android:id="@+id/etStudentId"

 Find the TextInputEditText for password and add:
     android:id="@+id/etPassword"

 Your XML currently has NO password field — you should add one
 inside a TextInputLayout after the Program field:

     <TextView ... android:text="Password" ... />
     <com.google.android.material.textfield.TextInputLayout ...>
         <com.google.android.material.textfield.TextInputEditText
             android:id="@+id/etPassword"
             android:layout_width="match_parent"
             android:layout_height="50dp"
             android:hint="Enter Password"
             android:inputType="textPassword" />
     </com.google.android.material.textfield.TextInputLayout>
 ══════════════════════════════════════════════════════════════
*/
