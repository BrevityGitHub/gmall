package com.brevity.spier;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JsonpClient {
    public static void main(String[] args) {
        String html = HttpClientUtil.doGet("https://www.jd.com/allSort.aspx");
        Document document = Jsoup.parse(html);
        Elements catalog1 = document.select("div[class='category-item m']");
        for (Element element : catalog1) {
            String ctg1 = element.select(".item-title span").text();
            System.out.println("一级分类：" + ctg1);
            Elements ctg2 = element.select(".items .clearfix");
            for (Element element1 : ctg2) {
                String dt_a = element1.select("dt a").text();
                System.out.println("    二级分类：" + dt_a);

                Elements ctg3 = element1.select("dd a");
                for (Element element2 : ctg3) {
                    String catg3 = element2.text();
                    System.out.println("        三级分类：" + catg3);
                }
            }
        }
    }
}
