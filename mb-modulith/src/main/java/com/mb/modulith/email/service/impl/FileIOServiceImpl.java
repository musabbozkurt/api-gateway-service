package com.mb.modulith.email.service.impl;

import com.google.gson.Gson;
import com.mb.modulith.email.client.emailresource.dto.DataSetResponseDto;
import com.mb.modulith.email.service.FileIOService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileIOServiceImpl implements FileIOService {

    @Override
    public DataSetResponseDto readDataSetResponseDtoFromFile() {
        try {
            File file = dataFile();
            if (!file.exists()) {
                log.info("File does not exist yet, nothing to read: {}", file);
                return null;
            }
            try (ObjectInputStream objectInput = new ObjectInputStream(new FileInputStream(file))) {
                return new Gson().fromJson(String.valueOf(objectInput.readObject()), DataSetResponseDto.class);
            }
        } catch (Exception ex) {
            log.error("Error occurred while reading from file : {}", ExceptionUtils.getStackTrace(ex));
        }
        return null;
    }

    // The classpath is not a writable directory inside the packaged jar (Docker), so use the temp directory.
    private File dataFile() {
        return new File(System.getProperty("java.io.tmpdir"), "resource-data.txt");
    }

    @Override
    public void writeDataSetResponseDtoToFile(String emailRequestJson) {
        try {
            File file = dataFile();
            if (file.createNewFile()) {
                log.info("File created: {} ", file.getName());
            } else {
                log.info("File already exists: {} ", file.getName());
            }
            try (ObjectOutputStream objectOut = new ObjectOutputStream(new FileOutputStream(file))) {
                objectOut.writeObject(emailRequestJson);
            }
            log.info("The Object was successfully written to a file");
        } catch (Exception ex) {
            log.error("Error occurred while writing to file : {}", ExceptionUtils.getStackTrace(ex));
        }
    }
}
