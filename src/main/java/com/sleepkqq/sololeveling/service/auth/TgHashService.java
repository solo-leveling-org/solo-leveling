package com.sleepkqq.sololeveling.service.auth;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.sleepkqq.sololeveling.view.auth.TgAuthData;
import java.net.URLDecoder;
import java.util.Map;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TgHashService {

  private static final String TG_SECRET_KEY = "WebAppData";

  private static final String HASH_FIELD = "hash";

  @Value("${telegram.bot.token}")
  private String tgBotToken;

  public boolean checkHash(TgAuthData tgAuthData) {
    var parsedQueryString = parseQueryString(tgAuthData.initData());
    return validateTelegramAuth(parsedQueryString, parsedQueryString.get(HASH_FIELD));
  }

  private boolean validateTelegramAuth(Map<String, String> paramMap, String receivedHash) {
    var dataString = EntryStream.of(paramMap)
        .filterKeys(k -> !HASH_FIELD.equals(k))
        .sorted(Map.Entry.comparingByKey())
        .mapKeyValue((k, v) -> k + "=" + v)
        .joining("\n");

    var tgBotTokenHash = getHash(TG_SECRET_KEY.getBytes(UTF_8), tgBotToken);
    var dataHash = getHash(tgBotTokenHash, dataString);

    var calculatedHash = Hex.toHexString(dataHash);
    return calculatedHash.equals(receivedHash);
  }

  private byte[] getHash(byte[] keyBytes, String data) {
    var hmac = new HMac(new SHA256Digest());
    hmac.init(new KeyParameter(keyBytes));
    var hashBytes = new byte[hmac.getMacSize()];
    hmac.update(data.getBytes(UTF_8), 0, data.length());
    hmac.doFinal(hashBytes, 0);

    return hashBytes;
  }

  private Map<String, String> parseQueryString(String queryString) {
    return StreamEx.of(queryString.split("&"))
        .map(pair -> pair.split("=", 2))
        .mapToEntry(
            keyValue -> URLDecoder.decode(keyValue[0], UTF_8),
            keyValue -> URLDecoder.decode(
                keyValue.length > 1 ? keyValue[1] : "", UTF_8
            )
        )
        .toMap();
  }
}