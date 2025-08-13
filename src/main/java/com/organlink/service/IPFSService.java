package com.organlink.service;

import org.springframework.web.multipart.MultipartFile;

public interface IpfsService {
    String uploadFile(MultipartFile file) throws Exception;
}
