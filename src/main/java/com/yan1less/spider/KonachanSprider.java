package com.yan1less.spider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class  KonachanSprider {


    private static String localPath = "D://konachan/";
    private static String uri = "http://konachan.com";
    private static String tags = "";
    private static Integer TotalPage = 1;
    private static Integer CurrentPage = 1;
    private static Integer EndPage = -1;


    private KonachanSprider() {
    }

    public static void startWithTags(String tags){
        starter(uri+"/post?tags="+tags);
    }


    public static void starter(String SpiderUri){

        //先做一下uri分解，检索出检索标签和当前页
        getSearchesAndCurrentPage(SpiderUri);
        //找出总页数
        JudgeTotalPage(SpiderUri);

        if(EndPage>TotalPage || EndPage==-1)
            EndPage=TotalPage;

        while(CurrentPage<=EndPage) {
            List<String> picUrlList =
                    getPicUrlList(uri+"/post?page="+CurrentPage+"&tags="+tags);
            //下载图片
            getImg(picUrlList, localPath);
            CurrentPage++;
            System.out.println("进入第 "+CurrentPage+" 页");

        }
        System.out.println("结束");

    }


    public static Boolean changeLocalPath(String newLocalPath){
        if(newLocalPath!=null)
            localPath = newLocalPath;
        return localPath==newLocalPath;
    }

    public static void EndWithWitchPage(int endPage){
        EndPage=endPage;
    }

    private static void getSearchesAndCurrentPage(String uri){
        String s = uri.substring(uri.lastIndexOf("?")+1);

        String[] split = s.split("&");
        if(split.length==1){
            //即只有条件，页数默认为1
            tags = split[0].substring(5);
            CurrentPage=1;

        }
        else if(split.length==2){
            //前一个是页数，后一个是条件
            if(split[0].substring(0,4)=="page") {
                CurrentPage = Integer.valueOf(split[0].substring(5));
                tags = split[1].substring(5);
            }
            else {
                tags = split[0].substring(5);
                CurrentPage = Integer.valueOf(split[1].substring(5));
            }

        }
        else {
            System.out.println("url经分割后得到三个条件，暂未判断");
        }
    }




    private static List<String> getPicUrlList(String url){
        String code = GetPageCodeByUrl(url);
        Document document = Jsoup.parse(code);
        //div content  ->div ->ul id=post-list-posts->li class=creator-id-* (has chicken)
        //->div class=inner->a class = thumb href=get
        //先获得页的信息




            Elements select = document.select(".inner a");
            int line = 0;
            List<String> hrefList = new ArrayList<String>();
            while (line < select.size()) {
                hrefList.add(getHrefByElement(select.get(line++).toString()));
            }

            return hrefList;


    }

    //通过总索引获取到class=pagination中所有的a对象，并判断其最大值，把pagesize设置进去
    private static void JudgeTotalPage(String url){
        String code = GetPageCodeByUrl(url);
        Document document = Jsoup.parse(code);
        Elements select = document.select(".pagination a");
        if(select.size()!=0)
            //最后一行是Next →，倒数第二行是我们需要的最后一页
            JudgeTotalPageRegex(select.get(select.size()-2>0?select.size()-2:0).toString());
        else
            TotalPage =1;

    }
    //判断总页数并自己维护；
    private static void JudgeTotalPageRegex(String regex){
        Pattern p = Pattern.compile("<a[^>]*>([^<]*)</a>");
        Matcher m = p.matcher(regex);
        while (m.find()){
            TotalPage= Integer.valueOf(m.group(1));
        }
        System.out.println("设置总页数为："+TotalPage);
    }

    private static String getHrefByElement(String currentLine){
        String href = "http://konachan.com";
        //获取href
        Pattern p = Pattern.compile("[\\s|\\S]* href=\"([^<]*)\">[\\s|\\S]*");
        Matcher m =p.matcher(currentLine);
        if(m.matches()){
            href += m.group(1);
        }
        return href;
    }







    //从标签页面拿到pic地址
    private static void getImg(List<String> imghref, String localPath){

        for(int i = 0 ; i<imghref.size();i++){
            String url = imghref.get(i);
            String code = GetPageCodeByUrl(url);
            Document document = Jsoup.parse(code);
            Elements select = document.select(".content img[class=image]");
            String imgSrc = getImgSrc(select.get(0).toString());
            String imgName = getImgName(imgSrc);

            downloadPicture(imgSrc,localPath+imgName);
            int sleepTime = (int)(Math.random()*10);
            System.out.println("下载"+imgName+"成功！，将休息0."+sleepTime+"秒");
            try {
                Thread.sleep(sleepTime*100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }


    }

    //获得图片的名字，即最后一个/之后的东西
    private  static String getImgName(String imgsrc){
        return imgsrc.substring(imgsrc.lastIndexOf("/")+1,imgsrc.length());
    }

    private static String getImgSrc(String img){
        //<img alt="blue_eyes blush breasts brown_hair cleavage clouds elbow_gloves gloves go-toubun_no_hanayome headdress long_hair nakano_miku oenothera petals sky wedding_attire" class="image" height="844" id="image" large_height="1080" large_width="1920" src="https://konachan.com/sample/0bfd9269c1c8d5b3f7f5a1546e27b99c/Konachan.com%20-%20281708%20sample.jpg" width="1500">
        String src = "";
        Pattern p = Pattern.compile("[\\s|\\S]* src=\"([^<]*)\" [\\s|\\S]*");
        Matcher m =p.matcher(img);
        if(m.matches()){
            src= m.group(1);
        }
        return src;
    }


    //链接url下载图片
    private static void downloadPicture(String urlList,String path) {
        URL url = null;
        try {
            url = new URL(urlList);
            DataInputStream dataInputStream = new DataInputStream(url.openStream());

            FileOutputStream fileOutputStream = new FileOutputStream(new File(path));
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int length;

            while ((length = dataInputStream.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            fileOutputStream.write(output.toByteArray());
            dataInputStream.close();
            fileOutputStream.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static  String GetPageCodeByUrl(String newUrl){

        try {

            URL url = new URL(newUrl);
            //建立连接
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            //获取输入流
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"utf-8"));
            //获取信息
            StringBuffer content =new StringBuffer();
            //定义每次读取的信息
            String temp = null;
            while((temp = reader.readLine())!=null){
                content.append( temp + "\n");
            }

            return  content.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


}
