package jp.co.skig.training.bookshelf.constants;

/**
 * 書籍機能で使用する定数
 */
public final class BookConstants {

  private BookConstants() {
  }

  /** セッションキー: 検索条件（タイトル） */
  public static final String SESSION_SEARCH_TITLE = "bookSearchTitle";

  /** セッションキー: 検索条件（著者名） */
  public static final String SESSION_SEARCH_AUTHOR = "bookSearchAuthor";

  /** セッションキー: 検索条件（カテゴリID） */
  public static final String SESSION_SEARCH_CATEGORY_ID = "bookSearchCategoryId";

  /** セッションキー: 検索条件（出版社） */
  public static final String SESSION_SEARCH_PUBLISHER = "bookSearchPublisher";

  /** セッションキー: 登録入力値 */
  public static final String SESSION_REGISTER_FORM = "bookRegisterForm";

  /** セッションキー: 編集入力値 */
  public static final String SESSION_EDIT_FORM = "bookEditForm";

  /** ISBN 10桁または13桁の数字を表す正規表現 */
  public static final String ISBN_PATTERN = "^(\\d{10}|\\d{13})$";

  /** タイトル最大文字数 */
  public static final int TITLE_MAX_LENGTH = 100;

  /** 著者最大文字数 */
  public static final int AUTHOR_MAX_LENGTH = 50;

  /** 出版社最大文字数 */
  public static final int PUBLISHER_MAX_LENGTH = 50;

  /** 概要最大文字数 */
  public static final int DESCRIPTION_MAX_LENGTH = 1000;
}
