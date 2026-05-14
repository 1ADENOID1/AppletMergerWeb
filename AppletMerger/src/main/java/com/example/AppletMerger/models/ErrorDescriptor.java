package com.example.AppletMerger.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorDescriptor {
    String status;
    String cause;
}
