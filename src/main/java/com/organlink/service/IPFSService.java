package com.organlink.service;

import org.springframework.web.multipart.MultipartFile;

public interface IPFSService {
    String uploadFile(MultipartFile file) throws Exception;
}
