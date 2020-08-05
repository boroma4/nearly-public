package com.ukrainianboyz.nearly;

import com.google.firebase.FirebaseApp;
import com.ukrainianboyz.nearly.service.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class ApplicationMonitor {

    @Autowired
    private FirebaseService firebase;

    /*
     * Ran on app start
     */
    @PostConstruct
    public void init(){
        firebase.init();
    }

    /*
     * Ran on app stop
     */
    @PreDestroy
    public void cleanup(){
        firebase.cleanup();
    }
}
