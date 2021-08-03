package com.harrymdev.toondownload.worker;

import com.harrymdev.toondownload.model.EpisodeStatus;
import com.harrymdev.toondownload.util.CloseUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@SuppressWarnings("WeakerAccess")
public class EpisodeTracker {
    private static final Logger logger = LoggerFactory.getLogger(EpisodeTracker.class);

    private static final String CARTOON_PATH_PROPERTY = "toon_download.download.%s.target.path.root";
    private static final String EPISODE_LIST_FILE_NAME = "episodeList";
    private static final String IGNORE_LIST_FILE_NAME = "ignoreList";

    private final String cartoonName;
    private final Set<String> savedFiles = new HashSet<>();
    private final Set<String> filesToIgnore = new HashSet<>();

    @Autowired
    private ApplicationContext applicationContext;

    private String downloadFolder;

    public String getDownloadFolder() {
        return downloadFolder;
    }

    @Autowired
    public EpisodeTracker(String cartoonName) {
        this.cartoonName = cartoonName;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @PostConstruct
    private void setup() throws IOException {
        String cartoonDownloadPathProperty = String.format(CARTOON_PATH_PROPERTY, cartoonName);
        downloadFolder = applicationContext.getEnvironment().getProperty(cartoonDownloadPathProperty);
        if (downloadFolder == null) {
            throw new IllegalArgumentException(String.format("Value for %s not provided.", cartoonDownloadPathProperty));
        }
        File filePath = new File(downloadFolder);
        filePath.mkdirs();
        FileReader episodeListReader = null;
        FileReader ignoreListReader = null;

        try {
            File episodeList = getEpisodeList();
            if (episodeList != null) {
                episodeListReader = new FileReader(episodeList);
                Arrays.stream(IOUtils
                        .toString(episodeListReader)
                        .split("\n"))
                        .filter(fileName -> !fileName.isEmpty())
                        .forEach(savedFiles::add);
            }
            File ignoreList = getIgnoreList();
            if (ignoreList.exists()) {
                ignoreListReader = new FileReader(ignoreList);
                Arrays.stream(IOUtils
                        .toString(ignoreListReader)
                        .split("\n"))
                        .filter(fileName -> !fileName.isEmpty())
                        .forEach(filesToIgnore::add);
            }
        } catch (IOException e) {
            logger.error("Failed to access episode list.");
            throw e;
        } finally {
            CloseUtil.close(episodeListReader);
            CloseUtil.close(ignoreListReader);
        }
    }

    private File getIgnoreList() {
        return new File(String.format("%s%s", downloadFolder, IGNORE_LIST_FILE_NAME));
    }

    public void addEpisodeToList(String episodeName) throws IOException {
        savedFiles.add(episodeName);
        File episodeList = getEpisodeList();
        FileWriter fileWriter = null;

        try {
            fileWriter = new FileWriter(episodeList, true);
            fileWriter.write(episodeName + "\n");
        } finally {
            CloseUtil.close(fileWriter);
        }
    }

    public EpisodeStatus shouldDownloadFile(String episodeName) {
        return savedFiles.contains(episodeName) ? EpisodeStatus.Downloaded :
                filesToIgnore.contains(episodeName) ? EpisodeStatus.Ignored :
                        EpisodeStatus.Pending;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private File getEpisodeList() throws IOException {
        File episodeList = new File(String.format("%s%s", downloadFolder, EPISODE_LIST_FILE_NAME));
        episodeList.createNewFile();
        return episodeList;
    }
}
