package com.harrymdev.toondownload.worker;

import com.harrymdev.toondownload.util.CloseUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

@Component
public class PageWalker {
    private static final Logger logger = Logger.getLogger(PageWalker.class);

    @Value("${toon_download.base.url}")
    private String url;

    public Map<String, String> getCartoonPaths(String cartoonBasePath) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;

        try {
            httpClient = HttpClients.custom().build();
            HttpGet httpGet = new HttpGet(url + cartoonBasePath);

            httpResponse = httpClient.execute(httpGet);
            String responseString = IOUtils.toString(httpResponse.getEntity().getContent(), "UTF-8");

            Document page = Jsoup.parse(responseString);

            Element baseElement = page.selectFirst("a[href=\""+ url+cartoonBasePath +"\"]");
            Element episodeListRoot = ((Element) baseElement.parentNode().parentNode()).selectFirst("ul");

            Elements listItems = episodeListRoot.select("a");

            Map<String, String> episodes = new TreeMap<>();

            for (Element listItem: listItems) {
                String cartoonUrl = listItem.attr("href");
                String episodeName = listItem.html();
                episodes.put(episodeName, cartoonUrl);
            }

            return episodes;
        } catch (IOException e) {
            logger.error("Error fetching cartoonUrls for " + cartoonBasePath, e);
        } finally {
            CloseUtil.close(httpClient);
            CloseUtil.close(httpResponse);
        }
        return null;
    }
}
