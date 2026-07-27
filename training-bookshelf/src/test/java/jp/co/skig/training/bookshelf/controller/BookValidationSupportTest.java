package jp.co.skig.training.bookshelf.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import jp.co.skig.training.bookshelf.util.MessageUtilTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BookValidationSupportTest {

  @BeforeAll
  static void setUpMessageSource() {
    MessageUtilTestSupport.init();
  }

  private static final String VALID_TITLE = "タイトル";
  private static final String VALID_AUTHOR = "著者";
  private static final String VALID_PUBLISHER = "出版社";
  private static final String VALID_DATE = "2024-01-01";
  private static final String VALID_ISBN = "1234567890";
  private static final String VALID_CATEGORY = "1";
  private static final String VALID_PRICE = "1500";

  private Map<String, String> validate(String title, String author, String publisher,
      String publishedDate, String isbn, String category, String price, String description) {
    return BookValidationSupport.validate(title, author, publisher, publishedDate, isbn,
        category, price, description);
  }

  @Test
  void validate_001_全項目正常() {
    // Given & When
    Map<String, String> errors = validate(VALID_TITLE, VALID_AUTHOR, VALID_PUBLISHER, VALID_DATE,
        VALID_ISBN, VALID_CATEGORY, VALID_PRICE, "概要");

    // Then
    assertThat(errors).isEmpty();
  }

  @Test
  void validate_002_タイトル未入力() {
    // Given & When
    Map<String, String> errors = validate("", VALID_AUTHOR, VALID_PUBLISHER, VALID_DATE,
        VALID_ISBN, VALID_CATEGORY, VALID_PRICE, null);

    // Then
    assertThat(errors.get("title")).isEqualTo("タイトルは必須です");
  }

  @Test
  void validate_003_タイトル文字数超過() {
    // Given: 101文字
    String longTitle = "あ".repeat(101);

    // When
    Map<String, String> errors = validate(longTitle, VALID_AUTHOR, VALID_PUBLISHER, VALID_DATE,
        VALID_ISBN, VALID_CATEGORY, VALID_PRICE, null);

    // Then
    assertThat(errors.get("title")).isEqualTo("タイトルは100文字以内で入力してください");
  }

  @Test
  void validate_004_著者未入力() {
    // Given & When
    Map<String, String> errors = validate(VALID_TITLE, null, VALID_PUBLISHER, VALID_DATE,
        VALID_ISBN, VALID_CATEGORY, VALID_PRICE, null);

    // Then
    assertThat(errors.get("author")).isEqualTo("著者は必須です");
  }

  @Test
  void validate_005_著者文字数超過() {
    // Given: 51文字
    String longAuthor = "あ".repeat(51);

    // When
    Map<String, String> errors = validate(VALID_TITLE, longAuthor, VALID_PUBLISHER, VALID_DATE,
        VALID_ISBN, VALID_CATEGORY, VALID_PRICE, null);

    // Then
    assertThat(errors.get("author")).isEqualTo("著者は50文字以内で入力してください");
  }

  @Test
  void validate_006_出版社未入力() {
    // Given & When
    Map<String, String> errors = validate(VALID_TITLE, VALID_AUTHOR, "", VALID_DATE,
        VALID_ISBN, VALID_CATEGORY, VALID_PRICE, null);

    // Then
    assertThat(errors.get("publisher")).isEqualTo("出版社は必須です");
  }

  @Test
  void validate_007_出版社文字数超過() {
    // Given: 51文字
    String longPublisher = "あ".repeat(51);

    // When
    Map<String, String> errors = validate(VALID_TITLE, VALID_AUTHOR, longPublisher, VALID_DATE,
        VALID_ISBN, VALID_CATEGORY, VALID_PRICE, null);

    // Then
    assertThat(errors.get("publisher")).isEqualTo("出版社は50文字以内で入力してください");
  }

  @Test
  void validate_008_出版日未入力() {
    // Given & When
    Map<String, String> errors = validate(VALID_TITLE, VALID_AUTHOR, VALID_PUBLISHER, null,
        VALID_ISBN, VALID_CATEGORY, VALID_PRICE, null);

    // Then
    assertThat(errors.get("publishedDate")).isEqualTo("出版日は必須です");
  }

  @Test
  void validate_009_出版日フォーマット不正() {
    // Given & When
    Map<String, String> errors = validate(VALID_TITLE, VALID_AUTHOR, VALID_PUBLISHER,
        "2024/13/40", VALID_ISBN, VALID_CATEGORY, VALID_PRICE, null);

    // Then
    assertThat(errors.get("publishedDate")).isEqualTo("出版日は正しい日付形式で入力してください");
  }

  @Test
  void validate_010_出版日が未来日付() {
    // Given: 翌日の日付
    String tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);

    // When
    Map<String, String> errors = validate(VALID_TITLE, VALID_AUTHOR, VALID_PUBLISHER, tomorrow,
        VALID_ISBN, VALID_CATEGORY, VALID_PRICE, null);

    // Then
    assertThat(errors.get("publishedDate")).isEqualTo("出版日は未来日付を指定できません");
  }

  @Test
  void validate_011_ISBN未入力() {
    // Given & When
    Map<String, String> errors = validate(VALID_TITLE, VALID_AUTHOR, VALID_PUBLISHER, VALID_DATE,
        "", VALID_CATEGORY, VALID_PRICE, null);

    // Then
    assertThat(errors.get("isbn")).isEqualTo("ISBNは必須です");
  }

  @Test
  void validate_012_ISBN形式不正_9桁() {
    // Given & When
    Map<String, String> errors = validate(VALID_TITLE, VALID_AUTHOR, VALID_PUBLISHER, VALID_DATE,
        "123456789", VALID_CATEGORY, VALID_PRICE, null);

    // Then
    assertThat(errors.get("isbn")).isEqualTo("ISBNは10桁または13桁の数字で入力してください");
  }

  @Test
  void validate_013_ISBN10桁は正常() {
    // Given & When
    Map<String, String> errors = validate(VALID_TITLE, VALID_AUTHOR, VALID_PUBLISHER, VALID_DATE,
        "1234567890", VALID_CATEGORY, VALID_PRICE, null);

    // Then
    assertThat(errors.get("isbn")).isNull();
  }

  @Test
  void validate_014_ISBN13桁は正常() {
    // Given & When
    Map<String, String> errors = validate(VALID_TITLE, VALID_AUTHOR, VALID_PUBLISHER, VALID_DATE,
        "1234567890123", VALID_CATEGORY, VALID_PRICE, null);

    // Then
    assertThat(errors.get("isbn")).isNull();
  }

  @Test
  void validate_015_カテゴリ未選択() {
    // Given & When
    Map<String, String> errors = validate(VALID_TITLE, VALID_AUTHOR, VALID_PUBLISHER, VALID_DATE,
        VALID_ISBN, "", VALID_PRICE, null);

    // Then
    assertThat(errors.get("category")).isEqualTo("カテゴリは必須です");
  }

  @Test
  void validate_016_価格未入力() {
    // Given & When
    Map<String, String> errors = validate(VALID_TITLE, VALID_AUTHOR, VALID_PUBLISHER, VALID_DATE,
        VALID_ISBN, VALID_CATEGORY, "", null);

    // Then
    assertThat(errors.get("price")).isEqualTo("価格は必須です");
  }

  @Test
  void validate_017_価格が数値でない() {
    // Given & When
    Map<String, String> errors = validate(VALID_TITLE, VALID_AUTHOR, VALID_PUBLISHER, VALID_DATE,
        VALID_ISBN, VALID_CATEGORY, "abc", null);

    // Then
    assertThat(errors.get("price")).isEqualTo("価格は数値で入力してください");
  }

  @Test
  void validate_018_価格が負数() {
    // Given & When
    Map<String, String> errors = validate(VALID_TITLE, VALID_AUTHOR, VALID_PUBLISHER, VALID_DATE,
        VALID_ISBN, VALID_CATEGORY, "-1", null);

    // Then
    assertThat(errors.get("price")).isEqualTo("価格は0以上で入力してください");
  }

  @Test
  void validate_019_価格0は境界値として正常() {
    // Given & When
    Map<String, String> errors = validate(VALID_TITLE, VALID_AUTHOR, VALID_PUBLISHER, VALID_DATE,
        VALID_ISBN, VALID_CATEGORY, "0", null);

    // Then
    assertThat(errors.get("price")).isNull();
  }

  @Test
  void validate_020_概要文字数超過() {
    // Given: 1001文字
    String longDescription = "あ".repeat(1001);

    // When
    Map<String, String> errors = validate(VALID_TITLE, VALID_AUTHOR, VALID_PUBLISHER, VALID_DATE,
        VALID_ISBN, VALID_CATEGORY, VALID_PRICE, longDescription);

    // Then
    assertThat(errors.get("description")).isEqualTo("概要は1,000文字以内で入力してください");
  }

  @Test
  void validate_021_概要は任意項目のため未入力でも正常() {
    // Given & When
    Map<String, String> errors = validate(VALID_TITLE, VALID_AUTHOR, VALID_PUBLISHER, VALID_DATE,
        VALID_ISBN, VALID_CATEGORY, VALID_PRICE, null);

    // Then
    assertThat(errors.get("description")).isNull();
  }

  @Test
  void validate_022_複数項目で同時にエラー発生() {
    // Given & When: タイトル未入力かつISBN不正
    Map<String, String> errors = validate("", VALID_AUTHOR, VALID_PUBLISHER, VALID_DATE,
        "123", VALID_CATEGORY, VALID_PRICE, null);

    // Then
    assertThat(errors).hasSizeGreaterThanOrEqualTo(2);
    assertThat(errors.get("title")).isNotNull();
    assertThat(errors.get("isbn")).isNotNull();
  }
}
