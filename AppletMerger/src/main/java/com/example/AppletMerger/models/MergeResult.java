package com.example.AppletMerger.models;

import com.example.AppletMerger.mergerMethods.MergerUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
public class MergeResult {
    FilePair source;
    JsonMap result;
}
