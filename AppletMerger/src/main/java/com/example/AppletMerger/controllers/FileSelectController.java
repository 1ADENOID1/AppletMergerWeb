package com.example.AppletMerger.controllers;

import com.example.AppletMerger.mergerMethods.MergerUtils;
import com.example.AppletMerger.models.*;
import com.google.gson.*;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.Session;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.FileWriter;
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
        List<String> returnFileNames = new ArrayList<>();
        try {
            settings = (InitialSettingsLocal) session.getAttribute("settings");

            if (session.getAttribute("filesList") == null) {     //TODO: если список файлов есть - не перезапускать слияние (обновили страницу). Можно сделать конпку reread (повесить на query параметр)

                List<FilePair> parsedFiles = MergerUtils.parseFiles(settings.getDistrDir(), settings.getUserDir());

                List<MergeResult> result = new ArrayList<>();
                for (FilePair i : parsedFiles) {
                    result.add(new MergeResult(i, MergerUtils.merge(i.getUserSettings(), i.getDistrSettings())));

                }

                session.setAttribute("filesList", result);

                for (MergeResult i : result) {
                    returnFileNames.add(i.getSource().getName());
                }
            } else {
                List<MergeResult> savedFileList = (List<MergeResult>) session.getAttribute("filesList");
                for (MergeResult i : savedFileList) {
                    returnFileNames.add(i.getSource().getName());
                }
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

    @RequestMapping(value = "fileSelect/save", method = RequestMethod.GET)
    public ResponseEntity<?> saveFiles(HttpSession session) {

        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        try {
            List<MergeResult> fileList = (List<MergeResult>) session.getAttribute("filesList");
            InitialSettingsLocal settings = (InitialSettingsLocal) session.getAttribute("settings");
            for (MergeResult mr : fileList) {
                File curentFile = new File(settings.getUserDir() + File.separator + mr.getSource().getName());
                FileWriter fw = new FileWriter(curentFile);
                fw.write(gson.toJson(mr.getResult().toJson()));
                fw.close();
            }

            session.invalidate();
            return ResponseEntity.ok(Map.of("success", "true",
                    "redirectUrl", "/fileSelect"));

        } catch (ClassCastException | NullPointerException saveFilesExc) {
            return ResponseEntity.internalServerError().body(new ErrorDescriptor("SaveFilesReadError", "Не удалось сохранить файлы. Попробуйте ещё раз"));
        } catch (IOException saveFilesExc) {
            return ResponseEntity.internalServerError().body(new ErrorDescriptor("SaveFilesError", "Не удалось сохранить файлы. Ошибка файловой системы"));
        }
    }
}
