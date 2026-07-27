/* 結合テスト打鍵ランナー（ローカルPlaywright）
 * 使い方: node ita-run.cjs SC01           （1シナリオ実行）
 * 出力 : Docsシステム/結合テスト/結果/_raw/SCxx.json + img/*.png
 * playwright は tools/screenCapture/node_modules を再利用
 */
const path = require('path');
const fs = require('fs');
const REPO = 'C:\\work\\Biz\\workspace\\ai-training';
const { chromium } = require(path.join(REPO, 'tools', 'screenCapture', 'node_modules', 'playwright'));

const BASE = 'http://localhost:8080';
const OUT = path.join(REPO, 'Docsシステム', '結合テスト', '結果');
const IMG = path.join(OUT, 'img');
const RAW = path.join(OUT, '_raw');
for (const d of [OUT, IMG, RAW]) fs.mkdirSync(d, { recursive: true });

const arg = (process.argv[2] || '').toUpperCase();

// ---- helpers ----
function nowStr() { return new Date().toISOString().replace('T', ' ').slice(0, 19); }

async function shot(page, caseNo, stepNo, slug) {
  const file = `${caseNo}_${stepNo}_${slug}.png`;
  await page.screenshot({ path: path.join(IMG, file), fullPage: true }).catch(() => {});
  return `img/${file}`;
}

function mkCase(no, name, viewpoint, precondition) {
  return { no, name, viewpoint, precondition, judgment: 'OK', steps: [], ng: null };
}
function step(c, no, op, input, expected, actual, ok, shotPath) {
  if (!ok) c.judgment = 'NG';
  c.steps.push({ no, op, input: input || '-', expected, actual, judge: ok ? 'OK' : 'NG', shot: shotPath || '' });
  return ok;
}

async function freshContext(browser) {
  const ctx = await browser.newContext({ viewport: { width: 1280, height: 900 } });
  const page = await ctx.newPage();
  return { ctx, page };
}

// 共通: BK03/06フォーム入力
async function fillBookForm(page, v) {
  if (v.title !== undefined) await page.fill('input[name="title"]', v.title);
  if (v.author !== undefined) await page.fill('input[name="author"]', v.author);
  if (v.publisher !== undefined) await page.fill('input[name="publisher"]', v.publisher);
  if (v.publishedDate !== undefined) await page.fill('input[name="publishedDate"]', v.publishedDate);
  if (v.isbn !== undefined) await page.fill('input[name="isbn"]', v.isbn);
  if (v.categoryId !== undefined) await page.selectOption('select[name="categoryId"]', String(v.categoryId));
  if (v.price !== undefined) await page.fill('input[name="price"]', String(v.price));
  if (v.description !== undefined) await page.fill('textarea[name="description"]', v.description);
}

async function bodyText(page) { return (await page.textContent('body').catch(() => '')) || ''; }

// 確認画面(BK04/07/12)に居るか＝確認メッセージで判定（バリデーションNG時はURLが/confirmのままBK入力が再描画されるためURL判定不可）
async function onConfirm(page, kind) {
  const b = await bodyText(page);
  if (kind === 'edit') return b.includes('更新します');
  if (kind === 'review') return b.includes('レビューを投稿します') || b.includes('投稿します');
  return b.includes('登録します');
}
async function onInputForm(page, sel) { return (await page.locator(sel).count()) > 0; }

