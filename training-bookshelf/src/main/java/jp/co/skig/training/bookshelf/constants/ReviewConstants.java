package jp.co.skig.training.bookshelf.constants;

/**
 * レビュー機能で使用する定数
 */
public final class ReviewConstants {

  private ReviewConstants() {
  }

  /** セッションキー: レビュー入力値 */
  public static final String SESSION_REVIEW_FORM = "reviewForm";

  /** セッションキー: レビュー投稿完了情報（レビュアー名・評価） */
  public static final String SESSION_REVIEW_COMPLETE = "reviewComplete";

  /** レビュアー名最大文字数 */
  public static final int REVIEWER_NAME_MAX_LENGTH = 50;

  /** コメント最大文字数 */
  public static final int COMMENT_MAX_LENGTH = 1000;

  /** 評価の最小値 */
  public static final int RATING_MIN = 1;

  /** 評価の最大値 */
  public static final int RATING_MAX = 5;
}
