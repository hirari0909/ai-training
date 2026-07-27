package jp.co.skig.training.bookshelf.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import jp.co.skig.training.bookshelf.entity.Review;
import jp.co.skig.training.bookshelf.mapper.ReviewMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

  @Mock
  private ReviewMapper reviewMapper;

  @InjectMocks
  private ReviewService reviewService;

  private Review createReview(int rating) {
    Review review = new Review();
    review.setBookId(1);
    review.setReviewerName("レビュアー");
    review.setRating(rating);
    return review;
  }

  @Test
  void findByBookId_001_正常取得_複数件() {
    // Given
    List<Review> expected = List.of(createReview(5), createReview(4));
    when(reviewMapper.findByBookId(1)).thenReturn(expected);

    // When
    List<Review> actual = reviewService.findByBookId(1);

    // Then
    assertThat(actual).hasSize(2);
    assertThat(actual.get(0).getRating()).isEqualTo(5);
    assertThat(actual.get(1).getRating()).isEqualTo(4);
  }

  @Test
  void findByBookId_002_該当なし() {
    // Given
    when(reviewMapper.findByBookId(999)).thenReturn(Collections.emptyList());

    // When
    List<Review> actual = reviewService.findByBookId(999);

    // Then
    assertThat(actual).isEmpty();
  }

  @Test
  void register_003_正常登録() {
    // Given
    Review review = createReview(5);

    // When
    reviewService.register(review);

    // Then
    verify(reviewMapper, times(1)).insert(review);
  }

  @Test
  void calculateAverageRating_004_複数レビューの平均を算出() {
    // Given: 5,4,3 -> 平均4.0
    List<Review> reviews = List.of(createReview(5), createReview(4), createReview(3));

    // When
    double actual = reviewService.calculateAverageRating(reviews);

    // Then
    assertThat(actual).isEqualTo(4.0);
  }

  @Test
  void calculateAverageRating_005_小数点1位で四捨五入() {
    // Given: 5,4,4 -> 4.333... -> 4.3
    List<Review> reviews = List.of(createReview(5), createReview(4), createReview(4));

    // When
    double actual = reviewService.calculateAverageRating(reviews);

    // Then
    assertThat(actual).isEqualTo(4.3);
  }

  @Test
  void calculateAverageRating_006_空リストの場合0() {
    // Given
    List<Review> reviews = Collections.emptyList();

    // When
    double actual = reviewService.calculateAverageRating(reviews);

    // Then
    assertThat(actual).isEqualTo(0.0);
  }

  @Test
  void calculateAverageRating_007_nullの場合0() {
    // When
    double actual = reviewService.calculateAverageRating(null);

    // Then
    assertThat(actual).isEqualTo(0.0);
  }
}
