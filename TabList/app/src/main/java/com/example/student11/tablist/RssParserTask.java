package com.example.student11.tablist;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

/**
 * Created by student11 on 2016/11/13.
 */
//「RssParserTask.java」では、RSSフィード（xmlファイル）の中から各要素（記事タイトルや記事概要など）を取得しています。
//その役目を行っているのが「parseXmlメソッド」です。
public class RssParserTask extends AsyncTask<String, Integer, RssListAdapter> {
    private MainActivity mActivity;
    private RssListAdapter mAdapter;
    private ProgressDialog mProgressDialog;
    public static long start;
    final String LOGDIR = Environment.getExternalStorageDirectory().getPath()+"/data/";
    final String SDFILE1 = LOGDIR + "displayed.txt";
    final String SDFILE2 = LOGDIR + "received.txt";
    final String SDFILE3 = LOGDIR + "tmp.txt";
    //final String SDFILE5 = LOGDIR + "tmp2.txt";
    final String SDFILE4 = LOGDIR + "all.txt";
    File DISPLAYED = new File(SDFILE1);       //前回表示した見出し文の一覧
    File RECEIVED = new File(SDFILE2);    //アプリ起動時に受信した見出し文の一覧（DISPLAYと重複する見出し文有り）
    File TMP = new File(SDFILE3);    //新しく表示する見出し文の一覧を一時格納
    File ALL = new File(SDFILE4);
    //File TMP2 = new File(SDFILE5);
    private String line;		//title_info.txtの先頭から１行ずつ取ってきたものを格納
    private String line2;
    private String title_displayed;
    private String title_received;
    private String title_tmp;
    private String link_displayed;
    private String link_received;
    private String link_tmp;
    private String date_displayed;
    private String date_received;
    private String date_tmp;
    PINOT_FILTER P = new PINOT_FILTER();
    String link;
    String title;
    String date;
    int displaycount;
    int viewcount;
    int touch;
    ArrayList<String> list;
    ArrayList<String> list2;
    boolean flag;
    private int val;
    private String title_all;


    // コンストラクタ
    public RssParserTask(MainActivity activity,RssListAdapter adapter) {
        mActivity = activity;
        mAdapter = adapter;
    }

    // タスクを実行した直後にコールされる
    @Override
    protected void onPreExecute() {
        // プログレスバーを表示する
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setMessage("Now Loading...");
        mProgressDialog.show();
    }

