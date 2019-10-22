package com.konasl.documenthandler.protocol.controllers.upload;

import com.konasl.documenthandler.protocol.RestResponse;
import com.konasl.documenthandler.protocol.services.upload.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

/**
 * Controller to handle document upload
 *
 * @author H. M. Shahriar (h.m.shahriar@konasl.com)
 * @since 10/11/2019 15:44
 */

@RestController
public class UploadController {

    @Autowired
    UploadService uploadService;

    @RequestMapping(value = "/api/document/upload", method = RequestMethod.POST)
    public RestResponse uploadDocument(@RequestParam("document") @Valid MultipartFile document,
                                       @RequestParam("documentKey") @Valid String documentKey) {
        return uploadService.uploadDocument(document, documentKey);
    }
}
