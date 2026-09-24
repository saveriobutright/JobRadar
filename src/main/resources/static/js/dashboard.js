const PAGE_SIZE = 12;

const form = document.querySelector("#job-filters");
const queryInput = document.querySelector("#query");
const locationInput = document.querySelector("#location");
const remoteInput = document.querySelector("#remote");
const sortInput = document.querySelector("#sort");
const clearButton = document.querySelector("#clear-filters");

const jobList = document.querySelector("#job-list");
const cardTemplate = document.querySelector("#job-card-template");

const loadingState = document.querySelector("#loading-state");
const errorState = document.querySelector("#error-state");
const emptyState = document.querySelector("#empty-state");

const totalJobs = document.querySelector("#total-jobs");
const topScore = document.querySelector("#top-score");
const remoteJobs = document.querySelector("#remote-jobs");
const resultSummary = document.querySelector("#result-summary");

const pagination = document.querySelector("#pagination");
const previousButton = document.querySelector("#previous-page");
const nextButton = document.querySelector("#next-page");
const pageIndicator = document.querySelector("#page-indicator");

const dateFormatter = new Intl.DateTimeFormat(
    "en",
    {
        dateStyle: "medium"
    }
);

const acronymLabels = new Map([
    ["ai", "AI"],
    ["aws", "AWS"],
    ["sql", "SQL"],
    ["api", "API"]
]);

let currentPage = 1;
let activeRequest;

restoreFiltersFromUrl();
loadJobs();

form.addEventListener("submit", event => {
    event.preventDefault();
    currentPage = 1;
    loadJobs();
});

clearButton.addEventListener("click", () => {
    form.reset();
    currentPage = 1;
    loadJobs();
});

previousButton.addEventListener("click", () => {
    if (currentPage > 1) {
        currentPage -= 1;
        loadJobs(true);
    }
});

nextButton.addEventListener("click", () => {
    currentPage += 1;
    loadJobs(true);
});

async function loadJobs(scrollToResults = false) {
    activeRequest?.abort();
    activeRequest = new AbortController();

    showLoading();
    updateBrowserUrl();

    try {
        const response = await fetch(
            buildApiUrl(),
            {
                signal: activeRequest.signal,
                headers: {
                    Accept: "application/json"
                }
            }
        );

        if (!response.ok) {
            throw new Error(
                await readProblemDetail(response)
            );
        }

        const data = await response.json();
        renderJobs(data);

        if (scrollToResults) {
            document.querySelector("#results-heading")
                .scrollIntoView({
                    behavior: "smooth",
                    block: "start"
                });
        }
    } catch (error) {
        if (error.name !== "AbortError") {
            showError(error.message);
        }
    }
}

function buildApiUrl() {
    const parameters = buildSearchParameters();
    return `/api/jobs?${parameters.toString()}`;
}

function buildSearchParameters() {
    const parameters = new URLSearchParams({
        page: currentPage.toString(),
        size: PAGE_SIZE.toString(),
        sort: sortInput.value
    });

    addOptionalParameter(
        parameters,
        "query",
        queryInput.value
    );

    addOptionalParameter(
        parameters,
        "location",
        locationInput.value
    );

    addOptionalParameter(
        parameters,
        "remote",
        remoteInput.value
    );

    return parameters;
}

function addOptionalParameter(
    parameters,
    name,
    value
) {
    const normalizedValue = value.trim();

    if (normalizedValue) {
        parameters.set(name, normalizedValue);
    }
}

function renderJobs(data) {
    jobList.replaceChildren();
    jobList.setAttribute("aria-busy", "false");

    loadingState.hidden = true;
    errorState.hidden = true;

    updateSummary(data);

    if (data.items.length === 0) {
        emptyState.hidden = false;
        pagination.hidden = true;
        return;
    }

    emptyState.hidden = true;

    data.items.forEach(job => {
        jobList.append(createJobCard(job));
    });

    updatePagination(data);
}

function createJobCard(job) {
    const card = cardTemplate.content
        .firstElementChild
        .cloneNode(true);

    setText(
        card,
        ".job-company",
        job.company || "Company unavailable"
    );

    card.href = job.sourceUrl;

    card.setAttribute(
        "aria-label",
        `Open ${job.title} at ${job.company}`
    );

    setText(
        card,
        ".job-title",
        job.title
    );

    setText(
        card,
        ".score-value",
        job.relevance.score.toString()
    );

    setText(
        card,
        ".job-location",
        job.location || "Location unavailable"
    );

    setText(
        card,
        ".job-remote",
        job.remote ? "Remote" : "Onsite / hybrid"
    );

    setText(
        card,
        ".job-types",
        formatJobTypes(job.jobTypes)
    );

    setText(
        card,
        ".job-description",
        job.description
        || "No description was provided by the source."
    );

    renderMatchedSignals(card, job.relevance);

    setText(
        card,
        ".job-date",
        formatPostedDate(job.postedAt)
    );

    setText(
        card,
        ".job-source",
        `Source: ${job.source}`
    );

    return card;
}

