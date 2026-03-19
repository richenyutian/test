package com.acme.support.ticket.ticket.service;

import com.acme.support.ticket.common.exception.BusinessException;
import com.acme.support.ticket.config.AppProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 本地文件存储服务。
 */
@Service
public class FileStorageService {

    private final AppProperties appProperties;

    public FileStorageService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public StoredFile store(MultipartFile file) {
        try {
            Path root = Paths.get(appProperties.file().uploadPath()).toAbsolutePath().normalize();
            Files.createDirectories(root);

            String original = file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename();
            String extension = "";
            int dotIndex = original.lastIndexOf('.');
            if (dotIndex >= 0) {
                extension = original.substring(dotIndex);
            }

            String storageFileName = UUID.randomUUID() + extension;
            Path target = root.resolve(storageFileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return new StoredFile(original, storageFileName, target.toString(), extension.replace(".", ""), file.getSize());
        } catch (IOException exception) {
            throw new BusinessException("附件保存失败：" + exception.getMessage());
        }
    }

    public Resource loadAsResource(String storagePath) {
        Path path = Paths.get(storagePath);
        if (!Files.exists(path)) {
            throw new BusinessException("附件不存在");
        }
        return new FileSystemResource(path);
    }

    public record StoredFile(
            String originalFileName,
            String storageFileName,
            String storagePath,
            String extension,
            long fileSize
    ) {
    }
}