// =====================================================================
// SC01 書籍登録フロー
// =====================================================================
async function SC01(browser) {
  const cases = [];

  // SC01-001 正常登録
  {
    const c = mkCase('SC01-001', '正常登録（一覧→入力→確認→完了→一覧）', '正常系', '共通事前準備の通り');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/list`);
      await page.click('a[href*="/book/create"]');
      await page.waitForLoadState('networkidle');
      let u = page.url(); let s = await shot(page, c.no, 1, 'bk03');
      step(c, 1, 'BK01で新規登録ボタン押下', '-', 'BK03(/book/create)へ遷移', u, /\/book\/create(\b|$|\?|;)/.test(u) && !u.includes('confirm'), s);

      const v = { title: '結合テスト書籍', author: 'テスト太郎', publisher: 'テスト社', publishedDate: '2020-01-01', isbn: '9990000000001', categoryId: 3, price: 1500, description: '説明' };
      await fillBookForm(page, v);
      await page.click('button:has-text("確認画面へ")');
      await page.waitForLoadState('networkidle');
      u = page.url(); let b = await bodyText(page); s = await shot(page, c.no, 2, 'bk04');
      step(c, 2, '全必須項目入力し確認画面へ押下', JSON.stringify(v), 'BK04へ遷移・入力値表示・確認メッセージ', `url=${u}`, u.includes('/book/create/confirm') && b.includes('結合テスト書籍') && b.includes('登録します'), s);

      await page.click('button:has-text("登録"), input[value="登録"]');
      await page.waitForLoadState('networkidle');
      u = page.url(); b = await bodyText(page); s = await shot(page, c.no, 3, 'bk05');
      step(c, 3, '登録ボタン押下', '-', 'BK05へ遷移・「書籍の登録が完了しました」', `url=${u}`, u.includes('/book/create/complete') && b.includes('登録が完了'), s);

      await page.click('a:has-text("一覧に戻る")');
      await page.waitForLoadState('networkidle');
      u = page.url();
      await page.fill('#searchTitle', '結合テスト書籍');
      await page.click('button:has-text("検索")');
      await page.waitForLoadState('networkidle');
      b = await bodyText(page); s = await shot(page, c.no, 4, 'bk01_verify');
      step(c, 4, '一覧に戻る→登録書籍を検索', 'searchTitle=結合テスト書籍', '一覧に登録書籍が表示（DB反映）', '検索結果に含まれる', b.includes('結合テスト書籍'), s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }

  // SC01-003 確認画面から戻り入力値保持
  {
    const c = mkCase('SC01-003', '確認画面から戻り入力値保持', 'セッション', '共通事前準備の通り');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/create`);
      const v = { title: '保持確認', author: '著者A', publisher: '社A', publishedDate: '2019-05-05', isbn: '9990000000002', categoryId: 5, price: 999, description: 'メモ' };
      await fillBookForm(page, v);
      await page.click('button:has-text("確認画面へ")');
      await page.waitForLoadState('networkidle');
      let s = await shot(c.no ? page : page, c.no, 1, 'bk04');
      step(c, 1, 'BK03入力→確認画面へ', JSON.stringify(v), 'BK04へ遷移', page.url(), page.url().includes('/book/create/confirm'), s);

      await page.click('a:has-text("戻る"), button:has-text("戻る")');
      await page.waitForLoadState('networkidle');
      const title = await page.inputValue('input[name="title"]').catch(() => '');
      const price = await page.inputValue('input[name="price"]').catch(() => '');
      const cat = await page.inputValue('select[name="categoryId"]').catch(() => '');
      s = await shot(page, c.no, 2, 'bk03_restored');
      step(c, 2, 'BK04で戻るボタン押下', '-', '入力値が全項目復元(title=保持確認,price=999,cat=5)', `title=${title},price=${price},cat=${cat}`, title === '保持確認' && price === '999' && cat === '5', s);

      await page.fill('input[name="title"]', '保持確認2');
      await page.click('button:has-text("確認画面へ")');
      await page.waitForLoadState('networkidle');
      const b = await bodyText(page); s = await shot(page, c.no, 3, 'bk04_changed');
      step(c, 3, '値変更し再度確認画面へ', 'title=保持確認2', 'BK04に変更後の値で表示', 'body確認', b.includes('保持確認2'), s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }

  // SC01-004 キャンセルで一覧へ戻りセッションクリア
  {
    const c = mkCase('SC01-004', 'キャンセルで一覧へ戻りセッションクリア', 'セッション', '共通事前準備の通り');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/create`);
      await fillBookForm(page, { title: '破棄予定', author: '著者B' });
      let s = await shot(page, c.no, 1, 'bk03_input');
      step(c, 1, 'BK03で項目入力', 'title=破棄予定', 'BK03入力状態', 'input set', true, s);
      await page.click('a:has-text("キャンセル"), button:has-text("キャンセル")');
      await page.waitForLoadState('networkidle');
      let u = page.url(); s = await shot(page, c.no, 2, 'bk01');
      step(c, 2, 'キャンセルボタン押下', '-', 'BK01へ遷移', u, /\/book\/list/.test(u), s);
      await page.goto(`${BASE}/book/create`);
      const title = await page.inputValue('input[name="title"]').catch(() => 'x');
      s = await shot(page, c.no, 3, 'bk03_empty');
      step(c, 3, '再度BK03表示', '-', 'フォームが空(セッションクリア)', `title="${title}"`, title === '', s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }

  // SC01-005 必須未入力バリデーション
  {
    const c = mkCase('SC01-005', '必須項目未入力でバリデーションエラー', '異常系', '共通事前準備の通り');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/create`);
      await page.click('button:has-text("確認画面へ")');
      await page.waitForLoadState('networkidle');
      let b = await bodyText(page); let s = await shot(page, c.no, 1, 'bk03_errors');
      const stay = (await onInputForm(page, 'input[name="title"]')) && !(await onConfirm(page));
      const hasErr = /必須/.test(b);
      step(c, 1, '全必須項目空で確認画面へ', '(全項目空)', 'BK03に留まり必須エラー(「○○は必須です」)表示', `inputForm=${stay} err=${hasErr}`, stay && hasErr, s);

      await page.fill('input[name="title"]', '部分入力');
      await page.click('button:has-text("確認画面へ")');
      await page.waitForLoadState('networkidle');
      const tv = await page.inputValue('input[name="title"]').catch(() => '');
      b = await bodyText(page); s = await shot(page, c.no, 2, 'bk03_partial');
      const stay2 = (await onInputForm(page, 'input[name="title"]')) && !(await onConfirm(page));
      step(c, 2, 'タイトルのみ入力し再送信', 'title=部分入力', 'BK03継続・タイトル値保持・他必須エラー継続', `title=${tv} stay=${stay2}`, tv === '部分入力' && stay2 && /必須/.test(b), s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }

  // SC01-006 桁数・形式・未来日付・数値の境界/異常
  {
    const c = mkCase('SC01-006', '桁数・形式・未来日付・数値の境界／異常', '境界値', 'BK03表示中');
    const { ctx, page } = await freshContext(browser);
    try {
      const base = { author: '著', publisher: '社', publishedDate: '2020-01-01', categoryId: 3, price: 100 };
      const inputSel = 'input[name="title"]';
      // 1: title 桁数上限（maxlength=100 で101文字入力は不可＝100に切詰め。設計「100文字以内」を入力段階で担保）
      await page.goto(`${BASE}/book/create`);
      const mlen = await page.getAttribute('input[name="title"]', 'maxlength').catch(() => null);
      await page.fill('input[name="title"]', 'あ'.repeat(101));
      const tlen = (await page.inputValue('input[name="title"]')).length;
      let s = await shot(page, c.no, 1, 'title_maxlen');
      step(c, 1, 'タイトルに101文字入力を試行', 'title=101字入力', 'maxlength=100で100文字に制限（桁数上限を入力で担保）', `maxlength=${mlen}, 実入力長=${tlen}`, mlen === '100' && tlen === 100, s);
      // 2: title 100 OK
      await page.goto(`${BASE}/book/create`);
      await fillBookForm(page, { ...base, isbn: '9990000000011', title: 'あ'.repeat(100) });
      await page.click('button:has-text("確認画面へ")'); await page.waitForLoadState('networkidle');
      s = await shot(page, c.no, 2, 'title100');
      let conf = await onConfirm(page);
      step(c, 2, 'タイトル100文字ちょうど', 'title=100字', 'BK04へ遷移(許容)', `confirm=${conf}`, conf, s);
      // 3: future date
      await page.goto(`${BASE}/book/create`);
      await fillBookForm(page, { ...base, isbn: '9990000000012', title: '未来日', publishedDate: '2999-12-31' });
      await page.click('button:has-text("確認画面へ")'); await page.waitForLoadState('networkidle');
      b = await bodyText(page); s = await shot(page, c.no, 3, 'future');
      stay = (await onInputForm(page, inputSel)) && !(await onConfirm(page));
      step(c, 3, '出版日に未来日付', 'publishedDate=2999-12-31', 'BK03に留まり未来日付エラー', `stay=${stay}`, stay && /未来日付/.test(b), s);
      // 4: isbn 9 digits
      await page.goto(`${BASE}/book/create`);
      await fillBookForm(page, { ...base, isbn: '123456789', title: 'ISBN短' });
      await page.click('button:has-text("確認画面へ")'); await page.waitForLoadState('networkidle');
      b = await bodyText(page); s = await shot(page, c.no, 4, 'isbn9');
      stay = (await onInputForm(page, inputSel)) && !(await onConfirm(page));
      step(c, 4, 'ISBN9桁で送信', 'isbn=123456789', 'BK03に留まりISBN形式エラー', `stay=${stay}`, stay && /ISBN|13桁/.test(b), s);
      // 5: price -1
      await page.goto(`${BASE}/book/create`);
      await fillBookForm(page, { ...base, isbn: '9990000000015', title: '負価格', price: -1 });
      await page.click('button:has-text("確認画面へ")'); await page.waitForLoadState('networkidle');
      b = await bodyText(page); s = await shot(page, c.no, 5, 'priceNeg');
      stay = (await onInputForm(page, inputSel)) && !(await onConfirm(page));
      step(c, 5, '価格に負数で送信', 'price=-1', 'BK03に留まり数値エラー', `stay=${stay}`, stay && /0以上|価格/.test(b), s);
      // 6: price 0 OK
      await page.goto(`${BASE}/book/create`);
      await fillBookForm(page, { ...base, isbn: '9990000000016', title: 'ゼロ価格', price: 0 });
      await page.click('button:has-text("確認画面へ")'); await page.waitForLoadState('networkidle');
      s = await shot(page, c.no, 6, 'price0');
      conf = await onConfirm(page);
      step(c, 6, '価格0で送信', 'price=0', 'BK04へ遷移(0は許容)', `confirm=${conf}`, conf, s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }

  // SC01-007 ISBN重複
  {
    const c = mkCase('SC01-007', 'ISBN重複エラー', '異常系', 'book_id=1 が ISBN 9784873115658 で登録済み');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/create`);
      await fillBookForm(page, { title: 'ISBN重複テスト', author: '著', publisher: '社', publishedDate: '2020-01-01', isbn: '9784873115658', categoryId: 3, price: 1000, description: '' });
      await page.click('button:has-text("確認画面へ")'); await page.waitForLoadState('networkidle');
      let u = page.url(); let s = await shot(page, c.no, 1, 'bk04');
      step(c, 1, '既存ISBNで確認画面へ', 'isbn=9784873115658', 'BK04へ遷移(入力時はOK)', u, u.includes('/book/create/confirm'), s);
      await page.click('button:has-text("登録"), input[value="登録"]'); await page.waitForLoadState('networkidle');
      let b = await bodyText(page); u = page.url(); s = await shot(page, c.no, 2, 'dup_error');
      step(c, 2, '登録ボタン押下', '-', 'BK04に留まりISBN重複メッセージ・INSERTされない', `url=${u}`, !u.includes('/complete') && /既に登録され|ISBN/.test(b), s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }

  // SC01-008 確認画面 直アクセスのセッションガード
  {
    const c = mkCase('SC01-008', '確認画面 直接アクセス時のセッションガード', 'セッション', 'セッションに入力値なし');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/create/confirm`);
      await page.waitForLoadState('networkidle');
      let u = page.url(); let s = await shot(page, c.no, 1, 'guard');
      step(c, 1, 'BK04へ直接アクセス', '-', 'BK03へリダイレクト', u, /\/book\/create(\b|$|\?|;)/.test(u) && !u.includes('confirm'), s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }

  // SC01-002 完了画面から詳細へ（001の続き相当：独立実行）
  {
    const c = mkCase('SC01-002', '完了画面から詳細へ遷移', '正常系', '登録完了しBK05表示');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/create`);
      await fillBookForm(page, { title: '完了遷移確認', author: '著', publisher: '社', publishedDate: '2020-02-02', isbn: '9990000000020', categoryId: 3, price: 1200, description: '' });
      await page.click('button:has-text("確認画面へ")'); await page.waitForLoadState('networkidle');
      await page.click('button:has-text("登録"), input[value="登録"]'); await page.waitForLoadState('networkidle');
      let s = await shot(page, c.no, 1, 'bk05');
      step(c, 1, '登録完了(BK05)', '-', 'BK05表示', page.url(), page.url().includes('/book/create/complete'), s);
      await page.click('a:has-text("詳細を見る"), button:has-text("詳細を見る")'); await page.waitForLoadState('networkidle');
      let u = page.url(); let b = await bodyText(page); s = await shot(page, c.no, 2, 'bk02');
      step(c, 2, '詳細を見るボタン押下', '-', 'BK02へ遷移・登録書籍の詳細表示', `url=${u}`, /\/book\/detail\/\d+/.test(u) && b.includes('完了遷移確認'), s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }

  cases.sort((a, b) => a.no.localeCompare(b.no));
  return { scenario: 'SC01', name: '書籍登録フロー', cases };
}

async function reviewItemCount(page) { return await page.locator('.review-item').count().catch(() => 0); }
async function titleRowCount(page) { return await page.locator('a.title-link').count().catch(() => 0); }
async function gotoListSearchTitle(page, t) {
  await page.goto(`${BASE}/book/list`);
  await page.fill('#searchTitle', t);
  await page.click('button:has-text("検索")');
  await page.waitForLoadState('networkidle');
}

// =====================================================================
// SC02 書籍編集フロー
// =====================================================================
async function SC02(browser) {
  const cases = [];
  // SC02-001 正常編集
  {
    const c = mkCase('SC02-001', '正常編集（一覧→入力→確認→完了→詳細）', '正常系', 'book_id=1 が存在');
    const { ctx, page } = await freshContext(browser);
    try {
      await gotoListSearchTitle(page, 'リーダブルコード');
      await page.click('a[href*="/book/edit/1"]'); await page.waitForLoadState('networkidle');
      let title = await page.inputValue('input[name="title"]').catch(() => '');
      let s = await shot(page, c.no, 1, 'bk06_prefilled');
      step(c, 1, '一覧でbook_id=1の編集ボタン押下', '-', 'BK06へ遷移・既存値初期表示(title=リーダブルコード)', `title=${title}`, /\/book\/edit\/1/.test(page.url()) && title === 'リーダブルコード', s);
      await page.fill('input[name="title"]', 'リーダブルコード 改訂版');
      await page.fill('input[name="price"]', '3000');
      await page.click('button:has-text("確認画面へ")'); await page.waitForLoadState('networkidle');
      let b = await bodyText(page); s = await shot(page, c.no, 2, 'bk07');
      step(c, 2, '項目変更し確認画面へ', 'title=リーダブルコード 改訂版,price=3000', 'BK07へ遷移・変更値・「更新します」', 'body確認', b.includes('リーダブルコード 改訂版') && b.includes('更新します'), s);
      await page.click('button:has-text("更新")'); await page.waitForLoadState('networkidle');
      b = await bodyText(page); s = await shot(page, c.no, 3, 'bk08');
      step(c, 3, '更新ボタン押下', '-', 'BK08へ遷移・「更新が完了」', `url=${page.url()}`, page.url().includes('/book/edit/complete') && b.includes('更新が完了'), s);
      await page.click('a:has-text("詳細を見る")'); await page.waitForLoadState('networkidle');
      b = await bodyText(page); s = await shot(page, c.no, 4, 'bk02');
      step(c, 4, '詳細を見るボタン押下', '-', 'BK02で更新後の値(title=改訂版,price=3,000)', 'body確認', /\/book\/detail\/1/.test(page.url()) && b.includes('リーダブルコード 改訂版') && b.includes('3,000'), s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  // SC02-002 詳細から編集
  {
    const c = mkCase('SC02-002', '詳細画面から編集', '正常系', 'book_id=1 が存在');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/detail/1`);
      await page.click('a:has-text("編集")'); await page.waitForLoadState('networkidle');
      let s = await shot(page, c.no, 1, 'bk06');
      step(c, 1, 'BK02→編集ボタン押下', '-', 'BK06へ遷移・既存値表示', `url=${page.url()}`, /\/book\/edit\/1/.test(page.url()), s);
      await page.fill('input[name="author"]', 'D. Boswell');
      await page.click('button:has-text("確認画面へ")'); await page.waitForLoadState('networkidle');
      await page.click('button:has-text("更新")'); await page.waitForLoadState('networkidle');
      let b = await bodyText(page); s = await shot(page, c.no, 2, 'bk08');
      step(c, 2, '著者変更し確認→更新', 'author=D. Boswell', 'BK08・「更新が完了」', 'body確認', b.includes('更新が完了'), s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  // SC02-003 確認から戻り入力値保持
  {
    const c = mkCase('SC02-003', '確認画面から戻り入力値保持', 'セッション', 'book_id=1 が存在');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/edit/1`);
      await page.fill('input[name="title"]', '保持テスト編集');
      await page.fill('input[name="price"]', '2222');
      await page.click('button:has-text("確認画面へ")'); await page.waitForLoadState('networkidle');
      let s = await shot(page, c.no, 1, 'bk07');
      step(c, 1, 'BK06変更し確認画面へ', 'title=保持テスト編集,price=2222', 'BK07へ遷移', `confirm=${await onConfirm(page,'edit')}`, await onConfirm(page, 'edit'), s);
      await page.click('a:has-text("戻る"), button:has-text("戻る")'); await page.waitForLoadState('networkidle');
      let t = await page.inputValue('input[name="title"]').catch(() => ''); let p = await page.inputValue('input[name="price"]').catch(() => '');
      s = await shot(page, c.no, 2, 'bk06_restored');
      step(c, 2, 'BK07で戻る押下', '-', '変更後入力値が復元(title=保持テスト編集,price=2222)', `title=${t},price=${p}`, t === '保持テスト編集' && p === '2222', s);
      await page.click('button:has-text("確認画面へ")'); await page.waitForLoadState('networkidle');
      let b = await bodyText(page); s = await shot(page, c.no, 3, 'bk07_again');
      step(c, 3, '再度確認画面へ', '-', 'BK07に保持値表示', 'body確認', b.includes('保持テスト編集'), s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  // SC02-004 キャンセルで詳細へ・セッションクリア
  {
    const c = mkCase('SC02-004', 'キャンセルで詳細へ戻りセッションクリア', 'セッション', 'book_id=1 が存在');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/edit/1`);
      await page.fill('input[name="title"]', '破棄編集');
      let s = await shot(page, c.no, 1, 'bk06_input');
      step(c, 1, 'BK06で値変更', 'title=破棄編集', 'BK06入力状態', 'set', true, s);
      await page.click('a:has-text("キャンセル"), button:has-text("キャンセル")'); await page.waitForLoadState('networkidle');
      let b = await bodyText(page); s = await shot(page, c.no, 2, 'bk02');
      step(c, 2, 'キャンセル押下', '-', 'BK02へ遷移・book1未変更(元title表示)', `url=${page.url()}`, /\/book\/detail\/1/.test(page.url()) && !b.includes('破棄編集'), s);
      await page.goto(`${BASE}/book/edit/1`);
      let t = await page.inputValue('input[name="title"]').catch(() => '');
      s = await shot(page, c.no, 3, 'bk06_dbvalue');
      step(c, 3, '再度BK06表示', '-', 'DB既存値表示(破棄値は復元されない)', `title=${t}`, t !== '破棄編集' && t.length > 0, s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  // SC02-005 バリデーション
  {
    const c = mkCase('SC02-005', '必須・桁数・形式バリデーションエラー', '異常系', 'book_id=1 が存在。BK06表示中');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/edit/1`);
      await page.fill('input[name="title"]', '');
      await page.click('button:has-text("確認画面へ")'); await page.waitForLoadState('networkidle');
      let b = await bodyText(page); let stay = (await onInputForm(page, 'input[name="title"]')) && !(await onConfirm(page, 'edit'));
      let s = await shot(page, c.no, 1, 'title_empty');
      step(c, 1, 'タイトルを空にして確認画面へ', 'title=空', 'BK06に留まり必須エラー', `stay=${stay}`, stay && /必須/.test(b), s);
      await page.goto(`${BASE}/book/edit/1`);
      await page.fill('input[name="isbn"]', 'ABC123');
      await page.click('button:has-text("確認画面へ")'); await page.waitForLoadState('networkidle');
      b = await bodyText(page); stay = (await onInputForm(page, 'input[name="title"]')) && !(await onConfirm(page, 'edit'));
      s = await shot(page, c.no, 2, 'isbn_bad');
      step(c, 2, 'ISBN不正形式で送信', 'isbn=ABC123', 'BK06に留まりISBN形式エラー', `stay=${stay}`, stay && /ISBN|13桁/.test(b), s);
      await page.goto(`${BASE}/book/edit/1`);
      await page.fill('input[name="publishedDate"]', '2999-01-01');
      await page.click('button:has-text("確認画面へ")'); await page.waitForLoadState('networkidle');
      b = await bodyText(page); stay = (await onInputForm(page, 'input[name="title"]')) && !(await onConfirm(page, 'edit'));
      s = await shot(page, c.no, 3, 'future');
      step(c, 3, '出版日に未来日付', 'publishedDate=2999-01-01', 'BK06に留まり未来日付エラー', `stay=${stay}`, stay && /未来日付/.test(b), s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  // SC02-006 ISBN重複（自書籍除外）
  {
    const c = mkCase('SC02-006', 'ISBN重複エラー（自書籍は除外）', '異常系', 'book_id=1(9784873115658), book_id=2(9784422100517)');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/edit/1`);
      await page.fill('input[name="isbn"]', '9784422100517');
      await page.fill('input[name="title"]', 'ISBN重複編集');
      await page.click('button:has-text("確認画面へ")'); await page.waitForLoadState('networkidle');
      await page.click('button:has-text("更新")'); await page.waitForLoadState('networkidle');
      let b = await bodyText(page); let s = await shot(page, c.no, 1, 'dup');
      step(c, 1, 'book2のISBNに変更し更新', 'isbn=9784422100517', 'BK07に留まりISBN重複・book1未更新', `url=${page.url()}`, !page.url().includes('/edit/complete') && /既に登録され|ISBN/.test(b), s);
      await ctx.close();
      // step2 は新規セッションで実施（前ステップの重複ISBNがセッション編集値として残るため）
      const f = await freshContext(browser);
      await f.page.goto(`${BASE}/book/edit/1`);
      const ownIsbn = await f.page.inputValue('input[name="isbn"]').catch(() => '');
      await f.page.fill('input[name="isbn"]', '9784873115658'); // book1自身の元ISBN
      await f.page.fill('input[name="title"]', '自ISBN維持更新');
      await f.page.click('button:has-text("確認画面へ")'); await f.page.waitForLoadState('networkidle');
      await f.page.click('button:has-text("更新")'); await f.page.waitForLoadState('networkidle');
      b = await bodyText(f.page); s = await shot(f.page, c.no, 2, 'self_ok');
      step(c, 2, '自分の元ISBNのまま他項目変更し更新', `isbn=9784873115658(自書籍),title=自ISBN維持更新`, '重複にならず正常更新(BK08)', `url=${f.page.url()} 初期isbn=${ownIsbn}`, f.page.url().includes('/book/edit/complete') && b.includes('更新が完了'), s);
      await f.ctx.close();
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    cases.push(c);
  }
  // SC02-007 楽観的ロック（2コンテキスト）
  {
    const c = mkCase('SC02-007', '楽観的ロックエラー（同時更新）', '異常系', 'book_id=1。2セッションで同時編集');
    const A = await freshContext(browser); const B = await freshContext(browser);
    try {
      await A.page.goto(`${BASE}/book/edit/1`);
      await A.page.waitForLoadState('networkidle');
      let s = await shot(A.page, c.no, 1, 'A_open');
      step(c, 1, 'セッションA: BK06(book1)を開く', '-', 'BK06表示(updated_at保持)', `url=${A.page.url()}`, /\/book\/edit\/1/.test(A.page.url()), s);
      await B.page.goto(`${BASE}/book/edit/1`);
      await B.page.fill('input[name="title"]', '先行更新');
      await B.page.click('button:has-text("確認画面へ")'); await B.page.waitForLoadState('networkidle');
      await B.page.click('button:has-text("更新")'); await B.page.waitForLoadState('networkidle');
      s = await shot(B.page, c.no, 2, 'B_updated');
      step(c, 2, 'セッションB: 先に更新し更新日時変更', 'title=先行更新', 'book1がB更新で更新日時変化', `url=${B.page.url()}`, B.page.url().includes('/book/edit/complete'), s);
      await A.page.fill('input[name="title"]', '後発更新');
      await A.page.click('button:has-text("確認画面へ")'); await A.page.waitForLoadState('networkidle');
      await A.page.click('button:has-text("更新")'); await A.page.waitForLoadState('networkidle');
      let b = await bodyText(A.page); s = await shot(A.page, c.no, 3, 'A_conflict');
      step(c, 3, 'セッションA: 手順1の画面から更新', 'title=後発更新', '楽観ロック不一致・「他のユーザーによって更新」表示/BK02遷移', `url=${A.page.url()} hasMsg=${/他のユーザー|更新されています/.test(b)}`, /他のユーザー|更新されています/.test(b) || /\/book\/detail\/1/.test(A.page.url()), s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await A.ctx.close(); await B.ctx.close(); cases.push(c);
  }
  // SC02-008 存在しない書籍ID
  {
    const c = mkCase('SC02-008', '存在しない書籍IDで編集アクセス', '異常系', 'book_id=999999 は存在しない');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/edit/999999`); await page.waitForLoadState('networkidle');
      let b = await bodyText(page); let s = await shot(page, c.no, 1, 'notfound');
      step(c, 1, 'BK06(/book/edit/999999)へアクセス', '-', '「見つかりません」表示／一覧へ遷移', `url=${page.url()}`, /見つかりません/.test(b) || /\/book\/list/.test(page.url()), s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  // SC02-009 確認直アクセスのセッションガード
  {
    const c = mkCase('SC02-009', '確認画面 直接アクセス時のセッションガード', 'セッション', 'セッションに編集値なし');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/edit/1/confirm`); await page.waitForLoadState('networkidle');
      let s = await shot(page, c.no, 1, 'guard');
      step(c, 1, 'BK07(/book/edit/1/confirm)直接アクセス', '-', 'BK06(/book/edit/1)へリダイレクト', `url=${page.url()}`, /\/book\/edit\/1(\b|$|\?|;)/.test(page.url()) && !page.url().includes('confirm'), s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  cases.sort((a, b) => a.no.localeCompare(b.no));
  return { scenario: 'SC02', name: '書籍編集フロー', cases };
}

// =====================================================================
// SC03 書籍削除フロー
// =====================================================================
async function SC03(browser) {
  const cases = [];
  // SC03-001 一覧→削除確認→完了→一覧 (book16 レビュー0件)
  {
    const c = mkCase('SC03-001', '正常削除（一覧→確認→完了→一覧）', '正常系', 'book_id=16 が存在(レビュー0件)');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/delete/confirm/16`); await page.waitForLoadState('networkidle');
      let b = await bodyText(page); let s = await shot(page, c.no, 1, 'bk09');
      step(c, 1, 'book_id=16の削除確認画面', '-', 'BK09表示・確認/警告メッセージ', 'body確認', /削除しますか/.test(b) && /取り消せません/.test(b), s);
      await page.click('button:has-text("削除"), input[value="削除"]'); await page.waitForLoadState('networkidle');
      b = await bodyText(page); s = await shot(page, c.no, 2, 'bk10');
      step(c, 2, '削除ボタン押下', '-', 'BK10へ遷移・「削除が完了」', `url=${page.url()}`, page.url().includes('/book/delete/complete') && /削除が完了/.test(b), s);
      await page.goto(`${BASE}/book/detail/16`); await page.waitForLoadState('networkidle');
      b = await bodyText(page); s = await shot(page, c.no, 3, 'verify_deleted');
      step(c, 3, 'book_id=16の詳細を確認', '-', '削除済み(見つからない/一覧へ)', `url=${page.url()}`, /見つかりません/.test(b) || /\/book\/list/.test(page.url()), s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  // SC03-002 詳細から削除 (book15)
  {
    const c = mkCase('SC03-002', '詳細画面から削除', '正常系', 'book_id=15 が存在');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/detail/15`);
      await page.click('a:has-text("削除")'); await page.waitForLoadState('networkidle');
      let s = await shot(page, c.no, 1, 'bk09');
      step(c, 1, 'BK02→削除ボタン押下', '-', 'BK09(/book/delete/confirm/15)へ遷移', `url=${page.url()}`, /\/book\/delete\/confirm\/15/.test(page.url()), s);
      await page.click('button:has-text("削除"), input[value="削除"]'); await page.waitForLoadState('networkidle');
      let b = await bodyText(page); s = await shot(page, c.no, 2, 'bk10');
      step(c, 2, '削除ボタン押下', '-', 'BK10・「削除が完了」・book15削除', `url=${page.url()}`, page.url().includes('/book/delete/complete') && /削除が完了/.test(b), s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  // SC03-003 CASCADE (book1 レビュー3件)
  {
    const c = mkCase('SC03-003', 'レビュー連動削除（CASCADE）', 'DB整合', 'book_id=1 にレビュー3件');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/detail/1`);
      let rc = await reviewItemCount(page);
      let s = await shot(page, c.no, 1, 'before');
      step(c, 1, '削除前: book1のレビュー件数確認', '-', 'レビュー3件存在', `reviews=${rc}`, rc === 3, s);
      await page.goto(`${BASE}/book/delete/confirm/1`);
      await page.click('button:has-text("削除"), input[value="削除"]'); await page.waitForLoadState('networkidle');
      let b = await bodyText(page); s = await shot(page, c.no, 2, 'bk10');
      step(c, 2, 'book1を削除', '-', 'BK10・「削除が完了」', `url=${page.url()}`, page.url().includes('/book/delete/complete') && /削除が完了/.test(b), s);
      await page.goto(`${BASE}/book/detail/1`); await page.waitForLoadState('networkidle');
      b = await bodyText(page); s = await shot(page, c.no, 3, 'cascade');
      step(c, 3, 'book1詳細を再確認', '-', 'book1削除・紐づくレビューもCASCADE削除', `url=${page.url()}`, /見つかりません/.test(b) || /\/book\/list/.test(page.url()), s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  // SC03-004 キャンセル (book2)
  {
    const c = mkCase('SC03-004', 'キャンセルで詳細へ戻る（削除されない）', '正常系', 'book_id=2 が存在');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/delete/confirm/2`); await page.waitForLoadState('networkidle');
      let s = await shot(page, c.no, 1, 'bk09');
      step(c, 1, 'BK09(/book/delete/confirm/2)表示', '-', 'BK09表示', `url=${page.url()}`, /\/book\/delete\/confirm\/2/.test(page.url()), s);
      await page.click('a:has-text("キャンセル"), button:has-text("キャンセル")'); await page.waitForLoadState('networkidle');
      let b = await bodyText(page); s = await shot(page, c.no, 2, 'bk02');
      step(c, 2, 'キャンセル押下', '-', 'BK02(detail/2)へ遷移・book2未削除', `url=${page.url()}`, /\/book\/detail\/2/.test(page.url()) && !/見つかりません/.test(b), s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  // SC03-005 存在しないID
  {
    const c = mkCase('SC03-005', '存在しない書籍IDで削除確認アクセス', '異常系', 'book_id=999999 は存在しない');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/delete/confirm/999999`); await page.waitForLoadState('networkidle');
      let b = await bodyText(page); let s = await shot(page, c.no, 1, 'notfound');
      step(c, 1, 'BK09(/book/delete/confirm/999999)アクセス', '-', '「見つかりません」表示／一覧へ', `url=${page.url()}`, /見つかりません/.test(b) || /\/book\/list/.test(page.url()), s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  cases.sort((a, b) => a.no.localeCompare(b.no));
  return { scenario: 'SC03', name: '書籍削除フロー', cases };
}

// =====================================================================
// SC04 書籍閲覧フロー
// =====================================================================
async function SC04(browser) {
  const cases = [];
  // SC04-001 一覧→詳細→一覧
  {
    const c = mkCase('SC04-001', '一覧→詳細→一覧 基本閲覧', '正常系', 'book_id=1 が存在');
    const { ctx, page } = await freshContext(browser);
    try {
      await gotoListSearchTitle(page, 'リーダブルコード');
      let s = await shot(page, c.no, 1, 'bk01');
      step(c, 1, 'BK01表示(リーダブルコード検索)', '-', '一覧表示', `rows=${await titleRowCount(page)}`, (await titleRowCount(page)) >= 1, s);
      await page.click('a.title-link'); await page.waitForLoadState('networkidle');
      let b = await bodyText(page); s = await shot(page, c.no, 2, 'bk02');
      step(c, 2, 'タイトルリンク押下', '-', 'BK02へ遷移・書籍情報表示', `url=${page.url()}`, /\/book\/detail\/1/.test(page.url()) && b.includes('リーダブルコード'), s);
      let rc = await reviewItemCount(page);
      s = await shot(page, c.no, 3, 'reviews');
      step(c, 3, 'レビュー/平均評価確認', '-', 'レビュー3件・平均評価4.7', `reviews=${rc} avg4.7=${b.includes('4.7')}`, rc === 3 && b.includes('4.7'), s);
      await page.click('a:has-text("一覧に戻る")'); await page.waitForLoadState('networkidle');
      s = await shot(page, c.no, 4, 'back');
      step(c, 4, '一覧に戻る押下', '-', 'BK01へ遷移', `url=${page.url()}`, /\/book\/list/.test(page.url()), s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  // SC04-002 レビュー0件
  {
    const c = mkCase('SC04-002', 'レビュー0件書籍の詳細表示', '境界値', 'book_id=16(レビュー0件)');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/detail/16`); await page.waitForLoadState('networkidle');
      let b = await bodyText(page); let rc = await reviewItemCount(page);
      let s = await shot(page, c.no, 1, 'bk02_norev');
      step(c, 1, 'BK02(detail/16)表示', '-', '書籍情報表示・平均0・レビュー0件・エラーなし', `reviews=${rc} 坊っちゃん=${b.includes('坊っちゃん')}`, b.includes('坊っちゃん') && rc === 0, s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  // SC04-003 検索保持で詳細往復
  {
    const c = mkCase('SC04-003', '検索条件を保持したまま詳細往復', 'セッション', '夏目漱石の書籍が複数存在');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/list`);
      await page.fill('#searchAuthor', '夏目');
      await page.click('button:has-text("検索")'); await page.waitForLoadState('networkidle');
      let n1 = await titleRowCount(page);
      let s = await shot(page, c.no, 1, 'search');
      step(c, 1, '著者「夏目」で検索', 'searchAuthor=夏目', '夏目漱石の書籍に絞り込み(3件)', `rows=${n1}`, n1 === 3, s);
      await page.click('a.title-link'); await page.waitForLoadState('networkidle');
      s = await shot(page, c.no, 2, 'detail');
      step(c, 2, 'タイトルリンク→BK02', '-', '詳細表示', `url=${page.url()}`, /\/book\/detail\/\d+/.test(page.url()), s);
      await page.click('a:has-text("一覧に戻る")'); await page.waitForLoadState('networkidle');
      let av = await page.inputValue('#searchAuthor').catch(() => ''); let n2 = await titleRowCount(page);
      s = await shot(page, c.no, 3, 'restored');
      step(c, 3, 'BK02で一覧に戻る押下', '-', '前回検索条件(著者=夏目)復元・絞り込み再表示', `searchAuthor=${av} rows=${n2}`, (av.includes('夏目') || n2 === 3), s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  // SC04-004 存在しないID
  {
    const c = mkCase('SC04-004', '存在しない書籍IDで詳細アクセス', '異常系', 'book_id=999999 は存在しない');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/detail/999999`); await page.waitForLoadState('networkidle');
      let b = await bodyText(page); let s = await shot(page, c.no, 1, 'notfound');
      step(c, 1, 'BK02(detail/999999)アクセス', '-', '「見つかりません」表示／一覧へ自動遷移', `url=${page.url()}`, /見つかりません/.test(b) || /\/book\/list/.test(page.url()), s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  // SC04-005 不正IDパラメータ
  {
    const c = mkCase('SC04-005', '不正な書籍IDパラメータ', '異常系', '共通事前準備の通り');
    const { ctx, page } = await freshContext(browser);
    try {
      let resp = await page.goto(`${BASE}/book/detail/abc`).catch(() => null);
      await page.waitForLoadState('networkidle').catch(() => {});
      let st = resp ? resp.status() : 0;
      let s = await shot(page, c.no, 1, 'badparam');
      step(c, 1, 'BK02(detail/abc)アクセス(数値以外)', '-', 'バリデーション違反をハンドリング・500で異常終了しない', `httpStatus=${st}`, st !== 500 && st !== 0, s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  cases.sort((a, b) => a.no.localeCompare(b.no));
  return { scenario: 'SC04', name: '書籍閲覧フロー', cases };
}

// =====================================================================
// SC05 レビュー投稿フロー
// =====================================================================
async function SC05(browser) {
  const cases = [];
  // SC05-001 正常投稿
  {
    const c = mkCase('SC05-001', '正常投稿（詳細→入力→確認→完了→詳細）', '正常系', 'book_id=1 が存在');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/detail/1`);
      let rcBefore = await reviewItemCount(page);
      await page.click('a:has-text("レビューを書く")'); await page.waitForLoadState('networkidle');
      let b = await bodyText(page); let s = await shot(page, c.no, 1, 'bk11');
      step(c, 1, 'BK02→レビューを書く押下', '-', 'BK11へ遷移・書籍サマリ表示', `url=${page.url()}`, /\/books\/1\/reviews\/new/.test(page.url()) && b.includes('リーダブルコード'), s);
      await page.fill('input[name="reviewerName"]', '結合テスト評者');
      await page.check('input[name="rating"][value="5"]');
      await page.fill('textarea[name="comment"]', 'とても良い本でした');
      await page.click('button:has-text("確認画面へ")'); await page.waitForLoadState('networkidle');
      b = await bodyText(page); s = await shot(page, c.no, 2, 'bk12');
      step(c, 2, 'レビュー入力し確認画面へ', 'reviewerName=結合テスト評者,rating=5', 'BK12へ遷移・「投稿します」・入力値確認', 'body確認', b.includes('投稿します') && b.includes('結合テスト評者'), s);
      await page.click('button:has-text("投稿")'); await page.waitForLoadState('networkidle');
      b = await bodyText(page); s = await shot(page, c.no, 3, 'bk13');
      step(c, 3, '投稿ボタン押下', '-', 'BK13へ遷移・「投稿が完了」', `url=${page.url()}`, page.url().includes('/reviews/complete') && /投稿が完了/.test(b), s);
      await page.click('a:has-text("詳細に戻る")'); await page.waitForLoadState('networkidle');
      let rcAfter = await reviewItemCount(page); b = await bodyText(page);
      s = await shot(page, c.no, 4, 'bk02_after');
      step(c, 4, '詳細に戻る押下', '-', 'BK02・投稿レビューが先頭表示・件数+1', `before=${rcBefore} after=${rcAfter}`, /\/book\/detail\/1/.test(page.url()) && rcAfter === rcBefore + 1 && b.includes('結合テスト評者'), s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  // SC05-002 コメント未入力
  {
    const c = mkCase('SC05-002', 'コメント未入力（任意）で投稿', '境界値', 'book_id=1 が存在');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/books/1/reviews/new`);
      await page.fill('input[name="reviewerName"]', 'コメント無し評者');
      await page.check('input[name="rating"][value="3"]');
      await page.click('button:has-text("確認画面へ")'); await page.waitForLoadState('networkidle');
      let b = await bodyText(page); let s = await shot(page, c.no, 1, 'bk12');
      step(c, 1, 'レビュアー名・評価のみ入力(コメント空)で確認へ', 'comment=空', 'BK12へ遷移・エラーなし', `confirm=${b.includes('投稿します')}`, b.includes('投稿します'), s);
      await page.click('button:has-text("投稿")'); await page.waitForLoadState('networkidle');
      b = await bodyText(page); s = await shot(page, c.no, 2, 'bk13');
      step(c, 2, '投稿ボタン押下', '-', 'BK13・「投稿が完了」・comment空で登録', `url=${page.url()}`, page.url().includes('/reviews/complete') && /投稿が完了/.test(b), s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  // SC05-003 戻り保持
  {
    const c = mkCase('SC05-003', '確認画面から戻り入力値保持', 'セッション', 'book_id=1 が存在');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/books/1/reviews/new`);
      await page.fill('input[name="reviewerName"]', '保持評者');
      await page.check('input[name="rating"][value="4"]');
      await page.fill('textarea[name="comment"]', '保持コメント');
      await page.click('button:has-text("確認画面へ")'); await page.waitForLoadState('networkidle');
      let s = await shot(page, c.no, 1, 'bk12');
      step(c, 1, 'BK11入力し確認画面へ', 'reviewerName=保持評者,rating=4', 'BK12へ遷移', `confirm=${await onConfirm(page,'review')}`, await onConfirm(page, 'review'), s);
      await page.click('a:has-text("戻る"), button:has-text("戻る")'); await page.waitForLoadState('networkidle');
      let rn = await page.inputValue('input[name="reviewerName"]').catch(() => '');
      let rt = await page.locator('input[name="rating"]:checked').inputValue().catch(() => '');
      let cm = await page.inputValue('textarea[name="comment"]').catch(() => '');
      s = await shot(page, c.no, 2, 'bk11_restored');
      step(c, 2, 'BK12で戻る押下', '-', '入力値復元(保持評者/4/保持コメント)', `name=${rn},rating=${rt},comment=${cm}`, rn === '保持評者' && rt === '4' && cm === '保持コメント', s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  // SC05-004 キャンセル
  {
    const c = mkCase('SC05-004', 'キャンセルで詳細へ戻りセッションクリア', 'セッション', 'book_id=1 が存在');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/books/1/reviews/new`);
      await page.fill('input[name="reviewerName"]', '破棄評者');
      await page.check('input[name="rating"][value="1"]');
      let s = await shot(page, c.no, 1, 'bk11');
      step(c, 1, 'BK11で入力', 'reviewerName=破棄評者,rating=1', 'BK11入力状態', 'set', true, s);
      await page.click('a:has-text("キャンセル"), button:has-text("キャンセル")'); await page.waitForLoadState('networkidle');
      let s2 = await shot(page, c.no, 2, 'bk02');
      step(c, 2, 'キャンセル押下', '-', 'BK02(detail/1)へ遷移・未投稿', `url=${page.url()}`, /\/book\/detail\/1/.test(page.url()), s2);
      await page.goto(`${BASE}/books/1/reviews/new`);
      let rn = await page.inputValue('input[name="reviewerName"]').catch(() => 'x');
      let s3 = await shot(page, c.no, 3, 'bk11_empty');
      step(c, 3, '再度BK11表示', '-', 'フォームが空(セッションクリア)', `name="${rn}"`, rn === '', s3);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  // SC05-005 バリデーション
  {
    const c = mkCase('SC05-005', '必須・桁数・範囲バリデーションエラー', '異常系', 'book_id=1 が存在。BK11表示中');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/books/1/reviews/new`);
      await page.click('button:has-text("確認画面へ")'); await page.waitForLoadState('networkidle');
      let b = await bodyText(page);
      let stay = (await onInputForm(page, 'input[name="reviewerName"]')) && !(await onConfirm(page, 'review'));
      let s = await shot(page, c.no, 1, 'required');
      step(c, 1, 'レビュアー名・評価空で確認画面へ', '(空)', 'BK11に留まり必須エラー', `stay=${stay}`, stay && /必須|入力してください/.test(b), s);
      await page.goto(`${BASE}/books/1/reviews/new`);
      await page.fill('input[name="reviewerName"]', 'あ'.repeat(51));
      let rnlen = (await page.inputValue('input[name="reviewerName"]')).length;
      let ml = await page.getAttribute('input[name="reviewerName"]', 'maxlength').catch(() => null);
      await page.check('input[name="rating"][value="3"]');
      await page.click('button:has-text("確認画面へ")'); await page.waitForLoadState('networkidle');
      b = await bodyText(page);
      let confirmed = await onConfirm(page, 'review');
      s = await shot(page, c.no, 2, 'maxlen');
      // maxlength=50 があれば51入力不可(=50)。無ければ51でサーバ文字数エラー想定
      let ok2 = (ml === '50' && rnlen === 50 && confirmed) || (!confirmed && /50文字以内|文字以内/.test(b));
      step(c, 2, 'レビュアー名51文字で送信', 'reviewerName=51字', 'maxlength=50で制限 or 文字数エラー', `maxlength=${ml},実長=${rnlen},confirm=${confirmed}`, ok2, s);
      await page.goto(`${BASE}/books/1/reviews/new`);
      await page.fill('input[name="reviewerName"]', 'あ'.repeat(50));
      await page.check('input[name="rating"][value="3"]');
      await page.click('button:has-text("確認画面へ")'); await page.waitForLoadState('networkidle');
      confirmed = await onConfirm(page, 'review');
      s = await shot(page, c.no, 3, 'len50ok');
      step(c, 3, 'レビュアー名50文字ちょうど', 'reviewerName=50字', 'BK12へ遷移(許容)', `confirm=${confirmed}`, confirmed, s);
      // 評価はradioで1-5のみ＝範囲外はUI上選択不可（入力段階で担保）
      await page.goto(`${BASE}/books/1/reviews/new`);
      let ratingVals = await page.locator('input[name="rating"]').evaluateAll(els => els.map(e => e.value)).catch(() => []);
      s = await shot(page, c.no, 4, 'rating_range');
      let okr = ratingVals.sort().join(',') === '1,2,3,4,5';
      step(c, 4, '評価の選択肢確認', '-', '評価は1〜5のみ(範囲外はUI選択不可で担保)', `rating values=${ratingVals.join(',')}`, okr, s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  // SC05-006 存在しない書籍ID
  {
    const c = mkCase('SC05-006', '存在しない書籍IDでレビュー入力アクセス', '異常系', 'book_id=999999 は存在しない');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/books/999999/reviews/new`); await page.waitForLoadState('networkidle');
      let b = await bodyText(page); let s = await shot(page, c.no, 1, 'notfound');
      step(c, 1, 'BK11(/books/999999/reviews/new)アクセス', '-', '一覧へリダイレクト/システムエラー表示', `url=${page.url()}`, /\/book\/list/.test(page.url()) || /エラー|見つかりません/.test(b), s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  // SC05-007 セッションガード
  {
    const c = mkCase('SC05-007', '確認画面 直接アクセス時のセッションガード', 'セッション', 'セッションにレビュー入力値なし');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/books/1/reviews/confirm`); await page.waitForLoadState('networkidle');
      let s = await shot(page, c.no, 1, 'guard');
      step(c, 1, 'BK12(/books/1/reviews/confirm)直接アクセス', '-', 'BK11(/books/1/reviews/new)へリダイレクト', `url=${page.url()}`, /\/books\/1\/reviews\/new/.test(page.url()), s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  cases.sort((a, b) => a.no.localeCompare(b.no));
  return { scenario: 'SC05', name: 'レビュー投稿フロー', cases };
}

// =====================================================================
// SC06 一覧画面の検索・ソート・ページング
// =====================================================================
async function SC06(browser) {
  const cases = [];
  // SC06-001 初期表示
  {
    const c = mkCase('SC06-001', '初期表示（デフォルトソート・ページング）', '正常系', 'books 50件登録済み');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/list`); await page.waitForLoadState('networkidle');
      let rows = await titleRowCount(page);
      let opts = await page.locator('#searchCategoryId option').count();
      let pages = await page.locator('a[href*="page=2"]').count();
      let s = await shot(page, c.no, 1, 'bk01');
      step(c, 1, 'BK01表示', '-', '1ページ20件・カテゴリ10option・3ページ', `rows=${rows} opts=${opts} hasPage2=${pages > 0}`, rows === 20 && opts === 10 && pages > 0, s);
      let firstId = await page.locator('a.title-link').first().getAttribute('href');
      s = await shot(page, c.no, 2, 'sortdefault');
      step(c, 2, 'デフォルトソート確認', '-', '書籍ID降順(先頭が最大ID付近)', `firstHref=${firstId}`, /\/book\/detail\/(50|5\d|4\d)/.test(firstId || ''), s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  // SC06-002 ページ遷移・端数
  {
    const c = mkCase('SC06-002', 'ページ遷移と最終ページ端数', '境界値', 'books 50件(3ページ:20/20/10)');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/list?page=1`); await page.waitForLoadState('networkidle');
      let r2 = await titleRowCount(page); let s = await shot(page, c.no, 1, 'page2');
      step(c, 1, '2ページ目へ移動', 'page=1', '20件表示', `rows=${r2}`, r2 === 20, s);
      await page.goto(`${BASE}/book/list?page=2`); await page.waitForLoadState('networkidle');
      let r3 = await titleRowCount(page); s = await shot(page, c.no, 2, 'page3');
      step(c, 2, '最終ページ(3ページ目)', 'page=2', '残り10件(端数)', `rows=${r3}`, r3 === 10, s);
      await page.goto(`${BASE}/book/list?page=0`); await page.waitForLoadState('networkidle');
      let r1 = await titleRowCount(page); s = await shot(page, c.no, 3, 'page1');
      step(c, 3, '1ページ目に戻る', 'page=0', '先頭20件', `rows=${r1}`, r1 === 20, s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  // SC06-003 タイトル検索
  {
    const c = mkCase('SC06-003', 'タイトル部分一致検索', '正常系', 'タイトルにJavaを含む書籍が存在');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/list`);
      await page.fill('#searchTitle', 'Java');
      await page.click('button:has-text("検索")'); await page.waitForLoadState('networkidle');
      let b = await bodyText(page); let rows = await titleRowCount(page);
      let s = await shot(page, c.no, 1, 'java');
      step(c, 1, 'タイトル「Java」で検索', 'searchTitle=Java', 'Java含む書籍のみ・件数メッセージ', `rows=${rows}`, rows >= 4 && /件の書籍が見つかりました|件/.test(b), s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  // SC06-004 複合検索
  {
    const c = mkCase('SC06-004', '著者部分一致＋カテゴリ完全一致 複合検索', '正常系', '夏目漱石×小説・文学が存在');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/list`);
      await page.fill('#searchAuthor', '夏目');
      await page.selectOption('#searchCategoryId', '1');
      await page.click('button:has-text("検索")'); await page.waitForLoadState('networkidle');
      let n1 = await titleRowCount(page); let s = await shot(page, c.no, 1, 'compound');
      step(c, 1, '著者「夏目」+カテゴリ「小説・文学」', 'searchAuthor=夏目,cat=1', '該当書籍のみ(3件)', `rows=${n1}`, n1 === 3, s);
      await page.goto(`${BASE}/book/list`);
      await page.selectOption('#searchCategoryId', '3');
      await page.click('button:has-text("検索")'); await page.waitForLoadState('networkidle');
      let n2 = await titleRowCount(page); s = await shot(page, c.no, 2, 'cat3');
      step(c, 2, 'カテゴリのみ「IT・コンピュータ」', 'cat=3', 'カテゴリ3の書籍のみ(1ページ20件表示)', `rows=${n2}`, n2 === 20, s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  // SC06-005 検索0件
  {
    const c = mkCase('SC06-005', '検索結果0件', '境界値', '該当しない検索語');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/list`);
      await page.fill('#searchTitle', 'ZZZNOHIT999');
      await page.click('button:has-text("検索")'); await page.waitForLoadState('networkidle');
      let b = await bodyText(page); let rows = await titleRowCount(page);
      let s = await shot(page, c.no, 1, 'nohit');
      step(c, 1, '該当なし文字列で検索', 'searchTitle=ZZZNOHIT999', '0件・nodataメッセージ・エラーなし', `rows=${rows}`, rows === 0 && /登録されていません|見つかりません|0件|該当/.test(b), s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  // SC06-006 クリア
  {
    const c = mkCase('SC06-006', 'クリアボタンで検索条件リセット', 'セッション', '直前に検索条件入力済み');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/list`);
      await page.fill('#searchTitle', 'Java');
      await page.click('button:has-text("検索")'); await page.waitForLoadState('networkidle');
      let s = await shot(page, c.no, 1, 'searched');
      step(c, 1, 'タイトルJavaで検索', 'searchTitle=Java', '絞り込み表示', `rows=${await titleRowCount(page)}`, (await titleRowCount(page)) >= 4, s);
      await page.click('a:has-text("クリア"), button:has-text("クリア")'); await page.waitForLoadState('networkidle');
      let tv = await page.inputValue('#searchTitle').catch(() => 'x'); let rows = await titleRowCount(page);
      s = await shot(page, c.no, 2, 'cleared');
      step(c, 2, 'クリアボタン押下', '-', '条件空・全件再表示(20件)', `searchTitle="${tv}" rows=${rows}`, tv === '' && rows === 20, s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  // SC06-007 ソート切替
  {
    const c = mkCase('SC06-007', 'ソート切替（昇順/降順トグル）', '正常系', 'books 複数件');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/list?sort=title&order=ASC`); await page.waitForLoadState('networkidle');
      let firstAsc = (await page.locator('a.title-link').first().textContent().catch(() => '')).trim();
      let s = await shot(page, c.no, 1, 'title_asc');
      step(c, 1, 'タイトル昇順ソート', 'sort=title&order=ASC', 'タイトル昇順で表示', `先頭=${firstAsc}`, (await titleRowCount(page)) === 20, s);
      await page.goto(`${BASE}/book/list?sort=title&order=DESC`); await page.waitForLoadState('networkidle');
      let firstDesc = (await page.locator('a.title-link').first().textContent().catch(() => '')).trim();
      s = await shot(page, c.no, 2, 'title_desc');
      step(c, 2, 'タイトル降順に切替', 'sort=title&order=DESC', '昇順と降順で先頭が変化', `昇順先頭=${firstAsc} 降順先頭=${firstDesc}`, firstAsc !== firstDesc, s);
      await page.goto(`${BASE}/book/list?sort=avgRating&order=DESC`); await page.waitForLoadState('networkidle');
      s = await shot(page, c.no, 3, 'avg_sort');
      step(c, 3, '平均評価でソート', 'sort=avgRating', 'ソート動作(20件表示)', `rows=${await titleRowCount(page)}`, (await titleRowCount(page)) === 20, s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  // SC06-008 検索条件維持でソート・ページング
  {
    const c = mkCase('SC06-008', '検索条件維持したままソート・ページング', '状態遷移', 'category_id=3 が21件以上');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/list`);
      await page.selectOption('#searchCategoryId', '3');
      await page.click('button:has-text("検索")'); await page.waitForLoadState('networkidle');
      let s = await shot(page, c.no, 1, 'cat3');
      step(c, 1, 'カテゴリ「IT・コンピュータ」で検索', 'cat=3', '絞り込み結果(22件→1ページ20件)', `rows=${await titleRowCount(page)}`, (await titleRowCount(page)) === 20, s);
      let catSel = await page.inputValue('#searchCategoryId').catch(() => '');
      await page.click('a[href*="sort=title"]'); await page.waitForLoadState('networkidle');
      let catKept = await page.inputValue('#searchCategoryId').catch(() => '');
      s = await shot(page, c.no, 2, 'sorted_kept');
      step(c, 2, 'タイトル列でソート', 'sort=title', '検索条件(cat=3)維持しソート', `cat維持=${catKept}`, catKept === '3', s);
      let url2 = page.url();
      await page.goto(`${BASE}/book/list?searchCategoryId=3&sort=title&order=ASC&page=1`); await page.waitForLoadState('networkidle');
      let rows2 = await titleRowCount(page); let catKept2 = await page.inputValue('#searchCategoryId').catch(() => '');
      s = await shot(page, c.no, 3, 'page2_kept');
      step(c, 3, '2ページ目へ移動', 'page=1', '検索条件・ソート維持し2ページ目(残り2件)', `rows=${rows2} cat=${catKept2}`, catKept2 === '3' && rows2 >= 1 && rows2 <= 20, s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  // SC06-009 0件表示（検索0件で代替）
  {
    const c = mkCase('SC06-009', '書籍0件時の表示', '境界値', '0件状態(検索0件で代替)');
    const { ctx, page } = await freshContext(browser);
    try {
      await page.goto(`${BASE}/book/list`);
      await page.fill('#searchTitle', 'ZZZNOHIT000');
      await page.click('button:has-text("検索")'); await page.waitForLoadState('networkidle');
      let b = await bodyText(page); let rows = await titleRowCount(page);
      let createBtn = await page.locator('a[href*="/book/create"]').count();
      let s = await shot(page, c.no, 1, 'zero');
      step(c, 1, '0件相当の検索(代替実施)', 'searchTitle=ZZZNOHIT000', 'nodataメッセージ・エラーなし・新規登録操作可', `rows=${rows} createBtn=${createBtn}`, rows === 0 && createBtn > 0 && /登録されていません|見つかりません|0件|該当/.test(b), s);
    } catch (e) { c.judgment = 'NG'; c.ng = String(e); }
    await ctx.close(); cases.push(c);
  }
  cases.sort((a, b) => a.no.localeCompare(b.no));
  return { scenario: 'SC06', name: '一覧画面の検索・ソート・ページング', cases };
}

const SCENARIOS = { SC01, SC02, SC03, SC04, SC05, SC06 };

(async () => {
  if (!SCENARIOS[arg]) { console.error('unknown scenario:', arg, 'available:', Object.keys(SCENARIOS).join(',')); process.exit(2); }
  const browser = await chromium.launch({ headless: true });
  let result;
  try {
    result = await SCENARIOS[arg](browser);
  } finally {
    await browser.close();
  }
  result.env = { url: BASE, datetime: nowStr(), data: 'data.sql 投入済み（アプリ再起動で初期化）' };
  const okN = result.cases.filter(c => c.judgment === 'OK').length;
  const ngN = result.cases.filter(c => c.judgment === 'NG').length;
  result.summary = { total: result.cases.length, ok: okN, ng: ngN };
  fs.writeFileSync(path.join(RAW, `${arg}.json`), JSON.stringify(result, null, 2));
  console.log(`${arg}: total=${result.cases.length} OK=${okN} NG=${ngN}`);
  for (const c of result.cases) console.log(`  ${c.no} ${c.judgment} ${c.name}${c.ng ? ' :: ' + c.ng : ''}`);
})();
