package com.example.AppletMerger.controllers;

import com.example.AppletMerger.mergerMethods.MergerUtils;
import com.example.AppletMerger.models.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.Session;
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
            if (session.getAttribute("filesList") == null) {     //TODO: если список файлов есть - не перезапускать слияние (обновили страницу). Можно сделать конпку reread
                session.setAttribute("filesList", result);
            }

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

    @RequestMapping(value = "/fileSelect/setFields", method = RequestMethod.POST)
    public ResponseEntity<?> setFields(HttpSession session, @RequestParam String name, @RequestBody Map<String, String> body) {
        try {
            List<MergeResult> fileList = (List<MergeResult>) session.getAttribute("filesList");
            for (MergeResult mr : fileList) {
                if (mr.getSource().getName().equals(name)) {
                    JsonMap jsonMap = new JsonMap(mr.getResult());
                    for (Map.Entry<String, String> element : body.entrySet()) {
                        jsonMap.changePropertyValue(element.getKey(), JsonParser.parseString(element.getValue()));
                        System.out.println("Key: " + jsonMap.getJsonMap().get(element.getKey()));
                    }
                    mr.setResult(jsonMap);
                    System.out.println("KeyAfter: " + jsonMap.getJsonMap().get("appLogo█default"));

                    break;
                }
            }

            session.setAttribute("filesList", fileList);

            return ResponseEntity.ok().build();

        } catch (ClassCastException | NullPointerException readSettingsExc) {
            return ResponseEntity.internalServerError().body(new ErrorDescriptor("SaveFieldsReadError", "Не удалось сохранить поля файла. Попробуйте ещё раз"));
        } catch (JsonSyntaxException jsonExc) {
            return ResponseEntity.internalServerError().body(new ErrorDescriptor("FieldValueError", "Не удалось сохранить поля файла. Некорректное значение поля"));
        }
    }
}
