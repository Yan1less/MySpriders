package com.yan1less;

import com.yan1less.spider.KonachanSprider;
import org.junit.Test;

public class Starter {
    @Test
    public void start(){
        String url = "http://konachan.com/post?tags=nakano_miku";
        String tag = "satou_natsuki";
        KonachanSprider.EndWithWitchPage(3);
        KonachanSprider.startWithTags(tag);




    }
}
