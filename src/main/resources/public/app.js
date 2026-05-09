const state = {
  view: "broadcasts",
  page: 1,
  size: 50,
  totalPages: 0,
  totalElements: 0,
  loading: false,
  previousList: null
};

const viewButtons = [...document.querySelectorAll(".view-button")];
const summary = document.querySelector("#summary");
const toolbar = document.querySelector(".toolbar");
const message = document.querySelector("#message");
const firstPage = document.querySelector("#first-page");
const previousPage = document.querySelector("#previous-page");
const nextPage = document.querySelector("#next-page");
const lastPage = document.querySelector("#last-page");
const pageStatus = document.querySelector("#page-status");
const pageJumpForm = document.querySelector("#page-jump-form");
const pageJump = document.querySelector("#page-jump");
const goPage = document.querySelector("#go-page");
const pageSize = document.querySelector("#page-size");
const tableShell = document.querySelector(".table-shell");
let tableHead = document.querySelector("#table-head");
let tableBody = document.querySelector("#table-body");

viewButtons.forEach((button) => {
  button.addEventListener("click", () => {
    showList(button.dataset.view);
  });
});

firstPage.addEventListener("click", () => {
  goToPage(1);
});

previousPage.addEventListener("click", () => {
  goToPage(state.page - 1);
});

nextPage.addEventListener("click", () => {
  goToPage(state.page + 1);
});

lastPage.addEventListener("click", () => {
  goToPage(state.totalPages);
});

pageJumpForm.addEventListener("submit", (event) => {
  event.preventDefault();
  goToPage(Number(pageJump.value));
});

pageSize.addEventListener("change", () => {
  state.size = Number(pageSize.value);
  state.page = 1;
  void loadPage();
});

window.addEventListener("hashchange", () => {
  void route();
});

await route();

async function route() {
  const songId = songIdFromHash();
  if (songId !== null) {
    await loadSong(songId);
    return;
  }

  if (state.view === "song") {
    state.view = "broadcasts";
    state.page = 1;
  }
  renderShell();
  await loadPage();
}

function showList(view) {
  state.view = view;
  state.page = 1;
  state.totalPages = 0;
  state.totalElements = 0;
  state.previousList = null;
  if (window.location.hash) {
    history.pushState(null, "", window.location.pathname + window.location.search);
  }
  renderShell();
  void loadPage();
}

