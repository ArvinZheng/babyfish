package org.babyfishdemo.war3shop.bll;

import org.babyfishdemo.war3shop.entities.TemporaryUploadedFile;
import org.babyfishdemo.war3shop.entities.TemporaryUploadedFile__;

/**
 * @author Tao Chen(&#38472;&#28059;)
 */
public interface UploadService {
    
    TemporaryUploadedFile getUploadedImage(TemporaryUploadedFile__ ... queryPaths);
    
    TemporaryUploadedFile getAndDeleteUploadedImage(TemporaryUploadedFile__ ... queryPaths);
    
    void prepareToUpload();
    
    void uploadImage(String mimeType, byte[] image);
    
    void cancelUpload(String key);
}
