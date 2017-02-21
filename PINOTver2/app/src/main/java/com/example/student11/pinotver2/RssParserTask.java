package com.example.student11.pinotver2;

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
    File DATA = new File(LOGDIR);
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
    ArrayList<String> list;     //画面に表示する見出し文一覧
    ArrayList<String> list2;       //配信終了した見出しの一覧
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
        if(!DATA.exists()){
            DATA.mkdirs();
        }
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
                                val = P.Pinot_Filter(title,1);
                            }
                            else if (tag.equals("pubDate")) {
                                //date = parser.nextText();
                                date = dateconvert(parser.nextText());
                            }
                            else if (tag.equals("link")) {
                                link = parser.nextText();
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:				//itemタグが終わったら、そこで１記事のセットが終了したとしてlistに追加。
                        tag = parser.getName();
                        if (tag.equals("item")) {
                            BufferedWriter received = new BufferedWriter(new FileWriter(RECEIVED,true));//trueは追記,
                            if(val == 1 && containsUnicode(title)){
                                //mAdapter.add(currentItem);
                                received.write(title+"\t"+link+"\t"+date+"\t"+0+"\t"+-1);     //閾値以上ならばreceived.txtに書き込む，ファイル記述内容（見出し文　本文のURL　日付　視認回数　タップの有無）
                                received.newLine();
                            }
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
                StringTokenizer tok = new StringTokenizer(line, "\t");
                title_tmp = tok.nextToken();
                link_tmp = tok.nextToken();
                date_tmp = tok.nextToken();
                viewcount = Integer.parseInt(tok.nextToken());
                touch = Integer.parseInt(tok.nextToken());
                MainActivity.TitleList.add(title_tmp);
                MainActivity.LinkList.add(link_tmp);
                MainActivity.DateList.add(date_tmp);
                MainActivity.V_CountList.add(viewcount);
                MainActivity.T_CountList.add(touch);
                MainActivity.TitleCount++;
                //System.out.println("見出し：" + title_tmp+"\t"+"リンク："+link_tmp);
            }
            MainActivity.LoadingFlag = true;
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

    void compare(){     //受信した見出し文と前回の見出し文を比較⇒新出見出しと既出見出しを判別
        try {
            BufferedReader received = new BufferedReader(new FileReader(RECEIVED));
            list = new ArrayList<String>();
            while ((line = received.readLine()) != null) {
                StringTokenizer tok = new StringTokenizer(line, "\t");
                title_received = tok.nextToken();
                link_received = tok.nextToken();
                date_received = tok.nextToken();
                try {
                    BufferedReader displayed = new BufferedReader(new FileReader(DISPLAYED));
                    flag = true;             //tmp.txtに書き込みをしたか否かのフラグ
                    while ((line2 = displayed.readLine()) != null) {
                        StringTokenizer tok2 = new StringTokenizer(line2, "\t");
                        title_displayed = tok2.nextToken();
                        link_displayed = tok2.nextToken();
                        date_displayed = tok2.nextToken();
                        viewcount = Integer.parseInt(tok2.nextToken());
                        touch = Integer.parseInt(tok2.nextToken());
                        if (title_displayed.equals(title_received)) {
                            list.add(title_displayed + "\t" + link_displayed + "\t" + date_displayed + "\t" + viewcount + "\t" + touch);
                            //Log.i("1", "既出" + ":" + title_displayed + "\t" + viewcount + "\t" + touch);
                            flag = false;
                            break;
                        }
                    }
                    if(flag){            //新出見出し文
                        list.add(title_received + "\t" + link_received + "\t" + date_received + "\t" + 0 + "\t" + -1);
                        //Log.i("2", "新出" + ":" + title_received);
                    }
                    displayed.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            received.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    void compare2() {     //前回の見出し文と受信した見出し文を比較⇒RSSでの配信が終了した見出しを判別
        list2 = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(DISPLAYED));
            while ((line = br.readLine()) != null) {
                StringTokenizer tok = new StringTokenizer(line, "\t");
                title_displayed = tok.nextToken();
                link_displayed = tok.nextToken();
                date_displayed = tok.nextToken();
                viewcount = Integer.parseInt(tok.nextToken());
                touch = Integer.parseInt(tok.nextToken());
                flag = true;             //tmp.txtに書き込みをしたか否かのフラグ
                try {
                    BufferedReader br2 = new BufferedReader(new FileReader(RECEIVED));
                    while ((line2 = br2.readLine()) != null) {
                        StringTokenizer tok2 = new StringTokenizer(line2, "\t");
                        title_received = tok2.nextToken();
                        System.out.println("dispyaed：" + title_displayed + "\t" + "received：" + title_received);
                        if (title_displayed.equals(title_received)) {     // 前回表示した見出し文との比較
                            flag = false;
                            System.out.println("フラグ：" + flag + "\t" + "見出し：" + title_displayed);
                            break;
                        }
                    }
                    if (flag) {            //配信されなくなった記事
                        list2.add(title_displayed + "\t" + link_displayed + "\t" + date_displayed + "\t" + viewcount + "\t" + touch);
                        System.out.println("フラグ：" + flag + "\t" + "見出し：" + title_displayed);
                    }
                    br2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(ALL, true));
            for (int i = 0; i < list2.size(); i++) {
                bw.write(list2.get(i));
                bw.newLine();
            }
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}