package dev.jpa.allimio;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import dev.jpa.allimio.tool.Tool;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer{
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Windows: path = "C:/kd/deploy/resort/contents/storage";
        // ▶ file:///C:/kd/deploy/resort/contents/storage
      
        // Ubuntu: path = "/home/ubuntu/deploy/resort/contents/storage";
        // ▶ file:////home/ubuntu/deploy/resort/contents/storage
      
        // C:/kd/deploy/issue_v9jpacsc/contents/storage ->  /contents/storage -> http://localhost:9091/contents/storage;
        // http://localhost:9100/home/storage/home.jpg 접근 허용
        registry.addResourceHandler("/home/storage/**").addResourceLocations("file:///" +  Tool.getServerDir("home"));
        registry.addResourceHandler("/board/storage/**").addResourceLocations("file:///" +  Tool.getServerDir("board"));
        
        // http://localhost:9100/attach/storage/notice/images/xxx.jpg 접근 허용
        // ⭐️ AttachService.java의 purl(/attach/storage/{tname}/images 등)이 이 매핑을 그대로 탑니다.
        registry.addResourceHandler("/attach/storage/**").addResourceLocations("file:///" +  Tool.getServerDir("attach"));
        
        // C:/kd/deploy/resort/food/storage ->  /food/storage -> http://localhost:9091/food/storage;
        // registry.addResourceHandler("/food/storage/**").addResourceLocations("file:///" +  Food.getUploadDir());

        // C:/kd/deploy/resort/trip/storage ->  /trip/storage -> http://localhost:9091/trip/storage;
        // registry.addResourceHandler("/trip/storage/**").addResourceLocations("file:///" +  Trip.getUploadDir());
        
    }
 
}
