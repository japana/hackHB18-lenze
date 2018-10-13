package io.omg.opticalmessageguide;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MessageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra("message");

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.messageTextView);
        textView.setText(message);

    }

    public void scanAgain(View view) {
//        Intent intent = new Intent(this, OMGActivity.class);
//        startActivity(intent);
        finish();
    }

}
