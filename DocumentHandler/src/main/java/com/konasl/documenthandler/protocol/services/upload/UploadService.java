package com.konasl.documenthandler.protocol.services.upload;

import com.konasl.documenthandler.protocol.RestResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author H. M. Shahriar (h.m.shahriar@konasl.com)
 * @since 10/11/2019 15:46
 */

public interface UploadService {
    RestResponse uploadDocument(MultipartFile file, String documentKey);
}
