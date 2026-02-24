package com.dbms.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class FileEntity {
    private String projectId;
    private String dbId;
    private String operator;
    private MultipartFile file;
}
