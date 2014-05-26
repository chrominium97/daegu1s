package kr.kdev.dg1s.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class Parsers {


    public static class WeatherParser {
        ArrayList<Adapters.WeatherAdapter> parseWeather;
        private String TAG = "WeatherParser";

        public WeatherParser() {
            parseWeather = new ArrayList<Adapters.WeatherAdapter>();
        }

        public ArrayList<Adapters.WeatherAdapter> parseWeather() {
            try {
                parseWeather.clear();
                InputStream inputStream = new URL("http://www.kma.go.kr/wid/queryDFSRSS.jsp?zone=2714074500").openStream();

                //Parser
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();

                parser.setInput(inputStream, "UTF-8");

                String tag;
                Adapters.WeatherAdapter wa = null;
                int parserEvent = parser.getEventType();
                int count = 0;
                boolean weather = false;
                boolean time = false;
                boolean temp = false;
                while (parserEvent != XmlPullParser.END_DOCUMENT && count <= 4) {
                    switch (parserEvent) {
                        case XmlPullParser.START_TAG:
                            tag = parser.getName();

                            if (tag.equals("data")) {
                                wa = new Adapters.WeatherAdapter();
                            } else if (tag.equals("wfKor")) {
                                weather = true;
                            } else if (tag.equals("hour")) {
                                time = true;
                            } else if (tag.equals("temp")) {
                                temp = true;
                            }
                            break;
                        case XmlPullParser.TEXT:
                            tag = parser.getName();
                            if (weather) {
                                wa.weather = parser.getText().toString();
                                weather = false;
                                Log.d(TAG, "Weather: " + wa.weather);
                            } else if (time) {
                                wa.time = parser.getText().toString();
                                time = false;
                                Log.d(TAG, "Time: " + wa.time);
                            } else if (temp) {
                                wa.temperature = parser.getText().toString();
                                temp = false;
                                Log.d(TAG, "Temp: " + wa.temperature);
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            tag = parser.getName();

                            if (tag.equals("data")) {
                                parseWeather.add(wa);
                                count++;
                                Log.d(TAG, "-----");
                            }
                            break;
                    }
                    parserEvent = parser.next();
                }
                inputStream.close();
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
                Log.d("TAG", "NPE");
            }

            return parseWeather;
        }
    }

    public static class HomeworkParser {
        ArrayList<Adapters.PostListAdapter> parsePost;
        String TAG = "HWParser";

        public HomeworkParser() {
            parsePost = new ArrayList<Adapters.PostListAdapter>();
        }

        public ArrayList<Adapters.PostListAdapter> parsePost() {
            try {
                parsePost.clear();
                String url = "http://junbread.woobi.co.kr/hwlist.xml";
                URL targetURL = new URL(url);

                InputStream is = targetURL.openStream();

                //HomeworkParser
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser XMLparser = factory.newPullParser();

                XMLparser.setInput(is, "UTF-8");

                String tag;
                Adapters.PostListAdapter postData = null;
                int parserEvent = XMLparser.getEventType();
                while (parserEvent != XmlPullParser.END_DOCUMENT) {
                    switch (parserEvent) {
                        case XmlPullParser.END_TAG:
                            tag = XMLparser.getName();
                            if (tag.compareTo("item") == 0) {
                                parsePost.add(postData);
                                Log.d(TAG, "-----");
                            }
                            break;
                        case XmlPullParser.START_TAG:
                            tag = XMLparser.getName();
                            if (tag.compareTo("item") == 0) {
                                postData = new Adapters.PostListAdapter();
                            } else if (postData != null && tag.compareTo("grade") == 0) {
                                postData.gradeNumber = XMLparser.getAttributeValue(null, "data");
                                Log.d(TAG, "학년: " + postData.gradeNumber);
                            } else if (postData != null && tag.compareTo("clss") == 0) {
                                postData.classNumber = XMLparser.getAttributeValue(null, "data");
                                Log.d(TAG, "반: " + postData.classNumber);
                            } else if (postData != null && tag.compareTo("subject") == 0) {
                                postData.subject = XMLparser.getAttributeValue(null, "data");
                                Log.d(TAG, "과목: " + postData.subject);
                            } else if (postData != null && tag.compareTo("dscp") == 0) {
                                postData.information = XMLparser.getAttributeValue(null, "data");
                                Log.d(TAG, "설명: " + postData.information);
                            } else if (postData != null && tag.compareTo("due") == 0) {
                                postData.dueDate = XMLparser.getAttributeValue(null, "data");
                                Log.d(TAG, "기한: " + postData.dueDate);
                            }
                            break;
                    }
                    parserEvent = XMLparser.next();
                }

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return parsePost;
        }
    }

    public static class MealParser {
        ArrayList<Adapters.MealAdapter> parseMeal;

        SharedPreferences prefs;

        private Source source;

        public ArrayList<Adapters.MealAdapter> parseMeal(Context context) {
            UpdateThread updateThread = new UpdateThread();
            prefs = context.getSharedPreferences("kr.kdev.dg1s", Context.MODE_PRIVATE);
            updateThread.start();
            return parseMeal;
        }

        private void updateMeal() throws IOException {
            try {
                parseMeal.clear();
                InputStream inputStream = new URL("http://www.dg1s.hs.kr/user/carte/list.do").openStream();
                source = new Source(new InputStreamReader(inputStream, "UTF-8"));
                source.fullSequentialParse();

                //테이블가져오기
                Element table = source.getFirstElementByClass("meals_today_list");
                int cnt = table.getAllElements(HTMLElementName.IMG).size();

                for (int i = 0; i < cnt; i++) {
                    String panbyul = table.getAllElements(HTMLElementName.IMG).get(i).getAttributeValue("alt");
                    Log.d("MealParser", panbyul);
                    if (panbyul != null) {
                        if (panbyul.equals("조식")) {
                            Log.d("MealPrefs", table.getAllElements(HTMLElementName.IMG).get(i)
                                    .getParentElement().getContent().toString().replaceAll("[^>]*/> ", "")
                                    .replaceAll("[①-⑮]", ""));
                            prefs.edit().putString("breakfast", table.getAllElements(HTMLElementName.IMG).get(i)
                                    .getParentElement().getContent().toString().replaceAll("[^>]*/> ", "")
                                    .replaceAll("[①-⑮]", "")).commit();
                        } else if (panbyul.equals("중식")) {
                            prefs.edit().putString("lunch", table.getAllElements(HTMLElementName.IMG).get(i)
                                    .getParentElement().getContent().toString().replaceAll("[^>]*/> ", "")
                                    .replaceAll("[①-⑮]", "")).commit();
                        } else if (panbyul.equals("석식")) {
                            prefs.edit().putString("dinner", table.getAllElements(HTMLElementName.IMG).get(i)
                                    .getParentElement().getContent().toString().replaceAll("[^>]*/> ", "")
                                    .replaceAll("[①-⑮]", "")).commit();
                        }
                    }
                }
                inputStream.close();
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
                Log.d("TAG", "NPE");
            }
        }

        class UpdateThread extends Thread {
            public void run() {
                try {
                    updateMeal();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}