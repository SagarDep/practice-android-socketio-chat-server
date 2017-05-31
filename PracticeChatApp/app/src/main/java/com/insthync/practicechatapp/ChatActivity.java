package com.insthync.practicechatapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChatActivity extends AppCompatActivity {
    public static String id = "";
    ArrayList<ChatData> chatDataList = new ArrayList<ChatData>();
    MsgAdapter msgAdapter;
    Socket socket;
    String roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        final ListView msgContainer = (ListView)findViewById(R.id.msgContainer);
        final EditText msgText = (EditText)findViewById(R.id.msgText);
        final Button enterButton = (Button)findViewById(R.id.enterButton);

        msgContainer.setAdapter(msgAdapter = new MsgAdapter(chatDataList));
        msgAdapter.notifyDataSetChanged();

        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = msgText.getText().toString();
                sendMessage(msg);
                msgText.setText("");
            }
        });

        if (id == null || id.isEmpty())
        {
            finish();
            return;
        }

        try {
            socket = IO.socket("http://192.168.1.40:3210");
        } catch (URISyntaxException ex) {
            Log.e("TAG", ex.getMessage());
            finish();
        }

        socket.on(Socket.EVENT_CONNECT, onConnect);
        socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        socket.on("login", onLogin);
        socket.on("joinRoom", onJoinRoom);
        socket.on("enterMessage", onEnterMessage);
        socket.on("deleteMessage", onDeleteMessage);
        socket.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (socket == null)
            return;

        socket.disconnect();

        socket.off(Socket.EVENT_CONNECT, onConnect);
        socket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        socket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        socket.off("login", onLogin);
        socket.off("joinRoom", onJoinRoom);
        socket.off("enterMessage", onEnterMessage);
        socket.off("deleteMessage", onDeleteMessage);
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("userId", id);
                obj.put("loginToken", "");  // Not required
                socket.emit("login", obj);
            } catch (JSONException ex) {
                Log.e("TAG", ex.getMessage());
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "Connected to chat server", Toast.LENGTH_LONG).show();

                }
            });
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "Disconnected from chat server", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "Chat server connection error", Toast.LENGTH_LONG).show();

                    finish();
                }
            });
        }
    };

    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("userId", id);
                obj.put("roomId", "global");
                socket.emit("joinRoom", obj);
            } catch (JSONException ex) {
                Log.e("TAG", ex.getMessage());
            }
        }
    };

    private Emitter.Listener onJoinRoom = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject obj = (JSONObject) args[0];
                roomId = obj.getString("roomId");
            } catch (JSONException ex) {
                Log.e("TAG", ex.getMessage());
            }
        }
    };

    private Emitter.Listener onEnterMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject obj = (JSONObject) args[0];
                String messageId = obj.getString("messageId");
                String roomId = obj.getString("roomId");
                String userId = obj.getString("userId");
                String message = obj.getString("message");

                final ChatData chatData = new ChatData();
                chatData.messageId = messageId;
                chatData.roomId = roomId;
                chatData.userId = userId;
                chatData.message = message;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chatDataList.add(chatData);
                        msgAdapter.notifyDataSetChanged();
                    }
                });
            } catch (JSONException ex) {
                Log.e("TAG", ex.getMessage());
            }
        }
    };

    private Emitter.Listener onDeleteMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

        }
    };

    void sendMessage(String message) {
        if (roomId == null || roomId.isEmpty())
            return;

        try {
            JSONObject obj = new JSONObject();
            obj.put("roomId", roomId);
            obj.put("message", message);
            socket.emit("enterMessage", obj);
        } catch (JSONException ex) {
            Log.e("TAG", ex.getMessage());
        }
    }


    public class ChatData {
        public String messageId;
        public String roomId;
        public String userId;
        public String message;
    }

    public class MsgAdapter extends BaseAdapter {
        ArrayList<ChatData> chatDataList;
        public MsgAdapter(ArrayList<ChatData> chatDataList) {
            this.chatDataList = chatDataList;
        }

        @Override
        public int getCount() {
            return chatDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return chatDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = getLayoutInflater();
            convertView = inflater.inflate(R.layout.list_item_chat_msg, parent, false);
            TextView name = (TextView)convertView.findViewById(R.id.msgName);
            TextView message = (TextView)convertView.findViewById(R.id.msgMessage);

            name.setText(chatDataList.get(position).userId);
            message.setText(chatDataList.get(position).message);
            return convertView;
        }
    }
}
