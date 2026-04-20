package com.example.shareninsulares;

import android.os.Bundle;

import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Post extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post);

        String[] campusOptions = {"Main Campus", "Balanga Campus"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                campusOptions
        );

        AutoCompleteTextView locationDropdown = findViewById(R.id.locationDropdown);
        if (locationDropdown != null) {
            locationDropdown.setAdapter(adapter);
        }

        String[] categoryOptions = {"Buy/Sell", "Rent"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, categoryOptions);

        AutoCompleteTextView categoryDropdown = findViewById(R.id.categoryDropdown);
        if (categoryDropdown != null) {
            categoryDropdown.setAdapter(categoryAdapter);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}