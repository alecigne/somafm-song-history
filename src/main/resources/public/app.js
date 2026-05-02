const state = {
  view: "broadcasts",
  page: 1,
  size: 50,
  totalPages: 0,
  totalElements: 0,
  loading: false
};

const viewButtons = [...document.querySelectorAll(".view-button")];
const summary = document.querySelector("#summary");
const message = document.querySelector("#message");
const previousPage = document.querySelector("#previous-page");
const nextPage = document.querySelector("#next-page");
const pageStatus = document.querySelector("#page-status");
const pageSize = document.querySelector("#page-size");
const tableHead = document.querySelector("#table-head");
const tableBody = document.querySelector("#table-body");

viewButtons.forEach((button) => {
  button.addEventListener("click", () => {
    state.view = button.dataset.view;
    state.page = 1;
    renderShell();
    void loadPage();
  });
});

previousPage.addEventListener("click", () => {
  if (state.page <= 1 || state.loading) return;
  state.page -= 1;
  void loadPage();
});

nextPage.addEventListener("click", () => {
  if (state.totalPages !== 0 && state.page >= state.totalPages) return;
  if (state.loading) return;
  state.page += 1;
  void loadPage();
});

pageSize.addEventListener("change", () => {
  state.size = Number(pageSize.value);
  state.page = 1;
  void loadPage();
});

renderShell();
await loadPage();

async function loadPage() {
  state.loading = true;
  renderLoading();

  try {
    const response = await fetch(`/${state.view}?page=${state.page}&size=${state.size}`, {
      headers: { Accept: "application/json" }
    });
    if (!response.ok) {
      const body = await response.text();
      throw new Error(body || `Request failed with status ${response.status}`);
    }

    const page = await response.json();
    state.totalPages = page.totalPages;
    state.totalElements = page.totalElements;
    renderPage(page);
  } catch (error) {
    renderError(error);
  } finally {
    state.loading = false;
    renderControls();
  }
}

function renderShell() {
  viewButtons.forEach((button) => {
    button.classList.toggle("is-active", button.dataset.view === state.view);
  });
  tableHead.innerHTML = state.view === "broadcasts"
    ? "<tr><th class=\"time-column\">Time</th><th class=\"channel-column\">Channel</th><th>Artist</th><th>Title</th><th>Album</th></tr>"
    : "<tr><th>Artist</th><th>Title</th><th>Album</th></tr>";
  tableBody.innerHTML = "";
  message.textContent = "";
  message.classList.remove("is-error");
  renderControls();
}

function renderLoading() {
  message.textContent = `Loading ${state.view}...`;
  message.classList.remove("is-error");
  tableBody.innerHTML = "";
  renderControls();
}

function renderPage(page) {
  const items = page.items || [];
  if (items.length === 0) {
    message.textContent = `No ${state.view} found.`;
    tableBody.innerHTML = "";
  } else {
    message.textContent = "";
    tableBody.innerHTML = items.map(state.view === "broadcasts" ? broadcastRow : songRow).join("");
  }

  summary.textContent = summarize(page);
  renderControls();
}

function renderError(error) {
  state.totalPages = 0;
  state.totalElements = 0;
  message.textContent = error.message;
  message.classList.add("is-error");
  summary.textContent = `Could not load ${state.view}.`;
  tableBody.innerHTML = "";
}

function renderControls() {
  const totalPages = state.totalPages;
  previousPage.disabled = state.loading || state.page <= 1;
  nextPage.disabled = state.loading || totalPages === 0 || state.page >= totalPages;
  pageStatus.textContent = totalPages === 0 ? "Page 0 of 0" : `Page ${state.page} of ${totalPages}`;
}

function summarize(page) {
  const label = state.view === "broadcasts" ? "broadcasts" : "songs";
  if (page.totalElements === 0) return `No ${label} stored yet.`;
  return `${page.totalElements} ${label}, ${page.totalPages} pages`;
}

function broadcastRow(item) {
  return `<tr>
    <td class="time-column">${escapeHtml(formatBroadcastTime(item.time))}</td>
    <td class="channel-column">${escapeHtml(item.channel)}</td>
    <td class="text-column">${escapeHtml(item.song?.artist)}</td>
    <td class="text-column">${escapeHtml(item.song?.title)}</td>
    <td class="text-column muted">${escapeHtml(item.song?.album)}</td>
  </tr>`;
}

function songRow(item) {
  return `<tr>
    <td class="text-column">${escapeHtml(item.artist)}</td>
    <td class="text-column">${escapeHtml(item.title)}</td>
    <td class="text-column muted">${escapeHtml(item.album)}</td>
  </tr>`;
}

function formatBroadcastTime(value) {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  const year = date.getFullYear();
  const month = padDatePart(date.getMonth() + 1);
  const day = padDatePart(date.getDate());
  const hours = padDatePart(date.getHours());
  const minutes = padDatePart(date.getMinutes());
  const seconds = padDatePart(date.getSeconds());
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

function padDatePart(value) {
  return String(value).padStart(2, "0");
}

function escapeHtml(value) {
  if (value === null || value === undefined || value === "") return "";
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}
