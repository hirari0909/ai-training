package jp.co.skig.training.bookshelf.util;

import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

/**
 * メッセージ取得ユーティリティ
 * messages.properties からメッセージを取得する
 */
@Component
public class MessageUtil {

  private static MessageSource messageSource;

  @Autowired
  public void setMessageSource(MessageSource messageSource) {
    MessageUtil.messageSource = messageSource;
  }

  /**
   * メッセージIDに対応するメッセージを取得する
   * @param code メッセージID
   * @param args 置換パラメータ
   * @return メッセージ本文
   */
  public static String getMessage(String code, Object... args) {
    return messageSource.getMessage(code, args, Locale.JAPAN);
  }
}
