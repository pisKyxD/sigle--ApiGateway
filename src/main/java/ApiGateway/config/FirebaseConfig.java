package ApiGateway.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @Value("${FIREBASE_SERVICE_ACCOUNT:}")
    private String firebaseServiceAccount;

    @PostConstruct
    public void initFirebase() throws IOException {
        InputStream serviceAccount;

        if (firebaseServiceAccount != null && !firebaseServiceAccount.isEmpty()) {
            serviceAccount = new ByteArrayInputStream(
                firebaseServiceAccount.getBytes(StandardCharsets.UTF_8));
        } else {
            File externalFile = new File("/app/firebase-service-account.json");
            if (externalFile.exists()) {
                serviceAccount = new FileInputStream(externalFile);
            } else {
                serviceAccount = getClass().getClassLoader()
                    .getResourceAsStream("firebase-service-account.json");
            }
        }

        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(
                GoogleCredentials.fromStream(serviceAccount))
            .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }
    }
}