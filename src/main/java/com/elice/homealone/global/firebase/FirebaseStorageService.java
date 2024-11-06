package com.elice.homealone.global.firebase;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Service
public class FirebaseStorageService {

    @Value("${firebase.key-path}")
    private String firebaseKeyPath;

    public void deleteImage(String fileName) {
        Storage storage = StorageClient.getInstance().bucket().getStorage();
        BlobId blobId = BlobId.of(firebaseKeyPath, "images/" + fileName);
        storage.delete(blobId);
    }

    public void deleteImageByUrl(String imageUrl){
        try {
            String decodedUrl = URLDecoder.decode(imageUrl, StandardCharsets.UTF_8.name());// URL을 디코딩
            String fileName = decodedUrl.substring(decodedUrl.lastIndexOf('/') + 1); //URL에서 파일 이름을 추출
            if (fileName.contains("?")) { //파라미터가 포함되 있는 경우 제거
                fileName = fileName.substring(0, fileName.indexOf('?'));
            }

            BlobId blobId = BlobId.of(firebaseKeyPath, fileName);  //blodId생성
            Storage storage = StorageClient.getInstance().bucket().getStorage();
            boolean deleted = storage.delete(blobId);  //스토리지에서 blodId제거
            if (deleted) {
                System.out.println("File deleted successfully: " + fileName);
            } else {
                System.out.println("File not found: " + fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
