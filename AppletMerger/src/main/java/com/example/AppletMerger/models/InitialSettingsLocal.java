package com.example.AppletMerger.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InitialSettingsLocal {
    private String distrDir;
    private String userDir;
    private String backupDir;
    private boolean createBackupSubdir;
    private boolean createChangelog;
    private String fileEncoding;
}
