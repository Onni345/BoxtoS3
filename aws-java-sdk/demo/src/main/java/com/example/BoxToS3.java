package com.example;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import com.box.sdk.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BoxToS3 {
    private static final String BUCKET_NAME = "mytestawsbucketz";
    private static final String REGION = "us-east-2";
    private static final String DEVELOPER_TOKEN = "Mh2JD2iAGSOaPLooc6zwnQrFqwyHdgh2";
    public static void main(String[] args) {

        BoxAPIConnection boxApi = new BoxAPIConnection(DEVELOPER_TOKEN);
        BoxFolder rootFolder = BoxFolder.getRootFolder(boxApi);
        S3Client s3Client = S3Client.builder()
                .region(Region.of(REGION))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        createS3Folder(s3Client, BUCKET_NAME, "rootFolder");
        traverseAndUpload(rootFolder, s3Client, BUCKET_NAME, "rootFolder");
    }

    private static void traverseAndUpload(BoxFolder folder, S3Client s3Client, String bucketName, String folderPath) {
        for (BoxItem.Info itemInfo : folder) {
            if (itemInfo instanceof BoxFile.Info) {
                downloadFile((BoxFile.Info) itemInfo);
                String s3FileName=((BoxFile.Info) itemInfo).getName();
                String s3FilePath=folderPath+"/"+s3FileName;
                System.out.println(s3FilePath);
                String pcFilePath="boxFiles/"+s3FileName;
                uploadS3File(s3Client, BUCKET_NAME, s3FilePath, pcFilePath);
                deleteFile((BoxFile.Info) itemInfo);
            } else if (itemInfo instanceof BoxFolder.Info) {
                BoxFolder childFolder = (BoxFolder) itemInfo.getResource();
                String childFolderPath = folderPath+"/"+itemInfo.getName();
                createS3Folder(s3Client, BUCKET_NAME, itemInfo.getName());
                System.out.println(childFolderPath);
                traverseAndUpload(childFolder, s3Client, BUCKET_NAME, childFolderPath);
            }
        }
    }
    private static void downloadFile(BoxFile.Info fileInfo) {
        try {
            String fileName = fileInfo.getName();
            System.out.println("Downloading file: " + fileName);
    

            File targetDir=new File("boxFiles");
            File targetFile=new File(targetDir, fileName);
            FileOutputStream stream = new FileOutputStream(targetFile);

            fileInfo.getResource().download(stream);
    
            stream.close();
    
            System.out.println("Downloaded file: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void deleteFile(BoxFile.Info fileInfo) {
        try {
            String fileName = fileInfo.getName();
            System.out.println("Deleting file: " + fileName);
    
            File targetDir = new File("boxFiles");
            File targetFile = new File(targetDir, fileName);
    
            if (targetFile.exists()) {
                //delete the file
                if (targetFile.delete()) {
                    System.out.println("Deleted file: " + fileName);
                } else {
                    System.out.println("Failed to delete file: " + fileName);
                }
            } else {
                System.out.println("File not found: " + fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static void uploadS3File(S3Client s3Client, String bucketName, String objectKey, String filePath) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        //upload the file
        s3Client.putObject(putObjectRequest, RequestBody.fromFile(new File(filePath)));

        System.out.println("File uploaded successfully.");
    }
    public static void createS3Folder(S3Client s3Client, String bucketName, String folderName) {
        if (folderName.contains("rootFolder")) {
            String folderKey = folderName+"/";
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(folderKey)
                    .build(),
                    RequestBody.empty());

            System.out.println("Folder '" + folderName + "' created in bucket '" + bucketName + "'.");
        }
        
    }
}