function renderMatchedSignals(card, relevance) {
    const matchSection =
        card.querySelector(".match-section");

    const matchList =
        card.querySelector(".match-list");

    const signals = [
        ...relevance.matchedRoles,
        ...relevance.matchedSkills,
        ...relevance.matchedJobTypes
    ];

    if (signals.length === 0) {
        matchSection.hidden = true;
        return;
    }

    [...new Set(signals)].forEach(signal => {
        const chip = document.createElement("span");
        chip.className = "signal-chip";
        chip.textContent = formatSignal(signal);
        matchList.append(chip);
    });
}

function updateSummary(data) {
    totalJobs.textContent =
        data.totalItems.toLocaleString("en");

    const scores = data.items.map(
        job => job.relevance.score
    );

    topScore.textContent = scores.length === 0
        ? "—"
        : `${Math.max(...scores)}/100`;

    remoteJobs.textContent = data.items
        .filter(job => job.remote)
        .length
        .toString();

    resultSummary.textContent =
        data.totalItems === 0
            ? "No matching jobs"
            : `${data.totalItems.toLocaleString("en")} jobs`
            + ` · Page ${data.page}`
            + ` of ${data.totalPages}`;
}

function updatePagination(data) {
    pagination.hidden = data.totalPages <= 1;

    previousButton.disabled = data.page <= 1;
    nextButton.disabled =
        data.page >= data.totalPages;

    pageIndicator.textContent =
        `Page ${data.page} of ${data.totalPages}`;
}

function showLoading() {
    loadingState.hidden = false;
    errorState.hidden = true;
    emptyState.hidden = true;
    pagination.hidden = true;

    jobList.replaceChildren();
    jobList.setAttribute("aria-busy", "true");

    resultSummary.textContent = "Loading jobs…";
}

function showError(message) {
    loadingState.hidden = true;
    emptyState.hidden = true;
    pagination.hidden = true;
    jobList.setAttribute("aria-busy", "false");

    const detail = errorState.querySelector("span");
    detail.textContent = message;

    errorState.hidden = false;
    resultSummary.textContent = "Request failed";
}

async function readProblemDetail(response) {
    try {
        const problem = await response.json();

        if (problem.detail) {
            return problem.detail;
        }
    } catch {
        // The server did not return a JSON problem response.
    }

    return `Request failed with status ${response.status}.`;
}

function updateBrowserUrl() {
    const parameters = buildSearchParameters();

    if (currentPage === 1) {
        parameters.delete("page");
    }

    parameters.delete("size");

    const queryString = parameters.toString();

    window.history.replaceState(
        null,
        "",
        queryString ? `/?${queryString}` : "/"
    );
}

function restoreFiltersFromUrl() {
    const parameters =
        new URLSearchParams(window.location.search);

    queryInput.value =
        parameters.get("query") || "";

    locationInput.value =
        parameters.get("location") || "";

    const remote = parameters.get("remote");

    if (remote === "true" || remote === "false") {
        remoteInput.value = remote;
    }

    const sort = parameters.get("sort");

    if (sort === "newest" || sort === "relevance") {
        sortInput.value = sort;
    }

    const page = Number(parameters.get("page"));

    if (Number.isInteger(page) && page > 0) {
        currentPage = page;
    }
}

function formatJobTypes(jobTypes) {
    if (!jobTypes || jobTypes.length === 0) {
        return "Type unavailable";
    }

    return jobTypes.join(" · ");
}

function formatPostedDate(value) {
    if (!value) {
        return "Date unavailable";
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return "Date unavailable";
    }

    return `Posted ${dateFormatter.format(date)}`;
}

function formatSignal(value) {
    return value
        .split(" ")
        .map(word => {
            const normalizedWord =
                word.toLowerCase();

            return acronymLabels.get(normalizedWord)
                || normalizedWord.charAt(0).toUpperCase()
                + normalizedWord.slice(1);
        })
        .join(" ");
}

function setText(parent, selector, value) {
    parent.querySelector(selector).textContent = value;
}
