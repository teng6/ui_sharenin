package com.example.shareninsulares;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shareninsulares.network.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.EditText;
import android.widget.Button;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

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
    private BottomNavigationView bottomNav;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private Button btnSend;
    private String chatId;
    private long otherUserId = -1;
    private String otherUserName = "";
    private List<Map<String, Object>> messages;
    private ChatMessageAdapter messageAdapter;

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
            otherUserId = getIntent().getLongExtra("seller_id", -1);
            otherUserName = getIntent().getStringExtra("seller_name");
        }

        if (otherUserId == -1) {
            Toast.makeText(this, "No user selected for chat", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI components
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        bottomNav = findViewById(R.id.bottom_navigation);

        // Set up RecyclerView for messages
        messages = new ArrayList<>();
        messageAdapter = new ChatMessageAdapter(messages, sessionManager.getUserId());
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(messageAdapter);

        // Initialize chat with backend messaging
        loadMessages();
        
        // Set up send button
        btnSend.setOnClickListener(v -> sendMessage());
        
        setupBottomNav();
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_chat);
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
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, Profile.class));
                return true;
            }
            return false;
        });
    }

    private void loadMessages() {
        // For now, show a simple message that chat is coming soon
        Map<String, Object> welcomeMessage = new HashMap<>();
        welcomeMessage.put("senderId", -1L);
        welcomeMessage.put("senderName", "System");
        welcomeMessage.put("text", "Chat with " + otherUserName + " - Feature coming soon!");
        welcomeMessage.put("timestamp", System.currentTimeMillis());
        
        messages.add(welcomeMessage);
        messageAdapter.notifyDataSetChanged();
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        // For now, just show a toast that messaging is coming soon
        Toast.makeText(this, "Messaging feature coming soon!", Toast.LENGTH_SHORT).show();
        etMessage.setText("");
        
        // Add the message to the local list for demonstration
        Map<String, Object> message = new HashMap<>();
        message.put("senderId", sessionManager.getUserId());
        message.put("senderName", sessionManager.getFullName());
        message.put("text", text);
        message.put("timestamp", System.currentTimeMillis());
        
        messages.add(message);
        messageAdapter.notifyDataSetChanged();
        rvMessages.scrollToPosition(messages.size() - 1);
    }

    /**
     * Builds a consistent chat room ID from two user IDs.
     * Always puts the smaller ID first so both users get the same room.
     */
    private String buildChatId(long userId1, long userId2) {
        return Math.min(userId1, userId2) + "_" + Math.max(userId1, userId2);
    }

    // Simple Chat Message Adapter
    private static class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ViewHolder> {
        private List<Map<String, Object>> messages;
        private long currentUserId;

        ChatMessageAdapter(List<Map<String, Object>> messages, long currentUserId) {
            this.messages = messages;
            this.currentUserId = currentUserId;
        }

        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map<String, Object> message = messages.get(position);
            String text = (String) message.get("text");
            String senderName = (String) message.get("senderName");
            Long senderId = (Long) message.get("senderId");
            
            boolean isMyMessage = senderId != null && senderId == currentUserId;
            String displayText = isMyMessage ? "You: " + text : senderName + ": " + text;
            
            holder.textView.setText(displayText);
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            android.widget.TextView textView;
            ViewHolder(android.view.View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}
