package com.mini188.smackdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.mini188.smackdemo.XmppService.XmppConnectionService;
import com.mini188.smackdemo.adapter.MessageAdapter;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    public static final String JID = "JID";

    private ChatManager _chatMgr;
    private Chat _chat;
    private EditText _msgEdit;
    private ImageButton _sendButton;
    protected ListView _messagesView;
    private  List<Message> _msgList;
    private MessageAdapter _messageAdapter;
    private String _jid;

    private void addMessage(final Message message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (StringUtils.isNullOrEmpty(message.getBody()) == false) {
                    _msgList.add(message);
                    _messageAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        _chatMgr = XmppConnectionService.getInstance().getChatManager();
        _msgEdit = (EditText) findViewById(R.id.textinput);
        _sendButton = (ImageButton) findViewById(R.id.textSendButton);

        _sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText sendText = (EditText) findViewById(R.id.textinput);
                String msg = sendText.getText().toString();
                if (!msg.equals("")) {
                    Message message = new Message();
                    message.setTo(_jid);
                    message.setBody(msg);
                    message.setType(Message.Type.chat);
                    addMessage(message);
                    try {
                        _chat.sendMessage(message);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                    sendText.setText("");
                }
            }
        });

        Intent intent = getIntent();
        createChat(intent);

        _messagesView = (ListView) findViewById(R.id.messages_view);
        _messagesView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        _messagesView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);

        _msgList = new ArrayList<>();
        _messageAdapter = new MessageAdapter(this, _msgList);
        _messagesView.setAdapter(_messageAdapter);
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        createChat(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        _chat.close();
    }

    private void createChat(Intent intent) {
        _jid = intent.getStringExtra(JID);
        _chat = XmppConnectionService.getInstance().getChat(_jid);
        if (_chat == null) {
            _chat = _chatMgr.createChat(_jid);
        }

        XmppConnectionService.getInstance().getXmppConnection().addAsyncStanzaListener(new StanzaListener() {
            @Override
            public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                addMessage((Message) packet);
            }
        }, new StanzaFilter() {
            @Override
            public boolean accept(Stanza stanza) {
                return stanza.getFrom().contains(_jid)
                        && stanza.getTo().contains(XmppConnectionService.getInstance().getUserName());
            }
        });
    }
}
