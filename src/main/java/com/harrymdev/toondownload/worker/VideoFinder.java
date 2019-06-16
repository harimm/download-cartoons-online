package com.harrymdev.toondownload.worker;

import com.harrymdev.toondownload.util.CloseUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@SuppressWarnings("WeakerAccess")
public class VideoFinder {
    private static final Logger logger = Logger.getLogger(VideoFinder.class);

    public String findVideoUrl(String episodeUrl) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;

        try {
            httpClient = HttpClients.custom().build();
            HttpGet httpGet = new HttpGet(episodeUrl);

            httpResponse = httpClient.execute(httpGet);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                logger.error(String.format("Failed connecting to %s. Status code is %d.", episodeUrl, statusCode));
                return null;
            }
            String responseString = IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8);

            Document page = Jsoup.parse(responseString);
            String javaScript = page.selectFirst("#jwplayer-0").nextElementSibling().html();
            int jsonStartIndex = javaScript.indexOf("https");
            int jsonEndIndex = javaScript.indexOf("type:") - 2;

            return javaScript.substring(jsonStartIndex, jsonEndIndex);
        } catch (IOException e) {
            logger.error(String.format("Error fetching video url from %s", episodeUrl), e);
        } finally {
            CloseUtil.close(httpClient);
            CloseUtil.close(httpResponse);
        }
        return null;
    }
}
