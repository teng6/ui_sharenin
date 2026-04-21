package com.example.shareninsulares;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shareninsulares.network.SessionManager;

/**
 * Chat Activity
 *
 * Your backend does NOT have a chat REST API — chat should be implemented
 * directly with Firebase Firestore or Realtime Database on the Android side,
 * since your backend already has Firebase configured (FirebaseConfig.java).
 *
 * Suggested Firebase chat structure:
 *   chats/{chatId}/messages/{messageId}
 *     - senderId: long
 *     - senderName: String
 *     - text: String
 *     - timestamp: long
 *
 *   chatId = min(userId1, userId2) + "_" + max(userId1, userId2)
 *
 * To enable Firebase on Android, add to build.gradle:
 *   implementation("com.google.firebase:firebase-firestore:24.10.3")
 *   implementation("com.google.firebase:firebase-messaging:23.4.1")
 * And add google-services.json to your app/ folder.
 */
public class Chat extends AppCompatActivity {

    private SessionManager sessionManager;
    private long otherUserId = -1;
    private String otherUserName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        sessionManager = new SessionManager(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get other user info passed via Intent
        if (getIntent() != null) {
            otherUserId = getIntent().getLongExtra("otherUserId", -1);
            otherUserName = getIntent().getStringExtra("otherUserName");
        }

        if (otherUserId == -1) {
            Toast.makeText(this, "No user selected for chat", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // TODO: Initialize Firebase Firestore and load messages
        // String chatId = buildChatId(sessionManager.getUserId(), otherUserId);
        // listenToMessages(chatId);
    }

    /**
     * Builds a consistent chat room ID from two user IDs.
     * Always puts the smaller ID first so both users get the same room.
     */
    private String buildChatId(long userId1, long userId2) {
        return Math.min(userId1, userId2) + "_" + Math.max(userId1, userId2);
    }
}
