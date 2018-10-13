package com.zeeibra.ibm.hermes;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.zeeibra.ibm.hermes.ModelClass.LastMessage;
import com.zeeibra.ibm.hermes.ModelClass.Messages;
import com.zeeibra.ibm.hermes.adapters.MessageAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Main3Activity extends AppCompatActivity {


    private EditText Contain_message;
    public String GroupChat,GroupID;
    public String GroupTitle;
    private String SenderId;
    private MessageAdapter mAdapter;
    private DatabaseReference mChat;
    private LinearLayoutManager mLinearLayoutmanager;
    private RecyclerView mMessageList;
    private final List<Messages> messagesList = new ArrayList();
    public String mm;
    private ImageButton msend,mimagesend;
    public ArrayList aib=new ArrayList<String>();
    private static final int RC_PHOTO_PICKER =  2;
    private OkHttpClient mClient;
    private ArrayList<String> arrayList;
    private String token;
    private boolean typingStarted=false;

    ///Testing//
    private Toolbar mChatToolbar;
    private TextView typingInd;
    private CircleImageView mProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        arrayList=new ArrayList<String>();
        SenderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        GroupChat = getIntent().getStringExtra("message");
        GroupTitle = getIntent().getStringExtra("name");
        GroupID = getIntent().getStringExtra("id");
        mm = GroupChat;
        msend = (ImageButton) findViewById(R.id.chat_send_btn);
        mimagesend=(ImageButton)findViewById(R.id.chat_add_btn);
        mChat = FirebaseDatabase.getInstance().getReference();
        //getSupportActionBar().setTitle(GroupTitle);
        final DatabaseReference mtoken=FirebaseDatabase.getInstance().getReference().child("Device_Registration_Token").child(mm);
        mClient = new OkHttpClient();
        final ArrayList a=new ArrayList();
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(Arrays.asList(a));


        //---------------------------Toolbar----------------------//

        mChatToolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);
        TextView mTitleView = (TextView) findViewById(R.id.custom_bar_title);
        typingInd = (TextView) findViewById(R.id.custom_bar_seen);
        mProfileImage = (CircleImageView) findViewById(R.id.custom_bar_image);
        mTitleView.setText(GroupTitle);
        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String a = mm + "mice";
                Intent ij = new Intent(Main3Activity.this, GroupUser.class);
                ij.putExtra("title", a);
                ij.putExtra("toolbar", GroupTitle);
                ij.putExtra("name", mm);
                ij.putExtra("id",GroupID);
                startActivity(ij);


            }
        });
        //---------------------------Toolbar  End----------------------//
        Contain_message = (EditText) Main3Activity.this.findViewById(R.id.chat_message_view);
        Contain_message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (!TextUtils.isEmpty(charSequence)){
                    DatabaseReference mref=FirebaseDatabase.getInstance().getReference();
                    HashMap h=new HashMap();
                    h.put("typing","true");
                    h.put("typer name",SenderId);
                    mref.child("Typing Indicator").child(GroupChat).setValue(h);

                }
                else {
                    DatabaseReference mref=FirebaseDatabase.getInstance().getReference();
                    HashMap h=new HashMap();
                    h.put("typing","false");
                    mref.child("Typing Indicator").child(GroupChat).setValue(h);
                }

            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        showTypingIndicator();

        msend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String message = Contain_message.getText().toString();


                ConnectivityManager connec =
                        (ConnectivityManager)getSystemService(getBaseContext().CONNECTIVITY_SERVICE);

                // Check for network connections
                if ( connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                        connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED ) {

                    // if connected with internet
                    if (TextUtils.isEmpty(message.trim())) {
                        Toast.makeText(Main3Activity.this, "Not Send", Toast.LENGTH_SHORT).show();
                    } else {


                        HashMap ref = new HashMap();
                        ref.put("Main_message", message.trim());
                        Main3Activity.this.Contain_message.setText("");
                        ref.put("Sender", Main3Activity.this.SenderId);
                        ref.put("Time", ServerValue.TIMESTAMP);
                        mChat.child("groupchat").child(GroupChat).push().setValue(ref).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(Main3Activity.this, "Send", Toast.LENGTH_SHORT).show();
                            }
                        });

                        //----------------------------Send Notification------------------------//

                        mtoken.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()) {
                                    HashMap<String, Object> hashmap = (HashMap) childSnapShot.getValue();
                                    token= (String) hashmap.get("token");
                                    arrayList.add(token);
                                   // Toast.makeText(Main3Activity.this,token,Toast.LENGTH_SHORT).show();
                                    OkHttpClient mClient = new OkHttpClient();
                                    ;//add your user refresh tokens who are logged in with firebase.
                                    JSONArray jsonArray = new JSONArray();
                                    jsonArray.put(token);
                                    String email=FirebaseAuth.getInstance().getCurrentUser().getEmail();
                                    // Toast.makeText(Main3Activity.this,email,Toast.LENGTH_SHORT).show();
                                    sendMessage(jsonArray,GroupTitle,message.trim(),"Http:\\google.com","My Name is Vishal",arrayList,email,GroupTitle,GroupChat);

                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        sendit(message.trim());

                        }
                }
                else if (
                        connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED  ) {

                    Toast.makeText(Main3Activity.this, "It seems u don't have intenet connection", Toast.LENGTH_LONG).show();

                }




            }
        });

        mimagesend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);

            }
        });

        mAdapter = new MessageAdapter(Main3Activity.this,messagesList,GroupChat);
        mMessageList = (RecyclerView) findViewById(R.id.messages_list);
        mLinearLayoutmanager = new LinearLayoutManager(this);
        mMessageList.setHasFixedSize(true);
        mMessageList.setLayoutManager(this.mLinearLayoutmanager);
        mLinearLayoutmanager.setStackFromEnd(true);
        mMessageList.setAdapter(mAdapter);
        loadMessage();


    }

    private void sendit(final String message) {


        final DatabaseReference mChat=FirebaseDatabase.getInstance().getReference();

        mChat.child("groupchat").child(GroupChat).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                long size = dataSnapshot.getChildrenCount();

                LastMessage lm = new LastMessage();
                lm.setLastm(message.trim());
                lm.setTimestampCreated(ServerValue.TIMESTAMP);
                lm.setLastmessageSender(Main3Activity.this.SenderId);
                lm.setTotalmessage(size);
                FirebaseDatabase.getInstance().getReference().child("LastMessage").child(GroupChat).setValue(lm);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });








    }

    private void showTypingIndicator() {

        DatabaseReference mref=FirebaseDatabase.getInstance().getReference();
        mref.child("Typing Indicator").child(GroupChat).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    String a = (String) dataSnapshot.child("typing").getValue();


                    if (a.equals("true")) {
                        String senderId = (String) dataSnapshot.child("typer name").getValue();
                        if (senderId.equals(SenderId)) {
                            typingInd.setVisibility(View.GONE);

                        } else {
                            typingInd.setVisibility(View.VISIBLE);
                            typingInd.setText("Someone is typing...");
                        }
                    } else if (a.equals("false")) {
                        typingInd.setVisibility(View.GONE);

                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }



    private void loadMessage() {

        mChat.child("groupchat").child(GroupChat).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                messagesList.add(message);
                mMessageList.scrollToPosition(Main3Activity.this.mMessageList.getAdapter().getItemCount() - 1);
                mAdapter.notifyItemInserted(mMessageList.getAdapter().getItemCount() - 1);
                // mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }


    /*
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.info, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.infoo:
                String a = mm + "mice";

                Intent ij = new Intent(this, GroupUser.class);
                ij.putExtra("title", a);
                ij.putExtra("toolbar", GroupTitle);
                ij.putExtra("name", mm);
                startActivity(ij);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }*/


    //-----------------------Update LAstMessage----------------//


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==RC_PHOTO_PICKER && resultCode==RESULT_OK){
            Uri selectImageUri=data.getData();

            selectImageUri.getLastPathSegment();

            StorageReference photoref= FirebaseStorage.getInstance().getReference().child("Chat_image").child(selectImageUri.getLastPathSegment());
            photoref.putFile(selectImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUri=taskSnapshot.getDownloadUrl();
                    String ii=FirebaseAuth.getInstance().getCurrentUser().getUid();
                   // Toast.makeText(Main3Activity.this,"Successfull",Toast.LENGTH_SHORT).show();
                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("groupchat").child(GroupChat);
                    DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("LastMessage").child(GroupChat);
                   /* LastMessage lastMessage=new LastMessage();
                    lastMessage.setLastm("Photo");
                    lastMessage.setLastmessageSender(ii);
                    lastMessage.setTimestampCreated(ServerValue.TIMESTAMP);
                    reference.setValue(lastMessage);*/
                   sendit("Photo");
                    HashMap h=new HashMap();
                    h.put("Sender",ii);
                    h.put("imageURL",downloadUri.toString());
                    h.put("Time",ServerValue.TIMESTAMP);
                    ref.push().setValue(h);
                }
            });

        }
    }












    public void sendMessage(final JSONArray recipients, final String title, final String body, final String icon, final String message, final ArrayList mlist, final String email, final String grtitle, final String grmessage) {

        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    JSONObject root = new JSONObject();
                    JSONObject notification = new JSONObject();
                    notification.put("body", body);
                    notification.put("title", title);
                    notification.put("icon", icon);

                    JSONObject data = new JSONObject();
                    data.put("grouptitle",grtitle);
                    data.put("groupchat",grmessage);
                    root.put("message", message);
                    root.put("operation", "add");

                    root.put("notification", notification);
                    root.put("notification_key_name",email);
                    root.put("data", data);
                    root.put("registration_ids",recipients);

                    String result = postToFCM(root.toString());
                    //Log.d("Main Activity", "Result: " + result);
                    return result;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    JSONObject resultJson = new JSONObject(result);
                    int success, failure;
                    success = resultJson.getInt("success");
                    failure = resultJson.getInt("failure");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    String postToFCM(String bodyString) throws IOException {

        OkHttpClient mClient = new OkHttpClient();


        final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        RequestBody body = RequestBody.create(JSON, bodyString);
        Request request = new Request.Builder()
                .url(FCM_MESSAGE_URL)
                .post(body)
                .addHeader("Authorization", "key=" + getString(R.string.server_key))
                .build();
        Response response = mClient.newCall(request).execute();
        return response.body().string();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        DatabaseReference mref=FirebaseDatabase.getInstance().getReference();
        HashMap h=new HashMap();
        h.put("typing","false");
        mref.child("Typing Indicator").child(GroupChat).setValue(h);

    }
}
