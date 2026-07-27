package jp.co.skig.training.bookshelf.mapper;

import java.util.List;
import jp.co.skig.training.bookshelf.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * カテゴリMapper
 */
@Mapper
public interface CategoryMapper {

  /**
   * カテゴリを全件取得する（category_id昇順）
   * @return カテゴリ一覧
   */
  @Select("SELECT category_id, category_name FROM categories ORDER BY category_id")
  List<Category> findAll();
}
