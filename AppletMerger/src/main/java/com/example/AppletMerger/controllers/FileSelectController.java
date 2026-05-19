package com.example.AppletMerger.controllers;

import com.example.AppletMerger.mergerMethods.MergerUtils;
import com.example.AppletMerger.models.ErrorDescriptor;
import com.example.AppletMerger.models.FilePair;
import com.example.AppletMerger.models.InitialSettingsLocal;
import com.example.AppletMerger.models.MergeResult;
import com.google.gson.JsonElement;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FileSelectController {

    @RequestMapping("/fileSelect")
    public String index() {
        return "FileSelect";
    }

    @RequestMapping(value = "/fileSelect/get", method = RequestMethod.GET)
    public ResponseEntity<?> getFilePairList(HttpSession session) {
        InitialSettingsLocal settings;
        try {
            settings = (InitialSettingsLocal) session.getAttribute("settings");

            List<FilePair> parsedFiles = MergerUtils.parseFiles(settings.getDistrDir(), settings.getUserDir());



            List<MergeResult> result = new ArrayList<>();
            for (FilePair i : parsedFiles) {
                result.add(new MergeResult(i, MergerUtils.merge(i.getUserSettings(), i.getDistrSettings())));

            }
            session.setAttribute("filesList", result);

            List<String> returnFileNames = new ArrayList<>();
            for (MergeResult i : result) {
                returnFileNames.add(i.getSource().getName());
            }

            return ResponseEntity.ok(returnFileNames);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(new ErrorDescriptor("FileReadError", "При чтении файлов произошла ошибка"));
        } catch (ClassCastException | NullPointerException readSettingsExc) {
            return ResponseEntity.internalServerError().body(new ErrorDescriptor("SessionReadError", "Не удалось прочитать настройки. Задайте их ещё раз"));
        }
    }

    @RequestMapping(value = "/fileSelect/getFields", method = RequestMethod.GET)
    public ResponseEntity<?> getFields(HttpSession session, @RequestParam String filePath) {
        try {
            Map<String, String> result = new LinkedHashMap<>();

            List<MergeResult> fileList = (List<MergeResult>) session.getAttribute("filesList");

            for (MergeResult mr : fileList) {

                if (mr.getSource().getName().equals(filePath)) {

                    for (Map.Entry<String, JsonElement> elem : mr.getResult().getJsonMap().entrySet()) {
                        result.put(elem.getKey(), elem.getValue().toString());
                    }
                }
            }

            return ResponseEntity.ok(result);

        } catch (ClassCastException | NullPointerException readSettingsExc) {
            return ResponseEntity.internalServerError().body(new ErrorDescriptor("FileFieldsReadError", "Не удалось прочитать поля файла. Попробуйте ещё раз"));
        }
    }
}
