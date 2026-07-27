package jp.co.skig.training.bookshelf.service;

import java.util.List;
import jp.co.skig.training.bookshelf.entity.Category;
import jp.co.skig.training.bookshelf.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * カテゴリサービス
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryMapper categoryMapper;

  /**
   * カテゴリを全件取得する（プルダウン用）
   * @return カテゴリ一覧
   */
  public List<Category> findAll() {
    return categoryMapper.findAll();
  }
}
