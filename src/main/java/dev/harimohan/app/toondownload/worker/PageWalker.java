package dev.harimohan.app.toondownload.worker;

import dev.harimohan.app.toondownload.util.CloseUtil;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

@Component
@SuppressWarnings("WeakerAccess")
public class PageWalker {
    private static final Logger logger = LoggerFactory.getLogger(PageWalker.class);

    @Value("${toon_download.base.url}")
    private String url;

    public Map<String, String> getCartoonPaths(String cartoonBasePath) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;

        try {
            httpClient = HttpClients.custom().build();
            HttpGet httpGet = new HttpGet(url + cartoonBasePath);

            Map<String, String> episodes = new TreeMap<>();

            httpResponse = httpClient.execute(httpGet);

            int statusCode = httpResponse.getCode();
            if (statusCode != HttpStatus.SC_OK) {
                logger.error(String.format("Failed connecting to %s. Status code is %d.", url, statusCode));
                return episodes;
            }

            String responseString = IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8);

            Document page = Jsoup.parse(responseString);

            Element baseElement = page.selectFirst("a[href=\""+ url+cartoonBasePath +"\"]");
            Element episodeListRoot = ((Element) baseElement.parentNode().parentNode()).selectFirst("ul");

            Elements listItems = episodeListRoot.select("a");

            for (Element listItem: listItems) {
                String cartoonUrl = listItem.attr("href");
                String episodeName = listItem.html();
                episodes.put(episodeName, cartoonUrl);
            }

            return episodes;
        } catch (Throwable t) {
            logger.error(String.format("Error fetching cartoonUrls for %s.",cartoonBasePath), t);
        } finally {
            CloseUtil.close(httpClient);
            CloseUtil.close(httpResponse);
        }
        return null;
    }
}
