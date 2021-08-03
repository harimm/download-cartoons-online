package dev.harimohan.app.toondownload.worker;

import dev.harimohan.app.toondownload.util.CloseUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@SuppressWarnings("WeakerAccess")
public class VideoFinder {
    private static final Logger logger = LoggerFactory.getLogger(VideoFinder.class);

    @Autowired
    private JSONParser jsonParser;

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
            return findVideoUrlFromPage(page);
        } catch (Throwable t) {
            logger.error(String.format("Error fetching video url from %s", episodeUrl), t);
        } finally {
            CloseUtil.close(httpClient);
            CloseUtil.close(httpResponse);
        }
        return null;
    }

    private String findVideoUrlFromPage(Document page) {
        // Method 1: Find url using jwplayer id
        try {
            String javaScript = page.selectFirst("#jwplayer-0").nextElementSibling().html();
            int jsonStartIndex = javaScript.indexOf("https");
            int jsonEndIndex = javaScript.indexOf("type:") - 2;

            return javaScript.substring(jsonStartIndex, jsonEndIndex);
        } catch (Throwable t) {
            logger.warn("Cannot find video url using method 1.", t);
        }
        // Method 2: Using video tag
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;
        try {
            String scriptNode = page.selectFirst(".playerpro").select("script").get(1).html();
            int startOfUrl = scriptNode.indexOf("url:") + 6;
            int endOfUrl = scriptNode.indexOf("\"", startOfUrl);
            String url = scriptNode.substring(startOfUrl, endOfUrl);

            int startOfRequest = scriptNode.indexOf("data: ", endOfUrl) + 6;
            int endOfRequest = scriptNode.indexOf("}", startOfRequest) + 1;
            String requestBody = scriptNode.substring(startOfRequest, endOfRequest);
            int startOfLink = requestBody.indexOf("link: ") + 7;
            int endOfLink = requestBody.length() - 3;
            String link = requestBody.substring(startOfLink, endOfLink);

            httpClient = HttpClients.custom().build();
            URIBuilder builder = new URIBuilder(url);
            builder.setParameter("poster", "");
            builder.setParameter("link", link);
            HttpGet httpGet = new HttpGet(builder.build());

            httpResponse = httpClient.execute(httpGet);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                return null;
            }

            return IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8);
        } catch (Throwable t) {
            logger.warn("Cannot find video url using method 2.", t);
        } finally {
            CloseUtil.close(httpClient);
            CloseUtil.close(httpResponse);
        }
        return null;
    }
}
