package com.example.internet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.internet.databinding.ActivityMainBinding;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static String TAG = MainActivity.class.getSimpleName();

    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mBinding.labelUrlToDownload.setText(getResources().getText(R.string.url_to_download)
                                    + " " + getResources().getText(R.string.url));
        mBinding.buttonDownload.setOnClickListener(this::connect);
    }

    private void connect(View view) {
        String path = mBinding.textPath.getText().toString();
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()) {
            new Thread(() -> {
                String response = downloadUrl(getResources().getString(R.string.url) + path);
                if(response != null) {
                    mBinding.textResult.setText(cleanText(response));
                } else {
                    mBinding.textResult.setText("Ocurrió un error durante la conexión");
                }

            }).start();
        } else {
            Toast.makeText(this, "No hay conexiones de red habilitadas", Toast.LENGTH_SHORT).show();
        }
    }

    private String downloadUrl(String urlCliente) {
        URL url;
        HttpURLConnection conn;
        String contentAsString = null;
        try {
            url = new URL(urlCliente);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Conetar
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(TAG, "Respuesta: " + response);
            try(InputStream is = conn.getInputStream()) {
                contentAsString = readIt(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentAsString;
    }

    private String readIt(InputStream stream) throws IOException {
        Reader reader = new InputStreamReader(stream, "UTF-8");
        StringBuilder sb = new StringBuilder();
        int k;
        while((k = reader.read()) != -1)
            sb.append((char)k);
        return sb.toString();
    }

    private String cleanText(String dom) {
        int initPos = dom.indexOf("articleContent");
        initPos = dom.indexOf(">", initPos) + 1;
        int endPos = dom.indexOf("</div>", initPos);
        return dom.substring(initPos, endPos).replaceAll("<[^>]*>", "");
    }

}