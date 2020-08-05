package com.ukrainianboyz.nearly;

import com.google.firebase.FirebaseApp;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NearlyApplicationTests {

    @Test
    void firebase_is_initialized() {
        assertEquals(1, FirebaseApp.getApps().size(), "firebase was not initialized!");
    }

}
