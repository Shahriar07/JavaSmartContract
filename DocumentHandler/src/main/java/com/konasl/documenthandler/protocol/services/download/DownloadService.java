package com.konasl.documenthandler.protocol.services.download;

import com.konasl.documenthandler.protocol.RestResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author H. M. Shahriar (h.m.shahriar@konasl.com)
 * @since 10/14/2019 12:17
 */
public interface DownloadService {

    RestResponse downloadDocument(String fileName, String documentKey);
}
