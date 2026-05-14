package com.example.AppletMerger.controllers;

import com.example.AppletMerger.mergerMethods.MergerUtils;
import com.example.AppletMerger.models.ErrorDescriptor;
import com.example.AppletMerger.models.InitialSettingsLocal;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Controller
public class StartPageController {

    @RequestMapping("/")
    public String index() {
        return "StartPage";
    }

    @RequestMapping(value = "/startpage/get", method = RequestMethod.POST)
    public ResponseEntity<?> getSettings(HttpSession session, @RequestBody InitialSettingsLocal requestBody) {

        if (MergerUtils.checkSettings(requestBody)) {
            session.setAttribute("settings", requestBody);
            return ResponseEntity.ok(Map.of("success", "true",
                    "redirectUrl", "/fileSelect"));
        } else return ResponseEntity.badRequest().body(new ErrorDescriptor("failed", "Некорректно заданы пути к директориям"));
    }
}
