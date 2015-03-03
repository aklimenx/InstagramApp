package com.example.alex.instagramapp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

    GridView mGridView;
    GridViewAdapter mAdapter;
    Bitmap arr[] = new Bitmap[20];

    public static final String SERVER_URL = "https://api.instagram.com/v1/tags/selfie/media/recent/?client_id=5f9365e9f1054aa991726d731c65aa02";
    private static final int DIALOG_KEY = 0;

    ProgressDialog mProgressDialog;
    protected Dialog onCreateDialog(int id){
        switch (id) {
            case DIALOG_KEY:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setMessage("Retrieving images...");
                mProgressDialog.setCancelable(false);
                return mProgressDialog;
        }
        return  null;

        }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid_view);
        Log.d("Entering to onCreate method", " onCreate" );
        DownloadImages dd = new DownloadImages();
        showDialog(DIALOG_KEY);
        dd.execute();
    }

    public void setImageDrawable(ArrayList<Bitmap> imageDrawable) {

        mGridView = (GridView)findViewById(R.id.gridView);
        mAdapter = new GridViewAdapter(imageDrawable);
        mGridView.setAdapter(mAdapter);
    }


    public class GridViewAdapter extends BaseAdapter {

        ArrayList<Bitmap> imageArray = new ArrayList<Bitmap>();

        public GridViewAdapter(ArrayList<Bitmap> imageDrawable) {
            this.imageArray = imageDrawable;
        }

        @Override
        public int getCount() {
            return imageArray.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater mInflater = getLayoutInflater();
            View v = mInflater.inflate(R.layout.activity_main,null);

            ImageView iv1 = (ImageView)v.findViewById(R.id.imageView);

            for(int i=0;i<arr.length;i++) {
                arr[i] = imageArray.get(i);
            }

            iv1.setImageBitmap(arr[position]);
            return v;
        }

    }

    public class DownloadImages extends AsyncTask<Void, Void, ArrayList<Bitmap>> {

        protected void onPreExecute(){
            mProgressDialog.show();
        }

        @Override
        protected ArrayList doInBackground(Void... params) {

            ArrayList<Bitmap> theList = new ArrayList<Bitmap>();
            Log.d("Entering to DownloadImages method", " DownloadImages" );

            try {

                DefaultHttpClient defaultClient = new DefaultHttpClient();
                HttpGet httpGetRequest = new HttpGet(SERVER_URL);

                HttpResponse httpResponse = defaultClient.execute(httpGetRequest);
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"));
                String json = reader.readLine();

                JSONObject jsonObject = new JSONObject(json);

                JSONArray jArray = jsonObject.getJSONArray("data");

                Log.d("JSON ARRAY is received: ", jsonObject.toString());

                for (int i=0; i < jArray.length(); i++) {

                    JSONObject oneObject = jArray.getJSONObject(i).getJSONObject("images").getJSONObject("thumbnail");
                    String theImageURL = oneObject.getString("url");
                    try {
                        URLConnection conn = (new URL(theImageURL)).openConnection();
                        InputStream is = conn.getInputStream();
                        BufferedInputStream bis = new BufferedInputStream(is);

                        ByteArrayBuffer baf = new ByteArrayBuffer(50);
                        int current = 0;
                        while ((current=bis.read()) != -1) {
                            baf.append((byte)current);
                        }

                        byte[] imageData = baf.toByteArray();
                        theList.add(BitmapFactory.decodeByteArray(imageData, 0, imageData.length));

                    } catch (Exception e) {
                        return null;
                    }


                    publishProgress();

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return theList;
        }

        protected void onProgressUpdate(Integer... values){
            mProgressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<Bitmap> result) {
            setImageDrawable(result);
            mProgressDialog.dismiss();
        }


    }


/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */
}
