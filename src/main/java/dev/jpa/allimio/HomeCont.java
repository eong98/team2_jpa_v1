//package dev.jpa.allimio;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//import dev.jpa.resort.tool.Security;
//
//@Controller
//public class HomeCont {
//  @Autowired
//  private Security security;
//  
//  public HomeCont() {
//    System.out.println("-> HomeCont created.");
//  }
//  
//  // URI Command 패턴 자동 지원, 'msg.seoul' 처럼 실제 존재하는 파일이 아님, 인터넷 주소를 명령어로 사용.
//  // http://localhost:9100/backend -> 주소 분석 -> GET/POST -> 메소드 실행
//  @GetMapping(value={"/backend"})
//  @ResponseBody  // 출력 내용이 html 파일의 내용임으로 브러우저가 바로 출력함.
//  public String msg() {
//    System.out.println("-> 암호화 테스트: " + security.aesEncode("1234"));
//    
//    return "<h2>KD11 Backend server running...</h2>";
//  }
//
//}
