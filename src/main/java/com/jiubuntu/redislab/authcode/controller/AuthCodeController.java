package com.jiubuntu.redislab.authcode.controller;

import com.jiubuntu.redislab.authcode.dto.SendCodeRequest;
import com.jiubuntu.redislab.authcode.dto.SendCodeResponse;
import com.jiubuntu.redislab.authcode.dto.VerifyCodeRequest;
import com.jiubuntu.redislab.authcode.dto.VerifyCodeResponse;
import com.jiubuntu.redislab.common.redis.RedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Auth-Code", description = "TTL 기반 임시 인증번호 예제 API (해싱된 전화번호 + SET TTL)")
public class AuthCodeController {

    private static final Duration AUTH_CODE_TTL = Duration.ofSeconds(300);
    private final RedisService redisService;

    @Operation(summary = "인증번호 발송", description = "6자리 인증번호를 생성해 해싱된 전화번호를 key로 TTL 300초 동안 저장한다.")
    @PostMapping(value = "/auth/send")
    public SendCodeResponse sendVerificationCode(@RequestBody SendCodeRequest request) {
        String code = generateCode();

        // 개인정보(휴대폰 번호)는 해싱 후 Key로 저장
        redisService.set(cacheKey(request.getPhone()), code, AUTH_CODE_TTL);

        // 실제로는 여기서 SMS 발송 처리를 한다고 가정
        log.info("[SMS 발송] To: {}, Code: {}", request.getPhone(), code);

        return new SendCodeResponse("Verification code sent", code, AUTH_CODE_TTL.getSeconds());
    }

    @Operation(summary = "인증번호 검증", description = "저장된 인증번호와 입력값을 비교해 일치할 때만 삭제한다. (오타로 실패한 경우 재시도할 수 있도록 실패 시에는 삭제하지 않음)")
    @PostMapping(value = "/auth/verify")
    public VerifyCodeResponse verifyCode(@RequestBody VerifyCodeRequest request) {
        String cacheKey = cacheKey(request.getPhone());
        String savedCode = redisService.get(cacheKey, String.class);

        if (savedCode == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code expired or not requested");
        }
        if (!savedCode.equals(request.getInputCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid code");
        }

        // 인증 성공 시 보안을 위해 즉시 삭제
        redisService.delete(cacheKey);

        return new VerifyCodeResponse("Authentication successful");
    }

    private String cacheKey(String phone) {
        return "auth:code:" + sha256(phone);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private String generateCode() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
    }
}
