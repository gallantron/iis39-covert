package at.tugraz.iis.group39.app1;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import at.tugraz.iis.group39.app1.at.tugraz.iis.group39.app1.testing.DummyChannelPipe;
import at.tugraz.iis.group39.covertstreams.RawCovertChannelInputStream;
import at.tugraz.iis.group39.covertstreams.RawCovertChannelOutputStream;

public class MainActivity extends AppCompatActivity
{
    void displayStatus(String text, int color)
    {
        TextView statusText = (TextView)findViewById(R.id.statusText);
        statusText.setText(text);
        statusText.setTextColor(color);
    }
    void displayStatus(String text) { displayStatus(text, Color.GRAY); }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        displayStatus("Awaiting user input");
    }

    public void transmitClicked(View view)
    {
        EditText input = (EditText)findViewById(R.id.editText);
        TextView output = (TextView)findViewById(R.id.textView);
        output.setText("");

        String text = input.getText().toString();
        Log.d("SENDING", text);

        DummyChannelPipe pipe = DummyChannelPipe.make();
        OutputStream writeStream = new RawCovertChannelOutputStream(pipe.entrance());
        InputStream readStream = new RawCovertChannelInputStream(pipe.exit());

        displayStatus("Reading...");
        try { writeStream.write(text.getBytes("UTF-16BE")); writeStream.close(); }
        catch (Exception e)
        {
            Log.d("WRITING", "Failed with exception", e);
            displayStatus("Write failed", Color.RED);
            return;
        }

        Log.d("STATUS", "write done, now reading...");
        displayStatus("Writing...");
        // is reading a string from stream really this annoying?
        ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        String result;
        try
        {
            while ((length = readStream.read(buffer)) != -1)
            {
                Log.d("READ", String.format("read %d bytes", length));
                resultStream.write(buffer, 0, length);
            }
            result = resultStream.toString("UTF-16BE");
        }
        catch (Exception e)
        {
            Log.d("READING", "Failed with exception", e);
            displayStatus("Read failed", Color.RED);
            return;
        }

        output.setText(result);
        displayStatus("Done", Color.GREEN);
    }
}
