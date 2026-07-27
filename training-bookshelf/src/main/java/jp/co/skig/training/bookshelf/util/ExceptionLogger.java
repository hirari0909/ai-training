package jp.co.skig.training.bookshelf.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 例外ログユーティリティ
 * 監視対象ログに例外内容を出力する
 */
@Slf4j
public final class ExceptionLogger {

  private ExceptionLogger() {
  }

  /**
   * 例外を監視対象ログに出力する
   * @param e 発生した例外
   */
  public static void log(Exception e) {
    log.error(MessageUtil.getMessage("log.error.exception", e.getMessage()), e);
  }
}
