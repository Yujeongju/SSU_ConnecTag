package com.hashtoggle.connectag;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    private RecyclerView list;
    private RecyclerViewAdapter list_ap;
    private EditText search_bar;
    private ImageButton menu_btn;
    private ImageButton search_btn;
    private List<Post> postlist;
    private int s_mYear, s_mMonth, s_mDay, f_mYear, f_mMonth, f_mDay;
    private Calendar cal;
    private TextView start_bar, end_bar, whole;
    private String keyword, string_sYear, string_sMonth, string_sDay, string_start,
            string_fYear, string_fMonth, string_fDay, string_finish;

    InputStream is = null;
    JSONObject JS;
    JSONArray temp = null;

    HttpPost request, post;
    HttpClient client;
    ResponseHandler reshandler;
    HttpEntity responseResultEntity;
    HttpResponse response;
    ProgressDialog pd, pd2;

    Handler handler, handler2, handler3, handler4, handler5, handler6, handler7;

    String wholeText;
    AlertDialog.Builder builder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler() {
            public void handleMessage(Message msg) {
                list.setAdapter(list_ap);
                list_ap.notifyDataSetChanged();
                //System.out.println("-----------------firstViewHandler-----  " + length + "  " + myAdapter.getItemCount());
            }
        };

        handler2 = new Handler() {
            public void handleMessage(Message msg) {
                whole.setText("전체: " + list_ap.getItemCount());
                //System.out.println("-----------------firstViewHandler-----  " + length + "  " + myAdapter.getItemCount());
            }
        };
        handler3 = new Handler() {
            public void handleMessage(Message msg) {
                //Toast.makeText(MainActivity.this, "데이터를 가져오는 중", Toast.LENGTH_LONG).show();
                pd = ProgressDialog.show(MainActivity.this, "로딩중", "데이터 로딩 중입니다...");
                //pd.setCancelable(true);
            }
        };

        handler4 = new Handler() {
            public void handleMessage(Message msg) {
                //Toast.makeText(MainActivity.this, "데이터를 로딩 완료", Toast.LENGTH_LONG).show();
                pd.dismiss();
            }
        };

        handler5 = new Handler() {
            public void handleMessage(Message msg) {
                builder.show();
            }
        };

        handler6 = new Handler() {
            public void handleMessage(Message msg) {
                //Toast.makeText(MainActivity.this, "데이터를 가져오는 중", Toast.LENGTH_LONG).show();
                pd2 = ProgressDialog.show(MainActivity.this, "입력중", "데이터 입력 중입니다...");
                //pd.setCancelable(true);
            }
        };

        handler7 = new Handler() {
            public void handleMessage(Message msg) {
                //Toast.makeText(MainActivity.this, "데이터를 로딩 완료", Toast.LENGTH_LONG).show();
                pd2.dismiss();
            }
        };

        builder = new AlertDialog.Builder(this);

        builder.setTitle("데이터 로딩 안내");
        builder.setMessage("데이터가 존재하지 않습니다.\n데이터를 넣겠습니까? 긴 시간이 소요되며 취소할 수 없습니다.");

        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(){
                            public void run(){
                                try {
                                    handler6.sendEmptyMessage(0);
                                    Instagram(keyword);
                                    insert_post(inputData());
                                    handler.sendEmptyMessage(0);
                                    handler7.sendEmptyMessage(0);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();

                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        handler4.sendEmptyMessage(0);
                    }
                });

        postlist = new ArrayList<>();
        list = (RecyclerView) findViewById(R.id.list);
        list_ap = new RecyclerViewAdapter(this, postlist);
        search_bar = (EditText) findViewById(R.id.search_bar);
        menu_btn = (ImageButton) findViewById(R.id.menu_button);
        search_btn = (ImageButton) findViewById(R.id.search_button);
        start_bar = (TextView) findViewById(R.id.start_bar);
        end_bar = (TextView) findViewById(R.id.end_bar);
        whole = (TextView) findViewById(R.id.whole);

        cal = new GregorianCalendar();
        s_mYear = cal.get(Calendar.YEAR);
        s_mMonth = cal.get(Calendar.MONTH);
        s_mDay = cal.get(Calendar.DAY_OF_MONTH);
        f_mYear = cal.get(Calendar.YEAR);
        f_mMonth = cal.get(Calendar.MONTH);
        f_mDay = cal.get(Calendar.DAY_OF_MONTH);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);

        string_start = "19700101";
        string_finish = "20200101";

        list.setLayoutManager(gridLayoutManager);//3행을 가진 그리드뷰로 레이아웃을 만듬

    }

    public void onClick_Search(View view) throws IOException {
        postlist = new ArrayList<>();
        list_ap = new RecyclerViewAdapter(this, postlist);

        new Thread() {
            public void run() {

                //System.out.println("+++++++++++++++++++++"+search_bar.getText().toString());
                keyword = "" + search_bar.getText().toString();

                try {
                    handler3.sendEmptyMessage(0);
                    check(keyword);
                    handler2.sendEmptyMessage(0);
                    handler4.sendEmptyMessage(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }

//                try {
//                    check_keyword(keyword);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                Instagram(keyword);
//                try {
//                    insert_post(inputData());
//                    handler.sendEmptyMessage(0);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
            }
        }.start();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(search_bar.getWindowToken(), 0);
    }

    public void onClick_end_Date(View view) {
        new DatePickerDialog(this, endDateSetListener, f_mYear, f_mMonth, f_mDay).show();
        //end_bar.setText(string_finish);
    }

    public void onClick_start_Date(View view) {
        new DatePickerDialog(this, startDateSetListener, s_mYear, s_mMonth, s_mDay).show();
        //start_bar.setText(string_start);
    }

    //날짜 대화상자 리스너 부분
    DatePickerDialog.OnDateSetListener endDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    f_mYear = year;
                    string_fYear = String.valueOf(f_mYear);
                    f_mMonth = monthOfYear;
                    if (f_mMonth + 1 < 10)
                        string_fMonth = "0" + String.valueOf(f_mMonth + 1);
                    else
                        string_fMonth = String.valueOf(f_mMonth + 1);
                    f_mDay = dayOfMonth;
                    if (f_mDay < 10)
                        string_fDay = "0" + String.valueOf(f_mDay);
                    else
                        string_fDay = String.valueOf(f_mDay);

                    string_finish = "" + string_fYear + string_fMonth + string_fDay;
                    end_bar.setText(string_finish);
                }

            };

    DatePickerDialog.OnDateSetListener startDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    s_mYear = year;
                    string_sYear = String.valueOf(s_mYear);
                    s_mMonth = monthOfYear;
                    if (s_mMonth + 1 < 10)
                        string_sMonth = "0" + String.valueOf(s_mMonth + 1);
                    else
                        string_sMonth = String.valueOf(s_mMonth + 1);
                    s_mDay = dayOfMonth;
                    if (s_mDay < 10)
                        string_sDay = "0" + String.valueOf(s_mDay);
                    else
                        string_sDay = String.valueOf(s_mDay);

                    string_start = "" + string_sYear + string_sMonth + string_sDay;
                    start_bar.setText(string_start);
                }

            };

    public void Instagram(String keyword) {
        System.out.println("\n-----------------Instagram2 Start--------------\n");

        try {
            //request 헤더 추가
            System.out.println("\n-----------------Instagram Connect Start--------------\n");

            Document wholeCode = Jsoup.connect("https://www.instagram.com/explore/tags/" + URLDecoder.decode(keyword, "UTF-8"))
//                    .header("Accept",
//                            "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
//                    .header("Accept-Encoding", "gzip, deflate, br")
//                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7").header("Connection", "keep-alive")
                    .header("Cookie",
                            "shbid=2382; csrftoken=ofJhzxbj5b7DqvZzd9BbecC9ddMYrYxL; ds_user_id=3451665929; mid=W3ZeWwAEAAFxRTah2gMqSLyFoUIA; mcd=3; fbm_124024574287414=base_domain=.instagram.com; csrftoken=ofJhzxbj5b7DqvZzd9BbecC9ddMYrYxL; rur=FTW; sessionid=IGSCbe1f22d109edc3691bd08d36485f9e900cac1f8b62fb2e5cf10ded0f97814a81%3A0rPYcNUdl69GWVgZVmcTdaFpnvqx1eZs%3A%7B%22_auth_user_id%22%3A3451665929%2C%22_auth_user_backend%22%3A%22accounts.backends.CaseInsensitiveModelBackend%22%2C%22_auth_user_hash%22%3A%22%22%2C%22_platform%22%3A4%2C%22_token_ver%22%3A2%2C%22_token%22%3A%223451665929%3ATls7kBIRv15DZszC7jFBZUPtxc2cgfrT%3A3a4a3e68491efeb3c447a4201458f7f0edaf37fd82695bc319512ed45ae049bc%22%2C%22last_refreshed%22%3A1534945024.2404546738%7D; fbsr_124024574287414=Rsn_jkyzvupVXCogJy4SMsU7kSm-xvudkXbRjpG6-VA.eyJhbGdvcml0aG0iOiJITUFDLVNIQTI1NiIsImNvZGUiOiJBUUNEUHRXejduZUMwYXBCZDNHbVdseXFmc1lZQUQzNU01NHFKYVZDaWlJUEx3V3lhR3dYYVVMOXU1MDRvSUFta3puYVA2ZzdLVnplRmNzekNhZW9OYlJmclRCZ0tFbHFUYXpYVkZHUGRxdmFTV3BpVW1mSU90eGpTRkRwZ1hRLUE2NDVseXFoWFAwX2d1UDBzV3I0a0E5OUFfSFoyS2JfSEpFck9CTktNZlRMUmtuQlR2QUxSY01wdW5wdi05N2ZHRnBlMTI2dFNQdVFnX0E2SzFkMU1lajloVWpKa3JiUkt0U0dMSWhlQWRPSkZZODNITW1QV3dXYjlDakdxeFdhV3FkNUxfWms5dDJhOTc5ZUNtTl82N1kwUndtbzRvdGZQb1VoN2kwUUozcGFOZnI4R3dZanJVTlBhTzhHSTlQZFNteDE0ck5yVXdjS21HWFJ2RFZMVE9XRyIsImlzc3VlZF9hdCI6MTUzNDk1MTAzNCwidXNlcl9pZCI6IjEwMDAwNDg2NTA1MDI4OCJ9; shbts=1534951070.1531627; urlgen=\"{\\\"210.93.56.23\\\": 23668\\054 \\\"121.170.57.238\\\": 4766}:1fsUtK:jqGFF0QxE9hkyQhCd-66-w5izC0\"")
//                    .header("Host", "www.instagram.com").header("Upgrade-Insecure-Requests", "1")
                    .userAgent(
                            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36")
//                    .method(Connection.Method.GET).referrer("http://www.instagram.com").ignoreContentType(true)
                    .get(); // 링크 연결

            System.out.println("\n-----------------Instagram Connect finish--------------\n");

            Elements scriptCode = wholeCode.select("body > script:eq(1)");
            wholeText = scriptCode.toString();
            System.out.println("\n-----------------Instagram2 finish--------------\n");

            getInstaInfoFromKeyword(wholeText, keyword);    //	정보를 수집하는 함수

            get_next_page();

        } catch (Exception e) {
            //System.out.println(e);
            e.printStackTrace();
            //System.out.println("Instagram Error!");
        }
    }

    public void getInstaInfoFromKeyword(String wholeText, String keyword) throws Exception { // keyword로 첫번째 게시물페이지의 정보를 가져옴
        System.out.println("\n-----------------getInstanInfoFromKeyword Start--------------\n");

        String[] shortcode = wholeText.split("\"shortcode\":\""); // 특정패턴 검색	/ 전체 문자열에서 필요한 부분만 수집하기 위해서
        String shortcode__end = new String("\",\"edge_medi");

        String[] contents = wholeText.split("text\":\"");
        String contents__end = new String("\"}}]},\"");

        String[] timestamp = wholeText.split("\"taken_at_timestamp\":");
        String timestamp__end = new String(",\"dimensi");

        for (int i = 1; i < contents.length; i++) {
            int contents_end = contents[i].indexOf(contents__end);
            if (contents_end > -1) {
                contents[i] = contents[i].substring(0, contents_end);
            }

            int shortcode_end = shortcode[i].indexOf(shortcode__end);
            if (shortcode_end > -1) {
                shortcode[i] = shortcode[i].substring(0, shortcode_end);
            }


            contents[i] = unicodeConvert(contents[i]);
            int timestamp_end = timestamp[i].indexOf(timestamp__end);
            if (timestamp_end > -1) {
                timestamp[i] = timestamp[i].substring(0, timestamp_end);
                // Date를 위해 import java.util.*;
            }
        }

        for (int i = 1; i < contents.length; i++) {
            // String content = URLDecoder.decode(contents[i],"UTF-8");
//            byte[] utf8 = contents[i].getBytes("UTF-8");
//            contents[i] = new String(utf8, "UTF-8");
//            System.out.println("--------" + contents[i] + "------------");
            contents[i] = contents[i].replaceAll("\\\\", "\\\\\\\\");

            connect("" + timestampConvert(timestamp[i]), "" + contents[i], "" + shortcode[i]);
        }

        System.out.println("\n-----------------getInstanInfoFromKeyword finish--------------\n");

        System.out.println("\n-----------------hasNextPage Start--------------\n");

//        while (true) { // 게시물을 새롭게 로드하여 끝까지 출력하는 반복문
////        //while (true) {
//            System.out.println("------------------getInstaInfoFromKeyword refresh1--------------");
//            if (hasNextPage(wholeText)) { // 로드해야 할 게시물이 남았다면
//                System.out.println("------------------getInstaInfoFromKeyword refresh2--------------");
//
//                String cur = getEndCursor(wholeText); // 현재 불러온 게시물들의 마지막을 가리키는 커서를 수집한다.
//
//                System.out.println("------------------getInstaInfoFromKeyword refresh3--------------");
//
//                try {
//
//                    wholeText = getNextPage(keyword, cur); // 키워드와 커서를 이용하여 다음 게시물의 페이지 소스코드를 가져온다.
//                    System.out.println("------------------getInstaInfoFromKeyword refresh4--------------");
//
//                    getInstaInfoFromKeyword(wholeText, keyword); // 소스코드에서 shortcode, 이미지링크, 좋아요수를 수집한다.
//                    System.out.println("------------------getInstaInfoFromKeyword refresh5--------------");
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            } else {
//                break;
//            }
//            return;
//        }
        System.out.println("\n-----------------hasNextPage finish--------------\n");

    }

    public void get_next_page() {
        System.out.println("\n=====================get_next_page Start=================");
        while (true) {
            if (hasNextPage(wholeText)) { // 로드해야 할 게시물이 남았다면
                System.out.println("------------------getInstaInfoFromKeyword refresh2--------------");

                String cur = getEndCursor(wholeText); // 현재 불러온 게시물들의 마지막을 가리키는 커서를 수집한다.

                System.out.println("------------------getInstaInfoFromKeyword refresh3--------------");

                try {

                    wholeText = getNextPage(keyword, cur); // 키워드와 커서를 이용하여 다음 게시물의 페이지 소스코드를 가져온다.
                    System.out.println("------------------getInstaInfoFromKeyword refresh4--------------");

                    getInstaInfoFromKeyword(wholeText, keyword); // 소스코드에서 shortcode, 이미지링크, 좋아요수를 수집한다.
                    System.out.println("------------------getInstaInfoFromKeyword refresh5--------------");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                return;
            }
        }
    }

    public String unicodeConvert(String str) {
        StringBuilder sb = new StringBuilder();
        char ch;
        int len = str.length();
        for (int i = 0; i < len; i++) {
            ch = str.charAt(i);
            if (ch == '\\' && str.charAt(i + 1) == 'u') {
                sb.append((char) Integer.parseInt(str.substring(i + 2, i + 6), 16));
                i += 5;
                continue;
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    public String timestampConvert(String str) {
        long timestamp = Long.parseLong(str);
        Date date = new Date((long) timestamp * 1000);
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyyMMdd");
        String dateToString = transFormat.format(date);
//        System.out.println(dateToString);

        return dateToString;
    }

    private void insert_post(JSONArray temp) throws Exception {
        System.out.println("\n-----------------Insert_post Start--------------\n");
        String[] jsonName = {"hashtag", "date", "keyword", "shortcode", "cnt"};
        String[][] parseData = new String[temp.length()][jsonName.length];
        JSONObject JS = null;
        System.out.println("\n-----------------JS Start--------------\n");
        for (int i = 0; i < temp.length(); i++) {
            JS = temp.getJSONObject(i);
            if (JS != null) {
                for (int j = 0; j < jsonName.length; j++) {
                    //System.out.println("!!!!!!!!!!!!!!!!!!!!!!" + JS.getString(jsonName[j]));
                    parseData[i][j] = JS.getString(jsonName[j]);
                }
            }
        }
        System.out.println("\n-----------------JS finish--------------\n");

        System.out.println("\n-----------------JS2 Start--------------\n");

        String[] ps_hashtag = new String[temp.length()];
        String[] ps_date = new String[temp.length()];
        String[] ps_keyword = new String[temp.length()];
        String[] ps_shortcode = new String[temp.length()];
        String[] ps_count = new String[temp.length()];

        for (int i = 0; i < temp.length(); i++) {
            if (JS != null) {
                for (int j = 0; j < jsonName.length; j++) {
//                    System.out.println("i : " + i + "j : " + j);
//                    System.out.println(parseData[i][j]);
                    if (j == 0)
                        ps_hashtag[i] = parseData[i][j];
                    else if (j == 1)
                        ps_date[i] = parseData[i][j];
                    else if (j == 2)
                        ps_keyword[i] = parseData[i][j];
                    else if (j == 3)
                        ps_shortcode[i] = parseData[i][j];
                    else if (j == 4)
                        ps_count[i] = parseData[i][j];
                }
            } else
                System.out.println("--------------!!!------------");
        }
        System.out.println("\n-----------------JS2 finish--------------\n");

        System.out.println("\n-----------------Glide Start--------------\n");

        for (int i = 1; i < ps_count.length; i++) {
            String number = Integer.toString(i);

            postlist.add(new Post("" + number, "" + ps_hashtag[i], "" + ps_count[i]));
                //handler.sendEmptyMessage(0);

            //System.out.println("\n!!!!!!!!!!!!!!!!!!!!!!!Image_URL   "+ps_display_url[i]);
        }

        System.out.println("\n-----------------Glide finish--------------\n");
        System.out.println("\n-----------------Insert_post finish--------------\n");
    }

    private JSONArray inputData() throws IOException {
        System.out.println("\n-----------------inputData Start--------------\n");

        sendjson();

        System.out.println("\ninputData_start");
        try {
            response = client.execute(request);
            responseResultEntity = response.getEntity();
            if (responseResultEntity != null) {
                is = responseResultEntity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                String result = sb.toString();

                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!" + result);////////////////////////////////////////////////////

                JS = new JSONObject(result);
                temp = JS.getJSONArray("result");
                System.out.println("\ninputData_end");
            } else
                System.out.println("--------------NULL----------");

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("\n-----------------inputData finish--------------\n");

        return temp;
    }

    private void check(final String keyword) throws Exception {
        try {
            check_keyword(keyword);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("\ninputData_start");
        try {
            response = client.execute(request);
            responseResultEntity = response.getEntity();
            if (responseResultEntity != null) {
                is = responseResultEntity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                String result = sb.toString();
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!" + result + "!!!!!!!!!!!!!!!!");////////////////////////////////////////////////////

                if (result.equals("\"" + "dup" + "\"" + "\n")) {
                    System.out.println("------------------");
                    insert_post(inputData());
                    handler.sendEmptyMessage(0);

                } else {
                    System.out.println("++++++++++++++++++++++++");

                    handler5.sendEmptyMessage(0);
                    handler.sendEmptyMessage(0);
                }

                System.out.println("\ninputData_end");
            } else
                System.out.println("--------------NULL----------");

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("\n-----------------inputData finish--------------\n");
    }

    private void connect(String Date, String wholeHashtag, String shortcode) throws IOException {
        // TODO Auto-generated method stub
        System.out.println("\n-----------------connect Start--------------\n");
        request = makeHttpPost(Date, wholeHashtag, shortcode, "http://ryunha.cafe24.com/user_signup/connect.php");
        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        client = new DefaultHttpClient(params);
        reshandler = new BasicResponseHandler();

        try {
            client.execute(request, reshandler);
            System.out.println("\nconnect_end");
        } catch (IOException e) {
            System.out.println("\nconnect_Exception");
            e.printStackTrace();
        }
        System.out.println("\n----------------connect finish--------------\n");

    }


    private void sendjson() throws IOException {
        // TODO Auto-generated method stub
        System.out.println("\n-----------------sendJson Start--------------\n");
        request = makeHttpPost("" + string_start, "" + string_finish, "http://ryunha.cafe24.com/user_signup/sendjson.php");
        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        client = new DefaultHttpClient(params);
        reshandler = new BasicResponseHandler();

        System.out.println("++++++++++" + string_start + "++++++++++++");
        System.out.println("++++++++++" + string_finish + "+++++++++++");

        try {
            client.execute(request, reshandler);
            System.out.println("\nsendJson_end");
        } catch (IOException e) {
            System.out.println("\nsendJson_Exception");
            e.printStackTrace();
        }
        System.out.println("\n-----------------sendJson finish--------------\n");

    }

    private void check_keyword(String keyword) throws IOException {
        request = makeHttpPost("http://ryunha.cafe24.com/user_signup/check_keyword.php");
        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        client = new DefaultHttpClient(params);
        reshandler = new BasicResponseHandler();

        try {
            client.execute(request, reshandler);
            System.out.println("\ncheck_keyword_end");
        } catch (IOException e) {
            System.out.println("\ncheck_keyword_Exception");
            e.printStackTrace();
        }
    }

    public boolean hasNextPage(String wholeText) { // 게시물 페이지가 마지막인지 확인하는 함수
        boolean check;
        String[] hasNextPage = wholeText.split("has_next_page\":");    //	~부터
        String end = new String(",\"end_");                            //	~까지 추출
        int hasNextPage_end = hasNextPage[1].indexOf(end);
        if (hasNextPage_end > -1) {
            hasNextPage[1] = hasNextPage[1].substring(0, hasNextPage_end);
        }

        if (hasNextPage[1].equals("true")) {
            System.out.println("---------hasnext : " + hasNextPage[1]);
            check = true;
        } else {
            check = false;
            System.out.println("---------hasnext : " + hasNextPage[1]);
        }

        return check;
    }

    public String getEndCursor(String wholeText) { // nextpage가 있을 때 커서를 확인하는 라인

        String[] endCursor = wholeText.split("\"end_cursor\":\"");
        String end = new String("\"},\"edges\":");
        int endCursor_end = endCursor[1].indexOf(end);
        if (endCursor_end > -1) {
            endCursor[1] = endCursor[1].substring(0, endCursor_end);
        }
//        System.out.println("endCursor : " + endCursor[1]);
        if (endCursor[1].equals("null"))
            return "null";
        else
            return endCursor[1];
    }

    public String getNextPage(String keyword, String cur) throws Exception {    //	로드해야할 다음 페이지소스코드를 가져오는 함수
//        System.out.println("++++++++++++++++getNextPage    "+ keyword);
        System.out.println("++++++++++++++++getNextPage    " + cur);
        String url = "https://www.instagram.com/graphql/query/?query_hash=faa8d9917120f16cec7debbd3f16929d&variables={\"tag_name\":\""
                + keyword + "\",\"first\":12,\"after\":\"" + cur + "\"}";
        // request 헤더를 포함시킴
        // System.out.println("++++++++++++++++" + url);

        System.out.println("\n-----------------getNextPage Connect Start--------------\n");

        Connection conn = Jsoup.connect(url)
                .header("Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7").header("Connection", "keep-alive")
                .header("Cookie",
                        "shbid=2382; csrftoken=ofJhzxbj5b7DqvZzd9BbecC9ddMYrYxL; ds_user_id=3451665929; mid=W3ZeWwAEAAFxRTah2gMqSLyFoUIA; mcd=3; fbm_124024574287414=base_domain=.instagram.com; csrftoken=ofJhzxbj5b7DqvZzd9BbecC9ddMYrYxL; rur=FTW; sessionid=IGSCbe1f22d109edc3691bd08d36485f9e900cac1f8b62fb2e5cf10ded0f97814a81%3A0rPYcNUdl69GWVgZVmcTdaFpnvqx1eZs%3A%7B%22_auth_user_id%22%3A3451665929%2C%22_auth_user_backend%22%3A%22accounts.backends.CaseInsensitiveModelBackend%22%2C%22_auth_user_hash%22%3A%22%22%2C%22_platform%22%3A4%2C%22_token_ver%22%3A2%2C%22_token%22%3A%223451665929%3ATls7kBIRv15DZszC7jFBZUPtxc2cgfrT%3A3a4a3e68491efeb3c447a4201458f7f0edaf37fd82695bc319512ed45ae049bc%22%2C%22last_refreshed%22%3A1534945024.2404546738%7D; fbsr_124024574287414=Rsn_jkyzvupVXCogJy4SMsU7kSm-xvudkXbRjpG6-VA.eyJhbGdvcml0aG0iOiJITUFDLVNIQTI1NiIsImNvZGUiOiJBUUNEUHRXejduZUMwYXBCZDNHbVdseXFmc1lZQUQzNU01NHFKYVZDaWlJUEx3V3lhR3dYYVVMOXU1MDRvSUFta3puYVA2ZzdLVnplRmNzekNhZW9OYlJmclRCZ0tFbHFUYXpYVkZHUGRxdmFTV3BpVW1mSU90eGpTRkRwZ1hRLUE2NDVseXFoWFAwX2d1UDBzV3I0a0E5OUFfSFoyS2JfSEpFck9CTktNZlRMUmtuQlR2QUxSY01wdW5wdi05N2ZHRnBlMTI2dFNQdVFnX0E2SzFkMU1lajloVWpKa3JiUkt0U0dMSWhlQWRPSkZZODNITW1QV3dXYjlDakdxeFdhV3FkNUxfWms5dDJhOTc5ZUNtTl82N1kwUndtbzRvdGZQb1VoN2kwUUozcGFOZnI4R3dZanJVTlBhTzhHSTlQZFNteDE0ck5yVXdjS21HWFJ2RFZMVE9XRyIsImlzc3VlZF9hdCI6MTUzNDk1MTAzNCwidXNlcl9pZCI6IjEwMDAwNDg2NTA1MDI4OCJ9; shbts=1534951070.1531627; urlgen=\"{\\\"210.93.56.23\\\": 23668\\054 \\\"121.170.57.238\\\": 4766}:1fsUtK:jqGFF0QxE9hkyQhCd-66-w5izC0\"")
                .header("Host", "www.instagram.com").header("Upgrade-Insecure-Requests", "1")
                .userAgent(
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36")
                .method(Connection.Method.GET).referrer("http://www.instagram.com").ignoreContentType(true);

        System.out.println("\n-----------------getNextPage Connect finish--------------\n");

        Document wholeCode = conn.get();
        System.out.println("\n-----------------getNextPage Connect finish1--------------\n");
        String wholeText = wholeCode.toString();
        System.out.println("\n-----------------getNextPage Connect finish2--------------\n");

        return wholeText;

    }


    private HttpPost makeHttpPost(String Date, String wholeHashtag, String shortcode, String url) throws UnsupportedEncodingException {
        System.out.println("\n-----------------makeHttpPost Start--------------\n");

        HttpPost request = new HttpPost(url);

        //System.out.println("\n\n-----------keyword_final----------" + keyword_final);
        Vector<NameValuePair> nameValue = new Vector<NameValuePair>();
        nameValue.add(new BasicNameValuePair("date", "" + Date));
        nameValue.add(new BasicNameValuePair("hashtag", "" + URLEncoder.encode("" + wholeHashtag, "UTF-8")));
        nameValue.add(new BasicNameValuePair("keyword", "" + URLEncoder.encode("" + keyword, "UTF-8")));
        nameValue.add(new BasicNameValuePair("shortcode", "" + shortcode));
        nameValue.add(new BasicNameValuePair("startDate", "" + string_start));
        nameValue.add(new BasicNameValuePair("endDate", "" + string_finish));

        request.setEntity(makeEntity(nameValue));
        System.out.println("\n-----------------makeHttpPost finish--------------\n");

        return request;
    }

    private HttpPost makeHttpPost(String url) throws UnsupportedEncodingException {
        System.out.println("\n-----------------makeHttpPost Start--------------\n");

        HttpPost request = new HttpPost(url);

        Vector<NameValuePair> nameValue = new Vector<NameValuePair>();
        nameValue.add(new BasicNameValuePair("keyword", "" + URLEncoder.encode("" + keyword, "UTF-8")));
        request.setEntity(makeEntity(nameValue));

        return request;
    }

    private HttpPost makeHttpPost(String startDate, String EndDate, String url) throws UnsupportedEncodingException {
        System.out.println("\n-----------------makeHttpPost Start--------------\n");

        HttpPost request = new HttpPost(url);

        Vector<NameValuePair> nameValue = new Vector<NameValuePair>();
        nameValue.add(new BasicNameValuePair("keyword", "" + URLEncoder.encode("" + keyword, "UTF-8")));
        nameValue.add(new BasicNameValuePair("startDate", "" + startDate));
        nameValue.add(new BasicNameValuePair("endDate", "" + EndDate));

        request.setEntity(makeEntity(nameValue));

        return request;
    }

    private HttpEntity makeEntity(Vector<NameValuePair> nameValue) {
        HttpEntity result = null;
        try {
            result = new UrlEncodedFormEntity(nameValue);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

}
