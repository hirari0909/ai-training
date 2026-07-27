package jp.co.skig.training.bookshelf.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import jp.co.skig.training.bookshelf.entity.Review;
import jp.co.skig.training.bookshelf.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * レビューサービス
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

  private final ReviewMapper reviewMapper;

  /**
   * 指定書籍IDに紐づくレビューを全件取得する（投稿日時降順）
   * @param bookId 書籍ID
   * @return レビュー一覧
   */
  public List<Review> findByBookId(Integer bookId) {
    return reviewMapper.findByBookId(bookId);
  }

  /**
   * レビューを登録する
   * @param review レビュー情報
   */
  @Transactional
  public void register(Review review) {
    reviewMapper.insert(review);
  }

  /**
   * レビュー一覧から平均評価を算出する（小数点1位で四捨五入、レビューなしの場合は0）
   * @param reviews レビュー一覧
   * @return 平均評価
   */
  public double calculateAverageRating(List<Review> reviews) {
    if (reviews == null || reviews.isEmpty()) {
      return 0.0;
    }
    double sum = reviews.stream().mapToInt(Review::getRating).sum();
    double average = sum / reviews.size();
    return BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP).doubleValue();
  }
}
