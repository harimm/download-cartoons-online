package dev.harimohan.app.toondownload.worker;

import dev.harimohan.app.toondownload.util.CloseUtil;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.LaxRedirectStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@SuppressWarnings("WeakerAccess")
public class DownloadWorker {
    private static final Logger logger = LoggerFactory.getLogger(DownloadWorker.class);

    private final EpisodeTracker episodeTracker;

    @Value("${toon_download.base.url}")
    private String url;

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
            int statusCode = httpResponse.getCode();
            if (statusCode != HttpStatus.SC_OK) {
                logger.error(String.format("Failed connecting to %s. Status code is %d.", url, statusCode));
                return Boolean.FALSE;
            }

            String fullFilePath = String.format("%s%s.mp4", episodeTracker.getDownloadFolder(), fileName);

            logger.info(String.format("Downloading video to: %s", fullFilePath));
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
            logger.info(String.format("Video saved to %s. Time taken: %d ms.", fullFilePath, (System.currentTimeMillis() - currentTime)));
            return Boolean.TRUE;
        } catch (Throwable t) {
            logger.error(String.format("Error downloading Video: %s", url), t);
        } finally {
            CloseUtil.close(httpClient);
            CloseUtil.close(httpResponse);
        }
        return Boolean.FALSE;
    }
}
