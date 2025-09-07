package dddan.paper_summary.auth.controller;

import dddan.paper_summary.auth.dto.RegisterRequest;
import dddan.paper_summary.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Map;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    // ✅ 로그인은 Spring Security가 처리하고, 성공/실패 리다이렉트만 처리
    @GetMapping("/success")
    public ResponseEntity<?> loginSuccess(Principal principal) {
        return ResponseEntity.ok().body(
                Map.of("message", "로그인 성공", "userId", principal.getName())
        );
    }

    @GetMapping("/fail")
    public ResponseEntity<?> loginFail(HttpServletRequest request) {
        Exception exception = (Exception) request.getSession().getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
        System.out.println("[로그] 로그인 실패: " + (exception != null ? exception.getMessage() : "이유 없음"));
        return ResponseEntity.status(401).body(Map.of("error", "로그인 실패"));
    }

}
