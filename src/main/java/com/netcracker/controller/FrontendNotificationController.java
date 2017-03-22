package com.netcracker.controller;


import com.fasterxml.jackson.annotation.JsonView;
import com.netcracker.exception.ResourceNotFoundException;
import com.netcracker.model.entity.FrontendNotification;
import com.netcracker.model.view.View;
import com.netcracker.service.frontendNotification.FrontendNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
public class FrontendNotificationController {


    @Autowired
    private FrontendNotificationService frontendNotificationService;


    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    //TODO add inspection current user to, or change to principal
    @RequestMapping("/api/notification/{personId}")
    public List<FrontendNotification> getAllNotificationToPerson(@PathVariable("personId") Long personId){
        return frontendNotificationService.getNotificationToPerson(personId);
    }


    @JsonView(View.Public.class)
    @GetMapping("/test/test")
    public void addAndSendComment() throws ResourceNotFoundException {
        simpMessagingTemplate.convertAndSendToUser("person@gmail.com","/queue/notification", System.currentTimeMillis());
    }

    @DeleteMapping("/api/notification/delete")
    public ResponseEntity<?> deleteNotificationById(@RequestParam(name = "id",required=true) Long id, Principal principal){
        frontendNotificationService.deleteNotificationById(id, principal.getName());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
