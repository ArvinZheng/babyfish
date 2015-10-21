package org.babyfishdemo.war3shop.web.captcha;

import java.io.IOException;
import java.io.OutputStream;

import com.github.bingoohuang.patchca.service.CaptchaService;
import com.github.bingoohuang.patchca.utils.encoder.EncoderHelper;

/**
 * @author Tao Chen
 */
public class CaptchaHolder {
    
    private CaptchaService captchaService;
    
    private String token;

    public CaptchaService getCaptchaService() {
        return captchaService;
    }

    public void setCaptchaService(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }
    
    public void generateImage(OutputStream os) throws IOException {
        this.token = EncoderHelper.getChallangeAndWriteImage(this.captchaService, "png", os);
    }

    public void validate(String token) {
        if (token == null) {
            throw new IllegalArgumentException("The captcha is null");
        }
        String oldToken = this.token;
        if (oldToken == null) {
            throw new IllegalStateException("The current captcha is not ready");
        }
        this.token = null;
        if (!oldToken.equals(token)) {
            throw new IllegalArgumentException("The captcha is wrong");
        }
    }
}
