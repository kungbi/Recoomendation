package org.appspot.apprtc.board;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.appspot.apprtc.Connect_server;
import org.appspot.apprtc.R;
import org.appspot.apprtc.user.UserProfile;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by kippe_000 on 2017-10-07.
 */

public class Main extends Fragment {
    Context context;
    ListView listView;
    ArrayList<Data> arrayList_item;
    int NowPage = 1;
    ListViewAdapter listViewAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.board_main, container, false); 

        context = view.getContext();
        listView = (ListView)view.findViewById(R.id.listView);
        arrayList_item = new ArrayList<>();
        listViewAdapter = new ListViewAdapter(getContext());


        listView.setAdapter(listViewAdapter);



        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

       // 게시물 가져오기
        Get_post();
    }

    void Get_post(){

        Connect_server connect_server = new Connect_server();
        connect_server.SetUrl("http://tlsdndql27.vps.phps.kr/recommendation/community/GetPost.php");
        connect_server.AddParams("NowPage", String.valueOf(NowPage));
        BufferedReader bufferedReader = connect_server.Connect(false);

        String buffer = null;
        JSONObject jsonObject = null;
        Data data = null;
        try {
            while((buffer = bufferedReader.readLine()) != null){
                jsonObject = new JSONObject(buffer);
                data = new Data(jsonObject.getString("mail"), jsonObject.getString("name"), jsonObject.getInt("post_id"), jsonObject.getString("time"), jsonObject.getString("content"),
                        jsonObject.getInt("answer_c"), jsonObject.getInt("liked"));

                arrayList_item.add(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        listViewAdapter.notifyDataSetChanged();

        Get_ProfileImage();

    }


    void Get_ProfileImage(){
        final android.os.Handler handelr = new android.os.Handler();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                for(int i = 0; i < arrayList_item.size(); i++){
                    InputStream inputstream = null;
                    try {
                        inputstream = new Get_Profile_Image(arrayList_item.get(i).mail).execute().get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    arrayList_item.get(i).bitmap = BitmapFactory.decodeStream(inputstream);
                }

                handelr.post(new Runnable() {
                    @Override
                    public void run() {
                        listViewAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
        thread.start();
    }



    class ListViewAdapter extends BaseAdapter{
        Context context;
        Data data;

          ListViewAdapter(Context context){
              this.context = context;
          }

        @Override
        public int getCount() {
            return arrayList_item.size();
        }

        @Override
        public Object getItem(int position) {
            return arrayList_item.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.board_item,parent,false);
            }

            data = arrayList_item.get(position);

            // 프로필 사진 올리기
            de.hdodenhof.circleimageview.CircleImageView circleImageView = (de.hdodenhof.circleimageview.CircleImageView)convertView.findViewById(R.id.profile_image);
            circleImageView.setImageBitmap(data.bitmap);

            // 이름 올리기
            TextView name_T = (TextView)convertView.findViewById(R.id.name);
            name_T.setText(data.profile_name);

            // 제목 올리기
            TextView content = (TextView)convertView.findViewById(R.id.content);
            content.setText(data.content);

            // 종아요수 올리기
            TextView like_c = (TextView)convertView.findViewById(R.id.like_c);
            like_c.setText(data.like_c+"");

            // 답변수 올리기
            TextView answer_c = (TextView)convertView.findViewById(R.id.answer_c);
            answer_c.setText(data.answer_c+" Answer");


            circleImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 프로필 들어가기
                    Intent intent = new Intent(getContext(), UserProfile.class);
                    intent.putExtra("mail", data.mail);
                    startActivity(intent);
                }
            });


            return convertView;
        }
    }


    class Get_Profile_Image extends AsyncTask<Void, Void, InputStream>{
        String mail;
        InputStream inputStream;

        Get_Profile_Image(String mail){
            this.mail = mail;
        }

        @Override
        protected InputStream doInBackground(Void... voids) {
            try {
                URL url = new URL("http://tlsdndql27.vps.phps.kr/recommendation/upload/Profile_Image/"+mail+".jpg");
                inputStream = url.openStream();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return inputStream;
        }

        @Override
        protected void onPostExecute(InputStream inputStream) {
            super.onPostExecute(inputStream);
        }
    }

}
