package jp.co.skig.training.bookshelf.mapper;

import java.util.List;
import jp.co.skig.training.bookshelf.entity.Review;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

/**
 * レビューMapper
 */
@Mapper
public interface ReviewMapper {

  /**
   * 指定書籍IDに紐づくレビューを全件取得する（投稿日時降順）
   * @param bookId 書籍ID
   * @return レビュー一覧
   */
  @Select("SELECT review_id, book_id, reviewer_name, rating, comment, created_at "
      + "FROM reviews WHERE book_id = #{bookId} ORDER BY created_at DESC")
  List<Review> findByBookId(Integer bookId);

  /**
   * レビューを登録する
   * @param review レビュー情報
   */
  @Insert("INSERT INTO reviews (book_id, reviewer_name, rating, comment, created_at) "
      + "VALUES (#{bookId}, #{reviewerName}, #{rating}, #{comment}, CURRENT_TIMESTAMP)")
  @Options(useGeneratedKeys = true, keyProperty = "reviewId")
  void insert(Review review);
}
