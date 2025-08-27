package dddan.paper_summary.auth.service;

import dddan.paper_summary.auth.domain.User;
import dddan.paper_summary.auth.dto.RegisterRequest;
import dddan.paper_summary.auth.dto.LoginRequest;
import dddan.paper_summary.auth.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public ResponseEntity<?> login(LoginRequest request) {
        User user = userRepository.findByUserId(request.getUserId())
                .orElse(null);  // Optional 제거

        if (user == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "존재하지 않는 사용자입니다."));
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "비밀번호가 일치하지 않습니다."));
        }

        return ResponseEntity.ok(Map.of("message", "로그인 성공"));
    }

    private final PasswordEncoder passwordEncoder;

    public ResponseEntity<?> register(RegisterRequest req) {
        if (userRepository.existsByUserIdIgnoreCase(req.getUserId())) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "이미 존재하는 사용자입니다."));
        }

        User user = new User();
        user.setUserId(req.getUserId());
        user.setName(req.getName());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));

        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "회원가입 성공"));
    }
}