function songIdFromHash() {
  const match = window.location.hash.match(/^#\/songs\/(\d+)$/);
  return match ? Number(match[1]) : null;
}

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

async function loadSong(songId) {
  state.loading = true;
  state.previousList ??= { view: state.view === "song" ? "broadcasts" : state.view, page: state.page };
  state.view = "song";
  renderSongLoading();

  try {
    const response = await fetch(`/songs/${songId}`, {
      headers: { Accept: "application/json" }
    });
    if (!response.ok) {
      const body = await response.text();
      throw new Error(body || `Request failed with status ${response.status}`);
    }

    const song = await response.json();
    renderSong(song);
  } catch (error) {
    renderSongError(error);
  } finally {
    state.loading = false;
    renderControls();
  }
}

function goToPage(page) {
  if (state.loading || state.totalPages === 0) return;

  const targetPage = clampPage(page);
  if (targetPage === state.page) {
    pageJump.value = String(state.page);
    return;
  }

  state.page = targetPage;
  void loadPage();
}

function clampPage(page) {
  if (!Number.isFinite(page)) return state.page;
  const pageNumber = Math.trunc(page);
  return Math.min(Math.max(pageNumber, 1), state.totalPages);
}

function renderShell() {
  viewButtons.forEach((button) => {
    button.classList.toggle("is-active", button.dataset.view === state.view);
  });
  toolbar.hidden = false;
  restoreListTable();
  clearTableCaption();
  tableHead.innerHTML = state.view === "broadcasts"
    ? "<tr><th class=\"time-column\">Time</th><th class=\"channel-column\">Channel</th><th>Artist</th><th>Title</th><th>Album</th><th class=\"action-column\"><span class=\"visually-hidden\">Actions</span></th></tr>"
    : "<tr><th>Artist</th><th>Title</th><th>Album</th><th class=\"action-column\"><span class=\"visually-hidden\">Actions</span></th></tr>";
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

function renderSongLoading() {
  viewButtons.forEach((button) => {
    button.classList.toggle("is-active", button.dataset.view === "songs");
  });
  toolbar.hidden = true;
  summary.textContent = "Loading song...";
  message.textContent = "Loading song...";
  message.classList.remove("is-error");
  restoreListTable();
  clearTableCaption();
  tableHead.innerHTML = "<tr><th class=\"time-column\">Time</th><th class=\"channel-column\">Channel</th></tr>";
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

function renderSong(song) {
  summary.textContent = `${song.broadcasts?.length || 0} broadcasts for ${song.title || "song"}`;
  message.classList.remove("is-error");
  message.innerHTML = `<button type="button" class="back-button" id="back-to-list">Back</button>`;
  document.querySelector("#back-to-list").addEventListener("click", backToList);
  const broadcasts = song.broadcasts || [];
  clearTableCaption();
  tableShell.classList.add("is-detail");
  tableShell.innerHTML = `<div class="table-card">
    <table>
      <caption class="table-caption">Song details</caption>
      <thead>
        <tr><th>Artist</th><th>Title</th><th>Album</th></tr>
      </thead>
      <tbody>
        <tr>
          <td class="text-column">${escapeHtml(song.artist)}</td>
          <td class="text-column">${escapeHtml(song.title)}</td>
          <td class="text-column muted">${escapeHtml(song.album)}</td>
        </tr>
      </tbody>
    </table>
  </div>
  <div class="table-card">
    <table>
      <caption class="table-caption">Broadcasts (${broadcasts.length})</caption>
      <thead>
        <tr><th class="time-column">Time</th><th class="channel-column">Channel</th></tr>
      </thead>
      <tbody>
        ${broadcasts.length === 0
          ? "<tr><td colspan=\"2\" class=\"muted\">No broadcasts found.</td></tr>"
          : broadcasts.map(songBroadcastRow).join("")}
      </tbody>
    </table>
  </div>`;
}

function renderSongError(error) {
  summary.textContent = "Could not load song.";
  message.textContent = error.message;
  message.classList.add("is-error");
  restoreListTable();
  clearTableCaption();
  tableHead.innerHTML = "<tr><th class=\"time-column\">Time</th><th class=\"channel-column\">Channel</th></tr>";
  tableBody.innerHTML = "";
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
  if (state.view === "song") {
    toolbar.hidden = true;
    return;
  }
  toolbar.hidden = false;
  const totalPages = state.totalPages;
  const atFirstPage = state.page <= 1;
  const atLastPage = totalPages === 0 || state.page >= totalPages;
  firstPage.disabled = state.loading || atFirstPage;
  previousPage.disabled = state.loading || state.page <= 1;
  nextPage.disabled = state.loading || atLastPage;
  lastPage.disabled = state.loading || atLastPage;
  pageJump.disabled = state.loading || totalPages === 0;
  goPage.disabled = state.loading || totalPages === 0;
  pageJump.max = totalPages === 0 ? "" : String(totalPages);
  pageJump.value = totalPages === 0 ? "" : String(state.page);
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
    <td class="action-column">${songAction(item.song?.id)}</td>
  </tr>`;
}

function songRow(item) {
  return `<tr>
    <td class="text-column">${escapeHtml(item.artist)}</td>
    <td class="text-column">${escapeHtml(item.title)}</td>
    <td class="text-column muted">${escapeHtml(item.album)}</td>
    <td class="action-column">${songAction(item.id)}</td>
  </tr>`;
}

function songBroadcastRow(item) {
  return `<tr>
    <td class="time-column">${escapeHtml(formatBroadcastTime(item.time))}</td>
    <td class="channel-column">${escapeHtml(item.channel)}</td>
  </tr>`;
}

function songAction(songId) {
  if (songId === null || songId === undefined) return "";
  return `<a class="icon-link" href="#/songs/${encodeURIComponent(songId)}" aria-label="View song" title="View song">›</a>`;
}

function backToList() {
  const previousList = state.previousList || { view: "broadcasts", page: 1 };
  state.previousList = null;
  state.view = previousList.view;
  state.page = previousList.page;
  history.pushState(null, "", window.location.pathname + window.location.search);
  renderShell();
  void loadPage();
}

function clearTableCaption() {
  document.querySelector(".table-caption")?.remove();
}

function restoreListTable() {
  tableShell.classList.remove("is-detail");
  if (document.querySelector("#table-head") && document.querySelector("#table-body")) return;
  tableShell.innerHTML = `<table>
    <thead id="table-head"></thead>
    <tbody id="table-body"></tbody>
  </table>`;
  tableHead = document.querySelector("#table-head");
  tableBody = document.querySelector("#table-body");
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
