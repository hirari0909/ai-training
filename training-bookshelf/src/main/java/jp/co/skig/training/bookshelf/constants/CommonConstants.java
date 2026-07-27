package jp.co.skig.training.bookshelf.constants;

/**
 * クラス横断で使用する共通定数
 */
public final class CommonConstants {

  private CommonConstants() {
  }

  /** 1ページあたりの表示件数 */
  public static final int PAGE_SIZE = 20;

  /** デフォルトソート列 */
  public static final String DEFAULT_SORT_COLUMN = "bookId";

  /** デフォルトソート順 */
  public static final String DEFAULT_SORT_ORDER = "DESC";

  /** ソート順（昇順） */
  public static final String SORT_ORDER_ASC = "ASC";

  /** ソート順（降順） */
  public static final String SORT_ORDER_DESC = "DESC";
}
