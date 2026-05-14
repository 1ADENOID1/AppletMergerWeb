package com.example.AppletMerger.mergerMethods;

import com.example.AppletMerger.models.FilePair;
import com.example.AppletMerger.models.InitialSettingsLocal;
import com.example.AppletMerger.models.JsonMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MergerUtils {
    public static boolean checkSettings(InitialSettingsLocal is) {
        File distrDirFile = new File(is.getDistrDir());
        if (!Files.exists(distrDirFile.toPath()) || !distrDirFile.isDirectory()) {
            return false;
        }

        File userDirFile = new File(is.getUserDir());
        if (!Files.exists(userDirFile.toPath()) || !userDirFile.isDirectory()) {
            return false;
        }

        File backupDirFile = new File(is.getBackupDir());
        if (!Files.exists(backupDirFile.toPath()) || !backupDirFile.isDirectory()) {
            return false;
        }

        if (distrDirFile.equals(userDirFile) || distrDirFile.equals(backupDirFile) || userDirFile.equals(backupDirFile)) {
            return false;
        }

        return true;
    }

    public static String getRelativePath(String baseDirectoryPath, String incFilePath) {
        Path basePath = Paths.get(baseDirectoryPath);
        Path incPath = Paths.get(incFilePath);
        return basePath.relativize(incPath).toString();
    }

    public static List<File> filesToList(File dir) {
        return filesToList(dir, true);
    }
    public static List<File> filesToList(File dir, boolean ignoreNotJsonFiles) {

        if (dir.listFiles() == null) {
            return new ArrayList<>();
        }

        List<File> expandedFileList = new ArrayList<>(Arrays.asList(dir.listFiles()));

        boolean hasDirs;
        do {
            hasDirs = false;

            ListIterator<File> distrExpandedFileListIterator = expandedFileList.listIterator();
            while (distrExpandedFileListIterator.hasNext()) {
                File file = distrExpandedFileListIterator.next();

                if (file.isDirectory()) {
                    hasDirs = true;

                    File[] includedFiles = file.listFiles();
                    distrExpandedFileListIterator.remove();

                    if (includedFiles != null) {
                        for (File i : includedFiles) {
                            distrExpandedFileListIterator.add(i);
                        }
                    }
                }
            }
        } while (hasDirs);

        if (ignoreNotJsonFiles) {
            expandedFileList.removeIf(file -> !file.getName().endsWith(".json"));
        }

        return new ArrayList<>(expandedFileList);
    }

    public static List<FilePair> parseFiles(String distrDir, String userDir) throws IOException {
        List<FilePair> parsedFilePairList = new ArrayList<>();
        List<String> overwrittenFiles = new ArrayList<>();

        List<File> distrExpandedFileList = filesToList(new File(distrDir));
        List<File> userExpandedFileList = filesToList(new File(userDir));

        for (File distrFile : distrExpandedFileList) {

            JsonMap distrFileJsonMap;
            JsonMap userFileJsonMap;
            boolean pairFounded = false;

            System.out.println("Parsing distributive file: " + distrFile.getAbsolutePath());
            distrFileJsonMap = new JsonMap(distrFile);

            String distrRelativeFilePath = getRelativePath(distrDir, distrFile.getAbsolutePath());

            for (File userFile : userExpandedFileList) {

                String userRelativeFilePath = getRelativePath(userDir, userFile.getAbsolutePath());

                if (distrRelativeFilePath.equals(userRelativeFilePath) && distrFile.isFile() && userFile.isFile()) {
                    pairFounded = true;
                    System.out.println("Parsing user file: " + userFile.getAbsolutePath());
                    userFileJsonMap = new JsonMap(userFile);
                    parsedFilePairList.add(new FilePair(distrRelativeFilePath, distrFileJsonMap, userFileJsonMap));
                    System.out.println("Parsed file pair: " + distrRelativeFilePath + " " + userRelativeFilePath);

                    break;
                }
            }

            if (!pairFounded && distrFile.isFile()) {
                System.out.println("Parsed distributive file (user file is not found, distributive file will be copied: " + distrRelativeFilePath);
                overwrittenFiles.add(distrRelativeFilePath);
                userFileJsonMap = new JsonMap(distrFileJsonMap);
                parsedFilePairList.add(new FilePair(distrRelativeFilePath, distrFileJsonMap, userFileJsonMap));
            }
        }

        return parsedFilePairList;
    }

    public static JsonMap merge(JsonMap userSettings, JsonMap defaultSettings) {
        Map<String, JsonElement> mergedMap = new LinkedHashMap<>();

        boolean userRootArrayFounded = false;
        boolean distrRootArrayFounded = false;


        if (defaultSettings.getJsonMap().size() == 0) {           //Если defaultSettings пуст, то цикл не откроется
            mergedMap.putAll(userSettings.getJsonMap());
            //this.changes.add("Distributive settings is empty. Saved all user settings");
        } else {
            for (Map.Entry<String, JsonElement> defaultElem : defaultSettings.getJsonMap().entrySet()) {
                boolean foundedInUserElements = false;

                if (defaultElem.getKey() == null) {
                    if (defaultSettings.getJsonMap().size() != 1) {
                        throw new JsonParseException("Json root is array, but json map length is not 1");
                    }

                    distrRootArrayFounded = true;

                }
                for (Map.Entry<String, JsonElement> userElem : userSettings.getJsonMap().entrySet()) {

                    if (userElem.getKey() == null) {
                        if (userSettings.getJsonMap().size() != 1) {
                            throw new JsonParseException("Json root is array, but json map length is not 1");
                        }

                        mergedMap.put(null, userElem.getValue());

                        /*if (distrRootArrayFounded && !userElem.getValue().equals(defaultElem.getValue())) {
                            changes.add("Value of root array is overwritten. Distributive value: " + defaultElem.getValue() + " Saved user value: " + userElem.getValue());
                        } else if (!distrRootArrayFounded) {
                            changes.add("Distributive root object is overwritten by user's root array. Distributive value: " + defaultElem.getValue() + " Saved user value: " + userElem.getValue());
                        }*/

                        userRootArrayFounded = true;
                        break;
                    }
                    /*if (defaultElem.getKey() == null) {
                        this.changes.add("Distributive root array is overwritten by user root object. Distributive value: " + defaultElem.getValue() + " Saved user root object");
                    }*/

                    if (!distrRootArrayFounded && ((userElem.getKey().startsWith(defaultElem.getKey())
                            && (userElem.getKey().length() == defaultElem.getKey().length() || userElem.getKey().charAt(defaultElem.getKey().length()) == userSettings.getSeparator())
                    )
                            || (defaultElem.getKey().startsWith(userElem.getKey())
                            && (defaultElem.getKey().length() == userElem.getKey().length() || defaultElem.getKey().charAt(userElem.getKey().length()) == userSettings.getSeparator())
                    )

                    )) {   //Взятие значения из пользовательских файлов если оно задано (

                        foundedInUserElements = true;
                        mergedMap.put(userElem.getKey(), userElem.getValue());

                        /*if (!userElem.getValue().equals(defaultElem.getValue())) {
                            this.changes.add("Value overwritten. Key: \"" + userElem.getKey() + "\" Distributive value: " + defaultElem.getValue() + " Saved user value: " + userElem.getValue());
                        }*/
                    }
                }

                if (userRootArrayFounded || distrRootArrayFounded) {

                    break;
                }

                if (!foundedInUserElements) {                                               //Добавление новых полей из дистрибутива
                    mergedMap.put(defaultElem.getKey(), defaultElem.getValue());
                    //this.changes.add("Added new value from distributive. Key: \"" + defaultElem.getKey() + "\". Saved value: " + defaultElem.getValue());
                }
            }

            for (Map.Entry<String, JsonElement> userElemNew : userSettings.getJsonMap().entrySet()) {
                for (Map.Entry<String, JsonElement> defaultElemNew : defaultSettings.getJsonMap().entrySet()) {               //Сохранение полей, отсутствующих в дистрибутиве
                    if (!userRootArrayFounded &&
                            (distrRootArrayFounded || !userElemNew.getKey().startsWith(defaultElemNew.getKey()) && !mergedMap.containsKey(userElemNew.getKey()))
                    ) {
                        mergedMap.put(userElemNew.getKey(), userElemNew.getValue());
                        //this.changes.add("Custom user value (not found in distributive). Key: \"" + userElemNew.getKey() + "\". Saved value: " + userElemNew.getValue());
                    }
                }
            }
        }


        return new JsonMap(mergedMap);
    }
}
