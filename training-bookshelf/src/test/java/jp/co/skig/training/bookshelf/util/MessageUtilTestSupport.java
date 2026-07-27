package jp.co.skig.training.bookshelf.util;

import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * テストで {@link MessageUtil} の静的フィールドに実際の MessageSource を注入するためのヘルパー。
 * Spring コンテキストを起動しないテスト（Mockito単体）や、
 * {@code @WebMvcTest} で MessageUtil がコンポーネントスキャン対象外となる場合に使用する。
 */
public final class MessageUtilTestSupport {

  private MessageUtilTestSupport() {
  }

  public static void init() {
    ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
    messageSource.setBasename("messages");
    messageSource.setDefaultEncoding("UTF-8");
    new MessageUtil().setMessageSource(messageSource);
  }
}
