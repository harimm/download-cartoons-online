package com.harrymdev.toondownload.worker;

import com.harrymdev.toondownload.util.CloseUtil;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DownloadWorker {
    private static final Logger logger = Logger.getLogger(DownloadWorker.class);

    @Value("${toon_download.base.url}")
    private String url;

    private EpisodeTracker episodeTracker;

    @Autowired
    public DownloadWorker(EpisodeTracker episodeTracker) {
        this.episodeTracker = episodeTracker;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Boolean downloadVideo(String fileName, String url) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;

        try {
            httpClient = HttpClients.custom()
                    .setRedirectStrategy(new LaxRedirectStrategy())
                    .build();
            HttpGet httpGet = new HttpGet(url);

            httpResponse = httpClient.execute(httpGet);

            String fullFilePath = String.format("%s%s.mp4", episodeTracker.getFolder(), fileName);

            logger.info("Downloading video to: " + fullFilePath);
            long currentTime = System.currentTimeMillis();
            File downloadedFile = new File(fullFilePath);
            downloadedFile.mkdirs();
            downloadedFile.delete();
            downloadedFile.createNewFile();
            InputStream inputStream = httpResponse.getEntity().getContent();
            ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
            FileOutputStream fileOutputStream = new FileOutputStream(downloadedFile);
            WritableByteChannel writableByteChannel = Channels.newChannel(fileOutputStream);
            ByteBuffer buffer = ByteBuffer.allocate(20480000);

            while (readableByteChannel.read(buffer) != -1) {
                buffer.flip();
                while (buffer.hasRemaining()) {
                    writableByteChannel.write(buffer);
                }
                buffer.clear();
            }

            CloseUtil.close(inputStream);
            CloseUtil.close(fileOutputStream);
            CloseUtil.close(readableByteChannel);
            CloseUtil.close(writableByteChannel);
            episodeTracker.addEpisodeToList(fileName);
            logger.info("Video saved to " + fullFilePath
                    + "\nTime taken: " + (System.currentTimeMillis() - currentTime) + " ms.");
            return Boolean.TRUE;
        } catch (IOException e) {
            logger.error("Error downloading Video: " + url, e);
        } finally {
            CloseUtil.close(httpClient);
            CloseUtil.close(httpResponse);
        }
        return Boolean.FALSE;
    }
}
