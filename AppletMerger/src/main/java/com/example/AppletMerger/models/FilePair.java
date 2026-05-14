package com.example.AppletMerger.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FilePair {
    private String name;
    private JsonMap distrSettings;
    private JsonMap userSettings;
}
