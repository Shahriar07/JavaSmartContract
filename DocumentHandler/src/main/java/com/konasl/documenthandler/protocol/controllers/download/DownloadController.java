package com.konasl.documenthandler.protocol.controllers.download;

import com.konasl.documenthandler.constants.Constants;
import com.konasl.documenthandler.protocol.ResponseCodeEnum;
import com.konasl.documenthandler.protocol.RestResponse;
import com.konasl.documenthandler.protocol.services.download.DownloadService;
import com.konasl.documenthandler.util.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;

/**
 * @author H. M. Shahriar (h.m.shahriar@konasl.com)
 * @since 10/14/2019 12:17
 */
@RestController
public class DownloadController {

    @Autowired
    DownloadService downloadService;

    @Autowired
    Utility utility;

    @RequestMapping(value = "/api/document/download", method = RequestMethod.GET)
    public void downloadDocument(HttpServletResponse response,
                                 @RequestParam("fileName") @Valid String fileName,
                                 @RequestParam("documentKey") @Valid String documentKey
    ) throws IOException {
        RestResponse restResponse = downloadService.downloadDocument(fileName, documentKey);
        if (restResponse == null) {
            response.setStatus(Constants.FILE_DOWNLOAD_ERROR);
            return;
        }
        if (!restResponse.getResponseCode().equals(ResponseCodeEnum.SUCCESS.getCode())) {
            response.sendError(Constants.FILE_DOWNLOAD_ERROR, restResponse.getResponseMessage());
            return;
        }
        File file = (File) restResponse.getData();
        response = utility.prepareResponseFromFile(file, response);
    }

    @RequestMapping(value = "/api/document/verify", method = RequestMethod.POST)
    public RestResponse verifyDocumentHash(@RequestParam("fileName") @Valid String fileName,
                                           @RequestParam("documentKey") @Valid String documentKey,
                                           @RequestParam("documentHash") @Valid String documentHash) {
        return downloadService.verifyDocument(fileName, documentKey, documentHash);
    }

    @RequestMapping(value = "/api/document/getName", method = RequestMethod.POST)
    public RestResponse getDocumentName(@RequestParam("documentKey") @Valid String documentKey) {
        return downloadService.getDocumentName(documentKey);
    }
}
