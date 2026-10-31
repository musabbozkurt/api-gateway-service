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
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileIOServiceImpl implements FileIOService {

    @Override
    public DataSetResponseDto readDataSetResponseDtoFromFile() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(Objects.requireNonNull(classLoader.getResource(".")).getFile() + "/resource-data.txt");
            FileInputStream inputStream = new FileInputStream(file);
            ObjectInputStream objectInput = new ObjectInputStream(inputStream);
            Gson gson = new Gson();
            DataSetResponseDto dataSetResponseDto = gson.fromJson(String.valueOf(objectInput.readObject()), DataSetResponseDto.class);
            objectInput.close();
            return dataSetResponseDto;
        } catch (Exception ex) {
            log.error("Error occurred while reading from file : {}", ExceptionUtils.getStackTrace(ex));
        }
        return null;
    }

    @Override
    public void writeDataSetResponseDtoToFile(String emailRequestJson) {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(Objects.requireNonNull(classLoader.getResource(".")).getFile() + "/resource-data.txt");
            if (file.createNewFile()) {
                log.info("File created: {} ", file.getName());
            } else {
                log.info("File already exists: {} ", file.getName());
            }
            FileOutputStream fileOut = new FileOutputStream(file);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(emailRequestJson);
            objectOut.close();
            log.info("The Object was succesfully written to a file");
        } catch (Exception ex) {
            log.error("Error occurred while writing to file : {}", ExceptionUtils.getStackTrace(ex));
        }
    }
}
