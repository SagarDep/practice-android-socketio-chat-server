package com.insthync.practicechatapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {
    public static String id = "";
    ArrayList<ChatData> chatData = new ArrayList<ChatData>();
    MsgAdapter msgAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        final ListView msgContainer = (ListView)findViewById(R.id.msgContainer);
        final EditText msgText = (EditText)findViewById(R.id.msgText);
        final Button enterButton = (Button)findViewById(R.id.enterButton);

        msgContainer.setAdapter(msgAdapter = new MsgAdapter(chatData));
        msgAdapter.notifyDataSetChanged();

        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = msgText.getText().toString();
                sendMessage(msg);
            }
        });

        if (id == null || id.isEmpty())
        {
            finish();
            return;
        }
    }

    void sendMessage(String message) {
        
    }


    public class ChatData {
        public String messageId;
        public String roomId;
        public String userId;
        public String message;
    }

    public class MsgAdapter extends BaseAdapter {
        ArrayList<ChatData> chatData;
        public MsgAdapter(ArrayList<ChatData> chatData) {
            this.chatData = chatData;
        }

        @Override
        public int getCount() {
            return chatData.size();
        }

        @Override
        public Object getItem(int position) {
            return chatData.get(position);
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

            name.setText(chatData.get(position).userId);
            message.setText(chatData.get(position).message);
            return convertView;
        }
    }
}
