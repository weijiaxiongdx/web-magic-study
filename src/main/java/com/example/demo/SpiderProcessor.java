package com.example.demo;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * @author
 */
public class SpiderProcessor implements PageProcessor {

    private Site site = Site.me().setRetryTimes(3).setSleepTime(100);

    @Override
    public void process(Page page) {

        page.putField("",page.getHtml().css("div[class= 'product-intro clearfix']"));
        page.putField("","");
    }

    @Override
    public Site getSite() {
        return site;
    }
}
