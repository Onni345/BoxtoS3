package com.example;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxItem;
import com.box.sdk.BoxLogger;
import com.box.sdk.BoxUser;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;

public class Main {
    private static final String BUCKET_NAME = "boxtoawsbucket";
    private static final String REGION = "us-east-2";
    private static final String DEVELOPER_TOKEN = "nezAbn2CgrUJu5UqQI2SkCSzfX97AxMj";
    private static final int MAX_DEPTH = 1;

    public static void main(String[] args) {
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




        // Create an S3Client using default credentials
        S3Client s3Client = S3Client.builder()
                .region(Region.of(REGION))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        File directoryPath = new File("boxFiles");
        File filesList[] = directoryPath.listFiles();
        String[] filePaths = new String[filesList.length];
        String[] fileNames = new String[filesList.length];
        int i = 0;
        for (File file : filesList) {
            filePaths[i] = file.getAbsolutePath();
            fileNames[i] = file.getName();
            i++;
        }
        // List objects in the bucket
        /*listObjects(s3Client, BUCKET_NAME);*/

        //Upload a file to the bucket
        for (int j = 0; j<filePaths.length; j++) {
            String filePath = filePaths[j];
            String objectKey = fileNames[j];
            uploadFile(s3Client, BUCKET_NAME, objectKey, filePath);
        }
        for (int w = 0; w<fileNames.length; w++) {
            System.out.println(fileNames[w] + " has been uploaded.");
        }
        /*String filePath = "path/to/your/local/file.txt";
        String objectKey = "file.txt";
        uploadFile(s3Client, BUCKET_NAME, objectKey, filePath);*/
        
        // Close the S3Client
        s3Client.close();

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

    // Method to list objects in the bucket
    private static void listObjects(S3Client s3Client, String bucketName) {
        ListObjectsRequest listObjectsRequest = ListObjectsRequest.builder()
                .bucket(bucketName)
                .build();

        ListObjectsResponse listObjectsResponse = s3Client.listObjects(listObjectsRequest);

        System.out.println("Objects in bucket '" + bucketName + "':");
        for (S3Object s3Object : listObjectsResponse.contents()) {
            System.out.println("- " + s3Object.key());
        }
    }

    // Method to upload a file to the bucket
    private static void uploadFile(S3Client s3Client, String bucketName, String objectKey, String filePath) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        // Upload the file
        s3Client.putObject(putObjectRequest, RequestBody.fromFile(new File(filePath)));

        System.out.println("File uploaded successfully.");
    }
}