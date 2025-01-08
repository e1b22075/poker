window.onload = function () {
  // 現在のURLのクエリパラメータを取得します。
  const urlParams = new URLSearchParams(window.location.search);
  // 'error' という名前のクエリパラメータを取得します。
  const errorMessage = urlParams.get('error');
  // errorMessageが存在する場合（エラーメッセージがURLに含まれる場合）
  if (errorMessage) {
    // エラーメッセージを表示するためのHTML要素にテキストとしてセットします。
    document.getElementById('error-message').textContent = errorMessage;
  }
};
