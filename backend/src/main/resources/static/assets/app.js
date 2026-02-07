(() => {
  const $ = (id) => document.getElementById(id);

  const state = {
    tokenKey: "token",
    apiBase: "",
    charts: {},
    chartReady: false
  };

  const nodes = {
    menuItems: Array.from(document.querySelectorAll(".menu-item")),
    views: Array.from(document.querySelectorAll(".view")),
    statusLine: $("statusLine"),
    tokenBadge: $("tokenBadge"),
    debugLog: $("debugLog"),

    loginResult: $("loginResult"),
    loginHint: $("loginHint"),
    sessionState: $("sessionState"),
    sessionUser: $("sessionUser"),
    sessionRole: $("sessionRole"),
    tokenPreview: $("tokenPreview"),
    copyTokenBtn: $("copyTokenBtn"),
    authRawToggle: $("authRawToggle"),

    uploadResult: $("uploadResult"),
    uploadHint: $("uploadHint"),
    uploadTotal: $("uploadTotal"),
    uploadSuccess: $("uploadSuccess"),
    uploadFailed: $("uploadFailed"),
    uploadRawToggle: $("uploadRawToggle"),

    totalCount: $("totalCount"),
    avgResponse: $("avgResponse"),
    minResponse: $("minResponse"),
    maxResponse: $("maxResponse")
  };

  const palette = {
    teal: "#0f6f79",
    orange: "#cb7c36",
    blue: "#2d5f92",
    red: "#b6453e",
    axis: "#4f667a",
    grid: "#d6e0e9",
    text: "#2d465b"
  };

  function getToken() {
    return localStorage.getItem(state.tokenKey);
  }

  function setToken(token) {
    if (!token) {
      localStorage.removeItem(state.tokenKey);
      return;
    }
    localStorage.setItem(state.tokenKey, token);
  }

  function shortenToken(token) {
    if (!token) {
      return "-";
    }
    if (token.length <= 34) {
      return token;
    }
    return `${token.slice(0, 18)}...${token.slice(-12)}`;
  }

  function roleFromAuthorities(authorities) {
    if (!Array.isArray(authorities) || authorities.length === 0) {
      return "-";
    }
    return String(authorities[0]).replace(/^ROLE_/, "");
  }

  function setCopyTokenEnabled(enabled) {
    if (!nodes.copyTokenBtn) {
      return;
    }
    nodes.copyTokenBtn.disabled = !enabled;
    nodes.copyTokenBtn.style.opacity = enabled ? "1" : "0.56";
    nodes.copyTokenBtn.style.cursor = enabled ? "pointer" : "not-allowed";
  }

  function setSessionInfo(sessionState, username, role, token) {
    if (nodes.sessionState) {
      nodes.sessionState.textContent = sessionState || "未登录";
      nodes.sessionState.classList.toggle("session-state-online", sessionState === "已登录");
      nodes.sessionState.classList.toggle("session-state-offline", sessionState !== "已登录");
    }

    if (nodes.sessionUser) {
      nodes.sessionUser.textContent = username || "-";
    }

    if (nodes.sessionRole) {
      nodes.sessionRole.textContent = role || "-";
    }

    if (nodes.tokenPreview) {
      nodes.tokenPreview.textContent = shortenToken(token);
    }

    setCopyTokenEnabled(Boolean(token));
  }

  function renderAuthRaw(payload) {
    if (!nodes.loginResult) {
      return;
    }
    nodes.loginResult.textContent = JSON.stringify(payload, null, 2);
  }

  function setAuthHint(message, isError = false) {
    if (!nodes.loginHint) {
      return;
    }
    nodes.loginHint.textContent = message || "";
    nodes.loginHint.style.color = isError ? "#b6453e" : "#5b7388";
  }

  function formatMetric(value) {
    const num = Number(value);
    if (!Number.isFinite(num)) {
      return "-";
    }
    return num.toLocaleString();
  }

  function setUploadSummary(total, success, failed) {
    if (nodes.uploadTotal) {
      nodes.uploadTotal.textContent = formatMetric(total);
    }
    if (nodes.uploadSuccess) {
      nodes.uploadSuccess.textContent = formatMetric(success);
    }
    if (nodes.uploadFailed) {
      nodes.uploadFailed.textContent = formatMetric(failed);
    }
  }

  function setUploadHint(message, isError = false) {
    if (!nodes.uploadHint) {
      return;
    }
    nodes.uploadHint.textContent = message || "";
    nodes.uploadHint.style.color = isError ? "#b6453e" : "#5b7388";
  }

  function renderUploadRaw(payload) {
    if (!nodes.uploadResult) {
      return;
    }
    nodes.uploadResult.textContent = JSON.stringify(payload, null, 2);
  }

  function syncAuthPanelFromStorage() {
    const token = getToken();
    if (token) {
      setSessionInfo("已登录", "-", "-", token);
    } else {
      setSessionInfo("未登录", "-", "-", "");
    }
  }

  function log(message, level = "info") {
    const now = new Date().toLocaleTimeString();
    if (nodes.debugLog) {
      nodes.debugLog.textContent = `[${now}] ${message}\n${nodes.debugLog.textContent}`;
    }

    if (nodes.statusLine) {
      nodes.statusLine.textContent = message;
      if (level === "error") {
        nodes.statusLine.style.background = "#fff2f0";
        nodes.statusLine.style.borderColor = "#f1cbc7";
        nodes.statusLine.style.color = "#8d3731";
      } else {
        nodes.statusLine.style.background = "#edf7fa";
        nodes.statusLine.style.borderColor = "#cde2e7";
        nodes.statusLine.style.color = "#1a6570";
      }
    }
  }

  function updateTokenBadge() {
    const token = getToken();
    if (!nodes.tokenBadge) {
      return;
    }

    if (token) {
      nodes.tokenBadge.textContent = "已登录";
      nodes.tokenBadge.classList.remove("chip-warn");
      nodes.tokenBadge.classList.add("chip-ok");
    } else {
      nodes.tokenBadge.textContent = "未登录";
      nodes.tokenBadge.classList.remove("chip-ok");
      nodes.tokenBadge.classList.add("chip-warn");
    }
  }

  async function request(path, options = {}) {
    const headers = { ...(options.headers || {}) };
    const token = getToken();
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }

    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 12000);

    try {
      const response = await fetch(`${state.apiBase}${path}`, {
        ...options,
        headers,
        signal: controller.signal
      });
      clearTimeout(timeout);

      let payload;
      try {
        payload = await response.json();
      } catch {
        payload = {
          success: false,
          message: `响应解析失败: ${response.status}`
        };
      }

      const ok = Boolean(response.ok && payload && payload.success);
      if (!ok) {
        const msg = payload?.message || `请求失败 ${response.status}`;
        log(`${path} -> ${msg}`, "error");
      }

      return {
        ok,
        status: response.status,
        payload
      };
    } catch (error) {
      clearTimeout(timeout);
      log(`${path} -> ${error.message}`, "error");
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

  function fillDemoDate() {
    const demo = {
      start: "2025-01-01 00:00:00",
      end: "2025-01-02 00:00:00",
      start1: "2025-01-01 00:00:00",
      end1: "2025-01-02 00:00:00",
      start2: "2025-01-03 00:00:00",
      end2: "2025-01-04 00:00:00"
    };

    Object.entries(demo).forEach(([id, value]) => {
      const el = $(id);
      if (el) {
        el.value = value;
      }
    });

    log("已填入示例时间");
  }

  function bindMenu() {
    nodes.menuItems.forEach((item) => {
      item.addEventListener("click", () => {
        const target = item.dataset.view;
        nodes.menuItems.forEach((node) => node.classList.toggle("active", node === item));
        nodes.views.forEach((view) => view.classList.toggle("active", view.dataset.view === target));
        setTimeout(() => resizeCharts(target), 80);
        log(`已切换到 ${item.querySelector("strong")?.textContent || target}`);
      });
    });
  }

  function ensureCharts() {
    if (!window.echarts) {
      log("ECharts 未加载", "error");
      return false;
    }

    if (!state.chartReady) {
      state.charts.level = echarts.init($("levelChart"));
      state.charts.status = echarts.init($("statusChart"));
      state.charts.trend = echarts.init($("trendChart"));
      state.charts.compare = echarts.init($("compareChart"));
      state.chartReady = true;

      window.addEventListener("resize", () => {
        Object.values(state.charts).forEach((chart) => chart?.resize());
      });
    }

    return true;
  }

  function resizeCharts(view) {
    if (!ensureCharts()) {
      return;
    }

    const map = {
      overview: [state.charts.level, state.charts.status],
      trend: [state.charts.trend],
      compare: [state.charts.compare]
    };
    (map[view] || []).forEach((chart) => chart?.resize());
  }

  function readTimeRange() {
    return {
      start: encodeURIComponent($("start").value),
      end: encodeURIComponent($("end").value)
    };
  }

  async function login() {
    setAuthHint("");

    if (location.protocol === "file:") {
      setAuthHint("请通过 http://localhost:18081 访问，不要直接打开本地文件。", true);
      return;
    }

    const username = $("username").value;
    const password = $("password").value;

    const { ok, payload } = await request("/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password })
    });

    renderAuthRaw(payload);

    if (ok && payload.data?.token) {
      setToken(payload.data.token);
      updateTokenBadge();
      setSessionInfo("已登录", payload.data.username || "-", payload.data.role || "-", payload.data.token);
      setAuthHint("登录成功，会话已更新。", false);
      if (nodes.authRawToggle) {
        nodes.authRawToggle.open = false;
      }
      log(`登录成功: ${payload.data.username}`);
      await loadAll();
      return;
    }

    setAuthHint(payload?.message || "登录失败", true);
    if (nodes.authRawToggle) {
      nodes.authRawToggle.open = true;
    }
  }

  async function whoami() {
    const { ok, payload } = await request("/api/auth/me");
    renderAuthRaw(payload);

    if (ok && payload.data) {
      const role = roleFromAuthorities(payload.data.authorities);
      setSessionInfo("已登录", payload.data.username || "-", role, getToken());
      setAuthHint("当前用户信息已刷新。", false);
      if (nodes.authRawToggle) {
        nodes.authRawToggle.open = false;
      }
      return;
    }

    setAuthHint(payload?.message || "获取当前用户失败", true);
    if (nodes.authRawToggle) {
      nodes.authRawToggle.open = true;
    }
  }

  async function copyToken() {
    const token = getToken();
    if (!token) {
      setAuthHint("当前没有可复制的 Token。", true);
      return;
    }

    try {
      if (navigator.clipboard && window.isSecureContext) {
        await navigator.clipboard.writeText(token);
      } else {
        const textarea = document.createElement("textarea");
        textarea.value = token;
        textarea.setAttribute("readonly", "readonly");
        textarea.style.position = "absolute";
        textarea.style.left = "-9999px";
        document.body.appendChild(textarea);
        textarea.select();
        document.execCommand("copy");
        document.body.removeChild(textarea);
      }
      setAuthHint("Token 已复制。", false);
      log("Token 已复制");
    } catch (error) {
      setAuthHint("复制失败，请手动复制。", true);
      log(`复制 Token 失败: ${error.message}`, "error");
    }
  }

  function logout() {
    setToken("");
    updateTokenBadge();
    setSessionInfo("未登录", "-", "-", "");
    setAuthHint("已退出登录。", false);
    renderAuthRaw({ success: true, message: "已退出", data: null });
    if (nodes.authRawToggle) {
      nodes.authRawToggle.open = false;
    }
    log("已退出登录");
  }

  async function uploadLogs() {
    const file = $("logFile").files?.[0];
    if (!file) {
      setUploadHint("请先选择日志文件。", true);
      log("请先选择日志文件", "error");
      return;
    }

    setUploadHint(`正在上传 ${file.name} ...`);
    if (nodes.uploadRawToggle) {
      nodes.uploadRawToggle.open = false;
    }

    const form = new FormData();
    form.append("file", file);

    const { ok, payload } = await request("/api/logs/upload", {
      method: "POST",
      body: form
    });

    renderUploadRaw(payload);
    setUploadSummary(payload?.data?.totalLines, payload?.data?.successCount, payload?.data?.failedCount);

    if (ok) {
      setUploadHint("上传并解析完成。");
      if (nodes.uploadRawToggle) {
        nodes.uploadRawToggle.open = false;
      }
      log("上传成功，正在刷新看板");
      await loadAll();
      return;
    }

    setUploadHint(payload?.message || "上传失败。", true);
    if (nodes.uploadRawToggle) {
      nodes.uploadRawToggle.open = true;
    }
  }

  function renderLevelChart(levelCounts) {
    const keys = Object.keys(levelCounts || {});
    const values = keys.map((k) => levelCounts[k]);

    state.charts.level.setOption({
      title: {
        text: "日志级别分布",
        left: "center",
        textStyle: { color: palette.text, fontSize: 13 }
      },
      tooltip: { trigger: "axis" },
      grid: { top: 44, left: 40, right: 18, bottom: 40 },
      xAxis: {
        type: "category",
        data: keys,
        axisLabel: { color: palette.axis },
        axisLine: { lineStyle: { color: palette.grid } }
      },
      yAxis: {
        type: "value",
        axisLabel: { color: palette.axis },
        splitLine: { lineStyle: { color: palette.grid } }
      },
      series: [{
        type: "bar",
        data: values,
        barWidth: 28,
        itemStyle: {
          color: palette.teal,
          borderRadius: [8, 8, 0, 0]
        }
      }]
    });
  }

  function renderStatusChart(statusCounts) {
    const values = Object.keys(statusCounts || {}).map((k) => ({
      name: k,
      value: statusCounts[k]
    }));

    state.charts.status.setOption({
      title: {
        text: "状态码占比",
        left: "center",
        textStyle: { color: palette.text, fontSize: 13 }
      },
      tooltip: { trigger: "item" },
      legend: {
        bottom: 0,
        textStyle: { color: palette.axis, fontSize: 11 }
      },
      series: [{
        type: "pie",
        radius: ["34%", "64%"],
        itemStyle: { borderColor: "#f4f8fb", borderWidth: 2 },
        data: values
      }]
    });
  }

  async function loadSummary() {
    if (!ensureCharts()) {
      return;
    }

    const { start, end } = readTimeRange();
    const { ok, payload } = await request(`/api/stats/summary?start=${start}&end=${end}`);
    if (!ok || !payload.data) {
      return;
    }

    nodes.totalCount.textContent = payload.data.totalCount ?? 0;
    renderLevelChart(payload.data.levelCounts || {});
    renderStatusChart(payload.data.statusCounts || {});
  }

  async function loadPerformance() {
    const { start, end } = readTimeRange();
    const { ok, payload } = await request(`/api/stats/performance?start=${start}&end=${end}`);
    if (!ok || !payload.data) {
      return;
    }

    const data = payload.data;
    nodes.avgResponse.textContent = Number(data.avgMs || 0).toFixed(2);
    nodes.minResponse.textContent = data.minMs ?? 0;
    nodes.maxResponse.textContent = data.maxMs ?? 0;
  }

  async function loadTrend() {
    if (!ensureCharts()) {
      return;
    }

    const { start, end } = readTimeRange();
    const statusCode = encodeURIComponent($("errorCode").value || "500");
    const bucketMinutes = encodeURIComponent($("bucketMinutes").value || "60");

    const { ok, payload } = await request(
      `/api/stats/error-trend?start=${start}&end=${end}&statusCode=${statusCode}&bucketMinutes=${bucketMinutes}`
    );
    if (!ok || !Array.isArray(payload.data)) {
      return;
    }

    const labels = payload.data.map((item) => item.bucket);
    const points = payload.data.map((item) => item.count);

    state.charts.trend.setOption({
      title: {
        text: "错误趋势",
        left: "center",
        textStyle: { color: palette.text, fontSize: 13 }
      },
      tooltip: { trigger: "axis" },
      grid: { top: 44, left: 40, right: 18, bottom: 50 },
      xAxis: {
        type: "category",
        data: labels,
        axisLabel: {
          color: palette.axis,
          rotate: labels.length > 9 ? 28 : 0
        },
        axisLine: { lineStyle: { color: palette.grid } }
      },
      yAxis: {
        type: "value",
        axisLabel: { color: palette.axis },
        splitLine: { lineStyle: { color: palette.grid } }
      },
      series: [{
        type: "line",
        smooth: true,
        showSymbol: false,
        lineStyle: { width: 3, color: palette.orange },
        areaStyle: { color: "rgba(203, 124, 54, 0.18)" },
        data: points
      }]
    });

    log("错误趋势已更新");
  }

  async function loadCompare() {
    if (!ensureCharts()) {
      return;
    }

    const start1 = encodeURIComponent($("start1").value);
    const end1 = encodeURIComponent($("end1").value);
    const start2 = encodeURIComponent($("start2").value);
    const end2 = encodeURIComponent($("end2").value);

    const { ok, payload } = await request(
      `/api/stats/compare?start1=${start1}&end1=${end1}&start2=${start2}&end2=${end2}`
    );
    if (!ok || !payload.data) {
      return;
    }

    const diff = payload.data.statusDiff || {};
    const labels = Object.keys(diff);
    const values = labels.map((k) => diff[k]);

    state.charts.compare.setOption({
      title: {
        text: "区间状态码差值",
        left: "center",
        textStyle: { color: palette.text, fontSize: 13 }
      },
      tooltip: { trigger: "axis" },
      grid: { top: 44, left: 40, right: 18, bottom: 40 },
      xAxis: {
        type: "category",
        data: labels,
        axisLabel: { color: palette.axis },
        axisLine: { lineStyle: { color: palette.grid } }
      },
      yAxis: {
        type: "value",
        axisLabel: { color: palette.axis },
        splitLine: { lineStyle: { color: palette.grid } }
      },
      series: [{
        type: "bar",
        data: values,
        barWidth: 28,
        itemStyle: {
          color: (p) => (p.value >= 0 ? palette.blue : palette.red),
          borderRadius: [8, 8, 0, 0]
        }
      }]
    });

    log("区间对比已更新");
  }

  async function loadAll() {
    log("正在刷新看板");
    await loadSummary();
    await loadPerformance();
    await loadTrend();
    log("看板刷新完成");
  }

  function bindActions() {
    $("fillDemoBtn").addEventListener("click", fillDemoDate);
    $("refreshBtn").addEventListener("click", loadAll);
    $("runOverviewBtn").addEventListener("click", loadAll);
    $("runCompareShortcutBtn").addEventListener("click", loadCompare);

    $("loginBtn").addEventListener("click", login);
    $("logoutBtn").addEventListener("click", logout);
    $("whoamiBtn").addEventListener("click", whoami);
    if (nodes.copyTokenBtn) {
      nodes.copyTokenBtn.addEventListener("click", copyToken);
    }

    $("uploadBtn").addEventListener("click", uploadLogs);
    $("fillDemoIngestBtn").addEventListener("click", fillDemoDate);

    $("trendBtn").addEventListener("click", loadTrend);
    $("compareBtn").addEventListener("click", loadCompare);
  }

  function init() {
    bindMenu();
    bindActions();
    fillDemoDate();
    ensureCharts();
    updateTokenBadge();
    syncAuthPanelFromStorage();
    renderAuthRaw({ success: true, message: "就绪", data: null });
    renderUploadRaw({ success: true, message: "尚未上传", data: null });
    setUploadSummary("-", "-", "-");
    setUploadHint("上传后将显示统计摘要。");
    if (nodes.uploadRawToggle) {
      nodes.uploadRawToggle.open = false;
    }
    log("前端已初始化");
  }

  init();
})();
