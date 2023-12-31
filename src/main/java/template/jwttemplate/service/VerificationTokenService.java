package template.jwttemplate.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import template.jwttemplate.data.VerificationToken;
import template.jwttemplate.exception.VerificationTokenNotFoundException;
import template.jwttemplate.repository.VerificationTokenRepository;

@Service
@RequiredArgsConstructor
public class VerificationTokenService {

    private final VerificationTokenRepository verificationTokenRepository;

    private final UserService userService;

    public void saveVerificationToken(VerificationToken token) {
        verificationTokenRepository.save(token);
    }

    public VerificationToken getToken(String token) {
        return verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new VerificationTokenNotFoundException("Confirmation Token Not found"));
    }

    private int setVerifiedAt(String token) {
        return verificationTokenRepository.updateVerifiedAt(
                token, LocalDateTime.now());
    }

    @Transactional
    public String verifyToken(String token) {
        VerificationToken verificationToken = getToken(token);

        if (verificationToken.getVerifiedAt() != null) {
            throw new IllegalStateException("email already confirmed");
        }

        LocalDateTime expiredAt = verificationToken.getExpiresAt();

        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Verification token expired");
        }

        setVerifiedAt(token);

        userService.verifyUserEmail(
                verificationToken.getUser().getEmail());
        return "Email successfuly confirmed";
    }

}