package giuliaciampa.YouRoster.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class Config {

    @Bean
    public Cloudinary getCloudinaryUploader(@Value("${cloudinary.apikey}") String apiKey, @Value("${cloudinary.name}") String name, @Value("${cloudinary.secret}") String secret) {
        Map<String, String> config = new HashMap<>();
        config.put("api_key", apiKey);
        config.put("cloud_name", name);
        config.put("api_secret", secret);
        return new Cloudinary(config);
    }
}
