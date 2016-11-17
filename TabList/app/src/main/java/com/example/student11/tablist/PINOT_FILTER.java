package com.example.student11.tablist;

import android.os.Environment;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;

import net.reduls.sanmoku.Morpheme;
import net.reduls.sanmoku.Tagger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by student11 on 2016/11/13.
 */
public class PINOT_FILTER implements TextWatcher {

    public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
    public void onTextChanged(CharSequence s, int start, int before, int count) {}
    public void afterTextChanged(Editable s) {}

    public int Pinot_Filter(String info_mec, int remocon) {
        //char info_word[];
        int i,k;
        //char result;
        String sanmoku_info = null;	//形態素解析結果
        StringTokenizer tok1;
        String tok2;
        StringTokenizer tok3;
        String tok4;
        StringTokenizer tok5;
        String tok6;
        StringTokenizer tok7;
        String tok8;
        String[] word_class = new String[10];
        String[] word_class_v = new String[5];
        List<String> Word = new ArrayList<String>();
        String UP_word1;			//userProfile.txtの内容を１行ずつ読み込む
        String UP_word2;
        String UP_word_only;	//UP_wordから単語部分を分割したもの
        String UP_interest;		//UP_wordから興味の度合い値部分を分割したもの
        String it_Word;
        //String it_word[];
        double UP_interest_double = 0.0;	//ユーザプロファイルに格納されている単語の興味の度合い
        double info_interest = 0.0;			//記事見出し文の興味の度合い
        List <Double> Word_interest = new ArrayList<Double>();
        Map<String, Boolean> UP_bFlag = new HashMap<String, Boolean>();			//ユーザプロファイルの更新がすべて行われたかのフラグ
        String it2;
        Boolean bFlag;
        //File UP = new File("userProfile");
        //File UP_w = new File("userProfile_new.txt");
        BufferedWriter bw = null;
        BufferedReader br = null;
        final String LOGDIR = Environment.getExternalStorageDirectory().getPath()+"/data/";
        final String SDFILE1 = LOGDIR + "userProfile.txt";
        final String SDFILE2 = LOGDIR + "userProfile_new.txt";
        double J = 0;
        //char ctx;	//内部的に使用するもの

        //////////////////
        //情報の形態素解析//
        /////////////////

        if(info_mec.length() > 0) {
            for(Morpheme e : Tagger.parse(info_mec)) {			//info_mecを形態素解析
                SpannableString spannable = DataFormatter.format("<"+e.surface+">"+"\t"+e.feature);		//spannableに（単語　品詞の説明）の1文を読み込む
                sanmoku_info = spannable.toString();

                i=0;
                k=0;
                //名詞を格納
                if(sanmoku_info.indexOf("記号")==-1&&sanmoku_info.indexOf("助詞")==-1&&sanmoku_info.indexOf("名詞,数")==-1&&sanmoku_info.indexOf("助動詞")==-1&&sanmoku_info.indexOf("動詞")==-1){
                    word_class[i] = sanmoku_info;
                    //System.out.println(word_class[i]);
                    tok1 = new StringTokenizer(word_class[i], "\t");		//StringTokenizerオブジェクトtok1を生成
                    tok2 = tok1.nextToken().toString();
                    //Log.e("a",""+tok2);
                    if(tok2.matches("[\\p{Punct}]+")){						//記号判定（さんもくでは記号は名詞と判断されてしまうため）
                    }else{
                        Word.add(tok2);
                        i++;
                    }
                }
                //動詞を格納
                else if(sanmoku_info.indexOf("動詞") != -1){
                    word_class_v[k] = sanmoku_info;
                    tok3 = new StringTokenizer(word_class_v[k], "\t");
                    tok4 = tok3.nextToken().toString();
                    //Log.e("ab",""+tok4);
                    Word.add(tok4);
                    k++;
                }
            }
        }

        if(remocon == 1){
            ///////////////////////
            //情報に対する興味の度合い//
            ///////////////////////
            bFlag = true;			//ユーザプロファイルに情報を構成する単語が登録されているかのフラグ
            Iterator<String> it=Word.iterator();
            while(it.hasNext()){	//リストに要素がなくなるまでtrueを返す
                bFlag = true;
                it_Word = it.next().toString();				////次の要素を返す
                File UP = new File(SDFILE1);
                try {
                    if(UP.createNewFile()){
                    }else{
                        //Log.e("a","すでにあります。");
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
			    	/*if (UP.exists()){
			    		System.out.println("ファイルは存在します");
			    	}else{
			    		System.out.println("ファイルは存在しません");
			    	}*/

                try {
                    br = new BufferedReader(new FileReader(UP));						//ファイルの読み込み
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    while((UP_word1 = br.readLine()) != null){							//userProfileの先頭から１行読み込む
                        tok5 =  new StringTokenizer(UP_word1,"\t");
                        tok6 = tok5.nextToken();
                        UP_word_only = tok6;
                        tok6 = tok5.nextToken();
                        UP_interest = tok6;
                        if(UP_word_only.equals(it_Word)){
                            UP_interest_double = Double.parseDouble(UP_interest);
                            Word_interest.add(UP_interest_double);
                            info_interest = info_interest + UP_interest_double;
                            bFlag = false;
                            break;
                        }
                    }
                    br.close();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //br.close();

                if(bFlag){					//新出単語の場合
                    Word_interest.add(1.0);
                    info_interest = info_interest + 1.0;
                }
            }
            //System.out.println(info_interest + "\t" +  Word_interest.size());
            info_interest = info_interest / Word_interest.size();
            System.out.println(info_mec+":"+info_interest);
        }

        if(remocon == 2 || remocon == 3){	//remocon=2:興味無し　　remocon=3:興味有り
            /////////////////////
            //ユーザプロファイルの更新//
            ////////////////////
            Iterator<String> it=Word.iterator();
            while(it.hasNext()){			//リストに要素がなくなるまでtrueを返す
                it2 = it.next().toString();
                UP_bFlag.put(it2,true);
            }
            if(remocon == 2){
                J = 0;
            }else if (remocon == 3){
                J = 1;
            }
            double alfa = 0.5;
            bFlag = true;
            try {
                File UP = new File(SDFILE1);
                UP.createNewFile();
                br = new BufferedReader(new FileReader(UP));							//ファイルの読み込み
                File UP_w = new File(SDFILE2);
                UP_w.createNewFile();
                try {
                    BufferedWriter pw = new BufferedWriter(new FileWriter(UP_w));		//ファイルの書き込み
                    while((UP_word2 = br.readLine()) != null){							//userProfileの先頭から１行読み込む
                        bFlag = true;
                        tok7 =  new StringTokenizer(UP_word2,"\t");
                        tok8 = tok7.nextToken();
                        UP_word_only = tok8;
                        tok8 = tok7.nextToken();
                        UP_interest = tok8;
                        it=Word.iterator();
                        while(it.hasNext()){					//リストに要素がなくなるまでtrueを返す
                            it_Word = it.next().toString();		//次の要素を返す
                            if(UP_word_only.equals(it_Word)){
                                UP_interest_double = Double.parseDouble(UP_interest);
                                UP_interest_double = (alfa * UP_interest_double + (1 - alfa) * J);
                                //System.out.println(UP_word_only + UP_interest_double);
                                pw.write(UP_word_only + "\t" + UP_interest_double);
                                pw.newLine();
                                UP_bFlag.put(UP_word_only, false);					//値の上書き：更新した単語の値をtrueからfalseへ
                                bFlag = false;
                                break;
                            }
                        }
                        if(bFlag){
                            pw.write(UP_word_only + "\t" + UP_interest);			//記事情報に出現しなかった単語をそのままuserProfile_newに書き込む
                            pw.newLine();
                        }
                    }
                    Iterator<String> it3 = UP_bFlag.keySet().iterator();
                    while(it3.hasNext()){
                        it_Word = it3.next().toString();
                        Boolean value = UP_bFlag.get(it_Word);		//it_Wordの値を取得
                        if(value == true){
                            UP_interest_double = (alfa + (1 - alfa) * J);
                            pw.write(it_Word + "\t" + UP_interest_double);		//新出単語をUP_wへ書き込み
                            pw.newLine();
                        }
                        //System.out.println(it_Word + UP_interest_double);
                    }
                    pw.close();
                } catch (IOException e) {
                    // TODO 自動生成された catch ブロック
                    e.printStackTrace();
                }
                br.close();
                UP.delete();
                UP_w.renameTo(UP);
            } catch (FileNotFoundException e) {
                // TODO 自動生成された catch ブロック
                e.printStackTrace();
            } catch (IOException e) {
                // TODO 自動生成された catch ブロック
                e.printStackTrace();
            }
        }
        if(remocon == 1){
            if(info_interest >= 0.7){		//	記事情報を表示するかの判断
                return 1;
            } else{
                return 0;
            }
        } else {
            return 0;
        }
    }
}