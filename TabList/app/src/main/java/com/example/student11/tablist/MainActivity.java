package com.example.student11.tablist;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TreeMap;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements ActionBar.TabListener {

    public static SectionsPagerAdapter mSectionsPagerAdapter;
    static ViewPager mViewPager;

    static Context mContext;
    public static ArrayList TitleList;
    public static ArrayList LinkList;
    public static ArrayList D_CountList;
    public static ArrayList V_CountList;
    public static ArrayList T_CountList;
    public static int TitleCount = 0;
    public static int PageCount = 28;
    public static ArrayList TapList;
    public static ArrayList<String> TapList2;
    int PreviousPage;
    public static long start=0;
    public static long stop=0;
    public static long diff=0;
    //private ProgressDialog mProgressDialog;

    private static final String RSS_FEED_URL =  "http://www.rssmix.com/u/6589813/rss.xml"; //http://www.rssmix.com/u/6589813/rss.xml or http://mix.chimpfeedr.com/9f2cd-yahoonews
    public static ArrayList<Item> mItems;
    public static RssListAdapter mAdapter;
    public static Item item;
    TreeMap<Integer,Long> TotalTime;//各タブの合計表示時間
    ArrayList TimeList;//タブの表示時間履歴
    static TreeMap<String,Long> TimeList2;//記事本文の表示時間履歴

    final String LOGDIR = Environment.getExternalStorageDirectory().getPath()+"/data/";
    final String SDFILE1 = LOGDIR + "displayed.txt";
    final String SDFILE2 = LOGDIR + "tmp.txt";
    final String SDFILE3 = LOGDIR + "all.txt";
    final String SDFILE4 = LOGDIR + "TabDisplayTime.txt";
    final String SDFILE5 = LOGDIR + "TextDisplayTime.txt";
    final String SDFILE6 = LOGDIR + "History.txt";
    File DISPLAYED = new File(SDFILE1);       //表示した見出し文の一覧
    File TMP = new File(SDFILE2);    //見出し文の一覧を一時格納
    File ALL = new File(SDFILE3);
    File TAB = new File(SDFILE4);
    File TEXT = new File(SDFILE5);
    File HIS = new File(SDFILE6);
    String Title;
    String Link;
    Integer DisplayCount;
    Integer ViewCount;
    Integer Tap;
    String line;



    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        PreviousPage = 0;
        start=0;
        stop=0;
        diff=0;
        TotalTime = new TreeMap<Integer,Long>();
        TimeList = new ArrayList();
        TimeList2 = new TreeMap<String,Long>();

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                stop = System.currentTimeMillis();
                diff = diff+stop-start;
                int second = (int) (diff / 1000);
                int comma = (int) (diff % 1000);
                int ViewCount = (int) (diff/2200);
                System.out.println(PreviousPage + "ページの表示時間:" + second + "." + comma + "  " + "視認件数：" + ViewCount);

                if(TotalTime.containsKey(PreviousPage)) {
                    TotalTime.put(PreviousPage, TotalTime.get(PreviousPage) + diff);
                }else{
                    TotalTime.put(PreviousPage, diff);
                }
                TimeList.add(PreviousPage+ "ページ"+"\t"+diff);

                PreviousPage = mViewPager.getCurrentItem();

                actionBar.setSelectedNavigationItem(position);
                mSectionsPagerAdapter.getItemPosition(this);
                mSectionsPagerAdapter.notifyDataSetChanged();

                start = System.currentTimeMillis();
                diff=0;
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        mItems = new ArrayList<Item>();
        TapList = new ArrayList<String>();
        mAdapter = new RssListAdapter(this, mItems);
        TitleList = new ArrayList();
        LinkList = new ArrayList();
        D_CountList = new ArrayList();
        V_CountList = new ArrayList();
        T_CountList = new ArrayList();

        // タスクを起動する
        RssParserTask task = new RssParserTask(this, mAdapter);
        task.execute(RSS_FEED_URL);

        try {
            Thread.sleep(1500); //1000ミリ秒Sleepする
        } catch (InterruptedException e) {
            Log.i("AAA：", "待機できませんでした");
        }
    }


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

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        //mViewPager.setCurrentItem(tab.getPosition());         //タブが押されたとき、それに応じて見出しを変化させる
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return ListViewFragment.newInstance(position + 1);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            /*TitleCount = RssParserTask.TitleCount;
            //System.out.println("PageCount::"+a);
            if(!(TitleCount == 0)){//TitleCountが0でない
                PageCount = (TitleCount / 7) + 1;
            }*/
            return PageCount;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
                case 3:
                    return getString(R.string.title_section4).toUpperCase(l);
                case 4:
                    return getString(R.string.title_section5).toUpperCase(l);
                case 5:
                    return getString(R.string.title_section6).toUpperCase(l);
                case 6:
                    return getString(R.string.title_section7).toUpperCase(l);
                case 7:
                    return getString(R.string.title_section8).toUpperCase(l);
                case 8:
                    return getString(R.string.title_section9).toUpperCase(l);
                case 9:
                    return getString(R.string.title_section10).toUpperCase(l);
                case 10:
                    return getString(R.string.title_section11).toUpperCase(l);
                case 11:
                    return getString(R.string.title_section12).toUpperCase(l);
                case 12:
                    return getString(R.string.title_section13).toUpperCase(l);
                case 13:
                    return getString(R.string.title_section14).toUpperCase(l);
                case 14:
                    return getString(R.string.title_section15).toUpperCase(l);
                case 15:
                    return getString(R.string.title_section16).toUpperCase(l);
                case 16:
                    return getString(R.string.title_section17).toUpperCase(l);
                case 17:
                    return getString(R.string.title_section18).toUpperCase(l);
                case 18:
                    return getString(R.string.title_section19).toUpperCase(l);
                case 19:
                    return getString(R.string.title_section20).toUpperCase(l);
                case 20:
                    return getString(R.string.title_section21).toUpperCase(l);
                case 21:
                    return getString(R.string.title_section22).toUpperCase(l);
                case 22:
                    return getString(R.string.title_section23).toUpperCase(l);
                case 23:
                    return getString(R.string.title_section24).toUpperCase(l);
                case 24:
                    return getString(R.string.title_section25).toUpperCase(l);
                case 25:
                    return getString(R.string.title_section26).toUpperCase(l);
                case 26:
                    return getString(R.string.title_section27).toUpperCase(l);
                case 27:
                    return getString(R.string.title_section28).toUpperCase(l);
            }
            return null;
        }
    }




    public static class ListViewFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";
        View view;
        //private ProgressDialog mProgressDialog;

        public static ListViewFragment newInstance(int sectionNumber) {
            ListViewFragment fragment = new ListViewFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        /*@Override
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);

            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("記事を読み込み中・・・");
            mProgressDialog.setCancelable(true);
            mProgressDialog.show();

            mProgressDialog.dismiss();
        }*/

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            view = inflater.inflate(R.layout.list, container, false);
            return view;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            // アイテムを追加します
            /*Item item=new Item("");
            item.setTitle("red");
            mItems.add(item);
            Item item2=new Item("");
            item2.setTitle("green");
            mItems.add(item2);
            Item item3=new Item("");
            item3.setTitle("blue");
            mItems.add(item3);*/

            mAdapter.clear();

            switch (mViewPager.getCurrentItem()) {
                case 0:
                    for(int i=0;i<=6;i++){
                        if(TitleList.size() == 0) {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle("配信するニュースがありません")
                                    .setMessage("アプリを終了します")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // OK button pressed
                                            getActivity().finish();
                                        }
                                    })
                                    //.setNegativeButton("Cancel", null)
                                    .show();
                            break;
                        }
                        if(TitleList.size() <= i) break;
                        item = new Item("");
                        item.setTitle((String) TitleList.get(i));
                        item.setLink((String) LinkList.get(i));
                        mAdapter.add(item);
                    }
                    //Toast.makeText(getActivity(), mViewPager.getCurrentItem()+"PAGE", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    for(int i=7;i<=13;i++){
                        if(TitleList.size() <= i) break;
                        item = new Item("");
                        item.setTitle((String) TitleList.get(i));
                        item.setLink((String) LinkList.get(i));
                        mAdapter.add(item);
                    }
                    break;
                case 2:
                    for(int i=14;i<=20;i++){
                        if(TitleList.size() <= i) break;
                        item = new Item("");
                        item.setTitle((String) TitleList.get(i));
                        item.setLink((String) LinkList.get(i));
                        mAdapter.add(item);
                    }
                    break;
                case 3:
                    for(int i=21;i<=27;i++){
                        if(TitleList.size() <= i) break;
                        item = new Item("");
                        item.setTitle((String) TitleList.get(i));
                        item.setLink((String) LinkList.get(i));
                        mAdapter.add(item);
                    }
                    break;
                case 4:
                    for(int i=28;i<=34;i++){
                        if(TitleList.size() <= i) break;
                        item = new Item("");
                        item.setTitle((String) TitleList.get(i));
                        item.setLink((String) LinkList.get(i));
                        mAdapter.add(item);
                    }
                    break;
                case 5:
                    for(int i=35;i<=41;i++){
                        if(TitleList.size() <= i) break;
                        item = new Item("");
                        item.setTitle((String) TitleList.get(i));
                        item.setLink((String) LinkList.get(i));
                        mAdapter.add(item);
                    }
                    break;
                case 6:
                    for(int i=42;i<=48;i++){
                        if(TitleList.size() <= i) break;
                        item = new Item("");
                        item.setTitle((String) TitleList.get(i));
                        item.setLink((String) LinkList.get(i));
                        mAdapter.add(item);
                    }
                    break;
                case 7:
                    for(int i=49;i<=55;i++){
                        if(TitleList.size() <= i) break;
                        item = new Item("");
                        item.setTitle((String) TitleList.get(i));
                        item.setLink((String) LinkList.get(i));
                        mAdapter.add(item);
                    }
                    break;
                case 8:
                    for(int i=56;i<=62;i++){
                        if(TitleList.size() <= i) break;
                        item = new Item("");
                        item.setTitle((String) TitleList.get(i));
                        item.setLink((String) LinkList.get(i));
                        mAdapter.add(item);
                    }
                    break;
                case 9:
                    for(int i=63;i<=69;i++){
                        if(TitleList.size() <= i) break;
                        item = new Item("");
                        item.setTitle((String) TitleList.get(i));
                        item.setLink((String) LinkList.get(i));
                        mAdapter.add(item);
                    }
                    break;
                case 10:
                    for(int i=70;i<=76;i++){
                        if(TitleList.size() <= i) break;
                        item = new Item("");
                        item.setTitle((String) TitleList.get(i));
                        item.setLink((String) LinkList.get(i));
                        mAdapter.add(item);
                    }
                    break;
                case 11:
                    for(int i=77;i<=83;i++){
                        if(TitleList.size() <= i) break;
                        item = new Item("");
                        item.setTitle((String) TitleList.get(i));
                        item.setLink((String) LinkList.get(i));
                        mAdapter.add(item);
                    }
                    break;
                case 12:
                    for(int i=84;i<=90;i++){
                        if(TitleList.size() <= i) break;
                        item = new Item("");
                        item.setTitle((String) TitleList.get(i));
                        item.setLink((String) LinkList.get(i));
                        mAdapter.add(item);
                    }
                    break;
                case 13:
                    for(int i=91;i<=97;i++){
                        if(TitleList.size() <= i) break;
                        item = new Item("");
                        item.setTitle((String) TitleList.get(i));
                        item.setLink((String) LinkList.get(i));
                        mAdapter.add(item);
                    }
                    break;
                case 14:
                    for(int i=98;i<=104;i++){
                        if(TitleList.size() <= i) break;
                        item = new Item("");
                        item.setTitle((String) TitleList.get(i));
                        item.setLink((String) LinkList.get(i));
                        mAdapter.add(item);
                    }
                    break;
                case 15:
                    for(int i=105;i<=111;i++){
                        if(TitleList.size() <= i) break;
                        item = new Item("");
                        item.setTitle((String) TitleList.get(i));
                        item.setLink((String) LinkList.get(i));
                        mAdapter.add(item);
                    }
                    break;
                case 16:
                    for(int i=112;i<=118;i++){
                        if(TitleList.size() <= i) break;
                        item = new Item("");
                        item.setTitle((String) TitleList.get(i));
                        item.setLink((String) LinkList.get(i));
                        mAdapter.add(item);
                    }
                    break;
                case 17:
                    for(int i=119;i<=125;i++){
                        if(TitleList.size() <= i) break;
                        item = new Item("");
                        item.setTitle((String) TitleList.get(i));
                        item.setLink((String) LinkList.get(i));
                        mAdapter.add(item);
                    }
                    break;
                case 18:
                    for(int i=126;i<=132;i++){
                        if(TitleList.size() <= i) break;
                        item = new Item("");
                        item.setTitle((String) TitleList.get(i));
                        item.setLink((String) LinkList.get(i));
                        mAdapter.add(item);
                    }
                    break;
                case 19:
                    for(int i=133;i<=139;i++){
                        if(TitleList.size() <= i) break;
                        item = new Item("");
                        item.setTitle((String) TitleList.get(i));
                        item.setLink((String) LinkList.get(i));
                        mAdapter.add(item);
                    }
                    break;
                case 20:
                    for(int i=140;i<=146;i++){
                        if(TitleList.size() <= i) break;
                        item = new Item("");
                        item.setTitle((String) TitleList.get(i));
                        item.setLink((String) LinkList.get(i));
                        mAdapter.add(item);
                    }
                    break;
                case 21:
                    for(int i=147;i<=153;i++){
                        if(TitleList.size() <= i) break;
                        item = new Item("");
                        item.setTitle((String) TitleList.get(i));
                        item.setLink((String) LinkList.get(i));
                        mAdapter.add(item);
                    }
                    break;
                case 22:
                    for(int i=154;i<=160;i++){
                        if(TitleList.size() <= i) break;
                        item = new Item("");
                        item.setTitle((String) TitleList.get(i));
                        item.setLink((String) LinkList.get(i));
                        mAdapter.add(item);
                    }
                    break;
                case 23:
                    for(int i=161;i<=167;i++){
                        if(TitleList.size() <= i) break;
                        item = new Item("");
                        item.setTitle((String) TitleList.get(i));
                        item.setLink((String) LinkList.get(i));
                        mAdapter.add(item);
                    }
                    break;
                case 24:
                    for(int i=168;i<=174;i++){
                        if(TitleList.size() <= i) break;
                        item = new Item("");
                        item.setTitle((String) TitleList.get(i));
                        item.setLink((String) LinkList.get(i));
                        mAdapter.add(item);
                    }
                    break;
                case 25:
                    for(int i=175;i<=181;i++){
                        if(TitleList.size() <= i) break;
                        item = new Item("");
                        item.setTitle((String) TitleList.get(i));
                        item.setLink((String) LinkList.get(i));
                        mAdapter.add(item);
                    }
                    break;
                case 26:
                    for(int i=182;i<=188;i++){
                        if(TitleList.size() <= i) break;
                        item = new Item("");
                        item.setTitle((String) TitleList.get(i));
                        item.setLink((String) LinkList.get(i));
                        mAdapter.add(item);
                    }
                    break;
                case 27:
                    for(int i=189;i<=195;i++){
                        if(TitleList.size() <= i) break;
                        item = new Item("");
                        item.setTitle((String) TitleList.get(i));
                        item.setLink((String) LinkList.get(i));
                        mAdapter.add(item);
                    }
                    break;
                case 28:
                    for(int i=196;i<=202;i++){
                        if(TitleList.size() <= i) break;
                        item = new Item("");
                        item.setTitle((String) TitleList.get(i));
                        item.setLink((String) LinkList.get(i));
                        mAdapter.add(item);
                    }
                    break;
            }

            ListView listView = (ListView) view.findViewById(R.id.listview1);

            // アダプターを設定します
            listView.setAdapter(mAdapter);

            start = System.currentTimeMillis();

            // リストビューのアイテムがクリックされた時に呼び出されるコールバックリスナーを登録します
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    /*ListView listView = (ListView) parent;
                    // クリックされたアイテムを取得します
                    String item = (String) listView.getItemAtPosition(position);
                    Toast.makeText(view.getContext(), item, Toast.LENGTH_SHORT)
                            .show();*/
                    stop = System.currentTimeMillis();
                    diff = diff + stop - start;
                    Item item0 = mItems.get(position);
                    TapList.add(String.valueOf(item0.getTitle()));
                    Intent intent = new Intent(getActivity(), ItemDetailActivity.class);
                    intent.putExtra("TITLE", item0.getTitle());
                    intent.putExtra("LINK", item0.getLink());
                    startActivity(intent);
                    //Toast.makeText(mContext,  "LINK:"+item0.getLink(), Toast.LENGTH_SHORT).show();
                }
            });

            // リストビューのアイテムが選択された時に呼び出されるコールバックリスナーを登録します
            listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int position, long id) {
                    ListView listView = (ListView) parent;
                    // 選択されたアイテムを取得します
                    String item = (String) listView.getSelectedItem();
                    Toast.makeText(view.getContext(), item, Toast.LENGTH_SHORT)
                            .show();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){				// 戻るボタンが押された！
            stop = System.currentTimeMillis();
            diff = diff+stop-start;
            if(TotalTime.containsKey(PreviousPage)) {
                TotalTime.put(PreviousPage, TotalTime.get(PreviousPage) + diff);
            }else{
                TotalTime.put(PreviousPage, diff);
            }
            TimeList.add(PreviousPage+ "ページ"+"\t"+diff);
            try {
                TAB.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                TEXT.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(TAB, true));
                Time time = new Time("Asia/Tokyo");
                time.setToNow();
                String date = time.year + "年" + (time.month+1) + "月" + time.monthDay + "日　"+time.hour + "時" + time.minute + "分" + time.second + "秒";
                bw.write(date);
                bw.newLine();
                for (Object key : TimeList) {
                    System.out.println(key);
                    bw.write(String.valueOf(key));
                    bw.newLine();
                }
                bw.close();
                try {
                    BufferedWriter pw = new BufferedWriter(new FileWriter(HIS, true));
                    pw.write(date);
                    pw.newLine();
                    for(int i =0;i<TitleList.size();i++){
                        pw.write(TitleList.get(i) + "\t" + LinkList.get(i) + "\t" + D_CountList.get(i) + "\t" + V_CountList.get(i) + "\t" + T_CountList.get(i));
                        pw.newLine();
                    }
                    pw.newLine();
                    pw.close();
                }catch (IOException e1) {
                    e1.printStackTrace();
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(TEXT, true));
                for (Object key : TimeList2.keySet()) {
                    bw.write(key+"\t"+TimeList2.get(key));
                    bw.newLine();
                }
                bw.close();
            }catch (IOException e) {
                e.printStackTrace();
            }

            for (Integer key : TotalTime.keySet()) {
                Long vcount = TotalTime.get(key)/2200;
                System.out.println(key + "ページ:" + vcount+"件");
            }


            try {
                ALL.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                TMP.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            viewcount();
            tapinfo();
            displaycount();

            finish();
        }
        return super.onKeyDown(keyCode, event);
    }


    public static void distinct(List<String> slist) {       //要素の重複を削除
        /*set = new HashSet<String>();
        for (Iterator<String> i = slist.iterator(); i.hasNext();) {
            String s = i.next();
            if (set.contains(s)) {
                i.remove();
            } else {
                set.add(s);
            }
        }*/
        TapList2 = new ArrayList<String>();
        for (Iterator<String> i = slist.iterator(); i.hasNext();) {
            String s = i.next();
            if (TapList2.contains(s)) {
                i.remove();
            } else {
                TapList2.add(s);
            }
        }
    }

    public void viewcount(){
            for(Integer key : TotalTime.keySet()) {
                Long ViewHeadline;       //keyページで視認したとみなす見出しの件数
                int tmp = key;
                int tmp2 = key*7;

                for(ViewHeadline = TotalTime.get(key)/2200;ViewHeadline!=0;ViewHeadline--) {
                    System.out.println("ViewHead："+ViewHeadline+"   "+"key:"+key);
                    if(key == 0) {//0ページ目の処理
                        if(TitleList.size() <= tmp) break;
                        ViewCount = (Integer) V_CountList.get(tmp);
                        //System.out.println("ViewCount："+ViewCount);
                        ViewCount++;
                        //System.out.println("ViewCount(ato)："+ViewCount);
                        V_CountList.set(tmp, ViewCount);
                        tmp++;
                        if(6 < tmp) break;
                    }else{
                        if(TitleList.size() <= tmp2) break;
                        ViewCount = (Integer) V_CountList.get(tmp2);
                        ViewCount++;
                        V_CountList.set(tmp2, ViewCount);
                        tmp2++;
                        if(tmp2+6 < tmp2) break;
                    }

                }
            }
        /*for (Object key : V_CountList) {
            System.out.println(key);
        }*/
        try {
            BufferedWriter pw = new BufferedWriter(new FileWriter(TMP, true));
            for(int i =0;i<TitleList.size();i++){
                pw.write(TitleList.get(i) + "\t" + LinkList.get(i) + "\t" + D_CountList.get(i) + "\t" + V_CountList.get(i) + "\t" + T_CountList.get(i));
                pw.newLine();
            }
            pw.close();
        }catch (IOException e1) {
            e1.printStackTrace();
        }
        DISPLAYED.delete();
        TMP.renameTo(DISPLAYED);
    }

    public void tapinfo(){
        distinct(TapList);
        try {
            DISPLAYED.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            TMP.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(DISPLAYED));
            try {
                BufferedWriter pw = new BufferedWriter(new FileWriter(TMP,true));
                while ((line = br.readLine()) != null) {
                    StringTokenizer tok = new StringTokenizer(line, "\t");
                    Title = tok.nextToken();
                    Link = tok.nextToken();
                    DisplayCount = Integer.parseInt(tok.nextToken());
                    ViewCount = Integer.parseInt(tok.nextToken());
                    Tap = Integer.parseInt(tok.nextToken());
                    boolean f = true;
                    for ( int i = 0; i < TapList2.size(); i++ ) {
                        if (TapList2.get(i).equals(Title)) {
                            if (Tap == -1) {//未タップの場合
                                pw.write(Title + "\t" + Link + "\t" + DisplayCount + "\t" + ViewCount + "\t" + ViewCount);
                                pw.newLine();
                            } else if (Tap >= 0) {        //タップ済みの場合
                                pw.write(Title + "\t" + Link + "\t" + DisplayCount + "\t" + ViewCount + "\t" + Tap);
                                pw.newLine();
                            }
                            f=false;
                            break;
                        }
                    }
                    if(f) {
                        pw.write(Title + "\t" + Link + "\t" + DisplayCount + "\t" + ViewCount + "\t" + Tap);
                        pw.newLine();
                    }
                }
                pw.close();
                br.close();
                if(TapList2.size()>=1) {
                    DISPLAYED.delete();
                    TMP.renameTo(DISPLAYED);
                }
                if(TMP.exists()){
                    TMP.delete();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void displaycount(){
        try {
            BufferedReader br = new BufferedReader(new FileReader(DISPLAYED));
            try {
                BufferedWriter all = new BufferedWriter(new FileWriter(ALL, true));
                while ((line = br.readLine()) != null) {
                    StringTokenizer tok = new StringTokenizer(line, "\t");
                    Title = tok.nextToken();
                    Link = tok.nextToken();
                    DisplayCount = Integer.parseInt(tok.nextToken());
                    ViewCount = Integer.parseInt(tok.nextToken());
                    Tap = Integer.parseInt(tok.nextToken());
                    if (DisplayCount >= 3) {
                        all.write(Title + "\t"+ DisplayCount + "\t" + ViewCount + "\t" + Tap);
                        all.newLine();
                    }else{
                        try {
                            BufferedWriter bw = new BufferedWriter(new FileWriter(TMP, true));
                            bw.write(Title + "\t" + Link + "\t" + DisplayCount + "\t" + ViewCount + "\t" + Tap);
                            bw.newLine();
                            bw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                all.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            br.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        DISPLAYED.delete();
        TMP.renameTo(DISPLAYED);
    }

}
