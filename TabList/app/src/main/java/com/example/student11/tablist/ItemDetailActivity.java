package com.example.student11.tablist;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by student11 on 2016/11/14.
 */
public class ItemDetailActivity extends Activity {
    private TextView mTitle;
    //public static long start = 0;
    String crlf = System.getProperty("line.separator");
    long start=0;
    long stop=0;
    long diff=0;
    String title;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_detail);

        Intent intent = getIntent();
        title = intent.getStringExtra("TITLE");
        mTitle = (TextView) findViewById(R.id.item_detail_title);
        mTitle.setText(title);

        MainActivity.ListViewFragment.mProgressDialog.dismiss();

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        try {

            String url = intent.getStringExtra("LINK");

            // HTMLのドキュメントを取得
            org.jsoup.nodes.Document document = Jsoup.connect(url).get();

            Elements links = null;
            links = document.getElementsByTag("a");

            for (org.jsoup.nodes.Element link : links) {
                String lin = link.attr("id");
                if(lin.equals("link")){
                    String url1 = link.attr("href");

                    org.jsoup.nodes.Document document1 = Jsoup.connect(url1).get();
                    Elements links1 = null;
                    links1 = document1.getElementsByTag("p");  		//タグ"p"の要素を格納
                    String detailtext1 = "";
                    for (org.jsoup.nodes.Element link1 : links1) {
                        String clas1 = link1.attr("class");			//属性"class"の属性値を取得
                        if (clas1.equals("ynDetailText") || clas1.equals("newsBody") || clas1.equals("yjS ymuiDate")) {            //取得した属性値が"ynDetailText"と一致
                            String str = link1.getElementsByAttribute("class").text();

                            String[] detailtext = str.split("。", 0);
                            for (String text : detailtext) {
                                detailtext1 += text + "。" + crlf + crlf;
                            }

                            TextView mDetailtext = (TextView) findViewById(R.id.item_detail_text);
                            mDetailtext.setText(detailtext1);
                        }
                    }
                    Elements links2 = null;
                    links2 = document1.getElementsByTag("div");  		//タグ"p"の要素を格納
                    String detailtext2 = "";
                    for (org.jsoup.nodes.Element link2 : links2) {
                        String clas2 = link2.attr("class");			//属性"class"の属性値を取得
                        if(clas2.equals("marB10 clearFix yjMt") || clas2.equals("newsParagraph piL") || clas2.equals("rics-column bd covered") || clas2.equals("mainBody")){
                            String str = link2.getElementsByAttribute("class").text();

                            String[] detailtext = str.split("。", 0);
                            for(String text: detailtext){
                                detailtext2 += text + "。" + crlf + crlf;
                            }

                            TextView mDetailtext = (TextView) findViewById(R.id.item_detail_text);
                            mDetailtext.setText(detailtext2);
                        }
                    }
                    Elements links3 = null;
                    links3 = document1.getElementsByTag("p");  		//タグ"p"の要素を格納
                    for (org.jsoup.nodes.Element link3 : links3) {
                        String clas3 = link3.attr("class");			//属性"class"の属性値を取得
                        if (clas3.equals("ymuiDate") || clas3.equals("source") || clas3.equals("ynLastEditDate yjSt") || clas3.equals("ynLastEditDate yjS")) {
                            String date = "\n"+link3.getElementsByAttribute("class").text()+"\n";
                            TextView mDate = (TextView) findViewById(R.id.date);
                            mDate.setText(date);
                        }
                    }

                    String image_url = "";
                    Elements links4 = document1.select("img[onContextMenu]");
                    image_url = links4.attr("src");            //属性"class"の属性値を取得
                    if(!image_url.isEmpty()) {
                        //imageを取得
                        final ImageView image = (ImageView) findViewById(R.id.imageView);
                        //画像取得スレッド起動
                        ImageGetTask task = new ImageGetTask(image);
                        task.execute(image_url);

                        image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ImageView imageView = new ImageView(ItemDetailActivity.this);
                                Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
                                imageView.setImageBitmap(bitmap);
                                // ディスプレイの幅を取得する（API 13以上）
                                Display display = getWindowManager().getDefaultDisplay();
                                Point size = new Point();
                                display.getSize(size);
                                int width = size.x;

                                float factor = width / bitmap.getWidth();
                                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                // ダイアログを作成する
                                Dialog dialog = new Dialog(ItemDetailActivity.this);
                                // タイトルを非表示にする
                                dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                                dialog.setContentView(imageView);
                                dialog.getWindow().setLayout((int) (bitmap.getWidth() * factor), (int) (bitmap.getHeight() * factor));
                                // ダイアログを表示する
                                dialog.show();
                            }
                        });
                    }

                    /*Elements links4 = null;
                    Elements links4_2 = null;
                    links4 = document1.getElementsByTag("div");  		//タグ"p"の要素を格納
                    for (org.jsoup.nodes.Element link4 : links4) {
                        links4_2 = link4.getElementsByTag("img");
                        System.out.println("links4_2：" + links4_2);
                        for (org.jsoup.nodes.Element link4_2 : links4_2) {
                                image_url = link4_2.attr("src");            //属性"class"の属性値を取得
                                System.out.println("画像のURL：" + image_url);
                                Toast.makeText(ItemDetailActivity.this, image_url, Toast.LENGTH_SHORT).show();
                            if(!image_url.isEmpty()) {
                                //imageを取得
                                ImageView image = (ImageView) findViewById(R.id.imageView);
                                //画像取得スレッド起動
                                ImageGetTask task = new ImageGetTask(image);
                                task.execute(image_url);
                            }
                        }
                    }*/

                    /*if(detailtext1.isEmpty()){
                        Toast.makeText(ItemDetailActivity.this, "記事情報を取得できませんでした", Toast.LENGTH_SHORT).show();
                    }*/
                }
            }
            start = System.currentTimeMillis();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e. printStackTrace();
        }
    }

    public boolean dispatchKeyEvent(KeyEvent event){
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){		// 戻るボタンが押された！
            stop = System.currentTimeMillis();
            diff = stop-start;
            MainActivity.TimeList2.put(title, diff);
            MainActivity.start = System.currentTimeMillis();
        }
        return super.dispatchKeyEvent(event);
    }
}