    // バックグラウンドにおける処理を担う。タスク実行時に渡された値を引数とする
    @Override
    protected RssListAdapter doInBackground(String... params) {
        RssListAdapter result = null;
        try {
            // HTTP経由でアクセスし、InputStreamを取得する
            URL url = new URL(params[0]);
            InputStream is = url.openConnection().getInputStream();		//コネクションを開き、接続先のデータを取得
            result = parseXml(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // ここで返した値は、onPostExecuteメソッドの引数として渡される
        return result;
    }

    // メインスレッド上で実行される
    @Override
    protected void onPostExecute(RssListAdapter result) {
        mProgressDialog.dismiss();
        //mActivity.setListAdapter(result);
        //start = System.currentTimeMillis();
    }



    // XMLをパースする
    public RssListAdapter parseXml(InputStream is) throws IOException, XmlPullParserException {		//inputStream:XMLストリームを指定する
        XmlPullParser parser = Xml.newPullParser();
        try {		//XMLパーサー解析開始
            parser.setInput(is, null);						//XMLのストリームを渡す
            int eventType = parser.getEventType();			//今読み込んでいる場所がどの状態かを知る
            Item currentItem = null;

            try {
                DISPLAYED.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                RECEIVED.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                TMP.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                ALL.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tag = null;
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tag = parser.getName();				//XMLのタグ名称を取得する
                        if (tag.equals("item")) {
                            currentItem = new Item("");
                        } else if (currentItem != null) {
                            if (tag.equals("title")) {
                                title = parser.nextText();
                                //val = P.Pinot_Filter(title,1);
                            }
                            else if (tag.equals("pubDate")) {
                                date = parser.nextText();
                            }
                            else if (tag.equals("link")) {
                                link = parser.nextText();
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:				//itemタグが終わったら、そこで１記事のセットが終了したとしてlistに追加。
                        tag = parser.getName();
                        if (tag.equals("item")) {
                            /*if(val == 1 && containsUnicode(title)){
                                //mAdapter.add(currentItem);
                                bw.write(title+"\t"+link+"\t"+date+"\t"+0+"\t"+-1);     //閾値以上ならばreceived.txtに書き込む
                                bw.newLine();
                            }*/

                            BufferedWriter received = new BufferedWriter(new FileWriter(RECEIVED,true));//trueは追記,
                            BufferedReader all = new BufferedReader(new FileReader(ALL));
                            //System.out.println(title+":"+link);
                            boolean flag=false;         //false:all.txtにない ,true:all.txtにある
                            while((line = all.readLine()) != null) {
                                StringTokenizer tok = new StringTokenizer(line, "\t");
                                title_all = tok.nextToken();
                                if(title.equals(title_all)) {
                                    flag = true;
                                    break;
                                }
                            }
                            if(!flag && containsUnicode(title)){              //3回表示された見出し文が再度表示されるのを防ぐ
                                received.write(title + "\t" + link);
                                received.newLine();
                            }
                            all.close();
                            received.close();
                        }
                        break;
                }
                eventType = parser.next();
            }
            compare();
            compare2();

            Collections.shuffle(list);
            try {
                BufferedWriter tmp = new BufferedWriter(new FileWriter(TMP));
                for (int i = 0; i < list.size(); i++) {
                    tmp.write(list.get(i));
                    tmp.newLine();
                }
                tmp.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            BufferedReader br2 = new BufferedReader(new FileReader(TMP));
            MainActivity.TitleCount = 0;
            while((line = br2.readLine()) != null){
                currentItem = new Item("");
                StringTokenizer tok = new StringTokenizer(line, "\t");
                title_tmp = tok.nextToken();
                link_tmp = tok.nextToken();
                displaycount = Integer.parseInt(tok.nextToken());
                viewcount = Integer.parseInt(tok.nextToken());
                touch = Integer.parseInt(tok.nextToken());
                MainActivity.TitleList.add(title_tmp);
                MainActivity.LinkList.add(link_tmp);
                MainActivity.D_CountList.add(displaycount);
                MainActivity.V_CountList.add(viewcount);
                MainActivity.T_CountList.add(touch);
                MainActivity.TitleCount++;
                /*date_tmp = tok.nextToken();
                String date = dateconvert(date_tmp);
                currentItem.setTitle(title_tmp);
                currentItem.setLink(link_tmp);
                currentItem.setDate(date);
                mAdapter.add(currentItem);*/
                //System.out.println("見出し：" + title_tmp+"\t"+"リンク："+link_tmp);
            }
            //MainActivity.PageCount = MainActivity.TitleCount/7 +1;
            System.out.println("見出し数：" + MainActivity.TitleCount);
            System.out.println("ページ数：" + MainActivity.PageCount);
            br2.close();

            DISPLAYED.delete();
            RECEIVED.delete();
            TMP.renameTo(DISPLAYED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mAdapter;
    }

    String dateconvert(String date){
        String mDate="";
        String week;
        String day;
        String month;
        String year;
        String time;
        String hour;
        String minute;

        StringTokenizer tok = new StringTokenizer(date, " ");
        week = tok.nextToken();
        day = tok.nextToken();
        month = tok.nextToken();
        year = tok.nextToken();
        time = tok.nextToken();

        StringTokenizer tok2 = new StringTokenizer(time, ":");
        hour = tok2.nextToken();
        minute = tok2.nextToken();

        switch (week){
            case "Mon,":
                week = "(月)";
                break;
            case "Tue,":
                week = "(火)";
                break;
            case "Wed,":
                week = "(水)";
                break;
            case "Thu,":
                week = "(木)";
                break;
            case "Fri,":
                week = "(金)";
                break;
            case "Sat,":
                week = "(土)";
                break;
            case "Sun,":
                week = "(日)";
                break;
        }

        switch (month){
            case "Jan":
                month = "1月";
                break;
            case "Feb":
                month = "2月";
                break;
            case "Mar":
                month = "3月";
                break;
            case "Apr":
                month = "4月";
                break;
            case "May":
                month = "5月";
                break;
            case "Jun":
                month = "6月";
                break;
            case "Jul":
                month = "7月";
                break;
            case "Aug":
                month = "8月";
                break;
            case "Sep":
                month = "9月";
                break;
            case "Oct":
                month = "10月";
                break;
            case "Nov":
                month = "11月";
                break;
            case "Dec":
                month = "12月";
                break;
        }
        mDate = month+day+"日"+week+" "+hour+"時"+minute+"分";

        return mDate;
    }

    public static boolean containsUnicode(String str) {
        for(int i = 0 ; i < str.length() ; i++) {
            char ch = str.charAt(i);
            Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(ch);

            if (Character.UnicodeBlock.HIRAGANA.equals(unicodeBlock))
                return true;

            if (Character.UnicodeBlock.KATAKANA.equals(unicodeBlock))
                return true;

            if (Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS.equals(unicodeBlock))
                return true;

            if (Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(unicodeBlock))
                return true;

            if (Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION.equals(unicodeBlock))
                return true;
        }
        return false;
    }

    void compare(){     //受信した見出し文と前回の見出し文を比較
        try {
            BufferedReader received = new BufferedReader(new FileReader(RECEIVED));
            list = new ArrayList<String>();
            while ((line = received.readLine()) != null) {
                StringTokenizer tok = new StringTokenizer(line, "\t");
                title_received = tok.nextToken();
                link_received = tok.nextToken();
                try {
                    BufferedReader displayed = new BufferedReader(new FileReader(DISPLAYED));
                    flag = true;             //tmp.txtに書き込みをしたか否かのフラグ
                    while ((line2 = displayed.readLine()) != null) {
                        StringTokenizer tok2 = new StringTokenizer(line2, "\t");
                        title_displayed = tok2.nextToken();
                        link_displayed = tok2.nextToken();
                        displaycount = Integer.parseInt(tok2.nextToken());
                        viewcount = Integer.parseInt(tok2.nextToken());
                        touch = Integer.parseInt(tok2.nextToken());
                        if (title_displayed.equals(title_received)) {     // 前回表示した見出し文
                            displaycount++;
                            list.add(title_displayed + "\t" + link_displayed + "\t" + displaycount + "\t" + viewcount + "\t" + touch);
                            Log.i("1", "既出" + ":" + title_displayed + "\t" + link_displayed + "\t" + displaycount + "\t" + viewcount + "\t" + touch);
                            flag = false;
                            break;
                        }
                    }
                    if(flag){            //新出見出し文
                        list.add(title_received + "\t" + link_received + "\t" + 1 + "\t" + 0 + "\t" + -1);
                        Log.i("2", "新出" + ":" + title_received);
                    }
                    displayed.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            received.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void compare2(){     //受信した見出し文と前回の見出し文を比較
        try {
            BufferedReader bw = new BufferedReader(new FileReader(DISPLAYED));
            while ((line = bw.readLine()) != null) {
                StringTokenizer tok = new StringTokenizer(line, "\t");
                title_displayed = tok.nextToken();
                link_displayed = tok.nextToken();
                displaycount = Integer.parseInt(tok.nextToken());
                viewcount = Integer.parseInt(tok.nextToken());
                touch = Integer.parseInt(tok.nextToken());
                try {
                    BufferedReader br = new BufferedReader(new FileReader(RECEIVED));
                    flag = true;             //tmp.txtに書き込みをしたか否かのフラグ
                    while ((line2 = br.readLine()) != null) {
                        StringTokenizer tok2 = new StringTokenizer(line2, "\t");
                        title_received = tok2.nextToken();
                        link_received = tok2.nextToken();
                        if (title_displayed.equals(title_received)) {     // 前回表示した見出し文
                            flag = false;
                            break;
                        }
                    }
                    if(flag){            //dispalyed.txtにあってreceived.txtにない
                        displaycount++;
                        list.add(title_displayed + "\t" + link_displayed + "\t" + displaycount + "\t" + viewcount + "\t" + touch);
                    }
                    br.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            bw.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}