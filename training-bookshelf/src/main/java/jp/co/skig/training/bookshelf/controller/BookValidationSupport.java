package jp.co.skig.training.bookshelf.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import jp.co.skig.training.bookshelf.constants.BookConstants;
import jp.co.skig.training.bookshelf.util.MessageUtil;
import org.springframework.util.StringUtils;

/**
 * 書籍登録・編集入力画面の共通バリデーション処理
 */
final class BookValidationSupport {

  private BookValidationSupport() {
  }

  /**
   * 書籍登録・編集入力値をバリデーションする
   * @return 項目IDをキーとしたエラーメッセージのマップ（エラーがなければ空）
   */
  static Map<String, String> validate(String title, String author, String publisher,
      String publishedDate, String isbn, String category, String price, String description) {

    Map<String, String> errors = new LinkedHashMap<>();

    if (!StringUtils.hasText(title)) {
      errors.put("title", MessageUtil.getMessage("validation.required", "タイトル"));
    } else if (title.length() > BookConstants.TITLE_MAX_LENGTH) {
      errors.put("title",
          MessageUtil.getMessage("validation.length.max", "タイトル", BookConstants.TITLE_MAX_LENGTH));
    }

    if (!StringUtils.hasText(author)) {
      errors.put("author", MessageUtil.getMessage("validation.required", "著者"));
    } else if (author.length() > BookConstants.AUTHOR_MAX_LENGTH) {
      errors.put("author",
          MessageUtil.getMessage("validation.length.max", "著者", BookConstants.AUTHOR_MAX_LENGTH));
    }

    if (!StringUtils.hasText(publisher)) {
      errors.put("publisher", MessageUtil.getMessage("validation.required", "出版社"));
    } else if (publisher.length() > BookConstants.PUBLISHER_MAX_LENGTH) {
      errors.put("publisher",
          MessageUtil.getMessage("validation.length.max", "出版社", BookConstants.PUBLISHER_MAX_LENGTH));
    }

    if (!StringUtils.hasText(publishedDate)) {
      errors.put("publishedDate", MessageUtil.getMessage("validation.required", "出版日"));
    } else {
      try {
        LocalDate date = LocalDate.parse(publishedDate);
        if (date.isAfter(LocalDate.now())) {
          errors.put("publishedDate", MessageUtil.getMessage("validation.date.future", "出版日"));
        }
      } catch (DateTimeParseException e) {
        errors.put("publishedDate", MessageUtil.getMessage("validation.date.format", "出版日"));
      }
    }

    if (!StringUtils.hasText(isbn)) {
      errors.put("isbn", MessageUtil.getMessage("validation.required", "ISBN"));
    } else if (!isbn.matches(BookConstants.ISBN_PATTERN)) {
      errors.put("isbn", MessageUtil.getMessage("validation.isbn.format"));
    }

    if (!StringUtils.hasText(category)) {
      errors.put("category", MessageUtil.getMessage("validation.required", "カテゴリ"));
    }

    if (!StringUtils.hasText(price)) {
      errors.put("price", MessageUtil.getMessage("validation.required", "価格"));
    } else {
      try {
        int priceValue = Integer.parseInt(price);
        if (priceValue < 0) {
          errors.put("price", MessageUtil.getMessage("validation.number.min", "価格", 0));
        }
      } catch (NumberFormatException e) {
        errors.put("price", MessageUtil.getMessage("validation.number.format", "価格"));
      }
    }

    if (StringUtils.hasText(description) && description.length() > BookConstants.DESCRIPTION_MAX_LENGTH) {
      errors.put("description",
          MessageUtil.getMessage("validation.length.max", "概要", BookConstants.DESCRIPTION_MAX_LENGTH));
    }

    return errors;
  }
}
