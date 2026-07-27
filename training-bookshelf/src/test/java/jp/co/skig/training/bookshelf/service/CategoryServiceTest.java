package jp.co.skig.training.bookshelf.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import jp.co.skig.training.bookshelf.entity.Category;
import jp.co.skig.training.bookshelf.mapper.CategoryMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

  @Mock
  private CategoryMapper categoryMapper;

  @InjectMocks
  private CategoryService categoryService;

  private Category createCategory(int id, String name) {
    Category category = new Category();
    category.setCategoryId(id);
    category.setCategoryName(name);
    return category;
  }

  @Test
  void findAll_001_正常取得_複数件() {
    // Given
    List<Category> expected = List.of(
        createCategory(1, "小説・文学"),
        createCategory(2, "ビジネス・経済"),
        createCategory(3, "IT・コンピュータ"));
    when(categoryMapper.findAll()).thenReturn(expected);

    // When
    List<Category> actual = categoryService.findAll();

    // Then
    assertThat(actual).hasSize(3);
    assertThat(actual.get(0).getCategoryName()).isEqualTo("小説・文学");
    assertThat(actual.get(1).getCategoryName()).isEqualTo("ビジネス・経済");
    assertThat(actual.get(2).getCategoryName()).isEqualTo("IT・コンピュータ");
  }

  @Test
  void findAll_002_該当なし() {
    // Given
    when(categoryMapper.findAll()).thenReturn(Collections.emptyList());

    // When
    List<Category> actual = categoryService.findAll();

    // Then
    assertThat(actual).isEmpty();
  }
}
