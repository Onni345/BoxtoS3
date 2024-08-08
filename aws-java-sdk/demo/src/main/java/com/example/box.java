package com.example;
import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxItem;
import com.box.sdk.BoxLogger;
import com.box.sdk.BoxUser;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;

public class box {
    private static final String DEVELOPER_TOKEN = "rOkf49ODQozpOSTjUlGsAiKxgRYO9pmy";
    private static final int MAX_DEPTH = 1;


    public static void main(String[] args) {
        // Limit logging messages to prevent polluting the output.
        System.out.println("Hello World");
        BoxLogger.defaultLogger().setLevelToWarning();

        BoxAPIConnection api = new BoxAPIConnection(DEVELOPER_TOKEN);

        BoxUser.Info userInfo = BoxUser.getCurrentUser(api).getInfo();
        System.out.format("Welcome, %s <%s>!\n\n", userInfo.getName(), userInfo.getLogin());

        //BoxFolder rootFolder = BoxFolder.getRootFolder(api);
        //listFolder(rootFolder, 0);

        BoxFolder rootFolder = BoxFolder.getRootFolder(api);

        // List all items in the root folder and its subfolders
        listFolder(rootFolder, 0);

        
    }

    private static void listFolder(BoxFolder folder, int depth) {
        for (BoxItem.Info itemInfo : folder) {
            if (itemInfo instanceof BoxFile.Info) {
                // Download the file if it's a BoxFile
                downloadFile((BoxFile.Info) itemInfo);
            } else if (itemInfo instanceof BoxFolder.Info && depth < MAX_DEPTH) {
                // Continue listing if it's a BoxFolder and depth hasn't reached the maximum
                BoxFolder childFolder = (BoxFolder) itemInfo.getResource();
                listFolder(childFolder, depth + 1);
            }
        }
    }

    private static void downloadFile(BoxFile.Info fileInfo) {
        try {
            String fileName = fileInfo.getName();
            System.out.println("Downloading file: " + fileName);
    
            // Create FileOutputStream to save the downloaded file
            File targetDir=new File("boxFiles");
            File targetFile=new File(targetDir, fileName);
            FileOutputStream stream = new FileOutputStream(targetFile);
    
            // Download the file
            fileInfo.getResource().download(stream);
    
            // Close the stream
            stream.close();
    
            System.out.println("Downloaded file: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
