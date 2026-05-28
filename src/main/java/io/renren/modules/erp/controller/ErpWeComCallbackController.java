package io.renren.modules.erp.controller;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

@RestController
@RequestMapping("erp/wecom")
public class ErpWeComCallbackController {

    @Value("${erp.wecom.corp-id:wwc4f753d9acfb9b2c}")
    private String corpId;

    @Value("${erp.wecom.callback-token:7rkM3e4kZSNf5X3qaltNFfSI}")
    private String callbackToken;

    @Value("${erp.wecom.encoding-aes-key:eutAQnNR0hV63dOowEIy0hBkF7gsfSd0rs60utdxefP}")
    private String encodingAesKey;

    @GetMapping(value = "/callback", produces = "text/plain;charset=UTF-8")
    public String verifyUrl(@RequestParam("msg_signature") String msgSignature,
                            @RequestParam("timestamp") String timestamp,
                            @RequestParam("nonce") String nonce,
                            @RequestParam("echostr") String echoStr) {
        if (!StringUtils.equalsIgnoreCase(msgSignature, signature(callbackToken, timestamp, nonce, echoStr))) {
            throw new RuntimeException("企业微信回调签名校验失败");
        }
        return decryptEcho(echoStr);
    }

    private String signature(String token, String timestamp, String nonce, String encrypted) {
        try {
            String[] values = new String[]{token, timestamp, nonce, encrypted};
            Arrays.sort(values);
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] bytes = digest.digest(StringUtils.join(values).getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception e) {
            throw new RuntimeException("企业微信回调签名生成失败", e);
        }
    }

    private String decryptEcho(String encrypted) {
        try {
            byte[] aesKey = Base64.getDecoder().decode(encodingAesKey + "=");
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new IvParameterSpec(aesKey, 0, 16));
            byte[] plain = removePkcs7Padding(cipher.doFinal(Base64.getDecoder().decode(encrypted)));
            int msgLength = recoverNetworkBytesOrder(plain, 16);
            String message = new String(plain, 20, msgLength, StandardCharsets.UTF_8);
            String receiveId = new String(plain, 20 + msgLength, plain.length - 20 - msgLength, StandardCharsets.UTF_8);
            if (StringUtils.isNotBlank(receiveId) && StringUtils.isNotBlank(corpId) && !StringUtils.equals(receiveId, corpId)) {
                throw new RuntimeException("企业微信回调企业ID不匹配");
            }
            return message;
        } catch (Exception e) {
            throw new RuntimeException("企业微信回调解密失败", e);
        }
    }

    private int recoverNetworkBytesOrder(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xff) << 24)
                | ((bytes[offset + 1] & 0xff) << 16)
                | ((bytes[offset + 2] & 0xff) << 8)
                | (bytes[offset + 3] & 0xff);
    }

    private byte[] removePkcs7Padding(byte[] decrypted) {
        int pad = decrypted[decrypted.length - 1] & 0xff;
        if (pad < 1 || pad > 32) {
            pad = 0;
        }
        return Arrays.copyOfRange(decrypted, 0, decrypted.length - pad);
    }
}
