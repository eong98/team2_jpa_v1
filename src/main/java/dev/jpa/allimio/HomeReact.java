package dev.jpa.allimio;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeReact {
  // http://localhost:9102
  @GetMapping({"/"})
  public String index() {
    return "forward:/index.html"; // /src/main/resources/static/index.html
  }
  
}
