package com.cs407.team15.redstone.utility;

import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class NotificationTool {
    private String TAG = getClass().toString();

    private FirebaseUser sender;
    private String postId;

    public NotificationTool (FirebaseUser sender, String postId) {
        this.sender = sender;
        this.postId = postId;
    }

    public void addNotification(String receiver, String text, boolean isPost){
        Log.e(TAG, "notification sent to " + receiver + "from " + postId);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(receiver);

        String key = reference.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", sender.getUid());
        hashMap.put("userEmail", sender.getEmail());
        hashMap.put("text", text);
        hashMap.put("notificationId", key); // will be the unique key in Table
        hashMap.put("postid", postId);
        hashMap.put("ispost", isPost);

        reference.child(key).setValue(hashMap);
    }


    public void deleteNotifications(final String key, String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if (snapshot.child("notificationId").getValue().equals(key)){
                        snapshot.getRef().removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
