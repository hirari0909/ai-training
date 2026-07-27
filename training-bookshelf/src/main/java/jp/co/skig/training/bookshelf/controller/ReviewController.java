package jp.co.skig.training.bookshelf.controller;

import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.Map;
import jp.co.skig.training.bookshelf.constants.ReviewConstants;
import jp.co.skig.training.bookshelf.entity.Book;
import jp.co.skig.training.bookshelf.entity.Review;
import jp.co.skig.training.bookshelf.form.ReviewForm;
import jp.co.skig.training.bookshelf.service.BookService;
import jp.co.skig.training.bookshelf.service.ReviewService;
import jp.co.skig.training.bookshelf.util.ExceptionLogger;
import jp.co.skig.training.bookshelf.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * レビュー投稿コントローラー（BK11-BK13）
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

  private final BookService bookService;
  private final ReviewService reviewService;

  // ========================================
  // BK11: レビュー投稿入力画面
  // ========================================

  /**
   * レビュー投稿入力画面を表示する
   */
  @GetMapping("/books/{bookId}/reviews/new")
  public String newForm(@PathVariable Integer bookId, HttpSession session, Model model) {
    Book book = bookService.findById(bookId);
    if (book == null) {
      model.addAttribute("errorMessage", MessageUtil.getMessage("common.system.error"));
      return "redirect:/book/list";
    }

    ReviewForm sessionForm = (ReviewForm) session.getAttribute(ReviewConstants.SESSION_REVIEW_FORM);
    ReviewForm form = (sessionForm != null && bookId.equals(sessionForm.getBookId()))
        ? sessionForm : new ReviewForm();

    model.addAttribute("book", book);
    model.addAttribute("form", form);
    return "book/BK11_ReviewPostInput";
  }

  /**
   * 入力内容をバリデーションし、レビュー投稿確認画面(BK12)を表示する
   */
  @PostMapping("/books/{bookId}/reviews/confirm")
  public String confirmReview(
      @PathVariable Integer bookId,
      @RequestParam(required = false) String reviewerName,
      @RequestParam(required = false) String rating,
      @RequestParam(required = false) String comment,
      HttpSession session,
      Model model) {

    Book book = bookService.findById(bookId);
    if (book == null) {
      return "redirect:/book/list";
    }

    Map<String, String> errors = new LinkedHashMap<>();
    if (!StringUtils.hasText(reviewerName)) {
      errors.put("reviewerName", MessageUtil.getMessage("validation.required", "レビュアー名"));
    } else if (reviewerName.length() > ReviewConstants.REVIEWER_NAME_MAX_LENGTH) {
      errors.put("reviewerName", MessageUtil.getMessage("validation.maxlength", "レビュアー名",
          ReviewConstants.REVIEWER_NAME_MAX_LENGTH));
    }

    Integer ratingValue = null;
    if (!StringUtils.hasText(rating)) {
      errors.put("rating", MessageUtil.getMessage("validation.required", "評価"));
    } else {
      try {
        ratingValue = Integer.valueOf(rating);
        if (ratingValue < ReviewConstants.RATING_MIN || ratingValue > ReviewConstants.RATING_MAX) {
          errors.put("rating", MessageUtil.getMessage("validation.range", "評価",
              ReviewConstants.RATING_MIN, ReviewConstants.RATING_MAX));
        }
      } catch (NumberFormatException e) {
        errors.put("rating", MessageUtil.getMessage("validation.range", "評価",
            ReviewConstants.RATING_MIN, ReviewConstants.RATING_MAX));
      }
    }

    if (StringUtils.hasText(comment) && comment.length() > ReviewConstants.COMMENT_MAX_LENGTH) {
      errors.put("comment", MessageUtil.getMessage("validation.maxlength", "コメント",
          ReviewConstants.COMMENT_MAX_LENGTH));
    }

    ReviewForm form = new ReviewForm();
    form.setBookId(bookId);
    form.setReviewerName(reviewerName);
    form.setRating(ratingValue);
    form.setComment(comment);

    if (!errors.isEmpty()) {
      model.addAttribute("book", book);
      model.addAttribute("form", form);
      model.addAttribute("errors", errors);
      return "book/BK11_ReviewPostInput";
    }

    session.setAttribute(ReviewConstants.SESSION_REVIEW_FORM, form);
    model.addAttribute("book", book);
    model.addAttribute("form", form);
    return "book/BK12_ReviewPostConfirm";
  }

  // ========================================
  // BK12 → BK13: レビュー投稿処理
  // ========================================

  /**
   * レビュー投稿処理を実行する
   */
  @PostMapping("/books/{bookId}/reviews")
  public String postReview(@PathVariable Integer bookId, HttpSession session,
      RedirectAttributes redirectAttributes, Model model) {
    ReviewForm form = (ReviewForm) session.getAttribute(ReviewConstants.SESSION_REVIEW_FORM);
    if (form == null || !bookId.equals(form.getBookId())) {
      redirectAttributes.addFlashAttribute("errorMessage", MessageUtil.getMessage("common.session.timeout"));
      return "redirect:/books/" + bookId + "/reviews/new";
    }

    try {
      Review review = new Review();
      review.setBookId(bookId);
      review.setReviewerName(form.getReviewerName());
      review.setRating(form.getRating());
      review.setComment(form.getComment());
      reviewService.register(review);

      session.removeAttribute(ReviewConstants.SESSION_REVIEW_FORM);
      Map<String, Object> completeInfo = new LinkedHashMap<>();
      completeInfo.put("reviewerName", form.getReviewerName());
      completeInfo.put("rating", form.getRating());
      session.setAttribute(ReviewConstants.SESSION_REVIEW_COMPLETE, completeInfo);

      return "redirect:/books/" + bookId + "/reviews/complete";
    } catch (Exception e) {
      ExceptionLogger.log(e);
      Book book = bookService.findById(bookId);
      model.addAttribute("book", book);
      model.addAttribute("form", form);
      model.addAttribute("errorMessage", MessageUtil.getMessage("db.error.insert"));
      return "book/BK12_ReviewPostConfirm";
    }
  }

  // ========================================
  // BK13: レビュー投稿完了画面
  // ========================================

  /**
   * レビュー投稿完了画面を表示する
   */
  @GetMapping("/books/{bookId}/reviews/complete")
  @SuppressWarnings("unchecked")
  public String reviewComplete(@PathVariable Integer bookId, HttpSession session, Model model) {
    Book book = bookService.findById(bookId);
    Map<String, Object> completeInfo =
        (Map<String, Object>) session.getAttribute(ReviewConstants.SESSION_REVIEW_COMPLETE);

    model.addAttribute("book", book);
    if (completeInfo != null) {
      model.addAttribute("reviewerName", completeInfo.get("reviewerName"));
      model.addAttribute("rating", completeInfo.get("rating"));
      session.removeAttribute(ReviewConstants.SESSION_REVIEW_COMPLETE);
    }
    return "book/BK13_ReviewPostComplete";
  }
}
