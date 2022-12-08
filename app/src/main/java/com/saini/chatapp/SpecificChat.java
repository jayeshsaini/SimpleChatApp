package com.saini.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.hardware.camera2.CameraOfflineSession;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class SpecificChat extends AppCompatActivity {

    EditText getMessageET;
    ImageButton sendMessageButton;

    CardView sendMessageCV;
    androidx.appcompat.widget.Toolbar toolbarSpecificChat;
    ImageView iVSpecificUser;
    TextView nameSpecificUser;

    private String enteredMessage;
    Intent intent;
    String recieverName, senderName, recieverUid, senderUid;
    private FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    String senderRoom, recieverRoom;

    ImageButton backBtnSpecificChat;
    RecyclerView messageRV;

    String currentTime;
    Calendar calendar;
    SimpleDateFormat simpleDateFormat;

    MessagesAdapter messagesAdapter;
    ArrayList<Messages> messagesArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_chat);

        getMessageET = findViewById(R.id.getMessage);
        sendMessageCV = findViewById(R.id.CVSendMessage);
        sendMessageButton = findViewById(R.id.sendMessageIB);

        toolbarSpecificChat = findViewById(R.id.toolbarOfSpecificChat);
        nameSpecificUser = findViewById(R.id.nameSpecificUser);
        iVSpecificUser = findViewById(R.id.specificUserImageInImageView);
        backBtnSpecificChat = findViewById(R.id.backButtonOfSpecificChat);

        messagesArrayList = new ArrayList<>();
        messageRV = findViewById(R.id.RVofSpecificChat);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messageRV.setLayoutManager(linearLayoutManager);
        messagesAdapter = new MessagesAdapter(SpecificChat.this, messagesArrayList);
        messageRV.setAdapter(messagesAdapter);

        intent = getIntent();

        setSupportActionBar(toolbarSpecificChat);
        toolbarSpecificChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Toolbar is Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("hh:mm a");

        senderUid = firebaseAuth.getUid();
        recieverUid = getIntent().getStringExtra("receiveruid");
        recieverName = getIntent().getStringExtra("name");

        senderRoom = senderUid + recieverUid;
        recieverRoom = recieverUid + senderUid;



        DatabaseReference databaseReference = firebaseDatabase.getReference().child("chats").child(senderRoom).child(("messages"));
        messagesAdapter = new MessagesAdapter(SpecificChat.this, messagesArrayList);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesArrayList.clear();
                for(DataSnapshot snapshot1:snapshot.getChildren()){
                    Messages messages = snapshot1.getValue(Messages.class);
                    messagesArrayList.add(messages);
                }
                messagesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




        backBtnSpecificChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        nameSpecificUser.setText(recieverName);
        String uri = intent.getStringExtra("imageuri");
        if(uri.isEmpty()) {
            Toast.makeText(getApplicationContext(), "null is received", Toast.LENGTH_SHORT).show();
        }
        else {
            Picasso.get().load(uri).into(iVSpecificUser);
        }

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enteredMessage = getMessageET.getText().toString();
                if(enteredMessage.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter message first", Toast.LENGTH_SHORT).show();
                }
                else {
                    Date date = new Date();
                    currentTime = simpleDateFormat.format(calendar.getTime());
                    Messages messages = new Messages(enteredMessage, firebaseAuth.getUid(), date.getTime(), currentTime);

                    firebaseDatabase = FirebaseDatabase.getInstance();
                    firebaseDatabase.getReference().child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            firebaseDatabase.getReference()
                                    .child("chats")
                                    .child(recieverRoom)
                                    .child("messages")
                                    .push()
                                    .setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                }
                            });
                        }
                    });

                    getMessageET.setText(null);
                }
            }
        });

    }
    @Override
    public void onStart() {
        super.onStart();
        messagesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(messagesAdapter!=null)
        {
            messagesAdapter.notifyDataSetChanged();
        }
    }
}