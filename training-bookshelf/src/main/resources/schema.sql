-- Sample Table DDL
DROP TABLE IF EXISTS sample;

CREATE TABLE sample (
    sample_id INT PRIMARY KEY AUTO_INCREMENT,
    sample_column1 VARCHAR(100) NOT NULL,
    sample_column2 VARCHAR(100) NOT NULL
);

-- ========================================
-- 書籍管理システム テーブル定義
-- ========================================

-- 外部キー制約があるため、依存する順に削除
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS categories;

-- カテゴリマスタ
CREATE TABLE categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL
);

-- 書籍情報
CREATE TABLE books (
    book_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    author VARCHAR(50) NOT NULL,
    publisher VARCHAR(50) NOT NULL,
    published_date DATE NOT NULL,
    isbn VARCHAR(13) NOT NULL,
    category_id INT NOT NULL,
    price INT NOT NULL,
    description TEXT,
    is_recommended BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (isbn),
    CHECK (price >= 0),
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

-- レビュー情報
CREATE TABLE reviews (
    review_id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT NOT NULL,
    reviewer_name VARCHAR(50) NOT NULL,
    rating TINYINT NOT NULL,
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (rating BETWEEN 1 AND 5),
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE
);
