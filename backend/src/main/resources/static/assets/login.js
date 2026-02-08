(() => {
  const TOKEN_KEY = "token";
  const DEFAULT_NEXT = "/index.html";
  const $ = (id) => document.getElementById(id);

  const nodes = {
    loginForm: $("loginForm"),
    loginBtn: $("loginBtn"),
    username: $("username"),
    password: $("password"),
    loginHint: $("loginHint"),
    loginResult: $("loginResult")
  };

  function getNextPath() {
    const next = new URLSearchParams(window.location.search).get("next");
    if (next && next.startsWith("/")) {
      return next;
    }
    return DEFAULT_NEXT;
  }

  function getToken() {
    return localStorage.getItem(TOKEN_KEY);
  }

  function setToken(token) {
    if (!token) {
      localStorage.removeItem(TOKEN_KEY);
      return;
    }
    localStorage.setItem(TOKEN_KEY, token);
  }

  function setHint(message, isError = false) {
    if (!nodes.loginHint) {
      return;
    }
    nodes.loginHint.textContent = message || "";
    nodes.loginHint.style.color = isError ? "#b6453e" : "#597082";
  }

  function setLoading(loading) {
    if (!nodes.loginBtn) {
      return;
    }
    nodes.loginBtn.disabled = loading;
    nodes.loginBtn.textContent = loading ? "正在登录..." : "登录并进入主页面";
  }

  function renderRaw(payload) {
    if (!nodes.loginResult) {
      return;
    }
    nodes.loginResult.textContent = JSON.stringify(payload, null, 2);
  }

  function gotoMain() {
    window.location.replace(getNextPath());
  }

  async function callApi(path, options = {}) {
    try {
      const response = await fetch(path, options);
      let payload;
      try {
        payload = await response.json();
      } catch {
        payload = {
          success: false,
          message: `响应解析失败: ${response.status}`
        };
      }

      return {
        ok: Boolean(response.ok && payload?.success),
        status: response.status,
        payload
      };
    } catch (error) {
      return {
        ok: false,
        status: 0,
        payload: {
          success: false,
          message: error.message
        }
      };
    }
  }

  async function checkExistingSession() {
    const token = getToken();
    if (!token) {
      return;
    }

    setHint("检测到历史会话，正在校验...");

    const { ok, payload } = await callApi("/api/auth/me", {
      headers: {
        Authorization: `Bearer ${token}`
      }
    });

    renderRaw(payload);
    if (ok) {
      gotoMain();
      return;
    }

    setToken("");
    setHint("历史会话已失效，请重新登录。", true);
  }

  async function submitLogin(event) {
    event.preventDefault();
    setHint("");

    if (window.location.protocol === "file:") {
      setHint("请通过 http://localhost:18081/login.html 访问登录页。", true);
      return;
    }

    const username = nodes.username?.value?.trim();
    const password = nodes.password?.value || "";
    if (!username || !password) {
      setHint("请输入用户名和密码。", true);
      return;
    }

    setLoading(true);

    const { ok, payload } = await callApi("/api/auth/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ username, password })
    });

    renderRaw(payload);
    if (ok && payload?.data?.token) {
      setToken(payload.data.token);
      setHint("登录成功，正在跳转...");
      gotoMain();
      return;
    }

    setHint(payload?.message || "登录失败，请重试。", true);
    setLoading(false);
  }

  async function init() {
    nodes.loginForm?.addEventListener("submit", submitLogin);
    await checkExistingSession();
  }

  init();
})();
